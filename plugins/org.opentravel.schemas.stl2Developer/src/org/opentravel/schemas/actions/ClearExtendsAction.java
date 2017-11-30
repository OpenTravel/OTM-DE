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

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Text;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.interfaces.ExtensionOwner;
import org.opentravel.schemas.properties.Messages;
import org.opentravel.schemas.properties.StringProperties;
import org.opentravel.schemas.stl2developer.DialogUserNotifier;
import org.opentravel.schemas.stl2developer.MainWindow;

/**
 * Action that handles the selection and assignment of extensions for cores, business objects, operations, and extension
 * point facets.
 * 
 * @author S. Livezey
 */
public class ClearExtendsAction extends OtmAbstractAction {

	private Text extendsField;
	private Button clearButton;

	public ClearExtendsAction(MainWindow mainWindow, StringProperties props, Text extendsField, Button clearButton) {
		super(mainWindow, props);
		this.extendsField = extendsField;
		this.clearButton = clearButton;

		clearButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				run();
			}

			@Override
			public void widgetDefaultSelected(final SelectionEvent e) {
			}

		});
	}

	/**
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run() {
		boolean confirmClear = DialogUserNotifier.openConfirm(Messages.getString("OtmW.352"),
				Messages.getString("OtmW.353"));

		if (confirmClear) {
			Node n = mc.getSelectedNode_TypeView();
			if (n instanceof ExtensionOwner)
				((ExtensionOwner) n).setExtension(null);
			mc.refresh();
		}
	}

	/**
	 * @see org.eclipse.jface.action.Action#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		if (extendsField != null)
			extendsField.setEnabled(enabled);
		if (clearButton != null)
			clearButton.setEnabled(enabled);
	}

}
