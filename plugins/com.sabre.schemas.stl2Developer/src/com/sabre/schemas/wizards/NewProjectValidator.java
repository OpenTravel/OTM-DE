/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.wizards;

import com.sabre.schemas.controllers.ProjectController;
import com.sabre.schemas.properties.Messages;
import com.sabre.schemas.stl2developer.OtmRegistry;

/**
 * @author Dave Hollander
 * 
 */
public class NewProjectValidator implements FormValidator {

    NewProjectWizardPage page;

    public NewProjectValidator() {
    }

    public void setPage(NewProjectWizardPage page) {
        this.page = page;
    }

    @Override
    public void validate() throws ValidationException {
        if (page == null)
            return;
        String nsID = page.getNamespace();

        // Test to see if the selected ns/ID is already in use.
        ProjectController pc = OtmRegistry.getMainController().getProjectController();

        if (nsID == null || nsID.isEmpty())
            throw new ValidationException(Messages.getString("wizard.newProject.error.missingNS"));

        if (nsID.equals(pc.getDefaultUnmanagedNS()))
            throw new ValidationException(Messages.getString("wizard.newProject.error.defaultNS"));

        for (String gns : pc.getOpenGovernedNamespaces()) {
            if (nsID.equals(gns)) {
                throw new ValidationException(
                        Messages.getString("wizard.newProject.error.nsGoverned"));
            }
        }
    }

}
