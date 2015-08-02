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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.opentravel.schemas.controllers.DefaultProjectController;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.preferences.GeneralPreferencePage;
import org.opentravel.schemas.properties.Messages;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.trees.repository.RepositoryNode;
import org.opentravel.schemas.trees.repository.RepositoryNode.RepositoryRootNsNode;
import org.opentravel.schemas.trees.repository.RepositoryNode.RepositoryInstanceNode;
import org.opentravel.schemas.widgets.WidgetFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Agnieszka Janowska
 * 
 */
public class NewProjectWizardPage extends WizardPage {

    private static final Logger LOGGER = LoggerFactory.getLogger(NewProjectWizardPage.class);

    private static final String DEFAULT_EXTENSION = DefaultProjectController.PROJECT_EXT;

    private String projectFile = "";
    private Text fileText;
    private Button fileButton;

    private Combo namespaceCombo;

    private String projectName = "";
    private Text nameText;
    private Combo nsExtensionCombo;

    private Text descriptionText;

    private final FormValidator validator;

    private Map<String, Collection<String>> managedNamespaces = Collections.emptyMap();
    private String selectedRoot;
    private String selectedExt;

    private String defaultName = "";

    protected NewProjectWizardPage(final String pageName, final String title,
            final FormValidator validator) {
        super(pageName, title, null);
        this.validator = validator;
    }

    private Map<String, Collection<String>> getManagedNamespaces(Collection<Node> selectedNodes) {
        Map<String, Collection<String>> ret = new LinkedHashMap<String, Collection<String>>();
        for (Node n : selectedNodes) {
            if (n instanceof RepositoryNode) {
                RepositoryInstanceNode r = getRepositoryParent((RepositoryNode) n);
                Map<String, Collection<String>> map = getNamespacesAndExt(r);
                ret.putAll(map);
            }
        }
        return ret;
    }

    private Map<String, Collection<String>> getNamespacesAndExt(RepositoryInstanceNode r) {
        Map<String, Collection<String>> ret = new LinkedHashMap<String, Collection<String>>();
        for (Node child : r.getChildren()) {
            RepositoryRootNsNode nn = (RepositoryRootNsNode) child;
            List<String> extensions = getExtensions(nn);
            ret.put(nn.getRootBasename(), extensions);
        }
        return ret;
    }

    private List<String> getExtensions(RepositoryRootNsNode nn) {
        List<String> ret = new ArrayList<String>();
        ret.add("");
        if (!nn.wasVisited())
            return ret;
        for (Node child : nn.getChildren()) {
            if (child instanceof RepositoryRootNsNode) {
                ret.add(child.getName());
            }
        }
        return ret;
    }

    private RepositoryInstanceNode getRepositoryParent(RepositoryNode node) {
        Node parent = node;
        while (parent != null) {
            if (parent instanceof RepositoryInstanceNode) {
                return (RepositoryInstanceNode) parent;
            }
            parent = parent.getParent();
        }
        return null;

    }

    /** Return user entered values. */
    @Override
    public String getDescription() {
        return descriptionText.getText();
    }

    /** Return user entered values. */
    public String getPath() {
        File f = new File(projectFile);
        try {
            return f.getCanonicalPath();
        } catch (IOException e) {
            // Should never occures. The path will be validated before calling this method.
            LOGGER.warn("Seems to be the validator didn't run");
        }
        return projectFile;
    }

    /** Return user entered values. */
    public String getProjectName() {
        return nameText.getText();
    }

    /** Return user entered values. */
    public String getNamespace() {
        String ns = namespaceCombo.getText();
        String ext = nsExtensionCombo.getText();
        if (ns.isEmpty()) {
            return ns;
        }
        return ProjectNode.appendExtension(ns, ext);
    }

    @Override
    public void createControl(final Composite parent) {
        RepositoryNode root = OtmRegistry.getMainController().getRepositoryController().getRoot();
        managedNamespaces = getManagedNamespaces(root.getChildren());

        final GridLayout layout = new GridLayout();
        layout.numColumns = 3;

        final Composite container = new Composite(parent, SWT.BORDER);// parent;
        container.setLayout(layout);

        final GridData singleColumnGD = new GridData();
        singleColumnGD.horizontalSpan = 1;
        singleColumnGD.horizontalAlignment = SWT.FILL;
        singleColumnGD.grabExcessHorizontalSpace = true;

        final GridData twoColumnsGD = new GridData();
        twoColumnsGD.horizontalSpan = 2;
        twoColumnsGD.horizontalAlignment = SWT.FILL;
        twoColumnsGD.grabExcessHorizontalSpace = true;

        final GridData threeColumnsGD = new GridData();
        threeColumnsGD.horizontalSpan = 3;
        threeColumnsGD.horizontalAlignment = SWT.FILL;
        threeColumnsGD.grabExcessHorizontalSpace = true;

        final GridData multiTextGD = new GridData();
        multiTextGD.horizontalSpan = 3;
        multiTextGD.horizontalAlignment = SWT.FILL;
        multiTextGD.verticalAlignment = SWT.FILL;
        multiTextGD.grabExcessHorizontalSpace = true;
        multiTextGD.grabExcessVerticalSpace = true;

        // File Path
        final Label fileLabel = new Label(container, SWT.NONE);
        fileLabel.setText(Messages.getString("wizard.newProject.pathField.label"));
        fileLabel.setToolTipText(Messages.getString("wizard.newProject.pathField.tooltip"));
        fileText = WidgetFactory.createText(container, SWT.SINGLE | SWT.BORDER);
        fileText.setLayoutData(singleColumnGD);
        fileText.setToolTipText(Messages.getString("wizard.newProject.pathField.tooltip"));
        fileText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(final ModifyEvent e) {
                final String fullPath = fileText.getText();
                nameText.setText(removeExtension(new File(fullPath).getName(), DEFAULT_EXTENSION));
                projectFile = fullPath;
                validate();
            }

        });
        fileButton = new Button(container, SWT.PUSH);
        fileButton.setText("...");
        fileButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                final FileDialog fd = new FileDialog(getShell(), SWT.SAVE);
                fd.setText("Select/Enter Project File");
                fd.setFilterExtensions(new String[] { "*." + DEFAULT_EXTENSION });
                final String fileName = fd.open();
                if (fileName != null) {
                    final String fullPath = appendExtension(fileName, DEFAULT_EXTENSION);
                    fileText.setText(fullPath);
                }
            }

        });

        // Name field
        final Label nameLabel = new Label(container, SWT.NONE);
        nameLabel.setText(Messages.getString("wizard.newProject.nameField.label"));
        nameLabel.setToolTipText(Messages.getString("wizard.newProject.nameField.tooltip"));
        nameText = WidgetFactory.createText(container, SWT.SINGLE | SWT.BORDER);
        nameText.setText(defaultName);
        nameText.setLayoutData(twoColumnsGD);
        nameText.setToolTipText(Messages.getString("wizard.newProject.nameField.tooltip"));
        nameText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(final ModifyEvent e) {
                validate();
            }

        });

        // Namespace Combo
        final Label namespaceLabel = new Label(container, SWT.NONE);
        namespaceLabel.setText(Messages.getString("wizard.newProject.namespace.label"));
        namespaceLabel.setToolTipText(Messages.getString("wizard.newProject.namespace.tooltip"));
        if (GeneralPreferencePage.areNamespacesManaged()) {
            namespaceCombo = WidgetFactory.createCombo(container, SWT.DROP_DOWN | SWT.V_SCROLL);
        }

        else
            namespaceCombo = WidgetFactory.createCombo(container, SWT.DROP_DOWN | SWT.V_SCROLL);
        if (managedNamespaces.isEmpty()) {
            populteCombo(namespaceCombo, OtmRegistry.getMainController().getRepositoryController()
                    .getRootNamespaces());
        } else {
            ArrayList<String> namespaces = new ArrayList<String>(managedNamespaces.keySet());
            populteCombo(namespaceCombo, namespaces);
            int selection = namespaces.indexOf(selectedRoot);
            if (selection < 0) {
                selection = 0;
            }
            namespaceCombo.select(selection);
        }
        namespaceCombo.setLayoutData(twoColumnsGD);
        namespaceCombo.setToolTipText(Messages.getString("wizard.newProject.namespace.tooltip"));
        namespaceCombo.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(final ModifyEvent e) {
                validate();
                populteCombo(nsExtensionCombo, managedNamespaces.get(namespaceCombo.getText()));
            }
        });

        // When created from a repository item, this should be a combo with values under the
        // selected root namespace. Initial value is the namespace selected when launching the
        // wizard.

        // Namespace Extension field
        final Label extension = new Label(container, SWT.NONE);
        extension.setText(Messages.getString("OtmW.NSExtension.Label"));
        extension.setToolTipText(Messages.getString("OtmW.NSExtension.Tooltip"));
        nsExtensionCombo = WidgetFactory.createCombo(container, SWT.DROP_DOWN | SWT.V_SCROLL);
        if (!GeneralPreferencePage.areNamespacesManaged()) {
            nsExtensionCombo.setEnabled(false);
        }
        if (!managedNamespaces.isEmpty()) {
            ArrayList<String> list = new ArrayList<String>(managedNamespaces.get(namespaceCombo
                    .getText()));
            populteCombo(nsExtensionCombo, list);
            int selection = list.indexOf(selectedExt);
            if (selection < 0) {
                selection = 0;
            }
            nsExtensionCombo.select(selection);
        }

        nsExtensionCombo.setLayoutData(twoColumnsGD);
        nsExtensionCombo.setToolTipText(Messages.getString("OtmW.NSExtension.Tooltip"));
        nsExtensionCombo.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(final ModifyEvent e) {
                validate();
            }

        });

        final Label commentsLabel = new Label(container, SWT.NONE);
        commentsLabel.setText(Messages.getString("wizard.newProject.commentsField.label"));
        commentsLabel.setToolTipText(Messages.getString("wizard.newProject.commentsField.tooltip"));
        descriptionText = WidgetFactory.createText(container, SWT.MULTI | SWT.BORDER);
        descriptionText.setToolTipText(Messages
                .getString("wizard.newProject.commentsField.tooltip"));
        descriptionText.setLayoutData(multiTextGD);

        setControl(container);
        validate();
    }

    private void populteCombo(Combo namespaceCombo, Collection<String> rootNamespaces) {
        String savedText = namespaceCombo.getText();
        namespaceCombo.removeAll();
        if (rootNamespaces != null) {
            for (String ns : rootNamespaces) {
                namespaceCombo.add(ns);
            }
        }
        namespaceCombo.setText(savedText);
    }

    private String removeExtension(final String fileName, final String ext) {
        if (fileName.endsWith("." + ext)) {
            return fileName.substring(0, fileName.lastIndexOf('.'));
        }
        return fileName;
    }

    private String appendExtension(final String fileName, final String ext) {
        if (!fileName.endsWith("." + ext)) {
            return fileName + "." + ext;
        }
        return fileName;
    }

    private void validate() {
        if (!projectFile.isEmpty() && !projectName.isEmpty())
            setPageComplete(true);

        String message = null;
        int level = INFORMATION;
        if (fileAlreadyExists(projectFile)) {
            message = Messages.getString("wizard.newProject.warning.override");
            level = WARNING;
        }

        message = isPathValid(projectFile);
        if (message != null) {
            level = ERROR;
        }

        try {
            if (validator instanceof NewProjectValidator)
                ((NewProjectValidator) validator).setPage(this);
            validator.validate();
        } catch (final ValidationException e) {
            message = e.getMessage();
            level = ERROR;
            LOGGER.debug("Validation output: " + e.getMessage());
        }
        setMessage(message, level);
        if (level == ERROR) {
            setPageComplete(false);
        } else {
            setPageComplete(true);
        }
        getWizard().getContainer().updateButtons();
    }

    private String isPathValid(String projectFile) {
        final String success = null;
        File f = new File(projectFile);
        try {
            f.getCanonicalPath();
            if (f.isDirectory()) {
                return Messages.getString("wizard.newProject.error.directory");
            }
        } catch (IOException ex) {
            return Messages.getString("wizard.newProject.error.invalid");
        }
        return success;
    }

    private boolean fileAlreadyExists(final String path) {
        final File libFile = new File(path);
        return libFile.exists();
    }

    public void setDefaultName(String defaultName) {
        if (defaultName == null) {
            defaultName = "";
        }
        this.defaultName = defaultName;
    }

    public void setSelectedRoot(String selectedRoot) {
        this.selectedRoot = selectedRoot;
    }

    public void setSelectedExt(String selectedExt) {
        this.selectedExt = selectedExt;
    }

}
