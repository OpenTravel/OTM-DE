
package org.opentravel.schemas.trees.type;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.opentravel.schemas.node.INode;
import org.opentravel.schemas.node.Node;

/**
 * 4/26/2012 - dmh - this is no longer used (I think) - see LibraryTreeLabelProvider
 * 
 * @author Dave Hollander
 * 
 *         to do - use styled provider as explained in
 *         http://www.vogella.de/articles/EclipseJFaceTree/article.html#example
 * 
 */
public class TypeTreeLabelProvider extends LabelProvider {

    @Override
    public Image getImage(final Object element) {
        return ((Node) element).getImage();
    }

    @Override
    public String getText(final Object element) {
        return ((INode) element).getName();
    }

}
