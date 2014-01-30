
package org.opentravel.schemas.actions;

import org.opentravel.schemas.node.AliasNode;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.properties.ExternalizedStringProperties;
import org.opentravel.schemas.properties.Messages;
import org.opentravel.schemas.properties.StringProperties;
import org.opentravel.schemas.stl2developer.DialogUserNotifier;
import org.opentravel.schemas.stl2developer.MainWindow;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.wizards.NewNodeNameValidator;
import org.opentravel.schemas.wizards.SimpleNameWizard;

/**
 * @author Agnieszka Janowska
 * 
 */
public class AddAliasAction extends OtmAbstractAction {
    private static StringProperties propsDefault = new ExternalizedStringProperties(
            "action.addAlias");

    /**
	 *
	 */
    public AddAliasAction(final MainWindow mainWindow) {
        super(mainWindow, propsDefault);
    }

    public AddAliasAction(final MainWindow mainWindow, final StringProperties props) {
        super(mainWindow, props);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        addAlias();
    }

    @Override
    public boolean isEnabled() {
        Node n = getMainController().getCurrentNode_NavigatorView().getOwningComponent();
        if (n.isAliasable())
            return n.getChain() == null ? n.isEditable() : n.getChain().isMajor();
        return false;
    }

    public void addAlias() {
        Node current = mc.getCurrentNode_NavigatorView();
        current = current.getOwningComponent();
        if (current != null && (current.isAliasable())) {
            final SimpleNameWizard wizard = new SimpleNameWizard(new ExternalizedStringProperties(
                    "wizard.aliasName"));
            final ComponentNode cn = (ComponentNode) current;
            wizard.setValidator(new NewNodeNameValidator(cn, wizard, Messages
                    .getString("error.aliasName")));
            wizard.run(OtmRegistry.getActiveShell());
            if (!wizard.wasCanceled()) {
                new AliasNode(current, wizard.getText());
                mc.refresh(current);
            }
        } else {
            DialogUserNotifier
                    .openWarning("Warning",
                            "New alias cannot be added because aliases are only for Business and Core Objects");
        }
    }

}
