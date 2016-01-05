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
package org.opentravel.schemas.node.interfaces;

import org.opentravel.schemas.node.ComponentNode;

/**
 * Nodes implementing this interface represent objects that can be created as minor versions by extending the previous
 * version.
 * 
 * @author Dave Hollander
 * 
 */
public interface VersionedObjectInterface {

	/**
	 * Create a new object in a minor version library. Creates an empty copy of this node's owner. Adds the new node to
	 * the owner's chain head library. Sets the new object base type to this node.
	 * 
	 * @return the new node summary facet or its detail if this node was the detail facet node.
	 */
	public ComponentNode createMinorVersionComponent();

}
