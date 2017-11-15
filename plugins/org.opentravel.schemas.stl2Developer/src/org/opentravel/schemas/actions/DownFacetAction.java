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
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.properties.StringProperties;
import org.opentravel.schemas.stl2developer.MainWindow;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.views.TypeView;

/**
 * @author Dave Hollander
 * 
 */
public class DownFacetAction extends OtmAbstractAction {

	public DownFacetAction(final MainWindow mainWindow, final StringProperties props) {
		super(mainWindow, props);
	}

	@Override
	public void run() {
		final TypeView view = OtmRegistry.getTypeView();
		if (view != null) {
			for (final Node n : view.getSelectedNodes())
				if (n instanceof PropertyNode)
					((PropertyNode) n).moveDown();
			view.setFacetViewFocus(1);
		}
	}

	@Override
	public boolean isEnabled(Node currentNode) {
		if (currentNode == null)
			return false;
		if (!(currentNode instanceof PropertyNode))
			return false;
		return (currentNode.isEditable_newToChain());
	}

}
