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
package org.opentravel.schemas.types.whereused;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.interfaces.WhereUsedNodeInterface;
import org.opentravel.schemas.node.libraries.LibraryNode;

/**
 * Root of a tree describing all the libraries and their type where the onwer's types are used.
 * 
 * Branch node assigned to a library to describe all the other libraries that use types from this library or any of the
 * versions in the chain.
 * 
 * Leaves are computed from
 * 
 * @author Dave Hollander
 * 
 */
public class LibraryWhereUsedNode extends WhereUsedNode<LibraryNode> implements WhereUsedNodeInterface {
	// private static final Logger LOGGER = LoggerFactory.getLogger(WhereLibraryUsedNode.class);

	public LibraryWhereUsedNode(final LibraryNode lib) {
		super(lib); // assigns owner
	}

	@Override
	public String getDecoration() {
		String decoration = "  (";
		decoration += "Libraries that use types from " + owner.getName();
		if (owner.getChain() != null)
			decoration += " version " + owner.getVersion_Major() + "+";
		decoration += ")";
		// decoration += this.getClass().getSimpleName();
		return decoration;
	}

	@Override
	public String getLabel() {
		return labelProvider.getLabel();
	}

	/**
	 * Get all of the components that use any aspect of the owning component. DO NOT make this a getChildren or the tree
	 * will become invalid with nodes having multiple parents which will break lots of getChildren() users.
	 * 
	 * @return new list of children
	 */
	@Override
	public List<Node> getChildren() {
		Set<Node> users = new HashSet<Node>();
		if (owner == null)
			return Collections.emptyList();

		for (Node l : owner.getWhereUsedHandler().getWhereUsed(true))
			if (l instanceof LibraryNode)
				users.add(new LibraryUserNode((LibraryNode) l, owner));
		return new ArrayList<Node>(users);
	}

	@Override
	public boolean hasChildren() {
		return true;
	}

	/**
	 * Always true because lazy evaluation of children.
	 */
	@Override
	public boolean hasTreeChildren(boolean deep) {
		return true;
	}

}
