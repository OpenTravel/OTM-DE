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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemacompiler.codegen.util.FacetCodegenUtils;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLComplexTypeBase;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetOwner;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemas.modelObject.ChoiceObjMO;
import org.opentravel.schemas.modelObject.ModelObject;
import org.opentravel.schemas.node.interfaces.ComplexComponentInterface;
import org.opentravel.schemas.node.interfaces.ExtensionOwner;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.interfaces.VersionedObjectInterface;
import org.opentravel.schemas.node.listeners.ListenerFactory;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.properties.PropertyOwnerInterface;
import org.opentravel.schemas.properties.Images;

/**
 * @author Dave Hollander
 * 
 */
public class ChoiceObjectNode extends ComponentNode implements ComplexComponentInterface, ExtensionOwner,
		VersionedObjectInterface, LibraryMemberInterface {

	// private static final Logger LOGGER = LoggerFactory.getLogger(BusinessObjectNode.class);

	public ChoiceObjectNode(LibraryMember mbr) {
		super(mbr);
		addMOChildren();
		ListenerFactory.setListner(this);

		assert (getSharedFacet() != null);
		assert (getModelObject() != null);
		if (getModelObject() == null) {
			// LOGGER.debug("Missing model object on choice object: " + this);
			return;
		}
	}

	/**
	 * Create a new choice object using the core as a template and add to the same library as the core object.
	 * 
	 * @param core
	 */
	// public ChoiceObjectNode(CoreObjectNode core) {
	// this(new TLChoiceObject());
	//
	// addAliases(core.getAliases());
	//
	// setName(core.getName());
	// core.getLibrary().addMember(this); // version managed library safe add
	// setDocumentation(core.getDocumentation());
	//
	// getSummaryFacet().copyFacet((FacetNode) core.getSummaryFacet());
	// // TODO ((FacetNode) getDetailFacet()).copyFacet((FacetNode) core.getDetailFacet());
	// }

	// public ChoiceObjectNode(VWA_Node vwa) {
	// this(new TLChoiceObject());
	//
	// setName(vwa.getName());
	// vwa.getLibrary().addMember(this);
	// setDocumentation(vwa.getDocumentation());
	//
	// getSummaryFacet().copyFacet((FacetNode) vwa.getAttributeFacet());
	// }

	// @Override
	// public boolean isExtensible() {
	// return getTLModelObject() != null ? !((TLComplexTypeBase) getTLModelObject()).isNotExtendable() : false;
	// }
	//
	// @Override
	// public boolean isExtensibleObject() {
	// return true;
	// }

	@Override
	public Node setExtensible(boolean extensible) {
		if (isEditable_newToChain())
			if (getTLModelObject() instanceof TLComplexTypeBase)
				((TLComplexTypeBase) getTLModelObject()).setNotExtendable(!extensible);
		return this;
	}

	// @Override
	// public Node getTypeNode() {
	// return getTypeClass().getTypeNode();
	// }

	// @Override
	// public boolean hasChildren_TypeProviders() {
	// return isXsdType() ? false : true;
	// }

	@Override
	public boolean isNamedType() {
		return true;
	}

	@Override
	public boolean isAssignedByReference() {
		return true;
	}

	// @Override
	// public List<Node> getChildren_TypeUsers() {
	// ArrayList<Node> users = new ArrayList<Node>();
	// // users.addAll(getIDFacet().getChildren());
	// // users.addAll(getSummaryFacet().getChildren());
	// // TODO users.addAll(getDetailFacet().getChildren());
	// // TODO for (INode facet : getCustomFacets())
	// // TODO users.addAll(facet.getChildren());
	// // TODO for (INode facet : getQueryFacets())
	// // TODO users.addAll(facet.getChildren());
	// return users;
	// }

	//

	// @Override
	// public FacetNode getSummaryFacet() {
	// // for (INode f : getChildren())
	// // if (((FacetNode) f).getFacetType().equals(TLFacetType.SUMMARY))
	// // return (FacetNode) f;
	// return null;
	// }

	// @Override
	// public PropertyOwnerInterface getDetailFacet() {
	// return null;
	// }

	@Override
	public PropertyOwnerInterface getDefaultFacet() {
		return getSharedFacet();
	}

	@Override
	public String getLabel() {
		if (getExtendsType() == null)
			return super.getLabel();
		else if (isVersioned())
			// else if (getExtendsType().getName().equals(getName()))
			return super.getLabel() + " (Extends version:  " + getExtendsType().getLibrary().getVersion() + ")";
		else
			return super.getLabel() + " (Extends: " + getExtendsType().getNameWithPrefix() + ")";
	}

	@Override
	public Node getExtendsType() {
		if (getModelObject().getBaseClass() != null)
			assert getTypeClass().getTypeNode() != null;
		return getTypeClass().getTypeNode();
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

	public FacetNode addFacet(String name) {
		if (!isEditable_newToChain())
			throw new IllegalArgumentException("Can not add facet to " + this);
		if (getLibrary().getDefaultContextId() == null || getLibrary().getDefaultContextId().isEmpty())
			throw new IllegalStateException("No context value to assign to facet.");

		TLFacet tf = getModelObject().addFacet(TLFacetType.CHOICE);
		tf.setLabel(NodeNameUtils.fixFacetName(name));
		tf.setContext(getLibrary().getDefaultContextId());
		return (FacetNode) NodeFactory.newComponentMember(this, tf);
	}

	@Override
	public boolean canExtend() {
		return true;
	}

	@Override
	public ComponentNode createMinorVersionComponent() {
		return super.createMinorVersionComponent(new ChoiceObjectNode(createMinorTLVersion(this)));
	}

	@Override
	public ChoiceObjMO getModelObject() {
		ModelObject<?> obj = super.getModelObject();
		return (ChoiceObjMO) (obj instanceof ChoiceObjMO ? obj : null);
	}

	// // Custom Facets
	// public List<ComponentNode> getCustomFacets() {
	// ArrayList<ComponentNode> ret = new ArrayList<ComponentNode>();
	// for (INode f : getChildren()) {
	// if (((Node) f).isCustomFacet()) {
	// ret.add((ComponentNode) f);
	// }
	// }
	// return ret;
	// }
	//
	// public List<ComponentNode> getQueryFacets() {
	// ArrayList<ComponentNode> ret = new ArrayList<ComponentNode>();
	// for (INode f : getChildren()) {
	// if (((Node) f).isQueryFacet()) {
	// ret.add((ComponentNode) f);
	// }
	// }
	// return ret;
	// }

	// private FacetNode findFacet(String label, String context) {
	// label = emptyIfNull(label);
	// context = emptyIfNull(context);
	// for (Node c : getChildren()) {
	// if (c instanceof FacetNode) {
	// TLFacet tlFacet = (TLFacet) c.getTLModelObject();
	// if (label.equals(emptyIfNull(tlFacet.getLabel())) && context.equals(emptyIfNull(tlFacet.getContext())))
	// return (FacetNode) c;
	// }
	// }
	// return null;
	// }

	// private String emptyIfNull(String str) {
	// if (str == null)
	// return "";
	// return str;
	// }

	/**
	 * It is copy of {@link FacetCodegenUtils#findGhostFacets(TLFacetOwner, TLFacetType)} but with this difference that
	 * it returns all facet with given facet type from all extension hierarchy of facetOwner.
	 * 
	 * @param facetOwner
	 *            the facet owner for which to return "ghost facets"
	 * @param facetType
	 *            the type of ghost facets to retrieve
	 * @return List<TLFacet>
	 */
	public List<TLFacet> findInheritedFacets(TLFacetOwner facetOwner, TLFacetType facetType) {
		Set<String> inheritedFacetNames = new HashSet<String>();
		List<TLFacet> inheritedFacets = new ArrayList<TLFacet>();
		TLFacetOwner extendedOwner = FacetCodegenUtils.getFacetOwnerExtension(facetOwner);
		Set<TLFacetOwner> visitedOwners = new HashSet<TLFacetOwner>();

		// Find all of the inherited facets of the specified facet type
		while (extendedOwner != null) {
			List<TLFacet> facetList = FacetCodegenUtils.getAllFacetsOfType(extendedOwner, facetType);

			for (TLFacet facet : facetList) {
				String facetKey = facetType.getIdentityName(facet.getContext(), facet.getLabel());

				if (!inheritedFacetNames.contains(facetKey)) {
					inheritedFacetNames.add(facetKey);
					inheritedFacets.add(facet);
				}
			}
			visitedOwners.add(extendedOwner);
			extendedOwner = FacetCodegenUtils.getFacetOwnerExtension(extendedOwner);

			if (visitedOwners.contains(extendedOwner)) {
				break; // exit if we encounter a circular reference
			}
		}

		List<TLFacet> ghostFacets = new ArrayList<TLFacet>();

		for (TLFacet inheritedFacet : inheritedFacets) {
			TLFacet ghostFacet = new TLFacet();
			ghostFacet.setFacetType(facetType);
			ghostFacet.setContext(inheritedFacet.getContext());
			ghostFacet.setLabel(inheritedFacet.getLabel());
			ghostFacet.setOwningEntity(facetOwner);
			ghostFacets.add(ghostFacet);
		}
		return ghostFacets;
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
	public void setName(String n) {
		this.setName(n, true);
		for (Node child : getChildren()) {
			for (Node users : child.getTypeUsers())
				NodeNameUtils.fixName(users);
		}
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
		// FIXME
		if (!(source instanceof ChoiceObjectNode)) {
			throw new IllegalStateException("Can only merge objects with the same type");
		}
		// ChoiceObjectNode business = (ChoiceObjectNode) source;
		// getIDFacet().addProperties(business.getIDFacet().getChildren(), true);
		// getSummaryFacet().addProperties(business.getSummaryFacet().getChildren(), true);
		// getDetailFacet().addProperties(business.getDetailFacet().getChildren(), true);
		// copyFacet(business.getCustomFacets());
		// copyFacet(business.getQueryFacets());
	}

	// private void copyFacet(List<ComponentNode> facets) {
	// for (ComponentNode f : facets) {
	// FacetNode facet = (FacetNode) f;
	// if (!NodeUtils.checker(facet).isInheritedFacet().get()) {
	// TLFacet tlFacet = (TLFacet) facet.getTLModelObject();
	// FacetNode newFacet = addFacet(tlFacet.getLabel(), tlFacet.getFacetType());
	// newFacet.addProperties(facet.getChildren(), true);
	// }
	// }
	// }

	@Override
	public boolean isMergeSupported() {
		return true;
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
			if (n instanceof FacetNode)
				if (((FacetNode) n).getFacetType().equals(TLFacetType.CHOICE))
					facets.add((FacetNode) n);
		return facets;
	}

	@Override
	public ComponentNodeType getComponentNodeType() {
		return ComponentNodeType.CHOICE;
	}

	/* ****************************************************
	 * Only needed for type hierarchy
	 */
	@Override
	public ComponentNode getSimpleType() {
		return null;
	}

	@Override
	public boolean setSimpleType(Node type) {
		return false;
	}

	@Override
	public SimpleFacetNode getSimpleFacet() {
		return null;
	}

}
