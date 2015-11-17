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

/**
 * Implementors can be extended with a base type.
 * 
 * if (typeOwner.isCoreObject() || typeOwner.isBusinessObject() || typeOwner.isExtensionPointFacet()
 * typeOwner.isValueWithAttributes()) {
 * 
 * @author Dave
 *
 */
public interface ExtensionOwner {

	/**
	 * @return the base type - the node displayed in select Extends field.
	 */
	public Node getExtendsType();

	/**
	 * Set the extension base to the passed source node. If null, remove assignment. Extension base is maintained in the
	 * TypeClass.typeNode.
	 * 
	 * @param type
	 *            to assign as base type that is extended
	 */
	public void setExtendsType(INode type);
}
