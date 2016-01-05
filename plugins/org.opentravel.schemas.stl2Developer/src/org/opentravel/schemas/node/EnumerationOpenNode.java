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
import java.util.Collection;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemacompiler.event.ModelElementListener;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLEnumValue;
import org.opentravel.schemacompiler.model.TLOpenEnumeration;
import org.opentravel.schemas.modelObject.OpenEnumMO;
import org.opentravel.schemas.node.interfaces.ComplexComponentInterface;
import org.opentravel.schemas.node.interfaces.Enumeration;
import org.opentravel.schemas.node.interfaces.ExtensionOwner;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.interfaces.VersionedObjectInterface;
import org.opentravel.schemas.node.listeners.NamedTypeListener;
import org.opentravel.schemas.node.properties.EnumLiteralNode;
import org.opentravel.schemas.node.properties.PropertyOwnerInterface;
import org.opentravel.schemas.properties.Images;

public class EnumerationOpenNode extends ComponentNode implements ComplexComponentInterface, Enumeration,
		ExtensionOwner, VersionedObjectInterface, LibraryMemberInterface {
	// private static final Logger LOGGER = LoggerFactory.getLogger(EnumerationOpenNode.class);

	public EnumerationOpenNode(LibraryMember mbr) {
		super(mbr);
		addMOChildren();
		if (getExtensionNode() == null)
			getTypeClass().setTypeNode(ModelNode.getDefaultStringNode()); // Set base type.
		else
			getTypeClass().setTypeNode(getExtensionNode());
	}

	/**
	 * Note: if the closed enum does not have a library the new enum will not have children copied.
	 * 
	 * @param closedEnum
	 */
	public EnumerationOpenNode(EnumerationClosedNode closedEnum) {
		this(new TLOpenEnumeration());

		if (closedEnum.getLibrary() != null) {
			// Do this first since clone needs to know library information.
			setLibrary(closedEnum.getLibrary());
			getLibrary().getTLaLib().addNamedMember((LibraryMember) this.getTLModelObject());

			for (Node lit : closedEnum.getChildren()) {
				addProperty(lit.clone(this, null));
			}
			getLibrary().getTLaLib().removeNamedMember((LibraryMember) closedEnum.getTLModelObject());
			closedEnum.unlinkNode();
			// If openEnum was being used, the tl model will reassign but not type node
			closedEnum.getTypeClass().replaceTypeProvider(this, null);

			getLibrary().getComplexRoot().linkChild(this);
			// handle version managed libraries
			if (getLibrary().isInChain())
				getChain().add(this);

			setDocumentation(closedEnum.getDocumentation());
			setName(closedEnum.getName());
			closedEnum.delete();
		}
		// LOGGER.debug("Created open enum from closed.");
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
				((TLOpenEnumeration) getTLModelObject()).addValue((TLEnumValue) enumLiteral.getTLModelObject());
				this.linkChild(enumLiteral, false);
			}
	}

	@Override
	public ComponentNode createMinorVersionComponent() {
		return super.createMinorVersionComponent(new EnumerationOpenNode(createMinorTLVersion(this)));
	}

	@Override
	public ComponentNodeType getComponentNodeType() {
		return ComponentNodeType.OPEN_ENUM;
	}

	@Override
	public PropertyOwnerInterface getDefaultFacet() {
		return null;
	}

	@Override
	public ImpliedNode getDefaultType() {
		return ModelNode.getDefaultStringNode();
	}

	@Override
	public String getLabel() {
		if (isVersioned())
			return super.getLabel() + " (Extends version:  " + getExtendsType().getLibrary().getVersion() + ")";
		return super.getLabel();
	}

	@Override
	public List<String> getLiterals() {
		ArrayList<String> literals = new ArrayList<String>();
		for (TLEnumValue v : ((TLOpenEnumeration) getTLModelObject()).getValues())
			literals.add(v.getLiteral());
		return literals;
	}

	@Override
	public Image getImage() {
		return Images.getImageRegistry().get(Images.Enumeration);
	}

	@Override
	public boolean isNamedType() {
		return true;
	}

	@Override
	public boolean isAssignableToVWA() {
		return true;
	}

	@Override
	public Node setExtensible(boolean extensible) {
		if (isEditable_newToChain())
			if (!extensible)
				return new EnumerationClosedNode(this);
		return this;
	}

	@Override
	public void merge(Node source) {
		if (!(source instanceof EnumerationOpenNode)) {
			throw new IllegalStateException("Can only merge objects with the same type");
		}
		EnumerationOpenNode open = (EnumerationOpenNode) source;
		for (Node literal : open.getChildren()) {
			addProperty(literal.clone(null, null));
		}
	}

	@Override
	public boolean isMergeSupported() {
		return true;
	}

	@Override
	public ComponentNode getSimpleType() {
		return null;
	}

	@Override
	public Node getExtendsType() {
		// base type might not have been loaded when constructor was called. check the tl model not the type node.
		return getExtensionNode();
	}

	public EnumerationOpenNode getExtensionNode() {
		Collection<ModelElementListener> listeners = null;
		Node node = null;
		OpenEnumMO mo = (OpenEnumMO) getModelObject();
		if (mo.getExtension(mo.getTLModelObj()) != null)
			listeners = mo.getExtension(mo.getTLModelObj()).getListeners();
		if (listeners == null || listeners.isEmpty())
			return null;
		for (ModelElementListener listener : listeners)
			if (listener instanceof NamedTypeListener)
				node = ((NamedTypeListener) listener).getNode();
		return (EnumerationOpenNode) (node instanceof EnumerationOpenNode ? node : null);
	}

	@Override
	public void setExtendsType(final INode sourceNode) {
		getTypeClass().setAssignedBaseType(sourceNode);
	}

	@Override
	public boolean setSimpleType(Node type) {
		return false;
	}

	@Override
	public INode.CommandType getAddCommand() {
		return INode.CommandType.ENUMERATION;
	}

	@Override
	public PropertyOwnerInterface getAttributeFacet() {
		return null;
	}

	@Override
	public SimpleFacetNode getSimpleFacet() {
		return null;
	}

}
