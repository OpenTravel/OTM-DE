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

import org.opentravel.schemacompiler.event.ModelElementListener;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.VersionNode;
import org.opentravel.schemas.node.interfaces.InheritedInterface;
import org.opentravel.schemas.node.listeners.InheritanceDependencyListener;
import org.opentravel.schemas.node.objectMembers.ContributedFacetNode;
import org.opentravel.schemas.types.TypeProviderAndOwners;
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
		return new ArrayList<>(get());
	}

	@Override
	public List<TypeProviderAndOwners> getChildren_TypeProviders() {
		final ArrayList<TypeProviderAndOwners> providers = new ArrayList<>();
		if (children != null)
			for (Node n : get()) {
				if (n instanceof VersionNode)
					n = ((VersionNode) n).get();
				if (n instanceof TypeProviderAndOwners)
					providers.add((TypeProviderAndOwners) n);
			}
		return providers;
	}

	@Override
	public List<TypeUser> getChildren_TypeUsers() {
		final ArrayList<TypeUser> users = new ArrayList<>();
		if (children != null)
			for (Node n : get()) {
				if (n instanceof VersionNode) // should this be facadeInterface?
					n = ((VersionNode) n).get();
				if (n instanceof TypeUser)
					users.add((TypeUser) n);
			}
		return users;
	}

	// @Override
	// public List<LibraryMemberInterface> getDescendants_LibraryMembers() {
	// // keep duplicates out of the list that version aggregates may introduce
	// HashSet<LibraryMemberInterface> namedKids = new HashSet<LibraryMemberInterface>();
	// for (Node c : get()) {
	// if (c.isDeleted())
	// continue;
	// // TL model considers services as named library member
	// if (c.isLibraryMember())
	// if (c instanceof FacadeInterface)
	// namedKids.add((LibraryMemberInterface) ((FacadeInterface) c).get());
	// else
	// namedKids.add((LibraryMemberInterface) c);
	// else if (c.hasChildren())
	// namedKids.addAll(c.getDescendants_LibraryMembers());
	// }
	// return new ArrayList<LibraryMemberInterface>(namedKids);
	// }

	@Override
	public List<C> getInheritedChildren() {
		return (List<C>) (inherited != null ? inherited : Collections.emptyList());
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
		ArrayList<C> kids = new ArrayList<>();
		for (C c : get()) {
			if (c == null)
				LOGGER.debug("Null child.");
			if (c.isNavChild(deep))
				kids.add(c);
		}
		for (C c : getInheritedChildren())
			if (c.isNavChild(deep))
				kids.add(c);
		return kids;
	}

	@Override
	public boolean hasNavChildren(boolean deep) {
		for (final C n : get())
			if (n.isNavChild(deep))
				return true;
		for (final C n : getInheritedChildren())
			if (n.isNavChild(deep))
				return true;
		return false;
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
		if (child == null || child.isEmpty())
			return;
		for (Node n : child) {
			// Get the base TL object and make sure it is valid
			if (!(n instanceof InheritedInterface))
				continue;
			Node base = ((InheritedInterface) n).getInheritedFrom();
			if (base instanceof ContributedFacetNode)
				base = ((ContributedFacetNode) base).get();
			if (base == null)
				continue;
			TLModelElement baseTL = base.getTLModelObject();
			if (baseTL == null)
				continue;
			// Get the node that should be pointed to by the listener
			if (n instanceof ContributedFacetNode)
				n = ((ContributedFacetNode) n).getContributor();

			// Remove this inheritance listener from base TL object (and avoid co-modification)
			List<ModelElementListener> listeners = new ArrayList<>(baseTL.getListeners());
			for (ModelElementListener l : listeners)
				if (l instanceof InheritanceDependencyListener)
					if (((InheritanceDependencyListener) l).getNode() == n) {
						baseTL.removeListener(l);
						LOGGER.debug("Removed listener for " + n);
					}
		}
	}
}
