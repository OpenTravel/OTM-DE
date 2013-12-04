/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.wizards;

import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import com.sabre.schemas.node.ContextNode;

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
