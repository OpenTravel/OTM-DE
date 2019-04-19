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

package org.opentravel.schemas.commands;

import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.NavNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.Node.NodeVisitor;
import org.opentravel.schemas.node.NodeVisitors;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.node.VersionNode;
import org.opentravel.schemas.node.interfaces.FacetInterface;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNavNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.listeners.TypeUserAssignmentListener;
import org.opentravel.schemas.node.properties.AttributeNode;
import org.opentravel.schemas.node.properties.ElementNode;
import org.opentravel.schemas.node.properties.VWA_SimpleAttributeFacadeNode;
import org.opentravel.schemas.node.resources.ResourceNode;
import org.opentravel.schemas.node.typeProviders.ChoiceObjectNode;
import org.opentravel.schemas.node.typeProviders.FacetProviderNode;
import org.opentravel.schemas.node.typeProviders.SimpleTypeNode;
import org.opentravel.schemas.node.typeProviders.facetOwners.BusinessObjectNode;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.testUtils.MockLibrary;
import org.opentravel.schemas.testUtils.NodeTesters;
import org.opentravel.schemas.testUtils.NodeTesters.TestNode;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeUser;
import org.opentravel.schemas.utils.BaseProjectTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Dave Hollander
 * 
 */
public class Delete_Tests extends BaseProjectTest {
    static final Logger LOGGER = LoggerFactory.getLogger( MockLibrary.class );

    MockLibrary ml = null;
    LibraryNode ln = null;
    ModelNode model = null;
    LoadFiles lf = null;

    NodeVisitor dv = new NodeVisitors().new deleteVisitor();
    // PrintNode pv = new NodeTesters().new PrintNode();
    TestNode tv = new NodeTesters().new TestNode();
    NodeTesters tt = new NodeTesters();

    @Before
    public void beforeAllTests() {
        ml = new MockLibrary();
        lf = new LoadFiles();
    }

    /**
     * Delete Libraries
     * 
     * @throws Exception
     */
    @Test
    public void deleteLibraries_Tests() throws Exception {
        // DeleteNodesHandler handler = new DeleteNodesHandler();
        //
        // List<Node> deleteList = null;

        // Given an empty list of selected nodes
        // deleteList = new ArrayList<Node>();

        // Given libraries in the default project
        ProjectNode project = pc.getDefaultProject();
        lf.loadTestGroupA( mc );
        // When deleted using nodeModelController
        // Then deleting libraries closes them
        deleteLibrariesTest( project );

        // Given a user defined project with libraries in a chain
        project = createProject( "Project1", rc.getLocalRepository(), "IT1" );
        LibraryChainNode lcn1 = ml.createNewManagedLibrary( "Lib1", project );
        LibraryChainNode lcn2 = ml.createNewManagedLibrary( "Lib2", project );
        LibraryChainNode lcn3 = ml.createNewManagedLibrary( "Lib3", project );
        // List<LibraryNode> managed = new ArrayList<LibraryNode>();
        // managed.add(lcn1.getHead());
        // rc.manage(rc.getLocalRepository(), managed);
        // rc.createMajorVersion(lcn1.getHead());
        // Then all libraries must be deleted
        deleteLibrariesTest( project );

    }

    /**
     * Delete Libraries
     * 
     * @throws Exception
     */
    @Test
    public void deleteLibrariesInMultipleProjects_Tests() throws Exception {
        // Given - same libraries in multiple projects
        ProjectNode project1 = createProject( "Project1", rc.getLocalRepository(), "IT1" );
        ProjectNode project2 = createProject( "Project2", rc.getLocalRepository(), "IT2" );
        lf.loadFile_FacetBase( project1 );
        lf.loadFile_FacetBase( project2 );
        LibraryNavNode lnn1 = (LibraryNavNode) project1.getChildren().get( 0 );
        LibraryNavNode lnn2 = (LibraryNavNode) project2.getChildren().get( 0 );
        assert lnn1 != null;
        assert lnn2 != null;
        assert lnn1 != lnn2;
        LibraryNode lib1 = (LibraryNode) lnn1.get();
        LibraryNode lib2 = (LibraryNode) lnn2.get();
        assertTrue( "Both libraryNavNodes contain the same library.", lib1 == lib2 );
        List<LibraryMemberInterface> p1Kids = lib1.getDescendants_LibraryMembers();

        // When - library is deleted from project 1
        deleteLibrariesTest( project1 );
        assertTrue( "Project 1 must not have library.", project1.getLibraries().isEmpty() );

        // Then - project 2 still has libraries
        assertTrue( "Project 2 must still have library.", !project2.getLibraries().isEmpty() );
        project2.contains( lnn2 );

        // When - project to is deleted
        deleteLibrariesTest( project2 );
        // Then
        assertTrue( "Project 2 must not have library.", project2.getLibraries().isEmpty() );
        // Then - assure everything is closed
        assertTrue( "Project 1 must not have library.", project1.getLibraries().isEmpty() );
        assert lnn1.isDeleted();
        assert lnn2.isDeleted();
        assert lib1.isDeleted();
        assert lib2.isDeleted();
        for (LibraryMemberInterface kid : p1Kids)
            assert kid.isDeleted();
        assert Node.getLibraryModelManager().getUserLibraries().isEmpty();
    }

    // NOTE -- libraries are never deleted but simply closed.
    //
    private void deleteLibrariesTest(ProjectNode project) throws Exception {
        // Given - Project with libraries loaded
        assertTrue( "Project must have libraries.", !project.getUserLibraries().isEmpty() );

        // When - one library is closed via its navNode
        LibraryNavNode lnn = (LibraryNavNode) project.getChildren().get( 0 );
        LibraryNode lib = lnn.getLibrary();
        lnn.close(); // must close libraryNavNode not libs.

        // Then - lnn is deleted and not in the project and the library's objects are all deleted.
        assertTrue( "Library is deleted.", lnn.isDeleted() );
        assertTrue( "Project must not contain library nav node.", !project.contains( lnn ) );
        assertTrue( "Project must not contain library.", !project.getUserLibraries().contains( lib ) );

        // Then - if the library still has a parent, then it was used in a different project
        if (lib.getParent() != null)
            for (LibraryMemberInterface n : lib.getDescendants_LibraryMembers())
                assertTrue( "Named Type " + n + " must not be deleted.", !n.isDeleted() );
        else
            for (LibraryMemberInterface n : lib.getDescendants_LibraryMembers())
                assertTrue( "Named Type " + n + " must be deleted.", n.isDeleted() );

        // When - rest are closed
        List<LibraryNode> libList = project.getUserLibraries();
        for (LibraryNode ln : libList)
            project.close( ln );

        // Then - they must not be in the list of children
        int libCnt = project.getUserLibraries().size();
        assertTrue( "Project must be empty.", libCnt == 0 );
    }

    @Test
    public void deleteProperties() {
        ln = ml.createNewLibrary( "http://opentravel.org/test", "TestLib", testProject );
        BusinessObjectNode bo = new BusinessObjectNode( new TLBusinessObject() );
        bo.setName( "TestBO" );
        ln.addMember( bo );
        FacetInterface facet = bo.getFacet_Summary();
        Assert.assertNotNull( facet );
        TypeProvider aType = ml.getSimpleTypeProvider();

        // Given type user properties assigned types
        ElementNode ele = new ElementNode( facet, "e1" );
        ele.setAssignedType( aType );
        AttributeNode attr = new AttributeNode( facet, "att1" );
        attr.setAssignedType( aType );
        int whereAssignedCount = aType.getWhereAssignedCount();
        Assert.assertEquals( 2, facet.getChildren().size() );

        // Library must be editable to delete
        Assert.assertTrue( ln.isEditable() );
        Assert.assertTrue( ele.isDeleteable() );

        // delete them and assure where used is updated.
        ele.delete();
        attr.delete();
        Assert.assertEquals( 0, facet.getChildren().size() );
        Assert.assertEquals( whereAssignedCount - 2, aType.getWhereAssignedCount() );
    }

    @Test
    public void deleteFacets_BusinessObjectV16() {
        ln = ml.createNewLibrary( "http://opentravel.org/test", "TestLib", testProject );

        // Given a business object with all facet types
        BusinessObjectNode bo = ml.addBusinessObjectToLibrary( ln, "TestBO" );
        int facetCount = bo.getChildren().size();
        FacetProviderNode q1 = bo.addFacet( "Query1", TLFacetType.QUERY );
        FacetProviderNode c1 = bo.addFacet( "Custom1", TLFacetType.CUSTOM );
        assertTrue( "Must have five children", bo.getChildren().size() == facetCount + 2 );
        assertTrue( "Query facet is NOT deleted.", !q1.isDeleted() );
        assertTrue( "Custom facet is NOT deleted.", !c1.isDeleted() );
        ml.check( bo );
        assertTrue( "Facet parent must NOT be the bo in v1.6", q1.getParent() != bo );

        // When the facets are deleted
        q1.delete();
        c1.delete();

        // Then object must not contain facets
        assertTrue( "Object does NOT contain query facet.", !bo.getChildren().contains( q1 ) );
        assertTrue( "Object does NOT contain custom facet.", !bo.getChildren().contains( c1 ) );
        assertTrue( "Object only has " + facetCount + " children.", bo.getChildren().size() == facetCount );
        assertTrue( "Facet is deleted.", q1.isDeleted() );
        assertTrue( "Facet is deleted.", c1.isDeleted() );
    }

    @Test
    public void deleteFacets_ChoiceObject() {
        ln = ml.createNewLibrary( "http://opentravel.org/test", "TestLib", testProject );

        // Given a choice object with 3 facets
        ChoiceObjectNode co = ml.addChoice( ln, "TestCO" );
        int facetCount = co.getChildren().size();
        ml.check( co );

        FacetProviderNode c1 = co.addFacet( "Added1" );
        FacetProviderNode c2 = co.addFacet( "Added2" );
        assertTrue( "Must have five children", co.getChildren().size() == facetCount + 2 );
        assertTrue( "Choice 1 facet must NOT be deleted.", !c1.isDeleted() );
        assertTrue( "Choice 2 facet must NOT be deleted.", !c2.isDeleted() );
        ml.check( co );

        // When the facets are deleted
        c1.delete();
        c2.delete();

        // Then object must not contain facets
        assertTrue( "Object must NOT contain facet.", !co.getChildren().contains( c1 ) );
        assertTrue( "Object must NOT contain facet.", !co.getChildren().contains( c2 ) );
        assertTrue( "Object must have original children count.", co.getChildren().size() == facetCount );
        assertTrue( "Facet must be deleted.", c1.isDeleted() );
        assertTrue( "Facet must be deleted.", c2.isDeleted() );

        // When the other choice facets are deleted
        for (FacetProviderNode f : co.getChoiceFacets())
            f.delete();

        // Then the object is still valid
        ml.check( co );
    }

    @Test
    public void deleteObjects() {
        // Given - test setup
        ln = ml.createNewLibrary( "http://opentravel.org/test", "TestLib", testProject );
        ml.addOneOfEach( ln, "DelTst" );
        assertTrue( ln != null );
        assertTrue( ln.getParent() instanceof LibraryNavNode );
        assertTrue( "Library must have members.", !ln.get_LibraryMembers().isEmpty() );
        assertTrue( "TL Library must have members.", !ln.getTLModelObject().getNamedMembers().isEmpty() );

        // When - Each member is deleted
        List<LibraryMemberInterface> members = ln.get_LibraryMembers();
        for (LibraryMemberInterface member : members) {
            member.delete();
        }

        // Then
        assertTrue( "Library must have no members.", ln.get_LibraryMembers().isEmpty() );
        assertTrue( "TL Library must have no members.", ln.getTLModelObject().getNamedMembers().isEmpty() );
    }

    @Test
    public void deleteTypeUsers_Test() {
        // Given - a library, business object summary facet and type provider
        ln = ml.createNewLibrary( "http://opentravel.org/test", "TestLib", testProject );
        BusinessObjectNode bo = new BusinessObjectNode( new TLBusinessObject() );
        bo.setName( "TestBO" );
        ln.addMember( bo );
        FacetInterface facet = bo.getFacet_Summary();
        Assert.assertNotNull( facet );
        TypeProvider aType = ml.getXsdDate();
        int whereAssignedCount = aType.getWhereAssignedCount();
        // Given - a simple type that can be deleted
        SimpleTypeNode simple = ml.addSimpleTypeToLibrary( ln, "Simple1" );
        simple.setAssignedType( aType );

        // Given two properties
        ElementNode ele = new ElementNode( facet, "e1" );
        AttributeNode attr = new AttributeNode( facet, "att1" );
        assert facet.getChildren().size() == 2;

        // When - assigned simple type
        ele.setAssignedType( simple );
        attr.setAssignedType( simple );
        // Then - check where assigned
        assertTrue( "Where assigned must contain element.", simple.getWhereAssigned().contains( ele ) );
        assertTrue( "Type assignment listener.", ele.getTypeHandler().getAssignmentListeners().getNode() == simple );
        assertTrue( "Where assigned must contain attribute.", simple.getWhereAssigned().contains( attr ) );
        assertTrue( "Type assignment listener.", attr.getTypeHandler().getAssignmentListeners().getNode() == simple );
        assertTrue( "Must have listener for element.",
            simple.getWhereAssignedHandler().getAssignmentListeners( ele ) != null );

        // When - simple is deleted
        simple.delete();
        // Then - it must not be assigned to properties ?? REALLY ?? TL will be
        assertTrue( "Where assigned must not contain element.", !simple.getWhereAssigned().contains( ele ) );
        assertTrue( "Where assigned must not contain attribute.", !simple.getWhereAssigned().contains( attr ) );
        TypeUserAssignmentListener assignmentListener = ele.getTypeHandler().getAssignmentListeners();
        assertTrue( "Must have one assignment listener.", assignmentListener != null );
        assertTrue( "Must have missing in assignment listener.",
            assignmentListener.getNode() == ModelNode.getUnassignedNode() );

        // When - aType is assigned
        ele.setAssignedType( aType );
        attr.setAssignedType( aType );
        // When - business object deleted
        bo.delete();
        Assert.assertTrue( ele.isDeleted() );
        Assert.assertTrue( attr.isDeleted() );

        // Then - assure the assigned types on properties are correct.
        Assert.assertEquals( "Should be equal.", whereAssignedCount, aType.getWhereAssignedCount() );
    }

    @Test
    public void deleteTypeProvider_Test() {
        ln = ml.createNewLibrary( "http://opentravel.org/test", "TestLib", testProject );
        BusinessObjectNode bo = new BusinessObjectNode( new TLBusinessObject() );
        bo.setName( "TestBO" );
        ln.addMember( bo );
        FacetInterface facet = bo.getFacet_Summary();
        Assert.assertNotNull( facet );
        ElementNode ele = new ElementNode( facet, "e1" );
        AttributeNode attr = new AttributeNode( facet, "att1" );
        Assert.assertEquals( 2, facet.getChildren().size() );

        TypeProvider aType = ml.getSimpleTypeProvider();
        int aTypeCount = aType.getWhereAssignedCount();

        TypeProvider simpleType = ml.addSimpleTypeToLibrary( ln, "A_Simple" );
        ((TypeUser) simpleType).setAssignedType( aType );
        assertTrue( "Must be 0.", simpleType.getWhereAssignedCount() == 0 );

        // Given a BO with two properties with assigned to simpleType
        ele.setAssignedType( simpleType );
        attr.setAssignedType( simpleType );
        assertTrue( "Must be 2.", simpleType.getWhereAssignedCount() == 2 );

        // Delete the simple type and assure the assigned types on properties are correct.
        ((Node) simpleType).delete();
        TypeProvider t = ele.getAssignedType();
        // TODO - what should be done with deleted types?
        // When the user explicitly deletes a type, then change assignments, otherwise?
        assertTrue( "Must be unassigned.", ele.getAssignedType() == ModelNode.getUnassignedNode() );
        assertTrue( "Must be unassigned.", attr.getAssignedType() == ModelNode.getUnassignedNode() );
        assertTrue( "Must be 0.", simpleType.getWhereAssignedCount() == 0 );
        assertTrue( "Must be starting value.", aTypeCount == aType.getWhereAssignedCount() );

    }

    @Test
    public void deleteVisitor() throws Exception {

        // Delete Visitor
        int namedTypeCnt = setUpCase( 1 );
        int descendants = ln.getDescendants().size();

        ln.visitAllNodes( tv ); // test visitor, should not change library
        Assert.assertEquals( descendants, ln.getDescendants().size() );
        Assert.assertEquals( namedTypeCnt, ln.getDescendants_LibraryMembers().size() );

        namedTypeCnt = setUpCase( 2 );
        ln.setParent( null ); // force delete to work
        ln.visitAllNodes( dv );
        assert ln.isEmpty();

        LOGGER.debug( "***Setting Up Test Case 3" );
        namedTypeCnt = setUpCase( 3 );
        LibraryChainNode lcn = ln.getChain();
        for (LibraryMemberInterface kid : ln.getDescendants_LibraryMembers())
            kid.visitAllNodes( dv );
        // ln.visitAllNodes(dv);
        // ln.visitAllNodes(pv);
        assert ln.isEmpty();
        lcn.close();
        assert lcn.isEmpty();

        LOGGER.debug( "***Setting Up Test Case 4" );
        namedTypeCnt = setUpCase( 4 );
        lcn = ln.getChain();
        for (LibraryMemberInterface kid : ln.getDescendants_LibraryMembers())
            kid.visitAllNodes( dv );
        // ln.visitAllNodes(dv);
        // ln.visitAllNodes(pv);
        assert ln.isEmpty();
        lcn.close();
        assert lcn.isEmpty();
    }

    // TODO - create test that assures naming errors are caught
    // 2nd library same name
    // 2nd library different name but not valid because of duplicate types w/ same name

    // TODO - create test case that assures all children of all types are deleted.
    // match against list created by visitAllNodes used in delete();

    private int setUpCase(int testCase) {
        int count = 0;
        switch (testCase) {

            case 1:
                // Case 1 - simple library, not editable, with constructed objects
                ln = ml.createNewLibrary( "http://test.com/ns" + testCase, "testCase" + testCase, testProject );
                ln.setEditable( false );
                count = ml.addOneOfEach( ln, "case1" );
                ln.visitAllNodes( tv );
                Assert.assertEquals( count, ln.getDescendants_LibraryMembers().size() );
                Assert.assertFalse( ln.isManaged() );
                Assert.assertFalse( ln.isInHead() );
                Assert.assertFalse( ln.isEditable() );
                Assert.assertTrue( ln.isDeleteable() ); // NOTE - lib is delete-able
                break;

            case 2:
                // Case 2 - unmanaged library, editable, with constructed objects
                ln = ml.createNewLibrary( "http://test.com/ns" + testCase, "testCase" + testCase, testProject );
                ln.setEditable( true );
                count = ml.addOneOfEach( ln, "case2" );
                ln.visitAllNodes( tv );
                Assert.assertEquals( count, ln.getDescendants_LibraryMembers().size() );
                Assert.assertTrue( ln.isDeleteable() );
                Assert.assertTrue( ln.isEditable() );
                break;

            case 3:
                // Case 3 - managed library, editable, with constructed objects
                ln = ml.createNewLibrary( "http://test.com/ns" + testCase, "testCase" + testCase, testProject );
                count = ml.addOneOfEach( ln, "case" + testCase );
                Assert.assertEquals( count, ln.getDescendants_LibraryMembers().size() );
                ml.check( ln );
                // ln.visitAllNodes(tv);

                LibraryChainNode lcn = new LibraryChainNode( ln );
                ln.setEditable( true ); // must be done after LCN created
                assertTrue( "Chain must be found from library.", ln.getChain() == lcn );
                assertTrue( "Chain must have only one library.", lcn.getLibraries().size() == 1 );
                Assert.assertEquals( count, ln.getDescendants_LibraryMembers().size() );
                ml.check( ln );

                Assert.assertTrue( ln.isEditable() );
                Assert.assertTrue( ln.getChain().isEditable() );
                Assert.assertTrue( ln.getChain().isMajor() );
                Assert.assertEquals( count, ln.getDescendants_LibraryMembers().size() );
                // List<Node> ck = ln.getChain().getChildren();
                // List<Node> lms = ln.getChain().getDescendants_LibraryMembers();
                Assert.assertEquals( count, ln.getChain().getDescendants_LibraryMembers().size() );
                Assert.assertFalse( ln.isManaged() );
                Assert.assertFalse( ln.isInHead() );
                break;

            case 4:
                ln = ml.createNewLibrary( "http://test.com/ns" + testCase, "testCase" + testCase, testProject );
                count = ml.addOneOfEach( ln, "case" + testCase );
                ln.visitAllNodes( tv );
                Assert.assertEquals( count, ln.getDescendants_LibraryMembers().size() );
                new LibraryChainNode( ln );
                ln.setEditable( true ); // must be done after LCN created

                Node n = ml.addSimpleTypeToLibrary( ln, "case4S" );
                final String fixedName = n.getName();
                count++;
                // Node n = ln.findNodeByName(fixedName);
                Assert.assertNotNull( n );
                Assert.assertNotNull( ln.getChain().findLibraryMemberByName( fixedName ) );
                Assert.assertNotNull( ln.findLibraryMemberByName( fixedName ) );
                Assert.assertTrue( n.getParent() instanceof NavNode );
                Assert.assertTrue( n.getVersionNode() instanceof VersionNode );
                Assert.assertEquals( count, ln.getDescendants_LibraryMembers().size() );
                Assert.assertEquals( count, ln.getChain().getDescendants_LibraryMembers().size() );
                break;
            case 10:
                // TODO - managed library this is head with multiple libraries
                // TODO - managed library that is not head
        }
        return count;
    }

    @Test
    public void deleteResource() throws Exception {
        // Given - test setup
        ln = ml.createNewLibrary( "http://opentravel.org/test", "TestLib", testProject );
        BusinessObjectNode rbo = ml.addBusinessObject_ResourceSubject( ln, "rbo" );
        ResourceNode resource = ml.addResource( rbo );
        assert rbo.getWhereAssigned().contains( resource );

        ml.check( ln );
        assertTrue( "Library must have resource.", ln.get_LibraryMembers().contains( resource ) );
        assertTrue( "TL Library must have resource.",
            ln.getTLModelObject().getNamedMembers().contains( resource.getTLModelObject() ) );

        // When - Each member is deleted
        List<LibraryMemberInterface> members = ln.get_LibraryMembers();
        for (LibraryMemberInterface member : members) {
            member.delete();
        }

        // Then
        assertTrue( "Library must have no members.", ln.get_LibraryMembers().isEmpty() );
        assertTrue( "TL Library must have no members.", ln.getTLModelObject().getNamedMembers().isEmpty() );
    }

    /**
     * Run the tests against test library files.
     * 
     * @throws Exception
     */
    @Test
    public void deleteFiles() throws Exception {
        model = mc.getModelNode();

        lf.loadTestGroupA( mc );
        for (LibraryNode ln : model.getUserLibraries()) {
            if (!ln.isInChain())
                new LibraryChainNode( ln ); // Test in a chain
            ln.setEditable( true );
            deleteEachNode( ln );
        }
        tt.visitAllNodes( model );
    }

    private void deleteEachNode(LibraryNode ln) {
        for (Node n : ln.getDescendants()) {
            if (n.isNavigation())
                continue;
            if (n.isDeleted())
                continue;
            if (!n.isDeleteable())
                continue;

            // INode user = null;

            // Make sure the users of this type are informed of deletion.
            List<TypeUser> users = null;
            int userCount = 0;
            if (n instanceof TypeProvider) {
                users = new ArrayList<>( ((TypeProvider) n).getWhereAssigned() );
                for (TypeUser u : users)
                    assertTrue( "Assigned node must be used as a type.", u.getAssignedType() == n );
                userCount = 0;
                if (users != null && !users.isEmpty())
                    userCount = users.size();
            }
            if (n instanceof SimpleTypeNode) {
                LOGGER.debug(
                    "Deleting : " + n.getClass().getSimpleName() + " \t" + n + " with " + userCount + " users." );
                if (users != null && !users.isEmpty())
                    for (TypeUser u : users)
                        if (u instanceof VWA_SimpleAttributeFacadeNode)
                            LOGGER.debug( "          : type assigned to SimpleAttributeFacade " + u );
            }
            // When - deleted
            n.delete();
            assert n.isDeleted();

            // Then - node is removed as assigned type
            if (users != null && !users.isEmpty())
                for (TypeUser u : users) {
                    if (u.getAssignedType() == n)
                        LOGGER.debug( "          ERROR - user " + u + " is still assigned deleted node " + n );
                    // assertTrue("Deleted node must not be used as a type.", u.getAssignedType() != n);
                }
            // if (user != null)
            // Assert.assertNotSame(n, user.getType());

        }
        ln.visitAllNodes( tv );
        MockLibrary.printDescendants( ln );
        MockLibrary.printDescendants( ln.getChain() );
    }
}
