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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.libraries.LibraryNavNode;

/**
 * 
 * @author Dave Hollander
 * 
 */

public class CloseLibrariesHandler extends OtmAbstractHandler {

	public static String COMMAND_ID = "org.opentravel.schemas.commands.CloseLibraries";

	private List<LibraryNavNode> toClose = new ArrayList<LibraryNavNode>();

	/**
	 * Close one or more libraries using library controller
	 */
	@Override
	public Object execute(ExecutionEvent exEvent) throws ExecutionException {
		if (isEnabled())
			// mc.getLibraryController().remove(toClose);
			// for (LibraryNavNode lnn : toClose)
			// lnn.close();
			mc.getProjectController().remove(toClose);

		return null;
	}

	@Override
	public String getID() {
		return COMMAND_ID;
	}

	/**
	 * Determine if selected navigator nodes are in libraries or chains.
	 * 
	 * Side effect of setting toClose to contain all libraries or chain heads.
	 * 
	 * @return true if toClose is not empty
	 */
	@Override
	public boolean isEnabled() {
		toClose.clear();
		List<Node> nodes = mc.getSelectedNodes_NavigatorView();
		for (Node n : nodes) {
			// Only library nav nodes know which project the library is in.
			if (n instanceof LibraryNavNode)
				toClose.add((LibraryNavNode) n);

			// if (n == null)
			// continue;
			// if (n instanceof LibraryNavNode)
			// n = (Node) ((LibraryNavNode) n).getThisLib();
			// else {
			// n = n.getLibrary();
			// if (((LibraryNode) n).isInChain())
			// n = n.getChain();
			// }
			// if (!toClose.contains(n))
			// toClose.add(n);
		}
		return !toClose.isEmpty();
	}
}
