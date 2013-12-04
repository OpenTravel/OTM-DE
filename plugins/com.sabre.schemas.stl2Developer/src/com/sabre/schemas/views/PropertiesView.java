/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemas.views;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sabre.schemas.controllers.OtmActions;
import com.sabre.schemas.node.ComponentNode;
import com.sabre.schemas.node.FacetNode;
import com.sabre.schemas.node.INode;
import com.sabre.schemas.node.Node;
import com.sabre.schemas.node.PropertyNodeType;
import com.sabre.schemas.node.SimpleTypeNode;
import com.sabre.schemas.node.XsdNode;
import com.sabre.schemas.node.controllers.NodeUtils;
import com.sabre.schemas.node.properties.EnumLiteralNode;
import com.sabre.schemas.node.properties.PropertyNode;
import com.sabre.schemas.node.properties.RoleNode;
import com.sabre.schemas.node.properties.SimpleAttributeNode;
import com.sabre.schemas.stl2developer.MainWindow;
import com.sabre.schemas.stl2developer.OtmRegistry;
import com.sabre.schemas.widgets.OtmEventData;
import com.sabre.schemas.widgets.OtmHandlers;
import com.sabre.schemas.widgets.OtmSections;
import com.sabre.schemas.widgets.OtmTextFields;
import com.sabre.schemas.widgets.OtmWidgets;
import com.sabre.schemas.widgets.OtmWidgets.SpinnerData;
import com.sabre.schemas.widgets.WidgetFactory;

/**
 * 
 * @author Dave Hollander
 * 
 */
public class PropertiesView extends OtmAbstractView implements ISelectionListener {
    public static String VIEW_ID = "com.sabre.schemas.stl2Developer.PropertiesView";
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

        propertyComposite = mc.getSections().formatSection(toolkit, form,
                OtmSections.propertySection);
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
        widgets.clearTextFields(propertyComposite);
        // widgets.clearTextFields(documentationComposite);
        roleCombo.setEnabled(false);
        roleCombo.setText("");
        mandatoryButton.setEnabled(false);
        mandatoryButton.setSelection(false);
        listButton.setEnabled(false);
        listButton.setSelection(false);
        repeatSpinner.setEnabled(false);
        repeatSpinner.setSelection(0);
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
        componentField = fields.formatTextField(propertyComposite, OtmTextFields.ComponentType,
                nCols);
        nameSpaceField = fields.formatTextField(propertyComposite, OtmTextFields.PropertyNS, nCols);
        nameField = fields.formatTextField(propertyComposite, OtmTextFields.PropertyName, nCols);
        descField = fields.formatTextField(propertyComposite, OtmTextFields.description, nCols);

        // Type
        widgets.postSash(propertyComposite, nCols,
                mainWindow.getColorProvider().getColor(SWT.COLOR_GRAY));

        typeField = fields.formatTextField(propertyComposite, OtmTextFields.typeName, nCols - 1);
        typeSelector = fields.formatButton(propertyComposite, OtmWidgets.typeSelector,
                OtmActions.propertyTypeSelector(), handlers.new ButtonSelectionHandler());
        typePrefix = fields.formatTextField(propertyComposite, OtmTextFields.typePrefix, nCols);

        // Property behaviors
        widgets.postSash(propertyComposite, nCols,
                mainWindow.getColorProvider().getColor(SWT.COLOR_GRAY));
        widgets.postSash(propertyComposite, nCols,
                mainWindow.getColorProvider().getColor(SWT.COLOR_WHITE));

        // final String[] roles = { "Element", "Attribute", "Indicator" };
        final String[] roles = getSupportedRoleTypes(propertyNode);
        roleCombo = widgets
                .formatCombo(propertyComposite, OtmWidgets.roleCombo, roles,
                        OtmActions.changePropertyRoleEventID(),
                        new OtmHandlers.TextModifyListener(), false);

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
        widgets.postSash(propertyComposite, nCols,
                mainWindow.getColorProvider().getColor(SWT.COLOR_GRAY));
        widgets.postSash(propertyComposite, nCols,
                mainWindow.getColorProvider().getColor(SWT.COLOR_WHITE));
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
        fractionDigitsSpinner = widgets.formatSpinner(constraintComposite,
                OtmWidgets.fractionDigitsSpinner, OtmActions.changeFractionDigits(), sdDigits,
                handlers.new ButtonSelectionHandler());

        totalDigitsSpinner = widgets.formatSpinner(constraintComposite,
                OtmWidgets.totalDigitsSpinner, OtmActions.changeTotalDigits(), sd,
                handlers.new ButtonSelectionHandler());
        final GridData spinnerGD2 = new GridData();
        spinnerGD2.horizontalSpan = nCols - 3;
        spinnerGD2.horizontalAlignment = SWT.BEGINNING;
        spinnerGD2.horizontalIndent = 30;
        totalDigitsSpinner.setLayoutData(spinnerGD2);

        minInclusiveText = fields.formatTextField(constraintComposite, OtmTextFields.minInclusive,
                1);
        maxInclusiveText = fields.formatTextField(constraintComposite, OtmTextFields.maxInclusive,
                3);
        minExclusiveText = fields.formatTextField(constraintComposite, OtmTextFields.minExclusive,
                1);
        maxExclusiveText = fields.formatTextField(constraintComposite, OtmTextFields.maxExclusive,
                3);

        final Label listLabel = new Label(propertyComposite, SWT.TRAIL);
        listLabel.setText("List");
        listButton = widgets.formatButton(constraintComposite, OtmWidgets.listButton,
                OtmActions.toggleList(), handlers.new ButtonSelectionHandler());
        final GridData listGD = new GridData();
        listGD.horizontalSpan = nCols - 1;
        listGD.horizontalAlignment = SWT.FILL;
        listGD.grabExcessHorizontalSpace = true;
        // listGD.horizontalIndent = 30;
        listButton.setLayoutData(listGD);

        // Eq and Ex
        widgets.postSash(propertyComposite, nCols,
                mainWindow.getColorProvider().getColor(SWT.COLOR_GRAY));
        widgets.postSash(propertyComposite, nCols,
                mainWindow.getColorProvider().getColor(SWT.COLOR_WHITE));

        exampleField = fields.formatTextField(propertyComposite, OtmTextFields.example, nCols);
        equivalentField = fields
                .formatTextField(propertyComposite, OtmTextFields.equivalent, nCols);
    }

    private void createSpinner(Composite parent, SpinnerData sd) {
        Label repeatLabel = new Label(parent, SWT.TRAIL);
        Composite repeaP = new Composite(parent, SWT.NONE);
        repeaP.setLayout(new StackLayout());
        repeatSpinner = widgets.formatSpinner(repeaP, OtmWidgets.repeatCount,
                OtmActions.changeRepeatCount(), sd, handlers.new ButtonSelectionHandler());
        repeatNonValid = toolkit.createText(repeaP, "");
        repeatNonValid.setEnabled(false);
        repeatLabel.setText(((OtmEventData) repeatSpinner.getData()).getLabel());
    }

    /**
     * @param node
     * @return
     */
    private String[] getSupportedRoleTypes(Node node) {
        Collection<PropertyNodeType> types = PropertyNodeType.getSupportedTypes(node);
        return generateDisplayList(types);
    }

    private static String[] generateDisplayList(Collection<PropertyNodeType> types) {
        List<String> props = new ArrayList<String>();
        for (PropertyNodeType t : types) {
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
        sn = new ArrayList<Node>();
        sn.add(propertyNode);
        return sn;
    }

    @Override
    public String getViewID() {
        return VIEW_ID;
    }

    /**
     * Update the mandatory button checked/unchecked setting.
     * 
     * @param setting
     *            - if true, set to checked
     * @param enabled
     *            - if true, enable the button
     */
    private void postMandatoryButton(final boolean setting, final boolean enabled) {
        mandatoryButton.setSelection(setting);
        mandatoryButton.setEnabled(enabled);
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
        if (!mainWindow.hasDisplay())
            return;

        if (n == null || n.getParent() == null) {
            clearProperties();
            return;
        }
        // LOGGER.debug("Posting Properties for: "+ n);

        OtmHandlers.suspendHandlers();
        clearProperties(); // Clear the fields, and the propertyNode pointer

        if (n instanceof FacetNode) {
            boolean edit = n.isEditable() && !NodeUtils.checker(n).isInheritedFacet().get();
            fields.postField(nameField, n.getLabel(), edit);
        } else {
            fields.postField(nameField, n.getName(), n.isEditable());
        }

        if (n.isID_Reference())
            nameField.setEnabled(false);
        if (n.isFacet() && !n.isOperation() && !n.isCustomFacet() && !n.isQueryFacet())
            nameField.setEnabled(false);
        if (n.isAlias() && n.getParent().isFacet())
            nameField.setEnabled(false);
        fields.postField(componentField, n.getComponentType(), false);
        fields.postField(descField, n.getDescription(), n.isEditable());
        fields.postField(nameSpaceField, n.getNamespace(), false);

        ComponentNode cn = null;
        if (n instanceof ComponentNode) {
            cn = (ComponentNode) n;
            String curContext = mc.getContextController().getDefaultContextId();
            updateEquivalent(cn, curContext);
            updateExample(cn, curContext);
        } else {
            if (n instanceof XsdNode) {
                updateConstraints((ComponentNode) n);
                updateType((PropertyNode) n);
            }
            OtmHandlers.enableHandlers();
            return;
        }
        // LOGGER.debug("Posting component node properties.");

        if (n.getParent() == null || n.getModelObject() == null || n.getTLModelObject() == null) {
            LOGGER.debug("Error with object: " + n);
        } else if (n.getParent().isValueWithAttributes() && n.isFacet()) {
            // for VWA - Facets should not have name and description editable
            fields.postField(nameField, n.getName(), false);
            fields.postField(descField, n.getDescription(), false);
            typeField.setEnabled(false);
        } else if (cn instanceof SimpleTypeNode) {
            updateType(cn);
            updateConstraints(cn);
        } else if (cn instanceof SimpleAttributeNode) {
            updateType(cn);
        } else if (n instanceof EnumLiteralNode || n instanceof RoleNode) {
            // Nothing to do.
        } else if (cn instanceof PropertyNode) {
            updateType(cn);

            updateConstraints((ComponentNode) cn.getType(), false);
            updatePropertyTypeCombo(cn);
            if (cn.isElement()) {
                if (cn.getTypeNode() != null && cn.getTypeNode().isDetailListFacet()) {
                    repeatNonValid.setVisible(true);
                    repeatNonValid.setText("---");
                    repeatSpinner.setVisible(false);
                } else {
                    repeatNonValid.setVisible(false);
                    repeatSpinner.setVisible(true);
                    widgets.postSpinner(repeatSpinner, cn.getRepeat(), cn.isEditable());
                }
            }
            if (!cn.isIndicator())
                postMandatoryButton(cn.isMandatory(), cn.isEditable());
            // in this case fixNames should set type
            if (cn.isAssignedComplexType())
                nameField.setEnabled(false);
        }

        if (NodeUtils.checker(cn).isPatch().existInPreviousVersions().get()) {
            disableEditingForPreviousVersions();
        }

        form.update();
        OtmHandlers.enableHandlers();
    }

    private void disableEditingForPreviousVersions() {
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

    private void updatePropertyTypeCombo(ComponentNode cn) {
        roleCombo.setItems(getSupportedRoleTypes(cn));
        roleCombo.setText(cn.getPropertyRole());
        roleCombo.setEnabled(cn.isEditable());
    }

    private void updateType(final ComponentNode cn) {
        fields.postField(typeField, cn.getTypeName(), cn.isEditable() && cn.isTypeUser());
        fields.postField(typePrefix, cn.getAssignedPrefix(), false);
        typeSelector.setEnabled(cn.isEditable() && cn.isTypeUser());
    }

    /**
     * Set field and spinner values for the constraints on simple types.
     * 
     * @param cn
     */
    private void updateConstraints(final ComponentNode cn) {
        updateConstraints(cn, cn.isEditable());
    }

    private void updateConstraints(final ComponentNode cn, boolean editable) {
        if (cn == null)
            return; // may be null when inherited property.
        if (cn.isSimpleType())
            listButton.setEnabled(true);
        if (cn.getModelObject().isSimpleList()) {
            listButton.setSelection(true);
        } else {
            listButton.setSelection(false);
            fields.postField(patternField, cn.getPattern(), editable);
            widgets.postSpinner(minLenSpinner, cn.getMinLen(), editable);
            widgets.postSpinner(maxLenSpinner, cn.getMaxLen(), editable);
            widgets.postSpinner(fractionDigitsSpinner, cn.getFractionDigits(), editable);
            widgets.postSpinner(totalDigitsSpinner, cn.getTotalDigits(), editable);
            fields.postField(minInclusiveText, cn.getMinInclusive(), editable);
            fields.postField(maxInclusiveText, cn.getMaxInclusive(), editable);
            fields.postField(minExclusiveText, cn.getMinExclusive(), editable);
            fields.postField(maxExclusiveText, cn.getMaxExclusive(), editable);
        }
    }

    private void updateEquivalent(ComponentNode cn, String context) {
        fields.postField(equivalentField, cn.getEquivalent(context), cn.isEditable());
    }

    private void updateExample(ComponentNode cn, String context) {
        boolean isExampleSupported = NodeUtils.checker(cn).isExampleSupported().get();
        fields.postField(exampleField, cn.getExample(context), cn.isEditable()
                && isExampleSupported);
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
