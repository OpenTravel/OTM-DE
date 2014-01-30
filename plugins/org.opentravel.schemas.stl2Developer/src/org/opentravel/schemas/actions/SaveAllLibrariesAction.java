
package org.opentravel.schemas.actions;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Event;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.properties.ExternalizedStringProperties;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.properties.StringProperties;

/**
 * @author Dave Hollander
 * 
 */
public class SaveAllLibrariesAction extends OtmAbstractAction {
    private final static StringProperties propsDefault = new ExternalizedStringProperties(
            "action.saveAll");

    public SaveAllLibrariesAction() {
        super(propsDefault);
    }

    /**
	 *
	 */
    public SaveAllLibrariesAction(final StringProperties props) {
        super(props);
    }

    @Override
    public void runWithEvent(Event event) {
        mc.runSaveLibraries(event);
    }

    @Override
    public ImageDescriptor getImageDescriptor() {
        return Images.getImageRegistry().getDescriptor(Images.SaveAll);
    }

    @Override
    public boolean isEnabled() {
        Node n = mc.getSelectedNode_NavigatorView();
        return n != null ? !n.isBuiltIn() : false;
    }

}
