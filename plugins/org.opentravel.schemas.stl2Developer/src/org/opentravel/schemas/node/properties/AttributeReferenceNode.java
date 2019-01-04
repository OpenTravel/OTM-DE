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
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLPropertyType;
import org.opentravel.schemas.node.ComponentNodeType;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
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
 * An attribute property that represents a reference to an object.
 * 
 * @see ElementReferenceNode
 * 
 * @author Dave Hollander
 * 
 */
public class AttributeReferenceNode extends AttributeNode {

	// FIXME - remove name from constructors
	public AttributeReferenceNode(FacetInterface parent) {
		this(parent, ModelNode.getUnassignedNode());
	}

	public AttributeReferenceNode(FacetInterface facet, TypeProvider reference) {
		super(facet, "", reference);
		getTLModelObject().setReference(true);
		setAssignedType(reference);
	}

	/**
	 * 
	 * @param tlObj
	 *            TLAttribute
	 * @param parent
	 *            can be null
	 */
	public AttributeReferenceNode(TLAttribute tlObj, FacetInterface parent) {
		super(tlObj, parent);
		getTLModelObject().setReference(true);
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
	public INode createProperty(Node type) {
		TLAttribute tlObj = (TLAttribute) cloneTLObj();
		tlObj.setReference(true);

		getTLModelObject().getOwner().addAttribute(tlObj);
		AttributeReferenceNode n = new AttributeReferenceNode(tlObj, null);
		n.setName(type.getName());
		// getParent().linkChild(n);
		n.setDescription(type.getDescription());
		if (type instanceof TypeProvider)
			n.setAssignedType((TypeProvider) type);
		return n;
	}

	@Override
	public ComponentNodeType getComponentNodeType() {
		return ComponentNodeType.ATTRIBUTE_REF;
	}

	@Override
	public Image getImage() {
		return Images.getImageRegistry().get(Images.ID_Attr_Reference);
	}

	@Override
	public TypeSelectionFilter getTypeSelectionFilter() {
		return new TypeTreeIdReferenceTypeOnlyFilter();
	}

	@Override
	public boolean isRenameable() {
		return false;
	}

	@Override
	public TypeProvider setAssignedType(TypeProvider provider) {
		boolean result = getTypeHandler().set(provider);
		initName();
		return result ? provider : null;
	}

	@Override
	public boolean setAssignedTLType(TLModelElement tla) {
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
