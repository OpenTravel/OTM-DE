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
import org.opentravel.schemas.modelObject.TLnSimpleAttribute;
import org.opentravel.schemas.modelObject.TLnValueWithAttributesFacet;
import org.opentravel.schemas.node.facets.ChoiceFacetNode;
import org.opentravel.schemas.node.facets.ContextualFacetNode;
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
import org.opentravel.schemas.node.interfaces.INode;
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
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create Component Nodes of various sub-types.
 * 
 * @author Dave Hollander
 * 
 */
public class NodeFactory {
	private static final Logger LOGGER = LoggerFactory.getLogger(NodeFactory.class);

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

	/**
	 * Create a new component and use the type node to set its type.
	 * 
	 * @param mbr
	 * @param type
	 * @return newly created and typed node.
	 */
	public static ComponentNode newComponent(TLLibraryMember mbr, INode type) {
		ComponentNode newNode = newComponent_UnTyped(mbr);
		if (newNode instanceof TypeUser && type instanceof TypeProvider)
			((TypeUser) newNode).setAssignedType((TypeProvider) type);
		return newNode;
	}

	/*******************************************************************************************
	 * New ComponentNode methods that also create new objects in underlying model
	 * 
	 * @return newly created node or null
	 */
	public static ComponentNode newComponent_UnTyped(final TLLibraryMember mbr) {
		ComponentNode cn = null;
		if (mbr == null)
			return cn;
		// LOGGER.debug("Creating new untyped component for " + mbr.getLocalName());

		if (mbr instanceof TLValueWithAttributes)
			cn = new VWA_Node((TLValueWithAttributes) mbr);
		else if (mbr instanceof TLBusinessObject)
			cn = new BusinessObjectNode(mbr);
		else if (mbr instanceof TLCoreObject)
			cn = new CoreObjectNode((TLCoreObject) mbr);
		else if (mbr instanceof TLChoiceObject)
			cn = new ChoiceObjectNode((TLChoiceObject) mbr);
		else if (mbr instanceof TLSimple)
			cn = new SimpleTypeNode((TLSimple) mbr);
		else if (mbr instanceof TLOpenEnumeration)
			cn = new EnumerationOpenNode(mbr);
		else if (mbr instanceof TLClosedEnumeration)
			cn = new EnumerationClosedNode(mbr);
		else if (mbr instanceof TLExtensionPointFacet)
			cn = new ExtensionPointNode((TLExtensionPointFacet) mbr);
		else if (mbr instanceof TLResource)
			cn = new ResourceNode(mbr);
		else if (mbr instanceof XSDSimpleType)
			cn = new SimpleTypeNode((TLSimple) Node.GetNode(mbr).getTLModelObject());
		else {
			// cn = new ComponentNode(mbr);
			assert (false);
			// LOGGER.debug("Using default factory type for " + mbr.getClass().getSimpleName());
		}

		// LOGGER.debug("Created new untyped component " + cn);

		return cn;
	}

	public static Node newComponent(ComponentNodeType type) {
		TLLibraryMember tlObj = null;

		switch (type) {
		case BUSINESS:
			tlObj = new TLBusinessObject();
			break;
		case CHOICE:
			tlObj = new TLChoiceObject();
			break;
		case CORE:
			tlObj = new TLCoreObject();
			break;
		case VWA:
			tlObj = new TLValueWithAttributes();
			break;
		case EXTENSION_POINT:
			tlObj = new TLExtensionPointFacet();
			break;
		case OPEN_ENUM:
			tlObj = new TLOpenEnumeration();
			break;
		case CLOSED_ENUM:
			tlObj = new TLClosedEnumeration();
			break;
		case SIMPLE:
			tlObj = new TLSimple();
			break;
		default:
			// LOGGER.debug("Unknown type in new component: "+type);
		}
		return newComponent(tlObj);
	}

	/**
	 * Creates a member of a top level component and properties.
	 * 
	 * @param parent
	 *            is the top-level component used for properties, can be null
	 * @param tlObj
	 *            is TL model object to create member from
	 * @return the newly created and modeled node
	 */
	public static ComponentNode newComponentMember(INode parent, Object tlObj) {
		ComponentNode nn = null;
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
		else if (tlObj instanceof TLContextualFacet)
			nn = createFacet((TLContextualFacet) tlObj);
		else if (tlObj instanceof TLFacet)
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

	public static PropertyNode createAttribute(TLAttribute tlObj, PropertyOwnerInterface parent) {
		PropertyNode nn;
		TLPropertyType type = tlObj.getType();
		if (type != null && type.getNamespace() != null && type.getNamespace().equals(ModelNode.XSD_NAMESPACE)
				&& type.getLocalName().equals("ID"))
			nn = new IdNode((TLModelElement) tlObj, parent);
		else
			nn = new AttributeNode(tlObj, parent);
		return nn;
	}

	public static ContextualFacetNode createFacet(TLContextualFacet facet) {
		switch (facet.getFacetType()) {
		case CUSTOM:
			return new CustomFacetNode(facet);
		case CHOICE:
			return new ChoiceFacetNode(facet);
		case QUERY:
			return new QueryFacetNode(facet);
		case UPDATE:
			return new UpdateFacetNode(facet);
		default:
			break;
		}
		return null;
	}

	public static FacetNode createFacet(TLFacet facet) {
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

	public static PropertyNode createIndicator(TLIndicator tlObj, PropertyOwnerInterface parent) {
		PropertyNode nn;
		if (tlObj.isPublishAsElement())
			nn = new IndicatorElementNode(tlObj, parent);
		else
			nn = new IndicatorNode(tlObj, parent);
		return nn;
	}

	public static PropertyNode createProperty(TLProperty tlObj, PropertyOwnerInterface parent) {
		PropertyNode nn;
		if (tlObj.isReference())
			nn = new ElementReferenceNode(tlObj, parent);
		else
			nn = new ElementNode(tlObj, parent);
		return nn;
	}

	/**
	 * Create a new component node and model object and link it to <i>this</i>library's Complex or Simple Root node.
	 * Used for creating model objects from nodes constructed by GUI otmHandlers and wizards.
	 * 
	 * @see {@link NewComponent_Tests.java}
	 * @param n
	 *            template node for name, description and parent
	 * @param type
	 *            objectType strings as defined in ComponentNodeType
	 * @return node created
	 * 
	 */
	public Node newComponent(Node n, final ComponentNodeType type) {
		if (n == null || n.getLibrary() == null)
			return null;
		Node cn = null;
		switch (type) {
		case SERVICE:
			return new ServiceNode(n);
		case ALIAS:
			return new AliasNode(n, n.getName());
		default:
			cn = newComponent(type);
			if (cn != null) {
				cn.setExtensible(true);
				cn.setName(n.getName());
				cn.setDescription(n.getDescription());
				// cn.setIdentity(n.getName());

				if (n.getLibrary().isEditable())
					n.getLibrary().addMember(cn);
				else
					// Put the new node at the head of the chain.
					n.getLibrary().getChain().getHead().addMember(cn);

				if (cn instanceof ChoiceObjectNode) {
					((ChoiceObjectNode) cn).addFacet("A");
					((ChoiceObjectNode) cn).addFacet("B");
				}
			}
		}
		return cn;
	}

}
