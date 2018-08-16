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
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFactory;
import org.opentravel.schemas.node.NodeNameUtils;
import org.opentravel.schemas.node.interfaces.FacetInterface;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.properties.Images;

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
		if (parent != null)
			changeHandler = new PropertyRoleChangeHandler(this);
	}

	public IndicatorNode(FacetInterface parent, String name) {
		super(new TLIndicator(), parent, name);
		if (parent != null)
			changeHandler = new PropertyRoleChangeHandler(this);
	}

	public IndicatorNode(TLIndicator tlObj, FacetInterface parent) {
		super(tlObj, parent);
		if (parent != null)
			changeHandler = new PropertyRoleChangeHandler(this);
	}

	@Override
	public void addToTL(final FacetInterface owner, final int index) {
		if (owner.getTLModelObject() instanceof TLIndicatorOwner)
			try {
				((TLIndicatorOwner) owner.getTLModelObject()).addIndicator(index, getTLModelObject());
			} catch (IndexOutOfBoundsException e) {
				((TLIndicatorOwner) owner.getTLModelObject()).addIndicator(getTLModelObject());
			}
		owner.getChildrenHandler().clear();
		setParent((Node) owner);
	}

	@Override
	public boolean canAssign(Node type) {
		return false;
	}

	/**
	 * @return true if this node could be assigned a type but is unassigned.
	 */
	@Override
	public boolean isUnAssigned() {
		return false;
	}

	@Override
	public INode createProperty(Node type) {
		TLIndicator tlClone = (TLIndicator) cloneTLObj();
		IndicatorNode n = new IndicatorNode(tlClone, null);
		return super.createProperty(n, type);
	}

	@Override
	public ComponentNodeType getComponentNodeType() {
		return ComponentNodeType.INDICATOR;
	}

	@Override
	public Image getImage() {
		return Images.getImageRegistry().get(Images.Indicator);
	}

	@Override
	public String getName() {
		return getTLModelObject() != null ? getTLModelObject().getName() : "";
	}

	@Override
	public Node getParent() {
		return super.getParent((TLModelElement) getTLModelObject().getOwner(), true);
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
		if (getTLModelObject() != null && getTLModelObject().getOwner() != null)
			getTLModelObject().getOwner().removeIndicator(getTLModelObject());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.properties.PropertyNode#hasNavChildren(boolean)
	 */
	@Override
	public boolean hasNavChildren(boolean deep) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.properties.PropertyNode#isNavChild(boolean)
	 */
	@Override
	public boolean isNavChild(boolean deep) {
		return false;
	}

}
