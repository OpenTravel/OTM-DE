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

import org.opentravel.schemacompiler.codegen.util.ResourceCodegenUtils;
import org.opentravel.schemacompiler.model.TLAction;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLActionRequest;
import org.opentravel.schemacompiler.model.TLActionResponse;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLHttpMethod;
import org.opentravel.schemacompiler.model.TLMimeType;
import org.opentravel.schemacompiler.model.TLParamGroup;
import org.opentravel.schemacompiler.model.TLParameter;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.typeProviders.AbstractContextualFacet;
import org.opentravel.schemas.node.typeProviders.facetOwners.BusinessObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates resources and the associated components.
 * 
 * @author Dave
 *
 */
public class ResourceBuilder {
	private static final Logger LOGGER = LoggerFactory.getLogger(ResourceBuilder.class);

	public enum ResourceFieldType {
		String, Int, Enum, List, EnumList, NodeList, CheckButton
	}

	final String NAME = "unNamedResource";

	public ResourceBuilder() {
	}

	/**
	 * Populate the passed resource to represent the passed business object.
	 * 
	 * @param rn
	 *            resource node to populate
	 * @param bo
	 *            business object to use as the subject, if null an abstract resource is created
	 */
	public void build(ResourceNode rn, BusinessObjectNode bo) {
		if (rn == null)
			return;
		if (bo == null) {
			buildAbstract(rn);
			return;
		}
		TLResource rnTL = rn.getTLModelObject();
		// rnTL.setBusinessObjectRef((TLBusinessObject) bo.getTLModelObject());
		rn.setSubject(bo);
		rn.setName(bo.getName() + "Resource");

		// Do not repeat the object name in the base path.
		// It will be added to request paths. This is required for when a resource is used as a parent resource since
		// the base path can not have parameters.
		rn.setBasePath("");
		// rn.setBasePath("/" + bo.getName() + "s");
		rnTL.setAbstract(false);
		rnTL.setFirstClass(true);

		// Parameter group for ID facet
		ParamGroup idPG = new ParamGroup(rn, bo.getFacet_ID(), true);

		// Action Facets
		// TODO - add base payload
		ActionFacet subAF = new ActionFacet(rn, null); // substitution group
		subAF.setName(rn.getSubjectName() + "Response");

		ActionFacet listAF = new ActionFacet(rn, null); // List of subjects
		listAF.setName(rn.getSubjectName() + "ListResponse");
		listAF.setReferenceRepeat(1000);
		listAF.setReferenceType("OPTIONAL");

		ActionFacet idAF = new ActionFacet(rn, TLFacetType.ID);
		idAF.setName(rn.getSubjectName() + "ID");
		// ActionFacet summaryAF = new ActionFacet(rn, TLFacetType.SUMMARY);

		// Query facet based parameter groups and action facets
		for (ComponentNode fn : bo.getQueryFacets()) {
			ParamGroup qpg = new ParamGroup(rn, fn, false);

			ActionFacet af = new ActionFacet(rn, null);
			af.setReferenceFacetName(ResourceCodegenUtils.getActionFacetReferenceName((TLFacet) fn.getTLModelObject()));
			af.setName(fn.getName());

			// Path += "Queries", POST, this AF payload, no PG
			ActionNode action = buildAction(rn, af, null, TLHttpMethod.POST); // Query
			action.getRequest().setPathTemplate(action.getRequest().getPathTemplate() + "/Queries");
			action.getResponse().setPayload(listAF);
			// ActionNode action = buildAction(rn, listAF, qpg, TLHttpMethod.GET); // Query
			action.setName(fn.getName());
		}
		// Add action facets for custom facets.
		for (AbstractContextualFacet fn : bo.getCustomFacets()) {
			ParamGroup qpg = new ParamGroup(rn, fn, false);

			// Response and Response List action facets
			ActionFacet af = new ActionFacet(rn, null);
			// af.setReferenceFacetName(fn.getLocalName());
			af.setReferenceFacetName(ResourceCodegenUtils.getActionFacetReferenceName(fn.getTLModelObject()));
			af.setName(fn.getLocalName() + rn.getSubjectName() + "Response");

			ActionFacet afList = new ActionFacet(rn, null);
			// afList.setReferenceFacetName(fn.getLocalName());
			afList.setReferenceFacetName(ResourceCodegenUtils.getActionFacetReferenceName(fn.getTLModelObject()));
			afList.setName(fn.getLocalName() + rn.getSubjectName() + "ListResponse");
			afList.setReferenceRepeat(1000);
			afList.setReferenceType("OPTIONAL");

			ActionNode action = buildAction(rn, af, idPG, TLHttpMethod.GET); // Read
			action.setName(action.getName() + fn.getLocalName());
			action.getRequest().setPathTemplate("/" + fn.getLocalName() + rn.getSubjectName() + "s");
		}

		// Action
		buildAction(rn, idAF, idPG, TLHttpMethod.GET); // Read
		buildAction(rn, subAF, null, TLHttpMethod.POST); // Create
		buildAction(rn, subAF, idPG, TLHttpMethod.PUT); // Update
		buildAction(rn, null, idPG, TLHttpMethod.DELETE); // Delete
	}

	public static String ABSTRACT_NAME = "BaseResource";
	public static String ERROR_AF_NAME = "ErrorResponse";
	public static String ERROR_ACTION_NAME = "ErrorResponse";

	private void buildAbstract(ResourceNode rn) {
		buildAbstract(rn, false);
	}

	/**
	 * 
	 * @param rn
	 * @param includeActionFacet
	 *            - create an action facet used in response but without payload it will be invalid
	 * @see ActionFacet#setBasePayload(Node)
	 */
	public ActionFacet buildAbstract(ResourceNode rn, boolean includeActionFacet) {
		rn.setAbstract(true);
		rn.setName(ABSTRACT_NAME);

		// Create an action facet for the base response
		ActionFacet af = null;
		if (includeActionFacet) {
			af = new ActionFacet(rn);
			af.setName(ERROR_AF_NAME);
		}

		// Create a common action
		ActionNode an = new ActionNode(rn, false);
		an.setCommon(true);
		an.setName(ERROR_ACTION_NAME);

		// Create error response
		ActionResponse ar = new ActionResponse(an);
		for (TLMimeType l : TLMimeType.values())
			if (l.toString().startsWith("APPL"))
				ar.toggleMimeType(l.toString());
		for (RestStatusCodes code : RestStatusCodes.values())
			if (code.value() >= 400)
				ar.setStatusCode(RestStatusCodes.getLabel(code.value()));
		ar.setPayload(af); // will remove mime types if null

		return af;
	}

	private ActionNode buildAction(ResourceNode rn, ActionFacet af, ParamGroup pg, TLHttpMethod method) {
		List<TLMimeType> mimeTypes = new ArrayList<>();
		mimeTypes.add(TLMimeType.APPLICATION_JSON);
		mimeTypes.add(TLMimeType.APPLICATION_XML);
		ActionNode an = new ActionNode(rn);
		ActionRequest rq = an.getRequest();
		ActionResponse rs = new ActionResponse(an);

		switch (method) {
		case GET:
			an.setRQRS("Get", af, null, mimeTypes, RestStatusCodes.OK, rq, rs);
			break;
		case POST:
			an.setRQRS("Create", af, mimeTypes, mimeTypes, RestStatusCodes.CREATED, rq, rs);
			an.getRequest().setPayload(af);
			break;
		case DELETE:
			an.setRQRS("Delete", af, null, null, RestStatusCodes.OK, rq, rs);
			break;
		case PUT:
			an.setRQRS("Update", af, mimeTypes, mimeTypes, RestStatusCodes.OK, rq, rs);
			an.getRequest().setPayload(af);
			break;
		default:
			break;
		}
		rq.setHttpMethod(method.toString());
		if (pg != null)
			rq.setParamGroup(pg.getName()); // do here to set path template
		rq.setPathTemplate(); // load tlObject from path template object
		return an;
	}

	/**
	 * 
	 * @return a TLResource populated with Parameter Group, Actions and Action Facets but NO subject
	 */
	public TLResource buildTL() {
		return buildTL(NAME);
	}

	public TLResource buildTL(String name) {
		TLResource resource = new TLResource();
		resource.setName(name);

		TLParamGroup params = new TLParamGroup();
		resource.addParamGroup(params);
		params.setName("ID Parameters");
		TLParameter parameter = new TLParameter();
		params.addParameter(parameter);
		parameter.setFieldRefName(name);

		TLAction action = new TLAction();
		resource.addAction(action);
		action.setActionId("Get");
		TLActionResponse response = new TLActionResponse();
		action.addResponse(response);
		response.setPayloadTypeName("resource");
		TLActionRequest request = new TLActionRequest();
		action.setRequest(request);
		request.setPayloadTypeName("resource");

		TLActionFacet facet = new TLActionFacet();
		resource.addActionFacet(facet);
		facet.setName("Summary");
		return resource;
	}

	public TLResource buildTL(BusinessObjectNode businessObject) {
		String name = "NewResource"; // must be named to add to library
		if (!businessObject.getName().isEmpty())
			name = businessObject.getName() + "Resource";
		TLResource tlr = buildTL(name);
		tlr.setBusinessObjectRef(businessObject.getTLModelObject());
		return tlr;
	}

}