/*
 * Copyright (c) 2013, Sabre Inc.
 */
package org.opentravel.schemas.stl2Developer.editor.internal.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.opentravel.schemas.stl2Developer.editor.i18n.Messages;
import org.opentravel.schemas.stl2Developer.editor.internal.Features;
import org.opentravel.schemas.stl2Developer.editor.view.DependeciesView;

/**
 * @author Pawel Jedruch
 * 
 */
public class ToggleLayout extends Action {
    private DependeciesView view;

    public ToggleLayout(DependeciesView view) {
        super(getLabel(), IAction.AS_CHECK_BOX);
        this.view = view;
    }

    @Override
    public void run() {
        setText(getLabel());
        view.refresh();
    }

    private static String getLabel() {
        if (Features.isLayoutEnabled()) {
            return Messages.ToggleLayout_DisableLayout;
        } else {
            return Messages.ToggleLayout_EnableLayout;
        }
    }

    @Override
    public boolean isChecked() {
        return Features.isLayoutEnabled();
    }

    @Override
    public void setChecked(boolean checked) {
        Features.setLayoutEnabled(checked);
        super.setChecked(checked);
    }

}
