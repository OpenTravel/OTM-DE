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
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.validate.FindingMessageFormat;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFinding;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemas.node.ContextNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.VersionNode;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.interfaces.ResourceMemberInterface;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.resources.ActionFacet;
import org.opentravel.schemas.node.resources.ActionNode;
import org.opentravel.schemas.node.resources.ResourceField;
import org.opentravel.schemas.node.resources.ResourceMenus;
import org.opentravel.schemas.node.resources.ResourceNode;
import org.opentravel.schemas.properties.Messages;
import org.opentravel.schemas.stl2developer.DialogUserNotifier;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.widgets.WidgetFactory;
import org.opentravel.schemas.wizards.TypeSelectionWizard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage the view onto REST resource descriptions
 * 
 * @author Dave Hollander
 * 
 */
public class RestResourceView extends OtmAbstractView
		implements ISelectionListener, ISelectionChangedListener, ITreeViewerListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(RestResourceView.class);

	public static String VIEW_ID = "org.opentravel.schemas.stl2Developer.ResourceView";

	private FormToolkit toolkit;
	private TreeViewer viewer;

	private boolean ignoreListener = false; // used by listeners during selection changes
	private Label objectIcon;

	private Text rName;
	private Label rType;
	private Text rDescription;
	private List<ResourceMemberInterface> expansionState = new LinkedList<>();
	private INode currentNode;
	private INode previousNode;

	private SashForm mainSashForm;

	// private ButtonBarManager bbManager;
	private Composite compRight;

	private List<PostedField> postedFields = new ArrayList<>();
	private Group objectPropertyGroup;
	private Group examplesGroup;
	private Group validationGroup;
	private IBaseLabelProvider decorator;
	private Display display;

	private Group examplePayloadGroup;

	public RestResourceView() {
		OtmRegistry.registerResourceView(this);
	}

	@Override
	public void clearSelection() {
		if (!getMainWindow().hasDisplay())
			return; // headless operation
		if (postedFields != null)
			for (PostedField field : postedFields)
				field.dispose();
		if (!examplesGroup.isDisposed())
			for (Control kid : examplesGroup.getChildren())
				kid.dispose();
		if (!validationGroup.isDisposed())
			for (Control kid : validationGroup.getChildren())
				kid.dispose();
		currentNode = null;
		// Static fields - name and label on validation table
		post(rName, "");
		post(rDescription, "");
		validationGroup.setText("Errors and Warnings: ");

		if (viewer != null)
			viewer.setSelection(null);
		// LOGGER.debug("Cleared Selection.");
	}

	@Override
	public void createPartControl(final Composite parent) {
		// LOGGER.info("Initializing part control of " + this.getClass());

		parent.addDisposeListener(new DisposeListener() {

			@Override
			public void widgetDisposed(DisposeEvent e) {
				if (postedFields != null)
					for (PostedField field : postedFields)
						field.dispose();
				viewer = null;
			}
		});

		display = parent.getDisplay();
		getSite().getPage().addSelectionListener(NavigatorView.VIEW_ID, this);
		select(mc.getCurrentNode_NavigatorView());

		// Using Documentation View as a guide
		//
		mainSashForm = new SashForm(parent, SWT.HORIZONTAL);
		mainSashForm.setSashWidth(2);

		toolkit = WidgetFactory.createFormToolkit(parent.getDisplay());

		ScrolledForm formLeft = toolkit.createScrolledForm(mainSashForm);

		GridLayout layoutLeft = new GridLayout(2, false);
		layoutLeft.marginBottom = 10;
		layoutLeft.marginLeft = 10;
		layoutLeft.marginTop = 10;
		layoutLeft.marginRight = 10;

		GridData textGD = new GridData();
		textGD.grabExcessHorizontalSpace = true;
		textGD.horizontalAlignment = SWT.FILL;

		GridData bbGD = new GridData();
		bbGD.horizontalSpan = 2;
		bbGD.horizontalIndent = 0;
		bbGD.verticalIndent = 0;

		GridData viewerGD = new GridData();
		viewerGD.grabExcessHorizontalSpace = true;
		viewerGD.grabExcessVerticalSpace = true;
		viewerGD.horizontalAlignment = SWT.FILL;
		viewerGD.verticalAlignment = SWT.FILL;
		viewerGD.horizontalSpan = 2;

		Composite compLeft = formLeft.getBody();
		compLeft.setLayout(layoutLeft);

		// Set up the Menus as parent of tree view contains the tree viewer
		//
		ResourceMenus resourceMenus = new ResourceMenus(compLeft, getSite());
		viewer = resourceMenus.getViewer();
		viewer.addSelectionChangedListener(this);
		viewer.addTreeListener(this);
		viewer.getTree().setLayoutData(viewerGD);
		decorator = viewer.getLabelProvider();
		ScrolledForm formRight = toolkit.createScrolledForm(mainSashForm);

		GridLayout layoutRight = new GridLayout(2, false);
		layoutRight.marginBottom = 10;
		layoutRight.marginLeft = 10;
		layoutRight.marginTop = 10;
		layoutRight.marginRight = 10;

		GridData docTextGD = new GridData();
		docTextGD.grabExcessHorizontalSpace = true;
		docTextGD.grabExcessVerticalSpace = true;
		docTextGD.horizontalAlignment = SWT.FILL;
		docTextGD.verticalAlignment = SWT.FILL;
		docTextGD.horizontalSpan = 2;

		GridData navBbGD = new GridData();
		navBbGD.horizontalSpan = 2;
		navBbGD.horizontalIndent = 0;
		navBbGD.verticalIndent = 0;
		navBbGD.horizontalAlignment = SWT.LEFT;

		compRight = formRight.getBody();
		compRight.setLayout(layoutRight);

		// ButtonBarManager bbManager = new ButtonBarManager(SWT.FLAT);
		// bbManager.add(addAction);
		// Composite navBB = bbManager.createControl(toolkit, compRight);
		// navBB.setLayoutData(navBbGD);

		// Add fixed text fields
		//
		// For the type, create in line to have icon and label instead of label and text
		Color bgColor = compRight.getBackground();
		objectIcon = toolkit.createLabel(compRight, "", SWT.NONE);
		objectIcon.setBackground(bgColor);
		rType = new Label(compRight, SWT.WRAP);
		rType.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		rType.setBackground(bgColor);

		rName = createField("rest.label.name", compRight, null);
		rName.addModifyListener(new NameListener());
		rName.addSelectionListener(new TextSelectionListener());
		rDescription = createField("rest.label.description", compRight, null);
		rDescription.addModifyListener(new DescriptionListener());
		rDescription.addSelectionListener(new TextSelectionListener());

		// An example for future use
		// Now using the name hight, layout 3 rows of rType
		// gridData.verticalSpan = 3;

		rType.pack(true);
		compRight.layout(true, true);
		String initTxt = "Resource View will display resoures in the selected library.";
		rType.setText(initTxt);

		// Fields use the objectPropertyGroup as the composite
		//
		objectPropertyGroup = new Group(compRight, SWT.NULL);
		objectPropertyGroup.setText("Object Properties");
		GridLayout opGL = new GridLayout();
		opGL.numColumns = 3;
		objectPropertyGroup.setLayout(opGL);
		GridData opGD = new GridData(SWT.FILL, SWT.TOP, true, false);
		// GridData opGD = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		opGD.horizontalSpan = 2;
		objectPropertyGroup.setLayoutData(opGD);

		// Is there room for - NO, has to be inside OPG
		// Example Payload display
		// examplePayloadGroup = new Group(objectPropertyGroup, SWT.NULL);
		// examplePayloadGroup.setText("Example Payload");
		// GridLayout epGL = new GridLayout();
		// epGL.numColumns = 1;
		// examplePayloadGroup.setLayout(epGL);
		// GridData epGD = new GridData(SWT.FILL, SWT.TOP, true, false);
		// // GridData opGD = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		// epGD.horizontalSpan = 1;
		// examplePayloadGroup.setLayoutData(opGD);

		// Fields use the objectPropertyGroup as the composite
		//
		examplesGroup = new Group(compRight, SWT.NULL);
		examplesGroup.setText("Example URLs");
		GridLayout exGL = new GridLayout();
		exGL.numColumns = 2;
		examplesGroup.setLayout(exGL);
		GridData exGD = new GridData(SWT.FILL, SWT.TOP, true, false);
		exGD.horizontalSpan = 2;
		examplesGroup.setLayoutData(exGD);

		// Validation group
		//
		validationGroup = new Group(compRight, SWT.NULL);
		validationGroup.setText("Example URLs");
		GridLayout vGL = new GridLayout();
		vGL.numColumns = 3;
		validationGroup.setLayout(vGL);
		GridData vGD = new GridData(SWT.FILL, SWT.TOP, true, false);
		vGD.horizontalSpan = 2;
		validationGroup.setLayoutData(vGD);
		validationGroup.setText("Errors and Warnings");

		mainSashForm.setWeights(new int[] { 1, 3 });

		// done in postResources - setCurrentNode(mc.getCurrentNode_NavigatorView());
		ignoreListener = true; // turn off any listeners
		postResources();
		ignoreListener = false; // enable listeners
	}

	@Override
	public INode getCurrentNode() {
		return currentNode;
	}

	@Override
	public INode getPreviousNode() {
		return previousNode;
	}

	private boolean viewerIsOk() {
		return viewer != null && viewer.getControl() != null && !viewer.getControl().isDisposed();
	}

	/**
	 */
	@Override
	public List<Node> getSelectedNodes() {
		if (!viewerIsOk())
			return null; // In case the view is not activated.
		List<Node> selected = new ArrayList<>();
		StructuredSelection selection = (StructuredSelection) viewer.getSelection();
		for (Object e : selection.toList()) {
			if (e != null) {
				if (e instanceof VersionNode)
					e = ((VersionNode) e).get();
				if (e instanceof ResourceMemberInterface)
					selected.add((Node) e);
			}
		}
		// LOGGER.debug("getSelectedNodes is returning " + selected.size() + " nodes.");
		return selected;
	}

	// Only works if the selection is on the rest resource tree
	public ResourceMemberInterface getSelectedResourceNode() {
		if (!viewerIsOk())
			return null; // In case the view is not activated.
		StructuredSelection selection = (StructuredSelection) viewer.getSelection();
		Object object = selection.getFirstElement();
		if (object != null && object instanceof ResourceMemberInterface)
			return (ResourceMemberInterface) object;
		// LOGGER.debug("getSelectedResourceNode is returning null.");
		return null;
	}

	@Override
	public String getViewID() {
		return VIEW_ID;
	}

	/**
	 * Create then post a complete tree starting with the model root.
	 * 
	 * @param forceRefresh
	 */
	public void postResources() {
		if (!viewerIsOk())
			return;
		LibraryNode rootLibrary = null;
		if (mc.getCurrentNode_NavigatorView() != null)
			rootLibrary = mc.getCurrentNode_NavigatorView().getLibrary();
		if (rootLibrary != null)
			if (currentNode == null || currentNode.getLibrary() != rootLibrary) {
				// clear old display then display this library
				clearSelection();
				currentNode = rootLibrary;
				if (rootLibrary.isInChain())
					viewer.setInput(rootLibrary.getChain());
				else
					viewer.setInput(rootLibrary.getResourceRoot());
				restoreExpansionState();
				relayoutFields();
			}
	}

	protected void relayoutFields() {
		if (viewerIsOk()) {
			if (!objectPropertyGroup.isDisposed()) {
				objectPropertyGroup.redraw();
				objectPropertyGroup.layout(true);
				objectPropertyGroup.update();
			}
			if (!examplesGroup.isDisposed()) {
				examplesGroup.layout(true);
				examplesGroup.update();
			}
			if (!validationGroup.isDisposed()) {
				validationGroup.layout(true);
				validationGroup.update();
			}
			if (!compRight.isDisposed()) {
				compRight.layout(true);
				compRight.update();
			}
		}
	}

	@Override
	public void refresh() {
		// FIXME - when widget is disposed - unregister listener
		ignoreListener = true; // ignore changes to fields
		if (viewerIsOk()) {
			viewer.refresh(true);
			updateFields(getSelectedResourceNode());
			// Inform decoration of change
			if (getCurrentNode() != null && getCurrentNode().getOwningComponent() != null)
				PlatformUI.getWorkbench().getDecoratorManager().update(getCurrentNode().getOwningComponent().getName());
		}
		ignoreListener = false;
	}

	@Override
	public void refresh(INode node) {
		if (viewerIsOk()) {
			ignoreListener = true;
			// viewer.expandToLevel(node, 3);
			if (node == null)
				clearSelection();
			else if (node instanceof ResourceMemberInterface)
				updateFields((ResourceMemberInterface) node);
			viewer.refresh(true);
			postResources();
			ignoreListener = false;
		}
	}

	@Override
	public void refreshAllViews() {
		if (viewerIsOk())
			viewer.refresh(true);
		updateFields(getSelectedResourceNode());
	}

	public void select(ContextNode node) {
		if (viewerIsOk())
			viewer.setSelection(new StructuredSelection(node), true);
	}

	@Override
	public void selectionChanged(final IWorkbenchPart part, final ISelection selection) {
		// LOGGER.debug("selection changed: curNode = " + currentNode + ". \t" + selection.getClass());
		if (selection == null)
			return;
		final IStructuredSelection iss = (IStructuredSelection) selection;
		// the data should be the first element selected and it should be a Node
		final Object object = iss.getFirstElement();
		if (!(object instanceof Node))
			return;

		// switch libraries if user navigated to a new library
		if (part instanceof NavigatorView && currentNode != null)
			if (((Node) object).getLibrary() == currentNode.getLibrary())
				return; // no update needed

		ignoreListener = true;
		postResources();
		ignoreListener = false;
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		// LOGGER.debug("selection changed. \tevent = " + event.getClass());
		Object object = null;
		if (viewerIsOk())
			object = ((IStructuredSelection) viewer.getSelection()).getFirstElement();
		if (!(object instanceof Node))
			return;

		ignoreListener = true;
		Node node = (Node) object;
		if (node instanceof VersionNode)
			node = ((VersionNode) node).get();

		setCurrentNode(node);
		if (node instanceof ResourceMemberInterface)
			updateFields((ResourceMemberInterface) node);
		else
			updateFields(null);
		ignoreListener = false;
	}

	@Override
	public void setCurrentNode(final INode node) {
		previousNode = currentNode;
		currentNode = node;
	}

	@Override
	public void setFocus() {
		// LOGGER.debug("setFocus.");
		refreshAllViews();
	}

	@Override
	public void treeCollapsed(TreeExpansionEvent event) {
		if (event != null) {
			Object collapsed = event.getElement();
			if (collapsed instanceof ResourceMemberInterface) {
				ResourceMemberInterface doc = (ResourceMemberInterface) collapsed;
				removeRecursivelyFromExpansionState((Node) doc);
			}
		}
	}

	@Override
	public void treeExpanded(TreeExpansionEvent event) {
		if (event != null) {
			Object expanded = event.getElement();
			if (expanded instanceof ResourceMemberInterface) {
				expansionState.add((ResourceMemberInterface) expanded);
			}
		}
	}

	private Text createField(String msgKey, Composite comp, PostedField pf) {
		Color bgColor = comp.getBackground();
		Label label = toolkit.createLabel(comp, Messages.getString(msgKey + ".text"), SWT.TRAIL);
		label.setBackground(bgColor);

		Text field = toolkit.createText(comp, "", SWT.BORDER);
		field.setToolTipText(Messages.getString(msgKey + ".tooltip"));
		GridData gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = SWT.FILL;
		field.setLayoutData(gd);

		if (pf != null) {
			pf.label = label;
			pf.text = field;
		}
		return field;
	}

	private Table getFindingsTable(Composite parent) {
		// 3 column table for object, level/type and message
		if (parent != null) {
			Table findingsTable = new Table(parent,
					SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL);
			findingsTable.setLinesVisible(false);
			findingsTable.setHeaderVisible(true);
			final GridData td = new GridData(SWT.FILL, SWT.FILL, true, false);
			findingsTable.setLayoutData(td);
			return findingsTable;
		}
		return null;
	}

	private void post(Button widget, String text, Object data) {
		if (text != null && widget != null && !widget.isDisposed())
			widget.setText(text);
		if (data != null && widget != null && !widget.isDisposed())
			widget.setData(data);
	}

	private void post(Combo combo, String[] strings, String value) {
		if (combo == null || combo.isDisposed())
			return;

		int i = 0;
		// assure each value is unique - if there are duplicates the combo just flashes
		TreeSet<String> uniqueSet = new TreeSet<>(); // sorts and de-dupes
		for (String s : strings)
			uniqueSet.add(s);

		if (value == null || value.isEmpty())
			value = ResourceField.NONE;
		for (String s : uniqueSet) {
			if (s == null || s.isEmpty())
				s = ResourceField.NONE;
			combo.add(s);
			if (s.equals(value))
				combo.select(i);
			i++;
		}
	}

	private void post(Group grp, ActionNode action) {
		if (grp == null || grp.isDisposed())
			return;

		Color bgColor = grp.getBackground();
		Label label = toolkit.createLabel(grp, action.getTLModelObject().getActionId(), SWT.TRAIL);
		label.setBackground(bgColor);

		// labels will not display & ("\u0026\u0026")
		String url = "";
		if (action.getRequest() != null)
			url = action.getRequest().getURL();
		Text field = toolkit.createText(grp, url, SWT.READ_ONLY);
		GridData gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = SWT.FILL;
		field.setLayoutData(gd);
		if (currentNode != null)
			field.setEnabled(currentNode.isEditable());
	}

	private void post(Label widget, Image icon) {
		if (icon != null && widget != null && !widget.isDisposed())
			widget.setImage(icon); // SWT errors if text is null
	}

	private void post(Label widget, String text) {
		if (text != null && widget != null && !widget.isDisposed())
			widget.setText(text); // SWT errors if text is null
	}

	private void post(Table table, ValidationFinding f, Node node) {
		String src;
		TLModelElement tlObj = ((TLModelElement) f.getSource());
		if (tlObj.getListeners().isEmpty())
			src = f.getSource().getValidationIdentity();
		else {
			Node n = node.getNode(((TLModelElement) f.getSource()).getListeners());
			src = n != null ? n.getName() : "";
		}
		if (table != null && !table.isDisposed()) {
			TableItem item = new TableItem(table, SWT.NONE);
			String[] itemText = new String[3];
			itemText[0] = f.getType().getDisplayName();
			itemText[1] = src;
			itemText[2] = f.getFormattedMessage(FindingMessageFormat.MESSAGE_ONLY_FORMAT);
			item.setText(itemText);
		}
	}

	private void post(Table table, ValidationFindings findings, Node node) {
		// Consider setting up a table viewer instead.
		if (table == null || table.isDisposed())
			return;
		if (findings.count() < 1)
			return;

		TableColumn type = new TableColumn(table, SWT.LEFT);
		TableColumn name = new TableColumn(table, SWT.LEFT);
		TableColumn msg = new TableColumn(table, SWT.LEFT);
		type.setText("Type");
		type.setWidth(50);
		name.setText("Component");
		name.setWidth(100);
		msg.setText("Description");
		msg.setWidth(500);

		for (ValidationFinding f : findings.getFindingsAsList(FindingType.ERROR))
			post(table, f, node);
		for (ValidationFinding f : findings.getFindingsAsList(FindingType.WARNING))
			post(table, f, node);

		// LOGGER.debug("Posted " + findings.count(FindingType.ERROR) + " : " + findings.count(FindingType.WARNING)
		// + " from " + findings.count());
	}

	private void post(Text widget, String text) {
		if (text != null && widget != null && !widget.isDisposed())
			widget.setText(text); // SWT errors if text is null
	}

	private void removeRecursivelyFromExpansionState(Node item) {
		for (Node child : item.getChildren()) {
			removeRecursivelyFromExpansionState(child);
		}
		if (expansionState != null)
			expansionState.remove(item);
	}

	private void restoreExpansionState() {
		if (viewerIsOk()) {
			viewer.collapseAll();
			for (ResourceMemberInterface node : expansionState)
				viewer.expandToLevel(node, 1);
		}
	}

	private void updateFields(ResourceMemberInterface node) {
		if (node == null || ((Node) node).isDeleted())
			return;
		if (objectPropertyGroup == null || objectPropertyGroup.isDisposed())
			return;

		// Post the object level data
		post(objectIcon, node.getImage());
		// post(rType, node.getComponentType());
		post(rName, node.getName());
		post(rDescription, node.getDescription());
		rName.setData(node);
		post(rType, node.getComponentType() + " - " + node.getTooltip());
		rName.setToolTipText(node.getTooltip());
		rDescription.setData(node);
		rType.pack(true);

		// Enable editing if appropriate
		boolean enabled = ((Node) node).isEditable();
		rName.setEnabled(node.isNameEditable());
		rDescription.setEnabled(enabled);

		// Clear old fields
		for (PostedField pf : postedFields)
			pf.dispose();
		postedFields.clear();

		// Now post the fields associated with this object
		objectPropertyGroup.setText(node.getComponentType() + " Properties");

		// Clear then Post the examples
		//
		for (Control kid : examplesGroup.getChildren())
			kid.dispose();
		for (ActionNode action : ((ResourceNode) node.getOwningComponent()).getActions()) {
			post(examplesGroup, action);
		}

		// Clear then post validation
		//
		validationGroup.setText("Errors and Warnings: " + node.getName());
		for (Control kid : validationGroup.getChildren())
			kid.dispose();
		Table t = getFindingsTable(validationGroup);
		post(t, node.getValidationFindings(), (Node) node);

		// Post the fields
		//
		for (ResourceField field : node.getFields()) {
			PostedField pf = new PostedField();
			postedFields.add(pf);

			switch (field.getType()) {
			case Enum:
				pf.label = toolkit.createLabel(objectPropertyGroup, Messages.getString(field.getKey() + ".text"),
						SWT.NONE);
				pf.label.setBackground(objectPropertyGroup.getBackground());
				pf.combo = new Combo(objectPropertyGroup, SWT.NULL);
				pf.combo.setToolTipText(Messages.getString(field.getKey() + ".tooltip"));
				post(pf.combo, (String[]) field.getData(), field.getValue());
				pf.combo.setEnabled(enabled && field.isEnabled());
				if (field.getListener() != null)
					pf.combo.addSelectionListener(new ComboListener());
				pf.combo.setData(field);
				pf.extra = toolkit.createLabel(objectPropertyGroup, "");
				break;
			case EnumList:
				pf.label = toolkit.createLabel(objectPropertyGroup, Messages.getString(field.getKey() + ".text"),
						SWT.NONE);
				pf.label.setBackground(objectPropertyGroup.getBackground());

				pf.buttons = new Group(objectPropertyGroup, SWT.SHADOW_IN);
				pf.buttons.setText(Messages.getString(field.getKey() + ".text"));
				pf.buttons.setToolTipText(Messages.getString(field.getKey() + ".tooltip"));
				pf.buttons.setData(field);
				// post single button for each item in the list.
				String[] data = (String[]) field.getData();
				pf.buttons.setLayout(new RowLayout(SWT.VERTICAL));
				if (data != null)
					for (int i = 0; i < data.length; i++) {
						if (data[i] != null) {
							Button button = new Button(pf.buttons, SWT.CHECK);
							post(button, data[i], field);
							button.setToolTipText(Messages.getString(field.getKey() + ".tooltip"));
							if (field.getValue() != null)
								button.setSelection(field.getValue().contains(data[i]));
							button.addSelectionListener(new ButtonListener());
						}
					}
				pf.extra = toolkit.createLabel(objectPropertyGroup, "");
				break;
			case CheckButton:
				pf.label = toolkit.createLabel(objectPropertyGroup, Messages.getString(field.getKey() + ".text"),
						SWT.NONE);
				pf.label.setBackground(objectPropertyGroup.getBackground());
				pf.button = new Button(objectPropertyGroup, SWT.CHECK);
				pf.button.setToolTipText(Messages.getString(field.getKey() + ".tooltip"));
				if (field.getValue().equals(Boolean.toString(true)))
					pf.button.setSelection(true);
				pf.button.setData(field);
				pf.button.setEnabled(enabled);
				pf.button.addSelectionListener(new CheckButtonListener());
				pf.extra = toolkit.createLabel(objectPropertyGroup, "");
				break;
			case Int:
				pf.label = toolkit.createLabel(objectPropertyGroup, Messages.getString(field.getKey() + ".text"),
						SWT.NONE);
				pf.label.setBackground(objectPropertyGroup.getBackground());
				pf.spinner = new Spinner(objectPropertyGroup, SWT.NULL);
				pf.spinner.setToolTipText(Messages.getString(field.getKey() + ".tooltip"));
				pf.spinner.setToolTipText(Messages.getString(field.getKey() + ".tooltip"));
				// pf.spinner.setBackground(objectPropertyGroup.getBackground());
				pf.spinner.setValues(Integer.parseInt(field.getValue()), 1, 10000, 0, 1, 100);
				pf.spinner.addSelectionListener(new SpinnerListener());
				pf.spinner.setData(field);
				pf.spinner.setEnabled(enabled);
				pf.extra = toolkit.createLabel(objectPropertyGroup, "");
				break;
			case ObjectSelect:
				pf.label = toolkit.createLabel(objectPropertyGroup, Messages.getString(field.getKey() + ".text"),
						SWT.NONE);
				pf.label.setBackground(objectPropertyGroup.getBackground());

				pf.button = new Button(objectPropertyGroup, SWT.PUSH);
				post(pf.button, field.getValue(), field);
				pf.button.setEnabled(enabled && field.isEnabled());
				pf.button.addSelectionListener(new ObjectSelectionListener());
				// Add a "Remove" button
				Button linkedButton = pf.button;
				pf.clear = new Button(objectPropertyGroup, SWT.PUSH);
				pf.clear.addSelectionListener(new ObjectClearListener(linkedButton));
				post(pf.clear, "-Remove-", field);
				// Enable if not none and this is not an abstract resource
				if (field.getValue().equals(ResourceField.ABSTRACT) || field.getValue().equals(ResourceField.NONE))
					pf.clear.setEnabled(false);
				else
					pf.clear.setEnabled(true);
				break;
			default:
				post(createField(field.getKey(), objectPropertyGroup, pf), field.getValue());
				pf.text.setData(field);
				pf.text.setEnabled(enabled);
				pf.text.addModifyListener(new TextListener());
				pf.text.addSelectionListener(new TextSelectionListener());
				pf.extra = toolkit.createLabel(objectPropertyGroup, "");
				break;
			}
		}
		viewer.refresh();
		relayoutFields();
	}

	// /////////////////////////////////////////////////////////////////
	/**
	 * Each widget is saved in the posted field list such that it can be disposed of properly.
	 */
	private class PostedField {
		public Text text;
		public Label label;
		public Button button;
		public Combo combo;
		public Label icon;
		public Spinner spinner;
		public Group buttons;
		public Button clear; // optional 3rd button to clear field
		public Label extra; // use when clear button is not used

		public void dispose() {
			if (text != null)
				text.dispose();
			if (label != null)
				label.dispose();
			if (button != null)
				button.dispose();
			if (combo != null)
				combo.dispose();
			if (icon != null)
				icon.dispose();
			if (spinner != null)
				spinner.dispose();
			if (clear != null)
				clear.dispose();
			if (extra != null)
				extra.dispose();
			if (buttons != null)
				buttons.dispose();
		}
	}

	// /////////////////////////////////////////////////////////////////
	//
	// Listener Classes
	//
	// Except for name and description, the listeners use the ResourceField data associated with the widget to access
	// the appropriate listener for the specific data field.
	//
	public class DescriptionListener implements ModifyListener {
		@Override
		public void modifyText(ModifyEvent e) {
			if (!(e.getSource() instanceof Text) || ignoreListener)
				return;
			Text text = (Text) e.getSource();
			if (text.getData() instanceof ResourceMemberInterface && text.getText() != null)
				((ResourceMemberInterface) text.getData()).setDescription(text.getText());
		}
	}

	public class TextSelectionListener implements SelectionListener {
		@Override
		public void widgetSelected(SelectionEvent e) {
			refresh();
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			refresh();
		}
	}

	public class NameListener implements ModifyListener {
		@Override
		public void modifyText(ModifyEvent e) {
			if (!(e.getSource() instanceof Text) || ignoreListener)
				return;
			Text text = (Text) e.getSource();
			if (text.getData() instanceof ResourceMemberInterface && text.getText() != null) {
				((ResourceMemberInterface) text.getData()).setName(text.getText());
				// viewer.refresh(text.getData());
			}
		}
	}

	public class TextListener implements ModifyListener {
		@Override
		public void modifyText(ModifyEvent e) {
			if (!(e.getSource() instanceof Text) || ignoreListener)
				return;
			Text text = (Text) e.getSource();
			if (!(text.getData() instanceof ResourceField))
				return;
			ResourceField field = (ResourceField) text.getData();
			// use listener to set value and refresh if listener returns true
			if (field.getListener() != null)
				field.getListener().set(text.getText());
			// if (field.getListener().set(text.getText()))
			// refresh(); // display changes to other fields and tree
			// TODO - don't refresh until you figure out how to regain focus
		}
	}

	class ButtonListener implements SelectionListener {

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			// Field data is in the parent group
			if (!(e.getSource() instanceof Button) || ignoreListener)
				return;

			Button button = (Button) e.getSource();
			// Composite parent = button.getParent();
			if (!(button.getData() instanceof ResourceField))
				return;
			ResourceField field = (ResourceField) button.getData();
			String value = button.getText();
			// LOGGER.debug("Button " + value + " selected? " + button.getSelection());
			if (field.getListener() != null)
				field.getListener().set(value);
			refresh();
		}
	}

	class ObjectClearListener implements SelectionListener {
		Button linkedButton; // Button to set the value into

		/**
		 * Clear the subject object field. Must pass the button used to display the subject for it to change when
		 * cleared.
		 * 
		 * @param button
		 */
		public ObjectClearListener(Button button) {
			this.linkedButton = button;
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			// Field data is in the parent group
			if (!(e.getSource() instanceof Button) || ignoreListener)
				return;
			Button button = (Button) e.getSource();
			if (!(button.getData() instanceof ResourceField))
				return;
			ResourceField field = (ResourceField) button.getData();
			field.getListener().set(null);
			linkedButton.setText(ResourceField.NONE);
			field.setValue(ResourceField.NONE);
			refresh();
		}

	}

	class ObjectSelectionListener implements SelectionListener {

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			// Field data is in the parent group
			if (!(e.getSource() instanceof Button) || ignoreListener)
				return;
			Button button = (Button) e.getSource();
			if (!(button.getData() instanceof ResourceField))
				return;

			// Run the object selection wizard.
			ResourceField field = (ResourceField) button.getData();
			Node subject = null;
			if (field.getData() instanceof Node) {
				final TypeSelectionWizard wizard = new TypeSelectionWizard((Node) field.getData());
				if (wizard.run(OtmRegistry.getActiveShell())) {
					subject = wizard.getSelection();
					if (field.getListener() != null)
						if (field.getListener() instanceof ActionFacet.BasePayloadListener)
							((ActionFacet.BasePayloadListener) field.getListener()).set(subject);
						else if (field.getListener() instanceof ResourceNode.SubjectListener)
							((ResourceNode.SubjectListener) field.getListener()).set(subject);
				}
			}
			if (subject != null) {
				field.setData(subject);
				field.setValue(subject.getName());
				button.setText(field.getValue());
			}
			refresh();
		}
	}

	class CheckButtonListener implements SelectionListener {

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			// Field data is in the parent group
			if (!(e.getSource() instanceof Button) || ignoreListener)
				return;

			Button button = (Button) e.getSource();
			if (!(button.getData() instanceof ResourceField))
				return;
			ResourceField field = (ResourceField) button.getData();
			// LOGGER.debug("Button selected? " + button.getSelection());

			if (field.getListener() != null) {
				// Issue a warning dialog if this is setting a resource to abstract
				if (button.getSelection() && field.getListener() instanceof ResourceNode.AbstractListener) {
					if (DialogUserNotifier.openConfirm(Messages.getString("rest.ResourceNode.dialog.abstract.title"),
							Messages.getString("rest.ResourceNode.dialog.abstract.text")))
						field.getListener().set(Boolean.toString(button.getSelection()));
					else
						button.setSelection(false);
				} else
					field.getListener().set(Boolean.toString(button.getSelection()));
				refresh();
			}
		}
	}

	class ComboListener implements SelectionListener {
		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			// LOGGER.debug("Event: " + e);
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			// LOGGER.debug("Combo Listener - event: " + e);
			if (!(e.getSource() instanceof Combo) || ignoreListener)
				return;

			Combo combo = (Combo) e.getSource();
			if (!(combo.getData() instanceof ResourceField))
				return;
			ResourceField field = (ResourceField) combo.getData();
			if (field.getListener() == null)
				return;
			String txt = combo.getText();

			// int index = combo.getSelectionIndex();
			// String[] valueSet = (String[]) field.getData();
			// String value = valueSet[index];
			field.getListener().set(txt);
			refresh(); // display changes to other fields and tree
		}
	}

	class SpinnerListener implements SelectionListener {

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			// LOGGER.debug("Event: " + e);
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			if (!(e.getSource() instanceof Spinner) || ignoreListener)
				return;
			Spinner spinner = (Spinner) e.getSource();
			if (!(spinner.getData() instanceof ResourceField))
				return;
			ResourceField field = (ResourceField) spinner.getData();
			if (field.getListener() == null)
				return;
			field.getListener().set(Integer.toString(spinner.getSelection()));
			refresh();
		}

	}
}
