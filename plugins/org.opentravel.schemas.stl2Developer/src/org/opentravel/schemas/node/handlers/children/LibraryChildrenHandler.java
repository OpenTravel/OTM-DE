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

import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.XSDComplexType;
import org.opentravel.schemacompiler.model.XSDElement;
import org.opentravel.schemacompiler.model.XSDSimpleType;
import org.opentravel.schemas.node.AggregateNode;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.NavNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFactory;
import org.opentravel.schemas.node.ServiceNode;
import org.opentravel.schemas.node.VersionAggregateNode;
import org.opentravel.schemas.node.handlers.XsdObjectHandler;
import org.opentravel.schemas.node.interfaces.ComplexMemberInterface;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.interfaces.SimpleMemberInterface;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.resources.ResourceNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Children handler for all Abstract Libraries.
 * <p>
 * Decided to have one handler with 3 library types to keep similar code together.
 * 
 * Provide an interface to the TLValueWithAttributes model object. TLValueWithAttributes does not use facets to contain
 * simple type and attributes, so this model class must adapt.
 * 
 * @author Dave Hollander
 * 
 */
public class LibraryChildrenHandler extends StaticChildrenHandler<Node,LibraryNode> {
    private final static Logger LOGGER = LoggerFactory.getLogger( LibraryChildrenHandler.class );

    protected NavNode complexRoot;
    protected NavNode simpleRoot;
    protected NavNode serviceRoot;
    protected NavNode resourceRoot;

    public LibraryChildrenHandler(final LibraryNode lib) {
        super( lib );
        complexRoot = new NavNode( LibraryNode.COMPLEX_OBJECTS, owner );
        simpleRoot = new NavNode( LibraryNode.SIMPLE_OBJECTS, owner );
        resourceRoot = new NavNode( LibraryNode.RESOURCES, owner );
        serviceRoot = new NavNode( LibraryNode.SERVICES, owner );
        initChildren();
        assert owner.getTLModelObject() instanceof AbstractLibrary;
    }

    @Override
    public boolean contains(Node member) {
        if (member instanceof LibraryMemberInterface) {
            if (complexRoot != null && complexRoot.contains( member ))
                return true;
            if (simpleRoot != null && simpleRoot.contains( member ))
                return true;
            if (serviceRoot != null && serviceRoot.contains( member ))
                return true;
            if (resourceRoot != null && resourceRoot.contains( member ))
                return true;
        }
        return false;
    }

    @Override
    public void clear(Node item) {
        children.remove( item );
        if (item == getComplexRoot())
            complexRoot = null;
        else if (item == getSimpleRoot())
            simpleRoot = null;
        else if (item == getResourceRoot())
            resourceRoot = null;
        else if (item == getServiceRoot())
            serviceRoot = null;
    }

    /**
     * @return the complexRoot
     */
    public NavNode getComplexRoot() {
        return complexRoot;
    }

    /**
     * @return the simpleRoot
     */
    public NavNode getSimpleRoot() {
        return simpleRoot;
    }

    /**
     * @return the serviceRoot
     */
    public NavNode getServiceRoot() {
        return serviceRoot;
    }

    /**
     * @return the resourceRoot
     */
    public NavNode getResourceRoot() {
        return resourceRoot;
    }

    @Override
    public List<TLModelElement> getChildren_TL() {
        final List<TLModelElement> kids = new ArrayList<>();
        for (final LibraryMember mbr : owner.getTLModelObject().getNamedMembers())
            kids.add( (TLModelElement) mbr );
        return kids;
    }

    // Since the TL Model and Node model are so different do the mapping here.
    @Override
    public void initChildren() {
        initRunning = true;
        children = new ArrayList<>();
        children.add( complexRoot );
        children.add( simpleRoot );
        children.add( resourceRoot );
        children.add( serviceRoot );

        // Version 1.6 and later contextual facets can be library members.
        // Do them first.
        Node n;

        if (owner.getTLModelObject() instanceof TLLibrary)
            for (TLContextualFacet cf : ((TLLibrary) owner.getTLModelObject()).getContextualFacetTypes())
                add( (Node) NodeFactory.newLibraryMember( cf ) );

        // Now get the rest of the children
        modelTLs( getChildren_TL(), null );
        initRunning = false;
    }

    @Override
    public boolean hasChildren_TypeProviders() {
        return get().size() > 0 ? true : false;
    }

    @Override
    public List<Node> getNavChildren(boolean deep) {
        if (owner.getParent() instanceof VersionAggregateNode)
            return new ArrayList<>();
        else
            return new ArrayList<>( get() );
    }

    @Override
    public boolean hasNavChildren(boolean deep) {
        if (owner.getParent() instanceof VersionAggregateNode)
            return false;
        return !get().isEmpty();
    }

    @Override
    public List<Node> getTreeChildren(boolean deep) {
        List<Node> treeKids = getNavChildren( deep );
        if (treeKids.isEmpty()) {
            treeKids = new ArrayList<>();
            if (!treeKids.contains( owner.getWhereUsedHandler().getWhereUsedNode() ))
                treeKids.add( owner.getWhereUsedHandler().getWhereUsedNode() );
            if (!treeKids.contains( owner.getWhereUsedHandler().getUsedByNode() ))
                treeKids.add( owner.getWhereUsedHandler().getUsedByNode() );
        }
        return treeKids;
    }

    @Override
    public boolean hasTreeChildren(boolean deep) {
        return true; // Include where used and uses from
    }

    protected List<Node> modelTLs(List<TLModelElement> list, Node base) {
        LibraryMemberInterface lm = null;
        List<Node> kids = new ArrayList<>();
        for (TLModelElement t : list) {
            assert t instanceof LibraryMember;
            LibraryMember mbr = (LibraryMember) t;

            // Contextual facets are either modeled earlier (v1.6) or as a child facet (v1.5)
            // Don't do them here.
            if (mbr instanceof TLContextualFacet)
                continue;

            // Factory will lookup or create a library member node
            lm = NodeFactory.newLibraryMember( mbr );
            if (lm == null) {
                if (mbr instanceof XSDSimpleType)
                    lm = new XsdObjectHandler( (XSDSimpleType) t, owner ).getOwner();
                else if (mbr instanceof XSDComplexType)
                    lm = new XsdObjectHandler( (XSDComplexType) t, owner ).getOwner();
                else if (mbr instanceof XSDElement)
                    lm = new XsdObjectHandler( (XSDElement) t, owner ).getOwner();
            }
            add( lm );
        }
        return kids;
    }

    @Override
    public void add(Node n) {
        if (n instanceof LibraryMemberInterface)
            add( (LibraryMemberInterface) n );
    }

    public void add(LibraryMemberInterface n) {
        if (n == null) {
            LOGGER.debug( "Skipping unknown library member type." );
            return;
        }

        NavNode nn = null;
        if (n instanceof ComplexMemberInterface)
            nn = complexRoot;
        else if (n instanceof SimpleMemberInterface)
            nn = simpleRoot;
        else if (n instanceof ResourceNode)
            nn = resourceRoot;
        else if (n instanceof ServiceNode)
            nn = serviceRoot;

        if (nn != null) {
            nn.add( n );
            if (owner.isInChain())
                owner.getChain().add( (ComponentNode) n );
        }
    }

    public void removeAggregate(AggregateNode n) {
        clear( n );
        // Now, update the appropriate global
        if (n == complexRoot)
            complexRoot.close();
        else if (n == simpleRoot)
            simpleRoot.close();
        else if (n == serviceRoot)
            serviceRoot.close();
        else if (n == resourceRoot)
            resourceRoot.close();
    }

    /**
     * Remove from the appropriate NavNode.
     */
    @Override
    public void remove(Node n) {
        if (n == null) {
            LOGGER.debug( "Skipping unknown library member type." );
            return;
        }
        if (n instanceof AggregateNode)
            removeAggregate( (AggregateNode) n );

        if (n instanceof ServiceNode) {
            serviceRoot = null;
            return;
        }

        NavNode nn = null;
        if (n instanceof ComplexMemberInterface)
            nn = complexRoot;
        else if (n instanceof SimpleMemberInterface)
            nn = simpleRoot;
        else if (n instanceof ResourceNode)
            nn = resourceRoot;

        if (nn != null) {
            nn.removeLM( (LibraryMemberInterface) n );
            n.setParent( nn );
            n.setLibrary( null );
        }
    }

}
