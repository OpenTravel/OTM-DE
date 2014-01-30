/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemas.actions;

import org.opentravel.schemas.properties.ExternalizedStringProperties;
import org.opentravel.schemas.properties.StringProperties;
import org.opentravel.schemas.stl2developer.DialogUserNotifier;
import org.opentravel.schemas.stl2developer.MainWindow;

/**
 * @author Dave Hollander
 * 
 */
public class CloseModelAction extends OtmAbstractAction {
    private static StringProperties propDefault = new ExternalizedStringProperties(
            "action.closeModel");

    public CloseModelAction() {
        super(propDefault);
    }

    public CloseModelAction(final MainWindow mainWindow, final StringProperties props) {
        super(mainWindow, props);
    }

    @Override
    public void run() {
        boolean okey = false;
        okey = DialogUserNotifier.openConfirm("Close Model",
                "Are you sure you want to close existing model? "
                        + "Closing will save and close all the currently open libraries");
        if (okey)
            mc.getModelController().close();
        mc.refresh();
    }
}
