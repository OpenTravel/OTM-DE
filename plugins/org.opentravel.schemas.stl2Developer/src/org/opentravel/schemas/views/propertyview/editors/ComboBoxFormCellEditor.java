/*
 * Copyright (c) 2013, Sabre Inc.
 */
package org.opentravel.schemas.views.propertyview.editors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.opentravel.schemas.widgets.WidgetFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Pawel Jedruch
 * 
 */
public class ComboBoxFormCellEditor extends FormCellEditor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComboBoxFormCellEditor.class);
    private CCombo combo;
    private List<String> items;
    private boolean readonly;

    public ComboBoxFormCellEditor(FormToolkit toolkit, Collection<String> items, boolean readonly) {
        super();
        this.items = new ArrayList<String>(items);
        this.readonly = readonly;
    }

    @Override
    protected Control createControl(Composite parent) {
        int style = SWT.DROP_DOWN | SWT.V_SCROLL;
        style |= readonly ? SWT.READ_ONLY : SWT.NONE;
        combo = WidgetFactory.createCCombo(parent, style);
        combo.setItems(items.toArray(new String[items.size()]));
        attachListeners(combo);
        return combo;
    }

    private void attachListeners(final CCombo widget) {
        widget.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                ComboBoxFormCellEditor.this.fireApplyEditorValue();
            }
        });

        widget.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                ComboBoxFormCellEditor.this.fireApplyEditorValue();
            }

        });

        if (!readonly) {
            widget.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent keyEvent) {
                    ComboBoxFormCellEditor.this.keyReleaseOccured(keyEvent);
                }
            });
        }
    }

    @Override
    protected Object doGetValue() {
        int idx = combo.getSelectionIndex();
        if (idx > -1)
            return items.get(idx);
        return combo.getText();
    }

    @Override
    protected void doSetFocus() {
        combo.setFocus();
    }

    @Override
    protected void doSetValue(Object value) {
        int idx = items.indexOf(value);
        if (idx < 0) {
            LOGGER.warn("Invalid value: [" + value + "]. Supporeted values: " + items);
            // combo.clearSelection();
        } else {
            combo.select(idx);
        }
    }

}
