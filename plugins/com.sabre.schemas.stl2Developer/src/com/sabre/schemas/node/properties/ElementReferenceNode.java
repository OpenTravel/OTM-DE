package com.sabre.schemas.node.properties;

import org.eclipse.swt.graphics.Image;

import com.sabre.schemacompiler.model.TLModelElement;
import com.sabre.schemacompiler.model.TLProperty;
import com.sabre.schemas.node.BusinessObjectNode;
import com.sabre.schemas.node.CoreObjectNode;
import com.sabre.schemas.node.ExtensionPointNode;
import com.sabre.schemas.node.FacetNode;
import com.sabre.schemas.node.INode;
import com.sabre.schemas.node.ImpliedNode;
import com.sabre.schemas.node.ModelNode;
import com.sabre.schemas.node.Node;
import com.sabre.schemas.node.NodeFactory;
import com.sabre.schemas.node.NodeNameUtils;
import com.sabre.schemas.node.PropertyNodeType;
import com.sabre.schemas.properties.Images;
import com.sabre.schemas.types.TypeUser;

/**
 * A property node that represents an XML element. See
 * {@link NodeFactory#newComponentMember(INode, Object)}
 * 
 * @author Dave Hollander
 * 
 */
public class ElementReferenceNode extends PropertyNode implements TypeUser {

    /**
     * Add an element reference property to a facet or extension point.
     * 
     * @param parent
     *            - if null, the caller must link the node and add to TL Model parent
     * @param name
     */

    public ElementReferenceNode(Node parent, String name) {
        super(new TLProperty(), parent, name, PropertyNodeType.ID_REFERENCE);
        ((TLProperty) getTLModelObject()).setReference(true);
        setAssignedType(ModelNode.getUnassignedNode());

        if (!(parent instanceof FacetNode || parent instanceof ExtensionPointNode))
            throw new IllegalArgumentException("Invalid parent for a element reference.");
    }

    /**
     * Create an element node from the TL Model object.
     * 
     * @param tlObj
     *            TL Model object to represent
     * @param parent
     *            if not null, add element to the parent.
     */
    public ElementReferenceNode(TLModelElement tlObj, INode parent) {
        super(tlObj, parent, PropertyNodeType.ID_REFERENCE);
    }

    @Override
    public boolean canAssign(Node type) {
        if (type.getOwningComponent() instanceof BusinessObjectNode)
            return true;
        if (type.getOwningComponent() instanceof CoreObjectNode)
            return true;
        if (type instanceof ImpliedNode)
            return true;
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.node.PropertyNode#createProperty(com.sabre.schemas.node.Node)
     */
    @Override
    public INode createProperty(Node type) {
        int index = indexOfNode();
        TLProperty tlObj = (TLProperty) cloneTLObj();
        tlObj.setReference(true);
        ((TLProperty) getTLModelObject()).getPropertyOwner().addElement(index, tlObj);
        ElementReferenceNode n = new ElementReferenceNode(tlObj, null);
        n.setName(type.getName());
        getParent().linkChild(n, indexOfNode());
        n.setDescription(type.getDescription());
        n.setAssignedType(type);
        return n;
    }

    @Override
    public Image getImage() {
        return Images.getImageRegistry().get(Images.ID_Reference);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.node.INode#getLabel()
     */
    @Override
    public String getLabel() {
        return modelObject.getLabel();
    }

    @Override
    public int indexOfTLProperty() {
        final TLProperty thisProp = (TLProperty) getTLModelObject();
        return thisProp.getPropertyOwner().getElements().indexOf(thisProp);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.node.Node#isID_Reference()
     */
    @Override
    public boolean isID_Reference() {
        return true;
    }

    @Override
    public boolean isElement() {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.node.Node#isTypeUser()
     */
    @Override
    public boolean isTypeUser() {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.node.PropertyNode#setName(java.lang.String)
     */
    @Override
    public void setName(String name) {
        if (getType() != null || !(getType() instanceof ImpliedNode))
            modelObject.setName(NodeNameUtils.fixElementRefName(name));
        else
            modelObject.setName(NodeNameUtils.fixElementRefName(getType().getName()));
    }

}
