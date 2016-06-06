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

import org.opentravel.schemas.node.AliasNode;
import org.opentravel.schemas.node.LibraryNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.Node.NodeVisitor;
import org.opentravel.schemas.node.SimpleFacetNode;
import org.opentravel.schemas.node.interfaces.ComplexComponentInterface;
import org.opentravel.schemas.node.interfaces.ExtensionOwner;
import org.opentravel.schemas.node.interfaces.INode;
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
		ArrayList<LibraryNode> libs = new ArrayList<LibraryNode>(Node.getAllLibraries());
		resolveTypes(libs);
	}

	public void resolveTypes(LibraryNode lib) {
		ArrayList<LibraryNode> newLibs = new ArrayList<LibraryNode>();
		newLibs.add(lib);
		resolveTypes(newLibs);
	}

	public void resolveTypes(List<LibraryNode> newLibs) {
		boolean wasEditable = false;
		for (LibraryNode lib : newLibs) {
			wasEditable = lib.isEditable(); // Resolve all libraries, not just editable ones
			lib.setEditable(true);
			if (lib.isInChain()) {
				lib.getChain().visitAllTypeUsers(new resolveTypes());
				lib.getChain().visitAllBaseTypeUsers(new resolveBaseTypes());
			} else {
				lib.visitAllTypeUsers(new resolveTypes());
				lib.visitAllBaseTypeUsers(new resolveBaseTypes());
			}
			lib.setEditable(wasEditable);
		}

		// LOGGER.debug("Visitor Resolver visited: " + typeUsers + "  Resolved: " + resolvedTypes + "  UnResolved: "
		// + unResolvedTypes + "  Unassigned: " + ModelNode.getUnassignedNode().getTypeUsersCount());
	}

	private class resolveBaseTypes implements NodeVisitor {
		@Override
		public void visit(INode in) {
			if (in instanceof ExtensionOwner) {
				Node base = ((ExtensionOwner) in).getExtensionBase();
				if (base != null)
					((ExtensionOwner) in).setExtension(base);
			}
		}
	}

	private class resolveTypes implements NodeVisitor {
		@Override
		public void visit(INode in) {
			if (in instanceof TypeUser) {
				TypeProvider provider = ((TypeUser) in).getAssignedType();
				if (provider != null) {
					provider.addWhereUsed((TypeUser) in); // add to list
					provider.setListener((TypeUser) in); // set listener
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
		Node type = n.getTypeNode();
		if (type == null)
			return null;

		if (type instanceof SimpleFacetNode) {
			ComplexComponentInterface owner = (ComplexComponentInterface) type.getOwningComponent();
			if (owner instanceof SimpleAttributeOwner)
				return (Node) ((SimpleAttributeOwner) owner).getSimpleType();
			else
				return (Node) owner;
		} else if (type instanceof AliasNode) {
			AliasNode alias = (AliasNode) type;
			return alias.getOwningComponent();
		}
		return type;
	}

	// ///////////////////// OLD Implelementation ////////////////////////////////////
	//
	// private final Map<QName, Node> providerMap = new HashMap<QName, Node>(Node.getNodeCount());
	//
	// public Map<QName, Node> getProviderMap() {
	// return providerMap;
	// }
	//
	// private int resolvedTypes = 0;
	// private int unResolvedTypes = 0;
	// // private int duplicates = 0;
	//
	// private int typeProviders = 0;
	// private int typeUsers = 0;
	// int visitCount = 0;
	//
	// /**
	// * Create a type resolver which creates a map of all type providers. NOTE - it creates a static, unmaintained map
	// of
	// * the node tree so it should NOT be retained after use.
	// */
	// public TypeResolver() {
	// // LOGGER.debug("Begin map building ************************************");
	// Node.getModelNode().visitAllTypeProviders(new addToMap());
	//
	// typeProviders = providerMap.size();
	// // LOGGER.debug("Resolver map created with " + typeProviders + " providers available.");
	// // if (typeProviders != visitCount)
	// // LOGGER.warn("Not all visited nodes (" + visitCount + ") were added to the type map (" + typeProviders
	// // + "). ");
	// // if (duplicates > 0) {
	// // LOGGER.debug("Duplicates: " + Node.getModelNode().getDuplicateTypes());
	// // }
	// }
	//
	// public class addToMap implements NodeVisitor {
	// @Override
	// public void visit(INode n) {
	// QName qn = new QName(n.getNamespace(), n.getName());
	// if (providerMap.put(qn, (Node) n) != null) {
	// // duplicates++; // must be able to tolerate duplicate objects.
	// // Node.getModelNode().addDuplicateType((Node) n);
	// // LOGGER.debug("Duplicate found: " + qn);
	// }
	// visitCount++;
	// // Verify listeners are in place
	// if (!(n instanceof ImpliedNode) && n != Node.GetNode(((Node) n).getTLModelObject()))
	// throw new IllegalStateException(n + " identity listener is wrong.");
	// // LOGGER.debug("Listener did NOT identify correct node.");
	// }
	// }
	//
	// public void clear() {
	// providerMap.clear();
	// resolvedTypes = 0;
	// }
	//
	// public void delete() {
	// clear();
	// }
	//
	// /**
	// * Resolved types across entire model.
	// */
	// public void resolveTypes() {
	// ArrayList<LibraryNode> libs = new ArrayList<LibraryNode>(Node.getAllLibraries());
	// resolveTypes(libs);
	// }
	//
	// public void resolveTypes(LibraryNode lib) {
	// ArrayList<LibraryNode> newLibs = new ArrayList<LibraryNode>();
	// newLibs.add(lib);
	// resolveTypes(newLibs);
	// }
	//
	// public void resolveTypes(List<LibraryNode> newLibs) {
	// boolean wasEditable = false;
	// for (LibraryNode lib : newLibs) {
	// wasEditable = lib.isEditable(); // Resolve all libraries, not just editable ones
	// lib.setEditable(true);
	// if (lib.isInChain()) {
	// lib.getChain().visitAllTypeUsers(new resolveTypes());
	// lib.getChain().visitAllBaseTypeUsers(new resolveBaseTypes());
	// } else {
	// lib.visitAllTypeUsers(new resolveTypes());
	// lib.visitAllBaseTypeUsers(new resolveBaseTypes());
	// }
	// lib.setEditable(wasEditable);
	// }
	//
	// // LOGGER.debug("Visitor Resolver visited: " + typeUsers + "  Resolved: " + resolvedTypes + "  UnResolved: "
	// // + unResolvedTypes + "  Unassigned: " + ModelNode.getUnassignedNode().getTypeUsersCount());
	// }
	//
	// private class resolveTypes implements NodeVisitor {
	// @Override
	// public void visit(INode in) {
	// Node n = (Node) in;
	// typeUsers++;
	//
	// if (n instanceof ResourceNode) {
	// // Don't use the map, use resource node's method that uses listeners
	// Node target = ((ResourceNode) n).getSubject();
	// n.getTypeClass().set(target, null);
	// // LOGGER.debug("ResolveTypes visitor assigned " + target + " to " + n + " resource.");
	// return;
	// }
	//
	// if (n.getTypeClass().getTypeNode() != null && !(n.getTypeClass().getTypeNode() instanceof ImpliedNode))
	// return;
	//
	// // Do assignment and record success/failure
	// if (isXSD_Atomic(n))
	// n.getTypeClass().setTypeNode(ModelNode.getAtomicTypeNode());
	// else if (!XsdModelingUtils.getAssignedXsdUnion(n).isEmpty())
	// n.getTypeClass().setTypeNode(ModelNode.getUnionNode());
	// else if (n.getTypeClass().setAssignedTypeForThisNode(n, providerMap))
	// resolvedTypes++;
	// else
	// unResolvedTypes++;
	// }
	// }
	//
	// private class resolveBaseTypes implements NodeVisitor {
	// @Override
	// public void visit(INode in) {
	// Node n = (Node) in;
	// INode target = findBaseType(n, providerMap);
	// if (target != null) {
	// n.setExtendsType(target);
	// }
	// }
	// }
	//
	// public static INode findBaseType(Node n, Map<QName, Node> providerMap) {
	// QName typeQname = null;
	// typeQname = new QName(n.getExtendsTypeNS(), n.getExtendsTypeName());
	// INode in = providerMap.get(typeQname);
	// // if ((typeQname != null && !typeQname.getLocalPart().isEmpty()) && in == null)
	// // LOGGER.error("ERROR - missing node for qname: " + typeQname);
	// return providerMap.get(typeQname);
	// }
	//
	// /**
	// * @return true if assigned type does not have JaxB type
	// */
	// private boolean isXSD_Atomic(Node n) {
	// if (!n.isXsdType())
	// return false;
	// XsdNode xn = n.getXsdNode();
	// if (xn != null)
	// if (xn.getTLModelObject() != null)
	// if (xn.getTLModelObject() instanceof XSDSimpleType)
	// if (((XSDSimpleType) xn.getTLModelObject()).getJaxbType() == null)
	// return true;
	// return false;
	// }
	//

}
