/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemas.controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.opentravel.schemas.node.ContextNode;
import org.opentravel.schemas.node.LibraryNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.properties.Messages;
import org.opentravel.schemas.stl2developer.DialogUserNotifier;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.views.ContextsView;
import org.opentravel.schemas.wizards.MergeContextNodeWizard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sabre.schemacompiler.model.AbstractLibrary;
import com.sabre.schemacompiler.model.TLContext;
import com.sabre.schemacompiler.model.TLContextReferrer;
import com.sabre.schemacompiler.model.TLLibrary;

/**
 * Implements interactions the user has with the Context View by acting upon the Context nodes and
 * maintains the context model. Constructed and maintained by the mainWindow, this manager uses the
 * contextModelManager to create and maintain the model complete with nodes and is used by the
 * manager view.
 * 
 * @author Agnieszka Janowska
 * 
 */
public class DefaultContextController extends OtmControllerBase implements ContextController {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultContextController.class);

    private ContextsView view;
    private ContextModelManager manager;

    private Map<ContextViewType, ViewSelection> viewSelections = new HashMap<ContextViewType, ViewSelection>();

    private class ViewSelection {
        LibraryNode lib;
        String contextId;

        private ViewSelection(LibraryNode lib, String id) {
            this.lib = lib;
            this.contextId = id;
        }
    }

    public static enum ContextViewType {
        CONTEXT_VIEW, TYPE_VIEW;
    }

    public DefaultContextController(final MainController mainController) {
        super(mainController);
        // LOGGER.debug("Context Controller constructor.");
        manager = new ContextModelManager();
        view = OtmRegistry.getContextsView();
    }

    @Override
    public void addContexts(LibraryNode ln) {
        manager.addLibraryContexts(ln);
    }

    @Override
    public List<String> getAvailableContextIds() {
        Node node = getCurrentNodeFromMainWindow();
        if (node == null)
            return Collections.emptyList();
        return manager.getAvailableContextIds(node.getLibrary());
    }

    @Override
    public List<String> getAvailableContextIds(final LibraryNode ln) {
        return manager.getAvailableContextIds(ln);
    }

    @Override
    public List<String> getAvailableContextIds(AbstractLibrary tlib) {
        return manager.getAvailableContextIds(tlib);
    }

    @Override
    public String getDefaultContextId() {
        Node cn = getCurrentNodeFromMainWindow();
        if (cn != null)
            return manager.getDefaultContextId(cn.getLibrary());
        return "";
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.opentravel.schemas.controllers.ContextController#getDefaultContextId(org.opentravel.schemas.node
     * .LibraryNode)
     */
    @Override
    public String getDefaultContextId(LibraryNode ln) {
        return manager.getDefaultContextId(ln);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.opentravel.schemas.controllers.ContextController#getApplicationContext(org.opentravel.schemas.node
     * .LibraryNode, java.lang.String)
     */
    @Override
    public String getApplicationContext(LibraryNode ln, String contextId) {
        return manager.getApplicationContext(ln, contextId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.opentravel.schemas.controllers.ContextController#getSelectedId(org.opentravel.schemas.controllers
     * .DefaultContextController.ContextViewType, org.opentravel.schemas.node.LibraryNode)
     */
    @Override
    public String getSelectedId(ContextViewType view, LibraryNode ln) {
        String id = "";
        if (view == null)
            return id;
        if (ln == null)
            return id;

        ViewSelection selection = viewSelections.get(view);
        if (selection == null || selection.lib != ln) {
            id = manager.getDefaultContextId(ln);
            setSelectedId(view, ln, id);
            return id;
        }
        return selection.contextId;
    }

    @Override
    public ContextNode getRoot() {
        return manager.getRoot();
    }

    @Override
    public void newContext(LibraryNode library, String id, String value) {
        ContextNode newNode = manager.newContext(library);
        newNode.setApplicationContext(value);
        newNode.setContextId(id);
    }

    @Override
    public void newContext() {
        if (view == null)
            view = OtmRegistry.getContextsView();
        ContextNode newNode = null;
        LibraryNode lib = null;
        ContextNode selected = view.getSelectedContextNode();
        if (selected == null) {
            // see if we can get a library from the modelNavigator
            List<LibraryNode> libs = OtmRegistry.getMainController().getSelectedUserLibraries();
            if (libs.size() > 0)
                lib = libs.get(0);
            else if (Node.getModelNode().getUserLibraries().size() == 1)
                lib = Node.getModelNode().getUserLibraries().get(0);
        } else
            lib = selected.getLibraryNode();

        if (lib != null) {
            newNode = manager.newContext(lib);
            newNode.setApplicationContext(view.getApplicationText());
            newNode.setContextId(view.getContextIdText());
            newNode.setDescription(view.getDescriptionText());
            view.setFocus(newNode);
        } else {
            DialogUserNotifier.openWarning("New Context",
                    Messages.getString("context.warning.newContext"));
        }
        view.refreshAllViews();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.controllers.ContextController#clearContexts()
     */
    @Override
    public void clearContexts() {
        manager.clear();
        viewSelections.clear();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.controllers.ContextController#clearContexts()
     */
    @Override
    public void clearContexts(LibraryNode ln) {
        manager.clear(ln);
        // remove any selections for this library
        ArrayList<ContextViewType> keys = new ArrayList<ContextViewType>();
        for (Entry<ContextViewType, ViewSelection> e : viewSelections.entrySet()) {
            if (e.getValue().lib == ln) {
                keys.add(e.getKey());
            }
        }
        for (ContextViewType key : keys) {
            if (key != null) {
                LOGGER.debug("Removing view selection for view: " + key.name() + " whose value is "
                        + viewSelections.get(key).contextId);
                viewSelections.remove(key);

            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.opentravel.schemas.controllers.ContextController#setSelectedId(org.opentravel.schemas.controllers
     * .DefaultContextController.ContextViewType, org.opentravel.schemas.node.LibraryNode,
     * java.lang.String)
     */
    @Override
    public boolean setSelectedId(ContextViewType view, LibraryNode ln, String id) {
        if (manager.getApplicationContext(ln, id).isEmpty())
            return false;
        viewSelections.put(view, new ViewSelection(ln, id));
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.controllers.ContextController#editContext()
     */
    @Override
    public void mergeContext() {
        final ContextsView view = OtmRegistry.getContextsView();
        LOGGER.info("default context manager mergeContext starting", this.getClass());
        ContextNode selected = view.getSelectedContextNode();
        if (selected == null) {
            LOGGER.warn("No context selected to merge.", this.getClass());
            DialogUserNotifier.openInformation("Merge Contexts",
                    "You  must select a context to merge.");
            return;
        }
        if (!selected.getType().equals(ContextNode.ContextNodeType.CONTEXT_ITEM)) {
            LOGGER.warn("Selected is not a context item. " + selected.getType());
            DialogUserNotifier.openInformation("Merge Contexts",
                    "You  must select a context to merge.");
            return;
        }
        boolean merge, refresh = false;
        String origAppCtx = selected.getApplicationContext();

        // Run wizard to allow them to select the context to merge into.
        final MergeContextNodeWizard wizard = new MergeContextNodeWizard(selected);
        wizard.run(OtmRegistry.getActiveShell());

        if (!wizard.wasCanceled()) {
            ContextNode chosen = wizard.getContext();
            if (chosen == null) {
                LOGGER.warn("Early Exit - No context chosen to merge.", this.getClass());
                return;
            }
            if (chosen.equals(selected)) {
                LOGGER.warn("Early Exit - Same context selected to merge.", this.getClass());
                return;
            }
            merge = DialogUserNotifier
                    .openConfirm(
                            "Context merge",
                            "Are you sure that you want to merge the original context "
                                    + selected.getContextId()
                                    + " ("
                                    + origAppCtx
                                    + ")"
                                    + " with "
                                    + chosen.getContextId()
                                    + " ("
                                    + chosen.getApplicationContext()
                                    + ") "
                                    + "?\n\n"
                                    + " Warning: This operation will create conflicts if there are properties (such as equivalents) "
                                    + "already existing in both contexts. The only way to resolve those conflicts is by manually editing the library file.");
            if (merge) {
                refresh = true;
                // chosen was the survivor - the context that remains.
                clearIfSelected(selected);
                manager.merge(selected, chosen);
            }
            if (refresh) {
                mc.getDefaultView().refreshAllViews();
                view.postContexts(true);
                view.select(chosen);
            }
        }
    }

    /**
     * @return the manager
     */
    @Override
    public ContextModelManager getContextModelManager() {
        return manager;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.opentravel.schemas.controllers.ContextController#getContextNode(org.opentravel.schemas.node.LibraryNode
     * , java.lang.String)
     */
    @Override
    public ContextNode getContextNode(LibraryNode ln, String id) {
        return manager.getNode(ln, id);
    }

    /**
     * If this is a selected Id, then reset then remove the selection.
     */
    private void clearIfSelected(ContextNode selected) {
        for (ContextViewType view : ContextViewType.values()) {
            if (selected.getContextId().equals(getSelectedId(view, selected.getLibraryNode())))
                viewSelections.remove(view);
        }
    }

    /**
     * @return the current properties or navigator node
     */
    private Node getCurrentNodeFromMainWindow() {
        Node node = (Node) mc.getCurrentNode_PropertiesView();
        if (node == null)
            node = mc.getCurrentNode_NavigatorView();
        return node;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.opentravel.schemas.controllers.ContextController#getDefaultContextNode(org.opentravel.schemas.node
     * .ContextNode)
     */
    @Override
    public TLContext getDefaultContext(TLLibrary library) {
        return manager.getDefaultContext(library);
    }

    /**
     * referrer is an TLExample or equivalent owner
     */
    @Override
    public void changeContext(final TLContextReferrer referrer) {
        LOGGER.info("Change context.");
        throw new IllegalAccessError("Change Context is not implemented.");
        // if (referrer == null) {
        // DialogUserNotifier.openWarning("Context Change",
        // "You can only change contexts on properties that are not empty");
        // return;
        // }
        // final AbstractLibrary owningLibrary = referrer.getOwningLibrary();
        // if (owningLibrary instanceof TLLibrary) {
        // final TLLibrary tlLib = (TLLibrary) owningLibrary;
        // final SimpleNameWizard wizard = new SimpleNameWizard(new ExternalizedStringProperties(
        // "wizard.changeContext"), getAvailableContextIds(tlLib));
        // wizard.setValidator(new ContextChangeValidator(referrer, wizard));
        // wizard.run(OtmRegistry.getActiveShell());
        // if (!wizard.wasCanceled()) {
        // final String contextId = wizard.getText();
        // referrer.setContext(contextId);
        // mainWindow.refreshAllViews();
        // }
        // }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.controllers.ContextController#cloneContext()
     */
    @Override
    public void cloneContext() {
        if (view == null)
            view = OtmRegistry.getContextsView();
        List<ContextNode> list = view.getSelectedContextNodes();
        ContextNode newNode = null;
        for (ContextNode selected : list) {
            newNode = manager.clone(selected);
            if (newNode != null) {
                view.refreshAllViews();
                view.setFocus(newNode);
            } else {
                DialogUserNotifier.openWarning("New Context", "Could not create new context.");
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.controllers.ContextController#copyContext()
     */
    @Override
    public void copyContext() {
        cloneContext();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.controllers.ContextController#copyContext(org.opentravel.schemas.node.Node,
     * org.opentravel.schemas.node.LibraryNode)
     */
    @Override
    public void copyContext(Node node, LibraryNode ln) {
        for (TLContext tlc : node.getContexts()) {
            manager.copyContext(tlc, ln);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.controllers.ContextController#setDefaultContext()
     */
    @Override
    public void setDefaultContext() {
        if (view == null)
            view = OtmRegistry.getContextsView();
        ContextNode selected = view.getSelectedContextNode();
        if (selected == null)
            DialogUserNotifier.openWarning("Change Context Default",
                    "No context is selected to set as the Default context.");
        else if (selected.isContextItem()) {
            view.setDefaultContextNode(selected);
            LOGGER.debug("setting default context to " + selected.getContextId());
            if (!manager.setDefaultContext(selected))
                LOGGER.warn("Could not set library node current context.");
            mc.getDefaultView().refreshAllViews();
        } else
            LOGGER.warn("Default context " + selected.getLabel() + " could not be set."
                    + selected.getType());
    }

    @Override
    public void refreshContexts() {
        manager.clear();
        for (LibraryNode lib : mc.getModelNode().getUserLibraries()) {
            manager.addLibraryContexts(lib);
        }
    }

}
