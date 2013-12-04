/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.actions;

import com.sabre.schemas.node.Node;
import com.sabre.schemas.properties.StringProperties;
import com.sabre.schemas.stl2developer.MainWindow;

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
