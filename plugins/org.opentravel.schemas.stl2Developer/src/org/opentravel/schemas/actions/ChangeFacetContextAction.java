/**
 * Copyright (C) 2014 OpenTravel Alliance (info@opentravel.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

import org.opentravel.schemacompiler.model.TLContextReferrer;

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
