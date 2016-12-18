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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.swt.graphics.Color;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.stl2Developer.editor.model.Connection;
import org.opentravel.schemas.stl2Developer.editor.model.UINode;
import org.opentravel.schemas.stl2Developer.editor.ui.figures.FacetFigure;

/**
 * @author Pawel Jedruch
 * 
 */
public class LibraryEditPart extends GenericEditPart<LibraryNode> {

    public LibraryEditPart(UINode model) {
        super(model);
    }

    public static Color LIBRARY_COLOR = new Color(null, 176, 222, 242);
    private Map<Object, IFigure> modelToFigure = new HashMap<Object, IFigure>();

    @Override
    protected IFigure createFigure() {
        return new LibraryFigure();
    }

    @Override
    protected void refreshSourceConnections() {
        super.refreshSourceConnections();
        if (children != null) {
            for (Object o : children) {
                ((EditPart) o).refresh();
            }
        }
    }

    @Override
    protected void refreshTargetConnections() {
        super.refreshTargetConnections();
        if (children != null) {
            for (Object o : children) {
                ((EditPart) o).refresh();
            }
        }
    }

    public class LibraryFigure extends Figure {

        public LibraryFigure() {

            setBorder(new LineBorder(ColorConstants.black, 1));
            setOpaque(true);
            setBackgroundColor(LIBRARY_COLOR);
            ToolbarLayout layout = new ToolbarLayout();
            layout.setMinorAlignment(ToolbarLayout.ALIGN_CENTER);
            layout.setStretchMinorAxis(false);
            layout.setSpacing(2);
            setLayoutManager(layout);
            add(ComponentNodeEditPart.newLabel(getNodeModel()));
            Figure container = new Figure();
            container.setBorder(new MarginBorder(0, 10, 0, 10));
            ToolbarLayout l = new ToolbarLayout();
            l.setSpacing(10);
            l.setHorizontal(true);
            container.setLayoutManager(l);
            add(container);
            for (Node child : getNodeModel().getChildren()) {
                addChild(child, container);
            }
        }

        private void addChild(Node node, Figure parent) {
            IFigure f = createChildFigure(ComponentNodeEditPart.newLabel(node));
            modelToFigure.put(node, f);
            parent.add(f);
        }

        private IFigure createChildFigure(Label newLabel) {
            FacetFigure ff = new FacetFigure(newLabel);
            return ff;
        }

    }

    @Override
    protected void addChildVisual(EditPart childEditPart, int index) {
        IFigure child = ((GraphicalEditPart) childEditPart).getFigure();
        modelToFigure.get(((Node) childEditPart.getModel()).getParent()).add(child);
    }

    @Override
    protected List<Connection> getModelTargetConnections() {
        List<Connection> connections = new ArrayList<Connection>();
        // if (Constants.withLibraryConnections()) {
        // for (Node n : getNodesToConnect(getNodeModel())) {
        // LibraryConnection libraryConn = new LibraryConnection(n, getNodeModel());
        // connections.add(libraryConn);
        // }
        // }
        return connections;
    }

    private List<Node> getNodesToConnect(LibraryNode model) {
        List<Node> ret = new ArrayList<Node>();
        for (Node c : model.getDescendentsNamedTypes()) {

            EditPart childEP = (EditPart) getViewer().getEditPartRegistry().get(c);
            if (childEP != null && !(childEP.getParent() instanceof LibraryEditPart)) {
                ret.add((Node) childEP.getModel());
            }
        }
        return ret;
    }

}
