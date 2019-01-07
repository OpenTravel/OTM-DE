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
package org.opentravel.schemas.views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLDocumentationItem;
import org.opentravel.schemacompiler.model.TLEquivalent;
import org.opentravel.schemacompiler.model.TLExample;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLInclude;
import org.opentravel.schemacompiler.model.TLNamespaceImport;
import org.opentravel.schemacompiler.validate.FindingMessageFormat;
import org.opentravel.schemacompiler.validate.Validatable;
import org.opentravel.schemacompiler.validate.ValidationFinding;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.impl.TLValidationBuilder;
import org.opentravel.schemas.controllers.OtmActions;
import org.opentravel.schemas.controllers.ValidationManager;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFinders;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.node.VersionNode;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.widgets.OtmEventData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Agnieszka Janowska
 * 
 */
public class ValidationResultsView extends OtmAbstractView {
	private static final Logger LOGGER = LoggerFactory.getLogger(ValidationResultsView.class);
	public static String VIEW_ID = "org.opentravel.schemas.stl2Developer.ValidationResultsView";

	// findings from the load or compile operations
	private ValidationFindings findings;

	private int sortColumn = 0;
	private boolean sortAscending = true;

	private List<ValidationFinding> findingsContent;
	private TableViewer findingsViewer;
	private OtmActions otmActions;
	private INode currentNode;

	@Override
	public void createPartControl(final Composite parent) {
		// LOGGER.info("Initializing part control of " + this.getClass());

		otmActions = new OtmActions(OtmRegistry.getMainController());

		// 3 column table for object, level/type and message
		Table findingsTable = new Table(parent,
				SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL);
		findingsTable.setLinesVisible(false);
		findingsTable.setHeaderVisible(true);
		final GridData td = new GridData(SWT.FILL, SWT.FILL, true, false);
		td.widthHint = 200;
		findingsTable.setLayoutData(td);

		final String[] titles = { "Level", "Component", "Description" };
		for (int i = 0; i < titles.length; i++) {
			final TableColumn column = new TableColumn(findingsTable, SWT.BOLD);

			column.setText(titles[i]);
			column.setData(i);

			column.addSelectionListener(new SelectionListener() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					sortTableColumns((Integer) ((TableColumn) e.getSource()).getData());
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
		}

		findingsViewer = new TableViewer(findingsTable);
		findingsViewer.setContentProvider(ArrayContentProvider.getInstance());
		findingsViewer.setLabelProvider(new FindingLabelProvider());
		findingsViewer.addDoubleClickListener(new IDoubleClickListener() {

			@Override
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				selectFindingSource((ValidationFinding) selection.getFirstElement());
			}

		});
		getSite().setSelectionProvider(findingsViewer);

		packTable();

		OtmRegistry.registerValidationResultsView(this);
		postFindings();
		// LOGGER.info("Done initializing part control of " + this.getClass());
	}

	private boolean viewerIsOk() {
		if (Display.getCurrent() == null)
			return false;
		return findingsViewer != null && findingsViewer.getControl() != null
				&& !findingsViewer.getControl().isDisposed();
	}

	private void packTable() {
		if (viewerIsOk())
			for (final TableColumn tc : findingsViewer.getTable().getColumns()) {
				tc.pack();
			}
	}

	/**
	 * Post validation findings from treeView into the findings section.
	 */
	public void postFindings() {
		// LOGGER.debug("Posting findings.");
		if (!viewerIsOk())
			return;

		clearFindings();
		if (findings != null)
			if (!findings.isEmpty()) {
				findingsContent = findings.getAllFindingsAsList();
				findingsViewer.setInput(findingsContent);
				packTable();
				findingsViewer.refresh();
				mc.postStatus(findingsContent.size() + " Warnings or Errors found.");
			} else {
				mc.postStatus("No Warnings or Errors found.");
			}
	}

	public void clearFindings() {
		// Run in UI thread if not in the UI thread.
		if (Display.getCurrent() == null) {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					if (viewerIsOk()) {
						findingsViewer.setInput(null);
						findingsViewer.refresh();
						packTable();
					}
				}
			});
		} else if (viewerIsOk()) {
			findingsViewer.setInput(null);
			packTable();
		}
	}

	public ValidationFindings getFindings() {
		return findings;
	}

	/**
	 * Set the view global findings then post them.
	 * 
	 * @param findings
	 * @param node
	 */
	// TODO - make this a controller method not view.
	public void setFindings(final ValidationFindings findings, final INode node) {
		this.findings = findings;
		postFindings();
		currentNode = node;
	}

	@Override
	public void setFocus() {
	}

	/**
	 * Locates the source node for the selected finding in the library navigation view.
	 */
	private void selectFindingSource(ValidationFinding finding) {
		Validatable findingSource = getNodeMappableFindingSource(finding);
		String validationIdentity = findingSource.getValidationIdentity();
		if (validationIdentity == null)
			return;
		Node n = NodeFinders.findNodeByValidationIentity(validationIdentity);
		if (n == null)
			return;
		if (n instanceof VersionNode)
			n = ((VersionNode) n).getNewestVersion();

		// LOGGER.debug("validation identity: " + validationIdentity);

		// If this is a unresolved entity reference, run the wizard
		// Pull of just the last portion of the message key - it is the error type ID
		int e = finding.getMessageKey().lastIndexOf(".");
		String error = finding.getMessageKey().substring(++e); // get past the period
		// LOGGER.debug(" error: " + error);
		if (error.equals(TLValidationBuilder.UNRESOLVED_NAMED_ENTITY_REFERENCE)) {
			OtmEventData wd = new OtmEventData();
			wd.setBusinessEvent(OtmActions.typeSelector());
			wd.setNode(null);
			wd.setNodeList(findMatching(finding, error));
			Node target = null;
			if (wd.getNodeList() != null && !wd.getNodeList().isEmpty())
				target = wd.getNodeList().get(0);
			if (target != null)
				otmActions.doEvent(wd);
			// re-validate to show changes
			validateNode((Node) currentNode);
		}

		// Go to the change
		// LOGGER.debug("Finding node " + n + " found for validation identity " +
		// validationIdentity);
		mc.selectNavigatorNodeAndRefresh(n);
	}

	/**
	 * In some cases, validation findings will be reported for entities that do not have corresponding nodes in the GUI.
	 * In those cases, the appropriate parent of the finding's source will be returned. If the source of the given
	 * finding is already mappable to a GUI node, the finding's source will be returned as-is.
	 * 
	 * @param finding
	 *            the validation finding to process
	 * @return Validatable
	 */
	private Validatable getNodeMappableFindingSource(ValidationFinding finding) {
		Validatable source = finding.getSource();

		if (source instanceof TLExample) {
			source = ((TLExample) source).getOwningEntity();

		} else if (source instanceof TLEquivalent) {
			source = ((TLEquivalent) source).getOwningEntity();

		} else if (source instanceof TLExtension) {
			source = ((TLExtension) source).getOwner();

		} else if (source instanceof TLDocumentation) {
			source = ((TLDocumentation) source).getOwner();

		} else if (source instanceof TLDocumentationItem) {
			TLDocumentation doc = ((TLDocumentationItem) source).getOwningDocumentation();
			source = (doc == null) ? null : doc.getOwner();

		} else if (source instanceof TLInclude) {
			source = ((TLInclude) source).getOwningLibrary();

		} else if (source instanceof TLNamespaceImport) {
			source = ((TLNamespaceImport) source).getOwningLibrary();
		}
		return source;
	}

	/**
	 * Match the error string and the first parameter of the findings.
	 * 
	 * @param finding
	 * @param error
	 * @return
	 */
	private ArrayList<Node> findMatching(ValidationFinding finding, String error) {
		// Create list and add finding to it.
		// Node n = new
		// ComponentNode().findNodeFromRoot(finding.getSource().getValidationIdentity());
		Node n = NodeFinders.findNodeByValidationIentity(finding.getSource().getValidationIdentity());
		ArrayList<Node> matches = new ArrayList<>();
		matches.add(n);

		// Look through findings to find match on both error id and param
		for (ValidationFinding f : findingsContent) {
			if (f.getMessageKey().contains(error)) {
				for (Object o : f.getMessageParams()) {
					if (o.equals(finding.getMessageParams()[0])) {
						// LOGGER.debug("Match found "+
						// f.getSource().getValidationIdentity());
						n = NodeFinders.findNodeByValidationIentity(f.getSource().getValidationIdentity());
						if (n != null) {
							// LOGGER.debug("Match " + n.getName() + " is property? " + n.isProperty());
							if (!(n instanceof PropertyNode))
								n = n.getAssignable();
							matches.add(n);
						}
					}
				}
			}
		}
		// for (INode m : matches) {
		// // if (m != null)
		// //// LOGGER.debug(" Match returned: " + m.getName());
		// }
		return matches;
	}

	/** Copies the content of the current findings to the clipboard. */
	@SuppressWarnings("unchecked")
	public void copyFindingsToClipboard() {
		if (!viewerIsOk())
			return;
		IStructuredSelection selection = (IStructuredSelection) findingsViewer.getSelection();
		Iterator<ValidationFinding> iterator;
		StringBuilder textResults = new StringBuilder();

		if ((selection == null) || selection.isEmpty()) {
			iterator = findingsContent.iterator();
		} else {
			iterator = selection.iterator();
		}

		while (iterator.hasNext()) {
			textResults.append(iterator.next().getFormattedMessage(FindingMessageFormat.IDENTIFIED_FORMAT))
					.append('\n');
		}
		if (textResults.length() > 0) {
			Clipboard clipboard = new Clipboard(null);

			clipboard.setContents(new Object[] { textResults.toString() },
					new Transfer[] { TextTransfer.getInstance() });
			clipboard.dispose();
		}
	}

	/**
	 * Sorts the table contents in ascending/descending order on the specified column index.
	 */
	private void sortTableColumns(int columnIndex) {
		if (sortColumn != columnIndex) {
			sortColumn = columnIndex;
			sortAscending = true;
		} else {
			sortAscending = !sortAscending;
		}

		if (viewerIsOk()) {
			Collections.sort(findingsContent,
					new FindingComparator((ITableLabelProvider) findingsViewer.getLabelProvider()));
			findingsViewer.refresh();
		}
	}

	/** Returns the appropriate label for each column of a table-row entry. */
	private class FindingLabelProvider extends LabelProvider implements ITableLabelProvider {

		@Override
		public String getColumnText(Object element, int columnIndex) {
			ValidationFinding finding = (ValidationFinding) element;
			String text = null;

			switch (columnIndex) {
			case 0:
				text = finding.getType().getDisplayName();
				break;
			case 1:
				text = finding.getSource().getValidationIdentity();
				break;
			case 2:
				text = finding.getFormattedMessage(FindingMessageFormat.MESSAGE_ONLY_FORMAT);
				break;
			}
			return text;
		}

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

	}

	/**
	 * Sorts the current list of findings according to the current user settings.
	 */
	private class FindingComparator implements Comparator<ValidationFinding> {

		private final ITableLabelProvider labelProvider;

		public FindingComparator(ITableLabelProvider labelProvider) {
			this.labelProvider = labelProvider;
		}

		/**
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(ValidationFinding finding1, ValidationFinding finding2) {
			String finding1Primary = labelProvider.getColumnText(finding1, sortColumn);
			String finding2Primary = labelProvider.getColumnText(finding2, sortColumn);
			int result = finding1Primary.compareTo(finding2Primary);

			if (result == 0) {
				String finding1Secondary = null, finding2Secondary = null;

				switch (sortColumn) {
				case 0:
					finding1Secondary = labelProvider.getColumnText(finding1, 1);
					finding2Secondary = labelProvider.getColumnText(finding2, 1);
					break;
				case 1:
					finding1Secondary = labelProvider.getColumnText(finding1, 2);
					finding2Secondary = labelProvider.getColumnText(finding2, 2);
					break;
				case 2:
					finding1Secondary = labelProvider.getColumnText(finding1, 1);
					finding2Secondary = labelProvider.getColumnText(finding2, 1);
					break;
				}
				result = finding1Secondary.compareTo(finding2Secondary);
			}
			return (sortAscending) ? result : -result;
		}

	}

	private ValidationFindings validate(Node n) {
		findings = ValidationManager.validate(n);
		// try {
		// return TLModelCompileValidator.validateModelElement(n.getTLModelObject());
		// } catch (Exception e) {
		// LOGGER.debug("Validation Exception on " + n + " " + e.getLocalizedMessage());
		// }
		mc.postStatus("Validated " + n.getLabel());
		return findings;
	}

	public void validateNode(Node node) {
		// LOGGER.debug("Validating node " + node);
		if (node == null)
			return;

		ValidationFindings findings = null;
		if (node.getChain() != null)
			node = node.getChain();

		if (node instanceof ModelNode)
			findings = validate(Node.getModelNode());
		else if (node instanceof LibraryChainNode) {
			for (LibraryNode ln : ((LibraryChainNode) node).getLibraries()) {
				if (findings == null)
					findings = validate(ln);
				else
					findings.addAll(validate(ln));
			}
		} else if (node instanceof ProjectNode) {
			for (LibraryNode ln : ((ProjectNode) node).getLibraries()) {
				if (findings == null)
					findings = validate(ln);
				else
					findings.addAll(validate(ln));
			}
		} else if (node.isTLLibrary())
			findings = validate(node);
		else if (node.isNavigation()) {
			for (LibraryMemberInterface n : node.getDescendants_LibraryMembers()) {
				if (findings == null)
					findings = validate(n);
				else
					findings.addAll(validate(n));
			}
		} else
			findings = validate(node);

		int findingsCount = 0;
		if (findings != null)
			findingsCount = findings.count();
		// LOGGER.debug("Validation of " + node + " complete, " + findingsCount
		// + " warnings and errors found.");
		setFindings(findings, node);
		mc.postStatus("Validation of " + node + " complete, " + findingsCount + " warnings and errors found.");
	}

	// @Override
	// public void showBusy(boolean on) {}
	// // http://blog.eclipse-tips.com/2009/02/using-progress-bars.html
	// Job job = new Job("Validation job") {
	// @Override
	// protected IStatus run(IProgressMonitor monitor) {
	// monitor.beginTask("Validating "+node, 100);
	// // execute the task ...
	//
	//
	// monitor.done();
	// return Status.OK_STATUS;
	// }
	// };
	// job.schedule();

	/**
	 * @param n
	 * @return
	 */
	private ValidationFindings validate(LibraryMemberInterface n) {
		// TODO Auto-generated method stub
		return null;
	}

	// ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);
	// dialog.run(true, true, new IRunnableWithProgress(){
	// public void run(IProgressMonitor monitor) {
	// monitor.beginTask("Some nice progress message here ...", 100);
	// // execute the task ...
	// monitor.done();
	// }
	// });
	// TODO - figure out how to implement this and move it to the Abstract View.
	// public void showProgress() {
	// Job job = new Job("My new job") {
	// @Override
	// protected IStatus run(IProgressMonitor monitor) {
	// monitor.beginTask("Some nice progress message here ...", 100);
	// // execute the task ...
	// monitor.done();
	// return Status.OK_STATUS;
	// }
	// };
	// job.schedule();
	// }
	//
	@Override
	public INode getCurrentNode() {
		return currentNode;
	}

	@Override
	public List<Node> getSelectedNodes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getViewID() {
		return VIEW_ID;
	}

	@Override
	public void refresh() {
		postFindings();
	}

	@Override
	public void refresh(INode node) {
		currentNode = node;
		validateNode((Node) currentNode);
		postFindings(); // TODO - go to finding related to this node

	}

	@Override
	public void refresh(boolean regenerate) {
		if (regenerate)
			validateNode((Node) currentNode);
		else
			postFindings();

	}

	@Override
	public void setCurrentNode(INode node) {
		// TODO Auto-generated method stub

	}

}
