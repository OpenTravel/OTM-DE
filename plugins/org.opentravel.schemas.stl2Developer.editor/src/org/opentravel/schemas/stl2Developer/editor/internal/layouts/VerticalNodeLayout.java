
package org.opentravel.schemas.stl2Developer.editor.internal.layouts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.opentravel.schemas.stl2Developer.editor.model.Diagram;
import org.opentravel.schemas.stl2Developer.editor.model.UINode;

/**
 * @author Pawel Jedruch
 * 
 */
public class VerticalNodeLayout implements INodeLayout {

    private static final int VERTICAL_GAP = 20;
    private static final int HORIZONTAL_GAP = 50;

    @Override
    public Map<UINode, Point> getConstraints(Diagram model, Map<UINode, Dimension> context,
            Rectangle size) {
        Map<UINode, Point> ret = new HashMap<UINode, Point>(model.getTopLevels().size());
        Map<UINode, GraphNode> graph = buildGraph(model);
        Collection<GraphNode> mostLeft = findMostLefts(graph);

        // layout first row
        int xOffset = layoutHorizontally(ret, 0, 0, toNodes(mostLeft), context) + HORIZONTAL_GAP;

        // layout each chains
        int currentY = 0;
        for (GraphNode onLeft : mostLeft) {
            layoutChain(ret, xOffset, currentY, onLeft, context);
            currentY = findMaxY(onLeft, ret, context, new HashSet<GraphNode>()) + VERTICAL_GAP;
        }

        Point t = findTransormationVector(size.getCenter(), ret, context);
        for (Point p : ret.values()) {
            p.translate(t);
        }
        return ret;
    }

    /**
     * 
     * @param pageCenter
     *            - the diagram center
     * @param points
     *            - the upper left corner for all nodes
     * @return transformation point to shift all nodes to be placed on center of page
     */
    private Point findTransormationVector(Point pageCenter, Map<UINode, Point> ret,
            Map<UINode, Dimension> context) {
        Point min = new Point();
        Point max = new Point();
        for (Entry<UINode, Point> e : ret.entrySet()) {
            Rectangle bounds = Rectangle.SINGLETON.setLocation(e.getValue()).setSize(
                    context.get(e.getKey()));
            min = Point.min(bounds.getBottomLeft(), min);
            max = Point.max(bounds.getBottomRight(), max);
        }
        Rectangle r = new Rectangle(min, max);
        Point rC = r.getCenter();

        return new Point(pageCenter.x - rC.x, pageCenter.y - rC.y);
    }

    private void layoutChain(Map<UINode, Point> act, int offsetX, int offsetY, GraphNode onLeft,
            Map<UINode, Dimension> context) {
        layoutChain(act, offsetX, offsetY, onLeft, context, new HashSet<GraphNode>());
    }

    private void layoutChain(Map<UINode, Point> act, int offsetX, int offsetY, GraphNode onLeft,
            Map<UINode, Dimension> context, Set<GraphNode> visited) {
        if (visited.contains(onLeft)) {
            return;
        }
        visited.add(onLeft);
        Collection<GraphNode> onRights = onLeft.rights;
        offsetX += layoutHorizontally(act, offsetX, offsetY, toNodes(onRights), context)
                + HORIZONTAL_GAP;
        for (GraphNode onRight : onRights) {
            layoutChain(act, offsetX, offsetY, onRight, context, visited);
        }
    }

    private int findMaxY(GraphNode onLeft, Map<UINode, Point> ret, Map<UINode, Dimension> context,
            Set<GraphNode> visited) {
        int maxy = 0;
        GraphNode bottom = onLeft;
        while (bottom != null && !visited.contains(bottom)) {
            maxy = Math.max(maxy, ret.get(bottom.topLevel).y + context.get(bottom.topLevel).height);
            bottom = bottom.rights.peekLast();
            visited.add(bottom);
        }
        return maxy;
    }

    private UINode findToLevel(UINode n) {
        return n.getTopLevelParent();
    }

    private int layoutHorizontally(Map<UINode, Point> act, int xOffset, int offsetY,
            Collection<UINode> nodes, Map<UINode, Dimension> context) {
        int currentY = offsetY;
        int maxWidth = 0;
        for (UINode n : nodes) {
            if (n.isTopLevel() && !act.containsKey(n)) {
                act.put(n, new Point(xOffset, currentY));
                currentY = currentY + context.get(n).height + VERTICAL_GAP;
                maxWidth = Math.max(maxWidth, context.get(n).width);
            }
        }
        return maxWidth;
    }

    private Collection<GraphNode> findMostLefts(Map<UINode, GraphNode> graph) {
        List<GraphNode> ret = new ArrayList<GraphNode>(graph.size());
        for (GraphNode n : graph.values()) {
            if (!n.lefts.isEmpty())
                continue;
            ret.add(n);
        }
        return ret;
    }

    private Collection<UINode> getNodesOnLeft(UINode node) {
        return convertToTopLevels(node.getConnectedAsTarget());
    }

    private Collection<UINode> getNodesOnRight(UINode node) {
        return convertToTopLevels(node.getConnectedAsSource());
    }

    private Collection<UINode> convertToTopLevels(Collection<UINode> nodes) {
        List<UINode> ret = new ArrayList<UINode>();
        for (UINode so : nodes) {
            ret.add(findToLevel(so));
        }
        return ret;
    }

    private Collection<UINode> toNodes(Collection<GraphNode> graphs) {
        Collection<UINode> ret = new ArrayList<UINode>();
        for (GraphNode n : graphs) {
            ret.add(n.topLevel);
        }
        return ret;
    }

    public Map<UINode, GraphNode> buildGraph(Diagram diag) {
        Map<UINode, GraphNode> g = new HashMap<UINode, VerticalNodeLayout.GraphNode>();
        for (UINode n : diag.getAllNodes()) {
            // skip unlinked nodes
            if (n.isUnlinked())
                continue;
            UINode parent = n.getTopLevelParent();
            GraphNode parentG = findOrCreate(parent, g);
            for (UINode onLeft : getNodesOnLeft(n)) {
                GraphNode connected = findOrCreate(onLeft, g);
                if (parentG != connected)
                    parentG.lefts.add(connected);
            }
            for (UINode onRight : getNodesOnRight(n)) {
                GraphNode connected = findOrCreate(onRight, g);
                if (parentG != connected)
                    parentG.rights.add(connected);
            }
        }
        return g;
    }

    private GraphNode findOrCreate(UINode parent, Map<UINode, GraphNode> g) {
        GraphNode n = g.get(parent);
        if (n == null) {
            n = new GraphNode(parent);
            g.put(parent, n);
        }
        return n;
    }

    class GraphNode {
        LinkedList<GraphNode> lefts = new LinkedList<VerticalNodeLayout.GraphNode>();
        LinkedList<GraphNode> rights = new LinkedList<VerticalNodeLayout.GraphNode>();
        UINode topLevel;

        public GraphNode(UINode topLevel) {
            if (!topLevel.isTopLevel()) {
                throw new IllegalStateException();
            }
            this.topLevel = topLevel;
        }

    }
}
