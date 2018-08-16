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
package org.opentravel.schemas.node.typeProviders;

import java.util.Collections;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.ComponentNodeType;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeNameUtils;
import org.opentravel.schemas.node.facets.AttributeFacetNode;
import org.opentravel.schemas.node.handlers.children.ValueWithAttributesChildrenHandler;
import org.opentravel.schemas.node.interfaces.ExtensionOwner;
import org.opentravel.schemas.node.interfaces.FacetInterface;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.interfaces.Sortable;
import org.opentravel.schemas.node.interfaces.VersionedObjectInterface;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.objectMembers.VWA_SimpleFacetFacadeNode;
import org.opentravel.schemas.node.properties.IValueWithContextHandler;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.properties.SimpleAttributeFacadeNode;
import org.opentravel.schemas.node.typeProviders.facetOwners.BusinessObjectNode;
import org.opentravel.schemas.node.typeProviders.facetOwners.CoreObjectNode;
import org.opentravel.schemas.node.typeProviders.facetOwners.FacetOwners;
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
public class VWA_Node extends FacetOwners
		implements ExtensionOwner, Sortable, VersionedObjectInterface, SimpleAttributeOwner {
	// private static final Logger LOGGER = LoggerFactory.getLogger(VWA_Node.class);

	private ExtensionHandler extensionHandler = null;
	private LibraryNode owningLibrary;

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
		if (bo == null)
			return;

		setName(bo.getName());
		bo.getLibrary().addMember(this);
		setDocumentation(bo.getDocumentation());

		if (bo.isDeleted())
			return;

		getFacet_Attributes().add(bo.getFacet_ID().getProperties(), true);
		getFacet_Attributes().add(bo.getFacet_Summary().getProperties(), true);
		getFacet_Attributes().add(bo.getFacet_Detail().getProperties(), true);

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
		if (core == null)
			return;

		setName(core.getName());
		core.getLibrary().addMember(this);
		setDocumentation(core.getDocumentation());

		if (core.isDeleted())
			return;

		getFacet_Attributes().add(core.getFacet_Summary().getProperties(), true);
		getFacet_Attributes().add(core.getFacet_Detail().getProperties(), true);
		setAssignedType(core.getAssignedType());
	}

	@Override
	public boolean canOwn(TLFacetType type) {
		return false;
	}

	@Override
	public LibraryNode getLibrary() {
		return owningLibrary;
	}

	@Override
	public void setLibrary(LibraryNode library) {
		owningLibrary = library;
	}

	@Override
	public ComponentNode createMinorVersionComponent() {
		TLValueWithAttributes tlMinor = (TLValueWithAttributes) createMinorTLVersion(this);
		if (tlMinor != null)
			return super.createMinorVersionComponent(new VWA_Node(tlMinor));
		return null;
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
	public VWA_SimpleFacetFacadeNode getFacet_Simple() {
		for (INode f : getChildren()) {
			if (f instanceof VWA_SimpleFacetFacadeNode)
				return (VWA_SimpleFacetFacadeNode) f;
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
	public FacetProviderNode getFacet_Default() {
		return null;
		// return getFacet_Attributes();
	}

	@Override
	public INode.CommandType getAddCommand() {
		return INode.CommandType.PROPERTY;
	}

	@Override
	public AttributeFacetNode getFacet_Attributes() {
		for (Node f : getChildren())
			if (f instanceof AttributeFacetNode)
				return (AttributeFacetNode) f;
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
	public TypeProvider setAssignedType(TypeProvider provider) {
		return getSimpleAttribute().setAssignedType(provider);
	}

	@Override
	public SimpleAttributeFacadeNode getSimpleAttribute() {
		return getFacet_Simple() != null ? getFacet_Simple().getSimpleAttribute() : null;
	}

	@Override
	public void sort() {
		getFacet_Attributes().sort();
	}

	@Override
	public void merge(Node source) {
		if (!(source instanceof VWA_Node)) {
			throw new IllegalStateException("Can only merge objects with the same type");
		}
		VWA_Node vwa = (VWA_Node) source;
		getFacet_Attributes().add(vwa.getFacet_Attributes().getProperties(), true);
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

	/**
	 * Get the type assigned to the simple attribute
	 */
	@Override
	public TypeProvider getAssignedType() {
		return getSimpleAttribute().getAssignedType();
	}

	@Override
	public List<AliasNode> getAliases() {
		return Collections.emptyList();
	}

	@Override
	public FacetProviderNode getFacet_Summary() {
		return null;
	}

	@Override
	public FacetProviderNode getFacet_Detail() {
		return null;
	}

	@Override
	public FacetProviderNode getFacet_ID() {
		return null;
	}

	@Override
	public FacetInterface getFacet(TLFacetType facetType) {
		if (facetType == TLFacetType.SIMPLE)
			return getFacet_Simple();
		return null;
	}

	/**
	 * @return
	 */
	public List<PropertyNode> getAttributes() {
		return getFacet_Attributes().getProperties();
	}

}
