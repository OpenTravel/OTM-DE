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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLListFacet;
import org.opentravel.schemacompiler.model.TLSimpleFacet;

public class ListFacetMO extends ModelObject<TLListFacet> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ListFacetMO.class);

    public ListFacetMO(final TLListFacet obj) {
        super(obj);
        // editable = true; // can edit facets
    }

    @Override
    public List<?> getChildren() {
        return getTLModelObj().getAliases();
    }

    @Override
    public String getComponentType() {
        return getTLModelObj().getLocalName();
    }

    @Override
    public String getLabel() {
        // Simple and Detail lists
        String label = srcObj.getFacetType() == null ? "" : FacetMO.getDisplayName(srcObj
                .getFacetType());
        label = "List_" + label;
        return label;
    }

    @Override
    protected AbstractLibrary getLibrary(final TLListFacet obj) {
        return null;
    }

    @Override
    public String getName() {
        return getTLModelObj().getLocalName() == null ? "" : getTLModelObj().getLocalName();
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
    public boolean setName(final String name) {
        return false;
    }

    @Override
    public AbstractLibrary getOwningLibrary() {
        return srcObj != null ? srcObj.getItemFacet().getOwningEntity().getOwningLibrary() : null;
    }

    // @Override
    // public boolean isFacet() {
    // return true;
    // }

    @Override
    public void delete() {
    }

    @Override
    public boolean isSimpleList() {
        return (getTLModelObj().getItemFacet() instanceof TLSimpleFacet);
    }

    public boolean isDetailList() {
        return (getTLModelObj().getItemFacet() instanceof TLFacet);
    }
}
