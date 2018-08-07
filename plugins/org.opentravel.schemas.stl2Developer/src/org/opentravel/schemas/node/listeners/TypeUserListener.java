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
import org.opentravel.schemas.types.TypeUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A node identity listener used on nodes that are assigned types (type users). Handles modified type assignments.
 * 
 * @author Dave
 *
 */
public class TypeUserListener extends NodeIdentityListener implements INodeListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(TypeUserListener.class);

	/**
	 * 
	 */
	public TypeUserListener(Node node) {
		super(node);
		assert node instanceof TypeUser;
	}

	@Override
	public void processValueChangeEvent(ValueChangeEvent<?, ?> event) {
		super.processValueChangeEvent(event); // logger.debug statements

		switch (event.getType()) {
		case NAME_MODIFIED:
			break;
		case DOCUMENTATION_MODIFIED:
			break;
		case TYPE_ASSIGNMENT_MODIFIED:
			break;
		default:
			break;
		}
	}

	@Override
	public void processOwnershipEvent(OwnershipEvent<?, ?> event) {
	}

}
