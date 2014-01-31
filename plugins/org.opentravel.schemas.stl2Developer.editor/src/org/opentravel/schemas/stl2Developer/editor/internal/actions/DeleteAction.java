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
package org.opentravel.schemas.stl2Developer.editor.internal.actions;

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
