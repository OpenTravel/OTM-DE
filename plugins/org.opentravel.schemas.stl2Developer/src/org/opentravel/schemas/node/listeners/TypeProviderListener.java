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
import org.opentravel.schemas.node.typeProviders.TypeProviders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This identity listener is used on type providers.
 * 
 * @author Dave
 *
 */
public class TypeProviderListener extends NodeIdentityListener implements INodeListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(TypeProviderListener.class);

	public TypeProviderListener(TypeProviders node) {
		super(node);
		// LOGGER.debug("Added type provider listener to " + node);
	}

	@Override
	public void processValueChangeEvent(ValueChangeEvent<?, ?> event) {
		super.processValueChangeEvent(event);
		// Node newValue = getNewValue(event);
		// Node oldValue = getOldValue(event);
		// LOGGER.debug("Value Change event: " + event.getType() + " this = " + thisNode + ", old = " + oldValue
		// + ", new = " + newValue);

		switch (event.getType()) {
		case NAME_MODIFIED:
			String oldName = null;
			String name = null;
			if (event.getOldValue() instanceof String)
				oldName = (String) event.getOldValue();
			if (event.getNewValue() instanceof String)
				name = (String) event.getNewValue();

			if (oldName != null && !oldName.equals(name)) {
				((TypeProviders) thisNode).setNameOnWhereAssigned(name);
			}

			break;
		case DOCUMENTATION_MODIFIED:
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
		super.processOwnershipEvent(event);
	}

}
