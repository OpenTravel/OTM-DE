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

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.opentravel.schemas.controllers.DefaultRepositoryController;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.properties.Messages;

/**
 * Library orchestration - lock, commit, unlock, finalize, version
 * 
 * @author Dave Hollander
 * 
 */
public class LibraryOrchestrationWizard extends Wizard implements Cancelable {

	private LibraryOrchestrationWizardPage page;
	private LibraryNode ln;
	private DefaultRepositoryController rc;
	private boolean canceled;

	/**
	 * Create a library node and return it complete with name and file path.
	 * 
	 * @param library
	 *            to orchestrate
	 */
	public LibraryOrchestrationWizard(LibraryNode library, DefaultRepositoryController rc) {
		this.ln = library;
		this.rc = rc;
	}

	@Override
	public void addPages() {
		page = new LibraryOrchestrationWizardPage(Messages.getString("wizard.OrchestrateLibrary.title"),
				Messages.getString("wizard.OrchestrateLibrary.description"), ln, rc);
		addPage(page);
	}

	@Override
	public boolean performFinish() {
		canceled = false;
		page.go();
		return true;
	}

	@Override
	public boolean performCancel() {
		canceled = true;
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
		return ln;
	}

	public void setLibraryNode(final LibraryNode libraryNode) {
		this.ln = libraryNode;
	}

}
