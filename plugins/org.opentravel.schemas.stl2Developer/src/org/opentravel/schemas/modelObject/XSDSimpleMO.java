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

import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.XSDSimpleType;
import org.opentravel.schemas.node.XsdNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3._2001.xmlschema.SimpleType;

// import org.opentravel.schemas.stl2developer.MainWindow;

public class XSDSimpleMO extends ModelObject<XSDSimpleType> {
	private static final Logger LOGGER = LoggerFactory.getLogger(XSDSimpleMO.class);
	private LibraryMember tlObj = null;

	public XSDSimpleMO(final XSDSimpleType obj) {
		super(obj);
	}

	/**
	 * Create a TLSimple from the xsdSimpleType's JAXB object.
	 * 
	 * @return
	 */
	@Override
	public LibraryMember buildTLModel(XsdNode node) {
		// If the simple has no jaxB type, it is probably a built-in type
		String name = srcObj.getJaxbType() != null ? srcObj.getJaxbType().getName() : srcObj.getName();
		return buildTLModel(name, node);
	}

	public LibraryMember buildTLModel(String name, XsdNode xsdNode) {
		if (tlObj != null)
			return tlObj; // already built, just return it.

		final SimpleType jTLS = this.getTLModelObj().getJaxbType();
		// LibraryMember lm = XsdModelingUtils.buildSimpleObject(jTLS, name, xsdNode);
		return XsdModelingUtils.buildSimpleObject(jTLS, name, xsdNode);
	}

	public LibraryMember getTlObj() {
		return tlObj;
	}

	@Override
	public XSDSimpleType getTLModelObj() {
		return srcObj;
	}

	// @Override
	// public String getAssignedName() {
	//
	// // NamedEntity tltype = super.getTLType(); // here we need to get srcObj
	// // instead of getTLType
	//
	// final NamedEntity tltype = getTLModelObj();
	//
	// if (tltype instanceof XSDSimpleType) {
	// final XSDSimpleType type = (XSDSimpleType) tltype;
	// if (type.getJaxbType() != null && type.getJaxbType().getRestriction() != null
	// && type.getJaxbType().getRestriction().getBase() != null) {
	// if (type.getJaxbType().getRestriction().getBase().getPrefix().length() == 0) {
	// return type.getJaxbType().getRestriction().getBase().getLocalPart();
	// } else {
	// return type.getJaxbType().getRestriction().getBase().getPrefix() + ":"
	// + type.getJaxbType().getRestriction().getBase().getLocalPart();
	// }
	// } else if (type.getJaxbType() != null && type.getJaxbType().getUnion() != null
	// && type.getJaxbType().getUnion().getMemberTypes() != null) {
	// return XsdModelingUtils.parseUnion(type);
	// }
	// }
	//
	// return super.getAssignedName();
	//
	// }

	@Override
	public void delete() {
		LOGGER.debug("ModelObject - delete - TODO");
	}

	// @Override
	// public List<?> getChildren() {
	// return null;
	// }

	// @Override
	// public String getComponentType() {
	// return "XSD Simple Type";
	// }
	//
	// @Override
	// protected AbstractLibrary getLibrary(final XSDSimpleType obj) {
	// return null;
	// }

	// @Override
	// public String getName() {
	// if (srcObj == null)
	// getTlObj(); // try to render its src object.
	// return srcObj == null ? "Undefined" : getTLModelObj().getLocalName();
	// }

	// @Override
	// public String getNamePrefix() {
	// return "";
	// }

	// @Override
	// public String getNamespace() {
	// return getTLModelObj().getNamespace();
	// }

	// @Override
	// public String getPattern() {
	// return ((tlObj != null) && (tlObj instanceof TLSimple)) ? ((TLSimple) tlObj).getPattern() : "";
	// }

	@Override
	public boolean isSimpleAssignable() {
		return true;
	}

	@Override
	public boolean setName(final String name) {
		return false;
	}

}
