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
package org.opentravel.schemas.node;

import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dave Hollander
 * 
 */
public class NodeFinders {
	private static final Logger LOGGER = LoggerFactory.getLogger(NodeFinders.class);

	/**
	 * Find a node by name. NOTE - node could be a property that is the same as a object.
	 * 
	 * @return the first node that matches the Name and Namespace in the model starting from the root.
	 */
	public static Node findNodeByName(final String name, final String ns) {
		Node n = Node.getModelNode().findNode(name, ns);
		// if (n instanceof XsdNode)
		// n = ((XsdNode) n).getOtmModel();
		// LOGGER.debug("findNodeByName returning: "+n);
		return n;
	}

	/**
	 * @return the first node that matches the QName in the model starting from the root or null.
	 */
	public static Node findNodeByQName(final QName qname) {
		if (qname == null)
			return null;
		Node n = Node.getModelNode().findNode(qname.getLocalPart(), qname.getNamespaceURI());
		// LOGGER.debug("findNodeByQName ("+qname+") returning: "+n);
		return n;
	}

	/**
	 * @return the first node that matches the validationIdentity from tl model starting from the root.
	 */
	public static Node findNodeByValidationIentity(final String validationIdentity) {
		return Node.getModelNode().findNode(validationIdentity);
	}

	/**
	 * @return the first node that matches the unique node ID starting from the model root.
	 */
	public static Node findNodeByID(final String ID) {
		return Node.getModelNode().findNodeID(ID);
	}
}
