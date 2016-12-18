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

import org.opentravel.schemas.controllers.RepositoryController;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.properties.ExternalizedStringProperties;
import org.opentravel.schemas.properties.StringProperties;

/**
 * @author Dave Hollander
 * 
 */
public class VersionPatchAction extends OtmAbstractAction {
    private static StringProperties propsDefault = new ExternalizedStringProperties(
            "action.library.version.patch");

    public VersionPatchAction() {
        super(propsDefault);
    }

    public VersionPatchAction(final StringProperties props) {
        super(props);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        for (Node node : mc.getSelectedNodes_NavigatorView()) {
            mc.postStatus("Patch Version " + node);
            RepositoryController rc = mc.getRepositoryController();
            node = node.getLibrary();
            if (node != null && node instanceof LibraryNode)
                rc.createPatchVersion((LibraryNode) node);
        }
        mc.refresh();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#isEnabled()
     */
    @Override
    public boolean isEnabled() {
        LibraryNode ln = null;
        if (mc.getSelectedNode_NavigatorView() != null)
            ln = mc.getSelectedNode_NavigatorView().getLibrary();
        return ln != null && ln.isManaged();
    }

}
