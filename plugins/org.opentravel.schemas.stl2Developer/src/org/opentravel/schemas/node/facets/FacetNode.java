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
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemas.modelObject.FacetMO;
import org.opentravel.schemas.node.AliasNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.VWA_Node;
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
public class FacetNode extends PropertyOwnerNode implements PropertyOwnerInterface, TypeProvider {
	// private static final Logger LOGGER = LoggerFactory.getLogger(FacetNode.class);

	public FacetNode() {
	}

	public FacetNode(final TLFacet obj) {
		super(obj);

		assert (modelObject instanceof FacetMO);
	}

	// // Needed for sub-classes
	// public FacetNode(final TLModelElement obj) {
	// super(obj);
	// // TLListFacet - ListFacetMO - ListFacetNode
	// // TLRoleEnumeration - RoleEnumberationMO - RoleFacetNode
	// // TLValueWithAttributesFacet - ValueWithAttributesAttributeFacetMO - VWA_AttributeFacetNode
	// // TLSimpleFacet - SimpleFacetNode
	// }

	// @Override
	// public void addProperties(List<Node> properties, boolean clone) {
	// PropertyNode np;
	// for (Node p : properties) {
	// if (!(p instanceof PropertyNode))
	// continue;
	// np = (PropertyNode) p;
	// if (clone)
	// np = (PropertyNode) p.clone(null, null); // add to clone not parent
	// if (isValidParentOf(np.getPropertyType()))
	// addProperty(np);
	// }
	// }
	//
	// @Override
	// public void addProperty(PropertyNode property) {
	// super.addProperty(property);
	// }

	// /**
	// * Make a copy of all the properties of the source facet and add to this facet. If the property is of the wrong
	// * type, it is changed into an attribute first.
	// *
	// * @param sourceFacet
	// */
	// public void copyFacet(FacetNode sourceFacet) {
	// PropertyNode newProperty = null;
	// for (Node p : sourceFacet.getChildren()) {
	// if (p instanceof PropertyNode) {
	// PropertyNode property = (PropertyNode) p;
	// newProperty = (PropertyNode) property.clone(null, null);
	// if (newProperty == null)
	// return; // ERROR
	// this.linkChild(newProperty); // must have parent for test and change to work
	// if (!this.isValidParentOf(newProperty.getPropertyType()))
	// newProperty = newProperty.changePropertyRole(PropertyNodeType.ATTRIBUTE);
	// modelObject.addChild(newProperty.getTLModelObject());
	// }
	// }
	// }
	//
	// @Override
	// public INode createProperty(final Node type) {
	// PropertyNode pn = null;
	// if (this instanceof VWA_AttributeFacetNode)
	// pn = new AttributeNode(new TLAttribute(), this);
	// else
	// pn = new ElementNode(new TLProperty(), this);
	// pn.setDescription(type.getDescription());
	// if (type instanceof TypeProvider)
	// pn.setAssignedType((TypeProvider) type);
	// pn.setName(type.getName());
	// return pn;
	// }

	// @Override
	// public INode.CommandType getAddCommand() {
	// return INode.CommandType.PROPERTY;
	// }
	//
	@Override
	public String getComponentType() {
		TLFacetType facetType = getTLModelObject().getFacetType();
		if (facetType == null)
			return "FIXME-delegate needed???";
		return facetType.getIdentityName();
	}

	@Override
	public TLFacet getTLModelObject() {
		return (TLFacet) modelObject.getTLModelObj();
	}

	@Override
	public TLFacetType getFacetType() {
		return getTLModelObject().getFacetType();
	}

	@Override
	public Image getImage() {
		return Images.getImageRegistry().get(Images.Facet);
	}

	@Override
	public String getLabel() {
		return getName();
		// String label = getModelObject().getLabel();
		// if (label.indexOf("-Facet") > 0)
		// label = label.substring(0, label.indexOf("-Facet"));
		// return label.isEmpty() ? "" : label;
	}

	// FIXME
	@Override
	public String getName() {
		return getComponentType();
		// if (modelObject == null)
		// return "";
		// // if (modelObject.getTLModelObj() instanceof TLFacet
		// // && ((TLFacet) modelObject.getTLModelObj()).getOwningEntity() == null)
		// // return "";
		// return modelObject.getName() == null ? "" : modelObject.getName();
	}

	@Override
	public boolean isDefaultFacet() {
		// FIXME - should never be VWA
		if (getOwningComponent() instanceof VWA_Node)
			return true;
		return getTLModelObject().getFacetType() == TLFacetType.SUMMARY;
	}

	public boolean isDetailFacet() {
		return getFacetType() != null ? getFacetType().equals(TLFacetType.DETAIL) : false;
	}

	public boolean isIDFacet() {
		return getFacetType() != null ? getFacetType().equals(TLFacetType.ID) : false;
	}

	@Override
	public boolean isNamedType() {
		return false;
	}

	/**
	 * Facets assigned to core object list types have no model objects but may be page1-assignable.
	 */
	@Override
	public boolean isSimpleAssignable() {
		return false;
	}

	public boolean isSummaryFacet() {
		return getFacetType() != null ? getFacetType().equals(TLFacetType.SUMMARY) : false;
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

}
