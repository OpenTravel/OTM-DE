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

import java.util.Collections;
import java.util.List;

import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFactory;
import org.opentravel.schemas.node.interfaces.FacetInterface;
import org.opentravel.schemas.node.interfaces.INode;

/**
 * A property node that is untyped or fixed type (id, indicator)
 * 
 * that represents a boolean XML attribute with the semantics of "False unless present and true". See
 * {@link NodeFactory#newMemberOLD(INode, Object)}
 * 
 * @author Dave Hollander
 * 
 */

public abstract class UnTypedPropertyNode extends PropertyNode {

	public UnTypedPropertyNode() {
		super();
	}

	public UnTypedPropertyNode(TLModelElement tlObj, FacetInterface parent) {
		super(tlObj, parent);
	}

	protected UnTypedPropertyNode(final TLModelElement tlObj, final FacetInterface parent, final String name) {
		super(tlObj, parent, name);
	}

	@Override
	public boolean canAssign(Node type) {
		return false;
	}

	@Override
	public List<Node> getNavChildren(boolean deep) {
		return Collections.emptyList();
	}

	@Override
	public List<Node> getTreeChildren(boolean deep) {
		return Collections.emptyList();
	}

	@Override
	public boolean hasNavChildren(boolean deep) {
		return false;
	}

	@Override
	public boolean isEnabled_AssignType() {
		return false;
	}

	@Override
	public boolean isNavChild(boolean deep) {
		return false;
	}

	@Override
	public boolean isRenameable() {
		return isEditable() && !isInherited();
	}

	/**
	 * @return true if this node could be assigned a type but is unassigned.
	 */
	@Override
	public boolean isUnAssigned() {
		return false;
	}

}
