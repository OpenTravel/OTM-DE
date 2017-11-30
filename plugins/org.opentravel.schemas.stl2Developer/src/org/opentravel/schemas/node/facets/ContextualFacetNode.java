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

import java.util.Collections;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemacompiler.model.LibraryElement;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLFacetOwner;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.util.OTM16Upgrade;
import org.opentravel.schemas.node.AliasNode;
import org.opentravel.schemas.node.NavNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFactory;
import org.opentravel.schemas.node.NodeNameUtils;
import org.opentravel.schemas.node.VersionNode;
import org.opentravel.schemas.node.handlers.children.FacetChildrenHandler;
import org.opentravel.schemas.node.interfaces.ComplexComponentInterface;
import org.opentravel.schemas.node.interfaces.ContextualFacetOwnerInterface;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.interfaces.InheritedInterface;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.properties.PropertyOwnerInterface;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.types.TypeUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used for custom, choice, query and update facets.
 * 
 * Contextual facets can be either in the library or in a different library from the object they contextualize.
 * 
 * contextual facets collaborate with their owning object. They add facets without having dependacies between the
 * object's library to the facet's library.
 * 
 * Contextual facets enable OTM to have dependency injection or Inversion of Control.
 * <p>
 * TLBO child is a TLCF <br>
 * BO_Node will have Contrib as child <br>
 * BO_Node will have a contextual facet library member with BO as where_contributed
 * 
 * @author Dave Hollander
 * 
 */
public abstract class ContextualFacetNode extends FacetNode implements LibraryMemberInterface,
		ComplexComponentInterface, ContextualFacetOwnerInterface {
	private static final Logger LOGGER = LoggerFactory.getLogger(ContextualFacetNode.class);

	protected LibraryNode owningLibrary = null;
	protected ContributedFacetNode whereContributed = null;
	protected Node inheritedFrom = null;

	/**
	 * Create a TLContextualFacet. Not added to object or library.
	 * 
	 * @param name
	 * @param type
	 * @return
	 */
	public static TLContextualFacet createTL(String name, TLFacetType type) {
		TLContextualFacet newFacet = new TLContextualFacet();
		newFacet.setName(name);
		newFacet.setFacetType(type);
		return newFacet;
	}

	public ContextualFacetNode() {
		// super();
		// Contributed facets do not have MO or TL objects.
	}

	public ContextualFacetNode(TLContextualFacet tlObj) {
		super(tlObj);
		// setContext();

		// On initial construction,
		// All properties created, parent is this node
		// library listener is not set so all library links are not set
		Node n = GetNode(tlObj.getOwningLibrary());
		if (n instanceof LibraryNode)
			setLibrary((LibraryNode) n);

		if (canBeLibraryMember()) {
			// v1.6+ - If the owner has already been modeled then create and link contributed facet
			Node owner = GetNode(tlObj.getOwningEntity());
			if (owner instanceof ContextualFacetOwnerInterface) {
				ContextualFacetOwnerInterface cOwner = (ContextualFacetOwnerInterface) owner;
				ContributedFacetNode contrib = cOwner.getContributedFacet(tlObj);
				// If this has not already been contributed the make contributed facet.
				if (contrib == null)
					contrib = new ContributedFacetNode(tlObj, cOwner);
				setWhereContributed(contrib);
			}
			// if (getLibrary() == null)
			// LOGGER.debug("HERE");
		}
		if (!isInherited())
			assert Node.GetNode(getTLModelObject()) == this;

		// assert GetNode(tlObj) == this; // Not true when creating inherited facets
		// assert getLibrary() != null; // Not true if the tlObj is newly created
	}

	@Override
	public FacetChildrenHandler getChildrenHandler() {
		return (FacetChildrenHandler) childrenHandler;
	}

	/**
	 * Simple Setter
	 */
	public void setInheritedFrom(Node owner) {
		// assert false;
		inheritedFrom = owner;
	}

	@Override
	public Node getInheritedFrom() {
		// assert false;
		// if (!canBeLibraryMember())
		return inheritedFrom;
		// return null;
	}

	/**
	 * @return true if the contextual facet could be a library member (true for v1.6 and later)
	 */
	public boolean canBeLibraryMember() {
		return OTM16Upgrade.otm16Enabled;
	}

	@Override
	public abstract boolean canOwn(ContextualFacetNode targetCF);

	@Override
	public Node clone(Node parent, String nameSuffix) {
		ContextualFacetNode newNode = (ContextualFacetNode) super.clone(parent, nameSuffix);
		if (newNode == null) {
			LOGGER.debug("Failed to clone " + this);
			return null;
		}
		// Now, add the owner
		if (parent != null && parent.getLibrary() != null)
			parent.getLibrary().addMember(newNode);
		if (getOwningComponent() != null)
			newNode.setOwner((ContextualFacetOwnerInterface) getOwningComponent());
		newNode.getTLModelObject().setOwningLibrary(parent.getLibrary().getTLModelObject());
		return newNode;
	}

	@Override
	public ContextualFacetNode copy(LibraryNode destLib) throws IllegalArgumentException {
		if (destLib == null)
			destLib = getLibrary();

		// Clone the TL object
		LibraryMember tlCopy = cloneTL();

		// Create contextual facet from the copy
		Node copy = (Node) NodeFactory.newLibraryMember(tlCopy);
		if (!(copy instanceof ContextualFacetNode))
			throw new IllegalArgumentException("Unable to copy " + this);
		ContextualFacetNode cf = (ContextualFacetNode) copy;

		// Set where contributed
		ContextualFacetOwnerInterface owner = null;
		ContributedFacetNode contributed = getWhereContributed();
		if (contributed != null && contributed.getOwningComponent() instanceof ContextualFacetOwnerInterface)
			owner = (ContextualFacetOwnerInterface) contributed.getOwningComponent();
		if (owner != null)
			cf.setOwner(owner); // puts in owner's library

		destLib.addMember(cf); // removed from current library then add to destLib

		// // Set the library for all children (bug patch)
		// for (Node child : cf.getDescendants())
		// child.setLibrary(cf.getLibrary());

		// Fix any contexts
		cf.fixContexts();
		return cf;
	}

	public LibraryMember cloneTL() throws IllegalArgumentException {
		if (getLibrary() == null)
			throw new IllegalArgumentException("Can not clone without having a library.");

		// Create the clone, throws exception
		LibraryElement newLE = getTLModelObject().cloneElement(getLibrary().getTLModelObject());

		// Owning library is null
		// OwningEntity is null, but does have owning entity name
		return (LibraryMember) newLE;
	}

	@Override
	public void close() {
		// super.close();
		removeFromTLParent();
		if (whereContributed != null) {
			whereContributed.clear();
		}
	}

	@Override
	public void delete() {
		// Normal delete then the contributed facet
		super.delete();
		if (OTM16Upgrade.otm16Enabled) {
			// May be null when contributed facet is deleted
			if (getTLModelObject() != null && getTLModelObject().getOwningLibrary() != null) {
				getTLModelObject().getOwningLibrary().removeNamedMember(getTLModelObject());
				getTLModelObject().setOwningLibrary(null); // not done automatically
			}
			if (whereContributed != null)
				whereContributed.delete();
		} else {
			removeFromTLParent();
		}

		// assert !oldParent.getChildren().contains(this);
		if (OTM16Upgrade.otm16Enabled) {
			assert getTLModelObject().getOwningLibrary() == null;
			assert getTLModelObject().getOwningEntity() == null;
		}
	}

	@Override
	public List<AliasNode> getAliases() {
		return Collections.emptyList();
	}

	@Override
	public PropertyOwnerInterface getFacet_Attributes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ContributedFacetNode getContributedFacet(TLContextualFacet tlCf) {
		// Get the TL children and return the found facet.
		ContributedFacetNode cfn = null;
		for (TLModelElement tlo : getChildrenHandler().getChildren_TL())
			if (tlo == tlCf)
				if (Node.GetNode(tlo) instanceof ContextualFacetNode) {
					// getNode will return the contextual facet so use its where contributed.
					ContextualFacetNode cxn = (ContextualFacetNode) Node.GetNode(tlo);
					if (cxn != null) {
						cfn = cxn.getWhereContributed();
						break;
					}
				}
		return cfn;
		// for (Node child : getChildren())
		// if (child instanceof ContributedFacetNode && child.getTLModelObject() == tlCf)
		// return (ContributedFacetNode) child;
		// return null;
	}

	@Override
	public String getDecoration() {
		String decoration = "";
		if (OTM16Upgrade.otm16Enabled) {
			// decoration += " :CtxF " + nodeID;
			decoration += "   : " + getLabel();
			if (whereContributed != null && whereContributed.getParent() != null)
				if (whereContributed.getParent().getLibrary() == getLibrary())
					decoration += " contributed to " + whereContributed.getParent().getName();
				else
					decoration += " contributed to " + whereContributed.getParent().getNameWithPrefix();
			decoration += "  (Version: " + getTlVersion();
			if (!isInHead2() || this instanceof InheritedInterface)
				decoration += " Not Editable";
			decoration += ")";
		}
		return decoration.isEmpty() ? " " : decoration;
	}

	@Override
	public String getExtendsTypeName() {
		return whereContributed != null && whereContributed.getParent() != null ? whereContributed.getParent()
				.getName() : "";
	}

	@Override
	public Image getImage() {
		if (OTM16Upgrade.otm16Enabled) {
			if (whereContributed == null)
				return Images.getImageRegistry().get(Images.Facet);
			// if (!isLocal())
			return Images.getImageRegistry().get(Images.ContextualFacet);
		}
		return Images.getImageRegistry().get(Images.Facet);
	}

	@Override
	public String getLabel() {
		if (isInherited())
			return getTLModelObject() != null ? "Inherited " + getFacetType().getIdentityName() : "";
		return getTLModelObject() != null ? getFacetType().getIdentityName() : "";
	}

	/**
	 * Version 1.6 and later is a library member so return library field.
	 * <p>
	 * Version 1.5 and older return owning component's library.
	 */
	@Override
	public LibraryNode getLibrary() {
		if (canBeLibraryMember())
			return owningLibrary;
		return getOwningComponent() != null ? getOwningComponent().getLibrary() : null;
	}

	/**
	 * Get the full name of this contextual (custom or query) facet. Name is the facet name plus its parent(s) names to
	 * create global type name.
	 */
	public String getLocalName() {
		if (getTLModelObject() == null)
			return "";
		return getTLModelObject().getLocalName() == null ? "" : getTLModelObject().getLocalName();
	}

	// @Override
	// @Deprecated
	// public FacetMO getModelObject() {
	// return (FacetMO) modelObject;
	// }

	/**
	 * Get the name of this contextual (custom or query) facet. Name is simply the facet name and not its global type
	 * name.
	 * 
	 * @see getLocalName()
	 */
	@Override
	public String getName() {
		// // see also get LocalName - has parentage in name
		return getLocalName();
		// if (getTLModelObject() == null)
		// return "";
		// return getTLModelObject().getName() == null ? "" : getTLModelObject().getName();
	}

	@Override
	public String getNavigatorName() {
		return getLocalName();
	}

	/**
	 * @return the owning entity reported by the TL Model Object. If no owning entity then this is returned.
	 */
	@Override
	public LibraryMemberInterface getOwningComponent() {
		// For version 1.6, contextual facets are their own owners
		if (canBeLibraryMember())
			return this;
		// Version 1.5 - parent is the owner. Make sure parent is set.
		if (parent == null)
			if (getTLModelObject() != null && getTLModelObject().getOwningEntity() != null)
				parent = Node.GetNode(getTLModelObject().getOwningEntity());
		// In junits it will be navNode if created in 1.6 but deleted in version 1.5 mode
		return parent instanceof LibraryMemberInterface ? (LibraryMemberInterface) parent : null;
	}

	@Override
	public SimpleFacetNode getFacet_Simple() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TLContextualFacet getTLModelObject() {
		return (TLContextualFacet) tlObj;
		// return (TLContextualFacet) modelObject.getTLModelObj();
	}

	/**
	 * @return the whereContributed field identifying the contributedNode on the injected object.
	 */
	public ContributedFacetNode getWhereContributed() {
		return whereContributed;
	}

	// @Override
	// public boolean hasTreeChildren(boolean deep) {
	// return false; // do not allow where used nodes
	// }

	@Override
	public boolean isDeleteable() {
		if (isInherited())
			return false;
		if (isNamedEntity()) {
			// Node's delete logic is based on owning component which will be wrong for contextual facets in version 1.6
			// and later.
			if (getLibrary() == null)
				return false;
			return getLibrary().isEditable() && isInHead2();
		}
		return super.isDeleteable(true) && !isInherited();
	}

	@Override
	public boolean isEditable() {
		if (getLibrary() == null)
			return false;
		if (getChain() == null)
			return getLibrary().isEditable();
		if (isInHead())
			return getLibrary().isEditable();
		return false;
		// return isInHead() || getChain() == null ;
	}

	@Override
	public boolean isEnabled_AddProperties() {
		if (isDeleted() || !isEditable())
			return false; // not editable
		if (getChain() == null)
			return true; // editable and not in a chain
		// If in a chain, it must be the head library
		return isInHead();
	}

	@Override
	public boolean isInHead() {
		// Owning component used in isInHead() does not control editing of contextual facets because they may be in
		// different libraries.
		if (getChain() == null || getChain().getHead() == null)
			return false;
		return getChain().getHead() == getLibrary();
	}

	// /**
	// * @return true if this facet is declared in the same library as the object it contributes to. Always true for
	// * versions 1.5 and earlier
	// */
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

	@Override
	public boolean isRenameable() {
		return isEditable() && !isInherited();
	}

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

	@Override
	@Deprecated
	public void setContext() {
		if (getLibrary() != null)
			getTLModelObject().setContext(getLibrary().getDefaultContextId());
		// very common
		// else
		// LOGGER.warn("Can't set initial context on " + this);
	}

	/**
	 * Set context to library's default context ID.
	 * 
	 * @param context
	 *            is IGNORED. Context is not used on facets in version 1.6 and later.
	 */
	@Override
	@Deprecated
	public void setContext(String context) {
		// setContext();
	}

	/**
	 * Set the name of this contextual (custom or query) facet. Name is simply the facet name and not its global type
	 * name.
	 */
	@Override
	public void setName(String n) {
		String name = n;
		// Strip the object name and "query" string if present.
		name = NodeNameUtils.fixContextualFacetName(this, name);
		getTLModelObject().setName(name);
		// rename their type users as well.
		for (TypeUser user : getWhereAssigned())
			user.setName(name);
	}

	/**
	 * Add this facet to the owner's TL facet owner. Then add to library or owner. Create contributed facet if
	 * canBeLibrarymember (v1.6)
	 * 
	 * @param owner
	 */
	public void setOwner(ContextualFacetOwnerInterface owner) {
		// Done by all but contrib
		addToTLParent(owner.getTLModelObject());
		add(owner, getTLModelObject()); // Adds to owning object's library!
	}

	@Override
	public void setLibrary(LibraryNode library) {
		if (canBeLibraryMember())
			owningLibrary = library;
	}

	/**
	 * Simple setter of whereContributed field.
	 * 
	 * @param contributedFacetNode
	 */
	protected void setWhereContributed(ContributedFacetNode contributedFacetNode) {
		whereContributed = contributedFacetNode;
	}

	// Drives sort order in navigation menu
	@Override
	public String toString() {
		return getLocalName();
	}

	/**
	 * Add this contextual facet to the owner.
	 * <p>
	 * if it can be a library member (v1.6 and later) then create contributed facet. Removes existing contributed facet
	 * if needed.
	 * 
	 * @param owner
	 * @param newFacet
	 */
	protected void add(ContextualFacetOwnerInterface owner, TLContextualFacet newFacet) {
		// if already contributed, remove the contributed facet.
		if (getWhereContributed() != null)
			getWhereContributed().getParent().getChildrenHandler().clear();

		if (canBeLibraryMember()) {
			if (getLibrary() == null)
				owner.getLibrary().addMember(this);
			new ContributedFacetNode(newFacet);
		} else
			parent = (Node) owner;

		if (owner != null && ((LibraryMemberInterface) owner).getChildrenHandler() != null)
			((Node) owner).getChildrenHandler().clear();
	}

	/**
	 * Add this contextual facet to owning entity. Overloaded by each contextual facet type.
	 * 
	 */
	protected abstract void addToTLParent(TLFacetOwner tlOwner);

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
		if (other instanceof ContextualFacetNode)
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

	/**
	 * Remove this contextual facet from owning entity. Overloaded by each contextual facet type.
	 * 
	 */
	protected abstract void removeFromTLParent();

}
