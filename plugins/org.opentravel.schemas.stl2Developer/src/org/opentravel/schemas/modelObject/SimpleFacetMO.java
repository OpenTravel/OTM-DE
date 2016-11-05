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
import org.opentravel.schemacompiler.model.TLSimpleFacet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple facets are facets that have one implied child.
 * 
 * A Simple Facet has one property node child whose assigned type must be a simple type.
 * 
 * @author Dave Hollander
 * 
 */
public class SimpleFacetMO extends ModelObject<TLSimpleFacet> {

	private final static Logger LOGGER = LoggerFactory.getLogger(SimpleFacetMO.class);

	private TLnSimpleAttribute simpleProperty; // the associated property

	public SimpleFacetMO(TLSimpleFacet obj) {
		super(obj);
		simpleProperty = new TLnSimpleAttribute(obj);
	}

	@Override
	public void delete() {
	}

	// public TLModelElement getSimpleAttribute() {
	// return simpleProperty;
	// }

	@Override
	public List<?> getChildren() {
		final List<Object> kids = new ArrayList<Object>();
		kids.add(simpleProperty);
		return kids;

	}

	// 10/5/2015 - dmh - added override to return simple type
	@Override
	public NamedEntity getTLType() {
		return srcObj.getSimpleType();
	}

	// @Deprecated
	// @Override
	// public String getName() {
	// // LOGGER.debug(getTLModelObj().getSimpleTypeName()+"|"+ getTLModelObj().getLocalName());
	// return getTLModelObj().getLocalName() == null ? "" : getTLModelObj().getLocalName();
	// }

	@Override
	public String getNamePrefix() {
		return "";
	}

	@Override
	public String getNamespace() {
		return getTLModelObj().getNamespace();
	}

	@Override
	public TLSimpleFacet getTLModelObj() {
		return srcObj;
	}

	// @Override
	// public String getComponentType() {
	// return FacetMO.getDisplayName(getTLModelObj().getFacetType());
	// }

	@Override
	protected AbstractLibrary getLibrary(final TLSimpleFacet obj) {
		return null;
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
		return false;
	}

	@Override
	public void clearTLType() {
		// this.type = null;
		this.srcObj.setSimpleType(null);
	}

}
