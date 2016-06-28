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

import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemas.node.Node.NodeVisitor;
import org.opentravel.schemas.node.interfaces.Enumeration;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.listeners.ListenerFactory;
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
			// LOGGER.debug("CloseVisitor: closing " + n);
			Node node = (Node) n;

			// Use override behavior because Library nodes must clear out context.
			if (node instanceof LibraryNode) {
				((LibraryNode) node).close(); // libraries may or may not do members
			}

			// Unlink from tree
			node.deleted = true;
			if (node.getParent() != null && node.getParent().getChildren() != null) {
				node.getParent().getChildren().remove(node);
				if (node.getParent() instanceof FamilyNode)
					node.getParent().updateFamily();
			}
			node.setParent(null);
			node.setLibrary(null);

			// LOGGER.debug("CloseVisitor: closed  " + n);
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
				LOGGER.debug("DeleteVisitor: not delete-able " + n);
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

			// Unlink from tree
			node.deleted = true;
			if (node.getParent() != null)
				node.getParent().remove(node);
			else
				LOGGER.warn("Warning, tried to delete " + node + " with no parent--skipped remove().");

			node.setParent(null);
			node.setLibrary(null);

			// Remove the TL entity from the TL Model.
			if (node.modelObject != null) {
				TLModelElement tlObj = node.getTLModelObject();
				node.modelObject.delete();
				ListenerFactory.clearListners(tlObj); // remove any listeners
				LOGGER.debug("DeleteVisitor: deleted tl object " + node);
			}

			LOGGER.debug("DeleteVisitor: deleted  " + nodeName);
		}
	}

	/**
	 * Assure node name conforms to the rules
	 * 
	 * @author Dave Hollander
	 * 
	 */
	public class FixNames implements NodeVisitor {

		@Override
		public void visit(INode in) {
			Node n = (Node) in;
			if (n instanceof ElementNode)
				n.setName(NodeNameUtils.fixElementName(n));
			else if (n instanceof AttributeNode)
				n.setName(NodeNameUtils.fixAttributeName(n.getName()));
			else if (n instanceof IndicatorNode)
				n.setName(NodeNameUtils.fixIndicatorName(n.getName()));
			else if (n instanceof Enumeration)
				n.setName(NodeNameUtils.fixEnumerationName(n.getName()));
			else if (n.isSimpleType())
				n.setName(NodeNameUtils.fixSimpleTypeName(n.getName()));
			else if (n instanceof VWA_Node)
				n.setName(NodeNameUtils.fixVWAName(n.getName()));
			else if (n instanceof CoreObjectNode)
				n.setName(NodeNameUtils.fixCoreObjectName(n.getName()));
			else if (n instanceof BusinessObjectNode)
				n.setName(NodeNameUtils.fixBusinessObjectName(n.getName()));
			else if (n instanceof AliasNode)
				n.setName(NodeNameUtils.adjustCaseOfName(PropertyNodeType.ALIAS, n.getName()));
			else if (n instanceof IndicatorElementNode)
				n.setName(NodeNameUtils.fixIndicatorElementName(n.getName()));
			else if (n instanceof ElementReferenceNode) {
				n.setName(n.getName());
			}
		}
	}

}
