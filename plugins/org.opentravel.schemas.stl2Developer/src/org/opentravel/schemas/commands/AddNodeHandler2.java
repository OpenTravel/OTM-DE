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
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.ServiceNode;
import org.opentravel.schemas.node.facets.FacetNode;
import org.opentravel.schemas.node.facets.OperationNode;
import org.opentravel.schemas.node.facets.RoleFacetNode;
import org.opentravel.schemas.node.interfaces.Enumeration;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.properties.EnumLiteralNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.properties.PropertyNodeType;
import org.opentravel.schemas.properties.ExternalizedStringProperties;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.properties.Messages;
import org.opentravel.schemas.stl2developer.DialogUserNotifier;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.wizards.NewPropertiesWizard2;
import org.opentravel.schemas.wizards.SimpleNameWizard;
import org.opentravel.schemas.wizards.validators.NewNodeNameValidator;
import org.opentravel.schemas.wizards.validators.NewPropertyValidator;

/**
 * Command Handler for the add a node to the model command. Handler for adding when a component node is selected.
 * 
 * Version 2 uses the NewPropertiesWizard2 that acts upon the currently selected object.
 * 
 * Handles action: org.opentravel.schemas.commands.AddProperties
 * 
 * @author Dave Hollander
 *
 */
public class AddNodeHandler2 extends OtmAbstractHandler {
	// private static final Logger LOGGER = LoggerFactory.getLogger(AddNodeHandler2.class);
	public static String COMMAND_ID = "org.opentravel.schemas.commands.Add";

	private Node selectedNode; // The user selected node.
	private ComponentNode actOnNode; // The node to perform the action on.

	public void execute(Event event) {
		mc = OtmRegistry.getMainController();
		selectedNode = mc.getGloballySelectNode();
		actOnNode = (ComponentNode) selectedNode;
		if (event.data instanceof ComponentNode)
			runCommand(((ComponentNode) event.data));
		else
			runCommand(getActionType(event));
		mc.postStatus("Add Property Handler added the property.");
	}

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
			actionType = getActionType(event);
		}
		return actionType;
	}

	private PropertyNodeType getActionType(Event event) {
		if (event.data instanceof PropertyNodeType) {
			return (PropertyNodeType) event.data;
		}
		return null;
	}

	/**
	 * Run add command to add property to target. if needed, create minor version of object; add property; set mandatory
	 * if needed
	 * 
	 * Global selectedNode is object to use as type to assign to property; if DND this is the dragged object.
	 * 
	 * @param targetNode
	 *            - property, facet or object to add property to, if DND this is the drop target
	 */
	private void runCommand(ComponentNode targetNode) {
		// Property will be added to newNode. newNode will be changed if new version is needed.
		ComponentNode newNode = targetNode;

		// if needed, create minor version of object
		if (targetNode.getChain() != null) {
			if (targetNode.isEnabled_AddProperties() && !targetNode.isInHead())
				newNode = createVersionExtension(targetNode.getOwningComponent());
			if (newNode == null)
				return; // they did a cancel

			// Find same facet in new object as they dropped onto
			if (targetNode == targetNode.getOwningComponent())
				newNode = (ComponentNode) newNode.getDefaultFacet();
			else {
				// if dropped on property, match owning facet
				if (targetNode instanceof PropertyNode)
					targetNode = (ComponentNode) targetNode.getParent();

				// find matching facet
				if (targetNode instanceof FacetNode)
					for (Node n : newNode.getChildren())
						if (n.getName().equals(targetNode.getName())) {
							newNode = (ComponentNode) n;
							break;
						}
			}
		}
		if (newNode == null) {
			// LOGGER.debug("newNode is null.");
			return; // Error
		}

		// add property - new node must be property owner
		newNode = (ComponentNode) newNode.createProperty(selectedNode);

		if (newNode == null) {
			// LOGGER.debug("newNode is null.");
			return; // Error
		}

		// make summary facet properties default to mandatory unless in minor version
		FacetNode owningFacet = (FacetNode) newNode.getParent();
		if (owningFacet.isSummaryFacet() && !newNode.getLibrary().isMinorVersion())
			newNode.setMandatory(true);

		mc.refresh(newNode);
		OtmRegistry.getNavigatorView().refresh();
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
			// LOGGER.debug("Not Supported: Adding to " + selectedNode);
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
		if (actOnNode == null || !(actOnNode instanceof CoreObjectNode))
			return; // should this post status or dialog?
		RoleFacetNode roleFacet = ((CoreObjectNode) actOnNode).getRoleFacet();
		if (roleFacet == null)
			return;

		// If GUI allows adding roles, but the node is not in the head library
		if (actOnNode.isEnabled_AddProperties() && !actOnNode.isInHead2()) {
			actOnNode = createVersionExtension(actOnNode);
			if (actOnNode == null)
				return; // should this post status or dialog?
			roleFacet = ((CoreObjectNode) actOnNode).getRoleFacet();
		}

		final SimpleNameWizard wizard = new SimpleNameWizard(new ExternalizedStringProperties("action.addRole"), 10);
		wizard.setValidator(new NewNodeNameValidator(roleFacet, wizard, "Role with same name already exists."));
		wizard.run(OtmRegistry.getActiveShell());
		if (!wizard.wasCanceled()) {
			roleFacet.addRoles(wizard.getNames());
			mc.refresh(roleFacet);
		}
	}

	public void addOperation() {
		if (!(actOnNode instanceof ServiceNode)) {
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

		// Create a minor version if needed.
		if (selectedNode.getChain() != null) {
			if (actOnNode.isEnabled_AddProperties() && !actOnNode.isInHead())
				actOnNode = createVersionExtension(selectedNode);
			if (actOnNode == null)
				return;
		}

		if (actOnNode != null && actOnNode instanceof Enumeration) {
			final SimpleNameWizard wizard = new SimpleNameWizard(new ExternalizedStringProperties("wizard.enumValue"),
					10);
			// TODO - fix and use validator - wizard.setValidator(new
			// NewNodeNameValidator(enumeration, wizard, Messages.getString("error.enumValue")));
			wizard.run(OtmRegistry.getActiveShell());
			if (!wizard.wasCanceled()) {
				for (String entry : wizard.getNames()) {
					((Enumeration) actOnNode).addLiteral(entry);
				}
				mc.refresh(actOnNode);
			}
		} else {
			DialogUserNotifier.openWarning("Warning", "New values can only be added to Enumeration Objects");
		}
	}

	private void addProperty() {
		if (selectedNode.getChain() != null) {
			if (actOnNode.isEnabled_AddProperties() && !actOnNode.isInHead())
				actOnNode = createVersionExtension(selectedNode);
		}
		if (actOnNode == null) {
			// LOGGER.debug("Error adding property, actOnNode is null.");
			return;
		}

		try {
			NewPropertiesWizard2 w2 = new NewPropertiesWizard2(actOnNode);
			w2.setValidator(new NewPropertyValidator(actOnNode, null));
			w2.run(OtmRegistry.getActiveShell());
		} catch (IllegalArgumentException e) {
			DialogUserNotifier.openError("Add properties error.", e.getLocalizedMessage());
			// LOGGER.error("ERROR: " + e);
		}
		mc.refresh(actOnNode.getOwningComponent());
	}

	@Override
	public String getID() {
		return COMMAND_ID;
	}

	protected Node getSelectedNode(ExecutionEvent exEvent) {
		return mc.getGloballySelectNode();
	}

	public static ImageDescriptor getIcon() {
		return Images.getImageRegistry().getDescriptor(Images.AddNode);
	}

}
