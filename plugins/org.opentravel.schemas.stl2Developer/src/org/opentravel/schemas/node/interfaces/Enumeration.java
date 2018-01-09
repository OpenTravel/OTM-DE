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
package org.opentravel.schemas.node.interfaces;

import java.util.List;

import org.opentravel.schemas.node.properties.EnumLiteralNode;

public interface Enumeration {
	/**
	 * Add the passed literal to the open or closed enumeration.
	 */
	public void addLiteral(String literal);

	public void add(EnumLiteralNode eln);

	/**
	 * Get a list of the enumeration values
	 */
	public List<String> getLiterals();

	public void setName(String name);
}
