
package org.opentravel.schemas.actions;

import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.properties.ExternalizedStringProperties;
import org.opentravel.schemas.properties.StringProperties;
import org.opentravel.schemas.stl2developer.MainWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dave Hollander
 * 
 */
public class CloneSelectedFacetNodesAction extends OtmAbstractAction implements IWithNodeAction {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(CloneSelectedFacetNodesAction.class);
    private static StringProperties propDefault = new ExternalizedStringProperties("action.copy");

    public CloneSelectedFacetNodesAction() {
        super(propDefault);
    }

    public CloneSelectedFacetNodesAction(final MainWindow mainWindow, final StringProperties props) {
        super(mainWindow, props);
    }

    @Override
    public void run() {
        getMainController().cloneSelectedFacetNodes();
    }

    @Override
    public boolean isEnabled(Node node) {
        if (node == null)
            return false;
        // TODO: only example. replace with some logic.
        return node.isSimpleFacet();
    }

}
