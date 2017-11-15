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
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLComplexTypeBase;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemas.modelObject.ChoiceObjMO;
import org.opentravel.schemas.modelObject.ModelObject;
import org.opentravel.schemas.node.facets.ChoiceFacetNode;
import org.opentravel.schemas.node.facets.ContextualFacetNode;
import org.opentravel.schemas.node.facets.ContributedFacetNode;
import org.opentravel.schemas.node.facets.FacetNode;
import org.opentravel.schemas.node.facets.SimpleFacetNode;
import org.opentravel.schemas.node.handlers.children.ChoiceObjectChildrenHandler;
import org.opentravel.schemas.node.interfaces.AliasOwner;
import org.opentravel.schemas.node.interfaces.ComplexComponentInterface;
import org.opentravel.schemas.node.interfaces.ContextualFacetOwnerInterface;
import org.opentravel.schemas.node.interfaces.ExtensionOwner;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.interfaces.Sortable;
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
public class ChoiceObjectNode extends LibraryMemberBase implements ComplexComponentInterface, ExtensionOwner, Sortable,
		AliasOwner, ContextualFacetOwnerInterface, VersionedObjectInterface, LibraryMemberInterface, TypeProvider {
	private static final Logger LOGGER = LoggerFactory.getLogger(BusinessObjectNode.class);

	private ExtensionHandler extensionHandler = null;

	public ChoiceObjectNode(TLChoiceObject mbr) {
		super(mbr);
		childrenHandler = new ChoiceObjectChildrenHandler(this);
		extensionHandler = new ExtensionHandler(this);

		assert (getSharedFacet() instanceof FacetNode);
	}

	@Override
	public void remove(AliasNode alias) {
		getTLModelObject().removeAlias(alias.getTLModelObject());
		clearAllAliasOwners();
	}

	private void clearAllAliasOwners() {
		for (Node child : getChildren())
			if (child instanceof AliasOwner && child.getChildrenHandler() != null)
				child.getChildrenHandler().clear();
		getChildrenHandler().clear();
	}

	@Override
	public void addAlias(AliasNode alias) {
		if (!getTLModelObject().getAliases().contains(alias.getTLModelObject()))
			getTLModelObject().addAlias(alias.getTLModelObject());
		clearAllAliasOwners();
	}

	@Override
	public AliasNode addAlias(String name) {
		AliasNode alias = null;
		if (this.isEditable_newToChain())
			alias = new AliasNode(this, name);
		addAlias(alias);
		return alias;
	}

	@Override
	public void cloneAliases(List<AliasNode> aliases) {
		for (AliasNode a : aliases)
			addAlias(a.getName());
	}

	/**
	 * Create a new choice contextual facet and assign to this object
	 * 
	 * @param name
	 * @return
	 */
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
		return canOwn(targetCF.getTLModelObject().getFacetType());
		// if (targetCF instanceof ChoiceFacetNode)
		// return true;
		// return false;
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
	public ContributedFacetNode getContributedFacet(TLContextualFacet tlcf) {
		ContributedFacetNode cfn = null;
		for (TLModelElement tlo : getChildrenHandler().getChildren_TL())
			if (tlo == tlcf)
				if (Node.GetNode(tlo) instanceof ContextualFacetNode) {
					ContextualFacetNode cxn = (ContextualFacetNode) Node.GetNode(tlo);
					if (cxn != null) {
						cfn = cxn.getWhereContributed();
						break;
					}
				}

		return cfn;
	}

	@Override
	public String getExtendsTypeNS() {
		return getExtensionBase() != null ? getExtensionBase().getNamespace() : "";
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
	public ExtensionHandler getExtensionHandler() {
		return extensionHandler;
	}

	@Override
	public PropertyOwnerInterface getFacet_Attributes() {
		return null;
	}

	@Override
	public PropertyOwnerInterface getFacet_Default() {
		return getSharedFacet();
	}

	@Override
	public SimpleFacetNode getFacet_Simple() {
		return null;
	}

	@Override
	public Image getImage() {
		return Images.getImageRegistry().get(Images.ChoiceObject);
	}

	@Override
	@Deprecated
	public ChoiceObjMO getModelObject() {
		ModelObject<?> obj = super.getModelObject();
		return (ChoiceObjMO) (obj instanceof ChoiceObjMO ? obj : null);
	}

	@Override
	public String getName() {
		return getTLModelObject().getName();
	}

	public PropertyOwnerInterface getSharedFacet() {
		for (Node n : getChildren()) {
			if (n instanceof FacetNode)
				if (((FacetNode) n).getFacetType().equals(TLFacetType.SHARED))
					return (FacetNode) n;
		}
		return null;
	}

	@Override
	public TLChoiceObject getTLModelObject() {
		return (TLChoiceObject) tlObj;
	}

	@Override
	public boolean isAliasable() {
		return isEditable_newToChain();
	}

	@Override
	public boolean isAssignableToElementRef() {
		return false;
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
	public boolean isAssignedByReference() {
		return true;
	}

	@Override
	public boolean isExtensibleObject() {
		return true;
	}

	@Override
	public boolean isMergeSupported() {
		return false;
	}

	@Override
	public void merge(Node source) {
		if (!(source instanceof ChoiceObjectNode)) {
			throw new IllegalStateException("Can not merge choice objects.");
		}
	}

	@Override
	public Node setExtensible(boolean extensible) {
		if (isEditable_newToChain())
			if (getTLModelObject() instanceof TLComplexTypeBase)
				((TLComplexTypeBase) getTLModelObject()).setNotExtendable(!extensible);
		return this;
	}

	@Override
	public void setExtension(final Node base) {
		if (extensionHandler == null)
			extensionHandler = new ExtensionHandler(this);
		extensionHandler.set(base);
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
		((FacetNode) getSharedFacet()).sort();
		for (PropertyOwnerInterface f : getChoiceFacets())
			((Sortable) f).sort();
	}

}
