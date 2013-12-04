/*
 * Copyright (c) 2013, Sabre Inc.
 */
package com.sabre.schemas.views.example;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sabre.schemas.node.ServiceNode;
import com.sabre.schemas.properties.Images;

/**
 * @author Pawel Jedruch
 * 
 */
public class DOMExampleModel extends ExampleModel {

    private Node domNode;

    /**
     * @param lib
     * @param examples
     */
    public DOMExampleModel(com.sabre.schemas.node.Node lib, Node domNode) {
        super(lib);
        this.domNode = domNode;
        this.setLabelProvider(new LabelProvider() {

            @Override
            public Image getImage(Object element) {
                DOMExampleModel e = (DOMExampleModel) element;
                if (e.isXSDComplexType()) {
                    return Images.getImageRegistry().get(Images.XSDComplexType);
                } else if (e.isService()) {
                    return Images.getImageRegistry().get(Images.Service);
                } else if (e.isFacet()) {
                    return Images.getImageRegistry().get(Images.Facet);
                } else if (e.isAttribute()) {
                    return Images.getImageRegistry().get(Images.XSDAttribute);
                }
                return Images.getImageRegistry().get(Images.XSDElement);
            }

            @Override
            public String getText(Object element) {
                DOMExampleModel e = (DOMExampleModel) element;
                return e.getDisplayText();
            }

        });
    }

    /**
     * @return
     */
    private boolean isAttribute() {
        return domNode instanceof Attr;
    }

    private boolean isFacet() {
        // TODO Auto-generated method stub
        return false;
    }

    private boolean isService() {
        return getNode() instanceof ServiceNode;
    }

    private boolean isXSDComplexType() {
        return domNode.getFirstChild() instanceof Element;

    }

    public String getDisplayText() {
        if (isService()) {
            return "Service: " + getNode().getName();
        }
        StringBuilder sb = new StringBuilder(domNode.getNodeName());
        if (isXSDComplexType() || domNode.getTextContent().isEmpty()) {
            return sb.toString();
        }
        return sb.append(" = ").append(domNode.getTextContent()).toString();
    }

    @Override
    public List<ExampleModel> getChildren() {
        List<ExampleModel> ret = new ArrayList<ExampleModel>();
        ret.addAll(createChildren(domNode.getChildNodes()));
        if (domNode.hasAttributes())
            ret.addAll(createChildrenForAttributes(domNode.getAttributes()));
        return ret;
    }

    private Collection<? extends ExampleModel> createChildrenForAttributes(NamedNodeMap map) {
        final List<ExampleModel> ret = new ArrayList<ExampleModel>(map.getLength());
        if (map != null) {
            for (int i = 0; i < map.getLength(); i++) {
                Node child = map.item(i);
                DOMExampleModel childModel = new DOMExampleModel(getNode(), child);
                childModel.setXmlString(getXmlString());
                ret.add(childModel);
            }
        }
        return ret;
    }

    private Collection<? extends ExampleModel> createChildren(NodeList childNodes) {
        List<ExampleModel> ret = new ArrayList<ExampleModel>(childNodes.getLength());
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node child = childNodes.item(i);
            if (child.getNodeType() != Node.TEXT_NODE) {
                DOMExampleModel childModel = new DOMExampleModel(getNode(), child);
                childModel.setXmlString(getXmlString());
                ret.add(childModel);
            }
        }
        return ret;
    }

}
