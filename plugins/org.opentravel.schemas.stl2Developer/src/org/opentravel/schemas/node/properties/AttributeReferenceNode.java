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
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemas.modelObject.AttributeMO;
import org.opentravel.schemas.node.BusinessObjectNode;
import org.opentravel.schemas.node.ComponentNodeType;
import org.opentravel.schemas.node.CoreObjectNode;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeNameUtils;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.types.TypeProvider;

/**
 * An attribute property that represents a reference to an object.
 * 
 * @see ElementReferenceNode
 * 
 * @author Dave Hollander
 * 
 */
public class AttributeReferenceNode extends AttributeNode {

	public AttributeReferenceNode(PropertyOwnerInterface parent, String name) {
		this(parent, name, ModelNode.getUnassignedNode());
	}

	public AttributeReferenceNode(PropertyOwnerInterface parent, String name, PropertyNodeType type) {
		super(parent, name);
		getTLModelObject().setReference(true);
		setAssignedType((TypeProvider) ModelNode.getUnassignedNode());
	}

	public AttributeReferenceNode(PropertyOwnerInterface facet, String name, TypeProvider reference) {
		super(facet, name, reference);
		getTLModelObject().setReference(true);
		setAssignedType(reference);
	}

	/**
	 * 
	 * @param tlObj
	 *            TLAttribute
	 * @param parent
	 *            can be null
	 */
	public AttributeReferenceNode(TLAttribute tlObj, PropertyOwnerInterface parent) {
		super(tlObj, parent);
		getTLModelObject().setReference(true);

		assert (modelObject instanceof AttributeMO);
	}

	@Override
	public boolean canAssign(Node type) {
		if (type instanceof BusinessObjectNode)
			return true;
		if (type instanceof CoreObjectNode)
			return true;
		return false;
	}

	@Override
	public INode createProperty(Node type) {
		TLAttribute tlObj = (TLAttribute) cloneTLObj();
		tlObj.setReference(true);

		getTLModelObject().getOwner().addAttribute(tlObj);
		AttributeReferenceNode n = new AttributeReferenceNode(tlObj, null);
		n.setName(type.getName());
		getParent().linkChild(n);
		n.setDescription(type.getDescription());
		if (type instanceof TypeProvider)
			n.setAssignedType((TypeProvider) type);
		return n;
	}

	@Override
	public ComponentNodeType getComponentNodeType() {
		return ComponentNodeType.ATTRIBUTE_REF;
	}

	@Override
	public Image getImage() {
		return Images.getImageRegistry().get(Images.ID_Attr_Reference);
	}

	@Override
	public String getLabel() {
		return getName();
	}

	@Override
	public boolean isRenameable() {
		return false;
	}

	@Override
	public void setName(String name) {
		getTLModelObject().setName(NodeNameUtils.fixAttributeRefName(getTypeName()));
	}

}
