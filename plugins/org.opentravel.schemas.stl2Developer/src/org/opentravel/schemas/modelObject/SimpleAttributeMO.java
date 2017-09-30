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
	public void delete() {
	}

	@Override
	public TLnSimpleAttribute getTLModelObj() {
		return srcObj;
	}

	@Override
	public NamedEntity getTLType() {
		return srcObj.getType();
	}

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
	// public boolean setName(final String name) {
	// return false;
	// }

	@Override
	public void setTLType(final NamedEntity tlObj) {
		getTLModelObj().setType(tlObj);
	}

}
