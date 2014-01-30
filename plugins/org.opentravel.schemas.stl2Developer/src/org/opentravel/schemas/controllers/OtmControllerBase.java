
package org.opentravel.schemas.controllers;

import java.util.List;

import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.stl2developer.MainWindow;
import org.opentravel.schemas.views.OtmView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dave Hollander
 * 
 */
public class OtmControllerBase implements OtmController {
    private static final Logger LOGGER = LoggerFactory.getLogger(OtmControllerBase.class);

    protected static MainController mc;
    private static MainWindow mainWindow;

    protected OtmControllerBase(MainController mwc) {
        this.mc = mwc;
        mainWindow = mwc.getMainWindow();
    }

    protected MainWindow getMainWindow() {
        return mainWindow;
    }

    protected MainController getMainWindowController() {
        return mc;
    }

    protected OtmView getView() {
        return mc.getDefaultView();
    }

    @Override
    public void refresh() {
        // TODO Auto-generated method stub

    }

    @Override
    public Node getSelected() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Node> getSelections() {
        // TODO Auto-generated method stub
        return null;
    }

}
