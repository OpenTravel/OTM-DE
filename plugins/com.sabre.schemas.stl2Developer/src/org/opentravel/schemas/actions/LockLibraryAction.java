/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemas.actions;

import org.eclipse.jface.resource.ImageDescriptor;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeEditStatus;
import org.opentravel.schemas.properties.ExternalizedStringProperties;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.properties.StringProperties;

/**
 * Manage a library in a repository.
 * 
 * @author Dave Hollander
 * 
 */
public class LockLibraryAction extends OtmAbstractAction {
    private static StringProperties propDefault = new ExternalizedStringProperties(
            "action.library.lock");

    public LockLibraryAction() {
        super(propDefault);
    }

    public LockLibraryAction(final StringProperties props) {
        super(props);
    }

    @Override
    public void run() {
        mc.getRepositoryController().lock();
    }

    @Override
    public ImageDescriptor getImageDescriptor() {
        return Images.getImageRegistry().getDescriptor(Images.Lock);
    }

    // enable when Work-in-progress item.
    @Override
    public boolean isEnabled(Node node) {
        Node n = getMainController().getCurrentNode_NavigatorView();
        if (n == null || n.getLibrary() == null)
            return false;
        return n.getLibrary().getEditStatus().equals(NodeEditStatus.MANAGED_READONLY);
    }
}
