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
package org.opentravel.schemas.node.properties;

import org.opentravel.schemacompiler.codegen.util.PropertyCodegenUtils;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.interfaces.FacadeInterface;
import org.opentravel.schemas.node.interfaces.FacetInterface;
import org.opentravel.schemas.node.interfaces.InheritedInterface;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeUserHandler;

/**
 * A property node that represents an inherited attribute. It is simply a facade for the underlying attribute from which
 * it was created.
 * <p>
 * The node is typically created from the node associated with a TL Attribute retrieved using
 * {@link PropertyCodegenUtils#getInheritedAttributes(org.opentravel.schemacompiler.model.TLFacet)}. It is a separate
 * node to allow it to be displayed uniquely in navigator trees.
 * 
 * @author Dave Hollander
 * 
 */
public class InheritedAttributeNode extends AttributeNode implements InheritedInterface, FacadeInterface {

	private AttributeNode inheritedFrom = null;

	public InheritedAttributeNode(AttributeNode from, FacetInterface parent) {
		super();
		inheritedFrom = from;
		this.parent = (Node) parent;
		changeHandler = null;
	}

	@Override
	public boolean canAssign(Node type) {
		return false;
	}

	@Override
	public TypeProvider getAssignedType() {
		return getInheritedFrom() != null ? getInheritedFrom().getAssignedType() : null;
	}

	@Override
	public String getLabel() {
		return getInheritedFrom() != null ? getInheritedFrom().getLabel() : "";
	}

	@Override
	public String getName() {
		return getInheritedFrom() != null ? getInheritedFrom().getName() : "";
	}

	@Override
	public TLAttribute getTLModelObject() {
		return getInheritedFrom() != null ? getInheritedFrom().getTLModelObject() : null;
	}

	/**
	 * @return the typeHandler
	 */
	@Override
	public TypeUserHandler getTypeHandler() {
		return getInheritedFrom() != null ? getInheritedFrom().getTypeHandler() : null;
	}

	/**
	 * Override to provide GUI assist: Since attributes can be renamed, there is no need to use the alias. Aliases are
	 * not TLAttributeType members so the GUI assist must convert before assignment.
	 */
	@Override
	public TypeProvider setAssignedType(TypeProvider provider) {
		return null;
	}

	@Override
	public IValueWithContextHandler setEquivalent(String example) {
		getEquivalentHandler().set(example, null);
		return getInheritedFrom().getEquivalentHandler();
	}

	@Override
	public IValueWithContextHandler setExample(String example) {
		getExampleHandler().set(example, null);
		return getInheritedFrom().getExampleHandler();
	}

	@Override
	public boolean setAssignedTLType(TLModelElement tla) {
		return false;
	}

	@Override
	public AttributeNode getInheritedFrom() {
		return inheritedFrom;
	}

	@Override
	public AttributeNode get() {
		return getInheritedFrom();
	}
}
