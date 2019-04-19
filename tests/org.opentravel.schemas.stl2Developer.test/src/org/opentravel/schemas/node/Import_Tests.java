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

import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemacompiler.model.TLAdditionalDocumentationItem;
import org.opentravel.schemacompiler.model.TLContext;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.controllers.ProjectController;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.properties.AttributeNode;
import org.opentravel.schemas.node.properties.ElementNode;
import org.opentravel.schemas.node.typeProviders.FacetProviderNode;
import org.opentravel.schemas.node.typeProviders.SimpleTypeNode;
import org.opentravel.schemas.node.typeProviders.VWA_Node;
import org.opentravel.schemas.node.typeProviders.facetOwners.BusinessObjectNode;
import org.opentravel.schemas.node.typeProviders.facetOwners.CoreObjectNode;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.testUtils.MockLibrary;
import org.opentravel.schemas.utils.BaseProjectTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

/**
 * @author Dave Hollander
 * 
 */
public class Import_Tests extends BaseProjectTest {
    private static final Logger LOGGER = LoggerFactory.getLogger( Import_Tests.class );

    ModelNode model = null;
    LoadFiles lf = new LoadFiles();
    MockLibrary ml = new MockLibrary();
    LibraryNode ln = null;
    MainController mc;
    ProjectController pc;
    ProjectNode defaultProject;

    @Override
    @Before
    public void beforeEachTest() {
        // mc = OtmRegistry.getMainController(); // don't do this - it messes up the project controller
        // if (mc == null)
        mc = OtmRegistry.getMainController();
        pc = mc.getProjectController();
        defaultProject = pc.getDefaultProject();
    }

    @Test
    public void ImportTest() throws Exception {
        // NodeTesters nt = new NodeTesters();

        LibraryNode sourceLib = lf.loadFile5Clean( mc );
        LibraryNode destLib = lf.loadFile1( mc );
        // final String DestLibNs = "http://www.opentravel.org/Sandbox/junits/ns1/v1";
        // Make sure they loaded OK.
        ml.check( sourceLib );
        ml.check( destLib );

        // LOGGER.debug("\n");
        LOGGER.debug( "Start Import ***************************" );
        // int destTypes = destLib.getDescendants_NamedTypes().size();

        // make sure that destLib is editable (move to project with correct ns)
        String projectFile = MockLibrary.createTempFile( "TempProject", ".otp" );
        ProjectNode project = pc.create( new File( projectFile ), destLib.getNamespace(), "Name", "" );
        destLib = pc.add( project, destLib.getTLModelObject() ).getLibrary();
        // FIXME - copy test lib so it is editable
        // assertTrue(destLib.isEditable());
        // Will not be editable if testFile1 is not editable

        // Make sure the source is still OK
        ml.check( sourceLib );
        ml.check( destLib );

        // Make sure the imported nodes are OK.
    }

    @Test
    public void importNode() {
        // Given two libraries in two projects
        ProjectNode project1 = createProject( "Project1", rc.getLocalRepository(), "IT1" );
        ProjectNode project2 = createProject( "Project2", rc.getLocalRepository(), "IT2" );

        // Create library and force different context id
        LibraryNode source = ml.createNewLibrary_Empty( project1.getNSRoot(), "test1", project1 );
        List<TLContext> ctxs1 = ((TLLibrary) source.getTLModelObject()).getContexts();
        TLContext tlc = ctxs1.get( 0 );
        tlc.setContextId( "id1" );
        tlc.setApplicationContext( "app1" );

        LibraryNode target = ml.createNewLibrary_Empty( project2.getNSRoot(), "test2", project2 );
        assert target.getDescendants_LibraryMembers().size() == 0;

        // Add library members to source that use context
        SimpleTypeNode simple = ml.addSimpleTypeToLibrary( source, "S1" );
        simple.setExample( "S1" );
        simple.setEquivalent( "Simple EQ1" );

        VWA_Node vwa = ml.addVWA_ToLibrary( source, "V1" );
        AttributeNode attr1 = new AttributeNode( vwa.getFacet_Attributes(), "a1", simple );
        attr1.setExample( "A1" );

        CoreObjectNode core = ml.addCoreObjectToLibrary( source, "testCore" );
        ElementNode c1 = new ElementNode( core.getFacet_Summary(), "c1", ml.getXsdString() );
        c1.setExample( "EX1" );

        BusinessObjectNode bo = ml.addBusinessObjectToLibrary( source, "testBO" );
        ElementNode e1 = new ElementNode( bo.getFacet_Summary(), "e1", ml.getXsdString() );
        e1.setExample( "EX1" );
        // add OtherDoc via handler and directly
        e1.getDocHandler().addOther( "OtherDoc 2" );
        TLAdditionalDocumentationItem tlod = new TLAdditionalDocumentationItem();
        tlod.setText( "OtherDoc 1" );
        tlod.setContext( source.getDefaultContextId() );
        e1.getDocumentation().addOtherDoc( tlod );

        assert source.getTLLibrary().getContexts().size() == 1;
        assert target.getTLLibrary().getContexts().size() == 1;

        // pc.save(project1);
        // String path = source.getLibrary().getTLModelObject().getLibraryUrl().getPath();
        // LOGGER.debug("Examine OTM file: " + path);
        // pc.save(project2);
        // path = target.getLibrary().getTLModelObject().getLibraryUrl().getPath();
        // LOGGER.debug("Examine OTM file: " + path);

        // When nodes are imported
        target.importNode( bo );
        target.importNode( core );
        target.importNode( vwa );
        target.importNode( simple );

        // pc.save(project2);
        // path = target.getLibrary().getTLModelObject().getLibraryUrl().getPath();
        // LOGGER.debug("Examine OTM file: " + path);

        // Then
        List<LibraryMemberInterface> members = target.getDescendants_LibraryMembers();
        assertTrue( target.getDescendants_LibraryMembers().size() > 2 );
        assert source.getTLLibrary().getContexts().size() == 1;
        assert target.getTLLibrary().getContexts().size() == 1;
    }

    @Test
    public void createAliases() {
        ml = new MockLibrary();

        ln = ml.createNewLibrary( defaultProject.getNSRoot(), "test", defaultProject );
        BusinessObjectNode bo = ml.addBusinessObjectToLibrary( ln, "testBO" );
        CoreObjectNode core = ml.addCoreObjectToLibrary( ln, "testCore" );
        FacetProviderNode summary = bo.getFacet_Summary();
        int coreKids = core.getChildren().size();
        int startingCount = summary.getChildren().size();

        // Add 3 core objects as property types to see the aliases get made.
        ElementNode prop1, prop2, prop3 = null;
        prop1 = new ElementNode( summary, "P1" );
        prop1.setAssignedType( core );
        prop2 = new ElementNode( summary, "P2" );
        prop2.setAssignedType( core );
        prop3 = new ElementNode( summary, "P3" );
        prop3.setAssignedType( core );

        bo.createAliasesForProperties();
        // FIXME - i now have 3 aliases on the core with the same name because the assign type changed the properties to
        // the same name
        Assert.assertTrue( startingCount + 3 == summary.getChildren().size() ); // 1 + the three added
        Assert.assertEquals( coreKids + 3, core.getChildren().size() );
        // Assert.assertEquals("P1_testCore", prop1.getName());
        // Assert.assertEquals("P1_testCore", prop1.getTypeName());
        // Assert.assertEquals("P1_testCore", prop1.getTLTypeObject().getLocalName());

    }
}
