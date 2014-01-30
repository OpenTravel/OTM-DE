
package org.opentravel.schemas.views.decoration;

import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.opentravel.schemas.controllers.MainController.IRefreshListener;
import org.opentravel.schemas.node.INode;
import org.opentravel.schemas.node.LibraryChainNode;
import org.opentravel.schemas.node.LibraryNode;
import org.opentravel.schemas.node.NamespaceHandler;
import org.opentravel.schemas.properties.Messages;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.trees.repository.RepositoryNode.NamespaceNode;
import org.opentravel.schemas.trees.repository.RepositoryNode.RepositoryChainNode;
import org.opentravel.schemas.trees.repository.RepositoryNode.RepositoryNameNode;

import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.repository.ProjectItem;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryItemState;
import org.opentravel.schemacompiler.repository.impl.RemoteRepositoryClient;

public class LibraryDecorator extends BaseLabelProvider implements ILightweightLabelDecorator,
        IRefreshListener {

    public LibraryDecorator() {
        OtmRegistry.getMainController().addRefreshListener(this);
    }

    @Override
    public boolean isLabelProperty(Object element, String property) {
        if (element instanceof LibraryNode) {
            return true;
        }
        return false;
    }

    @Override
    public void decorate(Object element, IDecoration decoration) {
        if (element instanceof LibraryNode) {
            decoration.addSuffix(getLibraryDecoration((LibraryNode) element));
        } else if (element instanceof LibraryChainNode) {
            LibraryNode head = ((LibraryChainNode) element).getHead();
            decoration.addSuffix(getLibraryDecoration(head));
        } else if (element instanceof RepositoryNameNode) {
            decoration.addSuffix(getRepositoryNameDecoration((RepositoryNameNode) element));

        } else if (element instanceof RepositoryChainNode) {
            RepositoryChainNode node = (RepositoryChainNode) element;
            decoration.addSuffix(getRepositoryItemDecoration(node.getItem()));
        } else if (element instanceof NamespaceNode) {
            decoration.addSuffix(getNamespaceDecoration((NamespaceNode) element));
        }
    }

    private String getRepositoryNameDecoration(RepositoryNameNode element) {
        if (element.getRepository() instanceof RemoteRepositoryClient) {
            RemoteRepositoryClient rc = (RemoteRepositoryClient) element.getRepository();
            String userID = rc.getUserId();
            if (rc.getUserId() == null) {
                userID = Messages.getString("repository.user.anonymous");
            }
            return surround("User: " + userID);
        }
        return "";
    }

    private String getNamespaceDecoration(NamespaceNode node) {
        StringBuilder sb = new StringBuilder();
        sb.append(" ");
        sb.append(getChildCount(node));
        sb.append(surround(node.getPermission()));
        return sb.toString();
    }

    @Override
    public void refresh() {
        fireLabelProviderChanged(new LabelProviderChangedEvent(this));
    }

    @Override
    public void refresh(INode node) {
        fireLabelProviderChanged(new LabelProviderChangedEvent(this, node));
    }

    private String getRepositoryItemDecoration(RepositoryItem item) {
        String status = translateStatusState(item.getStatus(), item.getState(),
                item.getLockedByUser(), false);
        return getDecoration(status, item.getVersion());
    }

    public static String translateStatusState(TLLibraryStatus status, RepositoryItemState state,
            String lockedBy, boolean isEditableInGui) {
        if (state == RepositoryItemState.BUILT_IN) {
            return "Built-in";
        }
        if (TLLibraryStatus.FINAL.equals(status)) {
            return "Final";
        } else if (status == null) {
            return "NULL Status";
        } else {
            switch (state) {
                case MANAGED_LOCKED:
                    return "Locked: " + lockedBy;
                case MANAGED_UNLOCKED:
                    return "Draft";
                case MANAGED_WIP:
                    if (isEditableInGui)
                        return "Editable";
                    else
                        return "Locked: " + lockedBy;
                case UNMANAGED:
                    return "";
                case BUILT_IN:
                    // should be handled before checking if final
                    return "Built-in";
                default:
                    return "Unknown";
            }
        }

    }

    private String getChildCount(NamespaceNode node) {
        int count = node.getChildren().size();
        return "(" + count + ")";
    }

    private String getLibraryDecoration(LibraryNode element) {
        return getDecoration(getLibraryStatus(element), getLibraryVersion(element));
    }

    public String getLibraryStatus(LibraryNode lib) {
        return OtmRegistry.getMainController().getLibraryController().getLibraryStatus(lib);
    }

    public String getLibraryVersion(LibraryNode lib) {
        NamespaceHandler nsHandler = lib.getNsHandler();
        ProjectItem projectItem = lib.getProjectItem();
        String version = "";
        if (projectItem != null && nsHandler != null
                && !RepositoryItemState.UNMANAGED.equals(projectItem.getState())) {
            version = nsHandler.getNSVersion(lib.getNamespace());
        }
        return version;
    }

    private String getDecoration(String status, String version) {
        return surround(status) + surround(version);
    }

    private String surround(String txt) {
        if (txt != null && !txt.isEmpty()) {
            return " [" + txt + "]";
        }
        return "";
    }

    @Override
    public void dispose() {
    }

}
