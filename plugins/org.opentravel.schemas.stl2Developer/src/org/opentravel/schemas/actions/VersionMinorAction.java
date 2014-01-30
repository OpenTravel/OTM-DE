
package org.opentravel.schemas.actions;

import org.opentravel.schemas.controllers.RepositoryController;
import org.opentravel.schemas.node.LibraryNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.properties.ExternalizedStringProperties;
import org.opentravel.schemas.properties.StringProperties;

/**
 * @author Dave Hollander
 * 
 */
public class VersionMinorAction extends OtmAbstractAction {
    private static StringProperties propsDefault = new ExternalizedStringProperties(
            "action.library.version.minor");

    public VersionMinorAction() {
        super(propsDefault);
    }

    public VersionMinorAction(final StringProperties props) {
        super(props);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        for (Node node : mc.getSelectedNodes_NavigatorView()) {
            mc.postStatus("Minor Version " + node);
            RepositoryController rc = mc.getRepositoryController();
            node = node.getLibrary();
            if (node != null && node instanceof LibraryNode) {
                // Make sure the library has an owner.
                // TODO - what to do if the owning model is null?
                if (((LibraryNode) node).getTLaLib().getOwningModel() != null)
                    rc.createMinorVersion((LibraryNode) node);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#isEnabled()
     */
    @Override
    public boolean isEnabled() {
        LibraryNode ln = null;
        if (mc.getSelectedNode_NavigatorView() != null)
            ln = mc.getSelectedNode_NavigatorView().getLibrary();
        return ln != null && ln.isManaged();
    }

}
