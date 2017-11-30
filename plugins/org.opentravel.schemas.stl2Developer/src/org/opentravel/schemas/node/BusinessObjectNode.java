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
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLComplexTypeBase;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemas.node.controllers.NodeUtils;
import org.opentravel.schemas.node.facets.ContextualFacetNode;
import org.opentravel.schemas.node.facets.ContributedFacetNode;
import org.opentravel.schemas.node.facets.CustomFacetNode;
import org.opentravel.schemas.node.facets.FacetNode;
import org.opentravel.schemas.node.facets.QueryFacetNode;
import org.opentravel.schemas.node.facets.SimpleFacetNode;
import org.opentravel.schemas.node.facets.UpdateFacetNode;
import org.opentravel.schemas.node.handlers.children.BusinessObjectChildrenHandler;
import org.opentravel.schemas.node.interfaces.AliasOwner;
import org.opentravel.schemas.node.interfaces.ComplexComponentInterface;
import org.opentravel.schemas.node.interfaces.ContextualFacetOwnerInterface;
import org.opentravel.schemas.node.interfaces.ExtensionOwner;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.interfaces.Sortable;
import org.opentravel.schemas.node.interfaces.VersionedObjectInterface;
import org.opentravel.schemas.node.listeners.BaseNodeListener;
import org.opentravel.schemas.node.listeners.TypeProviderListener;
import org.opentravel.schemas.node.properties.PropertyOwnerInterface;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.types.ExtensionHandler;
import org.opentravel.schemas.types.TypeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dave Hollander
 * 
 */
public class BusinessObjectNode extends LibraryMemberBase implements ComplexComponentInterface, ExtensionOwner,
		AliasOwner, Sortable, ContextualFacetOwnerInterface, VersionedObjectInterface, LibraryMemberInterface,
		TypeProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(BusinessObjectNode.class);
	private ExtensionHandler extensionHandler = null;

	public BusinessObjectNode(TLBusinessObject mbr) {
		super(mbr);

		childrenHandler = new BusinessObjectChildrenHandler(this);
		extensionHandler = new ExtensionHandler(this);
	}

	/**
	 * Create a new business object using the core as a template and add to the same library as the core object.
	 * 
	 * @param core
	 */
	public BusinessObjectNode(CoreObjectNode core) {
		this(new TLBusinessObject());

		cloneAliases(core.getAliases());

		setName(core.getName());
		core.getLibrary().addMember(this); // version managed library safe add
		setDocumentation(core.getDocumentation());

		getFacet_Summary().copyFacet(core.getFacet_Summary());
		getFacet_Detail().copyFacet(core.getFacet_Detail());
	}

	public BusinessObjectNode(VWA_Node vwa) {
		this(new TLBusinessObject());

		setName(vwa.getName());
		vwa.getLibrary().addMember(this);
		setDocumentation(vwa.getDocumentation());

		getFacet_Summary().copyFacet(vwa.getFacet_Attributes());
	}

	@Override
	public String getName() {
		return emptyIfNull(getTLModelObject().getName());
	}

	@Override
	public TLBusinessObject getTLModelObject() {
		return (TLBusinessObject) tlObj;
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
	public boolean hasChildren_TypeProviders() {
		return true;
	}

	@Override
	public boolean isAssignedByReference() {
		return true;
	}

	// @Override
	// public List<Node> getChildren_TypeUsers() {
	// return childrenHandler.getChildren_TypeUsers();
	// // ArrayList<Node> users = new ArrayList<Node>();
	// // users.addAll(getIDFacet().getChildren());
	// // users.addAll(getFacet_Summary().getChildren());
	// // users.addAll(getFacet_Detail().getChildren());
	// // for (INode facet : getCustomFacets())
	// // users.addAll(facet.getChildren());
	// // for (INode facet : getQueryFacets())
	// // users.addAll(facet.getChildren());
	// // return users;
	// }

	@Override
	public ComponentNodeType getComponentNodeType() {
		return ComponentNodeType.BUSINESS;
	}

	@Override
	public SimpleFacetNode getFacet_Simple() {
		return null;
	}

	@Override
	public FacetNode getFacet_Summary() {
		for (INode f : getChildren())
			if ((f instanceof FacetNode && ((FacetNode) f).isSummaryFacet()))
				return (FacetNode) f;
		return null;
	}

	@Override
	public FacetNode getFacet_ID() {
		for (INode f : getChildren())
			if ((f instanceof FacetNode && ((FacetNode) f).isIDFacet()))
				return (FacetNode) f;
		return null;
	}

	@Override
	public FacetNode getFacet_Detail() {
		for (INode f : getChildren())
			if ((f instanceof FacetNode) && ((FacetNode) f).isDetailFacet())
				return (FacetNode) f;
		return null;
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
	public FacetNode getFacet_Default() {
		return getFacet_Summary();
	}

	// @Override
	// public List<Node> getInheritedChildren() {
	// initInheritedChildren();
	// if (inheritedChildren == null)
	// inheritedChildren = Collections.emptyList();
	// return inheritedChildren;
	// }

	// 11/8/2016 - rework of initInheritedChildren()
	// /**
	// * Get the ghost facets from the TL Model. Model all of them.
	// */
	// @Deprecated
	// public void initInheritedChildren() {
	// inheritedChildren = Collections.emptyList();
	// // Model each facet returned in the list of new TLFacets from the TL Model
	// List<TLContextualFacet> tlCfs = new ArrayList<TLContextualFacet>();
	// tlCfs.addAll(FacetCodegenUtils.findGhostFacets(getTLModelObject(), TLFacetType.CUSTOM));
	// tlCfs.addAll(FacetCodegenUtils.findGhostFacets(getTLModelObject(), TLFacetType.QUERY));
	// tlCfs.addAll(FacetCodegenUtils.findGhostFacets(getTLModelObject(), TLFacetType.UPDATE));
	// // TODO - why is this called so often?
	// List<TLContextualFacet> allTLCfs = new ArrayList<TLContextualFacet>();
	// for (ContextualFacetNode cfn : getModelNode().getDescendants_ContextualFacets())
	// allTLCfs.add(cfn.getTLModelObject());
	//
	// // Some (all?) of the facets will be new without identity listener
	// // Match on facetType, Name, OwningLibrary and Owning Entity
	// for (TLContextualFacet cf : tlCfs) {
	// if (GetNode(cf) == null)
	// cf = find(allTLCfs, cf);
	// if (GetNode(cf) != null)
	// linkInheritedChild(NodeFactory.newMemberOLD(null, cf));
	// else
	// LOGGER.debug("Failed to find matching Contextual Facet Node: ");
	// }
	// // for (TLContextualFacet cf : FacetCodegenUtils.findGhostFacets(getTLModelObject(), TLFacetType.CUSTOM))
	// // linkInheritedChild(NodeFactory.newComponentMember(null, cf));
	// // for (TLContextualFacet cf : FacetCodegenUtils.findGhostFacets(getTLModelObject(), TLFacetType.QUERY))
	// // linkInheritedChild(NodeFactory.newComponentMember(null, cf));
	// // for (TLContextualFacet cf : FacetCodegenUtils.findGhostFacets(getTLModelObject(), TLFacetType.UPDATE))
	// // linkInheritedChild(NodeFactory.newComponentMember(null, cf));
	//
	// }

	// private TLContextualFacet find(List<TLContextualFacet> allTLCfs, TLContextualFacet ghostCF) {
	// // LOGGER.debug("Find: " + ghostCF.getName());
	// for (TLContextualFacet tlCf : allTLCfs)
	// if (tlCf.getName().equals(ghostCF.getName()))
	// // if (tlCf.getOwningEntity() == ghostCF.getOwningEntity())
	// if (tlCf.getFacetType() == ghostCF.getFacetType())
	// if (tlCf.getOwningLibrary() == ghostCF.getOwningLibrary())
	// return tlCf;
	// return null;
	// }

	@Override
	public Image getImage() {
		return Images.getImageRegistry().get(Images.BusinessObject);
	}

	@Override
	public BaseNodeListener getNewListener() {
		return new TypeProviderListener(this);
	}

	@Override
	public void remove(AliasNode alias) {
		getTLModelObject().removeAlias(alias.getTLModelObject());
		clearAllAliasOwners();
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
			alias = new AliasNode(this, NodeNameUtils.fixBusinessObjectName(name));
		return alias;
	}

	@Override
	public void cloneAliases(List<AliasNode> aliases) {
		for (AliasNode a : aliases)
			addAlias(a.getName());
	}

	private void clearAllAliasOwners() {
		for (Node child : getChildren())
			if (child instanceof AliasOwner && child.getChildrenHandler() != null)
				child.getChildrenHandler().clear();
		getChildrenHandler().clear();
	}

	/**
	 * 
	 * New facets can only be added in unmanaged or head versions.
	 * 
	 * @param name
	 * @param type
	 * @return the new contextual facet (not contributed)
	 */
	// TODO - consider allowing them in minor and use createMinorVersionOfComponent()
	public ContextualFacetNode addFacet(String name, TLFacetType type) {
		if (!isEditable_newToChain()) {
			isEditable_newToChain();
			throw new IllegalArgumentException("Not editable - Can not add facet to " + this);
		}
		TLContextualFacet tlCf = ContextualFacetNode.createTL(name, type);
		ContextualFacetNode cf = null;
		if (TLFacetType.CUSTOM.equals(type))
			cf = new CustomFacetNode(tlCf);
		else if (TLFacetType.QUERY.equals(type))
			cf = new QueryFacetNode(tlCf);
		else if (TLFacetType.UPDATE.equals(type))
			cf = new UpdateFacetNode(tlCf);

		cf.setOwner(this);

		if (!cf.canBeLibraryMember())
			assert cf.getParent() == this;
		else
			assert cf.getParent() instanceof NavNode;

		return cf;
	}

	@Override
	public boolean canOwn(ContextualFacetNode targetCF) {
		if (targetCF instanceof CustomFacetNode)
			return true;
		if (targetCF instanceof QueryFacetNode)
			return true;
		if (targetCF instanceof UpdateFacetNode)
			return true;
		return false;
	}

	@Override
	public boolean canOwn(TLFacetType type) {
		switch (type) {
		case ID:
		case SUMMARY:
		case DETAIL:
		case CUSTOM:
		case QUERY:
		case UPDATE:
			return true;
		default:
			return false;
		}
	}

	@Override
	public ComponentNode createMinorVersionComponent() {
		return super.createMinorVersionComponent(new BusinessObjectNode((TLBusinessObject) createMinorTLVersion(this)));
	}

	// @Override
	// @Deprecated
	// public BusinessObjMO getModelObject() {
	// ModelObject<?> obj = super.getModelObject();
	// return (BusinessObjMO) (obj instanceof BusinessObjMO ? obj : null);
	// }

	/**
	 * @return Custom Facets without inherited
	 */
	public List<CustomFacetNode> getCustomFacets() {
		return getCustomFacets(false);
	}

	/**
	 * @param includeInherited
	 *            add inherited facets to the list
	 * @return new list of custom facets
	 */
	public List<CustomFacetNode> getCustomFacets(boolean includeInherited) {
		ArrayList<CustomFacetNode> ret = new ArrayList<CustomFacetNode>();
		for (INode f : getChildren()) {
			if (f instanceof ContributedFacetNode)
				f = ((ContributedFacetNode) f).getContributor();
			if (f instanceof CustomFacetNode)
				ret.add((CustomFacetNode) f);
		}
		if (includeInherited)
			for (INode f : getInheritedChildren()) {
				if (f instanceof ContributedFacetNode)
					f = ((ContributedFacetNode) f).getContributor();
				if (f instanceof CustomFacetNode)
					ret.add((CustomFacetNode) f);
			}
		return ret;
	}

	public List<ComponentNode> getQueryFacets() {
		return getQueryFacets(false);
	}

	public List<ComponentNode> getQueryFacets(boolean includeInherited) {
		ArrayList<ComponentNode> ret = new ArrayList<ComponentNode>();
		for (INode f : getChildren()) {
			if (f instanceof QueryFacetNode)
				ret.add((ComponentNode) f);
		}
		if (includeInherited)
			for (INode f : getInheritedChildren())
				if (f instanceof QueryFacetNode)
					ret.add((ComponentNode) f);

		return ret;
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

	@Override
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

	@Override
	public void setName(String name) {
		getTLModelObject().setName(NodeNameUtils.fixBusinessObjectName(name));
		updateNames(NodeNameUtils.fixBusinessObjectName(name));
	}

	@Override
	public void sort() {
		getFacet_Summary().sort();
		getFacet_Detail().sort();
		for (ComponentNode f : getCustomFacets())
			((FacetNode) f).sort();
		for (ComponentNode f : getQueryFacets())
			((FacetNode) f).sort();
	}

	@Override
	public void merge(Node source) {
		if (!(source instanceof BusinessObjectNode)) {
			throw new IllegalStateException("Can only merge objects with the same type");
		}
		BusinessObjectNode business = (BusinessObjectNode) source;
		getFacet_ID().addProperties(business.getFacet_ID().getChildren(), true);
		getFacet_Summary().addProperties(business.getFacet_Summary().getChildren(), true);
		getFacet_Detail().addProperties(business.getFacet_Detail().getChildren(), true);

		List<ComponentNode> customs = new ArrayList<ComponentNode>();
		customs.addAll(business.getCustomFacets());
		copyFacet(customs);
		copyFacet(business.getQueryFacets());
		getChildrenHandler().clear();
	}

	private void copyFacet(List<ComponentNode> facets) {
		for (ComponentNode f : facets) {
			FacetNode facet = (FacetNode) f;
			if (!NodeUtils.checker(facet).isInheritedFacet().get()) {
				TLFacet tlFacet = facet.getTLModelObject();
				String name = "";
				if (tlFacet instanceof TLContextualFacet)
					name = ((TLContextualFacet) tlFacet).getName();
				FacetNode newFacet = addFacet(name, tlFacet.getFacetType());
				newFacet.addProperties(facet.getChildren(), true);
			}
		}
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
	public boolean isAssignableToVWA() {
		return false;
	}

	// // @Override
	// public boolean isAliasable() {
	// return isEditable_newToChain();
	// }

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
