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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.interfaces.ExtensionOwner;
import org.opentravel.schemas.node.interfaces.WhereUsedNodeInterface;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.typeProviders.ContextualFacetNode;
import org.opentravel.schemas.types.TypeUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Describes a library that is depended upon to provide types to the user library.
 * <p>
 * Children are other LibraryProviderNodes and Type/Extension UserNodes.
 * 
 * @author Dave Hollander
 * 
 */
public class LibraryProviderNode extends WhereUsedNode<LibraryNode> implements WhereUsedNodeInterface {
	private static final Logger LOGGER = LoggerFactory.getLogger(LibraryProviderNode.class);

	/**
	 * Create a node to represent the provider library.
	 * 
	 * @param providerLib
	 *            is the library providing types (owner)
	 * @param userLib
	 *            is the library that depends on the provided types (parent/userLib)
	 */
	public LibraryProviderNode(LibraryNode providerLib, LibraryNode userLib) {
		super(providerLib, userLib); // sets owner and parent
		// parent = userLib;
		labelProvider = simpleLabelProvider(providerLib.getName());
		imageProvider = nodeImageProvider((Node) providerLib.getOwningComponent());
	}

	@Override
	public String getDecoration() {
		String decoration = "  (";
		if (owner.getChain() != null)
			decoration += "version " + owner.getVersion_Major();
		decoration += " provides types to ";
		decoration += " " + parent.getName();
		if (parent.getChain() != null)
			decoration += " version " + ((LibraryNode) parent).getVersion_Major() + "+";
		decoration += ")";
		// decoration += this.getClass().getSimpleName();
		return decoration;
	}

	/**
	 * If this is the owner library, get whereUsedNodes for all type users, extension owners and contextual facets.
	 * 
	 * @return
	 */
	@Override
	public List<Node> getChildren() {
		if (owner == null)
			return Collections.emptyList();
		return getChildren(true);
	}

	/**
	 * If this is the owner library, get whereUsedNodes for all type users, extension owners and contextual facets.
	 * 
	 * @param deDuped
	 *            if True, the owner name and assigned object name are used as keys to de-duplicate the list
	 * 
	 * @return
	 */
	public List<Node> getChildren(boolean deDuped) {

		// Use hash map with Owner-Type names as key to limit the entries to 1 per owner per type provider
		Map<String, WhereUsedNode<?>> providerMap = new HashMap<String, WhereUsedNode<?>>();
		String key = "";

		// Get the types in the owner lib that are used in the parent's chain
		int i = 0;
		for (Node user : owner.getWhereUsedHandler().getUsersOfTypesFromOwnerLibrary((LibraryNode) parent, true)) {
			key = String.valueOf(++i);
			if (deDuped)
				key = user.getOwningComponent().getName();
			if (user instanceof TypeUser) {
				if (deDuped)
					key += ((TypeUser) user).getAssignedType().getName();
				providerMap.put(key, new TypeUserNode((TypeUser) user));
			} else if (user instanceof ExtensionOwner) {
				if (deDuped)
					key += ((ExtensionOwner) user).getExtensionBase();
				providerMap.put(key, new ExtensionUserNode((ExtensionOwner) user));
			} else if (user instanceof ContextualFacetNode) {
				if (deDuped)
					key += ((ContextualFacetNode) user).getWhereContributed().getOwningComponent();
				providerMap.put(key, new ContextualFacetUserNode((ContextualFacetNode) user));
			}
		}
		ArrayList<Node> x = new ArrayList<Node>(providerMap.values());
		return new ArrayList<Node>(providerMap.values());
	}

	@Override
	public boolean hasChildren() {
		return true;
	}

	@Override
	public boolean hasNavChildren(boolean deep) {
		return true;
	}

	/**
	 * Always true because lazy evaluation of children.
	 */
	@Override
	public boolean hasTreeChildren(boolean deep) {
		return true;
	}

	@Override
	public LibraryNode getOwner() {
		return owner;
	}

}
