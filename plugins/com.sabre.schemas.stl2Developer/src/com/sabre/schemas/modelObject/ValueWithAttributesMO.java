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
import com.sabre.schemacompiler.model.TLFacetType;
import com.sabre.schemacompiler.model.TLLibrary;
import com.sabre.schemacompiler.model.TLSimpleFacet;
import com.sabre.schemacompiler.model.TLValueWithAttributes;

/**
 * Value With Attribute Model Object.
 * 
 * Provide an interface to the TLValueWithAttributes model object. TLValueWithAttributes does not
 * use facets to contain simple type and attributes, so this model class must adapt.
 * 
 * @author Dave Hollander
 * 
 */
public class ValueWithAttributesMO extends ModelObject<TLValueWithAttributes> {

    @SuppressWarnings("unused")
    private final static Logger LOGGER = LoggerFactory.getLogger(ValueWithAttributesMO.class);

    private final TLSimpleFacet valueFacet;
    private final TLValueWithAttributesFacet attributeFacet;

    public ValueWithAttributesMO(final TLValueWithAttributes obj) {
        super(obj);
        valueFacet = new TLSimpleFacet();
        valueFacet.setSimpleType(obj.getParentType());
        valueFacet.setFacetType(TLFacetType.SIMPLE);

        attributeFacet = new TLValueWithAttributesFacet(obj);
        if (obj.getParentType() != null) {
            setTLType(obj.getParentType());
        }
    }

    @Override
    public void delete() {
        if (getTLModelObj() == null || getTLModelObj().getOwningLibrary() == null) {
            return;
        }
        getTLModelObj().getOwningLibrary().removeNamedMember(getTLModelObj());
    }

    @Override
    public List<Object> getChildren() {
        // return the two facets: value type and attributes.
        final List<Object> kids = new ArrayList<Object>();
        kids.add(valueFacet);
        kids.add(attributeFacet);
        return kids;
    }

    @Override
    public String getComponentType() {
        return "Value With Attributes";
    }

    // @Override
    // public boolean isValueWithAttrs() {
    // return true;
    // }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.modelObject.ModelObject#clearTLType()
     */
    @Override
    public void clearTLType() {
        // this.type = null;
        this.srcObj.setParentType(null);
    }

    @Override
    public String getName() {
        return getTLModelObj().getName();
    }

    @Override
    public String getNamespace() {
        return getTLModelObj().getNamespace();
    }

    @Override
    public String getNamePrefix() {
        final TLLibrary lib = (TLLibrary) getLibrary(getTLModelObj());
        return lib == null ? "" : lib.getPrefix();
    }

    @Override
    protected AbstractLibrary getLibrary(final TLValueWithAttributes obj) {
        return obj.getOwningLibrary();
    }

    public NamedEntity getSimpleValueType() {
        return srcObj.getParentType();
    }

    @Override
    public boolean isComplexAssignable() {
        return true;
    }

    @Override
    public boolean isSimpleAssignable() {
        return true;
    }

    @Override
    public boolean setName(final String name) {
        getTLModelObj().setName(name);
        return true;
    }

}
