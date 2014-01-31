/**
 * Copyright (C) 2014 OpenTravel Alliance (info@opentravel.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.opentravel.schemas.views.example;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.opentravel.schemas.node.ServiceNode;
import org.opentravel.schemas.properties.Images;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
    public DOMExampleModel(org.opentravel.schemas.node.Node lib, Node domNode) {
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
