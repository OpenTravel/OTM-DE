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
/**
 * 
 */
package org.opentravel.schemas.node;

import javax.xml.namespace.QName;

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemas.modelObject.ModelObject;
import org.opentravel.schemas.modelObject.XsdModelingUtils;
import org.opentravel.schemas.node.interfaces.Enumeration;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.interfaces.SimpleComponentInterface;
import org.opentravel.schemas.node.listeners.ListenerFactory;
import org.opentravel.schemas.node.properties.EqExOneValueHandler;
import org.opentravel.schemas.node.properties.EqExOneValueHandler.ValueWithContextType;
import org.opentravel.schemas.node.properties.IValueWithContextHandler;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles Both simple types and closed enumerations.
 * 
 * @author Dave Hollander
 * 
 */
public class SimpleTypeNode extends ComponentNode implements SimpleComponentInterface, TypeUser, LibraryMemberInterface {
	private static final Logger LOGGER = LoggerFactory.getLogger(SimpleTypeNode.class);

	// Handlers for equivalents and examples
	protected IValueWithContextHandler equivalentHandler = null;
	protected IValueWithContextHandler exampleHandler = null;

	public SimpleTypeNode(LibraryMember mbr) {
		super(mbr);
		assert (getTLModelObject() != null);

		ListenerFactory.setListner(this);
	}

	@Override
	public boolean canAssign(Node type) {
		if (type == null || !(type instanceof TypeProvider))
			return false;
		TypeProvider provider = (TypeProvider) type;
		return provider.isAssignableToSimple();
	}

	@Override
	public String getTypeName() {
		if (getTypeClass().getTypeNode() == null) {
			LOGGER.warn("Trying to fix missing type assignment for " + getName());
			getTypeClass().setAssignedTypeForThisNode(this);
		}

		String name = getTypeClass().getTypeNode().getName();

		// For implied nodes, use the name they provide.
		if (getTypeClass().getTypeNode() instanceof ImpliedNode) {
			ImpliedNode in = (ImpliedNode) getTypeClass().getTypeNode();
			name = in.getImpliedType().getImpliedNodeType();
			// If the implied node is a union, add that to its assigned name
			if (in.getImpliedType().equals(ImpliedNodeType.Union))
				name += ": " + XsdModelingUtils.getAssignedXsdUnion(this);
		}
		return name == null ? "" : name;
	}

	@Override
	public String getTypeNameWithPrefix() {
		String typeName = getTypeName() == null ? "" : getTypeName();
		if (getAssignedType() == null)
			return "";
		if (getAssignedType() instanceof ImpliedNode)
			return typeName;
		if (getNamePrefix().equals(getAssignedPrefix()))
			return typeName;
		return getType().getNamePrefix() + ":" + typeName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.ComponentNode#hasNavChildren()
	 */
	@Override
	public boolean hasNavChildren() {
		return true; // True because where used is a nav child.
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.ComponentNode#hasNavChildrenWithProperties()
	 */
	@Override
	public boolean hasNavChildrenWithProperties() {
		return true; // True because where used is a nav child.
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.Node#isTypeUser()
	 */
	@Override
	public boolean isTypeUser() {
		return this instanceof Enumeration ? false : true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.types.TypeProvider#getTypeNode()
	 */
	@Override
	public Node getTypeNode() {
		return getTypeClass().getTypeNode();
	}

	@Override
	public boolean isAssignableToSimple() {
		return true;
	}

	@Override
	public boolean isAssignableToVWA() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.Node#isNamedType()
	 */
	@Override
	public boolean isNamedType() {
		return true;
	}

	@Override
	public Node getBaseType() {
		if (getTypeClass().getTypeNode() == null) {
			if (getTLModelObject() instanceof TLSimple)
				getTypeClass().setTypeNode(ModelNode.getUnassignedNode());
		}
		return getTypeClass().getTypeNode();
	}

	@Override
	public Image getImage() {
		return Images.getImageRegistry().get(Images.XSDSimpleType);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.Node#getTLBaseType()
	 */
	@Override
	public NamedEntity getTLBaseType() {
		NamedEntity x = null;
		if (getTLModelObject() instanceof TLSimple) {
			x = ((TLSimple) getTLModelObject()).getParentType();
		} else
			x = (NamedEntity) getTypeClass().getTypeNode().getTLModelObject(); // created
																				// by
																				// constructor;
		return x;
	}

	@Override
	public NamedEntity getTLOjbect() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.ComponentNode#isSimpleType()
	 */
	@Override
	public boolean isSimpleType() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.Node#isSimpleTypeUser()
	 */
	@Override
	public boolean isOnlySimpleTypeUser() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.Node#isTypeProvider()
	 */
	@Override
	public boolean isTypeProvider() {
		return true;
	}

	@Override
	public ModelObject<?> getAssignedModelObject() {
		return getTypeClass().getTypeNode().getModelObject();
	}

	// /*
	// * (non-Javadoc)
	// *
	// * @see org.opentravel.schemas.types.TypeUser#getAssignedTLObject()
	// */
	@Override
	public NamedEntity getAssignedTLObject() {
		return getTypeClass().getTypeNode().getTLTypeObject();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.Node#setAssignedType(org.opentravel.schemas.node.Node)
	 * 
	 * @see org.opentravel.schemas.types.TypeUser#setAssignedType()
	 */
	@Override
	public boolean setAssignedType(Node typeNode) {
		return getTypeClass().setAssignedType(typeNode);
	}

	// @Override
	// @Deprecated
	// public boolean setAssignedType(Node typeNode, boolean refresh) {
	// return setAssignedType(typeNode);
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.Node#getAssignedType()
	 */
	@Override
	public Node getAssignedType() {
		return getTypeClass().getTypeNode();
	}

	@Override
	public QName getTLTypeQName() {
		if (getTLTypeObject() != null) {
			return new QName(getTLTypeObject().getNamespace(), getTLTypeObject().getLocalName());
		} else
			return XsdModelingUtils.getAssignedXsdType(this);
	}

	@Override
	public boolean isSimpleTypeProvider() {
		return true;
	}

	/**
	 * @return equivalent handler if property has equivalents, null otherwise
	 */
	public IValueWithContextHandler getEquivalentHandler() {
		return equivalentHandler;
	}

	@Override
	public ComponentNodeType getComponentNodeType() {
		return ComponentNodeType.SIMPLE;
	}

	@Override
	public String getEquivalent(String context) {
		if (equivalentHandler == null)
			equivalentHandler = new EqExOneValueHandler(this, ValueWithContextType.EQUIVALENT);
		return equivalentHandler != null ? equivalentHandler.get(context) : "";
	}

	public IValueWithContextHandler setEquivalent(String equivalent) {
		if (equivalentHandler == null)
			equivalentHandler = new EqExOneValueHandler(this, ValueWithContextType.EQUIVALENT);
		equivalentHandler.set(equivalent, null);
		return equivalentHandler;
	}

	/**
	 * @return equivalent handler if property has equivalents, null otherwise
	 */
	public IValueWithContextHandler getExampleHandler() {
		return exampleHandler;
	}

	@Override
	public String getExample(String context) {
		if (exampleHandler == null)
			exampleHandler = new EqExOneValueHandler(this, ValueWithContextType.EXAMPLE);
		return exampleHandler != null ? exampleHandler.get(context) : "";
	}

	public IValueWithContextHandler setExample(String example) {
		if (exampleHandler == null)
			exampleHandler = new EqExOneValueHandler(this, ValueWithContextType.EXAMPLE);
		exampleHandler.set(example, null);
		return exampleHandler;
	}

}
