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
import org.opentravel.schemas.node.interfaces.Enumeration;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.typeProviders.ChoiceObjectNode;
import org.opentravel.schemas.properties.StringProperties;
import org.opentravel.schemas.stl2developer.MainWindow;

/**
 * Attached to the Facet View button bar.
 * 
 * @author Dave Hollander
 * 
 */
public class ChangeObjectAction extends OtmAbstractAction {

	private ChangeActionController controller;

	public ChangeObjectAction(final MainWindow mainWindow, final StringProperties props) {
		super(mainWindow, props);
		controller = new ChangeActionController();
	}

	@Override
	public void run() {
		// getMainController().changeSelection();
		Node selected = mc.getSelectedNode_TypeView();
		if (selected != null) {
			// final ComponentNode n = (ComponentNode) selected.getOwningComponent();
			// if (n != null) {
			selected = controller.runWizard(selected.getOwningComponent());
			// mc.changeNode(n);
			// }
		}
		mc.refresh();
		mc.setCurrentNode_TypeView(selected);
	}

	@Override
	public boolean isEnabled(Node currentNode) {
		if (currentNode == null)
			return false;
		if (currentNode.isDeleted())
			return false;
		if (!(currentNode instanceof ComponentNode))
			return false;
		if (currentNode.isEditable_inService())
			return false;

		LibraryMemberInterface owner = currentNode.getOwningComponent();
		if (owner == null)
			return false;
		if (owner instanceof ChoiceObjectNode)
			return false;
		if (owner instanceof Enumeration)
			return false;

		return owner.isEditable_newToChain();
	}
}
