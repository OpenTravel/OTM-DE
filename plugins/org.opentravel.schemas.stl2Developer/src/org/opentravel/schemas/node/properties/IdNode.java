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

import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemas.node.INode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFactory;
import org.opentravel.schemas.node.NodeFinders;
import org.opentravel.schemas.node.PropertyNodeType;
import org.opentravel.schemas.types.TypeUser;

/**
 * A property node that represents an XML ID. See {@link NodeFactory#newComponentMember(INode, Object)}
 * 
 * @author Dave Hollander
 * 
 */
public class IdNode extends AttributeNode implements TypeUser {
	Node idType = null;

	public IdNode(Node parent, String name) {
		super(parent, name, PropertyNodeType.ID);
		setName("id");
		idType = NodeFinders.findNodeByName("ID", XSD_NAMESPACE);
		setAssignedType(idType);
		setIdentity("xml_ID on " + parent.getOwningComponent());
		propertyType = PropertyNodeType.ID;
	}

	public IdNode(TLModelElement tlObj, INode parent) {
		super(tlObj, parent, PropertyNodeType.ID);
		idType = NodeFinders.findNodeByName("ID", XSD_NAMESPACE);
		setAssignedType(idType);
		setIdentity("xml_ID on " + getOwningComponent());
		propertyType = PropertyNodeType.ID;
	}

	@Override
	public boolean canAssign(Node type) {
		return (type == idType);
	}

	@Override
	public boolean isTypeUser() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.properties.PropertyNode#setAssignedType(org.opentravel.schemas.node.Node,
	 * boolean)
	 */
	// @Override
	// public boolean setAssignedType(Node replacement, boolean refresh) {
	// return super.setAssignedType(idType);
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.properties.PropertyNode#setAssignedType(org.opentravel.schemas.node.Node)
	 */
	@Override
	public boolean setAssignedType(Node replacement) {
		return super.setAssignedType(idType);
	}

}
