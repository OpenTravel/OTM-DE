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
package org.opentravel.schemas.node;

import org.opentravel.schemas.node.Node.NodeVisitor;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.resources.ResourceNode;
import org.opentravel.schemas.node.typeProviders.ContextualFacetNode;
import org.opentravel.schemas.node.typeProviders.ImpliedNode;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Node visitors for generic, commonly used functions.
 * 
 * Sample Usage: NodeVisitor visitor = new NodeVisitors().new validateNodeTypes(); curNode.visitAllNodes(visitor);
 * 
 * @author Dave Hollander
 * 
 */
public class NodeVisitors {
	private static final Logger LOGGER = LoggerFactory.getLogger(NodeVisitors.class);

	/**
	 * Close this node visitors for closing objects with children. Close removes from model but does not modify
	 * contents. Does remove type assignments. Does not change the underlying TL model, delete children or change view
	 * contents.
	 * <p>
	 * Use delete visitor if changes are to be made to the TL model. Sample Usage: NodeVisitor visitor = new
	 * NodeVisitors().new validateNodeTypes(); curNode.visitAllNodes(visitor);
	 * 
	 * @author Dave Hollander
	 * 
	 */
	public class closeVisitor implements NodeVisitor {

		@Override
		public void visit(INode n) {
			// LOGGER.debug("CloseVisitor: closing " + n);
			if (n == null)
				return;
			Node node = (Node) n;

			// Use override behavior because Library nodes must clear out context.
			if (node instanceof LibraryNode)
				if (node.getParent() != null) {
					LOGGER.warn("Invalid visit - libraries with parents should not be closed.");
					assert false;
				} else
					((LibraryNode) node).close(); // libraries may or may not do members

			if (node instanceof ContextualFacetNode)
				((ContextualFacetNode) node).close();

			if (node instanceof VersionNode)
				((VersionNode) node).close();

			// if (node instanceof ServiceNode)
			// LOGGER.debug("Service node - parent is: " + node.getParent());

			// Remove from where used list
			if (node instanceof TypeProvider)
				((TypeProvider) node).removeAll(false);

			if (node instanceof TypeUser)
				((TypeUser) node).setAssignedType();

			// Unlink from tree
			node.deleted = true;
			if (node.getParent() != null && node.getParent().getChildrenHandler() != null)
				node.getParent().getChildrenHandler().clear(node);

			node.setParent(null);
		}
	}

	/**
	 * Delete this node and its and TL model. Does delete type assignments. Does not delete children nor change view
	 * contents. Use close visitor if no changes are to be made to the TL model.
	 * 
	 * @author Dave Hollander
	 * 
	 */
	public class deleteVisitor implements NodeVisitor {

		@Override
		public void visit(INode n) {
			// LOGGER.debug("DeleteVisitor: deleting " + n);
			Node node = (Node) n;

			if (node instanceof ImpliedNode)
				return;
			if (node instanceof ServiceNode) {
				node.delete();
				return;
			} else if (node instanceof ResourceNode) {
				node.delete(); // resource will do children, type users and chain
				return;
			}
			// NOTE - libraries are ALWAYS delete-able even when not edit-able
			if (!node.isDeleteable()) {
				// LOGGER.debug("Exit - not delete-able " + n);
				return;
			}

			node.deleted = true;

			// Remove from where used list
			if (node instanceof TypeProvider)
				((TypeProvider) node).removeAll(true);

			if (node instanceof TypeUser)
				((TypeUser) node).setAssignedType();

			// Use override behavior because Library nodes must clear out project and context.
			if (n instanceof LibraryNode) {
				// You can't just delete libraries without knowing their context (LibraryOwner)
				if (n.getParent() != null)
					assert false;
				((LibraryNode) n).delete(false); // just the library, not its members
			}

			// Remove from version aggregate
			if (node.getVersionNode() != null)
				node.getVersionNode().remove(node);

			// Unlink from tree
			if (node.getParent() != null && node.getParent().getChildrenHandler() != null)
				node.getParent().getChildrenHandler().clear(node);

			// Remove the TL entity from the TL Model.
			node.deleteTL();

			node.setParent(null);
			if (node instanceof LibraryMemberInterface)
				((LibraryMemberInterface) node).setLibrary(null);

			// LOGGER.debug("DeleteVisitor: deleted " + nodeName);
		}
	}

	/**
	 * Assure node name conforms to the rules
	 * 
	 * @author Dave Hollander
	 * 
	 */
	public class FixNames implements NodeVisitor {
		// FIXME - should not be needed. the node name utils are embedded in node name setters now (11/2016)
		@Override
		public void visit(INode in) {
			Node n = (Node) in;
			String name = n.getName();
			if (name == null)
				name = "";
			n.setName(name);
		}
	}
}
