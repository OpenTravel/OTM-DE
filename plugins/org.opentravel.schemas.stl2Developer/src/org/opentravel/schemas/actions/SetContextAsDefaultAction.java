/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemas.actions;

import org.opentravel.schemas.properties.StringProperties;
import org.opentravel.schemas.stl2developer.MainWindow;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.views.ContextsView;

/**
 * @author Agnieszka Janowska
 * 
 */
public class SetContextAsDefaultAction extends OtmAbstractAction {

    /**
	 *
	 */
    public SetContextAsDefaultAction(final MainWindow mainWindow, final StringProperties props) {
        super(mainWindow, props);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        ContextsView view = OtmRegistry.getContextsView();
        if (view != null) {
            view.getContextController().setDefaultContext();
        }
    }

}
