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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import org.opentravel.schemas.actions.ChangeActionController;
import org.opentravel.schemas.actions.ChangeActionController.ChangeObjectTypeHistoryItem;
import org.opentravel.schemas.actions.ChangeActionController.HistoryItem;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.SubType;
import org.opentravel.schemas.node.interfaces.FacetInterface;
import org.opentravel.schemas.node.interfaces.FacetOwner;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.typeProviders.VWA_Node;
import org.opentravel.schemas.node.typeProviders.facetOwners.BusinessObjectNode;
import org.opentravel.schemas.node.typeProviders.facetOwners.CoreObjectNode;
import org.opentravel.schemas.stl2developer.ColorProvider;
import org.opentravel.schemas.types.SimpleAttributeOwner;
import org.opentravel.schemas.widgets.FacetViewTablePoster;
import org.opentravel.schemas.widgets.WidgetFactory;
import org.opentravel.schemas.wizards.ChangeWizard.ExtendedTLFacetType;
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
 * @author Dave Hollander / Agnieszka Janowska
 * 
 */
public class ChangeWizardPage extends WizardPage {
	private static final Logger LOGGER = LoggerFactory.getLogger(ChangeWizardPage.class);

	private Text nameText;
	private Combo libraryCombo;
	private Table previewTable;
	private Button undoButton;

	private FacetViewTablePoster tablePoster;

	private ComponentNode editedNode;
	private final List<SubType> allowedObjectTypes;
	private final List<ExtendedTLFacetType> allowedFacetTypes;
	private final ChangeActionController changeController;

	private Map<ExtendedTLFacetType, Button> facetTypeButtons;
	private Map<SubType, Button> objectTypeButtons;
	private Map<String, LibraryNode> libraryNameMap;

	private final Stack<HistoryItem> history = new Stack<>();

	/**
	 * flag to prevent selection already selected radio button
	 */
	protected Button facetRadioButton;

	/**
	 * @param string
	 * @param string2
	 * @param validator
	 * @param editedNode2
	 * @param allowedObjectTypes2
	 * @param allowedFacetTypes2
	 * @param changeActionController
	 */
	protected ChangeWizardPage(final String pageName, final String title, final FormValidator validator,
			final ComponentNode editedNode, final List<SubType> allowedObjectTypes,
			final List<ExtendedTLFacetType> allowedFacetTypes, ChangeActionController changeController) {
		super(pageName, title, null);
		this.editedNode = editedNode;
		this.allowedObjectTypes = allowedObjectTypes;
		this.allowedFacetTypes = allowedFacetTypes;
		// TODO - make sure EditedNode is not null
		this.changeController = changeController;
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
		containerGD.heightHint = 1200;

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

		// Fill combo widget with a sorted set of editable libraries
		int index = 0;
		libraryNameMap = Node.getLibraryModelManager().getEditableLibrarySet();
		for (final Entry<String, LibraryNode> entry : libraryNameMap.entrySet()) {
			libraryCombo.add(entry.getKey());
			if (entry.getValue() == editedNode.getLibrary())
				libraryCombo.select(index);
			index++;
		}
		libraryCombo.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(final ModifyEvent e) {
				final String selected = libraryCombo.getText();
				if (selected != null && !selected.isEmpty()) {
					// Just set the library - the controller will make the change permanent on completion.
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
		tablePoster = new FacetViewTablePoster(previewTable, new ColorProvider(parent.getDisplay()));

		updateView();
		setControl(container);
		setPageComplete(true);
	}

	/**
	 * Set the type of object. The editNode becomes the newly created object and the current node is put into history.
	 */
	private void setObjectType(final SubType st) {
		if (editedNode instanceof LibraryMemberInterface) {
			final HistoryItem item = changeController.changeObject((LibraryMemberInterface) editedNode, st);
			editedNode = (ComponentNode) item.getNewNode();
			historyPush(item);
		}
		tablePoster.postTable(editedNode);
		updateFacetTypeButtons();
		updateUndoButton();
	}

	private String getLibraryString(final LibraryNode library) {
		return library != null ? library.getPrefix() + ":" + library.getName() : "";
	}

	public void undoAllOperation() {
		while (history.size() > 0) {
			undoLastOp();
		}
	}

	private void undoLastOp() {
		if (history.size() > 0) {
			final HistoryItem item = historyPop();
			if (item instanceof ChangeObjectTypeHistoryItem)
				editedNode = (ComponentNode) ((ChangeObjectTypeHistoryItem) item).getSourceNode();
			changeController.undo(item);
		}
		updateView();
	}

	/**
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
		history.removeAllElements();
	}

	private Table createPreviewTable(final Composite container) {
		final TableViewer viewer = new TableViewer(container,
				SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		final Table table = viewer.getTable();

		return table;
	}

	private void updateFacetTypeButtons() {
		enableFacetTypeButtons();
		final PropertyNode selected = getSelectedProperty();
		ExtendedTLFacetType facetType = null;
		if (selected != null) {
			final INode parent = selected.getParent();
			if (parent instanceof FacetInterface) {
				facetType = ExtendedTLFacetType.valueOf((ComponentNode) parent);
			}
		}
		// if there is no selected property all the radio buttons will be unselected
		for (final ExtendedTLFacetType ft : facetTypeButtons.keySet()) {
			if (ft.equals(facetType)) {
				markSelectedFacetTypeButton(facetTypeButtons.get(ft));
			} else {
				facetTypeButtons.get(ft).setSelection(false);
				facetRadioButton = null;
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
				if (n instanceof FacetInterface) {
					final ComponentNode facet = (ComponentNode) n;
					final Button b = facetTypeButtons.get(ExtendedTLFacetType.valueOf(facet));
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

	/**
	 * Push change into history then add edited node to selected library
	 * 
	 * @see LibraryNode#addMember(LibraryMemberInterface)
	 * @param selected
	 */
	private void setLibrary(final String selected) {
		final LibraryNode selectedLib = libraryNameMap.get(selected);
		if (editedNode instanceof LibraryMemberInterface)
			historyPush(changeController.changeLibrary((LibraryMemberInterface) editedNode, selectedLib));
	}

	private Composite createFacetTypeRadios(final Composite c) {
		final Composite container = new Composite(c, SWT.NULL);
		final GridLayout gl = new GridLayout();
		gl.numColumns = allowedFacetTypes.size();
		container.setLayout(gl);

		facetTypeButtons = new EnumMap<>(ExtendedTLFacetType.class);
		for (final ExtendedTLFacetType st : allowedFacetTypes) {
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
	private void setOwningFacet(final ExtendedTLFacetType st) {
		final List<PropertyNode> properties = getSelectedProperties();
		if (properties.isEmpty())
			return;

		// Do nothing if the current edited node is not a facet owner
		if (!(editedNode instanceof FacetOwner))
			return;

		// Get the facet where the properties are going to be moved
		FacetOwner editedFacetOwner = (FacetOwner) editedNode;
		FacetInterface destinationFacet = null;
		if (editedNode instanceof FacetOwner)
			switch (st) {
			case ID:
				destinationFacet = editedFacetOwner.getFacet_ID();
				break;
			case SUMMARY:
				destinationFacet = editedFacetOwner.getFacet_Summary();
				break;
			case DETAIL:
				destinationFacet = editedFacetOwner.getFacet_Detail();
				break;
			case SIMPLE:
				destinationFacet = editedFacetOwner.getFacet_Simple();
				break;
			case VWA_ATTRIBUTES:
				destinationFacet = editedFacetOwner.getFacet_Attributes();
				break;
			default:
				break;
			}
		if (destinationFacet == null) {
			LOGGER.warn("Do not support this facet type: " + st);
			return;
		}

		FacetInterface owningFacet = null;
		HistoryItem item = null;
		for (final PropertyNode property : properties) {
			if (!(property.getParent() instanceof FacetInterface))
				continue;
			owningFacet = (FacetInterface) property.getParent();

			if (owningFacet.getFacetType() == null || owningFacet.getFacetType().equals(st.toTLFacetType()))
				continue; // Skip property with same facet type

			if (destinationFacet.canOwn(property))
				item = changeController.changeOwningFacet(property, destinationFacet);

			else if (TLFacetType.SIMPLE.equals(st.toTLFacetType()))
				item = changeController.changeToSimple(property);

			else if (property.getOwningComponent() instanceof SimpleAttributeOwner)
				item = changeController.changeFromSimple((SimpleAttributeOwner) property.getOwningComponent(),
						destinationFacet);

			if (item != null)
				history.push(item);
		}
		tablePoster.postTable(editedNode);
		updateUndoButton();
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

	private List<PropertyNode> getSelectedProperties() {
		final List<PropertyNode> selected = new ArrayList<>();
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

		objectTypeButtons = new EnumMap<>(SubType.class);
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
		assert editedNode.getLibrary() != null;
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
}
