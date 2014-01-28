/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemas.actions;

import org.opentravel.schemas.node.BusinessObjectNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.properties.ExternalizedStringProperties;
import org.opentravel.schemas.properties.StringProperties;
import org.opentravel.schemas.stl2developer.MainWindow;

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
