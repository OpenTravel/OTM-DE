/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemas.controllers;

import java.util.ArrayList;
import java.util.List;

import org.opentravel.schemas.node.DocumentationNode;
import org.opentravel.schemas.node.controllers.DocumentationNodeModelManager;
import org.opentravel.schemas.stl2developer.DialogUserNotifier;
import org.opentravel.schemas.views.DocumentationView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This controller interacts with the user and the documentation node model.
 * 
 * @author Agnieszka Janowska
 * 
 */
public class DefaultDocumentationController implements DocumentationController {

    public static final Logger LOGGER = LoggerFactory
            .getLogger(DefaultDocumentationController.class);

    private final DocumentationView view;

    public DefaultDocumentationController(DocumentationView view) {
        this.view = view;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.controllers.DocumentationController#addDocItem()
     */
    @Override
    public void addDocItem() {
        DocumentationNode node = view.getSelectedDocumentationNode();
        String errorMsg = "Cannot create new documentation item for the selected type";
        if (node != null) {
            DocumentationNode typeRoot = node.getOwningTypeRoot();
            if (typeRoot != null) {
                DocumentationNodeModelManager factory = new DocumentationNodeModelManager();
                DocumentationNode child = null;
                try {
                    child = factory.createDocItemNodeForTypeRoot(typeRoot);
                } catch (Exception e) {
                    errorMsg = e.getMessage();
                }
                if (child == null) {
                    DialogUserNotifier.openWarning("New Documentation Item", errorMsg);
                    return;
                }
                view.refresh();
                view.setFocus(child);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.controllers.DocumentationController#deleteDocItem()
     */
    @Override
    public void deleteDocItems() {
        List<DocumentationNode> nodes = view.getSelectedDocumentationNodes();
        if (nodes != null) {
            for (DocumentationNode node : nodes) {
                boolean removed = true;
                String errorMsg = "Cannot delete documentation type root, you can only delete documentation items";
                if (!node.isDocItem()) {
                    DialogUserNotifier.openWarning("Delete Documentation Item", errorMsg);
                    continue;
                }
                DocumentationNode typeRoot = node.getOwningTypeRoot();
                if (typeRoot != null) {
                    DocumentationNodeModelManager factory = new DocumentationNodeModelManager();
                    int index = typeRoot.getChildren().indexOf(node);
                    try {
                        factory.removeDocItemNodeFromParent(node);
                    } catch (Exception e) {
                        errorMsg = e.getMessage();
                        removed = false;
                    }
                    if (!removed) {
                        DialogUserNotifier.openWarning("Delete Documentation Item", errorMsg);
                        continue;
                    }
                    view.refreshAllViews();
                    if (index > 0) {
                        view.select(typeRoot.getChildren().get(index - 1));
                    } else {
                        view.select(typeRoot);
                    }
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.controllers.DocumentationController#upDocItem()
     */
    @Override
    public void upDocItem() {
        DocumentationNode node = view.getSelectedDocumentationNode();
        if (node != null) {
            if (!node.isDocItem()) {
                DialogUserNotifier
                        .openWarning("Move Documentation Item",
                                "Cannot move documentation type root, you can only move documentation items");
                return;
            }
            DocumentationNodeModelManager factory = new DocumentationNodeModelManager();
            factory.moveUpDocItemNode(node);
            view.refreshAllViews();
            view.setFocus(node);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.controllers.DocumentationController#downDocItem()
     */
    @Override
    public void downDocItem() {
        DocumentationNode node = view.getSelectedDocumentationNode();
        if (node != null) {
            if (!node.isDocItem()) {
                DialogUserNotifier
                        .openWarning("Move Documentation Item",
                                "Cannot move documentation type root, you can only move documentation items");
                return;
            }
            DocumentationNodeModelManager factory = new DocumentationNodeModelManager();
            factory.moveDownDocItemNode(node);
            view.refreshAllViews();
            view.setFocus(node);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.controllers.DocumentationController#clearDocItem()
     */
    @Override
    public void clearDocItem() {
        DocumentationNode node = view.getSelectedDocumentationNode();
        if (node != null) {
            if (!node.isDocItem()) {
                DialogUserNotifier
                        .openWarning("Clear Documentation Item",
                                "Cannot clear documentation type root, you can only clear documentation items");
                return;
            }
            node.setValue(null);
            view.refreshAllViews();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.controllers.DocumentationController#changeDocItemType()
     */
    @Override
    public void changeDocItemsType() {
        List<DocumentationNode> nodes = view.getSelectedDocumentationNodes();
        DocumentationNode newRoot = view.getSelectedTypeRoot();
        String errorMsg = "Cannot move documentation item to the chosen type";
        boolean added = false;
        boolean error = false;
        List<DocumentationNode> newNodes = new ArrayList<DocumentationNode>();
        if (!nodes.isEmpty() && newRoot != null) {
            try {
                for (DocumentationNode node : nodes) {
                    if (!newRoot.equals(node.getOwningTypeRoot())) {
                        DocumentationNode newNode = new DocumentationNodeModelManager()
                                .moveToTypeRoot(node, newRoot);
                        newNodes.add(newNode);
                        added = true;
                    }
                }
            } catch (Exception e) {
                errorMsg = e.getMessage();
                error = true;
                LOGGER.info("While trying to change doc item type: ", e);
            }
            if (error) {
                DialogUserNotifier.openWarning("Change Documentation Item", errorMsg);
                view.setSelectedTypeRoot(newRoot);
            }
        }
        if (added) {
            LOGGER.info("Changed types of " + newNodes.size() + " to " + newRoot.getLabel());
            view.refreshAllViews();
            view.setFocus(newNodes);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.controllers.DocumentationController#cloneDocItems()
     */
    @Override
    public void cloneDocItems() {
        List<DocumentationNode> nodes = view.getSelectedDocumentationNodes();
        String errorMsg = "Cannot create new documentation item for the selected type";
        if (nodes != null && !nodes.isEmpty()) {
            for (DocumentationNode node : nodes) {
                DocumentationNode typeRoot = node.getOwningTypeRoot();
                if (typeRoot != null) {
                    DocumentationNodeModelManager factory = new DocumentationNodeModelManager();
                    DocumentationNode child = null;
                    try {
                        child = factory.createDocItemNodeForTypeRoot(typeRoot);
                        child.setValue(node.getValue());
                    } catch (Exception e) {
                        errorMsg = e.getMessage();
                        LOGGER.info("While trying to add doc item: ", e);
                    }
                    if (child == null) {
                        DialogUserNotifier.openWarning("New Documentation Item", errorMsg);
                        continue;
                    }
                    view.refreshAllViews();
                    view.setFocus(child);
                }
            }
        }
    }
}
