
package org.opentravel.schemas.stl2Developer.editor.internal.actions;

import org.eclipse.gef.ui.parts.AbstractEditPartViewer;

public class ClearAllNodesAction extends GEFAction {

    public ClearAllNodesAction(AbstractEditPartViewer viewer, String label) {
        super(viewer, label);
    }

    @Override
    public void run() {
        // TODO: should be similar to gef DeleteAction. Get command from policies.
        getInput().removeAll();
    }

    @Override
    public boolean isEnabled() {
        return !getInput().getTopLevels().isEmpty();
    }

}
