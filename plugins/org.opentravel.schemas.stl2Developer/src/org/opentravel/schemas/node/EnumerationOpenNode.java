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
import java.util.Collections;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLAbstractEnumeration;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLEnumValue;
import org.opentravel.schemacompiler.model.TLOpenEnumeration;
import org.opentravel.schemas.node.interfaces.ComplexComponentInterface;
import org.opentravel.schemas.node.interfaces.Enumeration;
import org.opentravel.schemas.node.interfaces.ExtensionOwner;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.interfaces.VersionedObjectInterface;
import org.opentravel.schemas.node.properties.EnumLiteralNode;
import org.opentravel.schemas.node.properties.PropertyOwnerInterface;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.types.ExtensionHandler;
import org.opentravel.schemas.types.TypeProvider;

public class EnumerationOpenNode extends TypeProviderBase implements ComplexComponentInterface, Enumeration,
		ExtensionOwner, VersionedObjectInterface, LibraryMemberInterface, TypeProvider {
	// private static final Logger LOGGER = LoggerFactory.getLogger(EnumerationOpenNode.class);
	private ExtensionHandler extensionHandler = null;

	public EnumerationOpenNode(LibraryMember mbr) {
		super(mbr);
		addMOChildren();
		extensionHandler = new ExtensionHandler(this);
	}

	/**
	 * Note: if the closed enum does not have a library the new enum will not have children copied.
	 * 
	 * @param closedEnum
	 */
	public EnumerationOpenNode(EnumerationClosedNode closedEnum) {
		this(new TLOpenEnumeration());

		if (closedEnum.getLibrary() != null) {
			if (closedEnum.getExtensionBase() != null)
				((TLAbstractEnumeration) getTLModelObject()).setExtension(((TLClosedEnumeration) closedEnum
						.getTLModelObject()).getExtension());

			// Do this first since clone needs to know library information.
			setLibrary(closedEnum.getLibrary());
			getLibrary().getTLaLib().addNamedMember((LibraryMember) this.getTLModelObject());

			for (Node lit : closedEnum.getChildren()) {
				addProperty(lit.clone(this, null));
			}
			getLibrary().getTLaLib().removeNamedMember((LibraryMember) closedEnum.getTLModelObject());
			closedEnum.unlinkNode();
			// If openEnum was being used, the tl model will reassign but not type node
			// FIXME - TESTME
			// closedEnum.getTypeClass().replaceTypeProvider(this, null);

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
				this.linkChild(enumLiteral);
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

	// @Override
	// public String getLabel() {
	// if (isVersioned())
	// return super.getLabel() + " (Extends version:  " + getExtensionBase().getLibrary().getVersion() + ")";
	// return super.getLabel();
	// }

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
	public void initInheritedChildren() {
		List<?> inheritedMOChildren = modelObject.getInheritedChildren();
		if (inheritedMOChildren == null || inheritedMOChildren.isEmpty()) {
			inheritedChildren = Collections.emptyList();
		} else {
			for (final Object obj : inheritedMOChildren) {
				ComponentNode nn = NodeFactory.newComponentMember(null, obj);
				if (nn != null) {
					linkInheritedChild(nn);
					nn.inheritsFrom = null; // override value from link
				}
			}
		}
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

	@Override
	public boolean isAssignableToSimple() {
		return false;
	}

	@Override
	public boolean isAssignableToElementRef() {
		return false;
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

}
