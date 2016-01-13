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

import org.opentravel.schemacompiler.model.TLAction;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLActionRequest;
import org.opentravel.schemacompiler.model.TLActionResponse;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLParamGroup;
import org.opentravel.schemacompiler.model.TLParameter;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemas.node.BusinessObjectNode;

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

	public TLResource buildTL() {
		return buildTL(NAME);
	}

	public TLResource buildTL(String name) {
		TLResource resource = new TLResource();
		resource.setName(name);

		TLParamGroup params = new TLParamGroup();
		resource.addParamGroup(params);
		params.setName(name);
		TLParameter parameter = new TLParameter();
		params.addParameter(parameter);
		parameter.setFieldRefName(name);

		TLAction action = new TLAction();
		resource.addAction(action);
		action.setActionId(name);
		TLActionResponse response = new TLActionResponse();
		action.addResponse(response);
		response.setPayloadTypeName(name);
		TLActionRequest request = new TLActionRequest();
		action.setRequest(request);
		request.setPayloadTypeName(name);

		TLActionFacet facet = new TLActionFacet();
		resource.addActionFacet(facet);
		facet.setName(name);
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