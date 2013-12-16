/*
 * Copyright (c) 2012, Sabre Inc.
 */
package com.sabre.schemas.controllers.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.osgi.framework.Version;

import com.sabre.schemacompiler.repository.RepositoryException;
import com.sabre.schemacompiler.saver.LibrarySaveException;
import com.sabre.schemacompiler.validate.FindingType;
import com.sabre.schemacompiler.validate.ValidationFindings;
import com.sabre.schemas.node.LibraryChainNode;
import com.sabre.schemas.node.LibraryNode;
import com.sabre.schemas.node.Node;
import com.sabre.schemas.node.NodeEditStatus;
import com.sabre.schemas.node.ProjectNode;
import com.sabre.schemas.trees.repository.RepositoryNode.RepositoryItemNode;
import com.sabre.schemas.utils.ComponentNodeBuilder;
import com.sabre.schemas.utils.LibraryNodeBuilder;

/**
 * @author Pawel Jedruch
 * 
 */
public abstract class RepositoryControllerTest extends RepositoryIntegrationTestBase {

    @Test
    public void connect() {
        Assert.assertNotNull(rc.getLocalRepository());
        Assert.assertSame(repositoryManager, rc.getLocalRepository().getRepository());
        Assert.assertEquals(1, repositoryManager.listRemoteRepositories().size());
        Assert.assertEquals(2, rc.getRoot().getChildren().size());
    }

    /**
     * Manage - Library Manage (publish) libraries (with and without errors) in repositories. Assure
     * published library is converted to LibraryChain in projectNode tree.
     * 
     * @throws LibrarySaveException
     */

    @Test
    public void manageWithWrongNamespaceShouldNotCreateChain() throws LibrarySaveException {
        LibraryNode testLibary = LibraryNodeBuilder.create("name", "__illegalNamespace", "prefix",
                new Version(1, 0, 0)).build(defaultProject, pc);
        List<LibraryChainNode> chains = rc.manage(getRepositoryForTest(),
                Collections.singletonList(testLibary));
        Assert.assertTrue("Should not craete library for wrong namespace.", chains.isEmpty());
    }

    @Test
    public void manageWithCorrectNamespaceShouldNotBeEdiableWithDisabledPolicy()
            throws RepositoryException, LibrarySaveException {
        ProjectNode project = createProject("correctNamespace", getRepositoryForTest(), "test");
        LibraryNode testLibary = LibraryNodeBuilder.create("name", project.getNamespace(),
                "prefix", new Version(1, 0, 0)).build(project, pc);
        Assert.assertTrue(testLibary.isEditable());
        LibraryChainNode library = rc.manage(getRepositoryForTest(),
                Collections.singletonList(testLibary)).get(0);
        Assert.assertNotNull("Should create library for correct namespace.", library);
        Assert.assertFalse("Libarry should be non-editable after managing in repository.",
                library.isEditable());
    }

    @Test
    public void manageShouldUpdateCorrectRepositoryNode() throws RepositoryException,
            LibrarySaveException {
        ProjectNode uploadProject = createProject("MangeThis", getRepositoryForTest(), "test");
        LibraryNode testLibary = LibraryNodeBuilder.create("TestLibrary",
                getRepositoryForTest().getNamespace() + "/Test", "prefix", new Version(1, 0, 0))
                .build(uploadProject, pc);
        LibraryChainNode library = rc.manage(getRepositoryForTest(),
                Collections.singletonList(testLibary)).get(0);
        RepositoryItemNode item = findRepositoryItem(library, getRepositoryForTest());
        Assert.assertNotNull(item);
    }

    /**
     * Retrieve - Retrieve repository object into pre-existing project Retrieve repository object
     * into a newly created project with same base namespace as library
     */

    @Test
    public void retrieveObjectToPreExisitingProject() throws RepositoryException,
            LibrarySaveException {
        ProjectNode uploadProject = createProject("ToUploadLibrary", getRepositoryForTest(), "test");
        LibraryNode testLibary = LibraryNodeBuilder.create("TestLibrary",
                getRepositoryForTest().getNamespace() + "/Test", "prefix", new Version(1, 0, 0))
                .build(uploadProject, pc);
        LibraryChainNode library = rc.manage(getRepositoryForTest(),
                Collections.singletonList(testLibary)).get(0);

        Assert.assertEquals(0, defaultProject.getChildren().size());
        RepositoryItemNode nodeToRetrive = findRepositoryItem(library, getRepositoryForTest());
        pc.add(defaultProject, nodeToRetrive.getItem());
        Assert.assertEquals(1, defaultProject.getChildren().size());
        LibraryChainNode nodes = (LibraryChainNode) defaultProject.getChildren().get(0);
        Assert.assertEquals(library.getName(), nodes.getName());
        Assert.assertEquals(library.getHead().getNamespace(), nodes.getHead().getNamespace());
        Assert.assertNotSame(library, nodes);

    }

    /**
     * Retrieve - Create repository object and retrieve it with and without closing project.
     */

    /**
     * Versioning - major, minor, patch Project controller Legal library, illegal library (duplicate
     * VWA namespace:name) Assure new version is in LibraryChain correctly. Assure old version is
     * not directly in TLProject or projectNode.children Assure new version is in repository. Assure
     * versions can start with 0.0.0 and variations.
     */
    
    @Test
    public void createChainNodeShouldSetHead() throws RepositoryException,
    LibrarySaveException {
        ProjectNode uploadProject = createProject("ToUploadLibrary", getRepositoryForTest(), "Test");
        LibraryNode testLibary = LibraryNodeBuilder
                .create("TestLibrary", getRepositoryForTest().getNamespace() + "/Test", "prefix",
                        new Version(1, 0, 0)).makeFinal().build(uploadProject, pc);
        LibraryChainNode chain = rc.manage(getRepositoryForTest(),
                Collections.singletonList(testLibary)).get(0);
        rc.createPatchVersion(chain.getHead());
        assertSame(chain.getHead(), chain.getLibrary());
    }

    @Test
    public void createMajorVersionWithSimpleCoreWithoutType() throws RepositoryException,
            LibrarySaveException {
        ProjectNode uploadProject = createProject("ToUploadLibrary", getRepositoryForTest(), "test");
        LibraryNode testLibary = LibraryNodeBuilder
                .create("TestLibrary", getRepositoryForTest().getNamespace() + "/Test", "prefix",
                        new Version(1, 0, 0)).makeFinal().build(uploadProject, pc);
        testLibary.addMember(ComponentNodeBuilder.createCoreObject("test")
                .addProperty("testProperty").get());
        LibraryChainNode chain = rc.manage(getRepositoryForTest(),
                Collections.singletonList(testLibary)).get(0);
        LibraryNode newMajor = rc.createMajorVersion(chain.getHead());
        ValidationFindings findings = newMajor.validate();
        Assert.assertEquals(0, findings.getFindingsAsList(FindingType.ERROR).size());
    }

    @Test
    public void createMajorVersionWithSimpleCoreWithEmtpyType() throws RepositoryException,
            LibrarySaveException {
        ProjectNode uploadProject = createProject("ToUploadLibrary", getRepositoryForTest(), "test");
        LibraryNode testLibary = LibraryNodeBuilder
                .create("TestLibrary", getRepositoryForTest().getNamespace() + "/Test", "prefix",
                        new Version(1, 0, 0)).makeFinal().build(uploadProject, pc);
        testLibary.addMember(ComponentNodeBuilder.createCoreObject("test")
                .addProperty("TestProperty").setSimpleType().get());
        LibraryChainNode chain = rc.manage(getRepositoryForTest(),
                Collections.singletonList(testLibary)).get(0);
        LibraryNode newMajor = rc.createMajorVersion(chain.getHead());
        ValidationFindings findings = newMajor.validate();
        Assert.assertEquals(0, findings.count());
    }
    
    @Test
    public void createPatchVersionWithSimpleCoreWithEmtpyType() throws RepositoryException,
    LibrarySaveException {
        ProjectNode uploadProject = createProject("ToUploadLibrary", getRepositoryForTest(), "Test");
        LibraryNode testLibary = LibraryNodeBuilder
                .create("TestLibrary", getRepositoryForTest().getNamespace() + "/Test", "prefix",
                        new Version(1, 0, 0)).makeFinal().build(uploadProject, pc);
        LibraryChainNode chain = rc.manage(getRepositoryForTest(),
                Collections.singletonList(testLibary)).get(0);
        LibraryNode newPatch= rc.createPatchVersion(chain.getHead());
        assertEquals(NodeEditStatus.PATCH,  newPatch.getEditStatus());
        assertEquals(NodeEditStatus.PATCH,  chain.getEditStatus());
        assertEquals(NodeEditStatus.PATCH,  chain.getLibrary().getEditStatus());
    }

    /**
     * Library Chain Access - Both patch and minor Version 3 times Get chain for latest version and
     * assure all members Get chain for middle member and assure only older members are returned
     */

    /**
     * Repository Item metadata accesss - Retrieve all fields used in library view from a repository
     * item.
     */

    /**
     * Lock/Unlock - Lock and unlock test libraries from repository Assure unlocked libraries are
     * read-only when retrieved from repository.
     */

    @Test
    public void lockShouldMakeLibaryEditable() throws RepositoryException, LibrarySaveException {
        ProjectNode uploadProject = createProject("ToUploadLibrary", getRepositoryForTest(), "test");
        LibraryNode testLibary = LibraryNodeBuilder.create("TestLibrary",
                getRepositoryForTest().getNamespace() + "/Test", "prefix", new Version(1, 0, 0))
                .build(uploadProject, pc);
        LibraryChainNode library = rc.manage(getRepositoryForTest(),
                Collections.singletonList(testLibary)).get(0);

        Assert.assertFalse(library.isEditable());
        library.getHead().lock();
        Assert.assertTrue(library.isEditable());
    }

    @Test
    public void unlockWithoutCommitShouldRevertChanges() throws RepositoryException,
            LibrarySaveException {
        ProjectNode uploadProject = createProject("ToUploadLibrary", getRepositoryForTest(), "test");
        LibraryNode testLibary = LibraryNodeBuilder.create("TestLibrary",
                getRepositoryForTest().getNamespace() + "/Test", "prefix", new Version(1, 0, 0))
                .build(uploadProject, pc);
        LibraryChainNode library = rc.manage(getRepositoryForTest(),
                Collections.singletonList(testLibary)).get(0);

        library.getHead().lock();
        library.getHead().addMember(
                ComponentNodeBuilder.createCoreObject("test").addProperty("TestProperty")
                        .setSimpleType().get());
        Assert.assertEquals(1, library.getDescendants_NamedTypes().size());

        String libraryName = library.getName();
        String namespace = library.getNamespace();
        ProjectNode revertProject = mc.getRepositoryController().unlockAndRevert(library.getHead());

        // make sure new project has reverted library
        Assert.assertEquals(1, revertProject.getLibraries().size());
        LibraryNode revertedLib = revertProject.getLibraries().get(0);
        Assert.assertEquals(0, revertedLib.getDescendants_NamedTypes().size());

        // revert library from repository
        RepositoryItemNode nodeToRetrive = findRepositoryItem(revertedLib.getChain(),
                getRepositoryForTest());
        pc.add(defaultProject, nodeToRetrive.getItem());
        Assert.assertEquals(1, defaultProject.getChildren().size());
        LibraryChainNode nodes = (LibraryChainNode) defaultProject.getChildren().get(0);
        Assert.assertEquals(libraryName, nodes.getName());
        Assert.assertEquals(namespace, nodes.getNamespace());
        Assert.assertEquals(0, nodes.getDescendants_NamedTypes().size());
    }

    @Test
    public void unlockWithoutCommitForDefaultProjectShouldRevertChanges()
            throws RepositoryException, LibrarySaveException {
        LibraryNode testLibary = LibraryNodeBuilder.create("TestLibrary",
                getRepositoryForTest().getNamespace() + "/Test", "prefix", new Version(1, 0, 0))
                .build(pc.getDefaultProject(), pc);
        LibraryChainNode library = rc.manage(getRepositoryForTest(),
                Collections.singletonList(testLibary)).get(0);

        library.getHead().lock();
        library.getHead().addMember(
                ComponentNodeBuilder.createCoreObject("test").addProperty("TestProperty")
                        .setSimpleType().get());
        List<Node> descendants = library.getDescendants_NamedTypes();
        Assert.assertEquals(1, library.getDescendants_NamedTypes().size());
        Assert.assertEquals(1, descendants.size());

        String libraryName = library.getName();
        String libraryNamespace = library.getNamespace();
        ProjectNode revertProject = mc.getRepositoryController().unlockAndRevert(library.getHead());

        // make sure new project has reverted library
        Assert.assertEquals(1, revertProject.getLibraries().size());
        LibraryNode revertedLib = revertProject.getLibraries().get(0);
        Assert.assertEquals(0, revertedLib.getDescendants_NamedTypes().size());

        // revert library from repository
        RepositoryItemNode nodeToRetrive = findRepositoryItem(revertedLib.getChain(),
                getRepositoryForTest());
        pc.add(defaultProject, nodeToRetrive.getItem());
        Assert.assertEquals(1, defaultProject.getChildren().size());
        LibraryChainNode nodes = (LibraryChainNode) defaultProject.getChildren().get(0);
        Assert.assertEquals(libraryName, nodes.getName());
        Assert.assertEquals(libraryNamespace, nodes.getNamespace());
        Assert.assertEquals(0, nodes.getDescendants_NamedTypes().size());
    }

    @Test
    public void unlockWithCommitShouldSetReadonlyToLibrary() throws RepositoryException,
            LibrarySaveException {
        ProjectNode uploadProject = createProject("ToUploadLibrary", getRepositoryForTest(), "test");
        LibraryNode testLibary = LibraryNodeBuilder.create("TestLibrary",
                getRepositoryForTest().getNamespace() + "/Test", "prefix", new Version(1, 0, 0))
                .build(uploadProject, pc);
        LibraryChainNode library = rc.manage(getRepositoryForTest(),
                Collections.singletonList(testLibary)).get(0);

        library.getHead().lock();
        String coreObjectName = "NewCoreObject";
        library.getHead().addMember(
                ComponentNodeBuilder.createCoreObject(coreObjectName).addProperty("TestProperty")
                        .setSimpleType().get());

        Assert.assertTrue(library.isEditable());
        mc.getRepositoryController().unlock(library.getHead());
        Assert.assertFalse(library.isEditable());
    }

    @Test
    public void unlockWithCommitShouldCommitChanges() throws RepositoryException,
            LibrarySaveException {
        ProjectNode uploadProject = createProject("ToUploadLibrary", getRepositoryForTest(), "test");
        LibraryNode testLibary = LibraryNodeBuilder.create("TestLibrary",
                getRepositoryForTest().getNamespace() + "/Test", "prefix", new Version(1, 0, 0))
                .build(uploadProject, pc);
        LibraryChainNode library = rc.manage(getRepositoryForTest(),
                Collections.singletonList(testLibary)).get(0);

        library.getHead().lock();
        String coreObjectName = "NewCoreObject";
        library.getHead().addMember(
                ComponentNodeBuilder.createCoreObject(coreObjectName).addProperty("TestProperty")
                        .setSimpleType().get());

        mc.getRepositoryController().unlock(library.getHead());

        Assert.assertEquals(1, library.getDescendants_NamedTypes().size());
        String libraryName = library.getHead().getName();
        String libraryChainName = library.getName();
        String namespace = library.getNamespace();
        library.close();

        // revert library from repository
        RepositoryItemNode nodeToRetrive = findRepositoryItem(libraryName, namespace, getRepositoryForTest());
        
        pc.add(defaultProject, nodeToRetrive.getItem());
        Assert.assertEquals(1, defaultProject.getChildren().size());
        LibraryChainNode nodes = (LibraryChainNode) defaultProject.getChildren().get(0);
        Assert.assertEquals(libraryChainName, nodes.getName());
        Assert.assertEquals(namespace, nodes.getHead().getNamespace());
        Assert.assertEquals(1, nodes.getDescendants_NamedTypes().size());
    }

    /**
     * Commit/revert - Retrieve library, add content then commit changes, close library. Retrieve
     * library Assure new content is in library. Revert library, close retrieve and assure content
     * is NOT in library.
     */

    /**
     * Finalize - Finalize (promote) library in project. Assure state changes in project and
     * repository.
     */

}
