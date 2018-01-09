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
package org.opentravel.schemas.node.objectMembers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemacompiler.codegen.util.XsdCodegenUtils;
import org.opentravel.schemacompiler.event.ModelElementListener;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.handlers.children.FacetMemberChildrenHandler;
import org.opentravel.schemas.node.interfaces.FacetInterface;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.interfaces.Sortable;
import org.opentravel.schemas.node.listeners.InheritanceDependencyListener;
import org.opentravel.schemas.node.properties.AttributeNode;
import org.opentravel.schemas.node.properties.ElementNode;
import org.opentravel.schemas.node.properties.IndicatorNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.utils.StringComparator;

/**
 * Facets are containers for properties. These facets are not type providers.
 * 
 * @author Dave Hollander
 * 
 */
public abstract class FacetOMNode extends NonTypeProviders implements FacetInterface, Sortable {
	// private static final Logger LOGGER = LoggerFactory.getLogger(FacetNode.class);

	public FacetOMNode() {
		// this(new TLFacet());
	}

	public FacetOMNode(final TLFacet obj) {
		super(obj);

		childrenHandler = new FacetMemberChildrenHandler(this);
		if (!isInherited())
			assert Node.GetNode(getTLModelObject()) == this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.interfaces.FacetInterface#add(java.util.List, boolean)
	 */
	@Override
	public void add(List<PropertyNode> properties, boolean clone) {
		for (PropertyNode np : properties) {
			if (clone)
				np = np.clone(this, null);
			// if (canOwn(np.getPropertyType()))
			add(np);
		}
	}

	@Override
	public void removeProperty(PropertyNode pn) {
		if (pn instanceof ElementNode)
			getTLModelObject().removeProperty(((ElementNode) pn).getTLModelObject());
		else if (pn instanceof AttributeNode)
			getTLModelObject().removeAttribute(((AttributeNode) pn).getTLModelObject());
		else if (pn instanceof IndicatorNode)
			getTLModelObject().removeIndicator(((IndicatorNode) pn).getTLModelObject());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.opentravel.schemas.node.interfaces.FacetInterface#add(org.opentravel.schemas.node.properties.PropertyNode)
	 */
	@Override
	public void add(PropertyNode property) {
		add(property, -1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.opentravel.schemas.node.interfaces.FacetInterface#add(org.opentravel.schemas.node.properties.PropertyNode,
	 * int)
	 */
	@Override
	public void add(PropertyNode pn, int index) {
		// Add to children list
		pn.setParent(this);

		// Add to the tl model
		if (index < 0)
			pn.addToTL(this);
		else
			pn.addToTL(this, index);

		// // Events are not being thrown (10/14/2017) so force their result
		// childrenHandler.clear();
		// clear handlers on any inherited "ghost" facets
		for (ModelElementListener l : getTLModelObject().getListeners())
			if (l instanceof InheritanceDependencyListener)
				((InheritanceDependencyListener) l).run();
	}

	// @Override
	// public String getComponentType() {
	// TLFacetType facetType = getTLModelObject().getFacetType();
	// if (facetType == null)
	// return "FIXME-delegate needed???";
	// return facetType.getIdentityName();
	// }

	/**
	 * @param type
	 * @return
	 */
	public boolean canOwn(TLFacetType type) {
		return false;
	}

	/**
	 * Copy into this facet all the members of the passed facet.
	 * 
	 * @param facet
	 */
	@Override
	public void copy(FacetInterface facet) {
		// TODO Auto-generated method stub

	}

	// @Override
	// public LibraryNode getLibrary() {
	// return getOwningComponent() == null ? null : getOwningComponent().getLibrary();
	// }

	@Override
	public PropertyNode createProperty(Node type) {
		// TODO
		return null;
	}

	@Override
	public void deleteTL() {
		getTLModelObject().clearFacet();
	}

	@Override
	public PropertyNode findChildByName(String name) {
		return get(name);
	}

	// @Override
	// public boolean isDefaultFacet() {
	// // FIXME - should never be VWA
	// if (getOwningComponent() instanceof VWA_Node)
	// assert false;
	// // return true;
	// return getTLModelObject().getFacetType() == TLFacetType.SUMMARY;
	// }
	//
	// @Override
	// public boolean isDetailFacet() {
	// return getFacetType() != null ? getFacetType().equals(TLFacetType.DETAIL) : false;
	// }
	//
	// @Override
	// public boolean isIDFacet() {
	// return getFacetType() != null ? getFacetType().equals(TLFacetType.ID) : false;
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.interfaces.FacetInterface#get()
	 */
	@Override
	public List<PropertyNode> getProperties() {
		List<PropertyNode> pns = new ArrayList<PropertyNode>();
		for (Node n : getChildrenHandler().get())
			if (n instanceof PropertyNode)
				pns.add((PropertyNode) n);
		return pns;
	}

	/**
	 * Get the property with the passed name or null.
	 * 
	 * @param string
	 * @return
	 */
	@Override
	public PropertyNode get(String name) {
		for (Node n : getChildrenHandler().get())
			if (n instanceof PropertyNode && n.getName().equals(name))
				return (PropertyNode) n;
		return null;
	}

	@Override
	public INode.CommandType getAddCommand() {
		return INode.CommandType.PROPERTY;
	}

	@Override
	public FacetMemberChildrenHandler getChildrenHandler() {
		return (FacetMemberChildrenHandler) childrenHandler;
	}

	// @Override
	// public boolean isSummaryFacet() {
	// return getFacetType() != null ? getFacetType().equals(TLFacetType.SUMMARY) : false;
	// }

	// /**
	// * Set name to type users where this alias is assigned. Only if the parent is type that requires assigned users to
	// * use the owner's name
	// */
	// @Override
	// public void setNameOnWhereAssigned(String n) {
	// if (getParent() instanceof TypeProvider && !((TypeProvider) getParent()).isRenameableWhereUsed())
	// for (TypeUser u : getWhereAssigned())
	// u.setName(n);
	// }
	@Override
	public String getComponentType() {
		return getFacetType().toString();
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
	public String getPropertyRole() {
		return getComponentType();
	}

	@Override
	public TLFacet getTLModelObject() {
		return (TLFacet) tlObj;
	}

	@Override
	public boolean isEnabled_AddProperties() {
		return getOwningComponent().isEnabled_AddProperties();
	}

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

	@Override
	public void sort() {

		// sort the TL lists
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
	}

	// /**
	// * @param targetCF
	// * @return
	// */
	// public abstract boolean canOwn(ContextualFacetNode targetCF);

}
