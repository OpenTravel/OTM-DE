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
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemas.node.ComponentNodeType;
import org.opentravel.schemas.node.ImpliedNode;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFactory;
import org.opentravel.schemas.node.NodeNameUtils;
import org.opentravel.schemas.node.PropertyNodeType;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.types.TypeProvider;

/**
 * A property node that represents a boolean XML attribute with the semantics of "False unless present and true". See
 * {@link NodeFactory#newComponentMember(INode, Object)}
 * 
 * @author Dave Hollander
 * 
 */

public class IndicatorNode extends PropertyNode {

	public IndicatorNode(PropertyOwnerInterface parent, String name) {
		super(new TLIndicator(), (Node) parent, name, PropertyNodeType.INDICATOR);
		// setIdentity(name);

		assert (!getTLModelObject().getListeners().isEmpty());
	}

	/**
	 * 
	 * @param tlObj
	 * @param parent
	 *            either a facet or extension point facet
	 */
	public IndicatorNode(TLModelElement tlObj, PropertyOwnerInterface parent) {
		super(tlObj, (INode) parent, PropertyNodeType.INDICATOR);

		if (!(tlObj instanceof TLIndicator))
			throw new IllegalArgumentException("Invalid object for an indicator.");
		assert (!getTLModelObject().getListeners().isEmpty());
	}

	@Override
	public boolean canAssign(Node type) {
		return (type == ModelNode.getIndicatorNode() || type == ModelNode.getUndefinedNode());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.node.PropertyNode#createProperty(org.opentravel.schemas.node.Node)
	 */
	@Override
	public INode createProperty(Node type) {
		TLIndicator tlObj = (TLIndicator) cloneTLObj();
		int index = indexOfNode();
		((TLIndicator) getTLModelObject()).getOwner().addIndicator(index, tlObj);
		IndicatorElementNode n = new IndicatorElementNode(tlObj, null);
		n.setName(type.getName());
		getParent().linkChild(n, indexOfNode());
		((TLIndicator) getTLModelObject()).getOwner().addIndicator(index, tlObj);
		n.setDescription(type.getDescription());
		return n;
	}

	@Override
	public TypeProvider getAssignedType() {
		return getRequiredType();
	}

	@Override
	public ImpliedNode getRequiredType() {
		return ModelNode.getIndicatorNode();
	}

	@Override
	public String getName() {
		return getTLModelObject() == null || getTLModelObject().getName() == null
				|| getTLModelObject().getName().isEmpty() ? "" : getTLModelObject().getName();
	}

	@Override
	public TLIndicator getTLModelObject() {
		return (TLIndicator) (modelObject != null ? modelObject.getTLModelObj() : null);
	}

	@Override
	public Image getImage() {
		return Images.getImageRegistry().get(Images.Indicator);
	}

	@Override
	public ComponentNodeType getComponentNodeType() {
		return ComponentNodeType.INDICATOR;
	}

	@Override
	public boolean isNavChild(boolean deep) {
		return false;
	}

	@Override
	public String getLabel() {
		return getName();
		// return modelObject.getLabel() == null ? "" : modelObject.getLabel();
	}

	@Override
	public int indexOfTLProperty() {
		final TLIndicator thisProp = (TLIndicator) getTLModelObject();
		return thisProp.getOwner().getIndicators().indexOf(thisProp);
	}

	@Override
	public boolean isIndicator() {
		return true;
	}

	@Override
	public void setName(String name) {
		getTLModelObject().setName(NodeNameUtils.fixIndicatorName(name));
	}

}
