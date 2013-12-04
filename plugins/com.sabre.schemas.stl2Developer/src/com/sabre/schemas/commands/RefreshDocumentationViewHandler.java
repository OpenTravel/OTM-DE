/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.commands;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.sabre.schemas.stl2developer.OtmRegistry;
import com.sabre.schemas.views.DocumentationView;

/**
 * 
 * @author Agnieszka Janowska
 * 
 */
public class RefreshDocumentationViewHandler extends OtmAbstractHandler {
    public static String COMMAND_ID = "com.sabre.schemas.commands.refreshDocumentation";

    @Override
    public Object execute(final ExecutionEvent event) throws ExecutionException {
        final DocumentationView view = OtmRegistry.getDocumentationView();
        if (view != null) {
            view.forceCurrentNode();
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
