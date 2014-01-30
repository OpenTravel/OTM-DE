/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemas.wizards;

import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Shell;
import org.opentravel.schemas.node.Node.DocTypes;

/**
 * Allows setting multiple documentation fields.
 * 
 * @author Dave Hollander
 * 
 */
public class SetDocumentationWizard extends ValidatingWizard implements ModifyListener, Cancelable {

    private SetDocumentationWizardPage page;
    private boolean canceled;

    private String docText;
    private DocTypes docType;
    private String props = "wizard.setDocumentation.Page";

    /**
     * Wizard to enter count docText.
     * 
     * @param cnt
     */
    public SetDocumentationWizard() {
    }

    public SetDocumentationWizard(String props) {
        this.props = props;
    }

    @Override
    public void addPages() {
        page = new SetDocumentationWizardPage(props);
        page.addModifyListener(this);
        addPage(page);
    }

    @Override
    public boolean performFinish() {
        canceled = false;
        docText = page.getDocText();
        docType = page.getDocType();
        return true;
    }

    @Override
    public boolean performCancel() {
        canceled = true;
        return true;
    }

    public void run(final Shell shell) {
        final WizardDialog dialog = new WizardDialog(shell, this);
        dialog.setPageSize(SWT.DEFAULT, 150);
        dialog.create();
        dialog.open();
    }

    /**
     * @return the text
     */
    public String getDocText() {
        return docText;
    }

    /**
     * @return the type of documentation
     */
    public DocTypes getDocType() {
        return docType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
     */
    @Override
    public void modifyText(final ModifyEvent e) {
        docText = page.getDocText();
    }

    /**
     * @return
     */
    @Override
    public boolean wasCanceled() {
        return canceled;
    }

}
