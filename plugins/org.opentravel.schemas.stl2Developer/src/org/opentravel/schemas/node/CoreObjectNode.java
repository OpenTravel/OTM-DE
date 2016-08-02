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
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLComplexTypeBase;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemas.modelObject.CoreObjectMO;
import org.opentravel.schemas.modelObject.ListFacetMO;
import org.opentravel.schemas.node.interfaces.ComplexComponentInterface;
import org.opentravel.schemas.node.interfaces.ExtensionOwner;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.interfaces.VersionedObjectInterface;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.properties.PropertyOwnerInterface;
import org.opentravel.schemas.node.properties.SimpleAttributeNode;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.types.ExtensionHandler;
import org.opentravel.schemas.types.SimpleAttributeOwner;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Core Object. This object has many facets: simple, summary, detail, roles and two lists. It implements the complex
 * component interface.
 * 
 * @author Dave Hollander
 * 
 */
public class CoreObjectNode extends TypeProviderBase implements ComplexComponentInterface, ExtensionOwner,
		VersionedObjectInterface, LibraryMemberInterface, TypeProvider, SimpleAttributeOwner {
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CoreObjectNode.class);
	private ExtensionHandler extensionHandler = null;

	public CoreObjectNode(LibraryMember mbr) {
		super(mbr);
		addMOChildren();
		extensionHandler = new ExtensionHandler(this);

		// If the mbr was not null but simple type is, set the simple type
		if (modelObject instanceof CoreObjectMO)
			if (((CoreObjectMO) modelObject).getSimpleValueType() == null)
				setSimpleType((TypeProvider) ModelNode.getEmptyNode());
	}

	public CoreObjectNode(BusinessObjectNode bo) {
		this(new TLCoreObject());

		addAliases(bo.getAliases());

		setName(bo.getName());
		bo.getLibrary().addMember(this);
		setDocumentation(bo.getDocumentation());

		((FacetNode) getSummaryFacet()).copyFacet((FacetNode) bo.getIDFacet());
		((FacetNode) getSummaryFacet()).copyFacet(bo.getSummaryFacet());
		((FacetNode) getDetailFacet()).copyFacet((FacetNode) bo.getDetailFacet());
		setSimpleType((TypeProvider) ModelNode.getEmptyNode());
	}

	public CoreObjectNode(VWA_Node vwa) {
		this(new TLCoreObject());

		setName(vwa.getName());
		vwa.getLibrary().addMember(this);
		setDocumentation(vwa.getDocumentation());

		((FacetNode) getSummaryFacet()).copyFacet((FacetNode) vwa.getAttributeFacet());
		setSimpleType(vwa.getSimpleType());
	}

	public void addAlias(String name) {
		if (this.isEditable_newToChain())
			new AliasNode(this, NodeNameUtils.fixCoreObjectName(name));
	}

	public void addAliases(List<AliasNode> aliases) {
		for (AliasNode a : aliases) {
			addAlias(a.getName());
		}
	}

	// @Override
	// public boolean canExtend() {
	// return true;
	// }

	@Override
	public ComponentNode createMinorVersionComponent() {
		return super.createMinorVersionComponent(new CoreObjectNode(createMinorTLVersion(this)));
	}

	@Override
	public boolean isExtensible() {
		return getTLModelObject() != null ? !((TLComplexTypeBase) getTLModelObject()).isNotExtendable() : false;
	}

	@Override
	public boolean isExtensibleObject() {
		return true;
	}

	// @Override
	// public boolean setAssignedType(TypeProvider replacement) {
	// return getSimpleFacet().getSimpleAttribute().getTypeClass().setAssignedType(replacement);
	// }

	@Override
	public Node setExtensible(boolean extensible) {
		if (isEditable_newToChain())
			if (getTLModelObject() instanceof TLComplexTypeBase)
				((TLComplexTypeBase) getTLModelObject()).setNotExtendable(!extensible);
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.types.TypeProvider#getTypeNode()
	 */
	@Override
	public Node getTypeNode() {
		return (Node) getSimpleFacet().getSimpleAttribute().getAssignedType();
		// return (Node) getTypeClass().getTypeNode();
	}

	@Override
	public boolean isNamedType() {
		return true;
	}

	@Override
	public List<Node> getChildren_TypeUsers() {
		ArrayList<Node> users = new ArrayList<Node>();
		users.add((Node) getSimpleType());
		users.addAll(getSummaryFacet().getChildren());
		users.addAll(getDetailFacet().getChildren());
		return users;
	}

	@Override
	public ComponentNodeType getComponentNodeType() {
		return ComponentNodeType.CORE;
	}

	// @Override
	// public String getLabel() {
	// if (getExtensionBase() == null)
	// return super.getLabel();
	// // else if (getExtendsType().getName().equals(getName()))
	// else if (isVersioned())
	// return super.getLabel() + " (Extends version: " + getExtensionBase().getLibrary().getVersion() + ")";
	// else
	// return super.getLabel() + " (Extends: " + getExtensionBase().getNameWithPrefix() + ")";
	// }

	// /////////////////////////////////////////////////////////////////
	//
	// Simple Attribute Owner implementations
	//
	@Override
	public TypeProvider getSimpleType() {
		return getSimpleAttribute().getAssignedType();
	}

	@Override
	public boolean setSimpleType(TypeProvider type) {
		return getSimpleAttribute().setAssignedType(type);
	}

	@Override
	public SimpleAttributeNode getSimpleAttribute() {
		return getSimpleFacet().getSimpleAttribute();
	}

	@Override
	public SimpleFacetNode getSimpleFacet() {
		for (INode f : getChildren())
			if (f instanceof SimpleFacetNode)
				return (SimpleFacetNode) f;
		return null;
	}

	@Override
	public Node getSimpleProperty() {
		return getSimpleFacet().getChildren().get(0);
	}

	@Override
	public PropertyOwnerInterface getSummaryFacet() {
		for (INode f : getChildren())
			if (((FacetNode) f).getFacetType().equals(TLFacetType.SUMMARY))
				return (PropertyOwnerInterface) f;
		return null;
	}

	@Override
	public PropertyOwnerInterface getDefaultFacet() {
		return getSummaryFacet();
	}

	@Override
	public PropertyOwnerInterface getDetailFacet() {
		for (INode f : getChildren())
			if (((FacetNode) f).getFacetType().equals(TLFacetType.DETAIL))
				return (PropertyOwnerInterface) f;
		return null;
	}

	// Role w/model object RoleEnumerationMO
	@Override
	public RoleFacetNode getRoleFacet() {
		for (Node f : getChildren())
			if (f instanceof RoleFacetNode)
				return (RoleFacetNode) f;
		return null;
	}

	// List w/model object ListFacetMO - Simple_List
	public ComponentNode getSimpleListFacet() {
		for (Node f : getChildren())
			if (f.modelObject instanceof ListFacetMO)
				if (((ListFacetMO) f.modelObject).isSimpleList())
					return (ComponentNode) f;
		return null;
	}

	// List w/model object ListFacetMO - Detail_List
	public ComponentNode getDetailListFacet() {
		for (Node f : getChildren())
			if (f.modelObject instanceof ListFacetMO)
				if (((ListFacetMO) f.modelObject).isDetailList())
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
	public PropertyOwnerInterface getAttributeFacet() {
		return null;
	}

	@Override
	public boolean hasChildren_TypeProviders() {
		return isXsdType() ? false : true;
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
		n = NodeNameUtils.fixCoreObjectName(n);
		super.setName(n);
		for (TypeUser user : getWhereAssigned()) {
			if (user instanceof PropertyNode)
				user.setName(n);
		}
	}

	// @Deprecated
	// @Override
	// public void setName(String n, boolean doFamily) {
	// // n = NodeNameUtils.fixCoreObjectName(n);
	// // super.setName(n);
	// // for (TypeUser user : getWhereAssigned()) {
	// // if (user instanceof PropertyNode)
	// // user.setName(n);
	// // }
	// }

	@Override
	public void sort() {
		((FacetNode) getSummaryFacet()).sort();
		((FacetNode) getDetailFacet()).sort();
	}

	@Override
	public void merge(Node source) {
		if (!(source instanceof CoreObjectNode)) {
			throw new IllegalStateException("Can only merge objects with the same type");
		}
		CoreObjectNode core = (CoreObjectNode) source;
		getSummaryFacet().addProperties(core.getSummaryFacet().getChildren(), true);
		getDetailFacet().addProperties(core.getDetailFacet().getChildren(), true);
		getRoleFacet().addProperties(core.getRoleFacet().getChildren(), true);
	}

	@Override
	public boolean isMergeSupported() {
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
