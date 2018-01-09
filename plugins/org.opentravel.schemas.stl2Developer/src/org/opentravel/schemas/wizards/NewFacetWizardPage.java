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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.stl2developer.ColorProvider;
import org.opentravel.schemas.widgets.FacetViewTablePoster;
import org.opentravel.schemas.widgets.WidgetFactory;
import org.opentravel.schemas.wizards.validators.FormValidator;
import org.opentravel.schemas.wizards.validators.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Agnieszka Janowska
 * 
 */
public class NewFacetWizardPage extends WizardPage {

    private static final Logger LOGGER = LoggerFactory.getLogger(NewFacetWizardPage.class);

    private Table previewTable;
    private FacetViewTablePoster tablePoster;

    private String facetName;

    private final ComponentNode propertyOwner;
    private final FormValidator validator;
    private List<PropertyNode> properties = new ArrayList<PropertyNode>();
    private String defaultName;
    private Text nameText = null;

    protected NewFacetWizardPage(final String pageName, final String title,
            final FormValidator validator, final ComponentNode propertyOwner, String defaultName) {
        super(pageName, title, null);
        this.propertyOwner = propertyOwner;
        this.validator = validator;
        this.defaultName = defaultName;
    }

    @Override
    public void createControl(final Composite parent) {
        parent.setLayout(new GridLayout());
        final GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        final GridData containerGD = new GridData();
        containerGD.horizontalAlignment = SWT.FILL;
        containerGD.verticalAlignment = SWT.FILL;
        containerGD.grabExcessHorizontalSpace = true;
        containerGD.grabExcessVerticalSpace = true;
        containerGD.widthHint = 600;
        containerGD.heightHint = 800;

        final Composite container = new Composite(parent, SWT.BORDER);// parent;
        container.setLayout(layout);
        container.setLayoutData(containerGD);

        final GridData generalGD = new GridData();
        generalGD.horizontalSpan = 1;
        generalGD.horizontalAlignment = SWT.FILL;
        generalGD.grabExcessHorizontalSpace = true;

        final GridData twoColumnsSpanGD = new GridData();
        twoColumnsSpanGD.horizontalSpan = 2;
        twoColumnsSpanGD.horizontalAlignment = SWT.FILL;
        twoColumnsSpanGD.grabExcessHorizontalSpace = true;

        final GridData tableGD = new GridData();
        tableGD.horizontalSpan = 2;
        tableGD.horizontalAlignment = SWT.FILL;
        tableGD.verticalAlignment = SWT.FILL;
        tableGD.grabExcessHorizontalSpace = true;
        tableGD.grabExcessVerticalSpace = true;

        final GridData rightGD = new GridData();
        rightGD.horizontalSpan = 1;
        rightGD.horizontalAlignment = SWT.RIGHT;
        rightGD.grabExcessHorizontalSpace = true;

        final GridData leftGD = new GridData();
        leftGD.horizontalSpan = 1;
        leftGD.horizontalAlignment = SWT.LEFT;

        // Set the name
        final Label nameLabel = new Label(container, SWT.NONE);
        nameLabel.setText("Name:");
        nameLabel.setLayoutData(leftGD);
        nameText = WidgetFactory.createText(container, SWT.BORDER | SWT.SINGLE);

        final GridData nameGD = new GridData();
        nameGD.horizontalSpan = 1;
        nameGD.horizontalAlignment = SWT.FILL;
        nameGD.grabExcessHorizontalSpace = true;
        if (defaultName != null) {
            nameText.setText(defaultName);
        }
        nameText.setToolTipText("Enter a name for the facet.");

        nameText.setLayoutData(nameGD);
        nameText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(final ModifyEvent e) {
                facetName = nameText.getText();
                validate(); // TODO - add unique name requirement to validator
            }
        });
        nameText.selectAll();
        facetName = nameText.getText();

        // Property selection
        final Label separator = new Label(container, SWT.NONE);
        separator.setLayoutData(twoColumnsSpanGD);

        final Label propertiesLabel = new Label(container, SWT.NONE);
        propertiesLabel.setText("Select properties for facet:");
        propertiesLabel.setLayoutData(leftGD);
        previewTable = createPreviewTable(container);
        previewTable.setLayoutData(tableGD);
        previewTable.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                if (e.detail == SWT.CHECK) {
                    updateProperties();
                }
            }

            @Override
            public void widgetDefaultSelected(final SelectionEvent e) {
            }

        });
        tablePoster = new FacetViewTablePoster(previewTable, new ColorProvider(parent.getDisplay()));

        updateView();
        validate();
        setControl(container);
        setPageComplete(true);
    }

    private Table createPreviewTable(final Composite container) {
        final TableViewer viewer = new TableViewer(container, SWT.MULTI | SWT.CHECK | SWT.H_SCROLL
                | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
        final Table table = viewer.getTable();

        return table;
    }

    public List<PropertyNode> getProperties() {
        return properties;
    }

    private void updateProperties() {
        final TableItem[] tia = previewTable.getItems();
        for (int i = 0; i < tia.length; i++) {
            if (tia[i].getChecked()) {
                previewTable.select(i);
            }
        }
        final List<PropertyNode> selected = new ArrayList<PropertyNode>();
        final TableItem[] items = previewTable.getSelection();
        for (final TableItem item : items) {
            if (item.getData() instanceof PropertyNode) {
                selected.add((PropertyNode) item.getData());
            }
        }
        properties = selected;
    }

    private void updateView() {
        tablePoster.postTable(propertyOwner);
    }

    private void validate() {
        boolean complete = true;
        String message = null;
        try {
            validator.validate();
        } catch (final ValidationException e) {
            message = e.getMessage();
            complete = false;
            LOGGER.info("Validation output " + e.getMessage());
        }
        setPageComplete(complete);
        setMessage(message, ERROR);
        getWizard().getContainer().updateButtons();
    }

    public INode getPropertyOwner() {
        return propertyOwner;
    }

    public String getFacetName() {
        return facetName;
    }

}
