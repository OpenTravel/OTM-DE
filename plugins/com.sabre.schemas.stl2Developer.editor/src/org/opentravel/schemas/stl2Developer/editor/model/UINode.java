/*
 * Copyright (c) 2013, Sabre Inc.
 */
package org.opentravel.schemas.stl2Developer.editor.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Image;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.OperationNode;
import org.opentravel.schemas.types.TypeResolver;

/**
 * @author Pawel Jedruch
 * 
 */
public class UINode {
    // TODO: remove getters and setters
    private final Diagram owner;
    private final Node node;
    private final UINode parent;
    private Point location;
    private Dimension size;
    private List<UINode> children = new ArrayList<UINode>();

    UINode(Node node, Diagram owner, UINode parent) {
        this.node = node;
        this.owner = owner;
        this.parent = parent;
        if (parent != null) {
            parent.addChild(this);
        }
    }

    public Node getNode() {
        return node;
    }

    public UINode getParent() {
        return parent;
    }

    public Point getLocation() {
        return location;
    }

    public Dimension getSize() {
        return size;
    }

    public List<UINode> getChildren() {
        return children;
    }

    public void addChild(UINode child) {
        children.add(child);
    }

    public Rectangle getBoundry() {
        return new Rectangle(location, size);
    }

    public void setSize(Dimension size) {
        this.size = size;
    }

    public void setLocation(Point location) {
        Point oldLocation = this.location;
        this.location = location;
        owner.publish(this, "location", oldLocation, location);
    }

    public Diagram getOwner() {
        return owner;
    }

    /**
     * Returns the List of the connection nodes objects for which this node model is the
     * <b>target</b>
     */
    public Collection<UINode> getConnectedAsTarget() {
        List<UINode> ret = new ArrayList<UINode>();
        for (Node u : node.getTypeUsers()) {
            UINode uiTypeUser = owner.findUINode(u);
            if (uiTypeUser != null)
                ret.add(uiTypeUser);
        }
        return ret;
    }

    /**
     * Returns the List of the connection nodes objects for which this node model is the
     * <b>source</b>
     */
    public Collection<UINode> getConnectedAsSource() {
        List<UINode> ret = new ArrayList<UINode>();
        UINode uiTypeUser = owner.findUINode(node.getTypeNode());
        if (uiTypeUser != null)
            ret.add(uiTypeUser);
        return ret;
    }

    public boolean isTopLevel() {
        return parent == null;
    }

    public UINode getTopLevelParent() {
        return getTopLevelParent(this);
    }

    private UINode getTopLevelParent(UINode n) {
        if (n.isTopLevel()) {
            return n;
        } else {
            return getTopLevelParent(n.getParent());
        }
    }

    public static Node getOwner(Node node) {
        if (node instanceof OperationNode)
            return node;
        else if (node == node.getOwningComponent()) {
            return node;
        } else {
            return getOwner(node.getOwningComponent());
        }
    }

    public Image getTypeImage() {
        Node type = TypeResolver.getNodeType(getNode());
        // TODO: inherited properties dosn't have a typeNode.
        if (type != null) {
            return type.getImage();
        }
        return null;
    }

    public boolean isUnlinked() {
        return node.isDeleted();
    }

}
