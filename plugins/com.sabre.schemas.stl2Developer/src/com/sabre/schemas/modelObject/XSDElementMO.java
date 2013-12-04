/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.modelObject;

import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3._2001.xmlschema.Element;

import com.sabre.schemacompiler.model.AbstractLibrary;
import com.sabre.schemacompiler.model.LibraryMember;
import com.sabre.schemacompiler.model.XSDElement;
import com.sabre.schemas.node.XsdNode;

public class XSDElementMO extends ModelObject<XSDElement> {
    private static final Logger LOGGER = LoggerFactory.getLogger(XSDElementMO.class);

    private LibraryMember tlObj = null;

    public XSDElementMO(final XSDElement obj) {
        super(obj);
    }

    /**
     * Create a TLSimple from the xsdSimpleType's JAXB object.
     * 
     * @return
     */
    @Override
    public LibraryMember buildTLModel(XsdNode node) {
        return this.getTLModelObj().getJaxbElement() == null ? null : buildTLModel(this
                .getTLModelObj().getJaxbElement().getName(), node);
    }

    public LibraryMember buildTLModel(String name, XsdNode xsdNode) {
        if (tlObj != null)
            return tlObj; // already built, just return it.

        final Element jaxbEle = this.getTLModelObj().getJaxbElement();
        if (jaxbEle == null)
            return null;

        if (jaxbEle.getComplexType() != null)
            tlObj = XsdModelingUtils.buildCoreObject(jaxbEle.getComplexType(), jaxbEle.getName(),
                    xsdNode);
        else if (jaxbEle.getSimpleType() != null)
            tlObj = XsdModelingUtils.buildSimpleObject(jaxbEle.getSimpleType(), jaxbEle.getName(),
                    xsdNode);
        // LOGGER.debug("Created XSD Element model object: " + jaxbEle.getName());
        return tlObj;
    }

    @Override
    public void delete() {
        System.out.println("ModelObject - delete - TODO");
    }

    // @Override
    // public List<?> getChildren() {
    // return null;
    // }

    @Override
    public String getComponentType() {
        return "XSD Element";
    }

    @Override
    protected AbstractLibrary getLibrary(final XSDElement obj) {
        return null;
    }

    @Override
    public String getName() {
        return getTLModelObj().getLocalName();
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
    public void setDeprecatedDoc(final String string, final int i) {
    }

    @Override
    public void setDeveloperDoc(final String string, final int index) {
    }

    @Override
    public void setReferenceDoc(final String string, final int index) {
    }

    @Override
    public void setMoreInfo(final String string, final int index) {
    }

    @Override
    public void setOtherDoc(final String string, final String context) {
    }

    // @Override
    // public String getAssignedTypeName() {
    // QName typeName = srcObj.getJaxbElement().getType();
    // return typeName != null ? typeName.getPrefix() + ":" + typeName.getLocalPart() : "";
    // }

    @Override
    public String getAssignedName() {
        QName typeName = srcObj.getJaxbElement().getType();
        return typeName != null ? typeName.getLocalPart() : "";
    }

    @Override
    public String getAssignedPrefix() {
        QName typeName = srcObj.getJaxbElement().getType();
        return typeName != null ? typeName.getPrefix() : "";
    }

}
