/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.node.controllers;

import java.util.Collections;
import java.util.List;

import com.sabre.schemacompiler.model.TLDocumentation;
import com.sabre.schemacompiler.model.TLDocumentationItem;

/**
 * 
 * @author Agnieszka Janowska
 * 
 */
public class DescriptionDocItemNodeModelController implements DocItemNodeModelController {

    private TLDocumentation parentDoc;

    public DescriptionDocItemNodeModelController(TLDocumentation parentDoc) {
        this.parentDoc = parentDoc;
    }

    @Override
    public TLDocumentationItem createChild() {
        if (parentDoc.getDescription() == null) {
            parentDoc.setDescription("");
        }
        throw new IllegalStateException("Cannot add more descriptions");
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.node.NodeModelController#removeChild()
     */
    @Override
    public void removeChild(TLDocumentationItem child) {
        throw new UnsupportedOperationException(
                "Cannot delete description, clear the description field instead");
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.node.NodeModelController#getChildren()
     */
    @Override
    public List<TLDocumentationItem> getChildren() {
        return Collections.emptyList();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.node.NodeModelController#getChild(int)
     */
    @Override
    public TLDocumentationItem getChild(int index) {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.node.NodeModelController#moveChildUp(java.lang.Object)
     */
    @Override
    public void moveChildUp(TLDocumentationItem child) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.node.NodeModelController#moveChildDown(java.lang.Object)
     */
    @Override
    public void moveChildDown(TLDocumentationItem child) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.node.controllers.NodeModelController#getChild(java.lang.Object)
     */
    @Override
    public TLDocumentationItem getChild(Object key) {
        throw new UnsupportedOperationException("Cannot retrieve documentation item by key");
    }

}
