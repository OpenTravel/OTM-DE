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

import org.opentravel.schemacompiler.model.TLRole;

public class RolePropertyMO extends ModelObject<TLRole> {

	public RolePropertyMO(final TLRole obj) {
		super(obj);
	}

	public RolePropertyMO() {
	}

	// @Override
	// public String getComponentType() {
	// return "Role";
	// }

	@Override
	public boolean setName(final String name) {
		if (getTLModelObj() == null) {
			return false;
		}
		getTLModelObj().setName(name);
		return true;
	}

	// @Override
	// protected AbstractLibrary getLibrary(final TLRole obj) {
	// return null;
	// }

	// @Override
	// public String getName() {
	// return getTLModelObj() != null ? getTLModelObj().getName() : "";
	// }
	//
	// @Override
	// public String getNamePrefix() {
	// return null;
	// }
	//
	// @Override
	// public String getNamespace() {
	// return "";
	// }

	@Override
	public TLRole getTLModelObj() {
		return srcObj;
	}

	@Override
	public void delete() {
		final TLRole tlModel = getTLModelObj();
		if (tlModel != null && tlModel.getRoleEnumeration() != null) {
			tlModel.getRoleEnumeration().removeRole(tlModel);
		}
	}

	@Override
	public void addToTLParent(final ModelObject<?> parentMO, int index) {
		if (parentMO instanceof RoleEnumerationMO) {
			((RoleEnumerationMO) parentMO).addRole(index, getTLModelObj());
		}
	}

	@Override
	public void addToTLParent(final ModelObject<?> parentMO) {
		if (parentMO instanceof RoleEnumerationMO) {
			((RoleEnumerationMO) parentMO).addRole(getTLModelObj());
		}
	}

	private int indexOf() {
		final TLRole thisProp = getTLModelObj();
		return getTLModelObj().getRoleEnumeration().getRoles().indexOf(thisProp);
	}

	@Override
	public boolean moveUp() {
		if (indexOf() > 0) {
			getTLModelObj().moveUp();
			return true;
		}
		return false;
	}

	@Override
	public boolean moveDown() {
		// only count attributes, not elements or indicators
		if (indexOf() + 1 < getTLModelObj().getRoleEnumeration().getRoles().size()) {
			getTLModelObj().moveDown();
			return true;
		}
		return false;
	}

}
