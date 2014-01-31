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
