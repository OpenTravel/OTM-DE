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
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.interfaces.FacadeInterface;
import org.opentravel.schemas.node.interfaces.FacetInterface;
import org.opentravel.schemas.node.interfaces.InheritedInterface;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeUserHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A property node that represents an inherited element property. It is simply a facade for the underlying property from
 * which it was created.
 * <p>
 * The node is typically created from the node associated with a TL Indicator retrieved using
 * {@link PropertyCodegenUtils#getInheritedFacetProperties(org.opentravel.schemacompiler.model.TLFacet)}. It is a
 * separate node to allow it to be displayed uniquely in navigator trees.
 * 
 * @author Dave Hollander
 * 
 */

public class InheritedElementNode extends ElementNode implements InheritedInterface, FacadeInterface {
	private static final Logger LOGGER = LoggerFactory.getLogger(InheritedElementNode.class);

	private ElementNode inheritedFrom = null;

	public InheritedElementNode(ElementNode from, FacetInterface parent) {
		super();
		inheritedFrom = from;
		this.parent = (Node) parent;
		changeHandler = null;

		// FIXME - add set listener
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
	public String getEquivalent(String context) {
		return getInheritedFrom() != null ? getInheritedFrom().getEquivalentHandler().get(context) : "";
	}

	@Override
	public IValueWithContextHandler getEquivalentHandler() {
		return getInheritedFrom() != null ? getInheritedFrom().getEquivalentHandler() : null;
	}

	@Override
	public String getExample(String context) {
		return getInheritedFrom() != null ? getInheritedFrom().getExampleHandler().get(context) : "";
	}

	@Override
	public IValueWithContextHandler getExampleHandler() {
		return getInheritedFrom() != null ? getInheritedFrom().exampleHandler : null;
	}

	@Override
	public ElementNode getInheritedFrom() {
		return inheritedFrom;
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
	public Node getParent() {
		return parent;
	}

	@Override
	public TLProperty getTLModelObject() {
		return getInheritedFrom() != null ? getInheritedFrom().getTLModelObject() : null;
	}

	/**
	 * @return the typeHandler
	 */
	@Override
	public TypeUserHandler getTypeHandler() {
		return getInheritedFrom() != null ? getInheritedFrom().getTypeHandler() : null;
	}

	@Override
	public boolean setAssignedTLType(TLModelElement tla) {
		return false;
	}

	@Override
	public Node get() {
		return getInheritedFrom();
	}
}
