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
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLExtensionPointFacet;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemas.node.properties.ElementNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.properties.PropertyOwnerInterface;
import org.opentravel.schemas.properties.Images;

/**
 * Extension points are used to add properties to facets in business or core objects. They have a facet structure but
 * are not facets because they are also Named Objects.
 * 
 * @author Dave Hollander
 * 
 */
public class ExtensionPointNode extends ComponentNode implements ComplexComponentInterface, ExtensionOwner,
		PropertyOwnerInterface {
	// private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionPointNode.class);

	public ExtensionPointNode(LibraryMember mbr) {
		super(mbr);
		addMOChildren();
	}

	@Override
	public void addProperty(PropertyNode property) {
		super.addProperty(property);
	}

	@Override
	public void addProperties(List<Node> properties, boolean clone) {
		// LOGGER.debug("addProperties not implemented for this class: " + this.getClass());
	}

	@Override
	public boolean canExtend() {
		return true;
	}

	@Override
	public INode createProperty(final Node type) {
		Node n = new ElementNode(this, type.getName());
		n.setDescription(type.getDescription());
		// linkChild(n, nodeIndexOf());
		n.setAssignedType(type);
		return n;
	}

	@Override
	public INode.CommandType getAddCommand() {
		return INode.CommandType.PROPERTY;
	}

	@Override
	public PropertyOwnerInterface getAttributeFacet() {
		return null;
	}

	/**
	 * @return null or the default facet for the complex object
	 */
	@Override
	public PropertyOwnerInterface getDefaultFacet() {
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
		String name = "unnamed";
		// Could be TLEmpty
		if ((getTLModelObject() != null) && (getTLModelObject() instanceof TLExtensionPointFacet))
			name = ((TLExtensionPointFacet) getTLModelObject()).getLocalName();
		if (name == null)
			name = "unnamed";
		String prefix = "ExtensionPoint_";
		if (name.startsWith(prefix))
			name = name.substring(prefix.length(), name.length());
		return name + "_ExtensionPoint";
	}

	// added 9/7/2015 - was not removing properties from newPropertyWizard
	@Override
	public void removeProperty(final Node property) {
		((PropertyNode) property).removeProperty();
	}

	@Override
	public boolean setSimpleType(Node type) {
		return false;
	}

	@Override
	public Node getExtendsType() {
		return getTypeClass().getTypeNode();
	}

	@Override
	public void setExtendsType(final INode sourceNode) {
		assert (sourceNode.getLibrary() != getLibrary());
		getTypeClass().setAssignedBaseType(sourceNode);
		setIdentity("EP-->" + sourceNode);
	}

	@Override
	public boolean isExtensionPointFacet() {
		return true;
	}

	protected Node newElementProperty() {
		ElementNode n = new ElementNode(new TLProperty(), this);
		return n;
	}

	@Override
	public ComponentNode getSimpleType() {
		return null;
	}

	@Override
	public SimpleFacetNode getSimpleFacet() {
		return (SimpleFacetNode) super.getSimpleFacet();
	}

	@Override
	public boolean isNamedType() {
		return true;
	}

}
