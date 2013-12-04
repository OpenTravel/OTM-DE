/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.node;

import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sabre.schemas.properties.Images;

/**
 * Navigation Nodes describe GUI model objects that are not part of the TL Model. They ease
 * navigating the GUI and <b>not</b> representing the OTM model.
 * 
 * @author Dave Hollander
 * 
 */
public class NavNode extends Node {
    @SuppressWarnings("unused")
    private static final Logger LOGGER = LoggerFactory.getLogger(NavNode.class);

    private String name = "";

    /**
     * Create a navigation node, get ns and prefix from parentNode node. link to parentNode node.
     * 
     * @param name
     * @param parent
     */
    public NavNode(final String name, final Node parent) {
        super();
        setName(name);
        setLibrary(parent.getLibrary());
        parent.linkChild(this, false); // link without doing family tests.
    }

    public boolean isComplexRoot() {
        return this == getLibrary().getComplexRoot() ? true : false;
    }

    @Override
    public void linkLibrary(LibraryNode lib) {
        if (lib != null && !getChildren().contains(lib))
            getChildren().add(lib);
    }

    @Override
    public Image getImage() {
        return Images.getImageRegistry().get("folder");
    }

    @Override
    public boolean isLibraryContainer() {
        return false;
    }

    public boolean isSimpleRoot() {
        return this == getLibrary().getSimpleRoot() ? true : false;
    }

    public boolean isServiceRoot() {
        return this == getLibrary().getServiceRoot() ? true : false;
    }

    public boolean isElementRoot() {
        return this == getLibrary().getElementRoot() ? true : false;
    }

    @Override
    public boolean isNavigation() {
        return true;
    }

    @Override
    public String getComponentType() {
        return "Navigation Node";
    }

    @Override
    public List<Node> getNavChildren() {
        return getChildren();
    }

    @Override
    public boolean hasNavChildren() {
        return !getChildren().isEmpty();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.node.Node#hasChildren_TypeProviders()
     */
    @Override
    public boolean hasChildren_TypeProviders() {
        return getChildren().size() > 0;
    }

    @Override
    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public String getLabel() {
        return name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isDeleteable() {
        return false;
    }

    @Override
    public boolean isDeleted() {
        return deleted;
    }

}
