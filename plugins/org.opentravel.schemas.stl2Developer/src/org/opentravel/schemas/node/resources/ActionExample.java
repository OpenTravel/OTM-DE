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
 * Manages access to data for the Action Example URLs.
 * 
 * An example: GET http://example.com/basePath/{PathParam}?QueryParam=xxx&Q2=yyy <BO>...</BO>
 * 
 * @author Dave
 *
 */
public class ActionExample {
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(ActionExample.class);

	private final static String SYSTEM = "http://example.com";
	private ActionNode action;
	private String resourceBaseURL;

	public ActionExample(ActionNode action) {
		this.action = action;
		if (getResourceBaseURL().isEmpty())
			resourceBaseURL = SYSTEM;
		else
			resourceBaseURL = getResourceBaseURL();
	}

	public String getURL() {
		if (getMethod().isEmpty())
			return "";
		if (action == null || action.getRequest() == null)
			return "";
		return action.getRequest().getURL();
	}

	private String getResourceBaseURL() {
		final CompilerPreferences compilePreferences = new CompilerPreferences(
				CompilerPreferences.loadPreferenceStore());
		return compilePreferences.getResourceBaseUrl();
	}

	private String getMethod() {
		if (action.tlObj.getRequest() == null)
			return "";
		return action.tlObj.getRequest().getHttpMethod() != null ? action.tlObj.getRequest().getHttpMethod().toString()
				: "";
	}

	// private String getQueryTemplate() {
	// return action.getQueryTemplate();
	// }

	public String getLabel() {
		return action.tlObj.getActionId();
	}

	@Override
	public String toString() {
		return getLabel() + ": " + getURL();
	}

}
