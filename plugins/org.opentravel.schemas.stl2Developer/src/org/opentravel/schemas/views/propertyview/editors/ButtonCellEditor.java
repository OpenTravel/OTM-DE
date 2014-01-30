
package org.opentravel.schemas.views.propertyview.editors;

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
public class ButtonCellEditor extends FormCellEditor {

    private FormToolkit toolkit;
    private Button button;

    public ButtonCellEditor(FormToolkit toolkit) {
        super();
        this.toolkit = toolkit;
    }

    @Override
    protected Control createControl(Composite parent) {
        button = toolkit.createButton(parent, "", SWT.FLAT);
        attachListeners(button);
        return button;
    }

    /**
     * @param checkBox2
     */
    private void attachListeners(Button checkBox) {
        checkBox.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                ButtonCellEditor.this.fireApplyEditorValue();
            }

        });
    }

    @Override
    protected Object doGetValue() {
        return button.getText();
    }

    @Override
    protected void doSetFocus() {
        button.setFocus();
    }

    @Override
    protected void doSetValue(Object value) {
        button.setText((String) value);
    }

}
