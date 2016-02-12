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
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLEnumValue;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemas.utils.StringComparator;

public class ClosedEnumMO extends ModelObject<TLClosedEnumeration> {

	public ClosedEnumMO(final TLClosedEnumeration obj) {
		super(obj);
	}

	@Override
	public boolean addChild(TLModelElement value) {
		if (value instanceof TLEnumValue)
			addLiteral((TLEnumValue) value);
		else
			return false;
		return true;
	}

	public void addLiteral(final TLEnumValue value) {
		getTLModelObj().addValue(value);
	}

	public void addLiteral(final TLEnumValue value, int index) {
		getTLModelObj().addValue(index, value);
	}

	@Override
	public void delete() {
		if (srcObj.getOwningLibrary() != null)
			srcObj.getOwningLibrary().removeNamedMember(srcObj);
	}

	@Override
	protected AbstractLibrary getLibrary(final TLClosedEnumeration obj) {
		return obj.getOwningLibrary();
	}

	@Override
	public List<TLEnumValue> getChildren() {
		return getTLModelObj().getValues();
	}

	@Override
	public String getComponentType() {
		return "Closed Enumeration";
	}

	@Override
	public String getName() {
		return getTLModelObj().getName();
	}

	@Override
	public String getNamespace() {
		return getTLModelObj().getNamespace();
	}

	@Override
	public TLClosedEnumeration getTLModelObj() {
		return srcObj;
	}

	@Override
	public String getNamePrefix() {
		final TLLibrary lib = (TLLibrary) getLibrary(getTLModelObj());
		return lib == null ? "" : lib.getPrefix();
	}

	@Override
	public boolean isSimpleAssignable() {
		return true;
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

	@Override
	public boolean setName(final String name) {
		getTLModelObj().setName(name);
		return true;
	}

	@Override
	public void sort() {
		TLClosedEnumeration eClosed = getTLModelObj();
		eClosed.sortValues(new StringComparator<TLEnumValue>() {

			@Override
			protected String getString(TLEnumValue object) {
				return object.getLiteral();
			}
		});
	}

}
