/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemas.node.controllers;

import java.util.List;

import com.sabre.schemacompiler.model.TLContext;
import com.sabre.schemacompiler.model.TLLibrary;

/**
 * @author Agnieszka Janowska
 * 
 */
public class ContextNodeModelController implements NodeModelController<TLContext> {
    /**
     * 
     */
    private final TLLibrary library;

    /**
     * @param library
     */
    public ContextNodeModelController(TLLibrary library) {
        this.library = library;
    }

    @Override
    public TLContext createChild() {
        TLContext context = new TLContext();
        library.addContext(context);
        return context;
    }

    @Override
    public void removeChild(TLContext child) {
        library.removeContext(child);
    }

    @Override
    public List<TLContext> getChildren() {
        return library.getContexts();
    }

    @Override
    public TLContext getChild(int index) {
        throw new UnsupportedOperationException("Cannot retrieve context by index");
    }

    @Override
    public void moveChildUp(TLContext child) {
    }

    @Override
    public void moveChildDown(TLContext child) {
    }

    @Override
    public TLContext getChild(Object key) {
        return library.getContext(key.toString());
    }
}
