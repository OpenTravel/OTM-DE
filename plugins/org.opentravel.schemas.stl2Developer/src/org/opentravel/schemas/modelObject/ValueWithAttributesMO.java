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
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLSimpleFacet;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;

/**
 * Value With Attribute Model Object.
 * 
 * Provide an interface to the TLValueWithAttributes model object. TLValueWithAttributes does not use facets to contain
 * simple type and attributes, so this model class must adapt.
 * 
 * @author Dave Hollander
 * 
 */
public class ValueWithAttributesMO extends ModelObject<TLValueWithAttributes> {
	// private final static Logger LOGGER = LoggerFactory.getLogger(ValueWithAttributesMO.class);

	private final TLSimpleFacet valueFacet;
	private final TLValueWithAttributesFacet attributeFacet;

	public ValueWithAttributesMO(final TLValueWithAttributes obj) {
		super(obj);
		valueFacet = new TLSimpleFacet();
		valueFacet.setSimpleType(obj.getParentType());
		valueFacet.setFacetType(TLFacetType.SIMPLE);

		attributeFacet = new TLValueWithAttributesFacet(obj);
		if (obj.getParentType() != null) {
			setTLType(obj.getParentType());
		}
	}

	@Override
	public void delete() {
		if (getTLModelObj() == null || getTLModelObj().getOwningLibrary() == null) {
			return;
		}
		getTLModelObj().getOwningLibrary().removeNamedMember(getTLModelObj());
	}

	@Override
	public List<Object> getChildren() {
		// return the two facets: value type and attributes.
		final List<Object> kids = new ArrayList<Object>();
		kids.add(valueFacet);
		kids.add(attributeFacet);
		return kids;
	}

	@Override
	public String getComponentType() {
		return "Value With Attributes";
	}

	@Override
	public void clearTLType() {
		// this.type = null;
		this.srcObj.setParentType(null);
	}

	@Override
	public void setExtendsType(ModelObject<?> mo) {
		if (mo != null || !(mo.getTLModelObj() instanceof TLValueWithAttributes))
			getTLModelObj().setParentType((TLValueWithAttributes) mo.getTLModelObj());
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
	public String getNamePrefix() {
		final TLLibrary lib = (TLLibrary) getLibrary(getTLModelObj());
		return lib == null ? "" : lib.getPrefix();
	}

	@Override
	protected AbstractLibrary getLibrary(final TLValueWithAttributes obj) {
		return obj.getOwningLibrary();
	}

	public NamedEntity getSimpleValueType() {
		return srcObj.getParentType();
	}

	// @Override
	// public boolean isComplexAssignable() {
	// return true;
	// }
	//
	@Override
	public boolean isSimpleAssignable() {
		return true;
	}

	@Override
	public boolean setName(final String name) {
		getTLModelObj().setName(name);
		return true;
	}

	@Override
	public TLValueWithAttributes getTLModelObj() {
		return srcObj;
	}

}
