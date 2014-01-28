/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemas.actions;

import java.util.List;

import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.navigation.GlobalSelectionProvider;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.properties.ExternalizedStringProperties;
import org.opentravel.schemas.stl2developer.OtmRegistry;

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
