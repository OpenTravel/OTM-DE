/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemas.node.controllers;

import com.sabre.schemacompiler.model.TLDocumentationItem;

/**
 * Marker interface for all the documentation items model controllers
 * 
 * @author Agnieszka Janowska
 * 
 */
public interface DocItemNodeModelController extends NodeModelController<TLDocumentationItem> {

    int MAX_ITEMS = 10;

}
