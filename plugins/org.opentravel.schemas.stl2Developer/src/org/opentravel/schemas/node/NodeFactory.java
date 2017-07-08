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
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLSimpleFacet;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.model.XSDSimpleType;
import org.opentravel.schemacompiler.util.OTM16Upgrade;
import org.opentravel.schemas.modelObject.TLnSimpleAttribute;
import org.opentravel.schemas.modelObject.TLnValueWithAttributesFacet;
import org.opentravel.schemas.node.facets.ChoiceFacetNode;
import org.opentravel.schemas.node.facets.ContextualFacetNode;
import org.opentravel.schemas.node.facets.ContributedFacetNode;
import org.opentravel.schemas.node.facets.CustomFacetNode;
import org.opentravel.schemas.node.facets.FacetNode;
import org.opentravel.schemas.node.facets.ListFacetNode;
import org.opentravel.schemas.node.facets.OperationFacetNode;
import org.opentravel.schemas.node.facets.OperationNode;
import org.opentravel.schemas.node.facets.QueryFacetNode;
import org.opentravel.schemas.node.facets.RoleFacetNode;
import org.opentravel.schemas.node.facets.SimpleFacetNode;
import org.opentravel.schemas.node.facets.UpdateFacetNode;
import org.opentravel.schemas.node.facets.VWA_AttributeFacetNode;
import org.opentravel.schemas.node.interfaces.ContextualFacetOwnerInterface;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.properties.AttributeNode;
import org.opentravel.schemas.node.properties.ElementNode;
import org.opentravel.schemas.node.properties.ElementReferenceNode;
import org.opentravel.schemas.node.properties.EnumLiteralNode;
import org.opentravel.schemas.node.properties.IdNode;
import org.opentravel.schemas.node.properties.IndicatorElementNode;
import org.opentravel.schemas.node.properties.IndicatorNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.properties.PropertyOwnerInterface;
import org.opentravel.schemas.node.properties.RoleNode;
import org.opentravel.schemas.node.properties.SimpleAttributeNode;
import org.opentravel.schemas.node.resources.ResourceNode;

/**
 * Create Component Nodes of various sub-types.
 * 
 * @author Dave Hollander
 * 
 */
public class NodeFactory {
	// private static final Logger LOGGER = LoggerFactory.getLogger(NodeFactory.class);

	/*******************************************************************************************
	 * New ComponentNode methods that also create new objects in underlying model
	 */

	/**
	 * Create a new component. Assigns types to all of its properties based on TL object and/or documentation for XSD
	 * derived nodes. Unlike constructors, the factory method also assigns the type node.
	 * 
	 * @param mbr
	 * @return
	 */
	public static ComponentNode newComponent(TLLibraryMember mbr) {
		ComponentNode newNode = newComponent_UnTyped(mbr);
		return newNode;
	}

	/*******************************************************************************************
	 * 
	 * New ComponentNode methods that also create new objects in underlying model objects.
	 */

	/**
	 * Create new Library Member (BO, Choice, Core, etc) based on the passed TL object.
	 * <p>
	 * For OTM version 1.6 and later, ContextualFacets are modeled as top-level ContextualFacetNodes. In version OTM
	 * version 1.5 contextual facets are skipped. Contributed facets are NOT created.
	 * 
	 * @param mbr
	 *            TLModelElement to be modeled
	 * @param library
	 *            LibraryNode to add the new node to
	 * @return newly created node or null
	 */
	public static ComponentNode newObjectNode(final LibraryMember mbr, final LibraryNode library) {
		ComponentNode cn = newComponent_UnTyped(mbr);
		if (cn != null) {
			// Why not the more feature rich addMember(cn);
			// library.linkMember(cn); // add to library and sets library on cn
			library.addMember(cn); // add to library and sets library on cn
			assert (library.getDescendants_LibraryMembers().contains(cn));
		}
		return cn;
	}

	/**
	 * Create new Library Member based on the passed TL object.
	 * <p>
	 * For OTM version 1.6 and later, ContextualFacets are modeled as top-level ContextualFacetNodes. In version OTM
	 * version 1.5 contextual facets are skipped.
	 * 
	 * @return newly created node or null
	 */
	public static ComponentNode newComponent_UnTyped(final LibraryMember mbr) {
		ComponentNode cn = null;
		if (mbr == null)
			return cn;
		// LOGGER.debug("Creating new untyped component for " + mbr.getLocalName());

		if (mbr instanceof TLValueWithAttributes)
			cn = new VWA_Node((TLValueWithAttributes) mbr);
		else if (mbr instanceof TLBusinessObject)
			cn = new BusinessObjectNode((TLBusinessObject) mbr);
		else if (mbr instanceof TLCoreObject)
			cn = new CoreObjectNode((TLCoreObject) mbr);
		else if (mbr instanceof TLChoiceObject)
			cn = new ChoiceObjectNode((TLChoiceObject) mbr);
		else if (mbr instanceof TLSimple)
			cn = new SimpleTypeNode((TLSimple) mbr);
		else if (mbr instanceof TLOpenEnumeration)
			cn = new EnumerationOpenNode((TLOpenEnumeration) mbr);
		else if (mbr instanceof TLClosedEnumeration)
			cn = new EnumerationClosedNode((TLClosedEnumeration) mbr);
		else if (mbr instanceof TLExtensionPointFacet)
			cn = new ExtensionPointNode((TLExtensionPointFacet) mbr);
		else if (mbr instanceof TLResource)
			cn = new ResourceNode((TLResource) mbr);
		else if (mbr instanceof XSDSimpleType)
			cn = new SimpleTypeNode((TLSimple) Node.GetNode(mbr).getTLModelObject());
		else if (mbr instanceof TLContextualFacet) {
			if (OTM16Upgrade.otm16Enabled)
				cn = createFacet((TLContextualFacet) mbr);
		} else {
			// cn = new ComponentNode(mbr);
			assert (false);
			// LOGGER.debug("Using default factory type for " + mbr.getClass().getSimpleName());
		}

		// LOGGER.debug("Created new untyped component " + cn);

		return cn;
	}

	/**
	 * Creates a <b>member</b> of a top level object (Library Member).
	 * 
	 * In version 1.5 contextual facets are added to the parent. In version 1.6 and later contextual facets become
	 * ContributedFacetNodes since these are parts of objects.
	 * 
	 * @param parent
	 *            is the top-level component used for properties, can be null
	 * @param tlObj
	 *            is TL model object to create member from
	 * @return the newly created and modeled node
	 */
	// TODO - change type of tlObj
	public static ComponentNode newMember(INode parent, Object tlObj) {
		ComponentNode nn = null;
		// if (!(parent instanceof ServiceNode) && parent != null && !(parent instanceof LibraryMemberInterface)
		// && !(parent instanceof PropertyOwnerInterface))
		// LOGGER.warn("Invalid parent type for new member: " + parent);

		//
		// Properties
		//
		if (tlObj instanceof TLProperty)
			nn = createProperty((TLProperty) tlObj, (PropertyOwnerInterface) parent);
		else if (tlObj instanceof TLIndicator)
			nn = createIndicator((TLIndicator) tlObj, (PropertyOwnerInterface) parent);
		else if (tlObj instanceof TLAttribute)
			nn = createAttribute((TLAttribute) tlObj, (PropertyOwnerInterface) parent);
		else if (tlObj instanceof TLRole)
			nn = new RoleNode((TLRole) tlObj, (RoleFacetNode) parent);
		else if (tlObj instanceof TLEnumValue)
			nn = new EnumLiteralNode((TLEnumValue) tlObj, parent);
		else if (tlObj instanceof TLnSimpleAttribute)
			nn = new SimpleAttributeNode((TLnSimpleAttribute) tlObj, parent);
		//
		// Alias
		//
		else if (tlObj instanceof TLAlias)
			nn = new AliasNode((Node) parent, (TLAlias) tlObj);
		//
		// Facets
		//
		else if (tlObj instanceof TLnValueWithAttributesFacet)
			nn = new VWA_AttributeFacetNode((TLnValueWithAttributesFacet) tlObj);
		else if (tlObj instanceof TLContextualFacet) {
			if (OTM16Upgrade.otm16Enabled) {
				if (parent instanceof ContextualFacetOwnerInterface)
					nn = new ContributedFacetNode((TLContextualFacet) tlObj, (ContextualFacetOwnerInterface) parent);
				else
					nn = new ContributedFacetNode((TLContextualFacet) tlObj);
			} else
				nn = createFacet((TLContextualFacet) tlObj);
		} else if (tlObj instanceof TLFacet)
			nn = createFacet((TLFacet) tlObj);
		else if (tlObj instanceof TLListFacet)
			nn = new ListFacetNode((TLListFacet) tlObj);
		else if (tlObj instanceof TLSimpleFacet)
			nn = new SimpleFacetNode((TLSimpleFacet) tlObj);
		else if (tlObj instanceof TLRoleEnumeration)
			nn = new RoleFacetNode((TLRoleEnumeration) tlObj);
		else if (tlObj instanceof TLOperation)
			nn = new OperationNode((TLOperation) tlObj);
		//
		// Others
		//
		else if (tlObj instanceof TLLibraryMember)
			nn = newComponent_UnTyped((TLLibraryMember) tlObj);

		if (parent != null && nn.getParent() == null) {
			NodeNameUtils.fixName(nn); // make sure the name is legal (2/2016)
			((Node) parent).linkChild(nn);
			if (parent.getLibrary() != null) {
				nn.setLibrary(parent.getLibrary());
				nn.setContext(); // assure default context set as needed
			}
		}

		return nn;
	}

	private static PropertyNode createAttribute(TLAttribute tlObj, PropertyOwnerInterface parent) {
		PropertyNode nn;
		TLPropertyType type = tlObj.getType();
		if (type != null && type.getNamespace() != null && type.getNamespace().equals(ModelNode.XSD_NAMESPACE)
				&& type.getLocalName().equals("ID"))
			nn = new IdNode((TLModelElement) tlObj, parent);
		else if (tlObj.isReference())
			nn = new AttributeNode(tlObj, parent);
		else
			nn = new AttributeNode(tlObj, parent);
		return nn;
	}

	public static ContextualFacetNode createFacet(TLContextualFacet tlFacet) {

		switch (tlFacet.getFacetType()) {
		case CUSTOM:
			return new CustomFacetNode(tlFacet);
		case CHOICE:
			return new ChoiceFacetNode(tlFacet);
		case QUERY:
			return new QueryFacetNode(tlFacet);
		case UPDATE:
			return new UpdateFacetNode(tlFacet);
		default:
			break;
		}
		return null;
	}

	private static FacetNode createFacet(TLFacet facet) {
		assert (facet.getFacetType() != null);

		switch (facet.getFacetType()) {
		case REQUEST:
		case RESPONSE:
		case NOTIFICATION:
			return new OperationFacetNode(facet);
		case SHARED:
		case DETAIL:
		case ID:
		case SIMPLE:
		case SUMMARY:
		default:
			return new FacetNode(facet);
		}
	}

	private static PropertyNode createIndicator(TLIndicator tlObj, PropertyOwnerInterface parent) {
		PropertyNode nn;
		if (tlObj.isPublishAsElement())
			nn = new IndicatorElementNode(tlObj, parent);
		else
			nn = new IndicatorNode(tlObj, parent);
		return nn;
	}

	private static PropertyNode createProperty(TLProperty tlObj, PropertyOwnerInterface parent) {
		PropertyNode nn;
		if (tlObj.isReference())
			nn = new ElementReferenceNode(tlObj, parent);
		else
			nn = new ElementNode(tlObj, parent);
		return nn;
	}

}
