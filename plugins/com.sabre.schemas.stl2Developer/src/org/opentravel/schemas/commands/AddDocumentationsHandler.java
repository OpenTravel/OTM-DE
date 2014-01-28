/**
 * 
 */
package org.opentravel.schemas.commands;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.wizards.SetDocumentationWizard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for adding documentation items to a set of nodes. Uses setDocumentation wizard to get the
 * documentation text.
 * 
 * @author Dave Hollander
 * 
 */
public class AddDocumentationsHandler extends OtmAbstractHandler {
    public static String COMMAND_ID = "org.opentravel.schemas.commands.addDocumentations";
    private static final Logger LOGGER = LoggerFactory.getLogger(AddDocumentationsHandler.class);

    @Override
    public Object execute(ExecutionEvent exEvent) throws ExecutionException {

        SetDocumentationWizard wiz = new SetDocumentationWizard();
        wiz.run(OtmRegistry.getActiveShell());
        String text = wiz.getDocText();
        Node.DocTypes docType = wiz.getDocType();

        for (Node node : mc.getSelectedNodes_NavigatorView()) {
            saveDoc(node, docType, text);
            mc.postStatus("Adding Documentation to " + node);
            // LOGGER.debug("Adding Documentation to "+node+" doc = "+wiz.getDocText());
        }
        mc.refresh();
        return null;
    }

    private void saveDoc(Node n, Node.DocTypes type, String text) {
        if (n == null || type == null || text.isEmpty())
            return;

        switch (type) {
            case Description:
                n.addDescription(text);
                break;
            case Deprecation:
                n.addDeprecated(text);
                break;
            case MoreInformation:
                n.addMoreInfo(text);
                break;
            case ReferenceLink:
                n.addReference(text);
                break;
            case Implementer:
                n.addImplementer(text);
                break;
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
