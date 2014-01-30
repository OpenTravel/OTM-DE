/*
 * Copyright (c) 2013, Sabre Inc.
 */
package org.opentravel.schemas.modelObject;

import org.junit.Test;

import org.opentravel.schemacompiler.codegen.util.PropertyCodegenUtils;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;

/**
 * @author Pawel Jedruch
 * 
 */
public class ValueWithAttributesAttributeFacetMOTest {

    @Test
    public void infinityLoopOnVWA() {
        TLValueWithAttributes vwaFirst = new TLValueWithAttributes();
        TLValueWithAttributes vwaSecond = new TLValueWithAttributes();
        TLAttribute attrOfSecondType = new TLAttribute();
        attrOfSecondType.setType(vwaSecond);
        vwaFirst.addAttribute(attrOfSecondType);
        // make loop
        TLAttribute attrOfFirstType = new TLAttribute();
        attrOfFirstType.setType(vwaFirst);
        vwaSecond.addAttribute(attrOfFirstType);

        // stack over flow here. Infinity loop
        PropertyCodegenUtils.getInheritedIndicators(vwaFirst);
        PropertyCodegenUtils.getInheritedAttributes(vwaFirst);
    }

}
