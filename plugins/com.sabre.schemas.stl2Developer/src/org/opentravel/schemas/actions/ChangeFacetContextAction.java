/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemas.actions;

import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.properties.ExternalizedStringProperties;
import org.opentravel.schemas.properties.StringProperties;
import org.opentravel.schemas.stl2developer.MainWindow;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.views.ContextsView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sabre.schemacompiler.model.TLContextReferrer;

/**
 * @author Agnieszka Janowska
 * 
 */
public class ChangeFacetContextAction extends OtmAbstractAction {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChangeFacetContextAction.class);
    private static StringProperties propsDefault = new ExternalizedStringProperties(
            "action.setContext");

    /**
	 *
	 */
    public ChangeFacetContextAction(final MainWindow mainWindow) {
        super(mainWindow, propsDefault);
    }

    public ChangeFacetContextAction(final MainWindow mainWindow, final StringProperties props) {
        super(mainWindow, props);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        final ContextsView view = OtmRegistry.getContextsView();
        final Node selected = getMainController().getCurrentNode_NavigatorView();
        if (selected != null && selected.isFacet()) {
            final Object model = selected.getModelObject().getTLModelObj();
            if (model instanceof TLContextReferrer) {
                view.getContextController().changeContext((TLContextReferrer) model);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#isEnabled()
     */
    @Override
    public boolean isEnabled() {
        LOGGER.debug("isEnabled for " + getMainController().getCurrentNode_NavigatorView());
        if (getMainController().getCurrentNode_NavigatorView().isQueryFacet())
            return true;
        return (getMainController().getCurrentNode_NavigatorView().isCustomFacet()) ? true : false;
    }
}
