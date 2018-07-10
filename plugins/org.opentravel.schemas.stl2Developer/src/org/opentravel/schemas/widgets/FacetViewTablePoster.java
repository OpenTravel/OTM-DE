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
package org.opentravel.schemas.widgets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.ServiceNode;
import org.opentravel.schemas.node.interfaces.ComplexMemberInterface;
import org.opentravel.schemas.node.interfaces.FacetInterface;
import org.opentravel.schemas.node.interfaces.FacetOwner;
import org.opentravel.schemas.node.interfaces.InheritedInterface;
import org.opentravel.schemas.node.objectMembers.ContributedFacetNode;
import org.opentravel.schemas.node.objectMembers.ExtensionPointNode;
import org.opentravel.schemas.node.objectMembers.FacetOMNode;
import org.opentravel.schemas.node.objectMembers.OperationNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.typeProviders.AliasNode;
import org.opentravel.schemas.node.typeProviders.ChoiceObjectNode;
import org.opentravel.schemas.node.typeProviders.FacetProviders;
import org.opentravel.schemas.node.typeProviders.ImpliedNode;
import org.opentravel.schemas.node.typeProviders.ListFacetNode;
import org.opentravel.schemas.node.typeProviders.facetOwners.BusinessObjectNode;
import org.opentravel.schemas.properties.Fonts;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.stl2developer.ColorProvider;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.trees.library.LibrarySorter;
import org.opentravel.schemas.types.TypeUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controls table posted in facet view.
 */
// TODO: replace this with JfaceTable (content provider, sorter, and label provider)
public class FacetViewTablePoster {
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(FacetViewTablePoster.class);
	private final Table table;
	private final ColorProvider colorProvider;

	/**
	 *
	 */
	public FacetViewTablePoster(final Table table, final ColorProvider colorProvider) {
		this.table = table;
		this.colorProvider = colorProvider;

		this.table.setLinesVisible(true);
		this.table.setHeaderVisible(true);

		final String[] titles = { "Name", "Role", "Type", "Description" };
		final int[] widths = { 155, 65, 100, 30 };
		for (int i = 0; i < titles.length; i++) {
			final TableColumn column = new TableColumn(this.table, SWT.BOLD);
			column.setText(titles[i]);
			column.setWidth(widths[i]);
		}

		packTable();

	}

	/**
	 * Post the node table and label to the display. This is the primary table display method. Remembers what node is
	 * posted. Posts the name and related properties adjacent to the table.
	 * 
	 * @param curNode
	 *            is the ComponentNode to display
	 */
	public void postTable(Node curNode) {
		clearTable();
		if (curNode == null)
			return;

		// restore focus after posting
		final int[] selectionIndices = table.getSelectionIndices();

		@SuppressWarnings("unchecked")
		// List<Node> sortedChildren = (List<Node>) curNode.getChildrenHandler().get();
		List<Node> sortedChildren = new ArrayList<>(curNode.getChildren());
		sortedChildren = sort(sortedChildren);

		// if (curNode instanceof ServiceNode) {
		// for (final Node kid : sortedChildren) {
		// postTableRows(kid, kid.getLabel());
		// }
		// } else
		if (curNode instanceof ComponentNode) {
			// If there are aliases, post them first.
			for (final Node kid : sortedChildren)
				if (kid instanceof AliasNode)
					postTableRow(kid);

			if (curNode instanceof FacetInterface)
				postTableRows(curNode, curNode.getName());
			else if (curNode instanceof ServiceNode)
				for (final Node kid : sortedChildren)
					postTableRows(kid, kid.getLabel());
			else if (curNode instanceof OperationNode)
				postTableRows(curNode, curNode.getName());
			else if (curNode instanceof ExtensionPointNode)
				postTableRows(curNode, "Extension Point: " + curNode.getName());
			else if (curNode instanceof FacetOwner) {
				if (curNode instanceof BusinessObjectNode || curNode instanceof ChoiceObjectNode)
					// These objects can have inherited facets.
					if (showInherited(curNode) && curNode.getInheritedChildren() != null) {
					sortedChildren.addAll(curNode.getInheritedChildren());
					sortedChildren = sort(sortedChildren);
					}
				for (final Node child : sortedChildren)
					if (child instanceof FacetInterface)
						postTableRows(child, child.getName());
			}
			if (table.getSelectionIndices() != selectionIndices) {
				table.select(selectionIndices);
			}
		}
		packTable();
		table.deselectAll();
	}

	private List<Node> sort(List<Node> children) {
		ArrayList<Node> sorted = new ArrayList<>(children);
		Collections.sort(sorted, LibrarySorter.createComparator());
		return sorted;
	}

	private boolean showInherited(Node n) {
		if (n == null || n.getOwningComponent() == null || OtmRegistry.getNavigatorView() == null)
			return false;
		if (OtmRegistry.getNavigatorView().isShowInheritedProperties())
			return n.isNewToChain() || n.getOwningComponent().isVersioned();
		// return NodeUtils.checker(n).isNewToChain().get() || n.getOwningComponent().isVersioned()
		return false;
	}

	/**
	 * Post the contents of the node into the table, row by row.
	 * 
	 * @param componentNode
	 * @param separator
	 */
	protected void postTableRows(final Node n, final String separator) {
		if (n != null) {
			if (n instanceof ListFacetNode)
				return;

			// Post the separator if not empty.
			TableItem item = null;
			if (!separator.isEmpty()) {
				item = new TableItem(table, SWT.BOLD | SWT.FILL);
				item.setText(separator);
				item.setBackground(colorProvider.getColor(SWT.COLOR_GRAY));
				item.setData(n);
				item.setImage(n.getImage());
				item.setText(1, n.getLabel()); // separator will be name
				item.setForeground(colorProvider.getColor(SWT.COLOR_DARK_BLUE));

				if (n instanceof ContributedFacetNode)
					decorateContributedItem(item, n);
				else if (n instanceof InheritedInterface)
					decorateInheritedItem(item);
			}

			// Sort the table rows
			// List<Node> inherited = (List<Node>) n.getChildrenHandler().getInheritedChildren();
			List<Node> children = n.getChildren_New();
			if (showInherited(n))
				children.addAll(n.getInheritedChildren());

			children = sort(children);

			// Post all the sorted children
			for (final Node cn : children) {
				if (cn instanceof PropertyNode)
					postTableRow(cn, n instanceof ContributedFacetNode);
				else if (cn instanceof FacetOMNode)
					postTableRows(cn, cn.getName()); // operations
				else if (cn instanceof FacetProviders)
					postTableRows(cn, cn.getName());
				else if (cn instanceof ContributedFacetNode)
					// continue; // skip
					postTableRows(cn, cn.getName());
				else if (cn instanceof AliasNode)
					continue; // skip
				else
					// what falls through to here? enum-literal
					assert false; // core summary facet
				// else if (cn instanceof WhereUsedNodeInterface)
				// }
			}

		}
	}

	private void decorateContributedItem(TableItem item, Node n) {
		item.setImage(0, n.getImage());
		item.setFont(Fonts.getFontRegistry().get(Fonts.inheritedItem));
		item.setForeground(colorProvider.getColor(SWT.COLOR_DARK_RED));
		item.setGrayed(true);
	}

	private void decorateInheritedItem(TableItem item) {
		item.setFont(Fonts.getFontRegistry().get(Fonts.inheritedItem));
		item.setForeground(colorProvider.getColor(SWT.COLOR_DARK_BLUE));
		item.setGrayed(true);
	}

	private void decorateContributedItem(TableItem item) {
		item.setFont(Fonts.getFontRegistry().get(Fonts.inheritedItem));
		item.setForeground(colorProvider.getColor(SWT.COLOR_DARK_RED));
		item.setGrayed(true);
	}

	private void decorateReadonlyItem(TableItem item) {
		item.setFont(Fonts.getFontRegistry().get(Fonts.readOnlyItem));
		item.setForeground(colorProvider.getColor(SWT.COLOR_DARK_GRAY));
		item.setGrayed(true);
	}

	/**
	 * Add rows to the global table. Expects a Facet node with leaves and optional text as a separator before the facet
	 * properties are posted. post one row at location i if > 0 or else at bottom of table
	 * 
	 * @param componentNode
	 * @param i
	 */
	protected TableItem postTableRow(final Node n) {
		return postTableRow(n, false);
	}

	/**
	 * Create a row table item for the node.
	 * 
	 * @param n
	 *            node to display
	 * @param contributed
	 *            if true will be decorated as contributed
	 * @return TableItem
	 */
	protected TableItem postTableRow(final Node n, final boolean contributed) {
		// LOGGER.debug("postTableRow( "+n.getName()+" ) - editable? "+n.isEditable());
		assert !(n instanceof ComplexMemberInterface);

		final TableItem item = new TableItem(table, SWT.NONE);
		item.setData(n); // link the node to the row item widget

		if (n instanceof ComponentNode) {
			final ComponentNode cn = (ComponentNode) n;
			// post the icons
			item.setImage(0, n.getImage());

			if (contributed)
				decorateContributedItem(item);
			else if (n instanceof InheritedInterface)
				// else if (n.isInherited() || NodeUtils.checker(n).isInheritedFacet().get())
				decorateInheritedItem(item);
			else if (!n.isEditable() || !n.isInHead2()) {
				decorateReadonlyItem(item);
				// if (n.getLibrary() != n.getParent().getLibrary())
				// LOGGER.debug(n + "library does not match parents. " + n.getParent().getLibrary());
				// n.isEditable();
				// n.isInHead2();
			}

			item.setText(0, n.getName());
			item.setText(1, cn.getPropertyRole());
			item.setText(2, cn.getTypeNameWithPrefix());

			item.setText(3, n.getDescription());

			// Warn the user
			if (n.isUnAssigned()) {
				item.setFont(Fonts.getFontRegistry().get(Fonts.inheritedItem));
				item.setBackground(2, colorProvider.getColor(SWT.COLOR_YELLOW));
			}
			if (cn instanceof TypeUser && ((TypeUser) cn).getRequiredType() instanceof ImpliedNode)
				item.setText(2, "-------");

			// flag duplicates
			// if (cn.isValid() && !cn.isInherited())
			if (!cn.isUnique() && !cn.isInherited())
				item.setImage(Images.getImageRegistry().get(Images.Error));

		}
		return item;
	}

	public void clearTable() {
		if (table == null || table.isDisposed()) {
			return;
		}
		table.removeAll();
	}

	private void packTable() {
		table.getColumn(3).pack();
	}

	/**
	 * Set which row is selected.
	 * 
	 * @param n
	 */
	public void select(final Node n) {
		for (int i = 0; i < table.getItemCount(); i++) {
			final Object data = table.getItem(i).getData();
			if (data != null && data.equals(n)) {
				table.select(i);
				table.showItem(table.getItem(i));
				return;
			}
		}
	}
}
