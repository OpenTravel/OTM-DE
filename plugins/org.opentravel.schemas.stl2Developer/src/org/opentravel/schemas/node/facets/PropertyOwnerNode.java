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

import org.opentravel.schemas.node.objectMembers.FacetOMNode;
import org.opentravel.schemas.node.properties.PropertyOwnerInterface;
import org.opentravel.schemas.node.typeProviders.TypeProviders;
import org.opentravel.schemas.types.TypeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Property Owners are any "Facet" that can contain children which are properties (elements, attributes, indicators,
 * ect.).
 * 
 * This controller implements the PropertyOwner Interface and represents a variety of TL objects which can contain
 * properties. Property Owners can contain properties as well as other facets. Extension points, simple facets on VWA
 * and Core objects, Operation RQ, RS and Notif messages are also modeled with PropertyOwnerNode.
 * 
 * @author Dave Hollander
 * 
 */
@Deprecated
public abstract class PropertyOwnerNode extends TypeProviders implements PropertyOwnerInterface, TypeProvider {
	private static final Logger LOGGER = LoggerFactory.getLogger(FacetOMNode.class);

	public PropertyOwnerNode() {
	}

	// // Needed for sub-classes
	// public PropertyOwnerNode(final TLModelElement obj) {
	// super(obj);
	// }
	//
	// // @Override
	// // public void add(final PropertyNode pn, final int index) {
	// // // Add to children list
	// // pn.setParent(this);
	// //
	// // // Add to the tl model
	// // if (index < 0)
	// // pn.addToTL(this);
	// // else
	// // pn.addToTL(this, index);
	// //
	// // // Events are not being thrown (10/14/2017) so force their result
	// // childrenHandler.clear();
	// // // clear handlers on any inherited "ghost" facets
	// // for (ModelElementListener l : getTLModelObject().getListeners())
	// // if (l instanceof InheritanceDependencyListener)
	// // ((InheritanceDependencyListener) l).run();
	// // }
	// //
	// // @Override
	// // public void addProperties(List<Node> properties, boolean clone) {
	// // PropertyNode np;
	// // for (Node p : properties) {
	// // if (!(p instanceof PropertyNode))
	// // continue;
	// // np = (PropertyNode) p;
	// // if (clone)
	// // np = (PropertyNode) p.clone(null, null); // add to clone not parent
	// // if (isValidParentOf(np.getPropertyType()))
	// // addProperty(np);
	// // }
	// // }
	// //
	// // @Override
	// // public void addProperty(PropertyNode property) {
	// // add(property, -1);
	// // }
	//
	// /**
	// * Make a copy of all the properties of the source facet and add to this facet. If the property is of the wrong
	// * type, it is changed into an attribute.
	// *
	// * @param sourceFacet
	// */
	// public void copyFacet(PropertyOwnerNode sourceFacet) {
	// // PropertyNode newProperty = null;
	// // for (Node p : sourceFacet.getChildren()) {
	// // if (p instanceof PropertyNode) {
	// // newProperty = ((PropertyNode) p).clone(this, null);
	// // if (newProperty == null)
	// // continue; // ERROR
	// // if (!this.isValidParentOf(newProperty.getPropertyType()))
	// // newProperty = newProperty.changePropertyRole(PropertyNodeType.ATTRIBUTE);
	// // newProperty.addToTL(this);
	// // }
	// // }
	// // getChildrenHandler().clear(); // flush parent children cache
	// }
	//
	// @Override
	// public INode createProperty(final Node type) {
	// PropertyNode pn = null;
	// // if (this instanceof AttributeFacetNode)
	// // pn = new AttributeNode(new TLAttribute(), this);
	// // else
	// // pn = new ElementNode(new TLProperty(), this);
	// // pn.setDescription(type.getDescription());
	// // if (type instanceof TypeProvider)
	// // pn.setAssignedType((TypeProvider) type);
	// // pn.setName(type.getName());
	// return pn;
	// }
	//
	// @Override
	// public PropertyNode findChildByName(String name) {
	// return (PropertyNode) super.findChildByName(name);
	// }
	//
	// @Override
	// public INode.CommandType getAddCommand() {
	// return INode.CommandType.PROPERTY;
	// }
	//
	// @Override
	// public abstract String getComponentType();
	//
	// @Override
	// public abstract TLFacetType getFacetType();
	//
	// @Override
	// public Image getImage() {
	// return Images.getImageRegistry().get(Images.Facet);
	// }
	//
	// @Override
	// public LibraryNode getLibrary() {
	// // contextual facets are property owners but are also library members in version 1.6
	// if (this instanceof LibraryMemberInterface)
	// return getLibrary();
	//
	// if (getOwningComponent() == null || getOwningComponent() == this)
	// return null;
	// return getOwningComponent().getLibrary();
	// }
	//
	// @Override
	// public abstract String getName();
	//
	// @Override
	// public LibraryMemberInterface getOwningComponent() {
	// if (getParent() == null)
	// return null;
	// if (!(getParent() instanceof LibraryMemberInterface))
	// return getParent().getOwningComponent();
	// return (LibraryMemberInterface) getParent();
	// }
	//
	// /**
	// * ******************************************* Abstract Methods
	// */
	// @Override
	// public abstract TLModelElement getTLModelObject();
	//
	// @Override
	// public boolean hasChildren_TypeProviders() {
	// return isXsdType() ? false : true;
	// }
	//
	// @Override
	// public boolean hasTreeChildren(boolean deep) {
	// return getWhereUsedCount() > 0 ? true : false; // where used node
	// }
	//
	// @Override
	// public boolean isAssignable() {
	// if (getParent() == null)
	// return false;
	// return (isComplexAssignable() || isSimpleAssignable()) ? true : false;
	// }
	//
	// @Override
	// public boolean isAssignableToElementRef() {
	// return false;
	// }
	//
	// @Override
	// public boolean isAssignableToSimple() {
	// if (!isSimpleListFacet())
	// return false;
	// if (getOwningComponent() instanceof CoreObjectNode)
	// if (((CoreObjectNode) getOwningComponent()).getAssignedType() != ModelNode.getEmptyNode())
	// return true;
	// return false;
	// }
	//
	// @Override
	// public boolean isAssignableToVWA() {
	// return isAssignableToSimple();
	// }
	//
	// @Override
	// public boolean isAssignedByReference() {
	// if (getOwningComponent() == null) {
	// // LOGGER.equals("No owning component for this facet: " + this);
	// return false;
	// }
	// if (isSimpleListFacet())
	// return false;
	// return getOwningComponent() instanceof VWA_Node ? false : true;
	// }
	//
	// public boolean isComplexAssignable() {
	// return true;
	// }
	//
	// @Override
	// public boolean isDefaultFacet() {
	// if (getOwningComponent() instanceof VWA_Node)
	// return true;
	// return false;
	// }
	//
	// @Override
	// public boolean isDeleteable() {
	// return false;
	// }
	//
	// public boolean isDetailFacet() {
	// return false;
	// }
	//
	// public boolean isIDFacet() {
	// return false;
	// }
	//
	// @Override
	// public boolean isNamedEntity() {
	// return getParent() != null ? true : false;
	// }
	//
	// @Override
	// public boolean isNavChild(boolean deep) {
	// return true;
	// }
	//
	// @Override
	// public boolean isRenameableWhereUsed() {
	// if (getOwningComponent() == null || getOwningComponent() == this)
	// return false;
	// return ((TypeProvider) getOwningComponent()).isRenameableWhereUsed();
	// }
	//
	// /**
	// * Facets assigned to core object list types have no model objects but may be page1-assignable.
	// */
	// @Override
	// public boolean isSimpleAssignable() {
	// return false;
	// }
	//
	// public boolean isSimpleListFacet() {
	// return false;
	// }
	//
	// public boolean isSummaryFacet() {
	// return false;
	// }
	//
	// @Override
	// @Deprecated
	// public boolean canOwn(PropertyNode pn) {
	// if (pn == null)
	// return false;
	// return PropertyNodeType.getAllTypedPropertyTypes().contains(pn.getPropertyType());
	// }
	//
	// @Override
	// public boolean canOwn(PropertyNodeType type) {
	// return PropertyNodeType.getAllTypedPropertyTypes().contains(type);
	// }
	//
	// /**
	// * Remove the properties in the list from this node and underlying tl model object. Use to move the property to a
	// * different facet.
	// *
	// * @param property
	// */
	// @Override
	// public void removeProperty(final Node property) {
	// ((PropertyNode) property).removeProperty();
	// }
	//
	// @Deprecated
	// public void setContext(final String context) {
	// if (!isEditable_newToChain())
	// return;
	// final Object ne = getTLModelObject();
	// if (ne instanceof TLFacet) {
	// ((TLFacet) ne).setContext(context);
	// }
	// }
	//
	// @Override
	// public void setName(String n) {
	// }
	//
	// /**
	// * Return true if the node is delete-able using the version and managed state information used by Node. Used by
	// * sub-types that are deleteable.
	// */
	// protected boolean isDeleteable(boolean deletable) {
	// return deletable ? super.isDeleteable() : false;
	// }

}
