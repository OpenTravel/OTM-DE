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
package org.opentravel.schemas.actions;

import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.properties.ExternalizedStringProperties;
import org.opentravel.schemas.properties.StringProperties;
import org.opentravel.schemas.stl2developer.MainWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dave Hollander
 * 
 */
public class CloneSelectedFacetNodesAction extends OtmAbstractAction implements IWithNodeAction {
	private static final Logger LOGGER = LoggerFactory.getLogger(CloneSelectedFacetNodesAction.class);
	private static StringProperties propDefault = new ExternalizedStringProperties("action.copy");

	public CloneSelectedFacetNodesAction() {
		super(propDefault);
	}

	public CloneSelectedFacetNodesAction(final MainWindow mainWindow, final StringProperties props) {
		super(mainWindow, props);
	}

	@Override
	public void run() {
		getMainController().cloneSelectedFacetNodes();
	}

	@Override
	public boolean isEnabled(Node node) {
		Node currentNode = mc.getSelectedNode_NavigatorView();
		if (!(currentNode instanceof ComponentNode))
			return false;
		if (currentNode.isEditable() && !currentNode.getLibrary().isPatchVersion()) {
			// LOGGER.debug(" copy Is enabled ");
			return false;
		} else
			return true;
		// return currentNode instanceof ComponentNode ? currentNode.isEditable() : false;
	}

}
