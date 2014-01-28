/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemas.actions;

import org.opentravel.schemas.properties.StringProperties;
import org.opentravel.schemas.stl2developer.MainWindow;

/**
 * @author Agnieszka Janowska
 * 
 */
public class OpenInSystemEditorAction extends OtmAbstractAction {

    /**
	 *
	 */
    public OpenInSystemEditorAction(final MainWindow mainWindow, final StringProperties props) {
        super(mainWindow, props);
    }

    @Override
    public void run() {
        mc.openLibraryInSystemEditor();
    }
}
