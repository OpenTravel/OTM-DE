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
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLPropertyOwner;
import org.opentravel.schemacompiler.model.TLPropertyType;
import org.opentravel.schemas.node.ComponentNodeType;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeEditStatus;
import org.opentravel.schemas.node.NodeFactory;
import org.opentravel.schemas.node.NodeNameUtils;
import org.opentravel.schemas.node.interfaces.FacetInterface;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.types.TypeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A property node that represents an XML element. See {@link NodeFactory#newMemberOLD(INode, Object)}
 * 
 * @author Dave Hollander
 * 
 */

public class ElementNode extends TypedPropertyNode {
	private static final Logger LOGGER = LoggerFactory.getLogger(ElementNode.class);

	/**
	 * Add an element property to a facet or extension point.
	 * 
	 * @param parent
	 *            - if null, the caller must link the node and add to TL Model parent
	 * @param name
	 */
	public ElementNode(FacetInterface parent, String name) {
		this(parent, name, null);
		if (parent != null)
			changeHandler = new PropertyRoleChangeHandler(this);
	}

	/**
	 * Add element to property owner (facet, extension point).
	 * 
	 * @param parent
	 *            - if null, the caller must link the node and add to TL Model parent
	 * @param name
	 * @param type
	 *            type to assign, will be set to" unassigned" if null
	 */
	public ElementNode(FacetInterface parent, String name, TypeProvider type) {
		super(new TLProperty(), parent, name);
		setAssignedType(type);
		setName(name); // super.setName will not work if missing type
		if (parent != null)
			changeHandler = new PropertyRoleChangeHandler(this);
	}

	/**
	 * Create an element node from the TL Model object.
	 * 
	 * @param tlObj
	 *            TL Model object to represent
	 * @param parent
	 *            if not null, add element to the parent.
	 */
	public ElementNode(TLProperty tlObj, FacetInterface parent) {
		super(tlObj, parent);
		if (getEditStatus().equals(NodeEditStatus.MINOR))
			setMandatory(false);
		if (tlObj.getType() == null)
			setAssignedType();
		if (parent != null)
			changeHandler = new PropertyRoleChangeHandler(this);
	}

	public ElementNode() {
		super();
		if (parent != null)
			changeHandler = new PropertyRoleChangeHandler(this);
	}

	@Override
	public void addToTL(final FacetInterface owner, final int index) {
		if (owner.getTLModelObject() instanceof TLPropertyOwner)
			try {
				((TLPropertyOwner) owner.getTLModelObject()).addElement(index, getTLModelObject());
			} catch (IndexOutOfBoundsException e) {
				((TLPropertyOwner) owner.getTLModelObject()).addElement(getTLModelObject());
			}
		owner.getChildrenHandler().clear();
		setParent((Node) owner);
	}

	@Override
	public boolean canAssign(Node type) {
		return (type instanceof TypeProvider);
	}

	@Override
	public INode createProperty(Node type) {
		// Clone this TL Object
		TLProperty tlClone = (TLProperty) cloneTLObj();
		ElementNode n = new ElementNode(tlClone, null);
		return super.createProperty(n, type);
	}

	@Override
	public NamedEntity getAssignedTLNamedEntity() {
		if (getTLModelObject() == null)
			return null;
		return getTLModelObject().getType();
	}

	@Override
	public String getAssignedTLTypeName() {
		return getTLModelObject() != null ? getTLModelObject().getTypeName() : "";
	}

	@Override
	public ComponentNodeType getComponentNodeType() {
		return ComponentNodeType.ELEMENT;
	}

	@Override
	public Image getImage() {
		return Images.getImageRegistry().get(Images.XSDElement);
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
		return super.getParent((TLModelElement) getTLModelObject().getOwner(), true);
	}

	public int getRepeat() {
		return getTLModelObject().getRepeat();
	}

	@Override
	public TLProperty getTLModelObject() {
		return (TLProperty) tlObj;
	}

	/**
	 * Get the index (0..sizeof()) of this property in the facet list.
	 */
	@Override
	public int indexOfTLProperty() {
		if (getTLModelObject() == null)
			return 0;
		return getTLModelObject().getOwner().getElements().indexOf(getTLModelObject());
	}

	@Override
	public boolean isMandatory() {
		if (getTLModelObject() == null)
			return false;
		return getTLModelObject().isMandatory();
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
		if (isEditable_newToChain())
			if (getOwningComponent().isNewToChain() || !getLibrary().isInChain())
				getTLModelObject().setMandatory(selection);
	}

	@Override
	public void setName(String name) {
		// let NodeNameUtils fix it as needed.
		if (getTLModelObject() != null)
			getTLModelObject().setName(NodeNameUtils.fixElementName(this, name));
	}

	public void setRepeat(final int i) {
		if (isEditable_newToChain())
			getTLModelObject().setRepeat(i);
		return;
	}

	@Override
	protected void moveDownTL() {
		getTLModelObject().moveDown();
	}

	@Override
	protected void moveUpTL() {
		getTLModelObject().moveUp();
	}

	@Override
	protected void removeFromTL() {
		if (getParent() != null && getParent().getTLModelObject() instanceof TLPropertyOwner)
			((TLPropertyOwner) getParent().getTLModelObject()).removeProperty(getTLModelObject());
	}

	@Override
	public void removeAssignedTLType() {
		setAssignedType();
		getTLModelObject().setType(null);
	}

	@Override
	public boolean setAssignedTLType(TLModelElement tla) {
		if (tla == null)
			return false; // Never override a saved type assignment
		if (tla == getTLModelObject().getType())
			return false;
		if (tla instanceof TLPropertyType)
			getTLModelObject().setType((TLPropertyType) tla);
		setName(getName()); // fix name if needed
		return getTLModelObject().getType() == tla;
	}

}
