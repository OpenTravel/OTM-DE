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
package org.opentravel.schemas.node.typeProviders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLEnumValue;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.ComponentNodeType;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeNameUtils;
import org.opentravel.schemas.node.handlers.children.EnumerationClosedChildrenHandler;
import org.opentravel.schemas.node.interfaces.Enumeration;
import org.opentravel.schemas.node.interfaces.ExtensionOwner;
import org.opentravel.schemas.node.interfaces.FacetInterface;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.interfaces.Sortable;
import org.opentravel.schemas.node.interfaces.VersionedObjectInterface;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.properties.EnumLiteralNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.types.ExtensionHandler;
import org.opentravel.schemas.utils.StringComparator;

public class EnumerationClosedNode extends SimpleTypeProviders implements FacetInterface, Enumeration,
		LibraryMemberInterface, Sortable, ExtensionOwner, VersionedObjectInterface {

	private ExtensionHandler extensionHandler = null;
	private LibraryNode owningLibrary = null;

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
		extensionHandler = new ExtensionHandler(this);
	}

	@Override
	public void removeProperty(PropertyNode pn) {
		if (pn instanceof EnumLiteralNode)
			getTLModelObject().removeValue(((EnumLiteralNode) pn).getTLModelObject());
	}

	@Override
	public EnumerationClosedChildrenHandler getChildrenHandler() {
		return (EnumerationClosedChildrenHandler) childrenHandler;
	}

	@Override
	public boolean isExtensionPointTarget() {
		return false;
	}

	@Override
	public void addLiteral(String literal) {
		if (isEditable_inMinor())
			new EnumLiteralNode(this, literal);
	}

	@Override
	public void add(List<PropertyNode> properties, boolean clone) {
		for (PropertyNode pn : properties)
			add(pn);
		assert false;
	}

	@Override
	public void add(PropertyNode pn) {
		if (pn instanceof EnumLiteralNode) {
			pn.setParent(this);
			pn.addToTL(this);
			getChildrenHandler().clear();
		}
	}

	@Override
	public void add(EnumLiteralNode enumLiteral) {
		// TODO - move test to controller.
		if (isEditable_newToChain())
			getTLModelObject().addValue(enumLiteral.getTLModelObject());
		if (childrenHandler != null)
			childrenHandler.clear();
	}

	@Override
	public void add(PropertyNode property, int i) {
		add(property);
	}

	@Override
	public PropertyNode createProperty(Node type) {
		PropertyNode pn = new EnumLiteralNode(new TLEnumValue());
		pn.setName(type.getName());
		this.add(pn);
		return pn;
	}

	@Override
	public ComponentNode createMinorVersionComponent() {
		TLClosedEnumeration tlMinor = (TLClosedEnumeration) createMinorTLVersion(this);
		if (tlMinor != null)
			return super.createMinorVersionComponent(new EnumerationClosedNode(tlMinor));
		return null;
	}

	@Override
	public EnumLiteralNode findChildByName(String name) {
		return get(name);
	}

	@Override
	public EnumLiteralNode get(String name) {
		for (Node n : getChildrenHandler().get())
			if (n instanceof EnumLiteralNode && n.getName().equals(name))
				return (EnumLiteralNode) n;
		return null;
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
	public String getExtendsTypeNS() {
		return getExtensionBase() != null ? getExtensionBase().getNamespace() : "";
	}

	@Override
	public Node getExtensionBase() {
		return extensionHandler != null ? extensionHandler.get() : null;
	}

	@Override
	public ExtensionHandler getExtensionHandler() {
		return extensionHandler;
	}

	@Override
	public Image getImage() {
		return Images.getImageRegistry().get(Images.Enumeration);
	}

	@Override
	public String getLabel() {
		return "CLOSED";
	}

	@Override
	public LibraryNode getLibrary() {
		return owningLibrary;
	}

	@Override
	public List<String> getLiterals() {
		ArrayList<String> literals = new ArrayList<>();
		for (TLEnumValue v : getTLModelObject().getValues())
			literals.add(v.getLiteral());
		return literals;
	}

	@Override
	public String getNavigatorName() {
		return getName();
	}

	@Override
	public String getName() {
		return getTLModelObject().getName();
	}

	@Override
	public List<Node> getNavChildren(boolean deep) {
		return new ArrayList<>();
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

	// 1/3/2019 - dmh - closed enum does not support list attribute
	// @Override
	// public boolean isSimpleList() {
	// TLClosedEnumeration tlCE = getTLModelObject();
	// return getTLModelObject().isListTypeInd();
	// }

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
	public void setLibrary(LibraryNode library) {
		owningLibrary = library;
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

	@Override
	public List<AliasNode> getAliases() {
		return Collections.emptyList();
	}

	@Override
	public void copy(FacetInterface facet) {
		add(facet.getProperties(), true);
	}

	@Override
	public List<PropertyNode> getProperties() {
		List<PropertyNode> kids = new ArrayList<>();
		for (Node n : getChildrenHandler().get())
			kids.add((PropertyNode) n);
		return kids;
	}

	@Override
	public boolean isFacet(TLFacetType type) {
		return false;
	}
}
