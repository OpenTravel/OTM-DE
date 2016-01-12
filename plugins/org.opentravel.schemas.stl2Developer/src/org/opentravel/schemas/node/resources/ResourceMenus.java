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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
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
import org.opentravel.schemas.actions.AddToProjectAction;
import org.opentravel.schemas.commands.ResourceCommandHandler;
import org.opentravel.schemas.controllers.ProjectController;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.properties.DefaultStringProperties;
import org.opentravel.schemas.properties.Messages;
import org.opentravel.schemas.properties.PropertyType;
import org.opentravel.schemas.properties.StringProperties;
import org.opentravel.schemas.stl2developer.OtmRegistry;
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

	IContributionItem newAction;
	private IContributionItem newActionNodeAction;
	private IContributionItem newActionFacetAction;
	private IContributionItem newParamGroupAction;
	private IContributionItem newActionResponse;
	private IContributionItem deleteAction;

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

	public ResourceMenus(final Composite parent) {
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		viewer.setContentProvider(new RestTreeContentProvider());
		// viewer.setLabelProvider(new RestTreeLabelProvider());
		viewer.setComparator(new RestTreeComparator());

		IWorkbench workbench = PlatformUI.getWorkbench();

		// decoration
		//
		DecoratingStyledCellLabelProvider decorator = new SupportTreeFilterProvider(new RestTreeStyledLabelProvider(),
				workbench.getDecoratorManager(), null);
		viewer.setLabelProvider(decorator);

		menuManager = new MenuManager();
		final Menu menu = menuManager.createContextMenu(viewer.getControl());
		final MenuManager addToProjectMenu = new MenuManager("Add to Project", "Repository_AddToProject_Menu_ID");

		menuManager.addMenuListener(new IMenuListener() {

			@Override
			public void menuAboutToShow(final IMenuManager manager) {

				addToProjectMenu.removeAll();

				manager.add(newAction);
				manager.add(newActionNodeAction);
				manager.add(newActionFacetAction);
				manager.add(newParamGroupAction);
				if (getSelectedNode() instanceof ActionNode) {
					manager.add(new Separator());
					manager.add(newActionResponse);
				}

				manager.add(new Separator());
				manager.add(deleteAction);

				//
				// final Node node = (Node) selected;
				//
				// if (node instanceof RepositoryItemNode) {
				// final List<Action> importActions = createAddActionsForItems(node);
				// for (final Action libAction : importActions) {
				// addToProjectMenu.add(libAction);
				// }
				// manager.add(addToProjectMenu);
				// // Need to link repository items to project items before these will work
				// // manager.add(new Separator());
				// // manager.add(commitLibraryAction);
				// // manager.add(lockLibraryAction);
				// // manager.add(revertLibraryAction);
				// // manager.add(unlockLibraryAction);
				//
				// }
				manager.updateAll(true);
				// }
			}

			private List<Action> createAddActionsForItems(final Node context) {
				final List<Action> itemActions = new ArrayList<Action>();
				ProjectController pc = OtmRegistry.getMainController().getProjectController();
				for (ProjectNode pn : pc.getAll()) {
					if (pn.isBuiltIn())
						continue;
					final StringProperties sp = new DefaultStringProperties();
					sp.set(PropertyType.TEXT, pn.getName());
					itemActions.add(new AddToProjectAction(sp, pn));
				}
				return itemActions;
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

	public ResourceMenus(Composite parent, IWorkbenchPartSite site) {
		this(parent);
		InitActions(site);
		site.registerContextMenu(menuManager, viewer);
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

	public void InitActions(IWorkbenchPartSite site) {
		// command id MUST be defined in plugin.xml
		newAction = RCPUtils.createCommandContributionItem(site, ResourceCommandHandler.COMMAND_ID,
				Messages.getString("rest.action.new.text"), null, ResourceCommandHandler.getIcon());
		newActionNodeAction = RCPUtils.createCommandContributionItem(site, ResourceCommandHandler.COMMAND_ID + "."
				+ ResourceCommandHandler.CommandType.ACTION, Messages.getString("rest.action.new.actionNode.text"), null,
				ResourceCommandHandler.getIcon());
		newActionFacetAction = RCPUtils.createCommandContributionItem(site, ResourceCommandHandler.COMMAND_ID + "."
				+ ResourceCommandHandler.CommandType.ACTIONFACET, Messages.getString("rest.action.new.actionFacet.text"),
				null, ResourceCommandHandler.getIcon());
		newParamGroupAction = RCPUtils.createCommandContributionItem(site, ResourceCommandHandler.COMMAND_ID + "."
				+ ResourceCommandHandler.CommandType.PARAMGROUP, Messages.getString("rest.action.new.paramGroup.text"),
				null, ResourceCommandHandler.getIcon());
		newActionResponse = RCPUtils.createCommandContributionItem(site, ResourceCommandHandler.COMMAND_ID + "."
				+ ResourceCommandHandler.CommandType.ACTIONRESPONSE,
				Messages.getString("rest.action.new.actionResponse.text"), null, ResourceCommandHandler.getIcon());
		deleteAction = RCPUtils.createCommandContributionItem(site, ResourceCommandHandler.COMMAND_ID + "."
				+ ResourceCommandHandler.CommandType.DELETE, Messages.getString("rest.action.delete.text"), null, null);
	}
}
