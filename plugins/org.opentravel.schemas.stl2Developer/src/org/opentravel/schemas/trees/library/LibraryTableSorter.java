
package org.opentravel.schemas.trees.library;

import org.eclipse.jface.viewers.ViewerSorter;
import org.opentravel.schemas.node.INode;
import org.opentravel.schemas.node.properties.PropertyNode;

public class LibraryTableSorter extends ViewerSorter {
    @Override
    public int category(final Object element) {
        final INode n = (INode) element;
        if (n instanceof PropertyNode) {
            if (n.getName().equals("Attribute")) {
                return 0;
            } else if (n.getName().equals("Element")) {
                return 2;
            } else if (n.getName().equals("Indicator")) {
                return 1;
            }
        }
        return 0;
    }
}
