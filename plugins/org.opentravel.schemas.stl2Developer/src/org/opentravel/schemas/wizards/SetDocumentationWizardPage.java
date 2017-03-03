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

import java.util.LinkedList;
import java.util.List;

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
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.Node.DocTypes;
import org.opentravel.schemas.properties.Messages;
import org.opentravel.schemas.widgets.WidgetFactory;
import org.opentravel.schemas.wizards.validators.FormValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dave Hollander
 * 
 */
public class SetDocumentationWizardPage extends WizardPage implements ModifyListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(SetDocumentationWizardPage.class);

	private Text docText;
	private String msgKey;
	private final FormValidator formValidator;
	private final List<ModifyListener> modifyListeners;
	private Combo docType;

	protected SetDocumentationWizardPage(final String props) {
		super(Messages.getString(props + ".pageName"), Messages.getString(props + ".pageTitle"), null);
		this.msgKey = props;
		formValidator = null;
		modifyListeners = new LinkedList<ModifyListener>();
	}

	@Override
	public void createControl(final Composite parent) {
		final GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		final Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(layout);

		final GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		final Label label = new Label(container, SWT.NULL);
		label.setText(Messages.getString(msgKey + ".label"));

		docText = WidgetFactory.createText(container, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		docText.setSize(SWT.DEFAULT, 75);
		docText.setToolTipText(Messages.getString(msgKey + ".toolTip"));
		docText.addModifyListener(this);
		docText.setLayoutData(new GridData(GridData.FILL_BOTH));

		docType = WidgetFactory.createCombo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
		docType.setItems(Node.docTypeStrings);
		docType.select(1);
		// Hide combo widget unless correct msgKey
		docType.setVisible(msgKey.equals(SetDocumentationWizard.docProps));

		setControl(container);
		setPageComplete(false);
		container.redraw();
	}

	/**
	 * @return the entered documentation text
	 */
	protected String getDocText() {
		return docText.getText();
	}

	/**
	 * @return the type of documentation text
	 */
	protected DocTypes getDocType() {
		return Node.docTypeFromString(docType.getText());
	}

	@Override
	public void modifyText(final ModifyEvent e) {
		notifyModifyListeners(e);
		if (!(e.widget instanceof Text))
			return;
		final String text = ((Text) e.widget).getText();
		String message = null;
		boolean complete = true;
		if (text.isEmpty())
			complete = false;

		setPageComplete(complete);
		setErrorMessage(message);
		getWizard().getContainer().updateButtons();
	}

	public void addModifyListener(final ModifyListener listener) {
		modifyListeners.add(listener);
	}

	public void removeModifyListener(final ModifyListener listener) {
		modifyListeners.remove(listener);
	}

	private void notifyModifyListeners(final ModifyEvent event) {
		for (final ModifyListener listener : modifyListeners) {
			listener.modifyText(event);
		}
	}

}
