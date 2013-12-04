/*
 * Copyright (c) 2013, Sabre Inc.
 */
package com.sabre.schemas.views.propertyview;

import org.eclipse.ui.views.properties.IPropertySource;

import com.sabre.schemas.node.LibraryChainNode;
import com.sabre.schemas.node.LibraryNode;
import com.sabre.schemas.node.Node;
import com.sabre.schemas.node.ProjectNode;
import com.sabre.schemas.trees.repository.RepositoryNode;

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
