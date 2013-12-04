/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.actions;

import org.eclipse.jface.resource.ImageDescriptor;

import com.sabre.schemas.node.Node;
import com.sabre.schemas.node.NodeEditStatus;
import com.sabre.schemas.properties.ExternalizedStringProperties;
import com.sabre.schemas.properties.Images;
import com.sabre.schemas.properties.StringProperties;

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
