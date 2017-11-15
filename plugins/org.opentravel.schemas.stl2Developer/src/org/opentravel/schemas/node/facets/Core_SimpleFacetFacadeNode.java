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
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemas.node.CoreObjectNode;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.listeners.ListenerFactory;
import org.opentravel.schemas.node.properties.Core_SimpleAttributeFacadeNode;
import org.opentravel.schemas.node.properties.SimpleAttributeFacadeNode;
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
public class Core_SimpleFacetFacadeNode extends SimpleFacetFacadeNode implements TypeProvider, SimpleAttributeOwner {
	private static final Logger LOGGER = LoggerFactory.getLogger(FacetNode.class);

	public Core_SimpleFacetFacadeNode(CoreObjectNode owner) {
		super(owner);

		// Set the tlObj to get the listener correct
		ListenerFactory.setIdentityListner(this, owner.getTLModelObject().getSimpleFacet());

		// Set the tlObj to be the TLCore
		tlObj = owner.getTLModelObject();
	}

	@Override
	public SimpleAttributeFacadeNode createAttributeFacade() {
		return new Core_SimpleAttributeFacadeNode(this);
	}

	@Override
	public CoreObjectNode getParent() {
		return (CoreObjectNode) parent;
	}

	@Override
	public boolean isAssignableToSimple() {
		return !getParent().getAssignedType().equals(ModelNode.getEmptyNode());
	}

	@Override
	public NamedEntity getAssignedTLNamedEntity() {
		return getTLModelObject() != null ? getTLModelObject().getSimpleFacet().getSimpleType() : null;
	}

	@Override
	public String getName() {
		return emptyIfNull(getTLModelObject().getLocalName());
	}

	@Override
	public TLCoreObject getTLModelObject() {
		return (TLCoreObject) tlObj;
	}

}
