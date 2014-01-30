
package org.opentravel.schemas.actions;

import java.util.ArrayList;
import java.util.List;

import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.node.INode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.properties.ExternalizedStringProperties;
import org.opentravel.schemas.properties.StringProperties;
import org.opentravel.schemas.stl2developer.MainWindow;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.types.PostTypeChange;
import org.opentravel.schemas.types.TypeNode;
import org.opentravel.schemas.wizards.TypeSelectionWizard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Assign type to navigator selected nodes. Wizard is run to allow the user to select the type to
 * assign. If a selected node is a TypeNode, the users of that type will be replaced.
 * 
 * TODO - move execute to a handler TODO - use AbstractAction's main window TODO - move the
 * performFinish code from the wizard to here and make public. TODO - make mainWindow=null safe
 * 
 * @author Dave Hollander
 * 
 */
public class AssignTypeAction extends OtmAbstractAction {
    private static final Logger LOGGER = LoggerFactory.getLogger(AssignTypeAction.class);
    private static StringProperties propsDefault = new ExternalizedStringProperties(
            "action.replaceUsers");

    private MainController mc;

    /**
	 *
	 */
    public AssignTypeAction(final MainWindow mainWindow) {
        super(mainWindow, propsDefault);
        mc = OtmRegistry.getMainController();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        LOGGER.debug("Replace starting.");
        typeSelector();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.actions.IWithNodeAction.AbstractWithNodeAction#isEnabled()
     */
    @Override
    public boolean isEnabled() {
        Node n = getMainController().getCurrentNode_NavigatorView();
        return n.getChain() == null ? n.isEditable() : n.getChain().isMajor();
    }

    // TODO - why is this static???
    public static boolean execute(List<Node> toChange, Node newType) {
        if (newType == null || !newType.isTypeProvider()) {
            LOGGER.warn("No type to assign. Early Exit.");
            return false;
        }
        if (toChange == null || toChange.size() <= 0) {
            LOGGER.warn("Nothing to assign to. Early Exit.");
            return false;
        }
        Node last = null;
        boolean ret = true;
        for (Node cn : toChange) {
            // TODO - this can be very slow when doing 150+ nodes.
            // because the setAssigned refreshes the display
            if (!cn.setAssignedType(newType, false)) {
                ret = false;
            } else {
                PostTypeChange.notyfications(cn, newType);
            }
            if (last != null && cn.getParent() != last.getParent()) {
                last = cn;
                OtmRegistry.getNavigatorView().refresh(last.getParent());
            }
        }
        OtmRegistry.getMainController().refresh();

        LOGGER.debug("Assigned " + newType.getName() + " to " + toChange.size() + " nodes.");
        return ret;
    }

    private void typeSelector() {
        List<Node> users = new ArrayList<Node>();

        List<Node> selections = getMainController().getSelectedNodes_NavigatorView();
        if (selections != null)
            for (Node s : selections) {
                if (s instanceof TypeNode)
                    addTypeUsers(s, users); // get the users of the node, not just the node.
                else
                    users.add(s);
            }

        // runSetTypeWizard(OtmRegistry.getActiveShell(), users );
        final TypeSelectionWizard wizard = new TypeSelectionWizard(new ArrayList<Node>(users));
        if (wizard.run(OtmRegistry.getActiveShell())) {
            execute(wizard.getList(), wizard.getSelection());
        }

        mc.refresh(users.get(0));
    }

    private void addTypeUsers(INode n, List<Node> users) {
        if (n instanceof TypeNode) {
            TypeNode tn = (TypeNode) n;
            if (tn.isUser()) {
                users.add(tn.getParent()); // This is a type node for a specific type user.
            } else {
                // This is a type node for the type provider.
                users.addAll(tn.getParent().getTypeClass().getComponentUsers());
            }
        }

    }

}
