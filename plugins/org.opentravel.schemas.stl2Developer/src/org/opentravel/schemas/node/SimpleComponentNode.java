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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLLibraryMember;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.interfaces.SimpleComponentInterface;
import org.opentravel.schemas.node.listeners.BaseNodeListener;
import org.opentravel.schemas.node.listeners.TypeUserListener;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeUser;
import org.opentravel.schemas.types.TypeUserHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for the simple type providers including Simple Type, Implied and Closed Enumeration
 * 
 * @author Dave Hollander
 * 
 */
public abstract class SimpleComponentNode extends TypeProviderBase implements SimpleComponentInterface, TypeUser,
		LibraryMemberInterface, TypeProvider {
	private static final Logger LOGGER = LoggerFactory.getLogger(SimpleComponentNode.class);

	protected TypeUserHandler typeHandler = null;

	public SimpleComponentNode(TLLibraryMember mbr) {
		super(mbr);
		assert (getTLModelObject() != null);
		typeHandler = new TypeUserHandler(this);
	}

	@Override
	public boolean canAssign(Node type) {
		return type instanceof TypeProvider ? ((TypeProvider) type).isAssignableToSimple() : false;
	}

	@Override
	public String getTypeName() {
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
	public boolean hasNavChildren(boolean deep) {
		return deep && getNavType() != null;
	}

	// Do not show implied types in tree views
	private Node getNavType() {
		Node type = getTypeNode();
		return type instanceof ImpliedNode ? null : type;
	}

	/**
	 * Return new array containing assigned type
	 */
	public List<Node> getNavChildren(boolean deep) {
		Node type = getNavType();
		ArrayList<Node> kids = new ArrayList<Node>();
		if (deep && type != null)
			kids.add(type);
		return kids;
	}

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
	public abstract Image getImage();

	@Override
	public BaseNodeListener getNewListener() {
		return new TypeUserListener(this);
	}

	@Override
	public NamedEntity getTLBaseType() {
		return typeHandler.getTLNamedEntity();
	}

	@Override
	public abstract NamedEntity getTLOjbect();

	@Override
	public abstract String getName();

	@Override
	public abstract TLLibraryMember getTLModelObject();

	/**
	 * Use instanceof SimpleComponentNode
	 */
	@Deprecated
	@Override
	public boolean isSimpleType() {
		return true;
	}

	@Override
	public boolean isOnlySimpleTypeUser() {
		return true;
	}

	@Override
	public boolean isNamedEntity() {
		return true;
	}

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

	@Override
	public abstract ComponentNodeType getComponentNodeType();

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
