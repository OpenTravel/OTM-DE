package org.opentravel.schemas.node.properties;

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemas.node.INode;
import org.opentravel.schemas.node.ImpliedNode;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFactory;
import org.opentravel.schemas.node.PropertyNodeType;
import org.opentravel.schemas.node.RoleFacetNode;
import org.opentravel.schemas.properties.Images;

import com.sabre.schemacompiler.model.TLModelElement;
import com.sabre.schemacompiler.model.TLRole;

/**
 * A property node that represents a role enumeration value in a core object. See
 * {@link NodeFactory#newComponentMember(INode, Object)}
 * 
 * @author Dave Hollander
 * 
 */

public class RoleNode extends PropertyNode {

    public RoleNode(RoleFacetNode parent, String name) {
        super(new TLRole(), parent, name, PropertyNodeType.ROLE);
    }

    public RoleNode(TLModelElement tlObj, RoleFacetNode parent) {
        super(tlObj, parent, PropertyNodeType.ROLE);
    }

    @Override
    public boolean canAssign(Node type) {
        return type instanceof ImpliedNode ? true : false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.node.PropertyNode#createProperty(org.opentravel.schemas.node.Node)
     */
    @Override
    public INode createProperty(Node type) {
        TLRole tlObj = (TLRole) cloneTLObj();
        int index = indexOfNode();
        ((TLRole) getTLModelObject()).getRoleEnumeration().addRole(index, tlObj);
        RoleNode n = new RoleNode(tlObj, null);

        getParent().getChildren().add(index, n);
        n.setParent(getParent());
        setLibrary(getParent().getLibrary());
        n.setName(type.getName());
        n.setDescription(type.getDescription());
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
     * @see org.opentravel.schemas.node.INode#getLabel()
     */
    @Override
    public String getLabel() {
        return modelObject.getLabel() == null ? "" : modelObject.getLabel();
    }

    @Override
    public RoleFacetNode getParent() {
        return (RoleFacetNode) parent;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.node.PropertyNode#setName(java.lang.String)
     */
    @Override
    public void setName(String name) {
        modelObject.setName(name);
    }

}
