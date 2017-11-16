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
import org.opentravel.schemacompiler.model.TLAttributeType;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
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

// TODO - delegate as many methods as possible to PropertyNode
//
public class VWA_SimpleAttributeFacadeNode extends SimpleAttributeFacadeNode {
	// private static final Logger LOGGER = LoggerFactory.getLogger(SimpleAttributeNode.class);

	public VWA_SimpleAttributeFacadeNode(SimpleFacetFacadeNode parent) {
		super(parent);
	}

	@Override
	protected TypeProvider getTLSimpleType() {
		TypeProvider assignedType = null;
		if (getTLModelObject().getParentType() != null)
			assignedType = (TypeProvider) Node.GetNode((TLModelElement) getTLModelObject().getParentType());
		if (assignedType == null)
			assignedType = getEmptyNode();
		return assignedType;
	}

	@Override
	public boolean setAssignedType(TLModelElement simpleType) {
		if (getTLModelObject().getParentType() == simpleType)
			return false;
		if (!(simpleType instanceof TLAttributeType))
			return false;

		TLAttributeType ne = null;
		if (simpleType == null || !(simpleType instanceof TLAttributeType))
			if (emptyNode != null)
				ne = (TLAttributeType) emptyNode.getTLModelObject();
		ne = (TLAttributeType) simpleType;
		getTLModelObject().setParentType(ne);
		return getTLModelObject().getParentType() == ne;
	}

	@Override
	public String getName() {
		return emptyIfNull(getTLModelObject().getName());
	}

	@Override
	public TLValueWithAttributes getTLModelObject() {
		return (TLValueWithAttributes) tlObj;
	}

	@Override
	public NamedEntity getAssignedTLNamedEntity() {
		return getTLModelObject().getParentType();
	}

}
