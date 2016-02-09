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
/**
 * 
 */
package org.opentravel.schemas.node;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAttributeType;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemas.modelObject.ValueWithAttributesAttributeFacetMO;
import org.opentravel.schemas.node.interfaces.ComplexComponentInterface;
import org.opentravel.schemas.node.interfaces.ExtensionOwner;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.interfaces.VersionedObjectInterface;
import org.opentravel.schemas.node.properties.PropertyOwnerInterface;
import org.opentravel.schemas.properties.Images;

/**
 * Value With Attributes. This object has two facets, the value is a simple facet and attributes facet. This makes it a
 * complex interface implementation.
 * 
 * Type Users under a VWA can only be assigned simple, vwa or enumeration (open or closed) objects.
 * 
 * @author Dave Hollander
 * 
 */
public class VWA_Node extends ComponentNode implements ComplexComponentInterface, ExtensionOwner,
		VersionedObjectInterface, LibraryMemberInterface {
	// private static final Logger LOGGER = LoggerFactory.getLogger(VWA_Node.class);

	public VWA_Node(LibraryMember mbr) {
		super(mbr);
		addMOChildren();
		if (((TLValueWithAttributes) getTLModelObject()).getParentType() == null)
			setSimpleType((Node) ModelNode.getEmptyNode());
	}

	public VWA_Node(BusinessObjectNode bo) {
		this(new TLValueWithAttributes());

		setName(bo.getName());
		bo.getLibrary().addMember(this);
		setDocumentation(bo.getDocumentation());

		((FacetNode) getAttributeFacet()).copyFacet((FacetNode) bo.getIDFacet());
		((FacetNode) getAttributeFacet()).copyFacet(bo.getSummaryFacet());
		((FacetNode) getAttributeFacet()).copyFacet((FacetNode) bo.getDetailFacet());
		setSimpleType((Node) ModelNode.getEmptyNode());
	}

	public VWA_Node(CoreObjectNode core) {
		this(new TLValueWithAttributes());

		setName(core.getName());
		core.getLibrary().addMember(this);
		setDocumentation(core.getDocumentation());

		((FacetNode) getAttributeFacet()).copyFacet((FacetNode) core.getSummaryFacet());
		((FacetNode) getAttributeFacet()).copyFacet((FacetNode) core.getDetailFacet());
		setSimpleType(core.getSimpleType());
	}

	// @Override
	// public Node getAssignedType() {
	// return getSimpleType();
	// }

	@Override
	public ComponentNode createMinorVersionComponent() {
		return super.createMinorVersionComponent(new VWA_Node(createMinorTLVersion(this)));
	}

	@Override
	public Node getExtendsType() {
		TLAttributeType tlParent = ((TLValueWithAttributes) getTLModelObject()).getParentType();
		Node parent = GetNode(((TLModelElement) tlParent).getListeners());
		return parent;
		// return getSimpleFacet().getTypeClass().getTypeNode();
	}

	@Override
	public QName getTLTypeQName() {
		QName typeQname = null;
		typeQname = getSimpleFacet().getSimpleAttribute().getTLTypeQName();
		return typeQname;
	}

	/**
	 * Return the parent type.
	 */
	@Override
	public NamedEntity getTLTypeObject() {
		return ((TLValueWithAttributes) getTLModelObject()).getParentType();
	}

	@Override
	public boolean isNamedType() {
		return true;
	}

	@Override
	public boolean isTypeProvider() {
		return true;
	}

	@Override
	public boolean isValueWithAttributes() {
		return true;
	}

	@Override
	public List<Node> getChildren_TypeUsers() {
		ArrayList<Node> users = new ArrayList<Node>();
		users.add(getSimpleFacet().getSimpleAttribute());
		// users.add(getSimpleType());
		users.addAll(getAttributeFacet().getChildren());
		return users;
	}

	@Override
	public String getLabel() {
		if (isVersioned())
			return super.getLabel() + " (Extends version:  " + getExtendsType().getLibrary().getVersion() + ")";
		else
			return super.getLabel();
	}

	@Override
	public Image getImage() {
		return Images.getImageRegistry().get(Images.ValueWithAttr);
	}

	@Override
	public SimpleFacetNode getSimpleFacet() {
		for (INode f : getChildren()) {
			if (f instanceof SimpleFacetNode)
				return (SimpleFacetNode) f;
		}
		return null;
	}

	@Override
	public ComponentNodeType getComponentNodeType() {
		return ComponentNodeType.VWA;
	}

	@Override
	public FacetNode getDefaultFacet() {
		return (FacetNode) getAttributeFacet();
	}

	@Override
	public INode.CommandType getAddCommand() {
		return INode.CommandType.PROPERTY;
	}

	@Override
	public PropertyOwnerInterface getAttributeFacet() {
		for (INode f : getChildren()) {
			if (f.getModelObject() instanceof ValueWithAttributesAttributeFacetMO)
				return (PropertyOwnerInterface) f;
		}
		return null;

	}

	@Override
	public ComponentNode getSimpleType() {
		// return (ComponentNode) getSimpleFacet().getSimpleAttribute().getAssignedType();
		return (ComponentNode) getAssignedType();
	}

	// 10/5/2015 - was commented out
	@Override
	public Node getAssignedType() {
		return getSimpleFacet().getAssignedType();
	}

	@Override
	public void sort() {
		((FacetNode) getAttributeFacet()).sort();
	}

	@Override
	public boolean setSimpleType(Node type) {
		// return getSimpleFacet().getSimpleAttribute().setAssignedType(type);
		return setAssignedType(type);
	}

	// Must use simple attribute because facet is not type user.
	// 10/5/2015 - was not defined here, used supertype
	@Override
	public boolean setAssignedType(Node replacement) {
		return getSimpleFacet().getSimpleAttribute().getTypeClass().setAssignedType(replacement);
	}

	@Override
	public void setExtendsType(final INode sourceNode) {
		// update TLModel
		super.setExtendsType(sourceNode);
		// make changes to node model
		setSimpleType((Node) sourceNode);
	}

	@Override
	public void merge(Node source) {
		if (!(source instanceof VWA_Node)) {
			throw new IllegalStateException("Can only merge objects with the same type");
		}
		VWA_Node vwa = (VWA_Node) source;
		getAttributeFacet().addProperties(vwa.getAttributeFacet().getChildren(), true);
	}

	@Override
	public boolean isAssignableToVWA() {
		return true;
	}

	@Override
	public boolean isMergeSupported() {
		return true;
	}

}
