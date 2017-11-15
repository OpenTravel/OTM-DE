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

import java.util.ArrayList;
import java.util.List;

import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLModelElement;

public class ChoiceObjMO extends ModelObject<TLChoiceObject> {
	// private static final Logger LOGGER = LoggerFactory.getLogger(ChoiceObjMO.class);

	public ChoiceObjMO(final TLChoiceObject obj) {
		super(obj);
	}

	// // It may already have been taken out of the library, but if not do so.
	// @Override
	// public void delete() {
	// if (getTLModelObj().getOwningLibrary() != null)
	// getTLModelObj().getOwningLibrary().removeNamedMember(getTLModelObj());
	// }

	@Override
	public List<?> getChildren() {
		final List<TLModelElement> kids = new ArrayList<TLModelElement>();
		kids.add(getTLModelObj().getSharedFacet());
		kids.addAll(getTLModelObj().getChoiceFacets());
		kids.addAll(getTLModelObj().getAliases());
		return kids;
	}

	// @Override
	// public String getExtendsType() {
	// TLExtension tlExtension = getTLModelObj().getExtension();
	// String extendsTypeName = "";
	//
	// if (tlExtension != null) {
	// if (tlExtension.getExtendsEntity() != null)
	// extendsTypeName = tlExtension.getExtendsEntity().getLocalName();
	// else
	// extendsTypeName = "--base type can not be found--";
	// }
	// return extendsTypeName;
	// }

	// @Override
	// public String getExtendsTypeNS() {
	// TLExtension tlExtension = getTLModelObj().getExtension();
	// String extendsNS;
	//
	// if ((tlExtension != null) && (tlExtension.getExtendsEntity() != null)) {
	// extendsNS = tlExtension.getExtendsEntity().getNamespace();
	// } else {
	// extendsNS = "";
	// }
	// return extendsNS;
	// }

	// @Override
	// public NamedEntity getTLBase() {
	// return srcObj.getExtension() != null ? srcObj.getExtension().getExtendsEntity() : null;
	// }

	@Override
	public TLChoiceObject getTLModelObj() {
		return srcObj;
	}

	// /**
	// * Is this Choice extended by <i>extension</i>?
	// */
	// @Override
	// public boolean isExtendedBy(NamedEntity extension) {
	// if (extension == null || !(extension instanceof TLChoiceObject))
	// return false;
	// if (extension.getValidationIdentity() == null)
	// return false;
	//
	// if (getTLModelObj() != null)
	// if (getTLModelObj().getExtension() != null)
	// if (getTLModelObj().getExtension().getValidationIdentity() != null)
	// return getTLModelObj().getExtension().getExtendsEntity() == extension;
	// return false;
	// }

	// /**
	// * @see
	// org.opentravel.schemas.modelObject.ModelObject#setExtendsType(org.opentravel.schemas.modelObject.ModelObject)
	// */
	// @Override
	// public void setExtendsType(ModelObject<?> mo) {
	// if (mo == null) {
	// getTLModelObj().setExtension(null);
	// } else {
	// TLExtension tlExtension = getTLModelObj().getExtension();
	// if (tlExtension == null) {
	// tlExtension = new TLExtension();
	// getTLModelObj().setExtension(tlExtension);
	// }
	// tlExtension.setExtendsEntity((NamedEntity) mo.getTLModelObj());
	// }
	// }

	// @Override
	// public boolean setName(final String name) {
	// getTLModelObj().setName(name);
	// return true;
	// }

}
