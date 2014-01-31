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
package org.opentravel.schemas.stl2developer;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class Perspective implements IPerspectiveFactory {

    @Override
    public void createInitialLayout(final IPageLayout layout) {
        layout.setEditorAreaVisible(false);
        layout.setFixed(false);

        // IFolderLayout topLeft =
        // layout.getFolderForView("org.opentravel.schemas.stl2developer.TypeView");//layout.createFolder("typeViews",
        // IPageLayout.RIGHT, 0.15f, IPageLayout.ID_EDITOR_AREA);
        // topLeft.addPlaceholder("org.opentravel.schemas.stl2Developer.TypeView:typeView*"); /* DYNAMIC
        // VIEWS. So just adding a placeholder */
        //
        // layout.getF
    }

}
