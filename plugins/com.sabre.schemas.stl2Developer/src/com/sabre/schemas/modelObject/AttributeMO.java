/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.modelObject;

import com.sabre.schemacompiler.model.AbstractLibrary;
import com.sabre.schemacompiler.model.NamedEntity;
import com.sabre.schemacompiler.model.TLAttribute;
import com.sabre.schemacompiler.model.TLAttributeOwner;
import com.sabre.schemacompiler.model.TLAttributeType;
import com.sabre.schemas.node.Node;
import com.sabre.schemas.node.NodeNameUtils;

public class AttributeMO extends ModelObject<TLAttribute> {

    public AttributeMO(final TLAttribute obj) {
        super(obj);
        if (obj.getType() != null) {
            setTLType(obj.getType());
        }
    }

    @Override
    public void delete() {
        removeFromTLParent();
    }

    @Override
    public void addToTLParent(final ModelObject<?> parentMO, int index) {
        if (parentMO.getTLModelObj() instanceof TLAttributeOwner) {
            ((TLAttributeOwner) parentMO.getTLModelObj()).addAttribute(index, getTLModelObj());
        }
    }

    @Override
    public void addToTLParent(final ModelObject<?> parentMO) {
        if (parentMO.getTLModelObj() instanceof TLAttributeOwner) {
            ((TLAttributeOwner) parentMO.getTLModelObj()).addAttribute(getTLModelObj());
        }
    }

    @Override
    public void removeFromTLParent() {
        final TLAttributeOwner attributeOwner = getTLModelObj().getAttributeOwner();
        if (attributeOwner != null) {
            attributeOwner.removeAttribute(getTLModelObj());
        }
    }

    @Override
    public String getName() {
        return getTLModelObj().getName() == null || getTLModelObj().getName().isEmpty() ? ""
                : getTLModelObj().getName();
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

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.modelObject.ModelObject#getTLType()
     */
    @Override
    public NamedEntity getTLType() {
        return srcObj.getType();
    }

    @Override
    public String getComponentType() {
        return "Attribute";
    }

    @Override
    protected AbstractLibrary getLibrary(final TLAttribute obj) {
        return null;
    }

    @Override
    protected int indexOf() {
        final TLAttribute thisProp = getTLModelObj();
        return thisProp.getAttributeOwner().getAttributes().indexOf(thisProp);
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
        if (indexOf() > 0) {
            getTLModelObj().moveUp();
            return true;
        }
        return false;
    }

    @Override
    public boolean moveDown() {
        // only count attributes, not elements or indicators
        if (indexOf() + 1 < getTLModelObj().getAttributeOwner().getAttributes().size()) {
            getTLModelObj().moveDown();
            return true;
        }
        return false;
    }

    @Override
    public boolean setMandatory(final boolean selection) {
        getTLModelObj().setMandatory(selection);
        return true;
    }

    @Override
    public boolean setName(final String name) {
        getTLModelObj().setName(name);
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.modelObject.ModelObject#clearTLType()
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
        if (tlObj instanceof TLAttributeType) {
            final TLAttributeType attributeType = (TLAttributeType) tlObj;
            getTLModelObj().setType(attributeType);
            // super.setTLType(attributeType);

            // Assure attribute names conform to the rules
            if (!((Node) getNode()).isXsdType())
                setName(NodeNameUtils.fixAttributeName(getName()));
        }
    }

    @Override
    public void setTLType(final NamedEntity tlObj) {
        getTLModelObj().setType((TLAttributeType) tlObj);
    }

}
