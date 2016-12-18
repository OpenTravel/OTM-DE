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

import org.opentravel.schemacompiler.model.NamedEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleAttributeMO extends ModelObject<TLnSimpleAttribute> {
	@SuppressWarnings("unused")
	private final static Logger LOGGER = LoggerFactory.getLogger(SimpleAttributeMO.class);

	public SimpleAttributeMO(final TLnSimpleAttribute obj) {
		super(obj);
	}

	@Override
	public void clearTLType() {
	}

	@Override
	public void delete() {
	}

	// @Override
	// public String getComponentType() {
	// return "Simple Attribute";
	// }

	// @Deprecated
	// @Override
	// public String getName() {
	// return getTLModelObj().getName();
	// }

	// // Model does not know what namespace the attribute or its owning component is in.
	// @Override
	// public String getNamePrefix() {
	// return "";
	// }
	//
	// @Override
	// public String getNamespace() {
	// return "";
	// }

	@Override
	public TLnSimpleAttribute getTLModelObj() {
		return srcObj;
	}

	@Override
	public NamedEntity getTLType() {
		// TL_VWA which is the srcObj parent does not provide access to the simple facet.
		// if (srcObj instanceof TLnSimpleAttribute) {
		// if (srcObj.getParentObject() instanceof TLCoreObject) {
		// return ((TLCoreObject) srcObj.getParentObject()).getSimpleFacet().getSimpleType();
		// } else if (srcObj.getParentObject() instanceof TLValueWithAttributes)
		// return ((TLValueWithAttributes) srcObj.getParentObject()).getParentType();
		//
		// return null;
		// // throw new IllegalStateException("Not Implemented yet.");
		// // return ((Node) node.getParent()).getAssignedTLObject();
		// }
		return srcObj.getType();
	}

	// @Override
	// public boolean isMandatory() {
	// return getTLModelObj().isMandatory();
	// }

	@Override
	public boolean moveDown() {
		return false;
	}

	/**
	 * Move if you can, return false if you can not.
	 * 
	 * @return
	 */
	@Override
	public boolean moveUp() {
		return false;
	}

	// @Override
	// public boolean setMandatory(final boolean selection) {
	// return false;
	// }

	@Override
	public boolean setName(final String name) {
		return false;
	}

	// @Override
	// public void setTLType(final ModelObject<?> mo) {
	// Object tlObj = null;
	// if (mo != null)
	// tlObj = mo.getTLModelObj();
	// if (tlObj instanceof NamedEntity) {
	// getTLModelObj().setType((NamedEntity) tlObj);
	// }
	// }
	//
	@Override
	public void setTLType(final NamedEntity tlObj) {
		getTLModelObj().setType(tlObj);
	}

	// @Override
	// protected AbstractLibrary getLibrary(final TLnSimpleAttribute obj) {
	// return null;
	// }

}
