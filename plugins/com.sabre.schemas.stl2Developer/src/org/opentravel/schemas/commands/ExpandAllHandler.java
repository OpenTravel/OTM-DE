/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemas.commands;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.views.OtmView;

/**
 * Handler for all Expand events. Uses command parameter to determine which view to expand.
 * 
 * @author Dave Hollander
 * 
 */
public class ExpandAllHandler extends OtmAbstractHandler {
    public static String COMMAND_ID = "org.opentravel.schemas.commands.expandAllNavigationView";

    @Override
    public Object execute(final ExecutionEvent event) throws ExecutionException {
        String selector = event.getParameter("stl2Developer.ViewSelection.Expand");
        if (selector == null)
            return null;

        OtmView view = OtmRegistry.getNavigatorView(); // equals("stl2Developer.ExpandViewSelection.Navigator"))
        if (selector.equals("stl2Developer.ViewSelection.Repository"))
            view = OtmRegistry.getRepositoryView();
        else if (selector.equals("stl2Developer.ViewSelection.Examples"))
            view = OtmRegistry.getExampleView();

        if (view != null) {
            view.expand();
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
