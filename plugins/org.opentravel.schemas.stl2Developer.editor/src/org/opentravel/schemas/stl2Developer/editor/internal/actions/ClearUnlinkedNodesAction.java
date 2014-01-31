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
package org.opentravel.schemas.stl2Developer.editor.internal.actions;

import org.eclipse.gef.ui.parts.AbstractEditPartViewer;

public class ClearUnlinkedNodesAction extends GEFAction {

    public ClearUnlinkedNodesAction(AbstractEditPartViewer viewer, String label) {
        super(viewer, label);
    }

    @Override
    public void run() {
        getInput().clearUnlinkedNodes();
    }

    @Override
    public boolean isEnabled() {
        return !getInput().getUnlinkedNodes().isEmpty();
    }

}
