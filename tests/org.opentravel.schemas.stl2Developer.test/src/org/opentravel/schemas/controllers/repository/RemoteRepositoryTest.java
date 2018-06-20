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

import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryItemState;
import org.opentravel.schemacompiler.saver.LibrarySaveException;
import org.opentravel.schemas.controllers.DefaultProjectController;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.trees.repository.RepositoryNode;
import org.opentravel.schemas.utils.ComponentNodeBuilder;
import org.opentravel.schemas.utils.LibraryNodeBuilder;
import org.osgi.framework.Version;

public class RemoteRepositoryTest extends RepositoryControllerTest {

	@Override
	public RepositoryNode getRepositoryForTest() {
		for (RepositoryNode rn : rc.getAll()) {
			if (rn.isRemote()) {
				return rn;
			}
		}
		throw new IllegalStateException("Missing remote repository. Check your configuration.");
	}

	/**
	 * Search
	 */
	@Test
	public void searchDraftLibrary() throws RepositoryException, LibrarySaveException {
		if (SKIP)
			return;
		ProjectNode uploadProject = createProject("ToUploadLibrary", getRepositoryForTest(), "");
		LibraryNode testLibary = LibraryNodeBuilder.create("TestLibrary",
				getRepositoryForTest().getNamespace() + "/Test", "prefix", new Version(1, 0, 0)).build(uploadProject,
				pc);
		testLibary.addMember((LibraryMemberInterface) ComponentNodeBuilder.createSimpleCore("test").get());
		rc.manage(getRepositoryForTest(), Collections.singletonList(testLibary));
		List<RepositoryItem> results = rc.search("tes*");
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(testLibary.getName(), results.get(0).getLibraryName());

	}

	@Test
	public void searchFinalLibrary() throws RepositoryException, LibrarySaveException {
		if (SKIP)
			return;
		ProjectNode uploadProject = createProject("ToUploadLibrary", getRepositoryForTest(), "");
		LibraryNode testLibary = LibraryNodeBuilder
				.create("TestLibrary", getRepositoryForTest().getNamespace() + "/Test", "prefix", new Version(1, 0, 0))
				.makeFinal().build(uploadProject, pc);
		testLibary.addMember((LibraryMemberInterface) ComponentNodeBuilder.createSimpleCore("test").get());
		rc.manage(getRepositoryForTest(), Collections.singletonList(testLibary));
		List<RepositoryItem> results = rc.search("tes*");
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(testLibary.getName(), results.get(0).getLibraryName());
	}

	@Test
	public void searchEarlyVersionLibrary() throws RepositoryException, LibrarySaveException {
		if (SKIP)
			return;
		ProjectNode uploadProject = createProject("ToUploadLibrary", getRepositoryForTest(), "");
		LibraryNode testLibary = LibraryNodeBuilder
				.create("TestLibrary", getRepositoryForTest().getNamespace() + "/Test", "prefix", new Version(1, 0, 0))
				.makeFinal().build(uploadProject, pc);
		testLibary.addMember((LibraryMemberInterface) ComponentNodeBuilder.createSimpleCore("test").get());
		LibraryChainNode chain = rc.manage(getRepositoryForTest(), Collections.singletonList(testLibary)).get(0);
		LibraryNode newMajor = rc.createMajorVersion(chain.getHead());
		List<RepositoryItem> results = rc.search("tes*");
		Assert.assertEquals(2, results.size());
		Assert.assertEquals(newMajor.getName(), results.get(0).getLibraryName());
	}

	/**
	 * <pre>
	 * 1. Manage Library in repository 
	 * 2. Lock Library 
	 * 3. Remove repository from step 1. 
	 * 4. Reopen project with library
	 * </pre>
	 * 
	 * @throws LibrarySaveException
	 * @throws RepositoryException
	 */
	@Test
	public void openLibraryWithMissingRepository() throws LibrarySaveException, RepositoryException {
		if (SKIP)
			return;
		ProjectNode uploadProject = createProject("ToUploadLibrary", getRepositoryForTest(), "test");
		LibraryNode testLibary = LibraryNodeBuilder.create("TestLibrary",
				getRepositoryForTest().getNamespace() + "/Test", "prefix", new Version(1, 0, 0)).build(uploadProject,
				pc);
		LibraryChainNode chain = rc.manage(getRepositoryForTest(), Collections.singletonList(testLibary)).get(0);
		boolean locked = rc.lock(chain.getHead());
		Assert.assertTrue(locked);
		Assert.assertEquals(RepositoryItemState.MANAGED_WIP, chain.getHead().getProjectItem().getState());
		try {
			rc.removeRemoteRepository(getRepositoryForTest());
			pc.close(uploadProject);

			DefaultProjectController dc = (DefaultProjectController) pc;
			// RepositoryUtils.checkItemState( item, this ); will throw NPE
			ProjectNode reopenedProject = dc.open(uploadProject.getTLProject().getProjectFile().toString(), null).project;

			Assert.assertNotNull("Project couldn't be created. "
					+ "Reason of this is that this project is already opened but"
					+ " with incosistent state becouse of NPE", reopenedProject);
		} finally {
			readdRemoteRepository();
		}

	}
}
