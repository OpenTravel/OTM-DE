/*
 * Copyright (c) 2013, Sabre Inc.
 */
package com.sabre.schemas.views.propertyview.desc;

import java.util.Collection;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import com.sabre.schemas.views.propertyview.editors.ComboBoxFormCellEditor;

/**
 * @author Pawel Jedruch
 * 
 */
public class ComboboxPropertyDescriptor extends PropertyDescriptor implements
        IFormPropertyDescriptor, IReadonlyPropertyDescriptor {

    private Collection<String> items;
    private boolean isReadonly;

    public ComboboxPropertyDescriptor(Object id, String displayName, Collection<String> items) {
        super(id, displayName);
        this.items = items;
    }

    @Override
    public CellEditor createPropertyEditor(FormToolkit toolkit) {
        return new ComboBoxFormCellEditor(toolkit, items, false);
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

}
