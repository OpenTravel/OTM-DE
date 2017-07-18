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
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.types.TypeUser;

/**
 * Describes a library that is depended upon to provide types to the user library.
 * <p>
 * Children are other LibraryProviderNodes and Type/Extension UserNodes.
 * 
 * @author Dave Hollander
 * 
 */
public class LibraryProviderNode extends WhereUsedNode<LibraryNode> implements WhereUsedNodeInterface {
	// private static final Logger LOGGER = LoggerFactory.getLogger(LibraryDependedOnNode.class);

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
		imageProvider = nodeImageProvider(providerLib.getOwningComponent());
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
	 * If this is the owner library, get all of the libraries containing type providers.
	 * 
	 * @return
	 */
	@Override
	public List<Node> getChildren() {
		if (owner == null)
			return Collections.emptyList();

		List<Node> providerLibs = new ArrayList<Node>();
		// Get libraries assigning types to owner
		// for (LibraryNode l : owner.getAssignedLibraries(false))
		// providerLibs.add(new LibraryProviderNode(l, owner));

		// Get the types in the owner lib that are used in the parent's chain
		for (Node user : owner.getWhereUsedHandler().getUsersOfTypesFromOwnerLibrary((LibraryNode) parent, true))
			if (user instanceof TypeUser)
				providerLibs.add(new TypeUserNode((TypeUser) user));
			else if (user instanceof ExtensionOwner)
				providerLibs.add(new ExtensionUserNode((ExtensionOwner) user));

		return providerLibs;
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

	public LibraryNode getOwner() {
		return (LibraryNode) owner;
	}

}
