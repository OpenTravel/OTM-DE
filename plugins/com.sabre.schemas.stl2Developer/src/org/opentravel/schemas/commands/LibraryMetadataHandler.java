/**
 * 
 */
package org.opentravel.schemas.commands;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NOT USED - TODO - refactor out of OtmActions.
 * 
 * @author Dave Hollander
 * 
 */
public class LibraryMetadataHandler extends OtmAbstractHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(LibraryMetadataHandler.class);
    public static String COMMAND_ID = "org.opentravel.schemas.commands.LibraryMetadata";

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        // TODO Auto-generated method stub

        // final OtmView view = OtmRegistry.getLibraryView();
        // if (view == null) return null;
        //
        // final LibraryNode lib = ((Node) view.getCurrentNode()).getLibrary();
        // final NamespaceHandler nsHandler = NamespaceHandler.getNamespaceForModel((ModelNode)
        // Node.getModelNode());
        //
        // // Handle the button(s)
        // if (ed.getWidget() instanceof Button) {
        // Node.getModelNode().setDefaultLibrary(lib);
        // return null;
        // }
        //
        // // Handle the text fields.
        // final Text w = (Text) ed.getWidget();
        // if (view != null) {
        // if (ed.getLabel().equals(Messages.getString("OtmW.334"))) {
        // lib.setName(w.getText());
        // } else if (ed.getLabel().equals(Messages.getString("OtmW.336"))) {
        // mainController.triggerNamespaceChange(lib, w.getText());
        // } else if (ed.getLabel().equals(Messages.getString("OtmW.338"))) {
        // nsHandler.setNamespacePrefix(lib.getNamespace(), w.getText());
        // } else if (ed.getLabel().equals(Messages.getString("OtmW.340"))) {
        // lib.setVersion(w.getText());
        // } else if (ed.getLabel().equals(Messages.getString("OtmW.342"))) {
        // lib.setPath(w.getText());
        // }
        // mainController.getDefaultView().refreshAllViews(lib);
        // }
        // }
        //
        return null;
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
