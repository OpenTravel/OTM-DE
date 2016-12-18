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

import static org.opentravel.schemas.stl2developer.MainWindow.NO_VALID_SELECTION_MSG;
import static org.opentravel.schemas.stl2developer.MainWindow.SELECT_AT_LEAST_ONE_MSG;
import static org.opentravel.schemas.stl2developer.MainWindow.WARNING_MSG;

import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.properties.ExternalizedStringProperties;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.properties.StringProperties;
import org.opentravel.schemas.stl2developer.DialogUserNotifier;

/**
 * @author Agnieszka Janowska
 * 
 */
public class SaveSelectedLibrariesAction extends OtmAbstractAction {
    private static StringProperties propsDefault = new ExternalizedStringProperties(
            "action.saveSelected");

    public SaveSelectedLibrariesAction() {
        super(propsDefault);
    }

    @Override
    public void run() {
        final List<LibraryNode> libraries = mc.getSelectedUserLibraries();
        if (libraries.isEmpty()) {
            // No libraries selected
            DialogUserNotifier.openWarning(WARNING_MSG, NO_VALID_SELECTION_MSG
                    + SELECT_AT_LEAST_ONE_MSG);
            return;
        }
        mc.getLibraryController().saveLibraries(libraries, false);
    }

    @Override
    public ImageDescriptor getImageDescriptor() {
        return Images.getImageRegistry().getDescriptor(Images.Save);
    }

    @Override
    public boolean isEnabled() {
        return mc.getCurrentNode_NavigatorView() != null ? mc.getCurrentNode_NavigatorView()
                .isEditable() : false;
    }

}
