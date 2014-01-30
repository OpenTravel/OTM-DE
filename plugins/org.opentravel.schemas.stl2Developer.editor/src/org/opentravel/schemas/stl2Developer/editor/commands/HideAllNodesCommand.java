
package org.opentravel.schemas.stl2Developer.editor.commands;

import org.eclipse.gef.commands.Command;
import org.opentravel.schemas.stl2Developer.editor.model.Diagram;

/**
 * @author Pawel Jedruch
 * 
 */
public class HideAllNodesCommand extends Command {

    private Diagram diagram;

    public HideAllNodesCommand(Diagram model) {
        this.diagram = model;
    }

    @Override
    public void execute() {
        diagram.removeAll();
    }

}
