/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemas.modelObject;

import java.util.List;

import com.sabre.schemacompiler.model.AbstractLibrary;
import com.sabre.schemacompiler.model.TLAlias;
import com.sabre.schemacompiler.model.TLAliasOwner;
import com.sabre.schemacompiler.model.TLFacet;
import com.sabre.schemacompiler.model.TLLibrary;
import com.sabre.schemacompiler.model.TLListFacet;

/**
 * @author Dave Hollander
 * 
 */
public class AliasMO extends ModelObject<TLAlias> {

    public AliasMO(final TLAlias obj) {
        super(obj);
    }

    @Override
    public void delete() {
        final TLAliasOwner owningEntity = getTLModelObj().getOwningEntity();
        if (owningEntity != null && !(owningEntity instanceof TLFacet)
                && !(owningEntity instanceof TLListFacet)) {
            owningEntity.removeAlias(getTLModelObj());
        }
        srcObj = null;
    }

    @Override
    public List<?> getChildren() {
        return null;
    }

    @Override
    public String getComponentType() {
        return "Alias: " + getName();
    }

    @Override
    protected AbstractLibrary getLibrary(final TLAlias obj) {
        return obj.getOwningLibrary();
    }

    @Override
    public String getName() {
        final TLAlias tlModelObj = getTLModelObj();
        if (tlModelObj != null) {
            return tlModelObj.getName();
        }
        return null;
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

    // @Override
    // public boolean isAlias() {
    // return true;
    // }

    @Override
    public boolean isComplexAssignable() {
        return true;
    }

    // @Override
    // public boolean isFacet() {
    // return true;
    // }

    @Override
    public boolean setName(final String name) {
        getTLModelObj().setName(name);
        return true;
    }

}
