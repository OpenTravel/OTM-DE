/*
 * Copyright (c) 2013, Sabre Inc.
 */
package com.sabre.schemas.node;

import org.junit.Assert;
import org.junit.Test;

import com.sabre.schemacompiler.model.TLProperty;
import com.sabre.schemacompiler.model.TLSimple;
import com.sabre.schemacompiler.model.XSDSimpleType;
import com.sabre.schemas.node.properties.ElementNode;

/**
 * @author Pawel Jedruch
 * 
 */
public class NodeFactoryTest {

    @Test
    public void createElementNodeShouldPersistType() {
        TLProperty tl = new TLProperty();
        XSDSimpleType simpleType = new XSDSimpleType("string", null);
        tl.setType(simpleType);
        ElementNode element = (ElementNode) NodeFactory.newComponentMember(null, tl);
        TLProperty afterCreate = (TLProperty) element.getModelObject().getTLModelObj();
        Assert.assertSame(simpleType, afterCreate.getType());
    }

    @Test
    public void guiTypeAccess() {
        TLProperty tl = new TLProperty();
        TLSimple tlSimple = new TLSimple();
        XSDSimpleType xsdSimple = new XSDSimpleType("string", null);
        tlSimple.setParentType(xsdSimple);
        tl.setType(tlSimple);
        Assert.assertSame(tlSimple, tl.getType());

        ElementNode element = (ElementNode) NodeFactory.newComponentMember(null, tl);
        TLProperty afterCreate = (TLProperty) element.getModelObject().getTLModelObj();
        Assert.assertSame(tlSimple, afterCreate.getType());

        // These will all return UNASSIGNED since resolver has not run.
        // getTypeName tries to fix the Type node resulting in a unassigned node.
        Assert.assertFalse(element.getTypeName().isEmpty()); // properties view
        Assert.assertFalse(element.getTypeNameWithPrefix().isEmpty()); // facet view
        Assert.assertNotNull(element.getAssignedType());

    }
}
