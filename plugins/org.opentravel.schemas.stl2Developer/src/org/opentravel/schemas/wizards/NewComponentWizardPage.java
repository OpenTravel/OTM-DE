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

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.ComponentNodeType;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeEditStatus;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.typeProviders.facetOwners.BusinessObjectNode;
import org.opentravel.schemas.properties.Messages;
import org.opentravel.schemas.trees.type.BusinessObjectOnlyTypeFilter;
import org.opentravel.schemas.trees.type.ContextualFacetOwnersContentProvider;
import org.opentravel.schemas.trees.type.ContextualFacetOwnersTypeFilter;
import org.opentravel.schemas.trees.type.ExtensionTreeContentProvider;
import org.opentravel.schemas.trees.type.TypeTreeExtensionSelectionFilter;
import org.opentravel.schemas.widgets.WidgetFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

public class NewComponentWizardPage extends WizardPage {
    // private final static Logger LOGGER = LoggerFactory.getLogger(NewComponentWizardPage.class);

    private Composite container;
    private Node targetNode;

    private Text name;
    private ComponentNodeType selectedType = null;
    private Text newDescription;
    private Combo combo;

    private static ComponentNodeType[] serviceComponentList = {ComponentNodeType.EXTENSION_POINT};
    private static ComponentNodeType[] noServiceComponentList =
        {ComponentNodeType.EXTENSION_POINT, ComponentNodeType.SERVICE};
    private static ComponentNodeType[] complexComponentList = {ComponentNodeType.BUSINESS, ComponentNodeType.CHOICE,
        ComponentNodeType.CORE, ComponentNodeType.VWA, ComponentNodeType.OPEN_ENUM};
    private static ComponentNodeType[] simpleComponentList = {ComponentNodeType.SIMPLE, ComponentNodeType.CLOSED_ENUM};

    private static ComponentNodeType[] PatchComponentList = {ComponentNodeType.EXTENSION_POINT};

    private static ComponentNodeType[] ComponentList = {ComponentNodeType.BUSINESS, ComponentNodeType.CHOICE,
        ComponentNodeType.CORE, ComponentNodeType.VWA, ComponentNodeType.OPEN_ENUM, ComponentNodeType.CLOSED_ENUM,
        ComponentNodeType.SIMPLE, ComponentNodeType.EXTENSION_POINT};

    private static ComponentNodeType[] ContextualFacetsList =
        {ComponentNodeType.CHOICE_FACET, ComponentNodeType.CUSTOM_FACET, ComponentNodeType.QUERY_FACET};

    private int nCols;
    private Map<ComponentNodeType,Button> objectTypeButtons;

    // TODO - how to validate that the name is unique?
    private final class FacetKeyListener implements KeyListener {
        @Override
        public void keyPressed(final KeyEvent e) {

        }

        @Override
        // Only Name must be filled out to move to next page
        public void keyReleased(final KeyEvent e) {
            if (!combo.getText().isEmpty()) {
                // System.out.println("keyReleased");
                setPageComplete( true );
                getWizard().getContainer().updateButtons();
            }
        }
    }

    protected NewComponentWizardPage(final String pageName, final String title, final ImageDescriptor titleImage,
        Node node) {
        super( pageName, title, titleImage );
        setTitle( title );
        setDescription( "" );
        // TODO - Why doesn't description show?
        targetNode = node;
    }

    @Override
    public boolean canFlipToNextPage() {
        // LOGGER.debug("NewComponentPage - can flip? ");
        if (!name.getText().isEmpty()) {
            setFilter( ComponentNodeType.fromString( combo.getText() ) );
            if (combo.getText().equals( ComponentNodeType.SERVICE.getDescription() )) {
                getNextPage().setTitle( Messages.getString( "wizard.newObject.page.service.title" ) );
                getNextPage().setDescription( Messages.getString( "wizard.newObject.page.service.description" ) );
                return true;
            }
            getNextPage().setTitle( Messages.getString( "wizard.newObject.page.facet.title" ) );
            getNextPage().setDescription( Messages.getString( "wizard.newObject.page.facet.description" ) );
            if (combo.getText().equals( ComponentNodeType.CHOICE_FACET.getDescription() ))
                return true;
            if (combo.getText().equals( ComponentNodeType.CUSTOM_FACET.getDescription() ))
                return true;
            if (combo.getText().equals( ComponentNodeType.QUERY_FACET.getDescription() ))
                return true;
        }
        return false;
    }

    private void setFilter(ComponentNodeType type) {
        switch (type) {
            case CHOICE_FACET:
                ((TypeSelectionPage) getNextPage())
                    .setTypeSelectionFilter( new ContextualFacetOwnersTypeFilter( TLFacetType.CHOICE ) );
                ((TypeSelectionPage) getNextPage())
                    .setTypeTreeContentProvider( new ContextualFacetOwnersContentProvider() );
                break;
            case CUSTOM_FACET:
                ((TypeSelectionPage) getNextPage())
                    .setTypeSelectionFilter( new ContextualFacetOwnersTypeFilter( TLFacetType.CUSTOM ) );
                ((TypeSelectionPage) getNextPage())
                    .setTypeTreeContentProvider( new ContextualFacetOwnersContentProvider() );
                break;
            case QUERY_FACET:
                ((TypeSelectionPage) getNextPage())
                    .setTypeSelectionFilter( new ContextualFacetOwnersTypeFilter( TLFacetType.QUERY ) );
                ((TypeSelectionPage) getNextPage())
                    .setTypeTreeContentProvider( new ContextualFacetOwnersContentProvider() );
                break;
            case SERVICE:
                ((TypeSelectionPage) getNextPage()).setTypeSelectionFilter( new BusinessObjectOnlyTypeFilter( null ) );
                ((TypeSelectionPage) getNextPage()).setTypeTreeContentProvider( new ExtensionTreeContentProvider() );
            case EXTENSION_POINT:
                // ModelObject<?> tlmo = new BusinessObjMO(new TLBusinessObject());
                // ((TypeSelectionPage) getNextPage()).setTypeSelectionFilter(new
                // TypeTreeExtensionSelectionFilter(tlmo));
                BusinessObjectNode filterBO = new BusinessObjectNode( new TLBusinessObject() );
                ((TypeSelectionPage) getNextPage())
                    .setTypeSelectionFilter( new TypeTreeExtensionSelectionFilter( filterBO ) );
                ((TypeSelectionPage) getNextPage()).setTypeTreeContentProvider( new ExtensionTreeContentProvider() );
                break;
        }

    }

    @Override
    public void createControl(final Composite parent) {
        // LOGGER.debug("NewComponentPage1 - create control. Target node: " + targetNode.getName());
        container = new Composite( parent, SWT.NULL );
        final GridLayout layout = new GridLayout();
        container.setLayout( layout );
        nCols = 2;
        layout.numColumns = nCols;
        // Size the wizard
        final GridData containerGD = new GridData();
        containerGD.horizontalAlignment = SWT.FILL;
        containerGD.verticalAlignment = SWT.FILL;
        containerGD.grabExcessHorizontalSpace = true;
        containerGD.grabExcessVerticalSpace = true;
        containerGD.widthHint = 600;
        // containerGD.heightHint = 800;
        containerGD.heightHint = 1600;
        container.setLayoutData( containerGD );

        // Post the library state and name
        LibraryNode targetLib = targetNode.getLibrary().getHead();
        final Label libraryLabel = new Label( container, SWT.NULL );
        if (!targetNode.isEditable())
            libraryLabel.setText( Messages.getString( "wizard.newObject.libraryState.notEditable" ) );
        else
            libraryLabel.setText( Messages.getString( "wizard.newObject.libraryState.editable" ) );
        libraryLabel.setToolTipText( Messages.getString( "wizard.newObject.libraryState.tooltip" ) );

        final Label libraryName = new Label( container, SWT.NULL | SWT.READ_ONLY );
        libraryName.setText( targetLib.getLabel() + " [" + targetLib.getVersion() + " ]" );
        libraryName.setToolTipText( Messages.getString( "wizard.newObject.libraryState.tooltip" ) );
        if (targetLib.getEditStatus().equals( NodeEditStatus.PATCH )) {
            libraryLabel.setText( Messages.getString( "wizard.newObject.libraryState.patch" ) );
            libraryName.setToolTipText( Messages.getString( "wizard.newObject.select.tooltip.patch" ) );
        }

        // Post the combo
        postCombo( Messages.getString( "wizard.newObject.select.text" ) );
        // Post name and description
        postCommonFields();

        final GridData gd = new GridData( GridData.FILL_HORIZONTAL );
        name.setLayoutData( gd );
        setControl( container ); // Required to avoid an error in the system
        setPageComplete( false );
    }

    @Override
    public String getName() {
        return name.getText();
    }

    @Override
    public String getDescription() {
        return newDescription.getText();
    }

    public String getComponentType() {
        return combo.getText();
        // return selectedType == null ? "" : selectedType.getDescription();
    }

    private void postCombo(final String label) {
        LibraryNode lib = targetNode.getLibrary().getHead();

        Label cl = new Label( container, SWT.NONE );
        cl.setText( label );
        combo = WidgetFactory.createCombo( container, SWT.BORDER | SWT.READ_ONLY );
        combo.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );

        ComponentNodeType targetType = contextGuess( targetNode );
        ArrayList<ComponentNodeType> list = new ArrayList<ComponentNodeType>();

        String tooltip = Messages.getString( "wizard.newObject.select.tooltip.noService" );

        if (lib.getEditStatus().equals( NodeEditStatus.PATCH )) {
            list.addAll( Arrays.asList( PatchComponentList ) );
            tooltip = Messages.getString( "wizard.newObject.select.tooltip.Patch" );
            targetType = ComponentNodeType.EXTENSION_POINT;
        } else {
            list.addAll( Arrays.asList( ComponentList ) );
            list.addAll( Arrays.asList( ContextualFacetsList ) );
        }
        // Put the list into combo widget
        int index = 0;
        for (ComponentNodeType item : list) {
            combo.add( item.getDescription() );
            if (item == targetType)
                combo.select( index );
            index++;
        }

        // If the library doesn't have a service, add it to the drop-down list.
        boolean hasService = false;
        if (targetNode.getChain() != null)
            hasService = targetNode.getChain().hasService();
        else
            hasService = targetNode.getLibrary().hasService();
        if (!hasService && !targetNode.getEditStatus().equals( NodeEditStatus.PATCH )) {
            combo.add( ComponentNodeType.SERVICE.getDescription() );
            tooltip = Messages.getString( "wizard.newObject.select.tooltip.Service" );
        }
        cl.setToolTipText( tooltip );
        combo.setToolTipText( tooltip );

        // set listeners
        combo.addKeyListener( new FacetKeyListener() );
        combo.addModifyListener( new TextModifyListener() ); // set dirty flag
        combo.addTraverseListener( new TextTraverseListener() ); // handle tabs
    }

    /**
     * Use target node to make a guess at the desired object type.
     */
    private ComponentNodeType contextGuess(Node target) {
        ComponentNodeType type = target.getComponentNodeType();
        return type != null ? type : ComponentNodeType.CORE;
    }

    // TODO - replace combo with array of radio buttons
    // This code is close, but does not use the widget factory and the layout is wrong.
    private Composite createObjectTypeRadios(final Composite c) {
        // final Composite container = new Composite(c, SWT.NULL);
        final GridLayout gl = new GridLayout();
        gl.numColumns = 2;
        container.setLayout( gl );

        objectTypeButtons = new EnumMap<ComponentNodeType,Button>( ComponentNodeType.class );
        ArrayList<ComponentNodeType> list = new ArrayList<ComponentNodeType>();
        String tooltip = Messages.getString( "wizard.newObject.select.tooltip.noService" );
        if (!targetNode.getLibrary().hasService())
            tooltip = Messages.getString( "wizard.newObject.select.tooltip.Service" );

        if (targetNode.getLibrary().getHead().getEditStatus().equals( NodeEditStatus.PATCH ))
            postButtons( PatchComponentList, tooltip, container );
        else {
            if (targetNode.getLibrary().hasService())
                postButtons( serviceComponentList, tooltip, container );
            else
                postButtons( noServiceComponentList, tooltip, container );
            postButtons( complexComponentList, tooltip, container );
            postButtons( simpleComponentList, tooltip, container );
        }
        return container;
    }

    private void postButtons(ComponentNodeType[] list, String tooltip, Composite comp) {
        for (final ComponentNodeType st : list) {
            final Button radioButton = new Button( comp, SWT.RADIO );
            radioButton.setText( st.value() );
            radioButton.setToolTipText( tooltip );
            radioButton.addListener( SWT.Selection, new Listener() {

                @Override
                public void handleEvent(final Event event) {
                    final Button button = objectTypeButtons.get( st );
                    if (button != null && button.getSelection()) {
                        setObjectType( st );
                    }
                }

            } );
            objectTypeButtons.put( st, radioButton );
        }
    }

    private void setObjectType(ComponentNodeType type) {
        selectedType = type;
    }

    /**
     * Create global name and description fields with labels and tool tips.
     */
    private void postCommonFields() {
        final Label label1 = new Label( container, SWT.NULL );
        label1.setText( Messages.getString( "wizard.nameField.label" ) );
        label1.setToolTipText( Messages.getString( "wizard.newObject.name.tooltip" ) );

        name = WidgetFactory.createText( container, SWT.BORDER | SWT.SINGLE );
        name.setText( "" );
        name.setToolTipText( Messages.getString( "wizard.newObject.name.tooltip" ) );
        name.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
        name.addKeyListener( new FacetKeyListener() );

        final Label label3 = new Label( container, SWT.NULL );
        label3.setText( Messages.getString( "wizard.descriptionField.label" ) );
        label3.setToolTipText( Messages.getString( "wizard.newObject.descriptionField.tooltip" ) );

        newDescription = WidgetFactory.createText( container, SWT.BORDER | SWT.MULTI );
        newDescription.setText( "" );
        newDescription.setToolTipText( Messages.getString( "wizard.newObject.descriptionField.tooltip" ) );
        newDescription.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
    }

    // this listener fires with each key stroke -- but does not have data
    private final class TextModifyListener implements ModifyListener {
        @Override
        public void modifyText(final ModifyEvent e) {
            if (e.widget instanceof Combo) {
                if (combo.getItem( combo.getSelectionIndex() )
                    .equals( ComponentNodeType.EXTENSION_POINT.getDescription() )) {
                    // if
                    // (combo.getItem(combo.getSelectionIndex()).equals(ComponentNode.xpFacetObj)) {
                    name.setText( "" );
                    name.setEnabled( false );
                } else {
                    name.setEnabled( true );
                }

                if (!combo.getText().isEmpty()) {
                    setPageComplete( true );
                }
                getWizard().getContainer().updateButtons();
            }
        }
    }

    private final class TextTraverseListener implements TraverseListener {
        @Override
        public void keyTraversed(final TraverseEvent e) {
            if (e.detail == SWT.TRAVERSE_TAB_NEXT || e.detail == SWT.TRAVERSE_TAB_PREVIOUS) {
                e.doit = true;
            }
            if (e.widget instanceof Text) {
                if (!name.getText().isEmpty()) {
                    setPageComplete( true );
                    getWizard().getContainer().updateButtons();
                }
            }
        }
    }

    public void setNode(final ComponentNode n) {
        targetNode = n;
    }
}
