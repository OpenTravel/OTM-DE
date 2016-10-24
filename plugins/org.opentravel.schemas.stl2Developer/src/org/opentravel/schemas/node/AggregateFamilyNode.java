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

import java.util.List;

/**
 * Aggregate Family Node groups types with the same name prefix under type aggregates (simple/complex).
 * 
 * @author Dave Hollander
 * 
 */
@Deprecated
public class AggregateFamilyNode extends FamilyNode {
	// private static final Logger LOGGER = LoggerFactory.getLogger(FamilyNode.class);

	/**
	 * Create a family node for type aggregates. There are no back links in type aggregates.
	 * 
	 * @param parent
	 * @param name
	 */
	public AggregateFamilyNode(AggregateNode parent, String name) {
		super(name, parent);
		setLibrary(parent.getLibrary());
		assert (parent != null) : "Family Node functions have been removed.";
	}

	/**
	 * Create an aggregate family node and move the members from the parent to this.
	 * 
	 * @param parent
	 * @param name
	 * @param members
	 */
	public AggregateFamilyNode(AggregateNode parent, String name, ComponentNode nodeToAdd, List<Node> members) {
		this(parent, name);
		assert (parent != null) : "Family Node functions have been removed.";
		// List<Node> kids = new ArrayList<Node>(members);
		// for (Node n : kids) {
		// parent.getChildren().remove(n);
		// this.getChildren().add(n);
		// }
		// this.getChildren().add(nodeToAdd); // add to family
	}

	// /**
	// * Simply add to child list. Nothing else.
	// */
	// public void add(Node n) {
	// getChildren().add(n);
	// }

	// /**
	// * Attempt to remove passed node from this family. If successful, update the family and delete it if there is only
	// * one member.
	// *
	// * @param node
	// */
	// protected void remove(Node node) {
	// if (getChildren().remove(node))
	// updateFamily();
	// }

	// @Override
	// protected void updateFamily() {
	// // If only one is left, move it up.
	// final Node parent = getParent();
	// if (getChildren().size() == 1) {
	// final Node child = getChildren().get(0);
	// parent.getChildren().add(child);
	// if (!parent.getChildren().remove(this))
	// LOGGER.info("Error removing " + this.getName() + " from " + parent.getName());
	// deleted = true;
	// getChildren().clear();
	// }
	//
	// }
}
