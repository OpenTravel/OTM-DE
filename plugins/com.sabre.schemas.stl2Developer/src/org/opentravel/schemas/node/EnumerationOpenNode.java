package org.opentravel.schemas.node;

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemas.node.properties.EnumLiteralNode;
import org.opentravel.schemas.properties.Images;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sabre.schemacompiler.model.LibraryMember;
import com.sabre.schemacompiler.model.TLEnumValue;
import com.sabre.schemacompiler.model.TLOpenEnumeration;

public class EnumerationOpenNode extends ComponentNode implements ComplexComponentInterface {
    private static final Logger LOGGER = LoggerFactory.getLogger(EnumerationOpenNode.class);

    public EnumerationOpenNode(LibraryMember mbr) {
        super(mbr);
        addMOChildren();

        // Set base type.
        if (mbr instanceof TLOpenEnumeration)
            getTypeClass().setTypeNode(ModelNode.getDefaultStringNode());
    }

    /**
     * Note: if the closed enum does not have a library the new enum will not have children copied.
     * 
     * @param closedEnum
     */
    public EnumerationOpenNode(EnumerationClosedNode closedEnum) {
        this(new TLOpenEnumeration());
        if (closedEnum.getLibrary() != null) {
            // Do this first since clone needs to know library information.
            setLibrary(closedEnum.getLibrary());
            getLibrary().getTLaLib().addNamedMember((LibraryMember) this.getTLModelObject());

            for (Node lit : closedEnum.getChildren()) {
                addProperty(lit.clone(this, null));
            }
            getLibrary().getTLaLib().removeNamedMember((LibraryMember) closedEnum.getTLModelObject());
            closedEnum.unlinkNode();
            // If openEnum was being used, the tl model will reassign but not type node
            closedEnum.getTypeClass().replaceTypeProvider(this, null);

            getLibrary().getComplexRoot().linkChild(this);
        }
        setDocumentation(closedEnum.getDocumentation());
        setName(closedEnum.getName());
        closedEnum.delete();
        // LOGGER.debug("Created open enum from closed.");
    }

    @Override
    public void addProperty(Node enumLiteral) {
        if (enumLiteral instanceof EnumLiteralNode) {
            ((TLOpenEnumeration) getTLModelObject()).addValue((TLEnumValue) enumLiteral
                    .getTLModelObject());
            this.linkChild(enumLiteral, false);
        }
    }

    @Override
    public ImpliedNode getDefaultType() {
        return ModelNode.getDefaultStringNode();
    }

    @Override
    public Image getImage() {
        return Images.getImageRegistry().get(Images.Enumeration);
    }

    @Override
    public boolean isEnumeration() {
        return true;
    }

    @Override
    public boolean isNamedType() {
        return true;
    }

    @Override
    public boolean isAssignableToVWA() {
        return true;
    }

    @Override
    public Node setExtensible(boolean extensible) {
        if (!extensible)
            return new EnumerationClosedNode(this);
        return this;
    }

    @Override
    public void merge(Node source) {
        if (!(source instanceof EnumerationOpenNode)) {
            throw new IllegalStateException("Can only merge objects with the same type");
        }
        EnumerationOpenNode open = (EnumerationOpenNode) source;
        for (Node literal : open.getChildren()) {
            addProperty(literal.clone(null, null));
        }
    }

    @Override
    public boolean isMergeSupported() {
        return true;
    }

    @Override
    public ComponentNode getSimpleType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean setSimpleType(Node type) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public ComponentNode getAttributeFacet() {
        return null;
    }

    @Override
    public SimpleFacetNode getSimpleFacet() {
        return null;
    }

}
