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

import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLAliasOwner;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLListFacet;

/**
 * @author Dave Hollander
 * 
 */
public class AliasMO extends ModelObject<TLAlias> {

	public AliasMO(final TLAlias obj) {
		super(obj);
	}

	@Override
	public void delete() {
		final TLAliasOwner owningEntity = getTLModelObj().getOwningEntity();
		if (owningEntity != null && !(owningEntity instanceof TLFacet) && !(owningEntity instanceof TLListFacet)) {
			owningEntity.removeAlias(getTLModelObj());
		}
		srcObj = null;
	}

	@Override
	public TLAlias getTLModelObj() {
		return srcObj;
	}

	// Used in rename visitor - FIXME
	@Deprecated
	@Override
	public boolean setName(final String name) {
		getTLModelObj().setName(name);
		return true;
	}

}
