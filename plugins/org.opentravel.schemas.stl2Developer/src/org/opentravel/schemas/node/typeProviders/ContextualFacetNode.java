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

import java.util.Collections;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemacompiler.event.ModelElementListener;
import org.opentravel.schemacompiler.model.LibraryElement;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFactory;
import org.opentravel.schemas.node.NodeNameUtils;
import org.opentravel.schemas.node.handlers.children.NavNodeChildrenHandler;
import org.opentravel.schemas.node.interfaces.ComplexMemberInterface;
import org.opentravel.schemas.node.interfaces.ContextualFacetOwnerInterface;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.listeners.InheritanceDependencyListener;
import org.opentravel.schemas.node.objectMembers.ContributedFacetNode;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.trees.type.ContextualFacetOwnersTypeFilter;
import org.opentravel.schemas.trees.type.TypeSelectionFilter;
import org.opentravel.schemas.types.TypeUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used for custom, choice, query and update facets in version 1.6 and later. Contextual facets are <i>contributed</i>
 * to an owner. The facet is a library member and can be in any library. The related {@link ContributedFacetNode} is
 * always a child of the owner.
 * <p>
 * Contextual facets enable OTM to have dependency injection (Inversion of Control). Contextual facets collaborate with
 * their owning object. They add facets without having dependencies between the object's library to the facet's library.
 * <p>
 * The TL Model structure is different than the node structure. The TL Model structure is:<br>
 * TL Owner Object has child which is a TLCF <br>
 * Owner Node (v16) will have Contributed facet as child <br>
 * Contextual Facet Node <?> will be a library member with TL object as tlObj.<br>
 * Contributed and Contextual facets will link to each other.<br>
 * <p>
 * When facets are inherited they use the inherited sub-type of contextual and contributed facets.
 * 
 * @author Dave Hollander
 * 
 */
public abstract class ContextualFacetNode extends AbstractContextualFacet implements ComplexMemberInterface {
	private static final Logger LOGGER = LoggerFactory.getLogger(ContextualFacetNode.class);

	protected LibraryNode owningLibrary = null;
	protected ContributedFacetNode whereContributed = null;

	// Needed for inherited contextual facets
	public ContextualFacetNode() {
	}

	/**
	 * Create contextual facet and related contributed facet for the passed TL Contextual facet.
	 * 
	 * @param tlObj
	 */
	public ContextualFacetNode(TLContextualFacet tlObj) {
		super(tlObj);

		assert canBeLibraryMember();

		Node ln = GetNode(tlObj.getOwningLibrary());
		if (ln instanceof LibraryNode)
			setLibrary((LibraryNode) ln);

		// If the owner has already been modeled then create and link contributed facet
		Node owner = GetNode(tlObj.getOwningEntity());
		if (owner instanceof ContextualFacetOwnerInterface) {
			ContextualFacetOwnerInterface cOwner = (ContextualFacetOwnerInterface) owner;
			ContributedFacetNode contrib = cOwner.getContributedFacet(tlObj);
			// If this has not already been contributed the make contributed facet.
			if (contrib == null)
				contrib = new ContributedFacetNode(tlObj, cOwner);
			setWhereContributed(contrib);
		}

		// // FIXME - should have owning entity but does not when entity is another contextual facet
		// if (tlObj.getOwningEntity() == null)
		// LOGGER.debug("Error - tlContextualFacet without owning entity: ", tlObj.getOwningEntityName());

	}

	/**
	 * Add this contextual facet to the owner.
	 * <p>
	 * Create contributed facet. Remove existing contributed facet if it existed.
	 * 
	 * @param owner
	 * @param newFacet
	 */
	@Override
	public void add(ContextualFacetOwnerInterface owner) {
		if (owner == null)
			return;
		// Remove from any existing TL owner
		removeFromTLParent();

		// Add to owner
		addToTLParent(owner);

		// if already contributed, remove the contributed facet.
		if (getWhereContributed() != null)
			getWhereContributed().clearContributor();
		// getWhereContributed().getParent().getChildrenHandler().clear();

		// Make sure it is in a library
		if (getLibrary() == null)
			owner.getLibrary().addMember(this);

		// Create the contributed facet
		ContributedFacetNode contrib = new ContributedFacetNode(this.getTLModelObject(), owner);
		//
		// Make sure the owner refreshes its children
		if (owner != null && ((LibraryMemberInterface) owner).getChildrenHandler() != null)
			((Node) owner).getChildrenHandler().clear();

		assert contrib != null;
		assert contrib.getContributor() == this;
		assert owner.getContributedFacets().contains(contrib);
		assert owner.getContextualFacets(false).contains(this);
	}

	/**
	 * Add this contextual facet to owning entity. Overloaded by each contextual facet type.
	 * 
	 */
	@Override
	protected abstract void addToTLParent(ContextualFacetOwnerInterface owner);

	@Override
	public abstract boolean canOwn(AbstractContextualFacet targetCF);

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
	public LibraryMemberInterface clone(LibraryNode targetLib, String nameSuffix) {
		if (getLibrary() == null || !getLibrary().isEditable()) {
			// LOGGER.warn("Could not clone node because library " + getLibrary() + " it is not editable.");
			return null;
		}

		LibraryMemberInterface clone = null;

		// Use the compiler to create a new TL src object.
		TLModelElement newLM = (TLModelElement) cloneTLObj();
		if (newLM != null) {
			clone = NodeFactory.newLibraryMember((LibraryMember) newLM);
			if (nameSuffix != null)
				clone.setName(clone.getName() + nameSuffix);
			targetLib.addMember(clone);
		}
		return clone;
	}

	@Override
	public void close() {
		super.close(); // Removes from owning TL
		if (whereContributed != null) {
			whereContributed.clear();
			if (getWhereContributed().getParent() != null)
				getWhereContributed().getParent().getChildrenHandler().clear();
			whereContributed = null;
		}
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

		destLib.addMember(cf); // removed from current library then add to destLib

		// Fix any contexts
		cf.fixContexts();
		return cf;
	}

	@Override
	public void delete() {

		for (Node child : getChildrenHandler().get())
			child.delete();

		deleted = true;

		// Do BEFORE changing the tl model so contributor can find owner
		if (whereContributed != null && !whereContributed.isDeleted())
			whereContributed.delete();

		if (getParent() != null && getParent().getChildrenHandler() instanceof NavNodeChildrenHandler)
			((NavNodeChildrenHandler) getParent().getChildrenHandler()).remove(this);

		// May be null when contributed facet is deleted
		if (getTLModelObject() != null) {
			removeFromTLParent();
			if (getTLModelObject().getOwningLibrary() != null) {
				getTLModelObject().getOwningLibrary().removeNamedMember(getTLModelObject());
				getTLModelObject().setOwningLibrary(null); // not done automatically
			}
		}
		parent = null;
		owningLibrary = null;

		assert getTLModelObject().getOwningLibrary() == null;
		assert getTLModelObject().getOwningEntity() == null;
	}

	@Override
	public List<AliasNode> getAliases() {
		return Collections.emptyList();
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
	}

	@Override
	public String getDecoration() {
		String decoration = super.getDecoration();

		if (whereContributed != null && whereContributed.getParent() != null)
			// if (whereContributed.getParent().getLibrary() == getLibrary())
			// decoration += " contributed to " + whereContributed.getParent().getName();
			// else
			decoration += " contributed to " + whereContributed.getParent().getNameWithPrefix();
		return decoration.isEmpty() ? " " : decoration;
	}

	@Override
	public String getExtendsTypeName() {
		return whereContributed != null && whereContributed.getParent() != null ? whereContributed.getParent().getName()
				: "";
	}

	@Override
	public Image getImage() {
		// if (whereContributed == null)
		// return Images.getImageRegistry().get(Images.Facet);
		return Images.getImageRegistry().get(Images.ContextualFacet);
	}

	/**
	 * Version 1.6 and later is a library member so return library field.
	 */
	@Override
	public LibraryNode getLibrary() {
		return owningLibrary;
	}

	/**
	 * For version 1.6, contextual facets are their own owners
	 * 
	 * @return the owning entity reported by the TL Model Object. If no owning entity then this is returned.
	 */
	@Override
	public LibraryMemberInterface getOwningComponent() {
		return this;
	}

	@Override
	public TypeSelectionFilter getTypeSelectionFilter() {
		return new ContextualFacetOwnersTypeFilter(this);
	}

	/**
	 * @return the whereContributed field identifying the contributedNode on the injected object.
	 */
	@Override
	public ContributedFacetNode getWhereContributed() {
		return whereContributed;
	}

	@Override
	public boolean isDeleteable() {
		// FIXME
		if (isInherited())
			return false;
		// Node's delete logic is based on owning component which will be wrong for contextual facets in version 1.6
		// and later.
		if (getLibrary() == null)
			return false;
		return getLibrary().isEditable() && isInHead2();
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

	/**
	 * Contextual facets are only named entities if their parent is a NavNode not an object.
	 */
	// Caller should use class instance test instead of this method
	@Override
	@Deprecated
	public boolean isNamedEntity() {
		return true;
	}

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

	@Override
	public void print() {
		LOGGER.debug("Contextual facet: " + getName());
		LOGGER.debug("   Label: " + getLabel());
		LOGGER.debug("   Is Local? " + getTLModelObject().isLocalFacet());
		LOGGER.debug("   Owner: " + getOwningComponent());
		LOGGER.debug("   Type: " + getTLModelObject().getFacetType());
	}

	/**
	 * Remove this contextual facet from owning entity. Overloaded by each contextual facet type.
	 * 
	 */
	@Override
	protected abstract void removeFromTLParent();

	@Override
	public void setDeleted(boolean value) {
		super.setDeleted(value);
		if (getWhereContributed() != null && !getWhereContributed().isDeleted())
			getWhereContributed().setDeleted(value);
	}

	@Override
	public void setLibrary(LibraryNode library) {
		owningLibrary = library;
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
	 * Simple setter of whereContributed field.
	 * 
	 * @param contributedFacetNode
	 */
	public void setWhereContributed(ContributedFacetNode contributedFacetNode) {
		whereContributed = contributedFacetNode;
	}

	/**
	 * Check the listeners on the TL object and return true if one points (getNode()) to parameter.
	 * 
	 * @param icf
	 * @return
	 */
	public boolean hasInheritanceDependacyListenerTo(InheritedContextualFacetNode icf) {
		for (ModelElementListener l : getTLModelObject().getListeners())
			if (l instanceof InheritanceDependencyListener) {
				if (((InheritanceDependencyListener) l).getNode() == icf)
					return true;
			}
		return false;
	}

}
