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
import com.sabre.schemacompiler.model.TLAlias;
import com.sabre.schemacompiler.model.TLCoreObject;
import com.sabre.schemacompiler.model.TLExtension;
import com.sabre.schemacompiler.model.TLLibrary;

public class CoreObjectMO extends ModelObject<TLCoreObject> {
    @SuppressWarnings("unused")
    private final static Logger LOGGER = LoggerFactory.getLogger(CoreObjectMO.class);

    public CoreObjectMO(final TLCoreObject obj) {
        super(obj);
        // obj.getSimpleFacet().setSimpleTypeName(obj.getName());
    }

    @Override
    public void delete() {
        final AbstractLibrary owningLibrary = getTLModelObj().getOwningLibrary();
        if (owningLibrary != null) {
            owningLibrary.removeNamedMember(getTLModelObj());
        }
    }

    @Override
    public List<Object> getChildren() {
        final List<Object> kids = new ArrayList<Object>();
        kids.add(getTLModelObj().getSimpleFacet());
        kids.add(getTLModelObj().getSummaryFacet());
        kids.add(getTLModelObj().getDetailFacet());
        kids.add(getTLModelObj().getDetailListFacet());
        kids.add(getTLModelObj().getSimpleListFacet());
        kids.addAll(getTLModelObj().getAliases());
        kids.add(getTLModelObj().getRoleEnumeration());
        return kids;
    }

    @Override
    public String getComponentType() {
        return "Core Object";
    }

    @Override
    protected AbstractLibrary getLibrary(final TLCoreObject obj) {
        return obj.getOwningLibrary();
    }

    @Override
    public String getName() {
        return getTLModelObj().getName();
    }

    @Override
    public String getNamePrefix() {
        final TLLibrary lib = (TLLibrary) getLibrary(getTLModelObj());
        return lib == null ? "" : lib.getPrefix();
    }

    @Override
    public String getNamespace() {
        return getTLModelObj().getNamespace();
    }

    public NamedEntity getSimpleValueType() {
        return srcObj.getSimpleFacet().getSimpleType();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.modelObject.ModelObject#getTLBase()
     */
    @Override
    public NamedEntity getTLBase() {
        return srcObj.getExtension() != null ? srcObj.getExtension().getExtendsEntity() : null;
    }

    @Override
    public boolean isComplexAssignable() {
        return true;
    }

    /**
     * Model will force a core used as simple to use the simple facet.
     */
    @Override
    public boolean isSimpleAssignable() {
        return true;
    }

    // @Override
    // public boolean isCoreObject() {
    // return true;
    // }

    // @Override
    // public boolean isExtendable() {
    // return !getTLModelObj().isNotExtendable();
    // }
    //
    // @Override
    // public void setExtension(final boolean state) {
    // getTLModelObj().setNotExtendable(!state);
    // }

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
        return tlExtension == null || tlExtension.getExtendsEntity() == null ? "" : tlExtension
                .getExtendsEntity().getNamespace();
    }

    @Override
    public boolean isExtendedBy(NamedEntity extension) {
        // LOGGER.debug("is Extension?");
        if (this.getTLModelObj() == null)
            return false;
        if (this.getTLModelObj().getExtension() == null)
            return false;
        if (this.getTLModelObj().getExtension().getValidationIdentity() == null)
            return false;
        if (extension == null)
            return false;
        if (extension.getValidationIdentity() == null)
            return false;

        // LOGGER.debug("  is "+this.getTLModelObj().getExtension().getValidationIdentity()+" Extended by? "+extension.getValidationIdentity());
        if (this.getTLModelObj().getExtension().getExtendsEntity() == extension)
            return true;
        return false;
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

    @Override
    public boolean setName(final String name) {
        getTLModelObj().setName(name);
        return true;
    }

    @Override
    public void addAlias(final TLAlias tla) {
        srcObj.addAlias(tla);
    }

}
