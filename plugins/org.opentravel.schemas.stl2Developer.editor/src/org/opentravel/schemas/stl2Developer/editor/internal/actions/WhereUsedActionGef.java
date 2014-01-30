
package org.opentravel.schemas.stl2Developer.editor.internal.actions;

import java.util.List;

import org.eclipse.gef.ui.parts.AbstractEditPartViewer;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.stl2Developer.editor.model.UINode;
import org.opentravel.schemas.stl2Developer.editor.model.Diagram.Position;

/**
 * @author Pawel Jedruch
 * 
 */
public class WhereUsedActionGef extends ShowHideNodeAction {

    public WhereUsedActionGef(AbstractEditPartViewer viewer, String label) {
        super(viewer, label);
    }

    @Override
    protected List<Node> getNewNodes(UINode n) {
        return getOwningComponents(n.getNode().getWhereUsed());
    }

    @Override
    protected boolean isValidSelection(List<UINode> nodes) {
        for (UINode n : nodes) {
            if (!n.getNode().getWhereUsed().isEmpty())
                return true;
        }
        return false;
    }

    @Override
    protected Position getInitialPosition(Node newNode, Node referance) {
        if (newNode.isInstanceOf(referance)) {
            return Position.BOTTOM;
        } else if (referance.isInstanceOf(newNode)) {
            return Position.TOP;
        }
        return Position.LEFT;
    }
}
