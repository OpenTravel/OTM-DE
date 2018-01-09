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
package org.opentravel.schemas.wizards;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.interfaces.ExtensionOwner;
import org.opentravel.schemas.node.objectMembers.ExtensionPointNode;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.trees.type.ExtensionTreeContentProvider;
import org.opentravel.schemas.trees.type.TypeTreeExtensionSelectionFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wizard that allows the user to select an entity from which the current entity will extend.
 * 
 * 
 * @author S. Livezey
 */
// TODO - can this be combined with the TypeSelectionWizard?

public class ExtensionSelectionWizard extends Wizard implements IDoubleClickListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionSelectionWizard.class);

	private final Node curNode;
	private TypeSelectionPage selectionPage;
	// private ExtensionInheritancePage inheritancePage;
	private WizardDialog dialog;

	public ExtensionSelectionWizard(final Node n) {
		// Same as type selection
		super();
		curNode = n;
	}

	@Override
	public void addPages() {
		// LOGGER.debug("add page - curNode is: "+curNode);
		if (curNode instanceof ExtensionPointNode)
			selectionPage = new TypeSelectionPage("Extension Point Selection", "Select Facet to Extend",
					"Select a facet from a different namespace that this extension point will extend.", null, curNode);
		else
			selectionPage = new TypeSelectionPage("Extension Selection", "Select Extension",
					"Select an entity from which the selected type will extend.", null, curNode);

		selectionPage.addDoubleClickListener(this);
		selectionPage.setTypeSelectionFilter(new TypeTreeExtensionSelectionFilter(curNode));
		selectionPage.setTypeTreeContentProvider(new ExtensionTreeContentProvider());
		addPage(selectionPage);

		// // Additional page needed for extensions.
		// if (!(curNode instanceof ExtensionPointNode)) {
		// inheritancePage = new ExtensionInheritancePage(
		// "Inherited Fields",
		// "Inherited Fields",
		// "Select the desired member of the inheritance hierarchy for each of the properties displayed below.",
		// curNode);
		// selectionPage.setTypeSelectionListener(inheritancePage);
		// addPage(inheritancePage);
		// }
		// else
		// LOGGER.debug("curNode is an extension point facet ... so skip the inheritance page.");
	}

	@Override
	public boolean canFinish() {
		// Same as type selection
		return selectionPage.getSelectedNode() == null ? false : true;
	}

	@Override
	public boolean performFinish() {
		if (curNode != null && curNode instanceof ExtensionOwner) {
			((ExtensionOwner) curNode).setExtension(selectionPage.getSelectedNode());
			// curNode.setExtendsType(selectionPage.getSelectedNode());
			// if (!(curNode instanceof ExtensionPointNode))
			// inheritancePage.doPerformFinish();
			OtmRegistry.getMainController().refresh();
		}
		return true;
	}

	public boolean postExtensionSelectionWizard(final Shell shell) {
		// Same as type selection
		if (curNode == null) {
			return false; // DO Nothing
		}

		dialog = new WizardDialog(shell, this);
		dialog.setPageSize(700, 600);
		dialog.create();
		dialog.open();

		return true;
	}

	@Override
	public void doubleClick(final DoubleClickEvent event) {
		// Same as type selection
		if (canFinish()) {
			performFinish();
			dialog.close();
		}
	}

}
