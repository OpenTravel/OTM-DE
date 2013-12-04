/*
 * Copyright (c) 2013, Sabre Inc.
 */
package com.sabre.schemas.stl2Developer.editor.parts;

import org.eclipse.draw2d.BorderLayout;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Cursors;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gef.requests.CreationFactory;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import com.sabre.schemas.node.Node;
import com.sabre.schemas.properties.Fonts;
import com.sabre.schemas.stl2Developer.editor.internal.Features;
import com.sabre.schemas.stl2Developer.editor.internal.GEFUtils;
import com.sabre.schemas.stl2Developer.editor.model.Diagram;
import com.sabre.schemas.stl2Developer.editor.model.Diagram.Position;
import com.sabre.schemas.stl2Developer.editor.model.UINode;
import com.sabre.schemas.stl2Developer.editor.ui.figures.HyperlinkLabel;
import com.sabre.schemas.stl2Developer.editor.ui.figures.NodeWithFacetsFigure;

/**
 * @author Pawel Jedruch
 * 
 */
public class PropertyNodeEditPart extends GenericEditPart<Node> implements MouseListener {

    public PropertyNodeEditPart(UINode node) {
        super(node);
    }

    @Override
    protected IFigure createFigure() {
        PropertyFigure ff = new PropertyFigure();
        ff.setBackgroundColor(NodeWithFacetsFigure.classColor);
        ff.addMouseListenerForLink(this);
        return ff;
    }

    @Override
    protected void refreshVisuals() {
        getFigure().setImage(getNodeModel().getImage());
        getFigure().setName(getNodeModel().getName());
        getFigure().setType(getNodeModel().getTypeNameWithPrefix());
        getFigure().setTypeImage(getModel().getTypeImage());
        if (getModel().getNode().isInheritedProperty()) {
            // TODO: move this to Features class
            getFigure().setFont(Fonts.getFontRegistry().get(Fonts.inheritedItem));
            getFigure().setForegroundColor(ColorConstants.darkBlue);
        }
        super.refreshVisuals();
    }

    @Override
    public PropertyFigure getFigure() {
        return (PropertyFigure) super.getFigure();
    }

    @Override
    public void mousePressed(MouseEvent me) {
        Node newNode = getNodeModel().getTypeNode();
        Command command = getCreateCommand(newNode);
        if (command != null && command.canExecute()) {
            getViewer().getEditDomain().getCommandStack().execute(command);
            selectNode(newNode);
        }
        me.consume();
    }

    private void selectNode(Node newNode) {
        Diagram diagram = getModel().getOwner();
        UINode uiNode = diagram.findUINode(newNode);

        EditPart nodeEP = (EditPart) getViewer().getEditPartRegistry().get(uiNode);
        final EditPart toSelecteEP = GEFUtils.getEditPartToSelect(nodeEP);

        getViewer().getEditPartRegistry().get(uiNode);
        // wait until layout will finish
        Display.getDefault().asyncExec(new Runnable() {

            @Override
            public void run() {
                if (Features.addNewPropertyFullyExpanded()) {
                    if (toSelecteEP instanceof ComponentNodeEditPart) {
                        ((ComponentNodeEditPart) toSelecteEP).expand();
                    }
                }
                getViewer().reveal(toSelecteEP);
                getViewer().select(toSelecteEP);

            }
        });

    }

    private Command getCreateCommand(final Node newNode) {
        CreateRequest create = new CreateRequest();
        create.setFactory(new CreationFactory() {

            @Override
            public Object getObjectType() {
                return null;
            }

            @Override
            public Object getNewObject() {
                return newNode;
            }
        });
        Diagram diagram = getModel().getOwner();
        Point location = diagram.findBestLocation(diagram.findUINode(getNodeModel())
                .getTopLevelParent(), (Node) create.getNewObject(), Position.RIGHT);
        create.setLocation(location);
        EditPart target = getTargetEditPart(create);
        return target.getParent().getCommand(create);
    }

    class PropertyFigure extends Figure {
        private Label image = new Label();
        private Label name = new Label();
        private HyperlinkLabel type = new HyperlinkLabel();
        private Label typeImage = new Label();

        public PropertyFigure() {
            this.setFont(Display.getDefault().getSystemFont());
            // name.setFont(Display.getDefault().getSystemFont());
            setLayoutManager(new BorderLayout());
            setOpaque(true);
            setBorder(new LineBorder(ColorConstants.black, 1));
            Figure leftC = new Figure();
            leftC.setLayoutManager(new ToolbarLayout(true));
            leftC.add(image);
            leftC.add(name);
            leftC.add(type);
            add(leftC, BorderLayout.LEFT);
            add(typeImage, BorderLayout.RIGHT);
        }

        public void setName(String name) {
            this.name.setText(name);
        }

        public void setType(String type) {
            if (type != null && !type.isEmpty())
                this.type.setText(" [" + type + "]");
        }

        public void setTypeImage(Image img) {
            this.typeImage.setIcon(img);
        }

        public void setImage(Image img) {
            this.image.setIcon(img);
        }

        public void addMouseListenerForLink(MouseListener listener) {
            type.addMouseListener(listener);
            typeImage.addMouseListener(listener);
            typeImage.setCursor(Cursors.HAND);
        }

    }

    @Override
    public void mouseReleased(MouseEvent me) {

    }

    @Override
    public void mouseDoubleClicked(MouseEvent me) {

    }

}
