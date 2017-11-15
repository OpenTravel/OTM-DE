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
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLComplexTypeBase;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemas.node.facets.FacetNode;
import org.opentravel.schemas.node.facets.ListFacetNode;
import org.opentravel.schemas.node.facets.RoleFacetNode;
import org.opentravel.schemas.node.facets.SimpleFacetFacadeNode;
import org.opentravel.schemas.node.handlers.children.CoreObjectChildrenHandler;
import org.opentravel.schemas.node.interfaces.AliasOwner;
import org.opentravel.schemas.node.interfaces.ComplexComponentInterface;
import org.opentravel.schemas.node.interfaces.ExtensionOwner;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.interfaces.Sortable;
import org.opentravel.schemas.node.interfaces.VersionedObjectInterface;
import org.opentravel.schemas.node.properties.AttributeNode;
import org.opentravel.schemas.node.properties.PropertyOwnerInterface;
import org.opentravel.schemas.node.properties.SimpleAttributeFacadeNode;
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
public class CoreObjectNode extends LibraryMemberBase implements ComplexComponentInterface, ExtensionOwner, Sortable,
		AliasOwner, VersionedObjectInterface, LibraryMemberInterface, TypeProvider, SimpleAttributeOwner {
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CoreObjectNode.class);
	private ExtensionHandler extensionHandler = null;

	public CoreObjectNode(TLCoreObject mbr) {
		super(mbr);

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

		addAliases(bo.getAliases());

		setName(bo.getName());
		bo.getLibrary().addMember(this);
		setDocumentation(bo.getDocumentation());

		getFacet_Summary().copyFacet(bo.getFacet_ID());
		getFacet_Summary().copyFacet(bo.getFacet_Summary());
		getFacet_Detail().copyFacet(bo.getFacet_Detail());

		setAssignedType((TypeProvider) ModelNode.getEmptyNode());
	}

	/**
	 * Add to VWA's library a new core with a copy of all the VWA attributes in the summary facet.
	 * 
	 * @param vwa
	 */
	public CoreObjectNode(VWA_Node vwa) {
		this(new TLCoreObject());

		setName(vwa.getName());
		vwa.getLibrary().addMember(this);
		setDocumentation(vwa.getDocumentation());

		getFacet_Summary().copyFacet(vwa.getFacet_Attributes());
		setAssignedType(vwa.getAssignedType());

		// User assist - create an attribute for the VWA base type
		AttributeNode attr = new AttributeNode(new TLAttribute(), getFacet_Summary());
		attr.setName(vwa.getName());
		attr.setAssignedType(vwa.getAssignedType());
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
		return alias;
	}

	// Should this be handled by Listeners?
	private void clearAllAliasOwners() {
		for (Node child : getChildren())
			if (child instanceof AliasOwner && child.getChildrenHandler() != null)
				child.getChildrenHandler().clear();
	}

	@Override
	public void cloneAliases(List<AliasNode> aliases) {
		for (AliasNode a : aliases)
			addAlias(a.getName());
	}

	public CoreObjectChildrenHandler getChildrenHandler() {
		return (CoreObjectChildrenHandler) childrenHandler;
	}

	private void addAliases(List<AliasNode> aliases) {
		for (AliasNode a : aliases)
			addAlias(a.getName());
	}

	@Override
	public ComponentNode createMinorVersionComponent() {
		return super.createMinorVersionComponent(new CoreObjectNode((TLCoreObject) createMinorTLVersion(this)));
	}

	@Override
	public boolean isExtensibleObject() {
		return true;
	}

	@Override
	public Node setExtensible(boolean extensible) {
		if (isEditable_newToChain())
			if (getTLModelObject() instanceof TLComplexTypeBase)
				((TLComplexTypeBase) getTLModelObject()).setNotExtendable(!extensible);
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

	// @Override
	// public List<Node> getChildren_TypeUsers() {
	// return (childrenHandler.getChildren_TypeUsers());
	//
	// // ArrayList<Node> users = new ArrayList<Node>();
	// // users.add((Node) getSimpleType());
	// // users.addAll(getFacet_Summary().getChildren());
	// // users.addAll(getFacet_Detail().getChildren());
	// // return users;
	// }

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
	public boolean setAssignedType(TypeProvider type) {
		return getSimpleAttribute().setAssignedType(type);
	}

	@Override
	public SimpleAttributeFacadeNode getSimpleAttribute() {
		return (SimpleAttributeFacadeNode) getFacet_Simple().getSimpleAttribute();
	}

	@Override
	public SimpleFacetFacadeNode getFacet_Simple() {
		for (INode f : getChildren())
			if (f instanceof SimpleFacetFacadeNode)
				return (SimpleFacetFacadeNode) f;
		return null;
	}

	@Override
	public Node getSimpleProperty() {
		return getSimpleAttribute();
	}

	@Override
	public FacetNode getFacet_Summary() {
		for (INode f : getChildren())
			if (f instanceof FacetNode && ((FacetNode) f).isSummaryFacet())
				return (FacetNode) f;
		return null;
	}

	@Override
	public PropertyOwnerInterface getFacet_Default() {
		return getFacet_Summary();
	}

	@Override
	public FacetNode getFacet_Detail() {
		for (INode f : getChildren())
			if (f instanceof FacetNode && ((FacetNode) f).isDetailFacet())
				return (FacetNode) f;
		return null;
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

	public List<AliasNode> getAliases() {
		List<AliasNode> aliases = new ArrayList<AliasNode>();
		for (Node c : getChildren())
			if (c instanceof AliasNode)
				aliases.add((AliasNode) c);
		return aliases;
	}

	@Override
	public PropertyOwnerInterface getFacet_Attributes() {
		return null;
	}

	// @Override
	// public boolean hasChildren_TypeProviders() {
	// return isXsdType() ? false : true;
	// }

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
		((FacetNode) getFacet_Summary()).sort();
		((FacetNode) getFacet_Detail()).sort();
	}

	@Override
	public void merge(Node source) {
		if (!(source instanceof CoreObjectNode)) {
			throw new IllegalStateException("Can only merge objects with the same type");
		}
		CoreObjectNode core = (CoreObjectNode) source;
		getFacet_Summary().addProperties(core.getFacet_Summary().getChildren(), true);
		getFacet_Detail().addProperties(core.getFacet_Detail().getChildren(), true);
		getFacet_Role().addProperties(core.getFacet_Role().getChildren(), true);
		// getChildrenHandler().clear();
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
	public boolean isAliasable() {
		return isEditable_newToChain();
	}

	@Override
	public boolean isAssignableToElementRef() {
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
