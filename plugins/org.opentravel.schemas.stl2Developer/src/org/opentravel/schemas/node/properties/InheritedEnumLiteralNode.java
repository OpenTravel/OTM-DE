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

import org.opentravel.schemacompiler.model.TLEnumValue;
import org.opentravel.schemas.node.ComponentNodeType;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFactory;
import org.opentravel.schemas.node.interfaces.FacadeInterface;
import org.opentravel.schemas.node.interfaces.FacetInterface;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.interfaces.InheritedInterface;

/**
 * A property node that represents a enumeration literal. See {@link NodeFactory#newMemberOLD(INode, Object)}
 * 
 * @author Dave Hollander
 * 
 */
public class InheritedEnumLiteralNode extends EnumLiteralNode implements FacadeInterface, InheritedInterface {

	private EnumLiteralNode inheritedFrom = null;

	public InheritedEnumLiteralNode(EnumLiteralNode from, FacetInterface parent) {
		super();
		inheritedFrom = from;
		this.parent = (Node) parent;

		assert from.getParent() != parent;
	}

	@Override
	public boolean canAssign(Node type) {
		return false;
	}

	@Override
	public INode.CommandType getAddCommand() {
		return INode.CommandType.ENUMERATION;
	}

	@Override
	public ComponentNodeType getComponentNodeType() {
		return ComponentNodeType.ENUM_LITERAL;
	}

	@Override
	public EnumLiteralNode getInheritedFrom() {
		return inheritedFrom;
	}

	@Override
	public String getLabel() {
		return getInheritedFrom().getLabel();
	}

	@Override
	public Node getParent() {
		return parent;
	}

	@Override
	public TLEnumValue getTLModelObject() {
		return getInheritedFrom().getTLModelObject();
	}

	@Override
	public boolean isRenameable() {
		return false;
	}

	@Override
	public void setName(String name) {
	}

	@Override
	public EnumLiteralNode get() {
		return getInheritedFrom();
	}

}
