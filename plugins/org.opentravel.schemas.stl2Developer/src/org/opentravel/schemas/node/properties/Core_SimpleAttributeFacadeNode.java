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
package org.opentravel.schemas.node.properties;

import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFactory;
import org.opentravel.schemas.node.facets.SimpleFacetFacadeNode;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.types.TypeProvider;

/**
 * A property node that represents a simple property of a core or value with attributes objects. The TL Object is the
 * parent {@link SimpleFacetFacade}'s tlObject.
 * <p>
 * The type is never null, but may be the Empty type.
 * <p>
 * See {@link NodeFactory#newMemberOLD(INode, Object)}
 * 
 * @author Dave Hollander
 * 
 */
public class Core_SimpleAttributeFacadeNode extends SimpleAttributeFacadeNode {
	// private static final Logger LOGGER = LoggerFactory.getLogger(SimpleAttributeNode.class);

	public Core_SimpleAttributeFacadeNode(SimpleFacetFacadeNode parent) {
		super(parent);
	}

	@Override
	protected TypeProvider getTLSimpleType() {
		TypeProvider assignedType = null;
		// Get the assigned type directly from the TL Object
		NamedEntity tlNE = ((TLCoreObject) tlObj).getSimpleFacet().getSimpleType();
		if (tlNE instanceof TLModelElement)
			assignedType = (TypeProvider) Node.GetNode((TLModelElement) tlNE);
		if (assignedType == null)
			assignedType = getEmptyNode();
		return assignedType;
	}

	@Override
	public boolean setAssignedType(TLModelElement simpleType) {
		if (simpleType == getTLModelObject().getSimpleFacet().getSimpleType())
			return false;
		NamedEntity ne = null;
		if (simpleType == null || !(simpleType instanceof NamedEntity))
			ne = (NamedEntity) emptyNode.getTLModelObject();
		else
			ne = (NamedEntity) simpleType;
		getTLModelObject().getSimpleFacet().setSimpleType(ne);
		return getTLModelObject().getSimpleFacet().getSimpleType() == ne;
	}

	@Override
	public String getName() {
		return emptyIfNull(getTLModelObject().getName());
	}

	@Override
	public TLCoreObject getTLModelObject() {
		return (TLCoreObject) tlObj;
	}

	@Override
	public NamedEntity getAssignedTLNamedEntity() {
		return ((TLCoreObject) tlObj).getSimpleFacet().getSimpleType();
	}

	// @Override
	// public IValueWithContextHandler getEquivalentHandler() {
	// return null;
	// }
	//
	// @Override
	// public IValueWithContextHandler getExampleHandler() {
	// return null;
	// }

}
