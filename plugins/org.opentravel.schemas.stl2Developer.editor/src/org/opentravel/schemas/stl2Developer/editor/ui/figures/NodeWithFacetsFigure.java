package org.opentravel.schemas.stl2Developer.editor.ui.figures;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.swt.graphics.Color;

public class NodeWithFacetsFigure extends Figure {
    public static Color classColor = new Color(null, 255, 255, 206);

    private List<FacetFigure> facets = new ArrayList<FacetFigure>();

    public NodeWithFacetsFigure(Label name) {
        ToolbarLayout layout = new ToolbarLayout();
        setLayoutManager(layout);
        setBorder(new LineBorder(ColorConstants.black, 1));
        setBackgroundColor(classColor);
        setOpaque(true);
        add(name);
    }

    public FacetFigure addFacet(String title) {
        FacetFigure ff = new FacetFigure(title);
        facets.add(ff);
        add(ff);
        return ff;
    }

}