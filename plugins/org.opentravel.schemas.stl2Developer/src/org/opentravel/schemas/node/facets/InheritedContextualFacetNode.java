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
package org.opentravel.schemas.node.facets;

import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLFacetOwner;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.util.OTM16Upgrade;
import org.opentravel.schemas.node.NavNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.VersionNode;
import org.opentravel.schemas.node.handlers.children.FacetChildrenHandler;
import org.opentravel.schemas.node.interfaces.ComplexComponentInterface;
import org.opentravel.schemas.node.interfaces.ContextualFacetOwnerInterface;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.interfaces.InheritedInterface;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.listeners.ListenerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used for inherited custom, choice, query and update facets.
 * <p>
 * Only used for version 1.6 and later where contextual facets are library members.
 * <p>
 * This node is in a library.
 * <p>
 * This node <i>has</i> a TL model object that is different than the inherited TL model object.
 * 
 * @author Dave Hollander
 * 
 */
public class InheritedContextualFacetNode extends ContextualFacetNode implements InheritedInterface,
		LibraryMemberInterface, ComplexComponentInterface, ContextualFacetOwnerInterface {
	private static final Logger LOGGER = LoggerFactory.getLogger(InheritedContextualFacetNode.class);

	private ContextualFacetNode inheritedFrom = null;

	/**
	 * Create a facade for an inherited contextual facet.
	 * 
	 * @param tlObj
	 *            the TL object unique to this inheritance
	 * @param from
	 *            the contextual facet inherited from
	 * @param library
	 *            the library of the owner
	 */
	public InheritedContextualFacetNode(TLContextualFacet tlFrom, ContextualFacetNode from, LibraryNode library) {
		super();
		tlObj = tlFrom;
		inheritedFrom = from;
		this.parent = library;
		setLibrary(library);
		ListenerFactory.setIdentityListner(this);

		assert tlFrom != from.getTLModelObject(); // must be a TL object unique to this parent object
		assert OTM16Upgrade.otm16Enabled;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.interfaces.FacadeInterface#get()
	 */
	@Override
	public Node get() {
		return getInheritedFrom();
	}

	@Override
	public ContextualFacetNode getInheritedFrom() {
		return inheritedFrom;
	}

	@Override
	public TLContextualFacet getTLModelObject() {
		return (TLContextualFacet) tlObj;
	}

	/**
	 * @return the whereContributed field identifying the contributedNode on the injected object.
	 */
	@Override
	public ContributedFacetNode getWhereContributed() {
		return whereContributed;
	}

	@Override
	public boolean canOwn(TLFacetType type) {
		return false;
	}

	@Override
	protected void removeFromTLParent() {
	}

	@Override
	public boolean canOwn(ContextualFacetNode targetCF) {
		return false;
	}

	@Override
	protected void addToTLParent(TLFacetOwner tlOwner) {
	}

	@Override
	public FacetChildrenHandler getChildrenHandler() {
		return getInheritedFrom().getChildrenHandler();
	}

	@Override
	public ContextualFacetNode copy(LibraryNode destLib) throws IllegalArgumentException {
		return null;
	}

	@Override
	public void close() {
		if (getWhereContributed() != null)
			getWhereContributed().clear();
	}

	@Override
	public void delete() {
		close();
	}

	// @Override
	// public String getDecoration() {
	// String decoration = "  Inherited";
	// decoration += super.getDecoration();
	// return decoration.isEmpty() ? " " : decoration;
	// }

	// @Override
	// public String getExtendsTypeName() {
	// return whereContributed != null && whereContributed.getParent() != null ? whereContributed.getParent()
	// .getName() : "";
	// }

	// @Override
	// public Image getImage() {
	// if (OTM16Upgrade.otm16Enabled) {
	// if (whereContributed == null)
	// return Images.getImageRegistry().get(Images.Facet);
	// // if (!isLocal())
	// return Images.getImageRegistry().get(Images.ContextualFacet);
	// }
	// return Images.getImageRegistry().get(Images.Facet);
	// }

	// @Override
	// public String getLabel() {
	// if (isInherited())
	// return getTLModelObject() != null ? getFacetType().getIdentityName() + " (Inherited)" : "";
	// return getTLModelObject() != null ? getFacetType().getIdentityName() : "";
	// }

	/**
	 * Version 1.6 and later is a library member so return library field.
	 */
	@Override
	public LibraryNode getLibrary() {
		return owningLibrary;
	}

	// /**
	// * Get the full name of this contextual (custom or query) facet. Name is the facet name plus its parent(s) names
	// to
	// * create global type name.
	// */
	// @Override
	// public String getLocalName() {
	// if (getTLModelObject() == null)
	// return "";
	// return getTLModelObject().getLocalName() == null ? "" : getTLModelObject().getLocalName();
	// }

	// @Override
	// @Deprecated
	// public FacetMO getModelObject() {
	// return (FacetMO) modelObject;
	// }

	// /**
	// * Get the name of this contextual (custom or query) facet. Name is simply the facet name and not its global type
	// * name.
	// *
	// * @see getLocalName()
	// */
	// @Override
	// public String getName() {
	// // // see also get LocalName - has parentage in name
	// return getLocalName();
	// // if (getTLModelObject() == null)
	// // return "";
	// // return getTLModelObject().getName() == null ? "" : getTLModelObject().getName();
	// }

	// @Override
	// public String getNavigatorName() {
	// return getLocalName();
	// }

	// /**
	// * @return the owning entity reported by the TL Model Object. If no owning entity then this is returned.
	// */
	// @Override
	// public LibraryMemberInterface getOwningComponent() {
	// // For version 1.6, contextual facets are their own owners
	// if (canBeLibraryMember())
	// return this;
	// // Version 1.5 - parent is the owner. Make sure parent is set.
	// if (parent == null)
	// if (getTLModelObject() != null && getTLModelObject().getOwningEntity() != null)
	// parent = Node.GetNode(getTLModelObject().getOwningEntity());
	// // In junits it will be navNode if created in 1.6 but deleted in version 1.5 mode
	// return parent instanceof LibraryMemberInterface ? (LibraryMemberInterface) parent : null;
	// }

	// @Override
	// public SimpleFacetNode getFacet_Simple() {
	// // TODO Auto-generated method stub
	// return null;
	// }

	// @Override
	// public boolean hasTreeChildren(boolean deep) {
	// return false; // do not allow where used nodes
	// }

	// @Override
	// public boolean isDeleteable() {
	// if (isInherited())
	// return false;
	// if (isNamedEntity()) {
	// // Node's delete logic is based on owning component which will be wrong for contextual facets in version 1.6
	// // and later.
	// if (getLibrary() == null)
	// return false;
	// return getLibrary().isEditable() && isInHead2();
	// }
	// return super.isDeleteable(true) && !isInherited();
	// }

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

	// @Override
	// public boolean isEnabled_AddProperties() {
	// if (isDeleted() || !isEditable())
	// return false; // not editable
	// if (getChain() == null)
	// return true; // editable and not in a chain
	// // If in a chain, it must be the head library
	// return isInHead();
	// }

	// @Override
	// public boolean isInHead() {
	// // Owning component used in isInHead() does not control editing of contextual facets because they may be in
	// // different libraries.
	// if (getChain() == null || getChain().getHead() == null)
	// return false;
	// return getChain().getHead() == getLibrary();
	// }

	// /**
	// * @return true if this facet is declared in the same library as the object it contributes to. Always true for
	// * versions 1.5 and earlier
	// */
	// @Override
	// public boolean isLocal() {
	// if (getTLModelObject() == null)
	// return false;
	// return OTM16Upgrade.otm16Enabled ? getTLModelObject().isLocalFacet() : true;
	// }

	/**
	 * Contextual facets are only named entities if their parent is a NavNode not an object.
	 */
	@Override
	public boolean isNamedEntity() {
		if (getParent() instanceof VersionNode)
			return getParent().getParent() instanceof NavNode;
		return getParent() instanceof NavNode;
	}

	// @Override
	// public boolean isRenameable() {
	// return isEditable() && !isInherited();
	// }
	//
	@Override
	public void print() {
		if (OTM16Upgrade.otm16Enabled) {
			LOGGER.debug("Contextual facet: " + getName());
			LOGGER.debug("   Label: " + getLabel());
			LOGGER.debug("   Is Local? " + getTLModelObject().isLocalFacet());
			LOGGER.debug("   Owner: " + getOwningComponent());
			LOGGER.debug("   Type: " + getTLModelObject().getFacetType());
		} else
			LOGGER.debug("Contextual facet as Version 1.5 facet.");
	}

	// @Override
	// @Deprecated
	// public void setContext() {
	// if (getLibrary() != null)
	// getTLModelObject().setContext(getLibrary().getDefaultContextId());
	// // very common
	// // else
	// // LOGGER.warn("Can't set initial context on " + this);
	// }

	// /**
	// * Set context to library's default context ID.
	// *
	// * @param context
	// * is IGNORED. Context is not used on facets in version 1.6 and later.
	// */
	// @Override
	// @Deprecated
	// public void setContext(String context) {
	// // setContext();
	// }

	/**
	 * Set the name of this contextual (custom or query) facet. Name is simply the facet name and not its global type
	 * name.
	 */
	@Override
	public void setName(String n) {
		// String name = n;
		// // Strip the object name and "query" string if present.
		// name = NodeNameUtils.fixContextualFacetName(this, name);
		// getTLModelObject().setName(name);
		// // rename their type users as well.
		// for (TypeUser user : getWhereAssigned())
		// user.setName(name);
	}

	// /**
	// * Add this facet to the owner's TL facet owner. Then add to library or owner. Create contributed facet if
	// * canBeLibrarymember (v1.6)
	// *
	// * @param owner
	// */
	// @Override
	// public void setOwner(ContextualFacetOwnerInterface owner) {
	// // Done by all but contrib
	// addToTLParent(owner.getTLModelObject());
	// add(owner, getTLModelObject()); // Adds to owning object's library!
	// }
	//
	// @Override
	// public void setLibrary(LibraryNode library) {
	// if (canBeLibraryMember())
	// owningLibrary = library;
	// }

	/**
	 * Simple setter of whereContributed field.
	 * 
	 * @param contributedFacetNode
	 */
	@Override
	public void setWhereContributed(ContributedFacetNode contributedFacetNode) {
		whereContributed = contributedFacetNode;
	}

	// // Drives sort order in navigation menu
	// @Override
	// public String toString() {
	// return getLocalName();
	// }

	/**
	 * Add this contextual facet to the owner.
	 * <p>
	 * if it can be a library member (v1.6 and later) then create contributed facet. Removes existing contributed facet
	 * if needed.
	 * 
	 * @param owner
	 * @param newFacet
	 */
	@Override
	protected void add(ContextualFacetOwnerInterface owner, TLContextualFacet newFacet) {
		// // if already contributed, remove the contributed facet.
		// if (getWhereContributed() != null)
		// getWhereContributed().getParent().getChildrenHandler().clear();
		// // getWhereContributed().unlinkNode();
		//
		// if (canBeLibraryMember()) {
		// if (getLibrary() == null)
		// owner.getLibrary().addMember(this);
		// new ContributedFacetNode(newFacet);
		// } else
		// parent = (Node) owner;
		//
		// ((Node) owner).getChildrenHandler().clear();

		// // Add this node to owner
		// // done by linkChild() - setLibrary(owner.getLibrary());
		// if (OTM16Upgrade.otm16Enabled) {
		// // Adding member takes care of version nodes
		// if (getLibrary() == null)
		// owner.getLibrary().addMember(this);
		// // Create contributed facet and link to owner
		// ContributedFacetNode contrib = new ContributedFacetNode(newFacet);
		// ((Node) owner).getChildrenHandler().clear();
		// // ((Node) owner).linkChild(contrib);
		// } else {
		// ((Node) owner).getChildrenHandler().clear();
		// // Just link it to the owner
		// // ((Node) owner).linkChild(this);
		// }
	}

	// /**
	// * Add this contextual facet to owning entity. Overloaded by each contextual facet type.
	// *
	// */
	// @Override
	// protected abstract void addToTLParent(TLFacetOwner tlOwner);

	/**
	 * @return true if name and namespace are equal to other node
	 */
	@Override
	protected boolean nameEquals(final INode other) {
		if (this == other)
			return true;
		if (other == null)
			return false;

		String thisName = getLocalName();
		String otherName = other.getName();
		if (other instanceof InheritedContextualFacetNode)
			otherName = getLocalName();

		if (thisName == null) {
			if (otherName != null) {
				return false;
			}
		} else if (!thisName.equals(otherName)) {
			return false;
		}
		if (getNamespace() == null) {
			if (other.getNamespace() != null) {
				return false;
			}
		} else if (!getNamespace().equals(other.getNamespace())) {
			return false;
		}
		return true;
	}

	// /**
	// * Remove this contextual facet from owning entity. Overloaded by each contextual facet type.
	// *
	// */
	// @Override
	// protected abstract void removeFromTLParent();
	//
}
