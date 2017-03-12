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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.opentravel.schemacompiler.loader.LibraryLoaderException;
import org.opentravel.schemacompiler.repository.ProjectManager;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemas.controllers.DefaultProjectController;
import org.opentravel.schemas.controllers.DefaultRepositoryController;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.stl2developer.DialogUserNotifier;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.widgets.WidgetFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dave Hollander
 * 
 */
public class LibraryOrchestrationWizardPage extends WizardPage {
	private final static Logger LOGGER = LoggerFactory.getLogger(LibraryOrchestrationWizard.class);

	private Text lockText;
	private Button lockButton;
	private String lockLabel = "Lock library.";
	private String lockMsg = "Locking library.";
	private Text commitText;
	private Button commitButton;
	private String commitLabel = "Commit library.";
	private String commitMsg = "Committing library.";
	private Text unlockText;
	private Button unlockButton;
	private String unlockLabel = "Unlock library.";
	private String unlockMsg = "Unlocking library.";
	private Text finalizeText;
	private Button finalizeButton;
	private String finalizeLabel = "Finalize library.";
	private String finalizeMsg = "Finalizing library.";
	private Text versionText;
	private Button versionButton;
	private String versionLabel = "Version library.";
	private String versionMsg = "Versioning library.";
	private Text commentsText;
	private Button goButton;
	private Text stepText;
	private Text instructionsText;
	private String commitRemarks = "";
	private final LibraryNode library;
	private final DefaultRepositoryController rc;

	private GridData singleColumnWide;
	private GridData twoColumnsGD;
	private String errorMsg;

	private class Step {

	}

	protected LibraryOrchestrationWizardPage(final String pageName, final String title, final LibraryNode library,
			final DefaultRepositoryController rc) {
		super(pageName, title, null);
		this.library = library;
		this.rc = rc;
	}

	@Override
	public void createControl(final Composite parent) {
		final GridLayout layout = new GridLayout();
		layout.numColumns = 3;

		final Composite container = new Composite(parent, SWT.BORDER); // parent;
		container.setLayout(layout);

		final GridData singleColumnGD = new GridData();
		singleColumnGD.horizontalSpan = 1;
		singleColumnGD.horizontalAlignment = SWT.BEGINNING;
		singleColumnGD.grabExcessHorizontalSpace = false;
		// singleColumnGD.horizontalAlignment = SWT.FILL;
		// singleColumnGD.grabExcessHorizontalSpace = true;

		singleColumnWide = new GridData();
		singleColumnWide.horizontalSpan = 1;
		singleColumnWide.horizontalAlignment = SWT.BEGINNING;
		singleColumnWide.grabExcessHorizontalSpace = true;
		singleColumnWide.verticalAlignment = SWT.CENTER;

		final GridData twoColumnsGD = new GridData();
		twoColumnsGD.horizontalSpan = 2;
		twoColumnsGD.horizontalAlignment = SWT.FILL;
		twoColumnsGD.grabExcessHorizontalSpace = true;
		twoColumnsGD.verticalAlignment = SWT.CENTER;

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

		final GridData multiTextCenter = new GridData();
		multiTextCenter.horizontalSpan = 3;
		multiTextCenter.horizontalAlignment = SWT.FILL;
		multiTextCenter.verticalAlignment = SWT.FILL;
		multiTextCenter.grabExcessHorizontalSpace = true;
		// multiTextCenter.grabExcessVerticalSpace = true;

		Display display = Display.getCurrent();
		Color darkGreen = display.getSystemColor(SWT.COLOR_DARK_GREEN);

		// Field Layouts
		//
		instructionsText = WidgetFactory.createText(container, SWT.SINGLE | SWT.READ_ONLY);
		instructionsText.setLayoutData(threeColumnsGD);
		instructionsText.setText("Enter remarks for the commit, select commit and version type.");

		// Comments
		// final Label commentsLabel = new Label(container, SWT.NONE);
		// commentsLabel.setText(Messages.getString("wizard.OrchestrateLibrary.commentsField.label"));
		// commentsLabel.setToolTipText(Messages.getString("wizard.newLibrary.commentsField.tooltip"));
		commentsText = WidgetFactory.createText(container, SWT.MULTI | SWT.BORDER);
		commentsText.setLayoutData(multiTextGD);
		// commentsText.setToolTipText(Messages.getString("wizard.newLibrary.commentsField.tooltip"));
		commentsText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(final ModifyEvent e) {
				commitRemarks = commentsText.getText();
				enableReadyButton(!commitRemarks.isEmpty());
			}

		});

		//
		// goButton = WidgetFactory.createText(container, SWT.SINGLE | SWT.BORDER);
		goButton = new Button(container, SWT.PUSH);
		goButton.setLayoutData(multiTextCenter);
		// goButton.setBackground(darkGreen);
		goButton.setText("");
		goButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				go();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// Label stepLabel = new Label(container, SWT.NONE);
		// stepLabel.setText("Current Step");
		// stepText = WidgetFactory.createText(container, SWT.SINGLE | SWT.READ_ONLY);
		// stepText.setLayoutData(twoColumnsGD);
		// stepText.setText("Current Step");

		// Lock Field
		lockButton = new Button(container, SWT.CHECK);
		lockButton.setSelection(false);
		lockButton.setBackground(darkGreen);
		lockButton.setEnabled(false);
		lockText = WidgetFactory.createText(container, SWT.SINGLE | SWT.READ_ONLY);
		lockText.setLayoutData(twoColumnsGD);
		lockText.setText(lockLabel);
		if (library.isLocked()) {
			lockButton.setGrayed(true);
			lockText.setEnabled(false);
		}

		commitButton = new Button(container, SWT.CHECK);
		commitButton.setSelection(false);
		commitButton.setBackground(darkGreen);
		commitButton.setEnabled(false);
		commitText = WidgetFactory.createText(container, SWT.SINGLE | SWT.READ_ONLY);
		commitText.setLayoutData(singleColumnGD);
		commitText.setText(commitLabel);
		commitRevertButtons(container);

		unlockButton = new Button(container, SWT.CHECK);
		unlockButton.setSelection(false);
		unlockButton.setBackground(darkGreen);
		unlockButton.setEnabled(false);
		unlockText = WidgetFactory.createText(container, SWT.SINGLE | SWT.READ_ONLY);
		unlockText.setLayoutData(twoColumnsGD);
		unlockText.setText(unlockLabel);

		finalizeButton = new Button(container, SWT.CHECK);
		finalizeButton.setSelection(false);
		finalizeButton.setBackground(darkGreen);
		finalizeButton.setEnabled(false);
		finalizeText = WidgetFactory.createText(container, SWT.SINGLE | SWT.READ_ONLY);
		finalizeText.setLayoutData(twoColumnsGD);
		finalizeText.setText(finalizeLabel);

		versionButton = new Button(container, SWT.CHECK);
		versionButton.setSelection(false);
		versionButton.setBackground(darkGreen);
		versionButton.setEnabled(false);
		versionText = WidgetFactory.createText(container, SWT.SINGLE | SWT.READ_ONLY);
		versionText.setLayoutData(singleColumnGD);
		versionText.setText(versionLabel);
		versionTypeButtons(container);

		setControl(container);
		setPageComplete(false);
	}

	public void enableReadyButton(boolean ready) {
		goButton.setEnabled(ready);
		goButton.setText("Press Finish to complete.");
		// setPageComplete(ready);
	}

	public void go() {
		LOGGER.debug("GO DO IT");
		goButton.setText("GO DO IT");
		doLock();
		doCommit();
		doUnlock();
		goButton.setText("Done");
		setPageComplete(true);
	}

	public void doLock() {
		if (library.isLocked())
			return;
		LOGGER.debug(lockMsg);
		goButton.setText(lockMsg);
		try {
			library.lock();
		} catch (RepositoryException e) {
			errorMsg = e.getLocalizedMessage();
		} catch (LibraryLoaderException e) {
			errorMsg = e.getLocalizedMessage();
		}
		lockButton.setSelection(true);
		lockText.setText("Locked");
		DialogUserNotifier.syncWithUi("Locked");
	}

	public void doCommit() {
		LOGGER.debug(commitMsg);
		goButton.setText(commitMsg);
		try {
			library.getProjectItem().getProjectManager().commit(library.getProjectItem(), commitRemarks);
		} catch (RepositoryException e) {
			errorMsg = e.getLocalizedMessage();
		}
		commitButton.setSelection(true);
		// commitText.setText("Committed");
		DialogUserNotifier.syncWithUi("Committed");

	}

	public void doUnlock() {
		if (!library.isLocked())
			return;
		LOGGER.debug(unlockMsg);
		goButton.setText(unlockMsg);
		try {
			MainController mc = OtmRegistry.getMainController();
			ProjectManager pm = ((DefaultProjectController) mc.getProjectController()).getDefaultProject()
					.getTLProject().getProjectManager();
			pm.unlock(library.getProjectItem(), true, commitRemarks);
			library.updateLibraryStatus();
		} catch (RepositoryException e) {
			errorMsg = e.getLocalizedMessage();
		}
		unlockButton.setSelection(true);
		unlockText.setText("Unlocked");
	}

	// enum actionType = { };
	final String REVERT = "Revert";
	final String COMMIT = "Commit";
	final String MAJOR = "Major";
	final String MINOR = "Minor";
	final String PATCH = "Patch";

	private void commitRevertButtons(Composite c) {
		final Composite ch2 = new Composite(c, SWT.NULL);
		final GridLayout glh2 = new GridLayout(2, false);
		ch2.setLayout(glh2);
		ch2.setLayoutData(singleColumnWide);

		Button radio = new Button(ch2, SWT.RADIO);
		radio.setText(COMMIT);
		radio.setSelection(true);
		// radio.setData(curNodeList);
		radio.addSelectionListener(new ButtonSelectionHandler());

		radio = new Button(ch2, SWT.RADIO);
		radio.setText(REVERT);
		// radio.setData(firstNodeOnly);
		radio.addSelectionListener(new ButtonSelectionHandler());
	}

	private void versionTypeButtons(Composite c) {
		final Composite ch2 = new Composite(c, SWT.NULL);
		final GridLayout glh2 = new GridLayout(3, false);
		ch2.setLayout(glh2);
		ch2.setLayoutData(singleColumnWide);

		Button radio = new Button(ch2, SWT.RADIO);
		radio.setText(MAJOR);
		radio.setSelection(true);
		// radio.setData(curNodeList);
		radio.addSelectionListener(new ButtonSelectionHandler());

		radio = new Button(ch2, SWT.RADIO);
		radio.setText(MINOR);
		// radio.setData(firstNodeOnly);
		radio.addSelectionListener(new ButtonSelectionHandler());

		radio = new Button(ch2, SWT.RADIO);
		radio.setText(PATCH);
		// radio.setEnabled(false);
		// radio.setData(firstNodeOnly);
		// radio.addSelectionListener(new ButtonSelectionHandler());
	}

	public final class ButtonSelectionHandler extends SelectionAdapter {

		@Override
		public void widgetSelected(final SelectionEvent e) {
			if (e.widget instanceof Button) {
				final Button b = (Button) e.widget;
				if (b.getText().equals(REVERT)) {
				}
			}
		}
	}

}
