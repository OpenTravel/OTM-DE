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
/**
 * 
 */
package org.opentravel.schemas.node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * These methods test the node model tree. They are safe in that all logic is encapsulated in a LOGGER.debug statement.
 * 
 * @author Dave Hollander
 * 
 *         OBSOLETE - Node_Tests covers the same content requirements plus more. DEAD CLASS - no users
 */
@Deprecated
public class NodeModelTestUtils {
	private static final Logger LOGGER = LoggerFactory.getLogger(NodeModelTestUtils.class);
	private static Node nodeInTest = null;
	private static int testedNodes = 0;

	/**
	 * Test the whole model.
	 */
	public static void testNodeModel() {
		LOGGER.debug("Test Node Model Tree.");
		LOGGER.debug(testTree() + " We now have " + Node.getNodeCount() + " nodes in "
				+ nodeInTest.getLibraries().size() + " libraries.");
	}

	private static String testTree() {
		for (Node n : Node.getModelNode().getChildren())
			testNodeI(n);
		return "Test Node Model Complete, " + testedNodes + " tested.";
	}

	public static void testNode(Node node) {
		testedNodes = 0;
		testNodeI(node);
		// LOGGER.debug("Test node "+node.getName()+" complete, "+testedNodes+" nodes tested.");
	}

	private static void testNodeI(Node n) {
		nodeInTest = n;
		testedNodes++;
		if (n.isDeleted()) {
			LOGGER.error("Test node found a deleted node: " + n.getName());
			return;
		}
		// checkChildren(n);
		// checkNames(n);
		for (Node c : n.getChildren()) {
			testNodeI(c);
		}
	}

	// private static void checkName(String n, String label) {
	// if (n == null)
	// LOGGER.debug("   " + label + " has NULL name.");
	// if (n.isEmpty())
	// LOGGER.debug("   " + label + " has empty name. Class = " + n.getClass().getSimpleName()
	// + " Parent = " + nodeInTest.getParent());
	// }
	//
	// private static void checkNames(Node n) {
	// checkName(n.getName(), "");
	// if (n.getComponentType() == null)
	// LOGGER.debug("Null ComponentType. node = " + nodeInTest.toString());
	// else if (n.getComponentType().isEmpty())
	// LOGGER.debug("Empty ComponentType. node = " + nodeInTest.toString());
	// }

	// /**
	// * Compare the number of node children to the number of model object children.
	// *
	// * @param n
	// */
	// private static void checkChildren(INode n) {
	// if (n.getChildren() == null || n.getModelObject() == null
	// || n.getModelObject().getChildren() == null)
	// return;
	// if (n instanceof LibraryNode)
	// return;
	// if (n.getModelObject() instanceof EmptyMO)
	// return; // dead code - empty.children = null
	// // It is possible to delete operations. Nodes are deleted, but mo still reports them.
	// if (n instanceof OperationNode)
	// return;
	//
	// int nodeKids = n.getChildren().size();
	// int moKids = n.getModelObject().getChildren().size();
	// if (nodeKids != moKids) {
	// LOGGER.debug("\n  Node/ModeObject child counts are not equal.");
	// LOGGER.debug("   Node = " + n + "\t kids = " + nodeKids + "\t class = "
	// + n.getClass().getSimpleName());
	// LOGGER.debug("   MO   = " + n.getModelObject().getName() + "\t kids = " + moKids
	// + "\t class = " + n.getModelObject().getClass().getSimpleName());
	// LOGGER.debug("   tl   = "
	// + n.getModelObject().getTLModelObj().getClass().getSimpleName());
	// }
	// // else LOGGER.debug("   child counts are equal.");
	// }

}
