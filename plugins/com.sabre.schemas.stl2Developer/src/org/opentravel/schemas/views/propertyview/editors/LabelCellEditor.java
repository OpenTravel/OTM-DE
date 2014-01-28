/*
 * Copyright (c) 2013, Sabre Inc.
 */
package org.opentravel.schemas.views.propertyview.editors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * @author Pawel Jedruch
 * 
 */
public class LabelCellEditor extends FormCellEditor {

    private FormToolkit toolkit;
    private Label label;

    public LabelCellEditor(FormToolkit toolkit) {
        super();
        this.toolkit = toolkit;
    }

    @Override
    protected Control createControl(Composite parent) {
        label = toolkit.createLabel(parent, "", SWT.WRAP);
        return label;
    }

    @Override
    protected Object doGetValue() {
        return label.getText();
    }

    @Override
    protected void doSetFocus() {
        label.setFocus();
    }

    @Override
    protected void doSetValue(Object value) {
        label.setText((String) value);
    }

}
