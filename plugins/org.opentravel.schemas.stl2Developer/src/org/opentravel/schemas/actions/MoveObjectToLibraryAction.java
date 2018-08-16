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

import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.properties.StringProperties;
import org.opentravel.schemas.stl2developer.DialogUserNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Enabled by adding/removing actions from the menus by NavigatorMenus.
 * 
 * @author Dave Hollander
 * 
 */
public class MoveObjectToLibraryAction extends OtmAbstractAction {
	private static final Logger LOGGER = LoggerFactory.getLogger(MoveObjectToLibraryAction.class);

	private final LibraryNode library;

	/**
	 *
	 */
	public MoveObjectToLibraryAction(final StringProperties props, final LibraryNode library) {
		super(props);
		this.library = library;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run() {
		moveSelectedToLibrary(library);
	}

	/**
	 * Move all of the selected nodes to the destination library.
	 * 
	 * @param destination
	 */
	public void moveSelectedToLibrary(final LibraryNode destination) {
		if (destination == null) {
			LOGGER.warn("Object move impossible - destination is null");
			return;
		}
		final List<ComponentNode> sourceNodes = mc.getSelectedComponents_NavigatorView();

		// Hold onto a sibling to keep focus on after the move.
		if (sourceNodes.size() > 0) {
			Node refocus = sourceNodes.get(0).getParent();
			for (final ComponentNode sourceNode : sourceNodes) {
				if (!destination.equals(sourceNode.getLibrary())) {
					moveNode(sourceNode, destination);
				}
			}
			mc.refresh();
			mc.setCurrentNode_NavigatorView(refocus);
		} else {
			LOGGER.warn("No valid nodes to move to " + destination.getName());
		}
	}

	// Allow junit to access this method
	protected void moveNode(final ComponentNode source, final LibraryNode destination) {
		String warning = null;
		if (destination == null)
			warning = "Destination library is missing.";
		else if (!(source instanceof LibraryMemberInterface))
			warning = "Object must be an library member object.";
		else if (source.getLibrary() == null)
			warning = source + " does not have a library.";
		else if (!source.getLibrary().isEditable())
			warning = source.getLibrary() + " is not editable.";
		else if (!destination.isEditable())
			warning = destination + " is not editable.";
		else if (!destination.isTLLibrary())
			warning = "You can not move object to a built-in or XSD library.";
		else if (!source.getLibrary().isTLLibrary())
			warning = "You can not move object from a built-in or XSD library; use control-drag to copy.";
		else if (source.getChain() != null && source.getChain() == destination.getChain())
			warning = "You can not move object within the same library version chain.";

		// FIXME - what is correct behavior?
		if (source.getVersionNode() != null && !source.getVersionNode().getOlderVersions().isEmpty())
			LOGGER.debug("User will be confused by moving a node with older versions that are not moved.");

		if (warning == null)
			destination.addMember((LibraryMemberInterface) source);
		else
			DialogUserNotifier.openInformation("WARNING", warning);
		return;

	}

	// /**
	// * Move one node to the new destination library. Links the node, the tl object and corrects context.
	// *
	// * @param source
	// * @param destination
	// */
	// public void moveNode(final ComponentNode source, final LibraryNode destination) {
	// if (destination == null || source == null) {
	// LOGGER.warn("Object move impossible - either destination or source is null");
	// return;
	// }
	// if (!destination.isTLLibrary()) {
	// LOGGER.warn("Cannot move to built in or XSD libraries");
	// DialogUserNotifier.openInformation("WARNING", "You can not move object to a built-in or XSD library.");
	// return;
	// }
	// // TODO - use this instead:
	// if (source.getChain().contains(destination))
	// LOGGER.debug("Source chain contains destination.");
	// if (source.getChain() != null && source.getChain() == destination.getChain()) {
	// DialogUserNotifier.openInformation("WARNING",
	// "You can not move object within the same library version chain.");
	// return;
	// }
	// if (!source.isInTLLibrary() || !(source.getTLModelObject() instanceof LibraryMember)) {
	// LOGGER.warn("Cannot move - " + source.getName() + " node is not in TLLibrary or is not top level object");
	// DialogUserNotifier.openInformation("WARNING",
	// "You can not move object from a built-in or XSD library; use control-drag to copy.");
	// return;
	// }
	// try {
	// source.getLibrary().moveMember(source, destination);
	// } catch (Exception e) {
	// LOGGER.error("Move Exception: " + e.getLocalizedMessage());
	// DialogUserNotifier.openError("Move Error", e.getLocalizedMessage());
	// return;
	// }
	// // LOGGER.info("Moved " + source + " to " + destination);
	// }
}
