/**
 * Copyright (C) 2014 OpenTravel Alliance (info@opentravel.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opentravel.schemas.node.handlers.children;

import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFactory;
import org.opentravel.schemas.node.interfaces.ContextualFacetOwnerInterface;
import org.opentravel.schemas.node.interfaces.FacetInterface;
import org.opentravel.schemas.node.interfaces.InheritedInterface;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.objectMembers.ContributedFacetNode;
import org.opentravel.schemas.node.properties.EnumLiteralNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.typeProviders.ContextualFacetNode;
import org.opentravel.schemas.node.typeProviders.FacetProviderNode;
import org.opentravel.schemas.node.typeProviders.InheritedContextualFacetNode;
import org.opentravel.schemas.node.typeProviders.TypeProviders;
import org.opentravel.schemas.trees.library.LibraryTreeContentProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Keeps a list of children and inherited children nodes for each parent. List is lazy created and disposed of (cleared)
 * on changes. The TL owner is the authority. Handles access to children, inherited children and various filtered lists
 * of children.
 * <p>
 * No change methods are present by design. These lists are only caches. Changes must be made to the TL model and clear
 * the children handler to effect change.
 * <p>
 * <b>Clear the handler</b> whenever the <b>TL Model Element</b> changes. This clears the children and inherited. Event
 * handlers clear children, but events are not always thrown so it <b>must</b> also be done explicitly when changing a
 * TL impacts its parent.
 * <p>
 * Children that are cleared have the TLModelObject removed and are marked as deleted. However,
 * InheritanceDependencyListers can not be moved because we may be inside an event and will cause a co-modification
 * exception. To remove these listeners, inherited children are cleared until retrieved and the first child is found to
 * be deleted.
 * 
 * @author Dave Hollander
 * 
 */
public abstract class CachingChildrenHandler<C extends Node, O extends Node> extends NodeChildrenHandler<C> {
    private final static Logger LOGGER = LoggerFactory.getLogger( CachingChildrenHandler.class );

    protected O owner = null;

    public CachingChildrenHandler(O owner) {
        this.owner = owner;
    }

    @Override
    public void clear() {
        if (!initRunning) {
            // FIXME - when running green, try not clearing children list
            // clearList(children); // let node remain and reconnect when re-creating children
            clearList( inherited );
            children = null;
            // inherited = null;
        }
    }

    @Override
    public void clear(Node item) {
        clear();
    }

    @Override
    public void remove(C item) {
        clear();
    }

    private void clearList(List<C> iKids) {
        if (iKids == null)
            return;
        for (Node n : iKids)
            n.setTlModelObject( null ); // Remove tl object
        if (iKids.isEmpty())
            inherited = null;
    }

    @Override
    public List<C> get() {
        if (owner.isDeleted()) {
            // LOGGER.debug("Trying to get children from deleted owner: " + owner);
            return Collections.emptyList();
        }
        if (children == null)
            initChildren();
        return children;
    }

    @Override
    public List<C> getInheritedChildren() {
        // If an inherited child is deleted, is was cleared.
        // Remove any obsolete listeners the clear the list.
        // if (inherited != null && !inherited.isEmpty())
        // if (inherited.get(0).isDeleted()) {
        // clearInheritedListeners(inherited);
        // inherited = null;
        // }
        //
        // if (inherited == null)
        // 11/28/2017 - for now, don't cache inherited
        if (inherited != null) {
            // Should not be needed if setter is picks up on deleted flag
            // clearInheritedListeners(inherited);
            for (Node n : inherited)
                // Don't delete node inherited from version extensions
                if (n instanceof InheritedInterface)
                    n.setDeleted( true );
            // Mark as deleted
            inherited.clear();
            // LOGGER.debug("Cleared inherited children of " + owner);
        }
        initInherited();
        return (List<C>) (inherited != null ? inherited : Collections.emptyList());
    }

    /**
     * Get all children to be presented in navigator tree. {@link LibraryTreeContentProvider}
     * <p>
     * Get all immediate navChildren and where-used nodes to be presented in the OTM Object Tree. . Overridden on nodes
     * that add nodes such as where used to the tree view.
     * 
     * @see {@link #getNavChildren()}
     * 
     * @param deep - include properties
     * 
     * @return new list
     */
    @Override
    public List<C> getTreeChildren(boolean deep) {
        List<C> navChildren = getNavChildren( deep );
        // Done by navChildren - navChildren.addAll(getInheritedChildren());
        if (owner instanceof TypeProviders && ((TypeProviders) owner).getWhereUsedCount() > 0)
            navChildren.add( (C) ((TypeProviders) owner).getWhereUsedNode() );
        return navChildren;
    }

    // Override if inheritance is supported
    @Override
    public List<TLModelElement> getInheritedChildren_TL() {
        return Collections.emptyList();
    }

    // Override if inheritance is supported
    // @Override
    public Node getInheritedOwner_TL() {
        return null;
    }

    @SuppressWarnings("unchecked")
    private C getOrModel(TLModelElement t) {
        C node = null;
        ComponentNode cnode = null;
        if (t == null)
            return node;

        // See if there is an identity listener on the TL Element.
        Node n = Node.GetNode( t );

        if (!(n instanceof ComponentNode))
            // Was not modeled or was not a component, so model it.
            cnode = NodeFactory.newChild( owner, t );
        else {
            cnode = (ComponentNode) n;
            if (cnode.isDeleted()) {
                // happens on close()
                // LOGGER.warn("ignoring a deleted node");
                cnode = null;
            }
            // If it is a contextual facet return the contributed facet
            if (cnode instanceof ContextualFacetNode)
                if (((ContextualFacetNode) cnode).getWhereContributed() != null)
                    cnode = ((ContextualFacetNode) cnode).getWhereContributed();
                else {
                    if (cnode instanceof InheritedInterface)
                        LOGGER.warn( "Trying to use a pre-modeled contextual facet but where contributed is null." );
                    cnode = new ContributedFacetNode( (TLContextualFacet) t, (ContextualFacetOwnerInterface) owner );
                }
        }
        if (cnode != null)
            node = (C) cnode;
        return node;
    }

    /**
     * Create children array. Get list of TL children and model their associated nodes.
     * <p>
     * Only override when node children are not directly created from list of TL children.
     */
    protected void initChildren() {
        // prevent adding new node to parent from clearing the children
        initRunning = true;
        children = modelTLs( getChildren_TL(), null );
        initRunning = false;
    }

    /**
     * Done - MUST be overridden for objects that have inherited children
     */
    protected void initInherited() {
        initRunning = true;

        // Facets can inherit from ExtendsType or Version
        if (owner instanceof FacetProviderNode)
            if (((FacetProviderNode) owner).getVersionBase() != owner)
                inheritedOwner = (Node) ((FacetProviderNode) owner).getVersionBase();

        // If not from version, then try extension
        if (inheritedOwner == null)
            inheritedOwner = owner.getExtendsType();

        if (inheritedOwner != null)
            inherited = modelTLs( getInheritedChildren_TL(), inheritedOwner );
        else
            inherited = Collections.emptyList();
        initRunning = false;
    }

    /**
     * Associate nodes with a TLModelElement. if the node has not been previously created as defined by a
     * NodeIdentityListener then create a new node.
     * 
     * @param list
     * @param base when not null, the base is the base node from which the children in the list were inherited from.
     * @return
     */
    @SuppressWarnings("unchecked")
    protected List<C> modelTLs(List<TLModelElement> list, Node base) {
        assert base != owner;
        C node = null;
        List<C> kids = new ArrayList<>();
        for (TLModelElement tlSrc : list) {

            node = getOrModel( tlSrc );
            if (node == null || !(node instanceof ComponentNode))
                continue;
            if (node.isDeleted()) {
                // LOGGER.debug("getOrModel() return deleted node." + node);
                continue;
            }
            // ComponentNode cnode = (ComponentNode) node;

            if (base != null && !base.isDeleted()) {
                // These are inherited children..."ghost" nodes made from "ghost" tl objects.
                // This could be a minor version of an extended object. Property could come from either the
                // version chain OR base
                if (node instanceof PropertyNode) {
                    if (node.getParent() == owner) {
                        assert base instanceof FacetInterface;
                        if (!(node instanceof EnumLiteralNode))
                            LOGGER.warn( "TLObject reports being owned by owner." ); // Enums do
                        // Try to find the actual base. A facet that has a child by the same name.
                        TLModelElement baseTL = null;
                        PropertyNode baseProp = (PropertyNode) base.findChildByName( node.getName() );
                        if (baseProp == null) {
                            LOGGER.warn( "No base property to inherit from." );
                            // May have to examine peers in this chain
                            LibraryChainNode chain = owner.getChain();
                        } else
                            baseTL = baseProp.getTLModelObject();

                        TLModelElement thisTL = node.getTLModelObject();
                        assert thisTL == baseTL; // this is why we need a facade
                        node.setParent( base ); // will this work without changing TL?
                    }
                    // build a new facade node to represent the inherited node complete with listeners
                    assert node.getParent() instanceof FacetInterface;
                    assert node.getParent() != owner;
                    node = (C) NodeFactory.newInheritedProperty( (PropertyNode) node, (FacetInterface) owner );
                } else if (node instanceof ContextualFacetNode) {
                    LOGGER.error( "Creating inherited contextual facet" );
                    assert false;
                } else if (node instanceof ContributedFacetNode) {
                    // LOGGER.debug("Handling contributed facet: " + node);
                    assert base instanceof ContextualFacetOwnerInterface;
                    assert tlSrc instanceof TLContextualFacet;
                    assert owner instanceof ContextualFacetOwnerInterface;

                    // To make ready to add to children list
                    // Make an inherited contextual facet and its associated contributed facet to finish modeling node
                    assert node.getTLModelObject() == tlSrc;
                    InheritedContextualFacetNode icf = NodeFactory
                        .newInheritedFacet( (ContextualFacetOwnerInterface) base, (ContributedFacetNode) node );
                    node = (C) icf.getWhereContributed(); // get the inherited contributed facet
                    // LOGGER.debug("Created inherited contextual facet " + icf + " from contributed facet.");

                } else {
                    LOGGER.error( "Unhandled ghost object: " + node );
                    assert false;
                }
            }
            // Add result to kids
            kids.add( node );

            if (base != null && !base.isDeleted())
                assert node instanceof InheritedInterface;
        }
        return kids;
    }

    @Override
    public String toString() {
        return owner.getName() + "_ChildrenHandler";
    }

}
