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

/**
 * @author Dave Hollander
 * 
 */
public class OpenLibraryAction extends OtmAbstractAction {
    private static StringProperties propDefault = new ExternalizedStringProperties(
            "action.library.open");

    /**
	 *
	 */
    public OpenLibraryAction(final StringProperties props) {
        super(props);
    }

    public OpenLibraryAction() {
        super(propDefault);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        // final ModelNode modelNode = mc.getModelNode();
        mc.getLibraryController().openLibrary(mc.getSelectedNode_NavigatorView());
    }

    @Override
    public boolean isEnabled(Node node) {
        return true;
    }
}
