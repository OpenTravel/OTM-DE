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
package org.opentravel.schemas.widgets;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.controllers.OtmActions;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.stl2developer.MainWindow;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link FacetViewTablePoster} decorated with 'Go To' buttons on types
 * 
 * @author Agnieszka Janowska
 * 
 */
public class LibraryTablePosterWithButtons extends FacetViewTablePoster {
	private static final Logger LOGGER = LoggerFactory.getLogger(LibraryTablePosterWithButtons.class);

	private final TableEditorButtonSet buttonSet;

	public LibraryTablePosterWithButtons(final Table table, final MainWindow mainWindow) {
		super(table, mainWindow.getColorProvider());
		MainController mc = OtmRegistry.getMainController();
		// Create a table editor button set to manage buttons added to the table.
		buttonSet = new TableEditorButtonSet(mc.getWidgets(), table, OtmActions.typeSelector(),
				OtmWidgets.typeSelector, mc.getHandlers().new ButtonSelectionHandler());
	}

	@Override
	protected TableItem postTableRow(final Node n, final boolean contributed) {
		if (contributed)
			return super.postTableRow(n, contributed);
		return postTableRow(n);
	}

	@Override
	protected TableItem postTableRow(final Node n) {
		final TableItem item = super.postTableRow(n, false);

		// Version Control - If the type has a newer version then allow that to be selected
		Button button = null;

		if (n.isEnabled_AssignType())
			button = buttonSet.addButton(n, item, 2); // Put typeSelection buttons on the row.

		return item;
	}

	@Override
	public void clearTable() {
		super.clearTable();
		buttonSet.clearButtons();
	}

}
