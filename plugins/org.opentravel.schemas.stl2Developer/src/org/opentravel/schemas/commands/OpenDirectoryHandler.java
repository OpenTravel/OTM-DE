
package org.opentravel.schemas.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.opentravel.schemas.stl2developer.OtmRegistry;

/**
 * July 1, 2013 - dmh - removed from plugin.xml
 * 
 * @author Dave Hollander
 * 
 */
public class OpenDirectoryHandler extends AbstractHandler {

    public static final String COMMAND_ID = "org.opentravel.schemas.commands.openDirectory";

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        OtmRegistry.getMainController().getProjectController().openDir();
        return null;
    }
}
