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
import org.opentravel.schemas.node.ContextNode;

/**
 * @author Agnieszka Janowska
 * 
 */
public class MergeContextNodeWizard extends ValidatingWizard implements Cancelable {

    private MergeContextNodeWizardPage page;
    private final ContextNode context;
    private boolean canceled;

    public MergeContextNodeWizard(ContextNode context) {
        this.context = context;
        setValidator(new MergeContextValidator(context, this));
    }

    @Override
    public void addPages() {
        page = new MergeContextNodeWizardPage("Merge Contexts", "Merge Contexts", context,
                getValidator());
        page.setDescription("Merge " + context.getContextId() + " ("
                + context.getApplicationContext() + ") into context selected below.");
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

    public ContextNode getContext() {
        return page.getContext();
    }

}
