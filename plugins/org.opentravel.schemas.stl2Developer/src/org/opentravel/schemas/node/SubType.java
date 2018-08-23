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
package org.opentravel.schemas.node;

public enum SubType {
	// facet sub-types
	SUMMARY("Summary"),
	INFO("Info"),
	ROLES("Roles"),
	SIMPLE_FACET("SimpleFacet"),
	ID("ID"),
	DETAIL("Detail"),
	QUERY("Query"),
	CUSTOM("Custom"),
	SHARED("Shared"),

	// Properties
	ATTRIBUTE("Attribute"),
	ELEMENT("Element"),
	INDICATOR("Indicator"),

	// message
	MESSAGE("Message"),
	OPERATION("Operation"),
	REQUEST("Request"),
	RESPONSE("Response"),
	NOTIFICATION("Notification"),

	// term sub-types
	SIMPLE("Simple"),
	SIMPLE_XSD("SimpleXSD"),
	ENUMERATION("Enumeration"),
	ENUMERATION_WITH_DOC("Enumeration With Doc"),
	VALUE_WITH_ATTRS("Value With Attributes"),
	CORE_OBJECT("Core Object"),
	BUSINESS_OBJECT("Business Object"),
	CHOICE_OBJECT("Choice Object"),
	RESOURCE("Resource"),
	SERVICE("Service"),
	UNKNOWN("Unknown");

	private final String value;

	SubType(final String v) {
		value = v;
	}

	public String value() {
		return value;
	}

	public static SubType fromValue(final String v) {

		for (final SubType c : SubType.values()) {
			if (c.value.equals(v)) {
				return c;
			}
		}
		return SubType.UNKNOWN;
	}
}
