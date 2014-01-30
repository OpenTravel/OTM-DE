
package org.opentravel.schemas.actions;

import org.opentravel.schemas.node.BusinessObjectNode;
import org.opentravel.schemas.node.CoreObjectNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.VWA_Node;
import org.opentravel.schemas.properties.ExternalizedStringProperties;
import org.opentravel.schemas.properties.StringProperties;
import org.opentravel.schemas.stl2developer.MainWindow;

/**
 * Attached to the navigator menus.
 * 
 * @author Agnieszka Janowska
 * 
 */
public class ChangeAction extends OtmAbstractAction {
    private final static StringProperties propsDefault = new ExternalizedStringProperties(
            "action.changeObject");

    /**
	 *
	 */
    public ChangeAction(final MainWindow mainWindow) {
        super(mainWindow, propsDefault);
    }

    public ChangeAction(final MainWindow mainWindow, final StringProperties props) {
        super(mainWindow, props);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        getMainController().changeTreeSelection();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#isEnabled()
     */
    @Override
    public boolean isEnabled() {
        Node n = getMainController().getCurrentNode_NavigatorView().getOwningComponent();
        if (n instanceof BusinessObjectNode || n instanceof CoreObjectNode || n instanceof VWA_Node) {
            return n.getChain() == null ? n.isEditable() : n.getChain().isMajor();
        }
        return false;
    }

}
