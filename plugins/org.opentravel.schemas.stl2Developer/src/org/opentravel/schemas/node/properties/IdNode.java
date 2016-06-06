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

import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLAttributeType;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFactory;
import org.opentravel.schemas.node.NodeFinders;
import org.opentravel.schemas.node.PropertyNodeType;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.types.TypeProvider;

/**
 * A property node that represents an XML ID. See {@link NodeFactory#newComponentMember(INode, Object)}
 * 
 * @author Dave Hollander
 * 
 */
public class IdNode extends AttributeNode {
	TypeProvider idType = null;

	public IdNode(PropertyOwnerInterface parent, String name) {
		super(parent, name, PropertyNodeType.ID);
		setName("id");
		idType = (TypeProvider) NodeFinders.findNodeByName("ID", ModelNode.XSD_NAMESPACE);
		// does nothing because there is a required type. - setAssignedType(idType);
		((TLAttribute) getTLModelObject()).setType((TLAttributeType) idType.getTLModelObject());
		setIdentity("xml_ID on " + parent.getOwningComponent());
		propertyType = PropertyNodeType.ID;
	}

	public IdNode(TLModelElement tlObj, PropertyOwnerInterface parent) {
		super(tlObj, parent, PropertyNodeType.ID);
		idType = (TypeProvider) NodeFinders.findNodeByName("ID", ModelNode.XSD_NAMESPACE);
		// setAssignedType(idType);
		((TLAttribute) getTLModelObject()).setType((TLAttributeType) idType.getTLModelObject());
		setIdentity("xml_ID on " + getOwningComponent());
		propertyType = PropertyNodeType.ID;
	}

	@Override
	public boolean canAssign(Node type) {
		return (type == idType);
	}

	@Override
	public TypeProvider getRequiredType() {
		return idType;
	}
}
