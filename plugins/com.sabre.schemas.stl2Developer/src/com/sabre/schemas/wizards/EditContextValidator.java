/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.wizards;

import com.sabre.schemacompiler.model.TLContext;
import com.sabre.schemacompiler.model.TLLibrary;
import com.sabre.schemas.properties.Messages;

/**
 * @author Agnieszka Janowska
 * 
 */
public class EditContextValidator implements FormValidator {

    private final EditContextWizard wizard;
    private final TLLibrary library;

    public EditContextValidator(final TLLibrary library, final EditContextWizard wizard) {
        this.library = library;
        this.wizard = wizard;
    }

    @Override
    public void validate() throws ValidationException {
        final TLContext contextObject = wizard.getContext();
        final String context = contextObject.getContextId();
        final String appCtx = contextObject.getApplicationContext();
        if (appCtx == null || appCtx.isEmpty()) {
            throw new ValidationException(Messages.getString("error.newContextApplicationEmpty"));
        }
        if (context == null || context.isEmpty()) {
            throw new ValidationException(Messages.getString("error.newContextIdEmpty"));
        }
        final TLContext origContextById = library.getContext(context);
        final TLContext origContextByApp = library.getContextByApplicationContext(appCtx);
        if (origContextById != null && origContextByApp != null
                && !origContextByApp.equals(origContextById)) {
            throw new ValidationException(Messages.getString("error.editContextNotMatching"));
        }
    }

}
