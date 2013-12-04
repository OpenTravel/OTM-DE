/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sabre.schemas.node.Node;
import com.sabre.schemas.properties.ExternalizedStringProperties;
import com.sabre.schemas.properties.StringProperties;
import com.sabre.schemas.stl2developer.MainWindow;

/**
 * @author Dave Hollander
 * 
 */
public class CloneSelectedFacetNodesAction extends OtmAbstractAction implements IWithNodeAction {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(CloneSelectedFacetNodesAction.class);
    private static StringProperties propDefault = new ExternalizedStringProperties("action.copy");

    public CloneSelectedFacetNodesAction() {
        super(propDefault);
    }

    public CloneSelectedFacetNodesAction(final MainWindow mainWindow, final StringProperties props) {
        super(mainWindow, props);
    }

    @Override
    public void run() {
        getMainController().cloneSelectedFacetNodes();
    }

    @Override
    public boolean isEnabled(Node node) {
        if (node == null)
            return false;
        // TODO: only example. replace with some logic.
        return node.isSimpleFacet();
    }

}
