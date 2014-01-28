package org.opentravel.schemas.node;

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemas.node.properties.EnumLiteralNode;
import org.opentravel.schemas.properties.Images;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sabre.schemacompiler.model.LibraryMember;
import com.sabre.schemacompiler.model.TLClosedEnumeration;
import com.sabre.schemacompiler.model.TLEnumValue;

public class EnumerationClosedNode extends SimpleTypeNode {
    private static final Logger LOGGER = LoggerFactory.getLogger(EnumerationClosedNode.class);

    public EnumerationClosedNode(LibraryMember mbr) {
        super(mbr);
        addMOChildren();

        // Set base type.
        if (mbr instanceof TLClosedEnumeration)
            getTypeClass().setTypeNode(ModelNode.getDefaultStringNode());
    }

    public EnumerationClosedNode(EnumerationOpenNode openEnum) {
        this(new TLClosedEnumeration());
        if (openEnum.getLibrary() != null) {
            setLibrary(openEnum.getLibrary());
            getLibrary().getTLaLib().addNamedMember((LibraryMember) this.getTLModelObject());

            for (Node lit : openEnum.getChildren()) {
                addProperty(lit.clone(this, null));
            }
            getLibrary().getTLaLib().removeNamedMember((LibraryMember) openEnum.getTLModelObject());
            openEnum.unlinkNode();
            // If openEnum was being used, the tl model will reassign but not type node
            openEnum.getTypeClass().replaceTypeProvider(this, null);

            getLibrary().getSimpleRoot().linkChild(this);
        }

        setDocumentation(openEnum.getDocumentation());
        setName(openEnum.getName());
        openEnum.delete();
        // LOGGER.debug("Created closed enum from open.");
    }

    @Override
    public void addProperty(Node enumLiteral) {
        if (enumLiteral instanceof EnumLiteralNode) {
            ((TLClosedEnumeration) getTLModelObject()).addValue((TLEnumValue) enumLiteral
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
    public boolean isAssignableToSimple() {
        return true;
    }

    @Override
    public boolean isAssignableToVWA() {
        return true;
    }

    @Override
    public boolean isEnumeration() {
        return true;
    }

    @Override
    public Node setExtensible(boolean extensible) {
        if (extensible)
            return new EnumerationOpenNode(this);
        return this;
    }

    @Override
    public void merge(Node source) {
        if (!(source instanceof EnumerationClosedNode)) {
            throw new IllegalStateException("Can only merge objects with the same type");
        }
        EnumerationClosedNode open = (EnumerationClosedNode) source;
        for (Node literal : open.getChildren()) {
            addProperty(literal.clone(null, null));
        }
    }

    @Override
    public boolean isMergeSupported() {
        return true;
    }

}
