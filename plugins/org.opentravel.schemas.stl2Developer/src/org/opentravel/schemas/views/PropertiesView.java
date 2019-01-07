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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.opentravel.schemas.controllers.OtmActions;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.XsdNode;
import org.opentravel.schemas.node.handlers.ConstraintHandler;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.objectMembers.FacetOMNode;
import org.opentravel.schemas.node.properties.ElementNode;
import org.opentravel.schemas.node.properties.EnumLiteralNode;
import org.opentravel.schemas.node.properties.IValueWithContextHandler;
import org.opentravel.schemas.node.properties.IndicatorNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.properties.PropertyNodeType;
import org.opentravel.schemas.node.properties.RoleNode;
import org.opentravel.schemas.node.properties.SimpleAttributeFacadeNode;
import org.opentravel.schemas.node.typeProviders.AbstractContextualFacet;
import org.opentravel.schemas.node.typeProviders.EnumerationClosedNode;
import org.opentravel.schemas.node.typeProviders.ListFacetNode;
import org.opentravel.schemas.node.typeProviders.SimpleTypeNode;
import org.opentravel.schemas.node.typeProviders.VWA_Node;
import org.opentravel.schemas.properties.Messages;
import org.opentravel.schemas.stl2developer.MainWindow;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.types.TypeUser;
import org.opentravel.schemas.widgets.OtmEventData;
import org.opentravel.schemas.widgets.OtmHandlers;
import org.opentravel.schemas.widgets.OtmSections;
import org.opentravel.schemas.widgets.OtmTextFields;
import org.opentravel.schemas.widgets.OtmWidgets;
import org.opentravel.schemas.widgets.OtmWidgets.SpinnerData;
import org.opentravel.schemas.widgets.WidgetFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * View for the node properties pane.
 * 
 * @author Dave Hollander
 * 
 */
public class PropertiesView extends OtmAbstractView implements ISelectionListener {
	public static String VIEW_ID = "org.opentravel.schemas.stl2Developer.PropertiesView";
	private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesView.class);

	private Node propertyNode = null; // current property being displayed

	private FormToolkit toolkit;
	private ScrolledForm form;
	private final MainWindow mainWindow;

	// Fields
	private Text componentField;
	private Text nameField;
	private Text nameSpaceField;
	private Text typeField;
	private Text typePrefix;
	private Button typeSelector;
	private Text descField;
	private Text exampleField;
	private Text equivalentField;
	private Text patternField;

	private int nCols;

	private Composite propertyComposite;

	private Button mandatoryButton = null;
	private Button listButton = null;
	private Combo roleCombo = null;

	private Spinner repeatSpinner;
	private Spinner maxLenSpinner;
	private Spinner minLenSpinner;
	private Spinner fractionDigitsSpinner;
	private Spinner totalDigitsSpinner;
	private Text minInclusiveText;
	private Text maxInclusiveText;
	private Text minExclusiveText;
	private Text maxExclusiveText;
	private Node prevPropNode = null;

	private OtmWidgets widgets;
	private OtmHandlers handlers;
	private OtmActions actions;
	private OtmTextFields fields;
	private Text repeatNonValid;

	public PropertiesView() {
		mainWindow = OtmRegistry.getMainWindow();
		OtmRegistry.registerPropertiesView(this);
	}

	@Override
	public boolean activate() {
		return false;
	}

	@Override
	public void createPartControl(Composite parent) {
		// to make this into a true view, use this instead of init.
	}

	protected void initialize(final Composite parent) {
		nCols = 5;

		// Use the otm widgets and handlers initialized by main controller.
		widgets = mc.getWidgets();
		handlers = mc.getHandlers();
		actions = mc.getActions();
		fields = mc.getFields();

		// set up display
		toolkit = WidgetFactory.createFormToolkit(parent.getDisplay());

		// Set up table layout in the form body.
		form = toolkit.createScrolledForm(parent);

		final TableWrapLayout layout = new TableWrapLayout();
		layout.numColumns = 1;
		layout.makeColumnsEqualWidth = true;
		form.getBody().setLayout(layout);
		toolkit.paintBordersFor(form.getBody());

		propertyComposite = mc.getSections().formatSection(toolkit, form, OtmSections.propertySection);
		formatPropertyFields();

		disableSelectors();
		// Enable the type field as a drop target.
		handlers.enableDropTarget(typeField, actions, OtmActions.setPropertyType(), widgets);
	}

	/**
	 * Clear the text from the posted propertyFields fields and set the propertyNode to null.
	 * 
	 * @param n
	 */
	private void clearProperties() {
		if (!mainWindow.hasDisplay())
			return;
		if (propertyComposite.isDisposed())
			return;
		widgets.clearTextFields(propertyComposite);
		roleCombo.setEnabled(false);
		roleCombo.setText("");
		mandatoryButton.setEnabled(false);
		mandatoryButton.setSelection(false);
		listButton.setEnabled(false);
		listButton.setSelection(false);
		repeatSpinner.setEnabled(false);
		repeatSpinner.setSelection(1);
		minLenSpinner.setEnabled(false);
		minLenSpinner.setSelection(0);
		maxLenSpinner.setEnabled(false);
		maxLenSpinner.setSelection(0);
		fractionDigitsSpinner.setEnabled(false);
		fractionDigitsSpinner.setSelection(0);
		totalDigitsSpinner.setEnabled(false);
		totalDigitsSpinner.setSelection(0);
		minInclusiveText.setText("");
		maxInclusiveText.setText("");
		minExclusiveText.setText("");
		maxExclusiveText.setText("");
		disableSelectors();
		form.reflow(true);
		form.update();
	}

	private void formatPropertyFields() {
		// LOGGER.debug("Formatting Property section");

		final GridLayout gl = new GridLayout();
		propertyComposite.setLayout(gl);
		gl.numColumns = nCols;

		// Identity and description
		componentField = fields.formatTextField(propertyComposite, OtmTextFields.ComponentType, nCols);
		nameSpaceField = fields.formatTextField(propertyComposite, OtmTextFields.PropertyNS, nCols);
		nameField = fields.formatTextField(propertyComposite, OtmTextFields.PropertyName, nCols);
		descField = fields.formatTextField(propertyComposite, OtmTextFields.description, nCols);

		// Type
		widgets.postSash(propertyComposite, nCols, mainWindow.getColorProvider().getColor(SWT.COLOR_GRAY));

		typeField = fields.formatTextField(propertyComposite, OtmTextFields.typeName, nCols - 1);
		typeSelector = fields.formatButton(propertyComposite, OtmWidgets.typeSelector,
				OtmActions.propertyTypeSelector(), handlers.new ButtonSelectionHandler());
		typePrefix = fields.formatTextField(propertyComposite, OtmTextFields.typePrefix, nCols);

		// Property behaviors
		widgets.postSash(propertyComposite, nCols, mainWindow.getColorProvider().getColor(SWT.COLOR_GRAY));
		widgets.postSash(propertyComposite, nCols, mainWindow.getColorProvider().getColor(SWT.COLOR_WHITE));

		// final String[] roles = { "Element", "Attribute", "Indicator" };
		final String[] roles = getSupportedRoleTypes(propertyNode);
		roleCombo = widgets.formatCombo(propertyComposite, OtmWidgets.roleCombo, roles,
				OtmActions.changePropertyRoleEventID(), new OtmHandlers.TextModifyListener(), false);

		final GridData comboGD = new GridData();
		comboGD.horizontalAlignment = SWT.FILL;
		comboGD.grabExcessHorizontalSpace = true;
		comboGD.horizontalSpan = nCols - 1;
		roleCombo.setLayoutData(comboGD);

		final SpinnerData sd = widgets.new SpinnerData();
		createSpinner(propertyComposite, sd);

		final Label mandatoryLabel = new Label(propertyComposite, SWT.TRAIL);
		mandatoryLabel.setText("Mandatory");
		mandatoryButton = widgets.formatButton(propertyComposite, OtmWidgets.mandatoryButton,
				OtmActions.toggleMandatory(), handlers.new ButtonSelectionHandler());
		final GridData mandatoryGD = new GridData();
		mandatoryGD.horizontalSpan = nCols - 3;
		mandatoryGD.horizontalAlignment = SWT.FILL;
		mandatoryGD.grabExcessHorizontalSpace = true;
		mandatoryGD.horizontalIndent = 30;
		mandatoryButton.setLayoutData(mandatoryGD);

		// Constraints
		Composite constraintComposite = propertyComposite;
		widgets.postSash(propertyComposite, nCols, mainWindow.getColorProvider().getColor(SWT.COLOR_GRAY));
		widgets.postSash(propertyComposite, nCols, mainWindow.getColorProvider().getColor(SWT.COLOR_WHITE));
		// // TODO - use expandable composite (or bar?) instead of sash
		// ExpandBar constraintBar = new ExpandBar (propertyComposite, SWT.V_SCROLL);
		// constraintBar.setEnabled(true);
		// Composite constraintComposite = new Composite(constraintBar, SWT.NONE);
		// ExpandItem item0 = new ExpandItem (constraintBar, SWT.NONE, 0);
		// http://help.eclipse.org/juno/index.jsp?topic=%2Forg.eclipse.platform.doc.isv%2Fguide%2Fforms_controls_section.htm

		patternField = fields.formatTextField(constraintComposite, OtmTextFields.pattern, nCols);
		minLenSpinner = widgets.formatSpinner(constraintComposite, OtmWidgets.minLenSpinner,
				OtmActions.changeMinLength(), sd, handlers.new ButtonSelectionHandler());
		maxLenSpinner = widgets.formatSpinner(constraintComposite, OtmWidgets.maxLenSpinner,
				OtmActions.changeMaxLength(), sd, handlers.new ButtonSelectionHandler());
		final GridData spinnerGD = new GridData();
		spinnerGD.horizontalSpan = nCols - 3;
		spinnerGD.horizontalAlignment = SWT.BEGINNING;
		spinnerGD.horizontalIndent = 30;
		maxLenSpinner.setLayoutData(spinnerGD);

		final SpinnerData sdDigits = widgets.new SpinnerData();
		sdDigits.min = -1;
		fractionDigitsSpinner = widgets.formatSpinner(constraintComposite, OtmWidgets.fractionDigitsSpinner,
				OtmActions.changeFractionDigits(), sdDigits, handlers.new ButtonSelectionHandler());

		totalDigitsSpinner = widgets.formatSpinner(constraintComposite, OtmWidgets.totalDigitsSpinner,
				OtmActions.changeTotalDigits(), sd, handlers.new ButtonSelectionHandler());
		final GridData spinnerGD2 = new GridData();
		spinnerGD2.horizontalSpan = nCols - 3;
		spinnerGD2.horizontalAlignment = SWT.BEGINNING;
		spinnerGD2.horizontalIndent = 30;
		totalDigitsSpinner.setLayoutData(spinnerGD2);

		minInclusiveText = fields.formatTextField(constraintComposite, OtmTextFields.minInclusive, 1);
		maxInclusiveText = fields.formatTextField(constraintComposite, OtmTextFields.maxInclusive, 3);
		minExclusiveText = fields.formatTextField(constraintComposite, OtmTextFields.minExclusive, 1);
		maxExclusiveText = fields.formatTextField(constraintComposite, OtmTextFields.maxExclusive, 3);

		final Label listLabel = new Label(propertyComposite, SWT.TRAIL);
		listLabel.setText("List");
		listButton = widgets.formatButton(constraintComposite, OtmWidgets.listButton, OtmActions.toggleList(),
				handlers.new ButtonSelectionHandler());
		final GridData listGD = new GridData();
		listGD.horizontalSpan = nCols - 1;
		listGD.horizontalAlignment = SWT.FILL;
		listGD.grabExcessHorizontalSpace = true;
		// listGD.horizontalIndent = 30;
		listButton.setLayoutData(listGD);

		// Eq and Ex
		widgets.postSash(propertyComposite, nCols, mainWindow.getColorProvider().getColor(SWT.COLOR_GRAY));
		widgets.postSash(propertyComposite, nCols, mainWindow.getColorProvider().getColor(SWT.COLOR_WHITE));

		exampleField = fields.formatTextField(propertyComposite, OtmTextFields.example, nCols);
		equivalentField = fields.formatTextField(propertyComposite, OtmTextFields.equivalent, nCols);
	}

	private void createSpinner(Composite parent, SpinnerData sd) {
		Label repeatLabel = new Label(parent, SWT.TRAIL);
		Composite repeaP = new Composite(parent, SWT.NONE);
		repeaP.setLayout(new StackLayout());
		repeatSpinner = widgets.formatSpinner(repeaP, OtmWidgets.repeatCount, OtmActions.changeRepeatCount(), sd,
				handlers.new ButtonSelectionHandler());
		repeatNonValid = toolkit.createText(repeaP, "");
		repeatNonValid.setEnabled(false);
		repeatLabel.setText(((OtmEventData) repeatSpinner.getData()).getLabel());
	}

	/**
	 * @param node
	 * @return
	 */
	private String[] getSupportedRoleTypes(Node node) {
		List<String> props = new ArrayList<>();
		if (node instanceof PropertyNode) {
			Collection<PropertyNodeType> types = PropertyNodeType.getSupportedTypes((PropertyNode) node);
			for (PropertyNodeType t : types)
				props.add(t.getName());
		}
		return props.toArray(new String[props.size()]);
	}

	/**************************************************************************
	 * Get the current node displayed in the properties section.
	 */
	@Override
	public INode getCurrentNode() {
		return propertyNode;
	}

	@Override
	public INode getPreviousNode() {
		return prevPropNode;
	}

	@Override
	public List<Node> getSelectedNodes() {
		ArrayList<Node> sn;
		sn = new ArrayList<>();
		sn.add(propertyNode);
		return sn;
	}

	@Override
	public String getViewID() {
		return VIEW_ID;
	}

	/**
	 * Update the mandatory button checked/unchecked setting and enabled.
	 */
	private void postMandatoryButton(final PropertyNode pn) {
		boolean enabled = pn.isEditable_newToChain();
		// Force optional if the property is in a minor and the owning component is a versioned object
		if (pn.getOwningComponent().isVersioned() && pn.getLibrary().isMinorVersion()) {
			enabled = false;
			pn.setMandatory(false);
		}
		mandatoryButton.setSelection(pn.isMandatory());
		mandatoryButton.setEnabled(enabled);
		if (enabled)
			if (pn.isMandatory())
				mandatoryButton.setToolTipText("Uncheck to make this property optional.");
			else
				mandatoryButton.setToolTipText("Check to make this property required.");
		else
			mandatoryButton.setToolTipText("Required/optional can not be changed.");
	}

	/**
	 * Post the last propertyFields that were posted. Used at the start of a drag-n-drop.
	 */
	protected void postPrevProperties() {
		propertyNode = prevPropNode;
		postProperties(prevPropNode);
	}

	/**
	 * Post the propertyFields appropriate to the type of node Sets propertyNode and prevPropNode
	 * 
	 * @param n
	 *            - Node to post
	 */
	private void postProperties(final Node n) {
		// All widgets are contained in the property composite. It is the only test needed to assure the view is active.
		if (!mainWindow.hasDisplay() || propertyComposite == null || propertyComposite.isDisposed())
			return;

		if (n == null || n.getParent() == null) {
			clearProperties();
			return;
		}
		// LOGGER.debug("Posting Properties for: "+ n);

		OtmHandlers.suspendHandlers();
		clearProperties(); // Clear the fields, and the propertyNode pointer
		if (n instanceof AbstractContextualFacet)
			mc.getFields().postField(nameField, ((AbstractContextualFacet) n).getLocalName(), n.isRenameable());
		else
			fields.postField(nameField, n.getName(), n.isRenameable());
		fields.postField(componentField, n.getComponentType(), false);
		fields.postField(descField, n.getDescription(), n.isEditable_description());
		fields.postField(nameSpaceField, n.getNamespace(), false);

		ComponentNode cn = null;
		if (n instanceof ComponentNode) {
			cn = (ComponentNode) n;
			if (n.getLibrary() != null) {
				String curContext = n.getLibrary().getDefaultContextId();
				updateEquivalent(cn, curContext);
				updateExample(cn, curContext);
			}
		} else {
			if (n instanceof XsdNode) {
				updateConstraints((ComponentNode) n);
				updateType((PropertyNode) n);
			}
			OtmHandlers.enableHandlers();
			return;
		}
		// LOGGER.debug("Posting component node properties.");

		if (n.getParent() == null || n.getTLModelObject() == null) {
			LOGGER.warn("Error with object: " + n.getNameWithPrefix());
		} else if (n.getParent() instanceof VWA_Node && n instanceof FacetOMNode) {
			// for VWA - Facets should not have name and description editable
			// fields.postField(nameField, n.getName(), false);
			fields.postField(descField, n.getDescription(), false);
			typeField.setEnabled(false);
		} else if (cn instanceof SimpleTypeNode) {
			updateType(cn);
			updateConstraints(cn);
		} else if (cn instanceof SimpleAttributeFacadeNode) {
			updateType(cn);
		} else if (n instanceof EnumLiteralNode || n instanceof RoleNode) {
			// Nothing to do.
		} else if (cn instanceof PropertyNode) {
			updateType(cn);

			updateConstraints((ComponentNode) cn.getType(), false);
			updatePropertyTypeCombo((PropertyNode) cn);
			if (cn instanceof ElementNode) {
				if (cn.getType() instanceof ListFacetNode && ((ListFacetNode) cn.getType()).isDetailListFacet()) {
					repeatNonValid.setVisible(true);
					repeatNonValid.setText("---");
					repeatSpinner.setVisible(false);
				} else {
					repeatNonValid.setVisible(false);
					repeatSpinner.setVisible(true);
					widgets.postSpinner(repeatSpinner, ((ElementNode) cn).getRepeat(), cn.isEditable());
				}
			}
			if (!(cn instanceof IndicatorNode))
				postMandatoryButton((PropertyNode) cn);
			// in this case fixNames should set type
			if (((PropertyNode) cn).isAssignedComplexType())
				nameField.setEnabled(false);
		}

		// if (NodeUtils.checker(cn).isPatch().existInPreviousVersions().get()) {
		if (!cn.isEditable_newToChain())
			disableEditingForPreviousVersions();

		form.update();
		OtmHandlers.enableHandlers();
	}

	private void disableEditingForPreviousVersions() {
		nameField.setEnabled(false);
		typeSelector.setEnabled(false);
		roleCombo.setEnabled(false);
		repeatSpinner.setEnabled(false);
		mandatoryButton.setEnabled(false);
		patternField.setEnabled(false);
		minLenSpinner.setEnabled(false);
		maxLenSpinner.setEnabled(false);
		minInclusiveText.setEnabled(false);
		minExclusiveText.setEnabled(false);
		maxInclusiveText.setEnabled(false);
		maxInclusiveText.setEnabled(false);
		listButton.setEnabled(false);
	}

	private void updatePropertyTypeCombo(PropertyNode pn) {
		roleCombo.setItems(getSupportedRoleTypes(pn));
		roleCombo.setText(pn.getPropertyRole());
		roleCombo.setEnabled(pn.isEditable());
	}

	private void updateType(final ComponentNode cn) {
		// If not type user or type user has a required type then do nothing.
		if (!(cn instanceof TypeUser))
			return;
		if (((TypeUser) cn).getRequiredType() != null)
			return;

		fields.postField(typeField, cn.getAssignedTypeName(), cn.isEnabled_AssignType());
		// fields.postField(typeField, cn.getTypeName(), cn.isEditable_newToChain() && cn.isTypeUser());
		fields.postField(typePrefix, cn.getAssignedPrefix(), false);
		// See logic in LibraryTablePosterWithButtons
		typeSelector.setEnabled(cn.isEnabled_AssignType());
		// typeSelector.setEnabled(cn.isEditable() && cn.isTypeUser()
		// && !NodeUtils.checker(cn).isInMinorOrPatch().existInPreviousVersions().get());
	}

	/**
	 * Set field and spinner values for the constraints on simple types.
	 * 
	 * @param cn
	 */
	private void updateConstraints(final ComponentNode cn) {
		if (cn == null)
			return; // may be null when inherited property.
		updateConstraints(cn, cn.isEditable_newToChain());
	}

	// Cn is the type assigned to property being posted in this view
	private void updateConstraints(final ComponentNode cn, boolean editable) {
		if (cn == null)
			return;
		if (cn instanceof SimpleTypeNode) {
			// Simple objects with parent of closed enumeration MUST have list checked
			if (!(((SimpleTypeNode) cn).getAssignedType() instanceof EnumerationClosedNode))
				listButton.setEnabled(true);
			listButton.setSelection(((SimpleTypeNode) cn).isSimpleList());
		}

		// simpleTypeNode
		ConstraintHandler ch = cn.getConstraintHandler();
		if (ch != null) {
			fields.postField(patternField, ch.getPattern(), editable);
			widgets.postSpinner(minLenSpinner, ch.getMinLen(), editable);
			widgets.postSpinner(maxLenSpinner, ch.getMaxLen(), editable);
			widgets.postSpinner(fractionDigitsSpinner, ch.getFractionDigits(), editable);
			widgets.postSpinner(totalDigitsSpinner, ch.getTotalDigits(), editable);
			fields.postField(minInclusiveText, ch.getMinInclusive(), editable);
			fields.postField(maxInclusiveText, ch.getMaxInclusive(), editable);
			fields.postField(minExclusiveText, ch.getMinExclusive(), editable);
			fields.postField(maxExclusiveText, ch.getMaxExclusive(), editable);
		}
	}

	private void updateEquivalent(ComponentNode cn, String context) {
		final String eqToolTip = Messages.getString("OtmW." + "317"); //$NON-NLS-1$
		equivalentField.setToolTipText(eqToolTip + " (" + context + ")"); // show context
		IValueWithContextHandler handler = cn.getEquivalentHandler();
		if (handler != null)
			fields.postField(equivalentField, handler.get(context), cn.isEditable_equivalent());
	}

	private void updateExample(ComponentNode cn, String context) {
		// boolean isExampleSupported = NodeUtils.checker(cn).isExampleSupported().get();
		final String toolTip = Messages.getString("OtmW." + "315"); //$NON-NLS-1$
		exampleField.setToolTipText(toolTip + " (" + context + ")"); // show context
		IValueWithContextHandler handler = cn.getExampleHandler();
		if (handler != null)
			fields.postField(exampleField, handler.get(context), cn.isEditable_example());
	}

	/**
	 * Update property, documentation and findings to their current object.
	 */
	@Override
	public void refresh() {
		postProperties(propertyNode);
	}

	@Override
	public void refresh(INode node) {
		setCurrentNode(node);
		refresh();
	}

	private void disableSelectors() {
		mandatoryButton.setEnabled(false);
		typeSelector.setEnabled(false);
	}

	@Override
	public void setFocus() {
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (TypeView.VIEW_ID.equals(part.getSite().getId()) && !selection.isEmpty()) {
			INode node = extractFirstNode(selection);
			setCurrentNode(node);
		}
	}

	@Override
	public void setCurrentNode(final INode node) {
		if (node != propertyNode) {
			prevPropNode = propertyNode;
			this.propertyNode = (Node) node;
			postProperties((Node) node);
		}
	}

}
