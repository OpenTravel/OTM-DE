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

import java.util.ArrayList;
import java.util.List;

import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.interfaces.FacetInterface;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.types.WhereAssignedHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for <b>all</b> facets that are type providers but not library members.
 * 
 * @author Dave Hollander
 * 
 */
public abstract class FacetProviders extends TypeProviders implements FacetInterface {
	private static final Logger LOGGER = LoggerFactory.getLogger(FacetProviders.class);

	// private WhereAssignedHandler whereAssignedHandler = null;
	// private XsdObjectHandler xsdObjectHandler;

	public FacetProviders() {
		// For types that have no TL
		whereAssignedHandler = new WhereAssignedHandler(this);
	}

	public FacetProviders(final TLModelElement obj) {
		super(obj);
		whereAssignedHandler = new WhereAssignedHandler(this);
		if (!isInherited())
			assert Node.GetNode(getTLModelObject()) == this;
	}

	@Override
	public boolean isSimpleAssignable() {
		return false;
	}

	@Override
	public INode.CommandType getAddCommand() {
		return INode.CommandType.PROPERTY;
	}

	@Override
	public LibraryMemberInterface getOwningComponent() {
		return (LibraryMemberInterface) getParent();
	}

	// @Override
	@Deprecated
	public boolean isSummaryFacet() {
		return getFacetType() != null ? getFacetType().equals(TLFacetType.SUMMARY) : false;
	}

	@Override
	public boolean isFacet(TLFacetType type) {
		return getFacetType() != null ? getFacetType().equals(type) : false;
	}

	// Contextual facets can contain contextual facets
	// @Override
	// public PropertyNode findChildByName(String name) {
	// return get(name);
	// }

	@Override
	public PropertyNode get(String name) {
		for (Node n : getProperties())
			if (n instanceof PropertyNode && n.getName().equals(name))
				return (PropertyNode) n;
		return null;
	}

	@Override
	public abstract TLModelElement getTLModelObject();

	@Override
	public List<PropertyNode> getProperties() {
		List<PropertyNode> pns = new ArrayList<PropertyNode>();
		for (Node n : getChildrenHandler().get())
			if (n instanceof PropertyNode)
				pns.add((PropertyNode) n);
		return pns;
	}

	/**
	 * Make a copy of all the properties of the source facet and add to this facet. If the property is of the wrong
	 * type, it is changed into an attribute.
	 * 
	 * @param facet
	 */
	@Override
	public void copy(FacetInterface facet) {
		add(facet.getProperties(), true);
		//
		// PropertyNode newProperty = null;
		// for (Node p : facet.getChildren()) {
		// if (p instanceof PropertyNode) {
		// newProperty = ((PropertyNode) p).clone(this, null);
		// if (newProperty == null)
		// continue; // ERROR
		// if (!this.canOwn(newProperty.getPropertyType()))
		// newProperty = newProperty.changePropertyRole(PropertyNodeType.ATTRIBUTE);
		// newProperty.addToTL(this);
		// }
		// }
		// getChildrenHandler().clear(); // flush parent children cache
	}

	@Override
	public abstract PropertyNode createProperty(Node type);

	// *********************************************************************************

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
