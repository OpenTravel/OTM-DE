/*
 * Copyright (c) 2013, Sabre Inc.
 */
package com.sabre.schemas.stl2Developer.editor.internal.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.ui.parts.AbstractEditPartViewer;

import com.sabre.schemas.node.Node;
import com.sabre.schemas.node.properties.AttributeNode;
import com.sabre.schemas.node.properties.ElementNode;
import com.sabre.schemas.node.properties.SimpleAttributeNode;
import com.sabre.schemas.stl2Developer.editor.internal.Features;
import com.sabre.schemas.stl2Developer.editor.model.Diagram.Position;
import com.sabre.schemas.stl2Developer.editor.model.UINode;

/**
 * @author Pawel Jedruch
 * 
 */
public class AddUsedTypesAction extends ShowHideNodeAction {

    public AddUsedTypesAction(AbstractEditPartViewer viewer, String label) {
        super(viewer, label);
    }

    @Override
    protected List<Node> getNewNodes(UINode n) {
        return getOwningComponents(getTypeUsersWithoutUnnasiged(n.getNode()));
    }

    @Override
    protected boolean isValidSelection(List<UINode> nodes) {
        for (UINode n : nodes) {
            if (!getTypeUsersWithoutUnnasiged(n.getNode()).isEmpty())
                return true;
        }
        return false;
    }

    private List<Node> getTypeUsersWithoutUnnasiged(Node node) {
        List<Node> ret = new ArrayList<Node>();
        for (Node n : node.getChildren_TypeUsers()) {
            if (validNodeType(n) && !n.isUnAssigned() && shouldDisplay(n.getTypeNode()))
                ret.add(n.getTypeNode());
        }
        return ret;

    }

    private boolean shouldDisplay(Node typeNode) {
        if (typeNode == null) {
            return false;
        }
        return Features.showAsUsedType(typeNode);
    }

    private boolean validNodeType(Node n) {
        boolean ret = n instanceof ElementNode;
        ret = ret || n instanceof AttributeNode;
        ret = ret || n instanceof SimpleAttributeNode;
        return ret;
    }

    @Override
    protected Position getInitialPosition(Node newNode, Node referance) {
        if (newNode.isInstanceOf(referance)) {
            return Position.BOTTOM;
        } else if (referance.isInstanceOf(newNode)) {
            return Position.TOP;
        }
        return Position.RIGHT;
    }

}
