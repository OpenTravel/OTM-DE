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
package org.opentravel.schemas.actions;

import java.util.List;

import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.properties.StringProperties;
import org.opentravel.schemas.stl2developer.MainWindow;

/**
 * Implements the "Copy" menu action.
 * 
 * {@link ImportObjectToLibraryAction}
 * 
 * @author Dave Hollander
 * 
 */
public class CopyNodeAction extends OtmAbstractAction {

	/**
	 *
	 */
	public CopyNodeAction(final MainWindow mainWindow, final StringProperties props) {
		super(mainWindow, props);
	}

	@Override
	public void run() {
		copySelectedNodes(mc.getSelectedNodes_NavigatorView());
	}

	@Override
	public boolean isEnabled() {
		// Enabled if this is unmanaged or the head is editable and not patch
		LibraryNode ln = mc.getSelectedNode_NavigatorView().getLibrary();
		if (ln == null)
			return false;
		if (!ln.isManaged())
			return ln.isEditable();
		return (ln.getChain().getHead().isEditable() && !ln.getChain().getHead().isPatchVersion());
	}

	/**
	 * Implements the Copy action.
	 */
	private final String CopyNameSuffix = "_Copy";

	// This is called when copying via navigator menus.
	// 9/4/2018 - nav menu, right click->copy with an object selected.
	// See ImportObject...(link above)
	public void copySelectedNodes(List<Node> nodes) {
		Node lastCloned = null;
		if (nodes == null || nodes.isEmpty())
			return;

		// Determine where to put the new nodes.
		LibraryNode targetLibrary = nodes.get(0).getLibrary();
		if (!nodes.get(0).getLibrary().isEditable())
			targetLibrary = nodes.get(0).getLibrary().getHead();
		// targetLibrary = nodes.get(0).getChain().getHead();

		LibraryMemberInterface clone = null;
		for (Node n : nodes) {
			if (n instanceof LibraryMemberInterface) {
				clone = ((LibraryMemberInterface) n).clone(targetLibrary, CopyNameSuffix);
				if (clone != null) {
					if (targetLibrary != null)
						targetLibrary.addMember(clone);
					lastCloned = (Node) clone;
				}
			}
		}
		if (lastCloned != null) {
			mc.selectNavigatorNodeAndRefresh(lastCloned);
		}
	}

}
