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
package org.opentravel.schemas.wizards;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.BuiltInLibrary;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.XSDLibrary;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.typeProviders.FacetProviderNode;
import org.opentravel.schemas.node.typeProviders.SimpleTypeNode;
import org.opentravel.schemas.node.typeProviders.VWA_Node;
import org.opentravel.schemas.node.typeProviders.facetOwners.CoreObjectNode;
import org.opentravel.schemas.trees.type.TypeSelectionFilter;
import org.opentravel.schemas.trees.type.TypeTree;
import org.opentravel.schemas.trees.type.TypeTreeNameFilter;
import org.opentravel.schemas.trees.type.TypeTreeNamespaceFilter;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeUser;
import org.opentravel.schemas.widgets.WidgetFactory;

/**
 * Wizard to allow user to select a type for the passed node objects.
 * 
 * @author Dave Hollander
 * 
 */
public class TypeSelectionPage extends WizardPage {
	// private static final Logger LOGGER = LoggerFactory.getLogger(TypeSelectionPage.class);

	private INode curNode = null;
	private Node selectedNode;
	private TreeViewer treeViewer;
	private TypeTreeNamespaceFilter nsFilter;
	private TypeTreeNameFilter nameFilter;
	private TypeSelectionFilter typeSelectionFilter;
	private Text nameSpace;
	private Text typeText;
	private Text descriptionText;

	private IDoubleClickListener doubleClickListener = null;
	private TypeSelectionListener typeSelectionListener;
	private boolean canNavigateToNextPage = true;

	private final Map<Button, Class<? extends AbstractLibrary>> radioButtons = new HashMap<>();

	protected TypeSelectionPage(final String pageName, final String title, String description,
			final ImageDescriptor titleImage, final INode n) {
		super(pageName, title, titleImage);
		setTitle(title);
		setDescription(description);
		curNode = n;
		checkCore((Node) n);
		// LOGGER.debug("Created Initial Type Selection Page for node: " + n);
	}

	public void setTypeSelectionListener(TypeSelectionListener listener) {
		this.typeSelectionListener = listener;
	}

	/**
	 * @see org.eclipse.jface.wizard.WizardPage#canFlipToNextPage()
	 */
	@Override
	public boolean canFlipToNextPage() {
		return super.canFlipToNextPage() && canNavigateToNextPage;
	}

	@Override
	public void createControl(final Composite parent) {
		Text name;
		Composite container;
		TypeTree typeTree;

		container = new Composite(parent, SWT.BORDER);
		container.setLayout(new GridLayout(2, false));

		GridData gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;

		final Label l1 = new Label(container, SWT.NONE);
		l1.setText("Assign Type to:");

		name = WidgetFactory.createText(container, SWT.READ_ONLY | SWT.BORDER);
		name.setLayoutData(gridData);
		name.setText(curNode.getNameWithPrefix());

		final Label label = new Label(container, SWT.NULL);
		if (curNode instanceof LibraryNode)
			label.setText("Library:");
		else
			label.setText("Object:");

		typeText = WidgetFactory.createText(container, SWT.BORDER);
		typeText.setLayoutData(gridData);
		typeText.addKeyListener(new TypeKeyListener());
		typeText.setFocus();

		// Namespace field
		final Label nsLabel = new Label(container, SWT.NULL);
		if (curNode instanceof LibraryNode)
			nsLabel.setText("Library Namespace:");
		else
			nsLabel.setText("Type Namespace:");
		nameSpace = WidgetFactory.createText(container, SWT.BORDER | SWT.READ_ONLY);
		nameSpace.setLayoutData(gridData);
		nameSpace.addKeyListener(new TypeKeyListener());

		// Description Field
		final Label desLabel = new Label(container, SWT.NULL);
		desLabel.setText("Description:");
		descriptionText = WidgetFactory.createText(container, SWT.BORDER | SWT.READ_ONLY);
		descriptionText.setLayoutData(gridData);

		// Tree filter by namespace
		if (!(curNode instanceof LibraryNode)) {
			final Label l2 = new Label(container, SWT.NULL);
			l2.setText("Show namespaces:");
			radioButtons(container);
		}

		// Type Tree
		// typeTree = new TypeTree(Node.getModelNode(), typeTreeContentProvider);
		typeTree = new TypeTree(Node.getModelNode(), null); // sets up type tree view part with TypeTreeContentProvider
		treeViewer = typeTree.setUpViewer(container);
		gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.horizontalSpan = 2;
		treeViewer.getTree().setLayoutData(gridData);

		nameFilter = new TypeTreeNameFilter();
		treeViewer.addFilter(nameFilter);
		nsFilter = new TypeTreeNamespaceFilter();
		treeViewer.addFilter(nsFilter);
		treeViewer.refresh();

		treeViewer.addSelectionChangedListener(new TTSelectionListener());
		if (doubleClickListener != null) {
			treeViewer.addDoubleClickListener(doubleClickListener);
		}
		List<Node> excluded = getExcludeNodes(curNode);
		if (!excluded.isEmpty()) {
			treeViewer.addFilter(new CurrentNodeFilter(excluded));
		}
		if (typeSelectionFilter != null) {
			treeViewer.addFilter(typeSelectionFilter);
		}
		// treeViewer.refresh(); // shouldn't be needed, but i was missing a changed BO to core once
		// as base of a core

		setControl(container); // Required to avoid an error in the system
		setPageComplete(false);
	}

	public void setTypeSelectionFilter(TypeSelectionFilter typeSelectionFilter) {
		if (treeViewer != null)
			treeViewer.removeFilter(this.typeSelectionFilter);
		this.typeSelectionFilter = typeSelectionFilter;
		if (treeViewer != null)
			treeViewer.addFilter(typeSelectionFilter);
	}

	public void setTypeTreeContentProvider(IContentProvider typeTreeContentProvider) {
		if (treeViewer != null)
			treeViewer.setContentProvider(typeTreeContentProvider);
		// LOGGER.debug("Set content provider to: " + typeTreeContentProvider.getClass().getSimpleName());
	}

	public void addDoubleClickListener(final IDoubleClickListener listener) {
		doubleClickListener = listener;
	}

	protected Node getSelectedNode() {
		return selectedNode;
	}

	private class CurrentNodeFilter extends ViewerFilter {
		private List<Node> excluded;

		public CurrentNodeFilter(List<Node> excluded) {
			this.excluded = excluded;
		}

		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			return !containReference(excluded, element);
		}

		private boolean containReference(List<Node> objects, Object element) {
			for (Node o : objects)
				if (o == element)
					return true;
			return false;
		}
	}

	private List<Node> getExcludeNodes(INode node) {
		Node n = (Node) node;
		if (n.getOwningComponent() instanceof VWA_Node && n instanceof SimpleTypeNode)
			return Collections.singletonList((Node) n.getOwningComponent());

		if (n.getOwningComponent() instanceof CoreObjectNode && n instanceof SimpleTypeNode)
			return Collections.singletonList((Node) n.getOwningComponent());

		if (n instanceof CoreObjectNode)
			return Collections.singletonList(n);

		return Collections.emptyList();
	}

	private final class TTSelectionListener implements ISelectionChangedListener {
		@Override
		public void selectionChanged(final SelectionChangedEvent event) {
			checkCore((Node) curNode);
			final IStructuredSelection iss = (IStructuredSelection) event.getSelection();
			final Object object = iss.getFirstElement();
			if (!(object instanceof Node)) {
				return;
			}
			final Node n = (Node) object;
			nameSpace.setText(n.getNamespace());
			typeText.setText(n.getName());
			descriptionText.setText(n.getDescription());

			// If no type selection filter is assigned, the selection is valid as long as it is
			// assignable. If a
			// filter is present, delegate the evaluation of the selected node to the filter.
			if ((typeSelectionFilter == null) ? n.isAssignable() : typeSelectionFilter.isValidSelection(n)) {
				selectedNode = n;
				setPageComplete(true); // create logic
			} else {
				selectedNode = null;
				setPageComplete(false);
			}
			if (typeSelectionListener != null) {
				canNavigateToNextPage = typeSelectionListener.notifyTypeSelected(selectedNode);
				getContainer().updateButtons();
			}
			checkCore((Node) curNode);
		}
	}

	private final class TypeKeyListener implements KeyListener {
		@Override
		public void keyPressed(final KeyEvent e) {
			// NO-OP
		}

		@Override
		// Only Name must be filled out to move to next page
		public void keyReleased(final KeyEvent e) {
			final String txt = typeText.getText();
			nameFilter.setText(txt);
			// treeViewer.expandAll();
			treeViewer.refresh();
		}
	}

	public final class ButtonSelectionHandler extends SelectionAdapter {

		@Override
		public void widgetSelected(final SelectionEvent e) {
			if (e.widget instanceof Button) {
				final Button b = (Button) e.widget;
				// Namespace radio button selected
				nsFilter.setLibrary(radioButtons.get(b));
				treeViewer.expandAll();
				treeViewer.refresh();
			}
		}
	}

	private void radioButtons(final Composite c) {
		final Composite c2 = new Composite(c, SWT.NULL);
		final GridLayout gl2 = new GridLayout(4, false);
		c2.setLayout(gl2);

		Button radio = new Button(c2, SWT.RADIO);
		radio.setText("All");
		radio.setSelection(true);
		radioButtons.put(radio, AbstractLibrary.class);
		radio.addSelectionListener(new ButtonSelectionHandler());

		radio = new Button(c2, SWT.RADIO);
		radio.setText("Libraries");
		radioButtons.put(radio, TLLibrary.class);
		radio.addSelectionListener(new ButtonSelectionHandler());

		radio = new Button(c2, SWT.RADIO);
		radio.setText("Built-ins");
		radioButtons.put(radio, BuiltInLibrary.class);
		radio.addSelectionListener(new ButtonSelectionHandler());

		radio = new Button(c2, SWT.RADIO);
		radio.setText("XSD Schemas");
		radioButtons.put(radio, XSDLibrary.class);
		radio.addSelectionListener(new ButtonSelectionHandler());
	}

	/**
	 * check assigned types and assure they have parents
	 * 
	 * @param curNode
	 *            checked if Core Object
	 */
	protected void checkCore(Node curNode) {
		if (curNode instanceof CoreObjectNode) {
			FacetProviderNode sum = ((CoreObjectNode) curNode).getFacet_Summary();
			// check assigned types and assure they have parents
			for (Node n : sum.getChildren()) {
				if (n instanceof TypeUser) {
					TypeProvider type = ((TypeUser) n).getAssignedType();
					assert ((Node) type).getParent() != null;
				}
			}
		}
	}

}
