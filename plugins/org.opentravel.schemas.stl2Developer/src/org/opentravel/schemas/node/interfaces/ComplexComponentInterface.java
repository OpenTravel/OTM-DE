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

import org.opentravel.schemas.node.facets.SimpleFacetNode;
import org.opentravel.schemas.node.properties.PropertyOwnerInterface;

/**
 * Nodes implementing this interface represent named types that have structure that contains other nodes. In the GUI,
 * they are presented in Complex Type folders. When compiled, they will create complex types.
 * 
 * NOTE: The service components (service, operation and messages) do <b>not</b> implement this interface.
 * 
 * NOTE: To determine how properties will be assigned types use {@link INode#isAssignedByReference()}, <b>not</b> this
 * interface.
 * 
 * @author Dave Hollander
 * 
 */
public interface ComplexComponentInterface {

	/**
	 * @return the simple facet or null if none.
	 */
	public SimpleFacetNode getFacet_Simple();

	public PropertyOwnerInterface getAttributeFacet(); // VWA only

	public PropertyOwnerInterface getFacet_Summary();

	public PropertyOwnerInterface getFacet_Detail();

	/**
	 * @return null or the default facet for the complex object
	 */
	public PropertyOwnerInterface getFacet_Default();

	/**
	 * Create aliases for complex types used by two or more properties.
	 */
	public void createAliasesForProperties();

	// public boolean isNamedType();
}
