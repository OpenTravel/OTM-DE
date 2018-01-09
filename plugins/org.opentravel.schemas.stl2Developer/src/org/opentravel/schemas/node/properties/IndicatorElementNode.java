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
import org.opentravel.schemas.node.ComponentNodeType;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFactory;
import org.opentravel.schemas.node.NodeNameUtils;
import org.opentravel.schemas.node.interfaces.FacetInterface;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.properties.Images;

/**
 * A property node that represents a boolean XML element with the semantics of "False unless present and true". See
 * {@link NodeFactory#newMemberOLD(INode, Object)}
 * 
 * @author Dave Hollander
 * 
 */

public class IndicatorElementNode extends IndicatorNode {

	public IndicatorElementNode(FacetInterface parent, String name) {
		super(parent, name);
		getTLModelObject().setPublishAsElement(true);
	}

	public IndicatorElementNode(TLIndicator tlObj, FacetInterface parent) {
		super(tlObj, parent);
		tlObj.setPublishAsElement(true);
	}

	@Override
	public INode createProperty(Node type) {
		TLIndicator tlObj = (TLIndicator) cloneTLObj();
		IndicatorElementNode n = new IndicatorElementNode(tlObj, null);
		return super.createProperty(n, type);
	}

	@Override
	public ComponentNodeType getComponentNodeType() {
		return ComponentNodeType.INDICATOR_ELEMENT;
	}

	@Override
	public Image getImage() {
		return Images.getImageRegistry().get(Images.IndicatorElement);
	}

	@Override
	public boolean isNavChild(boolean deep) {
		return false;
	}

	@Override
	public void setName(String name) {
		getTLModelObject().setName(NodeNameUtils.fixIndicatorElementName(name));
	}

}
