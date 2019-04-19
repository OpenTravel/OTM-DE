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

package org.opentravel.schemas.controllers.repository;

import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Test;
import org.opentravel.schemacompiler.loader.LibraryLoaderException;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryItemState;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.compile.TLModelCompileValidator;
import org.opentravel.schemas.controllers.LibraryModelManager;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.node.interfaces.FacadeInterface;
import org.opentravel.schemas.node.interfaces.LibraryInterface;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.interfaces.ResourceMemberInterface;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNavNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.resources.ParamGroup;
import org.opentravel.schemas.node.resources.ResourceNode;
import org.opentravel.schemas.node.typeProviders.facetOwners.BusinessObjectNode;
import org.opentravel.schemas.testUtils.BaseRepositoryTest;
import org.opentravel.schemas.trees.repository.RepositoryNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Dave Hollander
 * 
 */
public class RepoositoryFeatureTests extends BaseRepositoryTest {
    private static final Logger LOGGER = LoggerFactory.getLogger( RepoositoryFeatureTests.class );

    protected static boolean SKIP = true; // Skip these tests which can take 10 minutes or more
    // protected MockLibrary ml = new MockLibrary();
    // RepositoryNode localRepo = null;
    // ProjectNode project = null;
    // LibraryChainNode lcn = null;
    // LibraryNode ln = null;

    @Override
    public RepositoryNode getRepositoryForTest() {
        return rc.getLocalRepository();
    }

    /**
     * Create local repo (localRepo) containing a chain (lcn) with head library (ln) with one of each object type in it.
     */
    private void setUpLocal() {
        // Manage a library in a local repository
        localRepo = rc.getLocalRepository();

        // Create a project for the library
        project = createProject( "correctNamespace", getRepositoryForTest(), "test" );

        // Given a managed library in a chain
        ln = ml.createNewLibrary( project.getNamespace(), "TestLib1", project );
        assert ln != null;
        ml.addOneOfEach( ln, "testObj" );
        List<LibraryChainNode> lcns = rc.manage( localRepo, Collections.singletonList( ln ) );
        assertTrue( !lcns.isEmpty() );
        lcn = lcns.get( 0 );
        rc.lock( ln );

        // Some tests were creating tl objects without models
        for (LibraryMemberInterface lm : ln.getDescendants_LibraryMembers())
            assert lm.getTLModelObject().getOwningModel() != null;

        assert ln.getDescendants_LibraryMembers().size() > 1;
        assert localRepo != null;
        assert ln != null;
        assert ln.isEditable();
        assert lcn != null;
        assert lcn.getHead() == ln;
        assert project.contains( (LibraryInterface) ln );
        assert project.contains( ln.getTLModelObject() );
        ml.check();
    }

    @Test
    public void RI_connect() {
        Assert.assertNotNull( rc.getLocalRepository() );
        Assert.assertSame( repositoryManager, rc.getLocalRepository().getRepository() );
        // List<Node> rcKids = rc.getRoot().getChildren();
        assertTrue( "There must be 2 repositories.", 2 == rc.getRoot().getChildren().size() );
        assertTrue( "There must be 1 remote repository. ", 1 == repositoryManager.listRemoteRepositories().size() );
    }

    @Test
    public void RI_setUpLocalTest() {
        setUpLocal();
        // Then
        assertTrue( "Must have one chain.", lcn != null );
        assertTrue( "Chain must have head library.", lcn.get() == ln );
        assertTrue( "Chain must only have one library.", lcn.getLibraries().size() == 1 );
    }

    @Test
    public void RI_minorVersion() {
        // Manage a library in a local repository
        setUpLocal();

        // Given - the library is promoted to final status
        rc.promote( ln, TLLibraryStatus.UNDER_REVIEW );
        rc.promote( ln, TLLibraryStatus.FINAL );

        // Given the initial number of libraries managed in the library model manager
        LibraryModelManager lmm = Node.getLibraryModelManager();
        int initialSize = lmm.getAllLibraries().size();
        List<LibraryInterface> managedLibs = lmm.getLibraries();
        List<Node> projectKids = project.getChildren();

        // When - minor version created
        LibraryNode minor = rc.createMinorVersion( ln );

        // Then - minor library
        assertTrue( "New minor library must have been created.", minor != null );
        assertTrue( "New minor library must be head of chain", lcn.get() == minor );
        // Then - Containing Project
        assertTrue( "Project must have same number of children", project.getChildren().size() == projectKids.size() );
        assertTrue( "Project must report out minor as a managed library.", project.getLibraries().contains( minor ) );
        // Then - Library Manager
        assertTrue( "LMM must have same number of directly managed library interfaces.",
            managedLibs.size() == lmm.getLibraries().size() );
        assertTrue( "LMM must report out more libraries.", initialSize < lmm.getAllLibraries().size() );

    }

    @Test
    public void RI_minorVersionInMultipleProjects() {
        // Manage a library in a local repository

        setUpLocal();
        // assert project.contains((LibraryInterface) ln);
        // assert project.contains(ln.getTLModelObject());

        // Given - a business object and resource
        BusinessObjectNode bo = ml.addBusinessObject_ResourceSubject( ln, "Subject" );
        ResourceNode rn = ml.addResource( bo );

        for (ProjectNode pn : Node.getAllProjects())
            ml.check( pn, true );

        // Given - a second project
        ProjectNode project2 = createProject( "SecondProject", "http://example.com/someOtherNS", "test2" );

        // Given - add library to 2nd project
        // TODO - move this code to DefaultProjectController()
        RepositoryItem item = ln.getProjectItem();
        assert !item.getState().equals( RepositoryItemState.UNMANAGED );
        try {
            project2.getTLProject().getProjectManager().addManagedProjectItem( item, project2.getTLProject() );
        } catch (LibraryLoaderException | RepositoryException e) {
            e.printStackTrace();
            assert false;
        }
        assert lcn.getParent() instanceof LibraryNavNode;
        LibraryNavNode newLNN = new LibraryNavNode( lcn, project2 );
        assert project2.contains( (LibraryInterface) ln );
        assert project2.contains( ln.getTLModelObject() );

        for (ProjectNode pn : Node.getAllProjects())
            ml.check( pn, true );

        // Given the initial number of libraries managed in the library model manager
        LibraryModelManager lmm = Node.getLibraryModelManager();
        int initialSize = lmm.getAllLibraries().size();
        // List<LibraryInterface> managedLibs = lmm.getLibraries();
        // List<Node> projectKids = project.getChildren();
        // List<Node> project2Kids = project2.getChildren();

        assertTrue( "Library must be used in other project.", lmm.isUsedElsewhere( ln, project ) == true );
        assertTrue( "Library must be used in other project.", lmm.isUsedElsewhere( ln, project2 ) == true );

        assertTrue( "Failed to promote to under review.", rc.promote( ln, TLLibraryStatus.UNDER_REVIEW ) );
        assertTrue( "Failed to promote to final.", rc.promote( ln, TLLibraryStatus.FINAL ) );

        // When - minor version created
        LibraryNode minor = rc.createMinorVersion( ln );

        // Then
        assertTrue( minor != null );
        assertTrue( lcn.get() == minor );

        assertTrue( "LMM must have more libraries.", initialSize < lmm.getAllLibraries().size() );

        // When - Close project 2 to not interfere with other tests
        pc.close( project2 );
        assert !Node.getModelNode().getUserProjects().contains( project2 );
        assert lmm.getFirstOtherProject( ln, project, false ) == null;

        // Then - check will assure contextual facets don't lose their connections
        // // Force failure to verify check will detect error
        // List<ContextualFacetNode> cfs = project.getDescendants_ContextualFacets();
        // for (ContextualFacetNode cf : cfs)
        // cf.getTLModelObject().setOwningEntity(null);
        ml.check( project, true );

        // TODO - TEST valid ParamGroup facet ref with Lock/unlock and promote
        for (ResourceMemberInterface rmi : rn.getParameterGroups( false )) {
            assert rmi instanceof ParamGroup;
            ml.check( (Node) rmi, true );
        }

        // // Force failure - remove references and make sure ml.check() catches it.
        // rn.getTLModelObject().setBusinessObjectRef(null);
        // for (ResourceMemberInterface rmi : rn.getParameterGroups(false)) {
        // ((ParamGroup) rmi).setReferenceFacet((FacetInterface) null);
        // }
        ml.check( project, true );

        // TODO - TEST assure non-local contextual facets don't lose their connections

    }

    private void boScan(String targetName) {
        // check all BOs
        List<BusinessObjectNode> bos = new ArrayList<>();
        List<AbstractLibrary> tlLibs = new ArrayList<>();
        List<TLBusinessObject> tlBos = new ArrayList<>();

        for (LibraryNode ln : Node.getAllLibraries())
            tlLibs.add( ln.getTLModelObject() );
        for (AbstractLibrary tlLib : tlLibs)
            for (LibraryMember tl : tlLib.getNamedMembers()) {
                if (tl instanceof TLBusinessObject)
                    tlBos.add( (TLBusinessObject) tl );
                if (tl.getLocalName().equals( targetName ))
                    LOGGER.debug( "Found tl named " + targetName );
            }

        for (ProjectNode pn : Node.getAllProjects()) {
            for (LibraryMemberInterface lm : pn.getDescendants_LibraryMembers())
                if (lm instanceof BusinessObjectNode) {
                    if (bos.contains( lm ))
                        LOGGER.debug( "duplicate BO " + lm );
                    bos.add( (BusinessObjectNode) lm );
                }
            for (Node n : pn.getDescendants())
                if (!(n instanceof FacadeInterface))
                    if (n.getName().equals( targetName ))
                        LOGGER.debug( "Found " + n.getClass().getSimpleName() + " named " + targetName );
        }
        LOGGER.debug( "Found " + bos.size() + " business objects." );
        for (BusinessObjectNode bo : bos)
            assert tlBos.contains( bo.getTLModelObject() );

        LOGGER.debug( "Found " + tlBos.size() + " tl business objects." );
        for (TLBusinessObject tlbo : tlBos)
            assert bos.contains( Node.GetNode( tlbo ) );

        ValidationFindings findings = null;
        for (TLBusinessObject tlbo : tlBos)
            try {
                findings = TLModelCompileValidator.validateModelElement( tlbo, true );
            } catch (Exception e) {
                LOGGER.debug( "Validation threw error: " + e.getLocalizedMessage() );
            }
    }

    // TODO - test with 2 projects reading in Resource and CF test files
    // assure valid on load and when project is closed

    /** **************************************** Old Tests *****************************/

    // /**
    // * Manage - Library Manage (publish) libraries (with and without errors) in repositories. Assure published library
    // * is converted to LibraryChain in projectNode tree.
    // *
    // * @throws LibrarySaveException
    // */
    //
    // @Test
    // public void manageWithWrongNamespaceShouldNotCreateChain() throws LibrarySaveException {
    // if (SKIP)
    // return;
    // LibraryNode testLibary = LibraryNodeBuilder.create("name", "__illegalNamespace", "prefix", new Version(1, 0, 0))
    // .build(defaultProject, pc);
    // List<LibraryChainNode> chains = rc.manage(getRepositoryForTest(), Collections.singletonList(testLibary));
    // Assert.assertTrue("Should not craete library for wrong namespace.", chains.isEmpty());
    // }
    //
    // @Test
    // public void manageWithCorrectNamespaceShouldNotBeEdiableWithDisabledPolicy()
    // throws RepositoryException, LibrarySaveException {
    // if (SKIP)
    // return;
    // ProjectNode project = createProject("correctNamespace", getRepositoryForTest(), "test");
    // LibraryNode testLibary = LibraryNodeBuilder
    // .create("name", project.getNamespace(), "prefix", new Version(1, 0, 0)).build(project, pc);
    // Assert.assertTrue(testLibary.isEditable());
    // LibraryChainNode library = rc.manage(getRepositoryForTest(), Collections.singletonList(testLibary)).get(0);
    // Assert.assertNotNull("Should create library for correct namespace.", library);
    // Assert.assertFalse("Libarry should be non-editable after managing in repository.", library.isEditable());
    // }
    //
    // @Test
    // public void manageShouldUpdateCorrectRepositoryNode() throws RepositoryException, LibrarySaveException {
    // if (SKIP)
    // return;
    // ProjectNode uploadProject = createProject("MangeThis", getRepositoryForTest(), "test");
    // LibraryNode testLibary = LibraryNodeBuilder
    // .create("TestLibrary", getRepositoryForTest().getNamespace() + "/Test", "prefix", new Version(1, 0, 0))
    // .build(uploadProject, pc);
    // LibraryChainNode library = rc.manage(getRepositoryForTest(), Collections.singletonList(testLibary)).get(0);
    // RepositoryItemNode item = findRepositoryItem(library, getRepositoryForTest());
    // Assert.assertNotNull(item);
    // }
    //
    // /**
    // * Retrieve - Retrieve repository object into pre-existing project Retrieve repository object into a newly created
    // * project with same base namespace as library
    // */
    //
    // @Test
    // public void retrieveObjectToPreExisitingProject() throws RepositoryException, LibrarySaveException {
    // if (SKIP)
    // return;
    // ProjectNode uploadProject = createProject("ToUploadLibrary", getRepositoryForTest(), "test");
    // LibraryNode testLibary = LibraryNodeBuilder
    // .create("TestLibrary", getRepositoryForTest().getNamespace() + "/Test", "prefix", new Version(1, 0, 0))
    // .build(uploadProject, pc);
    // LibraryChainNode library = rc.manage(getRepositoryForTest(), Collections.singletonList(testLibary)).get(0);
    //
    // Assert.assertEquals(0, defaultProject.getChildren().size());
    // RepositoryItemNode nodeToRetrive = findRepositoryItem(library, getRepositoryForTest());
    // pc.add(defaultProject, nodeToRetrive.getItem());
    // Assert.assertEquals(1, defaultProject.getChildren().size());
    // LibraryChainNode nodes = (LibraryChainNode) defaultProject.getChildren().get(0);
    // Assert.assertEquals(library.getName(), nodes.getName());
    // Assert.assertEquals(library.getHead().getNamespace(), nodes.getHead().getNamespace());
    // Assert.assertNotSame(library, nodes);
    //
    // }
    //
    // /**
    // * Retrieve - Create repository object and retrieve it with and without closing project.
    // */
    //
    // /**
    // * Versioning - major, minor, patch Project controller Legal library, illegal library (duplicate VWA
    // namespace:name)
    // * Assure new version is in LibraryChain correctly. Assure old version is not directly in TLProject or
    // * projectNode.children Assure new version is in repository. Assure versions can start with 0.0.0 and variations.
    // */
    //
    // @Test
    // public void createChainNodeShouldSetHead() throws RepositoryException, LibrarySaveException {
    // if (SKIP)
    // return;
    // ProjectNode uploadProject = createProject("ToUploadLibrary", getRepositoryForTest(), "Test");
    // LibraryNode testLibary = LibraryNodeBuilder
    // .create("TestLibrary", getRepositoryForTest().getNamespace() + "/Test", "prefix", new Version(1, 0, 0))
    // .makeFinal().build(uploadProject, pc);
    // LibraryChainNode chain = rc.manage(getRepositoryForTest(), Collections.singletonList(testLibary)).get(0);
    // rc.createPatchVersion(chain.getHead());
    // assertSame(chain.getHead(), chain.getLibrary());
    // }
    //
    // @Test
    // public void createMajorVersionWithSimpleCoreWithoutType() throws RepositoryException, LibrarySaveException {
    // if (SKIP)
    // return;
    // ProjectNode uploadProject = createProject("ToUploadLibrary", getRepositoryForTest(), "test");
    // LibraryNode testLibary = LibraryNodeBuilder
    // .create("TestLibrary", getRepositoryForTest().getNamespace() + "/Test", "prefix", new Version(1, 0, 0))
    // .makeFinal().build(uploadProject, pc);
    // testLibary.addMember(ComponentNodeBuilder.createCoreObject("test").addProperty("testProperty").get());
    // LibraryChainNode chain = rc.manage(getRepositoryForTest(), Collections.singletonList(testLibary)).get(0);
    // LibraryNode newMajor = rc.createMajorVersion(chain.getHead());
    // ValidationFindings findings = newMajor.validate();
    // Assert.assertEquals(0, findings.getFindingsAsList(FindingType.ERROR).size());
    // }
    //
    // @Test
    // public void createMajorVersionWithSimpleCoreWithEmtpyType() throws RepositoryException, LibrarySaveException {
    // if (SKIP)
    // return;
    // ProjectNode uploadProject = createProject("ToUploadLibrary", getRepositoryForTest(), "test");
    // LibraryNode testLibary = LibraryNodeBuilder
    // .create("TestLibrary", getRepositoryForTest().getNamespace() + "/Test", "prefix", new Version(1, 0, 0))
    // .makeFinal().build(uploadProject, pc);
    // testLibary.addMember(
    // ComponentNodeBuilder.createCoreObject("test").addProperty("TestProperty").setAssignedType().get());
    // LibraryChainNode chain = rc.manage(getRepositoryForTest(), Collections.singletonList(testLibary)).get(0);
    // LibraryNode newMajor = rc.createMajorVersion(chain.getHead());
    // ValidationFindings findings = newMajor.validate();
    // Assert.assertEquals(0, findings.count());
    // }
    //
    // @Test
    // public void createPatchVersionWithSimpleCoreWithEmtpyType() throws RepositoryException, LibrarySaveException {
    // if (SKIP)
    // return;
    // ProjectNode uploadProject = createProject("ToUploadLibrary", getRepositoryForTest(), "Test");
    // LibraryNode testLibary = LibraryNodeBuilder
    // .create("TestLibrary", getRepositoryForTest().getNamespace() + "/Test", "prefix", new Version(1, 0, 0))
    // .makeFinal().build(uploadProject, pc);
    // LibraryChainNode chain = rc.manage(getRepositoryForTest(), Collections.singletonList(testLibary)).get(0);
    // LibraryNode newPatch = rc.createPatchVersion(chain.getHead());
    // assertEquals(NodeEditStatus.PATCH, newPatch.getEditStatus());
    // assertEquals(NodeEditStatus.PATCH, chain.getEditStatus());
    // assertEquals(NodeEditStatus.PATCH, chain.getLibrary().getEditStatus());
    // }
    //
    // /**
    // * Library Chain Access - Both patch and minor Version 3 times Get chain for latest version and assure all members
    // * Get chain for middle member and assure only older members are returned
    // */
    //
    // /**
    // * Repository Item metadata accesss - Retrieve all fields used in library view from a repository item.
    // */
    //
    // /**
    // * Lock/Unlock - Lock and unlock test libraries from repository Assure unlocked libraries are read-only when
    // * retrieved from repository.
    // *
    // * @throws LibraryLoaderException
    // */
    //
    // @Test
    // public void lockShouldMakeLibaryEditable()
    // throws RepositoryException, LibrarySaveException, LibraryLoaderException {
    // if (SKIP)
    // return;
    // ProjectNode uploadProject = createProject("ToUploadLibrary", getRepositoryForTest(), "test");
    // LibraryNode testLibary = LibraryNodeBuilder
    // .create("TestLibrary", getRepositoryForTest().getNamespace() + "/Test", "prefix", new Version(1, 0, 0))
    // .build(uploadProject, pc);
    // LibraryChainNode library = rc.manage(getRepositoryForTest(), Collections.singletonList(testLibary)).get(0);
    //
    // Assert.assertFalse(library.isEditable());
    // library.getHead().lock();
    // Assert.assertTrue(library.isEditable());
    // }
    //
    // @Test
    // public void unlockWithoutCommitShouldRevertChanges()
    // throws RepositoryException, LibrarySaveException, LibraryLoaderException {
    // if (SKIP)
    // return;
    // ProjectNode uploadProject = createProject("ToUploadLibrary", getRepositoryForTest(), "test");
    // LibraryNode testLibary = LibraryNodeBuilder
    // .create("TestLibrary", getRepositoryForTest().getNamespace() + "/Test", "prefix", new Version(1, 0, 0))
    // .build(uploadProject, pc);
    // LibraryChainNode library = rc.manage(getRepositoryForTest(), Collections.singletonList(testLibary)).get(0);
    //
    // library.getHead().lock();
    // library.getHead().addMember(
    // ComponentNodeBuilder.createCoreObject("test").addProperty("TestProperty").setAssignedType().get());
    // Assert.assertEquals(1, library.getDescendants_LibraryMembersAsNodes().size());
    //
    // String libraryName = library.getName();
    // String namespace = library.getNamespace();
    // ProjectNode revertProject = mc.getRepositoryController().unlockAndRevert(library.getHead());
    //
    // // make sure new project has reverted library
    // Assert.assertEquals(1, revertProject.getLibraries().size());
    // LibraryNode revertedLib = revertProject.getLibraries().get(0);
    // Assert.assertEquals(0, revertedLib.getDescendants_LibraryMembersAsNodes().size());
    //
    // // revert library from repository
    // RepositoryItemNode nodeToRetrive = findRepositoryItem(revertedLib.getChain(), getRepositoryForTest());
    // pc.add(defaultProject, nodeToRetrive.getItem());
    // Assert.assertEquals(1, defaultProject.getChildren().size());
    // LibraryChainNode nodes = (LibraryChainNode) defaultProject.getChildren().get(0);
    // Assert.assertEquals(libraryName, nodes.getName());
    // Assert.assertEquals(namespace, nodes.getNamespace());
    // Assert.assertEquals(0, nodes.getDescendants_LibraryMembersAsNodes().size());
    // }
    //
    // @Test
    // public void unlockWithoutCommitForDefaultProjectShouldRevertChanges()
    // throws RepositoryException, LibrarySaveException, LibraryLoaderException {
    // if (SKIP)
    // return;
    // LibraryNode testLibary = LibraryNodeBuilder
    // .create("TestLibrary", getRepositoryForTest().getNamespace() + "/Test", "prefix", new Version(1, 0, 0))
    // .build(pc.getDefaultProject(), pc);
    // LibraryChainNode library = rc.manage(getRepositoryForTest(), Collections.singletonList(testLibary)).get(0);
    //
    // library.getHead().lock();
    // library.getHead().addMember(
    // ComponentNodeBuilder.createCoreObject("test").addProperty("TestProperty").setAssignedType().get());
    // List<Node> descendants = library.getDescendants_LibraryMembersAsNodes();
    // Assert.assertEquals(1, library.getDescendants_LibraryMembersAsNodes().size());
    // Assert.assertEquals(1, descendants.size());
    //
    // String libraryName = library.getName();
    // String libraryNamespace = library.getNamespace();
    // ProjectNode revertProject = mc.getRepositoryController().unlockAndRevert(library.getHead());
    //
    // // make sure new project has reverted library
    // Assert.assertEquals(1, revertProject.getLibraries().size());
    // LibraryNode revertedLib = revertProject.getLibraries().get(0);
    // Assert.assertEquals(0, revertedLib.getDescendants_LibraryMembersAsNodes().size());
    //
    // // revert library from repository
    // RepositoryItemNode nodeToRetrive = findRepositoryItem(revertedLib.getChain(), getRepositoryForTest());
    // pc.add(defaultProject, nodeToRetrive.getItem());
    // Assert.assertEquals(1, defaultProject.getChildren().size());
    // LibraryChainNode nodes = (LibraryChainNode) defaultProject.getChildren().get(0);
    // Assert.assertEquals(libraryName, nodes.getName());
    // Assert.assertEquals(libraryNamespace, nodes.getNamespace());
    // Assert.assertEquals(0, nodes.getDescendants_LibraryMembersAsNodes().size());
    // }
    //
    // @Test
    // public void unlockWithCommitShouldSetReadonlyToLibrary()
    // throws RepositoryException, LibrarySaveException, LibraryLoaderException {
    // if (SKIP)
    // return;
    // ProjectNode uploadProject = createProject("ToUploadLibrary", getRepositoryForTest(), "test");
    // LibraryNode testLibary = LibraryNodeBuilder
    // .create("TestLibrary", getRepositoryForTest().getNamespace() + "/Test", "prefix", new Version(1, 0, 0))
    // .build(uploadProject, pc);
    // LibraryChainNode library = rc.manage(getRepositoryForTest(), Collections.singletonList(testLibary)).get(0);
    //
    // library.getHead().lock();
    // String coreObjectName = "NewCoreObject";
    // library.getHead().addMember(ComponentNodeBuilder.createCoreObject(coreObjectName).addProperty("TestProperty")
    // .setAssignedType().get());
    //
    // Assert.assertTrue(library.isEditable());
    // mc.getRepositoryController().unlock(library.getHead());
    // Assert.assertFalse(library.isEditable());
    // }
    //
    // @Test
    // public void unlockWithCommitShouldCommitChanges()
    // throws RepositoryException, LibrarySaveException, LibraryLoaderException {
    // if (SKIP)
    // return;
    // ProjectNode uploadProject = createProject("ToUploadLibrary", getRepositoryForTest(), "test");
    // LibraryNode testLibary = LibraryNodeBuilder
    // .create("TestLibrary", getRepositoryForTest().getNamespace() + "/Test", "prefix", new Version(1, 0, 0))
    // .build(uploadProject, pc);
    // LibraryChainNode lcn = rc.manage(getRepositoryForTest(), Collections.singletonList(testLibary)).get(0);
    //
    // lcn.getHead().lock();
    // String coreObjectName = "NewCoreObject";
    // lcn.getHead().addMember(ComponentNodeBuilder.createCoreObject(coreObjectName).addProperty("TestProperty")
    // .setAssignedType().get());
    //
    // DefaultRepositoryController rc = (DefaultRepositoryController) mc.getRepositoryController();
    // rc.unlock(lcn.getHead());
    //
    // Assert.assertEquals(1, lcn.getDescendants_LibraryMembersAsNodes().size());
    // String libraryName = lcn.getHead().getName();
    // String libraryChainName = lcn.getName();
    // String namespace = lcn.getNamespace();
    // lcn.close();
    //
    // // revert library from repository
    // RepositoryItemNode nodeToRetrive = findRepositoryItem(libraryName, namespace, getRepositoryForTest());
    //
    // pc.add(defaultProject, nodeToRetrive.getItem());
    // Assert.assertEquals(1, defaultProject.getChildren().size());
    // LibraryChainNode nodes = (LibraryChainNode) defaultProject.getChildren().get(0);
    // Assert.assertEquals(libraryChainName, nodes.getName());
    // Assert.assertEquals(namespace, nodes.getHead().getNamespace());
    // Assert.assertEquals(1, nodes.getDescendants_LibraryMembersAsNodes().size());
    // }
    //
    // /**
    // * Commit/revert - Retrieve library, add content then commit changes, close library. Retrieve library Assure new
    // * content is in library. Revert library, close retrieve and assure content is NOT in library.
    // */
    //
    // /**
    // * Finalize - Finalize (promote) library in project. Assure state changes in project and repository.
    // */

}
