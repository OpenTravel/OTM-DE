/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.node.controllers;

import java.util.List;

import com.sabre.schemacompiler.model.TLDocumentation;
import com.sabre.schemacompiler.model.TLDocumentationItem;

/**
 * 
 * @author Agnieszka Janowska
 * 
 */
class MoreInfoDocItemNodeModelController implements DocItemNodeModelController {

    private TLDocumentation parentDoc;

    public MoreInfoDocItemNodeModelController(TLDocumentation parentDoc) {
        this.parentDoc = parentDoc;
    }

    @Override
    public TLDocumentationItem createChild() {
        if (getChildren().size() < MAX_ITEMS) {
            TLDocumentationItem item = new TLDocumentationItem();
            parentDoc.addMoreInfo(item);
            return item;
        }
        throw new IllegalStateException("Cannot add more than " + MAX_ITEMS + " items to the list");
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.node.NodeModelController#removeChild()
     */
    @Override
    public void removeChild(TLDocumentationItem child) {
        parentDoc.removeMoreInfo(child);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.node.NodeModelController#getChildren()
     */
    @Override
    public List<TLDocumentationItem> getChildren() {
        return parentDoc.getMoreInfos();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.node.NodeModelController#getChild(int)
     */
    @Override
    public TLDocumentationItem getChild(int index) {
        return parentDoc.getMoreInfos().get(index);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.node.NodeModelController#moveChildUp(java.lang.Object)
     */
    @Override
    public void moveChildUp(TLDocumentationItem child) {
        parentDoc.moveMoreInfoUp(child);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.node.NodeModelController#moveChildDown(java.lang.Object)
     */
    @Override
    public void moveChildDown(TLDocumentationItem child) {
        parentDoc.moveMoreInfoDown(child);
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
