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
import org.opentravel.schemacompiler.model.TLAbstractEnumeration;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLEnumValue;
import org.opentravel.schemacompiler.model.TLLibraryMember;
import org.opentravel.schemacompiler.model.TLOpenEnumeration;
import org.opentravel.schemas.node.interfaces.Enumeration;
import org.opentravel.schemas.node.interfaces.ExtensionOwner;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.properties.EnumLiteralNode;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.types.ExtensionHandler;
import org.opentravel.schemas.types.TypeProvider;

// FIXME - should not extend simple type node, simple and enum should extend from abstract simple or simple interface
public class EnumerationClosedNode extends SimpleComponentNode implements Enumeration, LibraryMemberInterface,
		TypeProvider, ExtensionOwner {

	private ExtensionHandler extensionHandler = null;

	public EnumerationClosedNode(TLLibraryMember mbr) {
		super(mbr);
		addMOChildren();
		extensionHandler = new ExtensionHandler(this);

		assert (mbr instanceof TLClosedEnumeration);
	}

	public EnumerationClosedNode(EnumerationOpenNode openEnum) {
		this(new TLClosedEnumeration());
		if (openEnum.getLibrary() != null) {
			if (openEnum.getExtensionBase() != null)
				((TLAbstractEnumeration) getTLModelObject()).setExtension(((TLOpenEnumeration) openEnum
						.getTLModelObject()).getExtension());
			setLibrary(openEnum.getLibrary());
			getLibrary().getTLaLib().addNamedMember((TLLibraryMember) this.getTLModelObject());

			setName(openEnum.getName());
			for (Node lit : openEnum.getChildren()) {
				addProperty(lit.clone(this, null));
			}
			getLibrary().getTLaLib().removeNamedMember((TLLibraryMember) openEnum.getTLModelObject());
			openEnum.unlinkNode();
			// If openEnum was being used, the tl model will reassign but not type node

			getLibrary().getSimpleRoot().linkChild(this);
			// handle version managed libraries
			if (getLibrary().isInChain())
				getChain().add(this);

			setDocumentation(openEnum.getDocumentation());

			openEnum.delete();
		}
		// LOGGER.debug("Created closed enum from open.");
	}

	@Override
	public void addLiteral(String literal) {
		if (isEditable_inMinor())
			new EnumLiteralNode(this, literal);
	}

	@Override
	public void addProperty(Node enumLiteral) {
		if (isEditable_newToChain())
			if (enumLiteral instanceof EnumLiteralNode) {
				((TLClosedEnumeration) getTLModelObject()).addValue((TLEnumValue) enumLiteral.getTLModelObject());
				this.linkChild(enumLiteral);
			}
	}

	@Override
	public INode.CommandType getAddCommand() {
		return INode.CommandType.ENUMERATION;
	}

	@Override
	public ComponentNodeType getComponentNodeType() {
		return ComponentNodeType.CLOSED_ENUM;
	}

	@Override
	public ImpliedNode getRequiredType() {
		return ModelNode.getDefaultStringNode();
	}

	@Override
	public String getName() {
		return getTLModelObject().getName();
	}

	@Override
	public TLClosedEnumeration getTLModelObject() {
		return (TLClosedEnumeration) (modelObject != null ? modelObject.getTLModelObj() : null);
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
		for (TLEnumValue v : ((TLClosedEnumeration) getTLModelObject()).getValues())
			literals.add(v.getLiteral());
		return literals;
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
	public Image getImage() {
		return Images.getImageRegistry().get(Images.Enumeration);
	}

	@Override
	public boolean hasNavChildren(boolean deep) {
		return false;
	}

	@Override
	public List<Node> getNavChildren(boolean deep) {
		return new ArrayList<Node>();
	}

	@Override
	public Node setExtensible(boolean extensible) {
		if (isEditable_newToChain())
			if (extensible)
				return new EnumerationOpenNode(this);
		return this;
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
	}

	@Override
	public boolean isMergeSupported() {
		return true;
	}

	// /////////////////////////////////////////////////////////////////
	//
	// Extension Owner implementations
	//
	@Override
	public Node getExtensionBase() {
		return extensionHandler != null ? extensionHandler.get() : null;
	}

	public String getExtendsTypeNS() {
		return modelObject.getExtendsTypeNS();
	}

	@Override
	public void setExtension(final Node base) {
		if (extensionHandler == null)
			extensionHandler = new ExtensionHandler(this);
		extensionHandler.set(base);
	}

	@Override
	public ExtensionHandler getExtensionHandler() {
		return extensionHandler;
	}

	@Override
	public NamedEntity getTLOjbect() {
		return getTLModelObject();
	}

	@Override
	public void setName(String name) {
		getTLModelObject().setName(NodeNameUtils.fixEnumerationName(name));
	}
}
