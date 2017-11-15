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
package org.opentravel.schemas.types;

import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemas.node.Node;

/**
 * Interface for assignment handlers.
 * 
 * @author Dave
 *
 */
public interface AssignmentHandler<T> {

	public TLModelElement getAssignedTLModelElement();

	public NamedEntity getTLAssignedNamedEntity();

	public T get();

	//
	// // Nearly but not all ExtensionOwers will also be TypeProviders
	// public boolean set(T provider);

	public Node getOwner();
}
