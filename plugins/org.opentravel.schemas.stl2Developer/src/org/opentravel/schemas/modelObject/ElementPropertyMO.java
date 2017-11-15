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

import org.opentravel.schemacompiler.model.TLProperty;

public class ElementPropertyMO extends ModelObject<TLProperty> {
	// private static final Logger LOGGER = LoggerFactory.getLogger(ElementPropertyMO.class);

	public ElementPropertyMO(final TLProperty obj) {
		super(obj);
		// if (obj.getType() != null) {
		// super.setTLType(obj.getType());
		// }
	}

	// @Override
	// public void delete() {
	// if (getTLModelObj().getOwner() != null) {
	// (getTLModelObj().getOwner()).removeProperty(getTLModelObj());
	// }
	// }

	// @Override
	// public void addToTLParent(final ModelObject<?> parentMO, int index) {
	// if (parentMO.getTLModelObj() instanceof TLFacet) {
	// index = index > ((TLFacet) parentMO.getTLModelObj()).getElements().size() ? ((TLFacet) parentMO
	// .getTLModelObj()).getElements().size() : index;
	// ((TLFacet) parentMO.getTLModelObj()).addElement(index, getTLModelObj());
	// } else if (parentMO.getTLModelObj() instanceof TLExtensionPointFacet) {
	// ((TLExtensionPointFacet) parentMO.getTLModelObj()).addElement(index, getTLModelObj());
	// }
	// }
	//
	// @Override
	// public void addToTLParent(final ModelObject<?> parentMO) {
	// if (parentMO.getTLModelObj() instanceof TLFacet) {
	// ((TLFacet) parentMO.getTLModelObj()).addElement(getTLModelObj());
	// } else if (parentMO.getTLModelObj() instanceof TLExtensionPointFacet) {
	// ((TLExtensionPointFacet) parentMO.getTLModelObj()).addElement(getTLModelObj());
	// }
	// }

	// @Override
	// public void removeFromTLParent() {
	// if (getTLModelObj().getOwner() != null) {
	// getTLModelObj().getOwner().removeProperty(getTLModelObj());
	// }
	// }

	@Override
	public TLProperty getTLModelObj() {
		return srcObj;
	}

	// @Override
	// public NamedEntity getTLType() {
	// return srcObj.getType();
	// }

	// /**
	// * Get the index (0..sizeof()) of this property in the facet list. Use Node.indexTLProperty().
	// */
	// protected int indexOf() {
	// final TLProperty thisProp = getTLModelObj();
	// return thisProp.getOwner().getElements().indexOf(thisProp);
	// }

	// /**
	// * Business logic about correct name done at node level.
	// */
	// @Override
	// public boolean setName(final String name) {
	// getTLModelObj().setName(name);
	// // LOGGER.debug("Set name to " + name);
	// return true;
	// }

	// /**
	// * Move if you can, return false if you can not.
	// *
	// * @return
	// */
	// @Override
	// public boolean moveUp() {
	// if (indexOf() > 0) {
	// getTLModelObj().moveUp();
	// return true;
	// }
	// return false;
	// }
	//
	// @Override
	// public boolean moveDown() {
	// if (indexOf() + 1 < getTLModelObj().getOwner().getElements().size()) {
	// getTLModelObj().moveDown();
	// return true;
	// }
	// return false;
	// }

	// @Override
	// public void setTLType(final NamedEntity tlObj) {
	// getTLModelObj().setType((TLPropertyType) tlObj);
	// }

	// @Override
	// public boolean setRepeat(final int cnt) {
	// getTLModelObj().setRepeat(cnt);
	// return true;
	// }
}
