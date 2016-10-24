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

import java.util.List;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.opentravel.schemas.node.LibraryNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.PropertyNodeType;
import org.opentravel.schemas.wizards.validators.FormValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 10/15/2016 - dmh - commented out
 * 
 * @author Agnieszka Janowska
 * 
 */
@Deprecated
public class NewPropertiesWizardPage extends WizardPage {
	private static final Logger LOGGER = LoggerFactory.getLogger(NewPropertiesWizardPage.class);

	private class TextModifyListener implements ModifyListener {
		@Override
		public void modifyText(final ModifyEvent e) {
			// setNodeName(nameText.getText());
			// validate();
		}
	}

	// private List<PropertyNode> newProperties;
	// private final List<PropertyNodeType> propertyTypesOrder;
	//
	// private PropertyNode selectedNode;
	//
	// private final AtomicInteger counter = new AtomicInteger(1);
	//
	// private ListViewer propertyTree;
	// private TreeViewer libraryTree;
	// private TreeViewer propertyTree;
	// private Text nameText;
	// private Text typeText;
	// private Text descriptionText;
	// private Combo propertyCombo;
	// private Action copyAction;
	// private Action newAction;
	// private Action upAction;
	// private Action downAction;
	// private Action deleteAction;
	// private Button typeButton;
	// private final Node scopeNode;
	// private final PropertyOwnerInterface editedFacet;
	// // private final FormValidator validator;
	// private ViewerFilter propertyFilter;
	// private final LibraryNode library;
	// private final TextModifyListener textModifyListener = new TextModifyListener();

	/**
	 * @param pageName
	 * @param title
	 * @param titleImage
	 * @param scope
	 *            is a node that is the root of the tree of choices presented in the wizard
	 */
	protected NewPropertiesWizardPage(final String pageName, final String title, final FormValidator validator,
			final List<PropertyNodeType> enabledTypes, final LibraryNode library, final Node scope) {
		super(pageName, title, null);
		// this.validator = validator;
		// propertyTypesOrder = new ArrayList<PropertyNodeType>(enabledTypes);
		// this.scopeNode = scope;
		// editedFacet = new FacetNode();
		// this.library = library;
		// this.setNewProperties(new LinkedList<PropertyNode>());
	}

	@Override
	public void createControl(final Composite parent) {
		// final Composite container = new Composite(parent, SWT.BORDER);// parent;
		// final GridLayout layout = new GridLayout();
		// layout.numColumns = 5;
		// container.setLayout(layout);
		//
		// final GridData listGD = new GridData();
		// listGD.verticalAlignment = SWT.FILL;
		// listGD.horizontalAlignment = SWT.FILL;
		// listGD.grabExcessVerticalSpace = true;
		// listGD.grabExcessHorizontalSpace = true;
		// listGD.widthHint = 220;
		// listGD.verticalSpan = 6;
		//
		// final GridData buttonGD = new GridData();
		// buttonGD.horizontalAlignment = SWT.FILL;
		// buttonGD.grabExcessVerticalSpace = true;
		// buttonGD.verticalSpan = 6;
		//
		// final GridData rightPanelGD = new GridData();
		// rightPanelGD.verticalAlignment = SWT.FILL;
		// rightPanelGD.horizontalAlignment = SWT.FILL;
		// rightPanelGD.grabExcessVerticalSpace = true;
		// rightPanelGD.grabExcessHorizontalSpace = true;
		//
		// libraryTree = new TreeViewer(container);
		// libraryTree.setContentProvider(new LibraryTreeWithPropertiesContentProvider(false));
		// libraryTree.setLabelProvider(new LibraryTreeLabelProvider());
		// libraryTree.setSorter(new LibrarySorter());
		// libraryTree.setInput(scopeNode);
		// libraryTree.getControl().setLayoutData(listGD);
		// libraryTree.addSelectionChangedListener(new ISelectionChangedListener() {
		//
		// @Override
		// public void selectionChanged(final SelectionChangedEvent event) {
		// updateCopyState();
		// }
		// });
		// libraryTree.addDoubleClickListener(new IDoubleClickListener() {
		//
		// @Override
		// public void doubleClick(final DoubleClickEvent event) {
		// final ISelection selection = event.getSelection();
		// if (selection instanceof StructuredSelection) {
		// final StructuredSelection s = (StructuredSelection) selection;
		// final Object o = s.getFirstElement();
		// if (o instanceof PropertyNode) {
		// displayNewProperty(newProperty((PropertyNode) o));
		// } else if (o instanceof Node) {
		// final Node node = (Node) o;
		// newPropertyFromType(node);
		// }
		// }
		//
		// }
		// });
		// if (propertyFilter != null) {
		// libraryTree.addFilter(propertyFilter);
		// }
		//
		// propertyTree = new TreeViewer(container);
		// propertyTree.setContentProvider(new LibraryTreeWithPropertiesContentProvider(false));
		// propertyTree.setLabelProvider(new LibraryTreeLabelProvider());
		// propertyTree.setSorter(new LibrarySorter());
		// propertyTree.setInput(editedFacet);
		// propertyTree.getControl().setLayoutData(listGD);
		// propertyTree.addSelectionChangedListener(new ISelectionChangedListener() {
		//
		// @Override
		// public void selectionChanged(final SelectionChangedEvent event) {
		// final Object selection = propertyTree.getSelection();
		// if (selection instanceof IStructuredSelection) {
		// final Object first = ((IStructuredSelection) selection).getFirstElement();
		// if (first instanceof PropertyNode) {
		// final PropertyNode node = (PropertyNode) first;
		// setSelectedNode(node);
		// updateView();
		// }
		// }
		// }
		// });
		//
		// final GridData generalGD = new GridData();
		// generalGD.horizontalSpan = 2;
		// generalGD.horizontalAlignment = SWT.FILL;
		// generalGD.grabExcessHorizontalSpace = true;
		//
		// final GridData typeTextGD = new GridData();
		// typeTextGD.horizontalSpan = 1;
		// typeTextGD.horizontalAlignment = SWT.FILL;
		// typeTextGD.grabExcessHorizontalSpace = true;
		//
		// final GridData multiTextGD = new GridData();
		// multiTextGD.horizontalSpan = 3;
		// multiTextGD.horizontalAlignment = SWT.FILL;
		// multiTextGD.verticalAlignment = SWT.FILL;
		// multiTextGD.grabExcessHorizontalSpace = true;
		// multiTextGD.grabExcessVerticalSpace = true;
		//
		// final Composite rightPanel = container;
		//
		// final ButtonBarManager bbManager = new ButtonBarManager(SWT.FLAT);
		//
		// copyAction = new Action("Copy") {
		// @Override
		// public void run() {
		// for (final PropertyNode o : getSelectedValidPropertiesFromLibraryTree()) {
		// displayNewProperty(newProperty(o));
		// }
		// }
		// };
		// copyAction.setToolTipText("Copy selected properties from left tree to the list of new properties");
		//
		// newAction = new Action("New") {
		// @Override
		// public void run() {
		// displayNewProperty(newProperty());
		// }
		//
		// };
		// newAction.setToolTipText("Create new property");
		//
		// deleteAction = new Action("Delete") {
		// @Override
		// public void run() {
		// final PropertyNode selected = getSelectedNode();
		// deleteProperty(selected);
		// }
		//
		// };
		// upAction = new Action("Up") {
		// @Override
		// public void run() {
		// final PropertyNode selected = getSelectedNode();
		// int index = getNewProperties().indexOf(selected);
		// if (index > 0) {
		// getNewProperties().remove(index--);
		// getNewProperties().add(index, selected);
		// selected.moveProperty(PropertyNode.UP);
		// propertyTree.refresh();
		// selectInList(selected);
		// }
		// }
		// };
		// downAction = new Action("Down") {
		// @Override
		// public void run() {
		// final PropertyNode selected = getSelectedNode();
		// int index = getNewProperties().indexOf(selected);
		// if (index < getNewProperties().size() - 1) {
		// getNewProperties().remove(index++);
		// getNewProperties().add(index, selected);
		// selected.moveProperty(PropertyNode.DOWN);
		// propertyTree.refresh();
		// selectInList(selected);
		// }
		// }
		// };
		//
		// bbManager.add(copyAction);
		// bbManager.add(newAction);
		// // bbManager.add(upAction);
		// // bbManager.add(downAction);
		// bbManager.add(deleteAction);
		//
		// final Composite bb = bbManager.createControl(rightPanel);
		// final GridData bbgd = new GridData();
		// bbgd.horizontalSpan = 3;
		// bbgd.widthHint = 220;
		// bb.setLayoutData(bbgd);
		//
		// final Label propertyLabel = new Label(rightPanel, SWT.NONE);
		// propertyLabel.setText("Property:");
		// propertyCombo = WidgetFactory.createCombo(rightPanel, SWT.DROP_DOWN | SWT.V_SCROLL | SWT.READ_ONLY);
		// for (final PropertyNodeType propertyType : propertyTypesOrder) {
		// propertyCombo.add(propertyType.getName());
		// }
		// propertyCombo.addModifyListener(new ModifyListener() {
		//
		// @Override
		// public void modifyText(final ModifyEvent e) {
		// final String selected = propertyCombo.getText();
		// if (selected != null && !selected.isEmpty()) {
		// final PropertyNodeType type = PropertyNodeType.fromString(selected);
		// setPropertyType(type);
		// updateView();
		// }
		// }
		// });
		// propertyCombo.setLayoutData(generalGD);
		//
		// final Label nameLabel = new Label(rightPanel, SWT.NONE);
		// nameLabel.setText("Name:");
		// nameText = WidgetFactory.createText(rightPanel, SWT.SINGLE | SWT.BORDER);
		// nameText.setLayoutData(generalGD);
		// nameText.addModifyListener(textModifyListener);
		//
		// final Label typeLabel = new Label(rightPanel, SWT.NONE);
		// typeLabel.setText("Type:");
		// typeText = WidgetFactory.createText(rightPanel, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
		// typeText.setLayoutData(typeTextGD);
		// typeButton = new Button(rightPanel, SWT.PUSH);
		// typeButton.setText("...");
		// typeButton.setToolTipText("Select a type for the new property.");
		// typeButton.addSelectionListener(new SelectionListener() {
		//
		// @Override
		// public void widgetSelected(final SelectionEvent e) {
		// chooseTLType();
		// }
		//
		// @Override
		// public void widgetDefaultSelected(final SelectionEvent e) {
		// }
		//
		// });
		//
		// final Label descriptionLabel = new Label(rightPanel, SWT.NONE);
		// descriptionLabel.setText("Description:");
		// final GridData gd = new GridData();
		// gd.horizontalSpan = 2;
		// descriptionLabel.setLayoutData(gd);
		// descriptionText = WidgetFactory.createText(rightPanel, SWT.MULTI | SWT.BORDER);
		// descriptionText.setLayoutData(multiTextGD);
		// descriptionText.addModifyListener(new ModifyListener() {
		//
		// @Override
		// public void modifyText(final ModifyEvent e) {
		// setNodeDescription(descriptionText.getText());
		// }
		//
		// });
		//
		// updateWidgetsState();
		// setControl(container);
		// setPageComplete(false);
	}

	// private void displayNewProperty(final PropertyNode newNode) {
	// if (newNode != null) {
	// propertyTree.refresh();
	// selectInList(newNode);
	// updateView();
	// chooseType();
	// setFocusOnNameText();
	// }
	// }
	//
	// private PropertyNode newProperty(final PropertyNode o) {
	// if (!propertyTypesOrder.contains(o.getPropertyType())) {
	// setMessage(o.getPropertyType().getName() + "s are not allowed for this object", WARNING);
	// return null;
	// }
	// final PropertyNode copy = (PropertyNode) NodeFactory.newComponentMember((INode) editedFacet, o.cloneTLObj());
	// copy.setAssignedType((TypeProvider) o.getType());
	// getNewProperties().add(copy);
	// return copy;
	// }
	//
	// private PropertyNode newProperty() {
	// // parent is required by PropertyNode.changePropertyTo()
	// final PropertyNode newNode = createPropertyNode(editedFacet);
	// editedFacet.addProperty(newNode);
	// getNewProperties().add(newNode);
	// return newNode;
	// }

	// private PropertyNode createPropertyNode(PropertyOwnerInterface parent) {
	// final String name = "property" + counter.getAndIncrement();
	// // TODO - if element is not enabled, use AttributeNode
	// PropertyNode n = null;
	// if (propertyTypesOrder.contains(PropertyNodeType.ELEMENT))
	// n = new ElementNode(parent, name);
	// else
	// n = new AttributeNode(parent, name);
	// n.setName(name);
	// n.setLibrary(library);
	// n.setDescription("");
	// n.setAssignedType(ModelNode.getUnassignedNode());
	// return n;
	// }
	//
	// // @SuppressWarnings("unused")
	// // private PropertyNode createPropertyNode() {
	// // final String name = "property" + counter.getAndIncrement();
	// // final PropertyNode n = new PropertyNode(propertyTypesOrder.get(0));
	// // n.setName(name);
	// // n.setLibrary(library);
	// // n.setDescription("");
	// // n.setAssignedType(ModelNode.getUnassignedNode());
	// // return n;
	// // }

	// private void newPropertyFromType(final Node node) {
	// if (node.isAssignable()) {
	// final PropertyNode newProperty = newProperty();
	// if (node instanceof TypeProvider)
	// newProperty.setAssignedType((TypeProvider) node);
	// String adjusted = NodeNameUtils.adjustCaseOfName(newProperty.getPropertyType(), node.getName());
	// newProperty.setName(adjusted);
	// displayNewProperty(newProperty);
	// }
	// }
	//
	// private void deleteProperty(final PropertyNode selected) {
	// final int index = getNewProperties().indexOf(selected);
	// getNewProperties().remove(selected);
	// editedFacet.removeProperty(selected);
	// propertyTree.refresh();
	// if (getNewProperties().size() > 0) {
	// selectInList(getNewProperties().get(index > 0 ? index - 1 : 0));
	// }
	// }

	// private void chooseTLType() {
	// final PropertyNode node = getSelectedNode();
	// final TypeSelectionWizard wizard = new TypeSelectionWizard(node);
	// if (node.getOwningComponent() == null) {
	// LOGGER.error("chhoseTLType error - node " + node + " owner is null.");
	// return;
	// // FIXME - type assignment will not work correctly if anscestry is bad (can't find getOwningComponent())
	// }
	//
	// wizard.run(getShell(), true); // let the user select type then assign it
	// // Use the type name as the property name if the user has not already set one.
	// if (wizard.getSelection() != null
	// && (getSelectedNode().getName().startsWith("Property") || getSelectedNode().getName().startsWith(
	// "property")))
	// getSelectedNode().setName(wizard.getSelection().getName());
	// updateView();
	// propertyTree.update(node, null);
	// }
	//
	// private void chooseType() {
	// if (propertyTypesOrder.size() == 1) {
	// propertyCombo.select(0);
	// }
	// }

	// private void setPropertyType(final PropertyNodeType type) {
	// final PropertyNode node = getSelectedNode();
	// if (node != null) {
	// newProperties.remove(node);
	// setSelectedNode(node.changePropertyRole(type));
	// newProperties.add(getSelectedNode());
	// nameText.removeModifyListener(textModifyListener);
	// nameText.setText(getSelectedNode().getName());
	// nameText.addModifyListener(textModifyListener);
	// }
	// propertyTree.refresh();
	// }
	//
	// private void setNodeName(final String text) {
	// final Node node = getSelectedNode();
	// if (node != null) {
	// node.setName(text);
	// // LOGGER.debug("Set name on: " + node.getName());
	// NodeNameUtils.fixName(node);
	// // LOGGER.debug("Set name on: " + node.getName());
	// // TODO - figure out how to show the user the changed name
	// propertyTree.update(node, null);
	// }
	// }
	//
	// private void setNodeDescription(final String text) {
	// final Node node = getSelectedNode();
	// if (node != null) {
	// node.setDescription(text);
	// }
	// }

	// private void setFocusOnNameText() {
	// nameText.setFocus();
	// final int wordLength = nameText.getText() == null ? 0 : nameText.getText().length();
	// nameText.setSelection(0, wordLength);
	// }

	// public void updateView() {
	// updateWidgetsState();
	// final PropertyNode selectedNode = getSelectedNode();
	// if (selectedNode != null) {
	// nameText.setText(selectedNode.getName());
	// final ModelObject<?> modelObject = selectedNode.getModelObject();
	// typeText.setText(modelObject == null || modelObject.getTLType() == null ? "" : modelObject.getTLType()
	// .getLocalName());
	// descriptionText.setText(selectedNode.getDescription());
	// final int index = propertyTypesOrder.indexOf(selectedNode.getPropertyType());
	// if (index >= 0) {
	// if (index != propertyCombo.getSelectionIndex()) {
	// propertyCombo.select(index);
	// }
	// if (selectedNode.getPropertyType() != PropertyNodeType.ATTRIBUTE
	// && selectedNode.getPropertyType() != PropertyNodeType.ELEMENT) {
	// typeText.setText("");
	// }
	// } else {
	// propertyCombo.deselectAll();
	// }
	// } else {
	// nameText.setText("");
	// typeText.setText("");
	// descriptionText.setText("");
	// propertyCombo.deselectAll();
	// }
	// validate();
	// }
	//
	// private void updateWidgetsState() {
	// final INode selected = getSelectedNode();
	// boolean enabled = false;
	// if (selected != null) {
	// enabled = true;
	// }
	// newAction.setEnabled(true);
	// deleteAction.setEnabled(enabled);
	// upAction.setEnabled(enabled);
	// downAction.setEnabled(enabled);
	// nameText.setEnabled(enabled);
	// typeText.setEnabled(enabled);
	// propertyCombo.setEnabled(enabled);
	// descriptionText.setEnabled(enabled);
	// updateCopyState();
	// updateTypeButtonState();
	// }
	//
	// private void updateCopyState() {
	// final List<PropertyNode> selectedProps = getSelectedValidPropertiesFromLibraryTree();
	// if (selectedProps.size() > 0) {
	// copyAction.setEnabled(true);
	// } else {
	// copyAction.setEnabled(false);
	// }
	// }
	//
	// private void updateTypeButtonState() {
	// final PropertyNode node = getSelectedNode();
	// boolean enabled = false;
	// if (node != null) {
	// final PropertyNodeType propertyType = node.getPropertyType();
	// if (propertyType == PropertyNodeType.ELEMENT || propertyType == PropertyNodeType.ATTRIBUTE) {
	// enabled = true;
	// }
	// }
	// typeButton.setEnabled(enabled);
	// }
	//
	// private void validate() {
	// boolean complete = true;
	// String message = null;
	// try {
	// validator.validate();
	// } catch (final ValidationException e) {
	// message = e.getMessage();
	// complete = false;
	// // LOGGER.debug("Validation output " + e.getMessage());
	// }
	// setPageComplete(complete);
	// setMessage(message, ERROR);
	// getWizard().getContainer().updateButtons();
	// }
	//
	// /**
	// * @return the last node selected from the property tree.
	// */
	// public PropertyNode getSelectedNode() {
	// return selectedNode;
	// }

	// public void setSelectedNode(final PropertyNode selectedNode) {
	// this.selectedNode = selectedNode;
	// }
	//
	// private void selectInList(final PropertyNode newNode) {
	// propertyTree.setSelection(null);
	// propertyTree.setSelection(new StructuredSelection(newNode));
	// }
	//
	// private List<PropertyNode> getSelectedValidPropertiesFromLibraryTree() {
	// final List<PropertyNode> ret = new ArrayList<PropertyNode>();
	// final ISelection selection = libraryTree.getSelection();
	// if (selection instanceof StructuredSelection) {
	// final StructuredSelection strSel = (StructuredSelection) selection;
	// for (final Object o : strSel.toList()) {
	// if (o instanceof PropertyNode) {
	// final PropertyNode p = (PropertyNode) o;
	// if (propertyTypesOrder.contains(p.getPropertyType())) {
	// ret.add(p);
	// }
	// }
	// }
	// }
	// return ret;
	// }

	// /**
	// * @return the newProperties
	// */
	// public List<PropertyNode> getNewProperties() {
	// return newProperties;
	// }
	//
	// /**
	// * @param newProperties
	// * the newProperties to set
	// */
	// public void setNewProperties(final List<PropertyNode> newProperties) {
	// this.newProperties = newProperties;
	// }
	//
	// public void setPropertyFilter(final ViewerFilter filter) {
	// propertyFilter = filter;
	// }

}
