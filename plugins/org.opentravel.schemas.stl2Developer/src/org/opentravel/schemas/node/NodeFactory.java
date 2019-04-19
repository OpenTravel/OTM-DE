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

package org.opentravel.schemas.node;

import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLEnumValue;
import org.opentravel.schemacompiler.model.TLExtensionPointFacet;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLLibraryMember;
import org.opentravel.schemacompiler.model.TLListFacet;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLOpenEnumeration;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLPropertyType;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.model.TLRole;
import org.opentravel.schemacompiler.model.TLRoleEnumeration;
import org.opentravel.schemacompiler.model.TLService;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLSimpleFacet;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.model.XSDComplexType;
import org.opentravel.schemacompiler.model.XSDElement;
import org.opentravel.schemacompiler.model.XSDSimpleType;
import org.opentravel.schemas.node.interfaces.ContextualFacetOwnerInterface;
import org.opentravel.schemas.node.interfaces.FacetInterface;
import org.opentravel.schemas.node.interfaces.InheritedInterface;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.listeners.InheritanceDependencyListener;
import org.opentravel.schemas.node.objectMembers.ContributedFacetNode;
import org.opentravel.schemas.node.objectMembers.ExtensionPointNode;
import org.opentravel.schemas.node.objectMembers.InheritedContributedFacetNode;
import org.opentravel.schemas.node.objectMembers.OperationFacetNode;
import org.opentravel.schemas.node.objectMembers.OperationNode;
import org.opentravel.schemas.node.objectMembers.SharedFacetNode;
import org.opentravel.schemas.node.properties.AttributeNode;
import org.opentravel.schemas.node.properties.AttributeReferenceNode;
import org.opentravel.schemas.node.properties.ElementNode;
import org.opentravel.schemas.node.properties.ElementReferenceNode;
import org.opentravel.schemas.node.properties.EnumLiteralNode;
import org.opentravel.schemas.node.properties.IdNode;
import org.opentravel.schemas.node.properties.IndicatorElementNode;
import org.opentravel.schemas.node.properties.IndicatorNode;
import org.opentravel.schemas.node.properties.InheritedAttributeNode;
import org.opentravel.schemas.node.properties.InheritedElementNode;
import org.opentravel.schemas.node.properties.InheritedEnumLiteralNode;
import org.opentravel.schemas.node.properties.InheritedIndicatorNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.properties.PropertyNodeType;
import org.opentravel.schemas.node.properties.RoleNode;
import org.opentravel.schemas.node.resources.ResourceNode;
import org.opentravel.schemas.node.typeProviders.AbstractContextualFacet;
import org.opentravel.schemas.node.typeProviders.AliasNode;
import org.opentravel.schemas.node.typeProviders.ChoiceFacetNode;
import org.opentravel.schemas.node.typeProviders.ChoiceObjectNode;
import org.opentravel.schemas.node.typeProviders.ContextualFacetNode;
import org.opentravel.schemas.node.typeProviders.CustomFacetNode;
import org.opentravel.schemas.node.typeProviders.EnumerationClosedNode;
import org.opentravel.schemas.node.typeProviders.EnumerationOpenNode;
import org.opentravel.schemas.node.typeProviders.FacetProviderNode;
import org.opentravel.schemas.node.typeProviders.InheritedContextualFacetNode;
import org.opentravel.schemas.node.typeProviders.ListFacetNode;
import org.opentravel.schemas.node.typeProviders.QueryFacetNode;
import org.opentravel.schemas.node.typeProviders.RoleFacetNode;
import org.opentravel.schemas.node.typeProviders.SimpleTypeNode;
import org.opentravel.schemas.node.typeProviders.UpdateFacetNode;
import org.opentravel.schemas.node.typeProviders.VWA_Node;
import org.opentravel.schemas.node.typeProviders.facetOwners.BusinessObjectNode;
import org.opentravel.schemas.node.typeProviders.facetOwners.CoreObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create Component Nodes of various sub-types.
 * 
 * @author Dave Hollander
 * 
 */
public class NodeFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger( NodeFactory.class );

    /**
     * Create an inherited property from the node it inherits from. Add a InheritanceDependencyListener to the parent of
     * the base property.
     * <p>
     * 
     * @param property the property inherited from. Parent must be the base object.
     * @param owner the parent of the inherited property. Must <b>not</b> be the parent of the property.
     * @return new inherited property
     */
    public static InheritedInterface newInheritedProperty(PropertyNode property, FacetInterface owner) {
        // assert owner != property.getParent();
        if (owner == property.getParent())
            LOGGER.error( "FIXME - inherited property with wrong parent" );

        InheritedInterface ip = null; // the new inherited property
        if (property instanceof ElementNode)
            ip = new InheritedElementNode( (ElementNode) property, owner );
        else if (property instanceof AttributeNode)
            ip = new InheritedAttributeNode( (AttributeNode) property, owner );
        else if (property instanceof IndicatorNode)
            ip = new InheritedIndicatorNode( (IndicatorNode) property, owner );
        else if (property instanceof EnumLiteralNode)
            ip = new InheritedEnumLiteralNode( (EnumLiteralNode) property, owner );
        else
            LOGGER.debug( "Unhandled inherited property: " + property.getClass().getSimpleName() );

        // Place a listener on the owner of the property
        property.getParent().getTLModelObject().addListener( new InheritanceDependencyListener( ip ) );

        assert ip.getInheritedFrom() != null;
        // FIXME - this should be true
        // assert ((Node) ip).getParent() != property.getParent();

        return ip;
    }

    /**
     * Create an inherited version 1.6 and later contextual and contributed facet to match the contributed facet
     * reported out from the TL Model.
     * 
     * @param base - the base object extended to create the ghost facets.
     * @param contributed - the ghost contributed facet created from the tlGhost
     * @return
     */
    public static InheritedContextualFacetNode newInheritedFacet(ContextualFacetOwnerInterface base,
        ContributedFacetNode contributed) {

        // if (owner == contributed.getParent())
        // LOGGER.debug("Don't need no owner.");
        // else
        // assert false;
        ContextualFacetOwnerInterface owner = (ContextualFacetOwnerInterface) contributed.getParent();
        TLContextualFacet tlGhost = contributed.getTLModelObject();

        // Find the matching facet in the base node - baseCF
        // findByName() will not work because need to match the exact node local name and type
        Node baseNode = null;
        for (ContributedFacetNode cf : base.getContributedFacets()) {
            if (cf.getName().endsWith( contributed.getLocalName() ))
                if (cf.getFacetType().equals( contributed.getFacetType() ))
                    baseNode = cf;

        }
        for (Node n : base.getChildrenHandler().get())
            // if (((ExtensionOwner) base).getExtensionBase() != null)
            // LOGGER.debug("Must Search base too.");

            assert baseNode instanceof ContributedFacetNode;
        if (baseNode == null)
            return null;

        ContributedFacetNode baseContrib = (ContributedFacetNode) baseNode;
        ContextualFacetNode baseCF = baseContrib.getContributor();

        // If this inherited facet has been modeled before then reuse it
        InheritedContextualFacetNode icf =
            (InheritedContextualFacetNode) owner.getLibrary().findLibraryMemberByName( tlGhost.getLocalName() );
        if (icf != null) {
            icf.setDeleted( false ); // May have been deleted in children init processing
        } else {
            icf = new InheritedContextualFacetNode( tlGhost, baseCF, ((Node) owner).getParent() );
            owner.getLibrary().addMember( icf );
        }

        contributed.setContributor( icf.getTLModelObject() );
        InheritedContributedFacetNode iContrib = new InheritedContributedFacetNode( contributed );
        icf.setWhereContributed( iContrib );

        assert icf.getInheritedFrom() == baseCF;
        assert icf == Node.GetNode( icf.getTLModelObject() );
        assert icf == contributed.getContributor();
        assert baseCF.hasInheritanceDependacyListenerTo( icf );
        assert owner.getLibrary().contains( icf );

        return icf;
    }

    /**
     * Create new Library Member Interface Node based on the passed TL object.
     * <p>
     * The TL LibraryMember listeners are used to look up the node. If the node doesn't exist, one is created.
     * <p>
     * For OTM version 1.6 and later, ContextualFacets are modeled as top-level ContextualFacetNodes. In version OTM
     * version 1.5 contextual facets are skipped.
     * 
     * @return newly created node or null
     */
    public static LibraryMemberInterface newLibraryMember(final LibraryMember mbr) {
        // LOGGER.debug("Creating new library member node for " + mbr.getLocalName());
        LibraryMemberInterface lm = null;
        if (mbr == null)
            return lm;

        // Attempt to lookup already modeled node
        Node n = Node.GetNode( mbr );

        // If previously modeled, just return the node
        if (n instanceof LibraryMemberInterface)
            return (LibraryMemberInterface) n;

        // Create a node for the TL LibraryMember
        //
        if (mbr instanceof TLValueWithAttributes)
            lm = new VWA_Node( (TLValueWithAttributes) mbr );
        else if (mbr instanceof TLBusinessObject)
            lm = new BusinessObjectNode( (TLBusinessObject) mbr );
        else if (mbr instanceof TLCoreObject)
            lm = new CoreObjectNode( (TLCoreObject) mbr );
        else if (mbr instanceof TLChoiceObject)
            lm = new ChoiceObjectNode( (TLChoiceObject) mbr );
        else if (mbr instanceof TLSimple)
            lm = new SimpleTypeNode( (TLSimple) mbr );
        else if (mbr instanceof TLOpenEnumeration)
            lm = new EnumerationOpenNode( (TLOpenEnumeration) mbr );
        else if (mbr instanceof TLClosedEnumeration)
            lm = new EnumerationClosedNode( (TLClosedEnumeration) mbr );
        else if (mbr instanceof TLExtensionPointFacet)
            lm = new ExtensionPointNode( (TLExtensionPointFacet) mbr );
        else if (mbr instanceof TLResource)
            lm = new ResourceNode( (TLResource) mbr );
        else if (mbr instanceof TLService)
            lm = new ServiceNode( (TLService) mbr );
        else if (mbr instanceof TLContextualFacet) {
            lm = createFacet( (TLContextualFacet) mbr );
        } else if (mbr instanceof XSDSimpleType)
            lm = null; // FIXME - see LibraryChildrenHandler
        else if (mbr instanceof XSDComplexType)
            lm = null; // FIXME
        else if (mbr instanceof XSDElement)
            lm = null; // FIXME
        else {
            // cn = new ComponentNode(mbr);
            assert (false);
            // LOGGER.debug("Using default factory type for " + mbr.getClass().getSimpleName());
        }

        // LOGGER.debug("Created new library member node " + lm);

        return lm;
    }

    // public static ComponentNode newChild(FacetInterface parent, TLModelElement tlObj) {
    // // TODO - separate out newChild logic
    // return newChild((Node) parent, tlObj);
    // }

    /**
     * Create a component node representing this TL Object. The new component is <b>not</b> added to a library.
     * 
     * @param parent
     * @param tlObj
     * @return
     */
    public static ComponentNode newChild(Node parent, TLModelElement tlObj) {
        if (tlObj == null)
            return null;
        ComponentNode nn = null;
        //
        // Properties
        //
        if (tlObj instanceof TLProperty)
            nn = createProperty( (TLProperty) tlObj, (FacetInterface) parent );
        else if (tlObj instanceof TLIndicator)
            nn = createIndicator( (TLIndicator) tlObj, (FacetInterface) parent );
        else if (tlObj instanceof TLAttribute)
            nn = createAttribute( (TLAttribute) tlObj, (FacetInterface) parent );
        else if (tlObj instanceof TLRole)
            nn = new RoleNode( (TLRole) tlObj, (RoleFacetNode) parent );
        else if (tlObj instanceof TLEnumValue)
            nn = new EnumLiteralNode( (TLEnumValue) tlObj, (FacetInterface) parent );
        //
        // Alias
        //
        else if (tlObj instanceof TLAlias)
            nn = new AliasNode( parent, (TLAlias) tlObj );
        //
        // Facets
        //
        else if (tlObj instanceof TLContextualFacet) {
            if (parent instanceof ContextualFacetOwnerInterface)
                nn = new ContributedFacetNode( (TLContextualFacet) tlObj, (ContextualFacetOwnerInterface) parent );
            else
                nn = new ContributedFacetNode( (TLContextualFacet) tlObj );
        } else if (tlObj instanceof TLFacet)
            nn = (ComponentNode) createFacet( (TLFacet) tlObj );
        else if (tlObj instanceof TLListFacet)
            nn = new ListFacetNode( (TLListFacet) tlObj );
        else if (tlObj instanceof TLSimpleFacet)
            assert false;
        // nn = new SimpleFacetNode((TLSimpleFacet) tlObj);
        // TLRoleEnumeration is the TL Container for TLRoles
        else if (tlObj instanceof TLRoleEnumeration)
            nn = new RoleFacetNode( (TLRoleEnumeration) tlObj );
        else if (tlObj instanceof TLOperation)
            nn = new OperationNode( (TLOperation) tlObj );
        //
        // Others
        //
        else if (tlObj instanceof TLLibraryMember)
            nn = (ComponentNode) newLibraryMember( (TLLibraryMember) tlObj );

        if (nn == null)
            LOGGER.debug( "No node created." );

        nn.setParent( parent );
        NodeNameUtils.fixName( nn ); // make sure the name is legal (2/2016)
        return nn;
    }

    private static PropertyNode createAttribute(TLAttribute tlObj, FacetInterface parent) {
        PropertyNode nn;
        TLPropertyType type = tlObj.getType();
        if (type != null && type.getNamespace() != null && type.getNamespace().equals( ModelNode.XSD_NAMESPACE )
            && type.getLocalName().equals( "ID" ))
            nn = new IdNode( tlObj, parent );
        else if (tlObj.isReference())
            nn = new AttributeReferenceNode( tlObj, parent );
        else
            nn = new AttributeNode( tlObj, parent );
        return nn;
    }

    public static AbstractContextualFacet createContextualFacet(TLContextualFacet tlFacet) {
        return createFacet( tlFacet );
    }

    private static ContextualFacetNode createFacet(TLContextualFacet tlFacet) {
        switch (tlFacet.getFacetType()) {
            case CUSTOM:
                return new CustomFacetNode( tlFacet );
            case CHOICE:
                return new ChoiceFacetNode( tlFacet );
            case QUERY:
                return new QueryFacetNode( tlFacet );
            case UPDATE:
                return new UpdateFacetNode( tlFacet );
            default:
                break;
        }
        return null;
    }

    private static FacetInterface createFacet(TLFacet facet) {
        assert (facet.getFacetType() != null);

        switch (facet.getFacetType()) {
            case REQUEST:
            case RESPONSE:
            case NOTIFICATION:
                return new OperationFacetNode( facet );
            case SHARED:
                return new SharedFacetNode( facet );
            case DETAIL:
            case ID:
            case SIMPLE:
            case SUMMARY:
            default:
                return new FacetProviderNode( facet );
        }
    }

    private static PropertyNode createIndicator(TLIndicator tlObj, FacetInterface parent) {
        PropertyNode nn;
        if (tlObj.isPublishAsElement())
            nn = new IndicatorElementNode( tlObj, parent );
        else
            nn = new IndicatorNode( tlObj, parent );
        return nn;
    }

    private static PropertyNode createProperty(TLProperty tlObj, FacetInterface parent) {
        PropertyNode nn;
        if (tlObj.isReference())
            nn = new ElementReferenceNode( tlObj, parent );
        else
            nn = new ElementNode( tlObj, parent );

        if (parent != null && !parent.canOwn( nn.getPropertyType() ))
            nn = nn.changePropertyRole( PropertyNodeType.ATTRIBUTE, parent );

        return nn;
    }

}
