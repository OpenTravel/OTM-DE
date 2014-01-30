
package org.opentravel.schemas.stl2Developer.editor.parts;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.LibraryNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.SimpleTypeNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.stl2Developer.editor.model.Connection;
import org.opentravel.schemas.stl2Developer.editor.model.Diagram;
import org.opentravel.schemas.stl2Developer.editor.model.UINode;

/**
 * @author Pawel Jedruch
 * 
 */
public class EditPartsFactory implements EditPartFactory {

    @Override
    public EditPart createEditPart(EditPart context, Object element) {
        if (element instanceof Diagram) {
            return new DiagramEditPart((Diagram) element);
        }
        if (element instanceof Connection) {
            return new ConnectionEditPart((Connection) element);
        }

        UINode uiNode = (UINode) element;
        Node node = uiNode.getNode();
        if (node instanceof LibraryNode) {
            return new LibraryEditPart(uiNode);
        } else if (node instanceof PropertyNode) {
            return new PropertyNodeEditPart(uiNode);
        } else if (node instanceof SimpleTypeNode) {
            return new PropertyNodeEditPart(uiNode);
        } else if (node instanceof ComponentNode) {
            return new ComponentNodeEditPart(uiNode);
        } else if (node instanceof Node) {
            return new UnsupportedNodeEditPart(uiNode);
        }
        return null;
    }
}
