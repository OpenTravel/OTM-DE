/*
 * Copyright (c) 2013, Sabre Inc.
 */
package org.opentravel.schemas.stl2Developer.editor.commands;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.commands.Command;
import org.opentravel.schemas.stl2Developer.editor.model.UINode;

/**
 * @author Pawel Jedruch
 * 
 */
public class SetConstraintCommand extends Command {

    private UINode uiNode;
    private Rectangle constraint;

    public SetConstraintCommand(UINode uiNode, Rectangle constraint) {
        this.uiNode = uiNode;
        this.constraint = constraint;
    }

    @Override
    public void execute() {
        uiNode.setLocation(constraint.getLocation());
    }

}
