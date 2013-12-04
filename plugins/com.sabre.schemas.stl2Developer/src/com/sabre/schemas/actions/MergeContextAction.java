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
public class MergeContextAction extends OtmAbstractAction {

    /**
	 *
	 */
    public MergeContextAction(final MainWindow mainWindow, final StringProperties props) {
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
            view.getContextController().mergeContext();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.actions.IWithNodeAction.AbstractWithNodeAction#isEnabled()
     */
    // 5/20/2013 dmh - does not work. Is not called as the context tree is traversed.
    // @Override
    // public boolean isEnabled() {
    // ContextsView view = OtmRegistry.getContextsView();
    // return view == null || view.getCurrentNode() == null ? false : view.getCurrentNode()
    // .isEditable();
    // }

}
