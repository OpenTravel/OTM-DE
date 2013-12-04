/*
 * Copyright (c) 2013, Sabre Inc.
 */
package com.sabre.schemas.utils;

import com.sabre.schemacompiler.model.TLBusinessObject;
import com.sabre.schemacompiler.model.TLCoreObject;
import com.sabre.schemacompiler.model.TLFacet;
import com.sabre.schemacompiler.model.TLFacetType;
import com.sabre.schemacompiler.model.TLService;
import com.sabre.schemacompiler.model.TLSimple;
import com.sabre.schemacompiler.model.TLValueWithAttributes;
import com.sabre.schemas.node.AliasNode;
import com.sabre.schemas.node.BusinessObjectNode;
import com.sabre.schemas.node.ComplexComponentInterface;
import com.sabre.schemas.node.ComponentNode;
import com.sabre.schemas.node.CoreObjectNode;
import com.sabre.schemas.node.FacetNode;
import com.sabre.schemas.node.LibraryNode;
import com.sabre.schemas.node.Node;
import com.sabre.schemas.node.NodeFactory;
import com.sabre.schemas.node.NodeFinders;
import com.sabre.schemas.node.OperationNode;
import com.sabre.schemas.node.ServiceNode;
import com.sabre.schemas.node.SimpleTypeNode;
import com.sabre.schemas.node.VWA_Node;
import com.sabre.schemas.node.properties.ElementNode;
import com.sabre.schemas.node.properties.PropertyNode;

/**
 * @author Pawel Jedruch
 * 
 */
public class ComponentNodeBuilder<T extends ComponentNode> {

    public static class SimpleNodeBuilder extends ComponentNodeBuilder<SimpleTypeNode> {

        public SimpleNodeBuilder(SimpleTypeNode coreObject) {
            super(coreObject);
        }

        public SimpleNodeBuilder assignType(Node property) {
            componentObject.setAssignedType(property);
            return this;
        }
    }

    public static class ServiceNodeBuilder extends ComponentNodeBuilder<ServiceNode> {

        public ServiceNodeBuilder(ServiceNode coreObject) {
            super(coreObject);
        }

        public ServiceNodeBuilder createCRUDQOperations(BusinessObjectNode buisinesObject) {
            componentObject.addCRUDQ_Operations(buisinesObject);
            return this;
        }

        public ServiceNodeBuilder addOperation(String name) {
            new OperationNode(componentObject, name);
            return this;
        }
    }

    public static class CoreNodeBuilder extends ComponentNodeBuilder<CoreObjectNode> {

        public CoreNodeBuilder(CoreObjectNode coreObject) {
            super(coreObject);
        }

        public CoreNodeBuilder addToSummaryFacet(PropertyNode property) {
            componentObject.getSummaryFacet().addProperty(property);
            return this;
        }

        public CoreNodeBuilder extend(CoreObjectNode boBase) {
            componentObject.setExtendsType(boBase);
            return this;
        }

    }

    public static class VWANodeBuilder extends ComponentNodeBuilder<VWA_Node> {

        public VWANodeBuilder(VWA_Node coreObject) {
            super(coreObject);
        }

        public VWANodeBuilder addAttribute(PropertyNode property) {
            componentObject.getAttributeFacet().addProperty(property);
            return this;
        }
    }

    public static class BusinessNodeBuilder extends ComponentNodeBuilder<BusinessObjectNode> {
        public BusinessNodeBuilder(BusinessObjectNode businessObject) {
            super(businessObject);
        }

        public BusinessNodeBuilder addQueryFacet(String name) {
            String context = null;
            FacetNode newFacet = createFacetNode(name, context, TLFacetType.QUERY);
            componentObject.getModelObject().addQueryFacet((TLFacet) newFacet.getTLModelObject());
            componentObject.linkChild(newFacet, false);
            newFacet.setName(name);
            return this;
        }

        public BusinessNodeBuilder addCustomFacet(String name) {
            String context = null;
            FacetNode newFacet = createFacetNode(name, context, TLFacetType.CUSTOM);
            componentObject.getModelObject().addCustomFacet((TLFacet) newFacet.getTLModelObject());
            componentObject.linkChild(newFacet, false);
            newFacet.setName(name);
            return this;
        }

        public BusinessNodeBuilder addCustomFacet(String name, String context) {
            FacetNode newFacet = createFacetNode(name, context, TLFacetType.CUSTOM);
            componentObject.getModelObject().addCustomFacet((TLFacet) newFacet.getTLModelObject());
            componentObject.linkChild(newFacet, false);
            newFacet.setName(name);
            return this;
        }

        private FacetNode createFacetNode(String name, String context, TLFacetType type) {
            TLFacet tl = createFacet(type);
            tl.setContext(context);
            tl.setLabel(name);
            FacetNode node = new FacetNode(tl);
            return node;
        }

        private TLFacet createFacet(TLFacetType tlFacetType) {
            TLFacet f = new TLFacet();
            f.setFacetType(tlFacetType);
            return f;
        }

        public BusinessNodeBuilder extend(BusinessObjectNode boBase) {
            componentObject.setExtendsType(boBase);
            return this;
        }

        public BusinessNodeBuilder addAlias(String name) {
            new AliasNode(componentObject, name);
            return this;
        }
    }

    protected final T componentObject;

    public ComponentNodeBuilder(T coreObject) {
        this.componentObject = coreObject;
    }

    public static ComponentNodeBuilder<ComponentNode> createSimpleCore(String name) {
        CoreObjectNode newNode = (CoreObjectNode) NodeFactory.newComponent(new TLCoreObject());
        newNode.setName(name);
        newNode.setSimpleType(NodeFinders.findNodeByName("string", Node.XSD_NAMESPACE));
        PropertyNode newProp = new ElementNode(newNode.getSummaryFacet(), "Property");
        newProp.setAssignedType(NodeFinders.findNodeByName("string", Node.XSD_NAMESPACE));
        return new ComponentNodeBuilder<ComponentNode>(newNode);
    }

    public static CoreNodeBuilder createCoreObject(String name) {
        TLCoreObject tlCoreObject = new TLCoreObject();
        tlCoreObject.setName(name);
        tlCoreObject.getSimpleListFacet();
        CoreObjectNode newNode = (CoreObjectNode) NodeFactory.newComponent(tlCoreObject);
        return new CoreNodeBuilder(newNode);
    }

    public static VWANodeBuilder createVWA(String name) {
        TLValueWithAttributes tl = new TLValueWithAttributes();
        tl.setName(name);
        TLSimple simple = new TLSimple();
        simple.setName("fakeName");
        tl.setParentType(simple); // assignment should fail because it is recursive
        // type assignment will fail because of no library
        VWA_Node vwa = (VWA_Node) NodeFactory.newComponent(tl);
        return new VWANodeBuilder(vwa);
    }

    public static BusinessNodeBuilder createBusinessObject(String name) {
        TLBusinessObject tl = new TLBusinessObject();
        tl.setName(name);
        BusinessObjectNode businessNode = (BusinessObjectNode) NodeFactory.newComponent(tl);
        return new BusinessNodeBuilder(businessNode);
    }

    public static SimpleNodeBuilder createSimpleObject(String name) {
        TLSimple simple = new TLSimple();
        simple.setName(name);
        SimpleTypeNode so = (SimpleTypeNode) NodeFactory.newComponent(simple);
        return new SimpleNodeBuilder(so);
    }

    public static ServiceNodeBuilder createService(String name, LibraryNode libraryNode) {
        TLService service = new TLService();
        service.setName(name);
        ServiceNode serviceNode = new ServiceNode(service, libraryNode);
        return new ServiceNodeBuilder(serviceNode);
    }

    public ComponentNodeBuilder<T> addProperty(String name) {
        PropertyNode newProp = new ElementNode(componentObject.getSummaryFacet(), name);
        newProp.setAssignedType(NodeFinders.findNodeByName("string", Node.XSD_NAMESPACE));
        return this;
    }

    public ComponentNodeBuilder<T> setSimpleType() {
        if (componentObject instanceof ComplexComponentInterface) {
            ((ComplexComponentInterface) componentObject).setSimpleType(NodeFinders.findNodeByName(
                    "string", Node.XSD_NAMESPACE));
        }
        return this;
    }

    public T get() {
        return componentObject;
    }

    public T get(LibraryNode target) {
        target.addMember(componentObject);
        return componentObject;
    }

}
