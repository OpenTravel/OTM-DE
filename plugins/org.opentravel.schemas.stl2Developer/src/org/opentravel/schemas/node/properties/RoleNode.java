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
import org.opentravel.schemacompiler.model.TLRole;
import org.opentravel.schemacompiler.model.TLRoleEnumeration;
import org.opentravel.schemas.node.ComponentNodeType;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFactory;
import org.opentravel.schemas.node.interfaces.FacetInterface;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.typeProviders.RoleFacetNode;
import org.opentravel.schemas.properties.Images;

/**
 * A property node that represents a role enumeration value in a core object. See
 * {@link NodeFactory#newMemberOLD(INode, Object)}
 * 
 * @author Dave Hollander
 * 
 */
// TODO - delegate to PropertyNode when possible
public class RoleNode extends UnTypedPropertyNode {

	public RoleNode(RoleFacetNode parent, String name) {
		super(new TLRole(), parent, name);
		// setAssignedType(getRequiredType());
	}

	public RoleNode(TLRole tlObj, RoleFacetNode parent) {
		super(tlObj, parent);
		// setAssignedType(getRequiredType());
	}

	@Override
	public void addToTL(final FacetInterface owner, final int index) {
		if (owner.getTLModelObject() instanceof TLRoleEnumeration)
			try {
				((TLRoleEnumeration) owner.getTLModelObject()).addRole(index, getTLModelObject());
			} catch (IndexOutOfBoundsException e) {
				((TLRoleEnumeration) owner.getTLModelObject()).addRole(getTLModelObject());
			}
		owner.getChildrenHandler().clear();
	}

	// @Override
	// public boolean canAssign(Node type) {
	// return type instanceof ImpliedNode ? true : false;
	// }

	@Override
	public INode createProperty(Node type) {
		// Clone this TL Object
		TLRole tlClone = (TLRole) cloneTLObj();
		PropertyNode n = new RoleNode(tlClone, null);
		return super.createProperty(n, type);
	}

	@Override
	public INode.CommandType getAddCommand() {
		return INode.CommandType.ROLE;
	}

	@Override
	public ComponentNodeType getComponentNodeType() {
		return ComponentNodeType.ROLE;
	}

	@Override
	public Image getImage() {
		return Images.getImageRegistry().get(Images.RoleValue);
	}

	@Override
	public String getName() {
		return emptyIfNull(getTLModelObject().getName());
	}

	// @Override
	// public List<Node> getNavChildren(boolean deep) {
	// return Collections.emptyList();
	// }

	@Override
	public RoleFacetNode getParent() {
		if ((parent == null || parent.isDeleted()) && getTLModelObject() != null)
			// The parent may have failed to rebuild children
			parent = Node.GetNode(getTLModelObject().getRoleEnumeration());
		return (RoleFacetNode) parent;
	}

	@Override
	public TLRole getTLModelObject() {
		return (TLRole) tlObj;
	}

	// @Override
	// public List<Node> getTreeChildren(boolean deep) {
	// return Collections.emptyList();
	// }
	//
	// @Override
	// public boolean hasNavChildren(boolean deep) {
	// return false;
	// }
	//
	// @Override
	// public boolean isNavChild(boolean deep) {
	// return false;
	// }
	//
	// @Override
	// public boolean isRenameable() {
	// return isEditable() && !isInherited();
	// }

	@Override
	public void setName(String name) {
		if (getTLModelObject() != null)
			getTLModelObject().setName(name);
	}

	@Override
	protected void moveDownTL() {
		if (getTLModelObject() != null)
			getTLModelObject().moveDown();
	}

	@Override
	protected void moveUpTL() {
		if (getTLModelObject() != null)
			getTLModelObject().moveUp();
	}

	@Override
	protected void removeFromTL() {
		if (getParent() != null && getParent().getTLModelObject() instanceof TLRoleEnumeration)
			getParent().getTLModelObject().removeRole(getTLModelObject());
	}

}
