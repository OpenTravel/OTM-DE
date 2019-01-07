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
package org.opentravel.schemas.node.objectMembers;

import java.util.Collections;
import java.util.List;

import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.handlers.children.VWA_SimpleFacetFacadeChildrenHandler;
import org.opentravel.schemas.node.interfaces.FacetInterface;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.listeners.BaseNodeListener;
import org.opentravel.schemas.node.listeners.SimpleFacetNodeListener;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.properties.VWA_SimpleAttributeFacadeNode;
import org.opentravel.schemas.node.typeProviders.VWA_Node;
import org.opentravel.schemas.trees.type.TypeSelectionFilter;
import org.opentravel.schemas.trees.type.TypeTreeSimpleAssignableOnlyFilter;
import org.opentravel.schemas.types.TypeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple facets have only one attribute property. Used on ValueWithAtributes and Core Objects.
 * 
 * @author Dave Hollander
 * 
 */
public class VWA_SimpleFacetFacadeNode extends FacadeBase implements FacetInterface {
	private static final Logger LOGGER = LoggerFactory.getLogger(VWA_SimpleFacetFacadeNode.class);
	private VWA_Node owner;

	public VWA_SimpleFacetFacadeNode(VWA_Node owner) {
		super();
		parent = owner;
		this.owner = owner;
		childrenHandler = new VWA_SimpleFacetFacadeChildrenHandler(this);
	}

	@Override
	public VWA_Node getOwningComponent() {
		return getParent();
	}

	public void addProperty(final Node property, int index) {
	}

	@Override
	public boolean isExtensionPointTarget() {
		return false;
	}

	/**
	 * Create and return a facade node for the correct simple attribute.
	 */
	public VWA_SimpleAttributeFacadeNode createAttributeFacade() {
		return new VWA_SimpleAttributeFacadeNode(this);
	}

	@Override
	public PropertyNode createProperty(final Node sn) {
		return null;
	}

	@Override
	public VWA_Node get() {
		return owner;
	}

	@Override
	public Node getAssignable() {
		return getChildren().get(0);
	}

	public NamedEntity getAssignedTLNamedEntity() {
		return getTLModelObject() != null ? getTLModelObject().getParentType() : null;
	}

	// Use type assigned to simple attribute
	@Deprecated
	public TypeProvider getAssignedType() {
		return getSimpleAttribute().getAssignedType();
	}

	@Override
	public String getComponentType() {
		return getFacetType().getIdentityName();
	}

	@Override
	public TLFacetType getFacetType() {
		return TLFacetType.SIMPLE;
	}

	@Override
	public String getLabel() {
		return "VALUE";
		// return getFacetType().toString();
	}

	@Override
	public LibraryNode getLibrary() {
		return getParent().getLibrary();
	}

	@Override
	public String getName() {
		return emptyIfNull(getTLModelObject().getLocalName());
	}

	@Override
	public BaseNodeListener getNewListener() {
		return new SimpleFacetNodeListener(this);
	}

	@Override
	public VWA_Node getParent() {
		return (VWA_Node) parent;
	}

	/**
	 * This maps to the TLVWA parent.
	 */
	// @Override
	public VWA_SimpleAttributeFacadeNode getSimpleAttribute() {
		if (getChildren().isEmpty())
			return null;
		return (VWA_SimpleAttributeFacadeNode) getChildren().get(0);
		// return getChildren().isEmpty() ? ModelNode.getEmptyType() : (SimpleAttributeFacadeNode) getChildren().get(0);
	}

	@Override
	public TLValueWithAttributes getTLModelObject() {
		return get().getTLModelObject();
	}

	@Override
	public boolean hasNavChildren(boolean deep) {
		return deep; // only show attribute in deep mode
	}

	@Override
	public boolean isAssignedByReference() {
		// must override since super-type facet will return true.
		return false;
	}

	@Override
	public boolean isDeleteable() {
		return false;
	}

	@Override
	public boolean isEnabled_AddProperties() {
		return false;
	}

	@Override
	public boolean isNavChild(boolean deep) {
		return deep;
	}

	@Override
	public TypeSelectionFilter getTypeSelectionFilter() {
		return new TypeTreeSimpleAssignableOnlyFilter();
	}

	// @Override
	// public boolean isOnlySimpleTypeUser() {
	// // 6/21/2013 - this was true in Node.java. I dont think it should be.
	// return false;
	// }

	/**
	 * Facets assigned to core object list types have no model objects but may be page1-assignable.
	 */
	// FIXME - how can this be true but not isAssignable() ??
	@Override
	public boolean isSimpleAssignable() {
		return true;
	}

	@Override
	public void removeProperty(Node property) {
		LOGGER.debug("removeProperty() - Setting simple facet " + getName() + " to empty.");
		getSimpleAttribute().setAssignedType();
	}

	// @Override
	@Deprecated
	public TypeProvider setAssignedType(TypeProvider provider) {
		return getSimpleAttribute().setAssignedType(provider);
	}

	@Override
	public void copy(FacetInterface facet) {
		// NO-OP
	}

	@Override
	public void add(List<PropertyNode> properties, boolean clone) {
		// NO-OP
	}

	@Override
	public void add(PropertyNode property) {
		// NO-OP
	}

	@Override
	public void add(PropertyNode pn, int index) {
		// NO-OP
	}

	@Override
	public PropertyNode findChildByName(String name) {
		return getSimpleAttribute();
	}

	@Override
	public PropertyNode get(String name) {
		return getSimpleAttribute();
	}

	@Override
	public List<PropertyNode> getProperties() {
		return Collections.singletonList((PropertyNode) getSimpleAttribute());
	}

	@Override
	public boolean isFacet(TLFacetType type) {
		return type.equals(TLFacetType.SIMPLE);
	}

	@Override
	public void removeProperty(PropertyNode pn) {
		// NO-OP
	}

}
