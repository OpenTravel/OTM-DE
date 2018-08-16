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
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLSimpleFacet;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.typeProviders.CoreSimpleFacetNode;
import org.opentravel.schemas.types.TypeProvider;

/**
 * A property node that represents the simple property of core objects.
 * <p>
 * The TL Object is the parent's {@link CoreSimpleFacetNode}'s tlObject.
 * <p>
 * The type is never null, but may be the Empty type.
 * 
 * @author Dave Hollander
 * 
 */
public class CoreSimpleAttributeFacadeNode extends SimpleAttributeFacadeNode {
	// private static final Logger LOGGER = LoggerFactory.getLogger(SimpleAttributeNode.class);

	public CoreSimpleAttributeFacadeNode(CoreSimpleFacetNode parent) {
		super(parent);
	}

	@Override
	public NamedEntity getAssignedTLNamedEntity() {
		return getTLModelObject() != null ? getTLModelObject().getSimpleType() : null;
	}

	@Override
	public String getName() {
		return getParent() != null ? emptyIfNull(getParent().getName()) : "";
	}

	@Override
	public CoreSimpleFacetNode getParent() {
		return (CoreSimpleFacetNode) parent;
	}

	@Override
	public TLSimpleFacet getTLModelObject() {
		return getParent() != null ? getParent().getTLModelObject() : null;
	}

	@Override
	protected TypeProvider getTLSimpleType() {
		TypeProvider assignedType = null;
		// Get the assigned type directly from the TL Object
		NamedEntity tlNE = getTLModelObject().getSimpleType();
		if (tlNE instanceof TLModelElement)
			assignedType = (TypeProvider) Node.GetNode(tlNE);
		if (assignedType == null)
			assignedType = getEmptyNode();
		return assignedType;
	}

	@Override
	public void removeAssignedTLType() {
		setAssignedType();
		getTLModelObject().setSimpleType(null);
	}

	@Override
	public boolean setAssignedTLType(TLModelElement simpleType) {
		if (getTLModelObject() == null || simpleType == getTLModelObject().getSimpleType())
			return false;
		NamedEntity ne = null;
		if (simpleType == null || !(simpleType instanceof NamedEntity))
			ne = (NamedEntity) getEmptyNode().getTLModelObject();
		else
			ne = (NamedEntity) simpleType;
		getTLModelObject().setSimpleType(ne);
		return getTLModelObject().getSimpleType() == ne;
	}
}
