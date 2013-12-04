/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.actions;

import com.sabre.schemas.properties.StringProperties;
import com.sabre.schemas.stl2developer.MainWindow;
import com.sabre.schemas.stl2developer.OtmRegistry;
import com.sabre.schemas.views.OtmView;

/**
 * @author Agnieszka Janowska
 * 
 */
public class DisplayPropertyTreeViewAction extends OtmAbstractAction {

    /**
	 *
	 */
    public DisplayPropertyTreeViewAction(final MainWindow mainWindow, final StringProperties props) {
        super(mainWindow, props, AS_CHECK_BOX);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        final OtmView view = OtmRegistry.getNavigatorView();
        if (view != null) {
            view.setDeepPropertyView(isChecked());
        }
    }

}
