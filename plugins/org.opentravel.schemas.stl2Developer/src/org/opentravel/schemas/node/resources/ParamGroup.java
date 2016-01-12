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
package org.opentravel.schemas.node.resources;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemacompiler.codegen.util.ResourceCodegenUtils;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLExample;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLMemberField;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLParamGroup;
import org.opentravel.schemacompiler.model.TLParamLocation;
import org.opentravel.schemacompiler.model.TLParameter;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.resources.ResourceField.ResourceFieldType;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.properties.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parameter Group controller. Provides getters, setters and listeners for parameter group fields.
 * 
 * @author Dave
 *
 */
public class ParamGroup extends ResourceBase<TLParamGroup> {
	private static final Logger LOGGER = LoggerFactory.getLogger(ParamGroup.class);
	private String MSGKEY = "rest.ParamGroup";

	public class IdGroupListener implements ResourceFieldListener {
		@Override
		public boolean set(String value) {
			tlObj.setIdGroup(Boolean.valueOf(value));
			LOGGER.debug("Set id group to: " + tlObj.isIdGroup());
			return false;
		}
	}

	public class ReferenceFacetListener implements ResourceFieldListener {
		@Override
		public boolean set(String value) {
			return setReferenceFacet(value);
		}
	}

	public class ResourceParameterListener implements ResourceFieldListener {
		@Override
		public boolean set(String value) {
			return true;
		}
	}

	/********************************************************************************
	 * 
	 */
	public ParamGroup(TLParamGroup tlParamgroup) {
		super(tlParamgroup);
		parent = this.getNode(((LibraryMember) tlObj.getOwner()).getListeners());
		assert parent instanceof ResourceNode;
		getParent().addChild(this);
		// addChildren();
	}

	public ParamGroup(ResourceNode parent) {
		super(new TLParamGroup());
		getParent().getTLModelObject().addParamGroup(tlObj);
	}

	@Override
	public ResourceNode getParent() {
		return (ResourceNode) parent;
	}

	@Override
	public String getTooltip() {
		return Messages.getString(MSGKEY + ".tooltip");
	}

	@Override
	public void addChildren() {
		for (TLParameter tlParam : tlObj.getParameters())
			getChildren().add(new ResourceParameter(tlParam));
	}

	@Override
	public Image getImage() {
		return Images.getImageRegistry().get(Images.ParamGroup);

	}

	@Override
	public String getName() {
		return tlObj.getName() != null ? tlObj.getName() : "";
	}

	@Override
	public boolean isNameEditable() {
		return true;
	}

	@Override
	public String getComponentType() {
		return "Parameter Group";
	}

	protected Node getFacetRef() {
		return tlObj.getFacetRef() != null ? this.getNode(tlObj.getFacetRef().getListeners()) : null;
	}

	protected String getFacetLabel() {
		return getFacetRef() != null ? getFacetRef().getLabel() : "";
	}

	@Override
	public List<ResourceField> getFields() {
		List<ResourceField> fields = new ArrayList<ResourceField>();

		// Facet Name - list of facets from subject
		new ResourceField(fields, getFacetLabel(), "rest.ParamGroup.fields.referenceFacetName", ResourceFieldType.Enum,
				new ReferenceFacetListener(), getOwningComponent().getSubjectFacets());

		// idGroup - boolean
		new ResourceField(fields, Boolean.toString(tlObj.isIdGroup()), "rest.ParamGroup.fields.idGroup",
				ResourceFieldType.CheckButton, new IdGroupListener());

		return fields;
	}

	@Override
	public void setName(final String name) {
		tlObj.setName(name);
	}

	public List<Node> getPossibleFields() {
		// use resource codegen utils to get a complete list
		List<Node> fields = new ArrayList<Node>();
		for (TLMemberField<?> field : ResourceCodegenUtils.getEligibleParameterFields((TLFacet) getFacetRef()
				.getTLModelObject())) {
			Node f = this.getNode(((TLModelElement) field).getListeners());
			if (f != null)
				fields.add(f);
			else
				LOGGER.debug("Error: null node from field: " + field.getName());
		}
		return fields;
		// return getFacetRef().getChildren_TypeUsers();
	}

	/**
	 * @return an array of string template contributions based on parameters in this group
	 */
	public List<String> getPathTemplates() {
		ArrayList<String> contributions = new ArrayList<String>();
		int i = 0;
		boolean firstParam = true;
		for (Node param : getChildren()) {
			if (((ResourceParameter) param).getLocation().equals(TLParamLocation.PATH.toString()))
				if (firstParam)
					contributions.add("{" + param.getName() + "}");
				else
					contributions.add("/{" + param.getName() + "}");
		}
		firstParam = true;
		for (Node param : getChildren()) {
			if (((ResourceParameter) param).getLocation().equals(TLParamLocation.QUERY.toString()))
				if (firstParam) {
					contributions.add("?" + getQueryParam(param));
					firstParam = false;
				} else
					contributions.add("&" + getQueryParam(param));
		}
		return contributions;
	}

	private String getQueryParam(Node param) {
		String ex = "";
		PropertyNode n = null;
		TLMemberField<?> field = ((TLParameter) param.getTLModelObject()).getFieldRef();
		if (field instanceof TLModelElement)
			n = (PropertyNode) getNode(((TLModelElement) field).getListeners());

		List<TLExample> examples = ((TLParameter) param.getTLModelObject()).getExamples();
		if (examples != null && !examples.isEmpty())
			ex = examples.get(0).getValue();
		else if (n != null)
			ex = n.getExample(null); // Try to get example from the actual field being referenced.
		if (ex.isEmpty())
			ex = "xxx";
		return param.getName() + "=" + ex;
	}

	public void clearParameters() {
		List<TLParameter> parameters = new ArrayList<TLParameter>(tlObj.getParameters());
		for (TLParameter p : parameters)
			tlObj.removeParameter(p);
		getChildren().clear();
	}

	public void upDateParameters() {
		clearParameters();
		for (Node n : getPossibleFields()) {
			getChildren().add(new ResourceParameter(this, n));
			LOGGER.debug("Added parameter for " + n.getName());
		}
	}

	public boolean isIdGroup() {
		return tlObj.isIdGroup();
	}

	/**
	 * @return true if there is a change to children
	 */
	public boolean setReferenceFacet(String name) {
		for (Node n : getOwningComponent().getSubject().getChildren())
			if (n.getLabel().equals(name)) {
				tlObj.setFacetRef((TLFacet) n.getTLModelObject());
				upDateParameters();
				LOGGER.debug("Set reference facet to: " + tlObj.getFacetRefName());
				return true; // denote change
			}
		return false;
	}
}
