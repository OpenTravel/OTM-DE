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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLPropertyOwner;
import org.opentravel.schemacompiler.model.TLPropertyType;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFactory;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.objectMembers.FacetOMNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.properties.TypedPropertyNode;
import org.opentravel.schemas.trees.library.LibraryTreeLabelProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wizard page that allows the user to configure which properties whose types are part of a nested inheritance structure
 * are to be used in the extended entity.
 * 
 * @author S. Livezey
 */
// No longer used (11/28/2017) - leave in case people want functionality back. Asked Patty's team and they said no.
@Deprecated
public class ExtensionInheritancePage extends WizardPage implements TypeSelectionListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionInheritancePage.class);
	private static final String TREE_INPUT = "TREE_INPUT";

	private final Node curNode;
	private Node selectedExtensionNode;
	private Object selectedExtensionTLObj;
	private ComponentNode treeNode;
	private TableViewer fieldTable;
	private TableViewer fieldSelectionTable;
	private List<TableEditor> fieldSelectionButtons = new ArrayList<>();
	private TreeViewer treeViewer;

	private List<PropertyNode> extensionProperties = new ArrayList<>();
	private Map<PropertyNode, TLFacet> propertyFacetOwners = new HashMap<>();
	private Map<PropertyNode, List<ComponentNode>> inheritanceOptions = new HashMap<>();
	private Map<PropertyNode, ComponentNode> originalOptions = new HashMap<>();
	private Map<PropertyNode, ComponentNode> selectedOptions = new HashMap<>();
	private Map<NamedEntity, List<NamedEntity>> inheritanceHierarchyInfo = new HashMap<>();

	protected ExtensionInheritancePage(final String pageName, final String title, String description,
			final ImageDescriptor titleImage, final Node n) {
		super(pageName, title, titleImage);
		// setTitle(title);
		// setDescription(description);
		curNode = n;
		//
		// if (curNode != null) {
		// // ModelObject<?> modelObject = curNode.getModelObject();
		// // if (modelObject.getTLModelObj() instanceof TLModelElement) {
		// // TLModelElement tlObj = (TLModelElement) modelObject.getTLModelObj();
		//
		// if (curNode.getTLModelObject() instanceof TLModelElement) {
		// TLModelElement tlObj = curNode.getTLModelObject();
		// inheritanceHierarchyInfo = new InheritanceHierarchyBuilder(tlObj.getOwningModel()).buildHierarchyInfo();
		// }
		// }
	}

	protected ExtensionInheritancePage(final String pageName, final String title, String description, final Node n) {
		this(pageName, title, description, null, n);
	}

	public void doPerformFinish() {
		// for (PropertyNode property : originalOptions.keySet()) {
		// LOGGER.info("Processing Property: " + property.getName());
		//
		// INode originalOption = originalOptions.get(property);
		// ComponentNode selectedOption = selectedOptions.get(property);
		//
		// // Ignore options that were not modified by the user
		// if (originalOption == selectedOption) {
		// LOGGER.info(" No Change to Property: " + property.getName());
		// continue;
		// }
		//
		// if (isInheritedProperty(property)) {
		// // The original inherited property will be eclipsed by a new
		// // declared property
		// LOGGER.info(" Creating Property to Eclipse Inherited Property: " + selectedOption.getName());
		// FacetNode currentNodeFacet = getCurrentNodeFacet(property);
		//
		// if (currentNodeFacet != null) {
		// currentNodeFacet.createProperty(selectedOption);
		// }
		//
		// } else {
		// // The existing declared property must be deleted
		// LOGGER.info(" Deleting Property: " + property.getName());
		// property.delete();
		// // property.getParent().deleteChild(property);
		//
		// if (!isInheritedPropertyType(selectedOption)) {
		// // The original declared property will be replaced by a
		// // declared property of a
		// // different type
		// LOGGER.info(" Creating Property to Replace the Deleted Property: " + selectedOption.getName());
		// FacetNode currentNodeFacet = getCurrentNodeFacet(property);
		//
		// if (currentNodeFacet != null) {
		// currentNodeFacet.createProperty(selectedOption);
		// }
		//
		// } else {
		// // No further action - the original declared property was
		// // deleted, so the
		// // inherited property
		// // will be used.
		// }
		// }
		// }
		// curNode.resetInheritedChildren();
	}

	/**
	 * Returns false if the given node is a child (ancestor) of the current node. If the node was inherited from the
	 * extension node, true will be returned.
	 * 
	 * @param propertyNode
	 *            the node to analyze
	 * @return boolean
	 */
	private boolean isInheritedProperty(PropertyNode propertyNode) {
		boolean isInherited = true;
		// INode node = propertyNode;
		//
		// while (node != null) {
		// if (node == curNode) {
		// isInherited = false;
		// break;
		// }
		// node = node.getParent();
		// }
		return isInherited;
	}

	/**
	 * Returns true if the given property type is utilizes by one of the properties on the selected extension node.
	 * 
	 * @param propertyTypeNode
	 * @return
	 */
	private boolean isInheritedPropertyType(ComponentNode propertyTypeNode) {
		// Object selectedPropertyType = propertyTypeNode.getTLModelObject();
		// List<PropertyNode> extensionProperties = new ArrayList<PropertyNode>();
		boolean isInheritedType = false;
		//
		// getPropertyNodes(selectedExtensionNode, true, extensionProperties);
		//
		// for (PropertyNode propertyNode : extensionProperties) {
		// Object tlProperty = propertyNode.getTLModelObject();
		//
		// if (tlProperty instanceof TLProperty) {
		// if (selectedPropertyType == ((TLProperty) tlProperty).getType()) {
		// isInheritedType = true;
		// }
		// }
		// }
		return isInheritedType;
	}

	/**
	 * find the owning facet even if starting from an inherited attribute or element
	 * 
	 * Attempts to locate a facet on the node that is currently being edited by the user (the one for whom an extension
	 * is being chosen). This is necessary because the given property node may belong to the base type that is being
	 * extended.
	 * 
	 * @param propertyNode
	 *            the property for which to return a current node facet
	 * @return FacetNode
	 */
	private FacetOMNode getCurrentNodeFacet(PropertyNode propertyNode) {
		// TLFacet currentNodeFacet = null;
		// if (curNode.getModelObject().getTLModelObj() instanceof TLFacetOwner) {
		// TLFacetOwner currentNodeTLObj = (TLFacetOwner) curNode.getModelObject().getTLModelObj();
		// TLFacet propertyOwningFacet = propertyFacetOwners.get(propertyNode);
		// currentNodeFacet = FacetCodegenUtils.getFacetOfType(currentNodeTLObj, propertyOwningFacet.getFacetType(),
		// propertyOwningFacet.getContext(), propertyOwningFacet.getLabel());
		// }
		FacetOMNode facetNode = null;
		//
		// if (currentNodeFacet != null) {
		// for (INode childNode : curNode.getChildren()) {
		// Object childTLObj = childNode.getModelObject().getTLModelObj();
		//
		// if ((childNode instanceof FacetNode) && (childTLObj == currentNodeFacet)) {
		// facetNode = (FacetNode) childNode;
		// break;
		// }
		// }
		// } else
		// LOGGER.warn("get current node facet could not find the facet. curNode tl obj = "
		// + curNode.getModelObject().getTLModelObj());
		return facetNode;
	}

	//
	@Override
	public boolean notifyTypeSelected(Node selectedExtension) {
		// // Clear the state of the visual controls
		// disposeSelectionControls();
		this.extensionProperties.clear();
		// this.propertyFacetOwners.clear();
		// this.inheritanceOptions.clear();
		// this.originalOptions.clear();
		// this.selectedOptions.clear();
		//
		// // Construct the data structures required to populate the tables
		// if ((selectedExtension != null)
		// // && !(selectedExtension instanceof ExtensionPointNode)) {
		// // && !selectedExtension.getModelObject().isExtensionPointFacet()) {
		// && !(selectedExtension.getModelObject() instanceof ExtensionPointFacetMO)) {
		// List<PropertyNode> propertyNodes = getExtensionProperties(selectedExtension);
		//
		// for (PropertyNode propertyNode : propertyNodes) {
		// List<ComponentNode> inheritanceHierarchy = getInheritanceOptions(propertyNode);
		//
		// if ((inheritanceHierarchy != null) && (inheritanceHierarchy.size() > 1)) {
		// ComponentNode selectedOption = getInitiallySelectedOption(propertyNode, inheritanceHierarchy);
		//
		// extensionProperties.add(propertyNode);
		// propertyFacetOwners.put(propertyNode, getFacetOwner(propertyNode));
		// inheritanceOptions.put(propertyNode, inheritanceHierarchy);
		// originalOptions.put(propertyNode, selectedOption);
		// selectedOptions.put(propertyNode, selectedOption);
		// }
		// }
		// this.selectedExtensionNode = selectedExtension;
		// this.selectedExtensionTLObj = selectedExtension.getModelObject().getTLModelObj();
		// }
		//
		// // Refresh the visual controls to reflect the new inheritance options
		// fieldTable.refresh();
		// if (!extensionProperties.isEmpty()) {
		// PropertyNode fieldTableSelection = (PropertyNode) fieldTable.getTable().getItem(0).getData();
		//
		// fieldTable.getTable().setSelection(0);
		// fieldSelectionTable.setInput(fieldTableSelection);
		// }
		// instrumentSelectionTable();
		// fieldSelectionTable.refresh();
		// refreshTreeView();
		//
		// Do not allow navigation to this page if there is nothing for the user to do
		return !extensionProperties.isEmpty();
	}

	private List<PropertyNode> getExtensionProperties(Node extensionNode) {
		// List<PropertyNode> declaredProperties = new ArrayList<PropertyNode>();
		// List<PropertyNode> inheritedProperties = new ArrayList<PropertyNode>();
		// List<Object> inheritanceHierarchies = new ArrayList<Object>();
		List<PropertyNode> extensionProperties = new ArrayList<>();
		//
		// getPropertyNodes(curNode, false, declaredProperties);
		// getPropertyNodes(extensionNode, true, inheritedProperties);
		//
		// for (PropertyNode node : declaredProperties) {
		// Object inheritanceHierarchy = getInheritanceHierarchy(node);
		//
		// if (inheritanceHierarchy != null) {
		// extensionProperties.add(node);
		// inheritanceHierarchies.add(inheritanceHierarchy);
		// }
		// }
		// for (PropertyNode node : inheritedProperties) {
		// Object inheritanceHierarchy = getInheritanceHierarchy(node);
		//
		// if ((inheritanceHierarchy != null) && !inheritanceHierarchies.contains(inheritanceHierarchy)) {
		// extensionProperties.add(node);
		// inheritanceHierarchies.add(inheritanceHierarchy);
		// }
		// }
		return extensionProperties;
	}

	private TLFacet getFacetOwner(PropertyNode propertyNode) {
		// Object tlProperty = propertyNode.getModelObject().getTLModelObj();
		Object tlProperty = propertyNode.getTLModelObject();
		TLFacet owningFacet = null;

		if (tlProperty instanceof TLProperty) {
			TLPropertyOwner propOwner = ((TLProperty) tlProperty).getOwner();

			if (propOwner instanceof TLFacet) {
				owningFacet = (TLFacet) propOwner;
			}
		}
		return owningFacet;
	}

	private void getPropertyNodes(Node node, boolean includeInheritedNodes, List<PropertyNode> propertyNodes) {
		if (node instanceof PropertyNode) {
			propertyNodes.add((PropertyNode) node);

		} else {
			if (includeInheritedNodes) {
				for (Node child : node.getInheritedChildren()) {
					getPropertyNodes(child, includeInheritedNodes, propertyNodes);
				}
			}
			for (Node child : node.getChildren()) {
				getPropertyNodes(child, includeInheritedNodes, propertyNodes);
			}
		}
	}

	private Object getInheritanceHierarchy(PropertyNode node) {
		// Object propertyTLObj = node.getModelObject().getTLModelObj();
		Object propertyTLObj = node.getTLModelObject();
		Object hierarchy = null;

		if (propertyTLObj instanceof TLProperty) {
			TLPropertyType propertyType = ((TLProperty) propertyTLObj).getType();

			if (propertyType != null) {
				hierarchy = inheritanceHierarchyInfo.get(propertyType);
			}
		}
		return hierarchy;
	}

	private List<ComponentNode> getInheritanceOptions(PropertyNode propertyNode) {
		List<ComponentNode> inheritanceOptions = null;

		// if (propertyNode.getPropertyType() == PropertyNodeType.ELEMENT) {
		// TLProperty tlProperty = (TLProperty) propertyNode.getTLModelObject();
		// // TLProperty tlProperty = (TLProperty) propertyNode.getModelObject().getTLModelObj();
		// List<NamedEntity> tlHierarchyOptions = inheritanceHierarchyInfo.get(tlProperty.getType());
		//
		// if ((tlHierarchyOptions != null) && !tlHierarchyOptions.isEmpty()) {
		// inheritanceOptions = new ArrayList<ComponentNode>();
		//
		// for (NamedEntity tlOption : tlHierarchyOptions) {
		// inheritanceOptions.add(NodeFactory.newChild(null, (TLModelElement) tlOption));
		// // inheritanceOptions.add(new ComponentNode((TLModelElement) tlOption));
		// }
		// }
		// }
		return inheritanceOptions;
	}

	private ComponentNode getInitiallySelectedOption(PropertyNode propertyNode,
			List<ComponentNode> inheritanceHierarchy) {
		TLProperty tlProperty = (TLProperty) propertyNode.getTLModelObject();
		// TLProperty tlProperty = (TLProperty) propertyNode.getModelObject().getTLModelObj();
		// NamedEntity propertyType = tlProperty.getType();
		ComponentNode selectedNode = null;

		// if (!inheritanceHierarchy.isEmpty()) {
		// for (ComponentNode hierarchyNode : inheritanceHierarchy) {
		// Object tlHierarchyMember = hierarchyNode.getTLModelObject();
		//
		// if (tlHierarchyMember == propertyType) {
		// selectedNode = hierarchyNode;
		// break;
		// }
		// }
		// }
		return selectedNode;
	}

	@Override
	public void createControl(final Composite parent) {
		// SashForm sashForm = new SashForm(parent, SWT.HORIZONTAL);
		// sashForm.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));
		// Label lbl;
		//
		// SashForm leftPanel = new SashForm(sashForm, SWT.VERTICAL);
		// Composite rightPanel = new Composite(sashForm, SWT.NONE);
		// rightPanel.setLayout(new FillLayout());
		//
		// Composite leftTopPanel = new Composite(leftPanel, SWT.NONE);
		// Composite leftBottomPanel = new Composite(leftPanel, SWT.NONE);
		// leftTopPanel
		// .setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));
		// leftTopPanel.setLayout(new GridLayout(1, false));
		// leftBottomPanel.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL
		// | GridData.GRAB_VERTICAL));
		// leftBottomPanel.setLayout(new GridLayout(1, false));
		//
		// lbl = new Label(leftTopPanel, SWT.LEFT);
		// lbl.setText("For each of the following properties...");
		// fieldTable = new TableViewer(leftTopPanel, SWT.BORDER | SWT.FULL_SELECTION | SWT.SINGLE);
		// Table fieldTbl = fieldTable.getTable();
		// fieldTbl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL));
		// fieldTbl.setHeaderVisible(false);
		// fieldTbl.setLinesVisible(false);
		// fieldTable.setContentProvider(new FieldTableContentProvider());
		// fieldTable.setLabelProvider(new FieldTableLabelProvider());
		// fieldTable.setSorter(new LibrarySorter());
		// fieldTable.addSelectionChangedListener(new ISelectionChangedListener() {
		// @Override
		// public void selectionChanged(SelectionChangedEvent event) {
		// handleFieldSelectionChanged();
		// }
		// });
		// fieldTable.setInput("DUMMY_INPUT");
		//
		// new Label(leftBottomPanel, SWT.NONE); // blank space
		// lbl = new Label(leftBottomPanel, SWT.LEFT);
		// lbl.setText("...select a member of its inheritance hierarchy");
		// fieldSelectionTable = new TableViewer(leftBottomPanel, SWT.BORDER | SWT.FULL_SELECTION | SWT.SINGLE);
		// Table fieldSelectionTbl = fieldSelectionTable.getTable();
		// new TableColumn(fieldSelectionTbl, SWT.CENTER);
		// new TableColumn(fieldSelectionTbl, SWT.LEFT);
		// fieldSelectionTbl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL));
		// fieldSelectionTbl.setHeaderVisible(false);
		// fieldSelectionTbl.setLinesVisible(false);
		// fieldSelectionTable.setContentProvider(new FieldSelectionTableContentProvider());
		// fieldSelectionTable.setLabelProvider(new FieldSelectionTableLabelProvider());
		// fieldSelectionTable.setSorter(new LibrarySorter());
		//
		// treeViewer = new TreeViewer(rightPanel);
		// treeViewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		//
		// treeViewer.setContentProvider(new TreeViewContentProvider());
		// treeViewer.setLabelProvider(new LibraryTreeLabelProvider());
		// treeViewer.setSorter(new LibrarySorter());
		// treeViewer.setInput(TREE_INPUT);
		//
		// setControl(sashForm); // Required to avoid an error in the system
		// setPageComplete(true);
	}

	private void handleFieldSelectionChanged() {
		// TableItem[] selectedItem = fieldTable.getTable().getSelection();
		// PropertyNode selectedProperty = (selectedItem.length == 0) ? null : (PropertyNode) selectedItem[0].getData();
		//
		// disposeSelectionControls();
		// fieldSelectionTable.setInput(selectedProperty);
		// fieldSelectionTable.refresh();
		// instrumentSelectionTable();
	}

	private void handleInheritanceOptionChanged(ComponentNode selectedNode) {
		// if (selectedNode != null) {
		// selectedOptions.put((PropertyNode) fieldSelectionTable.getInput(), selectedNode);
		// refreshTreeView();
		// }
	}

	/**
	 * Initializes the node structure for trees to be displayed within this wizard page. This must be done differently
	 * than the normal tree views because this page's view is designed to show a "what-if" scenario based upon the
	 * user's current selections.
	 */
	private void initTreeNode() {
		// if (curNode != null) {
		// // ModelObject<?> modelObject = curNode.getModelObject();
		//
		// if (curNode.getTLModelObject() instanceof TLModelElement) {
		// // 3/26/13 - may need to be changed to fix the 2nd extension wizard page.
		// // treeNode = new ComponentNode((TLModelElement) modelObject.getTLModelObj());
		// if (curNode.getTLModelObject() instanceof TLLibraryMember) {
		// treeNode = (ComponentNode) NodeFactory.newLibraryMember((TLLibraryMember) curNode.cloneTLObj());
		// createTreeNodeChildren(treeNode);
		// }
		// }
		// }
	}

	/**
	 * Runs at wizard startup. Recursive method that creates and links the children of the given node down to and
	 * including the properties-level of the tree.
	 * 
	 * @param node
	 *            the node whose children are to be populated
	 */
	private void createTreeNodeChildren(ComponentNode node) {
		// if (node.getTLModelObject() instanceof TLFacet) {
		// // Replace the default list of properties using information from the user's selections
		// TLFacet facet = (TLFacet) node.getTLModelObject();
		// TLFacet baseFacet = (selectedExtensionTLObj == null) ? null : FacetCodegenUtils.getFacetOfType(
		// (TLFacetOwner) selectedExtensionTLObj, facet.getFacetType(), facet.getContext(), facet.getLabel());
		//
		// node.getChildren().clear();
		// node.getInheritedChildren().clear();
		//
		// // Start by adding the declared and inherited attributes &
		// // indicators (no special inheritance rules)
		// for (TLAttribute attribute : facet.getAttributes()) {
		// NodeFactory.newChild(node, attribute);
		// }
		// for (TLIndicator indicator : facet.getIndicators()) {
		// NodeFactory.newChild(node, indicator);
		// }
		// if (baseFacet != null) {
		// for (TLAttribute attribute : PropertyCodegenUtils.getInheritedFacetAttributes(baseFacet)) {
		// NodeFactory.newChild(node, attribute);
		// }
		// for (TLIndicator indicator : PropertyCodegenUtils.getInheritedFacetIndicators(baseFacet)) {
		// NodeFactory.newChild(node, indicator);
		// }
		// }
		//
		// // Add declared and inherited properties, accounting for inheritance
		// // rules based on the
		// // user's
		// // selections.
		// Set<Object> inheritanceHierarchies = new HashSet<Object>();
		//
		// for (TLProperty property : facet.getElements()) {
		// TLPropertyType propertyType = property.getType();
		// Object inheritanceHierarchy = inheritanceHierarchyInfo.get(propertyType);
		//
		// if (inheritanceHierarchy != null) {
		// TLPropertyType userSelection = getUserSelectedPropertyType(property);
		//
		// // Only use the declared property if it matches the current
		// // user selection
		// if (userSelection != null) {
		// if (userSelection == propertyType) {
		// node.linkChild(NodeFactory.newChild(node, property));
		// } else {
		// newTreePropertyNode(userSelection, node);
		// }
		// inheritanceHierarchies.add(inheritanceHierarchy);
		// } else {
		// node.linkChild(NodeFactory.newChild(node, property));
		// }
		// } else {
		// node.linkChild(NodeFactory.newChild(node, property));
		// }
		// }
		// if (baseFacet != null) {
		// for (TLProperty property : PropertyCodegenUtils.getInheritedFacetProperties(baseFacet)) {
		// TLPropertyType propertyType = property.getType();
		// Object inheritanceHierarchy = inheritanceHierarchyInfo.get(propertyType);
		//
		// if ((inheritanceHierarchy == null) || !inheritanceHierarchies.contains(inheritanceHierarchy)) {
		// if (inheritanceHierarchy != null) {
		// TLPropertyType userSelection = getUserSelectedPropertyType(property);
		//
		// if (userSelection != null) {
		// newTreePropertyNode(userSelection, node);
		// } else {
		// node.linkChild(NodeFactory.newChild(node, property));
		// }
		// inheritanceHierarchies.add(inheritanceHierarchy);
		// } else {
		// node.linkChild(NodeFactory.newChild(node, property));
		// }
		// }
		// }
		// }
		// } else {
		// if (node.getChildren() != null) {
		// for (Object childTLObj : node.getChildrenHandler().getChildren_TL()) {
		// ComponentNode childNode = NodeFactory.newChild(node, (TLModelElement) childTLObj);
		//
		// // node.linkChild(childNode);
		// createTreeNodeChildren(childNode);
		// }
		// }
		// // All non-TLFacet children constructed using the model object
		// // children
		// // ModelObject<?> modelObject = node.getModelObject();
		// //
		// // if (modelObject.getChildren() != null) {
		// // for (Object childTLObj : modelObject.getChildren()) {
		// // ComponentNode childNode = NodeFactory.newChild(node, (TLModelElement) childTLObj);
		// //
		// // node.linkChild(childNode);
		// // createTreeNodeChildren(childNode);
		// // }
		// // }
		// }
	}

	private TLPropertyType getUserSelectedPropertyType(TLProperty property) {
		TLPropertyType propertyType = null;

		// for (PropertyNode propertyNode : selectedOptions.keySet()) {
		// if (propertyNode.getTLModelObject() == property) {
		// ComponentNode typeNode = selectedOptions.get(propertyNode);
		//
		// if (typeNode != null) {
		// Object tlType = typeNode.getTLModelObject();
		//
		// if (tlType instanceof TLPropertyType) {
		// propertyType = (TLPropertyType) tlType;
		// break;
		// }
		// }
		// }
		// }
		return propertyType;
	}

	private PropertyNode newTreePropertyNode(TLPropertyType propertyType, ComponentNode parentNode) {
		// TLProperty tlProperty = new TLProperty();
		PropertyNode propertyNode = null;
		// ComponentNode propertyTypeNode;
		//
		// tlProperty.setName(propertyType.getLocalName());
		// tlProperty.setType(propertyType);
		// propertyNode = (PropertyNode) NodeFactory.newChild(parentNode, tlProperty);
		// propertyTypeNode = NodeFactory.newChild(parentNode, (TLModelElement) propertyType);
		// // parentNode.linkChild(propertyNode, false);
		// propertyNode.linkChild(propertyTypeNode);
		return propertyNode;
	}

	private void refreshTreeView() {
		// treeNode = null;
		// treeViewer.refresh();
		// treeViewer.expandToLevel(3);
	}

	private void instrumentSelectionTable() {
		synchronized (fieldSelectionButtons) {
			// if (!fieldSelectionButtons.isEmpty()) {
			// throw new IllegalStateException("Field selection radio buttons already initialized.");
			// }
			// INode selectedNode = selectedOptions.get(fieldSelectionTable.getInput());
			// Table selectionTable = fieldSelectionTable.getTable();
			//
			// for (TableItem item : selectionTable.getItems()) {
			// TableEditor editor = new TableEditor(selectionTable);
			// Button radioButton = new Button(selectionTable, SWT.RADIO);
			//
			// radioButton.setSelection((item.getData() == selectedNode));
			// radioButton.addSelectionListener(new SelectionListener() {
			// @Override
			// public void widgetSelected(SelectionEvent e) {
			// if (((Button) e.widget).getSelection()) {
			// handleInheritanceOptionChanged((ComponentNode) e.widget.getData());
			// }
			// }
			//
			// @Override
			// public void widgetDefaultSelected(SelectionEvent e) {
			// }
			// });
			// radioButton.setData(item.getData());
			// radioButton.pack();
			//
			// editor.minimumWidth = radioButton.getSize().x;
			// editor.minimumHeight = radioButton.getSize().y;
			// editor.horizontalAlignment = SWT.CENTER;
			// editor.setEditor(radioButton, item, 0);
			// fieldSelectionButtons.add(editor);
			// }
			// for (TableColumn column : selectionTable.getColumns()) {
			// column.pack();
			// }
		}
	}

	private void disposeSelectionControls() {
		synchronized (fieldSelectionButtons) {
			// for (TableEditor editor : fieldSelectionButtons) {
			// editor.getEditor().dispose();
			// editor.dispose();
			// }
			// fieldSelectionButtons.clear();
		}
	}

	@Override
	public void dispose() {
		// disposeSelectionControls();
		// super.dispose();
	}

	/** Provides the conent for the field table. */
	private class FieldTableContentProvider implements IStructuredContentProvider {

		/**
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
		 */
		@Override
		public Object[] getElements(Object inputElement) {
			return extensionProperties.toArray();
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		@Override
		public void dispose() {
		}

	}

	/**
	 * Removes the font/color formatting from the default application label provider.
	 */
	private class FieldTableLabelProvider extends LibraryTreeLabelProvider {

		@Override
		public Font getFont(Object element) {
			return null;
		}

		@Override
		public Color getForeground(Object element) {
			return null;
		}

	}

	/** Provides the conent for the field selection table. */
	private class FieldSelectionTableContentProvider implements IStructuredContentProvider {

		@Override
		public Object[] getElements(Object inputElement) {
			List<ComponentNode> options = inheritanceOptions.get(inputElement);
			return (options == null) ? new Object[0] : options.toArray();
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		@Override
		public void dispose() {
		}

	}

	/**
	 * Removes the font/color formatting from the default application label provider.
	 */
	private class FieldSelectionTableLabelProvider extends FieldTableLabelProvider implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return (columnIndex == 1) ? super.getImage(element) : null;
		}

		/**
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
		 */
		@Override
		public String getColumnText(Object element, int columnIndex) {
			return (columnIndex == 1) ? super.getText(element) : null;
		}

	}

	/**
	 * Content provider that populates the tree with nodes based on the selections made by the user. This is essentially
	 * a "what-if" that allows the user to browse the type/properties tree of the current node before actually
	 * committing to the current selections.
	 */
	private class TreeViewContentProvider implements ITreeContentProvider {

		@Override
		public Object[] getElements(final Object element) {
			return getChildren(element);
		}

		@Override
		public Object[] getChildren(final Object element) {
			if (treeNode == null) {
				initTreeNode();
			}
			List<Node> nodeChildren = new ArrayList<>();

			// TODO - 3/26/13 dmh - make sure these changes didn't break the 2nd extension page
			// before deleting them.
			if (element == TREE_INPUT) {
				nodeChildren.add(treeNode);

			} else {
				Node node = (Node) element;

				if (node instanceof PropertyNode) {
					final PropertyNode prop = (PropertyNode) node;
					NamedEntity elem = null;
					if (prop instanceof TypedPropertyNode)
						elem = ((TypedPropertyNode) prop).getAssignedTLNamedEntity();
					// final NamedEntity elem = prop.getModelObject().getTLType();

					if (elem != null)
						return new Object[] { NodeFactory.newLibraryMember((LibraryMember) elem) };

					// if (elem != null) {
					// if (elem instanceof TLLibraryMember) {
					// return new Object[] { NodeFactory.newLibraryMember((TLLibraryMember) elem) };
					// // return new Object[] { ComponentNode.newCN((LibraryMember) elem) };
					// }
					// if (elem instanceof TLFacet) {
					// // return new Object[] { new ComponentNode((TLFacet) elem) };
					// return new Object[] { new FacetOMNode((TLFacet) elem) };
					// }
					// }
				} else {
					// nodeChildren.addAll(node.getNavChildrenWithProperties());
					nodeChildren.addAll(node.getChildren());
					nodeChildren.addAll(node.getInheritedChildren());
				}
			}
			return nodeChildren.toArray();
		}

		@Override
		public boolean hasChildren(final Object element) {
			Object[] children = getChildren(element);
			return (children != null) && (children.length > 0);
		}

		@Override
		public Object getParent(final Object element) {
			return (element == TREE_INPUT) ? null : ((INode) element).getParent();
		}

		@Override
		public void dispose() {
		}

		@Override
		public void inputChanged(final Viewer viewer, final Object old_input, final Object new_input) {
		}

	}

}
