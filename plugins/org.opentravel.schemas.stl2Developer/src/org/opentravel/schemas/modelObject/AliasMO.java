
package org.opentravel.schemas.modelObject;

import java.util.List;

import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLAliasOwner;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLListFacet;

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
