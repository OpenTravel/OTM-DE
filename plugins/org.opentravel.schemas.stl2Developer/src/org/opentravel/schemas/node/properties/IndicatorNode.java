package org.opentravel.schemas.node.properties;

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemas.node.INode;
import org.opentravel.schemas.node.ImpliedNode;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFactory;
import org.opentravel.schemas.node.NodeNameUtils;
import org.opentravel.schemas.node.PropertyNodeType;
import org.opentravel.schemas.properties.Images;

import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLModelElement;

/**
 * A property node that represents a boolean XML attribute with the semantics of
 * "False unless present and true". See {@link NodeFactory#newComponentMember(INode, Object)}
 * 
 * @author Dave Hollander
 * 
 */

public class IndicatorNode extends PropertyNode {

    public IndicatorNode(Node parent, String name) {
        super(new TLIndicator(), parent, name, PropertyNodeType.INDICATOR);
    }

    /**
     * 
     * @param tlObj
     * @param parent
     *            either a facet or extension point facet
     */
    public IndicatorNode(TLModelElement tlObj, INode parent) {
        super(tlObj, parent, PropertyNodeType.INDICATOR);

        if (!(tlObj instanceof TLIndicator))
            throw new IllegalArgumentException("Invalid object for an indicator.");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.node.PropertyNode#createProperty(org.opentravel.schemas.node.Node)
     */
    @Override
    public INode createProperty(Node type) {
        TLIndicator tlObj = (TLIndicator) cloneTLObj();
        int index = indexOfNode();
        ((TLIndicator) getTLModelObject()).getOwner().addIndicator(index, tlObj);
        IndicatorElementNode n = new IndicatorElementNode(tlObj, null);
        n.setName(type.getName());
        getParent().linkChild(n, indexOfNode());
        ((TLIndicator) getTLModelObject()).getOwner().addIndicator(index, tlObj);
        n.setDescription(type.getDescription());
        return n;
    }

    @Override
    public ImpliedNode getDefaultType() {
        return ModelNode.getIndicatorNode();
    }

    @Override
    public Image getImage() {
        return Images.getImageRegistry().get(Images.Indicator);
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
    public int indexOfTLProperty() {
        final TLIndicator thisProp = (TLIndicator) getTLModelObject();
        return thisProp.getOwner().getIndicators().indexOf(thisProp);
    }

    @Override
    public boolean isIndicator() {
        return true;
    }

    // /*
    // * (non-Javadoc)
    // *
    // * @see org.opentravel.schemas.node.PropertyNode#isIndicatorProperty()
    // */
    // @Override
    // public boolean isIndicatorAttribute() {
    // return true;
    // }

    /*
     * (non-Javadoc)
     * 
     * @see org.opentravel.schemas.node.PropertyNode#setName(java.lang.String)
     */
    @Override
    public void setName(String name) {
        modelObject.setName(NodeNameUtils.fixIndicatorName(name));
    }

}
