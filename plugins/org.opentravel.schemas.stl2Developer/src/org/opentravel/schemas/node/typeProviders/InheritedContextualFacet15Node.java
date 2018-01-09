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
package org.opentravel.schemas.node.typeProviders;

import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.util.OTM16Upgrade;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.interfaces.ContextualFacetOwnerInterface;
import org.opentravel.schemas.node.interfaces.InheritedInterface;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.listeners.InheritanceDependencyListener;
import org.opentravel.schemas.node.listeners.ListenerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used for inherited custom, choice, query and update facets.
 * 
 * These classes are for version 1.5 where the contextual facets are members of the containing object.
 * 
 * 
 * @author Dave Hollander
 * 
 */
public class InheritedContextualFacet15Node extends ContextualFacet15Node implements InheritedInterface {
	private static final Logger LOGGER = LoggerFactory.getLogger(InheritedContextualFacet15Node.class);

	protected ContextualFacet15Node inheritedFrom = null;

	public InheritedContextualFacet15Node(TLContextualFacet tlGhost, ContextualFacet15Node from, Node parent) {
		super();
		tlObj = tlGhost;
		inheritedFrom = from;
		this.parent = parent;
		// setLibrary(parent.getLibrary());

		// Set two listeners - one on this and one on the base
		ListenerFactory.setIdentityListner(this);
		new InheritanceDependencyListener(this);

		assert tlGhost != from.getTLModelObject(); // must be a TL object unique to this parent object
		assert tlGhost != null;
		assert !OTM16Upgrade.otm16Enabled;
	}

	@Override
	public ContextualFacet15Node getInheritedFrom() {
		return inheritedFrom;
	}

	@Override
	public boolean canOwn(TLFacetType type) {
		return false;
	}

	@Override
	protected void addToTLParent(ContextualFacetOwnerInterface owner) {
		// NO-OP
	}

	// @Override
	// public TLModelElement getTLModelOject() {
	// return getInheritedFrom().getTLModelObject();
	// }

	@Override
	protected void removeFromTLParent() {
	}

	@Override
	public boolean canOwn(AbstractContextualFacet targetCF) {
		return false;
	}

	@Override
	public ContextualFacetNode copy(LibraryNode destLib) throws IllegalArgumentException {
		return null;
	}

	@Override
	public void delete() {
		close();
	}

	@Override
	public void print() {
		super.print();
		LOGGER.debug("Inherited from: " + inheritedFrom);
	}

	/**
	 * Set the name of this contextual (custom or query) facet. Name is simply the facet name and not its global type
	 * name.
	 */
	@Override
	public void setName(String n) {
	}

	@Override
	public void add(ContextualFacetOwnerInterface owner) {
	}

	// public InheritedContextualFacet15Node() {
	// }
	//
	// public InheritedContextualFacet15Node(TLContextualFacet tlObj) {
	// super(tlObj);
	// assert !canBeLibraryMember();
	//
	// // library listener is not set so all library links are not set
	// Node ln = GetNode(tlObj.getOwningLibrary());
	// if (ln instanceof LibraryNode)
	// setLibrary((LibraryNode) ln);
	//
	// if (!isInherited())
	// assert Node.GetNode(getTLModelObject()) == this;
	//
	// // assert GetNode(tlObj) == this; // Not true when creating inherited facets
	// // assert getLibrary() != null; // Not true if the tlObj is newly created
	// }
	//
	// /**
	// * Add this contextual facet to the owner.
	// * <p>
	// * if it can be a library member (v1.6 and later) then create contributed facet. Removes existing contributed
	// facet
	// * if needed.
	// *
	// * @param owner
	// * @param newFacet
	// */
	// @Override
	// protected void add(ContextualFacetOwnerInterface owner, TLContextualFacet newFacet) {
	//
	// // FIXME
	// parent = (Node) owner;
	//
	// if (owner != null && ((LibraryMemberInterface) owner).getChildrenHandler() != null)
	// ((Node) owner).getChildrenHandler().clear();
	// }
	//
	// /**
	// * Add this contextual facet to owning entity. Overloaded by each contextual facet type.
	// *
	// */
	// @Override
	// protected abstract void addToTLParent(TLFacetOwner tlOwner);
	//
	// // @Override
	// @Override
	// public abstract boolean canOwn(AbstractContextualFacet targetCF);
	//
	// @Override
	// public Node clone(Node parent, String nameSuffix) {
	// assert false;
	// return null;
	// // ContextualFacetOMNode newNode = (ContextualFacetOMNode) super.clone(parent, nameSuffix);
	// // if (newNode == null) {
	// // LOGGER.debug("Failed to clone " + this);
	// // return null;
	// // }
	// // // Now, add the owner
	// // if (parent != null && parent.getLibrary() != null)
	// // parent.getLibrary().addMember(newNode);
	// // if (getOwningComponent() != null)
	// // newNode.setOwner((ContextualFacetOwnerInterface) getOwningComponent());
	// // newNode.getTLModelObject().setOwningLibrary(parent.getLibrary().getTLModelObject());
	// // return newNode;
	// // }
	//
	// // @Override
	// // public ContextualFacetOMNode copy(LibraryNode destLib) throws IllegalArgumentException {
	// // if (destLib == null)
	// // destLib = getLibrary();
	// //
	// // // Clone the TL object
	// // LibraryMember tlCopy = cloneTL();
	// //
	// // // Create contextual facet from the copy
	// // Node copy = (Node) NodeFactory.newLibraryMember(tlCopy);
	// // if (!(copy instanceof ContextualFacetOMNode))
	// // throw new IllegalArgumentException("Unable to copy " + this);
	// // ContextualFacetOMNode cf = (ContextualFacetOMNode) copy;
	// //
	// // // Set where contributed
	// // ContextualFacetOwnerInterface owner = null;
	// // ContributedFacetNode contributed = getWhereContributed();
	// // if (contributed != null && contributed.getOwningComponent() instanceof ContextualFacetOwnerInterface)
	// // owner = (ContextualFacetOwnerInterface) contributed.getOwningComponent();
	// // if (owner != null)
	// // cf.setOwner(owner); // puts in owner's library
	// //
	// // destLib.addMember(cf); // removed from current library then add to destLib
	// //
	// // // // Set the library for all children (bug patch)
	// // // for (Node child : cf.getDescendants())
	// // // child.setLibrary(cf.getLibrary());
	// //
	// // // Fix any contexts
	// // cf.fixContexts();
	// // return cf;
	// // }
	//
	// // public LibraryMember cloneTL() throws IllegalArgumentException {
	// // if (getLibrary() == null)
	// // throw new IllegalArgumentException("Can not clone without having a library.");
	// //
	// // // Create the clone, throws exception
	// // LibraryElement newLE = getTLModelObject().cloneElement(getLibrary().getTLModelObject());
	// //
	// // // Owning library is null
	// // // OwningEntity is null, but does have owning entity name
	// // return (LibraryMember) newLE;
	// }
	//
	// @Override
	// public LibraryMember cloneTL() throws IllegalArgumentException {
	// assert false;
	// return null;
	// }
	//
	// @Override
	// public PropertyNode createProperty(Node type) {
	// // TODO
	// return null;
	// }
	//
	// @Override
	// public ContributedFacetNode getContributedFacet(TLContextualFacet tlCf) {
	// return null;
	// }
	//
	// @Override
	// public Node getInheritedFrom() {
	// return inheritedFrom;
	// }
	//
	// /**
	// * Version 1.5 and older return owning component's library.
	// */
	// @Override
	// public LibraryNode getLibrary() {
	// return getOwningComponent() != null ? getOwningComponent().getLibrary() : null;
	// }
	//
	// @Override
	// public String getName() {
	// return inheritedFrom != null ? inheritedFrom.getName() : super.getName();
	// }
	//
	// /**
	// * @return the owning entity reported by the TL Model Object. If no owning entity then this is returned.
	// */
	// @Override
	// public LibraryMemberInterface getOwningComponent() {
	// // Version 1.5 - parent is the owner. Make sure parent is set.
	// if (parent == null)
	// if (getTLModelObject() != null && getTLModelObject().getOwningEntity() != null)
	// parent = Node.GetNode(getTLModelObject().getOwningEntity());
	// // In junits it will be navNode if created in 1.6 but deleted in version 1.5 mode
	// return parent instanceof LibraryMemberInterface ? (LibraryMemberInterface) parent : null;
	// }
	//
	// @Override
	// public boolean isDeleteable() {
	// if (isInherited())
	// return false;
	// return getOwningComponent().isEditable();
	// // if (isNamedEntity()) {
	// // // Node's delete logic is based on owning component which will be wrong for contextual facets in version 1.6
	// // // and later.
	// // if (getLibrary() == null)
	// // return false;
	// // return getLibrary().isEditable() && isInHead2();
	// // }
	// // return super.isDeleteable(true) && !isInherited();
	// }
	//
	// @Override
	// public boolean isEditable() {
	// if (getLibrary() == null)
	// return false;
	// if (getChain() == null)
	// return getLibrary().isEditable();
	// if (isInHead())
	// return getLibrary().isEditable();
	// return false;
	// // return isInHead() || getChain() == null ;
	// }
	//
	// @Override
	// public boolean isEnabled_AddProperties() {
	// if (isDeleted() || !isEditable())
	// return false; // not editable
	// if (getChain() == null)
	// return true; // editable and not in a chain
	// // If in a chain, it must be the head library
	// return isInHead();
	// }
	//
	// /**
	// * @return true if name and namespace are equal to other node
	// */
	// @Override
	// protected boolean nameEquals(final INode other) {
	// if (this == other)
	// return true;
	// if (other == null)
	// return false;
	//
	// String thisName = getLocalName();
	// String otherName = other.getName();
	// if (other instanceof InheritedContextualFacet15Node)
	// otherName = getLocalName();
	//
	// if (thisName == null) {
	// if (otherName != null) {
	// return false;
	// }
	// } else if (!thisName.equals(otherName)) {
	// return false;
	// }
	// if (getNamespace() == null) {
	// if (other.getNamespace() != null) {
	// return false;
	// }
	// } else if (!getNamespace().equals(other.getNamespace())) {
	// return false;
	// }
	// return true;
	// }
	//
	// @Override
	// public void print() {
	// LOGGER.debug("Contextual facet as Version 1.5 facet.");
	// }
	//
	// /**
	// * Remove this contextual facet from owning entity. Overloaded by each contextual facet type.
	// *
	// */
	// @Override
	// protected abstract void removeFromTLParent();
	//
	// /**
	// * Simple Setter
	// */
	// @Override
	// public void setInheritedFrom(Node owner) {
	// inheritedFrom = owner;
	// new InheritanceDependencyListener(this);
	// }
	//
	// /**
	// * Set the name of this contextual (custom or query) facet. Name is simply the facet name and not its global type
	// * name.
	// */
	// @Override
	// public void setName(String n) {
	// if (inheritedFrom != null)
	// return;
	// String name = n;
	// // Strip the object name and "query" string if present.
	// name = NodeNameUtils.fixContextualFacetName(this, name);
	// getTLModelObject().setName(name);
	// // rename their type users as well.
	// for (TypeUser user : getWhereAssigned())
	// user.setName(name);
	// }
	//
}
