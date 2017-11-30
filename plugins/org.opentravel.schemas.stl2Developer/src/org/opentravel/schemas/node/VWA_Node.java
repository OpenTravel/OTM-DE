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

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemas.node.facets.AttributeFacetNode;
import org.opentravel.schemas.node.facets.FacetNode;
import org.opentravel.schemas.node.facets.PropertyOwnerNode;
import org.opentravel.schemas.node.facets.SimpleFacetFacadeNode;
import org.opentravel.schemas.node.handlers.children.ValueWithAttributesChildrenHandler;
import org.opentravel.schemas.node.interfaces.ComplexComponentInterface;
import org.opentravel.schemas.node.interfaces.ExtensionOwner;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.interfaces.Sortable;
import org.opentravel.schemas.node.interfaces.VersionedObjectInterface;
import org.opentravel.schemas.node.properties.IValueWithContextHandler;
import org.opentravel.schemas.node.properties.SimpleAttributeFacadeNode;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.types.ExtensionHandler;
import org.opentravel.schemas.types.SimpleAttributeOwner;
import org.opentravel.schemas.types.TypeProvider;

/**
 * Value With Attributes. This object has two facets, the value is a simple facet and attributes facet. This makes it a
 * complex interface implementation.
 * <p>
 * Type Users under a VWA can only be assigned simple, vwa or enumeration (open or closed) objects.
 * <p>
 * Structure: <br>
 * VWA_SimpleFacetFacadeNode -> VWA_SimpleAttributeFacadeNode (tlParent) <br>
 * AttributeFacetNode -> AttributeNode(s) (tlAttribute)
 * 
 * @author Dave Hollander
 * 
 */
public class VWA_Node extends LibraryMemberBase implements ComplexComponentInterface, ExtensionOwner, Sortable,
		VersionedObjectInterface, LibraryMemberInterface, SimpleAttributeOwner {
	// private static final Logger LOGGER = LoggerFactory.getLogger(VWA_Node.class);

	private ExtensionHandler extensionHandler = null;

	public VWA_Node(TLValueWithAttributes mbr) {
		super(mbr);
		childrenHandler = new ValueWithAttributesChildrenHandler(this);
	}

	/**
	 * Create a new VWA named the BO name and add to BO's library. Copy all documentation. Make an attribute property
	 * from each property in the ID, Summary and Detail facets.
	 * 
	 * @param bo
	 *            is the BusinessObject to copy from
	 */
	public VWA_Node(BusinessObjectNode bo) {
		this(new TLValueWithAttributes());

		setName(bo.getName());
		bo.getLibrary().addMember(this);
		setDocumentation(bo.getDocumentation());

		getFacet_Attributes().copyFacet(bo.getFacet_ID());
		getFacet_Attributes().copyFacet(bo.getFacet_Summary());
		getFacet_Attributes().copyFacet(bo.getFacet_Detail());
		setAssignedType((TypeProvider) ModelNode.getEmptyNode());
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

		getFacet_Attributes().copyFacet(core.getFacet_Summary());
		getFacet_Attributes().copyFacet(core.getFacet_Detail());
		setAssignedType(core.getAssignedType());
	}

	@Override
	public ComponentNode createMinorVersionComponent() {
		return super.createMinorVersionComponent(new VWA_Node((TLValueWithAttributes) createMinorTLVersion(this)));
	}

	@Override
	public String getName() {
		return getTLModelObject().getName();
	}

	@Override
	public TLValueWithAttributes getTLModelObject() {
		return (TLValueWithAttributes) tlObj;
	}

	@Override
	public boolean isNamedEntity() {
		return true;
	}

	@Override
	public boolean isSimpleAssignable() {
		return true;
	}

	@Override
	public boolean isRenameableWhereUsed() {
		return true;
	}

	@Override
	public Image getImage() {
		return Images.getImageRegistry().get(Images.ValueWithAttr);
	}

	@Override
	public SimpleFacetFacadeNode getFacet_Simple() {
		for (INode f : getChildren()) {
			if (f instanceof SimpleFacetFacadeNode)
				return (SimpleFacetFacadeNode) f;
		}
		return null;
	}

	@Override
	public Node getSimpleProperty() {
		return getFacet_Simple().getChildren().get(0);
	}

	@Override
	public ValueWithAttributesChildrenHandler getChildrenHandler() {
		return (ValueWithAttributesChildrenHandler) childrenHandler;
	}

	@Override
	public ComponentNodeType getComponentNodeType() {
		return ComponentNodeType.VWA;
	}

	@Override
	public PropertyOwnerNode getFacet_Default() {
		return getFacet_Attributes();
	}

	@Override
	public INode.CommandType getAddCommand() {
		return INode.CommandType.PROPERTY;
	}

	// FIXME - return AttributeFacetNode
	@Override
	public PropertyOwnerNode getFacet_Attributes() {
		for (Node f : getChildren())
			if (f instanceof AttributeFacetNode)
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

	/**
	 * Set the type assigned to the simple attribute in the simple facet.
	 */
	@Override
	public boolean setAssignedType(TypeProvider provider) {
		return getSimpleAttribute().setAssignedType(provider);
	}

	@Override
	public SimpleAttributeFacadeNode getSimpleAttribute() {
		return getFacet_Simple().getSimpleAttribute();
	}

	@Override
	public void sort() {
		((FacetNode) getFacet_Attributes()).sort();
	}

	@Override
	public void merge(Node source) {
		if (!(source instanceof VWA_Node)) {
			throw new IllegalStateException("Can only merge objects with the same type");
		}
		VWA_Node vwa = (VWA_Node) source;
		getFacet_Attributes().addProperties(vwa.getFacet_Attributes().getChildren(), true);
		getChildrenHandler().clear();
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

	@Override
	public String getExtendsTypeNS() {
		return getExtensionBase() != null ? getExtensionBase().getNamespace() : "";
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

	@Override
	public TypeProvider getAssignedType() {
		return getSimpleAttribute().getAssignedType();
	}

}
