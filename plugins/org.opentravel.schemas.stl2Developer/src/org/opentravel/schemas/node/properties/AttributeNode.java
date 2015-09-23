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
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemas.node.AliasNode;
import org.opentravel.schemas.node.INode;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFactory;
import org.opentravel.schemas.node.NodeNameUtils;
import org.opentravel.schemas.node.PropertyNodeType;
import org.opentravel.schemas.node.VWA_Node;
import org.opentravel.schemas.node.properties.EqExOneValueHandler.ValueWithContextType;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeUser;

/**
 * A property node that represents an XML attribute. See {@link NodeFactory#newComponentMember(INode, Object)}
 * 
 * @author Dave Hollander
 * 
 */
public class AttributeNode extends PropertyNode implements TypeUser {

	public AttributeNode(Node parent, String name) {
		super(new TLAttribute(), parent, name, PropertyNodeType.ATTRIBUTE);
		setAssignedType(ModelNode.getUnassignedNode());
	}

	public AttributeNode(Node parent, String name, PropertyNodeType type) {
		super(new TLAttribute(), parent, name, type);
		setAssignedType(ModelNode.getUnassignedNode());
	}

	public AttributeNode(TLModelElement tlObj, INode parent) {
		super(tlObj, parent, PropertyNodeType.ATTRIBUTE);

		if (!(tlObj instanceof TLAttribute))
			throw new IllegalArgumentException("Invalid object for an attribute.");
	}

	/*
	 * used for sub-types
	 */
	public AttributeNode(TLModelElement tlObj, INode parent, PropertyNodeType type) {
		super(tlObj, parent, type);
	}

	@Override
	public boolean canAssign(Node type) {
		if (super.canAssign(type)) {
			TypeProvider provider = (TypeProvider) type;

			// GUI assist: aliases stand in for their base type on attributes.
			if (type instanceof AliasNode)
				provider = (TypeProvider) type.getOwningComponent();

			if (getOwningComponent() instanceof VWA_Node)
				return provider.isAssignableToVWA();
			else
				return provider.isAssignableToSimple();
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.PropertyNode#createProperty(org.opentravel.schemas.node.Node)
	 */
	@Override
	public INode createProperty(Node type) {
		if (!(getTLModelObject() instanceof TLAttribute))
			throw new IllegalArgumentException("Invalid object for an attribute.");

		TLAttribute tlObj = (TLAttribute) cloneTLObj();
		int index = indexOfNode();
		Node n = new AttributeNode(tlObj, null);
		((TLAttribute) getTLModelObject()).getAttributeOwner().addAttribute(index, tlObj);
		n.setName(type.getName(), false);
		getParent().linkChild(n, index);
		n.setDescription(type.getDescription());
		n.setAssignedType(type);
		return n;
	}

	@Override
	public IValueWithContextHandler setEquivalent(String example) {
		if (equivalentHandler == null)
			equivalentHandler = new EqExOneValueHandler(this, ValueWithContextType.EQUIVALENT);
		equivalentHandler.set(example, null);
		return equivalentHandler;
	}

	@Override
	public IValueWithContextHandler setExample(String example) {
		if (exampleHandler == null)
			exampleHandler = new EqExOneValueHandler(this, ValueWithContextType.EXAMPLE);
		exampleHandler.set(example, null);
		return exampleHandler;
	}

	@Override
	public Image getImage() {
		return Images.getImageRegistry().get(Images.XSDAttribute);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.INode#getLabel()
	 */
	@Override
	public String getLabel() {
		String label = modelObject.getLabel();
		if (getType() != null)
			label = getName() + " [" + getTypeNameWithPrefix() + "]";
		return label;
	}

	@Override
	public int indexOfTLProperty() {
		final TLAttribute thisProp = (TLAttribute) getTLModelObject();
		return thisProp.getAttributeOwner().getAttributes().indexOf(thisProp);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.Node#isSimpleTypeUser()
	 */
	@Override
	public boolean isOnlySimpleTypeUser() {
		// allow VWAs to be assigned to VWA Attributes.
		return parent != null && parent.isVWA_AttributeFacet() ? false : true;
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
		modelObject.setName(NodeNameUtils.fixAttributeName(name));
	}

}
