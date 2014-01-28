package org.opentravel.schemas.node.properties;

import org.opentravel.schemas.node.INode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFactory;
import org.opentravel.schemas.node.NodeFinders;
import org.opentravel.schemas.node.PropertyNodeType;
import org.opentravel.schemas.types.TypeUser;

import com.sabre.schemacompiler.model.TLModelElement;

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
     * org.opentravel.schemas.node.properties.PropertyNode#setAssignedType(org.opentravel.schemas.node.Node,
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
     * org.opentravel.schemas.node.properties.PropertyNode#setAssignedType(org.opentravel.schemas.node.Node)
     */
    @Override
    public boolean setAssignedType(Node replacement) {
        return super.setAssignedType(idType);
    }

}
