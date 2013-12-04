/*
 * Copyright (c) 2013, Sabre Inc.
 */
package com.sabre.schemas.views.propertyview.editors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * @author Pawel Jedruch
 * 
 */
public class CheckboxCellEditor extends FormCellEditor {

    private FormToolkit toolkit;
    private Button checkBox;

    public CheckboxCellEditor(FormToolkit toolkit) {
        super();
        this.toolkit = toolkit;
    }

    @Override
    protected Control createControl(Composite parent) {
        checkBox = toolkit.createButton(parent, "", SWT.CHECK);
        attachListeners(checkBox);
        return checkBox;
    }

    /**
     * @param checkBox2
     */
    private void attachListeners(Button checkBox) {
        checkBox.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                CheckboxCellEditor.this.fireApplyEditorValue();
            }

        });
    }

    @Override
    protected Object doGetValue() {
        return checkBox.getSelection();
    }

    @Override
    protected void doSetFocus() {
        checkBox.setFocus();
    }

    @Override
    protected void doSetValue(Object value) {
        checkBox.setSelection((Boolean) value);
    }

}
