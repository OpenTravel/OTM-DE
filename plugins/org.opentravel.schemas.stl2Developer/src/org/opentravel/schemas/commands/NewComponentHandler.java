
package org.opentravel.schemas.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.node.ComponentNodeType;
import org.opentravel.schemas.node.EditNode;
import org.opentravel.schemas.node.LibraryNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeNameUtils;
import org.opentravel.schemas.node.ServiceNode;
import org.opentravel.schemas.stl2developer.DialogUserNotifier;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.wizards.NewComponentWizard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Pawel Jedruch
 * 
 */
public class NewComponentHandler extends AbstractHandler {

    public static final String COMMAND_ID = "org.opentravel.schemas.commands.newComponent";
    private static final Logger LOGGER = LoggerFactory.getLogger(NewComponentHandler.class);

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        newToLibrary();
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.commands.AbstractHandler#isEnabled()
     */
    // See NodeTester
    // @Override
    // public boolean isEnabled() {
    // Node n = OtmRegistry.getMainController().getCurrentNode_NavigatorView();
    // return n.isEditable();
    // }

    /**
     * Runs new component wizard and creates new node with TL type. Used by New Complex Type Action
     * handler.
     */
    public void newToLibrary() {
        MainController mc = OtmRegistry.getMainController();
        // LOGGER.debug("Adding new component to library");

        final Node selected = mc.getSelectedNode_NavigatorView();

        if (selected == null) {
            LOGGER.debug("No component selected - cannot add new object");
            DialogUserNotifier.openInformation("WARNING", "You must select a library to add to.");
            return;
        }
        if (!selected.isInTLLibrary()) {
            DialogUserNotifier.openInformation("WARNING", "You can only add to a model library.");
            return;
        }

        final NewComponentWizard wizard = new NewComponentWizard(selected);
        EditNode editNode = null;

        editNode = wizard.postNewComponentWizard(OtmRegistry.getActiveShell());
        if (editNode != null) {
            ComponentNodeType type = ComponentNodeType.fromString(editNode.getUseType());

            Node newOne = editNode.newComponent(type);

            if (editNode.getTLType() != null && newOne instanceof ServiceNode) {
                ((ServiceNode) newOne).addCRUDQ_Operations(editNode.getTLType());
            }
            newOne.setName(NodeNameUtils.fixComplexTypeName(newOne.getName()));
            mc.selectNavigatorNodeAndRefresh(newOne);
        }
    }
}
