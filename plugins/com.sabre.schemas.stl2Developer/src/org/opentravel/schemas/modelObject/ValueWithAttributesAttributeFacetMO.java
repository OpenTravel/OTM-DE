/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemas.modelObject;

import java.util.ArrayList;
import java.util.List;

import org.opentravel.schemacompiler.codegen.util.PropertyCodegenUtils;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;

public class ValueWithAttributesAttributeFacetMO extends ModelObject<TLValueWithAttributesFacet> {

    public static final String DISPLAY_NAME = "Attributes";

    public ValueWithAttributesAttributeFacetMO(final TLValueWithAttributesFacet obj) {
        super(obj);
    }

    public void addAttribute(final TLAttribute attribute, int index) {
        getTLModelObj().addAttribute(index, attribute);
    }

    @Override
    public List<?> getChildren() {
        final List<TLModelElement> kids = new ArrayList<TLModelElement>();
        kids.addAll(getTLModelObj().getAttributes());
        kids.addAll(getTLModelObj().getIndicators());
        return kids;
    }

    /**
     * @see org.opentravel.schemas.modelObject.ModelObject#getInheritedChildren()
     */
    @Override
    public List<?> getInheritedChildren() {
        final TLValueWithAttributes vwa = getTLModelObj().getValueWithAttributes();
        final List<TLModelElement> inheritedKids = new ArrayList<TLModelElement>();
        final List<?> declaredKids = getChildren();

        for (TLAttribute attribute : PropertyCodegenUtils.getInheritedAttributes(vwa)) {
            if (!declaredKids.contains(attribute)) {
                inheritedKids.add(attribute);
            }
        }
        for (TLIndicator indicator : PropertyCodegenUtils.getInheritedIndicators(vwa)) {
            if (!declaredKids.contains(indicator)) {
                inheritedKids.add(indicator);
            }
        }

        return inheritedKids;
    }

    @Override
    public String getComponentType() {
        return "Attributes Facet";
    }

    @Override
    public String getName() {
        return DISPLAY_NAME;
    }

    @Override
    protected AbstractLibrary getLibrary(final TLValueWithAttributesFacet obj) {
        return null;
    }

    @Override
    public String getNamePrefix() {
        return null;
    }

    @Override
    public String getNamespace() {
        return null;
    }

    @Override
    public boolean setName(final String name) {
        return false;
    }

    @Override
    public void delete() {
    }

    @Override
    public boolean addChild(final TLModelElement child) {
        if (child instanceof TLAttribute) {
            getTLModelObj().addAttribute((TLAttribute) child);
        } else if (child instanceof TLIndicator) {
            getTLModelObj().addIndicator((TLIndicator) child);
        } else {
            return false;
            // throw new IllegalArgumentException("Property of type "
            // + child.getClass().getSimpleName() + " not supported for VWA Facet");
        }
        return true;
    }

}
