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
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ContributionManager;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener2;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DecoratingStyledCellLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ArmEvent;
import org.eclipse.swt.events.ArmListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPartSite;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemas.actions.AddAliasAction;
import org.opentravel.schemas.actions.AddCRUDQOperationsAction;
import org.opentravel.schemas.actions.AddChoiceFacetAction;
import org.opentravel.schemas.actions.AddCustomFacetAction;
import org.opentravel.schemas.actions.AddEnumValueAction;
import org.opentravel.schemas.actions.AddOperationAction;
import org.opentravel.schemas.actions.AddQueryFacetAction;
import org.opentravel.schemas.actions.AddRoleAction;
import org.opentravel.schemas.actions.AssignTypeAction;
import org.opentravel.schemas.actions.ChangeAction;
import org.opentravel.schemas.actions.CommitLibraryAction;
import org.opentravel.schemas.actions.CopyNodeAction;
import org.opentravel.schemas.actions.ImportObjectToLibraryAction;
import org.opentravel.schemas.actions.LifeCycleAction;
import org.opentravel.schemas.actions.LockLibraryAction;
import org.opentravel.schemas.actions.ManageInRepositoryAction;
import org.opentravel.schemas.actions.MoveObjectToLibraryAction;
import org.opentravel.schemas.actions.NewLibraryAction;
import org.opentravel.schemas.actions.NewProjectAction;
import org.opentravel.schemas.actions.OpenLibraryAction;
import org.opentravel.schemas.actions.UnlockLibraryAction;
import org.opentravel.schemas.actions.VersionAction;
import org.opentravel.schemas.actions.VersionAction.VersionType;
import org.opentravel.schemas.commands.AddNodeHandler2;
import org.opentravel.schemas.commands.ChangeTypeProviderLibraryHandler;
import org.opentravel.schemas.commands.CloseLibrariesHandler;
import org.opentravel.schemas.commands.CloseProjectHandler;
import org.opentravel.schemas.commands.CompileHandler;
import org.opentravel.schemas.commands.DeleteNodesHandler;
import org.opentravel.schemas.commands.NewComponentHandler;
import org.opentravel.schemas.commands.OpenProjectHandler;
import org.opentravel.schemas.commands.ResourceCommandHandler;
import org.opentravel.schemas.commands.SaveLibrariesHandler;
import org.opentravel.schemas.commands.SaveLibraryHandler;
import org.opentravel.schemas.commands.ValidateHandler;
import org.opentravel.schemas.commands.VersionUpdateHandler;
import org.opentravel.schemas.controllers.RepositoryController;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.NavNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.node.ServiceNode;
import org.opentravel.schemas.node.VersionNode;
import org.opentravel.schemas.node.interfaces.Enumeration;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNavNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.objectMembers.ContributedFacetNode;
import org.opentravel.schemas.node.objectMembers.ExtensionPointNode;
import org.opentravel.schemas.node.objectMembers.FacetOMNode;
import org.opentravel.schemas.node.objectMembers.OperationNode;
import org.opentravel.schemas.node.properties.EnumLiteralNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.properties.RoleNode;
import org.opentravel.schemas.node.properties.SimpleAttributeFacadeNode;
import org.opentravel.schemas.node.resources.ResourceNode;
import org.opentravel.schemas.node.typeProviders.AliasNode;
import org.opentravel.schemas.node.typeProviders.ChoiceObjectNode;
import org.opentravel.schemas.node.typeProviders.ContextualFacetNode;
import org.opentravel.schemas.node.typeProviders.SimpleTypeNode;
import org.opentravel.schemas.node.typeProviders.VWA_Node;
import org.opentravel.schemas.node.typeProviders.facetOwners.BusinessObjectNode;
import org.opentravel.schemas.node.typeProviders.facetOwners.CoreObjectNode;
import org.opentravel.schemas.properties.DefaultStringProperties;
import org.opentravel.schemas.properties.ExternalizedStringProperties;
import org.opentravel.schemas.properties.Messages;
import org.opentravel.schemas.properties.PropertyType;
import org.opentravel.schemas.properties.StringProperties;
import org.opentravel.schemas.trees.library.LibrarySorter;
import org.opentravel.schemas.trees.library.LibraryTreeContentProvider;
import org.opentravel.schemas.trees.library.LibraryTreeLabelProvider;
import org.opentravel.schemas.trees.repository.RepositoryNode;
import org.opentravel.schemas.types.whereused.LibraryProviderNode;
import org.opentravel.schemas.types.whereused.TypeProviderWhereUsedNode;
import org.opentravel.schemas.utils.RCPUtils;
import org.opentravel.schemas.views.decoration.LibraryDecorator;

/**
 * Extend the treeViewer with menus and refresh behavior. Define menu managers and sets of menus. Define and instantiate
 * actions. Define members of the menus. Implement the menu listener
 * 
 * {@link LibraryTreeContentProvider} <br>
 * {@link LibraryTreeLabelProvider} <br>
 * {@link LibraryDecorator}
 * 
 * @author Dave Hollander
 * 
 */
public class NavigatorMenus extends TreeViewer {

	// private static final Logger LOGGER = LoggerFactory.getLogger(NavigatorMenus.class);
	MainWindow mainWindow = null;

	public NavigatorMenus(final Composite parent, final IWorkbenchPartSite site) {
		super(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		mainWindow = OtmRegistry.getMainWindow();

		// Set up the content and label providers
		//
		setContentProvider(new LibraryTreeContentProvider());
		IWorkbench workbench = site.getWorkbenchWindow().getWorkbench();
		DecoratingStyledCellLabelProvider decorator = new DecoratingStyledCellLabelProvider(
				new LibraryTreeLabelProvider(), workbench.getDecoratorManager(), null);
		setLabelProvider(decorator);
		setSorter(new LibrarySorter());

		// Define the menus
		//
		final MenuManager componentMenu = new MenuManager("Object", "Component_Menu_ID");
		final MenuManager facetMenu = new MenuManager("Object", "Facet_Menu_ID");
		final MenuManager propertyMenu = new MenuManager("Object", "Property_Menu_ID");
		final MenuManager navigationMenu = new MenuManager("Object", "Navication_Menu_ID");
		final MenuManager whereUsedMenu = new MenuManager("Object", "WhereUsed_ID");

		final MenuManager enumObjectMenu = new MenuManager("Object", "Enum_Object_ID");
		final MenuManager operationObjectMenu = new MenuManager("Object", "Operation_Object_ID");
		final MenuManager roleObjectMenu = new MenuManager("Object", "Role_ID");

		final MenuManager basicObjectMenu = new MenuManager("Object", "Basic_Object_ID");
		final MenuManager deleteMoveMenu = new MenuManager("Object", "Basic_With_CopyDeleteMove_ID");

		final MenuManager xpFacetObjectMenu = new MenuManager("Object", "ExtPointFacet_Object_ID");
		final MenuManager simpleObjectMenu = new MenuManager("Object", "Simple_Object_ID");
		final MenuManager serviceObjectMenu = new MenuManager("Object", "Service_Object_ID");

		final MenuManager libraryChainMenu = new MenuManager("LibraryChain", "LibraryChain_ID");

		final MenuManager libraryMenu = new MenuManager("Library", "Library_ID");
		final MenuManager projectMenu = new MenuManager("Project", "Project_ID");
		final MenuManager manageInMenu = new MenuManager("Manage in...", "Project_ID");
		final MenuManager versionUpdateMenu = new MenuManager("Update Type Users to new version", "VersionUpdate_ID");

		final MenuManager copyMenu = new DisableIfEmptyMenu(Messages.getString("action.menu.navigation.copy"),
				"Copy_ID", Messages.getString("action.menu.navigation.copy.tooltip"));
		final MenuManager moveMenu = new DisableIfEmptyMenu(Messages.getString("action.menu.navigation.move"),
				"Move_ID", Messages.getString("action.menu.navigation.move.tooltip"));
		final MenuManager versionMenu = new MenuManager("Version...", "VersionMenuID");

		// Define the Actions
		final Action newLibraryAction = new NewLibraryAction(mainWindow,
				new ExternalizedStringProperties("action.new"));
		final Action openLibraryAction = new OpenLibraryAction();
		final Action commitLibraryAction = new CommitLibraryAction();
		// final Action finalizeLibraryAction = new FinalizeLibraryAction();
		final Action lockLibraryAction = new LockLibraryAction();
		final Action unlockLibraryAction = new UnlockLibraryAction();

		final Action lifeCycle_finalize = new LifeCycleAction(TLLibraryStatus.FINAL);
		final Action lifeCycle_review = new LifeCycleAction(TLLibraryStatus.UNDER_REVIEW);
		final Action lifeCycle_obsolete = new LifeCycleAction(TLLibraryStatus.OBSOLETE);

		final Action addCrudqOperationsAction = new AddCRUDQOperationsAction(mainWindow,
				new ExternalizedStringProperties("action.addCRUDQOperations"));
		final Action cloneObjectAction = new CopyNodeAction(mainWindow,
				new ExternalizedStringProperties("action.cloneObject"));
		// This is the go-to approach - let the actions have default properties
		final Action changeObjectAction = new ChangeAction(mainWindow);
		final Action addAliasAction = new AddAliasAction(mainWindow);
		final Action addCustomFacetAction = new AddCustomFacetAction();
		final Action addChoiceFacetAction = new AddChoiceFacetAction();
		final Action addEnumValueAction = new AddEnumValueAction(mainWindow);
		final Action addOperationAction = new AddOperationAction();
		final Action addQueryFacetAction = new AddQueryFacetAction(mainWindow);
		final Action addRoleAction = new AddRoleAction(mainWindow);
		final Action replaceAction = new AssignTypeAction(mainWindow);

		final IContributionItem validateCommand = RCPUtils.createCommandContributionItem(site,
				ValidateHandler.COMMAND_ID, null, null, ValidateHandler.getIcon());

		// Contribution Items - used for command handlers
		final IContributionItem closeProjectCommand = RCPUtils.createCommandContributionItem(site,
				CloseProjectHandler.COMMAND_ID, null, null, null);
		final IContributionItem openProjectCommand = RCPUtils.createCommandContributionItem(site,
				OpenProjectHandler.COMMAND_ID, null, null, null);
		final Action newProjectAction = new NewProjectAction();
		final IContributionItem compileCommand = RCPUtils.createCommandContributionItem(site, CompileHandler.COMMAND_ID,
				null, null, null);

		final IContributionItem versionUpdateCommand = RCPUtils.createCommandContributionItem(site,
				VersionUpdateHandler.COMMAND_ID, null, null, null);
		final IContributionItem changeProviderLibraryCommand = RCPUtils.createCommandContributionItem(site,
				ChangeTypeProviderLibraryHandler.COMMAND_ID, null, null, null);

		final IContributionItem saveAllLibrariesCommand = RCPUtils.createCommandContributionItem(site,
				SaveLibrariesHandler.COMMAND_ID, null, null, SaveLibrariesHandler.getIcon());
		final IContributionItem saveSelectedLibrariesCommand = RCPUtils.createCommandContributionItem(site,
				SaveLibraryHandler.COMMAND_ID, null, null, SaveLibraryHandler.getIcon());
		final IContributionItem closeLibrariesCommand = RCPUtils.createCommandContributionItem(site,
				CloseLibrariesHandler.COMMAND_ID, null, null, null);

		final IContributionItem newComplexCommand = RCPUtils.createCommandContributionItem(site,
				NewComponentHandler.COMMAND_ID, Messages.getString("action.newComplex.text"), null, null);
		final IContributionItem addPropertiesCommand = RCPUtils.createCommandContributionItem(site,
				AddNodeHandler2.COMMAND_ID, Messages.getString("action.addProperty.text"), null,
				AddNodeHandler2.getIcon());

		final IContributionItem deleteObjectCommand = RCPUtils.createCommandContributionItem(site,
				DeleteNodesHandler.COMMAND_ID, null, null, null);

		// Expose Business Object as Resource Service action
		final IContributionItem newResourceCommand = RCPUtils.createCommandContributionItem(site,
				ResourceCommandHandler.COMMAND_ID, "New Resource", null, ResourceCommandHandler.getIcon());

		// Site registered top menu
		//
		final MenuManager menuManager = new MenuManager();
		final Menu menu = menuManager.createContextMenu(this.getControl());
		site.registerContextMenu(menuManager, this);

		menuManager.addMenuListener(new IMenuListener2() {

			private void createMenu() {
				addToMenu(libraryChainMenu, closeLibrariesCommand);

				libraryMenu.removeAll();
				libraryMenu.add(newLibraryAction);
				libraryMenu.add(openLibraryAction);
				libraryMenu.add(validateCommand);
				libraryMenu.add(new Separator());
				libraryMenu.add(manageInMenu);
				libraryMenu.add(versionMenu);
				libraryMenu.add(commitLibraryAction);
				libraryMenu.add(lockLibraryAction);
				libraryMenu.add(unlockLibraryAction);
				// libraryMenu.add(finalizeLibraryAction);
				libraryMenu.add(lifeCycle_review);
				libraryMenu.add(lifeCycle_finalize);
				libraryMenu.add(lifeCycle_obsolete);
				libraryMenu.add(new Separator());
				libraryMenu.add(saveSelectedLibrariesCommand);
				libraryMenu.add(saveAllLibrariesCommand);
				libraryMenu.add(new Separator());
				libraryMenu.add(closeLibrariesCommand);

				versionUpdateMenu.add(versionUpdateCommand);
				versionUpdateMenu.add(changeProviderLibraryCommand);

				projectMenu.removeAll();
				projectMenu.add(closeProjectCommand);
				projectMenu.add(newProjectAction);
				projectMenu.add(openProjectCommand);
				projectMenu.add(compileCommand);

				whereUsedMenu.removeAll();
				whereUsedMenu.add(replaceAction);

				addToMenu(navigationMenu, newComplexCommand, newResourceCommand);

				componentMenu.removeAll();
				componentMenu.add(addAliasAction);
				componentMenu.add(addPropertiesCommand);

				componentMenu.add(addPropertiesCommand);
				componentMenu.add(new Separator());
				componentMenu.add(addCustomFacetAction);
				componentMenu.add(addQueryFacetAction);
				componentMenu.add(addChoiceFacetAction);
				componentMenu.add(addRoleAction);
				componentMenu.add(addEnumValueAction);
				componentMenu.add(new Separator());
				componentMenu.add(changeObjectAction);
				componentMenu.add(cloneObjectAction);
				componentMenu.add(deleteObjectCommand);
				componentMenu.add(copyMenu);
				componentMenu.add(moveMenu);
				componentMenu.add(validateCommand);
				componentMenu.add(new Separator());
				componentMenu.add(newComplexCommand);
				componentMenu.add(newResourceCommand);

				facetMenu.removeAll();
				facetMenu.add(addPropertiesCommand);
				facetMenu.add(addRoleAction);
				facetMenu.add(addEnumValueAction);
				facetMenu.add(new Separator());
				facetMenu.add(changeObjectAction);
				facetMenu.add(deleteObjectCommand);
				facetMenu.add(addCustomFacetAction);
				facetMenu.add(addQueryFacetAction);
				facetMenu.add(addChoiceFacetAction);
				facetMenu.add(new Separator());
				facetMenu.add(newComplexCommand);

				propertyMenu.removeAll();
				propertyMenu.add(addPropertiesCommand);
				propertyMenu.add(deleteObjectCommand);
				propertyMenu.add(addRoleAction);
				propertyMenu.add(addEnumValueAction);
				propertyMenu.add(new Separator());
				propertyMenu.add(newComplexCommand);

				addToMenu(basicObjectMenu, newComplexCommand, copyMenu);

				roleObjectMenu.removeAll();
				roleObjectMenu.add(addRoleAction);
				roleObjectMenu.add(new Separator());
				roleObjectMenu.add(newComplexCommand);

				addToMenu(deleteMoveMenu, deleteObjectCommand, copyMenu, moveMenu);
				// addToMenu(deleteMoveMenu, deleteObjectCommand, moveMenu);

				simpleObjectMenu.removeAll();
				simpleObjectMenu.add(cloneObjectAction);
				simpleObjectMenu.add(deleteObjectCommand);
				simpleObjectMenu.add(moveMenu);
				simpleObjectMenu.add(copyMenu);
				simpleObjectMenu.add(new Separator());
				simpleObjectMenu.add(newComplexCommand);

				enumObjectMenu.removeAll();
				enumObjectMenu.add(addPropertiesCommand);
				enumObjectMenu.add(addEnumValueAction);
				enumObjectMenu.add(new Separator());
				enumObjectMenu.add(changeObjectAction);
				enumObjectMenu.add(cloneObjectAction);
				enumObjectMenu.add(moveMenu);
				enumObjectMenu.add(copyMenu);
				enumObjectMenu.add(deleteObjectCommand);
				enumObjectMenu.add(new Separator());
				enumObjectMenu.add(newComplexCommand);

				addToMenu(operationObjectMenu, addPropertiesCommand, new Separator(), deleteObjectCommand,
						new Separator(), newComplexCommand);

				addToMenu(xpFacetObjectMenu, addPropertiesCommand, new Separator(), moveMenu, copyMenu,
						deleteObjectCommand, new Separator(), newComplexCommand);

				serviceObjectMenu.removeAll();
				serviceObjectMenu.add(addOperationAction);
				serviceObjectMenu.add(addCrudqOperationsAction);
				serviceObjectMenu.add(new Separator());
				serviceObjectMenu.add(deleteObjectCommand);
				serviceObjectMenu.add(new Separator());
				serviceObjectMenu.add(newComplexCommand);

			}

			@Override
			public void menuAboutToShow(final IMenuManager manager) {
				createMenu();
				if (NavigatorMenus.this.getSelection().isEmpty()) {
					manager.add(libraryMenu);
					manager.add(projectMenu);
				} else if (NavigatorMenus.this.getSelection() instanceof IStructuredSelection) {
					final IStructuredSelection selection = (IStructuredSelection) NavigatorMenus.this.getSelection();
					final Object selected = selection.getFirstElement();
					if (!(selected instanceof Node)) {
						return;
					}
					Node node = (Node) selected;
					if (node instanceof VersionNode)
						node = ((VersionNode) node).get();

					// Prepare the dynamic list menus
					copyMenu.removeAll();
					moveMenu.removeAll();
					manager.updateAll(true);

					// Set up cascade menus
					versionMenu.removeAll();
					if (node != null && node.getLibrary() != null && node.getLibrary().isManaged())
						versionMenu.setVisible(true);
					for (final Action action : createVersionActions(node))
						versionMenu.add(action);

					for (final Action libAction : createImportActionsForLibraries(node))
						copyMenu.add(libAction);

					for (final Action libAction : createMoveActionsForLibraries(node))
						moveMenu.add(libAction);

					manageInMenu.removeAll();
					manageInMenu.setVisible(true);
					for (final Action action : createRepositoryActionsForLibraries(node))
						manageInMenu.add(action);

					if (node.isXsdType() || node.isXSDSchema()) {
						// You can only import nodes representing XSD types.
						manager.add(basicObjectMenu);
						manager.add(libraryMenu);
					} else if (node instanceof TypeProviderWhereUsedNode) {
						manager.add(whereUsedMenu);
					} else if (node instanceof LibraryProviderNode) {
						manager.add(versionUpdateCommand);
						manager.add(changeProviderLibraryCommand);
					} else if (node instanceof ProjectNode || node instanceof LibraryNode
							|| node instanceof LibraryNavNode || node instanceof LibraryChainNode
							|| node instanceof NavNode) {
						if (node.isInTLLibrary() || node instanceof NavNode) {
							manager.add(navigationMenu);
						}
						manager.add(libraryMenu);
						manager.add(projectMenu);
					} else if (node instanceof ComponentNode) {
						// if (node.isInModel()) {
						if (!node.isEditable()) {
							manager.add(basicObjectMenu);
						} else if (node instanceof ServiceNode) {
							manager.add(serviceObjectMenu);
						} else if (node instanceof OperationNode) {
							manager.add(operationObjectMenu);
						} else if (node instanceof ResourceNode) {
							manager.add(deleteMoveMenu);
							//
						} else if (node instanceof BusinessObjectNode) {
							manager.add(componentMenu);
						} else if (node instanceof ChoiceObjectNode) {
							manager.add(componentMenu);
						} else if (node instanceof CoreObjectNode) {
							manager.add(componentMenu);
						} else if (node instanceof VWA_Node) {
							manager.add(componentMenu);
						} else if (node instanceof Enumeration) {
							manager.add(componentMenu);
						} else if (node instanceof AliasNode) {
							if (!((AliasNode) node).isFacetAlias())
								manager.add(componentMenu); // Only allow root alias to be changed
						} else if (node instanceof ContextualFacetNode && !(node instanceof ContributedFacetNode)) {
							manager.add(componentMenu);
							//
						} else if (node instanceof FacetOMNode) {
							manager.add(facetMenu);
							// } else if (node.isFacetAlias()) {
							// assert false;
							// manager.add(facetMenu); // Reached? should match aliasNode
						} else if (node instanceof ExtensionPointNode) {
							manager.add(xpFacetObjectMenu);
						} else if (node instanceof SimpleTypeNode) {
							manager.add(simpleObjectMenu);
						} else if (node instanceof SimpleAttributeFacadeNode) {
						} else if (node instanceof RoleNode) {
							manager.add(roleObjectMenu);
						} else if (node instanceof EnumLiteralNode) {
							manager.add(enumObjectMenu);
						} else if (node instanceof PropertyNode) {
							manager.add(propertyMenu);
						}

						else if (node.isImportable()) {
							// FIXME - this may need to be changed to test if has XsdObjectHandler
							manager.add(copyMenu);
						}
						// }
						manager.add(libraryMenu);
						manager.add(projectMenu);
					}
				}
				manager.updateAll(true);
			}

			private List<Action> createImportActionsForLibraries(final Node context) {
				final List<Action> libActions = new ArrayList<>();
				for (final LibraryNode ln : getListOfLibraries(context)) {
					final StringProperties sp = new DefaultStringProperties();
					sp.set(PropertyType.TEXT, ln.getNameWithPrefix());
					libActions.add(new ImportObjectToLibraryAction(mainWindow, sp, ln));
				}
				return libActions;
			}

			private List<Action> createMoveActionsForLibraries(final Node n) {
				final List<Action> libActions = new ArrayList<>();
				// New content only, do not allow move minor versions of objects
				if (!n.isEditable_newToChain() || !n.getLibrary().isMoveable())
					return libActions; // No moves for xsd/builtin library members

				for (final LibraryNode ln : getListOfLibraries(n)) {
					final StringProperties sp = new DefaultStringProperties();
					sp.set(PropertyType.TEXT, ln.getNameWithPrefix());
					libActions.add(new MoveObjectToLibraryAction(sp, ln));
				}
				return libActions;
			}

			private List<Action> createVersionActions(Node node) {
				final List<Action> actions = new ArrayList<>();

				actions.add(new VersionAction(VersionType.MAJOR));
				actions.add(new VersionAction(VersionType.MINOR));
				actions.add(new VersionAction(VersionType.PATCH));
				// actions.add(new VersionMajorAction());
				// actions.add(new VersionMinorAction());
				// actions.add(new VersionPatchAction());
				return actions;
			}

			private List<LibraryNode> getListOfLibraries(final Node node) {
				final List<LibraryNode> libs = new ArrayList<>();
				for (final LibraryNode ln : Node.getAllUserLibraries()) {
					if (ln.getChain() != null) {
						// add if it is not in the node's chain and the head of the chain
						if (!ln.getChain().contains(node) && ln.isEditable())
							libs.add(ln);
					} else if (ln != node.getLibrary() && ln.isEditable()) {
						libs.add(ln);
					}
				}
				return libs;
			}

			@Override
			public void menuAboutToHide(IMenuManager manager) {
				// clean the status on menu close contributed by DisableIfEmptyMenu.
				// TODO: clean status on menu selection change. Right now we can only catch
				// SWT.ArmEvent (menu selection) There is missing on exit event.
				OtmRegistry.getMainController().postStatus(" ");

			}
		});
		menuManager.setRemoveAllWhenShown(true);
		getControl().setMenu(menu);
	}

	/**
	 * Clear the menu then add the items in the array
	 * 
	 * @param menu
	 * @param contributionItems
	 */
	protected void addToMenu(ContributionManager menu, IContributionItem... contributionItems) {
		menu.removeAll();
		for (IContributionItem item : contributionItems)
			menu.add(item);
	}

	public static List<Action> createRepositoryActionsForLibraries(final Node lib) {
		final List<Action> repoActions = new ArrayList<>();
		if (lib.getLibrary() == null || !lib.getLibrary().isEditable())
			return repoActions; // No actions available

		RepositoryController rc = OtmRegistry.getMainController().getRepositoryController();
		for (RepositoryNode rn : rc.getAll()) {
			final StringProperties sp = new DefaultStringProperties();
			sp.set(PropertyType.TEXT, rn.getName());
			ManageInRepositoryAction action = new ManageInRepositoryAction(sp, rn);
			if (lib instanceof LibraryNavNode)
				action.setLibrary(((LibraryNavNode) lib).getLibrary());
			else if (lib instanceof LibraryNode) {
				action.setLibrary((LibraryNode) lib);
			}
			repoActions.add(action);
		}
		return repoActions;
	}

	/**
	 * Update tree display to the current object.
	 */
	public void refreshNode(final Node n, final boolean expand) {
		this.refresh();
		if (expand) {
			this.expandToLevel(n, 3);
			// this.expandToLevel(n, 6);
		}
		this.selectNode(n);
	}

	public void refreshNode(final Node n) {
		this.refreshNode(n, true);
	}

	public void selectNode(Node n) {
		if (n == null) {
			((Viewer) this).setSelection(null);
		} else {
			// if (n.isProperty()) {
			// n = n.getParent();
			// }
			if (this.getSelection() != n)
				this.setSelection(new StructuredSelection(n), true);
		}
	}

	public void doubleClickNotification() {
		ISelection selection = getSelection();
		// updateSelection(new DoubleClickSelection((StructuredSelection) selection));
	}

	@Override
	public void preservingSelection(Runnable updateCode) {
		super.preservingSelection(updateCode);
	}

	class DisableIfEmptyMenu extends MenuManager {

		private String disabledDescription;

		public DisableIfEmptyMenu(String text, String id) {
			super(text, id);
		}

		public DisableIfEmptyMenu(String text, String id, String disabledDescription) {
			this(text, id);
			this.disabledDescription = disabledDescription;
		}

		@Override
		public boolean isEnabled() {
			return !isEmpty();
		}

		@Override
		public boolean isVisible() {
			return true;
		}

		@Override
		public void fill(Menu parent, int index) {
			super.fill(parent, index);
			final MenuItem parentItem = getMenu().getParentItem();

			parentItem.addArmListener(new ArmListener() {

				@Override
				public void widgetArmed(ArmEvent event) {
					if (!isEnabled()) {
						OtmRegistry.getMainController().postStatus("[" + getMenuText() + "] " + disabledDescription);
					}
				}

			});

		}
	}

}
