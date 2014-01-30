
package org.opentravel.schemas.actions;

import org.eclipse.swt.widgets.Event;
import org.opentravel.schemas.properties.ExternalizedStringProperties;
import org.opentravel.schemas.properties.StringProperties;
import org.opentravel.schemas.stl2developer.MainWindow;

/**
 * @author Dave Hollander
 * 
 */
public class AddEnumValueAction extends OtmAbstractAction {
    private static StringProperties props = new ExternalizedStringProperties("action.addEnumValue");

    public AddEnumValueAction(final MainWindow mainWindow) {
        super(mainWindow, props);
    }

    public AddEnumValueAction(final MainWindow mainWindow, final StringProperties props) {
        super(mainWindow, props);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#runWithEvent(org.eclipse.swt.widgets.Event)
     */
    @Override
    public void runWithEvent(Event event) {
        getMainController().runAddProperties(event);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#isEnabled()
     */
    @Override
    public boolean isEnabled() {
        if (getMainController().getCurrentNode_NavigatorView().isEnumerationLiteral())
            return true;
        return (getMainController().getCurrentNode_NavigatorView().isEnumeration()) ? true : false;
    }

}
