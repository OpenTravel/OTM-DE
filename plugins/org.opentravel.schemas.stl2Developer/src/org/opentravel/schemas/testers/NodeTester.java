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
package org.opentravel.schemas.testers;

import org.eclipse.core.expressions.PropertyTester;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.typeProviders.ImpliedNode;
import org.opentravel.schemas.types.TypeUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeTester extends PropertyTester {
	private static final Logger LOGGER = LoggerFactory.getLogger(NodeTester.class);

	public static final String IS_DELETEABLE = "isDeleteable";
	public static final String HAS_TYPE = "hasType";
	public static final String IS_IN_TLLIBRARY = "isInTLLibrary";
	public static final String IS_EDITABLE = "isEditable";
	public static final String IS_OWNER_LIBRARY_EDITABLE = "isOwnerLibraryEditable";
	public static final String CAN_ASSIGN = "canAssign";

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {

		if (!(receiver instanceof Node)) {
			return false;
		}
		Node node = (Node) receiver;
		// LOGGER.debug("Testing " + property + " of " + node + " " + isOwnerLibraryEditable(node));

		if (IS_DELETEABLE.equals(property)) {
			return node.isDeleteable();
			// return canDelete(node);
		} else if (HAS_TYPE.equals(property)) {
			return hasType(node);
		} else if (IS_IN_TLLIBRARY.equals(property)) {
			return node.isInTLLibrary();
		} else if (IS_EDITABLE.equals(property)) {
			return node.isEditable();
		} else if (IS_OWNER_LIBRARY_EDITABLE.equals(property)) {
			return isOwnerLibraryEditable(node);
		} else if (CAN_ASSIGN.equals(property)) {
			if (args[0] instanceof Node)
				return canAssign(node, (Node) args[0]);
		}
		return false;
	}

	private boolean canDelete(Node node) {
		// if (node instanceof ContextualFacetNode) {
		// return !node.isInherited();
		// // return !NodeUtils.checker(node).isInheritedFacet().get();
		// } else {
		return node.isDeleteable();
		// }
	}

	private boolean isOwnerLibraryEditable(Node node) {
		return node.getChain() != null ? node.getChain().isEditable() : node.isEditable();
	}

	private boolean hasType(Node node) {
		if (node instanceof TypeUser)
			return ((TypeUser) node).getAssignedType() != null
					&& !(((TypeUser) node).getAssignedType() instanceof ImpliedNode);
		return false;
	}

	public boolean canAssign(Node property, Node type) {
		if (property == null || type == null)
			return false;
		return property.canAssign(type);
	}
}