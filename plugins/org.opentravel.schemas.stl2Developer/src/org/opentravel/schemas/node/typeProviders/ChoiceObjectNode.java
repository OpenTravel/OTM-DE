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
/**
 * 
 */

package org.opentravel.schemas.node.typeProviders;

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLComplexTypeBase;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.ComponentNodeType;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeNameUtils;
import org.opentravel.schemas.node.handlers.children.ChoiceObjectChildrenHandler;
import org.opentravel.schemas.node.interfaces.AliasOwner;
import org.opentravel.schemas.node.interfaces.ContextualFacetOwnerInterface;
import org.opentravel.schemas.node.interfaces.ExtensionOwner;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.interfaces.Sortable;
import org.opentravel.schemas.node.interfaces.VersionedObjectInterface;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.objectMembers.ContributedFacetNode;
import org.opentravel.schemas.node.objectMembers.SharedFacetNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.typeProviders.facetOwners.BusinessObjectNode;
import org.opentravel.schemas.node.typeProviders.facetOwners.FacetOwners;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.types.ExtensionHandler;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Dave Hollander
 * 
 */
public class ChoiceObjectNode extends FacetOwners
    implements ExtensionOwner, Sortable, AliasOwner, ContextualFacetOwnerInterface, VersionedObjectInterface {
    private static final Logger LOGGER = LoggerFactory.getLogger( BusinessObjectNode.class );

    private ExtensionHandler extensionHandler = null;

    private LibraryNode owningLibrary;

    public ChoiceObjectNode(TLChoiceObject mbr) {
        super( mbr );

        childrenHandler = new ChoiceObjectChildrenHandler( this );
        extensionHandler = new ExtensionHandler( this );

        assert (getFacet_Shared() instanceof SharedFacetNode);
    }

    @Override
    public LibraryNode getLibrary() {
        return owningLibrary;
    }

    @Override
    public void setLibrary(LibraryNode library) {
        owningLibrary = library;
    }

    /**
     * Return true if the direct children (not inherited children) includes the candidate. If candidate is a contextual
     * facet, its matching where contributed node is used.
     */
    @Override
    public boolean contains(Node candidate) {
        if (candidate instanceof ContextualFacetNode)
            candidate = ((ContextualFacetNode) candidate).getWhereContributed();
        return getChildren() != null ? getChildren().contains( candidate ) : false;
    }

    @Override
    public void remove(AliasNode alias) {
        getTLModelObject().removeAlias( alias.getTLModelObject() );
        clearAllAliasOwners();
    }

    private void clearAllAliasOwners() {
        for (Node child : getChildren())
            if (child instanceof AliasOwner && child.getChildrenHandler() != null)
                child.getChildrenHandler().clear();
        getChildrenHandler().clear();
    }

    @Override
    public void addAlias(AliasNode alias) {
        if (!getTLModelObject().getAliases().contains( alias.getTLModelObject() ))
            getTLModelObject().addAlias( alias.getTLModelObject() );
        clearAllAliasOwners();
    }

    @Override
    public AliasNode addAlias(String name) {
        AliasNode alias = null;
        if (this.isEditable_newToChain())
            alias = new AliasNode( this, name );
        addAlias( alias );
        return alias;
    }

    @Override
    public void cloneAliases(List<AliasNode> aliases) {
        for (AliasNode a : aliases)
            addAlias( a.getName() );
    }

    /**
     * Create a new choice contextual facet and assign to this object
     * 
     * @param name
     * @return
     */
    public AbstractContextualFacet addFacet(String name) {
        return super.addFacet( name, TLFacetType.CHOICE );
    }

    @Override
    public boolean canOwn(AbstractContextualFacet targetCF) {
        return canOwn( targetCF.getTLModelObject().getFacetType() );
    }

    @Override
    public boolean canOwn(TLFacetType type) {
        switch (type) {
            case SHARED:
            case CHOICE:
                return true;
            default:
                return false;
        }
    }

    @Override
    public ComponentNode createMinorVersionComponent() {
        TLChoiceObject tlMinor = (TLChoiceObject) createMinorTLVersion( this );
        if (tlMinor != null)
            return super.createMinorVersionComponent( new ChoiceObjectNode( tlMinor ) );
        return null;
    }

    @Override
    public void delete() {
        // Must delete the contextual facets separately because they are separate library members.
        for (Node n : getChildren_New())
            if (n instanceof ContextualFacetNode)
                n.delete();
        super.delete();
    }

    @Override
    public INode.CommandType getAddCommand() {
        return INode.CommandType.PROPERTY;
    }

    @Override
    public List<AliasNode> getAliases() {
        List<AliasNode> aliases = new ArrayList<>();
        for (Node c : getChildren())
            if (c instanceof AliasNode)
                aliases.add( (AliasNode) c );
        return aliases;
    }

    /**
     * @param includeInherited add inherited facets to the list
     * @return new list of choice facets
     */
    public List<AbstractContextualFacet> getChoiceFacets(boolean includeInherited) {
        ArrayList<AbstractContextualFacet> ret = new ArrayList<>();
        for (Node f : getChildrenHandler().get()) {
            if (f instanceof ContributedFacetNode)
                f = ((ContributedFacetNode) f).getContributor();
            if (f instanceof ChoiceFacetNode)
                ret.add( (ChoiceFacetNode) f );
        }

        if (includeInherited)
            for (INode f : getChildrenHandler().getInheritedChildren()) {
                if (f instanceof ContributedFacetNode)
                    f = ((ContributedFacetNode) f).getContributor();
                if (f instanceof ChoiceFacetNode)
                    ret.add( (ChoiceFacetNode) f );
            }
        return ret;
    }

    /**
     * @return choice facets without inherited
     */
    public List<AbstractContextualFacet> getChoiceFacets() {
        return (getChoiceFacets( false ));
    }

    @Override
    public ComponentNodeType getComponentNodeType() {
        return ComponentNodeType.CHOICE;
    }

    @Override
    public ContributedFacetNode getContributedFacet(TLContextualFacet tlcf) {
        ContributedFacetNode cfn = null;
        for (TLModelElement tlo : getChildrenHandler().getChildren_TL())
            if (tlo == tlcf)
                if (Node.GetNode( tlo ) instanceof ContextualFacetNode) {
                    ContextualFacetNode cxn = (ContextualFacetNode) Node.GetNode( tlo );
                    if (cxn != null) {
                        cfn = cxn.getWhereContributed();
                        break;
                    }
                }

        return cfn;
    }

    @Override
    public String getExtendsTypeNS() {
        return getExtensionBase() != null ? getExtensionBase().getNamespace() : "";
    }

    // /////////////////////////////////////////////////////////////////
    //
    // Extension Owner implementations
    //
    @Override
    public Node getExtensionBase() {
        return extensionHandler != null ? extensionHandler.get() : null;
    }

    @Override
    public ExtensionHandler getExtensionHandler() {
        return extensionHandler;
    }

    @Override
    public SharedFacetNode getFacet_Default() {
        return getFacet_Shared();
    }

    @Override
    public Image getImage() {
        return Images.getImageRegistry().get( Images.ChoiceObject );
    }

    @Override
    public String getName() {
        return getTLModelObject().getName();
    }

    @Override
    public TLChoiceObject getTLModelObject() {
        return (TLChoiceObject) tlObj;
    }

    @Override
    public boolean isAssignableToElementRef() {
        return false;
    }

    @Override
    public boolean isAssignableToSimple() {
        return false;
    }

    @Override
    public boolean isAssignableToVWA() {
        return false;
    }

    @Override
    public boolean isAssignedByReference() {
        return true;
    }

    @Override
    public boolean isExtensibleObject() {
        return true;
    }

    @Override
    public boolean isMergeSupported() {
        return false;
    }

    @Override
    public void merge(Node source) {
        if (!(source instanceof ChoiceObjectNode)) {
            throw new IllegalStateException( "Can not merge choice objects." );
        }
    }

    @Override
    public Node setExtensible(boolean extensible) {
        if (isEditable_newToChain())
            if (getTLModelObject() instanceof TLComplexTypeBase)
                ((TLComplexTypeBase) getTLModelObject()).setNotExtendable( !extensible );
        return this;
    }

    @Override
    public void setExtension(final Node base) {
        if (extensionHandler == null)
            extensionHandler = new ExtensionHandler( this );
        extensionHandler.set( base );
    }

    @Override
    public void setName(String name) {
        name = NodeNameUtils.fixChoiceObjectName( name );
        getTLModelObject().setName( name );
        for (TypeUser user : getWhereAssigned()) {
            if (user instanceof PropertyNode)
                user.setName( name );
        }
        for (Node child : getChildren()) {
            // Shared facet is not a type provider
            if (child instanceof TypeProvider)
                for (TypeUser users : ((TypeProvider) child).getWhereAssigned())
                    NodeNameUtils.fixName( (Node) users );
        }
    }

    @Override
    public void sort() {
        getFacet_Shared().sort();
        for (AbstractContextualFacet f : getChoiceFacets())
            ((Sortable) f).sort();
    }

}
