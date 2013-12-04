/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.actions;

import com.sabre.schemas.node.INode;
import com.sabre.schemas.properties.StringProperties;
import com.sabre.schemas.stl2developer.MainWindow;

/**
 * @author Agnieszka Janowska
 * 
 */
public class SaveModelAction extends OtmAbstractAction {

    /**
	 *
	 */
    public SaveModelAction(final MainWindow mainWindow, final StringProperties props) {
        super(mainWindow, props);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        final INode modelNode = mc.getModelNode();
        if (modelNode != null) {
            mc.getModelController().saveModel(modelNode);
        }
    }

}
