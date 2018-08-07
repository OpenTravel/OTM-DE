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
/**
 * 
 */
package org.opentravel.schemas.types;

import java.util.ArrayList;
import java.util.List;

import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.Node.NodeVisitor;
import org.opentravel.schemas.node.interfaces.ExtensionOwner;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.objectMembers.ContributedFacetNode;
import org.opentravel.schemas.node.typeProviders.AliasNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Run across all nodes in the model and resolve types.
 * 
 * NOTE - it creates a static map of the node tree so it should NOT be retained after use. Future development - use
 * modelController to persist this map. Be very careful of all places where adding or deleting nodes.
 * 
 * @author Dave Hollander
 * 
 */
/**
 * Rewrite - 2/21/2016
 * 
 * Some providers many not have identity listener if that library has not been loaded and modeled.
 * 
 * All TypeProviders need to add where used listeners and add type user to their list.
 * 
 * There is no typeUser base type. minimize work needed to be done in type users.
 * 
 * @author Dave
 *
 */
public class TypeResolver {
	private static final Logger LOGGER = LoggerFactory.getLogger(TypeResolver.class);

	public TypeResolver() {
	}

	/**
	 * Resolved types across entire model.
	 */
	public void resolveTypes() {
		ArrayList<LibraryNode> libs = new ArrayList<>(Node.getAllLibraries());
		resolveTypes(libs);
	}

	public void resolveTypes(LibraryNode lib) {
		ArrayList<LibraryNode> newLibs = new ArrayList<>();
		newLibs.add(lib);
		resolveTypes(newLibs);
	}

	public void resolveTypes(List<LibraryNode> newLibs) {
		boolean wasEditable = false;
		for (LibraryNode lib : newLibs) {
			wasEditable = lib.isEditable(); // Resolve all libraries, not just editable ones
			// LOGGER.debug("Resolving Types in " + lib);
			lib.setEditable(true);

			// Load inherited children since they can change the model (add contrib and contextual facets)
			for (ExtensionOwner eo : lib.getDescendants_ExtensionOwners())
				eo.getInheritedChildren();

			// Resolve all un-linked contributed facets FIRST since that impacts children
			for (ContributedFacetNode cf : lib.getDescendants_ContributedFacets())
				cf.setContributor(null); // will resolve using identity listeners

			if (lib.isInChain()) {
				lib.getChain().visitAllTypeUsers(new resolveTypes());
				lib.getChain().visitAllExtensionOwners(new resolveBaseTypes());
			} else {
				lib.visitAllTypeUsers(new resolveTypes());
				lib.visitAllExtensionOwners(new resolveBaseTypes());
			}

			lib.setEditable(wasEditable);
		}

		// LOGGER.debug("Visitor Resolver visited: " + newLibs);
		// LOGGER.debug("Visitor Resolver visited: " + typeUsers + " Resolved: " + resolvedTypes + " UnResolved: "
		// + unResolvedTypes + " Unassigned: " + ModelNode.getUnassignedNode().getTypeUsersCount());
	}

	private class resolveBaseTypes implements NodeVisitor {
		@Override
		public void visit(INode in) {
			if (in instanceof ExtensionOwner) {
				Node base = ((ExtensionOwner) in).getExtensionBase();
				if (base != null && base.getLibrary() != null) {
					((ExtensionOwner) in).setExtension(base);
					base.getLibrary().checkExtension(base);
				}
			}
		}
	}

	private class resolveTypes implements NodeVisitor {
		@Override
		public void visit(INode in) {
			if (in instanceof TypeUser) {
				TypeProvider provider = ((TypeUser) in).getAssignedType();
				if (provider != null) {
					provider.addTypeUser((TypeUser) in); // add to list
					WhereUsedLibraryHandler handler = null;
					if (provider.getLibrary() != null)
						handler = provider.getLibrary().getWhereUsedHandler();
					if (handler != null)
						handler.add((TypeUser) in);
				}
			}
		}
	}

	/**
	 * @param n
	 *            - Node with assigned type
	 * @return type of given node. For most of the nodes this method will return {@link Node#getTypeNode()}. For
	 *         SimpleFacet it will return type of simple attribute. For alias it will return type of parent.
	 */
	// Used in graphical editor
	public static Node getNodeType(Node n) {
		Node type = n.getType(); // FIXME - should use assignedType but it returns unused
		if (type == null)
			return null;

		// if (type instanceof SimpleFacetNode) {
		// assert false; // FIXME
		// ComplexComponentInterface owner = (ComplexComponentInterface) type.getOwningComponent();
		// if (owner instanceof SimpleAttributeOwner)
		// return (Node) ((SimpleAttributeOwner) owner).getAssignedType();
		// else
		// return (Node) owner;
		// } else
		if (type instanceof AliasNode) {
			AliasNode alias = (AliasNode) type;
			return (Node) alias.getOwningComponent();
		}
		return type;
	}

}
