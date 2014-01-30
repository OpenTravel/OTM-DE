
package org.opentravel.schemas.stl2Developer.editor.commands;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.commands.Command;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.stl2Developer.editor.model.Diagram;
import org.opentravel.schemas.stl2Developer.editor.model.UINode;

/**
 * @author Pawel Jedruch
 * 
 */
public class AddNodeToDiagram extends Command {

    private Node newNode;
    private Diagram diagram;
    private Point location;

    public AddNodeToDiagram(Node newObject, Diagram model, Point location) {
        this.newNode = newObject;
        this.diagram = model;
        this.location = location;
    }

    @Override
    public void execute() {
        diagram.addChild(UINode.getOwner(newNode), location);
    }

    @Override
    public boolean canExecute() {
        return newNode != null && newNode.getOwningComponent() != null
                && newNode instanceof ComponentNode;
    }
}
