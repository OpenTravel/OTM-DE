/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.actions;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Text;

import com.sabre.schemas.node.Node;
import com.sabre.schemas.properties.Messages;
import com.sabre.schemas.properties.StringProperties;
import com.sabre.schemas.stl2developer.DialogUserNotifier;
import com.sabre.schemas.stl2developer.MainWindow;

/**
 * Action that handles the selection and assignment of extensions for cores, business objects,
 * operations, and extension point facets.
 * 
 * @author S. Livezey
 */
public class ClearExtendsAction extends OtmAbstractAction {

    private Text extendsField;
    private Button clearButton;

    public ClearExtendsAction(MainWindow mainWindow, StringProperties props, Text extendsField,
            Button clearButton) {
        super(mainWindow, props);
        this.extendsField = extendsField;
        this.clearButton = clearButton;

        clearButton.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                run();
            }

            @Override
            public void widgetDefaultSelected(final SelectionEvent e) {
            }

        });
    }

    /**
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        boolean confirmClear = DialogUserNotifier.openConfirm(Messages.getString("OtmW.352"),
                Messages.getString("OtmW.353"));

        if (confirmClear) {
            Node n = mc.getSelectedNode_TypeView();
            n.setExtendsType(null);
            n.resetInheritedChildren();
            mc.refresh();
        }
    }

    /**
     * @see org.eclipse.jface.action.Action#setEnabled(boolean)
     */
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (extendsField != null)
            extendsField.setEnabled(enabled);
        if (clearButton != null)
            clearButton.setEnabled(enabled);
    }

}
