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
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.opentravel.schemacompiler.model.TLDocumentationOwner;
import org.opentravel.schemas.actions.AddDocItemAction;
import org.opentravel.schemas.actions.ClearDocItemAction;
import org.opentravel.schemas.actions.CloneDocItemAction;
import org.opentravel.schemas.actions.DeleteDocItemAction;
import org.opentravel.schemas.actions.DownDocItemAction;
import org.opentravel.schemas.actions.NextDocItemAction;
import org.opentravel.schemas.actions.PrevDocItemAction;
import org.opentravel.schemas.actions.UpDocItemAction;
import org.opentravel.schemas.controllers.DefaultDocumentationController;
import org.opentravel.schemas.controllers.DocumentationController;
import org.opentravel.schemas.navigation.DoubleClickSelection;
import org.opentravel.schemas.node.DocumentationNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.controllers.DocumentationNodeModelManager;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.properties.ExternalizedStringProperties;
import org.opentravel.schemas.properties.Messages;
import org.opentravel.schemas.stl2developer.MainWindow;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.trees.documentation.DocumentationTreeContentProvider;
import org.opentravel.schemas.trees.documentation.DocumentationTreeLabelProvider;
import org.opentravel.schemas.widgets.ButtonBarManager;
import org.opentravel.schemas.widgets.WidgetFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO - spell check http://blog.ankursharma.org/2009/08/adding-spellchecking-to-custom-editors_19.html
 * TextSourceViewerConfiguration textViewer = new TextSourceViewerConfiguration();
 * 
 * @author dmh
 *
 */
public class DocumentationView extends OtmAbstractView
		implements ISelectionListener, ISelectionChangedListener, ITreeViewerListener, ModifyListener {
	public static String VIEW_ID = "org.opentravel.schemas.stl2Developer.DocumentationView";

	private class DocDragSourceListener implements DragSourceListener {
		private final TreeViewer viewer;

		private DocDragSourceListener(TreeViewer viewer) {
			this.viewer = viewer;
		}

		@Override
		public void dragStart(DragSourceEvent event) {
			if (!viewerIsOk()) {
				event.doit = false;
				return;
			}
			IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();

			Object firstElement = selection.getFirstElement();
			if (firstElement instanceof DocumentationNode) {
				if (((DocumentationNode) firstElement).isDocItem()) {
					event.doit = true;
					return;
				}
			}
			event.doit = false;
		}

		@Override
		public void dragSetData(DragSourceEvent event) {
			if (!viewerIsOk()) {
				return;
			}
			IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();

			Object firstElement = selection.getFirstElement();
			if (firstElement instanceof DocumentationNode) {
				event.data = ((DocumentationNode) firstElement).getLabel();
			}
		}

		@Override
		public void dragFinished(DragSourceEvent event) {

		}
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(DocumentationView.class);

	private DocumentationController controller;

	private FormToolkit toolkit;
	private TreeViewer viewer;
	private Text typeText;
	private Text nameText;
	private Text docText;

	private Action addDocItemAction;
	private Action cloneDocItemAction;
	private Action deleteDocItemAction;

	private Action upDocItemAction;
	private Action downDocItemAction;
	private Action clearDocItemAction;
	private Action prevDocItemAction;
	private Action nextDocItemAction;

	private INode currentNode;
	private DocumentationNode currentTypeRoot;
	private List<DocumentationNode> expansionState = new LinkedList<>();

	private SashForm mainSashForm;
	private ButtonBarManager bbManager;

	private boolean listening = true;

	public DocumentationView() {
		controller = new DefaultDocumentationController(this);
	}

	private boolean viewerIsOk() {
		if (docText == null || docText.isDisposed())
			return false;
		if (typeText == null || typeText.isDisposed())
			return false;
		if (nameText == null || nameText.isDisposed())
			return false;
		if (mainSashForm == null || mainSashForm.isDisposed())
			return false;
		if (expansionState == null)
			return false;
		return viewer != null && viewer.getControl() != null && !viewer.getControl().isDisposed();
	}

	@Override
	public void createPartControl(Composite parent) {
		// LOGGER.info("Initializing part control of " + this.getClass());

		OtmRegistry.registerDocumentationView(this);

		getSite().getPage().addSelectionListener(NavigatorView.VIEW_ID, this);
		getSite().getPage().addSelectionListener(TypeView.VIEW_ID, this);

		final MainWindow mainWindow = OtmRegistry.getMainWindow();

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

		toolkit.createLabel(compLeft, "Component Type", SWT.NONE);

		typeText = toolkit.createText(compLeft, "", SWT.BORDER | SWT.READ_ONLY);
		typeText.setLayoutData(textGD);

		toolkit.createLabel(compLeft, "Name", SWT.NONE);

		nameText = toolkit.createText(compLeft, "", SWT.BORDER | SWT.READ_ONLY);
		nameText.setLayoutData(textGD);

		addDocItemAction = new AddDocItemAction(mainWindow, new ExternalizedStringProperties("action.addDocItem"));
		cloneDocItemAction = new CloneDocItemAction(mainWindow,
				new ExternalizedStringProperties("action.cloneDocItem"));
		deleteDocItemAction = new DeleteDocItemAction(mainWindow,
				new ExternalizedStringProperties("action.deleteDocItem"));
		upDocItemAction = new UpDocItemAction(mainWindow, new ExternalizedStringProperties("action.upDocItem"));
		downDocItemAction = new DownDocItemAction(mainWindow, new ExternalizedStringProperties("action.downDocItem"));
		clearDocItemAction = new ClearDocItemAction(mainWindow,
				new ExternalizedStringProperties("action.clearDocItem"));

		bbManager = new ButtonBarManager(SWT.FLAT);
		bbManager.add(addDocItemAction);
		bbManager.add(cloneDocItemAction);
		bbManager.add(deleteDocItemAction);
		bbManager.add(upDocItemAction);
		bbManager.add(downDocItemAction);
		bbManager.add(clearDocItemAction);

		Composite bb = bbManager.createControl(toolkit, compLeft);
		bb.setLayoutData(bbGD);

		viewer = initializeTreeViewer(compLeft);
		viewer.getTree().setLayoutData(viewerGD);
		viewer.addSelectionChangedListener(this);
		viewer.addTreeListener(this);
		getSite().setSelectionProvider(viewer);

		ScrolledForm formRight = toolkit.createScrolledForm(mainSashForm);

		GridLayout layoutRight = new GridLayout(2, true);
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
		navBbGD.horizontalAlignment = SWT.RIGHT;

		Composite compRight = formRight.getBody();
		compRight.setLayout(layoutRight);

		prevDocItemAction = new PrevDocItemAction(mainWindow, new ExternalizedStringProperties("action.prevDocItem"));
		nextDocItemAction = new NextDocItemAction(mainWindow, new ExternalizedStringProperties("action.nextDocItem"));

		ButtonBarManager navBbManager = new ButtonBarManager(SWT.FLAT);
		navBbManager.add(prevDocItemAction);
		navBbManager.add(nextDocItemAction);

		Composite navBB = navBbManager.createControl(toolkit, compRight);
		navBB.setLayoutData(navBbGD);

		docText = toolkit.createText(compRight, "", SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		docText.setLayoutData(docTextGD);
		docText.addModifyListener(this);
		// docText.setToolTipText("HI");

		mainSashForm.setWeights(new int[] { 1, 1 });
		Node node = getMainController().getCurrentNode_NavigatorView();
		setCurrentNode(node, true);

		// LOGGER.info("Done initializing part control of " + this.getClass());
	}

	private TreeViewer initializeTreeViewer(Composite parent) {
		final TreeViewer viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		viewer.setContentProvider(new DocumentationTreeContentProvider());
		viewer.setLabelProvider(new DocumentationTreeLabelProvider());
		viewer.addDragSupport(DND.DROP_COPY | DND.DROP_MOVE, new Transfer[] { TextTransfer.getInstance() },
				new DocDragSourceListener(viewer));
		viewer.addDropSupport(DND.DROP_COPY | DND.DROP_MOVE, new Transfer[] { TextTransfer.getInstance() },
				new ViewerDropAdapter(viewer) {

					@Override
					public boolean performDrop(Object data) {
						if (data != null) {
							getDocumentationController().changeDocItemsType();
							return true;
						}
						return false;
					}

					@Override
					public void drop(DropTargetEvent event) {
						Object target = determineTarget(event);
						if (target instanceof DocumentationNode) {
							setSelectedTypeRoot(((DocumentationNode) target).getOwningTypeRoot());
						} else {
							setSelectedTypeRoot(null);
						}
						super.drop(event);
					}

					@Override
					public boolean validateDrop(Object target, int operation, TransferData transferType) {
						if (target instanceof DocumentationNode) {
							return true;
						}
						return false;
					}

				});
		return viewer;
	}

	public DocumentationController getDocumentationController() {
		return controller;
	}

	@Override
	public void setFocus() {
		refreshAllViews();
		docText.setFocus();
	}

	public void setFocus(DocumentationNode node) {
		select(node);
		docText.setFocus();
	}

	/**
	 * @param node
	 */
	public void select(DocumentationNode node) {
		if (viewerIsOk())
			viewer.setSelection(new StructuredSelection(node), true);
	}

	public void setFocus(List<DocumentationNode> nodes) {
		select(nodes);
		docText.setFocus();
	}

	/**
	 * @param nodes
	 */
	public void select(List<DocumentationNode> nodes) {
		if (viewerIsOk())
			viewer.setSelection(new StructuredSelection(nodes), true);
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		final IStructuredSelection iss = (IStructuredSelection) selection;
		final Object object = iss.getFirstElement();
		if (object instanceof Node) {
			if (selection instanceof DoubleClickSelection) {
				setCurrentNode((Node) object, true);
			} else {
				setCurrentNode((Node) object);
			}
		}
	}

	@Override
	public void setCurrentNode(INode node) {
		currentNode = node;
		postNode(currentNode);
	}

	public void setCurrentNode(INode node, boolean force) {
		this.currentNode = node;
		postNode(node, force);
	}

	@Override
	public INode getCurrentNode() {
		return currentNode;
	}

	public void postNode(INode node, boolean force) {
		if (listening || force) {
			DocumentationNode root = generateDocumentationTree(node);
			updateView(root);
			updateObjectFields(node);
			setEditState(node);
			refreshAllViews();
		}
	}

	// 3/10/2015 dmh - attempt to prevent edit on non-editable nodes.
	private void setEditState(INode node) {
		// TODO
		// bbManager.enable(node.isEditable());
		// if (node.isEditable()) {
		// // 1. warn user when they can't edit text.
		// // doesn't work docText.addModifyListener(this);
		// } else {
		// // doesn't work docText.removeModifyListener(this);
		// }
	}

	public void postNode(TLDocumentationOwner owner, boolean force) {
		if (listening || force) {
			DocumentationNode root = generateDocumentationTree(owner);
			updateView(root);
			// updateObjectFields(node);
			refreshAllViews();
		}
	}

	/**
	 * @param root
	 */
	private void updateView(DocumentationNode root) {
		if (viewerIsOk())
			viewer.setInput(root);
		restoreExpansionState();
		selectDescription(root);
	}

	public void postNode(INode node) {
		postNode(node, false);
	}

	public void setSelectedTypeRoot(DocumentationNode node) {
		this.currentTypeRoot = node;
	}

	public DocumentationNode getSelectedTypeRoot() {
		return currentTypeRoot;
	}

	private void updateObjectFields(INode node) {
		if (viewerIsOk())
			if (node != null) {
				typeText.setText(getStringOrEmpty(node.getComponentType()));
				nameText.setText(getStringOrEmpty(node.getName()));
			} else {
				typeText.setText("");
				nameText.setText("");
			}
	}

	private String getStringOrEmpty(String str) {
		return str == null ? "" : str;
	}

	@Override
	public void refreshAllViews() {
		if (viewerIsOk())
			viewer.refresh(true);
		updateDocField(getSelectedDocumentationNode());
	}

	public void forceCurrentNode() {
		postNode(currentNode, true);
	}

	/**
	 * @param node
	 * @return
	 */
	private DocumentationNode generateDocumentationTree(final INode node) {
		if (node == null) {
			return null;
		}
		Node n = (Node) node;
		DocumentationNode root = null;
		if (n.isDocumentationOwner())
			root = new DocumentationNodeModelManager().createDocumentationTreeRoot(n.getDocumentation());

		// ModelObject<?> model = node.getModelObject();
		// if (model != null && model.isDocumentationOwner()) {
		// root = new DocumentationNodeModelManager().createDocumentationTreeRoot(model.getDocumentation());
		// }
		return root;
	}

	private DocumentationNode generateDocumentationTree(final TLDocumentationOwner model) {
		DocumentationNode root = null;
		if (model != null) {
			root = new DocumentationNodeModelManager().createDocumentationTreeRoot(model.getDocumentation());
		}
		return root;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers
	 * .SelectionChangedEvent)
	 */
	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		DocumentationNode docItem = getSelectedDocumentationNode();
		updateDocField(docItem);

		// final IStructuredSelection iss = (IStructuredSelection) viewer.getSelection();
		// final Object object = iss.getFirstElement();
		// if ((object != null) && (object instanceof DocumentationNode)) {
		// DocumentationNode docItem = (DocumentationNode) object;
		// updateDocField(docItem);
		// } else {
		// updateDocField(null);
		// }
	}

	/**
	 * @param docItem
	 */
	private void updateDocField(DocumentationNode docItem) {
		if (viewerIsOk())
			if (docItem != null && docItem.isDocItem()) {
				docText.setEnabled(true);
				docText.setData(docItem);
				docText.setText(getStringOrEmpty(docItem.getValue()));
			} else {
				docText.setEnabled(false); // prevent user from typing when nothing is selected
				docText.setData(null);
				docText.setText(Messages.getString("view.documentation.noneditable"));
			}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ITreeViewerListener#treeCollapsed(org.eclipse.jface.viewers. TreeExpansionEvent)
	 */
	@Override
	public void treeCollapsed(TreeExpansionEvent event) {
		Object collapsed = event.getElement();
		if (collapsed instanceof DocumentationNode) {
			DocumentationNode doc = (DocumentationNode) collapsed;
			removeRecursivelyFromExpansionState(doc);
		}
	}

	private void removeRecursivelyFromExpansionState(DocumentationNode doc) {
		for (DocumentationNode child : doc.getChildren()) {
			removeRecursivelyFromExpansionState(child);
		}
		if (viewerIsOk())
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
		if (viewerIsOk())
			if (expanded instanceof DocumentationNode) {
				expansionState.add((DocumentationNode) expanded);
			}
	}

	private void restoreExpansionState() {
		if (viewerIsOk())
			for (DocumentationNode doc : expansionState) {
				viewer.expandToLevel(doc, 1);
			}
	}

	private void selectDescription(DocumentationNode root) {
		if (root != null) {
			for (DocumentationNode n : root.getChildren()) {
				if ("Description".equals(n.getLabel())) {
					DocumentationNode desc = n.getChildren().size() > 0 ? n.getChildren().get(0) : null;
					select(desc);
					return;
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
	 */
	@Override
	public void modifyText(ModifyEvent e) {
		String str = docText.getText();
		Object data = docText.getData();
		if (data instanceof DocumentationNode) {
			DocumentationNode docItem = (DocumentationNode) data;
			// This runs often..on startup, and multiple times when object posted.
			if (getCurrentNode().isEditable())
				docItem.setValue(str);
			if (viewerIsOk())
				viewer.refresh(docItem, true);
			// TODO - if this was deprecation, refresh the navigator node to change its color.
		}
	}

	public DocumentationNode getSelectedDocumentationNode() {
		if (viewerIsOk()) {
			StructuredSelection selection = (StructuredSelection) viewer.getSelection();
			Object object = selection.getFirstElement();
			if (object != null && object instanceof DocumentationNode) {
				return (DocumentationNode) object;
			}
		}
		return null;
	}

	public List<DocumentationNode> getSelectedDocumentationNodes() {
		List<DocumentationNode> nodes = new ArrayList<>();
		if (viewerIsOk()) {
			StructuredSelection selection = (StructuredSelection) viewer.getSelection();
			for (Object object : selection.toArray()) {
				if (object != null && object instanceof DocumentationNode) {
					nodes.add((DocumentationNode) object);
				}
			}
		}
		return nodes;
	}

	/**
	 * 
	 */
	public void nextDocItem() {
		// TODO - change to get description from next property in facet view table
		// FacetView facetView = (FacetView) OtmRegistry.getFacetView();

		// Old Code - move through this documentation item
		DocumentationNode item = getSelectedDocumentationNode();
		if (item != null) {
			DocumentationNode root = item.getOwningTypeRoot();
			List<DocumentationNode> children = root.getChildren();
			int index = -1;
			if (item.isDocItem()) {
				index = children.indexOf(item);
			}
			if (index < children.size() - 1) {
				DocumentationNode nextItem = children.get(index + 1);
				setFocus(nextItem);
			}
		}
	}

	public void prevDocItem() {
		DocumentationNode item = getSelectedDocumentationNode();
		if (item != null) {
			DocumentationNode root = item.getOwningTypeRoot();
			List<DocumentationNode> children = root.getChildren();
			int index = -1;
			if (item.isDocItem()) {
				index = children.indexOf(item);
			}
			if (index > 0) {
				DocumentationNode prevItem = children.get(index - 1);
				setFocus(prevItem);
			}
		}
	}

	public void setHorizontalView() {
		if (viewerIsOk())
			mainSashForm.setOrientation(SWT.HORIZONTAL);
	}

	public void setVerticalView() {
		if (viewerIsOk())
			mainSashForm.setOrientation(SWT.VERTICAL);
	}

	@Override
	public boolean isListening() {
		return listening;
	}

	@Override
	public void setListening(boolean listening) {
		this.listening = listening;
		postNode(currentNode, false);
	}

	@Override
	public List<Node> getSelectedNodes() {
		return null;
	}

	@Override
	public String getViewID() {
		return VIEW_ID;
	}

	@Override
	public void refresh() {
		postNode(currentNode);
	}

	@Override
	public void refresh(INode node) {
		setCurrentNode(node);
		postNode(node);
	}

	@Override
	public void refresh(INode node, boolean force) {
		setCurrentNode(node, force);
	}

	@Override
	public void remove(INode node) {
		if (viewerIsOk())
			viewer.remove(node);
	}

}
