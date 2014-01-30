
package org.opentravel.schemas.views.propertyview.editors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * @author Pawel Jedruch
 * 
 */
public class FormTextCellEditor extends FormCellEditor {

    private FormToolkit toolkit;
    private Text text;
    private boolean isMultiline;

    public FormTextCellEditor(FormToolkit toolkit) {
        super();
        this.toolkit = toolkit;
    }

    public FormTextCellEditor(FormToolkit toolkit, boolean isMultiline) {
        super();
        this.toolkit = toolkit;
        this.isMultiline = isMultiline;
    }

    @Override
    protected Control createControl(Composite parent) {
        int style = isMultiline ? SWT.MULTI | SWT.WRAP | SWT.V_SCROLL : SWT.SINGLE;
        text = toolkit.createText(parent, "", style);
        attachListeners(text);
        return text;
    }

    /**
     * @param text
     */
    private void attachListeners(final Text text) {
        text.addSelectionListener(new SelectionAdapter() {
            @Override
            // fired on Enter pressed
            public void widgetDefaultSelected(SelectionEvent e) {
                fireApplyEditorValue();
            }
        });

        text.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent keyEvent) {
                if (keyEvent.character == SWT.CR) {
                    // Enter is handled in widgetDefaultSelected.
                    // An exception is made for Ctrl+Enter for multi-line texts, since
                    // a default selection event is not sent in this case.
                    if (text != null && !text.isDisposed() && (text.getStyle() & SWT.MULTI) != 0) {
                        if ((keyEvent.stateMask & SWT.CTRL) != 0) {
                            FormTextCellEditor.this.keyReleaseOccured(keyEvent);
                        }
                    }
                    return;
                }
                FormTextCellEditor.this.keyReleaseOccured(keyEvent);
            }
        });

        text.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                FormTextCellEditor.this.fireApplyEditorValue();
            }
        });
    }

    @Override
    protected Object doGetValue() {
        return text.getText();
    }

    @Override
    protected void doSetFocus() {
        text.setFocus();
    }

    @Override
    protected void doSetValue(Object value) {
        if (value == null) {
            value = "";
        }
        text.setText((String) value);
    }

}
