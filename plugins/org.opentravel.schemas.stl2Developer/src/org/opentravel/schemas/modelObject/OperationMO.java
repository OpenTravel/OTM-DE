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

import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dave Hollander
 * 
 */
public class OperationMO extends ModelObject<TLOperation> {

	private static final Logger LOGGER = LoggerFactory.getLogger(OperationMO.class);

	public OperationMO(final TLOperation obj) {
		super(obj);
	}

	@Override
	public void delete() {
		if (getTLModelObj().getOwningService() != null)
			getTLModelObj().getOwningService().removeOperation(this.getTLModelObj());
	}

	@Override
	public List<?> getChildren() {
		final List<TLModelElement> kids = new ArrayList<TLModelElement>();
		kids.add(getTLModelObj().getRequest());
		kids.add(getTLModelObj().getResponse());
		kids.add(getTLModelObj().getNotification());
		return kids;
	}

	// @Override
	// public String getComponentType() {
	// return "Operation";
	// }

	// @Override
	// protected AbstractLibrary getLibrary(final TLOperation obj) {
	// return null;
	// }

	// @Deprecated
	// @Override
	// public String getName() {
	// // LOGGER.debug("OperationName from getTLModelObj().getLocalName():" +
	// // getTLModelObj().getLocalName());
	// return getTLModelObj().getName();
	//
	// }

	// @Override
	// public String getNamePrefix() {
	// return "";
	// }
	//
	// @Override
	// public String getNamespace() {
	// return getTLModelObj().getNamespace();
	// }

	@Override
	public TLOperation getTLModelObj() {
		return srcObj;
	}

	@Override
	public boolean setName(final String name) {
		getTLModelObj().setName(name);
		return true;
	}

	/**
	 * @see org.opentravel.schemas.modelObject.ModelObject#getExtendsType()
	 */
	@Override
	public String getExtendsType() {
		TLExtension tlExtension = getTLModelObj().getExtension();
		String extendsTypeName = "";

		if (tlExtension != null) {
			if (tlExtension.getExtendsEntity() != null)
				extendsTypeName = tlExtension.getExtendsEntity().getLocalName();
			else
				extendsTypeName = "--base type can not be found--";
		}
		return extendsTypeName;
	}

	@Override
	public String getExtendsTypeNS() {
		TLExtension tlExtension = getTLModelObj().getExtension();
		return tlExtension != null ? tlExtension.getExtendsEntity().getNamespace() : "";
	}

	/**
	 * @see org.opentravel.schemas.modelObject.ModelObject#setExtendsType(org.opentravel.schemas.modelObject.ModelObject)
	 */
	@Override
	public void setExtendsType(ModelObject<?> mo) {
		if (mo == null) {
			getTLModelObj().setExtension(null);

		} else {
			TLExtension tlExtension = getTLModelObj().getExtension();

			if (tlExtension == null) {
				tlExtension = new TLExtension();
				getTLModelObj().setExtension(tlExtension);
			}
			tlExtension.setExtendsEntity((NamedEntity) mo.getTLModelObj());
		}
	}

}
