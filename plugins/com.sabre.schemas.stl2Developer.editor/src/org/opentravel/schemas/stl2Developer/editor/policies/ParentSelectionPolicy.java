package org.opentravel.schemas.stl2Developer.editor.policies;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.editpolicies.SelectionEditPolicy;
import org.opentravel.schemas.stl2Developer.editor.parts.DiagramEditPart;

/**
 * In order to get top-level element (children of Diagram) this policy should be installed in
 * EditPart. After that each getTargetEditPart
 * 
 * @author Pawel Jedruch
 * 
 */
public class ParentSelectionPolicy extends SelectionEditPolicy {

    @Override
    public EditPart getTargetEditPart(Request request) {
        return getParentEP(getHost());
    }

    private EditPart getParentEP(EditPart ep) {
        while (!(ep.getParent() instanceof DiagramEditPart)) {
            ep = ep.getParent();
        }
        return ep;
    }

    @Override
    protected void hideSelection() {
    }

    @Override
    protected void showSelection() {
    }

}