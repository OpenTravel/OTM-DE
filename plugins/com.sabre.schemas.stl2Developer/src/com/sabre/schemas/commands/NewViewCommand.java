/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.commands;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

public class NewViewCommand extends OtmAbstractHandler {
    public static String COMMAND_ID = "com.sabre.schemas.commands.newView";

    private static Integer counter = 0;

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
     */
    @Override
    public Object execute(final ExecutionEvent event) throws ExecutionException {
        final IWorkbenchPage part = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getActivePage();
        try {
            part.showView("com.sabre.schemas.stl2Developer.TypeView", "typeView" + ++counter,
                    IWorkbenchPage.VIEW_ACTIVATE);
        } catch (final PartInitException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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
