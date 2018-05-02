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
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.typeProviders.ContextualFacetNode;
import org.opentravel.schemas.types.TypeUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Add or remove children from libraries.
 * 
 * @author Dave Hollander
 *
 */
public class LibraryNodeListener extends NodeIdentityListener implements INodeListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(LibraryNodeListener.class);

	public LibraryNodeListener(Node node) {
		super(node);
	}

	@Override
	public void processOwnershipEvent(OwnershipEvent<?, ?> event) {
		Node affectedNode = getAffectedNode(event);
		// LOGGER.debug(
		// "Library Ownership event: " + event.getType() + " this = " + thisNode + " affected = " + affectedNode);
		LibraryNode ln = (LibraryNode) thisNode;

		switch (event.getType()) {
		case MEMBER_ADDED:
			// LOGGER.debug("Member adding Ownership change event: added " + affectedNode + " to " + thisNode);
			// No listener on TLObject - do nothing
			if (affectedNode == null)
				return;
			// In version 1.5 there will be an event for contextual facets, ignore them
			if (affectedNode instanceof ContextualFacetNode)
				if (!((ContextualFacetNode) affectedNode).canBeLibraryMember())
					break;
			ln.getChildrenHandler().add(affectedNode);

			// TODO - versions, aggregates

			// Set where used
			addAssignedTypes(affectedNode);
			break;

		case MEMBER_REMOVED:
			// LOGGER.debug("Ownership change event: removing " + affectedNode + " from " + thisNode);
			// Remove affected from this library
			if (affectedNode == null || affectedNode.getParent() == null)
				return; // happens during deletes

			// Clear assigned types
			clearAssignedTypes(affectedNode);
			// 5/2/2018 - dmh - made symmetric with Member_Added
			// // FIXME - should this be part of delete()
			// for (TypeUser n : affectedNode.getDescendants_TypeUsers())
			// if (n.getAssignedType() != null)
			// n.getAssignedType().getWhereAssignedHandler().removeUser(n);
			// if (affectedNode instanceof TypeUser)
			// if (((TypeUser) affectedNode).getAssignedType() != null)
			// ((TypeUser) affectedNode).getAssignedType().getWhereAssignedHandler()
			// .removeUser((TypeUser) affectedNode);
			//
			// // FIXME - should remove be destructive? It is used in addMember to move and this breaks that.
			// // affectedNode.delete();

			ln.getChildrenHandler().remove(affectedNode);
			// TODO - versions, aggregates
			break;

		default:
			// LOGGER.debug("Unhandled Ownership event: " + event.getType() + " this = " + thisNode + " affected = "
			// + affectedNode);
			break;
		}
	}

	/**
	 * Remove affected node and all its type user descendants from type provider's where used lists.
	 * 
	 * @param affectedNode
	 */
	private void clearAssignedTypes(Node affectedNode) {
		for (TypeUser n : affectedNode.getDescendants_TypeUsers())
			if (n.getAssignedType() != null)
				n.getAssignedType().getWhereAssignedHandler().removeUser(n);
		if (affectedNode instanceof TypeUser)
			if (((TypeUser) affectedNode).getAssignedType() != null)
				((TypeUser) affectedNode).getAssignedType().getWhereAssignedHandler()
						.removeUser((TypeUser) affectedNode);
	}

	/**
	 * Add affected node and all its type user descendants to their type provider's where used lists.
	 * 
	 * @param affectedNode
	 */
	private void addAssignedTypes(Node affectedNode) {
		for (TypeUser n : affectedNode.getDescendants_TypeUsers())
			if (n.getAssignedType() != null)
				n.getAssignedType().getWhereAssignedHandler().addUser(n);
		if (affectedNode instanceof TypeUser)
			if (((TypeUser) affectedNode).getAssignedType() != null)
				((TypeUser) affectedNode).getAssignedType().getWhereAssignedHandler().addUser((TypeUser) affectedNode);
	}
}
