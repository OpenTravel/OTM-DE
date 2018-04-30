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
/**
 * 
 */
package org.opentravel.schemas.commands;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.jface.resource.ImageDescriptor;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.node.ServiceNode;
import org.opentravel.schemas.node.interfaces.FacetInterface;
import org.opentravel.schemas.node.interfaces.VersionedObjectInterface;
import org.opentravel.schemas.node.libraries.LibraryNavNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.objectMembers.ExtensionPointNode;
import org.opentravel.schemas.node.objectMembers.OperationNode;
import org.opentravel.schemas.properties.Messages;
import org.opentravel.schemas.stl2developer.DialogUserNotifier;
import org.opentravel.schemas.stl2developer.MainWindow;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.widgets.OtmEventData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dave Hollander
 * 
 */
public abstract class OtmAbstractHandler extends AbstractHandler implements OtmHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(OtmAbstractHandler.class);

	protected MainController mc;
	private MainWindow mainWindow;

	protected OtmAbstractHandler() {
		this(OtmRegistry.getMainController());
	}

	protected OtmAbstractHandler(MainController mc) {
		// LOGGER.debug("Handler constructed with controller: "+this.getClass());
		this.mc = mc;
		if (mc == null)
			throw new IllegalArgumentException("Tried to construct view without a main controller.");

		mainWindow = mc.getMainWindow();
		if (mainWindow == null)
			throw new IllegalArgumentException("Tried to construct view without a main window.");
	}

	/**
	 * @return the first globally selected node or null.
	 * @see org.opentravel.schemas.controllers.MainController#getGloballySelectNodes()
	 */
	public Node getFirstSelected() {
		if (mc == null)
			return null;
		List<Node> selectedNodes = mc.getGloballySelectNodes();
		Node selection = null;
		if (selectedNodes != null && !selectedNodes.isEmpty())
			selection = selectedNodes.get(0);
		return selection;
	}

	/**
	 * Get all libraries that own selected nodes. Remove duplicates. Note: library may be in multiple projects.
	 * 
	 * @return de-duplicated list of libraries containing selected in navigator view nodes
	 */
	public List<LibraryNode> getSelectedLibraries(boolean editableOnly) {
		List<LibraryNode> libraries = new ArrayList<>();
		for (Node cn : mc.getSelectedNodes_NavigatorView()) {
			if (cn.getLibrary() != null && !libraries.contains(cn.getLibrary()))
				if (editableOnly && cn.getLibrary().isEditable())
					libraries.add(cn.getLibrary());
				else
					libraries.add(cn.getLibrary());
		}
		return libraries;
	}

	/**
	 * Library Nav Nodes are returned. Library Nav Nodes connect a library to a specific project. Only library nav nodes
	 * know which project the library is in. Duplicates removed from list.
	 * 
	 * @return all selected libraryNavNodes or empty list.
	 */
	public List<LibraryNavNode> getSelectedLibraryNavNodes() {
		List<LibraryNavNode> libs = new ArrayList<>();
		List<Node> nodes = mc.getSelectedNodes_NavigatorView();
		for (Node n : nodes) {
			if (n instanceof LibraryNavNode)
				if (!libs.contains(n))
					libs.add((LibraryNavNode) n);
		}
		return libs;
	}

	/**
	 * @return ProjectNode containing the selected navigator view node or null.
	 */
	public ProjectNode getSelectedProject() {
		Node node = mc.getSelectedNode_NavigatorView();
		ProjectNode project = null;

		if (node != null) {
			if ((node instanceof ProjectNode))
				project = (ProjectNode) node;
			else {
				node = node.getLibrary();
				if (node != null)
					project = ((LibraryNode) node).getProject();
			}
		}
		return project;
	}

	public void execute(OtmEventData event) {
		LOGGER.debug("Method not implemented");
	}

	protected MainWindow getMainWindow() {
		return mainWindow;
	}

	protected MainController getMainController() {
		return mc;
	}

	/**
	 * Individual handlers should override if they have an icon.
	 */
	public static ImageDescriptor getIcon() {
		return null;
	}

	public static String COMMAND_ID = "org.opentravel.schemas.commands";

	@Override
	public String getID() {
		return COMMAND_ID;
	}

	/**
	 * Create a component in the head library that versions (extends) the selected node. Prompts the user to confirm
	 * before creating node.
	 * 
	 * @return newly created node or null if user cancelled or error.
	 */
	public ComponentNode createVersionExtension(Node selectedNode) {
		boolean result = false;
		ComponentNode actOnNode = null; // The node created by versioning the passed node.
		FacetInterface selectedFacet = null;
		VersionedObjectInterface selectedOwner = null;
		if (selectedNode.getOwningComponent() instanceof VersionedObjectInterface)
			selectedOwner = (VersionedObjectInterface) selectedNode.getOwningComponent();

		if (selectedNode.isEditable_inService()) {
			// services are unversioned so just return the selected node
			if (selectedNode.getLibrary().getChain().getHead() == selectedNode.getLibrary())
				actOnNode = (ComponentNode) selectedNode;
			else
				actOnNode = new OperationNode(
						(ServiceNode) selectedNode.getLibrary().getServiceRoot().getChildren().get(0), "newOperation");
			// TESTME - if the service is not in the head then create a new service in the head
			return actOnNode;
		}

		if (selectedOwner == null) {
			LOGGER.warn(selectedNode + " Owner " + selectedOwner + " is not a versioned object ");
			return null;
		}
		if (selectedNode.getChain() == null) {
			LOGGER.warn(selectedNode + " is not in a versioned library chain.");
			return null;
		}
		if (selectedNode.isInHead()) {
			LOGGER.warn("No version extension needed, " + selectedNode + " is already in head library.");
			return null;
		}

		// Do patch version if head of chain is patch
		if (selectedNode.getChain().getHead().isPatchVersion()) {
			// Will always be in a different library or else it is a ExtensionPoint facet.
			if (!(selectedNode instanceof ExtensionPointNode)) {
				if (result = postConfirm("action.component.version.patch", selectedNode))
					actOnNode = ((ComponentNode) selectedNode).createPatchVersionComponent();
			} else
				DialogUserNotifier.openWarning("Warning", "Can not create patch version of " + selectedNode);

			return actOnNode;
		}

		// Do a minor version if the head is a minor or major version
		if (selectedNode.getChain().getHead().isMinorOrMajorVersion()) {
			// Hold onto for later and use the owner to create versioned component
			if (selectedNode instanceof FacetInterface)
				selectedFacet = (FacetInterface) selectedNode;

			// Confirm with the user then create a new object of same type and add base link to this.
			if (result = postConfirm("action.component.version.minor", selectedNode))
				actOnNode = selectedOwner.createMinorVersionComponent();
		}

		if (actOnNode == null && result == true)
			// LOGGER.error("Did not create Version for " + selectedNode);
			DialogUserNotifier.openWarning("Error", "Could not create minor version of " + selectedNode
					+ ". Try validating the library and correcting any problems reported.");

		// If not null then return the matching facet in the new component
		if (selectedFacet != null && actOnNode != null) {
			for (Node n : actOnNode.getChildren())
				if (n.getName().equals(selectedFacet.getName()))
					actOnNode = (ComponentNode) n;
		}
		return actOnNode;
	}

	/**
	 * If the library of the passed node is not in the chain's head library, ask the user if they want to create one.
	 * 
	 * @return user response or false if in head library
	 */
	private boolean postConfirm(String message, Node selectedNode) {
		if (!selectedNode.isInHead())
			// if (selectedNode.getLibrary() != selectedNode.getChain().getHead())
			return (DialogUserNotifier.openConfirm(Messages.getString("action.component.version.title"),
					Messages.getString(message)));
		else
			return false;
	}

}
