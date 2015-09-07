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

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Event;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.CoreObjectNode;
import org.opentravel.schemas.node.INode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.OperationNode;
import org.opentravel.schemas.node.PropertyNodeType;
import org.opentravel.schemas.node.RoleFacetNode;
import org.opentravel.schemas.node.ServiceNode;
import org.opentravel.schemas.node.properties.EnumLiteralNode;
import org.opentravel.schemas.properties.ExternalizedStringProperties;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.properties.Messages;
import org.opentravel.schemas.stl2developer.DialogUserNotifier;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.wizards.NewPropertiesWizard2;
import org.opentravel.schemas.wizards.SimpleNameWizard;
import org.opentravel.schemas.wizards.validators.NewNodeNameValidator;
import org.opentravel.schemas.wizards.validators.NewPropertyValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command Handler for the add a node to the model command.
 * Handler for adding when a component node is selected.
 * 
 * Version 2 uses the NewPropertiesWizard2 that acts upon the currently selected object.
 * 
 * Handles action: org.opentravel.schemas.commands.AddProperties

 * @author Dave Hollander
 *
 */
/**
 * TODO - Move responsibility for deciding on what to do to the nodes.
 * 
 */
public class AddNodeHandler2 extends OtmAbstractHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(AddNodeHandler2.class);
	public static String COMMAND_ID = "org.opentravel.schemas.commands.Add2";

	private Node selectedNode; // The user selected node.
	private ComponentNode actOnNode; // The node to perform the action on.

	@Override
	public Object execute(ExecutionEvent exEvent) throws ExecutionException {
		mc = OtmRegistry.getMainController();
		selectedNode = mc.getGloballySelectNode();
		actOnNode = (ComponentNode) selectedNode;
		runCommand(getActionType(exEvent));
		mc.postStatus("Add Property Handler added the property.");
		return null;
	}

	// for enabled status, see GlobalSelectionTester.canAdd()
	private PropertyNodeType getActionType(ExecutionEvent exEvent) {
		PropertyNodeType actionType = PropertyNodeType.UNKNOWN;
		Event event;
		if (exEvent.getTrigger() instanceof Event) {
			event = (Event) exEvent.getTrigger();
			if (event.data instanceof PropertyNodeType) {
				actionType = (PropertyNodeType) event.data;
			}
		}
		return actionType;
	}

	private void runCommand(PropertyNodeType actionType) {
		INode.CommandType type = selectedNode.getAddCommand();
		if (selectedNode instanceof CoreObjectNode && actionType == PropertyNodeType.ROLE)
			type = INode.CommandType.ROLE;

		switch (type) {
		case ROLE:
			addRoleToNode();
			break;
		case PROPERTY:
			addProperty();
			break;
		case ENUMERATION:
			addEnumValue();
			break;
		case OPERATION:
			addOperation();
			break;
		case NONE:
		default:
			LOGGER.debug("Not Supported: Adding to " + selectedNode);
			DialogUserNotifier.openWarning("Not Supported", "Add properties not supported for this object type.");
		}
	}

	/**
	 * Add a user define role to this node. Does nothing if the node, children or siblings are not a role facet.
	 * 
	 * @param curNode
	 *            - core object or one of its facets or properties.
	 */
	public void addRoleToNode() {
		RoleFacetNode roleFacet = actOnNode.getRoleFacet();
		if (roleFacet == null)
			return;

		final SimpleNameWizard wizard = new SimpleNameWizard(new ExternalizedStringProperties("action.addRole"), 10);
		wizard.setValidator(new NewNodeNameValidator(roleFacet, wizard, "Role with same name already exists."));
		wizard.run(OtmRegistry.getActiveShell());
		if (!wizard.wasCanceled()) {
			roleFacet.addRoles(wizard.getNames());
			mc.refresh(roleFacet);
		}
	}

	public void addOperation() {
		if (!actOnNode.isService()) {
			DialogUserNotifier.openWarning("Warning", "You can add operations only to services. ");
			return;
		}
		ServiceNode svc = (ServiceNode) actOnNode;
		SimpleNameWizard wizard = new SimpleNameWizard("wizard.newOperation");
		wizard.setValidator(new NewNodeNameValidator(svc, wizard, Messages.getString("wizard.newOperation.error.name")));
		wizard.run(OtmRegistry.getActiveShell());
		if (!wizard.wasCanceled()) {
			new OperationNode(svc, wizard.getText());
			mc.refresh(svc);
		}
	}

	private void addEnumValue() {
		if (selectedNode instanceof EnumLiteralNode)
			actOnNode = (ComponentNode) selectedNode.getParent();

		if (actOnNode != null && actOnNode.isEnumeration()) {
			final SimpleNameWizard wizard = new SimpleNameWizard(new ExternalizedStringProperties("wizard.enumValue"),
					10);
			// TODO - fix and use validator - wizard.setValidator(new
			// NewNodeNameValidator(enumeration, wizard, Messages.getString("error.enumValue")));
			wizard.run(OtmRegistry.getActiveShell());
			if (!wizard.wasCanceled()) {
				for (String entry : wizard.getNames()) {
					final Node newValue = new EnumLiteralNode(actOnNode, entry);
					newValue.setLibrary(actOnNode.getLibrary());
				}
				mc.refresh(actOnNode);
			}
		} else {
			DialogUserNotifier.openWarning("Warning", "New values can only be added to Enumeration Objects");
		}
	}

	private void addProperty() {
		if (selectedNode.getChain() != null) {
			// If a patch, create an extension point facet to add to.
			if (selectedNode.getChain().getHead().isPatchVersion()) {
				// Will always be in a different library or else it is a ExtensionPoint facet.
				if (!selectedNode.isExtensionPointFacet()) {
					if (selectedNode.getLibrary() != selectedNode.getChain().getHead()) {
						if (!DialogUserNotifier.openConfirm(Messages.getString("action.component.version.title"),
								Messages.getString("action.component.version.patch")))
							return;
						actOnNode = ((ComponentNode) selectedNode).createPatchVersionComponent();
					}
				}
			}

			// If a major minor version, create a new object of same type and add base link to this.
			if (selectedNode.getChain().getHead().isMinorOrMajorVersion()) {
				if (selectedNode.getLibrary() != selectedNode.getChain().getHead()) {
					if (!DialogUserNotifier.openConfirm(Messages.getString("action.component.version.title"),
							Messages.getString("action.component.version.minor")))
						return;
					actOnNode = ((ComponentNode) selectedNode).createMinorVersionComponent();
					if (actOnNode == null) {
						LOGGER.error("Did not create Minor Version Component for " + selectedNode);
						return;
					}
				}
			}
		}

		try {
			NewPropertiesWizard2 w2 = new NewPropertiesWizard2(selectedNode);
			w2.setValidator(new NewPropertyValidator(actOnNode, null));
			w2.run(OtmRegistry.getActiveShell());
		} catch (IllegalArgumentException e) {
			LOGGER.error("ERROR: " + e);
		}
		mc.refresh(actOnNode.getOwningComponent());
	}

	@Override
	public String getID() {
		return COMMAND_ID;
	}

	public static ImageDescriptor getIcon() {
		return Images.getImageRegistry().getDescriptor(Images.AddNode);
	}

}
