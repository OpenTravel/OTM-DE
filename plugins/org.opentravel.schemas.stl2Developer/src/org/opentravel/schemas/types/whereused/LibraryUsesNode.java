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
import org.opentravel.schemas.node.interfaces.WhereUsedNodeInterface;
import org.opentravel.schemas.node.libraries.LibraryNode;

/**
 * Root of tree that describes libraries and type providers used by the owner library or library chain.
 * 
 * Branch nodes represent the collection of all libraries that provide types to the owning library.
 * 
 * @author Dave Hollander
 * 
 */
public class LibraryUsesNode extends WhereUsedNode<LibraryNode> implements WhereUsedNodeInterface {
	// private static final Logger LOGGER = LoggerFactory.getLogger(LibraryDependsOnNode.class);

	public LibraryUsesNode(final LibraryNode lib) {
		super(lib);
		labelProvider = simpleLabelProvider("Uses");
	}

	@Override
	public String getDecoration() {
		String decoration = " ";
		decoration += "(Libraries that provide types to " + owner.getName();
		if (owner.getChain() != null)
			decoration += " version " + owner.getVersion_Major() + "+";
		decoration += ")";
		// decoration += this.getClass().getSimpleName();
		return decoration;
	}

	/**
	 * If this is the owner library, get all of the libraries containing type providers.
	 */
	@Override
	public List<Node> getChildren() {
		if (owner != null) {
			List<Node> providerLibs = new ArrayList<Node>();
			for (LibraryNode l : owner.getAssignedLibraries(true))
				providerLibs.add(new LibraryProviderNode(l, owner));
			return providerLibs;
		} else
			return Collections.emptyList();
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

	@Override
	public List<Node> getTreeChildren(boolean deep) {
		return getChildren();
	}

}
