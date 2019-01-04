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

import javax.xml.namespace.QName;

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemacompiler.codegen.util.PropertyCodegenUtils;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLPropertyType;
import org.opentravel.schemas.node.ComponentNodeType;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFactory;
import org.opentravel.schemas.node.interfaces.FacetInterface;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.typeProviders.ImpliedNode;
import org.opentravel.schemas.node.typeProviders.facetOwners.BusinessObjectNode;
import org.opentravel.schemas.node.typeProviders.facetOwners.CoreObjectNode;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.trees.type.TypeSelectionFilter;
import org.opentravel.schemas.trees.type.TypeTreeIdReferenceTypeOnlyFilter;
import org.opentravel.schemas.types.TypeProvider;

/**
 * A property node that represents an XML element. See {@link NodeFactory#newMemberOLD(INode, Object)}
 * 
 * @author Dave Hollander
 * 
 */
public class ElementReferenceNode extends ElementNode {

	/**
	 * Add an element reference property to a facet or extension point.
	 * 
	 * @param parent
	 *            - if null, the caller must link the node and add to TL Model parent
	 * @param name
	 */
	public ElementReferenceNode(FacetInterface parent) {
		this(parent, ModelNode.getUnassignedNode());
	}

	public ElementReferenceNode(FacetInterface parent, TypeProvider reference) {
		super(parent, "", reference);

		getTLModelObject().setReference(true);
		assert getAssignedType() == reference;
		// setAssignedType(reference);
	}

	/**
	 * Create an element node from the TL Model object.
	 * 
	 * @param tlObj
	 *            TL Model object to represent
	 * @param parent
	 *            if not null, add element to the parent.
	 */
	public ElementReferenceNode(TLProperty tlObj, FacetInterface parent) {
		super(tlObj, parent);
		assert getTLModelObject().isReference();
	}

	@Override
	public boolean canAssign(Node type) {
		if (type instanceof BusinessObjectNode)
			return true;
		if (type instanceof CoreObjectNode)
			return true;
		if (type instanceof ImpliedNode)
			return true;
		return false;
	}

	@Override
	public ComponentNodeType getComponentNodeType() {
		return ComponentNodeType.ELEMENT_REF;
	}

	@Override
	public Image getImage() {
		return Images.getImageRegistry().get(Images.ID_Reference);
	}

	@Override
	public String getName() {
		if (getAssignedType() == null)
			setAssignedTLType(ModelNode.getEmptyNode().getTLModelObject());
		String name = getTLModelObject().getName();
		if (name == null || name.isEmpty())
			initName();
		if (getAssignedType() instanceof ImpliedNode)
			return getAssignedType().getName();
		return name;
	}

	@Override
	public TypeSelectionFilter getTypeSelectionFilter() {
		return new TypeTreeIdReferenceTypeOnlyFilter();
	}

	@Override
	public boolean isRenameable() {
		return false; // name must come from assigned object
	}

	@Override
	public TypeProvider setAssignedType(TypeProvider provider) {
		boolean result = getTypeHandler().set(provider);
		initName();
		return result ? provider : null;
	}

	@Override
	public boolean setAssignedTLType(TLModelElement tla) {
		if (tla == null)
			return false; // Never override a saved type assignment
		if (tla == getTLModelObject().getType())
			return false;
		if (canAssign(GetNode(tla)))
			if (tla instanceof TLPropertyType)
				getTLModelObject().setType((TLPropertyType) tla);
		return getTLModelObject().getType() == tla;
	}

	@Override
	public void setName(String name) {
		// NO-OP
	}

	// If the name is not set on the tlObj, set it.
	private void initName() {
		QName qn = PropertyCodegenUtils.getDefaultSchemaElementName((NamedEntity) getAssignedTLObject(), true);
		if (qn == null)
			getTLModelObject().setName("Missing");
		else {
			getTLModelObject().setName(qn.getLocalPart());
		}
	}

}
