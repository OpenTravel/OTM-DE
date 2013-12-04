/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.node.controllers;

import java.util.Collections;
import java.util.List;

import com.sabre.schemacompiler.model.TLDocumentationItem;

/**
 * @author Agnieszka Janowska
 * 
 */
public class NullDocItemNodeModelController implements NodeModelController<TLDocumentationItem> {

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.node.NodeModelController#addChild()
     */
    @Override
    public TLDocumentationItem createChild() {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.node.NodeModelController#removeChild(java.lang.Object)
     */
    @Override
    public void removeChild(TLDocumentationItem child) {
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
        // TODO Auto-generated method stub
        return null;
    }

}
