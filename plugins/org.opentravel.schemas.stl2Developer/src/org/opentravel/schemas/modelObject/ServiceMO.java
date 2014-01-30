
package org.opentravel.schemas.modelObject;

import java.util.List;

import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.model.TLService;

public class ServiceMO extends ModelObject<TLService> {

    public ServiceMO(final TLService obj) {
        super(obj);
    }

    @Override
    public void delete() {
        if (srcObj.getOwningLibrary() != null)
            srcObj.getOwningLibrary().removeNamedMember(srcObj);
    }

    @Override
    public List<TLOperation> getChildren() {
        return getTLModelObj().getOperations();
    }

    @Override
    public String getComponentType() {
        return "Service";
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
    public AbstractLibrary getLibrary(final TLService obj) {
        return obj.getOwningLibrary();
    }

    @Override
    public boolean setName(final String name) {
        getTLModelObj().setName(name);
        return true;
    }

}
