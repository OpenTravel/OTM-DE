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
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLExtensionPointFacet;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLProperty;

public class ExtensionPointFacetMO extends ModelObject<TLExtensionPointFacet> {
	// final static Logger LOGGER = LoggerFactory.getLogger(ExtensionPointFacetMO.class);

	public ExtensionPointFacetMO(final TLExtensionPointFacet obj) {
		super(obj);
	}

	@Override
	public void delete() {
		final AbstractLibrary owningLibrary = getTLModelObj().getOwningLibrary();
		if (owningLibrary != null) {
			owningLibrary.removeNamedMember(getTLModelObj());
		}
	}

	@Override
	public List<?> getChildren() {

		final List<TLModelElement> kids = new ArrayList<TLModelElement>();
		kids.addAll(getTLModelObj().getAttributes());
		kids.addAll(getTLModelObj().getIndicators());
		kids.addAll(getTLModelObj().getElements());

		return kids;

	}

	// @Override
	// public String getComponentType() {
	// return "Extension Point Facet";
	// }

	// @Deprecated
	// @Override
	// public String getName() {
	// return getTLModelObj().getLocalName() == null ? "-not assigned-" : getTLModelObj().getLocalName();
	// }

	@Override
	public String getNamePrefix() {
		final TLLibrary lib = (TLLibrary) getLibrary(getTLModelObj());
		return lib == null ? "" : lib.getPrefix();
	}

	@Override
	public String getNamespace() {
		return getTLModelObj().getNamespace();
	}

	@Override
	public TLExtensionPointFacet getTLModelObj() {
		return srcObj;
	}

	@Override
	public NamedEntity getTLBase() {
		return srcObj.getExtension() != null ? srcObj.getExtension().getExtendsEntity() : null;
	}

	// @Override
	// public boolean isComplexAssignable() {
	// return false;
	// }

	/**
	 * Model will force a core used as page1 to use the page1 facet.
	 */
	// FIXME - prevents inclusion in tree view but need to understand why first.
	@Override
	public boolean isSimpleAssignable() {
		return false;
	}

	/**
	 * Is this extension point extended by <i>extension</i>?
	 */
	@Override
	public boolean isExtendedBy(NamedEntity extension) {
		if (extension == null)
			return false;
		if (extension.getValidationIdentity() == null)
			return false;

		if (getTLModelObj() != null)
			if (getTLModelObj().getExtension() != null)
				if (getTLModelObj().getExtension().getValidationIdentity() != null)
					return getTLModelObj().getExtension().getExtendsEntity() == extension;
		return false;
	}

	/**
	 * @see org.opentravel.schemas.modelObject.ModelObject#getExtendsType()
	 */
	@Override
	public String getExtendsType() {
		TLExtension tlExtension = getTLModelObj().getExtension();
		String extendsTypeName = "";

		if (tlExtension != null) {
			if (tlExtension.getExtendsEntity() != null)
				extendsTypeName = tlExtension.getExtendsEntity().getLocalName();
			else
				extendsTypeName = "--base type can not be found--";
		}
		return extendsTypeName;
	}

	@Override
	public String getExtendsTypeNS() {
		TLExtension tlExtension = getTLModelObj().getExtension();
		// for EPF not extending anything tlExtension.getExtendsEntity() is null.
		return tlExtension != null && tlExtension.getExtendsEntity() != null ? tlExtension.getExtendsEntity()
				.getNamespace() : "";
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
	protected AbstractLibrary getLibrary(TLExtensionPointFacet obj) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean setName(String name) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean addChild(TLModelElement child) {
		if (child instanceof TLProperty) {
			getTLModelObj().addElement((TLProperty) child);
		} else if (child instanceof TLAttribute) {
			getTLModelObj().addAttribute((TLAttribute) child);
		} else if (child instanceof TLIndicator) {
			getTLModelObj().addIndicator((TLIndicator) child);
		} else
			return false;
		return true;
	}

}
