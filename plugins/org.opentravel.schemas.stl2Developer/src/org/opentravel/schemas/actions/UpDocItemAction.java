/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemas.actions;

import org.opentravel.schemas.properties.StringProperties;
import org.opentravel.schemas.stl2developer.MainWindow;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.views.DocumentationView;

/**
 * @author Agnieszka Janowska
 * 
 */
public class UpDocItemAction extends OtmAbstractAction {

    /**
	 *
	 */
    public UpDocItemAction(final MainWindow mainWindow, final StringProperties props) {
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
            view.getDocumentationController().upDocItem();
        }
    }

}
