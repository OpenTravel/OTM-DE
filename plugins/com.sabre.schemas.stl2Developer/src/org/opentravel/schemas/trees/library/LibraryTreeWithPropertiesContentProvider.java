/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemas.trees.library;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.opentravel.schemas.node.INode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFinders;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sabre.schemacompiler.model.NamedEntity;

/**
 * 5/4/2012 - dmh - NO LONGER USED for Model Navigator. Still used in wizards -
 * but doesn't have to be.
 * 
 * TODO - use standard tree provider for EqExWizard and NewPropertiesWizard.
 * 
 * @author Agnieszka Janowska
 * 
 */
public class LibraryTreeWithPropertiesContentProvider implements
		ITreeContentProvider {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(LibraryTreeContentProvider.class);

	private boolean includeInheritedChildren = false;

	public LibraryTreeWithPropertiesContentProvider(
			boolean includeInheritedChildren) {
		this.includeInheritedChildren = includeInheritedChildren;
	}

	// SAME
	@Override
	public Object[] getElements(final Object element) {
		return getChildren(element);
	}

	// used when a node that has children is selected
	// For the tree provider, return children who are parents, and if they're
	// facets return its
	// properties
	@Override
	public Object[] getChildren(final Object element) {
		if (element instanceof Node) {
			final Node node = (Node) element;

			if (node.isProperty() && (!node.isRoleProperty())) {
				final PropertyNode prop = (PropertyNode) node;
				final NamedEntity elem = prop.getModelObject().getTLType();
				final Node typeNode = NodeFinders
						.findNodeByValidationIentity(elem
								.getValidationIdentity());
				LOGGER.debug("findNode returned " + typeNode.getName());

				// 5/3/2012 - dmh used this instead of newly built nodes...needs
				// through testing.
				List<Node> typeArray = new ArrayList<Node>();
				typeArray.add(typeNode);
				// If it is an alias, list its object as well.
				if (typeNode.isAlias())
					typeArray.add(typeNode.getParent());
				return typeArray.toArray();
			}

			// If not a property.
			final List<Node> nodeChildren = new ArrayList<Node>(
					node.getChildren());

			if (includeInheritedChildren) {
				nodeChildren.addAll(node.getInheritedChildren());
			}
			return nodeChildren.toArray();
		}
		LOGGER.debug("getChildren was not passed a node. Element is " + element);
		return new Object[0];
	}

	@Override
	public boolean hasChildren(final Object element) {
		if (element instanceof Node) {
			final Node node = (Node) element;
			boolean hasChildren = node.hasNavChildrenWithProperties();

			if (!hasChildren && includeInheritedChildren) {
				hasChildren = node.hasInheritedChildren();
			}
			return hasChildren;
		}
		LOGGER.debug("hasChildren was not passed a node. Element is " + element);
		return false;
	}

	@Override
	public Object getParent(final Object element) {
		return ((INode) element).getParent();
	}

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(final Viewer viewer, final Object old_input,
			final Object new_input) {
	}

}
