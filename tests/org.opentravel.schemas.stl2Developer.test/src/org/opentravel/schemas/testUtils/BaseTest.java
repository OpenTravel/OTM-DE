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

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.saver.LibrarySaveException;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.controllers.ProjectController;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFinders;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
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

	protected static MainController mc;
	protected static ProjectController pc;
	protected static ProjectNode defaultProject;

	private static TLModel tlModel;

	public LoadFiles lf = new LoadFiles();
	public MockLibrary ml = new MockLibrary();
	public LibraryNode ln = null;

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

		tlModel = new TLModel();
	}

	@After
	public void afterEachTest() {
		// LOGGER.debug("After Each Test - starting clean up");
		// clearAll();
		// LOGGER.debug("After Each Test Complete");
	}

	@Before
	public void beforeEachTest() {
		LOGGER.debug("Before Each Test - starting clean up");
		clearAll();
		LOGGER.debug("Before Each Test Complete");
	}

	/**
	 * Clear the projects and libraries including default project.
	 */
	public static void clearAll() {
		pc.closeAll();
		pc.close(defaultProject);
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

	// public static ProjectNode createProject(String name, RepositoryNode nodeForNamespace, String nameSpaceSuffix) {
	// File projectDir = new File(tmpWorkspace, name);
	// File projectFile = new File(projectDir, name + ".otp");
	// ProjectNode project = pc.create(projectFile, nodeForNamespace.getNamespace() + "/" + nameSpaceSuffix, name, "");
	// projectsToClean.add(project);
	// return project;
	// }
	//
	// @AfterClass
	// public final static void afterTests() throws Exception {
	// RepositoryTestUtils.deleteContents(tmpWorkspace);
	// }
	//
	// public static File createTempDirectory(String name) throws IOException {
	// final File temp;
	//
	// temp = File.createTempFile(name, Long.toString(System.nanoTime()));
	//
	// if (!(temp.delete())) {
	// throw new IOException("Could not delete temp file: " + temp.getAbsolutePath());
	// }
	//
	// if (!(temp.mkdir())) {
	// throw new IOException("Could not create temp directory: " + temp.getAbsolutePath());
	// }
	//
	// return (temp);
	// }
	//
	// public static File createFolder(File parent, String folder) {
	// File file = new File(parent, folder);
	// file.mkdir();
	// return file;
	// }

	// FIXME - add these in
	// public abstract TLModelElement createTL();
	// public abstract void check();
}
