/*
 * Copyright (c) 2013, Sabre Inc.
 */
package org.opentravel.schemas.utils;

import org.opentravel.schemas.node.BusinessObjectNode;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.CoreObjectNode;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.NodeFactory;
import org.opentravel.schemas.node.PropertyNodeType;
import org.opentravel.schemas.node.VWA_Node;
import org.opentravel.schemas.node.properties.PropertyNode;

import com.sabre.schemacompiler.ic.TypeNameIntegrityChecker;
import com.sabre.schemacompiler.model.TLAttribute;
import com.sabre.schemacompiler.model.TLDocumentation;
import com.sabre.schemacompiler.model.TLProperty;

/**
 * @author Pawel Jedruch
 * 
 */
public class PropertyNodeBuilder {

    private final PropertyNode propertyNode;

    public PropertyNodeBuilder(PropertyNode propertyNode) {
        this.propertyNode = propertyNode;
    }

    public static PropertyNodeBuilder create(PropertyNodeType elementType) {
        Object tlObject = creatTLObject(elementType);
        ComponentNode newComponentMember = NodeFactory.newComponentMember(null, tlObject);
        if (newComponentMember instanceof PropertyNode) {
            return new PropertyNodeBuilder((PropertyNode) newComponentMember);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @SuppressWarnings("incomplete-switch")
    private static Object creatTLObject(PropertyNodeType type) {
        TLProperty tl = null;
        switch (type) {
            case ELEMENT:
                tl = new TLProperty();
                tl.setReference(false);
                return tl;
            case ID_REFERENCE:
                tl = new TLProperty();
                tl.setReference(true);
                return tl;
            case ATTRIBUTE:
                return new TLAttribute();
        }
        return tl;
    }

    public PropertyNode build() {
        return propertyNode;
    }

    public PropertyNodeBuilder makeSimpleList(String name) {
        CoreObjectNode coreObject = ComponentNodeBuilder.createCoreObject(name).get();
        this.propertyNode.setAssignedType(coreObject.getSimpleListFacet());
        return this;
    }

    public PropertyNodeBuilder makeDetailList(String name) {
        CoreObjectNode coreObject = ComponentNodeBuilder.createCoreObject(name).get();
        this.propertyNode.setAssignedType(coreObject.getDetailListFacet());
        return this;
    }

    public PropertyNodeBuilder setName(String typeName) {
        propertyNode.setName(typeName);
        return this;
    }

    public PropertyNodeBuilder assignCoreObject(String name) {
        CoreObjectNode coreObject = ComponentNodeBuilder.createCoreObject(name).get();
        this.propertyNode.setAssignedType(coreObject);
        return this;
    }

    public PropertyNodeBuilder assignBuisnessObject(String name) {
        BusinessObjectNode business = ComponentNodeBuilder.createBusinessObject(name).get();
        this.propertyNode.setAssignedType(business);
        return this;
    }

    /**
     * Make sure that before calling this the {@link ModelNode} is created with valid TLModel
     */
    public PropertyNodeBuilder assignVWA(String name) {
        VWA_Node coreObject = ComponentNodeBuilder.createVWA(name).get();
        this.propertyNode.setAssignedType(coreObject);
        return this;
    }

    /**
     * for proper assigned propagation, make sure before calling this method the TLModel can return
     * getOwningModel(). If it will return null then the {@link TypeNameIntegrityChecker} can not be
     * called. One one to make sure that TLModel can find owning model is to before call assign
     * execute {@link PropertyNodeBuilder#addToComponent(ComponentNode)}.
     * 
     * @param type
     * @return
     */
    public PropertyNodeBuilder assign(ComponentNode type) {
        propertyNode.getTypeClass().setAssignedType(type, false);
        return this;
    }

    public PropertyNodeBuilder setDescription(String string) {
        propertyNode.setDescription(string);
        return this;
    }

    public PropertyNodeBuilder setDocumentation(TLDocumentation documentation) {
        propertyNode.setDocumentation(documentation);
        return this;
    }

    public PropertyNodeBuilder addToComponent(ComponentNode node) {
        node.addProperty(propertyNode);
        return this;
    }

}
