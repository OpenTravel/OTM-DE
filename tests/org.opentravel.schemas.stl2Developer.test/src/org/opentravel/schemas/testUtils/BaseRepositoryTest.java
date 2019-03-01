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
package org.opentravel.schemas.testUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.opentravel.schemacompiler.index.FreeTextSearchService;
import org.opentravel.schemacompiler.index.FreeTextSearchServiceFactory;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.repository.Project;
import org.opentravel.schemacompiler.repository.ProjectItem;
import org.opentravel.schemacompiler.repository.RemoteRepository;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.repository.impl.RemoteRepositoryClient;
import org.opentravel.schemas.controllers.DefaultRepositoryController;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFinders;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.stl2Developer.reposvc.JettyTestServer;
import org.opentravel.schemas.stl2Developer.reposvc.RepositoryTestUtils;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.trees.repository.RepositoryNode;
import org.opentravel.schemas.trees.repository.RepositoryNode.RepositoryItemNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class that defines common methods used during live repository testing.
 * <p>
 * This base class must be used when tests save, open and manage libraries in repositories.
 * <p>
 * This compute intensive base class creates a local repository in the file system, starts the Jetty server and cleans
 * up on completion.
 * 
 * @author Dave Hollander / Pawel Jedruch
 */
// The @BeforeClass methods of superclasses will be run before those of the current class,
// unless they are shadowed in the current class.
public abstract class BaseRepositoryTest extends BaseTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(BaseRepositoryTest.class);

	protected static RepositoryManager repositoryManager;
	protected static RemoteRepository remoteRepository;
	protected static JettyTestServer jettyServer;
	protected static File tmpWorkspace;
	private static List<ProjectNode> projectsToClean = new ArrayList<>();

	// Public global variables to make tests more consistent.
	public static DefaultRepositoryController rc;
	public RepositoryNode localRepo = null;
	public ProjectNode project = null;
	public LibraryChainNode lcn = null;

	public abstract RepositoryNode getRepositoryForTest();

	@BeforeClass
	public final static void beforeTests() throws Exception {
		// This method shadows the base class to allow the server to be started.
		LOGGER.debug("Before class tests - base repository test.");

		tmpWorkspace = BaseTest.getTempDir();
		LOGGER.debug("Workspace created in: " + tmpWorkspace.getPath());

		// OtmRegistry.registerRepositoryView(Mockito.mock(RepositoryView.class));
		// tmpWorkspace = new File(System.getProperty("user.dir"), "/target/test-workspace/");
		// RepositoryTestUtils.deleteContents(tmpWorkspace);
		// tmpWorkspace.deleteOnExit();

		repositoryManager = startEmptyServer(); // must be done before MC accessed

		// Use new repository manager to create main controller
		mc = new MainController(repositoryManager);
		rc = (DefaultRepositoryController) mc.getRepositoryController();
		pc = mc.getProjectController();
		assert OtmRegistry.getMainController() == mc;
		assert pc.getBuiltInProject() != null;
		assert rc != null;
		assert NodeFinders.findNodeByName("ID", ModelNode.XSD_NAMESPACE) != null;

		readdRemoteRepository();
		LOGGER.debug("Before class complete - base repository test.");
	}

	// @Override
	// @Before
	// public void beforeEachTest() throws RepositoryException {
	// LOGGER.debug("Before class tests - base repository test.");
	// defaultProject = createProject("Otm-Test-DefaultProject", rc.getLocalRepository(), "IT");
	// }

	// Different name to avoid shadowing the base class
	@After
	public void afterEachRepositoryTest() throws RepositoryException, IOException {
		pc.closeAll();
		for (ProjectNode pn : projectsToClean) {
			RepositoryTestUtils.deleteContents(pn.getTLProject().getProjectFile().getParentFile());
		}
		projectsToClean.clear();
		reinitializeRepositories();
	}

	public void removeProject(ProjectNode pn) {
		if (projectsToClean.remove(pn)) {
			RepositoryTestUtils.deleteContents(pn.getTLProject().getProjectFile().getParentFile());
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
		List<RepositoryItemNode> nodes = new ArrayList<>();
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
		FreeTextSearchService searchSerfice = FreeTextSearchServiceFactory.getInstance();
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

	/**
	 * Create repositoryManager for local repository and start jetty server.
	 * 
	 * @throws Exception
	 */
	protected static RepositoryManager startEmptyServer() throws Exception {
		FreeTextSearchServiceFactory.setRealTimeIndexing( true );
		File emptySnapshot = new File(FileLocator
				.resolve(BaseRepositoryTest.class.getResource("/Resources/repo-snapshots/empty-repository")).toURI());
		File ota2config = new File(FileLocator
				.resolve(BaseRepositoryTest.class.getResource("/Resources/repo-snapshots/ota2.xml")).toURI());
		File tmpRepository = createFolder(tmpWorkspace, "ota-test-repository");
		File localRepository = createFolder(tmpWorkspace, "local-repository");
		RepositoryManager repoMgr = new RepositoryManager(localRepository);

		int port = getIntProperty("org.opentravel.schemas.test.repository.port", 19191);
		jettyServer = new JettyTestServer(port, emptySnapshot, tmpRepository, ota2config);
		jettyServer.start();
		return repoMgr;
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
		try {
			remoteRepository = jettyServer.configureRepositoryManager(repositoryManager);
		} catch (Exception e) {
			LOGGER.debug("Exception from repository manager: " + e.getLocalizedMessage());
			LOGGER.debug(
					"Repository Location: " + repositoryManager.getFileManager().getRepositoryLocation().toString());
			// TODO Auto-generated catch block
			LOGGER.debug("Project Counter is: " + projectCounter);
			LOGGER.debug("");
			// e.printStackTrace();
		}
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

	/**
	 * Make the library finalized.
	 * 
	 * @param ln
	 * @return true if the resulting library status is FINAL
	 */
	public boolean makeFinal(LibraryNode ln) {
		boolean result = false;
		ml.check(ln, true); // must be valid to promote

		if (ln.getStatus().equals(TLLibraryStatus.DRAFT))
			result = rc.promote(ln, TLLibraryStatus.UNDER_REVIEW);

		if (ln.getStatus().equals(TLLibraryStatus.UNDER_REVIEW))
			result = rc.promote(ln, TLLibraryStatus.FINAL);

		return ln.getStatus().equals(TLLibraryStatus.FINAL);
	}

}
