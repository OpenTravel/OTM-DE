/*
 * Copyright (c) 2013, Sabre Inc.
 */
package com.sabre.schemas.stl2Developer.editor.internal.actions;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.ui.parts.AbstractEditPartViewer;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;

import com.sabre.schemas.stl2Developer.editor.model.Diagram;

/**
 * @author Pawel Jedruch
 * 
 */
public abstract class GEFAction extends Action {

    private final AbstractEditPartViewer viewer;

    public GEFAction(AbstractEditPartViewer viewer) {
        this(viewer, null);
    }

    public GEFAction(AbstractEditPartViewer viewer, String label) {
        this.viewer = viewer;
        setText(label);
    }

    protected AbstractEditPartViewer getViewer() {
        return viewer;
    }

    protected IStructuredSelection getSelection() {
        return (IStructuredSelection) viewer.getSelection();
    }

    protected Diagram getInput() {
        return (Diagram) viewer.getContents().getModel();
    }

    protected void execute(Command cmd) {
        getViewer().getEditDomain().getCommandStack().execute(cmd);
    }

}
