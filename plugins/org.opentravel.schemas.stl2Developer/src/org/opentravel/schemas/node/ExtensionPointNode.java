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
package org.opentravel.schemas.node;

import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLExtensionPointFacet;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemas.node.facets.SimpleFacetNode;
import org.opentravel.schemas.node.handlers.children.ExtensionPointChildrenHandler;
import org.opentravel.schemas.node.interfaces.ComplexComponentInterface;
import org.opentravel.schemas.node.interfaces.ExtensionOwner;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.properties.AttributeNode;
import org.opentravel.schemas.node.properties.ElementNode;
import org.opentravel.schemas.node.properties.IndicatorNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.properties.PropertyOwnerInterface;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.types.ExtensionHandler;
import org.opentravel.schemas.types.TypeProvider;

/**
 * Extension points are used to add properties to facets in business or core objects. They have a facet structure but
 * are not facets because they are also Named Objects.
 * 
 * @author Dave Hollander
 * 
 */
public class ExtensionPointNode extends LibraryMemberBase implements ComplexComponentInterface, ExtensionOwner,
		PropertyOwnerInterface, LibraryMemberInterface {
	// private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionPointNode.class);

	private ExtensionHandler extensionHandler = null;

	public ExtensionPointNode(TLExtensionPointFacet mbr) {
		super(mbr);

		childrenHandler = new ExtensionPointChildrenHandler(this);
		extensionHandler = new ExtensionHandler(this);
	}

	@Override
	public ExtensionPointChildrenHandler getChildrenHandler() {
		return (ExtensionPointChildrenHandler) childrenHandler;
	}

	@Override
	public boolean isDefaultFacet() {
		return false;
	}

	@Override
	public void addProperty(PropertyNode pn) {

		pn.setParent(this);

		// Add to the tl model
		addToTL(pn);

		// Events are not being thrown (10/14/2017) so force their result
		childrenHandler.clear();
	}

	public void addToTL(PropertyNode pn) {
		if (pn instanceof ElementNode)
			getTLModelObject().addElement((TLProperty) pn.getTLModelObject());
		else if (pn instanceof AttributeNode)
			getTLModelObject().addAttribute((TLAttribute) pn.getTLModelObject());
		else if (pn instanceof IndicatorNode)
			getTLModelObject().addIndicator((TLIndicator) pn.getTLModelObject());
	}

	@Override
	public void add(PropertyNode property, int i) {
		addProperty(property);
	}

	@Override
	public void addProperties(List<Node> properties, boolean clone) {
		// LOGGER.debug("addProperties not implemented for this class: " + this.getClass());
	}

	@Override
	public INode createProperty(final Node type) {
		ElementNode n = new ElementNode(this, type.getName());
		n.setDescription(type.getDescription());
		if (type instanceof TypeProvider)
			n.setAssignedType((TypeProvider) type);
		return n;
	}

	@Override
	public PropertyNode findChildByName(String name) {
		return (PropertyNode) super.findChildByName(name);
	}

	@Override
	public INode.CommandType getAddCommand() {
		return INode.CommandType.PROPERTY;
	}

	@Override
	public PropertyOwnerInterface getFacet_Attributes() {
		return null;
	}

	@Override
	public ComponentNodeType getComponentNodeType() {
		return ComponentNodeType.EXTENSION_POINT;
	}

	/**
	 * @return null or the default facet for the complex object
	 */
	@Override
	public PropertyOwnerInterface getFacet_Default() {
		return this;
	}

	@Override
	public Image getImage() {
		return Images.getImageRegistry().get(Images.Facet);
	}

	@Override
	public String getLabel() {
		return getName();
	}

	@Override
	public String getName() {
		return getTLModelObject() == null || getTLModelObject().getLocalName() == null
				|| getTLModelObject().getLocalName().isEmpty() ? "-not assigned-" : getTLModelObject().getLocalName();
	}

	@Override
	public LibraryMemberInterface getOwningComponent() {
		return this;
	}

	@Override
	public TLExtensionPointFacet getTLModelObject() {
		return (TLExtensionPointFacet) tlObj;
		// return (TLExtensionPointFacet) (modelObject != null ? modelObject.getTLModelObj() : null);
	}

	// added 9/7/2015 - was not removing properties from newPropertyWizard
	@Override
	public void removeProperty(final Node property) {
		((PropertyNode) property).removeProperty();
	}

	protected Node newElementProperty() {
		ElementNode n = new ElementNode(new TLProperty(), this);
		return n;
	}

	@Override
	public SimpleFacetNode getFacet_Simple() {
		return (SimpleFacetNode) super.getFacet_Simple();
	}

	@Override
	public boolean isEnabled_AddProperties() {
		return isEditable_newToChain();
	}

	// @Override
	// public boolean isNamedType() {
	// return true;
	// }

	@Override
	public boolean isDeleteable() {
		if (isInherited())
			return false; // I don't think it can be inherited
		return getLibrary() != null ? getLibrary().isEditable() && isInHead2() : false;
	}

	// Gets name from where it extends
	@Override
	public boolean isRenameable() {
		return false;
	}

	// /////////////////////////////////////////////////////////////////
	//
	// Extension Owner implementations
	//
	@Override
	public Node getExtensionBase() {
		return extensionHandler != null ? extensionHandler.get() : null;
	}

	@Override
	public String getExtendsTypeNS() {
		return getExtensionBase() != null ? getExtensionBase().getNamespace() : "";
	}

	/**
	 * Validation constraint: The extension point facet MUST be assigned to a different namespace than the extended term
	 * or named entity
	 */
	@Override
	public void setExtension(final Node base) {
		assert getLibrary() != null;
		assert getLibrary() != base.getLibrary();

		if (extensionHandler == null)
			extensionHandler = new ExtensionHandler(this);
		extensionHandler.set(base);
	}

	@Override
	public ExtensionHandler getExtensionHandler() {
		return extensionHandler;
	}

	@Override
	public TLFacetType getFacetType() {
		return null;
	}

	@Override
	public String getComponentType() {
		// TLFacetType facetType = getTLModelObject().getFacetType();
		return ComponentNodeType.EXTENSION_POINT.getDescription();
	}

}
