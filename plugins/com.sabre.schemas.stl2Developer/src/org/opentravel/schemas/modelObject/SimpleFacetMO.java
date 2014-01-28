/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemas.modelObject;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sabre.schemacompiler.model.AbstractLibrary;
import com.sabre.schemacompiler.model.TLModelElement;
import com.sabre.schemacompiler.model.TLSimpleFacet;

/**
 * Simple facets are facets that have one implied child.
 * 
 * A Simple Facet has one property node child whose assigned type must be a simple type.
 * 
 * @author Dave Hollander
 * 
 */
public class SimpleFacetMO extends ModelObject<TLSimpleFacet> {

    private final static Logger LOGGER = LoggerFactory.getLogger(SimpleFacetMO.class);

    private TLnSimpleAttribute simpleProperty; // the associated property

    public SimpleFacetMO(TLSimpleFacet obj) {
        super(obj);
        simpleProperty = new TLnSimpleAttribute(obj);
    }

    @Override
    public void delete() {
    }

    public TLModelElement getSimpleAttribute() {
        return simpleProperty;
    }

    @Override
    public List<?> getChildren() {
        final List<Object> kids = new ArrayList<Object>();
        kids.add(simpleProperty);
        return kids;

    }

    @Override
    public String getName() {
        // LOGGER.debug(getTLModelObj().getSimpleTypeName()+"|"+ getTLModelObj().getLocalName());
        return getTLModelObj().getLocalName() == null ? "" : getTLModelObj().getLocalName();
    }

    @Override
    public String getNamePrefix() {
        return "";
    }

    @Override
    public String getNamespace() {
        return getTLModelObj().getNamespace();
    }

    @Override
    public String getComponentType() {
        return FacetMO.getDisplayName(getTLModelObj().getFacetType());
    }

    @Override
    protected AbstractLibrary getLibrary(final TLSimpleFacet obj) {
        return null;
    }

    @Override
    public boolean isComplexAssignable() {
        return true;
    }

    @Override
    public boolean isSimpleAssignable() {
        return true;
    }

    // @Override
    // public boolean isFacet() {
    // return true;
    // }

    // @Override
    // public boolean isSimpleFacet() {
    // return true;
    // }

    @Override
    public boolean setName(final String name) {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.modelObject.ModelObject#clearTLType()
     */
    @Override
    public void clearTLType() {
        // this.type = null;
        this.srcObj.setSimpleType(null);
    }

}
