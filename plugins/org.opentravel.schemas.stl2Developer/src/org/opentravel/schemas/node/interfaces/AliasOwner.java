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

import org.opentravel.schemas.node.typeProviders.AliasNode;

/**
 * Alias owners are the actual library member that can have aliases added or removed from them. AliasNode may be
 * assigned to TLAlias objects that are not alias owners.
 * 
 * @author dmh
 *
 */
public interface AliasOwner extends LibraryMemberInterface {
	/**
	 * All alias to TL model if not already present. Add to or clear children.
	 * 
	 * @param alias
	 */
	public void addAlias(AliasNode alias);

	public AliasNode addAlias(String name);

	public void cloneAliases(List<AliasNode> aliases);

	public void remove(AliasNode alias);
}
