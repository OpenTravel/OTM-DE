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
import java.util.Collection;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemacompiler.codegen.impl.QualifiedAction;
import org.opentravel.schemacompiler.codegen.util.ResourceCodegenUtils;
import org.opentravel.schemacompiler.model.TLAction;
import org.opentravel.schemacompiler.model.TLActionRequest;
import org.opentravel.schemacompiler.model.TLActionResponse;
import org.opentravel.schemacompiler.model.TLLibraryMember;
import org.opentravel.schemacompiler.model.TLMimeType;
import org.opentravel.schemacompiler.model.TLResourceParentRef;
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
	private List<InheritedResourceMember> inheritedResponses;

	// // These nodes are never presented in navigator tree so they don't need a children handler.
	// private List<Node> rChildren = new ArrayList<Node>();

	public class CommonListener implements ResourceFieldListener {
		@Override
		public boolean set(String value) {
			setCommon(Boolean.valueOf(value));
			// tlObj.setCommonAction(Boolean.valueOf(value));
			// LOGGER.debug("Set common to: " + tlObj.isCommonAction());
			return false;
		}
	}

	/******************************************************************
	 * 
	 * Set object, children, parent, add to parent's child list, listener(s)
	 */
	public ActionNode(TLAction tlAction) {
		super(tlAction);
		initInherited();
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

		initInherited();
	}

	public ActionNode(ResourceNode parent, Boolean addRequest) {
		super(new TLAction(), parent);
		tlObj.setActionId(""); // prevent NPE in validation
		parent.getTLModelObject().addAction(tlObj);

		// Create a request resource
		if (addRequest) {
			TLActionRequest tlr = new TLActionRequest();
			tlObj.setRequest(tlr); // must have owner for parent to be set correctly
			new ActionRequest(tlr);
		}

		initInherited();
	}

	public void setRQRS(String label, ActionFacet af, List<TLMimeType> rqMimeTypes, List<TLMimeType> rsMimeTypes,
			RestStatusCodes code, ActionRequest request, ActionResponse response) {
		List<Integer> statusCodes = new ArrayList<>(); // http://www.restapitutorial.com/httpstatuscodes.html
		statusCodes.add(code.value());
		setName(label);
		request.tlObj.setMimeTypes(rqMimeTypes);
		response.tlObj.setMimeTypes(rsMimeTypes);
		response.tlObj.setStatusCodes(statusCodes);
		if (af != null)
			response.setPayload(af);
	}

	@Override
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

	public List<InheritedResourceMember> getInherited() {
		return inheritedResponses;
	}

	public void initInherited() {
		// Remove old ones and assure there is an array
		// if (inheritedResponses == null)
		inheritedResponses = new ArrayList<>();
		if (tlObj == null)
			return;
		// Note - tlAR may not have been modeled yet
		for (TLActionResponse tlAR : ResourceCodegenUtils.getInheritedResponses(tlObj))
			if (!inheritedResponses.contains(Node.GetNode(tlAR)))
				if (!getChildren().contains(Node.GetNode(tlAR)))
					inheritedResponses.add(new InheritedResourceMember(tlAR));
		// inheritedResponses.add(new InheritedResourceMember((ActionResponse) Node.GetNode(tlAR)));
		// LOGGER.debug("Handle inherited responses.");
	}

	public void addResponse(ActionResponse actionResponse) {
		if (!getChildren().contains(actionResponse))
			getChildren().add(actionResponse);
	}

	@Override
	public void delete() {
		List<Node> kids = new ArrayList<>(getChildren()); // avoid co-modification of list
		for (Node child : kids)
			child.delete();
		if (tlObj.getOwner() != null)
			tlObj.getOwner().removeAction(tlObj);
		super.delete();
	}

	@Override
	public List<Node> getNavChildren(boolean deep) {
		List<Node> kids = new ArrayList<>();
		kids.addAll(super.getChildren());
		if (deep) {
			if (inheritedResponses == null)
				initInherited();
			if (!inheritedResponses.isEmpty())
				kids.addAll(inheritedResponses);
		}
		return kids;
	}

	@Override
	public String getComponentType() {
		return "Action";
	}

	@Override
	public String getDecoration() {
		// Get request's action facet
		String decoration = "  (";
		if (getRequest() != null) {
			decoration += getRequest().getHttpMethodAsString() + " : ";
			if (getRequest().getPayload() == null)
				decoration += getRequest().getParamGroup() + " Parameters";
			else
				decoration += getRequest().getPayload();
		}
		if (getResponse() != null) {
			decoration += " - Returns : ";
			if (getResponses().size() > 1)
				decoration += "Multiple responses";
			else {
				if (!getResponse().getPayloadName().isEmpty())
					decoration += getResponse().getPayloadName();
				else
					decoration += "Status only";
			}
		}
		return decoration + ")";
	}

	@Override
	public List<ResourceField> getFields() {
		List<ResourceField> fields = new ArrayList<>();
		new ResourceField(fields, Boolean.toString(tlObj.isCommonAction()), "rest.ActionNode.fields.common",
				ResourceFieldType.CheckButton, new CommonListener());
		return fields;
	}

	@Override
	public Image getImage() {
		return Images.getImageRegistry().get(Images.ResourceAction);

	}

	/**
	 * Return the actionId from the tl action object
	 */
	@Override
	public String getName() {
		return tlObj.getActionId() != null ? tlObj.getActionId() : "";
	}

	public ActionRequest getRequest() {
		for (Node n : getChildren())
			if (n instanceof ActionRequest)
				return (ActionRequest) n;
		return null;
	}

	/**
	 * Return the first action response found
	 * 
	 * @return
	 */
	public ActionResponse getResponse() {
		for (Node n : getChildren())
			if (n instanceof ActionResponse)
				return (ActionResponse) n;
		return null;
	}

	/**
	 * Return the first action response found
	 * 
	 * @return
	 */
	public Collection<ActionResponse> getResponses() {
		List<ActionResponse> responses = new ArrayList<>();
		for (Node n : getChildren())
			if (n instanceof ActionResponse)
				responses.add((ActionResponse) n);
		return responses;
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
		return super.isEditable();
		// return true;
	}

	@Override
	public void setName(final String name) {
		tlObj.setActionId(name);
	}

	public void setCommon(Boolean state) {
		tlObj.setCommonAction(state);
		return;
	}

	public String getQueryTemplate() {
		if (getRequest() == null)
			return "";
		if (getRequest().getParamGroup() == null)
			return "";
		return getRequest().getParamGroup().getQueryTemplate();
	}

	public String getPathTemplate() {
		return getRequest() != null ? getRequest().getPathTemplate() : "";
	}

	/**
	 * Uses ResourceCodegenUtils.
	 * 
	 * If a parent resource reference is present, the resulting path will include the path of the parent.
	 * 
	 * @return URL contribution made by parent ref or empty string
	 */
	public String getParentContribution() {
		// String contribution = getOwningComponent().getTLModelObject().getBasePath();
		String contribution = "";

		// // Pick any one of these
		// List<QualifiedAction> qa = ResourceCodegenUtils.getQualifiedActions(getTLModelObject());
		// if (qa.isEmpty()) return contribution;
		// String template = qa.get(0).getPathTemplate();

		// From hotfix: https://github.com/OpenTravel/OTM-DE/commit/a9be4859740aaeb9e5607485d844bf67786b4816
		// Since numerous combinations of parent reference paths are possible, pick
		// the last entry in the list of qualified actions. It is the most likely to
		// have an "interesting" path that contains parent references.
		List<QualifiedAction> qa = ResourceCodegenUtils.getQualifiedActions(getTLModelObject());
		if (qa.isEmpty())
			return contribution;
		List<TLResourceParentRef> parentRefs = qa.get(qa.size() - 1).getParentRefs();
		for (TLResourceParentRef tlRef : parentRefs) {
			if (tlRef.getPathTemplate() != null)
				contribution = tlRef.getPathTemplate() + contribution;
		}

		// This seems to duplicate the parent ref
		// List<TLResourceParentRef> list = ResourceCodegenUtils.getInheritedParentRefs(getOwningComponent()
		// .getTLModelObject());
		// for (TLResourceParentRef tlRef : list) {
		// // Parent Ref has its own PathTemplate complete with base path and parameters
		// if (tlRef.getPathTemplate() != null)
		// contribution = tlRef.getPathTemplate() + contribution;
		// }
		// 2/6/2018 - hotfix above does NOT remove need to recurse through parents
		// FIXME - codegen is only getting closet ancestor...either fix the codegen utils or walk ancestor vector
		// if (list.size() <= 1 && getOwningComponent().getParentRef() != null) {
		// ResourceNode ancestor = getOwningComponent().getParentRef().getParentResource();
		// if (ancestor != null) {
		// ActionNode action = ancestor.getActions().get(0);
		// contribution = action.getParentContribution() + contribution;
		// }
		// }

		// LOGGER.debug("Parent contribution to " + getOwningComponent() + ": " + contribution);
		return contribution;
	}
}
