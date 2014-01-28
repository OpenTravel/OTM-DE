/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemas.actions;

import java.util.List;

import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.LibraryNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.properties.StringProperties;
import org.opentravel.schemas.stl2developer.DialogUserNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Agnieszka Janowska
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

    /**
     * Move one node to the new destination library. Links the node, the tl object and corrects
     * context.
     * 
     * @param source
     * @param destination
     */
    public void moveNode(final ComponentNode source, final LibraryNode destination) {
        if (destination == null || source == null) {
            LOGGER.warn("Object move impossible - either destination or source is null");
            return;
        }
        if (!destination.isTLLibrary()) {
            LOGGER.warn("Cannot move to built in or XSD libraries");
            DialogUserNotifier.openInformation("WARNING",
                    "You can not move object to a built-in or XSD library.");
            return;
        }
        if (source.getChain() == destination.getChain()) {
            DialogUserNotifier.openInformation("WARNING",
                    "You can not move object within the same library version chain.");
            return;
        }
        if (source.isInTLLibrary() && source.isTopLevelObject()) {
            source.getLibrary().moveMember(source, destination);
            // done in the moveMember - destination.addContext(source);
        } else {
            LOGGER.warn("Cannot move - " + source.getName()
                    + " node is not in TLLibrary or is not top level object");
            LOGGER.debug("source in tllib? " + source.isInTLLibrary()
                    + "  and is top level object? " + source.isTopLevelObject());
            DialogUserNotifier
                    .openInformation("WARNING",
                            "You can not move object from a built-in or XSD library; use control-drag to copy.");
            return;
        }
        LOGGER.info("Moved node " + source.getName() + " to node " + destination.getName());
    }

}
