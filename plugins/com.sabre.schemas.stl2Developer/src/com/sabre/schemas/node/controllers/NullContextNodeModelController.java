/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.node.controllers;

import java.util.List;

import com.sabre.schemacompiler.model.TLContext;

/**
 * @author Agnieszka Janowska
 * 
 */
public class NullContextNodeModelController implements NodeModelController<TLContext> {

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.node.controllers.NodeModelController#createChild()
     */
    @Override
    public TLContext createChild() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.node.controllers.NodeModelController#removeChild(java.lang.Object)
     */
    @Override
    public void removeChild(TLContext child) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.node.controllers.NodeModelController#getChildren()
     */
    @Override
    public List<TLContext> getChildren() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.node.controllers.NodeModelController#getChild(int)
     */
    @Override
    public TLContext getChild(int index) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.node.controllers.NodeModelController#moveChildUp(java.lang.Object)
     */
    @Override
    public void moveChildUp(TLContext child) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.node.controllers.NodeModelController#moveChildDown(java.lang.Object)
     */
    @Override
    public void moveChildDown(TLContext child) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.node.controllers.NodeModelController#getChild(java.lang.Object)
     */
    @Override
    public TLContext getChild(Object key) {
        // TODO Auto-generated method stub
        return null;
    }

}
