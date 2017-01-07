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
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.eclipse.jface.viewers.TableViewer;
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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemas.node.BusinessObjectNode;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.CoreObjectNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.Node.NodeVisitor;
import org.opentravel.schemas.node.NodeVisitors;
import org.opentravel.schemas.node.SubType;
import org.opentravel.schemas.node.VWA_Node;
import org.opentravel.schemas.node.facets.FacetNode;
import org.opentravel.schemas.node.facets.SimpleFacetNode;
import org.opentravel.schemas.node.interfaces.ComplexComponentInterface;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.stl2developer.ColorProvider;
import org.opentravel.schemas.stl2developer.DialogUserNotifier;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeUser;
import org.opentravel.schemas.widgets.LibraryTablePoster;
import org.opentravel.schemas.widgets.WidgetFactory;
import org.opentravel.schemas.wizards.ChangeWizard.ExtentedTLFacetType;
import org.opentravel.schemas.wizards.validators.FormValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Change Node Wizard page controller. All changes are made to the passed edited node. When the type of object is
 * changed, the editNode becomes the newly created object and the old edit node is put into history.
 * 
 * History is used to maintain a stack of changes. Changes are pushed onto the stack, can are popped off the stack one
 * at a time for revert or all together for cancel.
 * <p>
 * On OK button, the edit node is complete. On cancel, edit node is restored from history stack.
 * 
 * @author Agnieszka Janowska
 * 
 */
public class ChangeWizardPage extends WizardPage {
	private static final Logger LOGGER = LoggerFactory.getLogger(ChangeWizardPage.class);

	private Text nameText;
	private Combo libraryCombo;
	private Table previewTable;
	private Button undoButton;

	private LibraryTablePoster tablePoster;

	private ComponentNode editedNode;
	private final List<SubType> allowedObjectTypes;
	private final List<ExtentedTLFacetType> allowedFacetTypes;

	private Map<ExtentedTLFacetType, Button> facetTypeButtons;
	private Map<SubType, Button> objectTypeButtons;
	private Map<String, LibraryNode> libraryNameMap;

	private final Stack<HistoryItem> history = new Stack<HistoryItem>();

	/**
	 * flag to prevent selection already selected radio button
	 */
	protected Button facetRadioButton;

	private class HistoryItem {
		private final OpType opType;
		private Node previousNode;
		private INode newNode;
		private INode tempNode;

		public HistoryItem(final OpType opType, final Node previousNode, final INode newNode) {
			this(opType, previousNode, newNode, null);
		}

		public HistoryItem(final OpType opType, final Node previousNode, final INode newNode, final INode tempNode) {
			super();
			this.opType = opType;
			this.previousNode = previousNode;
			this.newNode = newNode;
			this.tempNode = tempNode;
		}
	}

	private enum OpType {
		LIB_CHANGE,
		OBJECT_TYPE_CHANGE,
		OWNING_FACET_CHANGE,
		OWNING_FACET_CHANGE_TO_SIMPLE,
		OWNING_FACET_CHANGE_FROM_SIMPLE;
	}

	protected ChangeWizardPage(final String pageName, final String title, final FormValidator validator,
			final ComponentNode editedNode, final List<SubType> allowedObjectTypes,
			final List<ExtentedTLFacetType> allowedFacetTypes) {
		super(pageName, title, null);
		this.editedNode = editedNode;
		this.allowedObjectTypes = allowedObjectTypes;
		this.allowedFacetTypes = allowedFacetTypes;
		// TODO - make sure EditedNode is not null
	}

	@Override
	public void createControl(final Composite parent) {
		parent.setLayout(new GridLayout());
		final GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		final GridData containerGD = new GridData();
		containerGD.horizontalAlignment = SWT.FILL;
		containerGD.verticalAlignment = SWT.FILL;
		containerGD.grabExcessHorizontalSpace = true;
		containerGD.grabExcessVerticalSpace = true;
		containerGD.widthHint = 600;
		containerGD.heightHint = 800;

		final Composite container = new Composite(parent, SWT.BORDER);// parent;
		container.setLayout(layout);
		container.setLayoutData(containerGD);

		final GridData generalGD = new GridData();
		generalGD.horizontalSpan = 1;
		generalGD.horizontalAlignment = SWT.FILL;
		generalGD.grabExcessHorizontalSpace = true;

		final GridData twoColumnsSpanGD = new GridData();
		twoColumnsSpanGD.horizontalSpan = 2;
		twoColumnsSpanGD.horizontalAlignment = SWT.FILL;
		twoColumnsSpanGD.grabExcessHorizontalSpace = true;

		final GridData tableGD = new GridData();
		tableGD.horizontalSpan = 2;
		tableGD.horizontalAlignment = SWT.FILL;
		tableGD.verticalAlignment = SWT.FILL;
		tableGD.grabExcessHorizontalSpace = true;
		tableGD.grabExcessVerticalSpace = true;

		final GridData rightGD = new GridData();
		rightGD.horizontalSpan = 1;
		rightGD.horizontalAlignment = SWT.RIGHT;
		rightGD.grabExcessHorizontalSpace = true;

		final GridData leftGD = new GridData();
		leftGD.horizontalSpan = 1;
		leftGD.horizontalAlignment = SWT.LEFT;

		final Label propertyLabel = new Label(container, SWT.NONE);
		propertyLabel.setText("Name:");
		nameText = WidgetFactory.createText(container, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
		nameText.setText(editedNode.getName());
		nameText.setLayoutData(generalGD);
		nameText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(final ModifyEvent e) {
				setNodeName(nameText.getText());
				validate();
			}

		});

		final Label libraryLabel = new Label(container, SWT.NONE);
		libraryLabel.setText("Containing Library:");
		libraryCombo = WidgetFactory.createCombo(container, SWT.READ_ONLY);
		libraryCombo.setLayoutData(generalGD);
		// int index = 0;
		libraryNameMap = new HashMap<String, LibraryNode>();
		for (final Node lib : Node.getAllLibraries()) {
			if (lib.isTLLibrary()) {
				final LibraryNode library = (LibraryNode) lib;
				final String libDisplayName = getLibraryString(library);
				libraryNameMap.put(libDisplayName, library);
				libraryCombo.add(libDisplayName);
			}
		}
		libraryCombo.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(final ModifyEvent e) {
				final String selected = libraryCombo.getText();
				if (selected != null && !selected.isEmpty()) {
					setLibrary(selected);
					validate();
				}
			}
		});

		final Label typeLabel = new Label(container, SWT.NONE);
		typeLabel.setText("Type of Object:");
		final Composite objectButtonsComposite = createObjectTypeRadios(container);
		objectButtonsComposite.setLayoutData(generalGD);

		final Label facetLabel = new Label(container, SWT.NONE);
		facetLabel.setText("Owning facet:");
		final Composite facetButtonsComposite = createFacetTypeRadios(container);
		facetButtonsComposite.setLayoutData(generalGD);

		final Label separator = new Label(container, SWT.NONE);
		separator.setLayoutData(twoColumnsSpanGD);

		final Label previewLabel = new Label(container, SWT.NONE);
		previewLabel.setText("Preview (read-only):");
		previewLabel.setLayoutData(leftGD);
		undoButton = new Button(container, SWT.PUSH);
		undoButton.setText("Revert");
		undoButton.setLayoutData(rightGD);
		undoButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				undoLastOp();
			}

			@Override
			public void widgetDefaultSelected(final SelectionEvent e) {
			}
		});
		previewTable = createPreviewTable(container);
		previewTable.setLayoutData(tableGD);
		previewTable.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				updateFacetTypeButtons();
			}

			@Override
			public void widgetDefaultSelected(final SelectionEvent e) {
			}

		});
		tablePoster = new LibraryTablePoster(previewTable, new ColorProvider(parent.getDisplay()));

		updateView();
		setControl(container);
		setPageComplete(true);
	}

	/**
	 * Set the type of object. The editNode becomes the newly created object and the current node is put into history.
	 */
	private void setObjectType(final SubType st) {
		final HistoryItem item = new HistoryItem(OpType.OBJECT_TYPE_CHANGE, editedNode, null);
		historyPush(item);
		try {
			editedNode = editedNode.changeObject(st);
			tablePoster.postTable(editedNode);
			updateFacetTypeButtons();
		} catch (Exception ex) {
			undoLastOp();
			LOGGER.warn("Error on chaning type to: " + st.toString());
			DialogUserNotifier.openError("Error", "Operation finished with error. Check logs for more information.");
		}
	}

	private String getLibraryString(final LibraryNode library) {
		return library.getPrefix() + ":" + library.getName();
	}

	public void undoAllOperation() {
		while (history.size() > 0) {
			undoLastOp();
		}
	}

	private void undoLastOp() {
		if (history.size() > 0) {
			final HistoryItem item = historyPop();
			switch (item.opType) {
			case LIB_CHANGE:
				editedNode.setLibrary((LibraryNode) item.previousNode);
				break;
			case OBJECT_TYPE_CHANGE:
				editedNode.replaceWith(item.previousNode);
				editedNode = (ComponentNode) item.previousNode;
				break;
			case OWNING_FACET_CHANGE:
				final PropertyNode property = (PropertyNode) item.newNode;
				if (item.previousNode instanceof FacetNode) {
					property.moveProperty((FacetNode) item.previousNode);
				}
				break;
			case OWNING_FACET_CHANGE_TO_SIMPLE:
				// Reinstate the newNode on the previous node facet from where it came
				((FacetNode) item.previousNode).addProperty((Node) item.newNode);
				// Restore the simple node from the tempNode
				ComponentNode simpleFacet = ((ComponentNode) item.previousNode.getParent()).getSimpleFacet();
				simpleFacet.removeProperty(simpleFacet.getChildren().get(0));
				simpleFacet.addProperty((Node) item.tempNode); // TEST
				break;
			case OWNING_FACET_CHANGE_FROM_SIMPLE:
				final PropertyNode orig2 = (PropertyNode) item.previousNode;
				final PropertyNode property2 = (PropertyNode) item.newNode;
				((ComponentNode) property2.getParent()).removeProperty(property2);
				resetSimple(orig2);
				mergeToSimple(property2, orig2);
				break;
			}
		}
		updateView();
	}

	/**
	 * *****************************************************************
	 * 
	 */
	private void historyPush(final HistoryItem item) {
		history.push(item);
		updateUndoButton();
	}

	private HistoryItem historyPop() {
		final HistoryItem item = history.pop();
		updateUndoButton();
		return item;
	}

	protected void historyClear() {
		for (HistoryItem item : history) {
			// Need to delete them to remove them from where used type lists.
			// Change facets does not need delete.
			if (item.opType.equals(OpType.OBJECT_TYPE_CHANGE)) {
				// Use the visitor because without a library it will not be delete-able.
				NodeVisitor visitor = new NodeVisitors().new deleteVisitor();
				item.previousNode.visitAllNodes(visitor);
				// item.previousNode.delete();
			}
		}
		history.removeAllElements();
	}

	private Table createPreviewTable(final Composite container) {
		final TableViewer viewer = new TableViewer(container, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION | SWT.BORDER);
		final Table table = viewer.getTable();

		return table;
	}

	private void updateFacetTypeButtons() {
		enableFacetTypeButtons();
		final PropertyNode selected = getSelectedProperty();
		TLFacetType facetType = null;
		if (selected != null) {
			final INode parent = selected.getParent();
			if (parent instanceof FacetNode) {
				facetType = ((ComponentNode) parent).getFacetType();
			}
		}
		// if there is no selected property all the radio buttons will be unselected
		for (final ExtentedTLFacetType ft : facetTypeButtons.keySet()) {
			if (ft.equals(facetType)) {
				markSelectedFacetTypeButton(facetTypeButtons.get(ft));
			} else {
				facetTypeButtons.get(ft).setSelection(false);
			}
		}
	}

	private void markSelectedFacetTypeButton(Button button) {
		if (!button.getSelection()) {
			button.setSelection(true);
		}
		facetRadioButton = button;
	}

	private void enableFacetTypeButtons() {
		for (final Button b : facetTypeButtons.values()) {
			b.setEnabled(false);
		}
		if (editedNode != null) {
			for (final INode n : editedNode.getChildren()) {
				if (n instanceof FacetNode) {
					final ComponentNode facet = (ComponentNode) n;
					final Button b = facetTypeButtons.get(ExtentedTLFacetType.valueOf(facet));
					if (b != null) {
						b.setEnabled(true);
					}
				}
			}
		}
	}

	private void updateObjectTypeButtons() {
		if (editedNode instanceof BusinessObjectNode) {
			if (objectTypeButtons.containsKey(SubType.BUSINESS_OBJECT)) {
				objectTypeButtons.get(SubType.BUSINESS_OBJECT).setSelection(true);
			}
		} else if (editedNode instanceof CoreObjectNode) {
			if (objectTypeButtons.containsKey(SubType.CORE_OBJECT)) {
				objectTypeButtons.get(SubType.CORE_OBJECT).setSelection(true);
			}
		} else if (editedNode instanceof VWA_Node) {
			if (objectTypeButtons.containsKey(SubType.VALUE_WITH_ATTRS)) {
				objectTypeButtons.get(SubType.VALUE_WITH_ATTRS).setSelection(true);
			}
		}
	}

	private void setLibrary(final String selected) {
		final LibraryNode selectedLib = libraryNameMap.get(selected);
		if (!editedNode.getLibrary().equals(selectedLib)) {
			final HistoryItem item = new HistoryItem(OpType.LIB_CHANGE, editedNode.getLibrary(), selectedLib);
			historyPush(item);
			editedNode.setLibrary(selectedLib);
		}
	}

	// TODO - use Node facet types or FacetTypeNode methods, not the TLFacet. This is too
	// tightly coupled to the TL model for a GUI operation.
	private Composite createFacetTypeRadios(final Composite c) {
		final Composite container = new Composite(c, SWT.NULL);
		final GridLayout gl = new GridLayout();
		gl.numColumns = allowedFacetTypes.size();
		container.setLayout(gl);

		facetTypeButtons = new EnumMap<ExtentedTLFacetType, Button>(ExtentedTLFacetType.class);
		for (final ExtentedTLFacetType st : allowedFacetTypes) {
			final Button radioButton = new Button(container, SWT.RADIO);

			radioButton.setText(st.getIdentityName());
			radioButton.addListener(SWT.Selection, new Listener() {

				@Override
				public void handleEvent(final Event event) {
					final Button button = facetTypeButtons.get(st);
					if (button != null && button.getSelection() && facetRadioButton != button) {
						setOwningFacet(st);
						markSelectedFacetTypeButton(button);
					}
				}

			});
			facetTypeButtons.put(st, radioButton);
		}
		return container;
	}

	/**
	 * Change the selected properties to be owned by a different facet.
	 * 
	 * @param st
	 */
	private void setOwningFacet(final ExtentedTLFacetType st) {
		final List<PropertyNode> properties = getSelectedProperties();

		ComponentNode owningFacet = null;
		for (final PropertyNode property : properties) {
			if (property.getParent() instanceof FacetNode) {
				owningFacet = (ComponentNode) property.getParent();
			} else {
				LOGGER.warn("Trying to change facets for property " + property + " but parent is not a facet.");
				continue;
			}

			if (TLFacetType.SIMPLE.equals(st.toTLFacetType())) {
				// special case - Simple Facet in CO and VWA
				changeToSimple(property, owningFacet, editedNode.getSimpleFacet());
				continue;
			}
			if (owningFacet.getFacetType() == null || owningFacet.getFacetType().equals(st.toTLFacetType())) {
				// if the property parent is not a facet or its type is the same as the
				// selection skip this property and go to another one
				continue;
			}

			ComponentNode facet = null;
			switch (st) {
			case ID:
				facet = (ComponentNode) editedNode.getIDFacet();
				break;
			case SUMMARY:
				facet = (ComponentNode) editedNode.getSummaryFacet();
				break;
			case DETAIL:
				facet = (ComponentNode) editedNode.getDetailFacet();
				break;
			case SIMPLE:
				facet = editedNode.getSimpleFacet();
				break;
			case VWA_ATTRIBUTES:
				if (editedNode instanceof ComplexComponentInterface) {
					facet = (ComponentNode) ((ComplexComponentInterface) editedNode).getAttributeFacet();
				}
				break;
			default:
				LOGGER.warn("Do not support this facet type: " + st);
				return;
			}
			if (facet == null) {
				return;
			}
			if (owningFacet instanceof SimpleFacetNode) {
				// special case - moving out from simple
				changeFromSimple(property, (FacetNode) owningFacet, (FacetNode) facet);
			} else {
				changeFacet(property, owningFacet, facet);
			}
		}
		tablePoster.postTable(editedNode);
		setSelected(properties);
	}

	private void setSelected(final List<PropertyNode> properties) {
		final TableItem[] items = previewTable.getItems();
		for (int i = 0; i < items.length; i++) {
			final TableItem item = items[i];
			if (properties.contains(item.getData())) {
				previewTable.select(i);
			}
		}
	}

	// TODO - use FacetNode
	private void changeFacet(final PropertyNode property, final ComponentNode oldFacet, final ComponentNode newFacet) {
		if (newFacet != null) {
			final HistoryItem item = new HistoryItem(OpType.OWNING_FACET_CHANGE, oldFacet, property);
			historyPush(item);
			if (newFacet instanceof FacetNode) {
				property.moveProperty((FacetNode) newFacet);
			} else {
				oldFacet.removeProperty(property);
				newFacet.addProperty(property);
				// FIXME - this should be dead code!
				LOGGER.error("ChangeFacet without facet? UNDOING the OLD way.");
			}
		}
	}

	/**
	 * Set the simple attribute facet based on the passed property.
	 * 
	 * @param property
	 * @param oldFacet
	 * @param simpleFacet
	 */
	private void changeToSimple(final PropertyNode property, final ComponentNode oldFacet,
			final ComponentNode simpleFacet) {
		if (simpleFacet != null) {
			SimpleFacetNode sf = (SimpleFacetNode) simpleFacet;
			Node simpleProp = sf.getSimpleAttribute();
			if (simpleProp == null) {
				assert (false); // FIXME - if needed, create concrete node
				// simpleProp = new ComponentNode((TLModelElement) ModelNode.getEmptyType());
			}
			// Copy the simple property for history / revert. Set its type.
			Node clone = simpleProp.clone(null, null);
			((TypeUser) clone).setAssignedType((TypeProvider) simpleProp.getType());
			// clone has no parent as needed for setAssignedType, so use the type class.
			// clone.getTypeClass().setTypeNode(simpleProp.getType());

			OtmRegistry.getMainController().getModelController().changeToSimple(property);

			// NOTE - at this point the TLValueWithAttributes still has the property as an attribute.
			final HistoryItem item = new HistoryItem(OpType.OWNING_FACET_CHANGE_TO_SIMPLE, oldFacet, property, clone);
			historyPush(item);
		}
	}

	/**
	 * @param simpleProp
	 */
	private void resetSimple(INode simpleProp) {
		Object srcObj = simpleProp.getModelObject().getTLModelObj();
		LOGGER.debug("FIXME");
		// if (srcObj instanceof TLnSimpleAttribute) {
		// new TLSimpleAttributeResetter().reset((TLnSimpleAttribute) srcObj);
		// }
	}

	/**
	 * Create a new element property based on the name and type of the simple attribute.
	 * 
	 * @param simpleAttr
	 *            - the property node containing the simpleAttribute MO (TLnSimpleAttribute)
	 * @param simpleFacet
	 *            - facetNode containing the simpleFacetMO (TLSimpleFacet)
	 * @param targetFacet
	 *            - facetNode that will contain the new property
	 */
	private void changeFromSimple(final PropertyNode simpleAttr, final FacetNode simpleFacet,
			final FacetNode targetFacet) {
		if (targetFacet != null) {

			Node simpleAttrClone = simpleAttr.clone(null, null);
			ComponentNode newProperty = OtmRegistry.getMainController().getModelController()
					.moveSimpleToFacet(simpleAttr, targetFacet);
			// TODO - get examples and equivalents. The TL interface does not provide them in a
			// list.
			final HistoryItem item = new HistoryItem(OpType.OWNING_FACET_CHANGE_FROM_SIMPLE, simpleAttrClone,
					newProperty);
			historyPush(item);
		}
	}

	private List<PropertyNode> getSelectedProperties() {
		final List<PropertyNode> selected = new ArrayList<PropertyNode>();
		final TableItem[] items = previewTable.getSelection();
		for (final TableItem item : items) {
			if (item.getData() instanceof PropertyNode) {
				selected.add((PropertyNode) item.getData());
			}
		}
		return selected;
	}

	private Composite createObjectTypeRadios(final Composite c) {
		final Composite container = new Composite(c, SWT.NULL);
		final GridLayout gl = new GridLayout();
		gl.numColumns = allowedObjectTypes.size();
		container.setLayout(gl);

		objectTypeButtons = new EnumMap<SubType, Button>(SubType.class);
		for (final SubType st : allowedObjectTypes) {
			final Button radioButton = new Button(container, SWT.RADIO);
			radioButton.setText(st.value());
			radioButton.addListener(SWT.Selection, new Listener() {

				@Override
				public void handleEvent(final Event event) {
					final Button button = objectTypeButtons.get(st);
					if (button != null && button.getSelection()) {
						setObjectType(st);
					}
				}

			});
			objectTypeButtons.put(st, radioButton);
		}
		return container;
	}

	/**
	 * @param text
	 */
	private void setNodeName(final String text) {
	}

	private void updateView() {
		clearSelections();
		updateLibraryCombo();
		updateFacetTypeButtons();
		updateObjectTypeButtons();
		tablePoster.postTable(editedNode);
		updateUndoButton();
	}

	private void updateUndoButton() {
		undoButton.setEnabled(history.size() > 0);
	}

	private void clearSelections() {
		for (final Button b : objectTypeButtons.values()) {
			b.setSelection(false);
		}
		for (final Button b : facetTypeButtons.values()) {
			b.setSelection(false);
		}
	}

	private void updateLibraryCombo() {
		final String libString = getLibraryString(editedNode.getLibrary());
		for (int i = 0; i < libraryCombo.getItemCount(); i++) {
			if (libraryCombo.getItem(i).equals(libString)) {
				libraryCombo.select(i);
				break;
			}
		}
	}

	private void validate() {
		// TODO: implement validation
		// boolean complete = true;
		// String message = null;
		// try {
		// validator.validate();
		// } catch (ValidationException e) {
		// message = e.getMessage();
		// complete = false;
		// LOGGER.debug("Validation output " + e.getMessage());
		// }
		// setPageComplete(complete);
		// setMessage(message, ERROR);
		// getWizard().getContainer().updateButtons();
	}

	private PropertyNode getSelectedProperty() {
		final List<PropertyNode> selected = getSelectedProperties();
		if (selected.size() > 0) {
			return selected.get(0);
		}
		return null;
	}

	public ComponentNode getEditedComponent() {
		return editedNode;
	}

	// private void mergeToSimple(TLAttribute source, TLnSimpleAttribute destination) {
	// new TLAttributeToSimpleAttributeMerger().merge(source, destination);
	// }
	//
	// private void mergeToSimple(TLProperty source, TLnSimpleAttribute destination) {
	// new TLPropertyToSimpleAttributeMerger().merge(source, destination);
	// }
	//
	// private void mergeToSimple(TLIndicator source, TLnSimpleAttribute destination) {
	// new TLIndicatorToSimpleAttributeMerger().merge(source, destination);
	// }

	private void mergeToSimple(INode source, INode destination) {
		LOGGER.debug("FIXME!");
		// Object model = source.getModelObject().getTLModelObj();
		// Object simpleAtt = destination.getModelObject().getTLModelObj();
		// if (simpleAtt instanceof TLnSimpleAttribute) {
		// TLnSimpleAttribute destModel = (TLnSimpleAttribute) simpleAtt;
		// if (model instanceof TLAttribute) {
		// mergeToSimple((TLAttribute) model, destModel);
		// } else if (model instanceof TLProperty) {
		// mergeToSimple((TLProperty) model, destModel);
		// } else if (model instanceof TLIndicator) {
		// mergeToSimple((TLIndicator) model, destModel);
		// }
		// ((SimpleAttributeMO) destination.getModelObject()).refreshAssignedType();
		// }
	}

}
