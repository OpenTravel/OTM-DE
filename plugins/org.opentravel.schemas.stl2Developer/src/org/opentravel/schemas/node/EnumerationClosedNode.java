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
package org.opentravel.schemas.node;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLEnumValue;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemas.node.handlers.children.EnumerationClosedChildrenHandler;
import org.opentravel.schemas.node.interfaces.Enumeration;
import org.opentravel.schemas.node.interfaces.ExtensionOwner;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.interfaces.Sortable;
import org.opentravel.schemas.node.interfaces.VersionedObjectInterface;
import org.opentravel.schemas.node.properties.EnumLiteralNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.properties.PropertyOwnerInterface;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.types.ExtensionHandler;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.utils.StringComparator;

// FIXME - should not extend simple type node, simple and enum should extend from abstract simple or simple interface
public class EnumerationClosedNode extends SimpleComponentNode implements Enumeration, LibraryMemberInterface,
		PropertyOwnerInterface, Sortable, TypeProvider, ExtensionOwner, VersionedObjectInterface {

	private ExtensionHandler extensionHandler = null;

	public EnumerationClosedNode(EnumerationOpenNode openEnum) {
		this(new TLClosedEnumeration());
		setName(openEnum.getName());
		setDocumentation(openEnum.getDocumentation());

		if (openEnum.getLibrary() != null) {
			openEnum.getLibrary().addMember(this);
			getLibrary().removeMember(openEnum);
		}

		if (openEnum.getExtensionBase() != null)
			setExtension(openEnum.getExtensionBase());

		for (Node lit : openEnum.getChildren())
			addProperty(lit.clone(null, null));

		openEnum.delete();
		// LOGGER.debug("Created closed enum from open.");
	}

	public EnumerationClosedNode(TLClosedEnumeration mbr) {
		super(mbr);
		childrenHandler = new EnumerationClosedChildrenHandler(this);
		typeHandler = null; // created by SimpleComponentNode
		extensionHandler = new ExtensionHandler(this);
	}

	@Override
	public void addLiteral(String literal) {
		if (isEditable_inMinor())
			new EnumLiteralNode(this, literal);
	}

	@Override
	public void addProperties(List<Node> properties, boolean clone) {
		// TODO Auto-generated method stub
		assert false;
	}

	@Override
	public void addProperty(Node property) {
		if (property instanceof EnumLiteralNode)
			addProperty((EnumLiteralNode) property);
	}

	@Override
	public void addProperty(EnumLiteralNode enumLiteral) {
		// TODO - move test to controller.
		if (isEditable_newToChain()) {
			getTLModelObject().addValue(enumLiteral.getTLModelObject());
			if (childrenHandler != null)
				childrenHandler.clear();
			else
				assert false; // missing handler!
		}
	}

	@Override
	public void add(PropertyNode property, int i) {
		addProperty(property);
	}

	@Override
	public void addProperty(PropertyNode property) {
		if (property instanceof EnumLiteralNode)
			addProperty((EnumLiteralNode) property);

		// getTLModelObject().addValue((TLEnumValue) property.getTLModelObject());
	}

	@Override
	public ComponentNode createMinorVersionComponent() {
		return super.createMinorVersionComponent(new EnumerationClosedNode(
				(TLClosedEnumeration) createMinorTLVersion(this)));
	}

	@Override
	public EnumLiteralNode findChildByName(String name) {
		return (EnumLiteralNode) super.findChildByName(name);
	}

	@Override
	public INode.CommandType getAddCommand() {
		return INode.CommandType.ENUMERATION;
	}

	@Override
	public NamedEntity getAssignedTLNamedEntity() {
		// TODO Should this return parent?
		return null;
	}

	@Override
	public ComponentNodeType getComponentNodeType() {
		return ComponentNodeType.CLOSED_ENUM;
	}

	// Enumerations do not have equivalents
	@Override
	public String getEquivalent(String context) {
		return "";
	}

	// Enumerations do not have examples
	@Override
	public String getExample(String context) {
		return "";
	}

	@Override
	public String getExtendsTypeNS() {
		return getExtensionBase() != null ? getExtensionBase().getNamespace() : "";
	}

	// /////////////////////////////////////////////////////////////////
	//
	// Extension Owner implementations
	//
	@Override
	public Node getExtensionBase() {
		return extensionHandler != null ? extensionHandler.get() : null;
	}

	@Override
	public ExtensionHandler getExtensionHandler() {
		return extensionHandler;
	}

	@Override
	public PropertyOwnerInterface getFacet_Default() {
		return this;
	}

	@Override
	public Image getImage() {
		return Images.getImageRegistry().get(Images.Enumeration);
	}

	@Override
	public String getLabel() {
		if (isVersioned())
			return super.getLabel() + " (Extends version:  " + getExtensionBase().getLibrary().getVersion() + ")";
		return super.getLabel();
	}

	@Override
	public List<String> getLiterals() {
		ArrayList<String> literals = new ArrayList<String>();
		for (TLEnumValue v : getTLModelObject().getValues())
			literals.add(v.getLiteral());
		return literals;
	}

	@Override
	public String getName() {
		return getTLModelObject().getName();
	}

	@Override
	public List<Node> getNavChildren(boolean deep) {
		return new ArrayList<Node>();
	}

	@Override
	public ImpliedNode getRequiredType() {
		return ModelNode.getDefaultStringNode();
	}

	@Override
	public TLClosedEnumeration getTLModelObject() {
		return (TLClosedEnumeration) tlObj;
	}

	@Override
	public boolean hasNavChildren(boolean deep) {
		return false;
	}

	@Override
	public boolean isMergeSupported() {
		return true;
	}

	@Override
	public boolean isSimpleAssignable() {
		return true;
	}

	@Override
	public void merge(Node source) {
		if (!(source instanceof EnumerationClosedNode)) {
			throw new IllegalStateException("Can only merge objects with the same type");
		}
		EnumerationClosedNode open = (EnumerationClosedNode) source;
		for (Node literal : open.getChildren()) {
			addProperty(literal.clone(null, null));
		}
		getChildrenHandler().clear();
	}

	// @Override
	// public NamedEntity getTLOjbect() {
	// return getTLModelObject();
	// }

	@Override
	public Node setExtensible(boolean extensible) {
		if (isEditable_newToChain())
			if (extensible)
				return new EnumerationOpenNode(this);
		return this;
	}

	@Override
	public void setExtension(final Node base) {
		if (extensionHandler == null)
			extensionHandler = new ExtensionHandler(this);
		extensionHandler.set(base);
	}

	@Override
	public void setName(String name) {
		getTLModelObject().setName(NodeNameUtils.fixEnumerationName(name));
	}

	@Override
	public boolean setAssignedType(TLModelElement tla) {
		return false;
		// // FIXME - use extension not type assignment
		// if (tla instanceof TLAbstractEnumeration) {
		// TLExtension extension = new TLExtension();
		// extension.setExtendsEntity((NamedEntity) tla);
		// getTLModelObject().setExtension(extension);
		// }
	}

	@Override
	public void sort() {
		getTLModelObject().sortValues(new StringComparator<TLEnumValue>() {
			@Override
			protected String getString(TLEnumValue object) {
				return object.getLiteral();
			}
		});
	}
}
