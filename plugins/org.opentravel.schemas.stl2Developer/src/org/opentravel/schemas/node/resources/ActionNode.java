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
import org.opentravel.schemacompiler.model.TLAction;
import org.opentravel.schemacompiler.model.TLActionResponse;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.interfaces.ResourceMemberInterface;
import org.opentravel.schemas.node.listeners.ListenerFactory;
import org.opentravel.schemas.node.resources.ResourceField.ResourceFieldType;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.properties.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActionNode extends BaseResourceNode implements ResourceMemberInterface {
	private static final Logger LOGGER = LoggerFactory.getLogger(ActionNode.class);
	private String MSGKEY = "rest.ActionNode";

	public class CommonListener implements ResourceFieldListener {

		@Override
		public boolean set(String value) {
			tlObj.setCommonAction(Boolean.valueOf(value));
			LOGGER.debug("Set common to: " + tlObj.isCommonAction());
			return false;
		}

	}

	private TLAction tlObj = null;

	public ActionNode(TLAction tlAction) {
		this.tlObj = tlAction;
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
		if (tlObj.getRequest() != null)
			getChildren().add(new ActionRequest(tlObj.getRequest()));
		for (TLActionResponse res : tlObj.getResponses())
			getChildren().add(new ActionResponse(res));
	}

	@Override
	public String getDescription() {
		return tlObj.getDocumentation() != null ? tlObj.getDocumentation().getDescription() : "";
	}

	@Override
	public Image getImage() {
		return Images.getImageRegistry().get(Images.ResourceAction);

	}

	@Override
	public String getLabel() {
		return getName();
	}

	@Override
	public String getName() {
		return tlObj.getActionId() != null ? tlObj.getActionId() : "";
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
		return false;
		// return !getChildren().isEmpty();
	}

	@Override
	public String getComponentType() {
		return "Action";
	}

	@Override
	public List<ResourceField> getFields() {
		List<ResourceField> fields = new ArrayList<ResourceField>();
		ResourceField field = new ResourceField(fields, Boolean.toString(tlObj.isCommonAction()),
				"rest.ActionNode.fields.common", ResourceFieldType.CheckButton, new CommonListener());
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
		tlObj.setActionId(name);
	}

}
