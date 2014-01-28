package org.opentravel.schemas.node.properties;

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemas.node.INode;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeEditStatus;
import org.opentravel.schemas.node.NodeFactory;
import org.opentravel.schemas.node.NodeNameUtils;
import org.opentravel.schemas.node.PropertyNodeType;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.types.TypeUser;

import com.sabre.schemacompiler.model.TLModelElement;
import com.sabre.schemacompiler.model.TLProperty;

/**
 * A property node that represents an XML element. See
 * {@link NodeFactory#newComponentMember(INode, Object)}
 * 
 * @author Dave Hollander
 * 
 */

public class ElementNode extends PropertyNode implements TypeUser {

    /**
     * Add an element property to a facet or extension point.
     * 
     * @param parent
     *            - if null, the caller must link the node and add to TL Model parent
     * @param name
     */
    public ElementNode(Node parent, String name) {
        super(new TLProperty(), parent, name, PropertyNodeType.ELEMENT);
        setAssignedType(ModelNode.getUnassignedNode());
    }

    /**
     * Create an element node from the TL Model object.
     * 
     * @param tlObj
     *            TL Model object to represent
     * @param parent
     *            if not null, add element to the parent.
     */
    public ElementNode(TLModelElement tlObj, INode parent) {
        super(tlObj, parent, PropertyNodeType.ELEMENT);
        if (getEditStatus().equals(NodeEditStatus.MINOR))
            setMandatory(false);
        else if (tlObj instanceof TLProperty)
            setMandatory(((TLProperty) tlObj).isMandatory()); // default value for properties
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.node.PropertyNode#createProperty(org.opentravel.schemas.node.Node)
     */
    @Override
    public INode createProperty(Node type) {
        int index = indexOfNode();
        int tlIndex = indexOfTLProperty();
        TLProperty tlObj = (TLProperty) cloneTLObj();
        ((TLProperty) getTLModelObject()).getPropertyOwner().addElement(tlIndex, tlObj);
        ElementNode n = new ElementNode(tlObj, null);
        getParent().linkChild(n, indexOfNode());
        n.setDescription(type.getDescription());
        n.setAssignedType(type);
        n.setName(type.getName());
        return n;
    }

    /**
     * Get the index (0..sizeof()) of this property in the facet list.
     */
    @Override
    public int indexOfTLProperty() {
        final TLProperty thisProp = (TLProperty) getTLModelObject();
        return thisProp.getPropertyOwner().getElements().indexOf(thisProp);
    }

    @Override
    public Image getImage() {
        return Images.getImageRegistry().get(Images.XSDElement);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.node.INode#getLabel()
     */
    @Override
    public String getLabel() {
        String label = modelObject.getLabel();
        if (getType() != null)
            label = getName() + " [" + getTypeNameWithPrefix() + "]";
        return label;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.node.PropertyNode#isElementProperty()
     */
    @Override
    public boolean isElement() {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.node.Node#isTypeUser()
     */
    @Override
    public boolean isTypeUser() {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.node.PropertyNode#setName(java.lang.String)
     */
    @Override
    public void setName(String name) {
        modelObject.setName(name); // try the passed name
        modelObject.setName(NodeNameUtils.fixElementName(this)); // let utils fix it if needed.
    }

    @Override
    public void setName(String name, boolean doFamily) {
        setName(name);
    }

}
