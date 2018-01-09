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
package org.opentravel.schemas.node.typeProviders.facetOwners;

import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLAbstractFacet;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.util.OTM16Upgrade;
import org.opentravel.schemas.node.NavNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFactory;
import org.opentravel.schemas.node.facets.AttributeFacetNode;
import org.opentravel.schemas.node.interfaces.ContextualFacetOwnerInterface;
import org.opentravel.schemas.node.interfaces.FacetInterface;
import org.opentravel.schemas.node.interfaces.FacetOwner;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.objectMembers.FacetOMNode;
import org.opentravel.schemas.node.typeProviders.AbstractContextualFacet;
import org.opentravel.schemas.node.typeProviders.AliasNode;
import org.opentravel.schemas.node.typeProviders.ContextualFacetNode;
import org.opentravel.schemas.node.typeProviders.FacetProviderNode;
import org.opentravel.schemas.node.typeProviders.TypeProviders;
import org.opentravel.schemas.types.WhereAssignedHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for <b>all</b> type providers whose children include facets.
 * <p>
 * Role is to manage assignments including where used.
 * 
 * @author Dave Hollander
 * 
 */
public abstract class FacetOwners extends TypeProviders implements FacetOwner {
	private static final Logger LOGGER = LoggerFactory.getLogger(FacetOwners.class);

	// private WhereAssignedHandler whereAssignedHandler = null;
	// private XsdObjectHandler xsdObjectHandler;
	//
	public FacetOwners() {
		whereAssignedHandler = new WhereAssignedHandler(this);
	}

	public FacetOwners(final TLModelElement obj) {
		super(obj);
		whereAssignedHandler = new WhereAssignedHandler(this);
		if (!isInherited())
			assert Node.GetNode(getTLModelObject()) == this;
	}

	/**
	 * Add a facet to this owner. Only contextual facets can be added and only in unmanaged or head versions.
	 * 
	 * @param name
	 * @param type
	 *            a contextual facet type that can be owned by this facet owner
	 * @return the new contextual facet (not contributed)
	 */
	// TODO - consider allowing them in minor and use createMinorVersionOfComponent()
	public AbstractContextualFacet addFacet(String name, TLFacetType type) {
		if (!isEditable_newToChain()) {
			isEditable_newToChain();
			throw new IllegalArgumentException("Not editable - Can not add facet to " + this);
		}
		if (!type.isContextual() || !canOwn(type))
			return null;
		if (!(this instanceof ContextualFacetOwnerInterface))
			return null;

		TLContextualFacet tlCf = ContextualFacetNode.createTL(name, type);
		AbstractContextualFacet cf = NodeFactory.createContextualFacet(tlCf);
		cf.add((ContextualFacetOwnerInterface) this);

		if (OTM16Upgrade.otm16Enabled) {
			assert cf.getParent() instanceof NavNode;
			assert getChildren().contains(((ContextualFacetNode) cf).getWhereContributed());
			assert getLibrary().contains(cf);
		} else {
			assert cf.getParent() == this;
			assert getChildren().contains(cf);
		}
		return cf;
	}

	public abstract boolean canOwn(TLFacetType type);

	@Override
	public LibraryMemberInterface clone(LibraryNode targetLib, String nameSuffix) {
		if (getLibrary() == null || !getLibrary().isEditable()) {
			LOGGER.warn("Could not clone node because library " + getLibrary() + " it is not editable.");
			return null;
		}

		LibraryMemberInterface clone = null;

		// Use the compiler to create a new TL src object.
		TLModelElement newLM = (TLModelElement) cloneTLObj();
		if (newLM != null) {
			clone = NodeFactory.newLibraryMember((LibraryMember) newLM);
			assert clone != null;
			if (nameSuffix != null)
				clone.setName(clone.getName() + nameSuffix);
			for (AliasNode alias : clone.getAliases())
				alias.setName(alias.getName() + nameSuffix);
			targetLib.addMember(clone);
		}
		return clone;
	}

	@Override
	public FacetInterface getFacet(final TLFacetType facetType) {
		for (final Node n : getChildren()) {
			if (n instanceof FacetInterface) {
				TLFacetType type = ((FacetInterface) n).getFacetType();
				if (type != null && type.equals(facetType))
					return (FacetInterface) n;

				// final FacetInterface facet = (FacetInterface) n;
				// final TLFacetType ft = ((ComponentNode) facet).getFacetType();
				// if (ft != null && ft.equals(facetType)) {
				// return facet;
				// }
			}
		}
		return null;
	}

	@Override
	public TLFacetType getFacetType() {
		return getTLModelObject() instanceof TLAbstractFacet ? ((TLAbstractFacet) getTLModelObject()).getFacetType()
				: null;
	}

	@Override
	public abstract FacetInterface getFacet_Default();

	@Override
	public AttributeFacetNode getFacet_Attributes() {
		return null;
	}

	@Override
	public FacetProviderNode getFacet_Detail() {
		return (FacetProviderNode) getFacet(TLFacetType.DETAIL);
	}

	@Override
	public FacetProviderNode getFacet_ID() {
		return (FacetProviderNode) getFacet(TLFacetType.ID);
	}

	@Override
	public FacetInterface getFacet_Simple() {
		return getFacet(TLFacetType.SIMPLE);
	}

	@Override
	public FacetProviderNode getFacet_Summary() {
		return (FacetProviderNode) getFacet(TLFacetType.SUMMARY);
	}

	public FacetOMNode getSharedFacet() {
		return (FacetOMNode) getFacet(TLFacetType.SHARED);

		// for (Node n : getChildren()) {
		// if (n instanceof FacetMemberNode)
		// if (((FacetMemberNode) n).getFacetType().equals(TLFacetType.SHARED))
		// return (FacetMemberNode) n;
		// }
		// return null;
	}

	// ***********************************************************************
	//

	//
	// @Override
	// public BaseNodeListener getNewListener() {
	// return new TypeProviderListener(this);
	// }
	//
	// @Override
	// public void addTypeUser(TypeUser user) {
	// whereAssignedHandler.add(user);
	// whereAssignedHandler.setListener(user);
	// // FIXME - should also do library whereused
	// }
	//
	// /**
	// * {@link #addTypeUser(TypeUser)}
	// */
	// @Override
	// public void addWhereUsed(TypeUser user) {
	// whereAssignedHandler.add(user);
	// }
	//
	// @Override
	// public Collection<TypeUser> getWhereAssigned() {
	// return whereAssignedHandler.getWhereAssigned();
	// }
	//
	// /**
	// * @return where used count for all versions in this versioned object chain
	// */
	// @Override
	// public int getWhereUsedCount() {
	// return getWhereUsedNode() != null ? getWhereUsedNode().getWhereUsedCount() : 0;
	// }
	//
	// /**
	// * @return (where used count) the number of type users which are nodes that use this as a type definition or base
	// * type
	// */
	// @Override
	// public int getWhereAssignedCount() {
	// return getWhereAssignedHandler() != null ? whereAssignedHandler.getWhereAssignedCount() : 0;
	// }
	//
	// @Override
	// public WhereAssignedHandler getWhereAssignedHandler() {
	// return whereAssignedHandler;
	// }
	//
	// @Override
	// public Collection<TypeUser> getWhereUsedAndDescendants() {
	// return whereAssignedHandler.getWhereAssignedIncludingDescendants();
	// }
	//
	// /**
	// * @return count of where this provider and descendants is used as type or extension
	// */
	// @Override
	// public int getWhereUsedAndDescendantsCount() {
	// int count = 0;
	// if (whereExtendedHandler != null)
	// count += whereExtendedHandler.getWhereExtendedCount();
	// if (whereAssignedHandler != null)
	// count += whereAssignedHandler.getWhereAssignedIncludingDescendantsCount();
	// return count;
	// }
	//
	// @Override
	// public TypeProviderWhereUsedNode getWhereUsedNode() {
	// return (TypeProviderWhereUsedNode) whereAssignedHandler.getWhereUsedNode();
	// }
	//
	// // TODO - as this question either to the provider or user but not both
	// /**
	// * @return true if this node can be assigned to an element reference
	// */
	// @Override
	// public boolean isAssignableToElementRef() {
	// return false;
	// }
	//
	// /**
	// * @return true if this node can be assigned to an attribute or simple property
	// */
	// @Override
	// public boolean isAssignableToSimple() {
	// return false;
	// }
	//
	// /**
	// * @return true if this node can be assigned to an attribute, simple property or VWA attribute
	// */
	// @Override
	// public boolean isAssignableToVWA() {
	// return false;
	// }
	//
	// @Override
	// public boolean isRenameableWhereUsed() {
	// return false;
	// }
	//
	// @Override
	// public void removeAll() {
	// Collection<TypeUser> users = new ArrayList<TypeUser>(getWhereAssigned());
	// for (TypeUser user : users)
	// removeTypeUser(user);
	// }
	//
	// /**
	// * {@link #removeTypeUser(TypeUser)}
	// */
	// @Override
	// public void removeListener(TypeUser user) {
	// whereAssignedHandler.removeListener(user);
	// }
	//
	// @Override
	// public void removeTypeUser(TypeUser user) {
	// whereAssignedHandler.removeListener(user);
	// whereAssignedHandler.removeUser(user);
	// }
	//
	// /**
	// * {@link #addTypeUser(TypeUser)}
	// */
	// @Override
	// public void setListener(TypeUser user) {
	// whereAssignedHandler.setListener(user);
	// }
	//
	// /**
	// * Set name to type users where this alias is assigned. Only if the parent is type that requires assigned users to
	// * use the owner's name
	// */
	// public void setNameOnWhereAssigned(String n) {
	// if (!isRenameableWhereUsed())
	// for (TypeUser u : getWhereAssigned())
	// u.setName(n);
	// }
	//
	// /**
	// * Replace all type assignments (base and assigned type) to this node with assignments to passed node. For every
	// * assignable descendant of sourceNode, find where the corresponding sourceNode children are used and change them
	// as
	// * well. See {@link #replaceWith(Node)}.
	// *
	// * @param this - replace assignments to this node (sourceNode)
	// * @param replacement
	// * - use replacement TypeProvider node to be assigned
	// * @param scope
	// * (optional) - scope of the search or null for all libraries
	// */
	// @Override
	// public void replaceTypesWith(Node replacement, LibraryNode scope) {
	// if (!(replacement instanceof TypeProvider))
	// return;
	//
	// getWhereAssignedHandler().replaceAll((TypeProvider) replacement, scope);
	//
	// // If this has been extended, replace where extended
	// getWhereExtendedHandler().replace(replacement, scope);
	// }
	//
	// // ******************** Hierarchy methods **************************************************
	//
	// // TODO - either use children handler or move into children handler
	// @Override
	// public List<Node> getTreeChildren(boolean deep) {
	// List<Node> navChildren = getNavChildren(deep);
	// navChildren.addAll(getInheritedChildren());
	// if (getWhereUsedCount() > 0)
	// navChildren.add(getWhereUsedNode());
	// return navChildren;
	// }
	//
	// @Override
	// public boolean hasTreeChildren(boolean deep) {
	// if (getNavChildren(deep).isEmpty() && getInheritedChildren().isEmpty() && getWhereUsedCount() < 1)
	// return false; // allow where used nodes
	// return true;
	// }
	//
	// // @Override
	// // public LibraryElement cloneTLObj() {
	// // LibraryElement clone = super.cloneTLObj();
	// // // TODO - why is choice done here? Why not BO and CFs also?
	// // if (clone instanceof TLChoiceObject) {
	// // List<TLContextualFacet> tlCFs = ((TLChoiceObject) clone).getChoiceFacets();
	// // LibraryMemberInterface n;
	// // for (TLContextualFacet tlcf : tlCFs) {
	// // n = NodeFactory.newLibraryMember(tlcf);
	// // getLibrary().addMember(n);
	// // }
	// // }
	// // return clone;
	// // }
	//
	// @Override
	// public void setXsdHandler(XsdObjectHandler xsdObjectHandler) {
	// this.xsdObjectHandler = xsdObjectHandler;
	// }
	//
	// @Override
	// public XsdObjectHandler getXsdObjectHandler() {
	// return xsdObjectHandler;
	// }
	//
	// /**
	// * @return list of contextual facets identified by the contributed facets in this object
	// */
	// public List<ContextualFacetNode> getContextualFacets() {
	// if (!(this instanceof ContextualFacetOwnerInterface))
	// return Collections.emptyList();
	//
	// ArrayList<ContextualFacetNode> facets = new ArrayList<ContextualFacetNode>();
	// // Aug 12, 2017 - changed to include directly owned and contributed.
	// // for (ContributedFacetNode n : getContributedFacets())
	// // facets.add(n.getContributor());
	// for (Node n : getChildren())
	// if (n instanceof ContributedFacetNode)
	// facets.add(((ContributedFacetNode) n).getContributor());
	// else if (n instanceof ContextualFacetNode)
	// facets.add((ContextualFacetNode) n);
	//
	// return facets;
	// }
	//
	// /**
	// * @return list of contributed facet children of this object
	// */
	// public List<ContributedFacetNode> getContributedFacets() {
	// if (!(this instanceof ContextualFacetOwnerInterface))
	// return Collections.emptyList();
	//
	// ArrayList<ContributedFacetNode> facets = new ArrayList<ContributedFacetNode>();
	// for (Node n : getChildren())
	// if (n instanceof ContributedFacetNode)
	// facets.add((ContributedFacetNode) n);
	// return facets;
	// }
	//
	// /**
	// * @return true if this object can be used as an assigned type or base type
	// */
	// @Override
	// public boolean isNamedEntity() {
	// return true;
	// }
	//
	// @Override
	// public boolean isRenameable() {
	// return isEditable_newToChain();
	// }
	//
	// /**
	// * Assign the name to all properties that use this type provider. Also update all type users contained by
	// children.
	// *
	// * @param name
	// */
	// protected void updateNames(String name) {
	// // FIXME
	// for (TypeUser user : getWhereAssigned()) {
	// if (user instanceof PropertyNode)
	// user.setName(NodeNameUtils.fixBusinessObjectName(name));
	// }
	//
	// for (Node child : getChildren()) {
	// for (TypeUser users : ((TypeProvider) child).getWhereAssigned())
	// ((Node) users).visitAllNodes(new NodeVisitors().new FixNames());
	// }
	//
	// }
}
