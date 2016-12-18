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

import java.util.List;

import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.properties.StringProperties;
import org.opentravel.schemas.stl2developer.DialogUserNotifier;
import org.opentravel.schemas.stl2developer.FileDialogs;
import org.opentravel.schemas.stl2developer.MainWindow;

/**
 * @author Agnieszka Janowska
 * 
 */
//
// Uneeded functionality.
//
@Deprecated
public class SaveSelectedLibraryAsAction extends OtmAbstractAction {

	public static final String TOO_MANY_SELECTED_MSG = "Too many libraries selected";
	public static final String WARNING_MSG = "Warning";
	public static final String NO_VALID_SELECTION_MSG = "No valid selection";
	public static final String SELECT_EXACTLY_ONE_MSG = " - select exactly one user defined library";
	public static final String SELECT_AT_LEAST_ONE_MSG = " - select at least one library (cannot operate on built in libraries)";

	/**
	 *
	 */
	public SaveSelectedLibraryAsAction(final MainWindow mainWindow, final StringProperties props) {
		super(mainWindow, props);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run() {
		final MainWindow mainWindow = getMainWindow();
		final List<LibraryNode> libraries = mc.getSelectedUserLibraries();
		if (libraries.size() > 1) {
			DialogUserNotifier.openWarning(WARNING_MSG, TOO_MANY_SELECTED_MSG + SELECT_EXACTLY_ONE_MSG);
			return;
		}
		if (libraries.size() <= 0) {
			DialogUserNotifier.openWarning(WARNING_MSG, NO_VALID_SELECTION_MSG + SELECT_EXACTLY_ONE_MSG);
			return;
		}
		final LibraryNode library = libraries.get(0);

		final String fileName = FileDialogs.postFileSaveDialog();
		if (fileName == null || fileName.isEmpty()) {
			return;
		}
		library.setPath(fileName);
		mc.getLibraryController().saveLibrary(library, false);
	}

	@Override
	public boolean isEnabled() {
		Node n = mc.getSelectedNode_NavigatorView();
		return n != null ? !n.isBuiltIn() : false;
	}

}
