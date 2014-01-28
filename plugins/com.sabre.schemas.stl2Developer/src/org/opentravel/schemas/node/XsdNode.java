/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemas.node;

import org.opentravel.schemas.modelObject.TLEmpty;
import org.opentravel.schemas.modelObject.XSDElementMO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sabre.schemacompiler.model.LibraryMember;
import com.sabre.schemacompiler.model.NamedEntity;

/**
 * The XsdNode class extends componentNode with the addition of lazy evaluated modeled nodes
 * (otmModel), mo and tl objects with access methods.
 * 
 * @author Dave Hollander
 * 
 */
public class XsdNode extends ComponentNode implements SimpleComponentInterface {

    private static final Logger LOGGER = LoggerFactory.getLogger(XsdNode.class);
    protected ComponentNode otmModel = null; // a pointer to a node/model object
                                             // and tlObj
                                             // representing the xsd type

    /**
     * Create an XsdNode to represent and XSD Simple or Complex type. Creates an XsdNode with model
     * object, sets name and description, links TL library member to MO
     * 
     * @param obj
     *            - the TL XSDComplexType or XSDSimpleType
     */
    public XsdNode(final LibraryMember obj, LibraryNode lib) {
        super(obj); //
        this.setLibrary(lib);
        // Build all of the tl models now so they and their local types get rendered in the tree
        this.createTLModelChild();
    }

    /**
     * Utility function - use getOtmModelChild() which will create one if it did not exist.
     * 
     * @return
     */
    protected boolean hasOtmModelChild() {
        return otmModel == null ? false : true;
    }

    /**
     * Return the TL model Rendered child of this xsd node. If one does not exist, it tries to
     * create one. The new node is a member of the generated library and <b>not</b> part of the
     * TLModel. They can not be or else there will be name collisions.
     * 
     * @return
     */
    public ComponentNode getOtmModelChild() {
        return otmModel == null ? createTLModelChild() : otmModel;
    }

    private ComponentNode createTLModelChild() {
        // LOGGER.debug("Creating TLModel Child for node " +
        // this.getNameWithPrefix()+" in namespace "+ this.getNamespace());
        if (this.getLibrary() == null)
            LOGGER.error("Can not create a TL Model child without a library!. " + this.getName());

        // Use this model object to build a TL_Object and use that to create a node.
        ComponentNode cn = NodeFactory.newComponent_UnTyped(modelObject.buildTLModel(this));
        if (cn != null) {
            cn.xsdNode = this;
            otmModel = cn;
            cn.setLibrary(getLibrary());
            xsdType = true;
            setXsdTypeOnChildren(cn);
        }
        return cn;
    }

    private void setXsdTypeOnChildren(Node n) {
        n.xsdType = true;
        for (Node c : n.getChildren())
            setXsdTypeOnChildren(c);
    }

    @Override
    public String getName() {
        return modelObject.getName();
    }

    @Override
    public boolean isImportable() {
        if ((isInXSDSchema() || isInBuiltIn()) && getOtmModelChild() != null
                && !(getOtmModelChild().getModelObject().getTLModelObj() instanceof TLEmpty)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isDeleteable() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.node.Node#isMissingAssignedType()
     */
    @Override
    public boolean isMissingAssignedType() {
        // LOGGER.debug("check xsdNode "+getName()+" for missing type");
        return getOtmModelChild().isMissingAssignedType();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.node.ComponentNode#hasNavChildrenWithProperties()
     */
    @Override
    public boolean hasNavChildrenWithProperties() {
        if (getOtmModelChild() == null)
            return false;
        for (final INode n : getOtmModelChild().getChildren()) {
            // if (n.isNavChildWithProperties()) {
            return true;
            // }
        }
        return false;
    }

    public boolean isXsdElement() {
        return (modelObject instanceof XSDElementMO) ? true : false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.node.ComponentNode#isCoreObject()
     */
    @Override
    public boolean isCoreObject() {
        return otmModel.isCoreObject();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.node.Node#isEnumeration()
     */
    @Override
    public boolean isEnumeration() {
        return otmModel.isEnumeration();
    }

    // /*
    // * (non-Javadoc)
    // *
    // * @see org.opentravel.schemas.node.Node#isEnumerationOpen()
    // */
    // @Override
    // public boolean isEnumerationOpen() {
    // return otmModel.isEnumerationOpen();
    // }
    //
    // /*
    // * (non-Javadoc)
    // *
    // * @see org.opentravel.schemas.node.Node#isEnumerationClosed()
    // */
    // @Override
    // public boolean isEnumerationClosed() {
    // return otmModel instanceof EnumerationClosedNode;
    // }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.node.ComponentNode#isSimpleType()
     */
    @Override
    public boolean isSimpleType() {
        return otmModel.isSimpleType();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.node.ComponentNode#isBusinessObject()
     */
    @Override
    public boolean isBusinessObject() {
        return otmModel.isBusinessObject();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.node.Node#isValueWithAttributes()
     */
    @Override
    public boolean isValueWithAttributes() {
        return otmModel.isValueWithAttributes();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.node.SimpleComponentInterface#isSimpleTypeProvider()
     */
    @Override
    public boolean isSimpleTypeProvider() {
        return otmModel instanceof SimpleComponentInterface;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.node.SimpleComponentInterface#getBaseType()
     */
    @Override
    public INode getBaseType() {
        return otmModel.isBaseTypeUser() ? otmModel.getType() : null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.node.SimpleComponentInterface#getTLOjbect()
     */
    @Override
    public NamedEntity getTLOjbect() {
        return otmModel.getTLModelObject() instanceof NamedEntity ? (NamedEntity) otmModel
                .getTLModelObject() : null;
    }

}
