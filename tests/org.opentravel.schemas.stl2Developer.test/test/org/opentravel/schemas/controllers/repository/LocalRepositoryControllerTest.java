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

import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.opentravel.schemacompiler.repository.Repository;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.impl.RemoteRepositoryClient;
import org.opentravel.schemacompiler.saver.LibrarySaveException;
import org.opentravel.schemas.controllers.DefaultRepositoryController;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.testUtils.MockLibrary;
import org.opentravel.schemas.trees.repository.RepositoryNode;
import org.opentravel.schemas.utils.LibraryNodeBuilder;
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalRepositoryControllerTest extends RepositoryControllerTest {
	static final Logger LOGGER = LoggerFactory.getLogger(RepositoryControllerTest.class);

	@Override
	public RepositoryNode getRepositoryForTest() {
		return rc.getLocalRepository();
	}

	@Test
	public void saveLocalRepositoryMetadataDoesntCleanupChangeSet() throws RepositoryException, LibrarySaveException {
		// getRepositoryForTest is forcing refresh. We want to avoid this
		RepositoryNode localRepository = getRepositoryForTest();

		// change credential to anonymous
		repositoryManager.setCredentials(remoteRepository, null, "");
		RemoteRepositoryClient client = (RemoteRepositoryClient) remoteRepository;
		Assert.assertNull(client.getUserId());

		// Manage library in local repository to force "fileManager.startChangeSet();"
		LibraryNode testLibary = LibraryNodeBuilder.create("name", localRepository.getNamespace() + "/Test", "prefix",
				new Version(1, 0, 0)).build(defaultProject, pc);
		rc.manage(localRepository, Collections.singletonList(testLibary));

		// need to refresh local repository info and refreshLocalRepositoryInfo() is private
		repositoryManager.getLocalRepositoryDisplayName();

		// make sure user id didn't change
		Assert.assertNull(client.getUserId());
	}

	@Test
	public void repositoryTestsNeedToBeMoved() throws RepositoryException {
		String myNS = "http://local/junits";
		DefaultRepositoryController rc = (DefaultRepositoryController) mc.getRepositoryController();
		assertTrue("Repository controller must not be null.", rc != null);
		assertTrue("Local repository must not be null.", rc.getLocalRepository() != null);
		List<RepositoryNode> repos = rc.getAll();
		RepositoryNode localRepoNode = rc.getLocalRepository();
		LOGGER.debug("Repo namespace is ", rc.getLocalRepository().getNamespaceWithPrefix());
		Repository localRepo = localRepoNode.getRepository();
		List<String> repoRootNSs = localRepo.listRootNamespaces();
		List<String> repoNSs = localRepo.listAllNamespaces();
		List<String> repoBaseNSs = localRepo.listBaseNamespaces();
		LOGGER.debug("Repo Root namespaces: ", repoRootNSs);
		LOGGER.debug("Repo Base namespaces: ", repoBaseNSs);
		LOGGER.debug("Repo All namespaces: ", repoNSs);
		try {
			localRepo.createRootNamespace(myNS);
		} catch (Exception e) {
			LOGGER.debug("Error setting Repo Root namespaces: ", e.getLocalizedMessage());
		}
		LOGGER.debug("Repo Root namespaces: ", localRepo.listRootNamespaces());

		LibraryNode ln = null;
		MockLibrary ml = new MockLibrary();

		// Given - a library in the local repo namespace
		ln = ml.createNewLibrary(rc.getLocalRepository().getNamespace(), "test1r", defaultProject);
		assertTrue("Library must not be null.", ln != null);
		// When - managed
		List<LibraryChainNode> lcns = rc.manage(rc.getLocalRepository(), Collections.singletonList(ln));
		// Then
		assertTrue("There must be library chains.", !lcns.isEmpty());
	}

}
