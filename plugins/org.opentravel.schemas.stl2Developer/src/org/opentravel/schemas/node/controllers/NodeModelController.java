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
package org.opentravel.schemas.node.controllers;

import java.util.List;

/**
 * Controls structure (create, move, delete) of documentation underlying model on behalf of Node. Does NOT access
 * values. Values are access using {@link NodeValueController}.
 * 
 * @author Agnieszka Janowska
 * 
 */
public interface NodeModelController<T> {

	T createChild();

	void removeChild(T child);

	List<T> getChildren();

	T getChild(int index);

	T getChild(Object key);

	void moveChildUp(T child);

	void moveChildDown(T child);

}
