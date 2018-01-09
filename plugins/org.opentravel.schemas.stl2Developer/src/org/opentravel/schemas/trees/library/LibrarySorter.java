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
package org.opentravel.schemas.trees.library;

import java.util.Comparator;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemas.node.NavNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.node.VersionNode;
import org.opentravel.schemas.node.facets.AttributeFacetNode;
import org.opentravel.schemas.node.interfaces.Enumeration;
import org.opentravel.schemas.node.interfaces.FacetInterface;
import org.opentravel.schemas.node.interfaces.ResourceMemberInterface;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.objectMembers.ContributedFacetNode;
import org.opentravel.schemas.node.objectMembers.ExtensionPointNode;
import org.opentravel.schemas.node.objectMembers.OperationNode;
import org.opentravel.schemas.node.objectMembers.VWA_SimpleFacetFacadeNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.resources.ActionFacet;
import org.opentravel.schemas.node.resources.ActionNode;
import org.opentravel.schemas.node.resources.ActionRequest;
import org.opentravel.schemas.node.resources.ActionResponse;
import org.opentravel.schemas.node.resources.ParamGroup;
import org.opentravel.schemas.node.resources.ResourceParameter;
import org.opentravel.schemas.node.typeProviders.AliasNode;
import org.opentravel.schemas.node.typeProviders.CoreSimpleFacetNode;
import org.opentravel.schemas.node.typeProviders.ListFacetNode;
import org.opentravel.schemas.node.typeProviders.RoleFacetNode;
import org.opentravel.schemas.types.whereused.LibraryUsesNode;
import org.opentravel.schemas.types.whereused.LibraryWhereUsedNode;
import org.opentravel.schemas.types.whereused.WhereUsedNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LibrarySorter extends ViewerSorter {
	private static final Logger LOGGER = LoggerFactory.getLogger(LibrarySorter.class);

	private static final LibrarySorter instance = new LibrarySorter();

	/**
	 * @return {@link Comparator} adapter for this ViewerSorter applicable for {@link Node}s.
	 */
	public static Comparator<Node> createComparator() {
		return new Comparator<Node>() {

			@Override
			public int compare(Node o1, Node o2) {
				return instance.compare(null, o1, o2);
			}
		};
	}

	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		int cat1 = category(e1);
		int cat2 = category(e2);

		if (cat1 != cat2) {
			return super.compare(viewer, e1, e2);
		}

		int val = 0, index1 = 0, index2 = 0;
		final Node n1 = (Node) e1;
		final Node n2 = (Node) e2;

		if ((n1 instanceof PropertyNode) && (n2 instanceof PropertyNode)) {
			if (n1.getParent() != null)
				index1 = n1.getParent().getChildren().indexOf(n1);
			if (n2.getParent() != null)
				index2 = n2.getParent().getChildren().indexOf(n2);
			if (index1 > index2)
				val = 1;
			else if (index1 < index2)
				val = -1;
		} else {
			return super.compare(viewer, e1, e2);
		}
		return val;
	}

	@Override
	public int category(final Object element) {
		Node n = (Node) element;
		if (n instanceof VersionNode)
			n = ((VersionNode) n).get();

		if (n == null)
			return 0;

		if (n instanceof LibraryUsesNode)
			return 701;
		if (n instanceof LibraryWhereUsedNode)
			return 700;
		if (n instanceof WhereUsedNode<?>)
			return 699;

		if (n instanceof ProjectNode) {
			if (n.isEditable())
				return 4;
			else
				return 10;
		}

		if (n instanceof NavNode) {
			final String name = n.getName();
			if (LibraryNode.ELEMENTS.equals(name)) {
				return 3;
			} else if (LibraryNode.SIMPLE_OBJECTS.equals(name)) {
				return 2;
			} else if (LibraryNode.COMPLEX_OBJECTS.equals(name)) {
				return 1;
			} else if (LibraryNode.SERVICES.equals(name)) {
				return 4;
			} else if (LibraryNode.RESOURCES.equals(name)) {
				return 5;
			}
		}

		if (n instanceof PropertyNode) {
			int inheritAdjust = n.isInherited() ? 0 : 10;
			// All inherited first, then actual
			switch (((PropertyNode) n).getPropertyType()) {
			case INDICATOR:
				return inheritAdjust + 100;
			case INDICATOR_ELEMENT:
				return inheritAdjust + 100;
			case ATTRIBUTE:
				return inheritAdjust + 200;
			case ID_REFERENCE:
				return inheritAdjust + 300;
			case ELEMENT:
				return inheritAdjust + 300;
			case ALIAS:
				break;
			case ENUM_LITERAL:
				break;
			case ID:
				break;
			case ROLE:
				break;
			case SIMPLE:
				break;
			case UNKNOWN:
				break;
			default:
				break;
			}
		}
		if (n instanceof ContributedFacetNode)
			return 600; // after the properties

		if (n instanceof AliasNode)
			return 0;
		if (n instanceof ExtensionPointNode)
			return 1;

		if (n instanceof ResourceMemberInterface) {
			if (n instanceof ActionNode)
				return 11;
			if (n instanceof ParamGroup)
				return 12;
			if (n instanceof ActionFacet)
				return 13;
			if (n instanceof ActionRequest)
				return 14;
			if (n instanceof ActionResponse)
				return 15;
			if (n instanceof ResourceParameter)
				return 16;
			return 18;
		}

		if (n instanceof FacetInterface) {
			// if (n instanceof SimpleFacetNode)
			if (n instanceof VWA_SimpleFacetFacadeNode)
				return 1;
			if (n instanceof CoreSimpleFacetNode)
				return 1;
			if (n instanceof AttributeFacetNode)
				return 2;
			if (n instanceof RoleFacetNode)
				return 61;
			if (n instanceof ListFacetNode) {
				if (((ListFacetNode) n).isSimpleListFacet())
					return 65;
				return 66;
			}
			if (n instanceof OperationNode)
				return 67;
			if (n instanceof Enumeration)
				return 38;

			if (n.getTLModelObject() instanceof TLFacet) {
				switch (((TLFacet) n.getTLModelObject()).getFacetType()) {
				case ID:
					return 10;
				case SIMPLE:
					return 20;
				case SUMMARY:
					return 30;
				case SHARED:
					return 31;
				case DETAIL:
					return 40;
				case CUSTOM:
					return 50;
				case CHOICE:
					return 50;
				case QUERY:
					return 50;
				case UPDATE:
					return 50;
				case REQUEST:
					return 70;
				case RESPONSE:
					return 80;
				case NOTIFICATION:
					return 90;
				default:
					break;
				}
			} else
				LOGGER.error("Don't know what " + n + " is!");
			return 100;
		}

		if (n.isBuiltIn())
			return 3;
		if (n.isXSDSchema())
			return 2;
		if (n instanceof LibraryNode)
			return 1;

		return 10;
	}
}
