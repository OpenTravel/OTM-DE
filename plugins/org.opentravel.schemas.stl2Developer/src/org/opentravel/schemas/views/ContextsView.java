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
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.opentravel.schemas.actions.AddContextAction;
import org.opentravel.schemas.actions.CloneContextAction;
import org.opentravel.schemas.actions.MergeContextAction;
import org.opentravel.schemas.actions.SetContextAsDefaultAction;
import org.opentravel.schemas.controllers.ContextController;
import org.opentravel.schemas.node.ContextNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.properties.ExternalizedStringProperties;
import org.opentravel.schemas.properties.Messages;
import org.opentravel.schemas.stl2developer.MainWindow;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.trees.context.ContextTreeContentProvider;
import org.opentravel.schemas.trees.context.ContextTreeLabelProvider;
import org.opentravel.schemas.widgets.ButtonBarManager;
import org.opentravel.schemas.widgets.WidgetFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Manage the context view.
 * 
 * @author Agnieszka Janowska
 * 
 */
public class ContextsView extends OtmAbstractView
		implements ISelectionListener, ISelectionChangedListener, ITreeViewerListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(ContextsView.class);
	public static String VIEW_ID = "org.opentravel.schemas.stl2Developer.ContextsView";

	private FormToolkit toolkit;
	private ScrolledForm form;
	private TreeViewer viewer;
	private Text appCtxText;
	private Text ctxIdText;
	private Text ctxDescriptionText;

	private List<ContextNode> expansionState = new LinkedList<>();

	private INode currentNode;
	private INode previousNode;

	private ContextController controller;

	private Action addContextAction;
	private Action cloneContextAction;
	private Action mergeContextAction;
	private Action setContextAsDefaultAction;

	private ContextNode currentTypeRoot;

	public ContextsView() {
		// controller = new DefaultContextController(OtmRegistry.getMainWindow(), this);
	}

	@Override
	public void createPartControl(final Composite parent) {
		// LOGGER.info("Initializing part control of " + this.getClass());

		OtmRegistry.registerContextsView(this);

		getSite().getPage().addSelectionListener("org.opentravel.schemas.stl2Developer.ModelNavigatorView", this);

		final MainWindow mainWindow = OtmRegistry.getMainWindow();

		toolkit = WidgetFactory.createFormToolkit(parent.getDisplay());
		form = toolkit.createScrolledForm(parent);

		final FillLayout layout = new FillLayout();
		form.getBody().setLayout(layout);

		final GridLayout layoutLeft = new GridLayout(2, false);
		layoutLeft.marginBottom = 10;
		layoutLeft.marginLeft = 10;
		layoutLeft.marginTop = 10;
		layoutLeft.marginRight = 10;

		final GridData textGD = new GridData();
		textGD.grabExcessHorizontalSpace = true;
		textGD.horizontalAlignment = SWT.FILL;

		final GridData contextGD = new GridData();
		contextGD.horizontalAlignment = SWT.FILL;
		contextGD.grabExcessHorizontalSpace = true;
		contextGD.horizontalSpan = 2;

		final GridData viewerGD = new GridData();
		viewerGD.grabExcessHorizontalSpace = true;
		viewerGD.grabExcessVerticalSpace = true;
		viewerGD.horizontalAlignment = SWT.FILL;
		viewerGD.verticalAlignment = SWT.FILL;
		viewerGD.horizontalSpan = 2;

		GridData bbGD = new GridData();
		bbGD.horizontalSpan = 2;
		bbGD.horizontalIndent = 0;
		bbGD.verticalIndent = 0;

		Composite compLeft = form.getBody();
		compLeft.setLayout(layoutLeft);

		String txt = Messages.getString("context.label.applicationContext.text");
		toolkit.createLabel(compLeft, txt, SWT.NONE);

		appCtxText = toolkit.createText(compLeft, "", SWT.BORDER);
		appCtxText.setToolTipText(Messages.getString("context.label.applicationContext.tooltip"));
		appCtxText.setLayoutData(textGD);
		appCtxText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				modifyApplicationContext();
			}

		});

		// Local Name
		txt = Messages.getString("context.label.contextID.text");
		toolkit.createLabel(compLeft, txt, SWT.NONE);

		ctxIdText = toolkit.createText(compLeft, "", SWT.BORDER);
		txt = Messages.getString("context.label.contextID.tooltip");
		ctxIdText.setToolTipText(txt);
		ctxIdText.setLayoutData(textGD);

		ctxIdText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				modifyContextId();
			}
		});

		toolkit.createLabel(compLeft, "Description", SWT.NONE);

		ctxDescriptionText = toolkit.createText(compLeft, "", SWT.BORDER);
		ctxDescriptionText.setLayoutData(textGD);

		ctxDescriptionText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				modifyDescription();
			}
		});

		addContextAction = new AddContextAction(mainWindow, new ExternalizedStringProperties("action.addContext"));
		cloneContextAction = new CloneContextAction(mainWindow,
				new ExternalizedStringProperties("action.cloneContext"));
		mergeContextAction = new MergeContextAction(mainWindow,
				new ExternalizedStringProperties("action.mergeContext"));
		setContextAsDefaultAction = new SetContextAsDefaultAction(mainWindow,
				new ExternalizedStringProperties("action.setContextAsDefault"));

		ButtonBarManager bbManager = new ButtonBarManager(SWT.FLAT);
		bbManager.add(addContextAction);
		bbManager.add(cloneContextAction);
		bbManager.add(mergeContextAction);
		bbManager.add(setContextAsDefaultAction);

		Composite bb = bbManager.createControl(toolkit, compLeft);
		bb.setLayoutData(bbGD);

		viewer = initializeTreeViewer(compLeft);
		viewer.getTree().setLayoutData(viewerGD);
		viewer.addSelectionChangedListener(this);
		viewer.addTreeListener(this);

		setCurrentNode(mc.getCurrentNode_NavigatorView());
		postContexts(true);
	}

	private TreeViewer initializeTreeViewer(Composite parent) {
		final TreeViewer viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		viewer.setContentProvider(new ContextTreeContentProvider());
		viewer.setLabelProvider(new ContextTreeLabelProvider());

		viewer.addDragSupport(DND.DROP_COPY | DND.DROP_MOVE, new Transfer[] { TextTransfer.getInstance() },
				new DragSourceListener() {

					@Override
					public void dragStart(DragSourceEvent event) {
						IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();

						Object firstElement = selection.getFirstElement();
						if (firstElement instanceof ContextNode) {
							if (((ContextNode) firstElement).isContextItem()) {
								event.doit = true;
								return;
							}
						}
						event.doit = false;
					}

					@Override
					public void dragSetData(DragSourceEvent event) {
						IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();

						Object firstElement = selection.getFirstElement();
						if (firstElement instanceof ContextNode) {
							event.data = ((ContextNode) firstElement).getContextId();
						}
					}

					@Override
					public void dragFinished(DragSourceEvent event) {

					}

				});
		viewer.addDropSupport(DND.DROP_COPY | DND.DROP_MOVE, new Transfer[] { TextTransfer.getInstance() },
				new ViewerDropAdapter(viewer) {

					@Override
					public boolean performDrop(Object data) {
						if (data != null) {
							getContextController().copyContext();
							return true;
						}
						return false;
					}

					@Override
					public void drop(DropTargetEvent event) {
						Object target = determineTarget(event);
						if (target instanceof ContextNode) {
							setSelectedTypeRoot(((ContextNode) target).getOwningLibraryRoot());
						} else {
							setSelectedTypeRoot(null);
						}
						super.drop(event);
					}

					@Override
					public boolean validateDrop(Object target, int operation, TransferData transferType) {
						if (target instanceof ContextNode) {
							return true;
						}
						return false;
					}

				});

		return viewer;
	}

	@Override
	public void setFocus() {
		refreshAllViews();
	}

	public void setFocus(ContextNode node) {
		select(node);
		appCtxText.setFocus();
	}

	private boolean viewerIsOk() {
		// if (viewer.getControl().isDisposed()) return false;
		return viewer != null && viewer.getTree() != null && !viewer.getTree().isDisposed();
	}

	public void select(ContextNode node) {
		if (viewerIsOk())
			viewer.setSelection(new StructuredSelection(node), true);
	}

	@Override
	public void refreshAllViews() {
		if (viewerIsOk())
			viewer.refresh(true);
		updateFields(getSelectedContextNode());
		// on starting type view can be null
		if (OtmRegistry.getTypeView() != null) {
			OtmRegistry.getTypeView().refresh();
		}
	}

	/**
	 * Create then post a complete tree starting with the model root.
	 * 
	 * @param forceRefresh
	 */
	public void postContexts(final boolean forceRefresh) {
		final MainWindow mainWindow = OtmRegistry.getMainWindow();
		if (mainWindow == null)
			throw new IllegalArgumentException("Main window not accessible.");
		if (controller == null)
			controller = mc.getContextController();
		if (controller != null && forceRefresh)
			controller.refreshContexts();

		if (viewerIsOk())
			viewer.setInput(controller.getRoot());
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
		final IStructuredSelection iss = (IStructuredSelection) selection;
		// the data should be the first element selected and it should be a Node
		final Object object = iss.getFirstElement();
		INode curNode = null;
		if (object instanceof Node) {
			curNode = (INode) object;
		}
		setCurrentNode(curNode);
		postContexts(false);
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
		if (viewerIsOk())
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
		final IStructuredSelection iss = (IStructuredSelection) viewer.getSelection();
		final Object object = iss.getFirstElement();
		if ((object != null) && (object instanceof ContextNode)) {
			ContextNode docItem = (ContextNode) object;
			updateActions(docItem);
			updateFields(docItem);
		} else {
			updateActions(null);
			updateFields(null);
		}
	}

	private void updateActions(ContextNode context) {
		boolean enabled = false;
		if (context != null) {
			enabled = context.getLibraryNode().isEditable();
		}
		addContextAction.setEnabled(enabled);
		cloneContextAction.setEnabled(enabled);
		mergeContextAction.setEnabled(enabled);
		setContextAsDefaultAction.setEnabled(enabled);
	}

	private void updateFields(ContextNode context) {
		if (context != null) {
			appCtxText.setData(context);
			ctxIdText.setData(context);
			ctxDescriptionText.setData(context);
			appCtxText.setText(context.getApplicationContext());
			ctxIdText.setText(context.getContextId());
			ctxDescriptionText.setText(context.getDescription());
			retrieveDefaultContext(context);
		} else {
			appCtxText.setData(null);
			ctxIdText.setData(null);
			ctxDescriptionText.setData(null);
			appCtxText.setText("");
			ctxIdText.setText("");
			ctxDescriptionText.setText("");
		}
		boolean enabled = false;
		if (context != null && context.getLibraryNode().isEditable())
			enabled = true;
		appCtxText.setEnabled(enabled);
		ctxIdText.setEnabled(enabled);
		ctxDescriptionText.setEnabled(enabled);
	}

	/**
	 * ???
	 * 
	 * @param context
	 */
	private void retrieveDefaultContext(ContextNode context) {
		String contextId = controller.getDefaultContextId();
		for (ContextNode ctx : context.getOwningLibraryRoot().getChildren()) {
			if (ctx.getContextId().equals(contextId)) {
				setDefaultContextNode(ctx);
			}
		}
	}

	public void modifyApplicationContext() {
		Object data = appCtxText.getData();
		if (data instanceof ContextNode) {
			ContextNode context = (ContextNode) data;
			context.setApplicationContext(appCtxText.getText());
			if (viewerIsOk())
				viewer.refresh(context, true);
		}
	}

	public void modifyContextId() {
		Object data = ctxIdText.getData();
		if (data instanceof ContextNode) {
			ContextNode context = (ContextNode) data;
			context.setContextId(ctxIdText.getText());
			if (viewerIsOk())
				viewer.refresh(context, true);
		}
	}

	public void modifyDescription() {
		Object data = ctxDescriptionText.getData();
		if (data instanceof ContextNode) {
			ContextNode context = (ContextNode) data;
			context.setDescription(ctxDescriptionText.getText());
			if (viewerIsOk())
				viewer.refresh(context, true);
		}
	}

	public ContextNode getSelectedContextNode() {
		if (!viewerIsOk())
			return null; // In case the view is not activated.
		StructuredSelection selection = (StructuredSelection) viewer.getSelection();
		Object object = selection.getFirstElement();
		if (object != null && object instanceof ContextNode) {
			return (ContextNode) object;
		}
		// LOGGER.debug("getSelectedContextNode is returning null.\t" + this.getClass());
		return null;
	}

	public List<ContextNode> getSelectedContextNodes() {
		if (!viewerIsOk())
			return null; // In case the view is not activated.
		StructuredSelection selection = (StructuredSelection) viewer.getSelection();
		List<ContextNode> nodes = new ArrayList<>();
		for (Object object : selection.toArray()) {
			if (object != null && object instanceof ContextNode) {
				nodes.add((ContextNode) object);
			}
		}
		return nodes;
	}

	/**
	 * Set the default context node to the passed node.
	 * 
	 * @param selected
	 */
	public void setDefaultContextNode(ContextNode selected) {
		if (viewerIsOk())
			viewer.refresh(true);
	}

	/**
	 * @return
	 */
	public ContextController getContextController() {
		return controller;
	}

	public void setSelectedTypeRoot(ContextNode node) {
		this.currentTypeRoot = node;
	}

	public ContextNode getSelectedLibraryRoot() {
		return currentTypeRoot;
	}

	/**
	 * @return the text in the application context field.
	 */
	public String getApplicationText() {
		return appCtxText.getText();
	}

	/**
	 * @return the text in the context name (id) field.
	 */
	public String getContextIdText() {
		return ctxIdText.getText();
	}

	/**
	 * @return the text in the context description field.
	 */
	public String getDescriptionText() {
		return ctxDescriptionText.getText();
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
		// if (viewer.getControl().isDisposed())
		// return;
		if (viewerIsOk())
			viewer.refresh(true);
		updateFields(getSelectedContextNode());

	}

	@Override
	public void refresh(INode node) {
		if (viewerIsOk())
			viewer.refresh(true);
		postContexts(true);
		// TODO - select the context node that matches the passed node
	}
}
