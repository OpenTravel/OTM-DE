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
package org.opentravel.schemas.node.typeProviders.facetOwners;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.ComponentNodeType;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeNameUtils;
import org.opentravel.schemas.node.handlers.children.CoreObjectChildrenHandler;
import org.opentravel.schemas.node.interfaces.AliasOwner;
import org.opentravel.schemas.node.interfaces.ExtensionOwner;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.interfaces.Sortable;
import org.opentravel.schemas.node.interfaces.VersionedObjectInterface;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.objectMembers.FacetOMNode;
import org.opentravel.schemas.node.properties.AttributeNode;
import org.opentravel.schemas.node.properties.SimpleAttributeFacadeNode;
import org.opentravel.schemas.node.typeProviders.AliasNode;
import org.opentravel.schemas.node.typeProviders.CoreSimpleFacetNode;
import org.opentravel.schemas.node.typeProviders.FacetProviderNode;
import org.opentravel.schemas.node.typeProviders.ListFacetNode;
import org.opentravel.schemas.node.typeProviders.RoleFacetNode;
import org.opentravel.schemas.node.typeProviders.VWA_Node;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.types.ExtensionHandler;
import org.opentravel.schemas.types.SimpleAttributeOwner;
import org.opentravel.schemas.types.TypeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Core Object. This object has many facets: simple, summary, detail, roles and two lists. It implements the complex
 * component interface.
 * 
 * @author Dave Hollander
 * 
 */
public class CoreObjectNode extends FacetOwners
		implements ExtensionOwner, Sortable, AliasOwner, VersionedObjectInterface, TypeProvider, SimpleAttributeOwner {
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CoreObjectNode.class);
	private ExtensionHandler extensionHandler = null;
	private LibraryNode owningLibrary;

	public CoreObjectNode(TLCoreObject mbr) {
		super(mbr);
		// getName() on child facets will fail if null name
		if (getTLModelObject().getName() == null)
			getTLModelObject().setName("");

		// If the simple facet is null, make it Empty
		if (mbr.getSimpleFacet().getSimpleType() == null)
			mbr.getSimpleFacet().setSimpleType(ModelNode.getEmptyType());

		childrenHandler = new CoreObjectChildrenHandler(this);
		extensionHandler = new ExtensionHandler(this);
	}

	/**
	 * Create new core with same name and documentation as the business object. Copy all ID and summary facet properties
	 * in to the summary facet. Copy all detail properties into detail facet.
	 * 
	 * Note: this core is added to the same library as the business object creating a validation error.
	 * 
	 * @param bo
	 */
	public CoreObjectNode(BusinessObjectNode bo) {
		this(new TLCoreObject());
		if (bo == null)
			return;

		addAliases(bo.getAliases());

		setName(bo.getName());
		bo.getLibrary().addMember(this);
		setDocumentation(bo.getDocumentation());

		if (bo.isDeleted())
			return;

		getFacet_Summary().add(bo.getFacet_ID().getProperties(), true);
		getFacet_Summary().add(bo.getFacet_Summary().getProperties(), true);
		getFacet_Detail().add(bo.getFacet_Detail().getProperties(), true);

		setAssignedType((TypeProvider) ModelNode.getEmptyNode());
	}

	/**
	 * Add to VWA's library a new core with a copy of all the VWA attributes in the summary facet.
	 * 
	 * @param vwa
	 */
	public CoreObjectNode(VWA_Node vwa) {
		this(new TLCoreObject());
		if (vwa == null)
			return;
		setName(vwa.getName());
		vwa.getLibrary().addMember(this);
		setDocumentation(vwa.getDocumentation());

		if (vwa.isDeleted())
			return;

		getFacet_Summary().copy(vwa.getFacet_Attributes());
		setAssignedType(vwa.getAssignedType());

		// User assist - create an attribute for the VWA base type
		AttributeNode attr = new AttributeNode(new TLAttribute(), getFacet_Summary());
		attr.setName(vwa.getName());
		attr.setAssignedType(vwa.getAssignedType());
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
	public void remove(AliasNode alias) {
		getTLModelObject().removeAlias(alias.getTLModelObject());
		getChildrenHandler().remove(alias);
		clearAllAliasOwners();
	}

	@Override
	public void addAlias(AliasNode alias) {
		if (alias == null)
			return;
		List<TLAlias> tlAliases = getTLModelObject().getAliases();
		if (tlAliases != null && !tlAliases.contains(alias.getTLModelObject()))
			getTLModelObject().addAlias(alias.getTLModelObject());
		// Could be during child handler initialization
		if (getChildrenHandler() != null)
			getChildrenHandler().add(alias);
		clearAllAliasOwners();
	}

	@Override
	public AliasNode addAlias(String name) {
		AliasNode alias = null;
		if (this.isEditable_newToChain())
			alias = new AliasNode(this, NodeNameUtils.fixCoreObjectName(name));
		addAlias(alias);
		clearAllAliasOwners();
		return alias;
	}

	// Should this be handled by Listeners?
	private void clearAllAliasOwners() {
		for (Node child : getChildren())
			if (child.getChildrenHandler() != null)
				child.getChildrenHandler().clear();
	}

	@Override
	public void cloneAliases(List<AliasNode> aliases) {
		for (AliasNode a : aliases)
			addAlias(a.getName());
	}

	@Override
	public CoreObjectChildrenHandler getChildrenHandler() {
		return (CoreObjectChildrenHandler) childrenHandler;
	}

	private void addAliases(List<AliasNode> aliases) {
		for (AliasNode a : aliases)
			addAlias(a.getName());
	}

	@Override
	public ComponentNode createMinorVersionComponent() {
		TLCoreObject tlMinor = (TLCoreObject) createMinorTLVersion(this);
		if (tlMinor != null)
			return super.createMinorVersionComponent(new CoreObjectNode(tlMinor));
		return null;
	}

	@Override
	public boolean isExtensibleObject() {
		return true;
	}

	@Override
	public Node setExtensible(boolean extensible) {
		if (isEditable_newToChain())
			getTLModelObject().setNotExtendable(!extensible);
		return this;
	}

	@Override
	public String getName() {
		return getTLModelObject() == null || getTLModelObject().getName() == null ? "" : getTLModelObject().getName();
	}

	@Override
	public TLCoreObject getTLModelObject() {
		return (TLCoreObject) tlObj;
	}

	@Override
	public ComponentNodeType getComponentNodeType() {
		return ComponentNodeType.CORE;
	}

	// /////////////////////////////////////////////////////////////////
	//
	// Simple Attribute Owner implementations
	//
	@Override
	public TypeProvider getAssignedType() {
		return getSimpleAttribute().getAssignedType();
	}

	@Override
	public TypeProvider setAssignedType(TypeProvider type) {
		return getSimpleAttribute().setAssignedType(type);
	}

	@Override
	public SimpleAttributeFacadeNode getSimpleAttribute() {
		return getFacet_Simple().getSimpleAttribute();
	}

	@Override
	public CoreSimpleFacetNode getFacet_Simple() {
		for (INode f : getChildren())
			if (f instanceof CoreSimpleFacetNode)
				return (CoreSimpleFacetNode) f;
		return null;
	}

	@Override
	public Node getSimpleProperty() {
		return getSimpleAttribute();
	}

	@Override
	public FacetProviderNode getFacet_Default() {
		return getFacet_Summary();
	}

	public RoleFacetNode getFacet_Role() {
		for (Node f : getChildren())
			if (f instanceof RoleFacetNode)
				return (RoleFacetNode) f;
		return null;
	}

	// List w/model object ListFacetMO - Simple_List
	@Deprecated
	public ComponentNode getSimpleListFacet() {
		for (Node f : getChildren())
			if (f instanceof ListFacetNode && ((ListFacetNode) f).isSimpleListFacet())
				return (ComponentNode) f;
		return null;
	}

	// TODO - remove - Only used in 1 test
	@Deprecated
	public ComponentNode getDetailListFacet() {
		for (Node f : getChildren())
			if (f instanceof ListFacetNode && ((ListFacetNode) f).isDetailListFacet())
				return (ComponentNode) f;
		return null;
	}

	@Override
	public Image getImage() {
		return Images.getImageRegistry().get(Images.CoreObject);
	}

	@Override
	public INode.CommandType getAddCommand() {
		return INode.CommandType.PROPERTY;
	}

	@Override
	public List<AliasNode> getAliases() {
		List<AliasNode> aliases = new ArrayList<>();
		for (Node c : getChildren())
			if (c instanceof AliasNode)
				aliases.add((AliasNode) c);
		return aliases;
	}

	@Override
	public boolean isAssignableToSimple() {
		return true;
	}

	@Override
	public boolean isAssignableToVWA() {
		return true;
	}

	@Override
	public boolean isAssignedByReference() {
		// Note, core can also be assigned by type.
		return true;
	}

	@Override
	public void setName(String n) {
		getTLModelObject().setName(NodeNameUtils.fixCoreObjectName(n));
		updateNames(NodeNameUtils.fixCoreObjectName(n));
	}

	@Override
	public void sort() {
		getFacet_Summary().sort();
		getFacet_Detail().sort();
	}

	@Override
	public void merge(Node source) {
		if (!(source instanceof CoreObjectNode)) {
			throw new IllegalStateException("Can only merge objects with the same type");
		}
		CoreObjectNode core = (CoreObjectNode) source;
		getFacet_Summary().add(core.getFacet_Summary().getProperties(), true);
		getFacet_Detail().add(core.getFacet_Detail().getProperties(), true);
		getFacet_Role().add(core.getFacet_Role().getProperties(), true);
	}

	@Override
	public boolean isMergeSupported() {
		return true;
	}

	@Override
	public boolean isSimpleAssignable() {
		return true;
	}

	@Override
	public boolean isAssignableToElementRef() {
		return false;
	}

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
		// Let the children facets know to update
		for (Node fn : getChildrenHandler().get())
			if (fn instanceof FacetOMNode)
				((FacetOMNode) fn).getChildrenHandler().clear();
	}

	@Override
	public ExtensionHandler getExtensionHandler() {
		return extensionHandler;
	}

	@Override
	public boolean canOwn(TLFacetType type) {
		switch (type) {
		case SUMMARY:
		case DETAIL:
			return true;
		default:
			return false;
		}
	}

}
