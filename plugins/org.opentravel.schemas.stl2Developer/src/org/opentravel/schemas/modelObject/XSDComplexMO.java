
package org.opentravel.schemas.modelObject;

import java.util.List;

import org.opentravel.schemas.node.XsdNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3._2001.xmlschema.Annotation;
import org.w3._2001.xmlschema.TopLevelComplexType;

import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.XSDComplexType;

public class XSDComplexMO extends ModelObject<XSDComplexType> {
    private static final Logger LOGGER = LoggerFactory.getLogger(XSDComplexMO.class);

    private LibraryMember tlObj = null; // local storage for model objects representing the xsd type

    public XSDComplexMO(final XSDComplexType obj) {
        super(obj);
    }

    /**
     * Provide access to utility functions
     */
    protected XSDComplexMO() {
    }

    @Override
    public void delete() {
        LOGGER.debug("ModelObject - delete - TODO");
    }

    /**
     * Complex XSD types have children taken from the jaxb object.
     */
    @Override
    public List<?> getChildren() {
        return null;
    }

    /**
     * Create a TLModel element from the xsd complex type.
     * 
     * If it is simple content, then create a Value With Attributes. If it is complex content, then
     * create a core object with all properties in the summary facet. Local anonymous types are also
     * created.
     * 
     * @param xsdNode
     *            - node that contains this model object.
     */
    @Override
    public LibraryMember buildTLModel(XsdNode xsdNode) {
        // LOGGER.debug("\nXSDComplexMO:buildTLModel() - " + this.getName());
        if (tlObj != null) {
            return tlObj; // already built, just return it.
        }

        if (getTLModelObj() == null || getTLModelObj().getJaxbType() == null) {
            LOGGER.error("XSDComplexMO:buildTLModel() - ERROR, model or jaxbtype is null.");
            return null;
        }
        final TopLevelComplexType tlc = getTLModelObj().getJaxbType();
        return XsdModelingUtils.buildTLModel(tlObj, tlc, xsdNode);
    }

    /**
     * Create a TLDocumentation object. The description within the documentation is set to
     * annotation content if any.
     * 
     * @param annotation
     * @return - the new TLDocumentation object (always)
     */
    protected TLDocumentation makeDoc(final Annotation annotation) {
        return XsdModelingUtils.createDoc(annotation);
    }

    @Override
    public String getComponentType() {
        return "XSD Complex Type";
    }

    @Override
    protected AbstractLibrary getLibrary(final XSDComplexType obj) {
        return null;
    }

    @Override
    public String getName() {
        return getTLModelObj().getLocalName();
    }

    @Override
    public String getNamePrefix() {
        return getTLModelObj().getOwningLibrary().getPrefix();
    }

    @Override
    public String getNamespace() {
        return getTLModelObj().getNamespace();
    }

    @Override
    public boolean isComplexAssignable() {
        return true;
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

}
