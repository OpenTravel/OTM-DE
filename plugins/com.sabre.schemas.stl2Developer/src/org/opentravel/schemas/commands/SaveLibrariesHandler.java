/**
 * 
 */
package org.opentravel.schemas.commands;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

/**
 * Handler for the save libraries command.
 * 
 * @author Dave Hollander
 * 
 */
public class SaveLibrariesHandler extends OtmAbstractHandler {
    public static String COMMAND_ID = "org.opentravel.schemas.commands.SaveAllLibraries";

    @Override
    public Object execute(ExecutionEvent exEvent) throws ExecutionException {
        mc.getLibraryController().saveAllLibraries(false);
        return null;
    }

    @Override
    public String getID() {
        return COMMAND_ID;
    }

}
