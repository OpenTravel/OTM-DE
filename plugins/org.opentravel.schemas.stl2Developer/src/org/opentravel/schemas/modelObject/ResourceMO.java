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

import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLResource;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

public class ResourceMO extends ModelObject<TLResource> {
	// private static final Logger LOGGER = LoggerFactory.getLogger(ChoiceObjMO.class);

	public ResourceMO(final TLResource obj) {
		super(obj);
	}

	// It may already have been taken out of the library, but if not do so.
	@Override
	public void delete() {
		if (getTLModelObj().getOwningLibrary() != null)
			getTLModelObj().getOwningLibrary().removeNamedMember(getTLModelObj());
	}

	@Override
	public List<?> getChildren() {
		final List<TLModelElement> kids = new ArrayList<TLModelElement>();
		return kids;
	}

	@Override
	public String getComponentType() {
		return "Resource Object";
	}

	@Override
	public String getName() {
		return getTLModelObj().getName();
	}

	@Override
	public String getNamePrefix() {
		final TLLibrary lib = (TLLibrary) getLibrary(getTLModelObj());
		return lib == null ? "" : lib.getPrefix();
	}

	@Override
	public String getNamespace() {
		return getTLModelObj().getNamespace();
	}

	@Override
	public boolean setName(final String name) {
		getTLModelObj().setName(name);
		return true;
	}

	@Override
	protected AbstractLibrary getLibrary(final TLResource obj) {
		return obj.getOwningLibrary();
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
	//
	// @Override
	// public NamedEntity getTLBase() {
	// return srcObj.getExtension() != null ? srcObj.getExtension().getExtendsEntity() : null;
	// }
	//
	// @Override
	// public boolean isComplexAssignable() {
	// return true;
	// }
	//
	// @Override
	// public boolean isExtendedBy(NamedEntity extension) {
	// if (this.getTLModelObj() == null)
	// return false;
	// if (this.getTLModelObj().getExtension() == null)
	// return false;
	// if (this.getTLModelObj().getExtension().getValidationIdentity() == null)
	// return false;
	// if (extension == null)
	// return false;
	// if (extension.getValidationIdentity() == null)
	// return false;
	//
	// if (this.getTLModelObj().getExtension().getExtendsEntity() == extension)
	// return true;
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
	//

}
