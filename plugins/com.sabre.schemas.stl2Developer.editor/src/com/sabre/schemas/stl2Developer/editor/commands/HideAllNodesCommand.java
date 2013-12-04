/*
 * Copyright (c) 2013, Sabre Inc.
 */
package com.sabre.schemas.stl2Developer.editor.commands;

import org.eclipse.gef.commands.Command;

import com.sabre.schemas.stl2Developer.editor.model.Diagram;

/**
 * @author Pawel Jedruch
 * 
 */
public class HideAllNodesCommand extends Command {

    private Diagram diagram;

    public HideAllNodesCommand(Diagram model) {
        this.diagram = model;
    }

    @Override
    public void execute() {
        diagram.removeAll();
    }

}
