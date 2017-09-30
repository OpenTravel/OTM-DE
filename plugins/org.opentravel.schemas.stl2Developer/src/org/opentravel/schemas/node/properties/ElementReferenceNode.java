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
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemas.node.ComponentNodeType;
import org.opentravel.schemas.node.ImpliedNode;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.NodeFactory;
import org.opentravel.schemas.node.NodeNameUtils;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.types.TypeProvider;

/**
 * A property node that represents an XML element. See {@link NodeFactory#newMember(INode, Object)}
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
	public ElementReferenceNode(PropertyOwnerInterface parent, String name) {
		this(parent, name, ModelNode.getUnassignedNode());
	}

	public ElementReferenceNode(PropertyOwnerInterface parent, String name, TypeProvider reference) {
		super(parent, name, reference);

		getTLModelObject().setReference(true);
		setAssignedType(reference);
	}

	/**
	 * Create an element node from the TL Model object.
	 * 
	 * @param tlObj
	 *            TL Model object to represent
	 * @param parent
	 *            if not null, add element to the parent.
	 */
	public ElementReferenceNode(TLProperty tlObj, PropertyOwnerInterface parent) {
		super(tlObj, parent);
		getTLModelObject().setReference(true);
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
	public String getLabel() {
		return getName();
	}

	@Override
	public boolean isRenameable() {
		return false; // name must come from assigned object
	}

	@Override
	public void setName(String name) {
		QName ln = PropertyCodegenUtils.getDefaultSchemaElementName((NamedEntity) getAssignedTLObject(), true);
		if (ln == null || getType() == null || (getType() instanceof ImpliedNode))
			getTLModelObject().setName(NodeNameUtils.fixElementRefName(name));
		else {
			getTLModelObject().setName(ln.getLocalPart());
		}
	}

}
