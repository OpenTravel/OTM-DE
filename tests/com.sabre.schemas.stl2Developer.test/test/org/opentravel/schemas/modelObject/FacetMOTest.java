/*
 * Copyright (c) 2012, Sabre Inc.
 */
package org.opentravel.schemas.modelObject;

import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemas.modelObject.FacetMO;

import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLProperty;

public class FacetMOTest {

    private TLFacet facet;
    private TLProperty e3;
    private TLProperty e2;
    private TLProperty e1;
    private TLAttribute a3;
    private TLAttribute a2;
    private TLAttribute a1;
    private TLIndicator i3;
    private TLIndicator i2;
    private TLIndicator i1;
    private FacetMO facetMO;

    @Before
    public void beforeEachTest() {
        facet = new TLFacet();
        e3 = createElement("e3");
        e2 = createElement("e2");
        e1 = createElement("e1");
        facet.addElement(e2);
        facet.addElement(e3);
        facet.addElement(e1);

        a3 = createAttribute("a3");
        a2 = createAttribute("a2");
        a1 = createAttribute("a1");
        facet.addAttribute(a2);
        facet.addAttribute(a3);
        facet.addAttribute(a1);

        i3 = createIndicator("i3");
        i2 = createIndicator("i2");
        i1 = createIndicator("i1");
        facet.addIndicator(i2);
        facet.addIndicator(i3);
        facet.addIndicator(i1);
        facetMO = new FacetMO(facet);
    }

    @Test
    public void shouldSortElements() {
        facetMO.sort();

        List<TLProperty> expectedOrder = Arrays.asList(e1, e2, e3);
        Assert.assertEquals(expectedOrder, facet.getElements());
    }

    @Test
    public void shouldSortAttributes() {
        facetMO.sort();

        List<TLAttribute> expectedOrder = Arrays.asList(a1, a2, a3);
        Assert.assertEquals(expectedOrder, facet.getAttributes());

    }

    @Test
    public void shouldSortIndicators() {
        facetMO.sort();

        List<TLIndicator> expectedOrder = Arrays.asList(i1, i2, i3);
        Assert.assertEquals(expectedOrder, facet.getIndicators());

    }

    private TLIndicator createIndicator(String name) {
        TLIndicator tlProperty = new TLIndicator();
        tlProperty.setName(name);
        return tlProperty;
    }

    private TLAttribute createAttribute(String name) {
        TLAttribute tlProperty = new TLAttribute();
        tlProperty.setName(name);
        return tlProperty;
    }

    private TLProperty createElement(String name) {
        TLProperty tlProperty = new TLProperty();
        tlProperty.setName(name);
        return tlProperty;
    }

}
