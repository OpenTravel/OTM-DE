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

import java.util.ArrayList;
import java.util.Collection;

import org.opentravel.schemacompiler.event.ModelElementListener;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Trivial factory class created to consolidate all the listener assignments for maintenance.
 * 
 * @author Dave
 *
 */
public class ListenerFactory {
	private static final Logger LOGGER = LoggerFactory.getLogger(ListenerFactory.class);

	public ListenerFactory() {
	}

	/**
	 * Set NodeIdentity listener.
	 * 
	 * @param node
	 *            - node to identify
	 */
	public static void setIdentityListner(Node node) {
		setIdentityListner(node, node.getTLModelObject());
	}

	public static void setIdentityListner(Node node, TLModelElement tlObj) {
		if (node == null || tlObj == null)
			return;

		// Get listener from node delegated methods.
		BaseNodeListener listener = node.getNewListener();

		// Assign if there is not already one assigned
		if (listener != null && Node.GetNode(tlObj) == null)
			tlObj.addListener(listener);
	}

	/**
	 * Remove all listeners from this node's tl object.
	 */
	public static void clearListners(Node node) {
		if (node.getTLModelObject() == null)
			return;
		clearListners(node.getTLModelObject());
	}

	/**
	 * Remove all listeners from this node's tl object.
	 */
	public static void clearListners(TLModelElement tlObj) {
		Collection<ModelElementListener> listeners = new ArrayList<>(tlObj.getListeners());
		if (!listeners.isEmpty())
			for (ModelElementListener l : listeners) {
				// just in case an event was not handled, make sure the type is unassigned.
				if (l instanceof TypeUserListener) {
					TypeUser user = (TypeUser) ((TypeUserListener) l).getNode();
					TypeProvider type = user.getAssignedType();
					type.removeWhereAssigned(user);
				}
				tlObj.removeListener(l);
			}
	}
}
