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
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLEnumValue;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLOpenEnumeration;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.ComponentNodeType;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFactory;
import org.opentravel.schemas.node.NodeNameUtils;
import org.opentravel.schemas.node.handlers.children.EnumerationOpenChildrenHandler;
import org.opentravel.schemas.node.interfaces.ComplexMemberInterface;
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

public class EnumerationOpenNode extends TypeProviders implements FacetInterface, Enumeration, Sortable, ExtensionOwner,
		ComplexMemberInterface, VersionedObjectInterface {
	// private static final Logger LOGGER = LoggerFactory.getLogger(EnumerationOpenNode.class);

	private ExtensionHandler extensionHandler = null;
	private LibraryNode owningLibrary;

	/**
	 * Note: if the closed enum does not have a library the new enum will not have children copied.
	 * 
	 * @param closedEnum
	 */
	public EnumerationOpenNode(EnumerationClosedNode closedEnum) {
		this(new TLOpenEnumeration());
		setName(closedEnum.getName());
		setDocumentation(closedEnum.getDocumentation());

		if (closedEnum.getLibrary() != null) {
			closedEnum.getLibrary().addMember(this);
			getLibrary().removeMember(closedEnum);
		}
		if (closedEnum.getExtensionBase() != null)
			setExtension(closedEnum.getExtensionBase());

		for (Node lit : closedEnum.getChildren())
			addProperty(lit.clone(null, null));

		closedEnum.delete();

		// LOGGER.debug("Created open enum from closed.");
	}

	public EnumerationOpenNode(TLOpenEnumeration mbr) {
		super(mbr);

		childrenHandler = new EnumerationOpenChildrenHandler(this);
		extensionHandler = new ExtensionHandler(this);
	}

	@Override
	public boolean isExtensionPointTarget() {
		return true;
	}

	@Override
	public String getNavigatorName() {
		return getName();
	}

	@Override
	public String getLabel() {
		return "OPEN";
	}

	@Override
	public LibraryNode getLibrary() {
		return owningLibrary;
	}

	@Override
	public void setLibrary(LibraryNode library) {
		owningLibrary = library;
	}

	@Override
	public void add(List<PropertyNode> properties, boolean clone) {
		for (PropertyNode p : properties)
			add(p);
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
	public LibraryMemberInterface clone(LibraryNode targetLib, String nameSuffix) {
		if (getLibrary() == null || !getLibrary().isEditable()) {
			// LOGGER.warn("Could not clone node because library " + getLibrary() + " it is not editable.");
			return null;
		}

		LibraryMemberInterface clone = null;

		// Use the compiler to create a new TL src object.
		TLModelElement newLM = (TLModelElement) cloneTLObj();
		if (newLM != null) {
			clone = NodeFactory.newLibraryMember((LibraryMember) newLM);
			if (nameSuffix != null)
				clone.setName(clone.getName() + nameSuffix);
			targetLib.addMember(clone);
		}
		return clone;
	}

	@Override
	public void removeProperty(PropertyNode pn) {
		if (pn instanceof EnumLiteralNode)
			getTLModelObject().removeValue(((EnumLiteralNode) pn).getTLModelObject());
	}

	@Override
	public void add(PropertyNode property, int i) {
		add(property);
	}

	@Override
	public void addLiteral(String literal) {
		if (isEditable_inMinor())
			new EnumLiteralNode(this, literal);
		if (childrenHandler != null)
			childrenHandler.clear();
	}

	@Override
	public void add(EnumLiteralNode enumLiteral) {
		if (isEditable_newToChain())
			getTLModelObject().addValue(enumLiteral.getTLModelObject());
		if (childrenHandler != null)
			childrenHandler.clear();
	}

	@Override
	public void copy(FacetInterface facet) {
		add(facet.getProperties(), true);
		// if (facet instanceof Enumeration)
		// for (String e : ((Enumeration) facet).getLiterals())
		// addLiteral(e);
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
		TLOpenEnumeration tlMinor = (TLOpenEnumeration) createMinorTLVersion(this);
		if (tlMinor != null)
			return super.createMinorVersionComponent(new EnumerationOpenNode(tlMinor));
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
	public List<PropertyNode> getProperties() {
		List<PropertyNode> kids = new ArrayList<>();
		for (Node kid : getChildrenHandler().get())
			kids.add((PropertyNode) kid);
		return kids;
	}

	@Override
	public INode.CommandType getAddCommand() {
		return INode.CommandType.ENUMERATION;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.interfaces.LibraryMemberInterface#getAliases()
	 */
	@Override
	public List<AliasNode> getAliases() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EnumerationOpenChildrenHandler getChildrenHandler() {
		return (EnumerationOpenChildrenHandler) childrenHandler;
	}

	@Override
	public ComponentNodeType getComponentNodeType() {
		return ComponentNodeType.OPEN_ENUM;
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

	// @Override
	// public PropertyOwnerInterface getFacet_Default() {
	// // TODO Auto-generated method stub
	// return null;
	// }

	@Override
	public Image getImage() {
		return Images.getImageRegistry().get(Images.Enumeration);
	}

	@Override
	public List<String> getLiterals() {
		ArrayList<String> literals = new ArrayList<>();
		for (TLEnumValue v : getTLModelObject().getValues())
			literals.add(v.getLiteral());
		return literals;
	}

	@Override
	public String getName() {
		return getTLModelObject() == null || getTLModelObject().getName() == null ? "" : getTLModelObject().getName();
	}

	@Override
	public TLOpenEnumeration getTLModelObject() {
		return (TLOpenEnumeration) tlObj;
	}

	@Override
	public boolean isAssignableToElementRef() {
		return false;
	}

	// @Override
	// public boolean isAssignableToSimple() {
	// return false;
	// }

	@Override
	public boolean isAssignableToVWA() {
		return true;
	}

	// @Override
	// public boolean isDefaultFacet() {
	// return false;
	// }

	@Override
	public boolean isMergeSupported() {
		return true;
	}

	@Override
	public boolean isRenameableWhereUsed() {
		return true;
	}

	// 10/4/2017 - set true based on model object behavior but i am not sure it is correct.
	@Override
	public boolean isSimpleAssignable() {
		return true;
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
		getChildrenHandler().clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.opentravel.schemas.node.interfaces.FacetInterface#copy(org.opentravel.schemas.node.interfaces.FacetInterface)
	 */
	@Override
	public Node setExtensible(boolean extensible) {
		if (isEditable_newToChain())
			if (!extensible)
				return new EnumerationClosedNode(this);
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
	public void sort() {
		getTLModelObject().sortValues(new StringComparator<TLEnumValue>() {
			@Override
			protected String getString(TLEnumValue object) {
				return object.getLiteral();
			}
		});
	}

	@Override
	public boolean isFacet(TLFacetType type) {
		return false;
	}

}
