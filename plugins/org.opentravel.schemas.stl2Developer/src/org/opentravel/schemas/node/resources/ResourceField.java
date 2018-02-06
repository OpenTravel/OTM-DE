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

/**
 * Carries resource related information between the controllers and the GUI view. Defines generic resource fields types
 * for display and editing. Multiple constructors streamline controller code. Little or no data checking performed.
 * 
 * @author Dave
 *
 */
public class ResourceField {
	public enum ResourceFieldType {
		String, Int, Enum, List, EnumList, NodeList, CheckButton, ObjectSelect
	}

	public static final String NONE = "NONE";
	public static final String SUBGRP = "Substitution Group";
	public static final String ABSTRACT = "Abstract resources do not have business objects";

	String msgKey;
	String value;
	Object data;
	boolean enabled = true;

	ResourceFieldType type = ResourceFieldType.String; // default
	ResourceFieldListener listener = null;

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

	public ResourceField(List<ResourceField> parent, String value, String key, ResourceFieldType type,
			ResourceFieldListener listener, Object data) {
		this(parent, value, key, type, listener);
		this.data = data;
	}

	public ResourceField(List<ResourceField> parent, String value, String key, ResourceFieldType type, boolean enabled,
			ResourceFieldListener listener) {
		this(parent, value, key, type, listener);
		this.enabled = enabled;
	}

	public ResourceField(List<ResourceField> parent, String value, String key, ResourceFieldType type, boolean enabled,
			ResourceFieldListener listener, Object data) {
		this(parent, value, key, type, listener, data);
		this.enabled = enabled;
	}

	public ResourceField(String value, String key) {
		this.value = value;
		this.msgKey = key;
	}

	public Object getData() {
		return data;
	}

	public String getKey() {
		return msgKey;
	}

	public ResourceFieldListener getListener() {
		return listener;
	}

	public ResourceFieldType getType() {
		return type;
	}

	public String getValue() {
		return value;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public void setKey(String key) {
		msgKey = key;
	}

	public void setListener(ResourceFieldListener listener) {
		this.listener = listener;
	}

	public void setType(ResourceFieldType type) {
		this.type = type;
	}

	public void setType(String data) {
		type = ResourceFieldType.valueOf(data);
	}

	public void setValue(String value) {
		this.value = value;
	}

	public boolean isEnabled() {
		return enabled;
	}

}