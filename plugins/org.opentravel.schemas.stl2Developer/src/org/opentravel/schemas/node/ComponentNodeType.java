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

/**
 * @author Dave Hollander
 * 
 */
public enum ComponentNodeType {
	CLOSED_ENUM("Enumeration Closed"), ALIAS("Alias"), OPEN_ENUM("Enumeration Open"), VWA("Value With Attributes"), CHOICE(
			"Choice Object"), CORE("Core Object"), BUSINESS("Business Object"), RESOURCE("Resource"), SERVICE("Service"), MESSAGE(
			"Message"), OPERATION("Operation"), REQUEST("Request"), RESPONSE("Response"), NOTIFICATION("Notification"), EXTENSION_POINT(
			"Extension Point Facet"), SIMPLE("Simple Object");

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

	@Override
	public String toString() {
		return desc;
	}

	public static ComponentNodeType fromString(String string) {
		for (ComponentNodeType type : ComponentNodeType.values()) {
			if (type.getDescription().equals(string))
				return type;
		}
		return null;
	}
}
