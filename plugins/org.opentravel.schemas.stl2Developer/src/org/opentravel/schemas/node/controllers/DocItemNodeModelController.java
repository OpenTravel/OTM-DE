
package org.opentravel.schemas.node.controllers;

import org.opentravel.schemacompiler.model.TLDocumentationItem;

/**
 * Marker interface for all the documentation items model controllers
 * 
 * @author Agnieszka Janowska
 * 
 */
public interface DocItemNodeModelController extends NodeModelController<TLDocumentationItem> {

    int MAX_ITEMS = 10;

}
