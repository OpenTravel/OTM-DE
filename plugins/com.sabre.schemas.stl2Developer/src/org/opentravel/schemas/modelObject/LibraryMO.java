/**
 * 
 */
package org.opentravel.schemas.modelObject;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.opentravel.schemacompiler.model.AbstractLibrary;

/**
 * @author Dave Hollander
 * 
 */
public class LibraryMO extends ModelObject<AbstractLibrary> {
    private static final Logger LOGGER = LoggerFactory.getLogger(LibraryMO.class);

    public LibraryMO(AbstractLibrary obj) {
        super(obj);
    }

    @Override
    public List<?> getChildren() {
        // TODO Auto-generated method stub
        return getTLModelObj().getNamedMembers();
    }

    @Override
    public String getComponentType() {
        // TODO Auto-generated method stub
        return "Library: " + getName();
    }

    @Override
    protected AbstractLibrary getLibrary(AbstractLibrary obj) {
        return obj;
    }

    @Override
    public String getName() {
        return getTLModelObj().getName();
    }

    @Override
    public String getNamePrefix() {
        return "";
    }

    @Override
    public String getNamespace() {
        return getTLModelObj().getNamespace();
    }

    @Override
    public boolean setName(String name) {
        getTLModelObj().setName(name);
        return true;
    }

    @Override
    public void delete() {
        // LOGGER.debug("Not Implemented - library delete");
    }

}
