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
/**
 * 
 */
package org.opentravel.schemas.types;

import java.util.ArrayList;

import org.opentravel.schemas.node.LibraryNode;
import org.opentravel.schemas.node.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages type assignment and usage.
 * 
 * Note: all type users are treated the same, base, element and type references. To know if it is a base type
 * assignment, see if it is a simpleType, simpleFacet or extension.
 * 
 * @author Dave Hollander
 * 
 */
// 2/20/2016 dmh - Replaced by TypeUserHandler and WhereUsedHandler
@Deprecated
public class Type {
	private static final Logger LOGGER = LoggerFactory.getLogger(Type.class);

	protected Node typeOwner = null; // the node to which types are assigned
	protected Node typeNode = null; // node used as the type
	protected TypeNode treeNode = null;

	// nodes that use this node as a type definition. Includes base type users.
	protected ArrayList<Node> typeUsers = new ArrayList<Node>();

	// nodes that use this node as a base type for extension. Included in typeUsers.
	protected ArrayList<Node> baseUsers = new ArrayList<Node>();

	// TODO - use synchronization to protect against background operations
	// protected List<Node> typeUsers2 = Collections.synchronizedList(new ArrayList<Node>());
	// synchronized(list) {
	// for (Object o : list) {}
	// }

	public Type(Node n) {
		typeOwner = n;
		// treeNode = new TypeNode(n);
	}

	/**
	 * Clear out the type node and remove all links to and from type users. Does <b>not</b> modify underlying tl objects
	 * or type assignments.
	 */
	// public void clear() {
	// setTypeNode(null);
	// clearTypeUsers();
	// // LOGGER.debug("Cleared type class for owner: " + typeOwner);
	// }

	/**
	 * Clear users of the this type provider. Sets all users to unassigned. Intended for use in bulk operations as it
	 * does not do children or support limiting scope. Use ReplaceTypeProvider() for general usage.
	 */
	// private void clearTypeUsers() {
	// Node target = ModelNode.getUnassignedNode();
	// if (typeOwner == target)
	// return;
	// if (typeUsers == null)
	// return;
	//
	// // Set everyone who uses this as a type to unassigned
	// for (Node n : typeUsers) {
	// Type nt = n.getTypeClass();
	// // nt.setTypeNode(target);
	// // If unassigned doesn't know about this, add it
	// if (!target.typeUsers().contains(nt.typeOwner))
	// target.typeUsers().add(nt.typeOwner);
	// // nt.typeNode = target;
	// }
	// typeUsers.clear();
	//
	// }

	/**
	 * This owner is removed from assigned type's where-used list, but leaves typeNode and tlType assigned.
	 */
	public void clearWhereUsed() {
		// FIXME - logic is wrong. Only used in propertyNode.swap().
		// getTypeNode().typeUsers().remove(typeOwner);
		// if (!(typeNode == null) && (typeNode.typeUsers() == null))
		// typeNode.typeUsers().remove(typeOwner);
	}

	/**
	 * Delete the type node and remove all links to and from type users. Unlike {@link clear()}, delete does change the
	 * type assignments to the underlying TL model.
	 */
	// public void delete() {
	// // if (typeNode != null)
	// // typeNode.typeUsers().remove(typeOwner);
	// setTypeNode(null);
	// deleteTypeUsers();
	// // FIXME - where or how does this change the tl objects?
	// // LOGGER.debug("Cleared type class for owner: " + typeOwner);
	// }

	/**
	 * Deletes users of the this type provider. Sets all users to unassigned. Intended for use in bulk operations as it
	 * does not do children or support limiting scope. Use ReplaceTypeProvider() for general usage. This method
	 * <b>does</b> remove TL Model type assignments to this type.
	 */
	// private void deleteTypeUsers() {
	// if (typeOwner == ModelNode.getUnassignedNode())
	// return;
	// if (typeUsers == null)
	// return;
	// // need to create copy,
	// // the setTypeNode is breaking existing links (modifies the typeUsers collection)
	// ArrayList<Node> typeUsersCopy = new ArrayList<Node>(typeUsers);
	// for (Node n : typeUsersCopy) {
	// n.getTypeClass().setTypeNode(ModelNode.getUnassignedNode());
	// n.getModelObject().clearTLType();
	// }
	// typeUsers.clear();
	// // TODO - remove listeners and prevent listeners from assigning null
	// }
	//
	// public ArrayList<Node> getBaseUsers() {
	// return baseUsers;
	// }

	/**
	 * @return the Node used in type assignments to the owner of this type class.
	 */
	// public TypeProvider getTypeNode() {
	// if (typeNode == null || typeOwner.getAssignedTLObject() == null) {
	// return ModelNode.getUnassignedNode();
	// }
	// if (!(typeOwner.getAssignedTLObject() instanceof TLModelElement)) {
	// throw new IllegalStateException(typeOwner + " is not a model element.");
	// }
	//
	// // May have to trap simpleAttributeNode and get it from its parent.
	// // SimpleFacetNode does not match TL Object. Simple Attribute shares type node with SimpleFacet where owner is
	// // facet.
	// // if (typeOwner instanceof SimpleFacetNode) {
	// // typeOwner.getAssignedTLObject()
	// // return ((TypeUser) ((SimpleFacetNode) typeOwner).getSimpleAttribute()).getAssignedType();
	// // // return ((SimpleFacetNode) typeOwner).getAssignedType();
	// // }
	//
	// return (TypeProvider) Node.GetNode((TLModelElement) typeOwner.getAssignedTLObject());
	//
	// // // TypeNode may not have been set yet. thats OK.
	// // if (typeNode instanceof ImpliedNode) {
	// // return ((TypeUser) typeOwner).getAssignedType();
	// // }
	// // if (Node.GetNode((TLModelElement) typeOwner.getAssignedTLObject()) != typeNode) {
	// // // LOGGER.debug("FIXME - " + typeOwner + " type assignment.");
	// // throw new IllegalStateException("Invalid type assignment for " + typeOwner);
	// // }
	// // return ((TypeUser) typeOwner).getAssignedType();
	// // return typeNode;
	// }

	/**
	 * 
	 * @return the typeOwner
	 */
	@Deprecated
	public Node getTypeOwner() {
		return typeOwner;
	}

	// /**
	// * @return the typeTreeNode - used in viewers to represent the where-used content
	// */
	// public Node getTypeTreeNode() {
	// return treeNode;
	// }

	/**
	 * @return the live list of editable nodes where this node is assigned as a type.
	 */
	// public ArrayList<Node> getTypeUsers() {
	// if (typeUsers == null) {
	// // typeUsers = setTypeUsers();
	// // LOGGER.debug("Set type users durning a getUsers for " + getTypeOwner());
	// throw new IllegalStateException("Set type users durning a getUsers for " + typeOwner);
	// }
	// return typeUsers;
	// }

	// public int getTypeUsersCount() {
	// synchronized (this) {
	// if (typeUsers == null) {
	// // typeUsers = setTypeUsers();
	// // LOGGER.debug("Set type users durning a getUsers for " + getTypeOwner());
	// throw new IllegalStateException("Set type users durning a getUsers for " + typeOwner);
	// }
	// }
	// return typeUsers.size();
	// }

	/**
	 * get Type Users And Descendants - users of any member of the owning component
	 * 
	 * @return a new list of editable nodes where any member of the owning component (aliases, facets) are is assigned
	 *         as a type or base type.
	 */
	// public List<Node> getTypeUsersAndDescendants() {
	// List<Node> users = new ArrayList<Node>(getTypeUsers());
	// for (Node n : typeOwner.getOwningComponent().getDescendants_TypeProviders())
	// if (!n.isDeleted())
	// users.addAll(n.getTypeClass().getTypeUsers());
	// // for (Node n : typeOwner.getOwningComponent().getChildren()) {
	// // if (n.isTypeProvider() && !n.isDeleted())
	// // users.addAll(n.getTypeClass().getTypeUsers());
	// // }
	// return users;
	// }

	// public int getTypeUsersAndDescendantsCount() {
	// return getTypeUsersAndDescendants().size();
	// }

	// /**
	// * Remove the assigned type. Removes this node from typeNode's list of typeUsers. Sets typeNode = null.
	// *
	// * Does Change the TL Model. Does Not Change the Model Object. TEST Sets the TL model object type and adds this
	// node
	// * TEST the assigned type's users list.
	// */
	// @Deprecated
	// public void removeAssignedType() {
	// clearWhereUsed();
	// // Set underlying model to unassigned.
	// if (typeOwner.isTypeUser())
	// typeOwner.getModelObject().setTLType(ModelNode.getUnassignedNode().getModelObject());
	//
	// typeNode = null;
	// }

	/**
	 * Replace this provider with replacement for all users of this provider as a type. Also replaces type usage of
	 * descendants of this typeOwner node. Also does the TL properties. Note - user counts may change when business
	 * replace core objects because core is also a valid simple type.
	 * 
	 * It is OK for the owner to not be in a library. This happens when it is being replaced.
	 * 
	 * @param replacement
	 *            is the TypeProvider to use instead. Must be a typeProvider. skips assignment if replacement is not
	 *            compatible with user.
	 * @param libraryScope
	 *            - if null to entire model, otherwise only replace users within specified library.
	 */
	public void replaceTypeProvider(Node replacement, LibraryNode scopeLibrary) {

		// // Nothing to do.
		// if (getTypeUsers().isEmpty() && getBaseUsers().isEmpty())
		// return;
		// LOGGER.debug("Replacing " + typeOwner + " used " + getTypeUsers().size() + " times.");
		//
		// if (replacement == null || !replacement.isTypeProvider())
		// return;
		//
		// // Replace the users of this object as a type.
		// replaceUsers(replacement, scopeLibrary);
		//
		// // Replace users of all descendants.
		// // Create map of replacement candidates
		// HashMap<String, Node> replacementTypes = new HashMap<String, Node>();
		// for (Node r : replacement.getDescendants_NamedTypes())
		// replacementTypes.put(r.getName(), r);
		//
		// for (Node n : typeOwner.getDescendants_NamedTypes()) {
		// // Try to find a replacement equivalent from replacement object
		// Node r = replacementTypes.get(n.getName());
		// if (r == null)
		// r = ModelNode.getUnassignedNode();
		// n.getTypeClass().replaceUsers(r, scopeLibrary);
		// }
	}

	/**
	 * Set this type owner to a new value.
	 * 
	 * @param newValue
	 *            is node to use as this onwer's type
	 * @param oldValue
	 *            node that was this owner's type
	 */
	// public void set(Node newValue, Node oldValue) {
	//
	// // This test should test namespace:name not node equality
	// // if (typeNode != oldValue)
	// // LOGGER.warn("Old value is not correct. oldValue = " + oldValue + " while typeNode = " + typeNode);
	//
	// // Use unassigned instead of null. Provides location to find all unassigned types.
	// if (newValue == null)
	// newValue = ModelNode.getUnassignedNode();
	//
	// // remove owner from old value's user list
	// if (oldValue != null)
	// oldValue.getTypeUsers().remove(typeOwner);
	//
	// // Add owner to new value's type user list
	// if (!newValue.getTypeUsers().contains(typeOwner))
	// newValue.getTypeUsers().add(typeOwner);
	//
	// // Set typeNode to new value
	// typeNode = newValue;
	//
	// // LOGGER.debug("type.set() " + newValue + " replaced " + oldValue + " on " + typeOwner);
	// }

	/**
	 * Set the base type of the type owner. Links the type owner to target's type user list.
	 * 
	 * @param sourceNode
	 *            is the node to be assigned as base type.
	 */
	// public void setAssignedBaseType(INode sourceNode) {
	// if (!(typeOwner instanceof ExtensionOwner))
	// sourceNode = ModelNode.getUnassignedNode();
	// if ((sourceNode == null) || (!sourceNode.isTypeProvider()))
	// sourceNode = ModelNode.getUnassignedNode();
	//
	// setBaseType((Node) sourceNode);
	//
	// // Set the TL model if TLExtension owner or else set to null (clear)
	// // Note: VWAs are not members of TLExtensionOnwer and must have the parent type set instead.
	// if (!(sourceNode instanceof ImpliedNode))
	// if (typeOwner.getTLModelObject() instanceof TLExtensionOwner)
	// typeOwner.getModelObject().setExtendsType(sourceNode.getModelObject());
	// else if (typeOwner instanceof VWA_Node)
	// ((VWA_Node) typeOwner).setAssignedType((TypeProvider) sourceNode);
	// else
	// typeOwner.getModelObject().setExtendsType(null);
	//
	// // LOGGER.debug("Set base type of " + typeOwner + " to " + sourceNode);
	// }

	/**
	 * Set Assigned Type. Sets the Assigned type node and add this owner to that user list. This method assures there is
	 * a target and that the owner is editable. Sets the type class properties as well as the TLModel type If the target
	 * represents a complex type (BO, Core, facet, alias) then the owner's name is changed.
	 * 
	 * Callers may wish to validate assignment first; note that this MAY be a heavy process
	 * 
	 * boolean result = isValidAssignment(target);
	 * 
	 * @return true if assignment could be made, false otherwise
	 */
	// control refresh elsewhere
	// public boolean setAssignedType(TypeProvider target) {
	// if (!typeOwner.isEditable())
	// return false;
	//
	// if (typeOwner.getRequiredType() != null)
	// target = (TypeProvider) ((Node) typeOwner).getRequiredType();
	//
	// // TODO - resource node should use getDefaultType(), at least for some
	// if (typeOwner instanceof ResourceNode)
	// if (((ResourceNode) typeOwner).getSubject() == null)
	// target = ModelNode.getUndefinedNode(); // It is OK for subject to not be assigned.
	//
	// if (target == null)
	// target = ModelNode.getUnassignedNode();
	//
	// // Set the tl type and let the listeners handle the rest
	// TLModelElement tlOwner = typeOwner.getTLModelObject();
	// TLModelElement tlTarget = target.getTLModelObject();
	//
	// assert typeOwner instanceof TypeUser;
	// assert target instanceof TypeProvider;
	//
	// // Add handler listener
	// TypeProvider oldValue = ((TypeUser) typeOwner).getAssignedType(); // hold onto to remove listener at end
	// ((TypeProvider) target).setListener((TypeUser) typeOwner);
	//
	// if (tlOwner instanceof TLSimple)
	// if (tlTarget instanceof TLAttributeType) {
	// ((TLSimple) tlOwner).setParentType((TLAttributeType) tlTarget);
	// } else
	// return false; // do nothing
	// else if (tlOwner instanceof TLProperty)
	// if (tlTarget instanceof TLPropertyType)
	// ((TLProperty) tlOwner).setType((TLPropertyType) tlTarget);
	// else
	// return false; // do nothing
	// else if (tlOwner instanceof TLAttribute)
	// if (tlTarget instanceof TLAttributeType)
	// ((TLAttribute) tlOwner).setType((TLAttributeType) tlTarget);
	// else
	// return false;
	// else if (tlOwner instanceof TLSimpleFacet)
	// if (tlTarget instanceof NamedEntity) {
	// ((TLSimpleFacet) tlOwner).setSimpleType((NamedEntity) tlTarget);
	// // Listener will set the simple attribute node
	// } else
	// return false;
	// else if (tlOwner instanceof TLnSimpleAttribute)
	// if (tlTarget instanceof NamedEntity)
	// ((TLnSimpleAttribute) tlOwner).setType((NamedEntity) tlTarget);
	// else
	// return false;
	// else if (tlOwner instanceof TLAbstractEnumeration) {
	// TLExtension extension = new TLExtension();
	// extension.setExtendsEntity((NamedEntity) tlTarget);
	// ((TLAbstractEnumeration) tlOwner).setExtension(extension);
	// }
	// // Safety check. Will set if:
	// // 1. no listeners
	// // 2. No even thrown if the tl object original and target types are the same.
	// // 3. if it is an implied type (role, enum, etc)
	// if (!(typeNode == target))
	// set((Node) target, typeNode);
	//
	// // FIXME - what about names for the type user elements?
	// //
	// NodeNameUtils.fixName(typeOwner);
	// // Update the where used display node
	// // if (refresh && OtmRegistry.getNavigatorView() != null)
	// // OtmRegistry.getNavigatorView().refresh(treeNode);
	//
	// if (oldValue != target)
	// ((TypeProvider) oldValue).removeListener((TypeUser) typeOwner);
	// LOGGER.debug(typeOwner + " changed assigment from " + oldValue + " to " + target);
	// return true;
	// }

	/**
	 * Set assigned type. Sets type or base type as appropriate for the node. Extends setAssignedType() by using the
	 * passed source node to get the Qname of the node to find. To set the QName, the TLTypeObject is tried first if not
	 * null, otherwise the assigned xsd type in the documentation.
	 * 
	 * If the source node is not found, the Unassigned ImpliedNode is assigned.
	 * 
	 * Implied types (indicators, enumerations) are assigned to their appropriate implied Node.
	 * 
	 * @return true if successfully assigned.
	 */
	/**
	 * @param source
	 *            - the target and the node to use to find the type.
	 */
	// public boolean setAssignedTypeForThisNode(Node source) {
	// return setAssignedTypeForThisNode(source, null);
	// }

	/**
	 * Used in type resolver to assign type to the owner based on a source QName
	 * 
	 * @param source
	 *            - the target <b>and</b> the node to use to find the type.
	 */
	// public boolean setAssignedTypeForThisNode(Node source, Map<QName, Node> providerMap) {
	// QName typeQname = null;
	// LibraryNode typeLibrary = null;
	// AbstractLibrary srcLib = null;
	// Node lt = source.getAssignedTypeByListeners(); // save for checking later
	//
	// if (!source.isTypeUser()) {
	// LOGGER.warn("Tried to assign type to a node that is not a type user. " + source);
	// return false;
	// }
	//
	// // Get what we know about the assigned type.
	// // TargetNode if the node has a default type or else QName
	// TypeProvider target = source.getRequiredType();
	// if (target == null) {
	// if (source.getTLTypeObject() != null && source.getTLTypeObject().getOwningLibrary() == null) {
	// // LOGGER.debug(source + " TL type cant be found because it is not in a library!");
	// target = ModelNode.getUnassignedNode();
	// }
	// // try to get it from the property type or documentation
	// typeQname = source.getTLTypeQName();
	// if (typeQname == null)
	// LOGGER.debug("Qname is null for " + source);
	// }
	//
	// // If we don' have a map, get the library for the finder.
	// if (providerMap == null) {
	// srcLib = source.getModelObject().getOwningLibrary();
	// if (srcLib != null) {
	// for (LibraryNode ln : Node.getAllLibraries()) {
	// if (ln.getTLaLib().equals(srcLib)) {
	// typeLibrary = ln;
	// break;
	// }
	// }
	// }
	// }
	//
	// // Find the node to assign
	// if (target == null && typeQname != null && !typeQname.getLocalPart().isEmpty()) {
	// if (providerMap == null) {
	// // Try to find the exact node, regardless of duplicates.
	// target = (TypeProvider) NodeFinders.findTypeProviderByQName(typeQname, typeLibrary);
	// if (target == null)
	// // if not in the type's library, then try all libraries
	// target = (TypeProvider) NodeFinders.findTypeProviderByQName(typeQname);
	// } else {
	// // Use the provider map to find the type to assign.
	// target = (TypeProvider) providerMap.get(typeQname);
	// if (target == null)
	// // Try the name without the "type" suffix
	// target = (TypeProvider) providerMap.get(new QName(typeQname.getNamespaceURI(), NodeNameUtils
	// .stripTypeSuffix(typeQname.getLocalPart())));
	// if (target == null)
	// // Try a chameleon with the local part name.
	// target = (TypeProvider) providerMap.get(new QName(XsdModelingUtils.ChameleonNS, typeQname
	// .getLocalPart()));
	// if (target == null)
	// // Finally try without the "type" suffix in chameleon ns
	// target = (TypeProvider) providerMap.get(new QName(XsdModelingUtils.ChameleonNS, NodeNameUtils
	// .stripTypeSuffix(typeQname.getLocalPart())));
	// }
	// }
	//
	// // If the target was not found, use the UnassignedNode
	// boolean ret = true;
	// if (target == null || !target.isTypeProvider()) {
	// target = ModelNode.getUnassignedNode();
	// ret = false;
	// // LOGGER.warn("Missing assignment target for: " + source + " with qName = " +
	// // typeQname);
	// }
	//
	// if (source.getTLTypeObject() != null || target instanceof ImpliedNode) {
	// // Set only the type class, not the underlying TL model.
	// try {
	// source.getTypeClass().setTypeNode((Node) target);
	// } catch (IllegalArgumentException e) {
	// LOGGER.debug("Could not set TL Type " + e);
	// }
	// // LOGGER.debug("Type of " + source + " assigned " + target);
	// // verifyAssignment(providerMap, typeQname);
	// } else {
	// // Assign both type class and TL model to the found target
	// verifyAgainstListener(lt, source, (Node) target);
	// source.getTypeClass().setAssignedType(target);
	// // LOGGER.debug("Type and model of " + source + " assigned " + target);
	// }
	//
	// return ret;
	// }

	/**
	 * Set the type node and add this owner to that base user list. Remove typeOwner from typeNode-base users.
	 * Restricted use - Does NOT set the type the TLModel type {@link setAssignedBaseType}
	 */
	public void setBaseType(Node sourceNode) {
		// if (typeOwner instanceof ExtensionOwner) {
		//
		// // Unlink if base type is already set.
		// if ((typeNode != null) && (typeNode.getTypeClass().baseUsers != null))
		// typeNode.getTypeClass().baseUsers.remove(typeOwner);
		//
		// // Add this owner to the sources base users list
		// if (!sourceNode.getTypeClass().baseUsers.contains(typeOwner))
		// sourceNode.getTypeClass().baseUsers.add(typeOwner);
		// // TESTME - this used to also add to the typeUsers array
		//
		// typeNode = sourceNode;
		// }
	}

	/**
	 * Set the type node and add this owner to that user list. Remove typeOwner from typeNode-typeUsers. Restricted use
	 * - Does NOT set the type the TLModel type {@link setAssignedType}
	 */
	// public boolean setTypeNode(Node target) {
	// if (target != null) {
	// if (!target.typeUsers().contains(this))
	// target.typeUsers().add(this.typeOwner);
	// }
	//
	// if (typeNode != null)
	// typeNode.getTypeUsers().remove(typeOwner); // break existing link
	//
	// typeNode = target;
	// return true;
	// }

	/**
	 * USE ONLY FOR TESTING
	 * 
	 * @return true if the TypeOwner TL_TypeObject matches the typeNode's TL_ModelObject
	 */
	// @Deprecated
	// public boolean verifyAssignment() {
	// return verifyAssignment(null, null);
	// }

	// @Deprecated
	// private boolean isTLTypeAssigned() {
	// NamedEntity ownerTLType = typeOwner.getTLTypeObject();
	// // Name and library namespace are used to find the matching node. Must not be null.
	// boolean ret = true;
	// if (ownerTLType == null)
	// ret = false;
	// else if (ownerTLType.getOwningLibrary() == null)
	// ret = false;
	// else if (ownerTLType.getLocalName() == null)
	// ret = false;
	// else
	// ret = (!(ownerTLType.getLocalName().equals("Undefined")) || ownerTLType.getLocalName().equals(
	// "Unassigned-missingAssignment"));
	// // if (!ret)
	// // LOGGER.debug("Type owner " + typeOwner + " does not have tl type assigned.");
	// return ret;
	// }

	// @Deprecated
	// private boolean isTypeNodeAssigned() {
	// Node type = (Node) typeOwner.getTypeClass().getTypeNode();
	// if (type == null)
	// return false;
	// if (type.getLibrary() == null)
	// return false;
	// return ((type instanceof ImpliedNode)) ? false : true;
	// }

	// public boolean isValidAssignment(Node target) {
	// return isValidAssignment(typeOwner.getTLModelObject(), target.getTLModelObject());
	// }
	//
	// private boolean isValidAssignment(ModelElement tlOwner, ModelElement tlTarget) {
	// boolean result = true;
	// TypeAssignmentTester tester = new TypeAssignmentTester(ModelNode.getModelNode().getTLModel());
	//
	// if (tlOwner.getOwningModel() != null) {
	// if (tlOwner instanceof TLAttribute && tlTarget instanceof NamedEntity)
	// result = tester.isValidAssignment((TLAttribute) tlOwner, (NamedEntity) tlTarget);
	// if (tlOwner instanceof TLProperty && tlTarget instanceof NamedEntity)
	// result = tester.isValidAssignment((TLProperty) tlOwner, (NamedEntity) tlTarget);
	// if (result == false)
	// LOGGER.debug("Not Valid assignment of " + tlTarget.getValidationIdentity() + " to "
	// + tlOwner.getValidationIdentity());
	// }
	// return result;
	// }

	// Replace type assignments to all type and base user nodes IFF in scope and editable.
	// private void replaceUsers(Node replacement, LibraryNode scope) {
	// FixNames nameFixer = new NodeVisitors().new FixNames();
	// if (!(replacement instanceof ImpliedNode) && replacement.getLibrary() == null) {
	// LOGGER.warn("Error - replacement " + replacement + " is not in a library.");
	// replacement = ModelNode.getUnassignedNode();
	// }
	// // base users
	// ArrayList<Node> users = new ArrayList<Node>(getBaseUsers());
	// for (Node n : users) {
	// if (n.isEditable() && (scope == null || n.getLibrary().equals(scope))) {
	// n.getTypeClass().setAssignedBaseType(replacement);
	// baseUsers.remove(n);
	// }
	// }
	//
	// users = new ArrayList<Node>(getTypeUsers());
	// for (Node n : users) {
	// if (n.isEditable() && (scope == null || n.getLibrary().equals(scope))) {
	// if (n.getTypeClass().setAssignedType((TypeProvider) replacement)) {
	// nameFixer.visit(n);
	// getTypeUsers().remove(n);
	// } else {
	// // setAssignedType if replacement failed, try unassigned.
	// if (n.getTypeClass().setAssignedType(ModelNode.getUnassignedNode())) {
	// getTypeUsers().remove(n);
	// // LOGGER.debug("Set type to " + ModelNode.getUnassignedNode() + " for " +
	// // n);
	// } else {
	// LOGGER.warn("ERROR Setting type to " + replacement + " failed for " + n);
	// }
	// }
	// }
	// }
	// }

	// private void verifyAgainstListener(Node lt, Node source, Node target) {
	// // verify against listener
	// if (target != lt) {
	// Node lt2 = source.getAssignedTypeByListeners();
	// if (lt.getName().equals(target.getName()) && lt.getNamespace().equals(target.getNamespace()))
	// LOGGER.warn(lt + " has a duplicate type.");
	// else if (source.getLibrary().isBuiltIn())
	// LOGGER.warn("Built in library member " + source + " type does not match.");
	// else
	// // throw new IllegalStateException(target + " type assignment does not match listener " + lt + ".");
	// LOGGER.error(source + " type assignment error.");
	// }
	// }
	//
	// @Deprecated
	// private boolean verifyAssignment(Map<QName, Node> providerMap, QName typeQname) {
	// // LOGGER.debug("Verify assignments for type owner: " + typeOwner);
	// if (typeOwner == null) {
	// LOGGER.debug("NULL type owner: " + typeOwner);
	// return false;
	// }
	// if (!typeOwner.isTypeUser()) {
	// return true;
	// }
	// if (typeOwner.getTLModelObject() == null) {
	// LOGGER.debug("NULL model object on type owner: " + typeOwner);
	// return false;
	// }
	// if (typeNode == null) {
	// if (typeOwner.getModelObject().getTLType() != null) {
	// LOGGER.debug("Invalid type node: " + typeNode + " on " + typeOwner);
	// return false;
	// }
	// return true; // tests can not be made.
	// }
	// if (typeNode.getTLModelObject() == null) {
	// LOGGER.debug("Missing model object on " + typeOwner);
	// return false;
	// }
	// // If the TL type is not assigned, then the type node must not be either.
	// if (!isTLTypeAssigned()) {
	// if (isTypeNodeAssigned()) {
	// LOGGER.debug("Invalid: " + typeOwner + " TLType is not assigned " + " but TypeNode is assigned: "
	// + typeOwner.getTypeClass().getTypeNode());
	// return false;
	// } else
	// return true;
	// }
	//
	// boolean ret = typeOwner.getTLTypeObject().getValidationIdentity()
	// .equals(typeNode.getTLModelObject().getValidationIdentity());
	//
	// if (!ret)
	// if (Node.getModelNode().getDuplicateTypes().contains(typeNode)) {
	// LOGGER.warn("Warning, typeNode " + typeNode + " is a duplicate.");
	// ret = true;
	// } else if ((typeNode instanceof ImpliedNode)
	// && ((ImpliedNode) typeNode).getImpliedType().equals(ImpliedNodeType.String)) {
	// LOGGER.warn("Note - owner: " + typeOwner + " has an implied string type.");
	// ret = true;
	// } else if ((typeNode instanceof ImpliedNode)
	// && ((ImpliedNode) typeNode).getImpliedType().equals(ImpliedNodeType.Union)) {
	// LOGGER.warn("Note - owner: " + typeOwner + " has an union type.");
	// ret = true;
	// } else if (typeNode instanceof AliasNode) {
	// // aliases report different validation identities.
	// LOGGER.warn("Skipping test of alias: " + typeNode);
	// ret = true;
	// } else {
	// // LOGGER.debug("INVALID Assignment to " + typeOwner.getTLModelObject().getValidationIdentity()
	// // + " with TL type " + typeOwner.getTLTypeObject().getValidationIdentity());
	// // LOGGER.debug("    but type node has " + typeNode.getTLModelObject().getValidationIdentity());
	// if (providerMap != null) {
	// LOGGER.warn("Warning - Map results for " + typeQname + " = " + providerMap.get(typeQname));
	//
	// }
	// }
	// // else
	// // LOGGER.debug(typeOwner + " is correctly assigned " + typeNode + "? " + ret);
	// return ret;
	// }
}
