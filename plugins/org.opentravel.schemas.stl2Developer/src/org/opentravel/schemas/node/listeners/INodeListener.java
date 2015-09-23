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
package org.opentravel.schemas.node.listeners;

import org.opentravel.schemacompiler.event.ModelElementListener;
import org.opentravel.schemacompiler.event.OwnershipEvent;
import org.opentravel.schemacompiler.event.ValueChangeEvent;
import org.opentravel.schemas.node.Node;

/**
 * Listen to Node change events thrown by the schema compiler.
 * 
 * @author Dave Hollander
 * 
 */
public interface INodeListener extends ModelElementListener {

	/**
	 * Return the node that is associated with this listener
	 * 
	 * @return
	 */
	public Node getNode();

	/**
	 * @return the affected node from the listener on the event's affected tl model element
	 */
	public Node getAffectedNode(ValueChangeEvent<?, ?> event);

	public Node getAffectedNode(OwnershipEvent<?, ?> event);

}
