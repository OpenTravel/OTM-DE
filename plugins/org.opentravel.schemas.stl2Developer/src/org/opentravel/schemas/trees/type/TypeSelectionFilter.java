
package org.opentravel.schemas.trees.type;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.ViewerFilter;
import org.opentravel.schemas.node.LibraryNode;
import org.opentravel.schemas.node.Node;

/**
 * Viewer filter that adds an additional method to verify that the selection chosen by the user is a
 * valid type.
 * 
 * @author S. Livezey
 */
public abstract class TypeSelectionFilter extends ViewerFilter {

    /**
     * Returns true if the given node is a valid selection for the type selection page.
     * 
     * @param n
     *            the node instance to evaluate
     * @return boolean
     */
    public abstract boolean isValidSelection(Node n);

    /**
     * Returns true if the given node has one or more immediate children that would be considered
     * valid selections by this filter.
     * 
     * @param n
     *            the node to analyze
     * @return boolean
     */
    protected boolean hasValidChildren(Node n) {
        boolean hasValidChild = false;

        // Only the top level member of an xsd type can be used.
        if (n.isXsdType())
            return false;

        for (Node child : n.getChildren_TypeProviders()) {
            if (isValidSelection(child)) {
                hasValidChild = true;
                break;
            }
        }
        if (!hasValidChild && n.isService()) {
            for (Node child : n.getChildren()) {
                if (isValidSelection(child)) {
                    hasValidChild = true;
                    break;
                }
            }
        }

        // If we could not find an immediate child that was valid, recurse to see if any of the
        // deeper ancestors are valid
        if (!hasValidChild) {
            Set<Node> children = new HashSet<Node>(n.getChildren_TypeProviders());
            List<Node> navChildren = n.getNavChildren();

            if (navChildren != null)
                children.addAll(navChildren);

            if (n instanceof LibraryNode) {
                LibraryNode libNode = (LibraryNode) n;

                if (libNode.getServiceRoot() != null) {
                    children.add(libNode.getServiceRoot());
                }
            } else if (n.isService()) {
                children.addAll(n.getChildren());
            }

            for (Node child : children) {
                if (hasValidChildren(child)) {
                    hasValidChild = true;
                    break;
                }
            }
        }

        return hasValidChild;
    }

}
