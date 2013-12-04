/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.commands;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.sabre.schemas.stl2developer.OtmRegistry;
import com.sabre.schemas.views.OtmView;

/**
 * Handler for all Collapse actions/commands. Uses parameter to determine which view to collapse.
 * 
 * @author Dave Hollander
 * 
 */
public class CollapseAllHandler extends OtmAbstractHandler {
    public static String COMMAND_ID = "com.sabre.schemas.commands.navigatorView.collapse";

    @Override
    public Object execute(final ExecutionEvent event) throws ExecutionException {

        String selector = event.getParameter("stl2Developer.ViewSelection.Collapse");
        if (selector == null)
            return null;

        OtmView view = OtmRegistry.getNavigatorView(); // equals("stl2Developer.ExpandViewSelection.Navigator"))
        if (selector.equals("stl2Developer.ViewSelection.Repository"))
            view = OtmRegistry.getRepositoryView();
        else if (selector.equals("stl2Developer.ViewSelection.Examples"))
            view = OtmRegistry.getExampleView();

        if (view != null) {
            view.collapse();
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
