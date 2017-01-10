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
package org.opentravel.schemas.node;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemas.modelObject.ValueWithAttributesMO;
import org.opentravel.schemas.node.facets.FacetNode;
import org.opentravel.schemas.node.facets.PropertyOwnerNode;
import org.opentravel.schemas.node.facets.SimpleFacetNode;
import org.opentravel.schemas.node.facets.VWA_AttributeFacetNode;
import org.opentravel.schemas.node.interfaces.ComplexComponentInterface;
import org.opentravel.schemas.node.interfaces.ExtensionOwner;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.interfaces.VersionedObjectInterface;
import org.opentravel.schemas.node.properties.IValueWithContextHandler;
import org.opentravel.schemas.node.properties.SimpleAttributeNode;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.types.ExtensionHandler;
import org.opentravel.schemas.types.SimpleAttributeOwner;
import org.opentravel.schemas.types.TypeProvider;

/**
 * Value With Attributes. This object has two facets, the value is a simple facet and attributes facet. This makes it a
 * complex interface implementation.
 * 
 * Type Users under a VWA can only be assigned simple, vwa or enumeration (open or closed) objects.
 * 
 * @author Dave Hollander
 * 
 */
public class VWA_Node extends TypeProviderBase implements ComplexComponentInterface, ExtensionOwner,
		VersionedObjectInterface, LibraryMemberInterface, SimpleAttributeOwner {
	// private static final Logger LOGGER = LoggerFactory.getLogger(VWA_Node.class);
	private ExtensionHandler extensionHandler = null;

	public VWA_Node(TLValueWithAttributes mbr) {
		super(mbr);
		addMOChildren();

		// Make the parent of the simple attribute this tl object.
		// Parent is needed for parent type and eq/ex
		getSimpleAttribute().getTLModelObject().setParentObject(mbr);

		// ParentType is be type assigned to the value facet
		if (getTLModelObject().getParentType() == null)
			setSimpleType((TypeProvider) ModelNode.getEmptyNode());

		assert (modelObject instanceof ValueWithAttributesMO);
	}

	/**
	 * Create a new VWA named the BO name. Copy all documentation. Make an attribute property from each property in the
	 * ID, Summary and Detail facets.
	 * 
	 * @param bo
	 *            is the BusinessObject to copy from
	 */
	public VWA_Node(BusinessObjectNode bo) {
		this(new TLValueWithAttributes());

		setName(bo.getName());
		bo.getLibrary().addMember(this);
		setDocumentation(bo.getDocumentation());

		getAttributeFacet().copyFacet(bo.getIDFacet());
		getAttributeFacet().copyFacet(bo.getSummaryFacet());
		getAttributeFacet().copyFacet(bo.getDetailFacet());
		setSimpleType((TypeProvider) ModelNode.getEmptyNode());
	}

	/**
	 * Create a new VWA named the Core name. Copy all documentation. Make an attribute property from each property in
	 * the Summary and Detail facets. Assign simple type to be core simple type. Add to same library as the core object.
	 * 
	 * @param core
	 *            is the Core Object to copy from.
	 */
	public VWA_Node(CoreObjectNode core) {
		this(new TLValueWithAttributes());

		setName(core.getName());
		core.getLibrary().addMember(this);
		setDocumentation(core.getDocumentation());

		getAttributeFacet().copyFacet(core.getSummaryFacet());
		getAttributeFacet().copyFacet(core.getDetailFacet());
		setSimpleType(core.getSimpleType());
	}

	@Override
	public ComponentNode createMinorVersionComponent() {
		return super.createMinorVersionComponent(new VWA_Node((TLValueWithAttributes) createMinorTLVersion(this)));
	}

	// /**
	// * Return the parent type.
	// */
	// @Override
	// public NamedEntity getTLTypeObject() {
	// return getTLModelObject().getParentType();
	// }

	@Override
	public String getName() {
		return getTLModelObject().getName();
	}

	@Override
	public TLValueWithAttributes getTLModelObject() {
		return (TLValueWithAttributes) modelObject.getTLModelObj();
	}

	@Override
	public boolean isNamedType() {
		return true;
	}

	@Override
	public boolean isNamedEntity() {
		return true;
	}

	@Override
	public List<Node> getChildren_TypeUsers() {
		ArrayList<Node> users = new ArrayList<Node>();
		users.add(getSimpleFacet().getSimpleAttribute());
		users.addAll(getAttributeFacet().getChildren());
		return users;
	}

	// @Override
	// public String getLabel() {
	// if (isVersioned())
	// return super.getLabel() + " (Extends version:  " + getExtensionBase().getLibrary().getVersion() + ")";
	// else
	// return super.getLabel();
	// }

	@Override
	public Image getImage() {
		return Images.getImageRegistry().get(Images.ValueWithAttr);
	}

	@Override
	public SimpleFacetNode getSimpleFacet() {
		for (INode f : getChildren()) {
			if (f instanceof SimpleFacetNode)
				return (SimpleFacetNode) f;
		}
		return null;
	}

	@Override
	public Node getSimpleProperty() {
		return getSimpleFacet().getChildren().get(0);
	}

	@Override
	public ComponentNodeType getComponentNodeType() {
		return ComponentNodeType.VWA;
	}

	@Override
	public PropertyOwnerNode getDefaultFacet() {
		return (PropertyOwnerNode) getAttributeFacet();
	}

	@Override
	public INode.CommandType getAddCommand() {
		return INode.CommandType.PROPERTY;
	}

	@Override
	public PropertyOwnerNode getAttributeFacet() {
		for (INode f : getChildren())
			if (f instanceof VWA_AttributeFacetNode)
				return (PropertyOwnerNode) f;
		return null;
	}

	// VWA is NOT a TypeUser
	@Override
	public Node getType() {
		return null; // Does NOT have type ... simple attribute does
	}

	@Override
	public void setName(String name) {
		getTLModelObject().setName(NodeNameUtils.fixVWAName(name));
	}

	// /////////////////////////////////////////////////////////////////
	//
	// Simple Attribute Owner implementations
	//
	@Override
	public TypeProvider getSimpleType() {
		return getSimpleAttribute().getAssignedType();
	}

	@Override
	public boolean setSimpleType(TypeProvider provider) {
		return getSimpleAttribute().setAssignedType(provider);
	}

	@Override
	public SimpleAttributeNode getSimpleAttribute() {
		return getSimpleFacet().getSimpleAttribute();
	}

	// // 10/5/2015 - was commented out
	// /**
	// * Override to provide type from simple facet.
	// */
	@Override
	public void sort() {
		((FacetNode) getAttributeFacet()).sort();
	}

	@Override
	public void merge(Node source) {
		if (!(source instanceof VWA_Node)) {
			throw new IllegalStateException("Can only merge objects with the same type");
		}
		VWA_Node vwa = (VWA_Node) source;
		getAttributeFacet().addProperties(vwa.getAttributeFacet().getChildren(), true);
	}

	@Override
	public boolean isAssignableToVWA() {
		return true;
	}

	@Override
	public boolean isMergeSupported() {
		return true;
	}

	@Override
	public boolean isAssignableToSimple() {
		return false;
	}

	@Override
	public boolean isAssignableToElementRef() {
		return false;
	}

	// TL ValueWithAttributes is direct owner of example and equivalents, not the value facet
	@Override
	public String getEquivalent(final String context) {
		return getSimpleAttribute().getEquivalent(context);
	}

	/**
	 * @return equivalent handler from simple attribute
	 */
	@Override
	public IValueWithContextHandler getEquivalentHandler() {
		return getSimpleAttribute().getEquivalentHandler();
	}

	/**
	 * @return example handler from simple attribute
	 */
	@Override
	public IValueWithContextHandler getExampleHandler() {
		return getSimpleAttribute().getExampleHandler();
	}

	@Override
	public String getExample(final String context) {
		return getSimpleAttribute().getExample(context);
	}

	public void setEquivalent(final String value) {
		getSimpleAttribute().setEquivalent(value);
	}

	public void setExample(final String value) {
		getSimpleAttribute().setExample(value);
	}

	// /////////////////////////////////////////////////////////////////
	//
	// Extension Owner implementations
	//
	@Override
	public Node getExtensionBase() {
		return extensionHandler != null ? extensionHandler.get() : null;
	}

	public String getExtendsTypeNS() {
		return modelObject.getExtendsTypeNS();
	}

	@Override
	public void setExtension(final Node base) {
		if (extensionHandler == null)
			extensionHandler = new ExtensionHandler(this);
		extensionHandler.set(base);
	}

	@Override
	public ExtensionHandler getExtensionHandler() {
		return extensionHandler;
	}

}
