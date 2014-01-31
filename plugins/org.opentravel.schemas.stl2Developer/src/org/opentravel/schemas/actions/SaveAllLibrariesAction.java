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
import org.eclipse.swt.widgets.Event;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.properties.ExternalizedStringProperties;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.properties.StringProperties;

/**
 * @author Dave Hollander
 * 
 */
public class SaveAllLibrariesAction extends OtmAbstractAction {
    private final static StringProperties propsDefault = new ExternalizedStringProperties(
            "action.saveAll");

    public SaveAllLibrariesAction() {
        super(propsDefault);
    }

    /**
	 *
	 */
    public SaveAllLibrariesAction(final StringProperties props) {
        super(props);
    }

    @Override
    public void runWithEvent(Event event) {
        mc.runSaveLibraries(event);
    }

    @Override
    public ImageDescriptor getImageDescriptor() {
        return Images.getImageRegistry().getDescriptor(Images.SaveAll);
    }

    @Override
    public boolean isEnabled() {
        Node n = mc.getSelectedNode_NavigatorView();
        return n != null ? !n.isBuiltIn() : false;
    }

}
