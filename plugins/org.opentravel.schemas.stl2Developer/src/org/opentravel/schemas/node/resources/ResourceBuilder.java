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

import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAction;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLActionRequest;
import org.opentravel.schemacompiler.model.TLActionResponse;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLHttpMethod;
import org.opentravel.schemacompiler.model.TLMimeType;
import org.opentravel.schemacompiler.model.TLParamGroup;
import org.opentravel.schemacompiler.model.TLParameter;
import org.opentravel.schemacompiler.model.TLReferenceType;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemas.node.BusinessObjectNode;
import org.opentravel.schemas.node.ComponentNode;

/**
 * Creates resources and the associated components.
 * 
 * @author Dave
 *
 */
public class ResourceBuilder {
	public enum ResourceFieldType {
		String, Int, Enum, List, EnumList, NodeList, CheckButton
	}

	final String NAME = "unNamedResource";

	public ResourceBuilder() {
	}

	public void build(ResourceNode rn, BusinessObjectNode bo) {
		TLResource rnTL = rn.getTLModelObject();
		rnTL.setBusinessObjectRef((TLBusinessObject) bo.getTLModelObject());
		rn.setName(bo.getName() + "Resource");
		rn.setBasePath("/" + bo.getName() + "s");
		rnTL.setAbstract(false);
		rnTL.setFirstClass(true);

		// ID Parameters - ID, Query(s)
		ParamGroup pg = buildParamGroup(rn, (ComponentNode) bo.getIDFacet(), true);
		// Query Parameters
		for (ComponentNode fn : bo.getQueryFacets()) {
			ParamGroup qpg = buildParamGroup(rn, fn, false);
			buildAction(rn, qpg, TLHttpMethod.GET);
		}

		// Action Facet
		TLActionFacet facet = new TLActionFacet();
		rnTL.addActionFacet(facet);
		facet.setName("Summary");
		facet.setFacetType(TLFacetType.ACTION);
		ActionFacet af = new ActionFacet(facet);
		af.setReferenceFacetName("Summary");
		af.setReferenceType(TLReferenceType.REQUIRED.toString());

		// Action
		ActionNode an = buildAction(rn, pg, TLHttpMethod.GET);
		buildAction(rn, pg, TLHttpMethod.POST);
		// buildAction(rn, pg, af, TLHttpMethod.GET);
	}

	private ActionNode buildAction(ResourceNode rn, ParamGroup pg, TLHttpMethod method) {
		TLAction action = new TLAction();
		rn.getTLModelObject().addAction(action);
		TLActionRequest request = new TLActionRequest();
		action.setRequest(request);
		TLActionResponse response = new TLActionResponse();
		action.addResponse(response);

		switch (method) {
		case GET:
			action.setActionId("Get");
			response.setPayloadType((NamedEntity) rn.getSubject().getSummaryFacet());
			break;
		case POST:
			action.setActionId("Create");
			response.setPayloadType((NamedEntity) rn.getSubject().getIDFacet());
			break;
		case DELETE:
			action.setActionId("Delete");
			break;
		case PUT:
			action.setActionId("Update");
			break;
		default:
			break;
		}
		ActionNode an = new ActionNode(action); // creates request and response node controllers
		// FIXME - response.setMimeTypes(TLMimeType.APPLICATION_JSON);
		an.getRequest().setMimeType(TLMimeType.APPLICATION_JSON.toString());
		an.getRequest().setHttpMethod(method.toString());
		an.getRequest().setParamGroup(pg.getName()); // do here to set path template
		return an;
	}

	private ParamGroup buildParamGroup(ResourceNode rn, ComponentNode fn, boolean idGroup) {
		TLParamGroup params = new TLParamGroup();
		rn.getTLModelObject().addParamGroup(params);
		params.setName(fn.getLabel());
		params.setIdGroup(idGroup);
		ParamGroup pg = new ParamGroup(params);
		pg.setReferenceFacet(fn.getLabel()); // will force params to be created
		return pg;
	}

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
		tlr.setBusinessObjectRef((TLBusinessObject) businessObject.getTLModelObject());
		return tlr;
	}

}