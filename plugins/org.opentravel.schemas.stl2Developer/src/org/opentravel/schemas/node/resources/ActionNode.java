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
import org.opentravel.schemacompiler.codegen.impl.QualifiedAction;
import org.opentravel.schemacompiler.codegen.util.ResourceCodegenUtils;
import org.opentravel.schemacompiler.model.TLAction;
import org.opentravel.schemacompiler.model.TLActionRequest;
import org.opentravel.schemacompiler.model.TLActionResponse;
import org.opentravel.schemacompiler.model.TLLibraryMember;
import org.opentravel.schemacompiler.model.TLMimeType;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.interfaces.ResourceMemberInterface;
import org.opentravel.schemas.node.resources.ResourceField.ResourceFieldType;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.properties.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resource Action controller. Provides getters, setters and listeners for editable fields. Maintains example for the
 * actions.
 * 
 * @author Dave
 *
 */
public class ActionNode extends ResourceBase<TLAction> implements ResourceMemberInterface {
	private static final Logger LOGGER = LoggerFactory.getLogger(ActionNode.class);
	private String MSGKEY = "rest.ActionNode";
	private ActionExample example = null;

	public class CommonListener implements ResourceFieldListener {
		@Override
		public boolean set(String value) {
			tlObj.setCommonAction(Boolean.valueOf(value));
			LOGGER.debug("Set common to: " + tlObj.isCommonAction());
			return false;
		}
	}

	/******************************************************************
	 * 
	 * Set object, children, parent, add to parent's child list, listener(s)
	 */
	public ActionNode(TLAction tlAction) {
		super(tlAction);
	}

	/**
	 * Create Action node including TL object and the request. Designed for resource command actions.
	 * 
	 * @param parent
	 */
	public ActionNode(ResourceNode parent) {
		super(new TLAction(), parent);
		tlObj.setActionId(""); // prevent NPE in validation
		parent.getTLModelObject().addAction(tlObj);

		// Create a request resource
		TLActionRequest tlr = new TLActionRequest();
		tlObj.setRequest(tlr); // must have owner for parent to be set correctly
		new ActionRequest(tlr);
	}

	public void setRQRS(String label, ActionFacet af, List<TLMimeType> rqMimeTypes, List<TLMimeType> rsMimeTypes,
			RestStatusCodes code, ActionRequest request, ActionResponse response) {
		List<Integer> statusCodes = new ArrayList<Integer>(); // http://www.restapitutorial.com/httpstatuscodes.html
		statusCodes.add(code.value());
		setName(label);
		request.tlObj.setMimeTypes(rqMimeTypes);
		response.tlObj.setMimeTypes(rsMimeTypes);
		response.tlObj.setStatusCodes(statusCodes);
		if (af != null)
			response.setPayload(af);
	}

	public void addChild(ResourceMemberInterface child) {
		if (!getChildren().contains(child))
			getChildren().add((Node) child);
	}

	@Override
	public void addChildren() {
		if (tlObj.getRequest() != null)
			new ActionRequest(tlObj.getRequest());
		for (TLActionResponse res : tlObj.getResponses())
			new ActionResponse(res);
	}

	public void addResponse(ActionResponse actionResponse) {
		if (!getChildren().contains(actionResponse))
			getChildren().add(actionResponse);
	}

	@Override
	public void delete() {
		List<Node> kids = new ArrayList<Node>(getChildren()); // avoid co-modification of list
		for (Node child : kids)
			child.delete();
		if (tlObj.getOwner() != null)
			tlObj.getOwner().removeAction(tlObj);
		super.delete();
	}

	@Override
	public String getComponentType() {
		return "Action";
	}

	public ActionExample getExample() {
		if (example == null)
			example = new ActionExample(this);
		return example;
	}

	@Override
	public List<ResourceField> getFields() {
		List<ResourceField> fields = new ArrayList<ResourceField>();
		new ResourceField(fields, Boolean.toString(tlObj.isCommonAction()), "rest.ActionNode.fields.common",
				ResourceFieldType.CheckButton, new CommonListener());
		return fields;
	}

	@Override
	public Image getImage() {
		return Images.getImageRegistry().get(Images.ResourceAction);

	}

	@Override
	public String getName() {
		return tlObj.getActionId() != null ? tlObj.getActionId() : "";
	}

	public ActionRequest getRequest() {
		// FIXME
		// ResourceCodegenUtils.getInheritedActions(resource)
		// ResourceCodegenUtils.getInheritedResponses(action)
		// ResourceCodegenUtils.getEffectivePath()
		// ResourceCodegenUtils.getQualifiedActions(resource)

		for (Node n : getChildren())
			if (n instanceof ActionRequest)
				return (ActionRequest) n;
		return null;
	}

	@Override
	public TLAction getTLModelObject() {
		return tlObj;
	}

	@Override
	public TLLibraryMember getTLOwner() {
		return tlObj.getOwner();
	}

	@Override
	public String getTooltip() {
		return Messages.getString(MSGKEY + ".tooltip");
	}

	@Override
	public boolean hasNavChildren(boolean deep) {
		return false;
	}

	@Override
	public boolean isNameEditable() {
		return true;
	}

	@Override
	public void setName(final String name) {
		tlObj.setActionId(name);
	}

	public String getQueryTemplate() {
		if (getRequest() == null)
			return "";
		if (getRequest().getParamGroup() == null)
			return "";
		return getRequest().getParamGroup().getQueryTemplate();
	}

	/**
	 * @return URL contribution made by parent ref or empty string
	 */
	public String getParentContribution() {
		// FIXME - dependent on order of list
		// There are many possibilities depending on if the ancestors are 1st class.
		// Choose the longest.
		String qaContrib = "";
		for (QualifiedAction qa : ResourceCodegenUtils.getQualifiedActions(getOwningComponent().getTLModelObject())) {
			if (qa.getAction() == tlObj)
				if (qa.getPathTemplate().length() > qaContrib.length())
					qaContrib = qa.getPathTemplate();
		}
		return qaContrib;

		// String contribution = "";
		// List<TLResourceParentRef> list = ResourceCodegenUtils.getInheritedParentRefs(getOwningComponent()
		// .getTLModelObject());
		// for (TLResourceParentRef tlRef : list) {
		// contribution += tlRef.getPathTemplate();
		// }
		// return contribution;
		// Find the parent resource if any
		// ParentRef pr = getOwningComponent().getParentRef();
		// return pr != null ? pr.getUrlContribution() : "";
		// ResourceNode ppg = getOwningComponent().getParentParamGroup();
		// return parent != null ? ppg.getPathContribution() : "";
	}
}
