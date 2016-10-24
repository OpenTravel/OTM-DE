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

import java.util.Collections;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLRole;
import org.opentravel.schemas.node.ImpliedNode;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFactory;
import org.opentravel.schemas.node.PropertyNodeType;
import org.opentravel.schemas.node.facets.RoleFacetNode;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.types.TypeProvider;

/**
 * A property node that represents a role enumeration value in a core object. See
 * {@link NodeFactory#newComponentMember(INode, Object)}
 * 
 * @author Dave Hollander
 * 
 */

public class RoleNode extends PropertyNode {

	public RoleNode(RoleFacetNode parent, String name) {
		super(new TLRole(), parent, name, PropertyNodeType.ROLE);
		setAssignedType(getRequiredType());
	}

	public RoleNode(TLModelElement tlObj, RoleFacetNode parent) {
		super(tlObj, parent, PropertyNodeType.ROLE);
		setAssignedType(getRequiredType());
	}

	@Override
	public boolean canAssign(Node type) {
		return type instanceof ImpliedNode ? true : false;
	}

	@Override
	public INode createProperty(Node type) {
		TLRole tlObj = (TLRole) cloneTLObj();
		int index = indexOfNode();
		((TLRole) getTLModelObject()).getRoleEnumeration().addRole(index, tlObj);
		RoleNode n = new RoleNode(tlObj, null);

		getParent().getChildren().add(index, n);
		n.setParent(getParent());
		setLibrary(getParent().getLibrary());
		n.setName(type.getName());
		n.setDescription(type.getDescription());
		return n;
	}

	@Override
	public INode.CommandType getAddCommand() {
		return INode.CommandType.ROLE;
	}

	@Override
	public TypeProvider getAssignedType() {
		return getRequiredType();
	}

	@Override
	public ImpliedNode getRequiredType() {
		return ModelNode.getUndefinedNode();
	}

	@Override
	public List<Node> getTreeChildren(boolean deep) {
		return Collections.emptyList();
	}

	@Override
	public List<Node> getNavChildren(boolean deep) {
		return Collections.emptyList();
	}

	@Override
	public boolean hasNavChildren(boolean deep) {
		return false;
	}

	@Override
	public Image getImage() {
		return Images.getImageRegistry().get(Images.RoleValue);
	}

	@Override
	public String getLabel() {
		return modelObject.getLabel() == null ? "" : modelObject.getLabel();
	}

	@Override
	public RoleFacetNode getParent() {
		return (RoleFacetNode) parent;
	}

	@Override
	public boolean isNavChild(boolean deep) {
		return false;
	}

	@Override
	public void setName(String name) {
		modelObject.setName(name);
	}

}
