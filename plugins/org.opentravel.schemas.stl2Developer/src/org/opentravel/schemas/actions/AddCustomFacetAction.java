/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemas.actions;

import org.opentravel.schemas.node.BusinessObjectNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.properties.ExternalizedStringProperties;
import org.opentravel.schemas.properties.StringProperties;

/**
 * @author Agnieszka Janowska
 * 
 */
public class AddCustomFacetAction extends OtmAbstractAction {
    private static StringProperties propsDefault = new ExternalizedStringProperties(
            "action.addCustom");

    /**
	 *
	 */
    public AddCustomFacetAction() {
        super(propsDefault);
    }

    public AddCustomFacetAction(final StringProperties props) {
        super(props);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        mc.addCustomFacet();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#isEnabled()
     */
    @Override
    public boolean isEnabled() {
        // Unmanged or in the most current (head) library in version chain.
        Node n = mc.getCurrentNode_NavigatorView().getOwningComponent();
        return n instanceof BusinessObjectNode ? n.isEditable() && n.isNewToChain() : false;
    }

}
