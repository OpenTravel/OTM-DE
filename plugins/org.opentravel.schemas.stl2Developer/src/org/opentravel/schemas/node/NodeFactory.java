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
import org.opentravel.schemacompiler.model.TLAttributeType;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLEnumValue;
import org.opentravel.schemacompiler.model.TLExtensionPointFacet;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLListFacet;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLOpenEnumeration;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLRole;
import org.opentravel.schemacompiler.model.TLRoleEnumeration;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLSimpleFacet;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemas.modelObject.TLValueWithAttributesFacet;
import org.opentravel.schemas.modelObject.TLnSimpleAttribute;
import org.opentravel.schemas.node.properties.AttributeNode;
import org.opentravel.schemas.node.properties.ElementNode;
import org.opentravel.schemas.node.properties.ElementReferenceNode;
import org.opentravel.schemas.node.properties.EnumLiteralNode;
import org.opentravel.schemas.node.properties.IdNode;
import org.opentravel.schemas.node.properties.IndicatorElementNode;
import org.opentravel.schemas.node.properties.IndicatorNode;
import org.opentravel.schemas.node.properties.PropertyOwnerInterface;
import org.opentravel.schemas.node.properties.RoleNode;
import org.opentravel.schemas.node.properties.SimpleAttributeNode;

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
	public static ComponentNode newComponent(LibraryMember mbr) {
		ComponentNode newNode = newComponent_UnTyped(mbr);
		if (newNode.isTypeUser())
			newNode.getTypeClass().setAssignedTypeForThisNode(newNode);
		for (Node n : newNode.getDescendants_TypeUsers()) {
			n.getTypeClass().setAssignedTypeForThisNode(n);
		}
		return newNode;
	}

	/**
	 * Create a new component and use the type node to set its type.
	 * 
	 * @param mbr
	 * @param type
	 * @return newly created and typed node.
	 */
	public static ComponentNode newComponent(LibraryMember mbr, INode type) {
		ComponentNode newNode = newComponent_UnTyped(mbr);
		if (newNode.isTypeUser())
			newNode.setAssignedType((Node) type);
		return newNode;
	}

	/*******************************************************************************************
	 * New ComponentNode methods that also create new objects in underlying model
	 */
	public static ComponentNode newComponent_UnTyped(final LibraryMember mbr) {
		ComponentNode cn = null;
		if (mbr == null)
			return cn;

		if (mbr instanceof TLValueWithAttributes)
			cn = new VWA_Node(mbr);
		else if (mbr instanceof TLBusinessObject)
			cn = new BusinessObjectNode(mbr);
		else if (mbr instanceof TLCoreObject)
			cn = new CoreObjectNode(mbr);
		else if (mbr instanceof TLSimple)
			cn = new SimpleTypeNode(mbr);
		else if (mbr instanceof TLOpenEnumeration)
			cn = new EnumerationOpenNode(mbr);
		else if (mbr instanceof TLClosedEnumeration)
			cn = new EnumerationClosedNode(mbr);
		else if (mbr instanceof TLExtensionPointFacet)
			cn = new ExtensionPointNode(mbr);
		else {
			cn = new ComponentNode(mbr);
			// LOGGER.debug("Using default factory type for " + mbr.getClass().getSimpleName());
		}

		return cn;
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
		// LOGGER.debug("adding "+ tlObj.getClass().getCanonicalName());
		// Properties
		if (tlObj instanceof TLProperty) {
			if (((TLProperty) tlObj).isReference())
				nn = new ElementReferenceNode((TLModelElement) tlObj, (PropertyOwnerInterface) parent);
			else
				nn = new ElementNode((TLModelElement) tlObj, parent);
		} else if (tlObj instanceof TLIndicator) {
			if (((TLIndicator) tlObj).isPublishAsElement())
				nn = new IndicatorElementNode((TLModelElement) tlObj, (PropertyOwnerInterface) parent);
			else
				nn = new IndicatorNode((TLModelElement) tlObj, (PropertyOwnerInterface) parent);
		} else if (tlObj instanceof TLAttribute) {
			TLAttributeType type = ((TLAttribute) tlObj).getType();
			if (type != null && type.getNamespace() != null && type.getNamespace().equals(Node.XSD_NAMESPACE)
					&& type.getLocalName().equals("ID"))
				nn = new IdNode((TLModelElement) tlObj, (PropertyOwnerInterface) parent);
			else
				nn = new AttributeNode((TLModelElement) tlObj, (PropertyOwnerInterface) parent);
		} else if (tlObj instanceof TLRole) {
			nn = new RoleNode((TLRole) tlObj, (RoleFacetNode) parent);
		} else if (tlObj instanceof TLEnumValue) {
			nn = new EnumLiteralNode((TLModelElement) tlObj, parent);
		} else if (tlObj instanceof TLnSimpleAttribute) {
			nn = new SimpleAttributeNode((TLModelElement) tlObj, parent);
		} else if (tlObj instanceof TLAlias) {
			nn = new AliasNode((Node) parent, (TLAlias) tlObj);
			// Facets
		} else if (tlObj instanceof TLValueWithAttributesFacet) {
			nn = new FacetNode((TLValueWithAttributesFacet) tlObj);
		} else if (tlObj instanceof TLFacet) {
			nn = createFacet((TLFacet) tlObj);
		} else if (tlObj instanceof TLListFacet) {
			nn = new FacetNode((TLListFacet) tlObj);
		} else if (tlObj instanceof TLSimpleFacet) {
			nn = new SimpleFacetNode((TLSimpleFacet) tlObj);
		} else if (tlObj instanceof TLRoleEnumeration) {
			nn = new RoleFacetNode((TLRoleEnumeration) tlObj);
		} else if (tlObj instanceof TLOperation) {
			nn = new OperationNode((TLOperation) tlObj);
			// Others
		} else if (tlObj instanceof TLModelElement) {
			nn = new ComponentNode((TLModelElement) tlObj);
			// LOGGER.debug("newComponentNode() - generic source TLModelElement type. "
			// + tlObj.getClass().getSimpleName());
		}
		if (parent != null && nn.getParent() == null) {
			((Node) parent).linkChild(nn, false);
			nn.setLibrary(parent.getLibrary());
		}

		return nn;
	}

	/**
	 * Create a new component of the same class and add it to a library.
	 * 
	 * @param n
	 *            class of object to create - must be named type
	 * @param ln
	 *            library to add member or NULL
	 * @param nameRoot
	 *            string to put before the class name as the object name or NULL for unnamed.
	 * @return newly created library member
	 */
	public static Node newComponentMember(Node n, LibraryNode ln, String nameRoot) {
		Node newNode = null;
		if (n instanceof BusinessObjectNode)
			newNode = new BusinessObjectNode(new TLBusinessObject());
		if (n instanceof CoreObjectNode)
			newNode = new CoreObjectNode(new TLCoreObject());
		if (n instanceof VWA_Node)
			newNode = new VWA_Node(new TLValueWithAttributes());
		if (n instanceof EnumerationOpenNode)
			newNode = new EnumerationOpenNode(new TLOpenEnumeration());
		// Must test for closed enum before simple type because it extends simple
		if (n instanceof EnumerationClosedNode)
			newNode = new EnumerationClosedNode(new TLClosedEnumeration());
		else if (n instanceof SimpleTypeNode)
			newNode = new SimpleTypeNode(new TLSimple());
		if (n instanceof ExtensionPointNode)
			newNode = new ExtensionPointNode(new TLExtensionPointFacet());

		if (nameRoot != null)
			newNode.setName(nameRoot + n.getClass().getSimpleName());

		if (newNode != null && ln != null)
			ln.addMember(newNode);

		return newNode;
	}

	private static ComponentNode createFacet(TLFacet facet) {
		switch (facet.getFacetType()) {
		case CUSTOM:
		case QUERY:
			return new RenamableFacet(facet);
		default:
			return new FacetNode(facet);
		}
	}
}
