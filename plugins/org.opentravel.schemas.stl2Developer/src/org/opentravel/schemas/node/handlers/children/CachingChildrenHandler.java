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

import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFactory;
import org.opentravel.schemas.node.facets.ContextualFacetNode;
import org.opentravel.schemas.node.facets.ContributedFacetNode;
import org.opentravel.schemas.node.interfaces.ContextualFacetOwnerInterface;
import org.opentravel.schemas.node.listeners.InheritanceDependencyListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Keeps a list of children and inherited children nodes for each parent. List is lazy created and disposed of (cleared)
 * on changes. The TL owner is the authority. Handles access to children, inherited children and various filtered lists
 * of children.
 * <p>
 * No change methods are present by design. These lists are only caches. Changes must be made to the TL model and clear
 * the children handler to effect change.
 * <p>
 * <b>Clear the handler</b> when the object changes. This clears the children and inherited. Event handlers clear
 * children, but events are not always thrown so it <b>must</b> also be done explicitly when changing a parent.
 * <p>
 * Children that are cleared have the TLModelObject removed and are marked as deleted. However,
 * InheritanceDependencyListers can not be moved because we may be inside an event and will cause a comodification
 * exception. To remove these listeners, inherited children are cleared until retrieved and the first child is found to
 * be deleted.
 * 
 * @author Dave Hollander
 * 
 */
public abstract class CachingChildrenHandler<C extends Node, O extends Node> extends NodeChildrenHandler<C> {
	private final static Logger LOGGER = LoggerFactory.getLogger(CachingChildrenHandler.class);

	protected O owner = null;

	public CachingChildrenHandler(O owner) {
		this.owner = owner;
	}

	@Override
	public void clear() {
		if (!initRunning) {
			// FIXME - when running green, try not clearing children list
			// clearList(children); // let node remain and reconnect when re-creating children
			clearList(inherited);
			children = null;
			// inherited = null;
		}
	}

	@Override
	public List<C> get() {
		if (children == null)
			initChildren();
		return children;
	}

	@Override
	public List<C> getInheritedChildren() {
		// If an inherited child is deleted, is was cleared.
		// Remove any obsolete listeners the clear the list.
		if (inherited != null && !inherited.isEmpty())
			if (inherited.get(0).isDeleted()) {
				clearInheritedListeners(inherited);
				inherited = null;
			}

		if (inherited == null)
			initInherited();
		return inherited;
	}

	// Override if inheritance is supported
	@Override
	public List<TLModelElement> getInheritedChildren_TL() {
		return Collections.emptyList();
	}

	/**
	 * Create children array. Get list of TL children and model their associated nodes.
	 * <p>
	 * Only override when node children are not directly created from list of TL children.
	 */
	protected void initChildren() {
		// prevent adding new node to parent from clearing the children
		initRunning = true;
		children = modelTLs(getChildren_TL(), null);
		initRunning = false;
	}

	protected void initInherited() {
		inherited = Collections.emptyList();
		// @Override
		// initRunning = true;
		// // The base must be the PropertyOwner of the actual inherited children, not their copy in the list
		// Get the base object that owns the inherited properties
		// inherited = modelTLs(getInheritedChildren_TL(), inheritedOwner);
		// initRunning = false;
	}

	/**
	 * Associate nodes with a TLModelElement. if the node has not been previously created as defined by a
	 * NodeIdentityListener then create a new node.
	 * 
	 * @param list
	 * @param base
	 *            when not null, the base is the base node from which the children in the list were inherited from.
	 * @return
	 */
	protected List<C> modelTLs(List<TLModelElement> list, Node base) {
		C node = null;
		List<C> kids = new ArrayList<C>();
		for (TLModelElement t : list) {
			// TODO - extract into getOrModel(t)
			node = getOrModel(t);

			if (node == null || !(node instanceof ComponentNode))
				continue;
			kids.add((C) node);
			ComponentNode cnode = (ComponentNode) node;

			if (base != null) {
				// these are inherited children..."ghost" nodes made from "ghost" tl objects.
				Node baseNode = base.findChildByName(node.getName());
				assert baseNode != null; // must find a base node.
				if (node.getParent() != owner)
					node.setParent(owner);
				cnode.setInheritedFrom(base.findChildByName(node.getName()));
				assert node.getInheritedFrom() != null;
				node.getInheritedFrom().getTLModelObject().addListener(new InheritanceDependencyListener(node, this));
			}
			if (t instanceof TLContextualFacet && ((ContextualFacetNode) node).canBeLibraryMember())
				assert ((ContributedFacetNode) node).getContributor() == Node.GetNode(t);
			else
				assert node == Node.GetNode(t);
		}
		return kids;
	}

	@SuppressWarnings("unchecked")
	private C getOrModel(TLModelElement t) {
		C node = null;
		if (t == null)
			return node;

		// if (!(node instanceof ComponentNode))
		// return node;

		ComponentNode cnode = null;
		cnode = (ComponentNode) Node.GetNode(t);
		if (cnode == null) {
			// Create a node to represent this tl element
			cnode = NodeFactory.newChild(owner, t);
		} else {
			// assert !node.isDeleted();
			if (cnode.isDeleted())
				LOGGER.warn("Trying to re-model a deleted node");
			// If it is a contextual facet use the contributed facet
			if (cnode instanceof ContextualFacetNode && ((ContextualFacetNode) cnode).canBeLibraryMember())
				if (((ContextualFacetNode) cnode).getWhereContributed() != null)
					cnode = ((ContextualFacetNode) cnode).getWhereContributed();
				else
					cnode = new ContributedFacetNode((TLContextualFacet) t, (ContextualFacetOwnerInterface) owner);
		}
		node = (C) cnode;
		return node;
	}

	private void clearList(List<C> inherited) {
		if (inherited == null)
			return;
		for (Node n : inherited)
			n.setTlModelObject(null); // Remove tl object
	}

	@Override
	public String toString() {
		return owner.getName() + "_ChildrenHandler";
	}

}
