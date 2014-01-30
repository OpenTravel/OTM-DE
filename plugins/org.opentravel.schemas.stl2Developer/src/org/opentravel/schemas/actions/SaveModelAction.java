
package org.opentravel.schemas.actions;

import org.opentravel.schemas.node.INode;
import org.opentravel.schemas.properties.StringProperties;
import org.opentravel.schemas.stl2developer.MainWindow;

/**
 * @author Agnieszka Janowska
 * 
 */
public class SaveModelAction extends OtmAbstractAction {

    /**
	 *
	 */
    public SaveModelAction(final MainWindow mainWindow, final StringProperties props) {
        super(mainWindow, props);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        final INode modelNode = mc.getModelNode();
        if (modelNode != null) {
            mc.getModelController().saveModel(modelNode);
        }
    }

}
