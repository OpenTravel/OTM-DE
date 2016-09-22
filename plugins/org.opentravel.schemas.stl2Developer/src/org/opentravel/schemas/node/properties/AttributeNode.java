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
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFactory;
import org.opentravel.schemas.node.NodeNameUtils;
import org.opentravel.schemas.node.PropertyNodeType;
import org.opentravel.schemas.node.VWA_Node;
import org.opentravel.schemas.node.facets.VWA_AttributeFacetNode;
import org.opentravel.schemas.node.interfaces.INode;
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
public class AttributeNode extends PropertyNode {

	public AttributeNode(PropertyOwnerInterface parent, String name) {
		super(new TLAttribute(), (Node) parent, name, PropertyNodeType.ATTRIBUTE);
		setAssignedType((TypeProvider) ModelNode.getUnassignedNode());
	}

	public AttributeNode(PropertyOwnerInterface parent, String name, PropertyNodeType type) {
		super(new TLAttribute(), (Node) parent, name, type);
		setAssignedType((TypeProvider) ModelNode.getUnassignedNode());
	}

	/**
	 * 
	 * @param tlObj
	 *            TLAttribute
	 * @param parent
	 *            can be null
	 */
	public AttributeNode(TLModelElement tlObj, PropertyOwnerInterface parent) {
		super(tlObj, (INode) parent, PropertyNodeType.ATTRIBUTE);

		if (!(tlObj instanceof TLAttribute))
			throw new IllegalArgumentException("Invalid object for an attribute.");
	}

	/*
	 * used for sub-types
	 */
	public AttributeNode(TLModelElement tlObj, PropertyOwnerInterface parent, PropertyNodeType type) {
		super(tlObj, (INode) parent, type);
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

	@Override
	public INode createProperty(Node type) {
		if (!(getTLModelObject() instanceof TLAttribute))
			throw new IllegalArgumentException("Invalid object for an attribute.");

		TLAttribute tlObj = (TLAttribute) cloneTLObj();
		int index = indexOfNode();
		Node n = new AttributeNode(tlObj, null);
		((TLAttribute) getTLModelObject()).getOwner().addAttribute(index, tlObj);
		n.setName(type.getName());
		getParent().linkChild(n, index);
		n.setDescription(type.getDescription());
		if (type instanceof TypeProvider)
			((TypeUser) n).setAssignedType((TypeProvider) type);
		return n;
	}

	/**
	 * Override to provide GUI assist: Since attributes can be renamed, there is no need to use the alias. Aliases are
	 * not TLAttributeType members so the GUI assist must convert before assignment.
	 */
	@Override
	public boolean setAssignedType(TypeProvider provider) {
		if (provider instanceof AliasNode)
			provider = (TypeProvider) ((Node) provider).getOwningComponent();
		return typeHandler.set(provider);
		// return getTypeClass().setAssignedType(replacement);
	}

	@Override
	public String getEquivalent(String context) {
		if (equivalentHandler == null)
			equivalentHandler = new EqExOneValueHandler(this, ValueWithContextType.EQUIVALENT);
		return equivalentHandler != null ? equivalentHandler.get(context) : "";
	}

	@Override
	public IValueWithContextHandler setEquivalent(String example) {
		if (equivalentHandler == null)
			equivalentHandler = new EqExOneValueHandler(this, ValueWithContextType.EQUIVALENT);
		equivalentHandler.set(example, null);
		return equivalentHandler;
	}

	@Override
	public String getExample(String context) {
		if (exampleHandler == null)
			exampleHandler = new EqExOneValueHandler(this, ValueWithContextType.EXAMPLE);
		return exampleHandler != null ? exampleHandler.get(context) : "";
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

	@Override
	public boolean isOnlySimpleTypeUser() {
		// allow VWAs to be assigned to VWA Attributes.
		return parent != null && parent instanceof VWA_AttributeFacetNode ? false : true;
	}

	// @Override
	// public boolean isTypeUser() {
	// return true;
	// }

	@Override
	public void setName(String name) {
		modelObject.setName(NodeNameUtils.fixAttributeName(name));
	}

}
