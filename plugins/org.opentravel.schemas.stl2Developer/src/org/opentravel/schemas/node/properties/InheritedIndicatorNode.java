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
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.interfaces.FacadeInterface;
import org.opentravel.schemas.node.interfaces.InheritedInterface;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeUserHandler;

/**
 * A property node that represents an inherited indicator. It is simply a facade for the underlying indicator from which
 * it was created.
 * <p>
 * The node is typically created from the node associated with a TL Indicator retrieved using
 * {@link PropertyCodegenUtils#getInheritedFacetIndicators(org.opentravel.schemacompiler.model.TLFacet)}. It is a
 * separate node to allow it to be displayed uniquely in navigator trees.
 * 
 * @author Dave Hollander
 * 
 */

public class InheritedIndicatorNode extends IndicatorNode implements InheritedInterface, FacadeInterface {

	private IndicatorNode inheritedFrom = null;

	public InheritedIndicatorNode(IndicatorNode from, PropertyOwnerInterface parent) {
		super();
		inheritedFrom = from;
		this.parent = (Node) parent;
	}

	@Override
	public boolean canAssign(Node type) {
		return false;
	}

	@Override
	public IndicatorNode getInheritedFrom() {
		return inheritedFrom;
	}

	@Override
	public TypeProvider getAssignedType() {
		return getInheritedFrom().getRequiredType();
	}

	// @Override
	// public ComponentNodeType getComponentNodeType() {
	// return ComponentNodeType.INDICATOR;
	// }

	// @Override
	// public String getEquivalent(String context) {
	// return getEquivalentHandler().get(context);
	// }
	//
	// @Override
	// public IValueWithContextHandler getEquivalentHandler() {
	// if (equivalentHandler == null)
	// equivalentHandler = new EqExOneValueHandler(this, ValueWithContextType.EQUIVALENT);
	// return equivalentHandler;
	// }
	//
	// @Override
	// public String getExample(String context) {
	// return getExampleHandler().get(context);
	// }
	//
	// @Override
	// public IValueWithContextHandler getExampleHandler() {
	// if (exampleHandler == null)
	// exampleHandler = new EqExOneValueHandler(this, ValueWithContextType.EXAMPLE);
	// return exampleHandler;
	// }

	// @Override
	// public Image getImage() {
	// return Images.getImageRegistry().get(Images.Indicator);
	// }

	@Override
	public String getName() {
		if (deleted)
			return "";
		return emptyIfNull(getTLModelObject().getName());
	}

	@Override
	public Node getParent() {
		return parent;
	}

	// @Override
	// public ImpliedNode getRequiredType() {
	// return ModelNode.getIndicatorNode();
	// }

	/**
	 * @return the typeHandler
	 */
	@Override
	protected TypeUserHandler getTypeHandler() {
		return getInheritedFrom().getTypeHandler();
	}

	@Override
	public TLIndicator getTLModelObject() {
		return getInheritedFrom().getTLModelObject();
	}

	// @Override
	// public int indexOfTLProperty() {
	// return getTLModelObject() != null ? getTLModelObject().getOwner().getIndicators().indexOf(getTLModelObject())
	// : 0;
	// }
	//
	// @Override
	// public boolean isEnabled_AssignType() {
	// return false;
	// }
	//
	// @Override
	// public boolean isRenameable() {
	// return isEditable() && !isInherited();
	// }
	//
	// @Override
	// public void setName(String name) {
	// if (getTLModelObject() != null)
	// getTLModelObject().setName(NodeNameUtils.fixIndicatorName(name));
	// }
	//
	// @Override
	// protected void moveDownTL() {
	// if (getTLModelObject() != null)
	// getTLModelObject().moveDown();
	// }
	//
	// @Override
	// protected void moveUpTL() {
	// if (getTLModelObject() != null)
	// getTLModelObject().moveUp();
	// }

	// @Override
	// protected void removeFromTL() {
	// if (getTLModelObject() != null)
	// getTLModelObject().getOwner().removeIndicator(getTLModelObject());
	// }
	//
	// @Override
	// public boolean setAssignedType(TLModelElement tlProvider) {
	// return false;
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.interfaces.FacadeInterface#get()
	 */
	@Override
	public Node get() {
		return getInheritedFrom();
	}

}
