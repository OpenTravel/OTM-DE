/*
 * Copyright (c) 2013, Sabre Inc.
 */
package org.opentravel.schemas.stl2Developer.editor.internal.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gef.requests.CreationFactory;
import org.eclipse.gef.ui.parts.AbstractEditPartViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.stl2Developer.editor.internal.GEFUtils;
import org.opentravel.schemas.stl2Developer.editor.model.Diagram;
import org.opentravel.schemas.stl2Developer.editor.model.UINode;
import org.opentravel.schemas.stl2Developer.editor.model.Diagram.Position;

/**
 * @author Pawel Jedruch
 * 
 */
public abstract class ShowHideNodeAction extends GEFAction {

    private String desc;

    enum ActionMode {
        SHOW("Show"), HIDE("Hide");
        private ActionMode(String text) {
            this.text = text;
        }

        private String text;
    }

    public ShowHideNodeAction(AbstractEditPartViewer viewer) {
        super(viewer);
    }

    public ShowHideNodeAction(AbstractEditPartViewer viewer, String label) {
        super(viewer);
        this.desc = label;
    }

    @Override
    public void run() {
        if (getSelection().isEmpty())
            return;

        switch (calculateActionMode()) {
            case SHOW:
                for (UINode uiNode : getSelectedModels()) {
                    showNodes(uiNode, getNodesToAdd(uiNode));
                }
                return;
            case HIDE:
                for (UINode uiNode : getSelectedModels()) {
                    hideNodes(getNodesToHide((uiNode)));
                }
                return;
        }
    }

    private void hideNodes(List<EditPart> nodesToHide) {
        CompoundCommand cmds = new CompoundCommand(getText());
        for (Command cmd : DeleteAction.createDeleteCommands(nodesToHide)) {
            cmds.add(cmd);
        }
        execute(cmds);
    }

    private void showNodes(UINode uiNode, List<Node> nodesToAdd) {
        List<EditPart> toSelect = new ArrayList<EditPart>();
        for (final Node newNode : nodesToAdd) {
            CreateRequest req = new CreateRequest();
            req.setFactory(new CreationFactory() {

                @Override
                public Object getObjectType() {
                    return null;
                }

                @Override
                public Object getNewObject() {
                    return newNode;
                }
            });
            Point location = getInput().findBestLocation(uiNode, newNode,
                    getInitialPosition(newNode, uiNode.getNode()));
            req.setLocation(location);
            Command cmd = getViewer().getContents().getCommand(req);
            execute(cmd);

            toSelect.add((EditPart) getViewer().getEditPartRegistry().get(
                    getInput().findUINode(newNode).getTopLevelParent()));
        }
        // select all added nodes
        getViewer().setSelection(new StructuredSelection(toSelect));
    }

    @Override
    public String getText() {
        return calculateActionMode().text + ": " + desc;
    }

    protected abstract Position getInitialPosition(Node newNode, Node referance);

    protected abstract List<Node> getNewNodes(UINode n);

    protected List<Node> getOwningComponents(Collection<Node> nodes) {
        List<Node> ret = new ArrayList<Node>();
        for (Node n : nodes) {
            ret.add(UINode.getOwner(n));
        }
        return ret;
    }

    @Override
    public boolean isEnabled() {
        if (getSelection().size() == 1) {
            if (getSelection().getFirstElement() instanceof EditPart) {
                EditPart ep = (EditPart) getSelection().getFirstElement();
                // disable for corrupted (unlinked) nodes
                if (ep.getModel() instanceof UINode) {
                    if (((UINode) ep.getModel()).isUnlinked())
                        return false;
                }
                if (ep.getModel() instanceof Diagram) {
                    return false;
                } else {
                    return isValidSelection(getSelectedModels());
                }
            }
        }
        return false;
    }

    private ActionMode calculateActionMode() {
        List<UINode> models = getSelectedModels();
        if (isEnabled()) {
            if (getAllNodesToAdd(models).isEmpty()) {
                return ActionMode.HIDE;
            }
        }
        return ActionMode.SHOW;
    }

    @SuppressWarnings("unchecked")
    private List<UINode> getSelectedModels() {
        return GEFUtils.extractModels(getSelection().toList(), UINode.class);
    }

    private List<Node> getNodesToAdd(UINode selected) {
        List<Node> ret = new ArrayList<Node>();
        for (Node node : getNewNodes(selected)) {
            UINode ui = getInput().findUINode(node);
            if (ui == null || !ui.isTopLevel()) {
                ret.add(node);
            }
        }
        return ret;
    }

    private List<EditPart> getNodesToHide(UINode selected) {
        List<EditPart> ret = new ArrayList<EditPart>();
        for (Node node : getNewNodes(selected)) {
            UINode ui = getInput().findUINode(node);
            if (ui != null && ui.isTopLevel()) {
                ret.add((EditPart) getViewer().getEditPartRegistry().get(ui));
            }
        }
        return ret;
    }

    /**
     * @param selection
     * @return all nodes to add from current selection
     */
    private List<Node> getAllNodesToAdd(List<UINode> selection) {
        List<Node> ret = new ArrayList<Node>();
        for (UINode selected : selection) {
            ret.addAll(getNodesToAdd(selected));
        }
        return ret;
    }

    protected abstract boolean isValidSelection(List<UINode> nodes);

}