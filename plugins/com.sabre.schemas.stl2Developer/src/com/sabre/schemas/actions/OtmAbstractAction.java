/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.actions;

import com.sabre.schemas.controllers.MainController;
import com.sabre.schemas.properties.StringProperties;
import com.sabre.schemas.stl2developer.MainWindow;
import com.sabre.schemas.stl2developer.OtmRegistry;

/**
 * Establish an action with window context and string properties.
 * 
 * @param mainWindow
 * @param props
 *            - user implementations such as ExternalizedStringProperties() to establish string
 *            values for PropertyType.TEXT, PropertyType.TOOLTIP, PropertyType.IMAGE as saved in
 *            messages.properties and in the image registry.
 * 
 *            Example: new AddEnumValueAction(mainWindow, new
 *            ExternalizedStringProperties("action.addEnumValue"));
 * 
 * @author Agnieszka Janowska
 * 
 */
public class OtmAbstractAction extends OtmAbstractBaseAction {

    private final MainWindow mainWindow;
    protected final MainController mc;

    public OtmAbstractAction(final MainWindow mainWindow, final StringProperties props) {
        super(props);
        this.mainWindow = mainWindow;
        mc = OtmRegistry.getMainController();
    }

    public OtmAbstractAction(final StringProperties props) {
        super(props);
        this.mainWindow = OtmRegistry.getMainWindow();
        mc = OtmRegistry.getMainController();
    }

    public OtmAbstractAction(final MainWindow mainWindow, final StringProperties props,
            final int style) {
        super(props, style);
        this.mainWindow = mainWindow;
        mc = OtmRegistry.getMainController();
    }

    public OtmAbstractAction(MainWindow mainWindow) {
        super();
        this.mainWindow = mainWindow;
        mc = OtmRegistry.getMainController();
    }

    public MainWindow getMainWindow() {
        return mainWindow;
    }

    public MainController getMainController() {
        return mc;
    }

}
