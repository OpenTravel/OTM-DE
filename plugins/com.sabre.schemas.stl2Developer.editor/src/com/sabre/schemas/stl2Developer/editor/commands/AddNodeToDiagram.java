/*
 * Copyright (c) 2013, Sabre Inc.
 */
package com.sabre.schemas.stl2Developer.editor.commands;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.commands.Command;

import com.sabre.schemas.node.ComponentNode;
import com.sabre.schemas.node.Node;
import com.sabre.schemas.stl2Developer.editor.model.Diagram;
import com.sabre.schemas.stl2Developer.editor.model.UINode;

/**
 * @author Pawel Jedruch
 * 
 */
public class AddNodeToDiagram extends Command {

    private Node newNode;
    private Diagram diagram;
    private Point location;

    public AddNodeToDiagram(Node newObject, Diagram model, Point location) {
        this.newNode = newObject;
        this.diagram = model;
        this.location = location;
    }

    @Override
    public void execute() {
        diagram.addChild(UINode.getOwner(newNode), location);
    }

    @Override
    public boolean canExecute() {
        return newNode != null && newNode.getOwningComponent() != null
                && newNode instanceof ComponentNode;
    }
}
