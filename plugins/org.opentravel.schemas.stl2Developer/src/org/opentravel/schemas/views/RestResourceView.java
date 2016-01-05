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

import org.eclipse.jface.action.Action;
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.opentravel.schemas.actions.AddContextAction;
import org.opentravel.schemas.node.ContextNode;
import org.opentravel.schemas.node.LibraryNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.interfaces.ResourceMemberInterface;
import org.opentravel.schemas.node.resources.ResourceField;
import org.opentravel.schemas.properties.ExternalizedStringProperties;
import org.opentravel.schemas.properties.Messages;
import org.opentravel.schemas.stl2developer.MainWindow;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.trees.REST.RestTreeContentProvider;
import org.opentravel.schemas.trees.REST.RestTreeLabelProvider;
import org.opentravel.schemas.widgets.ButtonBarManager;
import org.opentravel.schemas.widgets.WidgetFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Manage the view onto REST resource descriptions
 * 
 * @author Dave Hollander
 * 
 */
public class RestResourceView extends OtmAbstractView implements ISelectionListener, ISelectionChangedListener,
		ITreeViewerListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(RestResourceView.class);
	public static String VIEW_ID = "org.opentravel.schemas.stl2Developer.ResourceView";

	private FormToolkit toolkit;
	private TreeViewer viewer;
	private boolean ignoreListener = false; // used by listeners during selection changes

	private Label objectIcon;
	private Text rName;
	private Label rType;
	private Text rDescription;

	private List<ContextNode> expansionState = new LinkedList<ContextNode>();

	private INode currentNode;
	private INode previousNode;

	private Action addAction;
	private SashForm mainSashForm;
	private ButtonBarManager bbManager;
	private Composite compRight;

	private List<PostedField> postedFields = new ArrayList<PostedField>();
	private Group objectPropertyGroup;

	private class PostedField {
		public Text text;
		public Label label;
		public Button button;
		public Combo combo;
		public Label icon;
		public Spinner spinner;
		public Group buttons;

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
			if (buttons != null)
				buttons.dispose();
		}
	}

	public RestResourceView() {
		OtmRegistry.registerResourceView(this);
	}

	@Override
	public void createPartControl(final Composite parent) {
		LOGGER.info("Initializing part control of " + this.getClass());

		// getSite().getPage().addSelectionListener("org.opentravel.schemas.stl2Developer.ModelNavigatorView", this);
		// getSite().setSelectionProvider(this);
		getSite().getPage().addSelectionListener(NavigatorView.VIEW_ID, this);
		select(mc.getCurrentNode_NavigatorView());

		// Define actions for the button bar
		final MainWindow mainWindow = OtmRegistry.getMainWindow();
		addAction = new AddContextAction(mainWindow, new ExternalizedStringProperties("action.addContext"));

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

		// bbManager = new ButtonBarManager(SWT.FLAT);
		// bbManager.add(addAction);
		// Composite bb = bbManager.createControl(toolkit, compLeft);
		// bb.setLayoutData(bbGD);

		viewer = new TreeViewer(compLeft, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		initializeTreeViewer();

		// viewer = initializeTreeViewer(compLeft);
		viewer.getTree().setLayoutData(viewerGD);
		// viewer.addSelectionChangedListener(this);
		// viewer.addTreeListener(this);
		// getSite().setSelectionProvider(viewer);

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

		ButtonBarManager bbManager = new ButtonBarManager(SWT.FLAT);
		bbManager.add(addAction);
		Composite navBB = bbManager.createControl(toolkit, compRight);
		navBB.setLayoutData(navBbGD);

		// Add fixed text fields
		//
		// For the type, create in line to have icon and label instead of label and text
		Color bgColor = compRight.getBackground();
		objectIcon = toolkit.createLabel(compRight, "", SWT.NONE);
		objectIcon.setBackground(bgColor);
		// GridData oGD = new GridData();
		// oGD.verticalSpan = 3;
		// objectIcon.setLayoutData(oGD);
		// rType = toolkit.createLabel(compRight, "", SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		rType = toolkit.createLabel(compRight, "", SWT.NONE);
		// rType = toolkit.createLabel(compRight, "", SWT.MULTI | SWT.BORDER | SWT.WRAP);
		rType.setBackground(bgColor);
		// GridData rGD = new GridData(GridData.FILL_BOTH);
		// GridData rGD = new GridData();
		// rType.setLayoutData(new GridData());
		// rGD.grabExcessHorizontalSpace = true;
		// rGD.horizontalAlignment = SWT.FILL;
		// rGD.verticalSpan = 3;
		// rType.setLayoutData(rGD);

		rName = createField("rest.ResourceNode.fields.name", compRight, null);
		rName.addModifyListener(new NameListener());
		rDescription = createField("rest.label.description", compRight, null);
		rDescription.addModifyListener(new DescriptionListener());

		// Fields use the objectPropertyGroup as the composite
		//
		objectPropertyGroup = new Group(compRight, SWT.NULL);
		objectPropertyGroup.setText("Object Properties");
		GridLayout opGL = new GridLayout();
		opGL.numColumns = 2;
		objectPropertyGroup.setLayout(opGL);
		GridData opGD = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		opGD.horizontalSpan = 2;
		objectPropertyGroup.setLayoutData(opGD);

		mainSashForm.setWeights(new int[] { 2, 3 });

		setCurrentNode(mc.getCurrentNode_NavigatorView());
		ignoreListener = true; // turn off any listeners
		postResources();
		ignoreListener = false; // enable listeners
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

	private TreeViewer initializeTreeViewer() {
		// final TreeViewer viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		viewer.addSelectionChangedListener(this);
		viewer.addTreeListener(this);

		viewer.setContentProvider(new RestTreeContentProvider());
		viewer.setLabelProvider(new RestTreeLabelProvider());

		// viewer.addDragSupport(DND.DROP_COPY | DND.DROP_MOVE, new Transfer[] { TextTransfer.getInstance() },
		// new DragSourceListener() {
		//
		// @Override
		// public void dragStart(DragSourceEvent event) {
		// IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
		//
		// Object firstElement = selection.getFirstElement();
		// if (firstElement instanceof ContextNode) {
		// if (((ContextNode) firstElement).isContextItem()) {
		// event.doit = true;
		// return;
		// }
		// }
		// event.doit = false;
		// }
		//
		// @Override
		// public void dragSetData(DragSourceEvent event) {
		// IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
		//
		// Object firstElement = selection.getFirstElement();
		// if (firstElement instanceof ContextNode) {
		// event.data = ((ContextNode) firstElement).getContextId();
		// }
		// }
		//
		// @Override
		// public void dragFinished(DragSourceEvent event) {
		//
		// }
		//
		// });
		// viewer.addDropSupport(DND.DROP_COPY | DND.DROP_MOVE, new Transfer[] { TextTransfer.getInstance() },
		// new ViewerDropAdapter(viewer) {
		//
		// @Override
		// public boolean performDrop(Object data) {
		// if (data != null) {
		// // getContextController().copyContext();
		// return true;
		// }
		// return false;
		// }
		//
		// @Override
		// public void drop(DropTargetEvent event) {
		// Object target = determineTarget(event);
		// if (target instanceof ContextNode) {
		// setSelectedTypeRoot(((ContextNode) target).getOwningLibraryRoot());
		// } else {
		// setSelectedTypeRoot(null);
		// }
		// super.drop(event);
		// }
		//
		// @Override
		// public boolean validateDrop(Object target, int operation, TransferData transferType) {
		// if (target instanceof ContextNode) {
		// return true;
		// }
		// return false;
		// }
		//
		// });

		return viewer;
	}

	@Override
	public void setFocus() {
		LOGGER.debug("setFocus.");
		refreshAllViews();
	}

	// public void setFocus(ContextNode node) {
	// LOGGER.debug("setFocus.");
	// select(node);
	// rName.setFocus();
	// }

	public void select(ContextNode node) {
		viewer.setSelection(new StructuredSelection(node), true);
	}

	@Override
	public void refreshAllViews() {
		viewer.refresh(true);
		updateFields(getSelectedResourceNode());
		// on starting type view can be null
		// if (OtmRegistry.getTypeView() != null) {
		// OtmRegistry.getTypeView().refresh();
		// }
	}

	/**
	 * Create then post a complete tree starting with the model root.
	 * 
	 * @param forceRefresh
	 */
	public void postResources() {
		LibraryNode rootLibrary = mc.getCurrentNode_NavigatorView().getLibrary();
		if (rootLibrary != null && !rootLibrary.getResourceRoot().getChildren().isEmpty())
			viewer.setInput(rootLibrary.getResourceRoot());
		restoreExpansionState();
		refreshAllViews();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart,
	 * org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void selectionChanged(final IWorkbenchPart part, final ISelection selection) {
		LOGGER.debug("selection changed. \t" + selection.getClass());
		final IStructuredSelection iss = (IStructuredSelection) selection;
		// the data should be the first element selected and it should be a Node
		final Object object = iss.getFirstElement();
		if (!(object instanceof Node))
			return;

		// switch libraries if user navigated to a new library
		if (part instanceof NavigatorView)
			if (((Node) object).getLibrary() == currentNode.getLibrary())
				return; // no update needed

		ignoreListener = true;
		setCurrentNode((Node) object);
		postResources();
		ignoreListener = false;
	}

	@Override
	public INode getCurrentNode() {
		return currentNode;
	}

	@Override
	public INode getPreviousNode() {
		return previousNode;
	}

	@Override
	public void setCurrentNode(final INode node) {
		previousNode = currentNode;
		currentNode = node;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ITreeViewerListener#treeCollapsed(org.eclipse.jface.viewers. TreeExpansionEvent)
	 */
	@Override
	public void treeCollapsed(TreeExpansionEvent event) {
		Object collapsed = event.getElement();
		if (collapsed instanceof ContextNode) {
			ContextNode doc = (ContextNode) collapsed;
			removeRecursivelyFromExpansionState(doc);
		}
	}

	private void removeRecursivelyFromExpansionState(ContextNode doc) {
		for (ContextNode child : doc.getChildren()) {
			removeRecursivelyFromExpansionState(child);
		}
		expansionState.remove(doc);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ITreeViewerListener#treeExpanded(org.eclipse.jface.viewers. TreeExpansionEvent)
	 */
	@Override
	public void treeExpanded(TreeExpansionEvent event) {
		Object expanded = event.getElement();
		if (expanded instanceof ContextNode) {
			expansionState.add((ContextNode) expanded);
		}
	}

	private void restoreExpansionState() {
		viewer.expandAll();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers
	 * .SelectionChangedEvent)
	 */
	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		LOGGER.debug("selection changed. \t" + event.getClass());
		final Object object = ((IStructuredSelection) viewer.getSelection()).getFirstElement();
		if (!(object instanceof Node))
			return;

		ignoreListener = true;
		setCurrentNode((Node) object);
		if (object instanceof ResourceMemberInterface) {
			// updateActions((Node) object);
			updateFields((ResourceMemberInterface) object);
		} else {
			// updateActions(null);
			updateFields(null);
		}
		ignoreListener = false;
	}

	private void updateActions(Node node) {
		boolean enabled = false;
		if (node != null) {
			enabled = node.isEditable();
		}
		// addContextAction.setEnabled(enabled);
		// cloneContextAction.setEnabled(enabled);
		// mergeContextAction.setEnabled(enabled);
		// setContextAsDefaultAction.setEnabled(enabled);
	}

	// Field types: string, int, Enum, list of Int
	// Nodes: Payload (ActionFacet or Core), ResourceNodes, business objects
	private void post(Text widget, String text) {
		if (text != null)
			widget.setText(text); // SWT errors if text is null
	}

	private void post(Label widget, String text) {
		if (text != null)
			widget.setText(text); // SWT errors if text is null
	}

	private void post(Button widget, String text, Object data) {
		if (text != null)
			widget.setText(text);
		widget.setData(data);
	}

	private void post(Label widget, Image icon) {
		if (icon != null)
			widget.setImage(icon); // SWT errors if text is null
	}

	private void post(Combo combo, String[] strings, String value) {
		int i = 0;
		for (String s : strings) {
			if (s == null || s.equals(""))
				s = ResourceField.NONE;
			combo.add(s);
			if (s.equals(value))
				combo.select(i);
			i++;
		}
	}

	private void updateFields(ResourceMemberInterface node) {
		if (node == null)
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

		// List<ResourceField> fields = node.getFields();
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
				pf.combo.setEnabled(enabled);
				if (field.getListener() != null)
					pf.combo.addSelectionListener(new ComboListener());
				pf.combo.setData(field);
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
				pf.button.addSelectionListener(new CheckButtonListener());
				break;
			case Int:
				pf.label = toolkit.createLabel(objectPropertyGroup, Messages.getString(field.getKey() + ".text"),
						SWT.NONE);
				pf.label.setBackground(objectPropertyGroup.getBackground());
				pf.spinner = new Spinner(objectPropertyGroup, SWT.NULL);
				pf.spinner.setToolTipText(Messages.getString(field.getKey() + ".tooltip"));
				pf.spinner.setToolTipText(Messages.getString(field.getKey() + ".tooltip"));
				// pf.spinner.setBackground(objectPropertyGroup.getBackground());
				pf.spinner.setValues(Integer.parseInt(field.getValue()), 1, 1000, 0, 10, 100);
				pf.spinner.addSelectionListener(new SpinnerListener());
				pf.spinner.setData(field);
				pf.spinner.setEnabled(enabled);
				break;
			default:
				post(createField(field.getKey(), objectPropertyGroup, pf), field.getValue());
				pf.text.setData(field);
				pf.text.addModifyListener(new TextListener());
				break;
			}
		}
		viewer.refresh();
		objectPropertyGroup.redraw();
		objectPropertyGroup.layout(true);
		objectPropertyGroup.update();
		compRight.layout(true);
		compRight.update();
	}

	// Only works if the selection is on the rest resource tree
	public ResourceMemberInterface getSelectedResourceNode() {
		if (viewer == null)
			return null; // In case the view is not activated.
		StructuredSelection selection = (StructuredSelection) viewer.getSelection();
		Object object = selection.getFirstElement();
		if (object != null && object instanceof ResourceMemberInterface)
			return (ResourceMemberInterface) object;
		LOGGER.debug("getSelectedResourceNode is returning null.");
		return null;
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
		if (viewer != null) {
			viewer.refresh(true);
			updateFields(getSelectedResourceNode());
		}
	}

	@Override
	public void refresh(INode node) {
		viewer.refresh(true);
		postResources();
	}

	// /////////////////////////////////////////////////////////////////
	//
	// Listener Classes
	//
	// Except for name and description, the listeners use the ResourceField data associated with the widget to access
	// the appropriate listener for the specific data field.
	//
	public class TextListener implements ModifyListener {
		@Override
		public void modifyText(ModifyEvent e) {
			if (!(e.getSource() instanceof Text) || ignoreListener)
				return;
			Text text = (Text) e.getSource();
			if (!(text.getData() instanceof ResourceField))
				return;
			ResourceField field = (ResourceField) text.getData();
			if (field.getListener() != null)
				field.getListener().set(text.getText());
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
				viewer.refresh(text.getData());
			}
		}
	}

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

	class SpinnerListener implements SelectionListener {

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
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			LOGGER.debug("Event: " + e);
		}

	}

	class ComboListener implements SelectionListener {
		@Override
		public void widgetSelected(SelectionEvent e) {
			LOGGER.debug("Combo Listener - event: " + e);
			if (!(e.getSource() instanceof Combo) || ignoreListener)
				return;

			Combo combo = (Combo) e.getSource();
			if (!(combo.getData() instanceof ResourceField))
				return;
			ResourceField field = (ResourceField) combo.getData();
			if (field.getListener() == null)
				return;

			int index = combo.getSelectionIndex();
			String[] valueSet = (String[]) field.getData();
			String value = valueSet[index];
			if (field.getListener().set(value))
				refresh(); // display changes to other fields and tree
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			LOGGER.debug("Event: " + e);
		}
	}

	class ButtonListener implements SelectionListener {

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
			LOGGER.debug("Button  " + value + " selected? " + button.getSelection());
			if (field.getListener() != null)
				field.getListener().set(value);
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
		}
	}

	class CheckButtonListener implements SelectionListener {

		@Override
		public void widgetSelected(SelectionEvent e) {
			// Field data is in the parent group
			if (!(e.getSource() instanceof Button) || ignoreListener)
				return;

			Button button = (Button) e.getSource();
			if (!(button.getData() instanceof ResourceField))
				return;
			ResourceField field = (ResourceField) button.getData();
			LOGGER.debug("Button selected? " + button.getSelection());
			if (field.getListener() != null)
				field.getListener().set(Boolean.toString(button.getSelection()));
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
		}
	}
}
