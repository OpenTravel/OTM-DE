
package org.opentravel.schemas.trees.library;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.opentravel.schemas.node.Node;

public class LibraryTreeInheritedFilter extends ViewerFilter {

    // private static final Logger LOGGER =
    // LoggerFactory.getLogger(LibraryTreeContentProvider.class);

    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
        if (element instanceof Node) {
            // LOGGER.debug(
            // "Is node "+((Node)element).getName()+" inherited? "+((Node)element).isInheritedProperty());
            if (((Node) element).isInheritedProperty())
                return false;
            else
                return true;
        }
        // LOGGER.debug( "select sent an element that is not a node. "+element.getClass());
        return false;
    }

}
