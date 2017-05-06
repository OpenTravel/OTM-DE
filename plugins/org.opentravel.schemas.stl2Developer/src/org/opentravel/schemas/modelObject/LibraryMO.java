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
/**
 * 
 */
package org.opentravel.schemas.modelObject;

import java.util.List;

import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dave Hollander
 * 
 */
public class LibraryMO extends ModelObject<AbstractLibrary> {
	private static final Logger LOGGER = LoggerFactory.getLogger(LibraryMO.class);

	public LibraryMO(AbstractLibrary obj) {
		super(obj);
	}

	@Override
	public List<?> getChildren() {
		// TODO Auto-generated method stub
		return getTLModelObj().getNamedMembers();
	}

	@Override
	public AbstractLibrary getTLModelObj() {
		return srcObj;
	}

	@Override
	public boolean setName(String name) {
		getTLModelObj().setName(name);
		return true;
	}

	@Override
	public void delete() {
		// LOGGER.debug("Not Implemented - library delete");
	}

}
