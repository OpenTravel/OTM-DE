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
package org.opentravel.schemas.stl2Developer.editor.ui.figures;

import org.eclipse.draw2d.AbstractBorder;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;

public class FacetFigure extends Figure {

    private static Font FacetFont = new Font(null, "Arial", 10, SWT.BOLD);

    public FacetFigure(String title) {
        this(new Label(title));
    }

    public FacetFigure(Label labelTitle) {
        ToolbarLayout layout = new ToolbarLayout();
        layout.setMinorAlignment(ToolbarLayout.ALIGN_TOPLEFT);
        layout.setStretchMinorAxis(false);
        layout.setSpacing(2);
        layout.setStretchMinorAxis(true);
        setLayoutManager(layout);
        setBorder(new CompartmentFigureBorder());
        setBorder(new LineBorder());
        labelTitle.setTextAlignment(PositionConstants.CENTER);
        labelTitle.setFont(FacetFont);
        labelTitle.setBackgroundColor(ColorConstants.lightGray);
        labelTitle.setOpaque(true);
        add(labelTitle);
    }

    class CompartmentFigureBorder extends AbstractBorder {
        @Override
        public Insets getInsets(IFigure figure) {
            return new Insets(1, 0, 0, 0);
        }

        @Override
        public void paint(IFigure figure, Graphics graphics, Insets insets) {
            graphics.drawLine(getPaintRectangle(figure, insets).getTopLeft(),
                    tempRect.getTopRight());
        }
    }

}