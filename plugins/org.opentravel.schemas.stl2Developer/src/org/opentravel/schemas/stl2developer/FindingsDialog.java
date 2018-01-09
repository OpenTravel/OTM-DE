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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IconAndMessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.LibraryElement;
import org.opentravel.schemacompiler.repository.ProjectItem;
import org.opentravel.schemacompiler.validate.FindingMessageFormat;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFinding;
import org.opentravel.schemas.properties.Messages;
import org.opentravel.schemas.utils.RCPUtils;

/**
 * A dialog to display given findings list. Displayed problems will be sorted by {@link FindingType} . This dialog
 * provide Cop
 * 
 * @author Pawel Jedruch
 * 
 */
public class FindingsDialog extends IconAndMessageDialog {

	private static final int COLUMN_MIN_WIDTH = 30;
	private static final int COPY_TO_CLIPBOARD_ID = IDialogConstants.CLIENT_ID;
	private static final String COPY_TO_CLIPBOARD_LABEL = "Copy to clipboard";
	private List<ValidationFinding> findings;
	private String title = "";

	public FindingsDialog(Shell parentShell, String title, String message, List<ValidationFinding> findings) {
		super(parentShell);
		this.title = title;
		this.findings = new ArrayList<ValidationFinding>(findings);
		Collections.sort(this.findings, new CompareByType());
		this.message = message;
		setShellStyle(SWT.CLOSE | SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL | SWT.RESIZE);
	}

	/**
	 * Opens an findings dialog to display the given problems.
	 * 
	 * @param parentShell
	 *            the parent shell of the dialog, or <code>null</code> if none
	 * @param title
	 *            the title to use for this dialog
	 * @param message
	 *            the message to show in this dialog
	 * @param findings
	 *            the findings to show to the user
	 * 
	 * @return {@link IDialogConstants#OK_ID}
	 */
	public static int open(Shell parentShell, String title, String message, List<ValidationFinding> findings) {
		FindingsDialog dialog = new FindingsDialog(parentShell, title, message, findings);
		return dialog.open();
	}

	/**
	 * 
	 * @param parentShell
	 * @param title
	 * @param message
	 * @param findings
	 * @param piList
	 *            - only show finding related to the library content of a project item in this list
	 * @return
	 */
	public static void open(Shell parentShell, String title, String message, List<ValidationFinding> findings,
			List<ProjectItem> piList) {
		// Remove any findings not related to a pi in the list
		List<ValidationFinding> releventFindings = new ArrayList<ValidationFinding>();
		List<AbstractLibrary> libList = new ArrayList<AbstractLibrary>();
		for (ProjectItem lpi : piList)
			libList.add(lpi.getContent());

		for (ValidationFinding finding : findings)
			if (finding.getSource() instanceof LibraryElement)
				if (libList.contains(((LibraryElement) finding.getSource()).getOwningLibrary()))
					releventFindings.add(finding);

		if (!releventFindings.isEmpty())
			open(OtmRegistry.getActiveShell(), Messages.getString("dialog.findings.title"),
					Messages.getString("dialog.findings.message"), releventFindings);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite comp = (Composite) super.createDialogArea(parent);
		((GridLayout) comp.getLayout()).numColumns = 2;
		createMessageArea(comp);
		createFindigsTable(comp);
		return comp;
	}

	/**
	 * @param comp
	 */
	private void createFindigsTable(Composite comp) {
		Composite parent = new Composite(comp, SWT.None);
		GridDataFactory.fillDefaults().span(2, 1).grab(false, true).applyTo(parent);
		parent.setLayout(new TableColumnLayout());
		TableViewer viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION
				| SWT.BORDER);
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		createColumns(viewer);

		final Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		viewer.setInput(findings);
		// for (int i = 0; i < table.getColumnCount(); i++) {
		// table.getColumn(i).pack();
		// }
	}

	/**
	 * @param viewer
	 */
	private void createColumns(TableViewer viewer) {
		createColumn(viewer, "Level", 10, new ColumnLabelProvider() {

			@Override
			public String getText(Object element) {
				return ((ValidationFinding) element).getType().getDisplayName();
			}

			@Override
			public Image getImage(Object element) {
				switch (((ValidationFinding) element).getType()) {
				case WARNING:
					return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_DEC_FIELD_WARNING);
				case ERROR:
					return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_DEC_FIELD_ERROR);
				}
				return null;
			}

		});
		createColumn(viewer, "Component", 30, new ColumnLabelProvider() {

			@Override
			public String getText(Object element) {
				return ((ValidationFinding) element).getSource().getValidationIdentity();
			}

		});
		createColumn(viewer, "Description", 50, new ColumnLabelProvider() {

			@Override
			public String getText(Object element) {

				return ((ValidationFinding) element).getFormattedMessage(FindingMessageFormat.MESSAGE_ONLY_FORMAT);
			}

			@Override
			public void update(ViewerCell cell) {
				super.update(cell);
			}

		});
	}

	private void createColumn(TableViewer viewer, String title, int weight, ColumnLabelProvider columnLabelProvider) {
		TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE);
		column.getColumn().setText(title);
		column.setLabelProvider(columnLabelProvider);
		column.getColumn().setData("org.eclipse.jface.LAYOUT_DATA", new ColumnWeightData(weight, COLUMN_MIN_WIDTH));
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(title);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, COPY_TO_CLIPBOARD_ID, COPY_TO_CLIPBOARD_LABEL, false);
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
	}

	@Override
	protected Image getImage() {
		return getErrorImage();
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (IDialogConstants.OK_ID == buttonId) {
			okPressed();
		} else if (COPY_TO_CLIPBOARD_ID == buttonId) {
			copyToClipboard(findings);
		}
	}

	private void copyToClipboard(List<ValidationFinding> findings) {
		StringBuilder sb = new StringBuilder();
		String eol = System.getProperty("line.separator");
		for (ValidationFinding f : findings) {
			sb.append(f.getFormattedMessage(FindingMessageFormat.DEFAULT));
			sb.append(eol);
		}
		RCPUtils.copyToClipboard(sb.toString());
	}

	class CompareByType implements Comparator<ValidationFinding> {

		@Override
		public int compare(ValidationFinding o1, ValidationFinding o2) {
			return o1.getType().compareTo(o2.getType());
		}

	}
}
