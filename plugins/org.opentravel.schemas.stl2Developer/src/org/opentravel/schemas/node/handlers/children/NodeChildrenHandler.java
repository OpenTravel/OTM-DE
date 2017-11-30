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
import java.util.List;

import org.opentravel.schemacompiler.event.ModelElementListener;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.VersionNode;
import org.opentravel.schemas.node.listeners.InheritanceDependencyListener;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class provides access to sets of children and inherited children specific to each method.
 * 
 * @author Dave Hollander
 * 
 */
// TODO - make the generic the type of node and the extensions add type of owner
public abstract class NodeChildrenHandler<C extends Node> implements ChildrenHandlerI<C> {
	private final static Logger LOGGER = LoggerFactory.getLogger(NodeChildrenHandler.class);

	// protected Node owner;
	protected boolean initRunning = false; // flag to prevent clearing children during initialization
	protected List<C> children = null;
	protected List<C> inherited = null;
	protected Node inheritedOwner = null; // The owning facet member that provides the inherited copies.

	public NodeChildrenHandler() {
	}

	@Override
	public boolean contains(C item) {
		return children.contains(item);
	}

	// Must override in backing store if children can be initialized
	@Override
	public List<C> get() {
		return children;
	}

	@Override
	public List<C> getChildren_New() {
		return new ArrayList<C>(get());
	}

	@Override
	public List<TypeProvider> getChildren_TypeProviders() {
		final ArrayList<TypeProvider> providers = new ArrayList<TypeProvider>();
		if (children != null)
			for (Node n : get()) {
				if (n instanceof VersionNode)
					n = ((VersionNode) n).get();
				if (n instanceof TypeProvider)
					providers.add((TypeProvider) n);
			}
		return providers;
	}

	@Override
	public List<TypeUser> getChildren_TypeUsers() {
		final ArrayList<TypeUser> users = new ArrayList<TypeUser>();
		if (children != null)
			for (Node n : get()) {
				if (n instanceof VersionNode) // should this be facadeInterface?
					n = ((VersionNode) n).get();
				if (n instanceof TypeUser)
					users.add((TypeUser) n);
			}
		return users;
	}

	@Override
	public List<C> getInheritedChildren() {
		return inherited;
	}

	@Override
	public boolean hasChildren() {
		return !get().isEmpty();
	}

	@Override
	public boolean hasChildren_TypeProviders() {
		return !getChildren_TypeProviders().isEmpty();
	}

	@Override
	public boolean hasInheritedChildren() {
		List<C> inheritedChildren = getInheritedChildren();
		return (inheritedChildren != null) && !inheritedChildren.isEmpty();
	}

	@Override
	public List<C> getNavChildren(boolean deep) {
		ArrayList<C> kids = new ArrayList<C>();
		for (C c : get())
			if (c.isNavChild(deep))
				kids.add(c);
		return kids;
	}

	@Override
	public boolean hasNavChildren(boolean deep) {
		for (final C n : get())
			if (n.isNavChild(deep))
				return true;
		return false;
	}

	@Override
	public List<C> getTreeChildren(boolean deep) {
		return getNavChildren(deep);
	}

	// Override on classes that add to getNavChildren()
	@Override
	public boolean hasTreeChildren(boolean deep) {
		return hasNavChildren(deep);
	}

	/**
	 * Utility function to remove inherited listeners from objects in the list.
	 * 
	 * @param child
	 */
	protected void clearInheritedListeners(List<C> child) {
		if (child == null)
			return;
		for (Node n : child) {
			if (n.getInheritedFrom() != null && n.getInheritedFrom().getTLModelObject() != null) {
				// Remove listeners and avoid co-modification
				List<ModelElementListener> listeners = new ArrayList<ModelElementListener>(n.getInheritedFrom()
						.getTLModelObject().getListeners());
				for (ModelElementListener l : listeners)
					if (l instanceof InheritanceDependencyListener)
						if (((InheritanceDependencyListener) l).getNode() == n)
							n.getInheritedFrom().getTLModelObject().removeListener(l);
			}
		}
	}

}
