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

import org.opentravel.schemacompiler.model.TLSimple;

/**
 * Class for top level Simple type objects.
 * 
 * @author Dave Hollander
 * 
 */
public class SimpleMO extends ModelObject<TLSimple> {

	public SimpleMO(final TLSimple obj) {
		super(obj);
		// if (obj.getParentType() != null) {
		// setTLType(obj.getParentType());
		// }
	}

	// @Override
	// public void delete() {
	// if (getTLModelObj().getOwningLibrary() != null) {
	// getTLModelObj().getOwningLibrary().removeNamedMember(getTLModelObj());
	// }
	// }

	@Override
	public List<Object> getChildren() {
		return new ArrayList<Object>();
	}

	@Override
	public TLSimple getTLModelObj() {
		return srcObj;
	}

	// @Override
	// public NamedEntity getTLType() {
	// return srcObj.getParentType();
	// }

	// @Override
	// public boolean isSimpleAssignable() {
	// return true;
	// }

	// @Override
	// public void setTLType(final NamedEntity tlObj) {
	// getTLModelObj().setParentType((TLAttributeType) tlObj);
	// }

}
