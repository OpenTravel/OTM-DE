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
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.opentravel.schemas.actions.ChangeObjectAction;
import org.opentravel.schemas.actions.ClearExtendsAction;
import org.opentravel.schemas.actions.DownFacetAction;
import org.opentravel.schemas.actions.ExtendableAction;
import org.opentravel.schemas.actions.ExtendsAction;
import org.opentravel.schemas.actions.IWithNodeAction;
import org.opentravel.schemas.actions.UpFacetAction;
import org.opentravel.schemas.commands.AddNodeHandler2;
import org.opentravel.schemas.commands.DeleteNodesHandler;
import org.opentravel.schemas.commands.GoToTypeHandler;
import org.opentravel.schemas.commands.NewComponentHandler;
import org.opentravel.schemas.controllers.OtmActions;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.interfaces.Enumeration;
import org.opentravel.schemas.node.interfaces.ExtensionOwner;
import org.opentravel.schemas.node.interfaces.FacadeInterface;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.interfaces.InheritedInterface;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.typeProviders.AbstractContextualFacet;
import org.opentravel.schemas.node.typeProviders.AliasNode;
import org.opentravel.schemas.node.typeProviders.ContextualFacetNode;
import org.opentravel.schemas.node.typeProviders.EnumerationOpenNode;
import org.opentravel.schemas.properties.ExternalizedStringProperties;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.properties.Messages;
import org.opentravel.schemas.stl2developer.ColorProvider;
import org.opentravel.schemas.stl2developer.MainWindow;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.utils.RCPUtils;
import org.opentravel.schemas.widgets.ButtonBarManager;
import org.opentravel.schemas.widgets.ButtonWithAction;
import org.opentravel.schemas.widgets.FacetViewTablePoster;
import org.opentravel.schemas.widgets.LibraryTablePosterWithButtons;
import org.opentravel.schemas.widgets.OtmHandlers;
import org.opentravel.schemas.widgets.OtmSections;
import org.opentravel.schemas.widgets.OtmTextFields;
import org.opentravel.schemas.widgets.OtmWidgets;
import org.opentravel.schemas.widgets.WidgetFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create and maintain the facet table panel. Posts content into the table. Maintains a current and previous node
 * cursor. Maintains enabled/disabled state on the buttons. See {@link LibraryTablePosterWithButtons}
 * 
 * @author Dave Hollander
 */
public class FacetView extends OtmAbstractView {
	public static String VIEW_ID = "org.opentravel.schemas.stl2Developer.FacetView";
	private static final Logger LOGGER = LoggerFactory.getLogger(FacetView.class);

	private FormToolkit toolkit;
	private ScrolledForm form;
	private MainWindow mainWindow;

	private Composite tableComposite;
	private ColorProvider colorProvider;

	private Table table; // listener needs this to be class scoped
	private TableViewer facetViewer;

	// TODO - work on selection processes.
	// Separate out node posted in table from selected node
	// The selected node will typically be a child/grand-child of the table node
	private Node currentNode;
	private Node prevNode;

	private Text typeField;
	private Text nameField;
	private Text extendsField;
	private Button extendsSelector;
	private Button extendsClearButton;

	private UpFacetAction upFacetAction;
	private DownFacetAction downFacetAction;
	private ChangeObjectAction changeObjectAction;
	private ExtendableAction extendableAction;
	private Label extendableLabel;
	private ExtendsAction extendsAction;
	private Label extendsLabel;
	private ClearExtendsAction clearExtendsAction;
	// private CloneSelectedFacetNodesAction cloneSelectedFacetNodesAction;
	private ButtonBarManager buttonBarManager;
	private FacetViewTablePoster tablePoster;
	private final List<IWithNodeAction> nodeActions = new ArrayList<>();

	private class FacetTableDoubleClick implements IDoubleClickListener {
		@Override
		public void doubleClick(DoubleClickEvent event) {
			String cmdId = GoToTypeHandler.COMMAND_ID;
			try {
				RCPUtils.executeCommand(cmdId, null, getSite());
			} catch (ExecutionException e) {
				// LOGGER.debug("Failed to execute cmd: " + cmdId + ", error :" + e.getMessage());
			}
		}
	}

	private class TableSelectionListener implements Listener {
		@Override
		public void handleEvent(final Event event) {
			if (event.detail == SWT.CHECK) {
				facetViewer.getTable().deselectAll();
				select(getSelectedNodes());
				return; // no need to do anything.
			}

			// Navigation event should change the other views except navigation
			// view.
			if (event.item instanceof TableItem) {
				final TableItem ti = (TableItem) event.item;
				final Node node = (Node) ti.getData();
				// Properties in contributed facets are the actual properties so there is no way to determine if the
				// view is showing an object or facet for enable tests. Used the grayed state instead.
				if (ti.getGrayed()) {
					// LOGGER.debug(node + " is grayed"); // grayed state of check box
					disableAll();
					buttonBarManager.enable(false);
				}
				// Update this view
				else {
					setButtonState(node);
					buttonBarManager.enable(true);
				}
				currentNode = node;
			}
		}
	}

	public FacetView() {
		this.mainWindow = getMainWindow();
		OtmRegistry.registerFacetView(this);
	}

	@Override
	public boolean activate() {
		return false;
	}

	@Override
	public void createPartControl(Composite parent) {
	}

	@Override
	public void setFocus() {
	}

	protected void initialize(final Composite facetComposite) {
		toolkit = WidgetFactory.createFormToolkit(facetComposite.getDisplay());
		mainWindow = getMainWindow();

		final TableWrapLayout layout = new TableWrapLayout();
		layout.numColumns = 1;

		// Set up table layout in the form body.
		form = toolkit.createScrolledForm(facetComposite);
		form.getBody().setLayout(layout);
		toolkit.paintBordersFor(form.getBody());

		// Add a section to the page's form body
		tableComposite = mc.getSections().formatSection(toolkit, form, OtmSections.facetSection);
		formatFacetsSection(tableComposite);

		// Enable drop onto the table
		mc.getHandlers().enableDropTarget(table, mc.getActions(), OtmActions.setOrNewPropertyType(), mc.getWidgets());

		// Save the color provider for this display
		colorProvider = new ColorProvider(facetComposite.getDisplay());

	}

	@Override
	public Node getCurrentNode() {
		return currentNode;
	}

	@Override
	public INode getPreviousNode() {
		return prevNode;
	}

	/**
	 * Return a node list of all the items selected or checked in the table. Warning - changes selection to include
	 * checked items.
	 * 
	 * @return
	 */
	@Override
	public List<Node> getSelectedNodes() {
		if (table == null || table.isDisposed())
			return Collections.emptyList();
		if (mainWindow == null || !mainWindow.hasDisplay())
			return currentNode.getChildren();

		final List<Node> actionList = new ArrayList<>();
		// Walk the table and if the row is checked, select it
		final TableItem[] tia = table.getItems();
		for (int i = 0; i < tia.length; i++) {
			if (tia[i].getChecked()) {
				table.select(i);
			}
		}

		// Find out which nodes were either selected or checked.
		final int[] indexes = table.getSelectionIndices();
		// LOGGER.debug("SELECTED" + Arrays.toString(indexes));
		for (int i = 0; i < indexes.length; i++) {
			if (table.getItem(indexes[i]).getData() instanceof ComponentNode) {
				actionList.add((Node) table.getItem(indexes[i]).getData());
			}
		}
		return actionList;
	}

	private void formatFacetsSection(final Composite parent) {

		final Composite container = parent;// new Composite(parent, SWT.NULL);
		final GridLayout gl = new GridLayout();
		gl.numColumns = 2;
		container.setLayout(gl);

		// Object type and name
		typeField = mc.getFields().formatTextField(container, OtmTextFields.ComponentType);
		nameField = mc.getFields().formatTextField(container, OtmTextFields.ComponentName);

		buttonBarManager = new ButtonBarManager(SWT.FLAT);

		// Set up button bar
		upFacetAction = new UpFacetAction(mainWindow, ExternalizedStringProperties.create("OtmW.66", "OtmW.67"));
		addAsNodeWithAction(upFacetAction);
		downFacetAction = new DownFacetAction(mainWindow, ExternalizedStringProperties.create("OtmW.68", "OtmW.69"));
		addAsNodeWithAction(downFacetAction);
		changeObjectAction = new ChangeObjectAction(mainWindow,
				ExternalizedStringProperties.create("OtmW.84", "OtmW.85"));
		addAsNodeWithAction(changeObjectAction);

		IContributionItem addAction = RCPUtils.createCommandContributionItem(getSite(), AddNodeHandler2.COMMAND_ID,
				Messages.getString("action.addProperty.text"), null, null);
		IContributionItem newComplexAction = RCPUtils.createCommandContributionItem(getSite(),
				NewComponentHandler.COMMAND_ID, Messages.getString("OtmW.60"), null, null);
		IContributionItem deleteFromCommand = RCPUtils.createCommandContributionItem(getSite(),
				DeleteNodesHandler.COMMAND_ID, null, null, null);
		buttonBarManager.add(newComplexAction);
		buttonBarManager.add(addAction);
		buttonBarManager.add(deleteFromCommand);
		buttonBarManager.add(upFacetAction);
		buttonBarManager.add(downFacetAction);
		buttonBarManager.add(changeObjectAction);

		// Extension / Open check box. Set button state will change label and value as needed.
		final GridLayout extensionCheckLayout = new GridLayout(2, false);
		extensionCheckLayout.marginWidth = 0;

		final GridData extensionCheckGD = new GridData();
		extensionCheckGD.horizontalIndent = 0;

		final Composite extensionCheckComposite = new Composite(parent, SWT.NONE);
		extensionCheckComposite.setLayoutData(extensionCheckGD);
		extensionCheckComposite.setLayout(extensionCheckLayout);

		final GridLayout extensionLayout = new GridLayout(4, false);
		extensionLayout.marginWidth = 0;

		final GridData extensionGD = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		extensionGD.horizontalSpan = 1;
		extensionGD.horizontalIndent = 0;

		final Composite extensionComposite = new Composite(parent, SWT.NONE);
		extensionComposite.setLayoutData(extensionGD);
		extensionComposite.setLayout(extensionLayout);

		extendableLabel = new Label(extensionCheckComposite, SWT.NONE);
		extendableLabel.setText(Messages.getString("OtmW.74"));
		extendableLabel.setToolTipText(Messages.getString("OtmW.75"));
		extendableAction = new ExtendableAction(mainWindow, ExternalizedStringProperties.create("OtmW.74", "OtmW.75"),
				null);
		@SuppressWarnings("unused")
		ButtonWithAction extendableCheck = new ButtonWithAction(extensionCheckComposite, SWT.CHECK, extendableAction);
		addAsNodeWithAction(extendableAction);

		// Field and button for the base type
		extendsLabel = new Label(extensionComposite, SWT.NONE);
		extendsLabel.setText(Messages.getString("OtmW.350"));
		extendsLabel.setToolTipText(Messages.getString("OtmW.351"));
		extendsField = mc.getFields().formatTextField(extensionComposite, extendsLabel, OtmTextFields.extendsName, 1);
		extendsField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
		extendsField.setEditable(false);
		extendsSelector = mc.getFields().formatButton(extensionComposite, OtmWidgets.extendsSelector,
				OtmActions.extendsSelector(), null);
		extendsAction = new ExtendsAction(mainWindow, ExternalizedStringProperties.create("OtmW.350", "OtmW.351"),
				extendsField, extendsSelector);

		extendsClearButton = mc.getFields().formatButton(extensionComposite, OtmWidgets.clearExtends,
				OtmActions.clearExtends(), null);
		extendsClearButton.setImage(Images.getImageRegistry().get("delete"));
		clearExtendsAction = new ClearExtendsAction(mainWindow,
				ExternalizedStringProperties.create("OtmW.352", "OtmW.353"), extendsField, extendsClearButton);

		// Post the button bar
		final Composite bb = buttonBarManager.createControl(parent);
		final GridData buttonsGD = new GridData();
		buttonsGD.horizontalSpan = 2;
		buttonsGD.horizontalIndent = 0;
		bb.setLayoutData(buttonsGD);

		disableAll();

		table = toolkit.createTable(parent,
				SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION | SWT.CHECK | SWT.V_SCROLL | SWT.H_SCROLL);
		final GridData td = new GridData(SWT.FILL, SWT.FILL, true, false);
		td.widthHint = 350;
		td.heightHint = SWT.DEFAULT;
		td.horizontalSpan = 2;

		table.setLayoutData(td);
		tablePoster = new LibraryTablePosterWithButtons(table, mainWindow);

		table.addListener(SWT.Selection, new TableSelectionListener());

		// Add viewer to enable double-click
		facetViewer = new TableViewer(table);
		facetViewer.setContentProvider(ArrayContentProvider.getInstance());
		facetViewer.addDoubleClickListener(new FacetTableDoubleClick());

		// getSite().setSelectionProvider(facetViewer);
	}

	private void addAsNodeWithAction(IWithNodeAction action) {
		nodeActions.add(action);
	}

	/**
	 * Move the selection focus up or down.
	 * 
	 * @param rows
	 *            +/- number of rows to move focus. Values out of range are ignored.
	 */
	public void setFocus(final int rows) {
		final int index = table.getSelectionIndex();
		OtmRegistry.getTypeView().refreshAllViews();
		table.deselectAll();
		table.select(index + rows);
		table.setFocus();

		final TableItem[] ti = table.getSelection();

		if ((ti != null) && (ti.length > 0))
			setButtonState((Node) ti[0].getData());
		// else
		// LOGGER.debug("setFocus could not set button state.");
	}

	private void clearTable() {
		if (!mainWindow.hasDisplay())
			return;
		tablePoster.clearTable();
		form.reflow(true);
	}

	@Override
	public void refresh() {
		// 8/28/2018 - setCurrentNode will reset to container in table
		setCurrentNode(currentNode);
		postNode(currentNode);
	}

	@Override
	public void refresh(INode node) {
		setCurrentNode(node);
		postNode(currentNode);
	}

	/**
	 * Post the node table and label to the display. This is the primary table display method. Remembers what node is
	 * posted. Posts the name and related properties adjacent to the table.
	 * 
	 * @param target
	 *            is the ComponentNode to display
	 */
	private void postNode(Node target) {
		if (!getMainWindow().hasDisplay())
			return;
		if (target == null || target.isDeleted()) {
			// LOGGER.warn("Posted deleted node: " + target);
			clearTable();
			mc.getFields().postField(nameField, "Deleted", false);
			return;
		}

		if (!target.isValid()) {
			ValidationResultsView validationView = OtmRegistry.getValidationResultsView();
			if (validationView != null)
				validationView.refresh(target);
		}

		// LOGGER.debug("Posting facet table for node: " + target);
		Node node = target;

		OtmHandlers.suspendHandlers();

		try {
			setButtonState(target);
			if (node instanceof AbstractContextualFacet)
				mc.getFields().postField(nameField, ((AbstractContextualFacet) node).getLocalName(),
						node.isRenameable());
			else
				mc.getFields().postField(nameField, node.getName(), node.isRenameable());
			mc.getFields().postField(typeField, node.getComponentType(), false);
			mc.getFields().postField(extendsField, node.getExtendsTypeName(), false);

			tablePoster.postTable(node);
			form.reflow(true);
		} finally {
			OtmHandlers.enableHandlers();
			select(target); // select the row that was passed in.
		}
		// LOGGER.debug("Tree Selection is: "+OtmRegistry.getModelNavigatorView().getSelectedNodes());
	}

	@Override
	public void setCurrentNode(INode curNode) {
		if (curNode instanceof InheritedInterface)
			curNode = ((InheritedInterface) curNode).getInheritedFrom();
		if (curNode instanceof FacadeInterface)
			curNode = ((FacadeInterface) curNode).get();

		// Don't try to post a property - show its whole component.
		if (curNode instanceof PropertyNode || curNode instanceof AliasNode)
			curNode = curNode.getOwningComponent();

		if (curNode != currentNode) {
			// LOGGER.debug("Setting previous node: " + currentNode);
			// If the node is a facade, show the underlying node

			prevNode = currentNode;
			currentNode = (Node) curNode;
			postNode(currentNode);
		}
	}

	/**
	 * Set the button and field state based on node. ONLY does name and extendible!
	 * 
	 * @param curNode
	 */
	private void setButtonState(final Node curNode) {
		disableAll();
		if (curNode == null) {
			return;
		}

		for (IWithNodeAction action : getNodeActions())
			action.setCurrentNode(curNode);

		nameField.setEnabled(curNode.isEditable_newToChain());

		// extend-able action controls extension points OR open/closed enum
		if (curNode instanceof Enumeration) {
			extendableLabel.setText(Messages.getString("OtmW.74.Open"));
			extendableLabel.setToolTipText(Messages.getString("OtmW.75.Open"));
		} else {
			extendableLabel.setText(Messages.getString("OtmW.74"));
			extendableLabel.setToolTipText(Messages.getString("OtmW.75"));
		}
		if (curNode instanceof EnumerationOpenNode)
			extendableAction.setChecked(true);
		else if (curNode.isExtensibleObject())
			extendableAction.setChecked(curNode.isExtensible());
		else
			extendableAction.setChecked(false);
		// Enable/disable extendable action
		if ((curNode instanceof Enumeration || curNode.isExtensibleObject()) && curNode.isEditable_newToChain()) {
			extendableAction.setEnabled(true);
			extendableLabel.setBackground(colorProvider.getColor(SWT.COLOR_WHITE));
		} else {
			extendableAction.setEnabled(false);
			extendableLabel.setBackground(colorProvider.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		}

		// Set extends field and buttons. Don't allow users to break version relationships
		if (curNode.isEditable_newToChain()) {
			if (curNode instanceof ExtensionOwner)
				extendsAction.setEnabled(true);
			if (curNode instanceof ContextualFacetNode)
				extendsAction.setEnabled(true);
			clearExtendsAction.setEnabled(curNode.getExtendsType() != null);
		}
		if (curNode instanceof AbstractContextualFacet) {
			extendsLabel.setText(Messages.getString("OtmW.350a"));
			extendsLabel.setToolTipText(Messages.getString("OtmW.351a"));
		} else {
			extendsLabel.setText(Messages.getString("OtmW.350"));
			extendsLabel.setToolTipText(Messages.getString("OtmW.351"));
		}

	}

	private Collection<IWithNodeAction> getNodeActions() {
		return nodeActions;
	}

	private void disableAll() {
		extendableAction.setChecked(false);
		extendableAction.setEnabled(false);
		extendsAction.setEnabled(false);
		clearExtendsAction.setEnabled(false);
		nameField.setEnabled(false);
	}

	@Override
	public void restorePreviousNode() {
		// LOGGER.debug("Restoring previous node: " + prevNode);
		postNode(prevNode);
		currentNode = prevNode;
	}

	private void select(Node node) {
		if (node != null) {
			facetViewer.setSelection(new StructuredSelection(node));
		}
	}

	private void select(List<Node> nodes) {
		if (nodes != null && facetViewer != null) {
			facetViewer.setSelection(new StructuredSelection(nodes));
		}
	}

	@Override
	public String getViewID() {
		return VIEW_ID;
	}

	public ISelectionProvider getSelectionProvider() {
		return facetViewer;
	}

}
