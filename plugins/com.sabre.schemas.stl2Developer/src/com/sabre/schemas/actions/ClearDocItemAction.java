/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.actions;

import com.sabre.schemas.properties.StringProperties;
import com.sabre.schemas.stl2developer.MainWindow;
import com.sabre.schemas.stl2developer.OtmRegistry;
import com.sabre.schemas.views.DocumentationView;

/**
 * @author Agnieszka Janowska
 * 
 */
public class ClearDocItemAction extends OtmAbstractAction {

    /**
	 *
	 */
    public ClearDocItemAction(final MainWindow mainWindow, final StringProperties props) {
        super(mainWindow, props);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        DocumentationView view = OtmRegistry.getDocumentationView();
        if (view != null) {
            view.getDocumentationController().clearDocItem();
        }
    }

}
