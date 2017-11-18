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
import org.opentravel.schemacompiler.model.TLEnumValue;
import org.opentravel.schemacompiler.model.TLOpenEnumeration;
import org.opentravel.schemas.node.facets.SimpleFacetNode;
import org.opentravel.schemas.node.handlers.children.EnumerationOpenChildrenHandler;
import org.opentravel.schemas.node.interfaces.ComplexComponentInterface;
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

public class EnumerationOpenNode extends LibraryMemberBase implements ComplexComponentInterface, Enumeration, Sortable,
		PropertyOwnerInterface, ExtensionOwner, VersionedObjectInterface, LibraryMemberInterface, TypeProvider {
	// private static final Logger LOGGER = LoggerFactory.getLogger(EnumerationOpenNode.class);
	private ExtensionHandler extensionHandler = null;

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

		// if (getLibrary().isInChain())
		// getChain().add(this);
		//
		// }""
		// LOGGER.debug("Created open enum from closed.");
	}

	public EnumerationOpenNode(TLOpenEnumeration mbr) {
		super(mbr);

		childrenHandler = new EnumerationOpenChildrenHandler(this);
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
	public void addProperty(EnumLiteralNode enumLiteral) {
		if (isEditable_newToChain()) {
			getTLModelObject().addValue(enumLiteral.getTLModelObject());
			if (childrenHandler != null)
				childrenHandler.clear();
		}
	}

	@Override
	public void add(PropertyNode property, int i) {
		addProperty(property);
	}

	@Override
	public void addProperty(Node enumLiteral) {
		if (enumLiteral instanceof EnumLiteralNode)
			addProperty((EnumLiteralNode) enumLiteral);
	}

	@Override
	public void addProperty(PropertyNode property) {
		if (property instanceof EnumLiteralNode)
			addProperty((EnumLiteralNode) property);
	}

	@Override
	public ComponentNode createMinorVersionComponent() {
		return super
				.createMinorVersionComponent(new EnumerationOpenNode((TLOpenEnumeration) createMinorTLVersion(this)));
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

	@Override
	public PropertyOwnerInterface getFacet_Attributes() {
		return null;
	}

	@Override
	public PropertyOwnerInterface getFacet_Default() {
		return this;
	}

	@Override
	public SimpleFacetNode getFacet_Simple() {
		return null;
	}

	@Override
	public Image getImage() {
		return Images.getImageRegistry().get(Images.Enumeration);
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
		return getTLModelObject() == null || getTLModelObject().getName() == null ? "" : getTLModelObject().getName();
	}

	@Override
	public TLOpenEnumeration getTLModelObject() {
		return (TLOpenEnumeration) tlObj;
		// return (TLOpenEnumeration) (modelObject != null ? modelObject.getTLModelObj() : null);
	}

	// public void initInheritedChildren() {
	// List<?> inheritedMOChildren = modelObject.getInheritedChildren();
	// if (inheritedMOChildren == null || inheritedMOChildren.isEmpty()) {
	// inheritedChildren = Collections.emptyList();
	// } else {
	// for (final Object obj : inheritedMOChildren) {
	// ComponentNode nn = NodeFactory.newMemberOLD(null, obj);
	// if (nn != null) {
	// linkInheritedChild(nn);
	// nn.inheritedFrom = null; // override value from link
	// }
	// }
	// }
	// }

	@Override
	public boolean isAssignableToElementRef() {
		return false;
	}

	@Override
	public boolean isAssignableToSimple() {
		return false;
	}

	@Override
	public boolean isAssignableToVWA() {
		return true;
	}

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

	// @Override
	// public void setAssignedType(TLModelElement tla) {
	// if (tla instanceof TLAbstractEnumeration) {
	// TLExtension extension = new TLExtension();
	// extension.setExtendsEntity((NamedEntity) tla);
	// getTLModelObject().setExtension(extension);
	// }
	// }

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

}
