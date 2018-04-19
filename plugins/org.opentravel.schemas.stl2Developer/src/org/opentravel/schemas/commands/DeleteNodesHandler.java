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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.actions.ActionFactory;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.VersionNode;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.libraries.LibraryNavNode;
import org.opentravel.schemas.properties.Messages;
import org.opentravel.schemas.stl2developer.DialogUserNotifier;

/**
 * Command Handler for deleting nodes from the model.
 * 
 * Enabled by {@link org.opentravel.schemas.testers.NodeTester#canDelete()} IS_DELETEABLE which simply invokes
 * isDeletable() on each node.
 * 
 * @author Dave Hollander
 * 
 */
public class DeleteNodesHandler extends OtmAbstractHandler {

	public static final String COMMAND_ID = ActionFactory.DELETE.getCommandId();

	// private final static Logger LOGGER = LoggerFactory.getLogger(DeleteNodesHandler.class);

	@Override
	public Object execute(ExecutionEvent exEvent) throws ExecutionException {
		List<Node> selectedNodes = mc.getGloballySelectNodes();
		if (isEnabled())
			deleteNodes(selectedNodes);
		return null;
	}

	// dmh - 3/23/2018 - moved control here and commented out section in plugin.xml
	@Override
	public boolean isEnabled() {
		List<Node> selectedNodes = mc.getGloballySelectNodes();
		if (selectedNodes != null && !selectedNodes.isEmpty()) {
			Node newSelection = selectedNodes.get(0);
			if (newSelection != null && !(newSelection instanceof LibraryNavNode))
				return newSelection.isDeleteable();
		}
		return false;
	}

	/**
	 * Guide the user through deleting the list of nodes. Inform them of which ones will not be deleted. Ask them to
	 * confirm deleting the rest. Use node to actually delete the nodes.
	 * 
	 * @param deleteList
	 */
	protected void deleteNodes(final List<Node> deleteList) {
		if (deleteList == null || deleteList.isEmpty())
			return;

		// find out if any of the nodes can not be deleted.
		final List<Node> toDelete = new ArrayList<>();
		final StringBuilder doNotDelete = new StringBuilder();
		int i = 0;
		for (final Node n : deleteList) {
			if (n.isDeleteable()) {
				toDelete.add(n);
			} else {
				if (i++ > 0)
					doNotDelete.append(", ");
				doNotDelete.append(n.getName());
			}
		}

		if (i > 0)
			// DialogUserNotifier.openWarning("Object Delete", "The following nodes are not allowed to be deleted: "
			DialogUserNotifier.openWarning(Messages.getString("OtmW.122"),
					Messages.getString("OtmW.123") + doNotDelete);

		// Make the user confirm the list of nodes to be deleted.
		if (toDelete.size() > 0) {
			final String listing = Messages.getString("OtmW.121"); //$NON-NLS-1$
			final StringBuilder sb = new StringBuilder();
			i = 0;
			for (final INode n : toDelete) {
				if (i++ > 0)
					sb.append(", ");
				sb.append(n.getName());
			}
			if (DialogUserNotifier.openConfirm(Messages.getString("OtmW.120"), listing + sb.toString())) {
				// LOGGER.debug("Deleting the objects: " + sb.toString());
				// Determine where to focus when delete is done
				Node currentNode = ((Node) mc.getCurrentNode_TypeView());
				Node focusNode = null;
				if (currentNode != null) {
					focusNode = (Node) currentNode.getOwningComponent();
					while (toDelete.contains(focusNode)) {
						focusNode = focusNode.getParent();
					}
					// If in a versioned chain, go to the containing nav node
					if (focusNode instanceof VersionNode)
						focusNode = focusNode.getParent();
				}

				for (final INode n : toDelete)
					n.delete();

				mc.refresh(focusNode);
				mc.selectNavigatorNodeAndRefresh(focusNode);
				// LOGGER.debug("Delete done. Refreshing views to display:", focusNode.getNameWithPrefix());
			}
		}
	}

}
