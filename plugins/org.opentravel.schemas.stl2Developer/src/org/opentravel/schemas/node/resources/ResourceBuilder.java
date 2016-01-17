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
		if (bo == null || rn == null)
			return;
		TLResource rnTL = rn.getTLModelObject();
		rnTL.setBusinessObjectRef((TLBusinessObject) bo.getTLModelObject());
		rn.setName(bo.getName() + "Resource");
		rn.setBasePath("/" + bo.getName() + "s");
		rnTL.setAbstract(false);
		rnTL.setFirstClass(true);

		// Action Facet
		ActionFacet subAF = new ActionFacet(rn, null); // substitution group
		ActionFacet idAF = new ActionFacet(rn, TLFacetType.ID);
		ActionFacet summaryAF = new ActionFacet(rn, TLFacetType.SUMMARY);

		// Parameters - ID, Query(s)
		ParamGroup idPG = new ParamGroup(rn, (ComponentNode) bo.getIDFacet(), true);
		for (ComponentNode fn : bo.getQueryFacets()) {
			ParamGroup qpg = new ParamGroup(rn, fn, false);
			ActionNode action = buildAction(rn, idAF, qpg, TLHttpMethod.GET); // Query
			action.setName(fn.getLabel());
		}

		// Action
		buildAction(rn, idAF, idPG, TLHttpMethod.GET); // Read
		buildAction(rn, subAF, idPG, TLHttpMethod.POST); // Create
		buildAction(rn, subAF, idPG, TLHttpMethod.PUT); // Update
		buildAction(rn, null, idPG, TLHttpMethod.DELETE); // Delete
	}

	private ActionNode buildAction(ResourceNode rn, ActionFacet af, ParamGroup pg, TLHttpMethod method) {
		List<TLMimeType> mimeTypes = new ArrayList<TLMimeType>();
		mimeTypes.add(TLMimeType.APPLICATION_JSON);
		mimeTypes.add(TLMimeType.APPLICATION_XML);
		ActionNode an = new ActionNode(rn);
		ActionRequest rq = an.getRequest();
		ActionResponse rs = new ActionResponse(an);

		switch (method) {
		case GET:
			an.setRQRS("Get", af, null, mimeTypes, RestStatusCodes.OK, rq.getTLModelObject(), rs.getTLModelObject());
			break;
		case POST:
			an.setRQRS("Create", af, null, mimeTypes, RestStatusCodes.CREATED, rq.getTLModelObject(),
					rs.getTLModelObject());
			break;
		case DELETE:
			an.setRQRS("Delete", af, null, mimeTypes, RestStatusCodes.OK, rq.getTLModelObject(), rs.getTLModelObject());
			break;
		case PUT:
			an.setRQRS("Update", af, null, mimeTypes, RestStatusCodes.OK, rq.getTLModelObject(), rs.getTLModelObject());
			break;
		default:
			break;
		}
		rq.setHttpMethod(method.toString());
		rq.setParamGroup(pg.getName()); // do here to set path template
		return an;
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