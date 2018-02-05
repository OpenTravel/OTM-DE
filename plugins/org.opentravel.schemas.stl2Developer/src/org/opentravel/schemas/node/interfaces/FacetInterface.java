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
package org.opentravel.schemas.node.interfaces;

import java.util.List;

import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.handlers.children.ChildrenHandlerI;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.properties.PropertyNodeType;

/**
 * Nodes implementing this interface have structural facets (attributes, summary, detail, default).
 * 
 * @author Dave Hollander
 * 
 */
public interface FacetInterface {

	public void add(List<PropertyNode> properties, boolean clone);

	public void add(PropertyNode property);

	/**
	 * Add property to this facet.
	 * 
	 * @param pn
	 *            property to add
	 * @param index
	 *            place in property list where to attempt to addd
	 */
	public void add(PropertyNode pn, int index);

	/**
	 * Return true if this facet can contain the passed property node.
	 */
	public boolean canOwn(PropertyNode pn);

	/**
	 * Return true if this facet can contain the passed property node type.
	 */
	public boolean canOwn(PropertyNodeType type);

	/**
	 * @param child
	 * @return
	 */
	public boolean contains(Node child);

	/**
	 * Make a copy of all the properties of the source facet and add to this facet. If the property is of the wrong
	 * type, it is changed into an attribute.
	 * 
	 * @param facet
	 */
	@Deprecated
	public void copy(FacetInterface facet);

	/**
	 * Drag-n-drop behavior. Either assign type if passed type is unassigned or else clone property and assign type
	 * 
	 * @param type
	 *            - type to assign
	 * @return newly create property node
	 */
	// FIXME - this dual behavior should be in controller
	public PropertyNode createProperty(Node type);

	public PropertyNode get(String name);

	/**
	 * Get a list of children nodes, may include other facets
	 */
	public List<Node> getChildren();

	/**
	 * Node Factory needs to use the handler to set listener.
	 * 
	 * @return children handler for the facet
	 */
	public ChildrenHandlerI<?> getChildrenHandler();

	public TLFacetType getFacetType();

	/**
	 * @return
	 */
	public String getName();

	/**
	 * @return
	 */
	public LibraryMemberInterface getOwningComponent();

	/**
	 * Get a list of children property nodes
	 */
	public List<PropertyNode> getProperties();

	/**
	 * @return the TL Model Element underlying this facet or null
	 */
	public TLModelElement getTLModelObject();

	/**
	 * @return true if an extension point can extend this facet
	 */
	public boolean isExtensionPointTarget();

	public boolean isFacet(TLFacetType facetType);

	/**
	 * @return
	 */
	public boolean isInherited();

	/**
	 * @param selected
	 */
	public void removeProperty(PropertyNode pn);

}
