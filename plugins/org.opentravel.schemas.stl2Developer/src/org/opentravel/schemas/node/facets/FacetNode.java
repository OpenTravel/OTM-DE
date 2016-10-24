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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemacompiler.model.TLAbstractFacet;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemas.node.AliasNode;
import org.opentravel.schemas.node.CoreObjectNode;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.PropertyNodeType;
import org.opentravel.schemas.node.TypeProviderBase;
import org.opentravel.schemas.node.VWA_Node;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.properties.AttributeNode;
import org.opentravel.schemas.node.properties.ElementNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.properties.PropertyOwnerInterface;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.utils.StringComparator;

/**
 * Facets are containers for properties (elements and attributes) as well as simple facets for core and VWA objects.
 * Operation RQ, RS and Notif messages are also modeled with facets. (See library sorter for all the variations in facet
 * types.)
 * 
 * @author Dave Hollander
 * 
 */
public class FacetNode extends TypeProviderBase implements PropertyOwnerInterface, TypeProvider {
	// private static final Logger LOGGER = LoggerFactory.getLogger(FacetNode.class);

	public FacetNode() {
	}

	public FacetNode(final TLFacet obj) {
		super(obj);
	}

	public FacetNode(final TLModelElement obj) {
		super(obj);
	}

	@Override
	public void addProperties(List<Node> properties, boolean clone) {
		PropertyNode np;
		for (Node p : properties) {
			if (!(p instanceof PropertyNode))
				continue;
			np = (PropertyNode) p;
			if (clone)
				np = (PropertyNode) p.clone(null, null); // add to clone not parent
			if (isValidParentOf(np.getPropertyType()))
				addProperty(np);
		}
	}

	@Override
	public void addProperty(PropertyNode property) {
		super.addProperty(property);
	}

	/**
	 * Make a copy of all the properties of the source facet and add to this facet. If the property is of the wrong
	 * type, it is changed into an attribute first.
	 * 
	 * @param sourceFacet
	 */
	public void copyFacet(FacetNode sourceFacet) {
		PropertyNode newProperty = null;
		for (Node p : sourceFacet.getChildren()) {
			if (p instanceof PropertyNode) {
				PropertyNode property = (PropertyNode) p;
				newProperty = (PropertyNode) property.clone(null, null);
				if (newProperty == null)
					return; // ERROR
				this.linkChild(newProperty); // must have parent for test and change to work
				if (!this.isValidParentOf(newProperty.getPropertyType()))
					newProperty = newProperty.changePropertyRole(PropertyNodeType.ATTRIBUTE);
				modelObject.addChild(newProperty.getTLModelObject());
			}
		}
	}

	@Override
	public INode createProperty(final Node type) {
		PropertyNode pn = null;
		if (this instanceof VWA_AttributeFacetNode)
			pn = new AttributeNode(new TLAttribute(), this);
		else
			pn = new ElementNode(new TLProperty(), this);
		pn.setDescription(type.getDescription());
		if (type instanceof TypeProvider)
			pn.setAssignedType((TypeProvider) type);
		pn.setName(type.getName());
		return pn;
	}

	@Override
	public INode.CommandType getAddCommand() {
		return INode.CommandType.PROPERTY;
	}

	@Override
	public String getComponentType() {
		return modelObject.getComponentType();
	}

	@Override
	public TLFacetType getFacetType() {
		if (!(getTLModelObject() instanceof TLAbstractFacet)) {
			return null;
		}
		return ((TLAbstractFacet) getTLModelObject()).getFacetType();
	}

	@Override
	public Image getImage() {
		return Images.getImageRegistry().get(Images.Facet);
	}

	@Override
	public String getLabel() {
		String label = getModelObject().getLabel();
		if (label.indexOf("-Facet") > 0)
			label = label.substring(0, label.indexOf("-Facet"));
		return label.isEmpty() ? "" : label;
	}

	@Override
	public List<Node> getTreeChildren(boolean deep) {
		List<Node> navChildren = getNavChildren(deep);
		navChildren.addAll(getInheritedChildren());
		navChildren.add(getWhereUsedNode());
		return navChildren;
	}

	@Override
	public boolean hasTreeChildren(boolean deep) {
		return true; // where used node
	}

	@Override
	public Node getOwningComponent() {
		return isExtensionPointFacet() ? this : getParent();
	}

	/**
	 * Each access of children is sorting them based on order of MO's children.
	 */
	@Override
	public List<Node> getChildren() {
		return synchChildrenWithMO(super.getChildren());
	}

	/**
	 * Synchronize order of children with ModelObject children order.
	 * 
	 * @param children
	 * @return sorted list of children based on order of ModelObject.
	 */
	protected List<Node> synchChildrenWithMO(List<Node> children) {
		if (getModelObject() == null)
			return Collections.emptyList(); // happens during delete.
		final List<?> tlChildrenOrder = getModelObject().getChildren();
		Collections.sort(children, new Comparator<Node>() {

			@Override
			public int compare(Node o1, Node o2) {
				Integer idx1 = tlChildrenOrder.indexOf(o1.getModelObject().getTLModelObj());
				Integer idx2 = tlChildrenOrder.indexOf(o2.getModelObject().getTLModelObj());
				return idx1.compareTo(idx2);
			}
		});
		return children;
	}

	@Override
	public boolean hasChildren_TypeProviders() {
		return isXsdType() ? false : true;
	}

	// @Override
	// public boolean hasNavChildren(boolean deep) {
	// if (deep)
	// return !getChildren().isEmpty();
	// for (final Node n : getChildren()) {
	// if (n instanceof AliasNode) {
	// return true;
	// }
	// }
	// return false;
	// }

	@Override
	public boolean isAssignable() {
		if (getParent() == null)
			return false;

		return (isComplexAssignable() || isSimpleAssignable()) ? true : false;
	}

	@Override
	public boolean isAssignableToElementRef() {
		return false;
	}

	@Override
	public boolean isAssignableToSimple() {
		if (!isSimpleListFacet())
			return false;
		if (getOwningComponent() instanceof CoreObjectNode)
			if (((CoreObjectNode) getOwningComponent()).getSimpleType() != ModelNode.getEmptyNode())
				return true;
		return false;
	}

	@Override
	public boolean isAssignableToVWA() {
		return isAssignableToSimple();
	}

	@Override
	public boolean isAssignedByReference() {
		if (getOwningComponent() == null) {
			// LOGGER.equals("No owning component for this facet: " + this);
			return false;
		}
		if (isSimpleListFacet())
			return false;
		return getOwningComponent() instanceof VWA_Node ? false : true;
	}

	public boolean isComplexAssignable() {
		return true;
	}

	@Override
	public boolean isDefaultFacet() {
		if (getOwningComponent() instanceof VWA_Node)
			return true;
		return ((TLAbstractFacet) getTLModelObject()).getFacetType() == TLFacetType.SUMMARY;
	}

	@Override
	public boolean isDeleteable() {
		return false;
	}

	public boolean isDetailFacet() {
		return getFacetType() != null ? getFacetType().equals(TLFacetType.DETAIL) : false;
	}

	public boolean isIDFacet() {
		return getFacetType() != null ? getFacetType().equals(TLFacetType.ID) : false;
	}

	@Override
	public boolean isNamedType() {
		return isExtensionPointFacet() ? true : false;
	}

	/**
	 * @return true if this facet is renameable.
	 */
	public boolean isRenameable() {
		return false;
	}

	/**
	 * Facets assigned to core object list types have no model objects but may be page1-assignable.
	 */
	@Override
	public boolean isSimpleAssignable() {
		return isSimpleListFacet();
	}

	public boolean isSimpleListFacet() {
		return false;
	}

	public boolean isSummaryFacet() {
		return getFacetType() != null ? getFacetType().equals(TLFacetType.SUMMARY) : false;
	}

	@Override
	public boolean isTypeProvider() {
		if (getParent() == null)
			return false;
		return true;
	}

	@Override
	public boolean isValidParentOf(PropertyNodeType type) {
		return PropertyNodeType.getAllTypedPropertyTypes().contains(type);
	}

	/**
	 * Remove the properties in the list from this node and underlying tl model object. Use to move the property to a
	 * different facet.
	 * 
	 * @param property
	 */
	@Override
	public void removeProperty(final Node property) {
		((PropertyNode) property).removeProperty();
	}

	@Deprecated
	public void setContext(final String context) {
		if (!isEditable_newToChain())
			return;
		final Object ne = modelObject.getTLModelObj();
		if (ne instanceof TLFacet) {
			((TLFacet) ne).setContext(context);
		}
	}

	@Override
	public void setName(String n) {
	}

	@Override
	public void sort() {
		Collections.sort(getChildren(), new StringComparator<Node>() {

			@Override
			protected String getString(Node object) {
				return object.getName();
			}
		});
		modelObject.sort();
	}

	/**
	 * Add nodes for all TLAliases that do not have nodes. This ONLY adds a node for an existing TLFacet. It does not
	 * create a TLFacet.
	 * 
	 * @param name
	 */
	public void updateAliasNodes() {
		if (!(getTLModelObject() instanceof TLFacet))
			return;
		List<String> knownAliases = new ArrayList<String>();
		for (Node n : getChildren()) {
			if (n instanceof AliasNode)
				knownAliases.add(n.getName());
		}
		for (TLAlias tla : ((TLFacet) getTLModelObject()).getAliases()) {
			if (!knownAliases.contains(tla.getName())) {
				new AliasNode(this, tla);
				knownAliases.add(tla.getName());
			}
		}
	}

	/**
	 * Return true if the node is delete-able using the version and managed state information used by Node. Used by
	 * sub-types that are deletable.
	 */
	protected boolean isDeletable(boolean deletable) {
		return deletable ? super.isDeleteable() : false;
	}

	@Override
	public boolean isNavChild(boolean deep) {
		// return isAssignable();
		return true;
	}

}
