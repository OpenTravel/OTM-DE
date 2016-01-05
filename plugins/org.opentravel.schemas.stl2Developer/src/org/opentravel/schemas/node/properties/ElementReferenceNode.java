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

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemas.node.BusinessObjectNode;
import org.opentravel.schemas.node.CoreObjectNode;
import org.opentravel.schemas.node.ImpliedNode;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFactory;
import org.opentravel.schemas.node.NodeNameUtils;
import org.opentravel.schemas.node.PropertyNodeType;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.types.TypeUser;

/**
 * A property node that represents an XML element. See {@link NodeFactory#newComponentMember(INode, Object)}
 * 
 * @author Dave Hollander
 * 
 */
public class ElementReferenceNode extends PropertyNode implements TypeUser {

	/**
	 * Add an element reference property to a facet or extension point.
	 * 
	 * @param parent
	 *            - if null, the caller must link the node and add to TL Model parent
	 * @param name
	 */

	public ElementReferenceNode(PropertyOwnerInterface parent, String name) {
		super(new TLProperty(), (Node) parent, name, PropertyNodeType.ID_REFERENCE);
		((TLProperty) getTLModelObject()).setReference(true);
		setAssignedType(ModelNode.getUnassignedNode());
	}

	/**
	 * Create an element node from the TL Model object.
	 * 
	 * @param tlObj
	 *            TL Model object to represent
	 * @param parent
	 *            if not null, add element to the parent.
	 */
	public ElementReferenceNode(TLModelElement tlObj, PropertyOwnerInterface parent) {
		super(tlObj, (INode) parent, PropertyNodeType.ID_REFERENCE);
	}

	@Override
	public boolean canAssign(Node type) {
		if (type.getOwningComponent() instanceof BusinessObjectNode)
			return true;
		if (type.getOwningComponent() instanceof CoreObjectNode)
			return true;
		if (type instanceof ImpliedNode)
			return true;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.PropertyNode#createProperty(org.opentravel.schemas.node.Node)
	 */
	@Override
	public INode createProperty(Node type) {
		int index = indexOfNode();
		TLProperty tlObj = (TLProperty) cloneTLObj();
		tlObj.setReference(true);
		((TLProperty) getTLModelObject()).getPropertyOwner().addElement(index, tlObj);
		ElementReferenceNode n = new ElementReferenceNode(tlObj, null);
		n.setName(type.getName());
		getParent().linkChild(n, indexOfNode());
		n.setDescription(type.getDescription());
		n.setAssignedType(type);
		return n;
	}

	@Override
	public Image getImage() {
		return Images.getImageRegistry().get(Images.ID_Reference);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.INode#getLabel()
	 */
	@Override
	public String getLabel() {
		return modelObject.getLabel();
	}

	@Override
	public int indexOfTLProperty() {
		final TLProperty thisProp = (TLProperty) getTLModelObject();
		return thisProp.getPropertyOwner().getElements().indexOf(thisProp);
	}

	@Override
	public boolean isElement() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.Node#isTypeUser()
	 */
	@Override
	public boolean isTypeUser() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.PropertyNode#setName(java.lang.String)
	 */
	@Override
	public void setName(String name) {
		if (getType() == null || (getType() instanceof ImpliedNode))
			modelObject.setName(NodeNameUtils.fixElementRefName(name));
		else
			modelObject.setName(NodeNameUtils.fixElementRefName(getType().getName()));
	}

}
