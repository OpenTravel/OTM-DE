/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.actions;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Event;

import com.sabre.schemas.node.Node;
import com.sabre.schemas.properties.ExternalizedStringProperties;
import com.sabre.schemas.properties.Images;
import com.sabre.schemas.properties.StringProperties;

/**
 * @author Dave Hollander
 * 
 */
public class SaveAllLibrariesAction extends OtmAbstractAction {
    private final static StringProperties propsDefault = new ExternalizedStringProperties(
            "action.saveAll");

    public SaveAllLibrariesAction() {
        super(propsDefault);
    }

    /**
	 *
	 */
    public SaveAllLibrariesAction(final StringProperties props) {
        super(props);
    }

    @Override
    public void runWithEvent(Event event) {
        mc.runSaveLibraries(event);
    }

    @Override
    public ImageDescriptor getImageDescriptor() {
        return Images.getImageRegistry().getDescriptor(Images.SaveAll);
    }

    @Override
    public boolean isEnabled() {
        Node n = mc.getSelectedNode_NavigatorView();
        return n != null ? !n.isBuiltIn() : false;
    }

}
