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

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLFacetOwner;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.util.OTM16Upgrade;
import org.opentravel.schemas.modelObject.FacetMO;
import org.opentravel.schemas.node.NavNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeNameUtils;
import org.opentravel.schemas.node.VersionNode;
import org.opentravel.schemas.node.interfaces.ComplexComponentInterface;
import org.opentravel.schemas.node.interfaces.ContextualFacetOwnerInterface;
import org.opentravel.schemas.node.interfaces.INode;
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
 * 
 * @author Dave Hollander
 * 
 */
public abstract class ContextualFacetNode extends FacetNode implements LibraryMemberInterface,
		ComplexComponentInterface, ContextualFacetOwnerInterface {
	private static final Logger LOGGER = LoggerFactory.getLogger(ContextualFacetNode.class);

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

	private ContributedFacetNode whereContributed = null;

	public ContextualFacetNode() {
		// super();
		// Contributed facets do not have MO or TL objects.
	}

	public ContextualFacetNode(TLContextualFacet tlObj) {
		super(tlObj);
		setContext();

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
		}

		// assert GetNode(tlObj) == this; // Not true when creating inherited facets
		// assert getLibrary() != null; // Not true if the tlObj is newly created
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
		Node newNode = super.clone(parent, nameSuffix);
		if (newNode == null) {
			LOGGER.debug("Failed to clone " + this);
			return null;
		}
		// Now, add the owner
		if (parent != null && parent.getLibrary() != null)
			parent.getLibrary().addMember(newNode);
		if (getOwningComponent() != null)
			((ContextualFacetNode) newNode).setOwner((ContextualFacetOwnerInterface) getOwningComponent());
		((TLContextualFacet) newNode.getTLModelObject()).setOwningLibrary(parent.getLibrary().getTLModelObject());
		return newNode;
	}

	@Override
	public void close() {
		// super.close();
		removeFromTLParent();
		if (whereContributed != null)
			whereContributed.unlinkNode();
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
	public PropertyOwnerInterface getAttributeFacet() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ContributedFacetNode getContributedFacet(TLContextualFacet tlCf) {
		for (Node child : getChildren())
			if (child instanceof ContributedFacetNode && child.getTLModelObject() == tlCf)
				return (ContributedFacetNode) child;
		return null;
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
			if (!isInHead())
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
		if (inherited)
			return getTLModelObject() != null ? getFacetType().getIdentityName() + " (Inherited)" : "";
		return getTLModelObject() != null ? getFacetType().getIdentityName() : "";
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

	@Override
	public FacetMO getModelObject() {
		return (FacetMO) modelObject;
	}

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
	public Node getOwningComponent() {
		if (getTLModelObject() == null)
			return null;
		if (getTLModelObject().getOwningEntity() == null)
			return this;
		return Node.GetNode(getTLModelObject().getOwningEntity());
	}

	@Override
	public SimpleFacetNode getFacet_Simple() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TLContextualFacet getTLModelObject() {
		return (TLContextualFacet) modelObject.getTLModelObj();
	}

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

	/**
	 * @return true if this facet is declared in the same library as the object it contributes to. Always true for
	 *         versions 1.5 and earlier
	 */
	@Override
	public boolean isLocal() {
		if (getTLModelObject() == null)
			return false;
		return OTM16Upgrade.otm16Enabled ? getTLModelObject().isLocalFacet() : true;
	}

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
		return isEditable() && !inherited;
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
	public void setContext(String context) {
		setContext();
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
	 * Add this facet to passed contextual facet owner.
	 * 
	 * @param owner
	 */
	public abstract void setOwner(ContextualFacetOwnerInterface owner);

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
	 * Add this contextual facet to the owner. Removes existing contributed facet if needed.
	 * 
	 * @param owner
	 * @param newFacet
	 */
	protected void add(ContextualFacetOwnerInterface owner, TLContextualFacet newFacet) {
		// if already contributed, remove the contributed facet.
		if (getWhereContributed() != null)
			getWhereContributed().unlinkNode();

		// Add this node to owner
		// done by linkChild() - setLibrary(owner.getLibrary());
		if (OTM16Upgrade.otm16Enabled) {
			// Adding member takes care of version nodes
			if (getLibrary() == null)
				owner.getLibrary().addMember(this);
			// Create contributed facet and link to owner
			ContributedFacetNode contrib = new ContributedFacetNode(newFacet);
			((Node) owner).linkChild(contrib);
		} else {
			// Just link it to the owner
			((Node) owner).linkChild(this);
		}
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
