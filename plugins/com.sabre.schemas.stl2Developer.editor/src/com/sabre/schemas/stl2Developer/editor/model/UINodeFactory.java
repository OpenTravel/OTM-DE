/*
 * Copyright (c) 2013, Sabre Inc.
 */
package com.sabre.schemas.stl2Developer.editor.model;

import org.eclipse.gef.requests.CreationFactory;

import com.sabre.schemas.node.Node;

public class UINodeFactory implements CreationFactory {

    private Node node;
    private Diagram diagram;

    public UINodeFactory(Diagram diagram) {
        this.diagram = diagram;
    }

    @Override
    public Class<? extends Node> getObjectType() {
        return node.getClass();
    }

    @Override
    public Node getNewObject() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

}