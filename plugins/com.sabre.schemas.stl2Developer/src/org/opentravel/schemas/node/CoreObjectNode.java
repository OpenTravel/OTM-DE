/**
 * 
 */
package org.opentravel.schemas.node;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemas.modelObject.CoreObjectMO;
import org.opentravel.schemas.modelObject.ListFacetMO;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.properties.Images;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sabre.schemacompiler.model.LibraryMember;
import com.sabre.schemacompiler.model.TLComplexTypeBase;
import com.sabre.schemacompiler.model.TLCoreObject;
import com.sabre.schemacompiler.model.TLFacetType;

/**
 * Core Object. This object has many facets: simple, summary, detail, roles and two lists. It
 * implements the complex component interface.
 * 
 * @author Dave Hollander
 * 
 */
public class CoreObjectNode extends ComponentNode implements ComplexComponentInterface {
    @SuppressWarnings("unused")
    private static final Logger LOGGER = LoggerFactory.getLogger(CoreObjectNode.class);

    public CoreObjectNode(LibraryMember mbr) {
        super(mbr);
        addMOChildren();
        if (((CoreObjectMO) modelObject).getSimpleValueType() == null)
            setSimpleType((Node) ModelNode.getEmptyNode());
    }

    public CoreObjectNode(BusinessObjectNode bo) {
        this(new TLCoreObject());

        setLibrary(bo.getLibrary());
        setName(bo.getName());
        setDocumentation(bo.getDocumentation());

        ((FacetNode) getSummaryFacet()).copyFacet((FacetNode) bo.getIDFacet());
        ((FacetNode) getSummaryFacet()).copyFacet(bo.getSummaryFacet());
        ((FacetNode) getDetailFacet()).copyFacet((FacetNode) bo.getDetailFacet());
        setSimpleType((Node) ModelNode.getEmptyNode());
    }

    public CoreObjectNode(VWA_Node vwa) {
        this(new TLCoreObject());

        setLibrary(vwa.getLibrary());
        setName(vwa.getName());
        setDocumentation(vwa.getDocumentation());

        ((FacetNode) getSummaryFacet()).copyFacet((FacetNode) vwa.getAttributeFacet());
        setSimpleType(vwa.getSimpleType());
    }

    @Override
    public boolean isExtensible() {
        return getTLModelObject() != null ? !((TLComplexTypeBase) getTLModelObject())
                .isNotExtendable() : false;
    }

    @Override
    public boolean isExtensibleObject() {
        return true;
    }

    @Override
    public Node setExtensible(boolean extensible) {
        if (getTLModelObject() instanceof TLComplexTypeBase)
            ((TLComplexTypeBase) getTLModelObject()).setNotExtendable(!extensible);
        return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.types.TypeProvider#getTypeNode()
     */
    @Override
    public Node getTypeNode() {
        return getTypeClass().getTypeNode();
    }

    @Override
    public boolean isNamedType() {
        return true;
    }

    @Override
    public List<Node> getChildren_TypeUsers() {
        ArrayList<Node> users = new ArrayList<Node>();
        users.add(getSimpleType());
        users.addAll(getSummaryFacet().getChildren());
        users.addAll(getDetailFacet().getChildren());
        return users;
    }

    @Override
    public String getLabel() {
        if (getExtendsType() == null)
            return super.getLabel();
        else
            return super.getLabel() + " (E> " + getExtendsType().getNameWithPrefix() + ")";
    }

    @Override
    public Node getExtendsType() {
        return getTypeClass().getTypeNode();
    }

    @Override
    public ComponentNode getSimpleType() {
        return (ComponentNode) getSimpleFacet().getSimpleAttribute().getAssignedType();
    }

    @Override
    public boolean setSimpleType(Node type) {
        return getSimpleFacet().getSimpleAttribute().setAssignedType(type);
    }

    @Override
    public SimpleFacetNode getSimpleFacet() {
        for (INode f : getChildren())
            if (f instanceof SimpleFacetNode)
                return (SimpleFacetNode) f;
        return null;
    }

    @Override
    public ComponentNode getSummaryFacet() {
        for (INode f : getChildren())
            if (((FacetNode) f).getFacetType().equals(TLFacetType.SUMMARY))
                return (ComponentNode) f;
        return null;
    }

    @Override
    public ComponentNode getDetailFacet() {
        for (INode f : getChildren())
            if (((FacetNode) f).getFacetType().equals(TLFacetType.DETAIL))
                return (ComponentNode) f;
        return null;
    }

    // Role w/model object RoleEnumerationMO
    @Override
    public RoleFacetNode getRoleFacet() {
        for (Node f : getChildren())
            if (f instanceof RoleFacetNode)
                return (RoleFacetNode) f;
        return null;
    }

    // List w/model object ListFacetMO - Simple_List
    public ComponentNode getSimpleListFacet() {
        for (Node f : getChildren())
            if (f.modelObject instanceof ListFacetMO)
                if (((ListFacetMO) f.modelObject).isSimpleList())
                    return (ComponentNode) f;
        return null;
    }

    // List w/model object ListFacetMO - Detail_List
    public ComponentNode getDetailListFacet() {
        for (Node f : getChildren())
            if (f.modelObject instanceof ListFacetMO)
                if (((ListFacetMO) f.modelObject).isDetailList())
                    return (ComponentNode) f;

        return null;
    }

    @Override
    public Image getImage() {
        return Images.getImageRegistry().get(Images.CoreObject);
    }

    @Override
    public ComponentNode getAttributeFacet() {
        return null;
    }

    @Override
    public boolean hasChildren_TypeProviders() {
        return isXsdType() ? false : true;
    }

    @Override
    public boolean isAssignableToSimple() {
        return true;
    }

    @Override
    public boolean isAssignableToVWA() {
        return true;
    }

    @Override
    public boolean isAssignedByReference() {
        // Note, core can also be assigned by type.
        return true;
    }

    @Override
    public void setName(String n) {
        this.setName(n, true);
    }

    @Override
    public void setName(String n, boolean doFamily) {
        super.setName(n, doFamily);
        for (Node user : getTypeUsers()) {
            if (user instanceof PropertyNode)
                user.setName(n);
        }
    }

    @Override
    public void sort() {
        ((FacetNode) getSummaryFacet()).sort();
        ((FacetNode) getDetailFacet()).sort();
    }

    @Override
    public void merge(Node source) {
        if (!(source instanceof CoreObjectNode)) {
            throw new IllegalStateException("Can only merge objects with the same type");
        }
        CoreObjectNode core = (CoreObjectNode) source;
        getSummaryFacet().addProperties(core.getSummaryFacet().getChildren(), true);
        getDetailFacet().addProperties(core.getDetailFacet().getChildren(), true);
        getRoleFacet().addProperties(core.getRoleFacet().getChildren(), true);
    }

    @Override
    public boolean isMergeSupported() {
        return true;
    }

}
