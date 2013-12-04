/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.modelObject;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sabre.schemacompiler.model.AbstractLibrary;
import com.sabre.schemacompiler.model.NamedEntity;
import com.sabre.schemacompiler.model.TLExtension;
import com.sabre.schemacompiler.model.TLModelElement;
import com.sabre.schemacompiler.model.TLOperation;

/**
 * @author Dave Hollander
 * 
 */
public class OperationMO extends ModelObject<TLOperation> {

    private static final Logger LOGGER = LoggerFactory.getLogger(OperationMO.class);

    public OperationMO(final TLOperation obj) {
        super(obj);
    }

    @Override
    public void delete() {
        if (getTLModelObj().getOwningService() == null)
            LOGGER.warn("Operation " + getName() + " has no owning service.");
        else
            getTLModelObj().getOwningService().removeOperation(this.getTLModelObj());
    }

    @Override
    public List<?> getChildren() {
        final List<TLModelElement> kids = new ArrayList<TLModelElement>();
        kids.add(getTLModelObj().getRequest());
        kids.add(getTLModelObj().getResponse());
        kids.add(getTLModelObj().getNotification());
        return kids;
    }

    @Override
    public String getComponentType() {
        return "Operation";
    }

    @Override
    protected AbstractLibrary getLibrary(final TLOperation obj) {
        return null;
    }

    @Override
    public String getName() {
        // LOGGER.debug("OperationName from getTLModelObj().getLocalName():" +
        // getTLModelObj().getLocalName());
        return getTLModelObj().getName();

    }

    @Override
    public String getNamePrefix() {
        return "";
    }

    @Override
    public String getNamespace() {
        return getTLModelObj().getNamespace();
    }

    // @Override
    // public boolean isOperation() {
    // return true;
    // }

    @Override
    public boolean setName(final String name) {
        getTLModelObj().setName(name);
        return true;
    }

    /**
     * @see com.sabre.schemas.modelObject.ModelObject#getExtendsType()
     */
    @Override
    public String getExtendsType() {
        TLExtension tlExtension = getTLModelObj().getExtension();
        String extendsTypeName = "";

        if (tlExtension != null) {
            if (tlExtension.getExtendsEntity() != null)
                extendsTypeName = tlExtension.getExtendsEntity().getLocalName();
            else
                extendsTypeName = "--base type can not be found--";
        }
        return extendsTypeName;
    }

    @Override
    public String getExtendsTypeNS() {
        TLExtension tlExtension = getTLModelObj().getExtension();
        return tlExtension != null ? tlExtension.getExtendsEntity().getNamespace() : "";
    }

    /**
     * @see com.sabre.schemas.modelObject.ModelObject#setExtendsType(com.sabre.schemas.modelObject.ModelObject)
     */
    @Override
    public void setExtendsType(ModelObject<?> mo) {
        if (mo == null) {
            getTLModelObj().setExtension(null);

        } else {
            TLExtension tlExtension = getTLModelObj().getExtension();

            if (tlExtension == null) {
                tlExtension = new TLExtension();
                getTLModelObj().setExtension(tlExtension);
            }
            tlExtension.setExtendsEntity((NamedEntity) mo.getTLModelObj());
        }
    }

}
