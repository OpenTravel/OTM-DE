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

import org.eclipse.swt.widgets.Event;
import org.opentravel.schemas.commands.AddNodeHandler2;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.ServiceNode;
import org.opentravel.schemas.properties.ExternalizedStringProperties;
import org.opentravel.schemas.properties.StringProperties;
import org.opentravel.schemas.stl2developer.MainWindow;

/**
 * 
 */
public class AddOperationAction extends OtmAbstractAction {
	private static StringProperties propDefault = new ExternalizedStringProperties("action.addOperation");

	/**
	 *
	 */
	public AddOperationAction() {
		super(propDefault);
	}

	public AddOperationAction(final MainWindow mainWindow, final StringProperties props) {
		super(props);
	}

	@Override
	public void runWithEvent(Event event) {
		new AddNodeHandler2().execute(event);
	}

	@Override
	public boolean isEnabled() {
		Node n = getMainController().getCurrentNode_NavigatorView();
		return n instanceof ServiceNode ? n.isEnabled_AddProperties() : false;
	}
}
