
package org.opentravel.schemas.modelObject;

import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.TLRole;

public class RolePropertyMO extends ModelObject<TLRole> {

    public RolePropertyMO(final TLRole obj) {
        super(obj);
    }

    public RolePropertyMO() {
    }

    // @Override
    // public boolean isRole() {
    // return true;
    // }

    // @Override
    // public boolean isRoleProperty() {
    // return true;
    // }

    @Override
    public String getComponentType() {
        return "Role";
    }

    @Override
    public boolean setName(final String name) {
        if (getTLModelObj() == null) {
            return false;
        }
        getTLModelObj().setName(name);
        return true;
    }

    @Override
    protected AbstractLibrary getLibrary(final TLRole obj) {
        return null;
    }

    @Override
    public String getName() {
        return getTLModelObj() != null ? getTLModelObj().getName() : "";
    }

    @Override
    public String getNamePrefix() {
        return null;
    }

    @Override
    public String getNamespace() {
        return "";
    }

    @Override
    public void delete() {
        final TLRole tlModel = getTLModelObj();
        if (tlModel != null && tlModel.getRoleEnumeration() != null) {
            tlModel.getRoleEnumeration().removeRole(tlModel);
        }
    }

    @Override
    public void addToTLParent(final ModelObject<?> parentMO, int index) {
        if (parentMO instanceof RoleEnumerationMO) {
            ((RoleEnumerationMO) parentMO).addRole(index, getTLModelObj());
        }
    }

    @Override
    public void addToTLParent(final ModelObject<?> parentMO) {
        if (parentMO instanceof RoleEnumerationMO) {
            ((RoleEnumerationMO) parentMO).addRole(getTLModelObj());
        }
    }

    @Override
    public int indexOf() {
        final TLRole thisProp = getTLModelObj();
        return getTLModelObj().getRoleEnumeration().getRoles().indexOf(thisProp);
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
        // only count attributes, not elements or indicators
        if (indexOf() + 1 < getTLModelObj().getRoleEnumeration().getRoles().size()) {
            getTLModelObj().moveDown();
            return true;
        }
        return false;
    }

}
