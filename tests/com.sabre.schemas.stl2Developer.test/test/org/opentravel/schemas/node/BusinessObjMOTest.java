/*
 * Copyright (c) 2013, Sabre Inc.
 */
package org.opentravel.schemas.node;

import org.junit.BeforeClass;
import org.opentravel.schemas.node.ModelNode;

import com.sabre.schemacompiler.model.TLModel;

;

/**
 * @author Pawel Jedruch
 * 
 */
public class BusinessObjMOTest {

    @BeforeClass
    public static void beforeTests() {
        new ModelNode(new TLModel());
    }

    // @Test
    // public void getInheritedChildren() {
    // BusinessObjectNode boBase = ComponentNodeBuilder.createBusinessObject("name")
    // .addCustomFacet("CCustom").addCustomFacet("CCustom2").addQueryFacet("name").get();
    // BusinessObjectNode boExtend = ComponentNodeBuilder.createBusinessObject("Extend")
    // .extend(boBase).get();
    // assertEquals(1, boExtend.getAllInheritedQueryFacets().size());
    // assertEquals(2, boExtend.getAllInheritedCustomFacets().size());
    // }
    //
    // @Test
    // public void boShouldInheritedOnlyNoneExisitingCustomFacet() {
    // String sameCustom = "SameCustom";
    // String extraCustom = "ExtraCustom";
    //
    // BusinessObjectNode boBase = ComponentNodeBuilder.createBusinessObject("name")
    // .addCustomFacet(sameCustom).addCustomFacet(extraCustom).get();
    // BusinessObjectNode boExtend = ComponentNodeBuilder.createBusinessObject("Extend")
    // .extend(boBase).addCustomFacet(sameCustom).get();
    // assertEquals(1, boExtend.getNotCraetedInheritedCustomFacets().size());
    //
    // assertEquals(extraCustom, ((TLFacet) boExtend.getNotCraetedInheritedCustomFacets().get(0)
    // .getTLModelObject()).getLabel());
    //
    // }
    //
    // @Test
    // public void boShouldInheritedOnlyNoneExisitingQueryFacet() {
    // String sameQuery = "SameQuery";
    // String extraQuery = "ExtraQuery";
    //
    // BusinessObjectNode boBase = ComponentNodeBuilder.createBusinessObject("name")
    // .addQueryFacet(extraQuery).addQueryFacet(sameQuery).get();
    // BusinessObjectNode boExtend = ComponentNodeBuilder.createBusinessObject("Extend")
    // .extend(boBase).addQueryFacet(sameQuery).get();
    //
    // assertEquals(1, boExtend.getNotCraetedInheritedQueryFacets().size());
    // assertEquals(extraQuery, ((TLFacet) boExtend.getNotCraetedInheritedQueryFacets().get(0)
    // .getTLModelObject()).getLabel());
    // }
    //
    // // 10 sec should be enough to make sure that it is infinity loop free
    // @Test(timeout = 10000)
    // public void boShouldPrevetCycleReference() {
    // String sameQuery = "SameQuery";
    // String extraQuery = "ExtraQuery";
    //
    // BusinessObjectNode boBase = ComponentNodeBuilder.createBusinessObject("Base")
    // .addQueryFacet(extraQuery).addQueryFacet(sameQuery).get();
    // BusinessObjectNode boExtend = ComponentNodeBuilder.createBusinessObject("Extend")
    // .extend(boBase).addQueryFacet(sameQuery).get();
    // // create cycle reference
    // boBase.setExtendsType(boExtend);
    //
    // }
}
