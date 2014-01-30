
package org.opentravel.schemas.commands;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.handlers.HandlerUtil;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.views.OtmView;

/**
 * Command handler used to toggle the state that controls whether inherited properties are to be
 * displayed.
 * 
 * @author S. Livezey
 */
public class DisplayInheritedPropertiesHandler extends OtmAbstractHandler {
    public static String COMMAND_ID = "org.opentravel.schemas.commands.displayInheritedProperties";

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
     * @see org.opentravel.schemas.commands.OtmHandler#getID()
     */
    @Override
    public String getID() {
        return COMMAND_ID;
    }

}
