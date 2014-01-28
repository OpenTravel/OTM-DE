/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemas.modelObject;

import com.sabre.schemacompiler.model.AbstractLibrary;
import com.sabre.schemacompiler.model.NamedEntity;
import com.sabre.schemacompiler.model.TLExtensionPointFacet;
import com.sabre.schemacompiler.model.TLFacet;
import com.sabre.schemacompiler.model.TLProperty;
import com.sabre.schemacompiler.model.TLPropertyType;

public class ElementPropertyMO extends ModelObject<TLProperty> {

    public ElementPropertyMO(final TLProperty obj) {
        super(obj);
        if (obj.getType() != null) {
            super.setTLType(obj.getType());
        }
    }

    @Override
    public void delete() {
        if (getTLModelObj().getPropertyOwner() != null) {
            (getTLModelObj().getPropertyOwner()).removeProperty(getTLModelObj());
        }
    }

    @Override
    public void addToTLParent(final ModelObject<?> parentMO, int index) {
        if (parentMO.getTLModelObj() instanceof TLFacet) {
            ((TLFacet) parentMO.getTLModelObj()).addElement(index, getTLModelObj());
        } else if (parentMO.getTLModelObj() instanceof TLExtensionPointFacet) {
            ((TLExtensionPointFacet) parentMO.getTLModelObj()).addElement(index, getTLModelObj());
        }
    }

    @Override
    public void addToTLParent(final ModelObject<?> parentMO) {
        if (parentMO.getTLModelObj() instanceof TLFacet) {
            ((TLFacet) parentMO.getTLModelObj()).addElement(getTLModelObj());
        } else if (parentMO.getTLModelObj() instanceof TLExtensionPointFacet) {
            ((TLExtensionPointFacet) parentMO.getTLModelObj()).addElement(getTLModelObj());
        }
    }

    @Override
    public void removeFromTLParent() {
        if (getTLModelObj().getPropertyOwner() != null) {
            getTLModelObj().getPropertyOwner().removeProperty(getTLModelObj());
        }
    }

    @Override
    public String getName() {
        return getTLModelObj() == null || getTLModelObj().getName() == null
                || getTLModelObj().getName().isEmpty() ? "" : getTLModelObj().getName();
    }

    /**
     * Returns the repeat count - returns a 1 if tl object is 0 because that is the xsd default the
     * user expects to see.
     */
    @Override
    public int getRepeat() {
        return getTLModelObj().getRepeat() == 0 ? 1 : getTLModelObj().getRepeat();
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
        return "Element";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.modelObject.ModelObject#getTLType()
     */
    @Override
    public NamedEntity getTLType() {
        return srcObj.getType();
    }

    /**
     * Get the index (0..sizeof()) of this property in the facet list. Use Node.indexTLProperty().
     */
    @Override
    protected int indexOf() {
        final TLProperty thisProp = getTLModelObj();
        return thisProp.getPropertyOwner().getElements().indexOf(thisProp);
    }

    @Override
    public boolean isMandatory() {
        return getTLModelObj().isMandatory();
    }

    @Override
    public boolean setMandatory(final boolean selection) {
        getTLModelObj().setMandatory(selection);
        return true;
    }

    /**
     * Business logic about correct name done at node level.
     */
    @Override
    public boolean setName(final String name) {
        getTLModelObj().setName(name);
        return true;
    }

    /**
     * Move if you can, return false if you can not.
     * 
     * @return
     */
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
        if (indexOf() + 1 < getTLModelObj().getPropertyOwner().getElements().size()) {
            getTLModelObj().moveDown();
            return true;
        }
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
        this.srcObj.setType(null);
    }

    @Override
    public void setTLType(final ModelObject<?> mo) {
        Object tlObj = null;
        if (mo != null)
            tlObj = mo.getTLModelObj();
        if (tlObj instanceof TLPropertyType) {
            final TLPropertyType propertyType = (TLPropertyType) tlObj;
            getTLModelObj().setType(propertyType);
        }
    }

    @Override
    public void setTLType(final NamedEntity tlObj) {
        getTLModelObj().setType((TLPropertyType) tlObj);
    }

    @Override
    public boolean setRepeat(final int cnt) {
        getTLModelObj().setRepeat(cnt);
        return true;
    }

    @Override
    protected AbstractLibrary getLibrary(final TLProperty obj) {
        return null;
    }

    public boolean isID_Reference() {
        return srcObj.isReference();
    }

    public void setToReference(boolean state) {
        srcObj.setReference(state);
    }

}
