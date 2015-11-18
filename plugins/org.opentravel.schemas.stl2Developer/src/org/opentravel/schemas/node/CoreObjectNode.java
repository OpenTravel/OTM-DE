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
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.properties.PropertyOwnerInterface;
import org.opentravel.schemas.properties.Images;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Core Object. This object has many facets: simple, summary, detail, roles and two lists. It implements the complex
 * component interface.
 * 
 * @author Dave Hollander
 * 
 */
public class CoreObjectNode extends ComponentNode implements ComplexComponentInterface, ExtensionOwner,
		VersionedObjectInterface {
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CoreObjectNode.class);

	public CoreObjectNode(LibraryMember mbr) {
		super(mbr);
		addMOChildren();
		// If the mbr was not null but simple type is, set the simple type
		if (modelObject instanceof CoreObjectMO)
			if (((CoreObjectMO) modelObject).getSimpleValueType() == null)
				setSimpleType((Node) ModelNode.getEmptyNode());
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
		setSimpleType((Node) ModelNode.getEmptyNode());
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
			new AliasNode(this, name);
	}

	public void addAliases(List<AliasNode> aliases) {
		for (AliasNode a : aliases) {
			addAlias(a.getName());
		}
	}

	@Override
	public boolean canExtend() {
		return true;
	}

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

	@Override
	public boolean setAssignedType(Node replacement) {
		return getSimpleFacet().getSimpleAttribute().getTypeClass().setAssignedType(replacement);
	}

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
		return getTypeClass().getTypeNode();
	}

	@Override
	public boolean isNamedType() {
		return true;
	}

	@Override
	public List<Node> getChildren_TypeUsers() {
		ArrayList<Node> users = new ArrayList<Node>();
		users.add(getSimpleType());
		users.addAll(getSummaryFacet().getChildren());
		users.addAll(getDetailFacet().getChildren());
		return users;
	}

	@Override
	public String getLabel() {
		if (getExtendsType() == null)
			return super.getLabel();
		// else if (getExtendsType().getName().equals(getName()))
		else if (isVersioned())
			return super.getLabel() + " (Extends version: " + getExtendsType().getLibrary().getVersion() + ")";
		else
			return super.getLabel() + " (Extends: " + getExtendsType().getNameWithPrefix() + ")";
	}

	@Override
	public Node getExtendsType() {
		return getTypeClass().getTypeNode();
	}

	@Override
	public ComponentNode getSimpleType() {
		return (ComponentNode) getSimpleFacet().getSimpleAttribute().getAssignedType();
	}

	@Override
	public boolean setSimpleType(Node type) {
		return getSimpleFacet().getSimpleAttribute().setAssignedType(type);
	}

	@Override
	public SimpleFacetNode getSimpleFacet() {
		for (INode f : getChildren())
			if (f instanceof SimpleFacetNode)
				return (SimpleFacetNode) f;
		return null;
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
		this.setName(n, true);
	}

	@Override
	public void setName(String n, boolean doFamily) {
		super.setName(n, doFamily);
		for (Node user : getTypeUsers()) {
			if (user instanceof PropertyNode)
				user.setName(n);
		}
	}

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

}
