/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.actions;

import java.util.List;

import com.sabre.schemas.controllers.MainController;
import com.sabre.schemas.navigation.GlobalSelectionProvider;
import com.sabre.schemas.node.ModelNode;
import com.sabre.schemas.node.Node;
import com.sabre.schemas.node.ProjectNode;
import com.sabre.schemas.properties.ExternalizedStringProperties;
import com.sabre.schemas.stl2developer.OtmRegistry;

/**
 * @author Dave Hollander
 * 
 */
public class CompileAction extends AbstractGlobalSelectionAction {

    public static final String ID = "action.compile";

    public CompileAction() {
        super(ID, GlobalSelectionProvider.NAVIGATION_VIEW);
        new ExternalizedStringProperties(getId()).initializeAction(this);
    }

    @Override
    public void run() {
        MainController mc = OtmRegistry.getMainController();
        Node cur = getSourceValue().get(0);
        if (cur == null || cur instanceof ModelNode)
            mc.getModelController().compileModel(mc.getModelNode());
        else {
            if (!(cur instanceof ProjectNode))
                cur = cur.getLibrary().getProject();
            mc.getModelController().compileModel((ProjectNode) cur);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Node> getSourceValue() {
        return (List<Node>) super.getSourceValue();
    }

    @Override
    protected boolean isEnabled(Object object) {
        return getSourceValue().size() == 1;
    }

}
