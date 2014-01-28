/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemas.modelObject;

import java.util.List;

import com.sabre.schemacompiler.model.AbstractLibrary;
import com.sabre.schemacompiler.model.TLEnumValue;

public class EnumLiteralMO extends ModelObject<TLEnumValue> {

    public EnumLiteralMO(final TLEnumValue obj) {
        super(obj);
    }

    @Override
    public void delete() {
        getTLModelObj().getOwningEnum().removeValue(this.getTLModelObj());
    }

    @Override
    protected AbstractLibrary getLibrary(final TLEnumValue obj) {
        return obj != null && obj.getOwningEnum() != null ? obj.getOwningEnum().getOwningLibrary()
                : null;
    }

    @Override
    public List<?> getChildren() {
        return null;
    }

    @Override
    public String getComponentType() {
        return "Enumeration Literal Value";
    }

    @Override
    public String getName() {
        return srcObj != null ? getTLModelObj().getLiteral() : "UndefindedLiteral";
    }

    @Override
    public String getNamePrefix() {
        return "";
    }

    @Override
    public String getNamespace() {
        return "";
    }

    @Override
    public boolean isComplexAssignable() {
        return false;
    }

    @Override
    public boolean setName(final String name) {
        getTLModelObj().setLiteral(name);
        return true;
    }

    @Override
    public void addToTLParent(final ModelObject<?> parentMO, int index) {
        if (parentMO instanceof OpenEnumMO) {
            ((OpenEnumMO) parentMO).addLiteral(getTLModelObj(), index);
        }
        if (parentMO instanceof ClosedEnumMO) {
            ((ClosedEnumMO) parentMO).addLiteral(getTLModelObj(), index);
        }
    }

    @Override
    public void addToTLParent(final ModelObject<?> parentMO) {
        if (parentMO instanceof OpenEnumMO) {
            ((OpenEnumMO) parentMO).addLiteral(getTLModelObj());
        }
        if (parentMO instanceof ClosedEnumMO) {
            ((ClosedEnumMO) parentMO).addLiteral(getTLModelObj());
        }
    }

    @Override
    public boolean moveUp() {
        if (indexOf() > 0) {
            getTLModelObj().moveUp();
            return true;
        }
        return false;
    }

    @Override
    public boolean moveDown() {
        if (indexOf() + 1 < getTLModelObj().getOwningEnum().getValues().size()) {
            getTLModelObj().moveDown();
            return true;
        }
        return false;
    }

    @Override
    protected int indexOf() {
        return getTLModelObj().getOwningEnum().getValues().indexOf(getTLModelObj());
    }

}
