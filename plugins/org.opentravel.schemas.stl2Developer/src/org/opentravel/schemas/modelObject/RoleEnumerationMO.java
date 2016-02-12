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
package org.opentravel.schemas.modelObject;

import java.util.List;

import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLRole;
import org.opentravel.schemacompiler.model.TLRoleEnumeration;

/**
 * A facet like class that contains a list of roles.
 * 
 * Can not keep a link to the live list as the TLCore object returns an unmodifiable list.
 * 
 * @author Dave Hollander
 * 
 */
public class RoleEnumerationMO extends ModelObject<TLRoleEnumeration> {

	public RoleEnumerationMO(final TLRoleEnumeration obj) {
		super(obj);
	}

	@Override
	public boolean addChild(final TLModelElement role) {
		if (role instanceof TLRole)
			addRole((TLRole) role);
		else
			return false;
		return true;
	}

	public void addRole(final TLRole role) {
		getTLModelObj().addRole(role);
	}

	public void addRole(int index, TLRole tlModelObj) {
		getTLModelObj().addRole(index, tlModelObj);
	}

	// @Override
	// public boolean isRole() {
	// return true;
	// }

	// @Override
	// public boolean isRoleFacet() {
	// return true;
	// }
	//
	// @Override
	// public boolean isRoleProperty() {
	// return false;
	// }

	@Override
	public boolean isComplexAssignable() {
		return true;
	}

	@Override
	public String getComponentType() {
		return getTLModelObj().getLocalName();
	}

	/**
	 * Set the name on a role - can't be done. Model objects do not know who their parentNode is. There is no
	 * TLModelElement for roles. The caller must use the parentNode core object setRole method instead OR use the
	 * setName method on the Property Node.
	 */
	@Override
	public boolean setName(final String name) {
		return false;
	}

	@Override
	public List<?> getChildren() {
		return getTLModelObj().getRoles();
	}

	@Override
	public String getLabel() {
		return "Roles";
	}

	@Override
	protected AbstractLibrary getLibrary(final TLRoleEnumeration obj) {
		return getTLModelObj().getOwningLibrary();
	}

	@Override
	public String getName() {
		return getTLModelObj().getLocalName();
	}

	@Override
	public String getNamePrefix() {
		return "";
	}

	@Override
	public String getNamespace() {
		return getTLModelObj().getNamespace();
	}

	@Override
	public TLRoleEnumeration getTLModelObj() {
		return srcObj;
	}

	@Override
	public void delete() {
	}

}
