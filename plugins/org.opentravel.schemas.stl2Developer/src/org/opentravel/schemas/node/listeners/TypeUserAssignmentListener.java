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
import org.opentravel.schemas.types.SimpleAttributeOwner;
import org.opentravel.schemas.types.TypeUser;
import org.opentravel.schemas.types.WhereAssignedHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*********************************************************************************
 *
 * Non-identity listener placed on type users.
 * <p>
 * Created by the whereAssignedHandler of the type provider, the listener is placed on the type user. <i>thisNode</i>
 * and the handler are for the type provider.
 * <p>
 * This listener notifies the handler if there has been a change to this type user changing its assigned type.
 */
public class TypeUserAssignmentListener extends BaseNodeListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(TypeUserAssignmentListener.class);

	private WhereAssignedHandler handler = null;

	public TypeUserAssignmentListener(WhereAssignedHandler handler) {
		super((Node) handler.getOwner());
		this.handler = handler;
	}

	@Override
	public void processOwnershipEvent(OwnershipEvent<?, ?> event) {
		super.processOwnershipEvent(event);
	}

	@Override
	public void processValueChangeEvent(ValueChangeEvent<?, ?> event) {
		super.processValueChangeEvent(event); // logger.debug statements

		switch (event.getType()) {
		case TYPE_ASSIGNMENT_MODIFIED:
			// Listeners can't be set/removed here, doing so makes the event stream not have the assignment event.
			// LOGGER.debug("Type Assignment Modified event - " + getSource(event) + " on " + getNode() + " changed to "
			// + getNewValue(event) + " from " + getOldValue(event));

			// VWA and Core have event listeners but are not type users.
			Node source = getSource(event);
			if (source instanceof SimpleAttributeOwner)
				source = ((SimpleAttributeOwner) source).getSimpleAttribute();
			if (!(source instanceof TypeUser))
				return;

			if (getNewValue(event) == getNode())
				handler.add((TypeUser) source);

			// Change assignment but NOT the listener or else a co-modification error
			if (getOldValue(event) == getNode() && event.getOldValue() != event.getNewValue())
				if (source instanceof TypeUser)
					handler.removeUser((TypeUser) source);
				else
					// LOGGER.debug("ERROR.");
					assert false; // Maybe not a simple attribute owner that should be?

			break;
		case DOCUMENTATION_MODIFIED:
		case NAME_MODIFIED:
			break;
		default:
			// LOGGER.debug(event.getType() + " - " + getSource(event) + " on " + getNode() + " changed to: "
			// + getNewValue(event) + " from " + getOldValue(event));

			break;
		}
	}
}
