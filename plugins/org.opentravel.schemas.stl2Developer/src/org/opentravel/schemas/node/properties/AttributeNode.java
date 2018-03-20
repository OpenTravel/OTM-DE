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
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLAttributeOwner;
import org.opentravel.schemacompiler.model.TLAttributeType;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemas.node.ComponentNodeType;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFactory;
import org.opentravel.schemas.node.NodeNameUtils;
import org.opentravel.schemas.node.facets.AttributeFacetNode;
import org.opentravel.schemas.node.interfaces.FacetInterface;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.typeProviders.AliasNode;
import org.opentravel.schemas.node.typeProviders.VWA_Node;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.types.TypeProvider;

/**
 * A property node that represents an XML attribute. See {@link NodeFactory#newMemberOLD(INode, Object)}
 * 
 * @author Dave Hollander
 * 
 */
public class AttributeNode extends PropertyNode {

	public AttributeNode() {
		super();
	}

	public AttributeNode(FacetInterface parent, String name) {
		this(parent, name, ModelNode.getUnassignedNode());
	}

	/**
	 * Create a new Attribute Node property and add to the facet.
	 * 
	 * @param parent
	 *            facet node to attach to
	 * @param name
	 *            to give the property
	 */
	public AttributeNode(FacetInterface parent, String name, TypeProvider type) {
		super(new TLAttribute(), parent, name);
		setAssignedType(type);
		setName(name); // super.setName will not work if missing type
	}

	public AttributeNode(TLAttribute tlObj, FacetInterface parent) {
		super(tlObj, parent);
	}

	@Override
	public void addToTL(FacetInterface owner, final int index) {
		if (owner != null)
			if (owner.getTLModelObject() instanceof TLAttributeOwner)
				try {
					((TLAttributeOwner) owner.getTLModelObject()).addAttribute(index, getTLModelObject());
				} catch (IndexOutOfBoundsException e) {
					((TLAttributeOwner) owner.getTLModelObject()).addAttribute(getTLModelObject());
				}
		owner.getChildrenHandler().clear();
	}

	@Override
	public boolean canAssign(Node type) {
		if (type != null && type instanceof TypeProvider) {
			TypeProvider provider = (TypeProvider) type;

			// GUI assist: aliases stand in for their base type on attributes.
			if (type instanceof AliasNode)
				provider = (TypeProvider) type.getOwningComponent();

			if (getOwningComponent() instanceof VWA_Node)
				return provider.isAssignableToVWA();
			else
				return provider.isAssignableToSimple();
		}
		return false;
	}

	@Override
	public INode createProperty(Node type) {
		// Clone this TL Object
		TLAttribute tlClone = (TLAttribute) cloneTLObj();
		AttributeNode n = new AttributeNode(tlClone, null);
		return super.createProperty(n, type);
	}

	@Override
	public NamedEntity getAssignedTLNamedEntity() {
		return getTLModelObject() != null ? getTLModelObject().getType() : null;
	}

	@Override
	public ComponentNodeType getComponentNodeType() {
		return ComponentNodeType.ATTRIBUTE;
	}

	// @Override
	// public IValueWithContextHandler getEquivalentHandler() {
	// if (equivalentHandler == null)
	// equivalentHandler = new EqExOneValueHandler(this, ValueWithContextType.EQUIVALENT);
	// return equivalentHandler;
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
		return Images.getImageRegistry().get(Images.XSDAttribute);
	}

	@Override
	public String getLabel() {
		String label = getName();
		if (getAssignedType() != null)
			label += " [" + getTypeNameWithPrefix() + "]";
		return label;
	}

	@Override
	public String getName() {
		if (deleted)
			return "-d-";
		if (getTLModelObject().getType() == null)
			setAssignedType();
		return emptyIfNull(getTLModelObject().getName());
	}

	@Override
	public Node getParent() {
		if ((parent == null || parent.isDeleted()) && getTLModelObject() != null)
			// The parent may have failed to rebuild children
			parent = Node.GetNode(getTLModelObject().getOwner());
		return parent;
	}

	@Override
	public TLAttribute getTLModelObject() {
		return (TLAttribute) tlObj;
	}

	@Override
	public int indexOfTLProperty() {
		return getTLModelObject() != null ? getTLModelObject().getOwner().getAttributes().indexOf(getTLModelObject())
				: 0;
	}

	@Override
	public boolean isMandatory() {
		return getTLModelObject() != null ? getTLModelObject().isMandatory() : false;
	}

	@Override
	public boolean isOnlySimpleTypeUser() {
		// allow VWAs to be assigned to VWA Attributes.
		return parent != null && parent instanceof AttributeFacetNode ? false : true;
	}

	@Override
	public boolean isRenameable() {
		return isEditable() && !isInherited();
	}

	/**
	 * Override to provide GUI assist: Since attributes can be renamed, there is no need to use the alias. Aliases are
	 * not TLAttributeType members so the GUI assist must convert before assignment.
	 */
	@Override
	public boolean setAssignedType(TypeProvider provider) {
		if (provider instanceof AliasNode)
			provider = (TypeProvider) ((Node) provider).getOwningComponent();
		return getTypeHandler().set(provider);
	}

	@Override
	public IValueWithContextHandler setEquivalent(String example) {
		getEquivalentHandler().set(example, null);
		return equivalentHandler;
	}

	@Override
	public IValueWithContextHandler setExample(String example) {
		getExampleHandler().set(example, null);
		return exampleHandler;
	}

	/**
	 * Allowed in major versions and on objects new in a minor.
	 */
	@Override
	public void setMandatory(final boolean selection) {
		if (getTLModelObject() != null)
			if (isEditable_newToChain())
				if (getOwningComponent().isNewToChain() || !getLibrary().isInChain())
					getTLModelObject().setMandatory(selection);
	}

	@Override
	public void setName(String name) {
		if (getTLModelObject() != null)
			getTLModelObject().setName(NodeNameUtils.fixAttributeName(name));
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
		if (getParent() != null && getParent().getTLModelObject() instanceof TLAttributeOwner)
			((TLAttributeOwner) getParent().getTLModelObject()).removeAttribute(getTLModelObject());
	}

	@Override
	public void removeAssignedTLType() {
		getTLModelObject().setType(null);
		setAssignedType();
	}

	@Override
	public boolean setAssignedTLType(TLModelElement tla) {
		if (tla == getTLModelObject().getType())
			return false;
		if (tla instanceof TLAttributeType)
			getTLModelObject().setType((TLAttributeType) tla);
		return getTLModelObject().getType() == tla;
	}

}
