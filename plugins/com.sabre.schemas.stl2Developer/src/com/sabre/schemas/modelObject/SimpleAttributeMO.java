/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.modelObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sabre.schemacompiler.model.AbstractLibrary;
import com.sabre.schemacompiler.model.NamedEntity;

public class SimpleAttributeMO extends ModelObject<TLnSimpleAttribute> {
    @SuppressWarnings("unused")
    private final static Logger LOGGER = LoggerFactory.getLogger(SimpleAttributeMO.class);

    public SimpleAttributeMO(final TLnSimpleAttribute obj) {
        super(obj);
    }

    @Override
    public void delete() {
    }

    public void refreshAssignedType() {
        if (getTLModelObj().getType() != null) {
            setTLType(getTLModelObj().getType());
        }
    }

    @Override
    public String getName() {
        return getTLModelObj().getName();
    }

    // Model does not know what namespace the attribute or its owning component
    // is in.
    @Override
    public String getNamePrefix() {
        return "";
    }

    @Override
    public String getNamespace() {
        return "";
    }

    @Override
    public String getComponentType() {
        return "Simple Attribute";
    }

    @Override
    protected AbstractLibrary getLibrary(final TLnSimpleAttribute obj) {
        return null;
    }

    @Override
    public NamedEntity getTLType() {
        return srcObj.getType();
    }

    @Override
    public boolean isMandatory() {
        return getTLModelObj().isMandatory();
    }

    /**
     * Move if you can, return false if you can not.
     * 
     * @return
     */
    @Override
    public boolean moveUp() {
        return false;
    }

    @Override
    public boolean moveDown() {
        return false;
    }

    @Override
    public boolean setMandatory(final boolean selection) {
        return false;
    }

    @Override
    public boolean setName(final String name) {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.modelObject.ModelObject#clearTLType()
     */
    @Override
    public void clearTLType() {
    }

    @Override
    public void setTLType(final ModelObject<?> mo) {
        Object tlObj = null;
        if (mo != null)
            tlObj = mo.getTLModelObj();
        if (tlObj instanceof NamedEntity) {
            getTLModelObj().setType((NamedEntity) tlObj);
        }
    }

    @Override
    public void setTLType(final NamedEntity tlObj) {
        getTLModelObj().setType(tlObj);
    }

}
