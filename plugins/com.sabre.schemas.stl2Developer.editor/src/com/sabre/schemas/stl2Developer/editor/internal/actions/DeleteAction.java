package com.sabre.schemas.stl2Developer.editor.internal.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.requests.GroupRequest;
import org.eclipse.gef.ui.parts.AbstractEditPartViewer;

public class DeleteAction extends GEFAction {

    public DeleteAction(AbstractEditPartViewer viewer) {
        super(viewer);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        execute(createDeleteCommand(getSelection().toList()));
    }

    @Override
    public boolean isEnabled() {
        @SuppressWarnings("unchecked")
        Command cmd = createDeleteCommand(getSelection().toList());
        if (cmd == null)
            return false;
        return cmd.canExecute();
    }

    public Command createDeleteCommand(List<EditPart> objects) {
        if (objects.isEmpty())
            return null;
        if (!(objects.get(0) instanceof EditPart))
            return null;

        CompoundCommand compoundCmd = new CompoundCommand(getText());
        for (Command cmd : createDeleteCommands(objects)) {
            compoundCmd.add(cmd);
        }

        return compoundCmd;
    }

    public static List<Command> createDeleteCommands(List<EditPart> editParts) {
        GroupRequest deleteReq = new GroupRequest(RequestConstants.REQ_DELETE);
        deleteReq.setEditParts(editParts);

        List<Command> ret = new ArrayList<Command>();
        for (EditPart ep : editParts) {
            Command cmd = ep.getCommand(deleteReq);
            if (cmd != null)
                ret.add(cmd);
        }
        return ret;
    }

}
