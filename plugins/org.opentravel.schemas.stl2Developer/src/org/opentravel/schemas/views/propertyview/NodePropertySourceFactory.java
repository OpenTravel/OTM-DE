
package org.opentravel.schemas.views.propertyview;

import org.eclipse.ui.views.properties.IPropertySource;
import org.opentravel.schemas.node.LibraryChainNode;
import org.opentravel.schemas.node.LibraryNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.trees.repository.RepositoryNode;

/**
 * @author Pawel Jedruch
 * 
 */
public class NodePropertySourceFactory {

    public IPropertySource createPropertySource(Node node) {
        if (node instanceof RepositoryNode) {
            return new RepositoryPropertySource((RepositoryNode) node);
        } else if (node instanceof LibraryChainNode) {
            return new LibraryPropertySource(((LibraryChainNode) node).getHead());
        } else if (node instanceof LibraryNode) {
            return new LibraryPropertySource((LibraryNode) node);
        } else if (node instanceof ProjectNode) {
            return new ProjectPropertySource((ProjectNode) node);
        }
        return null;
    }

}
