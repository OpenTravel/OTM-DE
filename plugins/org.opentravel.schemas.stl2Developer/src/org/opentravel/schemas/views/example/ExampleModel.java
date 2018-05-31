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
import org.opentravel.schemas.node.Node;

/**
 * May 30, 2018 - examples removed from OTM-DE
 * 
 * @author Pawel Jedruch
 * 
 */
@Deprecated
public class ExampleModel {

	private Node node;
	private LabelProvider labelProvider;
	private List<ExampleModel> children = new ArrayList<>();
	private ExampleModel parent;
	private String xmlString = "";
	private String jsonString = "";

	public ExampleModel(Node lib) {
		// this.node = lib;
		// this.setLabelProvider(new LabelProvider() {
		//
		// private final LibraryDecorator decorator = new LibraryDecorator();
		//
		// @Override
		// public Image getImage(Object element) {
		// if (node instanceof ServiceNode) {
		// return Images.getImageRegistry().get(Images.Service);
		// } else if (node instanceof OperationNode) {
		// return Images.getImageRegistry().get(Images.Facet);
		// } else if (node instanceof LibraryNode) {
		// return Images.getImageRegistry().get(Images.builtInLib);
		// } else if (node instanceof LibraryChainNode) {
		// return Images.getImageRegistry().get(Images.libraryChain);
		// }
		// return null;
		// }
		//
		// @Override
		// public String getText(Object element) {
		// String prefix = getPrefix(node);
		// String version = "";
		// if (node instanceof LibraryNode) {
		// String v = decorator.getLibraryVersion((LibraryNode) node);
		// if (!v.isEmpty())
		// version = " [" + v + "]";
		// }
		// return prefix + node.getName() + version;
		// }
		//
		// });
	}

	/**
	 * @param node2
	 * @return
	 */
	protected String getPrefix(Node node) {
		// if (node instanceof ServiceNode) {
		// return "Service: ";
		// } else if (node instanceof OperationNode) {
		// return "Operation: ";
		// } else {
		return "";
		// }
	}

	public void setLabelProvider(LabelProvider labelProvider) {
		// this.labelProvider = labelProvider;
	}

	public List<ExampleModel> getChildren() {
		return children;
	}

	public ExampleModel getParent() {
		return parent;
	}

	public Node getNode() {
		return node;
	}

	public LabelProvider getLabelProvider() {
		return labelProvider;
	}

	public void setJsonString(String string) {
		this.jsonString = string;
	}

	public void setXmlString(String string) {
		this.xmlString = string;
	}

	public String getJsonString() {
		return jsonString;
	}

	public String getXmlString() {
		return xmlString;
	}

	public void addChildren(List<ExampleModel> children) {
		// this.children.addAll(children);
		// Collections.sort(this.children, new Comparator<ExampleModel>() {
		//
		// @Override
		// public int compare(ExampleModel o1, ExampleModel o2) {
		// String str1 = o1.getLabelProvider().getText(o1);
		// String str2 = o2.getLabelProvider().getText(o2);
		// return String.CASE_INSENSITIVE_ORDER.compare(str1, str2);
		// }
		//
		// });
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((node == null) ? 0 : node.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (obj instanceof Node) {
			return compareNode(node, (Node) obj);
		} else if (getClass() != obj.getClass())
			return false;
		ExampleModel other = (ExampleModel) obj;
		if (node == null) {
			if (other.node != null)
				return false;
		} else if (!compareNode(node, other.node))
			return false;
		return true;
	}

	private boolean compareNode(Node thiss, Node that) {
		// boolean ret = thiss.getIdentity().equals(that.getIdentity());
		boolean ret = thiss.getNameWithPrefix().equals(that.getNameWithPrefix());
		return ret = ret || thiss == that;
	}
}
