/*
 * Copyright (c) 2012, Sabre Corporation and affiliates.
 * All Rights Reserved.
 * Use is subject to license agreement.
 */
package com.sabre.schemas.stl2developer;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.sabre.schemacompiler.security.LibrarySecurityHandler;
import com.sabre.schemacompiler.security.ProtectedNamespaceCredentials;
import com.sabre.schemacompiler.security.ProtectedNamespaceGroup;
import com.sabre.schemacompiler.security.ProtectedNamespaceRegistry;
import com.sabre.schemas.widgets.WidgetFactory;

/**
 * Preference page that allows the user to enter security credentials for one or more protected
 * namespace groupings.
 * 
 * @author S. Livezey
 */
public class ProtectedNamespacePreferencePage extends PreferencePage implements
        IWorkbenchPreferencePage {

    private ProtectedNamespaceRegistry nsRegistry = ProtectedNamespaceRegistry.getInstance();

    private ProtectedNamespaceCredentials userCredentials;
    private ProtectedNamespaceGroup selectedGroup;

    private Combo groupCombo;
    private Label groupTitleLabel;
    private List groupNamespacesList;
    private Text userIdText;
    private Text passwordText;

    /**
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createContents(Composite parent) {
        GridData gData;
        Label lbl;

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(2, false));
        composite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL
                | GridData.GRAB_HORIZONTAL));

        new Label(composite, SWT.LEFT).setText("Namespace Groupings:");
        groupCombo = WidgetFactory.createCombo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
        int groupCount = 0;

        for (ProtectedNamespaceGroup nsGroup : nsRegistry.getProtectedNamespaces()) {
            groupCombo.add(nsGroup.getGroupId());
            groupCount++;
        }
        groupCombo.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleGroupSelectionChange();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
        if (groupCount > 0)
            groupCombo.select(0);

        new Label(composite, SWT.LEFT).setText("Group Title:");
        groupTitleLabel = new Label(composite, SWT.LEFT);
        groupTitleLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));

        lbl = new Label(composite, SWT.LEFT);
        lbl.setText("Protected Namespaces:");
        lbl.setLayoutData(gData = new GridData(GridData.HORIZONTAL_ALIGN_FILL));
        gData.horizontalSpan = 2;
        groupNamespacesList = new List(composite, SWT.BORDER | SWT.SINGLE | SWT.READ_ONLY
                | SWT.V_SCROLL);
        groupNamespacesList.setLayoutData(gData = new GridData(GridData.HORIZONTAL_ALIGN_FILL
                | GridData.GRAB_HORIZONTAL));
        gData.horizontalSpan = 2;
        gData.heightHint = 60;

        lbl = new Label(composite, SWT.LEFT);
        lbl.setLayoutData(gData = new GridData(GridData.HORIZONTAL_ALIGN_FILL));
        gData.horizontalSpan = 2;

        new Label(composite, SWT.LEFT).setText("User ID:");
        userIdText = WidgetFactory.createText(composite, SWT.BORDER);
        userIdText.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL
                | GridData.GRAB_HORIZONTAL));
        userIdText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                handleCredentialsModified();
            }
        });

        new Label(composite, SWT.LEFT).setText("Password:");
        passwordText = WidgetFactory.createText(composite, SWT.BORDER);
        passwordText.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL
                | GridData.GRAB_HORIZONTAL));
        passwordText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                handleCredentialsModified();
            }
        });

        handleGroupSelectionChange();
        return composite;
    }

    /**
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    @Override
    public boolean performOk() {

        // Apply the new credentials to the compiler's run-time configuration
        LibrarySecurityHandler.setUserCredentials(userCredentials);

        return super.performOk();
    }

    /**
     * Called when the user changes their selection in the group combo widget.
     */
    public void handleGroupSelectionChange() {
        int selectedIndex = groupCombo.getSelectionIndex();
        String selectedGroupId = null;

        if (selectedIndex >= 0) {
            selectedGroupId = groupCombo.getItem(selectedIndex);
            selectedGroup = null;

            for (ProtectedNamespaceGroup nsGroup : nsRegistry.getProtectedNamespaces()) {
                if (selectedGroupId.equals(nsGroup.getGroupId())) {
                    selectedGroup = nsGroup;
                    break;
                }
            }
        }

        if (selectedGroup != null) {
            groupTitleLabel.setText(selectedGroup.getGroupTitle());
            groupNamespacesList.setItems(selectedGroup.getProtectedNamespaceUris().toArray(
                    new String[0]));
        } else {
            groupTitleLabel.setText("");
            groupNamespacesList.setItems(new String[0]);
        }

        if (userCredentials.getNamespaceGroups().contains(selectedGroupId)) {
            String userId = userCredentials.getUserId(selectedGroupId);
            String password = userCredentials.getPassword(selectedGroupId);

            userIdText.setText(userId);
            passwordText.setText(password);

        } else {
            userIdText.setText("");
            passwordText.setText("");
        }
    }

    /**
     * Called when the user modifies the contents of the userId or password text fields are
     * modified.
     */
    public void handleCredentialsModified() {
        int selectedIndex = groupCombo.getSelectionIndex();

        if (selectedIndex >= 0) {
            String selectedGroupId = groupCombo.getItem(groupCombo.getSelectionIndex());

            userCredentials.setCredentials(selectedGroupId, userIdText.getText(),
                    passwordText.getText());
        }
        userIdText.setEnabled(selectedIndex >= 0);
        passwordText.setEnabled(selectedIndex >= 0);
    }

    /**
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    @Override
    public void init(IWorkbench workbench) {
        // No action required
    }

}
