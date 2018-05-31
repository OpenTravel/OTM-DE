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
import java.util.List;

import org.eclipse.jface.viewers.LabelProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

/**
 * May 30, 2018 - examples removed from OTM-DE
 * 
 * @author Pawel Jedruch
 * 
 */
@Deprecated
public class DOMExampleModel extends ExampleModel {
	private final class DOMExampleLabelProvider extends LabelProvider {
		// @Override
		// public Image getImage(Object element) {
		// DOMExampleModel e = (DOMExampleModel) element;
		// if (e.isXSDComplexType()) {
		// return Images.getImageRegistry().get(Images.XSDComplexType);
		// } else if (e.isService()) {
		// return Images.getImageRegistry().get(Images.Service);
		// } else if (e.isFacet()) {
		// return Images.getImageRegistry().get(Images.Facet);
		// } else if (e.isAttribute()) {
		// return Images.getImageRegistry().get(Images.XSDAttribute);
		// }
		// return Images.getImageRegistry().get(Images.XSDElement);
		// }
		//
		// @Override
		// public String getText(Object element) {
		// DOMExampleModel e = (DOMExampleModel) element;
		// return e.getDisplayText();
		// }
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(DOMExampleModel.class);

	private Node domNode;
	private org.opentravel.schemas.node.Node owningNode = null;

	/**
	 * Constructor to use when for nested object. Owning Node set to actual nested node.
	 */
	public DOMExampleModel(org.opentravel.schemas.node.Node node, org.opentravel.schemas.node.Node owningNode,
			Node child) {
		super(node);
		this.domNode = child;
		this.owningNode = owningNode;
		// this.owningNode = findOwningNode();
		// this.setLabelProvider(new DOMExampleLabelProvider());
	}

	/**
	 * @param lib
	 * @param examples
	 */
	public DOMExampleModel(org.opentravel.schemas.node.Node lib, Node domNode) {
		super(lib);
		this.domNode = domNode;
		// this.owningNode = findOwningNode();
		// this.setLabelProvider(new DOMExampleLabelProvider());
	}

	// /**
	// * @return
	// */
	// private boolean isAttribute() {
	// return domNode instanceof Attr;
	// }
	//
	// private boolean isFacet() {
	// // TODO Auto-generated method stub
	// return false;
	// }
	//
	// private boolean isService() {
	// return getNode() instanceof ServiceNode;
	// }
	//
	// private boolean isXSDComplexType() {
	// return domNode.getFirstChild() instanceof Element;
	//
	// }

	public String getDisplayText() {
		// if (isService()) {
		// return "Service: " + getNode().getName();
		// }
		// StringBuilder sb = new StringBuilder(domNode.getNodeName());
		// if (isXSDComplexType() || domNode.getTextContent().isEmpty()) {
		// return sb.toString();
		// }
		// return sb.append(" = ").append(domNode.getTextContent()).toString();
		return "";
	}

	@Override
	public List<ExampleModel> getChildren() {
		List<ExampleModel> ret = new ArrayList<>();
		// ret.addAll(createChildren(domNode.getChildNodes()));
		// if (domNode.hasAttributes())
		// ret.addAll(createChildrenForAttributes(domNode.getAttributes()));
		return ret;
	}

	// private Collection<? extends ExampleModel> createChildrenForAttributes(NamedNodeMap map) {
	// final List<ExampleModel> ret = new ArrayList<>(map.getLength());
	// if (map != null) {
	// for (int i = 0; i < map.getLength(); i++) {
	// Node child = map.item(i);
	// DOMExampleModel childModel = new DOMExampleModel(getNode(), getOwningNode(), child);
	// childModel.setXmlString(getXmlString());
	// childModel.setJsonString(getJsonString());
	// ret.add(childModel);
	// //
	// // String nodeName = getNodeName();
	// // LOGGER.debug("Deals with Complex Properties: nodeName = " + nodeName);
	// }
	// }
	// return ret;
	// }

	// private Collection<? extends ExampleModel> createChildren(NodeList childNodes) {
	// List<ExampleModel> ret = new ArrayList<>(childNodes.getLength());
	// for (int i = 0; i < childNodes.getLength(); i++) {
	// Node child = childNodes.item(i);
	// if (child.getNodeType() != Node.TEXT_NODE) {
	// DOMExampleModel childModel = new DOMExampleModel(getNode(), getOwningNode(), child);
	// childModel.setXmlString(getXmlString());
	// childModel.setJsonString(getJsonString());
	// ret.add(childModel);
	// //
	// // String nodeName = getNodeName();
	// // LOGGER.debug("Deals with ??: nodeName = " + nodeName);
	// }
	// }
	// return ret;
	// }

	// private org.opentravel.schemas.node.Node findOwningNode() {
	// String nodeName = getNodeName();
	// org.opentravel.schemas.node.Node result = null;
	//
	// if (nodeName.equals(getNode().getName()))
	// return result; // this dom node is for this node
	// if (getNode().getName().startsWith("xmlns:"))
	// return result; // skip ns attrs
	//
	// org.opentravel.schemas.node.Node root = getNode();
	// // If the owner has been found for siblings then try its type or owner
	// if (owningNode != null)
	// if (owningNode instanceof TypeUser)
	// root = (org.opentravel.schemas.node.Node) ((TypeUser) owningNode).getAssignedType();
	// else
	// root = (org.opentravel.schemas.node.Node) owningNode.getOwningComponent();
	//
	// if (root != null)
	// for (org.opentravel.schemas.node.Node n : root.getDescendants())
	// if (n.getName().equals(nodeName)) {
	// result = n;
	// break;
	// } else if (n instanceof FacetOMNode) {
	// for (org.opentravel.schemas.node.Node in : n.getInheritedChildren())
	// if (in.getName().equals(nodeName)) {
	// result = in;
	// break;
	// }
	// }
	//
	// // TODO - extended object names are not found (e.g. SampleChoiceShared)
	// // if (result == null)
	// // LOGGER.debug("Owner not found for " + nodeName);
	// // else
	// // LOGGER.debug("Found owner for " + nodeName);
	// return result;
	// }

	// public org.opentravel.schemas.node.Node getOwningNode() {
	// return owningNode;
	// }

	// public String getNodeName() {
	// String nodeName = domNode.getNodeName(); // might have prefix
	// // String txt = getDisplayText();
	// // String nodeName = txt.split("=")[0]; // remove value
	// String[] nameArray = nodeName.split(":");
	// nodeName = nameArray[nameArray.length - 1]; // without prefix
	// return nodeName;
	//
	// }
}
