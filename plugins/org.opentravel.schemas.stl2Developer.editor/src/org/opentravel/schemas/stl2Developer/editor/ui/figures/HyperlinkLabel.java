/*
 * Copyright (c) 2013, Sabre Inc.
 */
package org.opentravel.schemas.stl2Developer.editor.ui.figures;

import org.eclipse.draw2d.Cursors;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.draw2d.text.AbstractFlowBorder;
import org.eclipse.draw2d.text.FlowFigure;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

public class HyperlinkLabel extends Label {

    private final static Color HYPERLINK_COLOR = JFaceColors.getActiveHyperlinkText(Display
            .getDefault());

    public HyperlinkLabel() {
        setOpaque(false);
        setBorder(new UnderlineBorder());
        setForegroundColor(HYPERLINK_COLOR);
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
