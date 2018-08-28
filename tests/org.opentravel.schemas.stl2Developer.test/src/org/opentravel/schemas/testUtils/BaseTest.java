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

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.saver.LibrarySaveException;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.controllers.ProjectController;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFinders;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.stl2Developer.reposvc.RepositoryTestUtils;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.utils.LibraryNodeBuilder;
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class that defines common methods used for all junit tests.
 * 
 * @author Dave Hollander
 */
public abstract class BaseTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(BaseTest.class);

	// Public global variables make tests more consistent.

	public static MainController mc;
	public static ProjectController pc;
	public static ProjectNode defaultProject;

	public final LoadFiles lf = new LoadFiles();
	public final MockLibrary ml = new MockLibrary();
	public LibraryNode ln = null;

	// private static TLModel tlModel;
	protected static int projectCounter = 1;

	// private static List<ProjectNode> projectsToClean = new ArrayList<>();
	// protected static ArrayList<File> filesToClean = new ArrayList<>();

	@BeforeClass
	public static void beforeTests() throws Exception {
		LOGGER.debug("Before class tests - base.");

		mc = OtmRegistry.getMainController();
		pc = mc.getProjectController();
		defaultProject = pc.getDefaultProject();

		assert OtmRegistry.getMainController() == mc;
		assert NodeFinders.findNodeByName("ID", ModelNode.XSD_NAMESPACE) != null;
		assert pc.getBuiltInProject() != null;

		projectCounter = 1;

		new TLModel();
	}

	@After
	public void afterEachTest() throws RepositoryException, IOException {
		// LOGGER.debug("After Each Test - starting clean up");
		// clearAll();
		// LOGGER.debug("After Each Test Complete");
	}

	@Before
	public void beforeEachTest() throws RepositoryException {
		LOGGER.debug("Before Each Test - starting clean up");
		clearAll();
		LOGGER.debug("Before Each Test Complete");
	}

	/**
	 * Clear the projects and libraries including default project.
	 */
	public static void clearAll() {
		pc.closeAll();
		defaultProject = pc.getDefaultProject();
		Node.getLibraryModelManager().clear(false);

		assert (Node.getModelNode().getManagedLibraries().size() == pc.getBuiltInProject().getChildren().size());
		assert Node.getModelNode().getTLModel().getUserDefinedLibraries().isEmpty();
		assert mc.getModelNode().getUserLibraries().isEmpty();
		assert Node.getLibraryModelManager().getUserLibraries().isEmpty();

		assert defaultProject.getLibraries().isEmpty();
		assert defaultProject.getChildren().isEmpty();
		assert defaultProject.getTLProject().getProjectItems().isEmpty();
	}

	// TODO - convert to not using builder
	public LibraryNode createLib(String name, String nsExtension, String prefix, ProjectNode project) {
		LibraryNode local1 = null;
		try {
			local1 = LibraryNodeBuilder.create(name, project.getNamespace() + nsExtension, prefix, new Version(1, 0, 0))
					.build(project, pc);
		} catch (LibrarySaveException e) {
			assertTrue(e.getLocalizedMessage(), 1 == 1);
		}
		return local1;
	}

	/**
	 * Create a new project in a temporary directory that will be deleted on completion.
	 * 
	 * @return
	 */
	public ProjectNode createProject() {
		File file = getTempDir();
		String ID = "TP-" + projectCounter;
		String name = ID;
		String description = "Test Project: " + ID;

		ProjectNode pn = pc.create(file, ID, name, description);

		assert pn != null;
		return pn;
	}

	/**
	 * Utility for sub-classes to get temporary directory in users file space. Directory will be deleted when virtual
	 * machine terminates.
	 */
	public static File getTempDir() {
		// OtmRegistry.registerRepositoryView(Mockito.mock(RepositoryView.class));
		String parentDir = System.getProperty("user.dir");
		String dirName = "/target/test-workspace/" + projectCounter++ + "/";
		File tmpWorkspace = new File(parentDir, dirName);
		LOGGER.debug("Created temporary directory " + dirName + "in " + parentDir);
		RepositoryTestUtils.deleteContents(tmpWorkspace);
		tmpWorkspace.deleteOnExit();
		return tmpWorkspace;
	}
}
