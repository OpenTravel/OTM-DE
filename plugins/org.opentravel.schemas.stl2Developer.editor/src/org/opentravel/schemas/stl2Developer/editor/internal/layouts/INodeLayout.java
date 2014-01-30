
package org.opentravel.schemas.stl2Developer.editor.internal.layouts;

import java.util.Collections;
import java.util.Map;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.opentravel.schemas.stl2Developer.editor.model.Diagram;
import org.opentravel.schemas.stl2Developer.editor.model.UINode;

/**
 * @author Pawel Jedruch
 * 
 */
public interface INodeLayout {

    /**
     * @param model
     *            - model to layout
     * @param context
     *            - UINodes size
     * @param size
     *            - the bounds in which the layout can place the nodes.
     * @return
     */
    Map<UINode, Point> getConstraints(Diagram model, Map<UINode, Dimension> context, Rectangle size);

    public class Stub implements INodeLayout {

        @Override
        public Map<UINode, Point> getConstraints(Diagram model, Map<UINode, Dimension> context,
                Rectangle size) {
            return Collections.emptyMap();
        }

    }

}
