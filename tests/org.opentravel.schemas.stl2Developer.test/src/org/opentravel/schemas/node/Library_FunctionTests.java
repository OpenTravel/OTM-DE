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
/**
 * 
 */

package org.opentravel.schemas.node;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Test;
import org.opentravel.schemacompiler.model.TLAdditionalDocumentationItem;
import org.opentravel.schemacompiler.model.TLContext;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.saver.LibrarySaveException;
import org.opentravel.schemas.node.interfaces.ExtensionOwner;
import org.opentravel.schemas.node.interfaces.InheritedInterface;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNavNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.properties.AttributeNode;
import org.opentravel.schemas.node.properties.ElementNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.typeProviders.ChoiceObjectNode;
import org.opentravel.schemas.node.typeProviders.SimpleTypeNode;
import org.opentravel.schemas.node.typeProviders.VWA_Node;
import org.opentravel.schemas.node.typeProviders.facetOwners.BusinessObjectNode;
import org.opentravel.schemas.node.typeProviders.facetOwners.CoreObjectNode;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.testUtils.MockLibrary;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeUser;
import org.opentravel.schemas.utils.BaseProjectTest;
import org.opentravel.schemas.utils.ComponentNodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Dave Hollander
 * 
 */
// TODO - validate assertion: Every new library is obliged to register itself
// within the
// NamespaceHandler.

public class Library_FunctionTests extends BaseProjectTest {
    private static final Logger LOGGER = LoggerFactory.getLogger( Library_FunctionTests.class );

    private LoadFiles lf = new LoadFiles();
    private MockLibrary ml = new MockLibrary();

    private void assertTypeAssigments(Node moved, PropertyNode withAssignedType) {
        if (withAssignedType instanceof TypeUser) {
            // make sure that after move assigned pointing to the same node
            Assert.assertSame( moved, withAssignedType.getType() );
            // make sure that after move TLObjects are pointing to the same TLType
            Assert.assertSame( moved.getTLModelObject(), ((TypeUser) withAssignedType).getAssignedTLObject() );
        }
    }

    // TODO - verify isEmpty() behavior

    @Test
    public void shouldNotDuplicatedContextOnImport() throws LibrarySaveException {
        LibraryNode importFrom =
            ml.createNewLibrary_Empty( testProject.getNamespace() + "/Test/One", "ImportFrom", testProject );

        TLContext c = new TLContext();
        c.setContextId( "ContextID" );
        c.setApplicationContext( "newContext" );
        ((TLLibrary) importFrom.getTLModelObject()).addContext( c );
        importFrom.addContexts();
        // FIXME - addCustomFacet
        // BusinessObjectNode bo = ComponentNodeBuilder.createBusinessObject("BO").addCustomFacet("name")
        // .addCustomFacet("name2").get(); // 9/2016 - custom facets no longer have context
        // importFrom.addMember(bo);

        // LibraryNode importTo = LibraryNodeBuilder.create("ImportTo", testProject.getNamespace() + "/Test/TO", "to",
        // new Version(1, 0, 0)).build(testProject, pc);

        // List<String> beforeImport = importTo.getContextIds();
        // List<String> fromContexts = importFrom.getContextIds();
        // importTo.importNode(bo);
        // List<String> afterImport = importTo.getContextIds();

        // FIXME - Assert.assertEquals(2, importTo.getContextIds().size());
        // assertTrue("Context must not be imported.", importTo.getTLModelObject().getContext(c.getContextId()) ==
        // null);
    }

    @Test
    public void moveNodeFromOneToOther() throws LibrarySaveException {
        // Given - move from and moved libraries
        TypeProvider stype = ml.getXsdString();
        assertTrue( stype != null );
        LibraryNode moveFrom = ml.createNewLibrary_Empty( testProject.getNamespace() + "/Test1", "From", testProject );
        SimpleTypeNode moved = ml.addSimpleTypeToLibrary( moveFrom, "MyString" );
        moved.setAssignedType( stype );
        moveFrom.addMember( moved );

        BusinessObjectNode bo = ml.addBusinessObjectToLibrary_Empty( moveFrom, "MyBO" );
        new ElementNode( bo.getFacet_ID(), "E1" ).setAssignedType( moved );
        moveFrom.addMember( bo );

        LibraryNode moveTo =
            ml.createNewLibrary_Empty( testProject.getNamespace() + "/Test/TO", "MoveTo", testProject );

        moveFrom.moveMember( moved, moveTo );

        assertTrue( moveTo == moved.getLibrary() );
        // FIXME - assertTypeAssigments(moved, withAssignedType);
    }

    @Test
    public void LN_ServiceUnmangedMethodTests() throws LibrarySaveException {
        MockLibrary ml = new MockLibrary();
        LibraryNode ln = ml.createNewLibrary( pc, "SvcTests" );

        assertTrue( !ln.hasService() );
        assertTrue( ln.getServiceRoot() instanceof NavNode );
        assertTrue( ln.getService() == null );

        // When service is added
        ServiceNode svc = ml.addService( ln, "TestService" );
        // Then
        assertTrue( ln.hasService() );
        assertTrue( ln.getServiceRoot() instanceof NavNode );
        assertTrue( ln.getService() == svc );
    }

    @Test
    public void LN_ServiceMangedMethodTests() throws LibrarySaveException {
        // Given a managed library
        MockLibrary ml = new MockLibrary();
        LibraryNode ln = ml.createNewLibrary( pc, "SvcTests" );
        LibraryChainNode lcn = new LibraryChainNode( ln );
        // Then - no service created
        assertTrue( !ln.hasService() );
        assertTrue( ln.getServiceRoot() instanceof NavNode );
        assertTrue( ln.getService() == null );

        // When service is added
        ServiceNode svc = ml.addService( ln, "TestService" );
        // Then
        assertTrue( ln.hasService() );
        assertTrue( ln.getServiceRoot() instanceof NavNode );
        assertTrue( ln.getService() == svc );
    }

    @Test
    public void libraryDocumentationTests() throws LibrarySaveException {
        String string1 = "This is a test.";
        MockLibrary ml = new MockLibrary();
        LibraryNode ln = ml.createNewLibrary( pc, "DocTest" );

        // When description added
        assertTrue( ln.getDocHandler() == null );
        // ln.getDocHander().addDescription(string1);
        // // then it can be read
        // String s = ln.getDescription();
        // assertTrue(s.equals(string1));

        // TODO - save, close, open and read
    }

    // 1/3/2018 - runs green when run from file but error when runAll
    @Test
    public void LN_moveComplexTests() throws LibrarySaveException {
        // Given
        LibraryNode moveFrom =
            ml.createNewLibrary_Empty( defaultProject.getNamespace() + "/Test/One", "MoveFrom", defaultProject );
        LibraryNode moveTo =
            ml.createNewLibrary_Empty( defaultProject.getNamespace() + "/Test/To", "MoveTo", defaultProject );

        // Given - a set of complex objects
        BusinessObjectNode bo = ml.addBusinessObjectToLibrary( moveFrom, "BO1" );
        ChoiceObjectNode ch = ml.addChoice( moveFrom, "CH1" );
        VWA_Node vwa = ml.addVWA_ToLibrary( moveFrom, "VWA1" );
        CoreObjectNode co = ml.addCoreObjectToLibrary( moveFrom, "CO" );
        // Given - an attribute with an assigned type named
        final String AttrName = "att1";
        PropertyNode withAssignedType = new AttributeNode( co.getFacet_Default(), AttrName, co );

        for (LibraryMemberInterface obj : moveFrom.getDescendants_LibraryMembers())
            moveFrom.moveMember( (Node) obj, moveTo );

        withAssignedType = (PropertyNode) co.getFacet_Default().findChildByName( AttrName );
        assertTypeAssigments( co, (PropertyNode) co.getFacet_Default().findChildByName( AttrName ) );
    }

    /**
     * ImportNodes uses importNode(source). This test focuses just on the clone and library add function of
     * importNode().
     * 
     * @throws LibrarySaveException
     */
    @Test
    public void importNode_Tests() throws LibrarySaveException {
        // given
        MockLibrary ml = new MockLibrary();
        // LibraryNode moveFrom = LibraryNodeBuilder.create("MoveFrom", testProject.getNamespace() + "/Test/One", "o1",
        // new Version(1, 0, 0)).build(testProject, pc);
        // CoreObjectNode coBase = ComponentNodeBuilder.createCoreObject("COBase").get(moveFrom);
        // ElementNode e1 = new ElementNode(new TLProperty(), coBase.getFacet_Summary());
        // e1.setAssignedType(ml.getSimpleTypeProvider());
        // coBase.addProperty(e1);
        // CoreObjectNode coExt = ComponentNodeBuilder.createCoreObject("COExt").extend(coBase).get(moveFrom);
        // assertTrue(coExt.isInstanceOf(coBase));
        // LibraryNode moveTo = LibraryNodeBuilder.create("MoveTo", testProject.getNamespace() + "/Test/TO", "to",
        // new Version(1, 0, 0)).build(testProject, pc);
        // assertTrue("Move to library must be editable.", moveTo.isEditable());

        LibraryNode moveFrom =
            ml.createNewLibrary_Empty( testProject.getNamespace() + "/Test/From", "MoveFrom", testProject );
        CoreObjectNode coBase = ml.addCoreObjectToLibrary( moveFrom, "COBase" );
        ElementNode e1 = new ElementNode( new TLProperty(), coBase.getFacet_Summary() );
        e1.setAssignedType( ml.getSimpleTypeProvider() );
        CoreObjectNode coExt = ml.addCoreObjectToLibrary( moveFrom, "COExt" );
        coExt.setExtension( coBase );
        assertTrue( coExt.isInstanceOf( coBase ) );
        assertTrue( moveFrom.getDescendants_LibraryMembers().contains( coBase ) );
        assertTrue( moveFrom.getDescendants_LibraryMembers().contains( coExt ) );

        LibraryNode moveTo =
            ml.createNewLibrary_Empty( testProject.getNamespace() + "/Test/TO", "MoveTo", testProject );
        assertTrue( "Move to library must be editable.", moveTo.isEditable() );
        CoreObjectNode coTo = ml.addCoreObjectToLibrary( moveTo, "COTO" );
        assertTrue( moveTo.getDescendants_LibraryMembersAsNodes().contains( coTo ) );

        // when
        Node newNode = moveTo.importNode( coBase );

        // then - cloned node must be in target library
        assertTrue( "Imported noded must not be null.", newNode != null );
        assertTrue( moveTo.getDescendants_LibraryMembers().contains( newNode ) );

        // when
        newNode = moveTo.importNode( coExt );

        // then - cloned node must be in target library
        assertTrue( newNode != null );
        assertTrue( moveTo.getDescendants_LibraryMembers().contains( newNode ) );
        assertTrue( newNode.isInstanceOf( coBase ) ); // should have cloned extension
        // NOTE - no type resolution has happened yet so where extended will not be set.

        // TODO - check contexts

        // Given - business object with custom and query facets
        BusinessObjectNode bo1 = ml.addBusinessObjectToLibrary( moveFrom, "MoveThisBO" );
        assertTrue( "Business object must have custom facet.", !bo1.getCustomFacets().isEmpty() );
        // When - imported
        BusinessObjectNode movedBO = (BusinessObjectNode) moveTo.importNode( bo1 );

        // Then - imported node must also have custom and query facets
        assertTrue( "Business object must have custom facet.", !movedBO.getCustomFacets().isEmpty() );
    }

    @Test
    public void importNodesLocallyShouldReplaceBaseTypes() throws LibrarySaveException {
        // given
        LibraryNode moveFrom =
            ml.createNewLibrary_Empty( testProject.getNamespace() + "/Test/One", "MoveFrom", testProject );
        CoreObjectNode coBase = ComponentNodeBuilder.createCoreObject( "COBase" ).get( moveFrom );
        CoreObjectNode coExt = ComponentNodeBuilder.createCoreObject( "COExt" ).extend( coBase ).get( moveFrom );
        assertTrue( coExt.isInstanceOf( coBase ) );

        LibraryNode importTo =
            ml.createNewLibrary_Empty( testProject.getNamespace() + "/Test/TO", "MoveTo", testProject );

        // when
        importTo.importNodes( moveFrom.getDescendants_LibraryMembersAsNodes(), false );

        // then
        assertEquals( 2, importTo.getDescendants_LibraryMembers().size() );
        Node newBase = importTo.findLibraryMemberByName( "COBase" );
        Node newExt = importTo.findLibraryMemberByName( "COExt" );
        assertTrue( "Original extension must extend original base.", coExt.isInstanceOf( coBase ) );
        assertTrue( "Imported extension must extend imported base.", newExt.isInstanceOf( newBase ) );
        assertTrue( "Imported must have new base.", ((ExtensionOwner) newExt).getExtensionBase() == newBase );
    }

    @Test
    public void importNodesGloballyShouldReplaceBaseTypes() throws LibrarySaveException {
        // Given
        LibraryNode moveFrom =
            ml.createNewLibrary_Empty( testProject.getNamespace() + "/Test/One", "MoveFrom", testProject );
        CoreObjectNode coBase = ComponentNodeBuilder.createCoreObject( "COBase" ).get( moveFrom );
        CoreObjectNode coExt = ComponentNodeBuilder.createCoreObject( "COExt" ).extend( coBase ).get( moveFrom );
        assertTrue( coExt.isInstanceOf( coBase ) );

        LibraryNode importTo =
            ml.createNewLibrary_Empty( testProject.getNamespace() + "/Test/TO", "MoveTo", testProject );
        assertTrue( "Import to library must be editable.", importTo.isEditable() );

        // when - global import
        importTo.importNodes( moveFrom.getDescendants_LibraryMembersAsNodes(), true );

        // then
        assertEquals( 2, importTo.getDescendants_LibraryMembers().size() );
        Node newBase = importTo.findLibraryMemberByName( "COBase" );
        Node newExt = importTo.findLibraryMemberByName( "COExt" );
        boolean x = newExt.isInstanceOf( newBase );
        boolean y = newBase.isInstanceOf( newExt );
        boolean z = coExt.isInstanceOf( newBase ); // true for global

        assertFalse( "New extension must NOT be to old base.", newExt.isInstanceOf( coBase ) );
        assertTrue( "New extension must be to new base.", newExt.isInstanceOf( newBase ) );
        assertTrue( "Global import must change base type.", coExt.isInstanceOf( newBase ) );
    }

    /**
     * @throws LibrarySaveException
     */
    @Test
    public void LN_importNodesReplaceTypes() throws LibrarySaveException {
        // Given - three libraries
        LibraryNode sourceLib = ml.createNewLibrary( "http://test.com/ns1", "FromLib", defaultProject );
        LibraryNode localLib = ml.createNewLibrary( "http://test.com/ns2", "LocalLib", defaultProject );
        LibraryNode globalLib = ml.createNewLibrary( "http://test.com/ns3", "GlobalLib", defaultProject );
        assertTrue( globalLib.isEditable() );
        assertTrue( localLib.isEditable() );
        assertTrue( sourceLib.isEditable() );

        // Given - two core objects, one extends the other
        CoreObjectNode coBase = ml.addCoreObjectToLibrary( sourceLib, "COBase" );
        CoreObjectNode coExt = ml.addCoreObjectToLibrary( sourceLib, "COExt" );
        coExt.setExtension( coBase );
        ElementNode e1 = new ElementNode( coExt.getFacet_Summary(), "Element1" );
        e1.setAssignedType( coBase );

        //
        // When - local import of both core objects
        localLib.importNodes( sourceLib.getDescendants_LibraryMembersAsNodes(), false );

        // Then - core objects are still in their original libraries.
        assertTrue( sourceLib.contains( coBase ) );
        assertTrue( sourceLib.contains( coExt ) );

        // Then - core objects have been moved
        CoreObjectNode importedBase = (CoreObjectNode) localLib.findLibraryMemberByName( "COBase" );
        CoreObjectNode importedExt = (CoreObjectNode) localLib.findLibraryMemberByName( "COExt" );
        assertTrue( importedBase != null );
        assertTrue( importedExt != null );
        assertTrue( localLib.contains( importedBase ) );
        assertTrue( localLib.contains( importedExt ) );
        assertTrue( importedBase.getLibrary() == localLib );
        assertTrue( importedExt.getLibrary() == localLib );

        // Then - assignment is still correct for source
        e1 = (ElementNode) coExt.getFacet_Summary().findChildByName( coBase.getName() );
        assertTrue( "Must find element in core extension.", e1 != null );
        TypeProvider at = e1.getAssignedType();
        assertTrue( "Element must still be assigned coBase.", coBase == e1.getAssignedType() );

        // Then - assignment is still correct-local import must change since both were imported
        e1 = (ElementNode) importedExt.getFacet_Summary().findChildByName( importedBase.getName() );
        assertTrue( "Must find element in core extension.", e1 != null );
        assertTrue( "Element must still be assigned imported.", importedBase == e1.getAssignedType() );

        //
        // When - global import of both core objects
        globalLib.importNodes( sourceLib.getDescendants_LibraryMembersAsNodes(), true );
        CoreObjectNode globalBase = (CoreObjectNode) globalLib.findLibraryMemberByName( "COBase" );
        CoreObjectNode globalExt = (CoreObjectNode) globalLib.findLibraryMemberByName( "COExt" );

        // Then - assignment in sourceLib must be to global
        e1 = (ElementNode) coExt.getFacet_Summary().findChildByName( globalBase.getName() );
        assertTrue( "Must find element in core extension.", e1 != null );
        at = e1.getAssignedType();
        assertTrue( "Element must still be assigned global.", globalBase == e1.getAssignedType() );

        // Then - assignment in localLib must NOT change because it is to a different object, not coBase
        e1 = (ElementNode) importedExt.getFacet_Summary().findChildByName( globalBase.getName() );
        assertTrue( "Must find element in core extension.", e1 != null );
        at = e1.getAssignedType();
        assertTrue( "Element must still be assigned imported.", importedBase == e1.getAssignedType() );

        // Then - assignment in global is to global
        e1 = (ElementNode) globalExt.getFacet_Summary().findChildByName( globalBase.getName() );
        assertTrue( "Must find element in core extension.", e1 != null );
        assertTrue( "Element must still be assigned global.", globalBase == e1.getAssignedType() );
    }

    // @Test
    // public void checkBuiltIns() {
    // for (INode n : Node.getAllLibraries()) {
    // Assert.assertTrue(n instanceof LibraryNode);
    // visitLibrary((LibraryNode) n);
    // }
    // }
    //
    // @Test
    // public void checkLibraries() throws Exception {
    // LibraryNode l1 = lf.loadFile1(mc);
    // visitLibrary(l1);
    //
    // // testNewWizard((ProjectNode) l1.getProject());
    //
    // lf.loadFile2(mc);
    // lf.loadFile3(mc);
    // lf.loadFile4(mc);
    // lf.loadFile5(mc);
    //
    // for (LibraryNode ln : Node.getAllLibraries())
    // visitLibrary(ln);
    //
    // // If not editable,most of the other tests will fail.
    // for (LibraryNode ln : Node.getAllUserLibraries()) {
    // ln.setEditable(true);
    // assertTrue(ln.isEditable());
    // assertFalse(ln.getPath().isEmpty());
    // assertTrue(ln.getNamespace().equals(ln.getTLaLib().getNamespace()));
    // assertTrue(ln.getPrefix().equals(ln.getTLaLib().getPrefix()));
    // }
    //
    // // Make sure we can create new empty libraries as used by wizard
    // LibraryNode newLib = new LibraryNode(l1.getProject());
    // assertTrue(newLib != null);
    //
    // for (LibraryNode ln : Node.getAllLibraries())
    // removeAllMembers(ln);
    // }

    // private void removeAllMembers(LibraryNode ln) {
    // for (Node n : ln.getDescendants_LibraryMembers())
    // ln.removeMember(n); // May change type assignments!
    //
    // Assert.assertTrue(ln.getDescendants_LibraryMembers().size() < 1);
    // }

    /**
     * Check the library. Checks library structures then all children. Asserts error if the library is empty!
     * 
     * @param ln
     */
    protected void visitLibrary(LibraryNode ln) {
        if (ln.isXSDSchema()) {
            assertTrue( ln.getGeneratedLibrary() != null );
            assertTrue( ln.hasGeneratedChildren() );
        }
        assertTrue( ln.getChildren().size() > 1 );
        assertTrue( ln.getDescendants_LibraryMembers().size() > 1 );

        if (ln.getName().equals( "OTA2_BuiltIns_v2.0.0" )) {
            Assert.assertEquals( 85, ln.getDescendants_LibraryMembers().size() );
        }

        if (ln.getName().equals( "XMLSchema" )) {
            Assert.assertEquals( 20, ln.getDescendants_LibraryMembers().size() );
        }

        if (!ln.isInChain()) {
            assertTrue( ln.getChildren().size() == ln.getNavChildren( false ).size() );
            assertTrue( ln.getParent() instanceof LibraryNavNode );
            assertTrue( "Must have at least one related project.", ln.getProject() != null );
        } else {
            assertTrue( ln.getParent() instanceof VersionAggregateNode );
            assertTrue( ln.getChain().getParent() instanceof LibraryNavNode );
            // What about size? == 0
            assertTrue( "Chain members do not have navChildren.", ln.getNavChildren( false ).size() == 0 );
        }

        Assert.assertNotNull( ln.getTLaLib() );

        Assert.assertFalse( ln.getName().isEmpty() );
        Assert.assertFalse( ln.getNamespace().isEmpty() );
        Assert.assertFalse( ln.getPrefix().isEmpty() );

        if (ln.isTLLibrary()) {
            Assert.assertFalse( ln.getContextIds().isEmpty() );
        }

        Assert.assertFalse( ln.getPath().isEmpty() );

        for (Node n : ln.getChildren()) {
            visitNode( n );
        }
    }

    public void visitNode(Node n) {
        // LOGGER.debug("Visit Node: " + n + " of type " + n.getClass().getSimpleName());
        Assert.assertNotNull( n );
        Assert.assertNotNull( n.getParent() );
        Assert.assertNotNull( n.getLibrary() );
        // Assert.assertNotNull(n.modelObject);
        Assert.assertNotNull( n.getTLModelObject() );
        // Assert.assertTrue(n.getTypeClass().verifyAssignment());

        // Assert.assertNotNull(n.getTypeClass());
        if (n instanceof TypeUser) {
            // LOGGER.debug("Visit Node: " + n + " of type " + n.getClass().getSimpleName());
            // boolean x = n.getTypeClass().verifyAssignment();
            // Resolver may not have run
            // Assert.assertNotNull(n.getType());
            Assert.assertEquals( n.getType(), ((TypeUser) n).getAssignedType() );
        }

        if (n.getName().isEmpty())
            LOGGER.debug( "no name" );
        Assert.assertFalse( n.getName().isEmpty() );
        for (Node nn : n.getChildren()) {
            visitNode( nn );
        }
    }

    @Test
    public void LN_statusTests() {
        LibraryNode ln = lf.loadFile5Clean( mc );

        Assert.assertEquals( ln.getEditStatus(), NodeEditStatus.NOT_EDITABLE );
        Assert.assertFalse( ln.getEditStatusMsg().isEmpty() );
        Assert.assertFalse( ln.isManaged() );
        Assert.assertFalse( ln.isLocked() );
        Assert.assertFalse( ln.isInProjectNS() );
        Assert.assertTrue( ln.isMajorVersion() );
        Assert.assertTrue( ln.isMinorOrMajorVersion() );
        Assert.assertFalse( ln.isPatchVersion() );

        ln.setNamespace( ln.getProject().getNamespace() + "test/v1_2_3" );
        Assert.assertNotNull( ln.getNsHandler() );
        String n = ln.getNamespace();
        Assert.assertFalse( ln.getNamespace().isEmpty() );
        n = ln.getNSExtension();
        Assert.assertTrue( ln.getNSExtension().equals( "test" ) );
        n = ln.getNSVersion();
        Assert.assertTrue( ln.getNSVersion().equals( "1.2.3" ) );
        Assert.assertTrue( ln.isPatchVersion() );

        ln.setNamespace( ln.getProject().getNamespace() + "test/v1_2" );
        n = ln.getNSVersion();
        Assert.assertTrue( ln.getNSVersion().equals( "1.2.0" ) );
        Assert.assertTrue( ln.isMinorOrMajorVersion() );
    }

    @Test
    public void checkNS() {

    }

    @Test
    public void LN_contextCollapseTests() {
        // LibraryChainNode fromChain = ml.createNewManagedLibrary("FromChain", defaultProject);
        // LibraryNode fromLib = fromChain.getHead();
        LibraryNode fromLib = ml.createNewLibrary( "http://test.com/ns1", "FromLib", defaultProject );
        LibraryNode toLib = ml.createNewLibrary( "http://test.com/ns2", "ToLib", defaultProject );
        Assert.assertTrue( fromLib.isEditable() );

        // Check initial library contexts
        List<TLContext> fromLibContexts = fromLib.getTLLibrary().getContexts();
        assertTrue( "Must only have 1 context.", fromLibContexts.size() == 1 );
        List<TLContext> toLibContexts = toLib.getTLLibrary().getContexts();
        assertTrue( "Must only have 1 context.", toLibContexts.size() == 1 );

        // When - Add context users
        PropertyNode pn = addContextUsers( fromLib );
        // Then - check context users
        Assert.assertEquals( 2, fromLibContexts.size() ); // default and other doc
        String appContext1 = pn.getExampleHandler().getApplicationContext();
        Assert.assertTrue( appContext1.startsWith( "http://test.com/ns1" ) );

        // Add another Context to TL Library
        TLContext tlc = new TLContext();
        tlc.setApplicationContext( "AppContext1" );
        tlc.setContextId( "Cid1" );
        fromLib.getTLLibrary().addContext( tlc );
        Assert.assertEquals( 3, fromLibContexts.size() );

        fromLib.collapseContexts();
        Assert.assertEquals( 1, fromLibContexts.size() );
        String appContext2 = pn.getExampleHandler().getApplicationContext();
        Assert.assertFalse( appContext2.isEmpty() );
        Assert.assertTrue( appContext2.startsWith( "http://test.com/ns1" ) );

        // // moveNamedMember will create contexts if the object has a context not already in destination library
        // Node object = (Node) pn.getOwningComponent();
        // try {
        // object.getLibrary().getTLLibrary()
        // .moveNamedMember((LibraryMember) object.getTLModelObject(), toLib.getLibrary().getTLLibrary());
        // } catch (Exception e) {
        // LOGGER.debug("moveNamedMember failed. " + e.getLocalizedMessage());
        // }
        // String appContext3 = pn.getExampleHandler().getApplicationContext();
        // Assert.assertTrue(appContext3.startsWith("http://test.com/ns1")); // app context copied on moveNamedMember
        // Assert.assertEquals(2, toLibContexts.size());

        //
        toLib.collapseContexts();
        Assert.assertEquals( 1, fromLibContexts.size() );
        Assert.assertEquals( 1, toLibContexts.size() );
    }

    /**
     * Add example and equivalent to 1st property in the library. Add TLAdditionalDocumentationItem and set context
     * 
     * @param lib
     * @return
     */
    private PropertyNode addContextUsers(LibraryNode lib) {
        Node object = null;
        for (Node n : lib.getDescendants_LibraryMembersAsNodes())
            object = n;
        assertTrue( object != null );
        TypeUser tu = null;
        for (TypeUser n : object.getDescendants_TypeUsers())
            if (n instanceof PropertyNode)
                tu = n;
        Assert.assertNotNull( tu );
        Assert.assertTrue( tu instanceof PropertyNode );
        PropertyNode pn = (PropertyNode) tu;

        // Add example
        pn.setExample( "Ex1" ); // use default context
        assertTrue( pn.getExample( null ).equals( "Ex1" ) );

        // add an equivalent
        pn.setEquivalent( "Eq1" );
        assertTrue( pn.getEquivalent( null ).equals( "Eq1" ) );

        // add an other doc with context - creates a second context
        TLAdditionalDocumentationItem otherDoc = new TLAdditionalDocumentationItem();
        otherDoc.setContext( "OD1" );
        otherDoc.setText( "description in OD1 context" );
        pn.getDocumentation().addOtherDoc( otherDoc );
        return pn;
    }

    // Runs green when run alone or just with the tests in this file. (1/3/2018)
    @SuppressWarnings("unused")
    @Test
    public void LN_moveMemberTests() throws Exception {
        // Given - from and to chains
        LibraryChainNode fromChain = ml.createNewManagedLibrary( "FromChain", defaultProject );
        LibraryChainNode toChain = ml.createNewManagedLibrary( "ToChain", defaultProject );
        LibraryNode fromLib = ml.createNewLibrary( "http://test.com/ns1", "FromLib", defaultProject );
        LibraryNode toLib = ml.createNewLibrary( "http://test.com/ns2", "ToLib", defaultProject );
        assertTrue( fromLib.isEditable() );
        assertTrue( toLib.isEditable() );

        // Then - verify the contextual facets all have libraries assigned
        for (LibraryMemberInterface cf : fromChain.getDescendants_LibraryMembers())
            assertTrue( cf.getLibrary() == fromChain.getHead() );
        for (LibraryMemberInterface cf : toChain.getDescendants_LibraryMembers())
            assertTrue( cf.getLibrary() == toChain.getHead() );
        for (LibraryMemberInterface cf : fromLib.getDescendants_LibraryMembers())
            assertTrue( cf.getLibrary() == fromLib );
        for (LibraryMemberInterface cf : toLib.getDescendants_LibraryMembers())
            assertTrue( cf.getLibrary() == toLib );

        // Then - Verify: Identity listener used in moveMember() so assure it is correct.
        assertTrue( Node.GetNode( toLib.getTLModelObject() ) == toLib );
        assertTrue( Node.GetNode( fromLib.getTLModelObject() ) == fromLib );

        // Given - contexts in the to and from libraries
        List<TLContext> toLibContexts = toLib.getTLLibrary().getContexts();
        List<TLContext> fromLibContexts = fromLib.getTLLibrary().getContexts(); // live list
        Assert.assertEquals( 1, fromLibContexts.size() );

        // When - A new context is used by a property (ex, eq, facet and other doc)
        LibraryMemberInterface object = addContextUsers( fromLib ).getOwningComponent();
        assertTrue( "object must not be null", object != null );
        assertTrue( 2 == fromLibContexts.size() );
        List<Node> kids = object.getChildren();

        // Given - the members in the to library before any moves
        List<LibraryMemberInterface> toMembers = toLib.getDescendants_LibraryMembers();

        // When - move the object from the from library to the to library
        toLib.addMember( object );
        // object.getLibrary().moveMember(object, toLib); // listener removed from toLib

        // Then - check to assure only one context in to-library
        Assert.assertEquals( 2, fromLibContexts.size() );
        Assert.assertEquals( 1, toLibContexts.size() );
        List<LibraryMemberInterface> toMbrs = toLib.getDescendants_LibraryMembers();
        Assert.assertEquals( toMembers.size() + 1, toLib.getDescendants_LibraryMembers().size() ); // BO and CustomFacet

        // // Then - verify the contextual facets all have libraries assigned
        // for (LibraryMemberInterface cf : fromChain.getDescendants_LibraryMembers())
        // assertTrue(cf.getLibrary() == fromChain.getHead());
        // for (LibraryMemberInterface cf : toChain.getDescendants_LibraryMembers())
        // assertTrue(cf.getLibrary() == toChain.getHead());
        // for (LibraryMemberInterface cf : fromLib.getDescendants_LibraryMembers())
        // assertTrue(cf.getLibrary() == fromLib);
        // for (LibraryMemberInterface cf : toLib.getDescendants_LibraryMembers())
        // assertTrue(cf.getLibrary() == toLib);

        //
        // Part 2
        //
        // Load up the old test libraries and move lots and lots of stuff then test to-library
        ModelNode model = mc.getModelNode();
        lf.loadTestGroupA( mc );

        // // Then - verify the contextual facets all have libraries assigned
        // for (LibraryMemberInterface cf : fromChain.getDescendants_LibraryMembers())
        // assertTrue(cf.getLibrary() == fromChain.getHead());
        // for (LibraryMemberInterface cf : toChain.getDescendants_LibraryMembers())
        // assertTrue(cf.getLibrary() == toChain.getHead());
        // for (LibraryMemberInterface cf : fromLib.getDescendants_LibraryMembers())
        // assertTrue(cf.getLibrary() == fromLib);
        // for (LibraryMemberInterface cf : toLib.getDescendants_LibraryMembers())
        // assertTrue(cf.getLibrary() == toLib);
        //
        // LibraryNode fromChainLib = fromChain.getHead();
        // List<LibraryMemberInterface> l1 = fromChain.getDescendants_LibraryMembers();
        // List<LibraryMemberInterface> l2 = fromChainLib.getDescendants_LibraryMembers();
        // for (LibraryMemberInterface cf : l2)
        // assertTrue(cf.getLibrary() == fromChainLib);

        // Identity listener used in moveMember() so assure it is correct.
        // assertTrue(Node.GetNode(toLib.getTLLibrary()) == toLib);
        // int count = toLib.getDescendants_LibraryMembers().size();
        for (LibraryNode ln : model.getUserLibraries()) {
            if (ln != toLib && ln != fromLib) {
                // if (!ln.isInChain())
                // new LibraryChainNode(ln);
                ln.setEditable( true );
                int libCount = ln.getDescendants_LibraryMembers().size();
                assertTrue( Node.GetNode( ln.getTLLibrary() ) == ln );

                List<LibraryMemberInterface> descendants = ln.getDescendants_LibraryMembers();
                for (LibraryMemberInterface n : ln.getDescendants_LibraryMembers()) {
                    if (n instanceof ServiceNode)
                        continue;

                    LOGGER.debug( "Moving " + n + " from " + n.getLibrary() + " to " + toLib );
                    assertTrue( n.getLibrary() != null );
                    // Inherited nodes report the library from where inherited
                    if (!(n instanceof InheritedInterface))
                        assertTrue( n.getLibrary() == ln );
                    assertTrue( n.getLibrary().isEditable() );
                    assertTrue( toLib.isEditable() );
                    try {
                        // When - move the member by adding to new library
                        // n.getLibrary().moveMember((Node) n, toLib);
                        // ml.check((Node) n, true);
                        toLib.addMember( n );
                        libCount--;

                        // Move member bug was changing contextual facet's libraries
                        List<LibraryMemberInterface> l1 = fromChain.getDescendants_LibraryMembers();
                        List<LibraryMemberInterface> l2 = fromChain.getHead().getDescendants_LibraryMembers();
                        for (LibraryMemberInterface cf : fromChain.getDescendants_LibraryMembers())
                            assertTrue( cf.getLibrary() == fromChain.getHead() );

                        // count++;
                        assertTrue( "To Lib must contain moved member.",
                            toLib.getDescendants_LibraryMembers().contains( n ) );

                        // Make sure the node is removed.
                        if (libCount != ln.getDescendants_LibraryMembers().size()) {
                            List<LibraryMemberInterface> dl = ln.getDescendants_LibraryMembers();
                            LOGGER.debug( "Bad Counts: " + dl.size() );
                        }
                        // Assert.assertEquals(--libCount, ln.getDescendants_LibraryMembers().size());

                        // Track toLib count growth - use to breakpoint when debugging
                        // int toCount = toLib.getDescendants_LibraryMembers().size();
                        // if (count != toCount)
                        // LOGGER.debug("Problem with " + n);
                        // count = toCount; // fix the count so the loop can continue
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        LOGGER.debug( "Failed to move " + n + " because " + e.getLocalizedMessage() );
                    }
                }
            }

        }

        Assert.assertEquals( 1, toLibContexts.size() );
        // Fails in AfterEachTest() -- library complex root still has inherited contextual facets
    }

    @Test
    public void LN_addMemberTests() {
        // Given - managed and unmanged libraries
        LibraryNode ln = ml.createNewLibrary_Empty( "http://www.test.com/test1", "testUnmanaged", defaultProject );
        LibraryNode ln_inChain =
            ml.createNewLibrary_Empty( "http://www.test.com/test1c", "testManaged", defaultProject );
        LibraryChainNode lcn = new LibraryChainNode( ln_inChain );
        ln_inChain.setEditable( true );

        // Given - 4 simple type nodes
        SimpleTypeNode s1 = ml.createSimple( "s_1" );
        SimpleTypeNode s2 = ml.createSimple( "s_2" );
        SimpleTypeNode sv1 = ml.createSimple( "sv_1" );
        SimpleTypeNode sv2 = ml.createSimple( "sv_2" );

        // Test un-managed
        ln.addMember( s1 );
        assertEquals( 1, ln.getSimpleRoot().getChildren().size() );
        assertEquals( 1, ln.getDescendants_LibraryMembers().size() );
        ln.addMember( s2 );
        assertEquals( 2, ln.getSimpleRoot().getChildren().size() );
        assertEquals( 2, ln.getDescendants_LibraryMembers().size() );
        assertTrue( ln.contains( s1 ) );

        // Test managed
        ln_inChain.addMember( sv1 );
        ln_inChain.addMember( sv2 );
        assertEquals( 2, ln_inChain.getSimpleRoot().getChildren().size() );
        assertEquals( 2, ln_inChain.getDescendants_LibraryMembers().size() );
        assertEquals( 2, lcn.getSimpleAggregate().getChildren().size() );
        assertTrue( lcn.contains( sv1 ) );

        // When - test adding s1 that is already in a library
        ln_inChain.addMember( s1 );
        // Then - make sure s1 is removed from ln
        assertTrue( "S1 must  be in new library chain", lcn.contains( s1 ) );
        assertTrue( "S1 must NOT be in old library", !ln.contains( s1 ) );
    }
}
