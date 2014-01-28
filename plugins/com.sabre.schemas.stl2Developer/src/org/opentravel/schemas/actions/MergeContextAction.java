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
     * @see org.opentravel.schemas.actions.IWithNodeAction.AbstractWithNodeAction#isEnabled()
     */
    // 5/20/2013 dmh - does not work. Is not called as the context tree is traversed.
    // @Override
    // public boolean isEnabled() {
    // ContextsView view = OtmRegistry.getContextsView();
    // return view == null || view.getCurrentNode() == null ? false : view.getCurrentNode()
    // .isEditable();
    // }

}
