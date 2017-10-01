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
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLAttributeOwner;
import org.opentravel.schemacompiler.model.TLAttributeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AttributeMO extends ModelObject<TLAttribute> {
	private static final Logger LOGGER = LoggerFactory.getLogger(AttributeMO.class);

	public AttributeMO(final TLAttribute obj) {
		super(obj);
		if (obj.getType() != null) {
			setTLType(obj.getType());
		}
	}

	@Override
	public void delete() {
		removeFromTLParent();
	}

	@Override
	public void addToTLParent(final ModelObject<?> parentMO, int index) {
		if (parentMO.getTLModelObj() instanceof TLAttributeOwner) {
			((TLAttributeOwner) parentMO.getTLModelObj()).addAttribute(index, getTLModelObj());
		}
	}

	@Override
	public void addToTLParent(final ModelObject<?> parentMO) {
		if (parentMO.getTLModelObj() instanceof TLAttributeOwner) {
			((TLAttributeOwner) parentMO.getTLModelObj()).addAttribute(getTLModelObj());
		}
	}

	@Override
	public void removeFromTLParent() {
		final TLAttributeOwner attributeOwner = getTLModelObj().getOwner();
		if (attributeOwner != null) {
			attributeOwner.removeAttribute(getTLModelObj());
		}
	}

	@Override
	public TLAttribute getTLModelObj() {
		return srcObj;
	}

	@Override
	public NamedEntity getTLType() {
		return srcObj.getType();
	}

	protected int indexOf() {
		final TLAttribute thisProp = getTLModelObj();
		return thisProp.getOwner().getAttributes().indexOf(thisProp);
	}

	/**
	 * Move if you can, return false if you can not.
	 * 
	 * @return
	 */
	@Override
	public boolean moveUp() {
		if (indexOf() > 0) {
			getTLModelObj().moveUp();
			return true;
		}
		return false;
	}

	@Override
	public boolean moveDown() {
		// only count attributes, not elements or indicators
		if (indexOf() + 1 < getTLModelObj().getOwner().getAttributes().size()) {
			getTLModelObj().moveDown();
			return true;
		}
		return false;
	}

	@Override
	public void setTLType(final NamedEntity tlObj) {
		if (tlObj instanceof TLAttributeType)
			getTLModelObj().setType((TLAttributeType) tlObj);
		else
			LOGGER.debug("Tried to set tlAttribute with " + tlObj.getLocalName() + " of class "
					+ tlObj.getClass().getSimpleName());
	}

}
