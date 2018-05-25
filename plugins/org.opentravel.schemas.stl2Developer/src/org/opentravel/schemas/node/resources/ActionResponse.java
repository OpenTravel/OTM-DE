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
import org.opentravel.schemacompiler.model.TLActionResponse;
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
 * Action Response controller. Provides getters, setters and listeners for editable fields.
 * 
 * @author Dave
 *
 */
public class ActionResponse extends ResourceBase<TLActionResponse> implements ResourceMemberInterface {
	private static final Logger LOGGER = LoggerFactory.getLogger(ActionResponse.class);
	private String MSGKEY = "rest.ActionResponse";

	public class PayloadListener implements ResourceFieldListener {
		@Override
		public boolean set(String name) {
			setPayloadType(name);
			return false;
		}
	}

	public class StatusCodeListener implements ResourceFieldListener {
		@Override
		public boolean set(String value) {
			setStatusCode(value);
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

	/*********************************************************************
	 * Create an TLActionResponse model object and add this to the parent
	 */
	public ActionResponse(TLActionResponse tlActionResponse) {
		super(tlActionResponse);
	}

	public ActionResponse(ActionNode parent) {
		super(new TLActionResponse(), parent);

		parent.getTLModelObject().addResponse(tlObj);
	}

	@Override
	public void addChildren() {
	}

	@Override
	public void addListeners() {
		if (tlObj.getPayloadType() != null)
			tlObj.getPayloadType().addListener(new ResourceDependencyListener(this));
	}

	/**
	 * Remove this from the parent, remove from TL Owner, and free any resources.
	 */
	@Override
	public void delete() {
		if (tlObj.getOwner() != null)
			tlObj.getOwner().removeResponse(tlObj);
		super.delete();
	}

	@Override
	public String getComponentType() {
		return "Action Response";
	}

	@Override
	public String getDecoration() {
		String decoration = "  (";
		decoration += " Returns ";
		if (getPayload() == null)
			decoration += "Status Only";
		else
			decoration += getPayloadName();
		return decoration + ")";
	}

	public String getPayloadName() {
		if (tlObj.getPayloadType() == null)
			tlObj.setPayloadTypeName(null); // tl model doesn't always do this
		return tlObj.getPayloadType() != null ? tlObj.getPayloadType().getName() : "";
	}

	@Override
	public List<ResourceField> getFields() {
		List<ResourceField> fields = new ArrayList<>();

		// Payload type = an action facet
		new ResourceField(fields, getPayloadName(), "rest.ActionResponse.fields.payload", ResourceFieldType.Enum,
				new PayloadListener(), getPossiblePayloadNames());

		// Mime Types = multi-select List of possible types
		new ResourceField(fields, tlObj.getMimeTypes().toString(), "rest.ActionResponse.fields.mimeTypes",
				ResourceFieldType.EnumList, new MimeTypeListener(), getMimeTypeStrings());

		// Status codes = string
		new ResourceField(fields, getStatusCodes(), "rest.ActionResponse.fields.statusCodes",
				ResourceFieldType.EnumList, new StatusCodeListener(), RestStatusCodes.getCodes());

		return fields;
	}

	@Override
	public Image getImage() {
		return Images.getImageRegistry().get(Images.ActionResponse);

	}

	@Override
	public String getName() {
		return !getStatusCodes().isEmpty() ? getStatusCodes() + " Response" : "Default";
	}

	public String getStatusCodes() {
		String codes = "";
		for (Integer i : tlObj.getStatusCodes())
			codes += RestStatusCodes.getLabel(i) + " ";
		return codes;
	}

	@Override
	public ActionNode getParent() {
		return (ActionNode) parent;
	}

	public Node getPayload() {
		return tlObj.getPayloadType() != null ? getNode(tlObj.getPayloadType().getListeners()) : null;
	}

	@Override
	public TLActionResponse getTLModelObject() {
		return tlObj;
	}

	@Override
	public TLAction getTLOwner() {
		return tlObj.getOwner();
	}

	@Override
	public String getTooltip() {
		return Messages.getString(MSGKEY + ".tooltip");
	}

	@Override
	public boolean isInherited() {
		// Inherited tlObj will still have true owners
		return Node.GetNode(tlObj.getOwner().getOwner()) != getOwningComponent();
	}

	@Override
	public boolean isNameEditable() {
		return false;
	}

	@Override
	public void removeDependency(ResourceMemberInterface dependent) {
		if (dependent instanceof ActionFacet)
			setPayload(null);
	}

	@Override
	public void setName(final String name) {
		// can't change name
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

	protected void setStatusCode(String value) {
		Integer selection = RestStatusCodes.valueOf(value).value();
		List<Integer> codes = new ArrayList<>(tlObj.getStatusCodes());
		if (codes.contains(selection))
			codes.remove(selection);
		else
			codes.add(selection);
		tlObj.setStatusCodes(codes);
		// LOGGER.debug("Set Status Codes to " + value + " : " + tlObj.getStatusCodes());
	}

	protected String[] getPossiblePayloadNames() {
		List<Node> nodes = getPossiblePayloads();
		String[] data = new String[nodes.size() + 1];
		int i = 0;
		data[i++] = ResourceField.NONE;
		for (Node n : nodes)
			// if (n.getOwningComponent() == this.getOwningComponent())
			data[i++] = n.getLabel();
		// // Do NOT include facets from base types
		// else
		// data[i++] = n.getParent().getName() + ":" + n.getLabel();
		return data;
	}

	protected List<Node> getPossiblePayloads() {
		List<Node> nodes = new ArrayList<>();
		for (ActionFacet af : getOwningComponent().getActionFacets())
			nodes.add(af);
		return nodes;
	}

}
