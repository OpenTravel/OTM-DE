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
package org.opentravel.schemas.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.opentravel.schemas.node.Node;

/**
 * Manage finding and replacing assigned types including base types.
 * 
 * 3/10/2015 - UNUSED
 * 
 * NOTE: this class creates a map of type assignments which can get out of sync with the actual assignments.
 * 
 * @author Dave Hollander
 * 
 */
public class Assignments {
	protected ArrayList<Node> users = new ArrayList<Node>();

	private class Users {
		public Users(Node n) {
			users.add(n);
		}

		protected void add(Node n) {
			users.add(n);
		}
	}

	// Maps to hold the current model indexed by assignedType and assignedBase
	private static Map<QName, Users> typeMap = new HashMap<QName, Users>(Node.getNodeCount());
	private static Map<QName, Users> baseMap = new HashMap<QName, Users>(Node.getNodeCount());
	protected static boolean isDirty = true;

	public Assignments() {
		// Initialize the maps
		assert typeMap == null;
		initializeMaps();
	}

	private void initializeMaps() {
		isDirty = false;
		for (Node n : Node.getModelNode().getChildren()) {
			if (n.getTLTypeObject() != null) {
				QName qn = new QName(n.getTLTypeObject().getNamespace(), n.getTLTypeObject().getLocalName());
				if (typeMap.containsKey(qn)) {
					typeMap.get(qn).add(n);
				} else
					typeMap.put(qn, new Users(n));
			}
			if (n.getTLBaseType() != null) {
				QName qn = new QName(n.getTLTypeObject().getNamespace(), n.getTLTypeObject().getLocalName());
				if (typeMap.containsKey(qn)) {
					typeMap.get(qn).add(n);
				} else
					typeMap.put(qn, new Users(n));
			}
		}
	}
}
