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
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLParamGroup;
import org.opentravel.schemacompiler.model.TLParamLocation;
import org.opentravel.schemacompiler.model.TLParameter;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.interfaces.ResourceMemberInterface;
import org.opentravel.schemas.node.listeners.ListenerFactory;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.properties.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceParameter extends BaseResourceNode implements ResourceMemberInterface {
	private static final Logger LOGGER = LoggerFactory.getLogger(ResourceParameter.class);
	private String MSGKEY = "rest.ResourceParameter";

	public class FieldRefListener implements ResourceFieldListener {
		@Override
		public boolean set(String name) {
			setFieldRef(name);
			return false;
		}
	}

	public class LocationListener implements ResourceFieldListener {
		@Override
		public boolean set(String name) {
			tlObj.setLocation(TLParamLocation.valueOf(name));
			LOGGER.debug("Set location to: " + tlObj.getLocation());
			return false;
		}
	}

	private TLParameter tlObj = null;

	public ResourceParameter(TLParameter tlParameter) {
		this.tlObj = tlParameter;
		ListenerFactory.setListner(this);
		parent = this.getNodeFromListeners(((TLParamGroup) tlObj.getOwner()).getListeners());
	}

	public void addChildren() {
	}

	@Override
	public String getTooltip() {
		return Messages.getString(MSGKEY + ".tooltip");
	}

	@Override
	public TLModelElement getTLModelObject() {
		return tlObj;
	}

	@Override
	public Image getImage() {
		return Images.getImageRegistry().get(Images.ResourceParameter);

	}

	@Override
	public String getLabel() {
		return getName();
	}

	@Override
	public String getName() {
		return tlObj.getFieldRefName() != null ? tlObj.getFieldRefName() : "";
	}

	@Override
	public boolean isNameEditable() {
		return false;
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
		return "Parameter";
	}

	public String getLocation() {
		return tlObj.getLocation() == null ? "" : tlObj.getLocation().toString();
	}

	@Override
	public List<ResourceField> getFields() {
		List<ResourceField> fields = new ArrayList<ResourceField>();

		// // Field Reference = list of fields in the parent facet
		// done by constructors when the parent's facet changes
		// String[] data = new String[getPossibleFields().size()];
		// int i = 0;
		// for (Node n : getPossibleFields())
		// data[i++] = n.getName();
		// ResourceField field = new ResourceField(fields, tlObj.getFieldRefName(),
		// "rest.ResourceParameter.fields.fieldRef", ResourceFieldType.Enum, new FieldRefListener());
		// field.setData(data);

		// Location = enum list of possible locations
		int i = 0;
		String[] values = new String[TLParamLocation.values().length];
		for (TLParamLocation l : TLParamLocation.values())
			values[i++] = l.toString();
		ResourceField field = new ResourceField(fields, getLocation(), "rest.ResourceParameter.fields.location",
				ResourceField.ResourceFieldType.Enum, new LocationListener());
		field.setData(values);
		return fields;
	}

	public void setFieldRef(String name) {
		tlObj.setFieldRefName(name);
		LOGGER.debug("Set Field Reference to: " + tlObj.getFieldRefName());
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
		tlObj.setFieldRefName(name);
	}

}
