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
import org.opentravel.schemacompiler.model.TLAbstractEnumeration;
import org.opentravel.schemacompiler.model.TLEnumValue;
import org.opentravel.schemas.node.ComponentNodeType;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFactory;
import org.opentravel.schemas.node.NodeNameUtils;
import org.opentravel.schemas.node.interfaces.Enumeration;
import org.opentravel.schemas.node.interfaces.FacetInterface;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.typeProviders.EnumerationClosedNode;
import org.opentravel.schemas.node.typeProviders.EnumerationOpenNode;
import org.opentravel.schemas.properties.Images;

/**
 * A property node that represents a enumeration literal. See {@link NodeFactory#newMemberOLD(INode, Object)}
 * 
 * @author Dave Hollander
 * 
 */

public class EnumLiteralNode extends UnTypedPropertyNode {

	public EnumLiteralNode(EnumerationClosedNode parent, String name) {
		super(new TLEnumValue(), parent, name);
		getTLModelObject().setLiteral(NodeNameUtils.fixEnumerationValue(name));
		parent.addProperty(this);
	}

	public EnumLiteralNode(EnumerationOpenNode parent, String name) {
		super(new TLEnumValue(), parent, name);
		getTLModelObject().setLiteral(NodeNameUtils.fixEnumerationValue(name));
		parent.addProperty(this);
	}

	public EnumLiteralNode(TLEnumValue tlObj, FacetInterface parent) {
		super(tlObj, parent);
	}

	public EnumLiteralNode(TLEnumValue tlObj) {
		super(tlObj, null);
	}

	public EnumLiteralNode() {
		super();
	}

	@Override
	public void addToTL(final FacetInterface owner, final int index) {
		if (owner.getTLModelObject() instanceof TLAbstractEnumeration)
			try {
				((TLAbstractEnumeration) owner.getTLModelObject()).addValue(index, getTLModelObject());
			} catch (IndexOutOfBoundsException e) {
				((TLAbstractEnumeration) owner.getTLModelObject()).addValue(getTLModelObject());
			}
		owner.getChildrenHandler().clear();
	}

	@Override
	protected void removeFromTL() {
		if (getParent() != null && getParent().getTLModelObject() instanceof TLAbstractEnumeration)
			((TLAbstractEnumeration) getParent().getTLModelObject()).removeValue(getTLModelObject());
	}

	/**
	 * Clone this literal. Parameters can be null.
	 */
	@Override
	public EnumLiteralNode clone(Node parent, String nameSuffix) {
		EnumLiteralNode eln = new EnumLiteralNode((TLEnumValue) getTLModelObject().cloneElement());
		if (parent != null && parent instanceof Enumeration)
			((Enumeration) parent).add(eln);
		if (nameSuffix != null && !nameSuffix.isEmpty())
			eln.setName(eln.getName() + nameSuffix);
		return eln;
	}

	@Override
	public INode createProperty(Node type) {
		TLEnumValue tlClone = (TLEnumValue) cloneTLObj();
		EnumLiteralNode n = new EnumLiteralNode(tlClone, null);
		return super.createProperty(n, type);
	}

	@Override
	public INode.CommandType getAddCommand() {
		return INode.CommandType.ENUMERATION;
	}

	@Override
	public ComponentNodeType getComponentNodeType() {
		return ComponentNodeType.ENUM_LITERAL;
	}

	@Override
	public Node getParent() {
		if ((parent == null || parent.isDeleted()) && getTLModelObject() != null)
			// The parent may have failed to rebuild children
			parent = Node.GetNode(getTLModelObject().getOwningEnum());
		return parent;
	}

	@Override
	public String getName() {
		return emptyIfNull(getTLModelObject().getLiteral());
	}

	@Override
	public TLEnumValue getTLModelObject() {
		return (TLEnumValue) tlObj;
	}

	@Override
	public Image getImage() {
		return Images.getImageRegistry().get(Images.RoleValue);
	}

	// @Override
	// public BaseNodeListener getNewListener() {
	// return new NodeIdentityListener(this);
	// }

	// @Override
	// public LibraryMemberInterface getOwningComponent() {
	// return (LibraryMemberInterface) getParent();
	// }

	@Override
	protected void moveDownTL() {
		getTLModelObject().moveDown();
	}

	@Override
	protected void moveUpTL() {
		getTLModelObject().moveUp();
	}

	@Override
	public void setName(String name) {
		if (isEditable_newToChain() && getTLModelObject() != null)
			getTLModelObject().setLiteral(NodeNameUtils.fixEnumerationValue(name));
	}

}
