/**
 * Copyright (C) 2014 OpenTravel Alliance (info@opentravel.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.opentravel.schemas.modelObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLIndicatorOwner;

public class IndicatorMO extends ModelObject<TLIndicator> {
    static final Logger LOGGER = LoggerFactory.getLogger(IndicatorMO.class);

    public IndicatorMO(final TLIndicator obj) {
        super(obj);
    }

    @Override
    public void delete() {
        if (getTLModelObj().getOwner() != null) {
            getTLModelObj().getOwner().removeIndicator(getTLModelObj());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.modelObject.ModelObject#clearTLType()
     */
    @Override
    public void clearTLType() {
        // Nothing to do - tl model has no type for indicators.
    }

    @Override
    public void addToTLParent(ModelObject<?> parentMO, int index) {
        if (parentMO.getTLModelObj() instanceof TLIndicatorOwner) {
            ((TLIndicatorOwner) parentMO.getTLModelObj()).addIndicator(index, getTLModelObj());
        }
    }

    @Override
    public void addToTLParent(final ModelObject<?> parentMO) {
        if (parentMO.getTLModelObj() instanceof TLIndicatorOwner) {
            ((TLIndicatorOwner) parentMO.getTLModelObj()).addIndicator(getTLModelObj());
        }
    }

    @Override
    public void removeFromTLParent() {
        if (getTLModelObj().getOwner() != null) {
            getTLModelObj().getOwner().removeIndicator(getTLModelObj());
        }
    }

    @Override
    public String getName() {
        String name = getTLModelObj().getName();
        return name == null || name.isEmpty() ? "" : name;
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
        return "Indicator";
    }

    @Override
    protected AbstractLibrary getLibrary(final TLIndicator obj) {
        return null;
    }

    /**
     * NOTE: as of 5/9/2012 this count includes inherited properties!
     */
    @Override
    protected int indexOf() {
        final TLIndicator thisProp = getTLModelObj();
        return thisProp.getOwner().getIndicators().indexOf(thisProp);
    }

    @Override
    public boolean isIndicatorProperty() {
        return !srcObj.isPublishAsElement();
    }

    @Override
    public boolean isIndicatorElement() {
        return srcObj.isPublishAsElement();
    }

    @Override
    public boolean isMandatory() {
        return false; // indicators are always assumed false unless present.
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
        if (indexOf() + 1 < getTLModelObj().getOwner().getIndicators().size()) {
            getTLModelObj().moveDown();
            return true;
        }
        return false;
    }

    @Override
    public boolean setName(final String name) {
        getTLModelObj().setName(name);
        return true;
    }

    public void setToElement(boolean state) {
        srcObj.setPublishAsElement(state);
    }

}
