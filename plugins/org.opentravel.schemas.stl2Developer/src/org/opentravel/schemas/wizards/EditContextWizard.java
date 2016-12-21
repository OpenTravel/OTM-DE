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

import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.opentravel.schemacompiler.model.TLContext;
import org.opentravel.schemacompiler.model.TLLibrary;

/**
 * @author Agnieszka Janowska
 * 
 */
public class EditContextWizard extends ValidatingWizard implements Cancelable {

	private EditContextWizardPage page;
	private final TLLibrary library;
	private final TLContext contextObject;
	private boolean canceled;

	// 12/21/2016 dmh - not used in plugin xml or called
	@Deprecated
	public EditContextWizard(final TLLibrary library, final TLContext context) {
		this.library = library;
		contextObject = context;
	}

	@Override
	public void addPages() {
		page = new EditContextWizardPage("Edit context", "Edit context properties", library, contextObject,
				getValidator());
		addPage(page);
	}

	@Override
	public boolean performFinish() {
		canceled = false;
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

	public TLContext getContext() {
		return contextObject;
	}

}
