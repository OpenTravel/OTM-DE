
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
