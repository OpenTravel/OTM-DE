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
import org.opentravel.schemas.node.interfaces.ExtensionOwner;
import org.opentravel.schemas.node.interfaces.WhereUsedNodeInterface;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeUser;
import org.opentravel.schemas.types.WhereAssignedHandler;
import org.opentravel.schemas.types.WhereExtendedHandler;

/**
 * Root of where used tree anchored to type providers in the navigation trees.
 * 
 * Describes where this type provider owner and previous minor versions of this owner and any of their sub-facets are
 * used as an assigned type or extension base.
 * 
 * Leaves are computed by the owner's {@link WhereAssignedHandler} and {@link WhereExtendedHandler}. Leaf
 * {@link TypeUserNode}s are dynamically assigned to reduce memory footprint and allow navigation to find the actual
 * library member.
 * 
 * @author Dave Hollander
 * 
 */
public class TypeProviderWhereUsedNode extends WhereUsedNode<TypeProvider> implements WhereUsedNodeInterface {
	// private static final Logger LOGGER = LoggerFactory.getLogger(WhereTypeProviderUsedNode.class);

	/**
	 * Create a new Where Used complete with new TL model and link to component
	 */
	public TypeProviderWhereUsedNode(final TypeProvider provider) {
		super(provider);
	}

	@Override
	public String getDecoration() {
		String decoration = "  ";
		decoration += "All versions of " + owner.getName() + " in this chain are used by:";
		return decoration;
	}

	@Override
	public String getLabel() {
		return labelProvider.getLabel() + " (" + getWhereUsedCount() + ")";
	}

	/**
	 * 
	 * @return count of where all minor versions of this provider is used as type or extension
	 */
	public int getWhereUsedCount() {
		int count = 0;
		for (Node v : getAllVersions())
			count += ((TypeProvider) v).getWhereUsedAndDescendantsCount();
		// versioned objects will include themselves so decrement count.
		if (((Node) owner).getVersionNode() != null)
			if (((Node) owner).getVersionNode().getPreviousVersion() != null)
				count--;
		return count;
	}

	/**
	 * @return list of all users of any version as type or extension
	 */
	public List<TypeUser> getAllUsers(boolean editableOnly) {
		List<TypeUser> users = new ArrayList<>();
		for (Node version : getAllVersions()) {
			for (TypeUser user : ((TypeProvider) version).getWhereAssigned())
				if (editableOnly) {
					if (user.getLibrary().isInHead2())
						users.add(user);
				} else
					users.add(user);
		}
		return users;
	}

	/**
	 * 
	 * @param editableOnly
	 * @return all extension owners that extend this node or any version of this node. Exclude other versions of this
	 *         node.
	 */
	public List<ExtensionOwner> getAllExtensions(boolean editableOnly) {
		List<ExtensionOwner> owners = new ArrayList<>();
		for (Node version : getAllVersions()) {
			if (version.getWhereExtendedHandler() != null) {
				for (ExtensionOwner e : version.getWhereExtendedHandler().getWhereExtended()) {
					if (owner.getName().equals(((Node) e).getName()))
						continue; // skip versions of this node.
					if (editableOnly) {
						if (((Node) e).getLibrary().isInHead2())
							owners.add(e);
					} else
						owners.add(e);
				}
			}
		}
		return owners;
	}

	/**
	 * @return live list of all versions of this object or list with just this object
	 */
	public List<Node> getAllVersions() {
		if (((Node) owner).getVersionNode() != null)
			return ((Node) owner).getVersionNode().getAllVersions();

		List<Node> versions = new ArrayList<>();
		versions.add((Node) owner);
		return versions;
	}

	/**
	 * Get all of the components that use any aspect of the owning component. DO NOT make this a getChildren or the tree
	 * will become invalid with nodes having multiple parents which will break lots of getChildren() users.
	 * 
	 * @return new list of children
	 */
	@Override
	public List<Node> getChildren() {
		List<Node> users = new ArrayList<>();
		if (owner == null)
			return Collections.emptyList();

		// Get all users of all versions of this object
		for (Node version : getAllVersions()) {
			if (version instanceof TypeProvider) {
				for (TypeUser u : ((TypeProvider) version).getWhereUsedAndDescendants())
					users.add(new TypeUserNode(u));
			}
			if (version instanceof ExtensionOwner) {
				for (ExtensionOwner whereExtended : version.getWhereExtendedHandler().getWhereExtended())
					if (whereExtended != owner)
						users.add(new ExtensionUserNode(whereExtended));
			}
		}

		// return stripOlderVersions(users);
		return users;
	}

	// 4/9/2017 - needed but does not work
	private List<Node> stripOlderVersions(List<Node> users) {
		List<Node> strippedList = new ArrayList<>();
		for (Node n : users) {
			if (n.getOwningComponent() instanceof ExtensionOwner) {
				String eName = ((ExtensionOwner) n.getOwningComponent()).getExtendsTypeName();
				String oName = n.getOwningComponent().getName();
				Node eType = ((Node) n.getOwningComponent()).getExtendsType();
				if (!eName.equals(oName))
					strippedList.add(n); // Not a versioning relationship
				if (n.getOwningComponent().isLatestVersion())
					strippedList.add(n);
			}
		}
		return strippedList;
	}

	@Override
	public boolean hasChildren() {
		return !getChildren().isEmpty();
	}

	// /**
	// * Always true because lazy evaluation of children.
	// */
	// @Override
	// public boolean hasTreeChildren(boolean deep) {
	// return true;
	// }

}
