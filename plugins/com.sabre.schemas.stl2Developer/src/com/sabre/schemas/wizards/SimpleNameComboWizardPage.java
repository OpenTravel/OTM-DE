/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.wizards;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.sabre.schemas.widgets.WidgetFactory;

/**
 * @author Agnieszka Janowska
 * 
 */
public class SimpleNameComboWizardPage extends SimpleNameWizardPage {

    private Combo name;
    private final List<String> availableValues;

    /**
     * @param pageName
     * @param title
     * @param validator
     */
    protected SimpleNameComboWizardPage(final String pageName, final String title,
            final FormValidator validator, final List<String> availableValues) {
        super(pageName, title, validator);
        this.availableValues = availableValues;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.sabre.schemas.wizards.SimpleNameWizardPage#createControl(org.eclipse.swt.widgets.Composite
     * )
     */
    @Override
    public void createControl(final Composite parent) {
        final GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        final Composite container = new Composite(parent, SWT.NONE);
        container.setLayout(layout);

        final GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        final Label label = new Label(container, SWT.NULL);
        label.setText(getFieldLabel());
        name = WidgetFactory.createCombo(container, SWT.BORDER | SWT.SINGLE | SWT.READ_ONLY);
        name.setToolTipText(getFieldToolTip());
        name.addModifyListener(this);
        name.setLayoutData(gd);
        for (final String s : availableValues) {
            name.add(s);
        }
        for (int i = 0; i < name.getItemCount(); i++) {
            if (name.getItem(i).equals(getDefault())) {
                name.select(i);
                break;
            }
        }

        setControl(container);
        setPageComplete(false);
    }

    @Override
    public String getText() {
        return name.getText();
    }

}
