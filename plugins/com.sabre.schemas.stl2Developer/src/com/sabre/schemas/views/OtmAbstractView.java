/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.views;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sabre.schemas.controllers.MainController;
import com.sabre.schemas.node.INode;
import com.sabre.schemas.stl2developer.MainWindow;
import com.sabre.schemas.stl2developer.OtmRegistry;
import com.sabre.schemas.types.TypeNode;

/**
 * Abstract base class for all classes that extend ViewPart to be a workbench view.
 * 
 * @author Dave Hollander
 * 
 */
public abstract class OtmAbstractView extends ViewPart implements OtmView {
    private static final Logger LOGGER = LoggerFactory.getLogger(OtmAbstractView.class);

    protected MainController mc;
    private MainWindow mainWindow;

    protected OtmAbstractView() {
        this(OtmRegistry.getMainController());
    }

    protected OtmAbstractView(MainController mc) {
        this.mc = mc;
        if (mc == null) {
            throw new IllegalArgumentException("Tried to construct view without a main controller.");
        }
        mainWindow = mc.getMainWindow();
    }

    protected MainWindow getMainWindow() {
        return mainWindow;
    }

    protected MainController getMainController() {
        return mc;
    }

    @Override
    public boolean activate() {
        if (mainWindow == null)
            return false;

        // TODO - how to know if it was already active?
        try {
            PlatformUI.getWorkbench();
        } catch (IllegalStateException e) {
            return false; // No workbench or display.
        }

        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().activate(this);
        try {
            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                    .showView(this.getViewID());
        } catch (PartInitException e) {
            LOGGER.debug("Error showing view: " + getViewID());
            return false;
        }
        return true;
    }

    @Override
    public void refreshAllViews() {
        for (OtmView view : OtmRegistry.getAllActiveViews()) {
            view.refresh();
        }
    }

    @Override
    public void refreshAllViews(INode node) {
        for (OtmView view : OtmRegistry.getAllActiveViews()) {
            view.refresh(node);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.views.OtmView#refresh(com.sabre.schemas.node.INode, boolean)
     */
    @Override
    public void refresh(INode node, boolean force) {
        refresh(node);
    }

    // Override this method if the view can recreate its contents.
    @Override
    public void refresh(boolean regenerate) {
        refresh();
    }

    /** ************** Override if needed for a view ***************** **/
    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.views.OtmView#clearFilter()
     */
    @Override
    public void clearFilter() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.views.OtmView#clearSelection()
     */
    @Override
    public void clearSelection() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.views.OtmView#getPreviousNode()
     */
    @Override
    public INode getPreviousNode() {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.views.OtmView#collapse()
     */
    @Override
    public void collapse() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.views.OtmView#expand()
     */
    @Override
    public void expand() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.views.OtmView#isShowInheritedProperties()
     */
    @Override
    public boolean isShowInheritedProperties() {
        return OtmRegistry.getNavigatorView().isShowInheritedProperties();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.views.OtmView#isListening()
     */
    @Override
    public boolean isListening() {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.views.OtmView#select(com.sabre.schemas.node.INode)
     */
    @Override
    public void select(INode node) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.views.OtmView#setDeepPropertyView(boolean)
     */
    @Override
    public void setDeepPropertyView(boolean state) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.views.OtmView#setExactMatchFiltering(boolean)
     */
    @Override
    public void setExactMatchFiltering(boolean state) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.views.OtmView#setInput(com.sabre.schemas.node.INode)
     */
    @Override
    public void setInput(INode node) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.views.OtmView#setInheritedPropertiesDisplayed(boolean)
     */
    @Override
    public void setInheritedPropertiesDisplayed(boolean state) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.views.OtmView#moveDown()
     */
    @Override
    public void moveDown() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.views.OtmView#moveUp()
     */
    @Override
    public void moveUp() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.views.OtmView#setListening(boolean)
     */
    @Override
    public void setListening(boolean state) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.views.OtmView#restorePreviousNode()
     */
    @Override
    public void restorePreviousNode() {
        setCurrentNode(getPreviousNode());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.views.OtmView#moveDown()
     * 
     * Overridden by views that maintain trees.
     */
    @Override
    public void remove(INode node) {
    }

    /**
     * @param selection
     * @return Return first node from selection. For the {@link TypeNode} will return his parent
     *         (since TypeNodes are not in the real tree). If selection is not Structured or the
     *         firstElement in selection is not {@link INode} then return null;
     */
    public INode extractFirstNode(ISelection selection) {
        if (selection instanceof StructuredSelection) {
            Object firstElement = ((StructuredSelection) selection).getFirstElement();
            if (firstElement instanceof INode) {
                INode node = (INode) firstElement;
                if (node instanceof TypeNode) {
                    return node.getParent();
                }
                return node;
            }
        }
        return null;
    }
}
