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
import org.opentravel.schemas.node.interfaces.ContextualFacetOwnerInterface;
import org.opentravel.schemas.node.typeProviders.ContextualFacetNode;
import org.opentravel.schemas.properties.StringProperties;
import org.opentravel.schemas.stl2developer.MainWindow;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.wizards.ExtensionSelectionWizard;
import org.opentravel.schemas.wizards.TypeSelectionWizard;

/**
 * Action that handles the selection and assignment of extensions for cores, business objects, operations, and extension
 * point facets.
 * 
 * @author S. Livezey
 */
public class ExtendsAction extends OtmAbstractAction {

	private Text extendsField;
	private Button extendsSelector;

	public ExtendsAction(MainWindow mainWindow, StringProperties props, Text extendsField, Button extendsSelector) {
		super(mainWindow, props);
		this.extendsField = extendsField;
		this.extendsSelector = extendsSelector;

		extendsSelector.addSelectionListener(new SelectionListener() {

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
		Node n = (Node) OtmRegistry.getTypeView().getCurrentNode();

		if (n instanceof ContextualFacetNode) {
			final TypeSelectionWizard wizard = new TypeSelectionWizard(n);
			if (wizard.run(OtmRegistry.getActiveShell())) {
				Node subject = wizard.getSelection();
				if (subject instanceof ContextualFacetOwnerInterface)
					((ContextualFacetNode) n).setOwner((ContextualFacetOwnerInterface) subject);
			}
			mc.refresh();
		} else if (n != null) {
			ExtensionSelectionWizard wizard = new ExtensionSelectionWizard(n);
			wizard.postExtensionSelectionWizard(OtmRegistry.getActiveShell());
		}
	}

	/**
	 * Explicitly controlled in FacetView
	 * 
	 * @see org.eclipse.jface.action.Action#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		if (extendsField != null)
			extendsField.setEnabled(enabled);
		if (extendsSelector != null)
			extendsSelector.setEnabled(enabled);
	}

}
