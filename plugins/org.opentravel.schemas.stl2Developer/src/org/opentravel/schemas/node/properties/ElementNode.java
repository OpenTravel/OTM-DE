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
import org.opentravel.schemas.node.INode;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeEditStatus;
import org.opentravel.schemas.node.NodeFactory;
import org.opentravel.schemas.node.NodeNameUtils;
import org.opentravel.schemas.node.PropertyNodeType;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeUser;

/**
 * A property node that represents an XML element. See {@link NodeFactory#newComponentMember(INode, Object)}
 * 
 * @author Dave Hollander
 * 
 */

public class ElementNode extends PropertyNode implements TypeUser {

	/**
	 * Add an element property to a facet or extension point.
	 * 
	 * @param parent
	 *            - if null, the caller must link the node and add to TL Model parent
	 * @param name
	 */
	public ElementNode(Node parent, String name) {
		super(new TLProperty(), parent, name, PropertyNodeType.ELEMENT);
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
	public ElementNode(TLModelElement tlObj, INode parent) {
		super(tlObj, parent, PropertyNodeType.ELEMENT);
		if (getEditStatus().equals(NodeEditStatus.MINOR))
			setMandatory(false);
		else if (tlObj instanceof TLProperty)
			setMandatory(((TLProperty) tlObj).isMandatory()); // default value for properties
	}

	@Override
	public boolean canAssign(Node type) {
		return (type instanceof TypeProvider);
	}

	@Override
	public INode createProperty(Node type) {
		int index = indexOfNode();
		int tlIndex = indexOfTLProperty();
		TLProperty tlObj = (TLProperty) cloneTLObj();
		((TLProperty) getTLModelObject()).getPropertyOwner().addElement(tlIndex, tlObj);
		ElementNode n = new ElementNode(tlObj, null);
		getParent().linkChild(n, indexOfNode());
		n.setDescription(type.getDescription());
		n.setAssignedType(type);
		n.setName(type.getName());
		return n;
	}

	/**
	 * Get the index (0..sizeof()) of this property in the facet list.
	 */
	@Override
	public int indexOfTLProperty() {
		final TLProperty thisProp = (TLProperty) getTLModelObject();
		return thisProp.getPropertyOwner().getElements().indexOf(thisProp);
	}

	@Override
	public Image getImage() {
		return Images.getImageRegistry().get(Images.XSDElement);
	}

	@Override
	public EquivalentHander getEquivalentHandler() {
		return equivalentHandler == null ? new EquivalentHander(this) : equivalentHandler;
	}

	@Override
	public ExampleHandler getExampleHandler() {
		return exampleHandler == null ? new ExampleHandler(this) : exampleHandler;
	}

	@Override
	public String getLabel() {
		String label = modelObject.getLabel();
		if (getType() != null)
			label = getName() + " [" + getTypeNameWithPrefix() + "]";
		return label;
	}

	@Override
	public boolean isElement() {
		return true;
	}

	@Override
	public boolean isTypeUser() {
		return true;
	}

	@Override
	public void setName(String name) {
		modelObject.setName(name); // try the passed name
		modelObject.setName(NodeNameUtils.fixElementName(this)); // let utils fix it if needed.
	}

	@Override
	public void setName(String name, boolean doFamily) {
		setName(name);
	}

}
