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

import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemas.node.Node;

/**
 * Nodes that implement the facade interface are not part of the TL Model. Facade nodes expose relationships to the GUI
 * such as versions and contributed contextual facets. These are nodes to allow the GUI to present them and their
 * wrapped node.
 * <p>
 * {@link Node#GetNode(ModelElement)} will return the node the facade wraps.
 * 
 * @author Dave
 *
 */
public interface FacadeInterface {
	/**
	 * Facades may wrap other nodes; version nodes wrap the library member, contributed facets wrap the contributor.
	 * 
	 * @return the node this is facade wraps if any.
	 * 
	 */
	public Node get();

	/**
	 * 
	 * @return the TLModelElement from the node this facade wraps
	 */
	public TLModelElement getTLModelObject();
}
