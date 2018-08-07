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
package org.opentravel.schemas.utils;

import org.opentravel.schemacompiler.ic.TypeNameIntegrityChecker;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.NodeFactory;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.properties.PropertyNodeType;
import org.opentravel.schemas.node.typeProviders.VWA_Node;
import org.opentravel.schemas.node.typeProviders.facetOwners.BusinessObjectNode;
import org.opentravel.schemas.node.typeProviders.facetOwners.CoreObjectNode;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeUser;

/**
 * @author Pawel Jedruch
 * 
 */
public class PropertyNodeBuilder {

	private final PropertyNode propertyNode;

	public PropertyNodeBuilder(PropertyNode propertyNode) {
		this.propertyNode = propertyNode;
	}

	public static PropertyNodeBuilder create(PropertyNodeType elementType) {
		Object tlObject = creatTLObject(elementType);
		ComponentNode newComponentMember = NodeFactory.newChild(null, (TLModelElement) tlObject);
		if (newComponentMember instanceof PropertyNode) {
			return new PropertyNodeBuilder((PropertyNode) newComponentMember);
		} else {
			throw new IllegalArgumentException();
		}
	}

	@SuppressWarnings("incomplete-switch")
	private static Object creatTLObject(PropertyNodeType type) {
		TLProperty tl = null;
		switch (type) {
		case ELEMENT:
			tl = new TLProperty();
			tl.setReference(false);
			return tl;
		case ID_REFERENCE:
			tl = new TLProperty();
			tl.setReference(true);
			return tl;
		case ATTRIBUTE:
			return new TLAttribute();
		}
		return tl;
	}

	public PropertyNode build() {
		return propertyNode;
	}

	public PropertyNodeBuilder makeSimpleList(String name) {
		CoreObjectNode coreObject = ComponentNodeBuilder.createCoreObject(name).get();
		if (this.propertyNode instanceof TypeUser)
			((TypeUser) this.propertyNode).setAssignedType((TypeProvider) coreObject.getSimpleListFacet());
		return this;
	}

	public PropertyNodeBuilder makeDetailList(String name) {
		CoreObjectNode coreObject = ComponentNodeBuilder.createCoreObject(name).get();
		if (this.propertyNode instanceof TypeUser)
			((TypeUser) this.propertyNode).setAssignedType((TypeProvider) coreObject.getDetailListFacet());
		return this;
	}

	public PropertyNodeBuilder setName(String typeName) {
		propertyNode.setName(typeName);
		return this;
	}

	public PropertyNodeBuilder assignCoreObject(String name) {
		CoreObjectNode coreObject = ComponentNodeBuilder.createCoreObject(name).get();
		if (this.propertyNode instanceof TypeUser)
			((TypeUser) this.propertyNode).setAssignedType(coreObject);
		return this;
	}

	public PropertyNodeBuilder assignBuisnessObject(String name) {
		BusinessObjectNode business = ComponentNodeBuilder.createBusinessObject(name).get();
		if (this.propertyNode instanceof TypeUser)
			((TypeUser) this.propertyNode).setAssignedType(business);
		return this;
	}

	/**
	 * Make sure that before calling this the {@link ModelNode} is created with valid TLModel
	 */
	public PropertyNodeBuilder assignVWA(String name) {
		VWA_Node coreObject = ComponentNodeBuilder.createVWA(name).get();
		if (this.propertyNode instanceof TypeUser)
			((TypeUser) this.propertyNode).setAssignedType(coreObject);
		return this;
	}

	/**
	 * for proper assigned propagation, make sure before calling this method the TLModel can return getOwningModel(). If
	 * it will return null then the {@link TypeNameIntegrityChecker} can not be called. One one to make sure that
	 * TLModel can find owning model is to before call assign execute
	 * {@link PropertyNodeBuilder#addToComponent(ComponentNode)}.
	 * 
	 * @param type
	 * @return
	 */
	public PropertyNodeBuilder assign(ComponentNode type) {
		if (propertyNode instanceof TypeUser && type instanceof TypeProvider)
			((TypeUser) propertyNode).setAssignedType((TypeProvider) type);
		// propertyNode.getTypeClass().setAssignedType(type);
		return this;
	}

	public PropertyNodeBuilder setDescription(String string) {
		propertyNode.setDescription(string);
		return this;
	}

	public PropertyNodeBuilder setDocumentation(TLDocumentation documentation) {
		propertyNode.setDocumentation(documentation);
		return this;
	}

	public PropertyNodeBuilder addToComponent(ComponentNode node) {
		node.addProperty(propertyNode);
		return this;
	}

}
