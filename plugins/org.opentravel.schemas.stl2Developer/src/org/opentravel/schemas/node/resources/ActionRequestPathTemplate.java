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

import org.opentravel.schemas.preferences.CompilerPreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements ActionRequest path template methods.
 * 
 * @author Dave
 *
 */
class ActionRequestPathTemplate {
	private static final Logger LOGGER = LoggerFactory.getLogger(ActionRequestPathTemplate.class);

	private String collection = "";
	private String query = "";
	private String pathParam = "";
	private ActionRequest owner;
	private final static String SYSTEM = "http://example.com";

	public ActionRequestPathTemplate(ActionRequest request) {
		owner = request;
		if (owner == null)
			return;

		if (request.getTLModelObject().getPathTemplate() == null
				|| request.getTLModelObject().getPathTemplate().isEmpty()) {
			collection = "/" + owner.getOwningComponent().getSubjectName();
			if (!collection.endsWith("s"))
				collection += "s";
		} else
			override(request.getTLModelObject().getPathTemplate());

		if (owner.getParamGroup() != null) {
			pathParam = owner.getParamGroup().getPathTemplate();
			query = owner.getParamGroup().getQueryTemplate();
		}
	}

	public String getCollection() {
		return collection;
	}

	public void setCollection(String base) {
		this.collection = base;
		assert (!base.contains("?"));
	}

	public void setParameters() {
		if (owner.getParamGroup() != null) {
			pathParam = owner.getParamGroup().getPathTemplate();
			query = owner.getParamGroup().getQueryTemplate();
		} else {
			pathParam = "";
			query = "";
		}
	}

	public void override(String value) {
		// Strip off anything after the first parameter's {
		value = value.trim();
		if (value.contains("{"))
			value = value.substring(0, value.indexOf("{")).trim();
		if (value.endsWith("/"))
			value = value.substring(0, value.length() - 1);
		if (!value.startsWith("/"))
			value = "/" + value;
		query = "";
		collection = value;
	}

	/**
	 * @return the collection name if any and path parameters
	 */
	public String get() {
		setParameters();
		String tmp = collection;
		if (!pathParam.isEmpty())
			tmp += pathParam;
		if (tmp.contains("//"))
			tmp = tmp.replaceAll("//", "/");
		return tmp;
	}

	private ResourceNode getResource() {
		if (owner != null)
			if (owner.getParent() != null)
				if (owner.getParent().getParent() instanceof ResourceNode)
					return (ResourceNode) owner.getParent().getParent();
		return null;
	}

	public String getURL() {
		String url = "";

		// Assure base path starts with slash and ends without slash
		String basePath = getResource().getBasePath();
		if (!basePath.startsWith("/"))
			basePath = "/" + basePath;
		if (url.endsWith("/"))
			url = url.substring(0, url.length() - 1);

		String thisPart = get();
		String parentPart = owner.getParent().getParentContribution();

		// contribution can be just this part
		if (thisPart.equals(parentPart))
			parentPart = "";

		// Add contribution from ParentRef
		if (parentPart != null && !parentPart.isEmpty())
			url += parentPart;
		if (!(url.endsWith("/") || thisPart.startsWith("/")))
			url += "/";
		if ((url.endsWith("/") && thisPart.startsWith("/")))
			url = url.substring(0, url.length() - 1);

		// Add this base path if any
		url += basePath;

		// Add contribution from this resource
		url += thisPart;
		if (!query.isEmpty())
			url += query;
		url += getPayloadExample();
		url = url.replaceAll("//", "/");
		// allow // before system name
		url = getMethod() + " " + getResourceBaseURL() + url;
		return url;
	}

	private String getResourceBaseURL() {
		String resourceBaseURL;
		final CompilerPreferences compilePreferences = new CompilerPreferences(
				CompilerPreferences.loadPreferenceStore());
		resourceBaseURL = compilePreferences.getResourceBaseUrl();
		// In junits the resource base URL will be empty
		if (resourceBaseURL.isEmpty())
			resourceBaseURL = SYSTEM;
		return resourceBaseURL;
	}

	private String getMethod() {
		return owner.getHttpMethodAsString();
	}

	private String getPayloadExample() {
		String payload = owner.getTLModelObject().getPayloadTypeName();
		if (payload == null)
			payload = "";
		return !payload.isEmpty() ? " <" + payload + ">...</" + payload + ">" : "";
	}

}
