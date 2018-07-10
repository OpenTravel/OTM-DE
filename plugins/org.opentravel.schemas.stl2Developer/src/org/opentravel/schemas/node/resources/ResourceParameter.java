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
import org.opentravel.schemacompiler.model.TLMemberField;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLParamGroup;
import org.opentravel.schemacompiler.model.TLParamLocation;
import org.opentravel.schemacompiler.model.TLParameter;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.interfaces.ResourceMemberInterface;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.properties.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resource Parameters controller. Provides getters, setters and listeners for editable fields.
 * 
 * @author Dave
 *
 */
public class ResourceParameter extends ResourceBase<TLParameter> implements ResourceMemberInterface {
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
			setLocation(name);
			return false;
		}
	}

	/*****************************************************************
	 * 
	 * Create new resource parameter based on the field to be referenced
	 */
	public ResourceParameter(TLParameter tlParameter) {
		super(tlParameter);

		// Log an error
		if (tlParameter.getFieldRef() == null) {
			String msg = "Null FieldRef " + tlParameter.getFieldRefName() + " ";
			if (tlParameter.getOwner() != null)
				msg += " in group " + tlParameter.getOwner().getName();
			LOGGER.warn(msg);
		}
	}

	public ResourceParameter(ParamGroup parent, Node field) {
		super(new TLParameter(), parent);

		parent.getTLModelObject().addParameter(tlObj);

		if (field != null && field.getTLModelObject() instanceof TLMemberField<?>)
			tlObj.setFieldRef((TLMemberField<?>) field.getTLModelObject());

		guessLocation(parent.isIdGroup());
	}

	@Override
	public void addListeners() {
		// TODO - set listener on parameter field
	}

	@Override
	public void delete() {
		// LOGGER.debug("Deleting parameter: " + this);
		parent.getChildren().remove(this); // must be done first to force the correct path template
		if (tlObj.getOwner() != null)
			tlObj.getOwner().removeParameter(tlObj);
		getParent().notifyActionRequests(); // delegate updating actions to the param group
		super.delete();
	}

	@Override
	public String getComponentType() {
		return "Parameter";
	}

	@Override
	public String getDecoration() {
		String decoration = "";
		Node n = null;
		if (tlObj.getFieldRef() instanceof TLModelElement)
			n = ModelNode.GetNode(((TLModelElement) tlObj.getFieldRef()).getListeners());
		if (n != null)
			decoration += " of type " + n.getAssignedTypeName();
		return decoration;
	}

	@Override
	public List<ResourceField> getFields() {
		List<ResourceField> fields = new ArrayList<>();

		new ResourceField(fields, getLocation(), "rest.ResourceParameter.fields.location",
				ResourceField.ResourceFieldType.Enum, new LocationListener(), getParamLocations());
		return fields;
	}

	@Override
	public Image getImage() {
		return Images.getImageRegistry().get(Images.ResourceParameter);
	}

	public String getLocation() {
		return tlObj.getLocation() == null ? "" : tlObj.getLocation().toString();
	}

	@Override
	public String getName() {
		return tlObj.getFieldRefName() != null ? tlObj.getFieldRefName() : "";
	}

	@Override
	public ParamGroup getParent() {
		return (ParamGroup) parent;
	}

	@Override
	public TLParameter getTLModelObject() {
		return tlObj;
	}

	@Override
	public TLParamGroup getTLOwner() {
		return tlObj.getOwner();
	}

	@Override
	public String getTooltip() {
		return Messages.getString(MSGKEY + ".tooltip");
	}

	/**
	 * Set location based on a guess based on the type of field and if this is an ID group.
	 * 
	 * @param isIdGroup
	 */
	public void guessLocation(boolean isIdGroup) {
		if (isIdGroup)
			tlObj.setLocation(TLParamLocation.PATH);
		// else if (tlObj.getFieldRef() instanceof TLProperty)
		// tlObj.setLocation(TLParamLocation.QUERY);
		else
			tlObj.setLocation(TLParamLocation.QUERY);
		// tlObj.setLocation(TLParamLocation.HEADER);
	}

	public boolean isPathParam() {
		return getLocation().equals(TLParamLocation.PATH.toString());
	}

	@Override
	public boolean isDeleteable() {
		return true;
	}

	@Override
	public boolean isNameEditable() {
		return false;
	}

	public void setLocation(String location) {
		tlObj.setLocation(TLParamLocation.valueOf(location));
		// LOGGER.debug("Set location to: " + tlObj.getLocation());
		// Update path templates that use this parameter
		// Parent has listeners for action requests that use the PG
		getParent().notifyActionRequests();

	}

	public void setFieldRef(String name) {
		tlObj.setFieldRefName(name);
		// LOGGER.debug("Set Field Reference to " + name + " : " + tlObj.getFieldRefName());
	}

	@Override
	public void setName(final String name) {
		tlObj.setFieldRefName(name);
		// LOGGER.debug("Set Field Reference to " + name + " : " + tlObj.getFieldRefName());
	}

}
