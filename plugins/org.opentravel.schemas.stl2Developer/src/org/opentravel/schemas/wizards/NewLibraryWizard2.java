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
import org.opentravel.schemas.node.handlers.NamespaceHandler;
import org.opentravel.schemas.properties.Messages;
import org.opentravel.schemas.wizards.validators.FormValidator;
import org.opentravel.schemas.wizards.validators.NewLibraryValidator2;

/**
 * Wizard to create a new library.
 * <p>
 * Version 2 of this wizard does not use a library to run the wizard, only on completion.
 * 
 * @author Dave Hollander
 * 
 */
public class NewLibraryWizard2 extends ValidatingWizard implements Cancelable {
	public class LibraryMetaData {
		private String baseNS;
		private String comments;
		private String name;
		private String namespace;
		private NamespaceHandler nsHandler;
		private String nsPrefix;
		private String path = "";
		private FormValidator validator;

		public String getBaseNS() {
			return baseNS;
		}

		public void setBaseNS(String baseNS) {
			this.baseNS = baseNS;
		}

		public String getComments() {
			return comments;
		}

		public String getName() {
			return name;
		}

		public String getNamespace() {
			return namespace;
		}

		public NamespaceHandler getNsHandler() {
			return nsHandler;
		}

		public String getPath() {
			return path;
		}

		public void setComments(String comments) {
			this.comments = comments;
		}

		public void setName(String name) {
			this.name = name;
		}

		public void setNamespace(String namespace) {
			this.namespace = namespace;
		}

		public void setNsHandler(NamespaceHandler nsHandler) {
			this.nsHandler = nsHandler;
		}

		public void setNSPrefix(String text) {
			this.nsPrefix = text;
		}

		public String getNsPrefix() {
			return nsPrefix;
		}

		public void setNsPrefix(String nsPrefix) {
			this.nsPrefix = nsPrefix;
		}

		public void setPath(String path) {
			this.path = path;
		}

		public FormValidator getValidator() {
			return validator;
		}

		public void setValidator(FormValidator validator) {
			this.validator = validator;
		}
	}

	// Inherits FormValidator with getter and setter
	private NewLibraryWizardPage2 page;
	private boolean canceled;
	private LibraryMetaData metaData = new LibraryMetaData();

	/**
	 * Create a library node and return it complete with name and file path.
	 * 
	 * @param pn
	 *            - the namespace the user can extend for the library.
	 */
	public NewLibraryWizard2(ProjectNode pn) {
		metaData.setBaseNS(pn.getNamespace());
		metaData.setNsHandler(NamespaceHandler.getNamespaceHandler(pn));
		setValidator(new NewLibraryValidator2(metaData, pn.getNamespace()));
		metaData.setValidator(getValidator());

	}

	@Override
	public void addPages() {
		page = new NewLibraryWizardPage2(Messages.getString("wizard.newLibrary.title"),
				Messages.getString("wizard.newLibrary.description"), metaData);
		addPage(page);
	}

	@Override
	public boolean performFinish() {
		canceled = false;
		final File file = new File(metaData.getPath());
		if (file.exists()) {
			return MessageDialog.openConfirm(getShell(), Messages.getString("wizard.newLibrary.fileOverwrite.title"),
					file.getAbsolutePath() + "wizard.newLibrary.fileOverwrite");
		}

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

	/**
	 * @return the data saved in the library meta data .
	 */
	public LibraryMetaData getLibraryMetaData() {
		return metaData;
	}

}
