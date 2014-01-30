
package org.opentravel.schemas.trees.repository;

import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.graphics.Image;
import org.opentravel.schemas.trees.repository.RepositoryNode.BaseNamespaceNode;
import org.opentravel.schemas.trees.repository.RepositoryNode.RepositoryChainNode;
import org.opentravel.schemas.trees.repository.RepositoryNode.RepositoryItemNode;

/**
 * Provides tree view content of only type providers.
 * 
 * @author Dave Hollander
 * 
 */
public class RepositoryTreeContentProvider implements ITreeContentProvider {

    private static final Object[] EMPTY_ARRAY = new Object[0];

    @Override
    public Object[] getElements(final Object element) {
        if (element instanceof RepositoryNode) {
            return ((RepositoryNode) element).getChildren().toArray();
        } else {
            return EMPTY_ARRAY;
        }
    }

    @Override
    public Object[] getChildren(final Object element) {
        if (element instanceof RepositoryNode) {
            RepositoryNode e = (RepositoryNode) element;
            return e.getChildren().toArray();
        }
        return EMPTY_ARRAY;
    }

    @Override
    public boolean hasChildren(final Object element) {
        if (element instanceof RepositoryItemNode) {
            return false;
        }
        return true;
    }

    @Override
    public Object getParent(final Object element) {
        if (element instanceof RepositoryNode) {
            return ((RepositoryNode) element).getParent();
        }
        return null;
    }

    public static class RepositoryTreeSorter extends ViewerSorter {

        @Override
        public int compare(Viewer viewer, Object e1, Object e2) {
            int cat1 = category(e1);
            int cat2 = category(e2);

            if (cat1 != cat2) {
                return cat1 - cat2;
            }

            if (e1 instanceof RepositoryNode && e2 instanceof RepositoryNode) {
                RepositoryNode r1 = (RepositoryNode) e1;
                RepositoryNode r2 = (RepositoryNode) e2;
                return r1.compareTo(r2);
            }
            return super.compare(viewer, e1, e2);
        }

        @Override
        public int category(Object element) {
            if (element instanceof BaseNamespaceNode) {
                return 10;
            } else if (element instanceof RepositoryChainNode) {
                return 5;
            }
            return super.category(element);
        }

    }

    static class RepositoryTreeLabelProvider extends LabelProvider implements IStyledLabelProvider {

        @Override
        public String getText(final Object element) {
            if (element instanceof RepositoryNode) {
                return ((RepositoryNode) element).getName();
            }
            return "Unknown object type";
        }

        @Override
        public Image getImage(final Object element) {
            if (element instanceof RepositoryNode) {
                return ((RepositoryNode) element).getImage();
            }
            return null;
        }

        @Override
        public StyledString getStyledText(Object element) {
            return new StyledString(getText(element));
        }

    }

    @Override
    public void dispose() {
    }

    @Override
    public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
    }
}
