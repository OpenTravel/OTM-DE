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
import com.sabre.schemacompiler.model.TLAttribute;
import com.sabre.schemacompiler.model.TLExtension;
import com.sabre.schemacompiler.model.TLExtensionPointFacet;
import com.sabre.schemacompiler.model.TLIndicator;
import com.sabre.schemacompiler.model.TLLibrary;
import com.sabre.schemacompiler.model.TLModelElement;
import com.sabre.schemacompiler.model.TLProperty;

public class ExtensionPointFacetMO extends ModelObject<TLExtensionPointFacet> {
    final static Logger LOGGER = LoggerFactory.getLogger(ExtensionPointFacetMO.class);

    public ExtensionPointFacetMO(final TLExtensionPointFacet obj) {
        super(obj);
    }

    @Override
    public void delete() {
        final AbstractLibrary owningLibrary = getTLModelObj().getOwningLibrary();
        if (owningLibrary != null) {
            owningLibrary.removeNamedMember(getTLModelObj());
        }
    }

    @Override
    public List<?> getChildren() {

        final List<TLModelElement> kids = new ArrayList<TLModelElement>();
        kids.addAll(getTLModelObj().getAttributes());
        kids.addAll(getTLModelObj().getIndicators());
        kids.addAll(getTLModelObj().getElements());

        return kids;

    }

    @Override
    public String getComponentType() {
        return "Extension Point Facet";
    }

    @Override
    public String getName() {
        return getTLModelObj().getLocalName() == null ? "-not assigned-" : getTLModelObj()
                .getLocalName();
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

    @Override
    public boolean isComplexAssignable() {
        return false;
    }

    /**
     * Model will force a core used as page1 to use the page1 facet.
     */
    // FIXME - prevents inclusion in tree view but need to understand why first.
    @Override
    public boolean isSimpleAssignable() {
        return false;
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
        // for EPF not extending anything tlExtension.getExtendsEntity() is null.
        return tlExtension != null && tlExtension.getExtendsEntity() != null ? tlExtension
                .getExtendsEntity().getNamespace() : "";
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
    protected AbstractLibrary getLibrary(TLExtensionPointFacet obj) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean setName(String name) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean addChild(TLModelElement child) {
        if (child instanceof TLProperty) {
            getTLModelObj().addElement((TLProperty) child);
        } else if (child instanceof TLAttribute) {
            getTLModelObj().addAttribute((TLAttribute) child);
        } else if (child instanceof TLIndicator) {
            getTLModelObj().addIndicator((TLIndicator) child);
        } else
            return false;
        return true;
    }

}
