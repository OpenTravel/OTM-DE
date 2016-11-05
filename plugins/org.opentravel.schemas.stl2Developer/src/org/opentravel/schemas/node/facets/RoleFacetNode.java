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
package org.opentravel.schemas.node.facets;

import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLRoleEnumeration;
import org.opentravel.schemas.modelObject.RoleEnumerationMO;
import org.opentravel.schemas.node.ComponentNodeType;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.PropertyNodeType;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.properties.RoleNode;

/**
 * 
 * @author Dave Hollander
 * 
 */
public class RoleFacetNode extends PropertyOwnerNode {

	public RoleFacetNode(TLRoleEnumeration tlObj) {
		super(tlObj);

		assert (modelObject instanceof RoleEnumerationMO);
	}

	/**
	 * Add a role property for the name. This can be any member of a core object.
	 * 
	 * @param name
	 *            - role names
	 */
	public RoleNode addRole(String name) {
		if (isEditable_inMinor())
			return new RoleNode(this, name);
		return null;
	}

	/**
	 * Add a role property for each of the names in the array. This can be any member of a core object.
	 * 
	 * @param names
	 *            - array of role names
	 */
	public void addRoles(String[] names) {
		for (String name : names)
			addRole(name);
	}

	@Override
	public INode createProperty(final Node type) {
		return new RoleNode(this, type.getName());
	}

	@Override
	public INode.CommandType getAddCommand() {
		return INode.CommandType.ROLE;
	}

	@Override
	public RoleFacetNode getRoleFacet() {
		return this;
	}

	@Override
	public boolean isDefaultFacet() {
		return false;
	}

	@Override
	public boolean isValidParentOf(PropertyNodeType type) {
		return type.equals(PropertyNodeType.ROLE);
	}

	@Override
	public TLRoleEnumeration getTLModelObject() {
		return (TLRoleEnumeration) (getModelObject() != null ? getModelObject().getTLModelObj() : null);

	}

	@Override
	public TLFacetType getFacetType() {
		return null;
	}

	@Override
	public String getComponentType() {
		return ComponentNodeType.ROLES.toString();
	}

	@Override
	public String getName() {
		return getComponentType();
	}

}
