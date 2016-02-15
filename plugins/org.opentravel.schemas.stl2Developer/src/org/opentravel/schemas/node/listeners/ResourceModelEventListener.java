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
import org.opentravel.schemacompiler.event.ModelEventListener;
import org.opentravel.schemacompiler.event.OwnershipEvent;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.interfaces.ResourceMemberInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Add or remove children from model. see ResourceMemberChangeIntegrityChecker for example.
 * 
 * Example: removing a Parameter Group: source = param group type affected item = parameter
 * 
 * @author Dave Hollander
 *
 */
public class ResourceModelEventListener implements
		ModelEventListener<OwnershipEvent<TLResource, TLModelElement>, TLResource> {
	private static final Logger LOGGER = LoggerFactory.getLogger(ResourceModelEventListener.class);

	@Override
	public void processModelEvent(OwnershipEvent<TLResource, TLModelElement> event) {
		ResourceMemberInterface source = null;
		ResourceMemberInterface affected = null;
		if (event.getSource() instanceof TLModelElement)
			source = getSourceNode(event.getSource());
		if (event.getAffectedItem() instanceof TLModelElement)
			affected = getSourceNode(event.getAffectedItem());
		// LOGGER.debug("Event type: " + event.getType() + "  Source = " + source + ", Affected = " + affected);
		if (affected == null || source == null)
			return;

		switch (event.getType()) {
		case ACTION_ADDED:
		case ACTION_FACET_ADDED:
		case ACTION_REQUEST_ADDED:
		case ACTION_RESPONSE_ADDED:
		case PARAM_GROUP_ADDED:
			break;
		case ACTION_REMOVED:
		case ACTION_FACET_REMOVED:
		case ACTION_REQUEST_REMOVED:
		case ACTION_RESPONSE_REMOVED:
		case PARAM_GROUP_REMOVED:
		case PARAMETER_REMOVED:
			// Triggered by ResourceModelInterface.delete() which will delete children, clear listeners and remove
			// itself from parent
			// Just handle dependencies here
			for (ModelElementListener l : event.getAffectedItem().getListeners())
				if (l instanceof ResourceDependencyListener)
					((ResourceMemberInterface) ((ResourceDependencyListener) l).getNode()).removeDependency(affected);
			break;
		default:
			LOGGER.debug("Unhandled event: " + event.getType() + " source = " + source + " affected = " + affected);
			break;
		}
	}

	@Override
	public Class<?> getEventClass() {
		return OwnershipEvent.class;
	}

	@Override
	public Class<TLResource> getSourceObjectClass() {
		return null;
	}

	private ResourceMemberInterface getSourceNode(TLModelElement tlSource) {
		Node n = null;
		for (ModelElementListener l : tlSource.getListeners())
			if (l instanceof INodeListener) {
				n = ((INodeListener) l).getNode();
				if (n.getTLModelObject() == tlSource)
					break;
			}
		return n instanceof ResourceMemberInterface ? (ResourceMemberInterface) n : null;
	}
}
