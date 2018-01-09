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
package org.opentravel.schemas.node;

import org.opentravel.schemacompiler.model.TLFacetType;

/**
 * Note - other facet types provided by compiler {@link TLFacetType}
 * 
 * @author Dave Hollander
 * 
 */
public enum ComponentNodeType {
	ALIAS("Alias"),
	ATTRIBUTE("Attribute"),
	ATTRIBUTES("Attributes"),
	BUSINESS("Business Object"),
	CHOICE("Choice Object"),
	CHOICE_FACET("Choice Facet"),
	CUSTOM_FACET("Custom Facet"),
	CLOSED_ENUM("Closed Enumeration"),
	CORE("Core Object"),
	DETAIL_LIST("Detail List"),
	ELEMENT("Element"),
	ELEMENT_REF("Element Reference"),
	ATTRIBUTE_REF("Attribute Reference"),
	ENUM_LITERAL("Enumeration Value"),
	EXTENSION_POINT("Extension Point Facet"),
	INDICATOR("Indicator"),
	INDICATOR_ELEMENT("Indicator Element"),
	MESSAGE("Message"),
	NAVIGATION("Navigation"),
	NOTIFICATION("Notification"),
	OPEN_ENUM("Open Enumeration"),
	OPERATION("Operation"),
	QUERY_FACET("Query Facet"),
	REQUEST("Request"),
	RESOURCE("Resource"),
	RESPONSE("Response"),
	ROLE("Role"),
	ROLES("Roles"),
	SIMPLE("Simple Object"),
	SIMPLE_LIST("Simple List"),
	SIMPLE_ATTRIBUTE("Simple Attribute"),
	SERVICE("Service"),
	VWA("Value With Attributes");

	private String desc;

	ComponentNodeType(String name) {
		this.desc = name;
	}

	public String getDescription() {
		return desc;
	}

	public String value() {
		return desc;
	}

	// @Override
	// public String toString() {
	// return desc;
	// }

	public static ComponentNodeType fromString(String string) {
		for (ComponentNodeType type : ComponentNodeType.values()) {
			if (type.getDescription().equals(string))
				return type;
		}
		return null;
	}
}
