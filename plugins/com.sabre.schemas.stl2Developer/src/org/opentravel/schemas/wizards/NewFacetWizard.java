/*
 * Copyright (c) 2011, Sabre Inc.
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
