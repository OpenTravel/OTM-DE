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
import org.opentravel.schemacompiler.event.ModelElementListener;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLComplexTypeBase;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetOwner;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemas.modelObject.ChoiceObjMO;
import org.opentravel.schemas.modelObject.ModelObject;
import org.opentravel.schemas.node.facets.ChoiceFacetNode;
import org.opentravel.schemas.node.facets.FacetNode;
import org.opentravel.schemas.node.facets.SimpleFacetNode;
import org.opentravel.schemas.node.interfaces.ComplexComponentInterface;
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
		VersionedObjectInterface, LibraryMemberInterface, TypeProvider {
	private static final Logger LOGGER = LoggerFactory.getLogger(BusinessObjectNode.class);

	private ExtensionHandler extensionHandler = null;

	public ChoiceObjectNode(TLChoiceObject mbr) {
		super(mbr);
		addMOChildren();
		extensionHandler = new ExtensionHandler(this);

		assert (getSharedFacet() != null);
		assert (getModelObject() != null);

		assert (modelObject instanceof ChoiceObjMO);
		// assert (mbr instanceof TLChoiceObject);

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
	public PropertyOwnerInterface getDefaultFacet() {
		return getSharedFacet();
	}

	// @Override
	// public String getLabel() {
	// if (getExtensionBase() == null)
	// return super.getLabel();
	// else if (isVersioned())
	// // else if (getExtendsType().getName().equals(getName()))
	// return super.getLabel() + " (Extends version:  " + getExtensionBase().getLibrary().getVersion() + ")";
	// else
	// return super.getLabel() + " (Extends: " + getExtensionBase().getNameWithPrefix() + ")";
	// }

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
			throw new IllegalArgumentException("Not Editable - can not add facet to " + this);
		if (getLibrary().getDefaultContextId() == null || getLibrary().getDefaultContextId().isEmpty())
			throw new IllegalStateException("No context value to assign to facet.");

		TLFacet tf = getModelObject().addFacet(TLFacetType.CHOICE);
		tf.setLabel(NodeNameUtils.fixFacetName(name));
		tf.setContext(getLibrary().getDefaultContextId());
		return (FacetNode) NodeFactory.newComponentMember(this, tf);
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
	public void initInheritedChildren() {
		// Create list of actual facet names from the tlFacet in this choice object
		List<String> facetNames = new ArrayList<String>();
		for (Node child : getChildren())
			if (child instanceof FacetNode)
				if (((TLFacet) child.getTLModelObject()).getLabel() != null)
					facetNames.add(((TLFacet) child.getTLModelObject()).getLabel());

		// Add each facet found by the TL utilities to the list if not already in this choice
		List<TLFacet> inheritedFacets = new ArrayList<TLFacet>();
		for (TLFacet tlFacet : findInheritedFacets((TLFacetOwner) getTLModelObject(), TLFacetType.CHOICE)) {
			// Node inherited = Node.GetNode(tlFacet);
			String facetName = tlFacet.getLabel();
			boolean found = false;
			// if (!(inherited instanceof FacetNode))
			// found = true; // don't add to inherited list
			// else
			for (String name : facetNames)
				if (name.equals(facetName))
					found = true;
			if (!found)
				inheritedFacets.add(tlFacet);
		}

		// For each facet, create a node and add to inherited structures
		for (TLFacet obj : inheritedFacets) {
			ComponentNode nn = NodeFactory.newComponentMember(null, obj);
			if (nn != null)
				linkInheritedChild(nn);
		}
		// LOGGER.debug(this + " has " + inheritedFacets.size() + " inherited children.");
	}

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
			for (ModelElementListener l : inheritedFacet.getListeners())
				ghostFacet.addListener(l);
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
		// this.setName(n, true);
		super.setName(n);
		for (TypeUser user : getWhereAssigned()) {
			if (user instanceof PropertyNode)
				user.setName(n);
		}

		for (Node child : getChildren()) {
			for (TypeUser users : ((TypeProvider) child).getWhereAssigned())
				NodeNameUtils.fixName((Node) users);
		}
	}

	// @Deprecated
	// @Override
	// public void setName(String n, boolean doFamily) {
	// super.setName(n);
	// for (TypeUser user : getWhereAssigned()) {
	// if (user instanceof PropertyNode)
	// user.setName(n);
	// }
	// }

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
