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
