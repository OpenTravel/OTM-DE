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
package org.opentravel.schemas.node.resources;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DecoratingStyledCellLabelProvider;
import org.eclipse.jface.viewers.IDecorationContext;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;
import org.opentravel.schemas.commands.ResourceCommandHandler;
import org.opentravel.schemas.commands.ResourceCommandHandlerPopup;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.trees.REST.RestTreeComparator;
import org.opentravel.schemas.trees.REST.RestTreeContentProvider;
import org.opentravel.schemas.trees.REST.RestTreeStyledLabelProvider;
import org.opentravel.schemas.utils.RCPUtils;

/**
 * Extend the resource treeViewer with menus and refresh behavior. Define menu managers and sets of menus. Define and
 * instantiate actions. Define members of the menus. Implement the menu listener
 * 
 * @author Dave Hollander
 * 
 */
public class ResourceMenus {

	private MenuManager menuManager;
	private TreeViewer viewer;

	private IContributionItem newActionResponse;

	class SupportTreeFilterProvider extends DecoratingStyledCellLabelProvider implements ILabelProvider {

		public SupportTreeFilterProvider(IStyledLabelProvider labelProvider, ILabelDecorator decorator,
				IDecorationContext decorationContext) {
			super(labelProvider, decorator, decorationContext);
		}

		@Override
		public String getText(Object element) {
			return getStyledText(element).getString();
		}

	}

	public ResourceMenus(Composite parent, IWorkbenchPartSite site) {
		this(parent);
		newActionResponse = RCPUtils.createCommandContributionItem(site, ResourceCommandHandler.COMMAND_ID + "."
				+ ResourceCommandHandler.CommandType.ACTIONRESPONSE, "New Action Response", null,
				ResourceCommandHandler.getIcon());

		// newActionResponse = RCPUtils.createCommandContributionItem(site, addResponseCommandId, "New Action Response",
		// null, ResourceCommandHandler.getIcon());

		IHandlerService handlerService = (IHandlerService) site.getService(IHandlerService.class);
		String addResponseCommandId = ResourceCommandHandler.COMMAND_ID + "."
				+ ResourceCommandHandler.CommandType.ACTIONRESPONSE;

		handlerService.activateHandler(addResponseCommandId, new ResourceCommandHandlerPopup());
		site.registerContextMenu(menuManager, viewer);
	}

	public ResourceMenus(final Composite parent) {
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		viewer.setContentProvider(new RestTreeContentProvider());
		viewer.setComparator(new RestTreeComparator());

		IWorkbench workbench = PlatformUI.getWorkbench();

		// decoration
		DecoratingStyledCellLabelProvider decorator = new SupportTreeFilterProvider(new RestTreeStyledLabelProvider(),
				workbench.getDecoratorManager(), null);
		viewer.setLabelProvider(decorator);

		menuManager = new MenuManager();
		final Menu menu = menuManager.createContextMenu(viewer.getControl());

		menuManager.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(final IMenuManager manager) {
				if (newActionResponse != null && getSelectedNode() instanceof ActionNode) {
					manager.removeAll();
					manager.add(new Separator());
					manager.add(newActionResponse);
				}
				manager.updateAll(true);
			}
		});
		menuManager.setRemoveAllWhenShown(true);
		viewer.getControl().setMenu(menu);
	}

	public Node getSelectedNode() {
		Node n = null;
		if (viewer.getSelection() instanceof IStructuredSelection) {
			final IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
			final Object selected = selection.getFirstElement();
			if (selected instanceof Node)
				n = (Node) selected;
		}
		return n;
	}

	public void selectNode(INode n) {
		if (n == null) {
			viewer.setSelection(null);
		} else {
			if (viewer.getSelection() != n)
				viewer.setSelection(new StructuredSelection(n), true);
		}
	}

	public TreeViewer getViewer() {
		return viewer;
	}

}
