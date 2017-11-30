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
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.handlers.children.ChildrenHandlerI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Inherited children have nodes created to control the TLModelObect that is also created. This listener is placed on
 * the base node to signal when the children/inherited children are out of sync with the base objects.
 * <p>
 * The only responsibility is to clear children from a children handler. Changes to the actual node must clear the
 * parent handler via {@link run()}. Changes to the ghost node's children clear the ghost's children via {@link
 * run_childrenChanged()}
 * 
 * @author Dave
 *
 */
public class InheritanceDependencyListener extends BaseNodeListener implements INodeListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(InheritanceDependencyListener.class);

	private ChildrenHandlerI<?> parentHandler;

	/**
	 * 
	 * @param ghost
	 *            is the ghost node created as a facade to the inherited tl object. It is <b>not</b> the base object
	 *            where this listener is registered.
	 * @param handler
	 *            handler for the parent of <i>this</i> ghost node. Cleared when a change happens to tl object where
	 *            this listener is registered.
	 */
	public InheritanceDependencyListener(Node ghost, ChildrenHandlerI<?> handler) {
		super(ghost);
		this.parentHandler = handler;
		assert handler != null;
	}

	/**
	 * Explicitly fire this event handler.
	 */
	public void run() {
		parentHandler.clear();
	}

	/**
	 * Explicitly fire this event handler.
	 */
	public void run_childrenChanged() {
		if (thisNode.getChildrenHandler() != null)
			thisNode.getChildrenHandler().clear();
	}

	public ChildrenHandlerI<?> getHandler() {
		return parentHandler;
	}

	@Override
	public void processOwnershipEvent(OwnershipEvent<?, ?> event) {
		Node affectedNode = getAffectedNode(event);
		LOGGER.debug("Ownership event: " + event.getType() + " this = " + thisNode + " affected = " + affectedNode);

		parentHandler.clear();
	}

	@Override
	public void processValueChangeEvent(ValueChangeEvent<?, ?> event) {
		LOGGER.debug("Value Change event: " + event.getType() + " this = " + thisNode);

		switch (event.getType()) {
		case FACET_OWNER_MODIFIED:
		case NAME_MODIFIED:
		case TYPE_ASSIGNMENT_MODIFIED:
		default:
		}
		// This event indicates something has changed to a child managed by the handler.
		// Clear the handler lists and let them regenerate on next request.
		parentHandler.clear();
	}
}
