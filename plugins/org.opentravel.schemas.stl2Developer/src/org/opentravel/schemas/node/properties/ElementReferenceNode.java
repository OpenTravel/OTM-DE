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

import javax.xml.namespace.QName;

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemacompiler.codegen.util.PropertyCodegenUtils;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemas.modelObject.ElementPropertyMO;
import org.opentravel.schemas.node.BusinessObjectNode;
import org.opentravel.schemas.node.ComponentNodeType;
import org.opentravel.schemas.node.CoreObjectNode;
import org.opentravel.schemas.node.ImpliedNode;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFactory;
import org.opentravel.schemas.node.NodeNameUtils;
import org.opentravel.schemas.node.PropertyNodeType;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.types.TypeProvider;

/**
 * A property node that represents an XML element. See {@link NodeFactory#newComponentMember(INode, Object)}
 * 
 * @author Dave Hollander
 * 
 */
public class ElementReferenceNode extends PropertyNode {

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
		setAssignedType((TypeProvider) ModelNode.getUnassignedNode());
	}

	/**
	 * Create an element node from the TL Model object.
	 * 
	 * @param tlObj
	 *            TL Model object to represent
	 * @param parent
	 *            if not null, add element to the parent.
	 */
	public ElementReferenceNode(TLProperty tlObj, PropertyOwnerInterface parent) {
		super(tlObj, (INode) parent, PropertyNodeType.ID_REFERENCE);

		assert (modelObject instanceof ElementPropertyMO);
	}

	@Override
	public TLProperty getTLModelObject() {
		return (TLProperty) getModelObject().getTLModelObj();
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
		// int index = indexOfNode();
		TLProperty tlObj = (TLProperty) cloneTLObj();
		tlObj.setReference(true);
		// getTLModelObject().getOwner().addElement(index, tlObj); // bad index
		getTLModelObject().getOwner().addElement(tlObj);
		ElementReferenceNode n = new ElementReferenceNode(tlObj, null);
		n.setName(type.getName());
		// getParent().linkChild(n, indexOfNode());
		getParent().linkChild(n);
		n.setDescription(type.getDescription());
		if (type instanceof TypeProvider)
			n.setAssignedType((TypeProvider) type);
		return n;
	}

	@Override
	public ComponentNodeType getComponentNodeType() {
		return ComponentNodeType.ELEMENT_REF;
	}

	@Override
	public Image getImage() {
		return Images.getImageRegistry().get(Images.ID_Reference);
	}

	@Override
	public String getLabel() {
		return getName();
		// return modelObject.getLabel();
	}

	@Override
	public String getName() {
		return getTLModelObject().getName();
	}

	@Override
	public int indexOfTLProperty() {
		final TLProperty thisProp = (TLProperty) getTLModelObject();
		return thisProp.getOwner().getElements().indexOf(thisProp);
	}

	@Override
	public void setName(String name) {
		QName ln = PropertyCodegenUtils.getDefaultSchemaElementName((NamedEntity) getAssignedTLObject(), true);
		if (ln == null || getType() == null || (getType() instanceof ImpliedNode))
			getTLModelObject().setName(NodeNameUtils.fixElementRefName(name));
		else {
			getTLModelObject().setName(ln.getLocalPart());
		}
		// 2/8/2016 - dmh - use codegen utils to get the facet names correct.
		// if (getType() == null || (getType() instanceof ImpliedNode))
		// modelObject.setName(NodeNameUtils.fixElementRefName(name));
		// else
		// modelObject.setName(NodeNameUtils.fixElementRefName(getType().getName()));
	}

}
