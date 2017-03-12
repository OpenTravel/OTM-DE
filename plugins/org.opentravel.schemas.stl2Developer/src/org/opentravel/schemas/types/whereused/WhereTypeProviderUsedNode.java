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
import java.util.List;

import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.VersionNode;
import org.opentravel.schemas.node.interfaces.ExtensionOwner;
import org.opentravel.schemas.node.interfaces.WhereUsedNodeInterface;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeUser;
import org.opentravel.schemas.types.WhereAssignedHandler;
import org.opentravel.schemas.types.WhereExtendedHandler;

/**
 * Branch node of a tree that describes where this owner and previous minor versions of this owner and any of thier
 * sub-facets are used as an assigned type or extension base.
 * 
 * Leaves are computed by the owner's {@link WhereAssignedHandler} and {@link WhereExtendedHandler}. Leaf
 * {@link TypeProviderUserNode}s are dynamically assigned to reduce memory footprint and allow navigation to find the
 * actual library member.
 * 
 * @author Dave Hollander
 * 
 */
public class WhereTypeProviderUsedNode extends WhereUsedNode<TypeProvider> implements WhereUsedNodeInterface {
	// private static final Logger LOGGER = LoggerFactory.getLogger(WhereTypeProviderUsedNode.class);

	/**
	 * Create a new Where Used complete with new TL model and link to component
	 */
	public WhereTypeProviderUsedNode(final TypeProvider parent) {
		super(parent);
	}

	@Override
	public String getDecoration() {
		String decoration = "  ";
		decoration += "All versions of " + owner.getName() + " in this chain are used by:";
		//
		decoration += "  " + this.getClass().getSimpleName();
		return decoration;
	}

	@Override
	public String getLabel() {
		return labelProvider.getLabel() + " (" + ((TypeProvider) owner).getWhereUsedAndDescendantsCount() + ")";
	}

	/**
	 * Get all of the components that use any aspect of the owning component. DO NOT make this a getChildren or the tree
	 * will become invalid with nodes having multiple parents which will break lots of getChildren() users.
	 * 
	 * @return new list of children
	 */
	@Override
	public List<Node> getChildren() {
		List<Node> users = new ArrayList<Node>();
		if (owner == null)
			return Collections.emptyList();

		// Get users of all versions of this object
		List<Node> versions = new ArrayList<Node>();
		if (owner.getParent() instanceof VersionNode)
			versions.addAll(((Node) owner).getVersionNode().getAllVersions());
		else
			versions.add((Node) owner);

		// Get all users of this as a type
		for (Node version : versions) {
			if (version instanceof TypeProvider) {
				for (TypeUser u : ((TypeProvider) version).getWhereUsedAndDescendants())
					users.add(new TypeProviderUserNode(u));
			}
			if (version instanceof ExtensionOwner) {
				for (ExtensionOwner whereExtended : version.getWhereExtendedHandler().getWhereExtended())
					users.add(new ExtensionUserNode(whereExtended));
			}
		}

		return users;
	}

	@Override
	public boolean hasChildren() {
		return false;
	}

	/**
	 * Always true because lazy evaluation of children.
	 */
	@Override
	public boolean hasTreeChildren(boolean deep) {
		return true;
	}

	// True if this node represents a type user
	public boolean isUser() {
		return true;
		// return nodeType.equals(TypeNodeType.USER) ? true : false;
	}
}
