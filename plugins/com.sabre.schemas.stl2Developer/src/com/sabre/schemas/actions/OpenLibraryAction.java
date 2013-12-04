/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.actions;

import com.sabre.schemas.node.Node;
import com.sabre.schemas.properties.ExternalizedStringProperties;
import com.sabre.schemas.properties.StringProperties;

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
