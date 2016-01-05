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
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLReferenceType;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.listeners.ListenerFactory;
import org.opentravel.schemas.node.resources.ResourceField.ResourceFieldType;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.properties.Messages;
import org.opentravel.schemas.views.RestResourceView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActionFacet extends BaseResourceNode {
	private static final Logger LOGGER = LoggerFactory.getLogger(RestResourceView.class);
	private String MSGKEY = "rest.ActionFacet";

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

	private TLActionFacet tlObj = null;

	public ActionFacet(TLActionFacet tlAction) {
		super();
		this.tlObj = tlAction;
		ListenerFactory.setListner(this);
		parent = this.getNodeFromListeners(((LibraryMember) tlObj.getOwningEntity()).getListeners());
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
	}

	@Override
	public Image getImage() {
		return Images.getImageRegistry().get(Images.ActionFacet);

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
	public String getDescription() {
		return tlObj.getDocumentation() != null ? tlObj.getDocumentation().getDescription() : "";
	}

	@Override
	public String getComponentType() {
		return "Action Facet";
	}

	@Override
	public List<ResourceField> getFields() {
		List<ResourceField> fields = new ArrayList<ResourceField>();

		// Facet Reference = This can only be set to a facet in the resource subject business object
		ResourceField field = new ResourceField(fields, tlObj.getReferenceFacetName(),
				"rest.ActionFacet.fields.referenceFacetName", ResourceFieldType.Enum, new ReferenceNameListner());
		field.setData(((ResourceNode) getOwningComponent()).getSubjectFacets());

		field = new ResourceField();
		fields.add(field);
		field.setValue(Integer.toString(tlObj.getReferenceRepeat()));
		field.setKey("rest.ActionFacet.fields.referenceRepeat");
		field.type = ResourceFieldType.Int;
		field.listener = new ReferenceRepeatListener();

		field = new ResourceField();
		fields.add(field);
		field.setValue(tlObj.getReferenceType().toString());
		field.setKey("rest.ActionFacet.fields.referenceType");
		field.type = ResourceField.ResourceFieldType.Enum;
		int i = 0;
		String[] values = new String[TLReferenceType.values().length];
		for (TLReferenceType l : TLReferenceType.values())
			values[i++] = l.toString();
		field.setData(values);
		field.setListener(new ReferenceTypeListener());

		return fields;
	}

	public void setReferenceFacetName(String name) {
		tlObj.setReferenceFacetName(name);
		LOGGER.debug("Set Reference name to " + name);
	}

	public void setReferenceRepeat(Integer i) {
		tlObj.setReferenceRepeat(i);
		LOGGER.debug("Set Reference repeat to " + i);
	}

	public void setReferenceType(String value) {
		TLReferenceType eVal = TLReferenceType.valueOf(value);
		tlObj.setReferenceType(eVal);
		LOGGER.debug("Set Reference Type to " + eVal);
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

}
