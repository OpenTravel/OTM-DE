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
import org.opentravel.schemacompiler.model.TLExample;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLMemberField;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLParamGroup;
import org.opentravel.schemacompiler.model.TLParamLocation;
import org.opentravel.schemacompiler.model.TLParameter;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.facets.FacetNode;
import org.opentravel.schemas.node.facets.QueryFacetNode;
import org.opentravel.schemas.node.listeners.ResourceDependencyListener;
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
	private static final String DEFAULT_NAME = "NewParameterGroup";
	private String MSGKEY = "rest.ParamGroup";

	public class IdGroupListener implements ResourceFieldListener {
		@Override
		public boolean set(String value) {
			setIdGroup(Boolean.valueOf(value));
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
		setName(null); // set to default if null or empty

	}

	public ParamGroup(ResourceNode parent) {
		super(new TLParamGroup(), parent);
		setName(null);
		getParent().getTLModelObject().addParamGroup(tlObj);
	}

	public ParamGroup(ResourceNode rn, ComponentNode fn, boolean idGroup) {
		this(rn);
		setName(fn.getLabel());
		setIdGroup(idGroup);
		setReferenceFacet(fn.getLabel());
	}

	@Override
	public void addListeners() {
		// TODO - test dependency listener to Reference Facet
		// tlObj.getFacetRef().addListener(new ResourceDependencyListener(this));
	}

	/**
	 * Add dependency listener to this group and all of its parameters. All parameters because they may change their
	 * contribution. Also assure that the listeners do not already exist.
	 * 
	 */
	public void addListeners(Node dependent) {
		removeListeners(dependent);
		tlObj.addListener(new ResourceDependencyListener(dependent));
		for (Node param : getChildren())
			((ResourceParameter) param).tlObj.addListener(new ResourceDependencyListener(dependent));
	}

	/**
	 * Remove dependency listener(s) to this group and all of its parameters.
	 * 
	 */
	public void removeListeners(Node dependent) {
		removeListeners(tlObj, dependent);
		for (Node param : getChildren())
			removeListeners(param.getTLModelObject(), dependent);
	}

	/**
	 * Add a dependency listener for all parameters that contribute to the path
	 * 
	 * @param pathUser
	 */
	@Deprecated
	public void addPathListeners(Node pathUser) {
		for (Node param : getChildren())
			if (((ResourceParameter) param).isPathParam())
				((ResourceParameter) param).tlObj.addListener(new ResourceDependencyListener(pathUser));
	}

	@Override
	public void delete() {
		// super.delete();
		ArrayList<Node> params = new ArrayList<>(getChildren());
		for (Node param : params)
			param.delete();
		if (tlObj.getOwner() != null)
			tlObj.getOwner().removeParamGroup(tlObj);
		super.delete();
	}

	public void setIdGroup(boolean idGroup) {
		tlObj.setIdGroup(idGroup);
		LOGGER.debug("Set id group to: " + tlObj.isIdGroup());
	}

	@Override
	public ResourceNode getParent() {
		return (ResourceNode) parent;
	}

	@Override
	public TLParamGroup getTLModelObject() {
		return tlObj;
	}

	@Override
	public TLResource getTLOwner() {
		return tlObj.getOwner();
	}

	@Override
	public String getTooltip() {
		return Messages.getString(MSGKEY + ".tooltip");
	}

	@Override
	public void addChildren() {
		for (TLParameter tlParam : tlObj.getParameters())
			new ResourceParameter(tlParam);
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

	/**
	 * Get facet names from subject business object. Omit query facets if this is an ID Parameter Group.
	 * 
	 * @return a string array of subject facet names.
	 */
	// TODO - JUNIT - add test for ID/non-ID group behavior
	public String[] getSubjectFacets() {
		List<FacetNode> facets = new ArrayList<FacetNode>();
		for (Node facet : getOwningComponent().getSubject().getChildren()) {
			if (!(facet instanceof FacetNode))
				continue;
			if (isIdGroup() && facet instanceof QueryFacetNode)
				continue;
			facets.add((FacetNode) facet);
		}
		int size = facets.size();
		String[] fs = new String[size];
		int i = 0;
		for (Node facet : facets)
			fs[i++] = facet.getLabel();
		return fs;

	}

	@Override
	public List<ResourceField> getFields() {
		List<ResourceField> fields = new ArrayList<ResourceField>();

		// Facet Name - list of facets from subject
		new ResourceField(fields, getFacetLabel(), "rest.ParamGroup.fields.referenceFacetName", ResourceFieldType.Enum,
				new ReferenceFacetListener(), getSubjectFacets());

		// idGroup - boolean
		new ResourceField(fields, Boolean.toString(tlObj.isIdGroup()), "rest.ParamGroup.fields.idGroup",
				ResourceFieldType.CheckButton, new IdGroupListener());

		return fields;
	}

	/**
	 * Set the name of this parameter group
	 * 
	 * @param name
	 *            string of the name to be set or null to set to default value if empty
	 */
	@Override
	public void setName(final String name) {
		if (name != null)
			tlObj.setName(name);
		else if (tlObj.getName() == null || tlObj.getName().isEmpty())
			if (tlObj.getFacetRefName() == null || tlObj.getFacetRefName().isEmpty())
				setName(DEFAULT_NAME);
			else
				setName(tlObj.getFacetRefName());
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
	}

	/**
	 * @return an array of string template contributions based on parameters in this group
	 */
	public List<String> getPathTemplates() {
		ArrayList<String> contributions = new ArrayList<String>();
		boolean firstParam = true;
		for (Node param : getChildren()) {
			if (((ResourceParameter) param).isPathParam())
				if (firstParam)
					contributions.add("{" + param.getName() + "}");
				else
					contributions.add("/{" + param.getName() + "}");
		}
		return contributions;
	}

	/**
	 * @return single string with all the path parameters in it.
	 */
	public String getPathTemplate() {
		String path = "";
		for (String pt : getPathTemplates())
			path += "/" + pt;
		if (path.isEmpty())
			path = "/"; // must at least have a slash
		LOGGER.debug("Get path template: " + path);
		return path;
	}

	/**
	 * @return list of strings for the components of the query
	 */
	public List<String> getQueryTemplates() {
		ArrayList<String> contributions = new ArrayList<String>();
		boolean firstParam = true;
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

	public String getQueryTemplate() {
		String query = "";
		for (String s : getQueryTemplates())
			query += s;
		return query;
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
		List<Node> params = new ArrayList<Node>(getChildren());
		for (Node p : params)
			p.delete();
		// List<TLParameter> parameters = new ArrayList<TLParameter>(tlObj.getParameters());
		// for (TLParameter p : parameters)
		// tlObj.removeParameter(p);
		// getChildren().clear();
	}

	public void upDateParameters() {
		clearParameters();
		for (Node n : getPossibleFields()) {
			new ResourceParameter(this, n);
			LOGGER.debug("Added parameter for " + n.getName());
		}
	}

	public boolean isIdGroup() {
		return tlObj.isIdGroup();
	}

	/**
	 * Sets the reference facet to the facet corresponding to the named resource subject facet. Updates the parameters
	 * to create parameters for each candidate field.
	 * 
	 * @return true if there is a change to children
	 */
	public boolean setReferenceFacet(String name) {
		for (Node n : getOwningComponent().getSubject().getChildren())
			if (n.getLabel().equals(name)) {
				tlObj.setFacetRef((TLFacet) n.getTLModelObject());
				upDateParameters();
				if (tlObj.getName().isEmpty())
					tlObj.setName(name);
				LOGGER.debug("Set reference facet to: " + tlObj.getFacetRefName());
				return true; // denote change
			}
		LOGGER.debug("Could not find reference facet named: " + name);
		return false;
	}
}
