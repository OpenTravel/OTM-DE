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

import org.eclipse.swt.widgets.Button;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.objectMembers.OperationFacetNode;
import org.opentravel.schemas.properties.StringProperties;
import org.opentravel.schemas.stl2developer.DialogUserNotifier;
import org.opentravel.schemas.stl2developer.MainWindow;
import org.opentravel.schemas.stl2developer.OtmRegistry;

/**
 * @author Dave Hollander
 * 
 */
public class ExtendableAction extends OtmAbstractAction {

	public ExtendableAction(final MainWindow mainWindow, final StringProperties props) {
		super(mainWindow, props, AS_CHECK_BOX);
	}

	public ExtendableAction(final MainWindow mainWindow, final StringProperties props, final Button check) {
		super(mainWindow, props, AS_CHECK_BOX);
	}

	@Override
	public void run() {
		Node node = mc.getSelectedNode_TypeView();
		Node nn = node;
		if (node != null && node.isEditable()) {
			nn = node.setExtensible(isChecked());
			mc.refresh(nn);
			OtmRegistry.getNavigatorView().refresh(); // refresh entire tree because content changed
		} else {
			setChecked(!node.isExtensible());
			DialogUserNotifier.openWarning("Warning", "Object is not editable.");
		}
	}

	@Override
	public boolean isEnabled(Node currentNode) {
		if (currentNode == null)
			return false;
		if (currentNode instanceof OperationFacetNode)
			return true;
		return (currentNode.isExtensibleObject());
	}

}
