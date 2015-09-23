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

import java.util.Collection;

import org.opentravel.schemacompiler.event.ModelElementListener;
import org.opentravel.schemacompiler.event.OwnershipEvent;
import org.opentravel.schemacompiler.event.ValueChangeEvent;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemas.node.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dave Hollander
 *
 */
public class BaseNodeListener implements INodeListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(BaseNodeListener.class);

	Node thisNode;

	/**
	 * 
	 */
	public BaseNodeListener(Node node) {
		thisNode = node;
	}

	@Override
	public void processValueChangeEvent(ValueChangeEvent<?, ?> event) {
		Node affectedNode = getAffectedNode(event);
		// LOGGER.debug("Ownership event: " + event.getType() + " this = " + thisNode + " affected = " + affectedNode);
	}

	@Override
	public void processOwnershipEvent(OwnershipEvent<?, ?> event) {
		// Node affectedNode = getAffectedNode(event);
		// LOGGER.debug("Ownership event: " + event.getType() + " this = " + thisNode + " affected = " + affectedNode);

		switch (event.getType()) {
		case MEMBER_ADDED:
			// LOGGER.debug("Ownership change event: added" + affectedNode + " to " + thisNode);
			break;
		case MEMBER_REMOVED:
			// LOGGER.debug("Ownership change event: removed " + affectedNode + " from " + thisNode);
			break;
		default:
			// LOGGER.debug("Unhandled Ownership event: " + event.getType() + " this = " + thisNode + " affected = "
			// + affectedNode);
			break;
		}
	}

	@Override
	public Node getNode() {
		return thisNode;
	}

	@Override
	public Node getAffectedNode(ValueChangeEvent<?, ?> event) {
		Node affectedNode = null;
		// FIXME - how to get the type associated with the value? event.getOldValue();
		return affectedNode;
	}

	@Override
	public Node getAffectedNode(OwnershipEvent<?, ?> event) {
		Node affectedNode = null;
		Collection<ModelElementListener> listeners = ((TLModelElement) event.getAffectedItem()).getListeners();
		for (ModelElementListener listener : listeners) {
			if (listener instanceof INodeListener)
				affectedNode = ((INodeListener) listener).getNode();
		}
		if (affectedNode == null)
			LOGGER.debug("Could not determine affected node.");
		return affectedNode;
	}

}
