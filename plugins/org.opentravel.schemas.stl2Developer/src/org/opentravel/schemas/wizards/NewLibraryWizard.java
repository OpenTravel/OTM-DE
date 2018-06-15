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

import java.io.File;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.properties.Messages;

/**
 * @author Agnieszka Janowska
 * 
 */
@Deprecated
public class NewLibraryWizard extends ValidatingWizard implements Cancelable {

	private NewLibraryWizardPage page;
	private LibraryNode libraryNode;
	private String baseNS;
	private boolean canceled;

	/**
	 * Create a library node and return it complete with name and file path.
	 * 
	 * @param pn
	 *            - the namespace the user can extend for the library.
	 */
	public NewLibraryWizard(ProjectNode pn) {
		assert false; // 6/14/2018 dmh
		// TODO - delete this file and NewLibraryWizardPage.java
		//
		// // TLLibrary absTLLibrary = new TLLibrary();
		// this.libraryNode = new LibraryNode(pn);
		// baseNS = pn.getNamespace();
	}

	@Override
	public void addPages() {
		page = new NewLibraryWizardPage(Messages.getString("wizard.newLibrary.title"),
				Messages.getString("wizard.newLibrary.description"), libraryNode, baseNS, getValidator());
		addPage(page);
	}

	@Override
	public boolean performFinish() {
		canceled = false;
		final File file = new File(libraryNode.getPath());
		if (file.exists()) {
			return MessageDialog.openConfirm(getShell(), Messages.getString("wizard.newLibrary.fileOverwrite.title"),
					file.getAbsolutePath() + "wizard.newLibrary.fileOverwrite");
		}

		return true;
	}

	@Override
	public boolean performCancel() {
		canceled = true;
		libraryNode.delete();
		return true;
	}

	public void run(final Shell shell) {
		final WizardDialog dialog = new WizardDialog(shell, this);
		dialog.setPageSize(SWT.DEFAULT, 300);
		dialog.create();
		dialog.open();
	}

	@Override
	public boolean wasCanceled() {
		return canceled;
	}

	public LibraryNode getLibraryNode() {
		return libraryNode;
	}

	public void setLibraryNode(final LibraryNode libraryNode) {
		this.libraryNode = libraryNode;
	}

}
