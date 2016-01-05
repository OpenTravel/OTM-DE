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
package org.opentravel.schemas.commands;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.views.OtmView;

/**
 * 
 * @author Dave Hollander
 * 
 */
public class RefreshViewHandler extends OtmAbstractHandler {
	// private static final Logger LOGGER = LoggerFactory.getLogger(SortNodeHandler.class);
	public static final String COMMAND_ID = "org.opentravel.schemas.commands.RefreshView";
	public static final String COMMAND_ID_MASTER = "org.opentravel.schemas.commands.RefreshView:MASTER";

	private String mode = "";

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		// I didn't make it an extension point like navigationCommand so use command id.
		if (event.getCommand().getId().equalsIgnoreCase(COMMAND_ID_MASTER))
			mc.refreshMaster();
		else {
			IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
			if (activePart instanceof OtmView) {
				OtmView view = (OtmView) activePart;
				INode currentNavigationNode = getSelectedNode();
				view.refresh(currentNavigationNode, true);
			}
		}
		return null;
	}

	private INode getSelectedNode() {
		INode node = OtmRegistry.getNavigatorView().getCurrentNode();
		return node;
	}

}
