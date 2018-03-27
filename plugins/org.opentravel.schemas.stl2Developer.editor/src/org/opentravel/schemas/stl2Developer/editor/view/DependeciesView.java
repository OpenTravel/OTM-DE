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
package org.opentravel.schemas.stl2Developer.editor.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditDomain;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.editparts.LayerManager;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.ui.actions.AlignmentAction;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.gef.ui.parts.AbstractEditPartViewer;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IFilter;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.ImageTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.ViewPart;
import org.opentravel.schemas.controllers.MainController.IRefreshListener;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.stl2Developer.editor.i18n.Messages;
import org.opentravel.schemas.stl2Developer.editor.internal.Activator;
import org.opentravel.schemas.stl2Developer.editor.internal.DropListener;
import org.opentravel.schemas.stl2Developer.editor.internal.GEFUtils;
import org.opentravel.schemas.stl2Developer.editor.internal.actions.AddUsedTypesAction;
import org.opentravel.schemas.stl2Developer.editor.internal.actions.ClearAllNodesAction;
import org.opentravel.schemas.stl2Developer.editor.internal.actions.ClearUnlinkedNodesAction;
import org.opentravel.schemas.stl2Developer.editor.internal.actions.DeleteAction;
import org.opentravel.schemas.stl2Developer.editor.internal.actions.DropDownAction;
import org.opentravel.schemas.stl2Developer.editor.internal.actions.EnableFilterAction;
import org.opentravel.schemas.stl2Developer.editor.internal.actions.LinkWithNavigator;
import org.opentravel.schemas.stl2Developer.editor.internal.actions.ShowHideNodeAction;
import org.opentravel.schemas.stl2Developer.editor.internal.actions.ToggleLayout;
import org.opentravel.schemas.stl2Developer.editor.internal.actions.ToggleShowSimpleObjects;
import org.opentravel.schemas.stl2Developer.editor.internal.actions.WhereUsedActionGef;
import org.opentravel.schemas.stl2Developer.editor.internal.filters.EmptyFacetFilter;
import org.opentravel.schemas.stl2Developer.editor.internal.filters.FilterManager;
import org.opentravel.schemas.stl2Developer.editor.model.Diagram;
import org.opentravel.schemas.stl2Developer.editor.model.UINode;
import org.opentravel.schemas.stl2Developer.editor.parts.DiagramEditPart;
import org.opentravel.schemas.stl2Developer.editor.parts.EditPartsFactory;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.utils.RCPUtils;

/**
 * @author Pawel Jedruch
 * 
 */
public class DependeciesView extends ViewPart implements IRefreshListener {

	private ScrollingGraphicalViewer viewer;
	private EditDomain domain;

	private Map<ISelectionListener, String> selectionListeners = new HashMap<ISelectionListener, String>();
	private List<SelectionAction> selectionActions = new ArrayList<SelectionAction>();

	public DependeciesView() {
		domain = new EditDomain();
		OtmRegistry.getMainController().addRefreshListener(this);
	}

	@Override
	public void createPartControl(Composite parent) {
		viewer = new ScrollingGraphicalViewer();
		viewer.createControl(parent);
		FilterManager filterManger = initFilterManager();
		viewer.setProperty(FilterManager.class.toString(), filterManger);

		domain.addViewer(viewer);
		viewer.getControl().setBackground(ColorConstants.listBackground);
		viewer.setRootEditPart(new ScalableFreeformRootEditPart());
		viewer.setEditPartFactory(new EditPartsFactory());
		getSite().setSelectionProvider(viewer);

		Diagram diag = new Diagram();
		viewer.setContents(diag);
		getGraphicalViewer().addDropTargetListener(new DropListener(getGraphicalViewer(), diag));

		addSelectionListener(new SelectionListener());
		hookContextMenu();
		hookToolbarActions();
		parent.addControlListener(new ControlAdapter() {

			@Override
			public void controlResized(ControlEvent e) {
				refresh();
			}
		});
	}

	public Diagram getDiagram() {
		return (Diagram) viewer.getRootEditPart().getContents().getModel();
	}

	private FilterManager initFilterManager() {
		FilterManager fm = new FilterManager();
		return fm;
	}

	public void addNodeFilter(IFilter filter) {
		getFilterManager().addFilter(filter);
		refresh();
	}

	public void removeNodeFilter(IFilter filter) {
		getFilterManager().removeFilter(filter);
		refresh();
	}

	private FilterManager getFilterManager() {
		return (FilterManager) viewer.getProperty(FilterManager.class.toString());
	}

	public AbstractEditPartViewer getGraphicalViewer() {
		return viewer;
	}

	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	@Override
	public void dispose() {
		domain.setActiveTool(null);
		OtmRegistry.getMainController().removeRefreshListener(this);
		disposeListeners();
		super.dispose();
	}

	private void disposeListeners() {
		for (ISelectionListener l : new HashSet<ISelectionListener>(selectionListeners.keySet())) {
			removeSelectionListener(l);
		}
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		fillContextMenu(menuMgr);

		menuMgr.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				manager.update(null);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);

		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void hookToolbarActions() {
		IAction screenShot = new Action(Messages.DependeciesView_ActionSaveToClipboard) {

			@Override
			public void run() {
				DependeciesView.this.saveToClipboard();
			}

		};
		screenShot.setImageDescriptor(Activator.getImageDescriptor("icons/screenshot.gif")); //$NON-NLS-1$
		getViewSite().getActionBars().getToolBarManager().add(screenShot);

		ToggleLayout disableLayout = new ToggleLayout(this);
		disableLayout.setImageDescriptor(Activator.getImageDescriptor("icons/layout-16.png")); //$NON-NLS-1$
		getViewSite().getActionBars().getToolBarManager().add(disableLayout);

		ToggleShowSimpleObjects toogleSimpleObjects = new ToggleShowSimpleObjects(this);
		getViewSite().getActionBars().getToolBarManager().add(toogleSimpleObjects);

		LinkWithNavigator linkWithNavigator = new LinkWithNavigator(this);
		linkWithNavigator.setImageDescriptor(Images.getImageRegistry().getDescriptor(Images.LinkedWithNavigator));
		getViewSite().getActionBars().getToolBarManager().add(linkWithNavigator);

		final EnableFilterAction emptyFacet = new EnableFilterAction(Messages.DependeciesView_EmptyFacetFilter,
				new EmptyFacetFilter(), DependeciesView.this);
		DropDownAction filtersDropDown = new DropDownAction(Messages.DependeciesView_FiltersMenu);
		filtersDropDown.addAction(emptyFacet);
		filtersDropDown.setImageDescriptor(Images.getImageRegistry().getDescriptor(Images.Filter));
		getViewSite().getActionBars().getToolBarManager().add(filtersDropDown);
	}

	private void registerAction(Action action) {
		getViewSite().getActionBars().setGlobalActionHandler(action.getActionDefinitionId(), action);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(new Separator());
		ShowHideNodeAction showWhereUsed = new WhereUsedActionGef(getGraphicalViewer(),
				Messages.DependenciesView_ActionWhereUsed);
		manager.add(showWhereUsed);
		ShowHideNodeAction showTypes = new AddUsedTypesAction(getGraphicalViewer(),
				Messages.DependeciesView_ActionUsedTypes);
		manager.add(showTypes);

		manager.add(RCPUtils.createCommandContributionItem(getSite(), ActionFactory.DELETE.getCommandId(),
				Messages.DependenciesView_ActionRemove, null, null));
		ClearAllNodesAction clearAll = new ClearAllNodesAction(getGraphicalViewer(),
				Messages.DependeciesView_ActionClearAll);
		manager.add(clearAll);
		ClearUnlinkedNodesAction clearUnlinked = new ClearUnlinkedNodesAction(getGraphicalViewer(),
				Messages.DependeciesView_ActionClearUnlinked);
		manager.add(clearUnlinked);

		MenuManager align = new MenuManager(Messages.DependeciesView_MenuAlignment);
		manager.add(align);
		align.add(createAlignmentAction(PositionConstants.LEFT));
		align.add(createAlignmentAction(PositionConstants.CENTER));
		align.add(createAlignmentAction(PositionConstants.RIGHT));
		align.add(new Separator());
		align.add(createAlignmentAction(PositionConstants.TOP));
		align.add(createAlignmentAction(PositionConstants.MIDDLE));
		align.add(createAlignmentAction(PositionConstants.BOTTOM));

		Action delete = new DeleteAction(getGraphicalViewer());
		delete.setActionDefinitionId(ActionFactory.DELETE.getCommandId());
		registerAction(delete);
	}

	private IAction createAlignmentAction(int position) {
		AlignmentAction action = new AlignmentAction(this, position);
		action.setSelectionProvider(this.getGraphicalViewer());
		selectionActions.add(action);
		return action;
	}

	@Override
	public void refresh(INode node) {
		UINode uiNode = getContentEditPart().getModel().findUINode((Node) node);
		GraphicalEditPart ep = (GraphicalEditPart) viewer.getEditPartRegistry().get(uiNode);
		if (ep != null) {
			ep.refresh();
		}
	}

	@Override
	public void refresh() {
		getContentEditPart().refresh();
	}

	private DiagramEditPart getContentEditPart() {
		return (DiagramEditPart) viewer.getRootEditPart().getContents();
	}

	public void saveToClipboard() {
		GraphicalViewer graphicalViewer = viewer;
		ScalableFreeformRootEditPart rootEditPart = (ScalableFreeformRootEditPart) graphicalViewer
				.getEditPartRegistry().get(LayerManager.ID);
		IFigure rootFigure = ((LayerManager) rootEditPart).getLayer(LayerConstants.PRINTABLE_LAYERS);
		Rectangle rootFigureBounds = rootFigure.getBounds();
		Control figureCanvas = graphicalViewer.getControl();

		Image img = new Image(Display.getDefault(), rootFigureBounds.width, rootFigureBounds.height);
		GC imageGC = new GC(img);
		figureCanvas.print(imageGC);

		Clipboard cb = new Clipboard(Display.getDefault());
		ImageTransfer textTransfer = ImageTransfer.getInstance();
		cb.setContents(new Object[] { img.getImageData() }, new Transfer[] { textTransfer });

		imageGC.dispose();
		img.dispose();
	}

	public void selectNode(Node n) {
		// 3/27 - is null sometimes
		// UINode uin = getDiagram().findUINode(n);
		EditPart ep = (EditPart) viewer.getEditPartRegistry().get(getDiagram().findUINode(n));
		if (ep != null) {
			EditPart toSelect = GEFUtils.getEditPartToSelect(ep);
			viewer.setSelection(new StructuredSelection(toSelect));
			// 3/27 - not the problem
			// if (getDiagram().findUINode(n) == null)
			// System.out.println("Node no longer registered.");
		}
	}

	public void addSelectionListener(ISelectionListener listener) {
		addSelectionListener(null, listener);
	}

	public void addSelectionListener(String partId, ISelectionListener listener) {
		if (!selectionListeners.containsKey(listener)) {
			selectionListeners.put(listener, partId);
			if (partId == null) {
				getSite().getPage().addSelectionListener(listener);
			} else {
				getSite().getPage().addSelectionListener(partId, listener);
			}
		}
	}

	public void removeSelectionListener(ISelectionListener listener) {
		if (selectionListeners.containsKey(listener)) {
			String partId = selectionListeners.get(listener);
			if (partId == null) {
				getSite().getPage().removeSelectionListener(listener);
			} else {
				getSite().getPage().removeSelectionListener(partId, listener);
			}
			selectionListeners.remove(listener);
		}
	}

	class SelectionListener implements ISelectionListener {

		@Override
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			for (SelectionAction s : DependeciesView.this.getSelectionActions()) {
				s.update();
			}
		}
	}

	public List<SelectionAction> getSelectionActions() {
		return selectionActions;
	}

	@Override
	public Object getAdapter(Class type) {
		if (type == CommandStack.class)
			return viewer.getEditDomain().getCommandStack();
		return super.getAdapter(type);
	}

}
