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

import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.Node.NodeVisitor;
import org.opentravel.schemas.node.NodeVisitors;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNavNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.properties.AttributeNode;
import org.opentravel.schemas.node.properties.ElementNode;
import org.opentravel.schemas.node.typeProviders.CustomFacetNode;
import org.opentravel.schemas.node.typeProviders.facetOwners.BusinessObjectNode;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.testUtils.MockLibrary;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeUser;
import org.opentravel.schemas.utils.BaseProjectTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Test close() methods and commands.
 * 
 * @author Dave Hollander
 * 
 */
public class Close_Tests extends BaseProjectTest {
    static final Logger LOGGER = LoggerFactory.getLogger( MockLibrary.class );

    MockLibrary ml = null;
    LibraryNode ln = null;
    ModelNode model = null;
    LoadFiles lf = null;

    // From baseProjecTest
    // rc, mc, pc, testProject

    @Before
    public void beforeAllTests() {
        ml = new MockLibrary();
        lf = new LoadFiles();
    }

    /**
     * Run tests against default project with loaded files.
     * 
     * @throws Exception
     */
    @Test
    public void closeTestGroupA_DefaultProject_Test() throws Exception {
        model = mc.getModelNode();

        lf.loadTestGroupA( mc );

        // When each library is closed
        for (LibraryNode ln : model.getUserLibraries()) {
            ln.getParent().close();
        }

        // Then the model can not contain libraries
        assertTrue( model.getUserLibraries().isEmpty() );
    }

    /**
     * Close unmanaged libraries in new project
     * 
     * @throws Exception
     */
    @Test
    public void closeTestGroupA_Test() throws Exception {

        // Given a new project
        ProjectNode project = createProject( "Project1", rc.getLocalRepository(), "IT1" );
        TLModel tlModel = project.getTLProject().getModel();

        // Given libraries in the new project
        lf.loadTestGroupA( project );
        assertTrue( "Must have user libraries.", tlModel.getUserDefinedLibraries().size() > 0 );

        // When project is closed
        boolean result = pc.close( project );

        // Then
        assert result == true;
        assertTrue( "Children handler must have no children.", project.getChildrenHandler().get().isEmpty() );
        assertTrue( "Must NOT have user libraries.", tlModel.getUserDefinedLibraries().size() == 0 );
        assertTrue( "Project Manager must not have items.",
            project.getTLProject().getProjectManager().getAllProjectItems().isEmpty() );

        // TL Project will have items, but not in the model
        // List<ProjectItem> items = project.getTLProject().getProjectItems();
    }

    /**
     * Close managed libraries in new project
     * 
     * @throws Exception
     */
    @Test
    public void closeManagedLibraries_Tests() throws Exception {

        // Given libraries in the default project
        ProjectNode defaultProj = createProject( "DP1", rc.getLocalRepository(), "IT0" );
        // FIXME - when default project is used, the model has no user libraries.
        // ProjectNode defaultProj = pc.getDefaultProject();

        // Given managed libraries in new project
        ProjectNode project = createProject( "Project1", rc.getLocalRepository(), "IT1" );
        TLModel tlModel = project.getTLProject().getModel();
        assert project != null;

        // Given test files loaded in to default project
        lf.loadTestGroupA( defaultProj );
        List<TLLibrary> tlLibs = defaultProj.getTLProject().getModel().getUserDefinedLibraries();
        assertTrue( "TLModel must have user libraries.", !tlLibs.isEmpty() );

        // Given managed libraries are created
        LibraryChainNode lcn1 = ml.createNewManagedLibrary( "Lib1", project );
        LibraryChainNode lcn2 = ml.createNewManagedLibrary( "Lib2", project );
        LibraryChainNode lcn3 = ml.createNewManagedLibrary( "Lib3", project );
        AbstractLibrary tlLib1 = lcn1.get().getTLModelObject();
        tlLibs = tlModel.getUserDefinedLibraries();
        assertTrue( tlLibs.contains( tlLib1 ) );

        // When new project is closed
        boolean result = pc.close( project );

        // Then
        assert result == true;
        tlLibs = tlModel.getUserDefinedLibraries();
        assertTrue( "Lib1 must not be in tl libraries.", !tlLibs.contains( tlLib1 ) );
        assertTrue( "TLModel must still have user libraries.", !tlLibs.isEmpty() );
        assertTrue( "Children handler must have no children.", project.getChildrenHandler().get().isEmpty() );

        // When default project is closed
        pc.close( defaultProj );

        // Then
        assert result == true;
        tlLibs = tlModel.getUserDefinedLibraries();
        assertTrue( "Project Manager must not have items.",
            project.getTLProject().getProjectManager().getAllProjectItems().isEmpty() );
        assertTrue( "All users libraries must be closed.", Node.getModelNode().getUserLibraries().isEmpty() );
        assertTrue( "Must NOT have user libraries.", tlLibs.isEmpty() );
    }

    /**
     * Close Libraries in multiple projects
     * 
     * @throws Exception
     */
    @Test
    public void closeLibrariesInMultipleProjects_Tests() throws Exception {
        // Given - same libraries in multiple projects
        ProjectNode project1 = createProject( "Project1", rc.getLocalRepository(), "IT1" );
        ProjectNode project2 = createProject( "Project2", rc.getLocalRepository(), "IT2" );
        lf.loadFile_FacetBase( project1 );
        lf.loadFile_FacetBase( project2 );

        // Assure the library Nav Nodes are unique and have same library
        LibraryNavNode lnn1 = (LibraryNavNode) project1.getChildren().get( 0 );
        LibraryNavNode lnn2 = (LibraryNavNode) project2.getChildren().get( 0 );
        assert lnn1 != null;
        assert lnn2 != null;
        assert lnn1 != lnn2;
        LibraryNode lib1 = (LibraryNode) lnn1.get();
        LibraryNode lib2 = (LibraryNode) lnn2.get();
        assertTrue( "Both libraryNavNodes contain the same library.", lib1 == lib2 );
        List<LibraryMemberInterface> p1Kids = lib1.getDescendants_LibraryMembers();

        // When - library is closed from project 1
        lnn1.close();
        assertTrue( "Project 1 must not have library.", project1.getLibraries().isEmpty() );

        // Then - project 2 still has libraries
        assertTrue( "Project 2 must still have library.", !project2.getLibraries().isEmpty() );
        assertTrue( "Project 2 must contain lnn2.", project2.contains( lnn2 ) );

        // When - project 2 is closed
        project2.close();
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

    /**
     * Test internal logic in Close Visitor
     * 
     * @throws Exception
     */
    @Test
    public void closeVisitor_Tests() throws Exception {
        NodeVisitor visitor = new NodeVisitors().new closeVisitor();

        // Given a project and library
        ProjectNode project = createProject( "Project1", rc.getLocalRepository(), "IT1" );
        LibraryChainNode lcn = ml.createNewManagedLibrary( "Lib1", project );

        // Must be null safe
        visitor.visit( null );

        // Special handling
        // LibraryNode
        // Only when parent is null ???

        // ContextualFacetNode - local
        BusinessObjectNode bo = ml.addBusinessObjectToLibrary( lcn.get(), "TBO", true );
        CustomFacetNode cf = (CustomFacetNode) bo.addFacet( "CF1", TLFacetType.CUSTOM );
        assert bo.contains( cf );
        cf = (CustomFacetNode) bo.addFacet( "CF2", TLFacetType.CUSTOM );
        assert bo.contains( cf );
        visitor.visit( cf );
        assertTrue( cf.isDeleted() );
        assertTrue( "Contextual Facet must be removed from BO.", !bo.contains( cf ) );

        // VersionNode
        // FIXME - do these tests

        // Type Providers

        // Type Users

        // Children handlers
    }

    @Test
    public void closeBaseTypes_Tests() throws Exception {
        // Given a project, library and BO
        ProjectNode project = createProject( "Project1", rc.getLocalRepository(), "IT1" );
        LibraryChainNode lcn1 = ml.createNewManagedLibrary( "Lib1", project );
        LibraryChainNode lcn2 = ml.createNewManagedLibrary( "Lib2", project );
        BusinessObjectNode bo = ml.addBusinessObjectToLibrary_Empty( lcn1.get(), "TBO" );
    }

    /**
     * Closing a library containing type providers to type users in different libraries must leave the TL Type unchanged
     * but us the unassigned node.
     * 
     * @see org.opentravel.schemas.commands.Delete_Tests#deleteTypeUsers_Test()
     * @throws Exception
     */
    @Test
    public void closeAssignedTypes_Tests() throws Exception {
        // Given a project, libraries
        ProjectNode project = createProject( "Project1", rc.getLocalRepository(), "IT1" );
        LibraryChainNode userLib = ml.createNewManagedLibrary( "Lib1", project );
        LibraryChainNode providerLib = ml.createNewManagedLibrary( "Lib2", project );

        // Given - all type providers
        ml.addOneOfEach( providerLib.getHead(), "PL1" );

        // Given - a BO
        BusinessObjectNode bo = ml.addBusinessObjectToLibrary_Empty( userLib.get(), "TBO" );

        // Given - a type user for each provider
        List<TypeUser> users = new ArrayList<TypeUser>();
        int i = 1;
        for (TypeProvider tp : providerLib.getDescendants_TypeProviders()) {
            if (tp.isAssignableToSimple())
                users.add( new AttributeNode( bo.getFacet_Summary(), "E" + i, tp ) );
            else
                users.add( new ElementNode( bo.getFacet_Summary(), "E" + i, tp ) );
            i++;
        }
        // Verify Givens
        for (Node u : bo.getFacet_Summary().getChildren())
            assert providerLib.contains( (Node) ((TypeUser) u).getAssignedType() );

        // When one provider directly closed
        TypeProvider provider = providerLib.getDescendants_TypeProviders().get( 0 );
        TypeUser assignedTo = null;
        for (TypeUser u : provider.getWhereAssigned())
            assignedTo = u;
        ((Node) provider).close();

        // Then
        LOGGER.debug( assignedTo + " " + assignedTo.getAssignedType() );

        // When provider lib is closed
        providerLib.close();

        for (Node u : bo.getFacet_Summary().getChildren()) {
            TypeProvider type = ((TypeUser) u).getAssignedType();
            TLModelElement tlType = ((TypeUser) u).getAssignedTLObject();
            LOGGER.debug( u + " " + type + " " );
        }
    }

    /**
     * Test closing contextual facets which may be in different libraries.
     * 
     * @throws Exception
     */
    @Test
    public void closeContextualFacet_Tests() throws Exception {
        // Given a project, library and BO
        ProjectNode project = createProject( "Project1", rc.getLocalRepository(), "IT1" );
        LibraryChainNode lcn1 = ml.createNewManagedLibrary( "Lib1", project );
        LibraryChainNode lcn2 = ml.createNewManagedLibrary( "Lib2", project );
        BusinessObjectNode bo = ml.addBusinessObjectToLibrary_Empty( lcn1.get(), "TBO" );

        // ContextualFacetNode - local
        CustomFacetNode localCf = (CustomFacetNode) bo.addFacet( "CF1", TLFacetType.CUSTOM );
        assert bo.contains( localCf );
        TLContextualFacet tlCF1 = bo.getTLModelObject().getCustomFacet( "CF1" );
        assert tlCF1.getOwningEntity() == bo.getTLModelObject();

        // ContextualFacetNode - in library 2
        CustomFacetNode lib2Cf = (CustomFacetNode) bo.addFacet( "CF2", TLFacetType.CUSTOM );
        lcn2.getHead().addMember( lib2Cf );
        assert bo.contains( lib2Cf );
        assert lcn2.contains( lib2Cf );

        // Save the project so the OTM file can be examined
        pc.save( project );
        String path = lcn1.getLibrary().getTLModelObject().getLibraryUrl().getPath();
        LOGGER.debug( "Examine OTM file: " + path );

        //
        // When - local is closed
        localCf.close();

        // Then - TL business object does not have tlContextualFacet
        tlCF1 = bo.getTLModelObject().getCustomFacet( "CF1" );
        assert tlCF1 == null;
        // Where contributed removed
        assertTrue( "Contextual Facet must be removed from BO.", !bo.contains( localCf ) );

        lib2Cf.close();
        assertTrue( "Contextual Facet must be removed from BO.", !bo.contains( lib2Cf ) );
    }

}
