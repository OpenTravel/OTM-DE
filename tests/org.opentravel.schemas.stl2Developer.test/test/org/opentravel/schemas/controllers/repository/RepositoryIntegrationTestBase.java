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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.mockito.Mockito;
import org.opentravel.schemacompiler.index.FreeTextSearchService;
import org.opentravel.schemacompiler.repository.Project;
import org.opentravel.schemacompiler.repository.ProjectItem;
import org.opentravel.schemacompiler.repository.RemoteRepository;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.repository.impl.RemoteRepositoryClient;
import org.opentravel.schemas.controllers.DefaultRepositoryController;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.controllers.ProjectController;
import org.opentravel.schemas.node.LibraryChainNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.stl2Developer.reposvc.JettyTestServer;
import org.opentravel.schemas.stl2Developer.reposvc.RepositoryTestUtils;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.trees.repository.RepositoryNode;
import org.opentravel.schemas.trees.repository.RepositoryNode.RepositoryItemNode;
import org.opentravel.schemas.views.RepositoryView;

/**
 * Abstract base class that defines common methods used during live repository testing.
 * 
 * @author Pawel Jedruch
 */
public abstract class RepositoryIntegrationTestBase {

	protected static RepositoryManager repositoryManager;
	protected static RemoteRepository remoteRepository;
	protected static JettyTestServer jettyServer;
	protected static File tmpWorkspace;

	protected static DefaultRepositoryController rc;
	protected static MainController mc;
	protected static ProjectController pc;
	protected static ProjectNode defaultProject;
	private static List<ProjectNode> projectsToClean = new ArrayList<ProjectNode>();

	public abstract RepositoryNode getRepositoryForTest();

	@BeforeClass
	public final static void beforeTests() throws Exception {
		OtmRegistry.registerRepositoryView(Mockito.mock(RepositoryView.class));
		tmpWorkspace = new File(System.getProperty("user.dir"), "/target/test-workspace/");
		RepositoryTestUtils.deleteContents(tmpWorkspace);
		tmpWorkspace.deleteOnExit();
		startEmptyServer();
		mc = new MainController(repositoryManager);
		rc = (DefaultRepositoryController) mc.getRepositoryController();
		pc = mc.getProjectController();
		readdRemoteRepository();

	}

	@Before
	public void beforeEachTest() throws RepositoryException {
		defaultProject = createProject("Otm-Test-DefaultProject", rc.getLocalRepository(), "IT");
	}

	@After
	public void afterEachTest() throws RepositoryException, IOException {
		pc.closeAll();
		for (ProjectNode pn : projectsToClean) {
			RepositoryTestUtils.deleteContents(pn.getProject().getProjectFile().getParentFile());
		}
		projectsToClean.clear();
		reinitializeRepositories();
	}

	public void removeProject(ProjectNode pn) {
		if (projectsToClean.remove(pn)) {
			RepositoryTestUtils.deleteContents(pn.getProject().getProjectFile().getParentFile());
		}
	}

	public static ProjectNode createProject(String name, RepositoryNode nodeForNamespace, String extension) {
		return createProject(name, nodeForNamespace.getNamespace(), extension);
	}

	public static ProjectNode createProject(String name, String namespace, String extension) {
		File projectDir = new File(tmpWorkspace, name);
		File projectFile = new File(projectDir, name + ".otp");
		if (extension != null && !extension.isEmpty()) {
			namespace = namespace + "/" + extension;
		}
		ProjectNode project = pc.create(projectFile, namespace, name, "");
		if (project != null)
			projectsToClean.add(project);
		return project;
	}

	public RepositoryItemNode findRepositoryItem(LibraryChainNode chainNode, RepositoryNode parent) {
		return findRepositoryItem(chainNode.getHead().getName(), chainNode.getHead().getNamespace(), parent);
	}

	public RepositoryItemNode findRepositoryItem(String name, String namespace, RepositoryNode parent) {
		for (RepositoryItemNode ri : getItems(parent)) {
			RepositoryItem item = ri.getItem();
			if (item.getNamespace().equals(namespace) && item.getLibraryName().equals(name)) {
				return ri;
			}
		}
		return null;
	}

	private List<RepositoryItemNode> getItems(Node parent) {
		List<RepositoryItemNode> nodes = new ArrayList<RepositoryItemNode>();
		if (parent instanceof RepositoryItemNode) {
			return Collections.singletonList((RepositoryItemNode) parent);
		} else {
			for (Node child : parent.getChildren()) {
				nodes.addAll(getItems(child));
			}
		}
		return nodes;
	}

	@AfterClass
	public final static void afterTests() throws Exception {
		shutdownTestServer();
		RepositoryTestUtils.deleteContents(tmpWorkspace);
	}

	public final static void reinitializeRepositories() throws IOException, RepositoryException {
		FreeTextSearchService searchSerfice = FreeTextSearchService.getInstance();
		searchSerfice.stopService();
		jettyServer.initializeRuntimeRepository();
		searchSerfice.startService();
		RepositoryTestUtils.deleteContents(repositoryManager.getRepositoryLocation());
		recreateLocalRepository(repositoryManager);
		// sync root
		rc.sync(null);
	}

	private static void recreateLocalRepository(RepositoryManager repositoryManager2) throws RepositoryException {
		new RepositoryManager(repositoryManager2.getRepositoryLocation());
		RemoteRepositoryClient remote = (RemoteRepositoryClient) remoteRepository;
		repositoryManager.addRemoteRepository(remote.getEndpointUrl());
		repositoryManager.setCredentials(remoteRepository, "testuser", "password");
	}

	protected static void startEmptyServer() throws Exception {
		System.setProperty("ota2.repository.realTimeIndexing", "true");
		File emptySnapshot = new File(FileLocator.resolve(
				RepositoryIntegrationTestBase.class.getResource("/Resources/repo-snapshots/empty-repository")).toURI());
		File ota2config = new File(FileLocator.resolve(
				RepositoryIntegrationTestBase.class.getResource("/Resources/repo-snapshots/ota2.xml")).toURI());
		File tmpRepository = createFolder(tmpWorkspace, "ota-test-repository");
		File localRepository = createFolder(tmpWorkspace, "local-repository");
		repositoryManager = new RepositoryManager(localRepository);

		int port = getIntProperty("org.opentravel.schemas.test.repository.port", 19191);
		jettyServer = new JettyTestServer(port, emptySnapshot, tmpRepository, ota2config);
		jettyServer.start();

	}

	private static int getIntProperty(String key, int def) {
		try {
			String value = System.getProperty(key);
			return Integer.valueOf(value).intValue();
		} catch (NumberFormatException ex) {
			return def;
		}

	}

	public static final String getUserID() {
		return "testuser";
	}

	public static final String getUserPassword() {
		return "password";
	}

	protected static void readdRemoteRepository() throws RepositoryException {
		remoteRepository = jettyServer.configureRepositoryManager(repositoryManager);
		repositoryManager.setCredentials(remoteRepository, getUserID(), getUserPassword());
		rc.getRoot().addRepository(remoteRepository);
	}

	protected static void shutdownTestServer() throws Exception {
		jettyServer.stop();
	}

	protected ProjectItem findProjectItem(Project project, String filename) {
		ProjectItem result = null;

		for (ProjectItem item : project.getProjectItems()) {
			if (item.getFilename().equals(filename)) {
				result = item;
				break;
			}
		}
		return result;
	}

	protected RepositoryItem findRepositoryItem(List<RepositoryItem> itemList, String filename) {
		RepositoryItem result = null;

		for (RepositoryItem item : itemList) {
			if (item.getFilename().equals(filename)) {
				result = item;
				break;
			}
		}
		return result;
	}

	public static File createTempDirectory(String name) throws IOException {
		final File temp;

		temp = File.createTempFile(name, Long.toString(System.nanoTime()));

		if (!(temp.delete())) {
			throw new IOException("Could not delete temp file: " + temp.getAbsolutePath());
		}

		if (!(temp.mkdir())) {
			throw new IOException("Could not create temp directory: " + temp.getAbsolutePath());
		}

		return (temp);
	}

	public static File createFolder(File parent, String folder) {
		File file = new File(parent, folder);
		try {
			file.mkdir();
		} catch (Exception e) {
			System.err.println("Error making directory " + parent + " " + folder + " " + e.getLocalizedMessage());
		}
		return file;
	}

}
