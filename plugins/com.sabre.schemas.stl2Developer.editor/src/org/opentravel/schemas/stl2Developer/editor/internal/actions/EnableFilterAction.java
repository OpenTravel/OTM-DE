/*
 * Copyright (c) 2013, Sabre Inc.
 */
package org.opentravel.schemas.stl2Developer.editor.internal.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.IFilter;
import org.opentravel.schemas.stl2Developer.editor.view.DependeciesView;

/**
 * @author Pawel Jedruch
 * 
 */
public class EnableFilterAction extends Action {

    private IFilter filter;
    private DependeciesView view;

    public EnableFilterAction(String text, IFilter filter, DependeciesView view) {
        super(text, IAction.AS_CHECK_BOX);
        this.filter = filter;
        this.view = view;
    }

    @Override
    public void run() {
        if (isChecked()) {
            view.addNodeFilter(filter);
        } else {
            view.removeNodeFilter(filter);
        }
    }

}
