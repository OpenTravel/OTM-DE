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
import org.opentravel.schemacompiler.model.TLAction;
import org.opentravel.schemacompiler.model.TLActionRequest;
import org.opentravel.schemacompiler.model.TLHttpMethod;
import org.opentravel.schemacompiler.model.TLMimeType;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.interfaces.ResourceMemberInterface;
import org.opentravel.schemas.node.listeners.ResourceDependencyListener;
import org.opentravel.schemas.node.resources.ResourceField.ResourceFieldType;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.properties.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements ActionRequest controller. Includes getters, setters and listeners for editable fields.
 * 
 * @author Dave
 *
 */
public class ActionRequest extends ResourceBase<TLActionRequest> implements ResourceMemberInterface {
	private static final Logger LOGGER = LoggerFactory.getLogger(ActionRequest.class);
	private String MSGKEY = "rest.ActionRequest";
	private ActionRequestPathTemplate pathTemplate;

	class HttpMethodListener implements ResourceFieldListener {
		@Override
		public boolean set(String value) {
			setHttpMethod(value);
			return false;
		}
	}

	class MimeTypeListener implements ResourceFieldListener {
		@Override
		public boolean set(String value) {
			toggleMimeType(value);
			return false;
		}
	}

	class ParamGroupListener implements ResourceFieldListener {
		@Override
		public boolean set(String value) {
			return setParamGroup(value);
		}
	}

	class PathTemplateListener implements ResourceFieldListener {
		@Override
		public boolean set(String value) {
			setPathTemplate(value);
			return false;
		}
	}

	class PayloadTypeListener implements ResourceFieldListener {
		@Override
		public boolean set(String value) {
			setPayloadType(value);
			return false;
		}
	}

	/*******************************************************************************
	 * 
	 */
	public ActionRequest(TLActionRequest tlActionRequest) {
		super(tlActionRequest);
		pathTemplate = new ActionRequestPathTemplate(this);
	}

	public ActionRequest(ActionNode parent) {
		super(new TLActionRequest(), parent);
		pathTemplate = new ActionRequestPathTemplate(this);

		if (parent.getRequest() != null)
			parent.getRequest().delete();
		parent.getTLModelObject().setRequest(tlObj);

		setPathTemplate();
	}

	@Override
	public void addListeners() {
		// set listeners onto Param Groups and params to change path template
		if (tlObj != null && tlObj.getParamGroup() != null)
			tlObj.getParamGroup().addListener(new ResourceDependencyListener(this));
		// ((ParamGroup) getNode(tlObj.getParamGroup().getListeners())).addListeners(this);

		if (tlObj.getPayloadType() != null)
			tlObj.getPayloadType().addListener(new ResourceDependencyListener(this));

		// Listen to the containing resource for changes to the path template
		getOwningComponent().getTLModelObject().addListener(new ResourceDependencyListener(this));
	}

	public void updateBasePath(String basePath) {
		// Is this needed? I don't think so!
	}

	public String getInheritedPath() {
		String template = "";
		template += getOwningComponent().getPathContribution(getParamGroup());
		return template;
	}

	@Override
	public void delete() {
		if (tlObj.getOwner() != null)
			tlObj.getOwner().setRequest(null);
		super.delete();
	}

	@Override
	public String getComponentType() {
		return "Action Request";
	}

	@Override
	public String getDecoration() {
		String decoration = "  (";
		decoration += getHttpMethodAsString() + " ";
		decoration += getPayloadName();
		if (getParamGroup() != null)
			decoration += " with " + getParamGroup() + " parameters";
		return decoration + ")";
	}

	@Override
	public List<ResourceField> getFields() {
		List<ResourceField> fields = new ArrayList<>();

		// Payload - Pass list of Action Facet names to select from
		new ResourceField(fields, getPayloadName(), MSGKEY + ".fields.payload", ResourceFieldType.Enum,
				new PayloadTypeListener(), getOwningComponent().getActionFacetNames());

		// Mime Types = multi-select List of possible types
		new ResourceField(fields, tlObj.getMimeTypes().toString(), MSGKEY + ".fields.mimeTypes",
				ResourceFieldType.EnumList, new MimeTypeListener(), getMimeTypeStrings());

		// Parameter Groups - List of group names
		new ResourceField(fields, tlObj.getParamGroupName(), MSGKEY + ".fields.paramGroup", ResourceFieldType.Enum,
				new ParamGroupListener(), getOwningComponent().getParameterGroupNames(false));

		// Path Template - simple String
		new ResourceField(fields, tlObj.getPathTemplate(), MSGKEY + ".fields.path",
				ResourceField.ResourceFieldType.String, new PathTemplateListener());

		// HTTP Method
		new ResourceField(fields, getHttpMethodAsString(), MSGKEY + ".fields.httpMethod", ResourceFieldType.Enum,
				new HttpMethodListener(), getHttpMethodStrings());

		return fields;
	}

	public String getHttpMethodAsString() {
		return tlObj.getHttpMethod() != null ? tlObj.getHttpMethod().toString() : "";
	}

	@Override
	public Image getImage() {
		return Images.getImageRegistry().get(Images.ActionRequest);
	}

	@Override
	public String getName() {
		return getParent() != null ? getParent().getName() + "_Request" : "";
	}

	public ParamGroup getParamGroup() {
		return tlObj.getParamGroup() != null ? (ParamGroup) getNode(tlObj.getParamGroup().getListeners()) : null;
	}

	@Override
	public ActionNode getParent() {
		return (ActionNode) parent;
	}

	public Node getPayload() {
		return tlObj.getPayloadType() != null ? getNode(tlObj.getPayloadType().getListeners()) : null;
	}

	public String getPayloadName() {
		if (tlObj.getPayloadType() == null)
			tlObj.setPayloadTypeName(null); // tl model doesn't always do this
		return tlObj.getPayloadType() != null ? tlObj.getPayloadType().getName() : "";
	}

	@Override
	public String getTooltip() {
		return Messages.getString(MSGKEY + ".tooltip");
	}

	@Override
	public TLActionRequest getTLModelObject() {
		return tlObj;
	}

	@Override
	public TLAction getTLOwner() {
		return tlObj.getOwner();
	}

	@Override
	public boolean isNameEditable() {
		return false;
	}

	@Override
	public void removeDependency(ResourceMemberInterface dependent) {
		if (dependent instanceof ParamGroup)
			setParameterGroup(null);
		else if (dependent instanceof ActionFacet)
			setPayload(null);
		// else if (dependent instanceof ResourceParameter)
		// createPathTemplate();
	}

	public void setHttpMethod(String method) {
		tlObj.setHttpMethod(TLHttpMethod.valueOf(method));
		// LOGGER.debug("Set HTTP method to " + method);
	}

	/**
	 * Implements a toggle action. If the mime type is defined, it is removed. If it not defined it is added.
	 * 
	 * @param value
	 */
	public void toggleMimeType(String value) {
		TLMimeType type = TLMimeType.valueOf(value);
		if (tlObj.getMimeTypes().contains(type))
			tlObj.removeMimeType(type);
		else
			tlObj.addMimeType(type);
		// LOGGER.debug("Set Mime types to: " + tlObj.getMimeTypes());
	}

	@Override
	public void setName(final String name) {
	}

	public boolean setParamGroup(String groupName) {
		if (groupName == null || groupName.equals(ResourceField.NONE)) {
			tlObj.setParamGroup(null);
			// LOGGER.debug("Set parameter group to null. " + groupName);
		} else if (tlObj.getParamGroupName() != null && tlObj.getParamGroupName().equals(groupName)) {
			// LOGGER.debug("No change because names are the same. " + groupName);
			return false;
		} else
			// find the param group with this name then set it
			for (ParamGroup node : getOwningComponent().getParameterGroups(false))
			if (node.getName().equals(groupName))
			setParameterGroup(node);
		// LOGGER.debug("Set parameter group to " + groupName + " : " + tlObj.getParamGroupName());

		pathTemplate.setParameters();
		setPathTemplate();
		return true;
	}

	// TODO - ADD TO JUNIT - remove old listener, add listeners to children parameters
	protected boolean setParameterGroup(ParamGroup group) {
		// Remove old listeners
		if (tlObj != null && tlObj.getParamGroup() != null) {
			Node n = getNode(tlObj.getParamGroup().getListeners());
			if (n instanceof ParamGroup)
				((ParamGroup) n).removeListeners(this);
		}
		if (group != null) {
			tlObj.setParamGroup(group.tlObj);
			group.addListeners(this);
		} else {
			tlObj.setParamGroup(null);
		}
		pathTemplate.setParameters();
		// LOGGER.debug("Set param group to " + group);
		return false;
	}

	/**
	 * Path Template
	 * <p>
	 * Starts with /
	 * <p>
	 * Must / Must Not have query parameters
	 * <p>
	 * Must Not have resource base path
	 * 
	 * @return
	 */
	public String getPathTemplate() {
		tlObj.setPathTemplate(pathTemplate.get());
		return tlObj.getPathTemplate();
	}

	public String getURL() {
		return pathTemplate.getURL();
	}

	/**
	 * Used by listener.
	 * 
	 * @param path
	 */
	public void setPathTemplate(String path) {
		pathTemplate.override(path);
		setPathTemplate();
	}

	/**
	 * Update the path template based on contents of the resource.
	 */
	public void setPathTemplate() {
		assert pathTemplate != null;
		tlObj.setPathTemplate(pathTemplate.get());
		// LOGGER.debug("Set Path template to " + tlObj.getPathTemplate());
	}

	/**
	 * Set the payload to the named object. If "NONE", set to null. Ignore if it is already set to the named type.
	 * 
	 * @param payloadName
	 * @return true if there was a change
	 */
	public boolean setPayloadType(String payloadName) {
		if (tlObj.getPayloadTypeName() != null && tlObj.getPayloadTypeName().equals(payloadName)) {
			// LOGGER.debug("No change because names are the same. " + payloadName);
			return false;
		}
		setPayload(getOwningComponent().getActionFacet(payloadName));
		return true;
	}

	public void setPayload(ActionFacet af) {
		if (af == null) {
			tlObj.setPayloadType(null);
			tlObj.setPayloadTypeName(null);
			tlObj.setMimeTypes(null); // validation warning when mime types are set.
			// LOGGER.debug("Reset payload.");
		} else {
			tlObj.setPayloadType(af.getTLModelObject());
			af.tlObj.addListener(new ResourceDependencyListener(this));
			// LOGGER.debug("Set payload to " + af.getName() + " : " + tlObj.getPayloadTypeName());
		}
	}

}
