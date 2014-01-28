/*
 * Copyright (c) 2013, Sabre Inc.
 */
package org.opentravel.schemas.node;

import com.sabre.schemacompiler.model.TLFacet;

/**
 * @author Pawel Jedruch
 * 
 */
public class RenamableFacet extends FacetNode {

    public RenamableFacet(TLFacet tlObj) {
        super(tlObj);
    }

    @Override
    public void setName(String n) {
        String name = n;
        // Strip the object name and "query" string if present.
        name = NodeNameUtils.stripFacetPrefix(this, name);
        if (getModelObject() != null) {
            // compiler doesn't allow empty context -
            // ((TLFacet) getTLModelObject()).setContext("");
            ((TLFacet) getTLModelObject()).setLabel(name);
            // rename their type users as well.
            for (Node user : getTypeUsers()) {
                user.setName(getName());
            }
        }
    }

}
