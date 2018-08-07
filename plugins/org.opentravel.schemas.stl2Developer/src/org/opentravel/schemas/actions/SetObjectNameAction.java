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
import org.eclipse.swt.widgets.Text;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.properties.ExternalizedStringProperties;
import org.opentravel.schemas.properties.StringProperties;
import org.opentravel.schemas.stl2developer.MainWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a prototype for migrating actions out of the OtmActions. At this point, i use OtmActions because it has the
 * text handlers. TODO - redo text handlers to support direct action dispatch.
 * 
 * @author Dave Hollander
 * 
 */
public class SetObjectNameAction extends OtmAbstractAction {
	private static final Logger LOGGER = LoggerFactory.getLogger(SetObjectNameAction.class);

	private final static StringProperties propsDefault = new ExternalizedStringProperties("action.setName");

	public SetObjectNameAction(final MainWindow mainWindow) {
		super(mainWindow, propsDefault);
	}

	/**
	 *
	 */
	public SetObjectNameAction(final MainWindow mainWindow, final StringProperties props) {
		super(mainWindow, props);
	}

	@Override
	public void runWithEvent(Event event) {
		String newName = "";
		if (event.widget instanceof Text)
			newName = ((Text) event.widget).getText();

		Node n = null;
		if (event.data instanceof Node)
			n = (Node) event.data;

		if (n != null && !newName.isEmpty()) {
			n.setName(newName); // setName will assure they are correct
			getMainController().refresh();
			// LOGGER.debug("Changed name to " + n);
		}
	}
}
