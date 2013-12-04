package com.sabre.schemas.node.properties;

import com.sabre.schemacompiler.model.TLModelElement;
import com.sabre.schemas.node.INode;
import com.sabre.schemas.node.Node;
import com.sabre.schemas.node.NodeFactory;
import com.sabre.schemas.node.NodeFinders;
import com.sabre.schemas.node.PropertyNodeType;
import com.sabre.schemas.types.TypeUser;

/**
 * A property node that represents an XML ID. See
 * {@link NodeFactory#newComponentMember(INode, Object)}
 * 
 * @author Dave Hollander
 * 
 */
public class IdNode extends AttributeNode implements TypeUser {
    Node idType = null;

    public IdNode(Node parent, String name) {
        super(parent, name);
        setName("id");
        idType = NodeFinders.findNodeByName("ID", XSD_NAMESPACE);
        setAssignedType(idType);
        setIdentity("xml_ID on " + parent.getOwningComponent());
        propertyType = PropertyNodeType.ID;
    }

    public IdNode(TLModelElement tlObj, INode parent) {
        super(tlObj, parent);
        idType = NodeFinders.findNodeByName("ID", XSD_NAMESPACE);
        setAssignedType(idType);
        setIdentity("xml_ID on " + getOwningComponent());
        propertyType = PropertyNodeType.ID;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.sabre.schemas.node.properties.PropertyNode#setAssignedType(com.sabre.schemas.node.Node,
     * boolean)
     */
    @Override
    public boolean setAssignedType(Node replacement, boolean refresh) {
        return super.setAssignedType(idType, refresh);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.sabre.schemas.node.properties.PropertyNode#setAssignedType(com.sabre.schemas.node.Node)
     */
    @Override
    public boolean setAssignedType(Node replacement) {
        return super.setAssignedType(idType);
    }

}
