
package org.opentravel.schemas.actions;

import org.eclipse.swt.widgets.Event;
import org.opentravel.schemas.node.PropertyNodeType;
import org.opentravel.schemas.properties.ExternalizedStringProperties;
import org.opentravel.schemas.properties.StringProperties;
import org.opentravel.schemas.stl2developer.MainWindow;

/**
 * Run the addNode command to add roles to a core object.
 * 
 * @author Dave Hollander
 * 
 */
public class AddRoleAction extends OtmAbstractAction {
    private static StringProperties propDefault = new ExternalizedStringProperties("action.addRole");

    public AddRoleAction(final MainWindow mainWindow) {
        super(mainWindow, propDefault);
    }

    public AddRoleAction(final MainWindow mainWindow, final StringProperties props) {
        super(mainWindow, props);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#runWithEvent(org.eclipse.swt.widgets.Event)
     */
    @Override
    public void runWithEvent(Event event) {
        event.data = PropertyNodeType.ROLE;
        getMainController().runAddProperties(event);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#isEnabled()
     */
    @Override
    public boolean isEnabled() {
        return (getMainController().getCurrentNode_NavigatorView().getOwningComponent()
                .isCoreObject()) ? true : false;
    }

}
