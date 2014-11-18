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
import org.opentravel.schemas.node.ExtensionPointNode;
import org.opentravel.schemas.node.LibraryChainNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeEditStatus;
import org.opentravel.schemas.node.OperationNode;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.node.ServiceNode;
import org.opentravel.schemas.node.properties.SimpleAttributeNode;

public class GlobalSelectionTester extends PropertyTester {

	public static final String CANADD = "canAdd";
	private static final String NAMESPACE = "stl2Developer.selection";

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (CANADD.equals(property)) {
			return canAdd((Node) receiver);
		}
		return false;
	}

	private boolean canAdd(Node node) {
		if (node == null || node.getParent() == null)
			return false;
		if (node.isDeleted() || !node.isEditable())
			return false;
		if (node instanceof ProjectNode)
			return false;
		if (node instanceof LibraryChainNode)
			return false;
		if (node instanceof OperationNode)
			return false;

		// Can we add children?
		// Disabled until we can test compiler and otm-b to assure support.
		if (node instanceof ExtensionPointNode) {
			return node.isNewToChain();
		}
		if (node.isBusinessObject() || node.isCoreObject() || node.isExtensionPointFacet()) {
			return true; // always can have properties added.
		}
		if (node.isValueWithAttributes() || node.isEnumeration()) {
			if (node.getEditStatus().equals(NodeEditStatus.FULL) || node.getEditStatus().equals(NodeEditStatus.MINOR))
				return true;
			else
				return false;
		}

		if (node.isMessage()) {
			if (node.getEditStatus().equals(NodeEditStatus.FULL))
				return true;
			else
				return false;
		}
		if (node instanceof ServiceNode) {
			// see also AddOperationAction.isEnabled()
			if (node.getEditStatus().equals(NodeEditStatus.FULL))
				return true;
			if (node.getEditStatus().equals(NodeEditStatus.MINOR))
				return true;
			return false;
		}

		// Facets. Unless a simple facet, same as parent.
		if (node.isSimpleFacet())
			return false;
		if (node.isListFacet())
			return false;
		if (node.isRoleFacet() && !node.getEditStatus().equals(NodeEditStatus.FULL))
			return false;
		if (node.isFacet())
			return canAdd(node.getParent());

		// Can we add siblings?
		if (node instanceof SimpleAttributeNode)
			return false;
		if (node.isRoleProperty() && !node.getEditStatus().equals(NodeEditStatus.FULL))
			return false;
		if (node.isProperty())
			if (node == node.getOwningComponent())
				return false; // ERROR
			else
				return canAdd(node.getOwningComponent());

		return false;
	}

	/**
	 * @return The fully qualified property name, NAMESPACE + property name.
	 */
	public static String getFullName(String property) {
		return NAMESPACE + "." + property;
	}

}
