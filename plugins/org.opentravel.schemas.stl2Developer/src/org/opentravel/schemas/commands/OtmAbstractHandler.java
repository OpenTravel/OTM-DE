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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.jface.resource.ImageDescriptor;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.ExtensionPointNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.ServiceNode;
import org.opentravel.schemas.node.facets.OperationNode;
import org.opentravel.schemas.node.interfaces.VersionedObjectInterface;
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
		if (mc == null) {
			throw new IllegalArgumentException("Tried to construct view without a main controller.");
		}
		mainWindow = mc.getMainWindow();
	}

	public void execute(OtmEventData event) {
		LOGGER.debug("Menthod not implemented");
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
		ComponentNode actOnNode = null; // The node to perform the action on.
		boolean result = false;
		if (selectedNode.getChain() == null)
			return null;

		if (selectedNode.isInHead())
			LOGGER.warn("No version extension needed, " + selectedNode + " is already in head library.");

		if (selectedNode.getChain().getHead().isPatchVersion()) {
			// Will always be in a different library or else it is a ExtensionPoint facet.
			if (!(selectedNode instanceof ExtensionPointNode)) {
				if (result = postConfirm("action.component.version.patch", selectedNode))
					actOnNode = ((ComponentNode) selectedNode).createPatchVersionComponent();
			}

		}

		// If a major minor version, create a new object of same type and add base link to this.
		else if (selectedNode.getChain().getHead().isMinorOrMajorVersion()) {
			if (selectedNode instanceof VersionedObjectInterface) {
				if (result = postConfirm("action.component.version.minor", selectedNode))
					actOnNode = ((VersionedObjectInterface) selectedNode).createMinorVersionComponent();
			} else if (selectedNode.isEditable_inService())
				// services are unversioned so just return the selected node
				if (selectedNode.getLibrary().getChain().getHead() == selectedNode.getLibrary())
					actOnNode = (ComponentNode) selectedNode;
				else
					actOnNode = new OperationNode((ServiceNode) selectedNode.getLibrary().getServiceRoot()
							.getChildren().get(0), "newOperation");
			// TESTME - if the service is not in the head then create a new service in the head
		}

		if (actOnNode == null && result == true)
			// LOGGER.error("Did not create Version for " + selectedNode);
			DialogUserNotifier.openWarning("Error", "Could not create minor version of " + selectedNode
					+ ". Try validating the library and correcting any problems reported.");

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
