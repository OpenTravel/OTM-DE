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
package org.opentravel.schemas.node.objectMembers;

import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.interfaces.FacadeInterface;

/**
 * Facades wrap other objects.
 * 
 * @author Dave Hollander
 * 
 */
public abstract class FacadeBase extends ComponentNode implements FacadeInterface {
	// private static final Logger LOGGER = LoggerFactory.getLogger(FacetNode.class);

	public FacadeBase() {
	}

	public FacadeBase(final TLFacet obj) {
		super(obj);
	}

}
