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
package org.opentravel.schemas.node.objectMembers;

import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.types.TypeProvider;

/**
 * Base class for classes that are not type providers or type users (properties). Note: not all object members inherit
 * from this base class as they are type providers.
 * <p>
 * These objects:
 * <ul>
 * <li>might be library members (extension points, version 1.6 contextual facets)
 * <li>can not be assigned or assigned to a type
 * <li>will have children that may be providers and/or users
 * <li>might have their name that may be combined with the parent's name
 * </ul>
 * 
 * @author Dave Hollander
 * 
 */
@Deprecated
public abstract class ObjectMembers extends ComponentNode implements TypeProvider {
	// private static final Logger LOGGER = LoggerFactory.getLogger(ObjectMembers.class);

	// private WhereAssignedHandler whereAssignedHandler = null;
	// private XsdObjectHandler xsdObjectHandler;

	@Deprecated
	public ObjectMembers() {
		// For types that have no TL
		// whereAssignedHandler = new WhereAssignedHandler(this);
	}

	@Deprecated
	public ObjectMembers(final TLModelElement obj) {
		// super(obj);
		// whereAssignedHandler = new WhereAssignedHandler(this);
		// if (!isInherited())
		// assert Node.GetNode(getTLModelObject()) == this;
	}
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
