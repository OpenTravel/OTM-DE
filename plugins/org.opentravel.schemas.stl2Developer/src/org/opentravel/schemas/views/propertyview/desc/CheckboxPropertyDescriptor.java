/*
 * Copyright (c) 2013, Sabre Inc.
 */
package org.opentravel.schemas.views.propertyview.desc;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.opentravel.schemas.views.propertyview.editors.CheckboxCellEditor;

/**
 * @author Pawel Jedruch
 * 
 */
public class CheckboxPropertyDescriptor extends PropertyDescriptor implements
        IFormPropertyDescriptor, IReadonlyPropertyDescriptor {

    private boolean isReadonly = false;

    public CheckboxPropertyDescriptor(Object id, String displayName) {
        super(id, displayName);
    }

    @Override
    public CellEditor createPropertyEditor(FormToolkit toolkit) {
        return new CheckboxCellEditor(toolkit);
    }

    @Override
    public GridData getCustomGridData() {
        return GridDataFactory.fillDefaults().hint(100, SWT.DEFAULT).create();
    }

    @Override
    public boolean isReadonly() {
        return isReadonly;
    }

    public void setReadonly(boolean isReadonly) {
        this.isReadonly = isReadonly;
    }

}
