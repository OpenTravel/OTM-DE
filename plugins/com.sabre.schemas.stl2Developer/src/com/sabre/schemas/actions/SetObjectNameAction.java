/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.actions;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sabre.schemas.node.Node;
import com.sabre.schemas.properties.ExternalizedStringProperties;
import com.sabre.schemas.properties.StringProperties;
import com.sabre.schemas.stl2developer.MainWindow;

/**
 * This is a prototype for migrating actions out of the OtmActions. At this point, i use OtmActions
 * because it has the text handlers. TODO - redo text handlers to support direct action dispatch.
 * 
 * @author Dave Hollander
 * 
 */
public class SetObjectNameAction extends OtmAbstractAction {
    private static final Logger LOGGER = LoggerFactory.getLogger(SetObjectNameAction.class);

    private final static StringProperties propsDefault = new ExternalizedStringProperties(
            "action.setName");

    public SetObjectNameAction(final MainWindow mainWindow) {
        super(mainWindow, propsDefault);
    }

    /**
	 *
	 */
    public SetObjectNameAction(final MainWindow mainWindow, final StringProperties props) {
        super(mainWindow, props);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#runWithEvent(org.eclipse.swt.widgets.Event)
     */
    @Override
    public void runWithEvent(Event event) {
        String newName = "";
        if (event.widget instanceof Text)
            newName = ((Text) event.widget).getText();
        final Node n = (Node) getMainController().getCurrentNode_TypeView();
        if (n != null) {
            n.setName(newName);
            getMainController().refresh();
        }
        // Now - make sure all the details are right!!!
        LOGGER.debug("Changed name to " + n.getName());
    }
}
