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

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLLibraryMember;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.interfaces.SimpleComponentInterface;
import org.opentravel.schemas.node.properties.EqExOneValueHandler;
import org.opentravel.schemas.node.properties.EqExOneValueHandler.ValueWithContextType;
import org.opentravel.schemas.node.properties.IValueWithContextHandler;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeUser;
import org.opentravel.schemas.types.TypeUserHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles simple types and is extended by closed enumerations.
 * 
 * @author Dave Hollander
 * 
 */
public class SimpleTypeNode extends TypeProviderBase implements SimpleComponentInterface, TypeUser,
		LibraryMemberInterface, TypeProvider {
	private static final Logger LOGGER = LoggerFactory.getLogger(SimpleTypeNode.class);

	// Handlers for equivalents and examples
	protected IValueWithContextHandler equivalentHandler = null;
	protected IValueWithContextHandler exampleHandler = null;
	protected TypeUserHandler typeHandler = null;

	public SimpleTypeNode(TLLibraryMember mbr) {
		super(mbr);
		assert (getTLModelObject() != null);

		typeHandler = new TypeUserHandler(this);

		if (this instanceof EnumerationClosedNode)
			assert getTLModelObject() instanceof TLClosedEnumeration;
		else if (this instanceof SimpleTypeNode) {
			assert getTLModelObject() instanceof TLSimple;
			constraintHandler = new ConstraintHandler((TLSimple) getTLModelObject(), this);
		}
	}

	@Override
	public boolean canAssign(Node type) {
		return type instanceof TypeProvider ? ((TypeProvider) type).isAssignableToSimple() : false;
	}

	@Override
	public String getTypeName() {
		// // FIXME - TESTME
		// // For implied nodes, use the name they provide.
		// if (typeHandler.get() instanceof ImpliedNode) {
		// ImpliedNode in = (ImpliedNode) typeHandler.get();
		// String name = in.getImpliedType().getImpliedNodeType();
		// // If the implied node is a union, add that to its assigned name
		// if (in.getImpliedType().equals(ImpliedNodeType.Union))
		// throw new IllegalStateException("How to handle getTypeName() for unions?");
		// name += ": " + XsdModelingUtils.getAssignedXsdUnion(this);
		// }
		//
		return typeHandler.getName();
	}

	@Override
	public String getTypeNameWithPrefix() {
		String typeName = getTypeName() == null ? "" : getTypeName();
		if (getAssignedType() == null)
			return "";
		if (getAssignedType() instanceof ImpliedNode)
			return typeName;
		if (getNamePrefix().equals(getAssignedPrefix()))
			return typeName; // only prefix names in different namespaces
		return getType().getNamePrefix() + ":" + typeName;
	}

	@Override
	public boolean hasNavChildren() {
		return true; // True because where used is a nav child.
	}

	@Override
	public boolean hasNavChildrenWithProperties() {
		return true; // True because where used is a nav child.
	}

	// @Override
	// public boolean isTypeUser() {
	// return this instanceof Enumeration ? false : true;
	// }
	//
	@Override
	public boolean isAssignableToSimple() {
		return true;
	}

	@Override
	public boolean isAssignableToVWA() {
		return true;
	}

	@Override
	public boolean isNamedType() {
		return true;
	}

	@Override
	public Node getBaseType() {
		// Base type is the assigned type
		return (Node) typeHandler.get();
		// FIXME - since this is base type, should this be type user?

		// if (getTypeClass().getTypeNode() == null) {
		// if (getTLModelObject() instanceof TLSimple)
		// getTypeClass().setTypeNode(ModelNode.getUnassignedNode());
		// }
		// return (Node) getTypeClass().getTypeNode();
	}

	@Override
	public Image getImage() {
		return Images.getImageRegistry().get(Images.XSDSimpleType);
	}

	@Override
	public NamedEntity getTLBaseType() {
		return typeHandler.getTLNamedEntity();
		// NamedEntity x = null;
		// if (getTLModelObject() instanceof TLSimple) {
		// x = ((TLSimple) getTLModelObject()).getParentType();
		// } else
		// x = (NamedEntity) getTypeClass().getTypeNode().getTLModelObject();
		// // created by constructor;
		// return x;
	}

	@Override
	public NamedEntity getTLOjbect() {
		return (NamedEntity) modelObject.getTLModelObj();
	}

	@Override
	public boolean isSimpleType() {
		return true;
	}

	@Override
	public boolean isOnlySimpleTypeUser() {
		return true;
	}

	@Override
	public boolean isTypeProvider() {
		return true;
	}

	// @Override
	// public ModelObject<?> getAssignedModelObject() {
	// return ((Node) typeHandler.get()).getModelObject();
	// }

	@Override
	public boolean setAssignedType(TypeProvider provider) {
		return typeHandler.set(provider);
	}

	@Override
	public boolean setAssignedType(TLModelElement tlProvider) {
		return typeHandler.set(tlProvider);
	}

	@Override
	public boolean setAssignedType() {
		return typeHandler.set();
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

	@Override
	public boolean isAssignableToElementRef() {
		return false;
	}

	@Override
	public TypeProvider getAssignedType() {
		return typeHandler.get();
	}

	@Override
	public NamedEntity getAssignedTLNamedEntity() {
		return typeHandler.getTLNamedEntity();
	}

	@Override
	public TLModelElement getAssignedTLObject() {
		return typeHandler.getTLModelElement();
	}

	@Override
	public TypeProvider getRequiredType() {
		return null;
	}

}
