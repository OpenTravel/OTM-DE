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
package org.opentravel.schemas.node.typeProviders;

import org.opentravel.schemacompiler.model.TLLibraryMember;
import org.opentravel.schemas.node.Node;

public enum ImpliedNodeType {

	Empty("Undefined", ImpliedNode.Undefined), XSD_Atomic("XSD_Atomic", ImpliedNode.XSD_Atomic), UnassignedType(
			Node.UNDEFINED_PROPERTY_TXT, ImpliedNode.missing), Duplicate("Duplicate", ImpliedNode.duplicate), Indicator(
			"Indicator", ImpliedNode.indicator), Union("Union", ImpliedNode.union), String("DefaultString",
			ImpliedNode.defaultString);

	private String impliedNodeType;
	private TLLibraryMember impliedTLObject;

	private ImpliedNodeType(String impliedNodeType, TLLibraryMember tlObject) {
		this.impliedNodeType = impliedNodeType;
		this.impliedTLObject = tlObject;
	}

	/**
	 * @return the impliedNodeType
	 */
	public String getImpliedNodeType() {
		return impliedNodeType;
	}

	/**
	 * @return the impliedTLObject
	 */
	public TLLibraryMember getTlObject() {
		return impliedTLObject;
	}

}
