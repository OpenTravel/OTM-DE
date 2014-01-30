/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemas.node.controllers;

import java.util.List;

import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLDocumentationItem;

/**
 * 
 * @author Agnieszka Janowska
 * 
 */
class ReferenceDocItemNodeModelController implements DocItemNodeModelController {

    private TLDocumentation parentDoc;

    public ReferenceDocItemNodeModelController(TLDocumentation parentDoc) {
        this.parentDoc = parentDoc;
    }

    @Override
    public TLDocumentationItem createChild() {
        if (getChildren().size() < MAX_ITEMS) {
            TLDocumentationItem item = new TLDocumentationItem();
            parentDoc.addReference(item);
            return item;
        }
        throw new IllegalStateException("Cannot add more than " + MAX_ITEMS + " items to the list");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.node.NodeModelController#removeChild()
     */
    @Override
    public void removeChild(TLDocumentationItem child) {
        parentDoc.removeReference(child);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.node.NodeModelController#getChildren()
     */
    @Override
    public List<TLDocumentationItem> getChildren() {
        return parentDoc.getReferences();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.node.NodeModelController#getChild(int)
     */
    @Override
    public TLDocumentationItem getChild(int index) {
        return parentDoc.getReferences().get(index);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.node.NodeModelController#moveChildUp(java.lang.Object)
     */
    @Override
    public void moveChildUp(TLDocumentationItem child) {
        parentDoc.moveReferenceUp(child);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.node.NodeModelController#moveChildDown(java.lang.Object)
     */
    @Override
    public void moveChildDown(TLDocumentationItem child) {
        parentDoc.moveReferenceDown(child);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.node.controllers.NodeModelController#getChild(java.lang.Object)
     */
    @Override
    public TLDocumentationItem getChild(Object key) {
        throw new UnsupportedOperationException("Cannot retrieve documentation item by key");
    }

}
