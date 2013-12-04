/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.commands;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.handlers.HandlerUtil;

import com.sabre.schemas.stl2developer.OtmRegistry;
import com.sabre.schemas.views.OtmView;

/**
 * Command handler used to toggle the state that controls whether inherited properties are to be
 * displayed.
 * 
 * @author S. Livezey
 */
public class DisplayInheritedPropertiesHandler extends OtmAbstractHandler {
    public static String COMMAND_ID = "com.sabre.schemas.commands.displayInheritedProperties";

    @Override
    public Object execute(final ExecutionEvent event) throws ExecutionException {
        Command command = event.getCommand();
        boolean oldValue = HandlerUtil.toggleCommandState(command);
        final OtmView view = OtmRegistry.getNavigatorView();
        if (view != null) {
            view.setInheritedPropertiesDisplayed(!oldValue);
        }
        OtmRegistry.getMainController().refresh();
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
