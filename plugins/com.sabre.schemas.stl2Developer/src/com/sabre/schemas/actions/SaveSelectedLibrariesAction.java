/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.actions;

import static com.sabre.schemas.stl2developer.MainWindow.NO_VALID_SELECTION_MSG;
import static com.sabre.schemas.stl2developer.MainWindow.SELECT_AT_LEAST_ONE_MSG;
import static com.sabre.schemas.stl2developer.MainWindow.WARNING_MSG;

import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;

import com.sabre.schemas.node.LibraryNode;
import com.sabre.schemas.properties.ExternalizedStringProperties;
import com.sabre.schemas.properties.Images;
import com.sabre.schemas.properties.StringProperties;
import com.sabre.schemas.stl2developer.DialogUserNotifier;

/**
 * @author Agnieszka Janowska
 * 
 */
public class SaveSelectedLibrariesAction extends OtmAbstractAction {
    private static StringProperties propsDefault = new ExternalizedStringProperties(
            "action.saveSelected");

    public SaveSelectedLibrariesAction() {
        super(propsDefault);
    }

    @Override
    public void run() {
        final List<LibraryNode> libraries = mc.getSelectedUserLibraries();
        if (libraries.isEmpty()) {
            // No libraries selected
            DialogUserNotifier.openWarning(WARNING_MSG, NO_VALID_SELECTION_MSG
                    + SELECT_AT_LEAST_ONE_MSG);
            return;
        }
        mc.getLibraryController().saveLibraries(libraries, false);
    }

    @Override
    public ImageDescriptor getImageDescriptor() {
        return Images.getImageRegistry().getDescriptor(Images.Save);
    }

    @Override
    public boolean isEnabled() {
        return mc.getCurrentNode_NavigatorView() != null ? mc.getCurrentNode_NavigatorView()
                .isEditable() : false;
    }

}
