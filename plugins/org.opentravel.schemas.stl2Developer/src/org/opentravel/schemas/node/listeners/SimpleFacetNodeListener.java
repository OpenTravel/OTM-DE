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
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAttributeType;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemas.modelObject.TLnSimpleAttribute;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.SimpleFacetNode;
import org.opentravel.schemas.node.VWA_Node;
import org.opentravel.schemas.node.VersionNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dave
 *
 */
public class SimpleFacetNodeListener extends BaseNodeListener implements INodeListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(SimpleFacetNodeListener.class);

	/**
	 * 
	 */
	public SimpleFacetNodeListener(Node node) {
		super(node);
	}

	@Override
	public void processValueChangeEvent(ValueChangeEvent<?, ?> event) {
		// super.processValueChangeEvent(event);
		if (thisNode instanceof VersionNode)
			LOGGER.debug("Value Change to version node " + thisNode + " ignored.");

		switch (event.getType()) {
		case DOCUMENTATION_MODIFIED:
			break;
		case TYPE_ASSIGNMENT_MODIFIED:
			// LOGGER.debug("Type Assignment Change to " + thisNode + ", old  = " + getOldValue(event) + ", new = "
			// + getNewValue(event) + " tl owner = " + thisNode.getTLModelObject().getOwningModel());
			// Set type, add to newValue's user list and remove from old value's user list
			thisNode.getTypeClass().set(getNewValue(event), getOldValue(event));

			// Also set the simple attribute node
			if (getNewValue(event) != null && thisNode.getParent() instanceof VWA_Node) {
				NamedEntity tlTarget = (NamedEntity) getNewValue(event).getTLModelObject();
				if (((SimpleFacetNode) thisNode).getSimpleAttribute() != null) {
					TLModelElement tlAttr = ((SimpleFacetNode) thisNode).getSimpleAttribute().getTLModelObject();
					if (tlAttr instanceof TLnSimpleAttribute && tlTarget instanceof TLAttributeType)
						((TLnSimpleAttribute) tlAttr).setType(tlTarget);
				}
			}
			break;
		default:
			break;
		}
	}

	@Override
	public void processOwnershipEvent(OwnershipEvent<?, ?> event) {
		// super.processOwnershipEvent(event);
	}

}
