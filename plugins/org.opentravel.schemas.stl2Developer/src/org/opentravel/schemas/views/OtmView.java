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
package org.opentravel.schemas.views;

import java.util.List;

import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.interfaces.INode;

/**
 * Base Interface for all controllers.
 * 
 * @author Dave Hollander
 * 
 */
public interface OtmView {

	/**
	 * Attempt to activate this view.
	 * 
	 * @return true if successful.
	 */
	public boolean activate();

	/**
	 * Clear filter text
	 */
	public void clearFilter();

	/**
	 * Clear the view selection.
	 */
	public void clearSelection();

	/**
	 * Collapse the view fully.
	 */
	public void collapse();

	/**
	 * Expand the view fully.
	 */
	public void expand();

	/**
	 * @return the current node being displayed
	 */
	public INode getCurrentNode();

	/**
	 * @return the node that was displayed prior to the current one.
	 */
	public INode getPreviousNode();

	/**
	 * @return a new list of the currently selected nodes, possibly empty.
	 */
	public List<Node> getSelectedNodes();

	/**
	 * @return the string that identifies this view.
	 */
	public String getViewID();

	/**
	 * @return the state of the listening control
	 */
	public boolean isListening();

	/**
	 * @return the state of the is inherited properties control
	 */
	public boolean isShowInheritedProperties();

	/**
	 * Command the view to refresh its contents.
	 */
	public void refresh();

	/**
	 * Refresh the view. If regenerate is true, regenerate the contents first.
	 */
	public void refresh(boolean regenerate);

	/**
	 * Command the view to refresh and set its contents.
	 */
	public void refresh(INode node);

	/**
	 * Command the view to refresh and set its contents. If force, any user controls are ignored/overriden.
	 */
	public void refresh(INode node, boolean force);

	/**
	 * Command to refresh all view contents.
	 */
	public void refreshAllViews();

	/**
	 * Command the view to refresh its contents and set the current view to <i>node</i>. By default, the navigator view
	 * is set.
	 */
	public void refreshAllViews(INode node);

	/**
	 * Set the previous node to the current node.
	 */
	public void restorePreviousNode();

	/**
	 * Select the view node. Generates a selection event.
	 * 
	 * @param node
	 *            to select.
	 */
	public void select(final INode node);

	/**
	 * Set the currently displayed node to the passed node.
	 */
	public void setCurrentNode(INode node);

	/**
	 * Set the property type filter.
	 */
	public void setDeepPropertyView(boolean state);

	/**
	 * Set the exact matches only filter.
	 */
	public void setExactMatchFiltering(final boolean state);

	/**
	 * Set the inherited properties filter.
	 */
	public void setInheritedPropertiesDisplayed(final boolean state);

	/**
	 * Set the input data source for the view.
	 */
	public void setInput(INode node);

	/**
	 * Enable or disable listening (linked behavior)
	 */
	public void setListening(final boolean state);

	void remove(INode node);

}
