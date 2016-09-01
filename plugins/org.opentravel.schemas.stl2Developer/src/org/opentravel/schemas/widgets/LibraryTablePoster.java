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
import org.opentravel.schemas.modelObject.XSDComplexMO;
import org.opentravel.schemas.node.AliasNode;
import org.opentravel.schemas.node.BusinessObjectNode;
import org.opentravel.schemas.node.ChoiceObjectNode;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.EnumerationClosedNode;
import org.opentravel.schemas.node.EnumerationOpenNode;
import org.opentravel.schemas.node.ExtensionPointNode;
import org.opentravel.schemas.node.ImpliedNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.ServiceNode;
import org.opentravel.schemas.node.XsdNode;
import org.opentravel.schemas.node.controllers.NodeUtils;
import org.opentravel.schemas.node.facets.FacetNode;
import org.opentravel.schemas.node.interfaces.ComplexComponentInterface;
import org.opentravel.schemas.node.properties.AttributeNode;
import org.opentravel.schemas.node.properties.ElementNode;
import org.opentravel.schemas.node.properties.ElementReferenceNode;
import org.opentravel.schemas.node.properties.IndicatorElementNode;
import org.opentravel.schemas.node.properties.IndicatorNode;
import org.opentravel.schemas.properties.Fonts;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.stl2developer.ColorProvider;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.trees.library.LibrarySorter;
import org.opentravel.schemas.types.TypeUser;

/**
 * 
 */
// TODO: replace this with JfaceTable (content provider, sorter, and label
// provider)
public class LibraryTablePoster {
	@SuppressWarnings("unused")
	// private static final Logger LOGGER = LoggerFactory.getLogger(LibraryTablePoster.class);
	private final Table table;
	private final ColorProvider colorProvider;

	/**
	 *
	 */
	public LibraryTablePoster(final Table table, final ColorProvider colorProvider) {
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

		if (curNode == null) {
			return;
		}

		// restore focus after posting
		final int[] selectionIndices = table.getSelectionIndices();

		List<Node> sortedChildren = new ArrayList<Node>(curNode.getChildren());
		sortedChildren = sort(sortedChildren);
		if (curNode instanceof ServiceNode) {
			for (final Node kid : sortedChildren) {
				postTableRows(kid, kid.getLabel());
			}
		} else if (curNode instanceof ComponentNode) {
			// If the node is an XSD node, display its xChild node
			// representation.
			if (curNode instanceof XsdNode && curNode.getModelObject() instanceof XSDComplexMO) {
				XsdNode xn = (XsdNode) curNode;// new XsdNode((LibraryMember)
				// curNode.getModelObject().getTLModelObj(), curNode);
				curNode = xn.getOtmModel();
			}

			if (curNode.isTLLibraryMember()) {
				// Put the aliases at the top of the table.
				for (final Node kid : sortedChildren) {
					if (kid instanceof AliasNode) {
						postTableRow(kid);
					}
				}
			}

			if (curNode instanceof EnumerationClosedNode) {
				postTableRows(curNode, "Closed: " + curNode.getName());
			} else if (curNode instanceof AliasNode) {
				postTableRows(curNode, "");
			} else if (curNode instanceof EnumerationOpenNode) {
				postTableRows(curNode, "Open: " + curNode.getName());
			} else if (curNode instanceof ExtensionPointNode) {
				postTableRows(curNode, "Extension Point: " + curNode.getName());
			} else if (curNode instanceof FacetNode) {
				postTableRows(curNode, curNode.getLabel());
			} else if (curNode instanceof ComplexComponentInterface) {
				if (curNode instanceof BusinessObjectNode || curNode instanceof ChoiceObjectNode)
					if (showInherited(curNode) && curNode.getInheritedChildren() != null) {
						sortedChildren.addAll(curNode.getInheritedChildren());
						sortedChildren = sort(sortedChildren);
					}
				for (final Node child : sortedChildren) {
					if (!(child instanceof AliasNode)) {
						postTableRows(child, child.getLabel());
					}
				}
			}
			if (table.getSelectionIndices() != selectionIndices) {
				table.select(selectionIndices);
			}
		}
		packTable();
		table.deselectAll();
	}

	private List<Node> sort(List<Node> children) {
		ArrayList<Node> sorted = new ArrayList<Node>(children);
		Collections.sort(sorted, LibrarySorter.createComparator());
		return sorted;
	}

	private boolean showInherited(Node n) {
		if (n == null || n.getOwningComponent() == null || OtmRegistry.getNavigatorView() == null)
			return true;
		if (NodeUtils.checker(n).isNewToChain().get() || n.getOwningComponent().isVersioned())
			return true;
		return OtmRegistry.getNavigatorView().isShowInheritedProperties();
	}

	/**
	 * Post the contents of the node into the table, row by row.
	 * 
	 * @param componentNode
	 * @param separator
	 */
	protected void postTableRows(final Node n, final String separator) {
		if (n != null) {
			if (n.isListFacet()) {
				return;
			}

			// Post the separator if not empty.
			TableItem item = null;
			if (!separator.isEmpty()) {
				item = new TableItem(table, SWT.BOLD | SWT.FILL);
				item.setText(separator);
				item.setBackground(colorProvider.getColor(SWT.COLOR_GRAY));
				item.setData(n);
				if (n.isInheritedProperty() || NodeUtils.checker(n).isInheritedFacet().get()) {
					decorateInheritedItem(item);
				} else {
					item.setForeground(colorProvider.getColor(SWT.COLOR_DARK_BLUE));
				}
			}

			// Sort the table rows

			List<Node> children = new ArrayList<Node>(n.getChildren());
			if (showInherited(n)) {
				children.addAll(n.getInheritedChildren());
			}
			children = sort(children);
			for (final Node cn : children) {
				if (cn instanceof ElementReferenceNode) {
					postTableRow(cn);
				} else if (cn instanceof IndicatorNode) {
					postTableRow(cn);
				} else if (cn instanceof AttributeNode) {
					postTableRow(cn);
				} else if ((cn instanceof ElementNode) || (cn instanceof IndicatorElementNode)) {
					postTableRow(cn);
				} else if (cn instanceof FacetNode) {
					postTableRows(cn, cn.getLabel());
				} else if (!(cn instanceof FacetNode) && !(cn instanceof AliasNode)) {
					postTableRow(cn); // what falls through to here? enum-literal
				}
			}

		}
	}

	private void decorateInheritedItem(TableItem item) {
		item.setFont(Fonts.getFontRegistry().get(Fonts.inheritedItem));
		item.setForeground(colorProvider.getColor(SWT.COLOR_DARK_BLUE));
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
		// LOGGER.debug("postTableRow( "+n.getName()+" ) - editable? "+n.isEditable());

		final TableItem item = new TableItem(table, SWT.NONE);
		item.setData(n); // link the node to the row item widget

		if (n instanceof ComponentNode) {
			final ComponentNode cn = (ComponentNode) n;
			// post the icons
			item.setImage(0, n.getImage());

			if (n.isInheritedProperty() || NodeUtils.checker(n).isInheritedFacet().get()) {
				// if (!n.isEnabled_AssignType()) {
				decorateInheritedItem(item);
			} else if (!n.isEditable() || !n.isInHead2()) {
				decorateReadonlyItem(item);

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
			if (!cn.isUnique() && !cn.isInheritedProperty()) {
				item.setImage(Images.getImageRegistry().get(Images.Error));
			}
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
