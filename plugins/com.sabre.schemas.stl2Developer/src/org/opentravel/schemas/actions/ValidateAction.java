/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemas.actions;

import java.util.List;

import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.navigation.GlobalSelectionProvider;
import org.opentravel.schemas.node.ImpliedNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.properties.ExternalizedStringProperties;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.stl2developer.DialogUserNotifier;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.types.TypeNode;
import org.opentravel.schemas.views.ValidationResultsView;

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
