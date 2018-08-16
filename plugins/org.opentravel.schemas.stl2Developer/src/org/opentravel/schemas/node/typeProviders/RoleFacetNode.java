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
package org.opentravel.schemas.node.typeProviders;

import java.util.List;

import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLRole;
import org.opentravel.schemacompiler.model.TLRoleEnumeration;
import org.opentravel.schemas.node.ComponentNodeType;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.handlers.children.RoleEnumerationChildrenHandler;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.properties.PropertyNodeType;
import org.opentravel.schemas.node.properties.RoleNode;
import org.opentravel.schemas.utils.StringComparator;

/**
 * Facet for containing core object roles.
 * 
 * @author Dave Hollander
 * 
 */
public class RoleFacetNode extends FacetProviders {

	public RoleFacetNode(TLRoleEnumeration tlObj) {
		super(tlObj);

		childrenHandler = new RoleEnumerationChildrenHandler(this);
	}

	@Override
	public void add(List<PropertyNode> roles, boolean clone) {
		for (PropertyNode pn : roles)
			add(pn);
	}

	@Override
	public boolean isExtensionPointTarget() {
		return false;
	}

	/**
	 * Add a role property for the name.
	 */
	public RoleNode add(String name) {
		RoleNode r = null;
		if (isEditable_inMinor())
			r = new RoleNode(this, name);
		childrenHandler.clear();
		return r;
	}

	@Override
	public boolean isNavChild(boolean deep) {
		return true;
	}

	@Override
	public void removeProperty(PropertyNode pn) {
		if (pn instanceof RoleNode)
			getTLModelObject().removeRole(((RoleNode) pn).getTLModelObject());
	}

	@Override
	public void add(PropertyNode pn) {
		if (pn instanceof RoleNode) {
			pn.setParent(this);
			pn.addToTL(this);
			// getChildrenHandler().clear();
		}
	}

	@Override
	public void add(PropertyNode pn, int i) {
		add(pn);
	}

	/**
	 * Add a role property for each of the names in the array. This can be any member of a core object.
	 * 
	 * @param names
	 *            - array of role names
	 */
	public void addRoles(String[] names) {
		for (String name : names)
			add(name);
	}

	@Override
	public PropertyNode createProperty(final Node type) {
		PropertyNode pn = new RoleNode(this, type.getName());
		childrenHandler.clear();
		return pn;
	}

	@Override
	public INode.CommandType getAddCommand() {
		return INode.CommandType.ROLE;
	}

	@Override
	public String getComponentType() {
		return ComponentNodeType.ROLES.toString();
	}

	// @Override
	// public boolean isDefaultFacet() {
	// return false;
	// }

	@Override
	public TLFacetType getFacetType() {
		return null;
	}

	@Override
	public String getLabel() {
		return "ROLES";
	}

	@Override
	public String getName() {
		return getComponentType();
	}

	public RoleFacetNode getRoleFacet() {
		return this;
	}

	@Override
	public TLRoleEnumeration getTLModelObject() {
		return (TLRoleEnumeration) tlObj;
	}

	@Override
	public boolean isDeleteable() {
		return false;
	}

	@Override
	public boolean isEnabled_AddProperties() {
		return getOwningComponent().isEnabled_AddProperties();
	}

	@Override
	public boolean isRenameable() {
		return false;
	}

	@Override
	public boolean canOwn(PropertyNode pn) {
		return pn instanceof RoleNode;
	}

	@Override
	public boolean canOwn(PropertyNodeType type) {
		return type.equals(PropertyNodeType.ROLE);
	}

	public void sort() {
		getTLModelObject().sortRoles(new StringComparator<TLRole>() {
			@Override
			protected String getString(TLRole object) {
				return object.getName();
			}
		});
	}
}
