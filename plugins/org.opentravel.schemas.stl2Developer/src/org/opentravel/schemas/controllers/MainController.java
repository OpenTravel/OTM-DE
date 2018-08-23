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
package org.opentravel.schemas.controllers;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryFileManager;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemas.actions.ImportObjectToLibraryAction;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.node.VersionNode;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.properties.Messages;
import org.opentravel.schemas.stl2developer.Activator;
import org.opentravel.schemas.stl2developer.DialogUserNotifier;
import org.opentravel.schemas.stl2developer.MainWindow;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.views.NavigatorView;
import org.opentravel.schemas.views.OtmView;
import org.opentravel.schemas.views.ValidationResultsView;
import org.opentravel.schemas.widgets.ErrorWithExceptionDialog;
import org.opentravel.schemas.widgets.OtmHandlers;
import org.opentravel.schemas.widgets.OtmSections;
import org.opentravel.schemas.widgets.OtmTextFields;
import org.opentravel.schemas.widgets.OtmWidgets;
import org.opentravel.schemas.wizards.NewPropertiesWizard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main Controller. Receives events from the view and workbench, drives user interactions then executes commands to
 * update/manipulate the models.
 * 
 * - This class provides access to controller that do not rely upon the workbench. - It can be used for junit tests. -
 * Provides and manages registry of the various view specific controllers. - Provides access to selected nodes.
 * 
 * @author Dave Hollander
 */

public class MainController {
	private static final Logger LOGGER = LoggerFactory.getLogger(MainController.class);

	public static final String WARNING_MSG = "Warning";
	public static final String NO_VALID_SELECTION_MSG = "No valid selection";

	private ModelNode modelNode;

	private MainWindow mainWindow = null;

	private OtmHandlers handlers;
	private OtmActions actions;
	private OtmWidgets widgets;
	private final OtmSections sections;
	private final OtmTextFields fields;

	private final LibraryController libraryController;
	private final ModelController modelController;
	private final ContextController contextController;
	// private final NodeModelController nodeModelController;
	private ProjectController projectController;
	private RepositoryController repositoryController;

	private final OtmView defaultView;

	private IHandlerService handlerService;
	private IWorkbenchPartSite site = null;
	private final String CopyNameSuffix = "_Copy";

	private ListenerList refreshList = new ListenerList();

	/**
	 * MUST only be created by OTM Registry. Use {@link OtmRegistry#getMainController()} to get the live copy.
	 */
	public MainController() {
		this(getDefaultRepositoryManager());
		// LOGGER.debug("MainController constructor complete.");
	}

	public static RepositoryManager getDefaultRepositoryManager() {
		RepositoryManager defaultManager = null;
		try {
			defaultManager = RepositoryManager.getDefault();
		} catch (RepositoryException ex) {
			IStatus ss = new Status(IStatus.ERROR, Activator.PLUGIN_ID, ex.getMessage(), ex);
			ErrorWithExceptionDialog.openError(PlatformUI.getWorkbench().getDisplay().getActiveShell(),
					JFaceResources.getString("error"),
					MessageFormat.format(Messages.getString("dialog.localRepository.error.message"),
							RepositoryFileManager.getDefaultRepositoryLocation()),
					ss);
			LOGGER.error("Invalid local repository", ex);
			PlatformUI.getWorkbench().close();
		} catch (Exception e) {
			LOGGER.warn("Unknow exception: " + e.getLocalizedMessage());
		}
		return defaultManager;
	}

	// FIXME - this should ONLY be called by OtmRegistry
	public MainController(final RepositoryManager repositoryManager) {
		// LOGGER.info("Initializing: " + this.getClass());

		OtmRegistry.registerMainController(this);
		mainWindow = OtmRegistry.getMainWindow(); // if headless it will be null
		defaultView = new NavigatorView(); // Always have one view.

		handlers = new OtmHandlers();
		actions = new OtmActions(this);
		widgets = new OtmWidgets(getActions(), getHandlers());
		sections = new OtmSections(getActions(), getHandlers());
		fields = new OtmTextFields(getActions(), getHandlers());

		// LOGGER.info("Initializing Library controller.");
		libraryController = new DefaultLibraryController(this);
		// LOGGER.info("Initializing Model controller.");
		modelController = new DefaultModelController(this, libraryController);
		// LOGGER.info("Initializing Model Node.");
		modelNode = modelController.getModel();
		// LOGGER.info("Initializing nodeModel controller.");
		// nodeModelController = new NodeModelController(this);
		// LOGGER.info("Initializing Context controller.");
		contextController = new DefaultContextController(this);
		// LOGGER.info("Initializing Repository controller.");
		repositoryController = new DefaultRepositoryController(this, repositoryManager);
		// LOGGER.info("Initializing Project controller.");
		projectController = new DefaultProjectController(this, repositoryManager);

		LOGGER.info("Initialization complete. ");
	}

	/** ************************ Model Controller Access ***************************** **/

	public ContextController getContextController() {
		return contextController;
	}

	public LibraryController getLibraryController() {
		return libraryController;
	}

	/**
	 * @return the repositoryController
	 */
	public RepositoryController getRepositoryController() {
		return repositoryController;
	}

	/**
	 * @param repositoryController
	 *            the repositoryController to set
	 */
	public void setRepositoryController(DefaultRepositoryController defaultRepositoryController) {
		this.repositoryController = defaultRepositoryController;
	}

	public MainWindow getMainWindow() {
		return mainWindow;
	}

	public ModelController getModelController() {
		return modelController;
	}

	/**
	 * @return the projectController
	 */
	public ProjectController getProjectController() {
		return projectController;
	}

	/**
	 * @param projectController
	 *            the projectController to set
	 */
	public void setProjectController(ProjectController projectController) {
		this.projectController = projectController;
	}

	public OtmHandlers getHandlers() {
		return handlers;
	}

	public void setHandlers(final OtmHandlers handlers) {
		this.handlers = handlers;
	}

	public OtmActions getActions() {
		return actions;
	}

	public void setActions(final OtmActions actions) {
		this.actions = actions;
	}

	public OtmWidgets getWidgets() {
		return widgets;
	}

	/**
	 * @return the workbench from the Platform UI or null.
	 */
	public IWorkbench getWorkbench() {
		IWorkbench workbench = null;
		try {
			workbench = PlatformUI.getWorkbench();
		} catch (IllegalStateException e) {
			return null; // No workbench or display.
		}
		return workbench;
	}

	public void setWidgets(final OtmWidgets widgets) {
		this.widgets = widgets;
	}

	public OtmSections getSections() {
		return sections;
	}

	public OtmTextFields getFields() {
		return fields;
	}

	/**
	 * @return the defaultView
	 */
	public OtmView getDefaultView() {
		return defaultView;
	}

	public ValidationResultsView getView_Validation() {
		return OtmRegistry.getValidationResultsView();
	}

	// public ExampleView getView_Example() {
	// return OtmRegistry.getExampleView();
	// }

	/** ************************ Current Item Access ***************************** **/

	/**
	 * @return the current node displayed in the type view facet table.
	 */
	public INode getCurrentNode_TypeView() {
		return OtmRegistry.getTypeView() != null ? OtmRegistry.getTypeView().getCurrentNode() : null;
	}

	public INode getCurrentNode_FacetView() {
		return OtmRegistry.getFacetView() != null ? OtmRegistry.getFacetView().getCurrentNode() : null;
	}

	/**
	 * @return the node currently be viewed in the properties view.
	 */
	public INode getCurrentNode_PropertiesView() {
		return OtmRegistry.getPropertiesView() != null ? OtmRegistry.getPropertiesView().getCurrentNode() : null;
	}

	/**
	 * @return the current navigator view (treeView) node or null if none selected.
	 */
	public Node getCurrentNode_NavigatorView() {
		return (Node) (OtmRegistry.getNavigatorView() != null ? OtmRegistry.getNavigatorView().getCurrentNode() : null);
	}

	/**
	 * @return the current resource view (treeView) node or null if none selected.
	 */
	public Node getCurrentNode_ResourceView() {
		return (Node) (OtmRegistry.getResourceView() != null ? OtmRegistry.getResourceView().getCurrentNode() : null);
	}

	/**
	 * Set the current node displayed in the type view facet table.
	 */
	public void setCurrentNode_TypeView(Node node) {
		final OtmView view = OtmRegistry.getTypeView();
		if (view != null) {
			view.setCurrentNode(node);
		}
	}

	/**
	 * Set the node currently be viewed in the properties view.
	 */
	public void setCurrentNode_PropertiesView(Node node) {
		final OtmView view = OtmRegistry.getPropertiesView();
		if (view != null) {
			view.setCurrentNode(node);
		}
	}

	/**
	 * Set the current navigator view (treeView) node.
	 */
	public void setCurrentNode_NavigatorView(Node node) {
		final OtmView view = OtmRegistry.getNavigatorView();
		if (view != null) {
			view.setCurrentNode(node);
		}
	}

	/**
	 * @return the catalogRoot
	 */
	public ModelNode getModelNode() {
		return modelNode;
	}

	/** ************************ Selected Item Access ***************************** **/

	/**
	 * Gets the navigator selected libraries. All libraries of selected projects are also returned.
	 * 
	 * @return new list of selected navigator view nodes, possibly empty.
	 */
	public List<LibraryNode> getSelectedLibraries() {
		final List<LibraryNode> libraries = new ArrayList<>();
		final List<Node> nodes = getSelectedNodes_NavigatorView();
		for (final Node node : nodes) {
			if (node != null) {
				final LibraryNode library = node.getLibrary();
				if (!libraries.contains(library)) {
					libraries.add(library);
				} else if (node instanceof ProjectNode) {
					for (INode n : node.getChildren())
						if (n instanceof LibraryNode && !libraries.contains(n))
							libraries.add((LibraryNode) n);
						else if (node instanceof LibraryChainNode)
							libraries.addAll(((LibraryChainNode) node).getLibraries());
				} else if (node instanceof LibraryChainNode)
					libraries.addAll(((LibraryChainNode) node).getLibraries());
			}
		}
		// Set<LibraryNode> libs = new HashSet<LibraryNode>();
		// return new ArrayList<>(libs);
		return libraries;
	}

	/**
	 * Gets the navigator selected nodes and filters out non-user libraries.
	 * 
	 * @return new list of selected navigator view nodes, possibly empty.
	 */
	public List<LibraryNode> getSelectedUserLibraries() {
		final List<LibraryNode> libraries = new ArrayList<>();
		for (final LibraryNode lib : getSelectedLibraries()) {
			if (lib != null && lib.isTLLibrary()) {
				libraries.add(lib);
			}
		}
		return libraries;
	}

	/**
	 * Get the current facet node or first node selected from facet table in type View.
	 * 
	 * @return selected node or null.
	 */
	public Node getSelectedNode_TypeView() {
		Node n = null;
		OtmView view = OtmRegistry.getTypeView();
		if (view == null)
			return null;

		final List<Node> selectedNodes = getSelectedNodes_TypeView();
		if (selectedNodes == null || selectedNodes.isEmpty()) {
			n = (Node) view.getCurrentNode();
		} else {
			n = selectedNodes.get(0);
		}
		return n;
	}

	/**
	 * Get the first node selected from navigator view.
	 * 
	 * @return selected node or null.
	 */
	public Node getSelectedNode_NavigatorView() {
		final List<Node> selected = getSelectedNodes_NavigatorView();
		if (selected.size() > 0) {
			if (selected.get(0) instanceof VersionNode)
				return ((VersionNode) selected.get(0)).get();
			return selected.get(0);
		}
		return null;
	}

	/**
	 * Gets the navigator selected nodes and filters out non-component nodes.
	 * 
	 * @return new list of selected navigator view nodes, possibly empty.
	 */
	public List<ComponentNode> getSelectedComponents_NavigatorView() {
		final List<ComponentNode> componentNodes = new ArrayList<>();
		final List<Node> sourceNodes = getSelectedNodes_NavigatorView();
		for (final INode node : sourceNodes) {
			if (node instanceof ComponentNode) {
				componentNodes.add((ComponentNode) node);
			}
		}
		return componentNodes;
	}

	/**
	 * The navigator selected nodes.
	 * 
	 * @return new list of selected navigator view nodes, possibly empty.
	 */
	public List<Node> getSelectedNodes_NavigatorView() {
		final OtmView view = OtmRegistry.getNavigatorView();
		if (view != null) {
			return view.getSelectedNodes();
		}
		return Collections.emptyList();
	}

	public List<Node> getSelectedNodes_TypeView() {
		final OtmView view = OtmRegistry.getTypeView();
		if (view != null) {
			return view.getSelectedNodes();
		}
		return Collections.emptyList();
	}

	/**
	 * Refresh all views convenience method.
	 */
	/**
	 * Re-read all libraries from repository, create new node structure.
	 */
	public void refreshMaster() {
		projectController.refreshMaster();
	}

	public void refresh() {
		OtmRegistry.getNavigatorView().refreshAllViews();
		fireRefreshNotyfication(null);
	}

	// used for libraryDecoration and elsewhere
	public void addRefreshListener(IRefreshListener listener) {
		refreshList.add(listener);
	}

	public void removeRefreshListener(IRefreshListener listener) {
		refreshList.remove(listener);
	}

	public interface IRefreshListener {
		public void refresh(INode node);

		public void refresh();
	}

	public void refresh(final INode node) {
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				OtmRegistry.getNavigatorView().refreshAllViews(node);
				fireRefreshNotyfication(node);
			}
		});
	}

	private void fireRefreshNotyfication(INode node) {
		for (Object lis : refreshList.getListeners()) {
			IRefreshListener ref = (IRefreshListener) lis;
			if (node == null) {
				ref.refresh();
			} else {
				ref.refresh(node);
			}
		}
	}

	/** *********************************** Command and Action methods ****************** **/

	/**
	 * Run the
	 */
	public void runDeleteNode(Event event) {
		String cmd = "org.opentravel.schemas.commands.DeleteNode";
		runCommand(cmd, event);
	}

	/**
	 * Run the
	 */
	public void runSaveLibraries(Event event) {
		String cmd = "org.opentravel.schemas.commands.SaveLibraries";
		runCommand(cmd, event);
	}

	/**
	 * Run a command handler.
	 * 
	 * @param cmd
	 *            = COMMAND_ID from handler
	 * @param node
	 *            to pass in command event.
	 */
	public void runCommand(String cmd, Node node) {
		Event event = new Event();
		event.data = node;
		runCommand(cmd, event);
	}

	public void runCommand(String cmd, Event event) {
		if (mainWindow != null && mainWindow.hasDisplay()) {
			if (site == null)
				site = mainWindow.getSite();
			// TODO - TEST - used to get site from navigator window
			if (handlerService == null)
				handlerService = (IHandlerService) site.getService(IHandlerService.class);
			try {
				// LOGGER.debug("Ready to execute command: "+cmd+" with event: "+event);
				handlerService.executeCommand(cmd, event);
			} catch (Exception ex) {
				LOGGER.error("Command error: " + ex.getLocalizedMessage());
				// DialogUserNotifier.openWarning(WARNING_MSG, "Could not execute command.");
			}
		} else {
			// LOGGER.debug("TODO - add non-workbench command dispatch");
		}
	}

	/**
	 * Post a message on the status line. Preferred method for status as it is safe to include in code that can be run
	 * in junit tests.
	 * 
	 * @param msg
	 */
	public void postStatus(String msg) {
		if (mainWindow != null)
			mainWindow.postStatus(msg);
		else
			LOGGER.debug(msg);
	}

	/**
	 * Show that the system is busy.
	 */
	public void showBusy(boolean state) {
		if (mainWindow.hasDisplay())
			mainWindow.showBusy(state);
	}

	/** ********************* LEGACY BUSINESS LOGIC *************************** **/

	// /**
	// * Runs change wizard on the selected component.
	// */
	// @Deprecated
	// public void changeTreeSelection() {
	// final Node n = getCurrentNode_NavigatorView();
	// if (n != null) {
	// changeNode((ComponentNode) n.getOwningComponent());
	// }
	// }

	// /**
	// * Change the selected type view node. Used by change object action. 1) clones node 2) replaces everything that
	// uses
	// * the selected node as a type to use the clone 3) runs wizard with the cloned node. Wizard is responsible for
	// * making any model changes directed by the user. 4a) original node replaced back into the model if the wizard is
	// * cancelled. 4b) original node deleted if wizard completes normally. 5) clone moved to new library if necessary
	// * (TODO -- wizard should do this)
	// */
	// @Deprecated
	// public void changeSelection() {
	// final Node selected = getSelectedNode_TypeView();
	// if (selected != null) {
	// final ComponentNode n = (ComponentNode) selected.getOwningComponent();
	// if (n != null) {
	// changeNode(n);
	// }
	// }
	// }

	// public void changeNode(final ComponentNode nodeToReplace) {
	//
	// if (nodeToReplace == null || nodeToReplace.getLibrary() == null) {
	// LOGGER.error("Null in change node.");
	// return;
	// }
	// if (nodeToReplace instanceof ServiceNode || !nodeToReplace.isInTLLibrary()) {
	// LOGGER.warn("Invalid state. Cannot change " + nodeToReplace);
	// return;
	// }
	//
	// // LOGGER.debug("Changing selected component: " + nodeToReplace.getName() + " with "
	// // + nodeToReplace.getTypeUsersCount() + " users.");
	//
	// LibraryNode srcLib = nodeToReplace.getLibrary();
	// ComponentNode editedNode = nodeToReplace;
	//
	// // LOGGER.debug("Changing Edited component: " + editedNode.getName() + " with "
	// // + editedNode.getTypeUsersCount() + " users.");
	//
	// // Wizard must maintain the editedComponent active in the library.
	// final ChangeWizard wizard = new ChangeWizard(editedNode);
	// wizard.run(OtmRegistry.getActiveShell());
	// if (wizard.wasCanceled()) {
	// selectNavigatorNodeAndRefresh(nodeToReplace);
	// } else {
	// editedNode = wizard.getEditedComponent();
	// // If the library is different than the srcLib, the object needs to be moved.
	// // The library in the object is only an indicator of the library to move to.
	// // The edited node will be in the src Library.
	// if (!editedNode.getLibrary().equals(srcLib)) {
	// LibraryNode destLib = editedNode.getLibrary();
	// editedNode.setLibrary(srcLib);
	// srcLib.moveMember(editedNode, destLib);
	// }
	// if (editedNode != nodeToReplace) {
	// // Use the visitor because without a library it will not be delete-able.
	// NodeVisitor visitor = new NodeVisitors().new deleteVisitor();
	// nodeToReplace.visitAllNodes(visitor);
	// // nodeToReplace.delete();
	// }
	// selectNavigatorNodeAndRefresh(editedNode);
	// refresh(editedNode);
	//
	// // LOGGER.info("Component after change: " + editedComponent + " with "
	// // + editedComponent.getTypeUsersCount() + " users.");
	// }
	// // LOGGER.debug("library has " + ln.getChildren_NamedTypes().size() + " children.");
	//
	// // Test Result
	// // NodeModelTestUtils.testNodeModel();
	// // Validate the library after doing change.
	// // checkModelCounts(srcLib); // these don't work right with chains and contextual facets
	// // checkModelCounts(editedNode.getLibrary());
	// // }
	// }

	public static boolean checkModelCounts(final LibraryNode lib) {
		int tlCount = 0, guiCount = 0;
		guiCount = lib.getDescendants_LibraryMembersAsNodes().size();
		tlCount = lib.getTLaLib().getNamedMembers().size();
		if (guiCount != tlCount) {
			LOGGER.error("GUI member count " + guiCount + " is out of sync with TL model " + tlCount + ".");
			return false;
		}
		// LOGGER.debug(lib + " has " + guiCount + " children.");
		return true;
	}

	public void cloneSelectedFacetNodes() {
		// get the action list from the facet table. If none selected, use
		// facets current node.
		final List<Node> facetCloneList = getSelectedNodes_TypeView();
		final List<Node> treeCloneList = getSelectedNodes_NavigatorView();
		final Node cn = getSelectedNode_TypeView();

		if (facetCloneList.isEmpty()) {
			if (treeCloneList.isEmpty()) {
				facetCloneList.add(cn);
			} else {
				facetCloneList.addAll(treeCloneList);
			}
		}
		cloneNodes(facetCloneList);
	}

	// /**
	// * Implements the Copy action.
	// */
	// public void copySelectedNodes() {
	// cloneNodes(getSelectedNodes_NavigatorView());
	// }

	private void cloneNodes(List<Node> nodes) {
		// LOGGER.debug("Cloning " + nodes.size() + " selected components. ");

		Node lastCloned = null;
		for (Node n : nodes) {
			Node clone = n.clone(CopyNameSuffix);
			if (clone != null)
				lastCloned = clone;
		}
		if (lastCloned != null) {
			selectNavigatorNodeAndRefresh(lastCloned);
		}
	}

	public void importSelectedToDragTarget(boolean isCopy) {
		if (modelNode != null) {
			final Node target = handlers.getDragTargetNode();
			if (target != null && target.getLibrary() != null) {
				final LibraryNode library = target.getLibrary();
				// LOGGER.debug("Importing selected nodes to drag target library: " + library.getName());
				ImportObjectToLibraryAction action = new ImportObjectToLibraryAction(mainWindow, library);
				action.importSelectedToLibrary(library);
			} else {
				LOGGER.error("Cannot import - drag target is null");
			}
		} else {
			LOGGER.error("Cannot import - source ( model ) is null");
		}
	}

	@Deprecated
	public NewPropertiesWizard initializeNewPropertiesWizard(final INode component) {
		NewPropertiesWizard newPropertiesWizard = null;
		// code migrated to AddPropertytoComponent handler.
		return newPropertiesWizard;
	}

	// /**
	// *
	// */
	// // /TODO - move
	// public void setExtendable(final boolean extendable) {
	// // final Node facetNode = getSelectedNode_TypeView();
	// // if (facetNode != null) {
	// // LOGGER.debug("Changing extendable property of " + facetNode + " to " + extendable);
	// // facetNode.setExtendable(extendable);
	// // defaultView.refreshAllViews();
	// // }
	// }

	public void clearSelection() {
		final OtmView view = OtmRegistry.getNavigatorView();
		if (view != null) {
			view.clearSelection();
		}
	}

	public Node getPrevTreeNode() {
		final OtmView view = OtmRegistry.getNavigatorView();
		if (view != null) {
			return (Node) view.getPreviousNode();
		}
		return null;
	}

	/**
	 * @param modelNode
	 *            - the catalogRoot to set
	 */
	public void setModelNode(final ModelNode modelNode) {
		// this.curNode = modelNode;
		// LOGGER.debug("setting catalog root node.");
		this.modelNode = modelNode;
		final OtmView mnView = OtmRegistry.getNavigatorView();
		if (mnView != null) {
			mnView.setCurrentNode(modelNode);
			mnView.setInput(modelNode);
			if (!Node.getAllUserLibraries().isEmpty())
				mnView.select(Node.getAllUserLibraries().get(0));
		}
		defaultView.refreshAllViews();
		defaultView.setCurrentNode(modelNode);
	}

	/**
	 *
	 */
	public void openLibraryInSystemEditor() {
		final List<LibraryNode> libs = this.getSelectedLibraries();
		final Desktop desktop = Desktop.getDesktop();
		for (final LibraryNode lib : libs) {
			final String path = lib.getPath();
			final File file = new File(path);
			try {
				if (!file.exists()) {
					if (path.startsWith("http")) {
						desktop.browse(new URI(path));
					} else {
						DialogUserNotifier.openError("Open file", "Could not find the file associated to the library "
								+ lib.getName() + " (" + path + ")", null);
					}
				} else {
					desktop.open(file);
				}
			} catch (final IOException e) {
				DialogUserNotifier.openError("Open file",
						"Could not open the file, an error occurred: " + e.getMessage(), e);
			} catch (final URISyntaxException e) {
				DialogUserNotifier.openError("Open file",
						"Could not open the file, its URI is malformed: " + e.getMessage(), e);
			}
		}
	}

	/**
	 * Change current selection in NavigatorView.
	 * 
	 * @param node
	 *            - node to select
	 */
	public void selectNavigatorNodeAndRefresh(INode node) {
		if (OtmRegistry.getNavigatorView() != null) {
			OtmRegistry.getNavigatorView().setCurrentNode(node); // sets IView current node
			OtmRegistry.getNavigatorView().select(node); // throws section event.
		}
		// OtmRegistry.getNavigatorView().refresh(); // updates tree contents
		// in some cases NavigatorView is selecting parent of node but FacetView should select node.
		if (OtmRegistry.getTypeView() != null)
			OtmRegistry.getTypeView().setCurrentNode(node);
	}

	/**
	 * @return First node selected in active view otherwise null
	 */
	public Node getGloballySelectNode() {
		Node node = null;
		List<Node> nodes = getGloballySelectNodes();
		if (!nodes.isEmpty())
			node = nodes.get(0);
		return node;
	}

	/**
	 * @return Selected nodes in TypeView. If selection of TypeView is empty then return selected nodes from
	 *         NavigatorView otherwise return empty list; Null if no workbench active page.
	 */
	public List<Node> getGloballySelectNodes() {
		// IWorkbenchPage page = getActivePage();
		// if (PlatformUI.getWorkbench() != null)
		// if (PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null)
		// page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		// if (page == null)
		// return null;

		// IWorkbenchPart part = page.getActivePart();
		IWorkbenchPart part = getActivePart();
		// FYI- this works: IViewPart view = page.findView(NavigatorView.VIEW_ID);

		List<Node> nodes = getSelectedNodes_TypeView();
		if (part instanceof OtmView)
			nodes = ((OtmView) part).getSelectedNodes();

		// This should never happen - but does when context view is open
		// assert nodes != null;
		if (nodes == null || nodes.isEmpty()) {
			nodes = getSelectedNodes_NavigatorView();
		}
		if (nodes == null) {
			nodes = Collections.emptyList();
		}
		return nodes;
	}

	/**
	 * Determine if the workbench has an active page. On startup, the page will be null.
	 * 
	 * @return active workbench page or null
	 */
	public IWorkbenchPart getActivePart() {
		IWorkbenchPage page = null;
		if (PlatformUI.getWorkbench() != null)
			if (PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null)
				page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		if (page == null)
			return null;

		return page.getActivePart();
	}
}
