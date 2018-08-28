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

import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.properties.StringProperties;
import org.opentravel.schemas.stl2developer.MainWindow;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.views.DocumentationView;

/**
 * @author Agnieszka Janowska
 * 
 */
public class PrevDocItemAction extends OtmAbstractAction {

	/**
	 *
	 */
	public PrevDocItemAction(final MainWindow mainWindow, final StringProperties props) {
		super(mainWindow, props);
	}

	@Override
	public void run() {
		Node selected = mc.getSelectedNode_TypeView();
		// if (selected instanceof PropertyNode)
		DocumentationView view = OtmRegistry.getDocumentationView();
		if (view != null) {
			view.prevDocItem();
		}
	}

	@Override
	public boolean isEnabled() {
		Node selected = mc.getSelectedNode_TypeView();
		// Get the next node from the typeView tablePoster since it sorted them
		return true;
	}

}
