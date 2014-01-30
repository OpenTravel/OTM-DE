
package org.opentravel.schemas.actions;

import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.properties.StringProperties;
import org.opentravel.schemas.stl2developer.MainWindow;

/**
 * @author Agnieszka Janowska
 * 
 */
public class NewLibraryAction extends OtmAbstractAction {

    /**
	 *
	 */
    public NewLibraryAction(final MainWindow mainWindow, final StringProperties props) {
        super(mainWindow, props);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        Node newNode = getMainController().getCurrentNode_NavigatorView();
        newNode = mc.getLibraryController().createLibrary();
        getMainController().selectNavigatorNodeAndRefresh(newNode);
    }

}
