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
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLReferenceType;
import org.opentravel.schemas.node.resources.ResourceField.ResourceFieldType;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.properties.Messages;
import org.opentravel.schemas.views.RestResourceView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action Facet Controller. Provides getters, setters and listeners for editable fields.
 * 
 * @author Dave
 *
 */
public class ActionFacet extends ResourceBase<TLActionFacet> {
	private static final Logger LOGGER = LoggerFactory.getLogger(RestResourceView.class);
	protected String MSGKEY = "rest.ActionFacet";

	class ReferenceNameListner implements ResourceFieldListener {
		@Override
		public boolean set(String value) {
			setReferenceFacetName(value);
			return false;
		}
	}

	class ReferenceRepeatListener implements ResourceFieldListener {
		@Override
		public boolean set(String value) {
			setReferenceRepeat(Integer.parseInt(value));
			return false;
		}
	}

	class ReferenceTypeListener implements ResourceFieldListener {
		@Override
		public boolean set(String value) {
			setReferenceType(value);
			return false;
		}
	}

	/***************************************************************************
	 * 
	 */
	public ActionFacet(TLActionFacet tlActionFacet) {
		super(tlActionFacet);
		parent = this.getNode(((LibraryMember) tlObj.getOwningEntity()).getListeners());
		assert parent instanceof ResourceNode;
		if (tlObj.getFacetType() != TLFacetType.ACTION)
			LOGGER.debug("Wrong Facet Type: " + tlObj.getFacetType());
		getParent().addChild(this);
	}

	public ActionFacet(ResourceNode parent) {
		super(new TLActionFacet());
		getParent().getTLModelObject().addActionFacet(tlObj);
	}

	@Override
	public ResourceNode getParent() {
		return (ResourceNode) parent;
	}

	public void addChildren() {
	}

	@Override
	public String getComponentType() {
		return "Action Facet";
	}

	@Override
	public List<ResourceField> getFields() {
		List<ResourceField> fields = new ArrayList<ResourceField>();

		// Facet Reference = This can only be set to a facet in the resource subject business object
		new ResourceField(fields, tlObj.getReferenceFacetName(), "rest.ActionFacet.fields.referenceFacetName",
				ResourceFieldType.Enum, new ReferenceNameListner(), getOwningComponent().getSubjectFacets());

		// Repeat Count - an int
		new ResourceField(fields, Integer.toString(tlObj.getReferenceRepeat()),
				"rest.ActionFacet.fields.referenceRepeat", ResourceFieldType.Int, new ReferenceRepeatListener());

		// Reference Type - enum list
		new ResourceField(fields, getReferenceType(), "rest.ActionFacet.fields.referenceType",
				ResourceField.ResourceFieldType.Enum, new ReferenceTypeListener(), getReferenceTypeStrings());

		return fields;
	}

	@Override
	public Image getImage() {
		return Images.getImageRegistry().get(Images.ActionFacet);

	}

	@Override
	public String getName() {
		return tlObj.getName() != null ? tlObj.getName() : "";
	}

	public String getReferenceType() {
		return tlObj.getReferenceType() != null ? tlObj.getReferenceType().toString() : "";
	}

	@Override
	public String getTooltip() {
		return Messages.getString(MSGKEY + ".tooltip");
	}

	@Override
	public boolean isNameEditable() {
		return true;
	}

	@Override
	public void setName(final String name) {
		tlObj.setName(name);
	}

	/**
	 * @param name
	 *            of the business object facet
	 */
	public void setReferenceFacetName(String name) {
		tlObj.setReferenceFacetName(name);
		LOGGER.debug("Set Reference facet name to " + name + " : " + tlObj.getReferenceFacetName());
	}

	public void setReferenceRepeat(Integer i) {
		tlObj.setReferenceRepeat(i);
		LOGGER.debug("Set Reference repeat to " + i);
	}

	public void setReferenceType(String value) {
		tlObj.setReferenceType(TLReferenceType.valueOf(value));
		LOGGER.debug("Set Reference Type to " + value + " : " + tlObj.getReferenceType());
	}

}
