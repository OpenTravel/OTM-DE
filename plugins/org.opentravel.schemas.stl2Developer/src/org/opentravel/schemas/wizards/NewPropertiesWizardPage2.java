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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFactory;
import org.opentravel.schemas.node.NodeNameUtils;
import org.opentravel.schemas.node.interfaces.FacadeInterface;
import org.opentravel.schemas.node.interfaces.FacetInterface;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.properties.AttributeNode;
import org.opentravel.schemas.node.properties.ElementNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.properties.PropertyNodeType;
import org.opentravel.schemas.node.properties.TypedPropertyNode;
import org.opentravel.schemas.trees.library.LibrarySorter;
import org.opentravel.schemas.trees.library.LibraryTreeContentProvider;
import org.opentravel.schemas.trees.library.LibraryTreeInheritedFilter;
import org.opentravel.schemas.trees.library.LibraryTreeLabelProvider;
import org.opentravel.schemas.trees.library.LibraryTreeWithPropertiesContentProvider;
import org.opentravel.schemas.trees.type.TypeTreeWhereUsedFilter;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeUser;
import org.opentravel.schemas.widgets.ButtonBarManager;
import org.opentravel.schemas.widgets.WidgetFactory;
import org.opentravel.schemas.wizards.validators.FormValidator;
import org.opentravel.schemas.wizards.validators.ValidationException;

/**
 * Page to add new properties directly to an existing facet. Adds to existing facet to allow all type assignment logic
 * to have correct context.
 * 
 * @author Agnieszka Janowska and Dave Hollander
 * 
 */
public class NewPropertiesWizardPage2 extends WizardPage {
	// private static final Logger LOGGER = LoggerFactory.getLogger(NewPropertiesWizardPage2.class);

	private class TextModifyListener implements ModifyListener {
		@Override
		public void modifyText(final ModifyEvent e) {
			setNodeName(nameText.getText());
		}
	}

	private List<PropertyNode> newProperties; // list of properties created by this page
	private final List<PropertyNodeType> enabledPropertyTypes;

	private final Node scopeNode;
	private final FacetInterface owningFacet;
	private PropertyNode selectedNode;

	private final AtomicInteger counter = new AtomicInteger(1);

	// private ListViewer propertyTree;
	private TreeViewer libraryTree;
	private TreeViewer propertyTree;
	private Text nameText;
	private Text typeText;
	private Text descriptionText;
	private Combo propertyCombo;
	private Action copyAction;
	private Action newAction;
	private Action upAction;
	private Action downAction;
	private Action deleteAction;
	private Button typeButton;

	private final FormValidator validator;
	private ViewerFilter propertyFilter;
	private final TextModifyListener textModifyListener = new TextModifyListener();

	/**
	 */
	protected NewPropertiesWizardPage2(final String pageName, final String title, final FormValidator validator,
			final List<PropertyNodeType> enabledTypes, final FacetInterface actOnNode, final Node scope) {
		super(pageName, title, null);
		this.validator = validator;
		this.enabledPropertyTypes = new ArrayList<>(enabledTypes);
		this.owningFacet = actOnNode;
		this.scopeNode = scope;
		this.newProperties = new LinkedList<>();
	}

	@Override
	public void createControl(final Composite parent) {
		final Composite container = new Composite(parent, SWT.BORDER);// parent;
		final GridLayout layout = new GridLayout();
		layout.numColumns = 5;
		container.setLayout(layout);

		final GridData listGD = new GridData();
		listGD.verticalAlignment = SWT.FILL;
		listGD.horizontalAlignment = SWT.FILL;
		listGD.grabExcessVerticalSpace = true;
		listGD.grabExcessHorizontalSpace = true;
		listGD.widthHint = 220;
		listGD.verticalSpan = 6;

		final GridData buttonGD = new GridData();
		buttonGD.horizontalAlignment = SWT.FILL;
		buttonGD.grabExcessVerticalSpace = true;
		buttonGD.verticalSpan = 6;

		final GridData rightPanelGD = new GridData();
		rightPanelGD.verticalAlignment = SWT.FILL;
		rightPanelGD.horizontalAlignment = SWT.FILL;
		rightPanelGD.grabExcessVerticalSpace = true;
		rightPanelGD.grabExcessHorizontalSpace = true;

		libraryTree = new TreeViewer(container);
		libraryTree.setContentProvider(new LibraryTreeContentProvider(true));
		// libraryTree.setContentProvider(new LibraryTreeWithPropertiesContentProvider(false));
		libraryTree.setLabelProvider(new LibraryTreeLabelProvider());
		libraryTree.setSorter(new LibrarySorter());
		libraryTree.setInput(scopeNode);
		libraryTree.getControl().setLayoutData(listGD);
		libraryTree.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(final SelectionChangedEvent event) {
				// LOGGER.debug("Library tree Selection Changed.");
				updateCopyState();
			}
		});
		libraryTree.addDoubleClickListener(new IDoubleClickListener() {

			@Override
			public void doubleClick(final DoubleClickEvent event) {
				// LOGGER.debug("Library Tree double click.");
				final ISelection selection = event.getSelection();
				if (selection instanceof StructuredSelection) {
					final StructuredSelection s = (StructuredSelection) selection;
					final Object o = s.getFirstElement();
					if (o instanceof PropertyNode)
						displayNewProperty(newProperty((PropertyNode) o));
					else if (o instanceof Node)
						displayNewProperty(newProperty((Node) o));
				}

			}
		});
		if (propertyFilter != null) {
			libraryTree.addFilter(propertyFilter);
		}

		propertyTree = new TreeViewer(container);
		propertyTree.setContentProvider(new LibraryTreeWithPropertiesContentProvider(false));
		propertyTree.setLabelProvider(new LibraryTreeLabelProvider());
		propertyTree.addFilter(new TypeTreeWhereUsedFilter());
		propertyTree.addFilter(new LibraryTreeInheritedFilter());
		propertyTree.setSorter(new LibrarySorter());
		propertyTree.setInput(owningFacet);
		propertyTree.getControl().setLayoutData(listGD);
		propertyTree.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(final SelectionChangedEvent event) {
				// LOGGER.debug("Property Tree Selection Changed.");
				final Object selection = propertyTree.getSelection();
				if (selection instanceof IStructuredSelection) {
					final Object first = ((IStructuredSelection) selection).getFirstElement();
					if (first instanceof PropertyNode) {
						final PropertyNode node = (PropertyNode) first;
						setSelectedNode(node);
						updateView();
					}
				}
			}
		});

		final GridData generalGD = new GridData();
		generalGD.horizontalSpan = 2;
		generalGD.horizontalAlignment = SWT.FILL;
		generalGD.grabExcessHorizontalSpace = true;

		final GridData typeTextGD = new GridData();
		typeTextGD.horizontalSpan = 1;
		typeTextGD.horizontalAlignment = SWT.FILL;
		typeTextGD.grabExcessHorizontalSpace = true;

		final GridData multiTextGD = new GridData();
		multiTextGD.horizontalSpan = 3;
		multiTextGD.horizontalAlignment = SWT.FILL;
		multiTextGD.verticalAlignment = SWT.FILL;
		multiTextGD.grabExcessHorizontalSpace = true;
		multiTextGD.grabExcessVerticalSpace = true;

		final Composite rightPanel = container;

		final ButtonBarManager bbManager = new ButtonBarManager(SWT.FLAT);

		copyAction = new Action("Copy") {
			@Override
			public void run() {
				nameText.removeModifyListener(textModifyListener);
				for (final PropertyNode o : getSelectedValidPropertiesFromLibraryTree()) {
					displayNewProperty(newProperty(o));
				}
				nameText.addModifyListener(textModifyListener);
			}
		};
		copyAction.setToolTipText("Copy selected properties from left tree to the list of new properties");

		newAction = new Action("New") {
			@Override
			public void run() {
				displayNewProperty(newProperty());
			}

		};
		newAction.setToolTipText("Create new property");

		deleteAction = new Action("Delete") {
			@Override
			public void run() {
				final PropertyNode selected = getSelectedNode();
				deleteProperty(selected);
			}

		};
		upAction = new Action("Up") {
			@Override
			public void run() {
				final PropertyNode selected = getSelectedNode();
				int index = getNewProperties().indexOf(selected);
				if (index > 0) {
					getNewProperties().remove(index--);
					getNewProperties().add(index, selected);
					selected.moveUp();
					propertyTree.refresh();
					selectInList(selected);
				}
			}
		};
		downAction = new Action("Down") {
			@Override
			public void run() {
				final PropertyNode selected = getSelectedNode();
				int index = getNewProperties().indexOf(selected);
				if (index < getNewProperties().size() - 1) {
					getNewProperties().remove(index++);
					getNewProperties().add(index, selected);
					selected.moveDown();
					propertyTree.refresh();
					selectInList(selected);
				}
			}
		};

		bbManager.add(copyAction);
		bbManager.add(newAction);
		// bbManager.add(upAction);
		// bbManager.add(downAction);
		bbManager.add(deleteAction);

		final Composite bb = bbManager.createControl(rightPanel);
		final GridData bbgd = new GridData();
		bbgd.horizontalSpan = 3;
		bbgd.widthHint = 220;
		bb.setLayoutData(bbgd);

		final Label propertyLabel = new Label(rightPanel, SWT.NONE);
		propertyLabel.setText("Property:");
		propertyCombo = WidgetFactory.createCombo(rightPanel, SWT.DROP_DOWN | SWT.V_SCROLL | SWT.READ_ONLY);
		for (final PropertyNodeType propertyType : enabledPropertyTypes) {
			propertyCombo.add(propertyType.getName());
		}
		propertyCombo.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(final ModifyEvent e) {
				final String selected = propertyCombo.getText();
				if (selected != null && !selected.isEmpty()) {
					final PropertyNodeType type = PropertyNodeType.fromString(selected);
					setPropertyType(type);
					updateView();
				}
			}
		});
		propertyCombo.setLayoutData(generalGD);

		final Label nameLabel = new Label(rightPanel, SWT.NONE);
		nameLabel.setText("Name:");
		nameText = WidgetFactory.createText(rightPanel, SWT.SINGLE | SWT.BORDER);
		nameText.setLayoutData(generalGD);
		nameText.addModifyListener(textModifyListener);

		final Label typeLabel = new Label(rightPanel, SWT.NONE);
		typeLabel.setText("Type:");
		typeText = WidgetFactory.createText(rightPanel, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
		typeText.setLayoutData(typeTextGD);
		typeButton = new Button(rightPanel, SWT.PUSH);
		typeButton.setText("...");
		typeButton.setToolTipText("Select a type for the new property.");
		typeButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				chooseAssignedType();
			}

			@Override
			public void widgetDefaultSelected(final SelectionEvent e) {
			}

		});

		final Label descriptionLabel = new Label(rightPanel, SWT.NONE);
		descriptionLabel.setText("Description:");
		final GridData gd = new GridData();
		gd.horizontalSpan = 2;
		descriptionLabel.setLayoutData(gd);
		descriptionText = WidgetFactory.createText(rightPanel, SWT.MULTI | SWT.BORDER);
		descriptionText.setLayoutData(multiTextGD);
		descriptionText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(final ModifyEvent e) {
				setNodeDescription(descriptionText.getText());
			}

		});

		updateWidgetsState();
		setControl(container);
		setPageComplete(false);
	}

	private void displayNewProperty(final PropertyNode newNode) {
		if (newNode != null) {
			propertyTree.refresh();
			selectInList(newNode);
			updateView();
			chooseType();
			setFocusOnNameText();
		}
	}

	/**
	 * @return newly created property cloned from passed property.
	 */
	private PropertyNode newProperty(final PropertyNode srcProperty) {
		// LOGGER.debug("New Property from property " + o);
		if (!enabledPropertyTypes.contains(srcProperty.getPropertyType())) {
			setMessage(srcProperty.getPropertyType().getName() + "s are not allowed for this object", WARNING);
			return null;
		}
		// Use the node factory to determine what type of property.
		final PropertyNode copy = (PropertyNode) NodeFactory.newChild((Node) owningFacet,
				(TLModelElement) srcProperty.cloneTLObj());
		// Should not be needed, but in case clone does not assign, assign type now
		if (copy instanceof TypedPropertyNode && srcProperty instanceof TypedPropertyNode)
			((AttributeNode) copy).setAssignedType(((TypedPropertyNode) srcProperty).getAssignedType());
		// Add newly created copy to list
		getNewProperties().add(copy);
		return copy;
	}

	/**
	 * @return newly created property with name and type taken from passed node.
	 */
	private PropertyNode newProperty(Node node) {
		// LOGGER.debug("New Property: " + node);
		if (node instanceof FacadeInterface)
			node = ((FacadeInterface) node).get();
		final PropertyNode newProperty = newProperty();
		newProperty.setName(NodeNameUtils.adjustCaseOfName(newProperty.getPropertyType(), node.getName()));
		if (node.isAssignable() && node instanceof TypeProvider && newProperty instanceof TypedPropertyNode)
			((TypedPropertyNode) newProperty).setAssignedType((TypeProvider) node);
		else
			setMessage(node + " is not assigable as type. No type assigned.", WARNING);
		return newProperty;
	}

	/**
	 * @return newly created blank property.
	 */
	private PropertyNode newProperty() {
		// LOGGER.debug("New Property.");
		final String name = "property" + counter.getAndIncrement();
		PropertyNode newProperty = null;
		if (enabledPropertyTypes.contains(PropertyNodeType.ELEMENT))
			newProperty = new ElementNode(owningFacet, name);
		else
			newProperty = new AttributeNode(owningFacet, name);
		newProperty.setDescription("");
		getNewProperties().add(newProperty);
		return newProperty;
	}

	private void deleteProperty(final PropertyNode selected) {
		final int index = getNewProperties().indexOf(selected);
		if (index < 0)
			return; // Do not delete unless new
		getNewProperties().remove(selected);
		owningFacet.removeProperty(selected);
		propertyTree.refresh();
		if (getNewProperties().size() > 0) {
			selectInList(getNewProperties().get(index > 0 ? index - 1 : 0));
		}
		updateView();
	}

	private void chooseAssignedType() {
		final PropertyNode node = getSelectedNode();
		if (node.getOwningComponent() == null) {
			// LOGGER.error("chhoseTLType error - node " + node + " owner is null.");
			setMessage("Error. " + node + " does not have an owner.", ERROR);
			return;
		}

		final TypeSelectionWizard wizard = new TypeSelectionWizard(node);
		// if (wizard.run(getShell(), true)) // let the user select type then assign it
		// let the user select type then assign it
		if (wizard.run(getShell()))
			((TypeUser) node).setAssignedType((TypeProvider) wizard.getSelection());

		// Use the type name as the property name if the user has not already set one.
		if (wizard.getSelection() != null && (getSelectedNode().getName().startsWith("Property")
				|| getSelectedNode().getName().startsWith("property")))
			getSelectedNode().setName(wizard.getSelection().getName());
		updateView();
		propertyTree.update(node, null);
	}

	private void chooseType() {
		if (enabledPropertyTypes.size() == 1) {
			propertyCombo.select(0);
		}
	}

	private void setPropertyType(final PropertyNodeType type) {
		final PropertyNode node = getSelectedNode();
		if (node != null) {
			setSelectedNode(node.changePropertyRole(type));
			// don't change pre-existing properties
			if (newProperties.contains(node)) {
				newProperties.remove(node);
				newProperties.add(getSelectedNode());
			}
			nameText.removeModifyListener(textModifyListener);
			nameText.setText(getSelectedNode().getName());
			nameText.addModifyListener(textModifyListener);
		}
		propertyTree.refresh();
	}

	private void setNodeName(final String text) {
		final Node node = getSelectedNode();
		if (node != null && isNameOK(text)) {
			node.setName(text);
			NodeNameUtils.fixName(node);
			propertyTree.update(node, null);
		}
	}

	private boolean isNameOK(String name) {
		String message = null; // clears message
		boolean complete = true;
		// FIXME - runs too often. first time gives right answer but second time the property is already set.
		// for (Node child : owningFacet.getChildren())
		// if (child.getName().equals(name)) {
		// complete = false;
		// message = (name + ": " + Messages.getString("error.newProperty"));
		// }
		setPageComplete(complete);
		setMessage(message, ERROR);
		getWizard().getContainer().updateButtons();
		return complete;
	}

	private void setNodeDescription(final String text) {
		final Node node = getSelectedNode();
		if (node != null) {
			node.setDescription(text);
		}
	}

	private void setFocusOnNameText() {
		nameText.setFocus();
		final int wordLength = nameText.getText() == null ? 0 : nameText.getText().length();
		nameText.setSelection(0, wordLength);
	}

	public void updateView() {
		updateWidgetsState();
		final PropertyNode selectedNode = getSelectedNode();
		if (selectedNode != null) {
			nameText.setText(selectedNode.getName());

			if (selectedNode instanceof TypedPropertyNode)
				if (((TypedPropertyNode) selectedNode).getAssignedTLNamedEntity() != null)
					typeText.setText(((TypedPropertyNode) selectedNode).getAssignedTLNamedEntity().getLocalName());
			// final ModelObject<?> modelObject = selectedNode.getModelObject();
			// typeText.setText(modelObject == null || modelObject.getTLType() == null ? "" : modelObject.getTLType()
			// .getLocalName());

			descriptionText.setText(selectedNode.getDescription());
			final int index = enabledPropertyTypes.indexOf(selectedNode.getPropertyType());
			if (index >= 0) {
				if (index != propertyCombo.getSelectionIndex()) {
					propertyCombo.select(index);
				}
				if (selectedNode.getPropertyType() != PropertyNodeType.ATTRIBUTE
						&& selectedNode.getPropertyType() != PropertyNodeType.ELEMENT) {
					typeText.setText("");
				}
			} else {
				propertyCombo.deselectAll();
			}
		} else {
			nameText.setText("");
			typeText.setText("");
			descriptionText.setText("");
			propertyCombo.deselectAll();
		}
		validate();
	}

	private void updateWidgetsState() {
		final INode selected = getSelectedNode();
		boolean enabled = false;
		typeButton.setEnabled(enabled);
		if (selected != null) {
			enabled = true;
			typeButton.setEnabled(selected instanceof TypeUser && newProperties.contains(selected));
		}
		newAction.setEnabled(true);
		updateCopyState();

		// Only allow change or deletion of new properties.
		if (!newProperties.contains(selected))
			enabled = false;
		upAction.setEnabled(enabled);
		downAction.setEnabled(enabled);
		nameText.setEnabled(enabled);
		typeText.setEnabled(enabled);
		deleteAction.setEnabled(enabled);
		propertyCombo.setEnabled(enabled);
		descriptionText.setEnabled(enabled);
	}

	private void updateCopyState() {
		final List<PropertyNode> selectedProps = getSelectedValidPropertiesFromLibraryTree();
		if (selectedProps.size() > 0) {
			copyAction.setEnabled(true);
		} else {
			copyAction.setEnabled(false);
		}
	}

	private boolean validate() {
		boolean valid = true;
		boolean complete = true;
		String message = null;
		try {
			validator.validate(selectedNode);
		} catch (final ValidationException e) {
			message = e.getMessage();
			complete = false;
			valid = false;
		}
		setPageComplete(complete);
		setMessage(message, ERROR);
		getWizard().getContainer().updateButtons();
		return valid;
	}

	/**
	 * @return the last node selected from the property tree.
	 */
	public PropertyNode getSelectedNode() {
		return selectedNode;
	}

	public void setSelectedNode(final PropertyNode selectedNode) {
		this.selectedNode = selectedNode;
	}

	private void selectInList(final PropertyNode newNode) {
		propertyTree.setSelection(null);
		propertyTree.setSelection(new StructuredSelection(newNode));
	}

	private List<PropertyNode> getSelectedValidPropertiesFromLibraryTree() {
		final List<PropertyNode> ret = new ArrayList<>();
		final ISelection selection = libraryTree.getSelection();
		if (selection instanceof StructuredSelection) {
			final StructuredSelection strSel = (StructuredSelection) selection;
			for (final Object o : strSel.toList()) {
				if (o instanceof PropertyNode) {
					final PropertyNode p = (PropertyNode) o;
					if (enabledPropertyTypes.contains(p.getPropertyType())) {
						ret.add(p);
					}
				}
			}
		}
		return ret;
	}

	/**
	 * @return the newProperties
	 */
	public List<PropertyNode> getNewProperties() {
		return newProperties;
	}

	public void setPropertyFilter(final ViewerFilter filter) {
		propertyFilter = filter;
	}

}
