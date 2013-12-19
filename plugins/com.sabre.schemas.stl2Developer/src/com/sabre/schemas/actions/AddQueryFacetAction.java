/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.actions;

import com.sabre.schemas.node.BusinessObjectNode;
import com.sabre.schemas.node.Node;
import com.sabre.schemas.properties.ExternalizedStringProperties;
import com.sabre.schemas.properties.StringProperties;
import com.sabre.schemas.stl2developer.MainWindow;

/**
 * @author Agnieszka Janowska
 * 
 */
public class AddQueryFacetAction extends OtmAbstractAction {
    private static StringProperties propsDefault = new ExternalizedStringProperties(
            "action.addQuery");

    /**
	 *
	 */
    public AddQueryFacetAction(final MainWindow mainWindow) {
        super(mainWindow, propsDefault);
    }

    public AddQueryFacetAction(final MainWindow mainWindow, final StringProperties props) {
        super(mainWindow, props);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        getMainController().addQueryFacet();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#isEnabled()
     */
    @Override
    public boolean isEnabled() {
        // Unmanged or in the most current (head) library in version chain.
        Node n = mc.getCurrentNode_NavigatorView().getOwningComponent();
        return n instanceof BusinessObjectNode ? n.isEditable() && n.isNewToChain() : false;
    }

}
