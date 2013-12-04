/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.actions;

import com.sabre.schemas.properties.StringProperties;
import com.sabre.schemas.stl2developer.MainWindow;

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
