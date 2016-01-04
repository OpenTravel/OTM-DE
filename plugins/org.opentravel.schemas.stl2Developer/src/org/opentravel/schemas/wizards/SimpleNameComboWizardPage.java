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

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.opentravel.schemas.widgets.WidgetFactory;
import org.opentravel.schemas.wizards.validators.FormValidator;

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
     * org.opentravel.schemas.wizards.SimpleNameWizardPage#createControl(org.eclipse.swt.widgets.Composite
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
