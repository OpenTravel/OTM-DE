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

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.VersionAggregateNode;
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
public class NewComponentHandler extends OtmAbstractHandler {
	// private static final Logger LOGGER = LoggerFactory.getLogger(NewComponentHandler.class);

	public static final String COMMAND_ID = "org.opentravel.schemas.commands.newComponent";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		newToLibrary();
		return null;
	}

	// dmh - 3/23/2018 - moved control here and commented out section in plugin.xml
	@Override
	public boolean isEnabled() {
		Node selected = getFirstSelected();
		if (selected == null)
			return false;
		if (selected instanceof VersionAggregateNode)
			return false;
		// is in library and library is editable
		return selected.isInTLLibrary() && selected.getLibrary().isEditable();
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

		mc.selectNavigatorNodeAndRefresh(newOne);
	}
}
