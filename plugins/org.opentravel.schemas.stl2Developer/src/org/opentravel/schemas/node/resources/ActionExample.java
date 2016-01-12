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

import org.opentravel.schemacompiler.event.ValueChangeEvent;
import org.opentravel.schemacompiler.model.TLAction;
import org.opentravel.schemacompiler.model.TLHttpMethod;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.listeners.BaseNodeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maintains data for the Action Example URLs. Provides listener to keep example current. Listener should be created
 * with NULL node.
 * 
 * An example: GET http://example.com/basePath/{PathParam}?QueryParam=xxx&Q2=yyy <BO>...</BO>
 * 
 * @author Dave
 *
 */
public class ActionExample {
	private static final Logger LOGGER = LoggerFactory.getLogger(ActionExample.class);

	private final static String system = "http://example.com";
	private TLHttpMethod method;
	private String basePath;
	private String template;
	private String payload = "";
	private String label;
	private TLAction action;

	public ActionExample(TLAction action) {
		this.action = action;
		setValues();

		// DON'T set listeners here ...
		// there is no control over order and the examples might get updated before the data changes
	}

	protected void setValues() {
		label = action.getActionId();
		if (action.getRequest() != null) {
			method = action.getRequest().getHttpMethod();
			template = action.getRequest().getPathTemplate();
			payload = action.getRequest().getPayloadTypeName();
			if (payload == null)
				payload = "";
		}
		if (action.getOwner() != null) {
			basePath = action.getOwner().getBasePath();
		}
	}

	public String getPayloadExample() {
		return !payload.isEmpty() ? "<" + payload + ">...</" + payload + ">" : "";
	}

	public String getLabel() {
		return label;
	}

	public String getURL() {
		if (method == null)
			return "";
		return method + " " + system + basePath + template + "  " + getPayloadExample();
	}

	@Override
	public String toString() {
		return label + ": " + getURL();
	}

	public class ActionExampleListener extends BaseNodeListener {

		/**
		 * Update example when a field changes. Can't be used to identify node.
		 */
		public ActionExampleListener() {
			super(null);
		}

		private ActionExampleListener(Node node) {
			super(node);
		}

		@Override
		public void processValueChangeEvent(ValueChangeEvent<?, ?> event) {
			super.processValueChangeEvent(event);
			LOGGER.debug("Value change event: " + event.getType() + " on "
					+ event.getSource().getClass().getSimpleName());
			switch (event.getType()) {
			case PARAM_GROUP_ADDED:
			case PARAM_GROUP_MODIFIED:
			case PARAM_GROUP_REMOVED:
			case PARAMETER_ADDED:
			case PARAMETER_REMOVED:
			case PARENT_PARAM_GROUP_MODIFIED:
				// TODO - update path template
			case PAYLOAD_TYPE_MODIFIED:
			case HTTP_METHOD_MODIFIED:
			case FACET_REF_MODIFIED:
			case BASE_PATH_MODIFIED:
				setValues();
				break;
			default:
				break;
			}
		}
	}
}
