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
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeUser;

/**
 * Represents a library (owner) that uses types from a provider library.
 * 
 * @author Dave Hollander
 * 
 */
public class LibraryUserNode extends WhereUsedNode<LibraryNode> implements WhereUsedNodeInterface {
	// private static final Logger LOGGER = LoggerFactory.getLogger(LibraryUserNode.class);

	/**
	 * 
	 * Owner will always be the head of the chain.
	 * 
	 * @param userLibrary
	 *            - owner of this node is the library that uses types from the other library
	 * @param providerLibrary
	 *            is the library that provides types to the user library
	 */
	public LibraryUserNode(LibraryNode userLibrary, LibraryNode providerLibrary) {
		super(userLibrary, providerLibrary);
		if (userLibrary.getChain() != null && userLibrary.getChain().getHead() != null)
			owner = userLibrary.getChain().getHead();
		// this.parent = providerLibrary;
		labelProvider = simpleLabelProvider(userLibrary.getName());
		imageProvider = nodeImageProvider((Node) providerLibrary.getOwningComponent());
	}

	@Override
	public String getDecoration() {
		String decoration = "  ";
		if (owner.getChain() != null)
			decoration += "(version " + owner.getVersion_Major() + "+)";
		// decoration += "  " + this.getClass().getSimpleName();
		return decoration;
	}

	@Override
	public String getLabel() {
		return owner instanceof TypeProvider ? labelProvider.getLabel() + " ("
				+ ((TypeProvider) owner).getWhereUsedAndDescendantsCount() + ")" : labelProvider.getLabel();
	}

	/**
	 * Get all of the components that use any any types from of the owning library or chain. TODO - see de-dup handling
	 * 
	 * @return new list of children
	 */
	// TEST - see de-dup handling
	// TEST - add contextual facets as dependents
	@Override
	public List<Node> getChildren() {
		return getChildren(true);
	}

	/**
	 * Get type users.
	 * 
	 * @param deDuped
	 *            when true use owing object - type as unique key {@link LibraryProviderNode#getChildren(boolean)}
	 * @return
	 */
	public List<Node> getChildren(boolean deDuped) {
		if (owner == null)
			return Collections.emptyList();

		// Use hash map with Owner-Type names as key to limit the entries to 1 per owner per type provider
		Map<String, WhereUsedNode<?>> providerMap = new HashMap<String, WhereUsedNode<?>>();
		String key = "";
		int i = 0;

		for (Node l : ((LibraryNode) parent).getWhereUsedHandler().getUsersOfTypesFromOwnerLibrary(owner, true)) {
			key = String.valueOf(++i);
			if (deDuped)
				key = l.getOwningComponent().getName();

			if (l instanceof TypeUser) {
				if (deDuped)
					key += ((TypeUser) l).getAssignedType().getName();
				providerMap.put(key, new TypeUserNode((TypeUser) l));
			} else if (l instanceof ExtensionOwner) {
				if (deDuped)
					key += ((ExtensionOwner) l).getExtensionBase();
				providerMap.put(key, new ExtensionUserNode((ExtensionOwner) l));
			} else if (l instanceof ContextualFacetNode) {
				if (deDuped)
					key += ((ContextualFacetNode) l).getWhereContributed().getOwningComponent();
				providerMap.put(key, new ContextualFacetUserNode((ContextualFacetNode) l));
			}
		}
		return new ArrayList<Node>(providerMap.values());
	}

	@Override
	public boolean hasChildren() {
		return false;
		// return true; // fixme - only type OWNER has children
	}

}
