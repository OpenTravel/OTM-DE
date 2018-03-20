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
package org.opentravel.schemas.node.handlers.children;

import java.util.List;

import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.types.TypeProviderAndOwners;
import org.opentravel.schemas.types.TypeUser;

/**
 * Interface defining methods for all children handlers.
 * 
 * <p>
 * Designing a children handler.
 * <ol>
 * <li>Decide between caching and static. Caching must include methods to initialize children. Caching always follows
 * the TL model. Any change to a child that impacts the parent causes the parent to refresh the cache. Static requires
 * the owner to keep in sync with the TL model. If the list is to be initialized, the owner must do it explicitly.
 * <p>
 * Use caching if the presentation order of children is controlled by the TL Object. Use caching if there are inherited
 * children.
 * <ol>
 * Caching -
 * <li>if all children are not from getChildren_TL() then override initChildren() to add facades.
 * <li>Create constructor and assure owner's tl model is as expected.
 * <li>Create getChildren_TL() as best possible.
 * <li>If all tlObjects are children override Create modelTL() if needed
 * </ol>
 * <ol>
 * Static
 * <li>Define initChildren() if needed
 * <li>Implement add() and remove()
 * </ol>
 * 
 * @author Dave
 *
 * @param <N>
 *            is the type of node to be managed as a child.
 */
public interface ChildrenHandlerI<C extends Node> {

	/**
	 * Clear the children. If caching handler they will be reread on next get. Ignored for static children handlers.
	 * <p>
	 * Generally, use {@link ChildrenHandlerI#clear(Node)} for either type of handler.
	 */
	public void clear();

	/**
	 * Clear the child item.
	 * <p>
	 * If caching handler all children are cleared and they will be reread on next get. If static, the item is removed.
	 */
	public void clear(Node item);

	/*
	 * Remove the item. If caching, the entire cache is cleared.
	 */
	public void remove(C item);

	/**
	 * Return true if the current children contains item. Does <b>Not</b> refresh the list or examine inherited
	 * children.
	 */
	public boolean contains(C item);

	/**
	 * List all children in default display order. Returns empty list if no children.
	 * 
	 * @return
	 */
	public List<C> get();

	/**
	 * @return a new ArrayList containing the children
	 */
	public List<C> getChildren_New();

	/**
	 * 
	 * @return new collection of TLModelElement children
	 */
	public List<TLModelElement> getChildren_TL();

	/**
	 * Get children that are type providers or can own type providers. If the provider is versioned, the actual provider
	 * is returned.
	 * 
	 * @see {@link Node#getDescendants_TypeProviders()} for list of only type providers
	 * @return new list of children that are type providers
	 */
	public List<TypeProviderAndOwners> getChildren_TypeProviders();

	/**
	 * Gets the children that are type users (can be assigned a type). Does not return navigation nodes.
	 * {@link #getDescendants_TypeUsers() Use getDescendants_TypeUsers() for all children.}
	 * 
	 * @return new list with all immediate children that can be assigned a type.
	 */
	public List<TypeUser> getChildren_TypeUsers();

	/**
	 * List all inherited children in default display order. Returns empty list if no children.
	 * 
	 * @return
	 */
	public List<C> getInheritedChildren();

	// Override if inheritance is supported
	public List<TLModelElement> getInheritedChildren_TL();

	/**
	 * Get a new list of child nodes that are to be displayed in navigator trees.
	 * 
	 * @param deep
	 *            when true some nodes will return more children such as properties
	 * 
	 * @see {@link #isNavChild()}
	 * @see {@link org.opentravel.schemas.node.Node_NavChildren_Tests#getNavChildrenTests()}
	 * 
	 * @return new list of children to be used for navigation purposes.
	 */
	public List<C> getNavChildren(boolean deep);

	/**
	 * Get all immediate navChildren that are to be presented in the OTM Object Tree. Includes where used nodes.
	 * Overridden on nodes that add nodes such as where used to the tree view.
	 * 
	 * @see {@link #getNavChildren()}
	 * 
	 * @param deep
	 *            - include properties
	 * 
	 * @return new list
	 */
	public List<C> getTreeChildren(boolean deep);

	public boolean hasChildren();

	public boolean hasChildren_TypeProviders();

	public boolean hasInheritedChildren();

	/**
	 * Fast (no array creation) method to determine if there are navChildren that should be displayed in navigator
	 * trees.
	 * 
	 * @param deep
	 *            enable the "deep" property mode
	 */
	public boolean hasNavChildren(boolean deep);

	// Override on classes that add to getNavChildren()
	public boolean hasTreeChildren(boolean deep);

	@Override
	public String toString();

	// /**
	// * Traverse via getChildren. For version chains, it returns the newest version using the version node and does not
	// * touch aggregates. Deleted members are not returned.
	// *
	// * return new list of NamedEntities.
	// */
	// List<LibraryMemberInterface> getDescendants_LibraryMembers();

}