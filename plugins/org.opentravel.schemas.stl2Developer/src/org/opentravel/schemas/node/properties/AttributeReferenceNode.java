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
import org.opentravel.schemas.node.AliasNode;
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
public class AttributeReferenceNode extends PropertyNode {

	public AttributeReferenceNode(PropertyOwnerInterface parent, String name) {
		this(parent, name, ModelNode.getUnassignedNode());
	}

	// TODO - refactor to property node
	public AttributeReferenceNode(PropertyOwnerInterface parent, String name, PropertyNodeType type) {
		super(new TLAttribute(), (Node) parent, name, type);
		getTLModelObject().setReference(true);
		setAssignedType((TypeProvider) ModelNode.getUnassignedNode());
	}

	public AttributeReferenceNode(PropertyOwnerInterface facet, String name, TypeProvider reference) {
		super(new TLAttribute(), (Node) facet, name, PropertyNodeType.ID_ATTR_REF);
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
		super(tlObj, (INode) parent, PropertyNodeType.ID_ATTR_REF);
		getTLModelObject().setReference(true);

		assert (modelObject instanceof AttributeMO);
	}

	// /*
	// * used for sub-types
	// */
	// public AttributeReferenceNode(TLModelElement tlObj, PropertyOwnerInterface parent, PropertyNodeType type) {
	// super(tlObj, (INode) parent, type);
	// getTLModelObject().setReference(true);
	// }

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
	public String getName() {
		return emptyIfNull(getTLModelObject().getName());
	}

	@Override
	public TLAttribute getTLModelObject() {
		return (TLAttribute) (modelObject != null ? modelObject.getTLModelObj() : null);
	}

	@Override
	public int indexOfTLProperty() {
		return getTLModelObject().getOwner().getAttributes().indexOf(getTLModelObject());
	}

	// @Override
	// public boolean isMandatory() {
	// return getTLModelObject().isMandatory();
	// }

	@Override
	public boolean isRenameable() {
		return false;
	}

	/**
	 * Override to provide GUI assist: Since attributes can be renamed, there is no need to use the alias. Aliases are
	 * not TLAttributeType members so the GUI assist must convert before assignment.
	 */
	@Override
	public boolean setAssignedType(TypeProvider provider) {
		if (provider instanceof AliasNode)
			provider = (TypeProvider) ((Node) provider).getOwningComponent();
		return typeHandler.set(provider);
	}

	// /**
	// * Allowed in major versions and on objects new in a minor.
	// */
	// public void setMandatory(final boolean selection) {
	// if (isEditable_newToChain())
	// if (getOwningComponent().isNewToChain() || !getLibrary().isInChain())
	// getTLModelObject().setMandatory(selection);
	// }

	@Override
	public void setName(String name) {
		getTLModelObject().setName(NodeNameUtils.fixAttributeRefName(getTypeName()));
		// getTLModelObject().setName(NodeNameUtils.fixAttributeName(name));
	}

}
