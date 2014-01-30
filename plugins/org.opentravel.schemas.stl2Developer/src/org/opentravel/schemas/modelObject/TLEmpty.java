/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemas.modelObject;

import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.TLProperty;

/**
 * An empty TLModelElement to allow constructors to work on entities that do not exist in the TL
 * Model
 */
public class TLEmpty extends TLProperty {

    @Override
    public String getValidationIdentity() {
        return "";
    }

    @Override
    public TLModel getOwningModel() {
        return null;
    }

    public void delete() {
    }

}
