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

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.opentravel.schemas.widgets.WidgetFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.opentravel.schemacompiler.model.TLContext;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLLibrary;

/**
 * @author Agnieszka Janowska
 * 
 */
public class EditContextWizardPage extends WizardPage {

    private static final Logger LOGGER = LoggerFactory.getLogger(EditContextWizardPage.class);

    private Text contextIdText;
    private Combo applicationCombo;
    private Text descriptionText;
    private final TLLibrary library;
    private final TLContext contextObject;
    private final FormValidator validator;

    protected EditContextWizardPage(final String pageName, final String title,
            final TLLibrary library, final TLContext context, final FormValidator validator) {
        super(pageName, title, null);
        this.library = library;
        this.validator = validator;
        contextObject = context;
    }

    @Override
    public void createControl(final Composite parent) {
        final GridLayout layout = new GridLayout();
        layout.numColumns = 2;

        final Composite container = new Composite(parent, SWT.BORDER);// parent;
        container.setLayout(layout);

        final GridData singleColumnGD = new GridData();
        singleColumnGD.horizontalSpan = 1;
        singleColumnGD.horizontalAlignment = SWT.FILL;
        singleColumnGD.grabExcessHorizontalSpace = true;

        final GridData multiTextGD = new GridData();
        multiTextGD.horizontalSpan = 2;
        multiTextGD.horizontalAlignment = SWT.FILL;
        multiTextGD.verticalAlignment = SWT.FILL;
        multiTextGD.grabExcessHorizontalSpace = true;
        multiTextGD.grabExcessVerticalSpace = true;

        final Label applicationLabel = new Label(container, SWT.NONE);
        applicationLabel.setText("Application context:");
        applicationCombo = WidgetFactory.createCombo(container, SWT.DROP_DOWN | SWT.V_SCROLL);
        int counter = 0;
        for (final TLContext context : library.getContexts()) {
            applicationCombo.add(context.getApplicationContext());
            if (context.getContextId().equals(contextObject.getContextId())) {
                applicationCombo.select(counter);
            }
            counter++;
        }
        applicationCombo.setLayoutData(singleColumnGD);
        applicationCombo.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(final ModifyEvent e) {
                final String applicationContext = applicationCombo.getText();
                setApplication(applicationContext);
                updateView(applicationContext);
                validate();
            }
        });

        final Label contextIdLabel = new Label(container, SWT.NONE);
        contextIdLabel.setText("Context ID:");
        contextIdText = WidgetFactory.createText(container, SWT.SINGLE | SWT.BORDER);
        contextIdText.setLayoutData(singleColumnGD);
        contextIdText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(final ModifyEvent e) {
                setContextId(contextIdText.getText());
                validate();
            }

        });

        final Label descriptionLabel = new Label(container, SWT.NONE);
        descriptionLabel.setText("Description:");
        descriptionText = WidgetFactory.createText(container, SWT.MULTI | SWT.BORDER);
        descriptionText.setLayoutData(multiTextGD);
        descriptionText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(final ModifyEvent e) {
                setComments(descriptionText.getText());
            }

        });

        updateView(contextObject.getApplicationContext());
        setControl(container);
        setPageComplete(false);
    }

    private void setApplication(final String text) {
        contextObject.setApplicationContext(text);
    }

    private void setContextId(final String namespace) {
        contextObject.setContextId(namespace);
    }

    private void setComments(final String comments) {
        TLDocumentation doc = contextObject.getDocumentation();
        if (doc == null) {
            doc = new TLDocumentation();
            contextObject.setDocumentation(doc);
        }
        doc.setDescription(comments);
    }

    private void validate() {
        boolean complete = true;
        String message = null;
        int level = INFORMATION;
        try {
            validator.validate();
        } catch (final ValidationException e) {
            message = e.getMessage();
            level = ERROR;
            complete = false;
            LOGGER.debug("Validation output " + e.getMessage());
        }
        setPageComplete(complete);
        setMessage(message, level);
        getWizard().getContainer().updateButtons();
    }

    private void updateView(final String appCtx) {
        final TLContext context = getContext(appCtx);
        if (context != null) {
            contextIdText.setText(context.getContextId());
            final TLDocumentation doc = context.getDocumentation();
            if (doc != null) {
                descriptionText.setText(doc.getDescription());
            } else {
                descriptionText.setText("");
            }
        }
    }

    private TLContext getContext(final String appCtx) {
        for (final TLContext ctx : library.getContexts()) {
            if (ctx.getApplicationContext().equals(appCtx)) {
                return ctx;
            }
        }
        return null;
    }

}
