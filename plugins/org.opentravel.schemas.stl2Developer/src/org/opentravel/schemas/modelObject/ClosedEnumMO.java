
package org.opentravel.schemas.modelObject;

import java.util.List;

import org.opentravel.schemas.utils.StringComparator;

import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLEnumValue;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModelElement;

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
