/**
 * Copyright (C) 2014 OpenTravel Alliance (info@opentravel.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.opentravel.schemas.wizards;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * Ask the user if they want to change the type assignments globally, locally or none.
 * 
 * @author Agnieszka Janowska / Dave Hollander
 * 
 */
public class GlobalLocalCancelDialog extends Dialog implements Cancelable {
    private String message;
    // private boolean allSelected = false;
    private GlobalDialogResult result = GlobalDialogResult.CANCEL;

    public enum GlobalDialogResult {
        GLOBAL, LOCAL, NONE, CANCEL
    }

    /**
     * InputDialog constructor
     * 
     * @param parent
     *            the parent
     */
    public GlobalLocalCancelDialog(final Shell parent, final String message) {
        this(parent, message, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
    }

    /**
     * InputDialog constructor
     * 
     * @param parent
     *            the parent
     * @param style
     *            the style
     */
    public GlobalLocalCancelDialog(final Shell parent, final String message, final int style) {
        super(parent, style);
        setText("Namespace Change");
        setMessage(message);
    }

    /**
     * Gets the message
     * 
     * @return String
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the message
     * 
     * @param message
     *            the new message
     */
    public void setMessage(final String message) {
        this.message = message;
    }

    public void open() {
        // Create the dialog window
        final Shell shell = new Shell(getParent(), getStyle());
        shell.setText(getText());
        createContents(shell);
        shell.pack();
        shell.open();
        final Display display = getParent().getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
    }

    /**
     * Creates the dialog's contents
     * 
     * @param shell
     *            the dialog window
     */
    private void createContents(final Shell shell) {
        final GridLayout layout = new GridLayout(3, true);
        shell.setLayout(layout);

        final Label label = new Label(shell, SWT.WRAP);
        label.setText(message);
        GridData data = new GridData();
        data.widthHint = 500;
        data.horizontalSpan = 3;
        label.setLayoutData(data);

        final Label separator = new Label(shell, SWT.NONE);
        data = new GridData();
        data.horizontalSpan = 3;
        separator.setLayoutData(data);

        // // Do they want the answer to apply to all changes?
        // final Button applyToAll = new Button(shell, SWT.CHECK);
        // applyToAll.setText("Apply to all Changes?");
        // applyToAll.setToolTipText("Select this have the Global/Local/Cancel answer apply to all.");
        // data = new GridData(GridData.FILL_HORIZONTAL);
        // applyToAll.setLayoutData(data);
        // applyToAll.addSelectionListener(new SelectionAdapter() {
        // @Override
        // public void widgetSelected(final SelectionEvent event) {
        // if (event.widget instanceof Button) {
        // allSelected(((Button) event.widget).getSelection());
        // }
        // }
        // });

        final Label separator2 = new Label(shell, SWT.NONE);
        data = new GridData();
        data.horizontalSpan = 3;
        separator2.setLayoutData(data);

        final Button globally = new Button(shell, SWT.PUSH);
        globally.setText("Globally");
        data = new GridData(GridData.FILL_HORIZONTAL);
        globally.setLayoutData(data);
        globally.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent event) {
                result = GlobalDialogResult.GLOBAL;
                shell.close();
            }
        });

        final Button locally = new Button(shell, SWT.PUSH);
        locally.setText("Locally");
        data = new GridData(GridData.FILL_HORIZONTAL);
        locally.setLayoutData(data);
        locally.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent event) {
                result = GlobalDialogResult.LOCAL;
                shell.close();
            }
        });

        final Button none = new Button(shell, SWT.PUSH);
        none.setText("None");
        data = new GridData(GridData.FILL_HORIZONTAL);
        none.setLayoutData(data);
        none.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent event) {
                result = GlobalDialogResult.NONE;
                shell.close();
            }
        });

        final Button cancel = new Button(shell, SWT.PUSH);
        cancel.setText("Cancel");
        data = new GridData(GridData.FILL_HORIZONTAL);
        cancel.setLayoutData(data);
        cancel.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent event) {
                result = GlobalDialogResult.CANCEL;
                shell.close();
            }
        });
        shell.setDefaultButton(globally);
    }

    // private void allSelected(boolean b) {
    // allSelected = b;
    // }

    public GlobalDialogResult getResult() {
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.wizards.Cancelable#wasCanceled()
     */
    @Override
    public boolean wasCanceled() {
        return result == GlobalDialogResult.CANCEL;
    }
}
