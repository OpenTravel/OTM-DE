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

		switch (event.getType()) {
		case DOCUMENTATION_MODIFIED:
			break;
		case NAME_MODIFIED:
			break;
		case TYPE_ASSIGNMENT_MODIFIED:
			break;
		case FACET_OWNER_MODIFIED:
			break;
		default:
			// LOGGER.debug("Value Change event: " + event.getType() + " this = " + thisNode + ", old = " + oldValue
			// + ", new = " + newValue);
		}
	}

	@Override
	public void processOwnershipEvent(OwnershipEvent<?, ?> event) {
		// Node affectedNode = getAffectedNode(event);
		// LOGGER.debug("Ownership event: " + event.getType() + " this = " + thisNode + " affected = " + affectedNode);
	}

	@Override
	public Node getNode() {
		// XsdNodes are always represented by their OTM Model counterpart.
		// return thisNode instanceof XsdNode ? ((XsdNode) thisNode).getOtmModel() : thisNode;
		return thisNode;
	}

	public Node getSource(ValueChangeEvent<?, ?> event) {
		Node source = null;
		if (event.getSource() instanceof TLModelElement)
			source = Node.GetNode((TLModelElement) event.getSource());
		return source;
	}

	@Override
	public Node getNewValue(ValueChangeEvent<?, ?> event) {
		Node affectedNode = null;
		if (event.getNewValue() instanceof TLModelElement)
			affectedNode = Node.GetNode((TLModelElement) event.getNewValue());
		return affectedNode;
	}

	public Node getOldValue(ValueChangeEvent<?, ?> event) {
		Node affectedNode = null;
		if (event.getOldValue() instanceof TLModelElement)
			affectedNode = Node.GetNode((TLModelElement) event.getOldValue());
		return affectedNode;
	}

	@Override
	public Node getAffectedNode(OwnershipEvent<?, ?> event) {
		Node affectedNode = null;
		if (event.getAffectedItem() instanceof TLModelElement)
			affectedNode = Node.GetNode((TLModelElement) event.getAffectedItem());
		return affectedNode;
	}

	@Override
	public Node getSource(OwnershipEvent<?, ?> event) {
		Node source = null;
		if (event.getSource() instanceof TLModelElement)
			source = Node.GetNode((TLModelElement) event.getSource());
		return source;
	}

	@Override
	public String toString() {
		return thisNode.getName() + " listener";
	}
}
