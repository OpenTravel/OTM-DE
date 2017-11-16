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
import org.opentravel.schemas.node.facets.ContextualFacetNode;
import org.opentravel.schemas.node.handlers.children.CachingChildrenHandler;
import org.opentravel.schemas.node.handlers.children.ChildrenHandlerI;
import org.opentravel.schemas.node.handlers.children.StaticChildrenHandler;
import org.opentravel.schemas.node.interfaces.Enumeration;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.properties.AttributeNode;
import org.opentravel.schemas.node.properties.ElementNode;
import org.opentravel.schemas.node.properties.ElementReferenceNode;
import org.opentravel.schemas.node.properties.IndicatorElementNode;
import org.opentravel.schemas.node.properties.IndicatorNode;
import org.opentravel.schemas.node.resources.ResourceNode;
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
	 * Close this node. Do not change the model. Does delete type assignments. Does not delete children nor change view
	 * contents. Use delete visitor if changes are to be made to the TL model.
	 * 
	 * NOTE: not version safe
	 * 
	 * @author Dave Hollander
	 * 
	 */
	public class closeVisitor implements NodeVisitor {

		@Override
		public void visit(INode n) {
			LOGGER.debug("CloseVisitor: closing " + n);
			Node node = (Node) n;

			// Use override behavior because Library nodes must clear out context.
			if (node instanceof LibraryNode)
				((LibraryNode) node).close(); // libraries may or may not do members

			if (node instanceof ContextualFacetNode)
				((ContextualFacetNode) node).close();

			if (node instanceof VersionNode)
				((VersionNode) node).close();

			// Unlink from tree
			node.deleted = true;
			if (node.getParent() != null && node.getParent().getChildrenHandler() != null)
				node.getParent().getChildrenHandler().clear();

			node.setParent(null);
			// node.setLibrary(null);

			LOGGER.debug("CloseVisitor: closed  " + n);
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
			LOGGER.debug("DeleteVisitor: deleting " + n);
			Node node = (Node) n;
			String nodeName = n.getName();

			if ((node instanceof ServiceNode) && node.getLibrary().isInChain()) {
				// this has a entry in the service aggregate but no version node!
				// LOGGER.debug("Deleting Service aggregate node.");
				node.getLibrary().getChain().removeAggregate((ComponentNode) node);
			} else if (node instanceof ResourceNode) {
				node.delete(); // resource will do children, type users and chain
				return;
			}
			// NOTE - libraries are ALWAYS delete-able even when not edit-able
			if (!node.isDeleteable()) {
				// LOGGER.debug("DeleteVisitor: not delete-able " + n);
				return;
			}

			// Remove from where used list
			if (node instanceof TypeProvider)
				((TypeProvider) node).removeAll();

			if (node instanceof TypeUser)
				((TypeUser) node).setAssignedType();

			// Use override behavior because Library nodes must clear out project and context.
			if (n instanceof LibraryNode)
				((LibraryNode) n).delete(false); // just the library, not its members

			// Remove from version aggregate
			if (node.getVersionNode() != null)
				node.getVersionNode().remove(node);

			// Unlink from tree
			node.deleted = true;
			ChildrenHandlerI<?> handler;
			if (node.getParent() != null && node.getParent().getChildrenHandler() != null) {
				// FIXME - delegate so all handlers have method for this purpose
				handler = node.getParent().getChildrenHandler();
				if (handler instanceof CachingChildrenHandler<?, ?>)
					handler.clear();
				else if (handler instanceof StaticChildrenHandler<?, ?>)
					((StaticChildrenHandler<Node, ?>) handler).remove(node);
			}
			// node.getParent().remove(node); // FIXME - use childrenHandler.clear()
			// else
			// LOGGER.warn("Warning, tried to delete " + node + " with no parent--skipped remove().");

			// Remove the TL entity from the TL Model.
			node.deleteTL();

			node.setParent(null);
			if (node instanceof LibraryMemberInterface)
				((LibraryMemberInterface) node).setLibrary(null);

			// // Remove the TL entity from the TL Model.
			// if (node.modelObject != null) {
			// TLModelElement tlObj = node.getTLModelObject();
			// node.deleteTL();
			// // node.modelObject.delete();
			// ListenerFactory.clearListners(tlObj); // remove any listeners
			// // LOGGER.debug("DeleteVisitor: deleted tl object " + node);
			// }

			LOGGER.debug("DeleteVisitor: deleted  " + nodeName);
		}
	}

	/**
	 * Assure node name conforms to the rules
	 * 
	 * @author Dave Hollander
	 * 
	 */
	@Deprecated
	public class FixNames implements NodeVisitor {
		// FIXME - should not be needed. the node name utils are embedded in node name setters now (11/2016)
		@Override
		public void visit(INode in) {
			Node n = (Node) in;
			if (n instanceof ElementNode)
				n.setName(n.getName());
			else if (n instanceof AttributeNode)
				n.setName(n.getName());
			else if (n instanceof IndicatorNode)
				n.setName(n.getName());
			else if (n instanceof Enumeration)
				n.setName(n.getName());
			else if (n instanceof SimpleComponentNode)
				n.setName(n.getName());
			else if (n instanceof VWA_Node)
				n.setName(n.getName());
			else if (n instanceof CoreObjectNode)
				n.setName(n.getName());
			else if (n instanceof BusinessObjectNode)
				n.setName(n.getName());
			else if (n instanceof AliasNode)
				n.setName(n.getName());
			else if (n instanceof IndicatorElementNode)
				n.setName(n.getName());
			else if (n instanceof ElementReferenceNode) {
				n.setName(n.getName());
			}
		}
	}

}
