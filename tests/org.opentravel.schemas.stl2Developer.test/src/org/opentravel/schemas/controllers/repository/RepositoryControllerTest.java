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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.opentravel.schemacompiler.loader.LibraryLoaderException;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.saver.LibrarySaveException;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemas.controllers.DefaultRepositoryController;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeEditStatus;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.testUtils.BaseRepositoryTest;
import org.opentravel.schemas.trees.repository.RepositoryNode.RepositoryItemNode;
import org.opentravel.schemas.utils.ComponentNodeBuilder;
import org.opentravel.schemas.utils.LibraryNodeBuilder;
import org.osgi.framework.Version;

/**
 * @author Pawel Jedruch
 * 
 */
public abstract class RepositoryControllerTest extends BaseRepositoryTest {

	protected static boolean SKIP = true; // Skip these tests which can take 10 minutes or more
	// protected MockLibrary ml = new MockLibrary();

	/**
	 * Manage - Library Manage (publish) libraries (with and without errors) in repositories. Assure published library
	 * is converted to LibraryChain in projectNode tree.
	 * 
	 * @throws LibrarySaveException
	 */

	@Test
	public void manageWithWrongNamespaceShouldNotCreateChain() throws LibrarySaveException {
		if (SKIP)
			return;
		LibraryNode testLibary = LibraryNodeBuilder.create("name", "__illegalNamespace", "prefix", new Version(1, 0, 0))
				.build(defaultProject, pc);
		List<LibraryChainNode> chains = rc.manage(getRepositoryForTest(), Collections.singletonList(testLibary));
		Assert.assertTrue("Should not craete library for wrong namespace.", chains.isEmpty());
	}

	@Test
	public void manageWithCorrectNamespaceShouldNotBeEdiableWithDisabledPolicy()
			throws RepositoryException, LibrarySaveException {
		if (SKIP)
			return;
		ProjectNode project = createProject("correctNamespace", getRepositoryForTest(), "test");
		LibraryNode testLibary = LibraryNodeBuilder
				.create("name", project.getNamespace(), "prefix", new Version(1, 0, 0)).build(project, pc);
		Assert.assertTrue(testLibary.isEditable());
		LibraryChainNode library = rc.manage(getRepositoryForTest(), Collections.singletonList(testLibary)).get(0);
		Assert.assertNotNull("Should create library for correct namespace.", library);
		Assert.assertFalse("Libarry should be non-editable after managing in repository.", library.isEditable());
	}

	@Test
	public void manageShouldUpdateCorrectRepositoryNode() throws RepositoryException, LibrarySaveException {
		if (SKIP)
			return;
		ProjectNode uploadProject = createProject("MangeThis", getRepositoryForTest(), "test");
		LibraryNode testLibary = LibraryNodeBuilder
				.create("TestLibrary", getRepositoryForTest().getNamespace() + "/Test", "prefix", new Version(1, 0, 0))
				.build(uploadProject, pc);
		LibraryChainNode library = rc.manage(getRepositoryForTest(), Collections.singletonList(testLibary)).get(0);
		RepositoryItemNode item = findRepositoryItem(library, getRepositoryForTest());
		Assert.assertNotNull(item);
	}

	/**
	 * Retrieve - Retrieve repository object into pre-existing project Retrieve repository object into a newly created
	 * project with same base namespace as library
	 */

	@Test
	public void retrieveObjectToPreExisitingProject() throws RepositoryException, LibrarySaveException {
		if (SKIP)
			return;
		ProjectNode uploadProject = createProject("ToUploadLibrary", getRepositoryForTest(), "test");
		LibraryNode testLibary = LibraryNodeBuilder
				.create("TestLibrary", getRepositoryForTest().getNamespace() + "/Test", "prefix", new Version(1, 0, 0))
				.build(uploadProject, pc);
		LibraryChainNode library = rc.manage(getRepositoryForTest(), Collections.singletonList(testLibary)).get(0);

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
	 * Versioning - major, minor, patch Project controller Legal library, illegal library (duplicate VWA namespace:name)
	 * Assure new version is in LibraryChain correctly. Assure old version is not directly in TLProject or
	 * projectNode.children Assure new version is in repository. Assure versions can start with 0.0.0 and variations.
	 */

	@Test
	public void createChainNodeShouldSetHead() throws RepositoryException, LibrarySaveException {
		if (SKIP)
			return;
		ProjectNode uploadProject = createProject("ToUploadLibrary", getRepositoryForTest(), "Test");
		LibraryNode testLibary = LibraryNodeBuilder
				.create("TestLibrary", getRepositoryForTest().getNamespace() + "/Test", "prefix", new Version(1, 0, 0))
				.makeFinal().build(uploadProject, pc);
		LibraryChainNode chain = rc.manage(getRepositoryForTest(), Collections.singletonList(testLibary)).get(0);
		rc.createPatchVersion(chain.getHead());
		assertSame(chain.getHead(), chain.getLibrary());
	}

	@Test
	public void createMajorVersionWithSimpleCoreWithoutType() throws RepositoryException, LibrarySaveException {
		if (SKIP)
			return;
		ProjectNode uploadProject = createProject("ToUploadLibrary", getRepositoryForTest(), "test");
		LibraryNode testLibary = LibraryNodeBuilder
				.create("TestLibrary", getRepositoryForTest().getNamespace() + "/Test", "prefix", new Version(1, 0, 0))
				.makeFinal().build(uploadProject, pc);
		testLibary.addMember(ComponentNodeBuilder.createCoreObject("test").addProperty("testProperty").get());
		LibraryChainNode chain = rc.manage(getRepositoryForTest(), Collections.singletonList(testLibary)).get(0);
		LibraryNode newMajor = rc.createMajorVersion(chain.getHead());
		ValidationFindings findings = newMajor.validate();
		Assert.assertEquals(0, findings.getFindingsAsList(FindingType.ERROR).size());
	}

	@Test
	public void createMajorVersionWithSimpleCoreWithEmtpyType() throws RepositoryException, LibrarySaveException {
		if (SKIP)
			return;
		ProjectNode uploadProject = createProject("ToUploadLibrary", getRepositoryForTest(), "test");
		LibraryNode testLibary = LibraryNodeBuilder
				.create("TestLibrary", getRepositoryForTest().getNamespace() + "/Test", "prefix", new Version(1, 0, 0))
				.makeFinal().build(uploadProject, pc);
		testLibary.addMember(
				ComponentNodeBuilder.createCoreObject("test").addProperty("TestProperty").setAssignedType().get());
		LibraryChainNode chain = rc.manage(getRepositoryForTest(), Collections.singletonList(testLibary)).get(0);
		LibraryNode newMajor = rc.createMajorVersion(chain.getHead());
		ValidationFindings findings = newMajor.validate();
		Assert.assertEquals(0, findings.count());
	}

	@Test
	public void createPatchVersionWithSimpleCoreWithEmtpyType() throws RepositoryException, LibrarySaveException {
		if (SKIP)
			return;
		ProjectNode uploadProject = createProject("ToUploadLibrary", getRepositoryForTest(), "Test");
		LibraryNode testLibary = LibraryNodeBuilder
				.create("TestLibrary", getRepositoryForTest().getNamespace() + "/Test", "prefix", new Version(1, 0, 0))
				.makeFinal().build(uploadProject, pc);
		LibraryChainNode chain = rc.manage(getRepositoryForTest(), Collections.singletonList(testLibary)).get(0);
		LibraryNode newPatch = rc.createPatchVersion(chain.getHead());
		assertEquals(NodeEditStatus.PATCH, newPatch.getEditStatus());
		assertEquals(NodeEditStatus.PATCH, chain.getEditStatus());
		assertEquals(NodeEditStatus.PATCH, chain.getLibrary().getEditStatus());
	}

	/**
	 * Library Chain Access - Both patch and minor Version 3 times Get chain for latest version and assure all members
	 * Get chain for middle member and assure only older members are returned
	 */

	/**
	 * Repository Item metadata accesss - Retrieve all fields used in library view from a repository item.
	 */

	/**
	 * Lock/Unlock - Lock and unlock test libraries from repository Assure unlocked libraries are read-only when
	 * retrieved from repository.
	 * 
	 * @throws LibraryLoaderException
	 */

	@Test
	public void lockShouldMakeLibaryEditable()
			throws RepositoryException, LibrarySaveException, LibraryLoaderException {
		if (SKIP)
			return;
		ProjectNode uploadProject = createProject("ToUploadLibrary", getRepositoryForTest(), "test");
		LibraryNode testLibary = LibraryNodeBuilder
				.create("TestLibrary", getRepositoryForTest().getNamespace() + "/Test", "prefix", new Version(1, 0, 0))
				.build(uploadProject, pc);
		LibraryChainNode library = rc.manage(getRepositoryForTest(), Collections.singletonList(testLibary)).get(0);

		Assert.assertFalse(library.isEditable());
		library.getHead().lock();
		Assert.assertTrue(library.isEditable());
	}

	@Test
	public void unlockWithoutCommitShouldRevertChanges()
			throws RepositoryException, LibrarySaveException, LibraryLoaderException {
		if (SKIP)
			return;
		ProjectNode uploadProject = createProject("ToUploadLibrary", getRepositoryForTest(), "test");
		LibraryNode testLibary = LibraryNodeBuilder
				.create("TestLibrary", getRepositoryForTest().getNamespace() + "/Test", "prefix", new Version(1, 0, 0))
				.build(uploadProject, pc);
		LibraryChainNode library = rc.manage(getRepositoryForTest(), Collections.singletonList(testLibary)).get(0);

		library.getHead().lock();
		library.getHead().addMember(
				ComponentNodeBuilder.createCoreObject("test").addProperty("TestProperty").setAssignedType().get());
		Assert.assertEquals(1, library.getDescendants_LibraryMembersAsNodes().size());

		String libraryName = library.getName();
		String namespace = library.getNamespace();
		ProjectNode revertProject = mc.getRepositoryController().unlockAndRevert(library.getHead());

		// make sure new project has reverted library
		Assert.assertEquals(1, revertProject.getLibraries().size());
		LibraryNode revertedLib = revertProject.getLibraries().get(0);
		Assert.assertEquals(0, revertedLib.getDescendants_LibraryMembersAsNodes().size());

		// revert library from repository
		RepositoryItemNode nodeToRetrive = findRepositoryItem(revertedLib.getChain(), getRepositoryForTest());
		pc.add(defaultProject, nodeToRetrive.getItem());
		Assert.assertEquals(1, defaultProject.getChildren().size());
		LibraryChainNode nodes = (LibraryChainNode) defaultProject.getChildren().get(0);
		Assert.assertEquals(libraryName, nodes.getName());
		Assert.assertEquals(namespace, nodes.getNamespace());
		Assert.assertEquals(0, nodes.getDescendants_LibraryMembersAsNodes().size());
	}

	@Test
	public void unlockWithoutCommitForDefaultProjectShouldRevertChanges()
			throws RepositoryException, LibrarySaveException, LibraryLoaderException {
		if (SKIP)
			return;
		LibraryNode testLibary = LibraryNodeBuilder
				.create("TestLibrary", getRepositoryForTest().getNamespace() + "/Test", "prefix", new Version(1, 0, 0))
				.build(pc.getDefaultProject(), pc);
		LibraryChainNode library = rc.manage(getRepositoryForTest(), Collections.singletonList(testLibary)).get(0);

		library.getHead().lock();
		library.getHead().addMember(
				ComponentNodeBuilder.createCoreObject("test").addProperty("TestProperty").setAssignedType().get());
		List<Node> descendants = library.getDescendants_LibraryMembersAsNodes();
		Assert.assertEquals(1, library.getDescendants_LibraryMembersAsNodes().size());
		Assert.assertEquals(1, descendants.size());

		String libraryName = library.getName();
		String libraryNamespace = library.getNamespace();
		ProjectNode revertProject = mc.getRepositoryController().unlockAndRevert(library.getHead());

		// make sure new project has reverted library
		Assert.assertEquals(1, revertProject.getLibraries().size());
		LibraryNode revertedLib = revertProject.getLibraries().get(0);
		Assert.assertEquals(0, revertedLib.getDescendants_LibraryMembersAsNodes().size());

		// revert library from repository
		RepositoryItemNode nodeToRetrive = findRepositoryItem(revertedLib.getChain(), getRepositoryForTest());
		pc.add(defaultProject, nodeToRetrive.getItem());
		Assert.assertEquals(1, defaultProject.getChildren().size());
		LibraryChainNode nodes = (LibraryChainNode) defaultProject.getChildren().get(0);
		Assert.assertEquals(libraryName, nodes.getName());
		Assert.assertEquals(libraryNamespace, nodes.getNamespace());
		Assert.assertEquals(0, nodes.getDescendants_LibraryMembersAsNodes().size());
	}

	@Test
	public void unlockWithCommitShouldSetReadonlyToLibrary()
			throws RepositoryException, LibrarySaveException, LibraryLoaderException {
		if (SKIP)
			return;
		ProjectNode uploadProject = createProject("ToUploadLibrary", getRepositoryForTest(), "test");
		LibraryNode testLibary = LibraryNodeBuilder
				.create("TestLibrary", getRepositoryForTest().getNamespace() + "/Test", "prefix", new Version(1, 0, 0))
				.build(uploadProject, pc);
		LibraryChainNode library = rc.manage(getRepositoryForTest(), Collections.singletonList(testLibary)).get(0);

		library.getHead().lock();
		String coreObjectName = "NewCoreObject";
		library.getHead().addMember(ComponentNodeBuilder.createCoreObject(coreObjectName).addProperty("TestProperty")
				.setAssignedType().get());

		Assert.assertTrue(library.isEditable());
		mc.getRepositoryController().unlock(library.getHead());
		Assert.assertFalse(library.isEditable());
	}

	@Test
	public void unlockWithCommitShouldCommitChanges()
			throws RepositoryException, LibrarySaveException, LibraryLoaderException {
		if (SKIP)
			return;
		ProjectNode uploadProject = createProject("ToUploadLibrary", getRepositoryForTest(), "test");
		LibraryNode testLibary = LibraryNodeBuilder
				.create("TestLibrary", getRepositoryForTest().getNamespace() + "/Test", "prefix", new Version(1, 0, 0))
				.build(uploadProject, pc);
		LibraryChainNode lcn = rc.manage(getRepositoryForTest(), Collections.singletonList(testLibary)).get(0);

		lcn.getHead().lock();
		String coreObjectName = "NewCoreObject";
		lcn.getHead().addMember(ComponentNodeBuilder.createCoreObject(coreObjectName).addProperty("TestProperty")
				.setAssignedType().get());

		DefaultRepositoryController rc = (DefaultRepositoryController) mc.getRepositoryController();
		rc.unlock(lcn.getHead());

		Assert.assertEquals(1, lcn.getDescendants_LibraryMembersAsNodes().size());
		String libraryName = lcn.getHead().getName();
		String libraryChainName = lcn.getName();
		String namespace = lcn.getNamespace();
		lcn.close();

		// revert library from repository
		RepositoryItemNode nodeToRetrive = findRepositoryItem(libraryName, namespace, getRepositoryForTest());

		pc.add(defaultProject, nodeToRetrive.getItem());
		Assert.assertEquals(1, defaultProject.getChildren().size());
		LibraryChainNode nodes = (LibraryChainNode) defaultProject.getChildren().get(0);
		Assert.assertEquals(libraryChainName, nodes.getName());
		Assert.assertEquals(namespace, nodes.getHead().getNamespace());
		Assert.assertEquals(1, nodes.getDescendants_LibraryMembersAsNodes().size());
	}

	/**
	 * Commit/revert - Retrieve library, add content then commit changes, close library. Retrieve library Assure new
	 * content is in library. Revert library, close retrieve and assure content is NOT in library.
	 */

	/**
	 * Finalize - Finalize (promote) library in project. Assure state changes in project and repository.
	 */

}
