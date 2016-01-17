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

// http://www.restapitutorial.com/httpstatuscodes.html
public enum RestStatusCodes {
	OK(200), CREATED(201), BadRequest(400), Unauthorized(401), PaymentRequired(402), Forbidden(403), NotFound(404), InternalServerError(
			500);

	private final Integer value;

	RestStatusCodes(Integer value) {
		this.value = value;
	}

	public Integer value() {
		return value;
	}

	public static String getLabel(Integer value) {
		for (RestStatusCodes label : RestStatusCodes.values())
			if (label.value == value)
				return label.toString();
		return "";
	}

	public static String[] getCodes() {
		int i = 0;
		String[] codes = new String[RestStatusCodes.values().length];
		for (RestStatusCodes code : RestStatusCodes.values())
			codes[i++] = code.toString();
		return codes;
	}
}
