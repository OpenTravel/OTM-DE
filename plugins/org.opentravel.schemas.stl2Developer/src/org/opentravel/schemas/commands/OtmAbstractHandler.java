/**
 * 
 */
package org.opentravel.schemas.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.stl2developer.MainWindow;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.widgets.OtmEventData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dave Hollander
 * 
 */
public abstract class OtmAbstractHandler extends AbstractHandler implements OtmHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(OtmAbstractHandler.class);

    protected MainController mc;
    private MainWindow mainWindow;

    protected OtmAbstractHandler() {
        this(OtmRegistry.getMainController());
    }

    protected OtmAbstractHandler(MainController mc) {
        // LOGGER.debug("Handler constructed with controller: "+this.getClass());
        this.mc = mc;
        if (mc == null) {
            throw new IllegalArgumentException("Tried to construct view without a main controller.");
        }
        mainWindow = mc.getMainWindow();
    }

    public void execute(OtmEventData event) {
        LOGGER.debug("Menthod not implemented");
    }

    protected MainWindow getMainWindow() {
        return mainWindow;
    }

    protected MainController getMainController() {
        return mc;
    }

    public static String COMMAND_ID = "org.opentravel.schemas.commands";

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.commands.OtmHandler#getID()
     */
    @Override
    public String getID() {
        return COMMAND_ID;
    }

}
