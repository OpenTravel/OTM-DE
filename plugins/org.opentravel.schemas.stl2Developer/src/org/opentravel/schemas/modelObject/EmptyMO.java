
package org.opentravel.schemas.modelObject;

import org.opentravel.schemacompiler.model.AbstractLibrary;

/**
 * Class to use when there is no TL model object.
 * 
 * @author Dave Hollander
 * 
 */
public class EmptyMO extends ModelObject<TLEmpty> {
    // Object tlEmpty = null;

    public EmptyMO(final TLEmpty obj) {
        super(obj);
    }

    // @Override
    // public boolean isEmpty() {
    // return true;
    // }

    @Override
    public String getComponentType() {
        return "Empty Model";
    }

    @Override
    public String getName() {
        return "empty";
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
    public boolean setName(final String name) {
        return false;
    }

    @Override
    public void delete() {
        if (srcObj == null)
            return;
        srcObj.delete();
        srcObj = null;
    }

    @Override
    protected AbstractLibrary getLibrary(final TLEmpty obj) {
        return null;
    }

}
