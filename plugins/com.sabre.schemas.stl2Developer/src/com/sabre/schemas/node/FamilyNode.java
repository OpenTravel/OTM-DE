/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.node;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sabre.schemas.properties.Images;

public class FamilyNode extends NavNode {
    private static final Logger LOGGER = LoggerFactory.getLogger(FamilyNode.class);

    /**
     * Constructor that creates a family node based on the node parameter. Move the node and its
     * peer into the family node.
     * 
     * @param n
     * @param peer
     */
    public FamilyNode(final Node n, final Node peer) {
        super(NodeNameUtils.makeFamilyName(n.getName()), n.getParent());
        setName(n.family.isEmpty() ? NodeNameUtils.makeFamilyName(n.getName()) : n.family);
        setLibrary(n.getLibrary());

        n.setParent(this);
        peer.setParent(this);

        final Node parent = getParent();

        getChildren().add(n);
        parent.removeChild(n);

        getChildren().add(peer);
        parent.removeChild(peer);
        // LOGGER.debug("Family "+getName()+" created for "+n.getName());
    }

    /**
     * Constructor for use with sub-types that do not maintain 2 way links with children.
     * 
     * @param name
     * @param parent
     */
    public FamilyNode(final String name, final Node parent) {
        super(name, parent);
    }

    @Override
    public String getComponentType() {
        return "Family";
    }

    @Override
    public List<Node> getNavChildren() {
        return getChildren();
    }

    @Override
    public List<Node> getDescendants_NamedTypes() {
        ArrayList<Node> kids = new ArrayList<Node>();
        for (Node c : getChildren())
            if (c.isTypeProvider())
                kids.add(c);
        return kids;
    }

    @Override
    public String getName() {
        return family;
    }

    @Override
    public String getLabel() {
        return family;
    }

    @Override
    public Image getImage() {
        final ImageRegistry imageRegistry = Images.getImageRegistry();
        return imageRegistry.get("family");
    }

    @Override
    public boolean hasNavChildren() {
        return !getChildren().isEmpty();
    }

    @Override
    public boolean hasChildren_TypeProviders() {
        return getChildren().size() > 0;
    }

    @Override
    public boolean isNavigation() {
        return true;
    }

    @Override
    public boolean isImportable() {
        // False if any child is not importable.
        boolean importable = true;
        for (final Node n : getChildren()) {
            if (!n.isImportable()) {
                importable = false;
                break;
            }
        }
        return importable;
    }

    @Override
    public void setName(final String name) {
        family = name;
        // Rename all the children just the first part of the name
        for (Node kid : getChildren())
            kid.setName(name + "_" + NodeNameUtils.getGivenName(kid.getName()));
    }

    @Override
    protected void updateFamily() {
        if (getParent() == null)
            return; // During construction or delete, may not have a parent, do nothing.
        if (getChildren().size() > 1)
            return; // if more than 2 siblings in the family and there is nothing to do.

        // If only one is left, move it up.
        final Node parent = getParent();
        if (getChildren().size() == 1) {
            final Node child = getChildren().get(0);
            parent.getChildren().add(child);
            child.setParent(parent);
        }
        // Delete the family node. Don't use delete() because that does
        // children.
        if (!parent.getChildren().remove(this))
            LOGGER.info("Error removing " + this.getName() + " from " + parent.getName());
        this.getChildren().clear();
        this.setParent(null);
    }

}
