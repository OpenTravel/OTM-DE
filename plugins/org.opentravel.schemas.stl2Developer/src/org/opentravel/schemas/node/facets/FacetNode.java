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
package org.opentravel.schemas.node.facets;

import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemacompiler.codegen.util.XsdCodegenUtils;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemas.node.AliasNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.VWA_Node;
import org.opentravel.schemas.node.handlers.children.FacetChildrenHandler;
import org.opentravel.schemas.node.interfaces.AliasOwner;
import org.opentravel.schemas.node.interfaces.Sortable;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeUser;
import org.opentravel.schemas.utils.StringComparator;

/**
 * Facets are containers for properties (elements and attributes) as well as simple facets for core and VWA objects.
 * Operation RQ, RS and Notif messages are also modeled with facets. (See library sorter for all the variations in facet
 * types.)
 * 
 * @author Dave Hollander
 * 
 */
public class FacetNode extends PropertyOwnerNode implements TypeProvider, Sortable, AliasOwner {
	// private static final Logger LOGGER = LoggerFactory.getLogger(FacetNode.class);

	public FacetNode() {
		// this(new TLFacet());
	}

	public FacetNode(final TLFacet obj) {
		super(obj);

		childrenHandler = new FacetChildrenHandler(this);
		if (!isInherited())
			assert Node.GetNode(getTLModelObject()) == this;
	}

	@Override
	public void addAlias(AliasNode alias) {
		// if (!getTLModelObject().getAliases().contains(alias.getTLModelObject()))
		// getTLModelObject().addAlias(alias.getTLModelObject());
		getChildrenHandler().clear();
	}

	@Override
	public void remove(AliasNode alias) {
		// Not sure how-to or if i need-to associate this alias with owning compnent's
		getChildrenHandler().clear();
	}

	@Override
	public AliasNode addAlias(String name) {
		// NO-OP
		return null;
	}

	@Override
	public void cloneAliases(List<AliasNode> aliases) {
		// NO-OP
	}

	@Override
	public void deleteTL() {
		getTLModelObject().clearFacet();
	}

	@Override
	public String getComponentType() {
		TLFacetType facetType = getTLModelObject().getFacetType();
		if (facetType == null)
			return "FIXME-delegate needed???";
		return facetType.getIdentityName();
	}

	@Override
	public TLFacet getTLModelObject() {
		return (TLFacet) tlObj;
		// return (TLFacet) modelObject.getTLModelObj();
	}

	@Override
	public TLFacetType getFacetType() {
		return getTLModelObject() != null ? getTLModelObject().getFacetType() : null;
	}

	@Override
	public Image getImage() {
		return Images.getImageRegistry().get(Images.Facet);
	}

	@Override
	public LibraryNode getLibrary() {
		if (getOwningComponent() == null || getOwningComponent() == this)
			return null;
		return getOwningComponent().getLibrary();
	}

	@Override
	public String getPropertyRole() {
		return getComponentType();
	}

	@Override
	public String getLabel() {
		if (isInherited())
			return getComponentType() + " (Inherited)";
		return getComponentType();
	}

	@Override
	public String getName() {
		return XsdCodegenUtils.getSubstitutableElementName(getTLModelObject()).getLocalPart();
	}

	@Override
	public boolean isDefaultFacet() {
		// FIXME - should never be VWA
		if (getOwningComponent() instanceof VWA_Node)
			assert false;
		// return true;
		return getTLModelObject().getFacetType() == TLFacetType.SUMMARY;
	}

	public boolean isDetailFacet() {
		return getFacetType() != null ? getFacetType().equals(TLFacetType.DETAIL) : false;
	}

	public boolean isIDFacet() {
		return getFacetType() != null ? getFacetType().equals(TLFacetType.ID) : false;
	}

	@Override
	public boolean isEnabled_AddProperties() {
		return getOwningComponent().isEnabled_AddProperties();
	}

	// @Override
	// public boolean isNamedType() {
	// return false;
	// }

	@Override
	public boolean isRenameable() {
		return false;
	}

	/**
	 * Facets assigned to core object list types have no model objects but may be page1-assignable.
	 */
	@Override
	public boolean isSimpleAssignable() {
		return false;
	}

	public boolean isSummaryFacet() {
		return getFacetType() != null ? getFacetType().equals(TLFacetType.SUMMARY) : false;
	}

	/**
	 * Set name to type users where this alias is assigned. Only if the parent is type that requires assigned users to
	 * use the owner's name
	 */
	@Override
	public void setNameOnWhereAssigned(String n) {
		if (getParent() instanceof TypeProvider && !((TypeProvider) getParent()).isRenameableWhereUsed())
			for (TypeUser u : getWhereAssigned())
				u.setName(n);
	}

	@Override
	public void sort() {
		// Collections.sort(getChildren(), new StringComparator<Node>() {
		// @Override
		// protected String getString(Node object) {
		// return object.getName();
		// }
		// });

		// Now sort the TL lists
		getTLModelObject().sortIndicators(new StringComparator<TLIndicator>() {
			@Override
			protected String getString(TLIndicator object) {
				return object.getName();
			}
		});
		getTLModelObject().sortAttributes(new StringComparator<TLAttribute>() {
			@Override
			protected String getString(TLAttribute object) {
				return object.getName();
			}
		});
		getTLModelObject().sortElements(new StringComparator<TLProperty>() {
			@Override
			protected String getString(TLProperty object) {
				return object.getName();
			}
		});

		getChildrenHandler().clear();
		// modelObject.sort();
	}

	// /**
	// * Add nodes for all TLAliases that do not have nodes. This ONLY adds a node for an existing TLFacet. It does not
	// * create a TLFacet.
	// *
	// * @param name
	// */
	// public void updateAliasNodes() {
	// List<String> knownAliases = new ArrayList<String>();
	// for (Node n : getChildren()) {
	// if (n instanceof AliasNode)
	// knownAliases.add(n.getName());
	// }
	// // model object can be null for contributed facets
	// if (getTLModelObject() != null)
	// for (TLAlias tla : getTLModelObject().getAliases()) {
	// if (!knownAliases.contains(tla.getName())) {
	// new AliasNode(this, tla);
	// knownAliases.add(tla.getName());
	// }
	// }
	// }

	/**
	 * Get the property with the passed name or null.
	 * 
	 * @param string
	 * @return
	 */
	public PropertyNode get(String name) {
		for (Node n : getChildren())
			if (n.getName().equals(name))
				return (PropertyNode) n;
		return null;
	}

}
