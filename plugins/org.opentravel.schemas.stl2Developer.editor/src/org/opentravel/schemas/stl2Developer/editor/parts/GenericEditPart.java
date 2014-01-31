/**
 * Copyright (C) 2014 OpenTravel Alliance (info@opentravel.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.opentravel.schemas.stl2Developer.editor.parts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Label;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.editpolicies.ComponentEditPolicy;
import org.eclipse.gef.requests.GroupRequest;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.stl2Developer.editor.commands.HideNodeCommand;
import org.opentravel.schemas.stl2Developer.editor.i18n.Messages;
import org.opentravel.schemas.stl2Developer.editor.internal.filters.FilterManager;
import org.opentravel.schemas.stl2Developer.editor.model.Connection;
import org.opentravel.schemas.stl2Developer.editor.model.Diagram;
import org.opentravel.schemas.stl2Developer.editor.model.UINode;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.trees.library.LibrarySorter;

/**
 * @author Pawel Jedruch
 * 
 */
public abstract class GenericEditPart<T extends Node> extends AbstractGraphicalEditPart {

    public GenericEditPart(UINode model) {
        setModel(model);
        // to make sure node has correct type, otherwise will throw exception
        getNodeModel();
    }

    @SuppressWarnings("unchecked")
    public T getNodeModel() {
        return (T) getModel().getNode();
    }

    @Override
    public UINode getModel() {
        return (UINode) super.getModel();
    }

    @Override
    protected void createEditPolicies() {
        installEditPolicy(EditPolicy.COMPONENT_ROLE, new ComponentEditPolicy() {

            @Override
            protected Command createDeleteCommand(GroupRequest deleteRequest) {
                return new HideNodeCommand(getNodeModel(), getModel().getOwner());
            }

        });
    }

    @Override
    protected List<Connection> getModelSourceConnections() {
        List<Connection> ret = new ArrayList<Connection>();
        for (UINode target : getModel().getConnectedAsSource()) {
            ret.add(new Connection(getModel(), target));
        }
        return ret;
    }

    @Override
    protected List<Connection> getModelTargetConnections() {
        List<Connection> ret = new ArrayList<Connection>();
        for (UINode source : getModel().getConnectedAsTarget()) {
            ret.add(new Connection(source, getModel()));
        }
        return ret;
    }

    @Override
    protected final List<?> getModelChildren() {
        FilterManager fm = (FilterManager) getViewer().getProperty(FilterManager.class.toString());
        List<UINode> children = new ArrayList<UINode>();
        for (Node n : sort(getNodeModel().getChildren())) {
            UINode uiNode = Diagram.createModel(n, getModel().getOwner(), getModel());
            if (fm.select(uiNode))
                children.add(uiNode);
        }
        return children;
    }

    @Override
    protected void refreshVisuals() {
        if (getModel().isUnlinked()) {
            getFigure().setBackgroundColor(ColorConstants.red);
            getFigure().setToolTip(new Label(Messages.GenericEditPart_UnlinkedTooltip));
        }
    }

    private Collection<Node> sort(List<Node> children) {
        ArrayList<Node> sorted = new ArrayList<Node>(children);
        // TODO: create own inheritance flag or create global property (not NAvigatorView owned)
        // after that remove org.opentravel.schemas.views from exported-packages of stl2Developer
        // manifest
        if (OtmRegistry.getNavigatorView().isShowInheritedProperties()) {
            sorted.addAll(getNodeModel().getInheritedChildren());
        }
        Collections.sort(sorted, LibrarySorter.createComparator());
        return sorted;
    }

}
