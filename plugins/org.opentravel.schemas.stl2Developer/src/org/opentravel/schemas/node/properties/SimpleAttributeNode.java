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
package org.opentravel.schemas.node.properties;

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemacompiler.model.TLFacetOwner;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemas.modelObject.TLnSimpleAttribute;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFactory;
import org.opentravel.schemas.node.PropertyNodeType;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.types.TypeProvider;

/**
 * A property node that represents a simple property of a core or value with attributes object. See
 * {@link NodeFactory#newComponentMember(INode, Object)}
 * 
 * @author Dave Hollander
 * 
 */

public class SimpleAttributeNode extends PropertyNode {
	// private static final Logger LOGGER = LoggerFactory.getLogger(SimpleAttributeNode.class);

	public SimpleAttributeNode(TLModelElement tlObj, INode parent) {
		super(tlObj, parent, PropertyNodeType.SIMPLE);

		if (parent != null) {
			TLModelElement tlOwner = ((Node) parent.getParent()).getTLModelObject();
			if ((tlOwner instanceof TLFacetOwner) || (tlObj instanceof TLnSimpleAttribute))
				((TLnSimpleAttribute) tlObj).setParentObject(tlOwner);

			// Since the type assigned to this is the same as the parent facet, share the type class
			// type = ((Node) parent).getTypeClass();
		}
	}

	@Override
	public boolean canAssign(Node type) {
		return type instanceof TypeProvider ? ((TypeProvider) type).isAssignableToSimple() : false;
	}

	@Override
	public INode createProperty(Node type) {
		// Need for DND but can't actually create a property, just set the type.
		if (type instanceof TypeProvider)
			setAssignedType((TypeProvider) type);
		return this;
	}

	/**
	 * Simple Attribute Properties are new to a chain if their parent is new. Override the behavior in the property
	 * class.
	 */
	@Override
	public boolean isNewToChain() {
		if (getChain() == null || super.isNewToChain())
			return true; // the parent is new so must be its properties
		return false;
	}

	@Override
	public boolean isOnlySimpleTypeUser() {
		return true;
	}

	@Override
	public Image getImage() {
		return Images.getImageRegistry().get(Images.XSDAttribute);
	}

	@Override
	public String getLabel() {
		return modelObject.getLabel() == null ? "" : modelObject.getLabel();
	}

	@Override
	public TypeProvider getAssignedType() {
		return typeHandler.get();
		// return (TypeProvider) getTypeClass().getTypeNode();
	}

	@Override
	public void setName(String name) {
		// LOGGER.debug("Tried to set the name of a simple property.");
	}

}
