package com.sabre.schemas.node.properties;

import org.eclipse.swt.graphics.Image;

import com.sabre.schemacompiler.model.TLIndicator;
import com.sabre.schemacompiler.model.TLModelElement;
import com.sabre.schemas.node.INode;
import com.sabre.schemas.node.ImpliedNode;
import com.sabre.schemas.node.ModelNode;
import com.sabre.schemas.node.Node;
import com.sabre.schemas.node.NodeFactory;
import com.sabre.schemas.node.NodeNameUtils;
import com.sabre.schemas.node.PropertyNodeType;
import com.sabre.schemas.properties.Images;

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
     * @see com.sabre.schemas.node.PropertyNode#createProperty(com.sabre.schemas.node.Node)
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
     * @see com.sabre.schemas.node.INode#getLabel()
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
    // * @see com.sabre.schemas.node.PropertyNode#isIndicatorProperty()
    // */
    // @Override
    // public boolean isIndicatorAttribute() {
    // return true;
    // }

    /*
     * (non-Javadoc)
     * 
     * @see com.sabre.schemas.node.PropertyNode#setName(java.lang.String)
     */
    @Override
    public void setName(String name) {
        modelObject.setName(NodeNameUtils.fixIndicatorName(name));
    }

}
