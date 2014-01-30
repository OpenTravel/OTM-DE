/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemas.node.controllers;

import java.util.LinkedList;
import java.util.List;

import org.opentravel.schemacompiler.model.TLAdditionalDocumentationItem;
import org.opentravel.schemacompiler.model.TLContext;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLDocumentationItem;

/**
 * 
 * @author Agnieszka Janowska
 * 
 */
class OtherDocItemNodeModelController implements DocItemNodeModelController {

    private TLDocumentation parentDoc;
    private TLContext context;

    public OtherDocItemNodeModelController(TLDocumentation parentDoc, TLContext context) {
        this.parentDoc = parentDoc;
        this.context = context;
    }

    @Override
    public TLDocumentationItem createChild() {
        if (parentDoc.getOtherDoc(context.getContextId()) == null) {
            TLAdditionalDocumentationItem item = new TLAdditionalDocumentationItem();
            item.setContext(context.getContextId());
            parentDoc.addOtherDoc(item);
            return item;
        }
        throw new IllegalStateException("Cannot add more than 1 item to the context");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.node.NodeModelController#removeChild()
     */
    @Override
    public void removeChild(TLDocumentationItem child) {
        parentDoc.removeOtherDoc(wrap(child));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.node.NodeModelController#getChildren()
     */
    @Override
    public List<TLDocumentationItem> getChildren() {
        LinkedList<TLDocumentationItem> list = new LinkedList<TLDocumentationItem>();
        list.add(parentDoc.getOtherDoc(context.getContextId()));
        return list;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.node.NodeModelController#getChild(int)
     */
    @Override
    public TLDocumentationItem getChild(int index) {
        return getChildren().get(index);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.node.NodeModelController#moveChildUp(java.lang.Object)
     */
    @Override
    public void moveChildUp(TLDocumentationItem child) {
        parentDoc.moveOtherDocUp(wrap(child));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.node.NodeModelController#moveChildDown(java.lang.Object)
     */
    @Override
    public void moveChildDown(TLDocumentationItem child) {
        parentDoc.moveOtherDocDown(wrap(child));
    }

    /**
     * @param child
     * @return
     */
    private TLAdditionalDocumentationItem wrap(TLDocumentationItem child) {
        if (child instanceof TLAdditionalDocumentationItem) {
            return (TLAdditionalDocumentationItem) child;
        }
        return null;
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
