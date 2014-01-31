/**
 * Copyright (C) 2014 OpenTravel Alliance (info@opentravel.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.opentravel.schemas.types;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.controllers.NodeImageProvider;
import org.opentravel.schemas.node.controllers.NodeLabelProvider;
import org.opentravel.schemas.properties.Images;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates a tree anchored to the type object on a ComponentNode to represent
 * users of a NamedType.
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
 * Inline labelProvider in this class and use it in the libraryTreeLabelProvider. replace lable
 * provider to children so that the type and property are seen. Then don't use getOwningComponent.
 * create a where used child node such that it can be assigned behavior and stay out of children
 * trees easier! expose the implied types as children of Model figure how how to respond to
 * inheritedChildren in libraryContentProvider
 * 
 */
public class TypeNode extends Node {
    private static final Logger LOGGER = LoggerFactory.getLogger(TypeNode.class);

    public enum TypeNodeType {
        OWNER, USER
    }

    private NodeImageProvider imageProvider = simpleImageProvider("WhereUsed");
    private NodeLabelProvider labelProvider = simpleLabelProvider("Where Used");
    private Node owner = null;
    private TypeNodeType nodeType = TypeNodeType.OWNER;

    /**
     * Create a new Where Used complete with new TL model and link to component
     */
    public TypeNode(final Node parent) {
        this.owner = parent;
    }

    public TypeNode(Node typeNode, TypeNodeType nodeType) {
        this.owner = typeNode; // The user of this type
        this.nodeType = nodeType;
        String label = typeNode.getOwningComponent().getName();
        if (typeNode.isNamedType())
            label = typeNode.getComponentType();
        labelProvider = simpleLabelProvider(label + " : " + typeNode.getName());
        if (typeNode.isProperty())
            imageProvider = nodeImageProvider(typeNode.getOwningComponent());
        else
            imageProvider = nodeImageProvider(typeNode);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.node.Node#isEditable()
     */
    @Override
    public boolean isEditable() {
        return owner.isEditable();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.node.Node#getParent()
     */
    @Override
    public Node getParent() {
        return owner;
    }

    @Override
    public String getLabel() {
        return labelProvider.getLabel() + " (" + getParent().getComponentUsersCount() + ")";
        // return labelProvider.getLabel();
    }

    @Override
    public Image getImage() {
        return imageProvider.getImage();
    }

    @Override
    public void delete() {
    }

    @Override
    public String getComponentType() {
        return "WhereUsed:" + owner.getName();
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
    @Override
    public List<Node> getChildren() {
        List<Node> users = new ArrayList<Node>();
        // HashSet<Node> users = new HashSet<Node>();
        // for (Node e : owner.getTypeClass().getComponentUsers())
        // users.add(e.getOwningComponent());
        if (owner == null) {
            return Collections.emptyList();
        }
        for (Node e : owner.getTypeClass().getComponentUsers())
            users.add(new TypeNode(e.getTypeClass().getTypeOwner(), TypeNodeType.USER));
        // for (Node n : users) uniqueUsers.add(n);
        return users;
    }

    @Override
    public boolean hasChildren() {
        return true;
    }

    private NodeImageProvider simpleImageProvider(final String imageName) {
        return new NodeImageProvider() {

            @Override
            public Image getImage() {
                final ImageRegistry imageRegistry = Images.getImageRegistry();
                return imageRegistry.get(imageName);
            }
        };
    }

    private NodeImageProvider nodeImageProvider(final Node node) {
        final Node imageNode = node;
        return new NodeImageProvider() {

            @Override
            public Image getImage() {
                return imageNode.getImage();
            }
        };
    }

    private NodeLabelProvider simpleLabelProvider(final String txt) {
        final String label = txt;
        return new NodeLabelProvider() {

            @Override
            public String getLabel() {
                return label;
            }
        };
    }

    @Override
    public List<Node> getNavChildren() {
        return getChildren();
    }

    @Override
    public boolean hasNavChildren() {
        return true;
    }

    public boolean isUser() {
        return nodeType.equals(TypeNodeType.USER) ? true : false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.node.Node#sort()
     */
    @Override
    public void sort() {
        getParent().sort();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((owner == null) ? 0 : owner.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TypeNode other = (TypeNode) obj;
        if (owner == null) {
            if (other.owner != null)
                return false;
        } else if (!owner.equals(other.owner))
            return false;
        return true;
    }

}
