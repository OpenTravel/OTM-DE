package com.sabre.schemas.node.properties;

import org.eclipse.swt.graphics.Image;

import com.sabre.schemacompiler.model.TLEnumValue;
import com.sabre.schemacompiler.model.TLModelElement;
import com.sabre.schemas.node.ComponentNode;
import com.sabre.schemas.node.EnumerationClosedNode;
import com.sabre.schemas.node.EnumerationOpenNode;
import com.sabre.schemas.node.INode;
import com.sabre.schemas.node.ImpliedNode;
import com.sabre.schemas.node.ModelNode;
import com.sabre.schemas.node.Node;
import com.sabre.schemas.node.NodeFactory;
import com.sabre.schemas.node.NodeNameUtils;
import com.sabre.schemas.node.PropertyNodeType;
import com.sabre.schemas.properties.Images;

/**
 * A property node that represents a enumeration literal. See
 * {@link NodeFactory#newComponentMember(INode, Object)}
 * 
 * @author Dave Hollander
 * 
 */

public class EnumLiteralNode extends PropertyNode {

    public EnumLiteralNode(ComponentNode parent, String name) {
        super(new TLEnumValue(), parent, name, PropertyNodeType.ENUM_LITERAL);
        parent.getModelObject().addChild(this.getTLModelObject());

        validateParent(parent);
    }

    public EnumLiteralNode(TLModelElement tlObj, INode parent) {
        super(tlObj, parent, PropertyNodeType.ENUM_LITERAL);

        validateParent(parent);
    }

    private void validateParent(INode parent) {
        if (parent == null) {
            return;
        }

        if (!((parent instanceof EnumerationOpenNode) || (parent instanceof EnumerationClosedNode)))
            throw new IllegalArgumentException("Invalid parent for enumeration literal.");
    }

    @Override
    public boolean canAssign(Node type) {
        return type instanceof ImpliedNode ? true : false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.node.PropertyNode#createProperty(com.sabre.schemas.node.Node)
     */
    @Override
    public INode createProperty(Node type) {
        TLEnumValue tlObj = (TLEnumValue) cloneTLObj();
        int index = indexOfNode();
        ((TLEnumValue) getTLModelObject()).getOwningEnum().addValue(index, tlObj);
        EnumLiteralNode n = new EnumLiteralNode(tlObj, null);
        getParent().getChildren().add(index, n);
        n.setName(type.getName());
        return n;
    }

    @Override
    public ImpliedNode getDefaultType() {
        return ModelNode.getUndefinedNode();
    }

    @Override
    public Image getImage() {
        return Images.getImageRegistry().get(Images.RoleValue);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.node.INode#getLabel()
     */
    @Override
    public String getLabel() {
        return modelObject.getLabel() == null ? "" : modelObject.getLabel();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.node.properties.PropertyNode#getOwningComponent()
     */
    @Override
    public Node getOwningComponent() {
        return getParent();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.node.PropertyNode#setName(java.lang.String)
     */
    @Override
    public void setName(String name) {
        modelObject.setName(NodeNameUtils.fixEnumerationValue(name));
    }

}
