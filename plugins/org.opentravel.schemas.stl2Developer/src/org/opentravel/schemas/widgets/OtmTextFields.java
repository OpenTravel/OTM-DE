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
package org.opentravel.schemas.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.opentravel.schemas.controllers.OtmActions;
import org.opentravel.schemas.properties.Messages;

/**
 * @author Dave Hollander
 * 
 */
public class OtmTextFields extends OtmWidgets {
	private static final int DEFAULT_VSPAN = 1;
	// fieldName, tool tip, eventID
	public static int[] ComponentName = { 300, 301, OtmActions.setComponentName() };
	public static int[] ComponentType = { 302, 303, OtmActions.getNoOp() };
	public static int[] PropertyName = { 304, 305, OtmActions.setName() };
	public static int[] PropertyNS = { 306, 307, OtmActions.setNameSpace() };
	public static int[] extendsName = { 350, 351, OtmActions.getNoOp() };
	public static int[] typeName = { 308, 309, OtmActions.getNoOp() };
	public static int[] typePrefix = { 310, 311, OtmActions.getNoOp() };
	public static int[] description = { 312, 313, OtmActions.setDescription() };
	public static int[] example = { 314, 315, OtmActions.setExample() };
	public static int[] equivalent = { 316, 317, OtmActions.setEquivalence() };
	public static int[] pattern = { 318, 319, OtmActions.setPattern() };
	public static int[] minInclusive = { 354, 355, OtmActions.setMinInclusive() };
	public static int[] maxInclusive = { 356, 357, OtmActions.setMaxInclusive() };
	public static int[] minExclusive = { 358, 359, OtmActions.setMinExclusive() };
	public static int[] maxExclusive = { 360, 361, OtmActions.setMaxExclusive() };

	public static int[] versionScheme = { 332, 333, OtmActions.getNoOp() };

	public OtmTextFields(final OtmActions actions, final OtmHandlers handlers) {
		super(actions, handlers);
	}

	/**
	 * Format a multi-line text block.
	 * 
	 * @param container
	 * @param nCols
	 * @return
	 */
	// TODO - add the event handler code
	public Text formatTextBlock(final Composite container, final int nCols) {
		GridData gd = null;
		final Text description = WidgetFactory.createText(container, SWT.BORDER | SWT.MULTI);
		description.setText("");
		description.setToolTipText("Description of the new component. Describe the object it represents.");
		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = nCols - 1;
		description.setLayoutData(gd);
		return description;
	}

	public Text formatTextField(final FormToolkit toolkit, final Composite parent, final int[] properties) {
		return formatTextField(toolkit, parent, properties, SWT.SINGLE);
	}

	public Text formatTextField(final FormToolkit toolkit, final Composite parent, final int[] properties, int style) {
		return formatTextField(toolkit, parent, properties, style, DEFAULT_VSPAN);
	}

	public Text formatTextField(final FormToolkit toolkit, final Composite parent, final int[] properties, int style,
			int vSpan) {
		final Label label = toolkit.createLabel(parent, "");
		final Text text = toolkit.createText(parent, "", style);
		return formatTextField(text, label, properties, 2, vSpan);
	}

	public Text formatTextField(final Composite parent, final int[] properties) {
		return formatTextField(parent, properties, 2);
	}

	/**
	 * Create text widget with label and parent. Event Number used OtmActions to handle text modify and text focus
	 * listeners.
	 * 
	 * @param parent
	 *            composite to own the widget
	 * @param properties
	 *            label, tooltip, event number
	 * @param span
	 * @return
	 */
	public Text formatTextField(final Composite parent, final int[] properties, final int span) {
		final Label label = new Label(parent, SWT.NULL);
		final Text text = WidgetFactory.createText(parent, SWT.BORDER | SWT.SINGLE);
		return formatTextField(text, label, properties, span);
	}

	public Text formatTextField(final Composite parent, Label label, final int[] properties, final int span) {
		// final Label label = new Label(parent, SWT.NULL);
		final Text text = WidgetFactory.createText(parent, SWT.BORDER | SWT.SINGLE);
		return formatTextField(text, label, properties, span);
	}

	private Text formatTextField(final Text text, final Label label, final int[] properties, final int span) {
		return formatTextField(text, label, properties, span, DEFAULT_VSPAN);

	}

	private Text formatTextField(final Text text, final Label label, final int[] properties, final int span,
			int vSpan) {
		final OtmEventData wd = new OtmEventData();
		wd.label = Messages.getString("OtmW." + properties[0]); //$NON-NLS-1$
		final String toolTip = Messages.getString("OtmW." + properties[1]); //$NON-NLS-1$
		wd.businessEvent = properties[2]; // OtmActions event number
		wd.actionHandler = actionHandler; // handler call back

		label.setText(wd.label);
		label.setToolTipText(toolTip);
		fillGaps(vSpan, label.getParent());
		text.setText("");
		text.setData(wd);
		final GridData gd = new GridData();
		gd.horizontalSpan = span - 1; // allow for the label column
		gd.verticalSpan = vSpan;
		gd.horizontalAlignment = SWT.FILL;
		gd.verticalAlignment = SWT.FILL;
		gd.grabExcessHorizontalSpace = true;
		text.setLayoutData(gd);
		text.setToolTipText(toolTip);

		text.addModifyListener(new OtmHandlers.TextModifyListener()); // set dirty flag
		text.addListener(SWT.DefaultSelection, new OtmHandlers.TextDefaultListener());
		// handle CR text.addTraverseListener(new OtmHandlers.TextTraverseListener()); CR
		text.addFocusListener(new OtmHandlers.TextFocusListener());
		wd.widget = text;
		// widgetTbl.put(wd.key, wd);

		return text;

	}

	private void fillGaps(int vSpan, Composite parent) {
		if (vSpan > 1) {
			for (int i = 0; i < vSpan - 1; i++) {
				new Label(parent, SWT.NONE);
			}
		}
	}

	/**
	 * post field with enable/disable control
	 * 
	 * @param field
	 *            - the text control to set the text into.
	 * @param value
	 *            - text to post
	 * @param editable
	 *            - enable/disable editing of the field
	 */
	public void postField(final Text field, final String value, final boolean editable) {
		if (field.isDisposed())
			return;
		// for editable enable control if disabled
		if (editable && !field.isEnabled()) {
			field.setEnabled(true);
		}
		field.setEditable(editable);
		if (value == null || value.isEmpty()) {
			field.setText("");
		} else {
			field.setText(value);
		}
	}
}
