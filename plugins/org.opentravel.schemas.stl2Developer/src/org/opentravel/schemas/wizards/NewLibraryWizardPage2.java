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

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.opentravel.schemas.node.handlers.NamespaceHandler;
import org.opentravel.schemas.preferences.GeneralPreferencePage;
import org.opentravel.schemas.properties.Messages;
import org.opentravel.schemas.widgets.WidgetFactory;
import org.opentravel.schemas.wizards.NewLibraryWizard2.LibraryMetaData;
import org.opentravel.schemas.wizards.validators.FormValidator;
import org.opentravel.schemas.wizards.validators.ValidationException;

/**
 * @author Dave Hollander / Agnieszka Janowska
 * 
 */
public class NewLibraryWizardPage2 extends WizardPage {

	public static final String DEFAULT_EXTENSION = "otm";
	private Text fileText;
	private Button fileButton;
	private Text nameText;
	private Text nsExtText;
	private Text namespaceText;
	private Text prefixText;
	private Text versionText;

	private Text commentsText;

	private NewLibraryWizard2.LibraryMetaData metaData;

	private final FormValidator validator;
	private final NamespaceHandler nsHandler;
	private final String baseNS;
	private final String InitialVersionNumber = "0.0";

	protected NewLibraryWizardPage2(final String pageName, final String title, LibraryMetaData metaData) {
		super(pageName, title, null);
		this.metaData = metaData;
		this.validator = metaData.getValidator();
		this.nsHandler = metaData.getNsHandler();
		this.baseNS = metaData.getBaseNS();
	}

	private String appendExtension(final String fileName, final String ext) {
		if (!fileName.endsWith("." + ext)) {
			return fileName + "." + ext;
		}
		return fileName;
	}

	@Override
	public void createControl(final Composite parent) {
		final GridLayout layout = new GridLayout();
		layout.numColumns = 3;

		final Composite container = new Composite(parent, SWT.BORDER); // parent;
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

		// File Path Selection
		final Label fileLabel = new Label(container, SWT.NONE);
		fileLabel.setText(Messages.getString("wizard.newLibrary.pathField.label"));
		fileLabel.setToolTipText(Messages.getString("wizard.newLibrary.pathField.tooltip"));
		fileText = WidgetFactory.createText(container, SWT.SINGLE | SWT.BORDER);
		fileText.setLayoutData(singleColumnGD);
		fileText.setToolTipText(Messages.getString("wizard.newLibrary.pathField.tooltip"));
		fileText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(final ModifyEvent e) {
				final String fullPath = fileText.getText();
				nameText.setText(removeExtension(new File(fullPath).getName(), DEFAULT_EXTENSION));
				metaData.setPath(fullPath);
				validate();
			}

		});
		fileButton = new Button(container, SWT.PUSH);
		fileButton.setText("...");
		fileButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(final SelectionEvent e) {
			}

			@Override
			public void widgetSelected(final SelectionEvent e) {
				final FileDialog fd = new FileDialog(getShell(), SWT.SAVE);
				fd.setText("Select Library Module");
				fd.setFilterPath(metaData.getPath());
				fd.setFilterExtensions(new String[] { "*." + DEFAULT_EXTENSION });
				final String fileName = fd.open();
				if (fileName != null) {
					final String fullPath = appendExtension(fileName, DEFAULT_EXTENSION);
					fileText.setText(fullPath);
				}
			}

		});

		// Name Field
		final Label nameLabel = new Label(container, SWT.NONE);
		nameLabel.setText(Messages.getString("wizard.newLibrary.nameField.label"));
		nameLabel.setToolTipText(Messages.getString("wizard.newLibrary.nameField.tooltip"));
		nameText = WidgetFactory.createText(container, SWT.SINGLE | SWT.BORDER);
		nameText.setLayoutData(twoColumnsGD);
		nameText.setToolTipText(Messages.getString("wizard.newLibrary.nameField.tooltip"));
		nameText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(final ModifyEvent e) {
				metaData.setName(nameText.getText());
				validate();
			}

		});

		// Base Namespace Label
		// When in managed namespace mode, only present project base ns in read-only field.
		// When in un-managed namespace mode, use the project base as a hint but allow editing.
		final Label baseNamespaceLabel = new Label(container, SWT.NONE);
		baseNamespaceLabel.setText(Messages.getString("view.properties.library.label.managedRoot"));
		baseNamespaceLabel.setToolTipText(Messages.getString("view.properties.library.tooltip.managedRoot"));
		namespaceText = WidgetFactory.createText(container, SWT.SINGLE | SWT.BORDER);
		namespaceText.setLayoutData(twoColumnsGD);
		namespaceText.setToolTipText(Messages.getString("OtmW.BaseNS.Tooltip"));
		metaData.setNamespace(metaData.getNsHandler().createValidNamespace(baseNS, InitialVersionNumber)); // put
		// ns
		namespaceText.setText(baseNS);
		if (GeneralPreferencePage.areNamespacesManaged()) {
			namespaceText.setEnabled(false);
		} else {
			namespaceText.addModifyListener(new ModifyListener() {

				@Override
				public void modifyText(final ModifyEvent e) {
					updateNS_Fields();
				}
			});
		}

		// Namespace Extension
		final Label nsExtensionLabel = new Label(container, SWT.NONE);
		nsExtensionLabel.setText(Messages.getString("OtmW.NSExtension.Label"));
		nsExtensionLabel.setToolTipText(Messages.getString("OtmW.NSExtension.Tooltip"));
		nsExtText = WidgetFactory.createText(container, SWT.SINGLE | SWT.BORDER);
		nsExtText.setLayoutData(twoColumnsGD);
		nsExtText.setToolTipText(Messages.getString("OtmW.NSExtension.Tooltip"));
		nsExtText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(final ModifyEvent e) {
				updateNS_Fields();
			}

		});

		// Prefix
		final Label prefixLabel = new Label(container, SWT.NONE);
		prefixLabel.setText(Messages.getString("OtmW.NSPrefix.Label"));
		prefixLabel.setToolTipText(Messages.getString("OtmW.NSPrefix.Tooltip"));
		prefixText = WidgetFactory.createText(container, SWT.SINGLE | SWT.BORDER);
		prefixText.setLayoutData(twoColumnsGD);
		prefixText.setToolTipText(Messages.getString("OtmW.NSPrefix.Tooltip"));
		prefixText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(final ModifyEvent e) {
				metaData.setNSPrefix(prefixText.getText());
				validate();
			}
		});

		// Version
		final Label versionLabel = new Label(container, SWT.NONE);
		versionLabel.setText(Messages.getString("OtmW.NSVersion.Label"));
		versionLabel.setToolTipText(Messages.getString("OtmW.NSVersion.Tooltip"));
		versionText = WidgetFactory.createText(container, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
		versionText.setText(InitialVersionNumber);
		versionText.setLayoutData(twoColumnsGD);
		versionText.setToolTipText(Messages.getString("OtmW.NSVersion.Tooltip"));
		versionText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(final ModifyEvent e) {
				updateNS_Fields();
			}
		});

		// Comments
		final Label commentsLabel = new Label(container, SWT.NONE);
		commentsLabel.setText(Messages.getString("wizard.newLibrary.commentsField.label"));
		commentsLabel.setToolTipText(Messages.getString("wizard.newLibrary.commentsField.tooltip"));
		commentsText = WidgetFactory.createText(container, SWT.MULTI | SWT.BORDER);
		commentsText.setLayoutData(multiTextGD);
		commentsText.setToolTipText(Messages.getString("wizard.newLibrary.commentsField.tooltip"));
		commentsText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(final ModifyEvent e) {
				metaData.setComments(commentsText.getText());
			}

		});

		updateNS_Fields();
		setControl(container);
		setPageComplete(false);
	}

	private boolean fileAlreadyExists(final String path) {
		final File libFile = new File(path);
		return libFile.exists();
	}

	private String removeExtension(final String fileName, final String ext) {
		if (fileName.endsWith("." + ext)) {
			return fileName.substring(0, fileName.lastIndexOf('.'));
		}
		return fileName;
	}

	private void updateNS_Fields() {
		final String ns = metaData.getNsHandler().createValidNamespace(namespaceText.getText(), nsExtText.getText(),
				versionText.getText());
		metaData.setNamespace(ns);

		final String prefix = nsHandler.getPrefix(ns);
		if (prefix != null && !prefix.isEmpty()) {
			metaData.setNSPrefix(prefix);
			prefixText.setText(prefix);
		}
		validate();
	}

	private void validate() {
		boolean complete = true;
		String message = null;
		int level = INFORMATION;
		if (fileAlreadyExists(metaData.getPath())) {
			message = "File already exists and will be overriden";
			level = WARNING;
		}

		try {
			validator.validate();
		} catch (final ValidationException e) {
			message = e.getMessage();
			level = ERROR;
			complete = false;
			// LOGGER.debug("Validation output: " + e.getMessage());
		}
		setPageComplete(complete);
		setMessage(message, level);
		getWizard().getContainer().updateButtons();
	}

}
