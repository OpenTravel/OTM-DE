
package org.opentravel.schemas.stl2Developer.editor.parts;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.ToolbarLayout;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.stl2Developer.editor.model.UINode;

/**
 * @author Pawel Jedruch
 * 
 */
public class UnsupportedNodeEditPart extends GenericEditPart<Node> {

    public UnsupportedNodeEditPart(UINode model) {
        super(model);
    }

    @Override
    protected IFigure createFigure() {
        return new UnsupporetedFigure();
    }

    public class UnsupporetedFigure extends RectangleFigure {

        public UnsupporetedFigure() {
            ToolbarLayout layout = new ToolbarLayout();
            layout.setMinorAlignment(ToolbarLayout.ALIGN_CENTER);
            layout.setStretchMinorAxis(false);
            layout.setSpacing(2);
            setLayoutManager(layout);
            setPreferredSize(100, 100);
            add(new Label("???: " + getNodeModel().getName()));
        }

    }

}
