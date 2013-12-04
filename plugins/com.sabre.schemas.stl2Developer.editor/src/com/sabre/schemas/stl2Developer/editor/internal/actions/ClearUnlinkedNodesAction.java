package com.sabre.schemas.stl2Developer.editor.internal.actions;

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
