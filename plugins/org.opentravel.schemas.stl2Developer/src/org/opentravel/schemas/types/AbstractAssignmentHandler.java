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
/**
 * 
 */
package org.opentravel.schemas.types;

import org.opentravel.schemas.node.ImpliedNode;
import org.opentravel.schemas.node.ImpliedNodeType;
import org.opentravel.schemas.node.interfaces.INode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles type assignment. The handler applies behavior and does not maintain its data except for the back link.
 * Subclasses are expected to provide set() method.
 * 
 * @author Dave Hollander
 * 
 */
public abstract class AbstractAssignmentHandler<T> implements AssignmentHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractAssignmentHandler.class);

	public AbstractAssignmentHandler() {
	}

	// /**
	// * @return the Node used in type assignments to the owner of this type class or unassigned node. Will never return
	// * null.
	// */
	// @SuppressWarnings("unchecked")
	// public T get() {
	// if (getTLModelElement() == null)
	// if (getOwner().getXsdNode() != null)
	// return (T) ModelNode.getUndefinedNode();
	// else
	// return (T) ModelNode.getUnassignedNode();
	//
	// Node n = Node.GetNode(getTLModelElement());
	// if (n == null)
	// LOGGER.debug("get(); of assigned type is null. Perhaps containing library has not been loaded and modeled yet.");
	// // throw new IllegalStateException("TypeUser.get() is null.");
	// return (T) n;
	// }

	public String getName() {
		// FIXME - TESTME
		// For implied nodes, use the name they provide.
		if (get() instanceof ImpliedNode) {
			ImpliedNode in = (ImpliedNode) get();
			String name = in.getImpliedType().getImpliedNodeType();
			// If the implied node is a union, add that to its assigned name
			if (in.getImpliedType().equals(ImpliedNodeType.Union))
				throw new IllegalStateException("How to handle getTypeName() for unions?");
			// name += ": " + XsdModelingUtils.getAssignedXsdUnion(this);
		}
		return (INode) get() != null ? ((INode) get()).getName() : "";
	}

	// public QName getQName() {
	// QName typeQname = null;
	//
	// NamedEntity type = getTLNamedEntity();
	// if (type != null) {
	// String ns = type.getNamespace();
	// String ln = type.getLocalName();
	// if (ns != null && ln != null)
	// typeQname = new QName(type.getNamespace(), type.getLocalName());
	//
	// // If empty, try the XSD type information in the documentation
	// if (typeQname == null)
	// typeQname = XsdModelingUtils.getAssignedXsdType((Node) getOwner());
	//
	// }
	// return typeQname;
	//
	// }

}
