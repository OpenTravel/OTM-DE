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
	 * @param node
	 *            - node associated with this listener. If NULL, this listener will be skipped in
	 *            Node.getNode(listeners).
	 */
	public BaseNodeListener(Node node) {
		thisNode = node;
	}

	@Override
	public void processValueChangeEvent(ValueChangeEvent<?, ?> event) {
		// Node newValue = getNewValue(event);
		// Node oldValue = getOldValue(event);
		// LOGGER.debug("Value Change event: " + event.getType() + " this = " + thisNode + ", old = " + oldValue
		// + ", new = " + newValue);
	}

	@Override
	public void processOwnershipEvent(OwnershipEvent<?, ?> event) {
		// Node affectedNode = getAffectedNode(event);
		// LOGGER.debug("Ownership event: " + event.getType() + " this = " + thisNode + " affected = " + affectedNode);
	}

	@Override
	public Node getNode() {
		return thisNode;
	}

	public Node getNewValue(ValueChangeEvent<?, ?> event) {
		Node affectedNode = null;
		if (event.getNewValue() instanceof TLModelElement) {
			Collection<ModelElementListener> listeners = ((TLModelElement) event.getNewValue()).getListeners();
			for (ModelElementListener listener : listeners) {
				if (listener instanceof INodeListener)
					affectedNode = ((INodeListener) listener).getNode();
			}
		}
		return affectedNode;
	}

	public Node getOldValue(ValueChangeEvent<?, ?> event) {
		Node affectedNode = null;
		if (event.getOldValue() instanceof TLModelElement) {
			Collection<ModelElementListener> listeners = ((TLModelElement) event.getOldValue()).getListeners();
			for (ModelElementListener listener : listeners) {
				if (listener instanceof INodeListener)
					affectedNode = ((INodeListener) listener).getNode();
			}
		}
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
		return affectedNode;
	}

}
