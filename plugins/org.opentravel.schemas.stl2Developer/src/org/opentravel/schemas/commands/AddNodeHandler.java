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

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Event;
import org.opentravel.schemas.node.BusinessObjectNode;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.CoreObjectNode;
import org.opentravel.schemas.node.FacetNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.OperationNode;
import org.opentravel.schemas.node.PropertyNodeType;
import org.opentravel.schemas.node.RoleFacetNode;
import org.opentravel.schemas.node.ServiceNode;
import org.opentravel.schemas.node.VWA_Node;
import org.opentravel.schemas.node.properties.EnumLiteralNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.properties.ExternalizedStringProperties;
import org.opentravel.schemas.properties.Messages;
import org.opentravel.schemas.stl2developer.DialogUserNotifier;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.wizards.NewNodeNameValidator;
import org.opentravel.schemas.wizards.NewPropertiesWizard;
import org.opentravel.schemas.wizards.NewPropertyValidator;
import org.opentravel.schemas.wizards.SimpleNameWizard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for the add a node to the model command.
 * Handler for adding when a component node is selected.
 * 
 * Handles action: org.opentravel.schemas.commands.AddProperties

 * @author Dave Hollander
 *
 */
/**
 * TODO - Move responsibility for deciding on what to do to the nodes.
 * 
 */
public class AddNodeHandler extends OtmAbstractHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(AddNodeHandler.class);
	public static String COMMAND_ID = "org.opentravel.schemas.commands.Add";

	private Node selectedNode; // The user selected node.
	private ComponentNode actOnNode; // The node to perform the action on.
	private Node scopeNode; // The node to seed the selection tree with.

	// Enumeration of the types of nodes this handler can add.
	private enum CommandType {
		PROPERTY, ROLE, LIBRARY, ATTRIBUTE, ENUMERATION, QUERY, CUSTOM, OPERATION, NONE, COMPONENT
	}

	private final List<PropertyNodeType> enabledTypes = new ArrayList<PropertyNodeType>();
	private Event event;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent exEvent) throws ExecutionException {
		mc = OtmRegistry.getMainController();

		selectedNode = getSelectedNode(exEvent);

		PropertyNodeType actionType = getActionType(exEvent);
		CommandType type = decideWhatToAdd(selectedNode, actionType);
		runCommand(type);
		mc.postStatus("Add Property Handler added the property.");
		return null;
	}

	// for enabled status, see GlobalSelectionTester.canAdd()

	private PropertyNodeType getActionType(ExecutionEvent exEvent) {
		PropertyNodeType actionType = PropertyNodeType.UNKNOWN;
		if (exEvent.getTrigger() instanceof Event) {
			this.event = (Event) exEvent.getTrigger();
			if (event.data instanceof PropertyNodeType) {
				actionType = (PropertyNodeType) event.data;
			}
		}
		return actionType;
	}

	protected Node getSelectedNode(ExecutionEvent exEvent) {
		return mc.getGloballySelectNode();
	}

	private void runCommand(CommandType type) {
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
			LOGGER.debug("Not Supported: Add " + type + " node to " + selectedNode);
			DialogUserNotifier.openWarning("Not Supported", "Add properties not supported for this object type.");
		}
	}

	// TODO - refactor by designing a command data object and allowing the nodes to load it.
	private CommandType decideWhatToAdd(Node selectedNode, PropertyNodeType actionType) {
		CommandType type = CommandType.PROPERTY;
		if (selectedNode == null) {
			return CommandType.LIBRARY;
		}
		if (!(selectedNode instanceof ComponentNode)) {
			return CommandType.COMPONENT;
		}

		// Set the defaults - may be overridden in logic below.
		actOnNode = (ComponentNode) selectedNode;
		scopeNode = mc.getModelNode();

		// Role could be one of three signals
		if (selectedNode.isRoleFacet())
			type = CommandType.ROLE;
		else if (selectedNode.isRoleProperty()) {
			type = CommandType.ROLE;
			actOnNode = (ComponentNode) selectedNode.getParent();
		} else if (selectedNode.getOwningComponent().isCoreObject() && actionType == PropertyNodeType.ROLE) {
			type = CommandType.ROLE;
			actOnNode = ((CoreObjectNode) selectedNode.getOwningComponent()).getRoleFacet();
			//
		} else if (selectedNode.isBusinessObject()) {
			type = CommandType.PROPERTY;
			actOnNode = ((BusinessObjectNode) selectedNode).getSummaryFacet();
		} else if (selectedNode.isCoreObject()) {
			type = CommandType.PROPERTY;
			actOnNode = ((CoreObjectNode) selectedNode).getSummaryFacet();
		} else if (selectedNode.isValueWithAttributes()) {
			actOnNode = ((ComponentNode) selectedNode).getDefaultFacet();
		} else if (selectedNode.isFacet()) {
			type = CommandType.PROPERTY;
			//
		} else if (selectedNode.isExtensionPointFacet()) {
			type = CommandType.PROPERTY;
			//
		} else if (selectedNode.isSimpleFacet()) {
			actOnNode = ((ComponentNode) selectedNode.getParent()).getDefaultFacet();
		} else if (selectedNode.isQueryFacet()) {
			type = CommandType.QUERY;
			scopeNode = selectedNode.getOwningComponent();
		} else if (selectedNode.isCustomFacet()) {
			type = CommandType.CUSTOM;
			scopeNode = ((BusinessObjectNode) selectedNode.getOwningComponent()).getDetailFacet();
			//
		} else if (selectedNode.isEnumeration())
			type = CommandType.ENUMERATION;
		else if (selectedNode.isEnumerationLiteral()) {
			actOnNode = (ComponentNode) selectedNode.getParent();
			type = CommandType.ENUMERATION;
		} else if (selectedNode.isProperty()) {
			actOnNode = (ComponentNode) selectedNode.getParent();
		} else if (selectedNode.isService()) {
			type = CommandType.OPERATION;
		} else {
			type = CommandType.NONE;
		}

		enabledTypes.clear();
		if (selectedNode.isSimpleFacet() || selectedNode.getOwningComponent().isValueWithAttributes())
			enabledTypes.addAll(PropertyNodeType.getVWA_PropertyTypes());
		else
			enabledTypes.addAll(PropertyNodeType.getAllTypedPropertyTypes());
		return type;
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
		if (actOnNode != null && actOnNode.isEnumeration()) {
			final SimpleNameWizard wizard = new SimpleNameWizard(new ExternalizedStringProperties("wizard.enumValue"),
					10);
			// TODO - fix and use validator - wizard.setValidator(new
			// NewNodeNameValidator(enumeration, wizard, Messages.getString("error.enumValue")));
			wizard.run(OtmRegistry.getActiveShell());
			if (!wizard.wasCanceled()) {
				for (String entry : wizard.getNames()) {
					final Node newValue = new EnumLiteralNode(actOnNode, entry);
					// final Node newValue = new PropertyNode(actOnNode, entry,
					// PropertyNodeType.ENUM_LITERAL);
					newValue.setLibrary(actOnNode.getLibrary());
				}
				mc.refresh(actOnNode);
			}
		} else {
			DialogUserNotifier.openWarning("Warning", "New values can only be added to Enumeration Objects");
		}
	}

	private void addProperty() {
		ComponentNode newNode = null;
		if (selectedNode.getChain() != null) {
			// If a patch, create an extension point facet to add to.
			if (selectedNode.getChain().getHead().isPatchVersion()) {
				// Will always be in a different library or else it is a ExtensionPoint facet.
				if (!selectedNode.isExtensionPointFacet()) {
					if (selectedNode.getLibrary() != selectedNode.getChain().getHead()) {
						if (!DialogUserNotifier.openConfirm(Messages.getString("action.component.version.title"),
								Messages.getString("action.component.version.patch")))
							return;
						newNode = ((ComponentNode) selectedNode).createPatchVersionComponent();
						actOnNode = newNode;
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
					// selectedNode = actOnNode.getOwningComponent();
				}
			}
		}

		// Match the actual selected facet by matching names
		if (selectedNode instanceof FacetNode) {
			for (Node n : actOnNode.getChildren()) {
				if (n.getName().equals(selectedNode.getName()))
					actOnNode = (ComponentNode) n;
			}
		} else {
			// use default facet to act upon
			if (actOnNode instanceof BusinessObjectNode) {
				actOnNode = actOnNode.getSummaryFacet();
			} else if (actOnNode instanceof CoreObjectNode) {
				actOnNode = actOnNode.getSummaryFacet();
			} else if (actOnNode instanceof VWA_Node) {
				actOnNode = actOnNode.getDefaultFacet();
			}
		}
		if (!(actOnNode instanceof FacetNode))
			throw new IllegalStateException("Must have a facet to add properties.");

		NewPropertiesWizard newPropertiesWizard = new NewPropertiesWizard(actOnNode.getLibrary(), scopeNode,
				enabledTypes);
		newPropertiesWizard.setValidator(new NewPropertyValidator(actOnNode, newPropertiesWizard));
		newPropertiesWizard.run(OtmRegistry.getActiveShell());

		if (!newPropertiesWizard.wasCanceled()) {
			List<PropertyNode> newProperties = newPropertiesWizard.getNewProperties();
			Node lastOne = null;
			// New nodes are not connected to the parent.
			for (final PropertyNode n : newProperties) {
				actOnNode.addProperty(n);
				if (actOnNode.getLibrary().isMinorVersion())
					n.setMandatory(false); // properties in minor extensions must be optional.
				lastOne = n;
			}
			if (lastOne != null) {
				mc.selectNavigatorNodeAndRefresh(lastOne);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.opentravel.schemas.commands.OtmHandler#getID()
	 */
	@Override
	public String getID() {
		return COMMAND_ID;
	}

}
