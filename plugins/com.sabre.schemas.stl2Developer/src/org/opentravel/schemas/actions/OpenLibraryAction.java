/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemas.actions;

import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.properties.ExternalizedStringProperties;
import org.opentravel.schemas.properties.StringProperties;

/**
 * @author Dave Hollander
 * 
 */
public class OpenLibraryAction extends OtmAbstractAction {
    private static StringProperties propDefault = new ExternalizedStringProperties(
            "action.library.open");

    /**
	 *
	 */
    public OpenLibraryAction(final StringProperties props) {
        super(props);
    }

    public OpenLibraryAction() {
        super(propDefault);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        // final ModelNode modelNode = mc.getModelNode();
        mc.getLibraryController().openLibrary(mc.getSelectedNode_NavigatorView());
    }

    @Override
    public boolean isEnabled(Node node) {
        return true;
    }
}
