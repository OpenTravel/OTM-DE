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

import java.util.List;

public class ResourceField {
	String msgKey;
	String value;
	Object data;
	ResourceFieldType type = ResourceFieldType.String; // default
	ResourceFieldListener listener = null;

	public static final String NONE = "NONE";

	public enum ResourceFieldType {
		String, Int, Enum, List, EnumList, NodeList, CheckButton
	};

	public ResourceField() {
	}

	public ResourceField(List<ResourceField> parent, String value, String key, ResourceFieldType type,
			ResourceFieldListener listener) {
		this.value = value;
		this.msgKey = key;
		this.type = type;
		this.listener = listener;
		parent.add(this);
	}

	public ResourceField(String value, String key) {
		this.value = value;
		this.msgKey = key;
	}

	public String getValue() {
		return value;
	}

	public String getKey() {
		return msgKey;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public void setKey(String key) {
		msgKey = key;
	}

	public void setType(String data) {
		type = ResourceFieldType.valueOf(data);
		ResourceFieldType[] x = ResourceFieldType.values();
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public ResourceFieldType getType() {
		return type;
	}

	public void setListener(ResourceFieldListener listener) {
		this.listener = listener;
	}

	public ResourceFieldListener getListener() {
		return listener;
	}

	public void setType(ResourceFieldType type) {
		this.type = type;
	}

}