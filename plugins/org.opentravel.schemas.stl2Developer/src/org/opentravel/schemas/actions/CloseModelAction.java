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

import org.opentravel.schemas.properties.ExternalizedStringProperties;
import org.opentravel.schemas.properties.StringProperties;
import org.opentravel.schemas.stl2developer.DialogUserNotifier;
import org.opentravel.schemas.stl2developer.MainWindow;

/**
 * 3/28/2015 dmh - NEVER USED.
 * 
 * @author Dave Hollander
 * 
 */
public class CloseModelAction extends OtmAbstractAction {
	private static StringProperties propDefault = new ExternalizedStringProperties("action.closeModel");

	public CloseModelAction() {
		super(propDefault);
	}

	public CloseModelAction(final MainWindow mainWindow, final StringProperties props) {
		super(mainWindow, props);
	}

	@Override
	public void run() {
		boolean okey = false;
		okey = DialogUserNotifier.openConfirm("Close Model", "Are you sure you want to close existing model? "
				+ "Closing will save and close all the currently open libraries");
		if (okey)
			mc.getModelController().close();
		mc.refresh();
	}
}
