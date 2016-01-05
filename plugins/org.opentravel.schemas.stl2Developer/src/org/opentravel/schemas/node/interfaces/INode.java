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
package org.opentravel.schemas.node.interfaces;

import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemas.modelObject.ModelObject;
import org.opentravel.schemas.node.LibraryNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.Node.NodeVisitor;
import org.opentravel.schemas.node.ProjectNode;

/**
 * Interface to nodes in the model.
 * 
 * @author Dave Hollander
 *
 */
/**
 * TODO Force all non-node package members to use INode Make sure all INode methods are covered by JUnit tests. Make all
 * other methods protected/private Clean up/refactor the methods Clean up semantics of the various methods to be
 * consistent with: Add() - add contents to the TL model. Contents that are not INodes are also modeled. Load() - read
 * contents from tl model and create GUI model to represent them Link() - create parent/child links
 * 
 */

public interface INode {

	// Enumeration of the types of command actions nodes can handle.
	public enum CommandType {
		PROPERTY, ROLE, LIBRARY, ATTRIBUTE, ENUMERATION, QUERY, CUSTOM, OPERATION, NONE, COMPONENT
	}

	/**
	 * @return enumeration value of what add commands the node supports.
	 */
	public CommandType getAddCommand();

	/**
	 * Close this node to remove it from the GUI node model. Same as delete(false) except it overrides the editable
	 * settings. Sets library to editable. Closes all descendants. Removes all nodes from their parent's child list.
	 * Removes all nodes from type and base-type user lists. Does NOT change the underlying TL Model.
	 */
	public void close();

	/**
	 * Delete this node and all of its underlying children. Does not delete nodes that are not delete-able such as
	 * navigation nodes. All children of navigation nodes are deleted. Removes all nodes from their parent's child list.
	 * Removes all nodes from type and base-type user lists. Removes TL Entity from TLModel.
	 * 
	 * @return false if this node was not deleted.
	 */
	public void delete();

	/**
	 * Returns the live children list.
	 * 
	 * @return
	 */
	public List<Node> getChildren();

	/**
	 * Get a new list of the children which are either type providers or can contain type providers.
	 * 
	 * @return
	 */
	public List<Node> getChildren_TypeProviders();

	/**
	 * Gets the descendants that are named types. Does not return navigation nodes. Does not return named properties
	 * (aliases, facets).
	 * 
	 * @return all descendants that are named types.
	 */
	public List<Node> getDescendants_NamedTypes();

	public boolean hasChildren();

	/**
	 * @return true if any direct child is a type provider or may contain type providers. Note that XSD types do not
	 *         have type provider children because only the named type may be used for assignments.
	 */
	public boolean hasChildren_TypeProviders();

	/**
	 * @return a string used in the GUI to describe the type of component
	 */
	public String getComponentType();

	/**
	 * @return the image used to represent this node in the GUI.
	 */
	public Image getImage();

	/**
	 * @return new list with all descendants that are libraries.
	 */
	public List<LibraryNode> getLibraries();

	/**
	 * @return new list of all immediate children that are projects.
	 */
	public List<ProjectNode> getProjects();

	public ModelObject<?> getModelObject();

	/*****************************************************************************
	 * Names
	 * 
	 * <b>Label</b> is the node's name plus any optional additional generated text that clarifies the role/purpose/type
	 * of the node to the user. Labels can <i>not</i> be set. Label to be used in tree views.
	 * 
	 * @return
	 */
	public String getLabel();

	public String getName();

	public String getNamePrefix();

	public String getNamespace();

	public String getNameWithPrefix();

	/*****************************************************************************
	 * Public getters
	 */
	public INode getOwningComponent();

	public INode getParent();

	/**
	 * Is in library chain tells if this node is presented to the user both under its library and in a chain aggregate
	 * node.
	 * 
	 * @return aggregate parent if exists, null otherwise
	 */
	public INode getParentAggregate();

	/**
	 * Same as {@link getAssignedType()}
	 * 
	 * @return
	 */
	public Node getType();

	/**
	 * TypeName is the name of the type assigned to this object or property. Reads name from type node. If type node is
	 * null, it tries to assign it. If the type is not found, a unassigned implied node is assigned.
	 * 
	 * @return name of the assigned type from the type node.
	 */
	public String getTypeName();

	/**
	 * TypeNameWithPrefix is the name of the type assigned to this object or property with namespace prefix. Uses
	 * {@link INode#getTypeName()}. If the prefix is the same as the prefix of the node, it is not added.
	 * 
	 * @return name of the assigned type with prefix if the prefix is different that this node.
	 */
	public String getTypeNameWithPrefix();

	/**
	 * 
	 * @return live list of editable nodes where this node is assigned as a type. New empty list if none.
	 */
	public List<Node> getTypeUsers();

	public List<LibraryNode> getUserLibraries();

	// see also
	// public static List<LibraryNode> getAllLibraries();
	// public static List<LibraryNode> getAllUserLibraries();

	/**
	 * **** IS A Methods ***********************************************
	 * 
	 */
	public boolean isDeleted();

	/**
	 * @return true if the node has been deprecated. A component is deprecated if there are any non-empty deprecation
	 *         document items.
	 */
	public boolean isDeprecated();

	/**
	 * Implied nodes and nodes without libraries are always editable.
	 * 
	 * @return true if the node's library is editable.
	 */
	public boolean isEditable();

	/**
	 * Can this type of node contain libraries?
	 * 
	 * @see isNavigation()
	 * 
	 * @return
	 */
	public boolean isLibraryContainer();

	/**
	 * Is this node a navigation node or part of the OTM model? Includes all library containers and libraries.
	 * 
	 * @see isLibraryContainer()
	 * 
	 * @return
	 */
	public boolean isNavigation();

	public boolean isVWA_AttributeFacet();

	/**
	 * @return true if NamedEntity and not operation or service
	 */
	public boolean isTypeProvider();

	/**
	 * @return true if this node can be assigned a type.
	 */
	public boolean isTypeUser();

	/**
	 * @return true if this type of node will be assigned by reference not name. Implies the property name must be the
	 *         same as the assigned type node's name. For type structure, see {@link ComplexComponentInterface}
	 */
	public boolean isAssignedByReference();

	/**
	 * Remove this node from its library.
	 */
	public void removeFromLibrary();

	/**
	 * Sets the type assigned to this node if appropriate. Sets TL type, type node and type users on the target node.
	 * 
	 * Restrictions enforced: 1) Simple Facets and Attribute Properties must have simple type. 2) VWA Attribute facets
	 * may have simple type or VWA or Open Enum. ) SimpleProperties may not be circularly assigned to their owning
	 * components. 4) node must be editable or in XSD library to set the TL type.
	 * 
	 * If typeNode is implied, the TL type is cleared.
	 * 
	 * @param typeNode
	 *            to assign, or null to clear assignments.
	 * @return true if set
	 */
	public boolean setAssignedType(Node typeNode);

	// /**
	// * Sets the type assigned to this node if appropriate. Sets TL type, type node and type users on the target node.
	// *
	// * Restrictions enforced: 1) Simple Facets and Attribute Properties must have simple type. 2) VWA Attribute facets
	// * may have simple type or VWA or Open Enum. ) SimpleProperties may not be circularly assigned to their owning
	// * components. 4) node must be editable or in XSD library to set the TL type.
	// *
	// * If typeNode is implied, the TL type is cleared.
	// *
	// * @param typeNode
	// * to assign, or null to clear assignments.
	// * @param refresh
	// * refresh the navigator tree to update where-used counts
	// * @return true if set
	// */
	// public boolean setAssignedType(Node replacement, boolean refresh);

	public void setName(final String n, final boolean doFamily);

	/**
	 * Visit this node and all of its descendants (recursive).
	 */
	public void visitAllNodes(NodeVisitor visitor);

	/**
	 * Visit this node and all of its children.
	 */
	public void visitChildren(NodeVisitor visitor);

	/**
	 * Visit this node and all of its descendants that are type providers (recursive).
	 */
	public void visitAllTypeProviders(NodeVisitor visitor);

	/**
	 * Visit this node and all of its descendants that are type users (recursive).
	 */
	void visitAllTypeUsers(NodeVisitor visitor);

	public LibraryNode getLibrary();

}