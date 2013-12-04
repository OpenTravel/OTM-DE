/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.actions;

import org.eclipse.swt.widgets.Button;

import com.sabre.schemas.node.Node;
import com.sabre.schemas.properties.StringProperties;
import com.sabre.schemas.stl2developer.MainWindow;
import com.sabre.schemas.stl2developer.OtmRegistry;

/**
 * @author Dave Hollander
 * 
 */
public class ExtendableAction extends OtmAbstractAction {

    public ExtendableAction(final MainWindow mainWindow, final StringProperties props) {
        super(mainWindow, props, AS_CHECK_BOX);
    }

    public ExtendableAction(final MainWindow mainWindow, final StringProperties props,
            final Button check) {
        super(mainWindow, props, AS_CHECK_BOX);
    }

    @Override
    public void run() {
        Node node = mc.getSelectedNode_TypeView();
        Node nn = node;
        if (node != null) {
            nn = node.setExtensible(isChecked());
        }
        mc.refresh(nn);
        OtmRegistry.getNavigatorView().refresh(); // refresh entire tree because content changed
    }

    @Override
    public boolean isEnabled(Node currentNode) {
        if (currentNode == null)
            return false;
        if (currentNode.isMessage())
            return true;
        return (currentNode.isExtensibleObject());
    }

}
