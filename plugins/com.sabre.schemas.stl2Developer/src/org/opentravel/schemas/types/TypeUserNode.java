/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemas.types;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.properties.Images;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A leaf on a typeTree that represents a user of the parent type.
 *
 * NOTE: there is no attempt to maintain this tree. It is created dynamically
 * when the tree is attached to a view as done in the LibraryTreeContentProvider.
 * 
 * @author Dave Hollander
 * 
 */
/**
 * TODO - Stop using the node tree! Use the "mistletoe" pattern making WhereUsed a separate tree
 * parasitic on the main tree. In libraryTreeContentProvider, inspect "typeProviders" to get the
 * Users data from this structure. This should Type Class--Type class can hold the TypeNode and its
 * children.
 * 
 * Inline labelProvider in this class and use it in the libraryTreeLabelProvider. replace label
 * provider to children so that the type and property are seen. Then don't use getOwningComponent.
 * create a where used child node such that it can be assigned behavior and stay out of children
 * trees easier! expose the implied types as children of Model
 * 
 */
public class TypeUserNode extends ComponentNode {
    @SuppressWarnings("unused")
    private static final Logger LOGGER = LoggerFactory.getLogger(TypeUserNode.class);

    /**
     * Create a new Where Used complete with new TL model and link to component
     */
    public TypeUserNode(final Node parent) {
        super();
        this.setParent(parent);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.node.Node#delete()
     */
    @Override
    public void delete() {
        super.delete();
    }

    @Override
    public String getPropertyRole() {
        return "Alias";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.node.Node#getName()
     */
    @Override
    public String getName() {
        return "Where Used";
        // return "Where "+getParent().getName()+" is used";
    }

    @Override
    public Image getImage() {
        return Images.getImageRegistry().get(Images.WhereUsed);
    }

    @Override
    public boolean isNavigation() {
        return true;
    }

    @Override
    public String getComponentType() {
        return "WhereUsed: " + getName();
    }

    /**
     * Get all of the components that use any aspect of the owning component. DO NOT make this a
     * getChildren or the tree will become invalid with nodes having multiple parents which will
     * break lots of getChildren() users.
     * 
     * This is used directly by the Library tree content provider.
     * 
     * @return
     */
    public List<Node> getUsers() {
        List<Node> users = new ArrayList<Node>();
        // HashSet<Node> users = new HashSet<Node>();
        for (Node e : getParent().getTypeClass().getComponentUsers())
            users.add(e.getOwningComponent());
        // for (Node n : users) uniqueUsers.add(n);
        return users;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.node.ComponentNode#hasNavChildrenWithProperties()
     */
    @Override
    public boolean hasNavChildrenWithProperties() {
        return hasChildren();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.node.Node#hasChildren()
     */
    @Override
    public boolean hasChildren() {
        return true;
    }

    @Override
    public List<Node> getNavChildren() {
        return null;
    }

    @Override
    public boolean hasNavChildren() {
        return false;
    }

    @Override
    public boolean isDeleted() {
        if (super.isDeleted()) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isDeleteable() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.node.Node#setName(java.lang.String)
     */
    @Override
    public void setName(String n) {
        return;
    }

}
