/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.actions;

import java.util.List;

import com.sabre.schemas.controllers.MainController;
import com.sabre.schemas.navigation.GlobalSelectionProvider;
import com.sabre.schemas.node.ImpliedNode;
import com.sabre.schemas.node.Node;
import com.sabre.schemas.properties.ExternalizedStringProperties;
import com.sabre.schemas.properties.Images;
import com.sabre.schemas.stl2developer.DialogUserNotifier;
import com.sabre.schemas.stl2developer.OtmRegistry;
import com.sabre.schemas.types.TypeNode;
import com.sabre.schemas.views.ValidationResultsView;

/**
 * @author Dave Hollander
 * 
 */
public class ValidateAction extends AbstractGlobalSelectionAction {

    public static final String ID = "action.validate";

    public ValidateAction() {
        super(ID, GlobalSelectionProvider.NAVIGATION_VIEW);
        new ExternalizedStringProperties(getId()).initializeAction(this);
        setImageDescriptor(Images.getImageRegistry().getDescriptor(Images.Validate));
    }

    @Override
    public void run() {
        MainController mc = OtmRegistry.getMainController();

        ValidationResultsView view = OtmRegistry.getValidationResultsView();
        if (view == null || !view.activate()) {
            DialogUserNotifier.openWarning("Warning",
                    "Please open the validation view before validating.");
            return;
        }

        Node node = getSourceValue().get(0);
        // If in a version chain, validate all members of the chain.
        if (node.getChain() != null) {
            view.validateNode(node.getChain());
        } else {
            view.validateNode(node);
        }
        mc.refresh();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Node> getSourceValue() {
        return (List<Node>) super.getSourceValue();
    }

    @Override
    public boolean isEnabled(Object object) {
        List<Node> newSelection = getSourceValue();
        if (newSelection.size() != 1) {
            return false;
        }
        Node n = newSelection.get(0);
        if (n instanceof ImpliedNode || n instanceof TypeNode)
            return false;
        return n != null ? !n.isBuiltIn() && !n.isXSDSchema() : false;
    }
}
