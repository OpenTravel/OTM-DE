/*
 * Copyright (c) 2013, Sabre Inc.
 */
package com.sabre.schemas.stl2Developer.editor.parts;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;

import com.sabre.schemas.node.ComponentNode;
import com.sabre.schemas.node.LibraryNode;
import com.sabre.schemas.node.Node;
import com.sabre.schemas.node.SimpleTypeNode;
import com.sabre.schemas.node.properties.PropertyNode;
import com.sabre.schemas.stl2Developer.editor.model.Connection;
import com.sabre.schemas.stl2Developer.editor.model.Diagram;
import com.sabre.schemas.stl2Developer.editor.model.UINode;

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
