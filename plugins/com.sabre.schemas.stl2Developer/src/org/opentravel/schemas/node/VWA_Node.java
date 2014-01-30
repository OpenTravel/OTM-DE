/**
 * 
 */
package org.opentravel.schemas.node;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemas.modelObject.ValueWithAttributesAttributeFacetMO;
import org.opentravel.schemas.properties.Images;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;

/**
 * Value With Attributes. This object has two facets, the value is a simple facet and attributes
 * facet. This makes it a complex interface implementation.
 * 
 * Type Users under a VWA can only be assigned simple, vwa or enumeration (open or closed) objects.
 * 
 * @author Dave Hollander
 * 
 */
public class VWA_Node extends ComponentNode implements ComplexComponentInterface {
    @SuppressWarnings("unused")
    private static final Logger LOGGER = LoggerFactory.getLogger(VWA_Node.class);

    public VWA_Node(LibraryMember mbr) {
        super(mbr);
        addMOChildren();
        if (((TLValueWithAttributes) getTLModelObject()).getParentType() == null)
            setSimpleType((Node) ModelNode.getEmptyNode());
    }

    public VWA_Node(BusinessObjectNode bo) {
        this(new TLValueWithAttributes());

        setLibrary(bo.getLibrary());
        setName(bo.getName());
        setDocumentation(bo.getDocumentation());

        ((FacetNode) getAttributeFacet()).copyFacet((FacetNode) bo.getIDFacet());
        ((FacetNode) getAttributeFacet()).copyFacet(bo.getSummaryFacet());
        ((FacetNode) getAttributeFacet()).copyFacet((FacetNode) bo.getDetailFacet());
        setSimpleType((Node) ModelNode.getEmptyNode());
    }

    public VWA_Node(CoreObjectNode core) {
        this(new TLValueWithAttributes());

        setLibrary(core.getLibrary());
        setName(core.getName());
        setDocumentation(core.getDocumentation());

        ((FacetNode) getAttributeFacet()).copyFacet((FacetNode) core.getSummaryFacet());
        ((FacetNode) getAttributeFacet()).copyFacet((FacetNode) core.getDetailFacet());
        setSimpleType(core.getSimpleType());
    }

    // @Override
    // public boolean canAssign(Node type) {
    // // TODO - make sure all simple type providers implement the simple interface
    // if (type instanceof SimpleComponentInterface)
    // return true;
    // if (type.isSimpleType())
    // return true;
    // if (type instanceof VWA_Node)
    // return true;
    // if (type instanceof EnumerationOpenNode)
    // return true;
    // return false;
    // }

    // @Override
    // public Node getAssignedType() {
    // return getSimpleType();
    // }

    @Override
    public QName getTLTypeQName() {
        QName typeQname = null;
        typeQname = getSimpleFacet().getSimpleAttribute().getTLTypeQName();
        return typeQname;
    }

    @Override
    public NamedEntity getTLTypeObject() {
        return ((TLValueWithAttributes) getTLModelObject()).getParentType();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.types.TypeProvider#getTypeNode()
     */
    // @Override
    // public Node getTypeNode() {
    // if (getSimpleFacet() != null && getSimpleFacet().getSimpleAttribute() != null)
    // return getSimpleFacet().getSimpleAttribute().getTypeClass().getTypeNode();
    // else
    // return null;
    // }
    //
    // @Override
    // public Type getTypeClass() {
    // if (getSimpleFacet() != null && getSimpleFacet().getSimpleAttribute() != null)
    // return getSimpleFacet().getSimpleAttribute().getTypeClass();
    // else
    // return null;
    // }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.node.Node#isNamedType()
     */
    @Override
    public boolean isNamedType() {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.node.Node#isTypeProvider()
     */
    @Override
    public boolean isTypeProvider() {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.node.Node#isValueWithAttributes()
     */
    @Override
    public boolean isValueWithAttributes() {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.node.Node#getChildren_TypeUsers()
     */
    @Override
    public List<Node> getChildren_TypeUsers() {
        ArrayList<Node> users = new ArrayList<Node>();
        users.add(getSimpleFacet().getSimpleAttribute());
        // users.add(getSimpleType());
        users.addAll(getAttributeFacet().getChildren());
        return users;
    }

    @Override
    public Image getImage() {
        return Images.getImageRegistry().get(Images.ValueWithAttr);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.node.ComponentNode#getSimpleFacet()
     */
    @Override
    public SimpleFacetNode getSimpleFacet() {
        for (INode f : getChildren()) {
            if (f instanceof SimpleFacetNode)
                return (SimpleFacetNode) f;
        }
        return null;
    }

    @Override
    public ComponentNode getDefaultFacet() {
        return getAttributeFacet();
    }

    @Override
    public ComponentNode getAttributeFacet() {
        for (INode f : getChildren()) {
            if (f.getModelObject() instanceof ValueWithAttributesAttributeFacetMO)
                return (ComponentNode) f;
        }
        return null;

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.node.ComplexComponentInterface#getSimpleType()
     */
    @Override
    public ComponentNode getSimpleType() {
        return (ComponentNode) getSimpleFacet().getSimpleAttribute().getAssignedType();
    }

    @Override
    public void sort() {
        ((FacetNode) getAttributeFacet()).sort();
    }

    @Override
    public boolean setSimpleType(Node type) {
        return getSimpleFacet().getSimpleAttribute().setAssignedType(type);
    }

    @Override
    public void merge(Node source) {
        if (!(source instanceof VWA_Node)) {
            throw new IllegalStateException("Can only merge objects with the same type");
        }
        VWA_Node vwa = (VWA_Node) source;
        getAttributeFacet().addProperties(vwa.getAttributeFacet().getChildren(), true);
    }

    @Override
    public boolean isAssignableToVWA() {
        return true;
    }

    @Override
    public boolean isMergeSupported() {
        return true;
    }

}
