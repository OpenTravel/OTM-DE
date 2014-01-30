/*
 * Copyright (c) 2013, Sabre Inc.
 */
package org.opentravel.schemas.views.propertyview.desc;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.opentravel.schemas.views.propertyview.editors.ButtonCellEditor;
import org.opentravel.schemas.views.propertyview.editors.PulldownButtonCellEditor;

/**
 * @author Pawel Jedruch
 * 
 */
public class ButtonPropertyDescriptor extends PropertyDescriptor implements
        IFormPropertyDescriptor, IReadonlyPropertyDescriptor {

    private boolean isReadonly = false;
    private List<Action> list = new ArrayList<Action>(1);

    public ButtonPropertyDescriptor(Object id, String displayName) {
        super(id, displayName);
    }

    @Override
    public CellEditor createPropertyEditor(FormToolkit toolkit) {
        if (list.size() > 0) {
            return new PulldownButtonCellEditor(list);
        }
        return new ButtonCellEditor(toolkit);
    }

    @Override
    public GridData getCustomGridData() {
        return null;
    }

    @Override
    public boolean isReadonly() {
        return isReadonly;
    }

    public void setReadonly(boolean isReadonly) {
        this.isReadonly = isReadonly;
    }

    public void add(Action a) {
        list.add(a);
    }

}
