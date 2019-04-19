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

package org.opentravel.schemas.node.typeProviders.facetOwners;

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLComplexTypeBase;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.ComponentNodeType;
import org.opentravel.schemas.node.NavNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFactory;
import org.opentravel.schemas.node.NodeNameUtils;
import org.opentravel.schemas.node.controllers.NodeUtils;
import org.opentravel.schemas.node.handlers.children.BusinessObjectChildrenHandler;
import org.opentravel.schemas.node.interfaces.AliasOwner;
import org.opentravel.schemas.node.interfaces.ContextualFacetOwnerInterface;
import org.opentravel.schemas.node.interfaces.ExtensionOwner;
import org.opentravel.schemas.node.interfaces.FacetInterface;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.interfaces.Sortable;
import org.opentravel.schemas.node.interfaces.VersionedObjectInterface;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.listeners.BaseNodeListener;
import org.opentravel.schemas.node.listeners.TypeProviderListener;
import org.opentravel.schemas.node.objectMembers.ContributedFacetNode;
import org.opentravel.schemas.node.objectMembers.FacetOMNode;
import org.opentravel.schemas.node.properties.IdNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.properties.PropertyNodeType;
import org.opentravel.schemas.node.typeProviders.AbstractContextualFacet;
import org.opentravel.schemas.node.typeProviders.AliasNode;
import org.opentravel.schemas.node.typeProviders.ContextualFacetNode;
import org.opentravel.schemas.node.typeProviders.CustomFacetNode;
import org.opentravel.schemas.node.typeProviders.FacetProviderNode;
import org.opentravel.schemas.node.typeProviders.QueryFacetNode;
import org.opentravel.schemas.node.typeProviders.VWA_Node;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.types.ExtensionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Dave Hollander
 * 
 */
public class BusinessObjectNode extends FacetOwners
    implements ExtensionOwner, AliasOwner, Sortable, ContextualFacetOwnerInterface, VersionedObjectInterface {

    private static final Logger LOGGER = LoggerFactory.getLogger( BusinessObjectNode.class );
    private ExtensionHandler extensionHandler = null;
    private LibraryNode owningLibrary = null;

    public BusinessObjectNode(TLBusinessObject mbr) {
        super( mbr );

        childrenHandler = new BusinessObjectChildrenHandler( this );
        extensionHandler = new ExtensionHandler( this );
    }

    /**
     * Create a new business object using the core as a template and add to the same library as the core object.
     * 
     * @param core
     */
    public BusinessObjectNode(CoreObjectNode core) {
        this( new TLBusinessObject() );
        if (core == null)
            return;
        cloneAliases( core.getAliases() );

        setName( core.getName() );
        core.getLibrary().addMember( this ); // version managed library safe add
        setDocumentation( core.getDocumentation() );

        if (core.isDeleted())
            return;
        getFacet_Summary().copy( core.getFacet_Summary() );
        getFacet_Detail().copy( core.getFacet_Detail() );

        // Assure business object has one and only one ID and it is in the ID facet.
        fixIDs();
    }

    public BusinessObjectNode(VWA_Node vwa) {
        this( new TLBusinessObject() );
        if (vwa == null)
            return;

        setName( vwa.getName() );
        vwa.getLibrary().addMember( this );
        setDocumentation( vwa.getDocumentation() );
        if (vwa.isDeleted())
            return;

        getFacet_Summary().copy( vwa.getFacet_Attributes() );

        // Assure business object has one and only one ID and it is in the ID facet.
        fixIDs();

    }

    @Override
    public LibraryNode getLibrary() {
        return owningLibrary;
    }

    @Override
    public void setLibrary(LibraryNode library) {
        owningLibrary = library;
    }

    @Override
    public String getName() {
        return emptyIfNull( getTLModelObject().getName() );
    }

    @Override
    public TLBusinessObject getTLModelObject() {
        return (TLBusinessObject) tlObj;
    }

    @Override
    public boolean isExtensibleObject() {
        return true;
    }

    @Override
    public Node setExtensible(boolean extensible) {
        if (isEditable_newToChain())
            if (getTLModelObject() instanceof TLComplexTypeBase)
                ((TLComplexTypeBase) getTLModelObject()).setNotExtendable( !extensible );
        return this;
    }

    @Override
    public boolean hasChildren_TypeProviders() {
        return true;
    }

    @Override
    public boolean isAssignedByReference() {
        return true;
    }

    @Override
    public ComponentNodeType getComponentNodeType() {
        return ComponentNodeType.BUSINESS;
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
    public FacetProviderNode getFacet_Default() {
        return getFacet_Summary();
    }

    @Override
    public Image getImage() {
        return Images.getImageRegistry().get( Images.BusinessObject );
    }

    @Override
    public BaseNodeListener getNewListener() {
        return new TypeProviderListener( this );
    }

    @Override
    public void remove(AliasNode alias) {
        getTLModelObject().removeAlias( alias.getTLModelObject() );
        clearAllAliasHolders();
    }

    @Override
    public void addAlias(AliasNode alias) {
        if (!getTLModelObject().getAliases().contains( alias.getTLModelObject() ))
            getTLModelObject().addAlias( alias.getTLModelObject() );
        clearAllAliasHolders();
    }

    @Override
    public AliasNode addAlias(String name) {
        AliasNode alias = null;
        if (this.isEditable_newToChain())
            alias = new AliasNode( this, NodeNameUtils.fixBusinessObjectName( name ) );
        return alias;
    }

    @Override
    public void cloneAliases(List<AliasNode> aliases) {
        for (AliasNode a : aliases)
            addAlias( a.getName() );
    }

    private void clearAllAliasHolders() {
        for (Node child : getChildren())
            if (child.getChildrenHandler() != null)
                child.getChildrenHandler().clear();
        getChildrenHandler().clear();
    }

    /**
     * 
     * New facets can only be added in unmanaged or head versions.
     * 
     * @param name
     * @param type
     * @return the new contextual facet (not contributed)
     */
    // TODO - consider allowing them in minor and use createMinorVersionOfComponent()
    @Override
    public AbstractContextualFacet addFacet(String name, TLFacetType type) {
        if (!isEditable_newToChain()) {
            isEditable_newToChain();
            throw new IllegalArgumentException( "Not editable - Can not add facet to " + this );
        }
        TLContextualFacet tlCf = ContextualFacetNode.createTL( name, type );
        AbstractContextualFacet cf = NodeFactory.createContextualFacet( tlCf );
        cf.setOwner( this );
        if (cf instanceof LibraryMemberInterface)
            getLibrary().addMember( (LibraryMemberInterface) cf );
        cf.setName( NodeNameUtils.fixContextualFacetName( cf, name ) );

        assert cf.getParent() instanceof NavNode;
        assert getChildren().contains( ((ContextualFacetNode) cf).getWhereContributed() );
        return cf;
    }

    @Override
    public boolean canOwn(AbstractContextualFacet targetCF) {
        return canOwn( targetCF.getTLModelObject().getFacetType() );
    }

    @Override
    public boolean canOwn(TLFacetType type) {
        switch (type) {
            case ID:
            case SUMMARY:
            case DETAIL:
            case CUSTOM:
            case QUERY:
            case UPDATE:
                return true;
            default:
                return false;
        }
    }

    @Override
    public ComponentNode createMinorVersionComponent() {
        TLBusinessObject tlMinor = (TLBusinessObject) createMinorTLVersion( this );
        if (tlMinor != null)
            return super.createMinorVersionComponent( new BusinessObjectNode( tlMinor ) );
        return null;
    }

    /**
     * @return Custom Facets without inherited
     */
    public List<AbstractContextualFacet> getCustomFacets() {
        ArrayList<AbstractContextualFacet> ret = new ArrayList<>();
        for (INode f : getContextualFacets( false ))
            if (f instanceof CustomFacetNode)
                ret.add( (CustomFacetNode) f );

        return ret;
    }

    @Override
    public NavNode getParent() {
        return (NavNode) parent;
    }

    // FIXME - make return abstractContextualFacet
    public List<ComponentNode> getQueryFacets() {
        ArrayList<ComponentNode> ret = new ArrayList<>();
        for (AbstractContextualFacet f : getContextualFacets( false )) {
            if (f instanceof QueryFacetNode)
                ret.add( f );
        }
        return ret;
    }

    @Override
    public void delete() {
        // Must delete the contextual facets separately because they are separate library members.
        for (Node n : getChildren_New())
            if (n instanceof ContextualFacetNode)
                n.delete();
        super.delete();
    }

    /**
     * Assure business object has one and only one ID and it is in the ID facet. Change extra IDs to attributes. Create
     * new ID if needed.
     * 
     * @return
     */
    private IdNode fixIDs() {
        IdNode finalID = null;
        // Use from ID facet if found. if more than one found, change extras to attribute
        for (Node n : getFacet_ID().getChildren())
            if (n instanceof IdNode)
                if (finalID == null) {
                    ((IdNode) n).moveProperty( getFacet_ID() );
                    finalID = (IdNode) n;
                } else
                    ((PropertyNode) n).changePropertyRole( PropertyNodeType.ATTRIBUTE );

        // Search for any ID types. Move 1st one to ID facet and make rest into attributes.
        List<Node> properties = new ArrayList<>( getFacet_Summary().getChildren() );
        properties.addAll( getFacet_Detail().getChildren() );
        for (Node n : properties)
            if (n instanceof IdNode)
                if (finalID == null) {
                    ((IdNode) n).moveProperty( getFacet_ID() );
                    finalID = (IdNode) n;
                } else
                    ((PropertyNode) n).changePropertyRole( PropertyNodeType.ATTRIBUTE );

        // If none were found, make one
        if (finalID == null)
            finalID = new IdNode( getFacet_ID(), "newID" ); // BO must have at least one ID facet property
        return finalID;
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

    @Override
    public void setName(String name) {
        getTLModelObject().setName( NodeNameUtils.fixBusinessObjectName( name ) );
        updateNames( NodeNameUtils.fixBusinessObjectName( name ) );
    }

    @Override
    public void sort() {
        getFacet_Summary().sort();
        getFacet_Detail().sort();
        for (ComponentNode f : getCustomFacets())
            ((FacetOMNode) f).sort();
        for (ComponentNode f : getQueryFacets())
            ((FacetOMNode) f).sort();
    }

    @Override
    public void merge(Node source) {
        if (!(source instanceof BusinessObjectNode)) {
            throw new IllegalStateException( "Can only merge objects with the same type" );
        }
        BusinessObjectNode business = (BusinessObjectNode) source;
        getFacet_ID().add( business.getFacet_ID().getProperties(), true );
        getFacet_Summary().add( business.getFacet_Summary().getProperties(), true );
        getFacet_Detail().add( business.getFacet_Detail().getProperties(), true );

        copyFacet( business.getContextualFacets( false ) );
        getChildrenHandler().clear();
    }

    private void copyFacet(List<AbstractContextualFacet> facets) {
        // assert false;
        // // FIXME
        // }
        //
        // private void copyFacet(List<ComponentNode> facets) {
        // FIXME
        for (ComponentNode f : facets) {
            FacetInterface facet = (FacetInterface) f;
            if (!NodeUtils.checker( (Node) facet ).isInheritedFacet().get()) {
                TLFacet tlFacet = (TLFacet) facet.getTLModelObject();
                String name = "";
                if (tlFacet instanceof TLContextualFacet)
                    name = ((TLContextualFacet) tlFacet).getName();
                ComponentNode newFacet = addFacet( name, tlFacet.getFacetType() );
                ((FacetInterface) newFacet).add( facet.getProperties(), true );
            }
        }
    }

    @Override
    public boolean isMergeSupported() {
        return true;
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
    public String getExtendsTypeNS() {
        return getExtensionBase() != null ? getExtensionBase().getNamespace() : "";
    }

    @Override
    public void setExtension(final Node base) {
        if (extensionHandler == null)
            extensionHandler = new ExtensionHandler( this );
        extensionHandler.set( base );
    }

    @Override
    public ExtensionHandler getExtensionHandler() {
        return extensionHandler;
    }

}
