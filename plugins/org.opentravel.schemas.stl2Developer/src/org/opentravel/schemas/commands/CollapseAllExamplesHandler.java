
package org.opentravel.schemas.commands;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.views.OtmView;

/**
 * 
 * @author Agnieszka Janowska
 * 
 */
public class CollapseAllExamplesHandler extends OtmAbstractHandler {
    public static String COMMAND_ID = "org.opentravel.schemas.commands.exampleView.collapse";

    @Override
    public Object execute(final ExecutionEvent event) throws ExecutionException {
        final OtmView view = OtmRegistry.getExampleView();
        if (view != null) {
            view.collapse();
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
