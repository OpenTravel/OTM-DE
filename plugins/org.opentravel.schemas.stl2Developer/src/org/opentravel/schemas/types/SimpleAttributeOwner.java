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
package org.opentravel.schemas.types;

import org.opentravel.schemas.node.properties.SimpleAttributeFacadeNode;

/**
 * Implementations have a simple attribute as a member of the object. Includes VWA and Core.
 * 
 * @author Dave
 *
 */
public interface SimpleAttributeOwner {

	public SimpleAttributeFacadeNode getSimpleAttribute();

	// @Deprecated
	// public boolean setSimpleType(TypeProvider provider);
	//
	// @Deprecated
	// public TypeProvider getSimpleType();

	/**
	 * 
	 * @return type assigned to the simple attribute
	 */
	public TypeProvider getAssignedType();

	/**
	 * Attempt to set the type assigned to the simple attribute.
	 * 
	 * @param type
	 * @return true if the state of the simple type changed.
	 */
	public TypeProvider setAssignedType(TypeProvider type);
}
