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
import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemas.controllers.DefaultProjectController;
import org.opentravel.schemas.controllers.LibraryController;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.controllers.ProjectController;
import org.opentravel.schemas.node.handlers.DocumentationHandler;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.typeProviders.SimpleTypeNode;
import org.opentravel.schemas.node.typeProviders.VWA_Node;
import org.opentravel.schemas.node.typeProviders.facetOwners.BusinessObjectNode;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.testUtils.MockLibrary;
import org.opentravel.schemas.testUtils.NodeTesters;

/**
 * These tests cover the Node and ModelObject class functions. NOTE: these functions are NOT used in the
 * documentationView. // TODO - use documentation controller/class not node directly. OBSOLETE
 * 
 * See controllers/DocumentationNodeModelManager_Tests
 * 
 * @author Dave Hollander
 * 
 */
public class DocumentationNode_Tests {
    ModelNode model = null;
    NodeTesters nt = new NodeTesters();

    MockLibrary ml = new MockLibrary();
    LibraryNode ln = null;
    MainController mc;
    DefaultProjectController pc;
    ProjectNode defaultProject;
    LoadFiles lf = null;

    @Before
    public void beforeEachTest() {
        mc = OtmRegistry.getMainController();
        pc = (DefaultProjectController) mc.getProjectController();
        defaultProject = pc.getDefaultProject();
        lf = new LoadFiles();
    }

    /**
     * documentation handler is assigned to each node that has documentation.
     */
    @Test
    public void docHandlerTest() {
        // Given an editable business object in a library
        LibraryNode ln = ml.createNewLibrary_Empty( "http://example.com", "TestLib1", defaultProject );
        ln.setEditable( true );
        BusinessObjectNode bo = ml.addBusinessObjectToLibrary( ln, "TestBO" );
        String context = ln.getDefaultContextId();

        // Given 3 test strings
        final String s1 = "String1 test documentation.";
        final String s2 = "String2 test documentation.";
        final String s3 = "String3 test documentation.";

        // Given the doc handler from the BO
        DocumentationHandler dh = bo.getDocHandler();
        // Given a new documentation handler for the BO
        // DocumentationHandler dh = new DocumentationHandler(bo);

        // When/Then - no errors getting empty documentation
        assertTrue( dh.getDescription().isEmpty() );
        assertTrue( dh.getOther() == null );
        assertTrue( dh.getDeprecation( 0 ) == null );
        assertTrue( dh.getImplementer( 0 ) == null );
        assertTrue( dh.getMoreInfo( 0 ) == null );
        assertTrue( dh.getReference( 0 ) == null );

        // When - setting to string s1
        dh.addDescription( s1 );
        dh.addOther( s1 );
        dh.addDeprecation( s1 );
        dh.addImplementer( s1 );
        dh.addMoreInfo( s1 );
        dh.addReference( s1 );
        // Then - get returns s1
        String d = dh.getDescription();
        assertTrue( dh.getDescription().equals( s1 ) );
        assertTrue( dh.getOther().equals( s1 ) );
        assertTrue( dh.getDeprecation( 0 ).equals( s1 ) );
        assertTrue( dh.getImplementer( 0 ).equals( s1 ) );
        assertTrue( dh.getMoreInfo( 0 ).equals( s1 ) );
        assertTrue( dh.getReference( 0 ).equals( s1 ) );

        // When - setting new value
        dh.setDescription( s2 );
        dh.setOther( s2 );
        dh.setDeprecation( s2, 0 );
        dh.setImplementer( s2, 0 );
        dh.setMoreInfo( s2, 0 );
        dh.setReference( s2, 0 );
        // Then - get returns s2
        assertTrue( dh.getDescription().equals( s2 ) );
        assertTrue( dh.getOther().equals( s2 ) );
        assertTrue( dh.getDeprecation( 0 ).equals( s2 ) );
        assertTrue( dh.getImplementer( 0 ).equals( s2 ) );
        assertTrue( dh.getMoreInfo( 0 ).equals( s2 ) );
        assertTrue( dh.getReference( 0 ).equals( s2 ) );
        // Then - get on index 1 is null
        assertTrue( dh.getDeprecation( 1 ) == null );
        assertTrue( dh.getImplementer( 1 ) == null );
        assertTrue( dh.getMoreInfo( 1 ) == null );
        assertTrue( dh.getReference( 1 ) == null );

        // When - add new value
        dh.addDescription( s3 );
        dh.addOther( s3 );
        dh.addDeprecation( s3 );
        dh.addImplementer( s3 );
        dh.addMoreInfo( s3 );
        dh.addReference( s3 );
        // Then - get returns s3
        assertTrue( dh.getDescription().endsWith( s3 ) );
        assertTrue( dh.getOther().endsWith( s3 ) );
        // Then - get on index 0 is unchanged (s2)
        assertTrue( dh.getDeprecation( 0 ).equals( s2 ) );
        assertTrue( dh.getImplementer( 0 ).equals( s2 ) );
        assertTrue( dh.getMoreInfo( 0 ).equals( s2 ) );
        assertTrue( dh.getReference( 0 ).equals( s2 ) );
        // Then - get on index 1 is s3
        assertTrue( dh.getDeprecation( 1 ).equals( s3 ) );
        assertTrue( dh.getImplementer( 1 ).equals( s3 ) );
        assertTrue( dh.getMoreInfo( 1 ).equals( s3 ) );
        assertTrue( dh.getReference( 1 ).equals( s3 ) );

        // Able to use set to create new items
        dh.setDeprecation( s2, 3 );
        dh.setImplementer( s2, 3 );
        dh.setMoreInfo( s2, 3 );
        dh.setReference( s2, 3 );
    }

    //
    // FIXME - add test to assure contexts are "fixed"
    //
    @Test
    public void DOC_fixContextTests() {
        // assert false;
    }

    @Test
    public void coreDescriptionTest() {
        MainController mc = OtmRegistry.getMainController();
        MockLibrary mockLibUtil = new MockLibrary();
        LibraryController lc = mc.getLibraryController();
        ProjectController pc = mc.getProjectController();
        ProjectNode defaultProject = pc.getDefaultProject();
        final String testDescription = "Test Description";

        LibraryNode lib = mockLibUtil.createNewLibrary( defaultProject.getNamespace() + "/Test",
            getClass().getSimpleName(), defaultProject );
        Assert.assertNotNull( lib );
        String path = lib.getPath();
        String name = lib.getName();
        String desc = "";
        String d2 = "";

        // CoreObjectNode core = mockLibUtil.addCoreObjectToLibrary(lib, "Core1");
        // core.addDescription(testDescription);
        // Assert.assertFalse(core.getDescription().isEmpty());
        // lc.saveLibrary(lib, false);
        // // lc.closeLibrary(lib);
        // // TODO - find out why the saved value is "null Test Description"
        //
        // List<File> files = new ArrayList<>();
        // files.add(new File(path));
        // defaultProject.add(files); // FIXME
        // for (LibraryNode ln : defaultProject.getLibraries()) {
        // if (ln.getName().equals(name)) {
        // for (Node n : ln.getDescendentsNamedTypes()) {
        // if (n.getName().equals("Core1")) {
        // desc = n.getDescription();
        // TLCoreObject tlc = (TLCoreObject) n.getTLModelObject();
        // d2 = tlc.getDocumentation().getDescription();
        // Assert.assertNotNull(d2);
        // Assert.assertFalse(desc.isEmpty());
        // }
        // }
        // }
        // }
        testLoadFromFile();
    }

    @Test
    public void testLoadFromFile() {
        MainController mc = OtmRegistry.getMainController();
        LoadFiles lf = new LoadFiles();
        // model = mc.getModelNode();

        // File 1 has documentation in it.
        LibraryNode lib1 = lf.loadFile1( mc );

        // Payment has description
        Node n = lib1.findLibraryMemberByName( "Payment" );
        assertTrue( "Must find payment object.", n != null );
        assertTrue( !n.getDescription().isEmpty() );

        // PaymentCard_MagneticStrip has description, implementor and more info
        n = lib1.findLibraryMemberByName( "PaymentCard_MagneticStrip" );
        assertTrue( !n.getDescription().isEmpty() );
        assertTrue( !n.getDocumentation().getImplementers().isEmpty() );
        assertTrue( !n.getDocumentation().getMoreInfos().isEmpty() );

        // TODO - move to eq/ex junit
        // Date_MMYY has both ex and eq
        n = lib1.findLibraryMemberByName( "Date_MMYY" );
        assertTrue( !((SimpleTypeNode) n).getExample( null ).isEmpty() );
        assertTrue( !((SimpleTypeNode) n).getEquivalent( null ).isEmpty() );

        // Age has both equivalents and examples
        n = lib1.findLibraryMemberByName( "Age" );
        assertTrue( !((VWA_Node) n).getExample( null ).isEmpty() );
        assertTrue( !((VWA_Node) n).getEquivalent( null ).isEmpty() );
    }

    @Test
    public void docTests() throws Exception {
        MainController thisModel = OtmRegistry.getMainController();
        LoadFiles lf = new LoadFiles();
        model = thisModel.getModelNode();

        lf.loadTestGroupA( thisModel );
        for (LibraryNode ln : Node.getAllLibraries()) {
            nt.visitAllNodes( ln );
            getDocs( ln );
        }
        // NodeModelTestUtils.testNodeModel();
    }

    private void getDocs(LibraryNode ln) {
        for (Node n : ln.getDescendants()) {
            if (!n.isDocumentationOwner())
                continue;

            final String doc = "ABCdef124 now is the time for all good...you know";
            int index = 0;
            // use docHandler
            DocumentationHandler dh = n.getDocHandler();

            assertTrue( n.getDescription() != null );

            dh.setDescription( doc );
            dh.setImplementer( doc, index );
            dh.setMoreInfo( doc, index );
            dh.setDeprecation( doc, index );
            dh.setReference( doc, index );

            assertEquals( doc, dh.getDescription() );
            assertEquals( doc, dh.getImplementer( index ) );
            assertEquals( doc, dh.getMoreInfo( index ) );
            assertEquals( doc, dh.getDeprecation( index ) );
            assertEquals( doc, dh.getReference( index ) );
        }

    }
}
