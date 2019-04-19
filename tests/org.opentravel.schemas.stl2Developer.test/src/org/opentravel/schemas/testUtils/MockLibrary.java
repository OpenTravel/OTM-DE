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

package org.opentravel.schemas.testUtils;

import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.opentravel.schemacompiler.event.ModelElementListener;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLExtensionPointFacet;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.model.TLOpenEnumeration;
import org.opentravel.schemacompiler.model.TLService;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.repository.ProjectItem;
import org.opentravel.schemacompiler.saver.LibraryModelSaver;
import org.opentravel.schemacompiler.saver.LibrarySaveException;
import org.opentravel.schemacompiler.util.URLUtils;
import org.opentravel.schemacompiler.validate.FindingMessageFormat;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFinding;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemas.controllers.ProjectController;
import org.opentravel.schemas.node.AliasTests;
import org.opentravel.schemas.node.BusinessObjectTests;
import org.opentravel.schemas.node.ChoiceObjectTests;
import org.opentravel.schemas.node.CoreObjectTests;
import org.opentravel.schemas.node.Enumeration_Tests;
import org.opentravel.schemas.node.ExtensionPointNode_Tests;
import org.opentravel.schemas.node.FacetsTests;
import org.opentravel.schemas.node.InheritedChildren_Tests;
import org.opentravel.schemas.node.LibraryChainNodeTests;
import org.opentravel.schemas.node.LibraryNavNodeTests;
import org.opentravel.schemas.node.LibraryNodeTest;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.ModelNodeTests;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFactory;
import org.opentravel.schemas.node.NodeFinders;
import org.opentravel.schemas.node.OperationTests;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.node.ProjectNodeTest;
import org.opentravel.schemas.node.PropertyNodeTest;
import org.opentravel.schemas.node.ResourceObjectTests;
import org.opentravel.schemas.node.ServiceNode;
import org.opentravel.schemas.node.ServiceTests;
import org.opentravel.schemas.node.SimpleTypeNodeTests;
import org.opentravel.schemas.node.VWA_Tests;
import org.opentravel.schemas.node.interfaces.FacadeInterface;
import org.opentravel.schemas.node.interfaces.FacetInterface;
import org.opentravel.schemas.node.interfaces.FacetOwner;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNavNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.listeners.BaseNodeListener;
import org.opentravel.schemas.node.objectMembers.ExtensionPointNode;
import org.opentravel.schemas.node.objectMembers.OperationNode;
import org.opentravel.schemas.node.properties.AttributeNode;
import org.opentravel.schemas.node.properties.ElementNode;
import org.opentravel.schemas.node.properties.ElementReferenceNode;
import org.opentravel.schemas.node.properties.IdNode;
import org.opentravel.schemas.node.properties.IndicatorElementNode;
import org.opentravel.schemas.node.properties.IndicatorNode;
import org.opentravel.schemas.node.properties.InheritedElementNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.properties.TypedPropertyNode;
import org.opentravel.schemas.node.resources.ResourceBuilder;
import org.opentravel.schemas.node.resources.ResourceNode;
import org.opentravel.schemas.node.typeProviders.AbstractContextualFacet;
import org.opentravel.schemas.node.typeProviders.AliasNode;
import org.opentravel.schemas.node.typeProviders.ChoiceFacetNode;
import org.opentravel.schemas.node.typeProviders.ChoiceObjectNode;
import org.opentravel.schemas.node.typeProviders.ContextualFacetNode;
import org.opentravel.schemas.node.typeProviders.CustomFacetNode;
import org.opentravel.schemas.node.typeProviders.EnumerationClosedNode;
import org.opentravel.schemas.node.typeProviders.EnumerationOpenNode;
import org.opentravel.schemas.node.typeProviders.FacetProviderNode;
import org.opentravel.schemas.node.typeProviders.ImpliedNode;
import org.opentravel.schemas.node.typeProviders.QueryFacetNode;
import org.opentravel.schemas.node.typeProviders.SimpleTypeNode;
import org.opentravel.schemas.node.typeProviders.VWA_Node;
import org.opentravel.schemas.node.typeProviders.facetOwners.BusinessObjectNode;
import org.opentravel.schemas.node.typeProviders.facetOwners.CoreObjectNode;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Creates a mock library in the runtime-OT2Editor.product directory. Is added to the passed project.
 * 
 * @author Dave Hollander
 * 
 */
public class MockLibrary {
    static final Logger LOGGER = LoggerFactory.getLogger( MockLibrary.class );
    /**
     * 
     */
    public static final String XsdINT = "int";
    public static final String XsdSTRING = "string";
    public static final String XsdDATE = "date";
    public static final String XsdDECIMAL = "decimal";

    public TypeProvider getXsd(String name) {
        TypeProvider x = (TypeProvider) NodeFinders.findNodeByName( name, ModelNode.XSD_NAMESPACE );
        assert x != null;
        return x;
    }

    public TypeProvider getXsdString() {
        return getXsd( XsdSTRING );
    }

    public TypeProvider getXsdDate() {
        return getXsd( XsdDATE );
    }

    public TypeProvider getXsdInt() {
        return getXsd( XsdINT );
    }

    public TypeProvider getXsdDecimal() {
        return getXsd( XsdDECIMAL );
    }

    /**
     * Create unmanaged, editable library in the default project
     * 
     * @param string
     * @return
     */
    public LibraryNode createNewLibrary(ProjectController pc, String name) {
        return createNewLibrary( pc.getDefaultUnmanagedNS(), name, pc.getDefaultProject() );
    }

    /**
     * Create an unmanaged, editable library with one business object.
     */
    public LibraryNode createNewLibrary(String ns, String name, ProjectNode parent) {
        LOGGER.debug( "Creating library with ns=" + ns + " in project " + parent.getNamespace() );
        LibraryNode ln = createNewLibrary_Empty( ns, name, parent );
        addBusinessObjectToLibrary( ln, name + "InitialBO" );
        Assert.assertEquals( 3, ln.getDescendants_LibraryMembers().size() );
        return ln;
    }

    /**
     * Create an unmanaged, editable library.
     * <p>
     * Create TLLibrary, assign it to a file, create node and validate.
     */
    public LibraryNode createNewLibrary_Empty(String ns, String name, ProjectNode parent) {
        TLLibrary tllib = new TLLibrary();
        tllib.setName( name );
        tllib.setStatus( TLLibraryStatus.DRAFT );
        tllib.setNamespaceAndVersion( ns, "1.0.0" );
        tllib.setPrefix( "nsPrefix" );

        // Check the project before using it.
        if (parent.getTLProject() == null)
            LOGGER.error( "Project with null TL Project." );
        if (parent.getTLProject().getProjectManager() == null)
            LOGGER.error( "Project with null TL Project Manager." );
        if (parent.getTLProject().getProjectManager().getModel() == null)
            LOGGER.error( "Project with null owning model." );

        String testPath;
        try {
            testPath = createTempFile( name + "-Test", ".otm" );
        } catch (IOException ex) {
            throw new RuntimeException( ex );
        }
        URL testURL = URLUtils.toURL( new File( testPath ) );
        tllib.setLibraryUrl( testURL );

        // Create Library Node
        LibraryNode ln = new LibraryNode( tllib, parent );
        assertTrue( "Error adding library to TL project.", ln.getProjectItem() != null );

        ln.setEditable( true ); // override ns policy

        // Has to be saved to be used in a project. Is not editable yet, so
        // can't use lib controller
        try {
            new LibraryModelSaver().saveLibrary( tllib );
        } catch (LibrarySaveException e) {
            LOGGER.debug( "Error Saving: ", e );
        }

        // Assure new library is valid
        ValidationFindings findings = ln.validate();
        boolean valid = findings.count( FindingType.ERROR ) == 0 ? true : false;
        if (!valid)
            printFindings( findings );
        assert valid;

        // Assure project and tl project contain this library
        assert parent.getLibraries().contains( ln );
        ProjectItem thisPI = null;
        for (ProjectItem pi : parent.getTLProject().getProjectItems())
            if (pi.getContent() == ln.getTLModelObject())
                thisPI = pi;
        assert thisPI != null;

        return ln;
    }

    /**
     * Create new library as a managed library in a chain.
     */
    public LibraryChainNode createNewManagedLibrary(String ns, String name, ProjectNode parent) {
        LibraryNode ln = createNewLibrary( ns, name, parent );
        LibraryChainNode lcn = new LibraryChainNode( ln );
        lcn.getHead().setEditable( true );
        return lcn;
    }

    /**
     * Create new library as a managed library in a chain.
     */
    public LibraryChainNode createNewManagedLibrary_Empty(String ns, String name, ProjectNode parent) {
        LibraryNode ln = createNewLibrary_Empty( ns, name, parent );
        LibraryChainNode lcn = new LibraryChainNode( ln );
        lcn.getHead().setEditable( true );
        return lcn;
    }

    /**
     * Create new library as a managed library in a chain. While not managed in repository, Library will be in the
     * namespace of the project so it will be forced editable.
     */
    public LibraryChainNode createNewManagedLibrary(String name, ProjectNode parent) {
        assert parent != null;
        LibraryNode ln = createNewLibrary( parent.getNamespace(), name, parent );
        if (!ln.isInProjectNS())
            LOGGER.debug( "Created library in wrong namespace. ProjNS = " + parent.getNamespace() + "  lnNS = "
                + ln.getNamespace() );
        Assert.assertTrue( ln.isInProjectNS() );
        LibraryChainNode lcn = new LibraryChainNode( ln );
        // not needed -- ???
        ln.setEditable( true ); // override ns policy for chains
        Assert.assertTrue( ln.isEditable() );
        return lcn;
    }

    public static void printDescendants(Node ln) {
        String names = "printDescendants: ";
        for (Node n : ln.getDescendants()) {
            names += n.getName() + " ";
        }
        LOGGER.debug( names );
    }

    public static void printFindings(ValidationFindings findings) {
        for (ValidationFinding finding : findings.getAllFindingsAsList()) {
            LOGGER.debug( "FINDING: " + finding.getFormattedMessage( FindingMessageFormat.IDENTIFIED_FORMAT ) );
        }
    }

    public static String createTempFile(String name, String suffix) throws IOException {
        final File tempDir = File.createTempFile( "temp-otm-" + name, Long.toString( System.nanoTime() ) );

        if (!(tempDir.delete())) {
            throw new IOException( "Could not delete temp file: " + tempDir.getAbsolutePath() );
        }

        if (!(tempDir.mkdir())) {
            throw new IOException( "Could not create temp directory: " + tempDir.getAbsolutePath() );
        }
        // Because the tempDir contains the *.bak file the deleteOnExit will not work.
        Runtime.getRuntime().addShutdownHook( new Thread() {
            @Override
            public void run() {
                if (tempDir.exists()) {
                    deleteContents( tempDir );
                }
            }
        } );
        tempDir.deleteOnExit();
        File f = File.createTempFile( name, suffix, tempDir );
        f.deleteOnExit();
        return f.getPath();
    }

    public static void deleteContents(File fileOrFolder) {
        if (fileOrFolder.isDirectory()) {
            for (File folderMember : fileOrFolder.listFiles()) {
                deleteContents( folderMember );
            }
        }
        fileOrFolder.delete();
    }

    /**
     * Add one of each object type to the library. Does <b>not</b> create extension points.
     * 
     * @param ln
     */
    public int addOneOfEach(LibraryNode ln, String nameRoot) {
        Assert.assertNotNull( ln );
        LOGGER.debug( "Adding one of each object type to " + ln + " with name root of " + nameRoot );

        int initialCount = ln.getDescendants_LibraryMembers().size();
        int finalCount = initialCount;
        if (ln.isEditable()) {
            addBusinessObjectToLibrary( ln, nameRoot + "BO" );
            addClosedEnumToLibrary( ln, nameRoot + "CE" );
            addCoreObjectToLibrary( ln, nameRoot + "CO" );
            addOpenEnumToLibrary( ln, nameRoot + "OE" );
            addSimpleTypeToLibrary( ln, nameRoot + "S" );
            addVWA_ToLibrary( ln, nameRoot + "VWA" );
            addChoice( ln, nameRoot + "Choice" );
            // DO NOT DO THIS - addExtensionPoint(ln, bo.getSummaryFacet());

            int addedCount = 11;
            // List<Node> descendants = ln.getDescendants_LibraryMembers();
            finalCount = ln.getDescendants_LibraryMembers().size();
            Assert.assertEquals( addedCount + initialCount, finalCount );
        } else
            Assert.assertEquals( initialCount, finalCount );
        return finalCount;
    }

    /**
     * @return new business object with no properties (invalid) or null if library is not editable
     */
    public BusinessObjectNode addBusinessObjectToLibrary_Empty(LibraryNode ln, String name) {
        if (name.isEmpty())
            name = "TestBO";

        BusinessObjectNode newNode = (BusinessObjectNode) NodeFactory.newLibraryMember( new TLBusinessObject() );
        newNode.setName( name );
        ln.addMember( newNode );
        newNode.setExtensible( true );
        return ln.isEditable() ? newNode : null;
    }

    /**
     * @return new business object with properties or null if library is not editable
     */
    public BusinessObjectNode addBusinessObjectToLibrary(LibraryNode ln, String name) {
        return addBusinessObjectToLibrary( ln, name, true );

    }

    /**
     * Create business object suitable for use as a resource subject.
     * 
     * @param ln
     * @param name
     * @return
     */
    public BusinessObjectNode addBusinessObject_ResourceSubject(LibraryNode ln, String name) {
        for (LibraryMemberInterface n : ln.getDescendants_LibraryMembers())
            if (n.getName().equals( name )) {
                LOGGER.warn( "Tried to create a business object with duplicate name: " + name );
                return (BusinessObjectNode) n;
            }
        BusinessObjectNode newNode = addBusinessObjectToLibrary_Empty( ln, name );
        if (newNode == null)
            return null;

        TypeProvider string = (TypeProvider) NodeFinders.findNodeByName( XsdSTRING, ModelNode.XSD_NAMESPACE );

        new IdNode( newNode.getFacet_ID(), name + "Id" );
        new ElementNode( newNode.getFacet_Summary(), "TestEle" + name, string );
        new AttributeNode( newNode.getFacet_Summary(), "TestAttribute" + name, string );

        // Add facets
        new ElementReferenceNode( newNode.addFacet( "C1" + name, TLFacetType.CUSTOM ), newNode );
        new AttributeNode( newNode.addFacet( "Q1" + name, TLFacetType.QUERY ), "TestAttribute2" + name, string );

        if (!ln.isValid()) {
            printValidationFindings( ln );
            assertTrue( "Library must be valid with new BO.", ln.isValid() ); // validates TL library
        }

        LOGGER.debug( "Created business object " + name );
        return ln.isEditable() ? newNode : null;
    }

    public BusinessObjectNode addBusinessObjectToLibrary(LibraryNode ln, String name, boolean addID) {
        for (LibraryMemberInterface n : ln.getDescendants_LibraryMembers())
            if (n.getName().equals( name )) {
                LOGGER.warn( "Tried to create a business object with duplicate name: " + name );
                return (BusinessObjectNode) n;
            }
        boolean wasValid = ln.isValid(); // if valid now then must be valid after

        BusinessObjectNode newNode = addBusinessObjectToLibrary_Empty( ln, name );
        if (newNode == null)
            return null;

        TypeProvider string = getXsdString();
        FacetProviderNode idFacet = newNode.getFacet_ID();
        assert idFacet != null;
        new ElementNode( newNode.getFacet_ID(), name + "ID", string );
        IdNode idNode = new IdNode( newNode.getFacet_ID(), "TestId" + name );

        FacetProviderNode facet = newNode.getFacet_Summary();
        assert facet != null;
        new ElementNode( newNode.getFacet_Summary(), "TestEle" + name, string );
        new AttributeNode( newNode.getFacet_Summary(), "TestAttribute" + name, string );
        new IndicatorElementNode( newNode.getFacet_Summary(), "TestIndicatorEle" + name );
        new IndicatorNode( newNode.getFacet_Summary(), "TestIndicator" + name );

        // Add contextual facets
        AbstractContextualFacet acf = newNode.addFacet( "c1" + name, TLFacetType.CUSTOM );
        new ElementReferenceNode( acf, newNode );
        acf = newNode.addFacet( "q1" + name, TLFacetType.QUERY );
        new AttributeNode( acf, "TestAttribute2" + name, string );

        if (wasValid && !ln.isValid()) {
            printValidationFindings( ln );
            assertTrue( "Library must be valid with new BO.", ln.isValid() ); // validates TL library
        }
        // If there are too many ID in extension the library is invalid.
        if (!addID)
            idNode.delete();
        LOGGER.debug( "Created business object " + name );
        return ln.isEditable() ? newNode : null;
    }

    /**
     * Create a choice object with an alias, summary and two choice facets all with properties.
     */
    public ChoiceObjectNode addChoice(LibraryNode ln, String name) {
        if (name.isEmpty())
            name = "ChoiceTest";
        TypeProvider string = getXsdString();
        TypeProvider decimal = getXsdDecimal();

        ChoiceObjectNode choice = new ChoiceObjectNode( new TLChoiceObject() );
        if (!ln.isEditable())
            return choice;

        choice.setName( name );
        if (ln != null)
            ln.addMember( choice );
        // restored 12/2-17 : after alias codegen utils are fixed 11/2016.
        choice.addAlias( "CAlias" + name );

        // Add properties to shared facet
        FacetInterface shared = choice.getFacet_Shared();
        new ElementNode( shared, "shared1" + name, string );

        // Add two choice facets
        FacetInterface f1 = choice.addFacet( "c1" );
        new ElementNode( f1, "c1p1" + name, string );
        new AttributeNode( f1, "c1p2" + name, string );
        new IndicatorNode( f1, "c1p3" + name );

        FacetInterface f2 = choice.addFacet( "c2" );
        new ElementNode( f2, "c2p1" + name, decimal );
        new AttributeNode( f2, "c2p2" + name, decimal );
        new IndicatorNode( f2, "c2p3" + name );

        return choice;
    }

    /**
     * Create two business objects where one extends the other.
     * 
     * @return the extended business object
     */
    public Node addExtendedBO(LibraryNode ln1, LibraryNode ln2, String baseName) {
        BusinessObjectNode n1 = (BusinessObjectNode) NodeFactory.newLibraryMember( new TLBusinessObject() );
        n1.setName( baseName + "Base" );
        ln1.addMember( n1 );
        TypeUser newProp = new ElementNode( n1.getFacet_ID(), "TestID" );
        newProp.setAssignedType( (TypeProvider) NodeFinders.findNodeByName( XsdSTRING, ModelNode.XSD_NAMESPACE ) );

        BusinessObjectNode n2 = (BusinessObjectNode) NodeFactory.newLibraryMember( new TLBusinessObject() );
        n2.setName( baseName + "Ext" );
        ln2.addMember( n2 );
        newProp = new ElementNode( n2.getFacet_ID(), "TestID" );
        newProp.setAssignedType( (TypeProvider) NodeFinders.findNodeByName( XsdSTRING, ModelNode.XSD_NAMESPACE ) );

        n2.setExtension( n1 );

        // Verify extension
        assertTrue( n2.getExtensionBase() == n1 );
        // Verify libraries and objects match
        assertTrue( n1.getLibrary() == ln1 );
        assertTrue( n2.getLibrary() == ln2 );
        assertTrue( ln1.getDescendants_LibraryMembersAsNodes().contains( n1 ) );
        assertTrue( ln2.getDescendants_LibraryMembersAsNodes().contains( n2 ) );

        return n2;
    }

    /**
     * Create several nodes that use each other as types
     * 
     * @param ln
     * @return
     */
    public Node addNestedTypes(LibraryNode ln) {
        BusinessObjectNode n1 = (BusinessObjectNode) NodeFactory.newLibraryMember( new TLBusinessObject() );
        n1.setName( "N1" );
        ln.addMember( n1 );

        CoreObjectNode n2 = (CoreObjectNode) NodeFactory.newLibraryMember( new TLCoreObject() );
        n2.setName( "N2" );
        ln.addMember( n2 );
        n2.setAssignedType( (TypeProvider) NodeFinders.findNodeByName( XsdINT, ModelNode.XSD_NAMESPACE ) );
        new ElementNode( n2.getFacet_Summary(), n1.getName(), n1 );

        CoreObjectNode n3 = (CoreObjectNode) NodeFactory.newLibraryMember( new TLCoreObject() );
        n3.setName( "N3" );
        ln.addMember( n3 );
        n3.setAssignedType( (TypeProvider) NodeFinders.findNodeByName( XsdINT, ModelNode.XSD_NAMESPACE ) );
        TypeUser n3PropA = new ElementNode( n3.getFacet_Summary(), n1.getName() );
        n3PropA.setAssignedType( n1 );
        TypedPropertyNode n3PropB = new ElementNode( n3.getFacet_Summary(), n2.getName() );
        n3PropB.setAssignedType( n2.getFacet_Summary() );

        TypeUser newProp = new ElementNode( n1.getFacet_ID(), "TestID" );
        newProp.setAssignedType( (TypeProvider) NodeFinders.findNodeByName( XsdSTRING, ModelNode.XSD_NAMESPACE ) );
        newProp = new ElementNode( n1.getFacet_Summary(), n2.getName() );
        newProp.setAssignedType( n2 );
        newProp = new ElementNode( n1.getFacet_Summary(), "TestSumB" );
        newProp.setAssignedType( n3.getFacet_Simple() );
        return n1;
    }

    public CoreObjectNode addCoreObjectToLibrary_Empty(LibraryNode ln, String name) {
        if (name.isEmpty())
            name = "TestCore";
        CoreObjectNode newNode = (CoreObjectNode) NodeFactory.newLibraryMember( new TLCoreObject() );
        newNode.setName( name );
        // newNode.setAssignedType((TypeProvider) NodeFinders.findNodeByName(XsdINT, ModelNode.XSD_NAMESPACE));
        ln.addMember( newNode );
        return newNode;
    }

    public CoreObjectNode addCoreObjectToLibrary(LibraryNode ln, String name) {
        TypeProvider type = (getXsdString());
        CoreObjectNode newNode = addCoreObjectToLibraryNoID( ln, name );
        new IdNode( newNode.getFacet_Summary(), "TestElements" + name );
        return newNode;
    }

    /**
     * Create a core object
     * 
     * @param ln
     * @param name
     * @param noID if true, create without any ID properties
     * @return
     */
    public CoreObjectNode addCoreObjectToLibraryNoID(LibraryNode ln, String name) {
        TypeProvider type = (getXsdString());
        CoreObjectNode newNode = addCoreObjectToLibrary_Empty( ln, name );
        new ElementNode( newNode.getFacet_Summary(), "TestElements" + name, type );
        new ElementNode( newNode.getFacet_Detail(), "TestElementd" + name, type );
        newNode.setAssignedType( getXsdInt() );
        newNode.getFacet_Role().add( name + "Role" );
        return newNode;
    }

    /**
     * Create a simple type node and assign type to xsd:int
     */
    public SimpleTypeNode addSimpleTypeToLibrary(LibraryNode ln, String name) {
        assert ln != null;
        if (name.isEmpty())
            name = "SimpleType";
        SimpleTypeNode sn = new SimpleTypeNode( new TLSimple() );
        sn.setName( name );
        sn.setAssignedType( getXsdInt() );
        ln.addMember( sn );

        assert (sn != null);
        assertTrue( "New simple type must be in library.", sn.getLibrary() == ln );
        assertTrue( "New simple type must be assigned type.", sn.getAssignedType() != null );
        return sn;
    }

    /**
     * Create VWA with one attribute property
     * 
     * @param ln
     * @param name
     * @return
     */
    public VWA_Node addVWA_ToLibrary(LibraryNode ln, String name) {
        if (name.isEmpty())
            name = "TestVWA";
        VWA_Node newNode = (VWA_Node) NodeFactory.newLibraryMember( new TLValueWithAttributes() );
        newNode.setName( name );
        newNode.setAssignedType( (TypeProvider) NodeFinders.findNodeByName( XsdDATE, ModelNode.XSD_NAMESPACE ) );
        TypedPropertyNode newProp = new AttributeNode( newNode.getFacet_Attributes(), "TestAttribute" );
        newProp.setAssignedType( (TypeProvider) NodeFinders.findNodeByName( XsdSTRING, ModelNode.XSD_NAMESPACE ) );
        ln.addMember( newNode );

        assert (newNode.getAssignedType().getName().equals( XsdDATE ));
        return newNode;
    }

    public EnumerationOpenNode addOpenEnumToLibrary(LibraryNode ln, String name) {
        if (name.isEmpty())
            name = "TestOpen";
        EnumerationOpenNode newNode = (EnumerationOpenNode) NodeFactory.newLibraryMember( new TLOpenEnumeration() );
        newNode.setName( name );
        ln.addMember( newNode );
        newNode.addLiteral( name + "Lit1" );
        return newNode;
    }

    public EnumerationClosedNode addClosedEnumToLibrary(LibraryNode ln, String name) {
        if (name.isEmpty())
            name = "TestClosed";
        EnumerationClosedNode newNode =
            (EnumerationClosedNode) NodeFactory.newLibraryMember( new TLClosedEnumeration() );
        newNode.setName( name );
        ln.addMember( newNode );
        newNode.addLiteral( name + "Lit1" );
        return newNode;
    }

    /**
     * Create a simple type with assigned simple type (date)
     */
    public SimpleTypeNode createSimple(String name) {
        SimpleTypeNode n2 = new SimpleTypeNode( new TLSimple() );
        n2.setName( name );
        n2.setAssignedType( getSimpleTypeProvider() );
        return n2;
    }

    /**
     * Create a complex type (core object) with property assigned to simple type (date)
     */
    public FacetOwner createComplex(String name) {
        CoreObjectNode n2 = new CoreObjectNode( new TLCoreObject() );
        n2.setName( name );
        new ElementNode( n2.getFacet_Summary(), name + "Property", getSimpleTypeProvider() );
        return n2;
    }

    /**
     * @param xsd if true, return the xsd not ota2 library <br>
     *        xsd : XMLSchema <br>
     *        ota2 : OTM_BuiltIns.xsd
     */
    public LibraryNode getBuiltInLibrary(boolean xsd) {
        LibraryNode builtIn = null;
        for (LibraryNode ln : Node.getAllLibraries())
            if (ln.isBuiltIn())
                if (xsd && ln.getPrefix().equals( "xsd" ))
                    builtIn = ln;
                else if (!xsd && ln.getPrefix().equals( "ota2" ))
                    builtIn = ln;
        assertTrue( builtIn != null );
        return builtIn;
    }

    /**
     * @return an XSD simple (date) assigned to type provider
     */
    public TypeProvider getSimpleTypeProvider() {
        return (TypeProvider) NodeFinders.findNodeByName( XsdDATE, ModelNode.XSD_NAMESPACE );
    }

    /**
     * @param ln - library to add extension point to
     * @param eln - library containing business object to extend. must be different.
     * @return
     */
    public ExtensionPointNode addEP(LibraryNode ln, LibraryNode eln) {
        FacetInterface facet = null;
        for (LibraryMemberInterface d : eln.getDescendants_LibraryMembers())
            if (d instanceof BusinessObjectNode)
                facet = ((BusinessObjectNode) d).getFacet_Summary();
        return addExtensionPoint( ln, facet );
    }

    /**
     * 
     * @param ln
     * @param facet - facet in different library to extend.
     * @return
     */
    public ExtensionPointNode addExtensionPoint(LibraryNode ln, FacetInterface facet) {
        if (ln == ((Node) facet).getLibrary())
            LOGGER.warn( "Adding extension point to same library as referenced facet." );
        ExtensionPointNode ep = new ExtensionPointNode( new TLExtensionPointFacet() );
        ln.addMember( ep );
        ep.setExtension( (Node) facet );
        return ep;
    }

    /**
     * Add for properties to the passed facet. No IDs.
     * 
     * @param facet parent for the new properties
     * @param suffix added to name
     * @return list of new properties
     */
    public List<PropertyNode> addProperties(FacetInterface facet, String suffix) {
        List<PropertyNode> newProperties = new ArrayList<>();
        newProperties.add( new ElementNode( facet, "E" + suffix, getXsdDate() ) );
        newProperties.add( new AttributeNode( facet, "A" + suffix, getXsdDecimal() ) );
        newProperties.add( new IndicatorNode( facet, "Ind" + suffix ) );
        newProperties.add( new IndicatorElementNode( facet, "TestIndicatorEle" + suffix ) );
        return newProperties;
    }

    /**
     * Create a new resource using the passed BO in the BO's library.
     */
    public ResourceNode addResource(BusinessObjectNode bo) {
        ResourceNode resource = new ResourceNode( bo.getLibrary(), bo );
        new ResourceBuilder().build( resource, bo );
        return resource;
    }

    public void printValidationFindings(Node n) {
        if (n == null)
            return;
        ValidationFindings findings = n.validate();
        if (findings == null)
            return;
        for (ValidationFinding finding : findings.getAllFindingsAsList())
            LOGGER.debug( finding.getFormattedMessage( FindingMessageFormat.DEFAULT ) );
    }

    public void printListners(Node node) {
        for (ModelElementListener tl : node.getTLModelObject().getListeners())
            if (tl instanceof BaseNodeListener) {
                LOGGER.debug( "Listener on " + node + " of type " + tl.getClass().getSimpleName() + " GetNode() = "
                    + ((BaseNodeListener) tl).getNode() );
                if (((BaseNodeListener) tl).getNode().isDeleted())
                    LOGGER.debug( ((BaseNodeListener) tl).getNode() + " is deleted." );
            }
    }

    /**
     * Check and validate the entire model.
     * 
     * @param node
     */
    public void check() {
        check( Node.getModelNode(), true );
    }

    /**
     * Run node object specific tests then validate.
     * 
     * @param node
     */
    public void check(Node node) {
        check( node, true );
    }

    /**
     * Run node object specific tests.
     * 
     * @param node
     * @param validate if true run validation
     */
    public void check(Node node, boolean validate) {
        assert (node != null);

        if (node.isDeleted()) {
            LOGGER.debug( "Skipping check of deleted object: " + node );
            return;
        }

        if (node instanceof ModelNode) {
            new ModelNodeTests().check( (ModelNode) node, validate );
            printValidationFindings( node );
            return;
        }
        // TODO - propagate the validate flag to all object check() methods.

        // Validate Identity Listener
        if (!(node instanceof ImpliedNode)) {
            if (!(node instanceof FacadeInterface))
                assertTrue( "Must have identity listener.", Node.GetNode( node.getTLModelObject() ) == node );
            else
                assertTrue( "Facade.get() must return a object with listener",
                    Node.GetNode( node.getTLModelObject() ) == ((FacadeInterface) node).get() );
        }

        // Dispatch to appropriate check method
        if (node instanceof ProjectNode)
            new ProjectNodeTest().check( (ProjectNode) node, validate );
        else if (node instanceof LibraryNavNode)
            new LibraryNavNodeTests().check( (LibraryNavNode) node, validate );
        else if (node instanceof LibraryChainNode)
            new LibraryChainNodeTests().check( (LibraryChainNode) node, validate );
        else if (node instanceof LibraryNode)
            new LibraryNodeTest().check( (LibraryNode) node, validate );
        else if (node instanceof BusinessObjectNode)
            new BusinessObjectTests().check( (BusinessObjectNode) node, validate );
        else if (node instanceof ChoiceObjectNode)
            new ChoiceObjectTests().check( (ChoiceObjectNode) node, validate );
        else if (node instanceof CoreObjectNode)
            new CoreObjectTests().check( (CoreObjectNode) node, validate );
        else if (node instanceof VWA_Node)
            new VWA_Tests().check( (VWA_Node) node );
        else if (node instanceof FacetInterface)
            new FacetsTests().check( (FacetInterface) node, validate );
        else if (node instanceof ServiceNode)
            new ServiceTests().check( (ServiceNode) node );
        else if (node instanceof OperationNode)
            new OperationTests().check( (OperationNode) node );
        else if (node instanceof ResourceNode)
            new ResourceObjectTests().check( (ResourceNode) node );
        else if (node instanceof AliasNode)
            new AliasTests().check( (AliasNode) node );
        else if (node instanceof SimpleTypeNode)
            new SimpleTypeNodeTests().check( (SimpleTypeNode) node, validate );
        else if (node instanceof EnumerationOpenNode)
            new Enumeration_Tests().check( (EnumerationOpenNode) node );
        else if (node instanceof EnumerationClosedNode)
            new Enumeration_Tests().check( (EnumerationClosedNode) node );
        else if (node instanceof ExtensionPointNode)
            new ExtensionPointNode_Tests().check( (ExtensionPointNode) node );
        else if (node instanceof InheritedElementNode)
            new InheritedChildren_Tests().check( (InheritedElementNode) node );
        else if (node instanceof PropertyNode)
            new PropertyNodeTest().check( (PropertyNode) node );
        else
            LOGGER.debug( "TODO - add tests for " + node.getClass().getSimpleName() + " object type." );

        if (validate)
            validate( node );
    }

    public void validate(Node node) {
        // Projects will not validate.
        if (node instanceof ProjectNode)
            return;
        if (!node.isValid()) {
            ValidationFindings findings = node.validate();
            if (findings == null || findings.isEmpty()) {
                LOGGER.debug( node + " is not valid but has no findings." );
                node.isValid();
            }
            if (node.getOwningComponent() == null)
                LOGGER.debug( "Null node owner - can't print validation findings." );
            printValidationFindings( (Node) node.getOwningComponent() );
            printValidationFindings( node );
            // LibraryNode l = node.getLibrary();
            assertTrue( "Node must be valid.", node.isValid() );
        }

    }

    /**
     * @see #addProperties(FacetInterface, String)
     * @param facet
     */
    public void addAllProperties(FacetInterface facet) {
        new AttributeNode( facet, "a1" );
        new AttributeNode( facet, "a2" );
        new AttributeNode( facet, "a3" );
        new IdNode( facet, "id1" );
        new IdNode( facet, "id2" );
        new IdNode( facet, "id3" );
        new ElementNode( facet, "E1" );
        new ElementNode( facet, "E2" );
        new ElementNode( facet, "E3" );
        new IndicatorNode( facet, "i1" );
        new IndicatorNode( facet, "i2" );
        new IndicatorNode( facet, "i3" );
        new IndicatorElementNode( facet, "Ie1" );
        new IndicatorElementNode( facet, "Ie2" );
        new IndicatorElementNode( facet, "Ie3" );
    }

    public void addAllProperties(FacetInterface facet, String suffix, TypeProvider type) {
        new AttributeNode( facet, "a1" + suffix, type );
        new AttributeNode( facet, "a2" + suffix, type );
        new AttributeNode( facet, "a3" + suffix, type );
        // new IdNode(facet, "id1" + suffix);
        new ElementNode( facet, "E1" + suffix, type );
        new ElementNode( facet, "E2" + suffix, type );
        new ElementNode( facet, "E3" + suffix, type );
        new IndicatorNode( facet, "i1" + suffix );
        new IndicatorNode( facet, "i2" + suffix );
        new IndicatorNode( facet, "i3" + suffix );
        new IndicatorElementNode( facet, "Ie1" + suffix );
        new IndicatorElementNode( facet, "Ie2" + suffix );
        new IndicatorElementNode( facet, "Ie3" + suffix );
    }

    public TLLibrary createTLLibrary(String name, String ns) {
        return new LibraryNodeTest().createTL( name, ns );
    }

    public ServiceNode addService(LibraryNode ln, String name) {
        assert ln != null;
        assert ln.getTLModelObject() != null;

        TLService tlSvc = new TLService();
        if (name != null)
            tlSvc.setName( name );
        ((TLLibrary) ln.getTLModelObject()).setService( tlSvc );
        return new ServiceNode( tlSvc, ln );
    }

    public ContextualFacetNode addChoiceFacet(LibraryNode lib, String name, ChoiceObjectNode choiceObject) {
        ChoiceFacetNode cfn = new ChoiceFacetNode();
        lib.addMember( cfn );
        cfn.setName( name );
        new AttributeNode( cfn, "a1" + name, getXsdString() );
        new ElementNode( cfn, "E1" + name, getXsdDate() );
        cfn.setOwner( choiceObject );
        return cfn;
    }

    public ContextualFacetNode addCustomFacet(LibraryNode lib, String name, BusinessObjectNode object) {
        CustomFacetNode cfn = new CustomFacetNode();
        lib.addMember( cfn );
        cfn.setName( name );
        new AttributeNode( cfn, "a1" + name, getXsdString() );
        new ElementNode( cfn, "E1" + name, getXsdDate() );
        cfn.setOwner( object );
        return cfn;
    }

    public ContextualFacetNode addQueryFacet(LibraryNode lib, String name, BusinessObjectNode object) {
        QueryFacetNode cfn = new QueryFacetNode();
        lib.addMember( cfn );
        cfn.setName( name );
        new AttributeNode( cfn, "a1" + name, getXsdString() );
        new ElementNode( cfn, "E1" + name, getXsdDate() );
        cfn.setOwner( object );
        return cfn;
    }

}
