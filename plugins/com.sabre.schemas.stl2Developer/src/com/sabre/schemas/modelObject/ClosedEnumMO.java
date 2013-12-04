/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.modelObject;

import java.util.List;

import com.sabre.schemacompiler.model.AbstractLibrary;
import com.sabre.schemacompiler.model.TLClosedEnumeration;
import com.sabre.schemacompiler.model.TLEnumValue;
import com.sabre.schemacompiler.model.TLLibrary;
import com.sabre.schemacompiler.model.TLModelElement;
import com.sabre.schemas.utils.StringComparator;

public class ClosedEnumMO extends ModelObject<TLClosedEnumeration> {

    public ClosedEnumMO(final TLClosedEnumeration obj) {
        super(obj);
    }

    @Override
    public boolean addChild(TLModelElement value) {
        if (value instanceof TLEnumValue)
            addLiteral((TLEnumValue) value);
        else
            return false;
        return true;
    }

    public void addLiteral(final TLEnumValue value) {
        getTLModelObj().addValue(value);
    }

    public void addLiteral(final TLEnumValue value, int index) {
        getTLModelObj().addValue(index, value);
    }

    @Override
    public void delete() {
        if (srcObj.getOwningLibrary() != null)
            srcObj.getOwningLibrary().removeNamedMember(srcObj);
    }

    @Override
    protected AbstractLibrary getLibrary(final TLClosedEnumeration obj) {
        return obj.getOwningLibrary();
    }

    @Override
    public List<TLEnumValue> getChildren() {
        return getTLModelObj().getValues();
    }

    @Override
    public String getComponentType() {
        return "Closed Enumeration";
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
    public boolean isSimpleAssignable() {
        return true;
    }

    @Override
    public boolean setName(final String name) {
        getTLModelObj().setName(name);
        return true;
    }

    @Override
    public void sort() {
        TLClosedEnumeration eClosed = getTLModelObj();
        eClosed.sortValues(new StringComparator<TLEnumValue>() {

            @Override
            protected String getString(TLEnumValue object) {
                return object.getLiteral();
            }
        });
    }

}
