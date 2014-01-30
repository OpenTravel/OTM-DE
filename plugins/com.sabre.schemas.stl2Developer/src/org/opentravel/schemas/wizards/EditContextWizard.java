/*
 * Copyright (c) 2011, Sabre Inc.
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

    public EditContextWizard(final TLLibrary library, final TLContext context) {
        this.library = library;
        contextObject = context;
    }

    @Override
    public void addPages() {
        page = new EditContextWizardPage("Edit context", "Edit context properties", library,
                contextObject, getValidator());
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
