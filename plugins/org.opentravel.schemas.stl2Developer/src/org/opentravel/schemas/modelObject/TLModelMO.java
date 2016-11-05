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
import org.opentravel.schemacompiler.model.TLModel;

public class TLModelMO extends ModelObject<TLModel> {

	public TLModelMO(final TLModel obj) {
		super(obj);
	}

	@Override
	public void delete() {
		System.out.println("ModelObject - delete - TODO");
	}

	@Override
	public List<?> getChildren() {
		return null;
	}

	// @Override
	// public String getComponentType() {
	// return null;
	// }

	@Override
	protected AbstractLibrary getLibrary(final TLModel obj) {
		return null;
	}

	// @Deprecated
	// @Override
	// public String getName() {
	// return null;
	// }
	//
	@Override
	public String getNamePrefix() {
		return null;
	}

	@Override
	public String getNamespace() {
		return null;
	}

	@Override
	public boolean setName(final String name) {
		return false;
	}

	// @Override
	// public void setDeprecatedDoc(final String string, final int i) {
	// }

	@Override
	public void setDeveloperDoc(final String string, final int index) {
	}

	@Override
	public void setReferenceDoc(final String string, final int index) {
	}

	@Override
	public void setMoreInfo(final String string, final int index) {
	}

	@Override
	public TLModel getTLModelObj() {
		return srcObj;
	}

	// @Override
	// public void setOtherDoc(final String string, final String context) {
	// }

}
