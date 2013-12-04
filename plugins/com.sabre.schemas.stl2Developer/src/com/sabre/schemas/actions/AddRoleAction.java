/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.actions;

import org.eclipse.swt.widgets.Event;

import com.sabre.schemas.node.PropertyNodeType;
import com.sabre.schemas.properties.ExternalizedStringProperties;
import com.sabre.schemas.properties.StringProperties;
import com.sabre.schemas.stl2developer.MainWindow;

/**
 * Run the addNode command to add roles to a core object.
 * 
 * @author Dave Hollander
 * 
 */
public class AddRoleAction extends OtmAbstractAction {
    private static StringProperties propDefault = new ExternalizedStringProperties("action.addRole");

    public AddRoleAction(final MainWindow mainWindow) {
        super(mainWindow, propDefault);
    }

    public AddRoleAction(final MainWindow mainWindow, final StringProperties props) {
        super(mainWindow, props);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#runWithEvent(org.eclipse.swt.widgets.Event)
     */
    @Override
    public void runWithEvent(Event event) {
        event.data = PropertyNodeType.ROLE;
        getMainController().runAddProperties(event);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#isEnabled()
     */
    @Override
    public boolean isEnabled() {
        return (getMainController().getCurrentNode_NavigatorView().getOwningComponent()
                .isCoreObject()) ? true : false;
    }

}
