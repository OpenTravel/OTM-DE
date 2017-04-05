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
import java.util.Collections;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemacompiler.codegen.util.FacetCodegenUtils;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLComplexTypeBase;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemas.modelObject.ChoiceObjMO;
import org.opentravel.schemas.modelObject.ModelObject;
import org.opentravel.schemas.node.facets.ChoiceFacetNode;
import org.opentravel.schemas.node.facets.ContextualFacetNode;
import org.opentravel.schemas.node.facets.ContributedFacetNode;
import org.opentravel.schemas.node.facets.FacetNode;
import org.opentravel.schemas.node.facets.SimpleFacetNode;
import org.opentravel.schemas.node.interfaces.ComplexComponentInterface;
import org.opentravel.schemas.node.interfaces.ContextualFacetOwnerInterface;
import org.opentravel.schemas.node.interfaces.ExtensionOwner;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.interfaces.VersionedObjectInterface;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.properties.PropertyOwnerInterface;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.types.ExtensionHandler;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dave Hollander
 * 
 */
public class ChoiceObjectNode extends TypeProviderBase implements ComplexComponentInterface, ExtensionOwner,
		ContextualFacetOwnerInterface, VersionedObjectInterface, LibraryMemberInterface, TypeProvider {
	private static final Logger LOGGER = LoggerFactory.getLogger(BusinessObjectNode.class);

	private ExtensionHandler extensionHandler = null;

	public ChoiceObjectNode(TLChoiceObject mbr) {
		super(mbr);
		addMOChildren();
		extensionHandler = new ExtensionHandler(this);
		inheritedChildren = Collections.emptyList();

		assert (getSharedFacet() instanceof FacetNode);
		assert (getTLModelObject() instanceof TLChoiceObject);
		assert (modelObject instanceof ChoiceObjMO);
	}

	@Override
	public Node setExtensible(boolean extensible) {
		if (isEditable_newToChain())
			if (getTLModelObject() instanceof TLComplexTypeBase)
				((TLComplexTypeBase) getTLModelObject()).setNotExtendable(!extensible);
		return this;
	}

	@Override
	public boolean isNamedType() {
		return true;
	}

	@Override
	public boolean isAssignedByReference() {
		return true;
	}

	@Override
	public ContributedFacetNode getContributedFacet(TLContextualFacet tlObj) {
		for (Node child : getChildren())
			if (child instanceof ContributedFacetNode && child.getTLModelObject() == tlObj)
				return (ContributedFacetNode) child;
		return null;
	}

	@Override
	public PropertyOwnerInterface getDefaultFacet() {
		return getSharedFacet();
	}

	@Override
	public Image getImage() {
		return Images.getImageRegistry().get(Images.ChoiceObject);
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

	public ContextualFacetNode addFacet(String name) {
		if (!isEditable_newToChain())
			throw new IllegalArgumentException("Not Editable - can not add facet to " + this);

		TLContextualFacet tlCf = ContextualFacetNode.createTL(name, TLFacetType.CHOICE);
		ContextualFacetNode cf = new ChoiceFacetNode(tlCf);
		cf.setOwner(this);
		return cf;
	}

	@Override
	public boolean canOwn(ContextualFacetNode targetCF) {
		if (targetCF instanceof ChoiceFacetNode)
			return true;
		return false;
	}

	@Override
	public boolean canOwn(TLFacetType type) {
		switch (type) {
		case SHARED:
		case CHOICE:
			return true;
		default:
			return false;
		}
	}

	@Override
	public ComponentNode createMinorVersionComponent() {
		return super.createMinorVersionComponent(new ChoiceObjectNode((TLChoiceObject) createMinorTLVersion(this)));
	}

	@Override
	public ChoiceObjMO getModelObject() {
		ModelObject<?> obj = super.getModelObject();
		return (ChoiceObjMO) (obj instanceof ChoiceObjMO ? obj : null);
	}

	@Override
	public String getName() {
		return getTLModelObject().getName();
	}

	@Override
	public TLChoiceObject getTLModelObject() {
		return (TLChoiceObject) (modelObject != null ? modelObject.getTLModelObj() : null);
	}

	@Override
	public List<Node> getInheritedChildren() {
		initInheritedChildren();
		if (inheritedChildren == null)
			inheritedChildren = Collections.emptyList();
		return inheritedChildren;
	}

	// 11/8/2016 - rework of initInheritedChildren()
	/**
	 * Get the ghost facets from the TL Model. Model all of them.
	 */
	@Override
	public void initInheritedChildren() {
		inheritedChildren = Collections.emptyList();
		// Model each facet returned in the list of new TLFacets from the TL Model
		for (TLContextualFacet cf : FacetCodegenUtils.findGhostFacets(getTLModelObject(), TLFacetType.CHOICE)) {
			linkInheritedChild(NodeFactory.newMember(null, cf));
		}
	}

	@Override
	public void delete() {
		// Must delete the contextual facets separately because they are separate library members.
		for (Node n : getChildren_New())
			if (n instanceof ContextualFacetNode)
				n.delete();
		super.delete();
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
	public void setName(String name) {
		name = NodeNameUtils.fixChoiceObjectName(name);
		getTLModelObject().setName(name);
		for (TypeUser user : getWhereAssigned()) {
			if (user instanceof PropertyNode)
				user.setName(name);
		}
		for (Node child : getChildren()) {
			for (TypeUser users : ((TypeProvider) child).getWhereAssigned())
				NodeNameUtils.fixName((Node) users);
		}
	}

	@Override
	public void sort() {
		// FIXME
		// getSummaryFacet().sort();
		// ((FacetNode) getDetailFacet()).sort();
		// for (ComponentNode f : getCustomFacets())
		// ((FacetNode) f).sort();
		// for (ComponentNode f : getQueryFacets())
		// ((FacetNode) f).sort();
	}

	@Override
	public void merge(Node source) {
		if (!(source instanceof ChoiceObjectNode)) {
			throw new IllegalStateException("Can not merge choice objects.");
		}
	}

	@Override
	public boolean isExtensibleObject() {
		return true;
	}

	@Override
	public boolean isMergeSupported() {
		return false;
	}

	public PropertyOwnerInterface getSharedFacet() {
		for (Node n : getChildren()) {
			if (n instanceof FacetNode)
				if (((FacetNode) n).getFacetType().equals(TLFacetType.SHARED))
					return (FacetNode) n;
		}
		return null;
	}

	public List<PropertyOwnerInterface> getChoiceFacets() {
		List<PropertyOwnerInterface> facets = new ArrayList<PropertyOwnerInterface>();
		for (Node n : getChildren())
			if (n instanceof ChoiceFacetNode)
				facets.add((FacetNode) n);
		return facets;
	}

	@Override
	public ComponentNodeType getComponentNodeType() {
		return ComponentNodeType.CHOICE;
	}

	@Override
	public boolean isAssignableToSimple() {
		return false;
	}

	@Override
	public boolean isAssignableToVWA() {
		return false;
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

	/* ****************************************************
	 * Only needed for type hierarchy
	 */
	@Override
	public SimpleFacetNode getSimpleFacet() {
		return null;
	}

}
