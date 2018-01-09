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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.typeProviders.TypeProviders;
import org.opentravel.schemas.trees.library.LibraryTreeContentProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Static children handlers maintain a static list of children.
 * <p>
 * No attempt is made in the constructor to create the list. Add and remove methods are added to the base handler for
 * managing the list of children.
 * <p>
 * Designed for use with navigation, library and project level nodes. Implementations of the static handler do not
 * support inheritance or inherited children. These methods will return an empty collection.
 * <p>
 * Assures child is in list only once.
 * <p>
 * <b>Clear the handler</b> to dispose of the node structure. Clearing does <b>not</b> impact the underlying TL Model.
 * 
 * @author Dave Hollander
 * 
 */
public abstract class StaticChildrenHandler<C extends Node, O extends Node> extends NodeChildrenHandler<C> {
	private final static Logger LOGGER = LoggerFactory.getLogger(StaticChildrenHandler.class);

	protected O owner = null;

	public StaticChildrenHandler(O owner) {
		this.owner = owner;
		children = new ArrayList<C>();
		inherited = Collections.emptyList();
	}

	public void add(C item) {
		if (!children.contains(item))
			children.add(item);
	}

	@Override
	public void clear(Node item) {
		children.remove(item);
	}

	@Override
	public void remove(C item) {
		children.remove(item);
	}

	@Override
	public void clear() {
		// NO-OP - this is a static list
	}

	@Override
	public List<TLModelElement> getInheritedChildren_TL() {
		return Collections.emptyList();
	}

	protected void initChildren() {
	}

	protected void initInherited() {
	}

	private void clearList(List<C> list) {
		// FIXME - dispose of node
	}

	@Override
	public String toString() {
		return owner.getName() + "_ChildrenHandler";
	}

	/**
	 * Get all children to be presented in navigator tree. {@link LibraryTreeContentProvider}
	 * <p>
	 * Get all immediate navChildren and where-used nodes to be presented in the OTM Object Tree. . Overridden on nodes
	 * that add nodes such as where used to the tree view.
	 * 
	 * @see {@link #getNavChildren()}
	 * 
	 * @param deep
	 *            - include properties
	 * 
	 * @return new list
	 */
	@Override
	public List<C> getTreeChildren(boolean deep) {
		List<C> navChildren = getNavChildren(deep);
		navChildren.addAll(getInheritedChildren());
		if (owner instanceof TypeProviders && ((TypeProviders) owner).getWhereUsedCount() > 0)
			navChildren.add((C) ((TypeProviders) owner).getWhereUsedNode());
		return navChildren;
	}

}
