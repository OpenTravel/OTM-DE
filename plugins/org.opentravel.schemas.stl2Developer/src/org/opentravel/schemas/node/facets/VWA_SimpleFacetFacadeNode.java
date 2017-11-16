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
package org.opentravel.schemas.node.facets;

import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemas.node.VWA_Node;
import org.opentravel.schemas.node.properties.SimpleAttributeFacadeNode;
import org.opentravel.schemas.node.properties.VWA_SimpleAttributeFacadeNode;
import org.opentravel.schemas.types.SimpleAttributeOwner;
import org.opentravel.schemas.types.TypeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple facets have only one attribute property. Used on ValueWithAtributes and Core Objects.
 * 
 * @author Dave Hollander
 * 
 */
public class VWA_SimpleFacetFacadeNode extends SimpleFacetFacadeNode implements TypeProvider, SimpleAttributeOwner {
	private static final Logger LOGGER = LoggerFactory.getLogger(VWA_SimpleFacetFacadeNode.class);

	public VWA_SimpleFacetFacadeNode(VWA_Node owner) {
		super(owner);

		// Set the tlObj to get the listener correct
		// tlObj = owner.getTLModelObject().;
		// ListenerFactory.setListner(this);

		// Now, set the tlObj to be the tl vwa
		tlObj = owner.getTLModelObject();

	}

	@Override
	public SimpleAttributeFacadeNode createAttributeFacade() {
		return new VWA_SimpleAttributeFacadeNode(this);
	}

	@Override
	public NamedEntity getAssignedTLNamedEntity() {
		return getTLModelObject() != null ? getTLModelObject().getParentType() : null;
	}

	@Override
	public String getName() {
		return "Value";
	}

	@Override
	public VWA_Node getParent() {
		return (VWA_Node) parent;
	}

	@Override
	public TLValueWithAttributes getTLModelObject() {
		return (TLValueWithAttributes) tlObj;
	}

	@Override
	public boolean isAssignableToSimple() {
		return false;
	}

}
