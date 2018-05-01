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
import org.opentravel.schemas.commands.AddNodeHandler2;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.Node.NodeVisitor;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.properties.IValueWithContextHandler;
import org.opentravel.schemas.types.TypeProviderAndOwners;

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
	 * Type of property or child that can be added to this node. Used in {@link AddNodeHandler2}
	 * 
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
	public List<TypeProviderAndOwners> getChildren_TypeProviders();

	/**
	 * Gets the descendants that are named types. Does not return navigation nodes. Does not return named properties
	 * (aliases, facets).
	 * 
	 * @return all descendants that are named types.
	 */
	public List<Node> getDescendants_LibraryMembersAsNodes();

	/**
	 * @return equivalent handler if supported, null otherwise
	 */
	public IValueWithContextHandler getEquivalentHandler();

	/**
	 * @return example handler if supported, null otherwise
	 */
	public IValueWithContextHandler getExampleHandler();

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
	 * @return new list of all immediate children that are projects.
	 */
	public List<ProjectNode> getProjects();

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

	/**
	 * @return the namespace prefix from the TL Library
	 */
	public String getPrefix();

	/**
	 * @return the namespace from the TL Library
	 */
	public String getNamespace();

	public String getNameWithPrefix();

	/*****************************************************************************
	 * Public getters
	 */
	/**
	 * @return the component (named object) owner of this node or else this node.
	 */
	public LibraryMemberInterface getOwningComponent();

	public INode getParent();

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
	public String getAssignedTypeName();

	/**
	 * TypeNameWithPrefix is the name of the type assigned to this object or property with namespace prefix. Uses
	 * {@link INode#getAssignedTypeName()}. If the prefix is the same as the prefix of the node, it is not added.
	 * 
	 * @return name of the assigned type with prefix if the prefix is different that this node.
	 */
	public String getTypeNameWithPrefix();

	public List<LibraryNode> getUserLibraries();

	// see also
	// public static List<LibraryNode> getAllLibraries();
	// public static List<LibraryNode> getAllUserLibraries();

	/**
	 * **** IS A Methods ***********************************************
	 * 
	 */
	/**
	 * @return true if node has been closed or deleted
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
	 * @return true if this node can be renamed. Overriding nodes must account for all factors, not just class
	 *         membership. (is editable, is inherited, etc).
	 */
	boolean isRenameable();

	/**
	 * Is this node a navigation node or part of the OTM model? Includes all library containers and libraries.
	 * 
	 * @see isLibraryContainer()
	 * 
	 * @return
	 */
	public boolean isNavigation();

	/**
	 * @return true if NamedEntity and not implied
	 */
	public boolean isNamedEntity();

	/**
	 * @return true if this type of node will be assigned by reference not name. Implies the property name must be the
	 *         same as the assigned type node's name. For type structure, see {@link FacetOwner}
	 */
	public boolean isAssignedByReference();

	// public boolean isAliasable();

	// /**
	// * Remove this node from its library.
	// */
	// public void removeFromLibrary();

	public void setName(String string);

	/**
	 * Visit this node and all of its descendants (recursive).
	 */
	public void visitAllNodes(NodeVisitor visitor);

	/**
	 * Visit this node and all of its children.
	 */
	public void visitChildren(NodeVisitor visitor);

	/**
	 * Visit this node and all of its descendants that are type users (recursive).
	 */
	void visitAllTypeUsers(NodeVisitor visitor);

	public LibraryNode getLibrary();

	// public TLModelElement getTLModelObject();

}