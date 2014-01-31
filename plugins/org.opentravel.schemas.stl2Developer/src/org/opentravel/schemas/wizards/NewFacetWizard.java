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

import java.util.List;

import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.properties.PropertyNode;

/**
 * @author Agnieszka Janowska
 * 
 */
public class NewFacetWizard extends ValidatingWizard implements Cancelable {

    private NewFacetWizardPage page;
    private boolean canceled;
    private final ComponentNode propertyOwner;
    private String defaultName;

    public NewFacetWizard(final ComponentNode propertyOwner, String defaultName) {
        this.propertyOwner = propertyOwner;
        this.defaultName = defaultName;
    }

    @Override
    public void addPages() {
        page = new NewFacetWizardPage("New Facet", "Select context and properties of new facet",
                getValidator(), propertyOwner, defaultName);
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
        dialog.setPageSize(600, 400);
        dialog.create();
        dialog.open();
    }

    @Override
    public boolean wasCanceled() {
        return canceled;
    }

    public List<PropertyNode> getSelectedProperties() {
        return page.getProperties();
    }

    public String getName() {
        return page.getFacetName();
    }
}
