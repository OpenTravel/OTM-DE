/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemas.actions;

import org.eclipse.swt.widgets.Event;
import org.opentravel.schemas.properties.ExternalizedStringProperties;
import org.opentravel.schemas.properties.StringProperties;
import org.opentravel.schemas.stl2developer.MainWindow;

/**
 * @author Agnieszka Janowska
 * 
 */
public class AddPropertiesToTreeSelectionAction extends OtmAbstractAction {
    private static StringProperties propDefault = new ExternalizedStringProperties(
            "action.addProperty");

    /**
     * use AddProperty to Component Action.
     */
    @Deprecated
    public AddPropertiesToTreeSelectionAction(final MainWindow mainWindow) {
        super(mainWindow, propDefault);
    }

    @Deprecated
    public AddPropertiesToTreeSelectionAction(final MainWindow mainWindow,
            final StringProperties props) {
        super(mainWindow, props);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#runWithEvent(org.eclipse.swt.widgets.Event)
     */
    @Override
    public void runWithEvent(Event event) {
        mc.runAddProperties(event);
    }

}
