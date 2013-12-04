/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.commands;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.sabre.schemas.stl2developer.OtmRegistry;
import com.sabre.schemas.views.ValidationResultsView;

/**
 * 
 * @author S. Livezey
 */
public class CopyValidationResultsHandler extends OtmAbstractHandler {
    public static String COMMAND_ID = "com.sabre.schemas.commands.copyValidationResults";

    @Override
    public Object execute(final ExecutionEvent event) throws ExecutionException {
        final ValidationResultsView validationView = OtmRegistry.getValidationResultsView();

        if (validationView != null) {
            validationView.copyFindingsToClipboard();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.commands.OtmHandler#getID()
     */
    @Override
    public String getID() {
        return COMMAND_ID;
    }

}
