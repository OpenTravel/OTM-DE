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
package org.opentravel.schemas.node.typeProviders;

import java.util.ArrayList;
import java.util.List;

import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.interfaces.FacetInterface;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.types.WhereAssignedHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for <b>all</b> facets that are type providers but not library members.
 * 
 * @author Dave Hollander
 * 
 */
public abstract class FacetProviders extends TypeProviders implements FacetInterface {
	private static final Logger LOGGER = LoggerFactory.getLogger(FacetProviders.class);

	public FacetProviders() {
		// For types that have no TL
		whereAssignedHandler = new WhereAssignedHandler(this);
	}

	public FacetProviders(final TLModelElement obj) {
		super(obj);
		whereAssignedHandler = new WhereAssignedHandler(this);
		if (!isInherited())
			assert Node.GetNode(getTLModelObject()) == this;
	}

	@Override
	public boolean isSimpleAssignable() {
		return false;
	}

	@Override
	public INode.CommandType getAddCommand() {
		return INode.CommandType.PROPERTY;
	}

	@Override
	public LibraryMemberInterface getOwningComponent() {
		return (LibraryMemberInterface) getParent();
	}

	// @Override
	@Deprecated
	public boolean isSummaryFacet() {
		return getFacetType() != null ? getFacetType().equals(TLFacetType.SUMMARY) : false;
	}

	@Override
	public boolean isFacet(TLFacetType type) {
		return getFacetType() != null ? getFacetType().equals(type) : false;
	}

	@Override
	public PropertyNode get(String name) {
		for (Node n : getProperties())
			if (n instanceof PropertyNode && n.getName().equals(name))
				return (PropertyNode) n;
		return null;
	}

	@Override
	public abstract TLModelElement getTLModelObject();

	@Override
	public List<PropertyNode> getProperties() {
		List<PropertyNode> pns = new ArrayList<PropertyNode>();
		for (Node n : getChildrenHandler().get())
			if (n instanceof PropertyNode)
				pns.add((PropertyNode) n);
		return pns;
	}

	/**
	 * Make a copy of all the properties of the source facet and add to this facet. If the property is of the wrong
	 * type, it is changed into an attribute.
	 * 
	 * @param facet
	 */
	@Override
	public void copy(FacetInterface facet) {
		add(facet.getProperties(), true);
	}

	@Override
	public abstract PropertyNode createProperty(Node type);

}
