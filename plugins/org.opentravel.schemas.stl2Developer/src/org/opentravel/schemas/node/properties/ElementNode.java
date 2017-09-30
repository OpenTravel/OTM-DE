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
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemas.modelObject.ElementPropertyMO;
import org.opentravel.schemas.node.ComponentNodeType;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeEditStatus;
import org.opentravel.schemas.node.NodeFactory;
import org.opentravel.schemas.node.NodeNameUtils;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.properties.EqExOneValueHandler.ValueWithContextType;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.types.TypeProvider;

/**
 * A property node that represents an XML element. See {@link NodeFactory#newMember(INode, Object)}
 * 
 * @author Dave Hollander
 * 
 */

public class ElementNode extends PropertyNode {

	/**
	 * Add an element property to a facet or extension point.
	 * 
	 * @param parent
	 *            - if null, the caller must link the node and add to TL Model parent
	 * @param name
	 */
	public ElementNode(PropertyOwnerInterface parent, String name) {
		this(parent, name, null);
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
	public ElementNode(PropertyOwnerInterface parent, String name, TypeProvider type) {
		super(new TLProperty(), (Node) parent, name, PropertyNodeType.ELEMENT);
		setAssignedType(type);
	}

	/**
	 * Create an element node from the TL Model object.
	 * 
	 * @param tlObj
	 *            TL Model object to represent
	 * @param parent
	 *            if not null, add element to the parent.
	 */
	public ElementNode(TLProperty tlObj, PropertyOwnerInterface parent) {
		super(tlObj, (INode) parent, PropertyNodeType.ELEMENT);
		if (getEditStatus().equals(NodeEditStatus.MINOR))
			setMandatory(false);
		else if (tlObj instanceof TLProperty)
			setMandatory(((TLProperty) tlObj).isMandatory()); // default value for properties

		assert (tlObj instanceof TLProperty);
		assert (modelObject instanceof ElementPropertyMO);
	}

	@Override
	public boolean canAssign(Node type) {
		return (type instanceof TypeProvider);
	}

	@Override
	public INode createProperty(Node type) {
		// int index = indexOfNode();
		int tlIndex = indexOfTLProperty();
		TLProperty tlObj = (TLProperty) cloneTLObj();
		getTLModelObject().getOwner().addElement(tlIndex, tlObj);
		ElementNode n = new ElementNode(tlObj, null);
		getParent().linkChild(n, indexOfNode());
		n.setDescription(type.getDescription());
		if (type instanceof TypeProvider)
			n.setAssignedType((TypeProvider) type);
		n.setName(type.getName());
		return n;
	}

	@Override
	public ComponentNodeType getComponentNodeType() {
		return ComponentNodeType.ELEMENT;
	}

	@Override
	public String getEquivalent(String context) {
		return getEquivalentHandler().get(context);
	}

	@Override
	public IValueWithContextHandler getEquivalentHandler() {
		if (equivalentHandler == null)
			equivalentHandler = new EqExOneValueHandler(this, ValueWithContextType.EQUIVALENT);
		return equivalentHandler;
	}

	@Override
	public String getExample(String context) {
		return getExampleHandler().get(context);
	}

	@Override
	public IValueWithContextHandler getExampleHandler() {
		if (exampleHandler == null)
			exampleHandler = new EqExOneValueHandler(this, ValueWithContextType.EXAMPLE);
		return exampleHandler;
	}

	@Override
	public Image getImage() {
		return Images.getImageRegistry().get(Images.XSDElement);
	}

	@Override
	public String getLabel() {
		String label = getName();
		if (getType() != null)
			label += " [" + getTypeNameWithPrefix() + "]";
		return label;
	}

	@Override
	public String getName() {
		return emptyIfNull(getTLModelObject().getName());
	}

	public int getRepeat() {
		return getTLModelObject().getRepeat();
	}

	@Override
	public TLProperty getTLModelObject() {
		return (TLProperty) (modelObject != null ? modelObject.getTLModelObj() : null);
	}

	/**
	 * Get the index (0..sizeof()) of this property in the facet list.
	 */
	@Override
	public int indexOfTLProperty() {
		return getTLModelObject().getOwner().getElements().indexOf(getTLModelObject());
	}

	@Override
	public boolean isMandatory() {
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
	public void setMandatory(final boolean selection) {
		if (isEditable_newToChain())
			if (getOwningComponent().isNewToChain() || !getLibrary().isInChain())
				getTLModelObject().setMandatory(selection);
	}

	@Override
	public void setName(String name) {
		// modelObject.setName(name); // try the passed name
		// modelObject.setName(NodeNameUtils.fixElementName(this)); // let utils fix it if needed.
		// getTLModelObject().setName(name); // Must set name before the utils try to fix them.
		getTLModelObject().setName(NodeNameUtils.fixElementName(this, name)); // let utils fix it as needed.
	}

	public void setRepeat(final int i) {
		if (isEditable_newToChain())
			getTLModelObject().setRepeat(i);
		return;
	}

}
