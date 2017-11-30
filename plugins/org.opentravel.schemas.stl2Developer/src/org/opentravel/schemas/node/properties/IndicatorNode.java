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

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLIndicatorOwner;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemas.node.ComponentNodeType;
import org.opentravel.schemas.node.ImpliedNode;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFactory;
import org.opentravel.schemas.node.NodeNameUtils;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.types.TypeProvider;

/**
 * A property node that represents a boolean XML attribute with the semantics of "False unless present and true". See
 * {@link NodeFactory#newMemberOLD(INode, Object)}
 * 
 * @author Dave Hollander
 * 
 */

public class IndicatorNode extends PropertyNode {

	public IndicatorNode() {
		super();
	}

	public IndicatorNode(PropertyOwnerInterface parent, String name) {
		super(new TLIndicator(), parent, name);
	}

	public IndicatorNode(TLIndicator tlObj, PropertyOwnerInterface parent) {
		super(tlObj, parent);
	}

	@Override
	public void addToTL(final PropertyOwnerInterface owner, final int index) {
		if (owner.getTLModelObject() instanceof TLIndicatorOwner)
			try {
				((TLIndicatorOwner) owner.getTLModelObject()).addIndicator(index, getTLModelObject());
			} catch (IndexOutOfBoundsException e) {
				((TLIndicatorOwner) owner.getTLModelObject()).addIndicator(getTLModelObject());
			}
	}

	@Override
	public boolean canAssign(Node type) {
		return false;
	}

	@Override
	public INode createProperty(Node type) {
		TLIndicator tlClone = (TLIndicator) cloneTLObj();
		IndicatorNode n = new IndicatorNode(tlClone, null);
		return super.createProperty(n, type);
	}

	@Override
	public TypeProvider getAssignedType() {
		return getRequiredType();
	}

	@Override
	public ComponentNodeType getComponentNodeType() {
		return ComponentNodeType.INDICATOR;
	}

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

	@Override
	public Image getImage() {
		return Images.getImageRegistry().get(Images.Indicator);
	}

	@Override
	public String getName() {
		return getTLModelObject() != null ? emptyIfNull(getTLModelObject().getName()) : "";
	}

	@Override
	public Node getParent() {
		if ((parent == null || parent.isDeleted()) && getTLModelObject() != null)
			// The parent may have failed to rebuild children
			parent = Node.GetNode(getTLModelObject().getOwner());
		return parent;
	}

	@Override
	public ImpliedNode getRequiredType() {
		return ModelNode.getIndicatorNode();
	}

	@Override
	public TLIndicator getTLModelObject() {
		return (TLIndicator) tlObj;
	}

	@Override
	public int indexOfTLProperty() {
		return getTLModelObject() != null ? getTLModelObject().getOwner().getIndicators().indexOf(getTLModelObject())
				: 0;
	}

	@Override
	public boolean isEnabled_AssignType() {
		return false;
	}

	@Override
	public boolean isRenameable() {
		return isEditable() && !isInherited();
	}

	@Override
	public void setName(String name) {
		if (getTLModelObject() != null)
			getTLModelObject().setName(NodeNameUtils.fixIndicatorName(name));
	}

	@Override
	protected void moveDownTL() {
		if (getTLModelObject() != null)
			getTLModelObject().moveDown();
	}

	@Override
	protected void moveUpTL() {
		if (getTLModelObject() != null)
			getTLModelObject().moveUp();
	}

	@Override
	protected void removeFromTL() {
		if (getTLModelObject() != null)
			getTLModelObject().getOwner().removeIndicator(getTLModelObject());
	}

	@Override
	public boolean setAssignedType(TLModelElement tlProvider) {
		return false;
	}

}
