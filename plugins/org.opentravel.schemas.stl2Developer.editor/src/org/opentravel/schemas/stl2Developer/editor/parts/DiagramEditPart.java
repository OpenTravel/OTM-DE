
package org.opentravel.schemas.stl2Developer.editor.parts;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.draw2d.Animation;
import org.eclipse.draw2d.ConnectionLayer;
import org.eclipse.draw2d.Cursors;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.FreeformLayout;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LayoutAnimator;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.editpolicies.NonResizableEditPolicy;
import org.eclipse.gef.editpolicies.XYLayoutEditPolicy;
import org.eclipse.gef.handles.MoveHandle;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gef.tools.DragEditPartsTracker;
import org.eclipse.swt.SWT;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.stl2Developer.editor.commands.AddNodeToDiagram;
import org.opentravel.schemas.stl2Developer.editor.commands.SetConstraintCommand;
import org.opentravel.schemas.stl2Developer.editor.internal.Features;
import org.opentravel.schemas.stl2Developer.editor.internal.layouts.INodeLayout;
import org.opentravel.schemas.stl2Developer.editor.model.Diagram;
import org.opentravel.schemas.stl2Developer.editor.model.UINode;
import org.opentravel.schemas.stl2Developer.editor.policies.ParentSelectionPolicy;

/**
 * @author Pawel Jedruch
 * 
 */
public class DiagramEditPart extends AbstractGraphicalEditPart implements PropertyChangeListener {

    private final Dimension DEFAULT_SIZE = new Dimension(-1, -1);

    public DiagramEditPart(Diagram model) {
        model.addListener(this);
        model.setOwnerEP(this);
        setModel(model);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        refresh();
    }

    @Override
    public void refresh() {
        Animation.markBegin();
        super.refresh();
        postLayout();
        Animation.run();
    }

    private void postLayout() {
        // make sure all figuers are up-to-date
        // getFigure().getUpdateManager().performUpdate();
        Rectangle size = getFigure().getClientArea();
        INodeLayout alg = Features.getLayoutAlgorithm();
        Map<UINode, Dimension> context = getUINodePosition();
        Map<UINode, Point> constrains = alg.getConstraints(getModel(), context, size);
        getModel().setListenersEnabled(false);
        for (Entry<UINode, Point> e : constrains.entrySet()) {
            GraphicalEditPart gep = (GraphicalEditPart) getViewer().getEditPartRegistry().get(
                    e.getKey());
            IFigure f = gep.getFigure();
            getFigure().setConstraint(f, new Rectangle(e.getValue(), DEFAULT_SIZE));
            e.getKey().setLocation(e.getValue());
            // remove from context. later used to update set location based on uinode loc
            context.remove(e.getKey());
        }
        getModel().setListenersEnabled(true);

        for (UINode left : context.keySet()) {
            GraphicalEditPart gep = (GraphicalEditPart) getViewer().getEditPartRegistry().get(left);
            if (left.getLocation() != null)
                getFigure().setConstraint(gep.getFigure(),
                        new Rectangle(left.getLocation(), DEFAULT_SIZE));
        }
    }

    private Map<UINode, Dimension> getUINodePosition() {
        Map<UINode, Dimension> ret = new HashMap<UINode, Dimension>(getChildren().size());
        for (Object o : getChildren()) {
            GenericEditPart<?> ep = (GenericEditPart<?>) o;
            ret.put(ep.getModel(), ep.getFigure().getPreferredSize());
        }
        return ret;
    }

    @Override
    protected IFigure createFigure() {
        Figure f = new FreeformLayer();
        f.setLayoutManager(new FreeformLayout());
        f.setBorder(new MarginBorder(5));
        // listen on layout to update each UINode size
        f.addLayoutListener(getModel());
        if (Features.isLayoutAnimationEnabled()) {
            f.addLayoutListener(LayoutAnimator.getDefault());
        }
        return f;
    }

    @Override
    protected List<UINode> getModelChildren() {
        return new ArrayList<UINode>(getModel().getTopLevels());
    }

    @Override
    protected void createEditPolicies() {
        installEditPolicy(EditPolicy.LAYOUT_ROLE, new ShapesXYLayoutEditPolicy());
    }

    @Override
    protected void addChildVisual(EditPart childEditPart, int index) {
        GenericEditPart<?> child = (GenericEditPart<?>) childEditPart;
        IFigure ff = child.getFigure();
        getContentPane().add(ff, new Rectangle(0, 0, -1, -1), index);
    }

    private static class ShapesXYLayoutEditPolicy extends XYLayoutEditPolicy {

        @Override
        protected Command getCreateCommand(CreateRequest request) {
            Rectangle constraints = (Rectangle) getConstraintFor(request);
            Node newNode = (Node) request.getNewObject();
            return new AddNodeToDiagram(newNode, ((DiagramEditPart) getHost()).getModel(),
                    constraints.getLocation());
        }

        @Override
        protected Command createChangeConstraintCommand(ChangeBoundsRequest request,
                EditPart child, Object constraint) {
            if (constraint instanceof Rectangle) {
                GraphicalEditPart gep = (GraphicalEditPart) child;
                return new SetConstraintCommand((UINode) gep.getModel(), (Rectangle) constraint);
            }
            return super.createChangeConstraintCommand(request, child, constraint);
        }

        @Override
        protected EditPolicy createChildEditPolicy(EditPart child) {
            return new BackgroundEditPolicy();
        }
    }

    static class BackgroundEditPolicy extends NonResizableEditPolicy {

        private ParentSelectionPolicy selectionPolicy = new ParentSelectionPolicy();

        @SuppressWarnings({ "rawtypes", "unchecked" })
        @Override
        protected List createSelectionHandles() {
            List list = new ArrayList();
            // createDragHandle(list, PositionConstants.NORTH_EAST);
            // createDragHandle(list, PositionConstants.NORTH_WEST);
            // createDragHandle(list, PositionConstants.SOUTH_EAST);
            // createDragHandle(list, PositionConstants.SOUTH_WEST);
            DragEditPartsTracker tracker = getDragTracker();
            SelectionHandle handle = new SelectionHandle((GraphicalEditPart) getHost());
            handle.setDragTracker(tracker);
            list.add(handle);
            return list;
        }

        @Override
        public void setHost(EditPart host) {
            super.setHost(host);
            selectionPolicy.setHost(host);
        }

        @Override
        public EditPart getTargetEditPart(Request request) {
            return selectionPolicy.getTargetEditPart(request);
        }

    }

    static class SelectionHandle extends MoveHandle {

        public SelectionHandle(GraphicalEditPart owner) {
            super(owner);
        }

        @Override
        protected void initialize() {
            setOpaque(false);
            setForegroundColor(Features.getSelectionColor());
            setBorder(new LineBorder(2));
            setCursor(Cursors.SIZEALL);
        }

        @Override
        protected void paintFigure(Graphics graphics) {
            graphics.pushState();
            super.paintFigure(graphics);
            graphics.popState();
        }

    }

    @Override
    public Diagram getModel() {
        return (Diagram) super.getModel();
    }

    @Override
    protected void refreshChildren() {
        super.refreshChildren();
        // force refresh on children
        for (Object p : getChildren()) {
            ((EditPart) p).refresh();
        }
    }

    @Override
    protected void refreshVisuals() {
        ConnectionLayer cLayer = (ConnectionLayer) getLayer(LayerConstants.CONNECTION_LAYER);
        if ((getViewer().getControl().getStyle() & SWT.MIRRORED) == 0)
            cLayer.setAntialias(SWT.ON);
        Collection<?> editParts = getViewer().getEditPartRegistry().values();
        for (Object o : editParts) {
            // refresh connections
            if (o instanceof ConnectionEditPart) {
                ((ConnectionEditPart) o).refresh();
            }
        }
    }

}
