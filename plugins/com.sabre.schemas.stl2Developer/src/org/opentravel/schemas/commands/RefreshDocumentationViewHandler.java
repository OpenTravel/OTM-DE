/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemas.commands;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.views.DocumentationView;

/**
 * 
 * @author Agnieszka Janowska
 * 
 */
public class RefreshDocumentationViewHandler extends OtmAbstractHandler {
    public static String COMMAND_ID = "org.opentravel.schemas.commands.refreshDocumentation";

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
     * @see org.opentravel.schemas.commands.OtmHandler#getID()
     */
    @Override
    public String getID() {
        return COMMAND_ID;
    }

}
