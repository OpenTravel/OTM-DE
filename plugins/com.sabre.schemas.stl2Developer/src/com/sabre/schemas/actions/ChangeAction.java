/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.actions;

import com.sabre.schemas.node.Node;
import com.sabre.schemas.properties.ExternalizedStringProperties;
import com.sabre.schemas.properties.StringProperties;
import com.sabre.schemas.stl2developer.MainWindow;

/**
 * @author Agnieszka Janowska
 * 
 */
public class ChangeAction extends OtmAbstractAction {
    private final static StringProperties propsDefault = new ExternalizedStringProperties(
            "action.changeObject");

    /**
	 *
	 */
    public ChangeAction(final MainWindow mainWindow) {
        super(mainWindow, propsDefault);
    }

    public ChangeAction(final MainWindow mainWindow, final StringProperties props) {
        super(mainWindow, props);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        getMainController().changeTreeSelection();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#isEnabled()
     */
    @Override
    public boolean isEnabled() {
        Node n = getMainController().getCurrentNode_NavigatorView().getOwningComponent();
        if (n.isBusinessObject())
            return true;
        if (n.isCoreObject())
            return true;
        if (n.isValueWithAttributes())
            return true;
        return false;
    }

}
