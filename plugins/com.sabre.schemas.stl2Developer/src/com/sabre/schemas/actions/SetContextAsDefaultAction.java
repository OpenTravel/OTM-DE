/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.actions;

import com.sabre.schemas.properties.StringProperties;
import com.sabre.schemas.stl2developer.MainWindow;
import com.sabre.schemas.stl2developer.OtmRegistry;
import com.sabre.schemas.views.ContextsView;

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
