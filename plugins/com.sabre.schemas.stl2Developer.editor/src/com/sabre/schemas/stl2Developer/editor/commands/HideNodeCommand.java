/*
 * Copyright (c) 2013, Sabre Inc.
 */
package com.sabre.schemas.stl2Developer.editor.commands;

import org.eclipse.gef.commands.Command;

import com.sabre.schemas.node.Node;
import com.sabre.schemas.stl2Developer.editor.model.Diagram;

/**
 * @author Pawel Jedruch
 * 
 */
public class HideNodeCommand extends Command {

    private Node toHide;
    private Diagram diagram;

    public HideNodeCommand(Node toHide, Diagram model) {
        this.toHide = toHide;
        this.diagram = model;
    }

    @Override
    public void execute() {
        diagram.remove(toHide);
    }

}
