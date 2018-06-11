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
package org.opentravel.schemas.node.handlers.children;

import java.util.Collections;
import java.util.List;

import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.interfaces.LibraryInterface;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNavNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.types.TypeProviderAndOwners;

/**
 * Model node children handler using a static children handler for projects and implied types.
 * <p>
 * 
 * @author Dave
 *
 */
public class LibraryNavChildrenHandler extends StaticChildrenHandler<Node, LibraryNavNode> {
	// private static final Logger LOGGER = LoggerFactory.getLogger(ModelNodeChildrenHandler.class);

	public LibraryNavChildrenHandler(LibraryNavNode owner) {
		super(owner);
	}

	public LibraryInterface getThisLibI() {
		if (!children.isEmpty() && children.get(0) instanceof LibraryInterface)
			return (LibraryInterface) children.get(0);
		return null;
	}

	// LibraryNavNode only ever has 1 child.
	@Override
	public void add(Node c) {
		children.clear();
		children.add(c);
	}

	/**
	 * Return true if the passed library or chain is contained in this nav node. Members of chains are tested as
	 * required.
	 */
	public boolean contains(LibraryInterface li) {
		for (Node child : children) {
			if (child instanceof LibraryChainNode)
				if (((LibraryChainNode) child).contains((Node) li))
					return true;
			if (child instanceof LibraryNode)
				return child == li;
		}
		return false;
	}

	@Override
	public List<TLModelElement> getChildren_TL() {
		return Collections.emptyList();
	}

	@Override
	public List<TLModelElement> getInheritedChildren_TL() {
		return Collections.emptyList();
	}

	@Override
	public boolean hasNavChildren(boolean deep) {
		return getThisLibI() != null ? getThisLibI().hasNavChildren(deep) : false;
	}

	@Override
	public List<Node> getNavChildren(boolean deep) {
		return getThisLibI() != null ? getThisLibI().getNavChildren(deep) : null;
	}

	@Override
	public boolean hasChildren_TypeProviders() {
		return getThisLibI() != null ? ((Node) getThisLibI()).hasChildren_TypeProviders() : false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<TypeProviderAndOwners> getChildren_TypeProviders() {
		return getThisLibI() != null ? ((Node) getThisLibI()).getChildrenHandler().getChildren_TypeProviders()
				: Collections.EMPTY_LIST;
	}

	@Override
	public boolean hasTreeChildren(boolean deep) {
		return getThisLibI() != null ? getThisLibI().hasTreeChildren(deep) : false;
	}

	@Override
	public List<Node> getTreeChildren(boolean deep) {
		return getThisLibI() != null ? getThisLibI().getTreeChildren(deep) : null;
	}

}
