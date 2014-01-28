/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemas.commands;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

/**
 * 
 * @author Agnieszka Janowska
 * 
 */
public class AddContextHandler extends OtmAbstractHandler {
    public static String COMMAND_ID = "org.opentravel.schemas.commands.addContext";

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
     */
    @Override
    public Object execute(final ExecutionEvent event) throws ExecutionException {
        mc.getContextController().newContext();
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
