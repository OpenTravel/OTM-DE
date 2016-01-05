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
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLParamGroup;
import org.opentravel.schemacompiler.model.TLParamLocation;
import org.opentravel.schemacompiler.model.TLParameter;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.listeners.ListenerFactory;
import org.opentravel.schemas.node.resources.ResourceField.ResourceFieldType;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.properties.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParamGroup extends BaseResourceNode {
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

	private TLParamGroup tlObj = null;

	public ParamGroup(TLParamGroup tlParamgroup) {
		this.tlObj = tlParamgroup;
		ListenerFactory.setListner(this);
		parent = this.getNodeFromListeners(((LibraryMember) tlObj.getOwner()).getListeners());
		addChildren();
	}

	@Override
	public String getTooltip() {
		return Messages.getString(MSGKEY + ".tooltip");
	}

	@Override
	public TLModelElement getTLModelObject() {
		return tlObj;
	}

	public void addChildren() {
		for (TLParameter tlParam : tlObj.getParameters())
			getChildren().add(new ResourceParameter(tlParam));
	}

	@Override
	public String getDescription() {
		return tlObj.getDocumentation() != null ? tlObj.getDocumentation().getDescription() : "";
	}

	@Override
	public Image getImage() {
		return Images.getImageRegistry().get(Images.ParamGroup);

	}

	@Override
	public String getLabel() {
		return getName();
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
	public Node getOwningComponent() {
		return parent;
	}

	@Override
	public boolean hasNavChildren() {
		return !getChildren().isEmpty();
	}

	@Override
	public String getComponentType() {
		return "Parameter Group";
	}

	protected Node getFacetRef() {
		return this.getNodeFromListeners(tlObj.getFacetRef().getListeners());
	}

	@Override
	public List<ResourceField> getFields() {
		List<ResourceField> fields = new ArrayList<ResourceField>();

		// Facet Name - list of facets from subject
		String facetName = getFacetRef().getLabel();
		ResourceField field = new ResourceField(fields, facetName, "rest.ParamGroup.fields.referenceFacetName",
				ResourceFieldType.Enum, new ReferenceFacetListener());
		field.setData(((ResourceNode) getOwningComponent()).getSubjectFacets());

		// idGroup - boolean
		field = new ResourceField(fields, Boolean.toString(tlObj.isIdGroup()), "rest.ParamGroup.fields.idGroup",
				ResourceFieldType.CheckButton, new IdGroupListener());
		return fields;
	}

	@Override
	public void setDescription(final String description) {
		TLDocumentation doc = tlObj.getDocumentation();
		if (doc == null) {
			doc = new TLDocumentation();
			tlObj.setDocumentation(doc);
		}
		doc.setDescription(description);
	}

	@Override
	public void setName(final String name) {
		tlObj.setName(name);
	}

	public List<Node> getPossibleFields() {
		return getFacetRef().getChildren_TypeUsers();
	}

	/**
	 * @return an array of string template contributions based on parameters in this group
	 */
	public List<String> getPathTemplates() {
		ArrayList<String> contributions = new ArrayList<String>();
		int i = 0;
		for (Node param : getChildren()) {
			if (((ResourceParameter) param).getLocation().equals(TLParamLocation.PATH.toString()))
				contributions.add("{" + param.getName() + "}");
			if (((ResourceParameter) param).getLocation().equals(TLParamLocation.QUERY.toString()))
				contributions.add("?" + param.getName() + "?");
		}
		return contributions;
	}

	public void upDateParameters() {
		getChildren().clear();
		for (Node n : getPossibleFields()) {
			TLParameter tlp = new TLParameter();
			tlp.setFieldRefName(n.getName());
			tlp.setOwner((TLParamGroup) getTLModelObject());
			getChildren().add(new ResourceParameter(tlp));
			LOGGER.debug("Added parameter " + n.getName() + " :" + tlp.getFieldRefName());
		}
	}

	/**
	 * @return true if there is a change to children
	 */
	public boolean setReferenceFacet(String name) {
		for (Node n : ((ResourceNode) getOwningComponent()).getSubject().getChildren())
			if (n.getLabel().equals(name)) {
				tlObj.setFacetRef((TLFacet) n.getTLModelObject());
				LOGGER.debug("Set reference facet to: " + tlObj.getFacetRefName());

				// If the facet changes then the kids must change to match possible fields.
				upDateParameters();
				return true; // denote change
			}
		return false;
	}
}
