/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.commands;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.sabre.schemas.stl2developer.OtmRegistry;
import com.sabre.schemas.views.OtmView;

/**
 * OBSOLETE - use ExpandAll with a parameter.
 * 
 * @author Agnieszka Janowska
 * 
 */
public class ExpandAllExamplesHandler extends OtmAbstractHandler {
    public static String COMMAND_ID = "com.sabre.schemas.commands.exxpandAllExamplesView";

    @Override
    public Object execute(final ExecutionEvent event) throws ExecutionException {
        final OtmView view = OtmRegistry.getExampleView();
        if (view != null) {
            view.expand();
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
