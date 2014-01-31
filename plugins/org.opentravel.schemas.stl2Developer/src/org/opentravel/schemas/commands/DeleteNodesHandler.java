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
/**
 * 
 */
package org.opentravel.schemas.commands;

import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.actions.ActionFactory;
import org.opentravel.schemas.node.Node;

/**
 * Command Handler for deleting nodes from the model .
 * 
 * @author Dave Hollander
 * 
 */
public class DeleteNodesHandler extends OtmAbstractHandler {

    public static final String COMMAND_ID = ActionFactory.DELETE.getCommandId();

    @Override
    public Object execute(ExecutionEvent exEvent) throws ExecutionException {
        List<Node> selectedNodes = mc.getGloballySelectNodes();
        mc.getNodeModelController().deleteNodes(selectedNodes);
        return null;
    }

}
