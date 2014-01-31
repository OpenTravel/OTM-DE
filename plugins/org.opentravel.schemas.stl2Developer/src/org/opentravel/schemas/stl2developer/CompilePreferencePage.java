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
package org.opentravel.schemas.stl2developer;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.opentravel.schemas.preferences.CompilerPreferences;
import org.opentravel.schemas.widgets.WidgetFactory;

import org.opentravel.schemacompiler.ioc.CompilerExtensionRegistry;

/**
 * Preference page that allows the user to select the compiler bindings and configure the
 * compilation task options.
 * 
 * @author S. Livezey
 */
public class CompilePreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

    private CompilerPreferences compilerPreferences;
    private IPreferenceStore preferenceStore;
    private Combo extensionCombo;
    private Button compileSchemasCheckbox;
    private Button compileServicesCheckbox;
    private Text serviceEndpointUrlText;
    private Button generateExamplesCheckbox;
    private Button examplesMaxDetailsCheckbox;
    private Spinner exampleMaxRepeatSpinner;
    private Spinner exampleMaxDepthSpinner;

    /**
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createContents(final Composite parent) {
        GridData gData;

        // Load the compiler preferences from the workbench file, or use the default values
        preferenceStore = CompilerPreferences.loadPreferenceStore();
        compilerPreferences = new CompilerPreferences(preferenceStore);

        // Initialize the GUI components of the preference page
        final Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(2, false));
        composite.setLayoutData(gData = new GridData(GridData.HORIZONTAL_ALIGN_FILL
                | GridData.GRAB_HORIZONTAL));

        new Label(composite, SWT.LEFT).setText("Binding Style (requires restart) :");
        extensionCombo = WidgetFactory.createCombo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
        final String[] extensionIds = CompilerExtensionRegistry.getAvailableExtensionIds().toArray(
                new String[0]);
        final String activeExtension = CompilerExtensionRegistry.getActiveExtension();
        int extensionSelection = 0;

        for (int i = 0; i < extensionIds.length; i++) {
            if (extensionIds[i].equals(activeExtension)) {
                extensionSelection = i;
            }
            extensionCombo.add(extensionIds[i]);
        }
        extensionCombo.select(extensionSelection);

        new Label(composite, SWT.LEFT).setText("Compile Schemas:");
        compileSchemasCheckbox = new Button(composite, SWT.CHECK);
        compileSchemasCheckbox.setSelection(compilerPreferences.isCompileSchemas());

        new Label(composite, SWT.LEFT).setText("Compile Services:");
        compileServicesCheckbox = new Button(composite, SWT.CHECK);
        compileServicesCheckbox.setSelection(compilerPreferences.isCompileServices());

        final String serviceEndpoint = compilerPreferences.getServiceEndpointUrl();
        new Label(composite, SWT.LEFT).setText("Service Endpoint:");
        serviceEndpointUrlText = WidgetFactory.createText(composite, SWT.BORDER);
        serviceEndpointUrlText.setLayoutData(gData = new GridData(GridData.HORIZONTAL_ALIGN_FILL
                | GridData.GRAB_HORIZONTAL));
        serviceEndpointUrlText.setText((serviceEndpoint == null) ? "" : serviceEndpoint);
        gData.widthHint = 200;

        new Label(composite, SWT.LEFT).setLayoutData(gData = new GridData(
                GridData.HORIZONTAL_ALIGN_FILL));
        gData.horizontalSpan = 2;

        generateExamplesCheckbox = new Button(composite, SWT.CHECK);
        generateExamplesCheckbox.setText("Generate Example XML");
        generateExamplesCheckbox
                .setLayoutData(gData = new GridData(GridData.HORIZONTAL_ALIGN_FILL));
        gData.horizontalSpan = 2;
        generateExamplesCheckbox.setSelection(compilerPreferences.isGenerateExamples());
        generateExamplesCheckbox.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                handleGenerateExamplesSelectionChange();
            }

            @Override
            public void widgetDefaultSelected(final SelectionEvent e) {
                handleGenerateExamplesSelectionChange();
            }
        });

        final Group exampleGroup = new Group(composite, SWT.NONE);
        exampleGroup.setLayout(new GridLayout(2, false));
        exampleGroup.setLayoutData(gData = new GridData(GridData.HORIZONTAL_ALIGN_FILL));
        gData.horizontalSpan = 2;

        new Label(exampleGroup, SWT.LEFT).setText("Max Details:");
        examplesMaxDetailsCheckbox = new Button(exampleGroup, SWT.CHECK);
        examplesMaxDetailsCheckbox.setSelection(compilerPreferences
                .isGenerateMaxDetailsForExamples());

        new Label(exampleGroup, SWT.LEFT).setText("Max Repeat Count:");
        exampleMaxRepeatSpinner = new Spinner(exampleGroup, SWT.BORDER);
        exampleMaxRepeatSpinner.setMinimum(1);
        exampleMaxRepeatSpinner.setMaximum(10);
        exampleMaxRepeatSpinner.setSelection(compilerPreferences.getExampleMaxRepeat());

        new Label(exampleGroup, SWT.LEFT).setText("Max Recursion Depth:");
        exampleMaxDepthSpinner = new Spinner(exampleGroup, SWT.BORDER);
        exampleMaxDepthSpinner.setMinimum(1);
        exampleMaxDepthSpinner.setMaximum(10);
        exampleMaxDepthSpinner.setSelection(compilerPreferences.getExampleMaxDepth());

        handleGenerateExamplesSelectionChange();

        return composite;
    }

    /**
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    @Override
    public boolean performOk() {
        // Update the preferences using the settings from the page controls
        compilerPreferences.setCompilerExtensionId(extensionCombo.getItem(extensionCombo
                .getSelectionIndex()));
        compilerPreferences.setCompileSchemas(compileSchemasCheckbox.getSelection());
        compilerPreferences.setCompileServices(compileServicesCheckbox.getSelection());
        compilerPreferences.setServiceEndpointUrl(serviceEndpointUrlText.getText());
        compilerPreferences.setGenerateExamples(generateExamplesCheckbox.getSelection());
        compilerPreferences.setGenerateMaxDetailsForExamples(examplesMaxDetailsCheckbox
                .getSelection());
        compilerPreferences.setExampleMaxRepeat(exampleMaxRepeatSpinner.getSelection());
        compilerPreferences.setExampleMaxDepth(exampleMaxDepthSpinner.getSelection());

        // Save the preferences and switch compiler extensions, if necessary
        compilerPreferences.saveTaskOptions(preferenceStore);
        CompilerExtensionRegistry.setActiveExtension(compilerPreferences.getCompilerExtensionId());
        return super.performOk();
    }

    /**
     * Called when the selection status of the 'generateExamplesCheckbox' has been modified by the
     * user.
     */
    private void handleGenerateExamplesSelectionChange() {
        final boolean enableStatus = generateExamplesCheckbox.getSelection();

        examplesMaxDetailsCheckbox.setEnabled(enableStatus);
        exampleMaxRepeatSpinner.setEnabled(enableStatus);
        exampleMaxDepthSpinner.setEnabled(enableStatus);
    }

    /**
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    @Override
    public void init(final IWorkbench workbench) {
        // No action required
    }

}
