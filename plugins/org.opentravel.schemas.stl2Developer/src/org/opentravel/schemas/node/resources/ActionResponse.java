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
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAction;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLActionResponse;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLMimeType;
import org.opentravel.schemas.node.CoreObjectNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.interfaces.ResourceMemberInterface;
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
			setPayload(name);
			return false;
		}
	}

	public class StatusCodeListener implements ResourceFieldListener {
		@Override
		public boolean set(String value) {
			// tlObj.setStatusCodes(value);
			LOGGER.debug("Set status codes to: " + tlObj.getStatusCodes());
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
	 * 
	 */
	public ActionResponse(ActionNode parent) {
		super(new TLActionResponse()); // don't use this() - no owner yet
		this.parent = parent;
		((TLAction) parent.getTLModelObject()).addResponse(tlObj);
		getParent().addChild(this);
	}

	public ActionResponse(TLActionResponse tlActionResponse) {
		super(tlActionResponse);
		parent = this.getNode(((TLAction) tlObj.getOwner()).getListeners());
		assert parent instanceof ActionNode;
		((ActionNode) parent).addResponse(this);
	}

	public void addChildren() {
	}

	/**
	 * Remove this from the parent, remove from TL Owner, and free any resources.
	 */
	@Override
	public void delete() {
		clearListeners();
		tlObj.getOwner().removeResponse(tlObj);
		parent.getChildren().remove(this);
	}

	@Override
	public String getComponentType() {
		return "Action Response";
	}

	public String getFacetName() {
		String facetname = "";
		if (tlObj.getPayloadType() instanceof TLCoreObject)
			facetname = ((TLCoreObject) tlObj.getPayloadType()).getName();
		else if (tlObj.getPayloadType() instanceof TLActionFacet)
			facetname = ((TLActionFacet) tlObj.getPayloadType()).getName();
		return facetname;
	}

	@Override
	public List<ResourceField> getFields() {
		List<ResourceField> fields = new ArrayList<ResourceField>();

		// Mime Types = multi-select List of possible types
		new ResourceField(fields, tlObj.getMimeTypes().toString(), "rest.ActionResponse.fields.mimeTypes",
				ResourceFieldType.EnumList, new MimeTypeListener(), getMimeTypeStrings());

		// Payload type = either an action facet or a core object
		new ResourceField(fields, getFacetName(), "rest.ActionResponse.fields.payload", ResourceFieldType.Enum,
				new PayloadListener(), getPossiblePayloadNames());

		// Status codes = string
		new ResourceField(fields, tlObj.getStatusCodes().toString(), "rest.ActionResponse.fields.statusCodes",
				ResourceFieldType.String, new StatusCodeListener());

		return fields;
	}

	@Override
	public Image getImage() {
		return Images.getImageRegistry().get(Images.ActionResponse);

	}

	@Override
	public String getName() {
		return tlObj.getLocalName() != null ? tlObj.getLocalName() : "";
	}

	@Override
	public ActionNode getParent() {
		return (ActionNode) parent;
	}

	public Node getPayload() {
		Node payload = null;
		// action facet or core object
		NamedEntity pl = tlObj.getPayloadType();
		if (pl instanceof TLActionFacet)
			payload = this.getNode(((TLActionFacet) pl).getListeners());
		else if (pl instanceof TLCoreObject)
			payload = this.getNode(((TLCoreObject) pl).getListeners());
		else if (pl != null)
			throw new IllegalArgumentException("Invalid Response Payload type: " + pl.getClass().getSimpleName());
		return payload;
	}

	@Override
	public String getTooltip() {
		return Messages.getString(MSGKEY + ".tooltip");
	}

	@Override
	public boolean isNameEditable() {
		return false;
	}

	@Override
	public void setName(final String name) {
		// TODO - can't change name
	}

	public void setPayload(String name) {
		for (Node n : getPossiblePayloads())
			if (n.getName().equals(name)) {
				tlObj.setPayloadType((NamedEntity) n.getTLModelObject());
				LOGGER.debug("Set payload to: " + tlObj.getPayloadTypeName());
			}
	}

	/**
	 * Implements a toggle action. If the mime type is defined, it is removed. If it not defined it is added.
	 * 
	 * @param value
	 */
	public void toggleMimeType(String value) {
		TLMimeType type = TLMimeType.valueOf(value);
		// FIXME when issue https://github.com/OpenTravel/OTM-DE-Compiler/issues/32 resolved
		// MimeType x = MimeType.valueOf(value);
		// if (tlObj.getMimeTypes().contains(type))
		// tlObj.removeMimeType(x);
		// else
		tlObj.addMimeType(type);
		LOGGER.debug("Set Mime types to: " + tlObj.getMimeTypes());
	}

	protected String[] getPossiblePayloadNames() {
		List<Node> nodes = getPossiblePayloads();
		String[] data = new String[nodes.size()];
		int i = 0;
		for (Node n : nodes)
			data[i++] = n.getLabel();
		return data;
	}

	protected List<Node> getPossiblePayloads() {
		List<ActionFacet> afs = getOwningComponent().getActionFacets();
		List<Node> nodes = new ArrayList<Node>();
		for (Node n : getOwningComponent().getLibrary().getDescendants_NamedTypes())
			if (n instanceof CoreObjectNode)
				nodes.add(n);
		for (ActionFacet af : afs)
			nodes.add(af);
		return nodes;
	}

}
