/**
 * 
 */
package com.sabre.schemas.trees.library;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import com.sabre.schemas.node.Node;

/**
 * Filter out types assigned to properties by not selecting nodes whose parents are properties. This
 * filter is initially on for the model navigator view.
 * 
 * @author Dave Hollander
 * 
 */
public class LibraryPropertyOnlyFilter extends ViewerFilter {
    // private static final Logger LOGGER =
    // LoggerFactory.getLogger(LibraryTreeContentProvider.class);

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer,
     * java.lang.Object, java.lang.Object)
     */
    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
        if ((parentElement instanceof Node) && (element instanceof Node)) {
            final Node n = (Node) parentElement;
            // LOGGER.debug( "Is node property type? "+n.isProperty());
            if (n.isProperty())
                return false;
            else
                return true;
        }
        return false;
    }

}
