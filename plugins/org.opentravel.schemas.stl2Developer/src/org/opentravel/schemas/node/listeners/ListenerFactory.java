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
package org.opentravel.schemas.node.listeners;

import java.util.ArrayList;
import java.util.Collection;

import org.opentravel.schemacompiler.event.ModelElementListener;
import org.opentravel.schemas.node.BusinessObjectNode;
import org.opentravel.schemas.node.LibraryNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.SimpleFacetNode;
import org.opentravel.schemas.node.SimpleTypeNode;
import org.opentravel.schemas.node.properties.EnumLiteralNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.resources.ActionRequest;

/**
 * Trivial factory class created to consolidate all the listener assignments for maintenance.
 * 
 * @author Dave
 *
 */
public class ListenerFactory {

	public ListenerFactory() {
	}

	/**
	 * Set listener. Make sure it is the ONLY one.
	 * 
	 * @param node
	 */
	public static void setListner(Node node) {
		clearListners(node);
		if (node.getTLModelObject() == null)
			return;

		if (node instanceof EnumLiteralNode)
			return; // do not assign listener
		// if (node instanceof IndicatorNode)

		if (node instanceof PropertyNode)
			node.getTLModelObject().addListener(new PropertyNodeListener(node));
		else if (node instanceof SimpleFacetNode)
			node.getTLModelObject().addListener(new SimpleFacetNodeListener(node));
		else if (node instanceof SimpleTypeNode)
			node.getTLModelObject().addListener(new PropertyNodeListener(node));
		else if (node instanceof LibraryNode)
			node.getTLModelObject().addListener(new LibraryNodeListener(node));
		else if (node instanceof BusinessObjectNode)
			node.getTLModelObject().addListener(new BusinessObjectNodeListener(node));
		else if (node instanceof ActionRequest)
			node.getTLModelObject().addListener(new ActionRequestListener(node));
		else
			node.getTLModelObject().addListener(new NamedTypeListener(node));
	}

	/**
	 * Remove all listeners from this node's tl object.
	 */
	public static void clearListners(Node node) {
		if (node.getTLModelObject() == null)
			return;
		Collection<ModelElementListener> listeners = new ArrayList<>(node.getTLModelObject().getListeners());
		if (!listeners.isEmpty())
			for (ModelElementListener l : listeners)
				node.getTLModelObject().removeListener(l);
	}

}
