package com.sabre.schemas.stl2Developer.editor.ui.figures;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Cursors;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.StackLayout;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.draw2d.text.AbstractFlowBorder;
import org.eclipse.draw2d.text.FlowFigure;
import org.eclipse.draw2d.text.FlowPage;
import org.eclipse.draw2d.text.TextFlow;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

public class FlowTextFigure extends Figure {

    public static final Color LINK_COLOR = ColorConstants.blue;
    private FlowPage fp;

    public FlowTextFigure() {
        fp = new FlowPage();
        fp.setFont(JFaceResources.getDefaultFont());
        fp.setOpaque(true);
        add(fp);
        fp.setBorder(new LineBorder(ColorConstants.black, 1));
    }

    public static FlowTextFigure create() {
        return new FlowTextFigure();
    }

    public static FlowTextFigure create(String text) {
        FlowTextFigure hl = new FlowTextFigure();
        hl.appendText(text);
        return hl;
    }

    public FlowTextFigure appendLink(String linkText, MouseListener mouseListener) {
        TextLinkFlow tfl = new TextLinkFlow(linkText);
        tfl.addMouseListener(mouseListener);
        fp.add(tfl);
        return this;
    }

    public FlowTextFigure appendImage(Image img) {
        ImageFlowAdapter flow = new ImageFlowAdapter();
        flow.add(new Label(img));
        flow.setLayoutManager(new StackLayout());
        fp.add(flow);
        return this;
    }

    public FlowTextFigure appendText(String text) {
        fp.add(new TextFlow(text));
        return this;
    }

    public IFigure getFigure() {
        return fp;
    }

    class TextLinkFlow extends TextFlow {

        public TextLinkFlow(String s) {
            super(s);
            setBorder(new UnderlineBorder());
            setForegroundColor(LINK_COLOR);
            setCursor(Cursors.HAND);
        }

        class UnderlineBorder extends AbstractFlowBorder {

            @Override
            public Insets getInsets(IFigure figure) {
                return new Insets(0, 0, -1, 0);
            }

            @Override
            public void paint(FlowFigure figure, Graphics g, Rectangle where, int sides) {
                PointList points = new PointList(2);
                where.resize(-1, -1);
                points.addPoint(where.getBottomLeft());
                points.addPoint(where.getBottomRight());
                g.drawPolyline(points);
            }

        }

    }
}