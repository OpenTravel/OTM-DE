/*
 * Copyright (c) 2013, Sabre Inc.
 */
package com.sabre.schemas.views.propertyview.desc;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import com.sabre.schemas.views.propertyview.editors.FormTextCellEditor;

/**
 * @author Pawel Jedruch
 * 
 */
public class TextFormPropertyDescriptor extends PropertyDescriptor implements
        IFormPropertyDescriptor {

    private boolean isMultiline;

    public TextFormPropertyDescriptor(Object id, String displayName) {
        this(id, displayName, false);
    }

    public TextFormPropertyDescriptor(Object id, String displayName, boolean isMultiline) {
        super(id, displayName);
        this.isMultiline = isMultiline;
    }

    @Override
    public CellEditor createPropertyEditor(FormToolkit toolkit) {
        if (isMultiline) {
            return new FormTextCellEditor(toolkit, isMultiline);
        } else {
            return new FormTextCellEditor(toolkit);
        }
    }

    @Override
    public GridData getCustomGridData() {
        if (isMultiline) {
            return GridDataFactory.fillDefaults().hint(SWT.DEFAULT, 60).grab(true, false).create();
        }
        return null;
    }

}
