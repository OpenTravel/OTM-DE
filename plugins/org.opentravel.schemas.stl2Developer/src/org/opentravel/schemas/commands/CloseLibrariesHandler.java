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
import org.opentravel.schemas.node.LibraryChainNode;
import org.opentravel.schemas.node.LibraryNode;
import org.opentravel.schemas.node.Node;

/**
 * 
 * @author Dave Hollander
 * 
 */

public class CloseLibrariesHandler extends OtmAbstractHandler {

	public static String COMMAND_ID = "org.opentravel.schemas.commands.CloseLibraries";

	private List<Node> toClose = new ArrayList<Node>();

	@Override
	public Object execute(ExecutionEvent exEvent) throws ExecutionException {
		if (isEnabled())
			mc.getLibraryController().remove(toClose);

		return null;
	}

	@Override
	public String getID() {
		return COMMAND_ID;
	}

	@Override
	public boolean isEnabled() {
		toClose.clear();
		List<Node> nodes = mc.getSelectedNodes_NavigatorView();
		for (Node n : nodes) {
			LibraryNode l = n.getLibrary();
			if (n.getLibrary() == null)
				continue;
			if (l.isInChain())
				n = l.getChain();
			if (n instanceof LibraryChainNode) {
				l = ((LibraryChainNode) n).getHead();
			}
			if (toClose.contains(l))
				continue;
			toClose.add(l);
		}
		return !toClose.isEmpty();
	}
}
