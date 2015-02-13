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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.opentravel.schemas.node.properties.PropertyNode;

/**
 * @author Dave Hollander
 * 
 */
public enum PropertyNodeType {
	ELEMENT(1, "Element"), ATTRIBUTE(2, "Attribute"), INDICATOR(3, "Indicator"), ROLE(4, "Role"), ENUM_LITERAL(5,
			"Literal"), ALIAS(6, "Alias"), SIMPLE(7, "Simple"), ID_REFERENCE(8, "ID Reference"), INDICATOR_ELEMENT(9,
			"Indicator Element"), ID(10, "XML ID"), UNKNOWN(-1, "Unknown");

	private int propertyType;
	private String propertyTypeName;

	private PropertyNodeType(final int propertyType, final String propertyTypeName) {
		this.propertyType = propertyType;
		this.propertyTypeName = propertyTypeName;
	}

	/**
	 * 
	 * @return - Returns what type of property this is from enumerated list.
	 */
	public int getType() {
		return propertyType;
	}

	/**
	 * Check if given node supports {@link PropertyNodeType}. This method supports {@link PropertyNode} and
	 * {@link AliasNode}
	 * 
	 * @param node
	 * @param type
	 * @return true if for given {@link PropertyNodeType} can be assigned {@link Node}
	 */
	public static boolean isSupported(Node node, PropertyNodeType type) {
		Collection<PropertyNodeType> supportedTypes = getSupportedTypes(node);
		return supportedTypes.contains(type);
	}

	/**
	 * This method supports {@link PropertyNode} and {@link AliasNode}
	 * 
	 * @param node
	 * @return list of supported {@link PropertyNodeType} for given node.
	 */
	public static Collection<PropertyNodeType> getSupportedTypes(Node node) {
		if (node instanceof PropertyNode) {
			return getSupportedTypes((PropertyNode) node);
			// TODO: should AliasNode extends a PropertyNode ???
		} else if (node instanceof AliasNode) {
			return Collections.singleton(PropertyNodeType.ALIAS);
		}
		return Collections.emptyList();
	}

	private static Collection<PropertyNodeType> getSupportedTypes(PropertyNode node) {
		if (node.isVWA_Attribute() && !(node.getPropertyType() == SIMPLE)) {
			return getVWA_PropertyTypes();
		} else if (!getAllTypedPropertyTypes().contains(node.getPropertyType())) {
			return Arrays.asList(node.getPropertyType());
		} else {
			return getAllTypedPropertyTypes();
		}
	}

	public static Collection<PropertyNodeType> getVWA_PropertyTypes() {
		return Arrays.asList(ATTRIBUTE, INDICATOR, ID);
	}

	public static Collection<PropertyNodeType> getAllTypedPropertyTypes() {
		return Arrays.asList(ELEMENT, ATTRIBUTE, INDICATOR, INDICATOR_ELEMENT, ID_REFERENCE, ID);
	}

	public String getName() {
		return propertyTypeName;
	}

	public static PropertyNodeType fromString(final String value) {
		for (final PropertyNodeType type : values()) {
			if (type.getName().equalsIgnoreCase(value)) {
				return type;
			}
			if (type.toString().equalsIgnoreCase(value)) {
				return type;
			}
		}
		return null;
	}

}
