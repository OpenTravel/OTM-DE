
package org.opentravel.schemas.trees.type;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.opentravel.schemas.node.INode;
import org.opentravel.schemas.node.LibraryNode;
import org.opentravel.schemas.node.Node;

/**
 * Content provider used to gather tree elements for the extension selection wizard.
 * 
 * @author S. Livezey
 */
public class ExtensionTreeContentProvider implements ITreeContentProvider {

    public ExtensionTreeContentProvider() {
    }

    @Override
    public Object[] getElements(final Object element) {
        return getChildren(element);
    }

    @Override
    public Object[] getChildren(final Object element) {
        List<Node> children = new ArrayList<Node>();
        Node n = (Node) element;

        children.addAll(n.getChildren_TypeProviders());

        if (n instanceof LibraryNode) {
            LibraryNode libNode = (LibraryNode) n;

            if (libNode.getServiceRoot() != null) {
                children.add(libNode.getServiceRoot());
            }
        } else if (n.isService()) {
            children.addAll(n.getChildren());
        }
        return children.toArray();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang. Object)
     */
    @Override
    public boolean hasChildren(final Object element) {
        return (getElements(element).length > 0);
    }

    @Override
    public Object getParent(final Object element) {
        return ((INode) element).getParent();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.IContentProvider#dispose()
     */
    @Override
    public void dispose() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface
     * .viewers.Viewer, java.lang.Object, java.lang.Object)
     */
    @Override
    public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
    }

}
