/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.stl2developer;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sabre.schemas.controllers.ContextController;
import com.sabre.schemas.controllers.MainController;
import com.sabre.schemas.views.NavigatorView;

/**
 * Main Window View. Interacts with display underlying SWT and shell underlying the main window. It
 * also receives requests from the models.
 * 
 * On initialization, it initializes the main window controller. Provides ForceNode() and
 * ActivateView() methods.
 * 
 * @author Dave Hollander
 * 
 *         TODO: this class is too fat, segregation is needed, make sure you introduce specialized
 *         Controller, like for Library and Model TODO: move all the business logic out of here! Use
 *         NodeController for node-related business logic.
 */
public class MainWindow extends ViewPart implements ISelectionListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(MainWindow.class);
    private static final String ID = "com.sabre.schemas.stl2Developer.view";

    public enum ViewType {
        CONTEXT, NAVIGATOR, LIBRARY, TYPE, VALIDATION
    }

    public static final String WARNING_MSG = "Warning";
    public static final String NO_VALID_SELECTION_MSG = "No valid selection";
    public static final String SELECT_AT_LEAST_ONE_MSG = "Select at least one.";

    private ColorProvider colorProvider;

    private MainController mc;
    private Display mainDisplay;

    public MainWindow() {
        LOGGER.info("Main Window constructor ran. " + this.getClass());
        try {
            mainDisplay = PlatformUI.getWorkbench().getDisplay();
            initializeMainWindow(mainDisplay);
        } catch (IllegalStateException e) {
            mainDisplay = null;
            LOGGER.debug("No Main Display.");
        }
        OtmRegistry.registerMainView(this);
    }

    @Override
    public void createPartControl(final Composite parent) {
        LOGGER.info("Create part control in " + this.getClass());
        initializeMainWindow(parent.getDisplay());

    }

    private void initializeMainWindow(final Display display) {
        // LOGGER.info("Initializing part control of " + this.getClass());

        mc = OtmRegistry.getMainController();

        mainDisplay = display;
        colorProvider = new ColorProvider(display);
        // LOGGER.info("Done initializing part control of " + this.getClass());
    }

    public boolean hasDisplay() {
        return mainDisplay != null;
    }

    public Display getDisplay() {
        return mainDisplay;
    }

    /**
     * Get the workbench part site from the navigator view.
     */
    @Override
    public IWorkbenchPartSite getSite() {
        return (NavigatorView) OtmRegistry.getNavigatorView() == null ? null
                : ((NavigatorView) OtmRegistry.getNavigatorView()).getSite();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    @Override
    public void setFocus() {
        // getTreeView().setFocus();
    }

    public String getID() {
        return ID;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui. IWorkbenchPart,
     * org.eclipse.jface.viewers.ISelection)
     */
    @Override
    public void selectionChanged(final IWorkbenchPart part, final ISelection selection) {
    }

    @Deprecated
    public ContextController getContextController() {
        return mc != null ? mc.getContextController() : null;
    }

    public ColorProvider getColorProvider() {
        return colorProvider;
    }

    /**
     * NOTE - do not use this unless from a view! Not junit safe. Use mainController.postStatus
     * instead.
     * 
     * @param msg
     */
    public void postStatus(String msg) {
        if (msg.isEmpty())
            return;

        IWorkbench wb;
        try {
            wb = PlatformUI.getWorkbench();
        } catch (IllegalStateException e) {
            return; // No workbench or display.
        }
        // TODO - save the actionBars to speed up using status

        IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
        if (win == null)
            return;
        IWorkbenchPage page = win.getActivePage();
        if (page == null)
            return;
        IWorkbenchPart part = page.getActivePart();
        IWorkbenchPartSite site = part.getSite();
        IViewSite vSite = (IViewSite) site;
        IActionBars actionBars = vSite.getActionBars();

        if (actionBars == null)
            return;

        IStatusLineManager statusLineManager = actionBars.getStatusLineManager();
        if (statusLineManager == null)
            return;

        statusLineManager.setMessage(msg);
    }

}
