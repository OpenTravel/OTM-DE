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
package org.opentravel.schemas.node.handlers.children;

import java.util.ArrayList;
import java.util.List;

import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.facets.AttributeFacetNode;
import org.opentravel.schemas.node.objectMembers.VWA_SimpleFacetFacadeNode;
import org.opentravel.schemas.node.typeProviders.VWA_Node;

/**
 * Value With Attribute Model Object.
 * 
 * Provide an interface to the TLValueWithAttributes model object. TLValueWithAttributes does not use facets to contain
 * simple type and attributes, so this model class must adapt.
 * 
 * @author Dave Hollander
 * 
 */
public class ValueWithAttributesChildrenHandler extends CachingChildrenHandler<Node, VWA_Node> {
	// private final static Logger LOGGER = LoggerFactory.getLogger(ValueWithAttributesMO.class);

	public ValueWithAttributesChildrenHandler(final VWA_Node obj) {
		super(obj);
	}

	// Since the TL Model and Node model are so different do the mapping here.
	@Override
	public void initChildren() {
		initRunning = true;
		children = new ArrayList<>();
		children.add(new VWA_SimpleFacetFacadeNode(owner));
		children.add(new AttributeFacetNode(owner));
		initRunning = false;
	}

	@Override
	public List<TLModelElement> getChildren_TL() {
		// return the two facets: value type and attributes.
		final List<TLModelElement> kids = new ArrayList<>();
		owner.getTLModelObject().getParentType();
		owner.getTLModelObject().getAttributes();
		owner.getTLModelObject().getIndicators();
		return kids;
	}

	/**
	 * Override to provide where used when appropriate. Needed because this object has no navChildren.
	 */
	@Override
	public boolean hasTreeChildren(boolean deep) {
		return owner.getWhereUsedCount() > 0 ? true : false;
	}

}
