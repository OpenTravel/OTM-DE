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
import org.opentravel.schemas.properties.Messages;

/**
 * @author Dave Hollander
 * 
 */
public class NewProjectWizard extends ValidatingWizard implements Cancelable {

    private NewProjectWizardPage page;
    private boolean canceled;
    private File projectFile;
    private String defaultName = "";
    private String description;
    private String name;
    private String namespace;
    private String selectedRoot;
    private String selectedExt;

    public NewProjectWizard() {
    }

    public NewProjectWizard(String defaultName, String selectedRoot, String selectedExt) {
        this.defaultName = defaultName;
        this.selectedRoot = selectedRoot;
        this.selectedExt = selectedExt;

    }

    @Override
    public void addPages() {
        page = new NewProjectWizardPage(Messages.getString("wizard.newProject.title"),
                Messages.getString("wizard.newProject.description"), getValidator());
        page.setDefaultName(defaultName);
        page.setSelectedRoot(selectedRoot);
        page.setSelectedExt(selectedExt);
        addPage(page);
    }

    @Override
    public boolean performFinish() {
        canceled = false;
        projectFile = new File(page.getPath());
        namespace = page.getNamespace();
        name = page.getProjectName();
        description = page.getDescription();

        if (projectFile.exists()) {
            return MessageDialog.openConfirm(
                    getShell(),
                    "File overwrite",
                    "File " + projectFile.getAbsolutePath()
                            + Messages.getString("wizard.newProject.error.fileExists"));
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

    public File getFile() {
        if (projectFile == null)
            performFinish();
        return projectFile;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

}
