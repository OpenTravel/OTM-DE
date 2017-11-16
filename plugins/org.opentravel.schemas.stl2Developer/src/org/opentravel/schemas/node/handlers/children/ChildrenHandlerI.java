package org.opentravel.schemas.node.handlers.children;

import java.util.List;

import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.types.TypeProvider;
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
	 * Remove the children. If caching handler they will be reread on next get.
	 */
	public void clear();

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
	 * Get children that are type providers. If the provider is versioned, the actual provider is returned.
	 * 
	 * @return new list of children that are type providers
	 */
	public List<TypeProvider> getChildren_TypeProviders();

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

	public String toString();

}