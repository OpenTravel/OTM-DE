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
import org.opentravel.schemas.node.VersionNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * These tests are available to the plugin.xml for enabling commands.
 * 
 * @author Dave
 *
 */
public class GlobalSelectionTester extends PropertyTester {
	private static final Logger LOGGER = LoggerFactory.getLogger(GlobalSelectionTester.class);

	public static final String CANADD = "canAdd";
	public static final String NewToChain = "newToChain";
	private static final String NAMESPACE = "stl2Developer.selection";

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (receiver == null || !(receiver instanceof Node))
			return false;
		Node node = (Node) receiver;
		if (node instanceof VersionNode)
			node = ((VersionNode) node).get();

		boolean result = false;

		// LOGGER.debug("Property = " + property + "  add = " + add + "  new = " + newTo);
		switch (property) {
		case CANADD:
			result = node.isEnabled_AddProperties();
			break;
		case NewToChain:
			result = node.isEditable_newToChain();
			break;
		default:
			result = false;
		}

		// LOGGER.debug(result + " = test of " + property + " on " + node);

		return result;
	}

	/**
	 * @return The fully qualified property name, NAMESPACE + property name.
	 */
	public static String getFullName(String property) {
		return NAMESPACE + "." + property;
	}

}
