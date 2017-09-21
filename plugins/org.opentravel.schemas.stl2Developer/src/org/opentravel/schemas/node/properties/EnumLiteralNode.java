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
import org.opentravel.schemacompiler.model.TLEnumValue;
import org.opentravel.schemas.modelObject.EnumLiteralMO;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.ComponentNodeType;
import org.opentravel.schemas.node.ImpliedNode;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFactory;
import org.opentravel.schemas.node.NodeNameUtils;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.listeners.BaseNodeListener;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.types.TypeProvider;

/**
 * A property node that represents a enumeration literal. See {@link NodeFactory#newMember(INode, Object)}
 * 
 * @author Dave Hollander
 * 
 */

public class EnumLiteralNode extends PropertyNode {

	public EnumLiteralNode(ComponentNode parent, String name) {
		super(new TLEnumValue(), parent, name, PropertyNodeType.ENUM_LITERAL);
		this.setName(name);
		// setAssignedType(getRequiredType());
		// parent.getModelObject().addChild(this.getTLModelObject());
	}

	public EnumLiteralNode(TLEnumValue tlObj, INode parent) {
		super(tlObj, parent, PropertyNodeType.ENUM_LITERAL);

		assert (modelObject instanceof EnumLiteralMO);

	}

	@Override
	public boolean canAssign(Node type) {
		return type instanceof ImpliedNode ? true : false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.PropertyNode#createProperty(org.opentravel.schemas.node.Node)
	 */
	@Override
	public INode createProperty(Node type) {
		TLEnumValue tlObj = (TLEnumValue) cloneTLObj();
		int index = indexOfNode();
		((TLEnumValue) getTLModelObject()).getOwningEnum().addValue(index, tlObj);
		EnumLiteralNode n = new EnumLiteralNode(tlObj, null);
		getParent().getChildren().add(index, n);
		n.setParent(getParent());
		// n.setLibrary(getLibrary());
		n.setName(type.getName());
		return n;
	}

	@Override
	public INode.CommandType getAddCommand() {
		return INode.CommandType.ENUMERATION;
	}

	@Override
	public TypeProvider getAssignedType() {
		return getRequiredType();
	}

	@Override
	public ComponentNodeType getComponentNodeType() {
		return ComponentNodeType.ENUM_LITERAL;
	}

	@Override
	public ImpliedNode getRequiredType() {
		return ModelNode.getUndefinedNode();
	}

	@Override
	public String getName() {
		return emptyIfNull(getTLModelObject().getLiteral());
	}

	@Override
	public TLEnumValue getTLModelObject() {
		return (TLEnumValue) (modelObject != null ? modelObject.getTLModelObj() : null);
	}

	@Override
	public Image getImage() {
		return Images.getImageRegistry().get(Images.RoleValue);
	}

	@Override
	public String getLabel() {
		return getName();
		// return modelObject.getLabel() == null ? "" : modelObject.getLabel();
	}

	@Override
	public BaseNodeListener getNewListener() {
		return null;
	}

	@Override
	public Node getOwningComponent() {
		return getParent();
	}

	@Override
	public boolean isEnabled_AssignType() {
		return false;
	}

	@Override
	public boolean isNavChild(boolean deep) {
		return false;
	}

	@Override
	public boolean isRenameable() {
		return isEditable() && !inherited;
	}

	// @Override
	// public boolean hasNavChildrenWithProperties() {
	// return false;
	// }
	//
	@Override
	public void setName(String name) {
		if (isEditable_newToChain())
			getTLModelObject().setLiteral(NodeNameUtils.fixEnumerationValue(name));
	}

}
