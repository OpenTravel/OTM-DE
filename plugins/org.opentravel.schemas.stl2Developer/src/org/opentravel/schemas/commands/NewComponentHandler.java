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
package org.opentravel.schemas.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.node.EditNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.stl2developer.DialogUserNotifier;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.wizards.NewComponentWizard;

/**
 * Verify library state allows new objects then run new component wizard to create a new library member.
 * {@link NewComponentWizard}
 * 
 * @author Dave Hollander
 * 
 */
public class NewComponentHandler extends AbstractHandler {

	public static final String COMMAND_ID = "org.opentravel.schemas.commands.newComponent";

	// private static final Logger LOGGER = LoggerFactory.getLogger(NewComponentHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		newToLibrary();
		return null;
	}

	/**
	 * Runs new component wizard and creates new node with TL type. Used by New Complex Type Action handler.
	 */
	public void newToLibrary() {
		MainController mc = OtmRegistry.getMainController();
		// LOGGER.debug("Adding new component to library");

		final Node selected = mc.getSelectedNode_NavigatorView();

		if (selected == null) {
			DialogUserNotifier.openInformation("WARNING", "You must select a library to add to.");
			return;
		}
		if (!selected.isInTLLibrary()) {
			DialogUserNotifier.openInformation("WARNING", "You can not add to this library.");
			return;
		}
		if (selected.getLibrary() == null) {
			DialogUserNotifier.openInformation("WARNING", "You can not add to this library.");
			return;
		}
		if (!selected.isEditable()) {
			DialogUserNotifier.openInformation("WARNING", "Library is not editable. You can not add to this library.");
			return;
		}

		final NewComponentWizard wizard = new NewComponentWizard(selected);
		Node newOne = wizard.run(OtmRegistry.getActiveShell());

		EditNode editNode = null;

		// editNode = wizard.run(OtmRegistry.getActiveShell());
		// if (editNode != null) {
		// ComponentNodeType type = ComponentNodeType.fromString(editNode.getUseType());
		//
		// Node newOne = new NodeFactory().newComponent(editNode, type);
		// newOne.setName(NodeNameUtils.fixComplexTypeName(newOne.getName()));
		// Assert.notNull(newOne.getLibrary());

		// If they created a service and selected an object, then build CRUD operations for that object.
		// if (editNode.getTLType() != null && newOne instanceof ServiceNode)
		// ((ServiceNode) newOne).addCRUDQ_Operations(editNode.getTLType());
		mc.selectNavigatorNodeAndRefresh(newOne);

		// Edit node is NOT in the library so there is no need to remove it
		// assert (!editNode.getLibrary().contains(editNode));
		// }
	}
}
