/**
 * 
 */
package com.sabre.schemas.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sabre.schemas.node.BusinessObjectNode;
import com.sabre.schemas.node.ComponentNode;
import com.sabre.schemas.node.CoreObjectNode;
import com.sabre.schemas.node.Node;
import com.sabre.schemas.node.OperationNode;
import com.sabre.schemas.node.PropertyNodeType;
import com.sabre.schemas.node.RoleFacetNode;
import com.sabre.schemas.node.ServiceNode;
import com.sabre.schemas.node.properties.EnumLiteralNode;
import com.sabre.schemas.node.properties.PropertyNode;
import com.sabre.schemas.properties.ExternalizedStringProperties;
import com.sabre.schemas.properties.Messages;
import com.sabre.schemas.stl2developer.DialogUserNotifier;
import com.sabre.schemas.stl2developer.OtmRegistry;
import com.sabre.schemas.wizards.NewNodeNameValidator;
import com.sabre.schemas.wizards.NewPropertiesWizard;
import com.sabre.schemas.wizards.NewPropertyValidator;
import com.sabre.schemas.wizards.SimpleNameWizard;

/**
 * Handler for the add a node to the model command.
 * Handler for adding when a component node is selected.
 * 
 * Handles action: com.sabre.schemas.commands.AddProperties

 * @author Dave Hollander
 *
 */
/**
 * TODO - Move responsibility for deciding on what to do to the nodes.
 * 
 */
public class AddNodeHandler extends OtmAbstractHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(AddNodeHandler.class);
    public static String COMMAND_ID = "com.sabre.schemas.commands.Add";

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
                DialogUserNotifier.openWarning("Not Supported",
                        "Add properties not supported for this object type.");
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
        } else if (selectedNode.getOwningComponent().isCoreObject()
                && actionType == PropertyNodeType.ROLE) {
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
        if (selectedNode.isSimpleFacet()
                || selectedNode.getOwningComponent().isValueWithAttributes())
            enabledTypes.addAll(Arrays.asList(PropertyNodeType.ATTRIBUTE,
                    PropertyNodeType.INDICATOR));
        else
            enabledTypes
                    .addAll(Arrays.asList(PropertyNodeType.ELEMENT, PropertyNodeType.ATTRIBUTE,
                            PropertyNodeType.INDICATOR, PropertyNodeType.ID_REFERENCE,
                            PropertyNodeType.ID));

        return type;
    }

    /**
     * Add a user define role to this node. Does nothing if the node, children or siblings are not a
     * role facet.
     * 
     * @param curNode
     *            - core object or one of its facets or properties.
     */
    public void addRoleToNode() {
        RoleFacetNode roleFacet = actOnNode.getRoleFacet();
        if (roleFacet == null)
            return;

        final SimpleNameWizard wizard = new SimpleNameWizard(new ExternalizedStringProperties(
                "action.addRole"), 10);
        wizard.setValidator(new NewNodeNameValidator(roleFacet, wizard,
                "Role with same name already exists."));
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
        wizard.setValidator(new NewNodeNameValidator(svc, wizard, Messages
                .getString("wizard.newOperation.error.name")));
        wizard.run(OtmRegistry.getActiveShell());
        if (!wizard.wasCanceled()) {
            new OperationNode(svc, wizard.getText());
            mc.refresh(svc);
        }
    }

    private void addEnumValue() {
        if (actOnNode != null && actOnNode.isEnumeration()) {
            final SimpleNameWizard wizard = new SimpleNameWizard(new ExternalizedStringProperties(
                    "wizard.enumValue"), 10);
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
            DialogUserNotifier.openWarning("Warning",
                    "New values can only be added to Enumeration Objects");
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
                        if (!DialogUserNotifier.openConfirm(
                                Messages.getString("action.component.version.title"),
                                Messages.getString("action.component.version.patch")))
                            return;
                        newNode = ((ComponentNode) selectedNode).createPatchVersionComponent();
                        actOnNode = newNode;
                    }
                }
            }

            // If a minor version, create a new object of same type and add base link to this.
            if (selectedNode.getChain().getHead().isMinorVersion()) {
                if (selectedNode.getLibrary() != selectedNode.getChain().getHead()) {
                    if (!DialogUserNotifier.openConfirm(
                            Messages.getString("action.component.version.title"),
                            Messages.getString("action.component.version.minor")))
                        return;
                    actOnNode = ((ComponentNode) selectedNode).createMinorVersionComponent();
                    if (actOnNode == null) {
                        LOGGER.error("Did not create Minor Version Component for " + selectedNode);
                        return;
                    }
                    selectedNode = actOnNode.getOwningComponent();
                }
            }
        }

        NewPropertiesWizard newPropertiesWizard = new NewPropertiesWizard(
                selectedNode.getLibrary(), scopeNode, enabledTypes);
        newPropertiesWizard
                .setValidator(new NewPropertyValidator(selectedNode, newPropertiesWizard));
        newPropertiesWizard.run(OtmRegistry.getActiveShell());

        if (!newPropertiesWizard.wasCanceled()) {
            List<PropertyNode> newProperties = newPropertiesWizard.getNewProperties();
            Node lastOne = null;
            // New nodes are not connected to the parent.
            for (final PropertyNode n : newProperties) {
                actOnNode.addProperty(n);
                if (selectedNode.getLibrary().isMinorVersion())
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
     * @see com.sabre.schemas.commands.OtmHandler#getID()
     */
    @Override
    public String getID() {
        return COMMAND_ID;
    }

}
