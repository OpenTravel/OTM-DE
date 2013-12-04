package com.sabre.schemas.node.properties;

import org.eclipse.swt.graphics.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sabre.schemacompiler.model.TLFacetOwner;
import com.sabre.schemacompiler.model.TLModelElement;
import com.sabre.schemas.modelObject.TLnSimpleAttribute;
import com.sabre.schemas.node.INode;
import com.sabre.schemas.node.Node;
import com.sabre.schemas.node.NodeFactory;
import com.sabre.schemas.node.PropertyNodeType;
import com.sabre.schemas.properties.Images;
import com.sabre.schemas.types.TypeUser;

/**
 * A property node that represents a simple property of a core or value with attributes object. See
 * {@link NodeFactory#newComponentMember(INode, Object)}
 * 
 * @author Dave Hollander
 * 
 */

public class SimpleAttributeNode extends PropertyNode implements TypeUser {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleAttributeNode.class);

    public SimpleAttributeNode(TLModelElement tlObj, INode parent) {
        super(tlObj, parent, PropertyNodeType.SIMPLE);

        if (parent != null) {
            TLModelElement tlOwner = ((Node) parent.getParent()).getTLModelObject();
            if ((tlOwner instanceof TLFacetOwner) || (tlObj instanceof TLnSimpleAttribute))
                ((TLnSimpleAttribute) tlObj).setParentObject(tlOwner);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.node.Node#isTypeUser()
     */
    // Not needed because it implements typeUser()
    // @Override
    // public boolean isTypeUser() {
    // return true;
    // }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.node.PropertyNode#createProperty(com.sabre.schemas.node.Node)
     */
    @Override
    public INode createProperty(Node type) {
        // Need for DND but can't actually create a property, just set the type.
        setAssignedType(type);
        return this;
        // LOGGER.error("Tried to create a new simple property.");
        // throw new IllegalAccessError("Tried to create new simple property.");
        // return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.node.Node#isSimpleTypeUser()
     */
    @Override
    public boolean isOnlySimpleTypeUser() {
        return true;
    }

    @Override
    public Image getImage() {
        return Images.getImageRegistry().get(Images.XSDAttribute);
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
     * @see com.sabre.schemas.node.PropertyNode#setName(java.lang.String)
     */
    @Override
    public void setName(String name) {
        LOGGER.debug("Tried to set the name of a simple property.");
    }

}
