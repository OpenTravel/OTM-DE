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
import org.opentravel.schemacompiler.event.OwnershipEvent;
import org.opentravel.schemacompiler.event.ValueChangeEvent;
import org.opentravel.schemacompiler.model.TLAction;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLActionRequest;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLHttpMethod;
import org.opentravel.schemacompiler.model.TLMimeType;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLParamGroup;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.interfaces.ResourceMemberInterface;
import org.opentravel.schemas.node.listeners.BaseNodeListener;
import org.opentravel.schemas.node.listeners.INodeListener;
import org.opentravel.schemas.node.resources.ResourceField.ResourceFieldType;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.properties.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActionRequest extends Node implements ResourceMemberInterface {
	private static final Logger LOGGER = LoggerFactory.getLogger(ActionRequest.class);
	private String MSGKEY = "rest.ActionRequest";

	private TLActionRequest tlObj = null;
	private Node payload = null;

	class PayloadTypeListener implements ResourceFieldListener {
		@Override
		public boolean set(String value) {
			setPayloadType(value);
			return false;
		}
	}

	class HttpMethodListener implements ResourceFieldListener {
		@Override
		public boolean set(String value) {
			setHttpMethod(value);
			return false;
		}
	}

	class PathTemplateListener implements ResourceFieldListener {
		@Override
		public boolean set(String value) {
			setPathTemplate(value);
			return false;
		}
	}

	class MimeTypeListener implements ResourceFieldListener {
		@Override
		public boolean set(String value) {
			setMimeType(value);
			return false;
		}
	}

	class ParamGroupListener implements ResourceFieldListener {
		@Override
		public boolean set(String value) {
			return setParamGroup(value);
		}
	}

	/**
	 * Listen for changes to the action request. If the change is to the parameters, the path has to change.
	 * 
	 */
	class ActionRequestListener extends BaseNodeListener implements INodeListener {

		public ActionRequestListener(Node node) {
			super(node);
		}

		@Override
		public void processOwnershipEvent(OwnershipEvent<?, ?> event) {
			super.processOwnershipEvent(event);
			LOGGER.debug("Ownership event: " + event);
		}

		@Override
		public void processValueChangeEvent(ValueChangeEvent<?, ?> event) {
			super.processValueChangeEvent(event);
			LOGGER.debug("Value change event: " + event);
			switch (event.getType()) {
			case PARAM_GROUP_ADDED:
			case PARAM_GROUP_MODIFIED:
				if (event.getNewValue() instanceof TLParamGroup)
					createPathTemplate((TLParamGroup) event.getNewValue());
				break;
			case PARAM_GROUP_REMOVED:
				tlObj.setPathTemplate("");
				break;
			case PARAMETER_ADDED:
			case PARAMETER_REMOVED:
			case PARENT_PARAM_GROUP_MODIFIED:
				LOGGER.debug("TODO - individual parameter changed.");
			default:
				LOGGER.debug("TODO - unhandled event.");
				break;
			}
		}

	}

	/***************************************************************
	 * 
	 * @param tlAction
	 */
	public ActionRequest(TLActionRequest tlAction) {
		this.tlObj = tlAction;
		// ListenerFactory.setListner(this);
		tlObj.addListener(new ActionRequestListener(this));
		parent = this.getNodeFromListeners(((TLAction) tlObj.getOwner()).getListeners());
	}

	public Node getPayload() {
		if (tlObj.getPayloadType() != null)
			payload = this.getNodeFromListeners(tlObj.getPayloadType().getListeners());
		return payload;
	}

	public void addChildren() {
	}

	@Override
	public TLModelElement getTLModelObject() {
		return tlObj;
	}

	@Override
	public String getTooltip() {
		return Messages.getString(MSGKEY + ".tooltip");
	}

	@Override
	public Image getImage() {
		return Images.getImageRegistry().get(Images.ActionRequest);

	}

	@Override
	public String getLabel() {
		return getName();
	}

	@Override
	public String getName() {
		return tlObj.getLocalName() != null ? tlObj.getLocalName() : "";
	}

	@Override
	public boolean isNameEditable() {
		return false;
	}

	@Override
	public Node getOwningComponent() {
		return parent.getParent();
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
		return "Action Request";
	}

	@Override
	public List<ResourceField> getFields() {
		List<ResourceField> fields = new ArrayList<ResourceField>();

		// Pass list of Action Facet names to select from
		ResourceField field = new ResourceField();
		fields.add(field);
		if (tlObj.getPayloadType() != null)
			field.setValue(tlObj.getPayloadType().getName());
		field.setKey("rest.ActionRequest.fields.payload");
		field.setType(ResourceFieldType.Enum);
		field.setListener(new PayloadTypeListener());
		field.setData(((ResourceNode) getOwningComponent()).getActionFacetsNames());

		// Mime Types = multi-select List of possible types
		field = new ResourceField();
		fields.add(field);
		field.setValue(tlObj.getMimeTypes().toString());
		field.setKey("rest.ActionRequest.fields.mimeTypes");
		// pass data to set up a field of buttons for mime types
		field.type = ResourceField.ResourceFieldType.EnumList;
		int i = 0;
		String[] values = new String[TLMimeType.values().length];
		for (TLMimeType l : TLMimeType.values())
			values[i++] = l.toString();
		field.setData(values);
		field.setListener(new MimeTypeListener());

		field = new ResourceField();
		fields.add(field);
		field.setValue(tlObj.getParamGroupName());
		field.setKey("rest.ActionRequest.fields.paramGroup");
		// create a list of the parameter groups to be selected from
		List<ParamGroup> paramGroups = ((ResourceNode) getOwningComponent()).getParameterGroups();
		String[] groupNames = new String[paramGroups.size()];
		i = 0;
		for (Node n : paramGroups)
			groupNames[i++] = n.getName();
		field.setData(groupNames);
		field.setType(ResourceFieldType.Enum);
		field.setListener(new ParamGroupListener());

		field = new ResourceField();
		fields.add(field);
		field.setValue(tlObj.getPathTemplate());
		field.setKey("rest.ActionRequest.fields.path");
		field.setListener(new PathTemplateListener());

		field = new ResourceField();
		fields.add(field);
		field.setValue(tlObj.getHttpMethod().toString());
		field.setKey("rest.ActionRequest.fields.httpMethod");
		field.type = ResourceField.ResourceFieldType.Enum;
		i = 0;
		values = new String[TLHttpMethod.values().length];
		for (TLHttpMethod l : TLHttpMethod.values())
			values[i++] = l.toString();
		field.setData(values);
		field.setListener(new HttpMethodListener());

		return fields;
	}

	public boolean setPayloadType(String payloadName) {
		if (tlObj.getPayloadTypeName().equals(payloadName)) {
			LOGGER.debug("No change because names are the same. " + payloadName);
			return false;
		}

		if (payloadName.equals(ResourceField.NONE)) {
			tlObj.setPayloadType(null);
			LOGGER.debug("Reset payload. " + payloadName);
			return true;
		}

		for (ActionFacet f : ((ResourceNode) getOwningComponent()).getActionFacets())
			if (f.getName().equals(payloadName)) {
				tlObj.setPayloadType((TLActionFacet) f.getTLModelObject());
				LOGGER.debug("Set payload to " + payloadName);
				return true;
			}
		LOGGER.debug("No Action - Set payload name not found. " + payloadName);
		return false;
	}

	public void setPathTemplate(String path) {
		tlObj.setPathTemplate(path);
		LOGGER.debug("Set Path template to: " + tlObj.getPathTemplate());
	}

	/**
	 * Set the path to have all the parameters from the parameter group that are PATH, null or empty.
	 * 
	 * @param tlParamGroup
	 */
	public void createPathTemplate(TLParamGroup tlParamGroup) {
		ParamGroup pg = (ParamGroup) this.getNodeFromListeners(tlParamGroup.getListeners());
		String path = "/TODO-Base/";
		for (String pt : pg.getPathTemplates())
			path += pt;
		tlObj.setPathTemplate(path);
		LOGGER.debug("Created path template: " + tlObj.getPathTemplate());
	}

	public void setHttpMethod(String method) {
		tlObj.setHttpMethod(TLHttpMethod.valueOf(method));
		LOGGER.debug("Set HTTP method to " + method);
	}

	public boolean setParamGroup(String groupName) {
		if (tlObj.getParamGroupName() != null && tlObj.getParamGroupName().equals(groupName)) {
			LOGGER.debug("No change because names are the same. " + groupName);
			return false;
		}

		// find the param group with this name then set it
		for (Node node : ((ResourceNode) getOwningComponent()).getParameterGroups())
			if (node instanceof ParamGroup && node.getName().equals(groupName))
				tlObj.setParamGroup((TLParamGroup) node.getTLModelObject());

		// Listener will notify path it needs to change
		LOGGER.debug("Set parameter group to " + groupName);
		return true;
	}

	/**
	 * Implements a toggle action. If the mime type is defined, it is removed. If it not defined it is added.
	 * 
	 * @param value
	 */
	public void setMimeType(String value) {
		TLMimeType type = TLMimeType.valueOf(value);
		// FIXME when issue https://github.com/OpenTravel/OTM-DE-Compiler/issues/32 resolved
		// MimeType x = MimeType.valueOf(value);
		// if (tlObj.getMimeTypes().contains(type))
		// tlObj.removeMimeType(x);
		// else
		tlObj.addMimeType(type);
		LOGGER.debug("Set Mime types to: " + tlObj.getMimeTypes());
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
		// TODO - what to do about objects with fixed names?
	}

}
