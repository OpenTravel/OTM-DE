/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemas.modelObject;

import java.util.List;

import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.TLModel;

public class TLModelMO extends ModelObject<TLModel> {

    public TLModelMO(final TLModel obj) {
        super(obj);
    }

    @Override
    public void delete() {
        System.out.println("ModelObject - delete - TODO");
    }

    @Override
    public List<?> getChildren() {
        return null;
    }

    @Override
    public String getComponentType() {
        return null;
    }

    @Override
    protected AbstractLibrary getLibrary(final TLModel obj) {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getNamePrefix() {
        return null;
    }

    @Override
    public String getNamespace() {
        return null;
    }

    @Override
    public boolean setName(final String name) {
        return false;
    }

    @Override
    public void setDeprecatedDoc(final String string, final int i) {
    }

    @Override
    public void setDeveloperDoc(final String string, final int index) {
    }

    @Override
    public void setReferenceDoc(final String string, final int index) {
    }

    @Override
    public void setMoreInfo(final String string, final int index) {
    }

    @Override
    public void setOtherDoc(final String string, final String context) {
    }

}
