/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.modelObject;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sabre.schemacompiler.model.AbstractLibrary;
import com.sabre.schemacompiler.model.TLFacet;
import com.sabre.schemacompiler.model.TLListFacet;
import com.sabre.schemacompiler.model.TLSimpleFacet;

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
