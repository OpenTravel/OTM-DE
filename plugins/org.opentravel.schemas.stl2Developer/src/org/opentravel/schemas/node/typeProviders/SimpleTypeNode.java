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
package org.opentravel.schemas.node.typeProviders;

import java.util.Collections;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAttributeType;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemas.node.ComponentNodeType;
import org.opentravel.schemas.node.NodeNameUtils;
import org.opentravel.schemas.node.handlers.ConstraintHandler;
import org.opentravel.schemas.node.handlers.EqExOneValueHandler;
import org.opentravel.schemas.node.handlers.EqExOneValueHandler.ValueWithContextType;
import org.opentravel.schemas.node.handlers.children.SimpleTypeChildrenHandler;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.properties.IValueWithContextHandler;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.trees.type.TypeSelectionFilter;
import org.opentravel.schemas.trees.type.TypeTreeSimpleAssignableOnlyFilter;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeUser;
import org.opentravel.schemas.types.TypeUserHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles simple types (TLSimple).
 * 
 * @author Dave Hollander
 * 
 */
public class SimpleTypeNode extends SimpleTypeProviders implements TypeUser, LibraryMemberInterface {
	private static final Logger LOGGER = LoggerFactory.getLogger(SimpleTypeNode.class);

	// Handlers for constraints, equivalents and examples
	protected IValueWithContextHandler equivalentHandler = null;
	protected IValueWithContextHandler exampleHandler = null;
	protected ConstraintHandler constraintHandler = null;
	protected TypeUserHandler typeHandler = null;
	private LibraryNode owningLibrary = null;

	public SimpleTypeNode(TLSimple mbr) {
		super(mbr);
		typeHandler = new TypeUserHandler(this);
		constraintHandler = new ConstraintHandler(getTLModelObject(), this);
		childrenHandler = new SimpleTypeChildrenHandler(this);

		assert (getTLModelObject() != null);
	}

	@Override
	public TypeUserHandler getTypeHandler() {
		return typeHandler;
	}

	// @Override
	// public TypeSelectionFilter getTypeSelectionFilter() {
	// return new TypeTreeSimpleTypeOnlyFilter();
	// }
	@Override
	public TypeSelectionFilter getTypeSelectionFilter() {
		return new TypeTreeSimpleAssignableOnlyFilter();
	}

	@Override
	public NamedEntity getAssignedTLNamedEntity() {
		return getTLModelObject() != null ? getTLModelObject().getParentType() : null;
	}

	@Override
	public SimpleTypeChildrenHandler getChildrenHandler() {
		return (SimpleTypeChildrenHandler) childrenHandler;
	}

	@Override
	public ComponentNodeType getComponentNodeType() {
		return ComponentNodeType.SIMPLE;
	}

	@Override
	public ConstraintHandler getConstraintHandler() {
		return constraintHandler;
	}

	public String getEquivalent(String context) {
		if (equivalentHandler == null)
			equivalentHandler = new EqExOneValueHandler(this, ValueWithContextType.EQUIVALENT);
		return equivalentHandler != null ? equivalentHandler.get(context) : "";
	}

	/**
	 * @return equivalent handler if property has equivalents, null otherwise
	 */
	@Override
	public IValueWithContextHandler getEquivalentHandler() {
		return equivalentHandler;
	}

	@Override
	public String getAssignedPrefix() {
		return getXsdObjectHandler() != null ? getXsdObjectHandler().getAssignedPrefix() : getLibrary().getPrefix();
	}

	@Override
	public String getAssignedTLTypeName() {
		return getTLModelObject() != null ? getTLModelObject().getParentTypeName() : "";
	}

	public String getExample(String context) {
		if (exampleHandler == null)
			exampleHandler = new EqExOneValueHandler(this, ValueWithContextType.EXAMPLE);
		return exampleHandler != null ? exampleHandler.get(context) : "";
	}

	/**
	 * @return equivalent handler if property has equivalents, null otherwise
	 */
	@Override
	public IValueWithContextHandler getExampleHandler() {
		return exampleHandler;
	}

	@Override
	public Image getImage() {
		return Images.getImageRegistry().get(Images.XSDSimpleType);
	}

	@Override
	public LibraryNode getLibrary() {
		return owningLibrary;
	}

	@Override
	public String getName() {
		return getTLModelObject() == null || getTLModelObject().getName() == null ? "" : getTLModelObject().getName();
	}

	// Type User handler will resolve to xsd node as needed.
	@Override
	public TLSimple getTLModelObject() {
		return (TLSimple) tlObj;
	}

	// @Override
	// public boolean isOnlySimpleTypeUser() {
	// return true;
	// }
	//
	@Override
	public boolean isSimpleAssignable() {
		return true;
	}

	@Override
	public boolean isSimpleList() {
		return getTLModelObject().isListTypeInd();
	}

	public IValueWithContextHandler setEquivalent(String equivalent) {
		if (equivalentHandler == null)
			equivalentHandler = new EqExOneValueHandler(this, ValueWithContextType.EQUIVALENT);
		equivalentHandler.set(equivalent, null);
		return equivalentHandler;
	}

	public IValueWithContextHandler setExample(String example) {
		if (exampleHandler == null)
			exampleHandler = new EqExOneValueHandler(this, ValueWithContextType.EXAMPLE);
		exampleHandler.set(example, null);
		return exampleHandler;
	}

	public void setList(final boolean selected) {
		getTLModelObject().setPattern("");
		getTLModelObject().setListTypeInd(selected);
	}

	@Override
	public void setLibrary(LibraryNode library) {
		owningLibrary = library;
	}

	@Override
	public boolean setAssignedTLType(TLModelElement tla) {
		if (tla == null)
			return false; // Never override a saved type assignment
		if (tla == getTLModelObject().getParentType())
			return false;
		if (tla instanceof TLAttributeType)
			getTLModelObject().setParentType((TLAttributeType) tla);
		return getTLModelObject().getParentType() == tla;
	}

	@Override
	public void setName(String name) {
		getTLModelObject().setName(NodeNameUtils.fixSimpleTypeName(this, name));
	}

	@Override
	public List<AliasNode> getAliases() {
		return Collections.emptyList();
	}

	@Override
	public TLModelElement getAssignedTLObject() {
		return (TLModelElement) getTLModelObject().getParentType();
	}

	@Override
	public TypeProvider getAssignedType() {
		return typeHandler.get();
	}

	@Override
	public TypeProvider getRequiredType() {
		return null;
	}

	@Override
	public void removeAssignedTLType() {
		setAssignedType();
		getTLModelObject().setParentType(null);
	}

	@Override
	public TypeProvider setAssignedType(TypeProvider provider) {
		// Simple objects with parent of closed enumeration MUST have list checked
		if (provider instanceof EnumerationClosedNode)
			setList(true);
		return getTypeHandler().set(provider) ? provider : null;
	}

	@Override
	public boolean setAssignedType() {
		return typeHandler.set();
	}

}
