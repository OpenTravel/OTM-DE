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

import org.eclipse.jface.resource.ImageDescriptor;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeEditStatus;
import org.opentravel.schemas.properties.ExternalizedStringProperties;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.properties.StringProperties;

/**
 * Manage a library in a repository.
 * 
 * @author Dave Hollander
 * 
 */
public class LockLibraryAction extends OtmAbstractAction {
    private static StringProperties propDefault = new ExternalizedStringProperties(
            "action.library.lock");

    public LockLibraryAction() {
        super(propDefault);
    }

    public LockLibraryAction(final StringProperties props) {
        super(props);
    }

    @Override
    public void run() {
        mc.getRepositoryController().lock();
    }

    @Override
    public ImageDescriptor getImageDescriptor() {
        return Images.getImageRegistry().getDescriptor(Images.Lock);
    }

    // enable when Work-in-progress item.
    @Override
    public boolean isEnabled(Node node) {
        Node n = getMainController().getCurrentNode_NavigatorView();
        if (n == null || n.getLibrary() == null)
            return false;
        return n.getLibrary().getEditStatus().equals(NodeEditStatus.MANAGED_READONLY);
    }
}
