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
package org.opentravel.schemas.utils;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.saver.LibrarySaveException;
import org.opentravel.schemas.controllers.DefaultRepositoryController;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.controllers.ProjectController;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFinders;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.typeProviders.SimpleTypeNode;
import org.opentravel.schemas.stl2Developer.reposvc.RepositoryTestUtils;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.trees.repository.RepositoryNode;
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class that defines common methods used during live repository testing.
 * 
 * @author Pawel Jedruch
 */
public abstract class BaseProjectTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(BaseProjectTest.class);

	protected static DefaultRepositoryController rc;
	protected static ProjectNode testProject;

	protected static File tmpWorkspace;
	private static List<ProjectNode> projectsToClean = new ArrayList<>();

	protected static MainController mc;
	protected static ProjectController pc;
	protected static ProjectNode defaultProject;

	// Takes about 40-60 seconds
	@BeforeClass
	public static void beforeTests() throws Exception {
		LOGGER.debug("Before class tests - cleaning project workspace.");
		tmpWorkspace = new File(System.getProperty("user.dir"), "/target/test-workspace/");
		RepositoryTestUtils.deleteContents(tmpWorkspace);
		tmpWorkspace.deleteOnExit();
		mc = OtmRegistry.getMainController();
		// mc = new MainController(); // Loads the built-in libraries.
		rc = (DefaultRepositoryController) mc.getRepositoryController();
		pc = mc.getProjectController();
		defaultProject = pc.getDefaultProject();

		SimpleTypeNode sn = (SimpleTypeNode) NodeFinders.findNodeByName("ID", ModelNode.XSD_NAMESPACE);
		if (sn == null)
			LOGGER.error("Missing simple type ID.");

		assert OtmRegistry.getMainController() == mc;
		assert NodeFinders.findNodeByName("ID", ModelNode.XSD_NAMESPACE) != null;
		assert pc.getBuiltInProject() != null;
		LOGGER.debug("Before class tests cleaned workspace: " + tmpWorkspace.getPath());
	}

	@Before
	public void beforeEachTest() throws Exception {
		LOGGER.debug("Before Each Test");
		pc.closeAll();
		Node.getLibraryModelManager().clear(false);
		defaultProject = pc.getDefaultProject();

		assert rc.getLocalRepository() != null;
		testProject = createProject("Otm-Test-TestProject", rc.getLocalRepository(), "IT");
		assertTrue(testProject != null);
		LOGGER.debug("Before Each Test end.");
	}

	@Deprecated
	protected void callBeforeEachTest() throws Exception {
		LOGGER.debug("Before tests");
		pc.closeAll();
		testProject = createProject("Otm-Test-TestProject", rc.getLocalRepository(), "IT");
		assertTrue(testProject != null);
		// Give access to the sub-classes
	}

	@After
	public void afterEachTest() throws RepositoryException, IOException {
		LOGGER.debug("After Each Test - starting project clean up");

		// Get file list before closing the projects
		ArrayList<File> filesToClean = new ArrayList<>();
		for (ProjectNode pn : projectsToClean)
			filesToClean.add(pn.getTLProject().getProjectFile().getParentFile());
		projectsToClean.clear();

		pc.closeAll();
		// Node.getLibraryModelManager().clear(false);
		defaultProject = pc.getDefaultProject(); // close all creates a new defaultProject

		// use file list from super.afterEachTest()
		for (File projFile : filesToClean) {
			LOGGER.debug("Cleaning project file = " + projFile.toString());
			RepositoryTestUtils.deleteContents(projFile);
		}

		assert (Node.getModelNode().getManagedLibraries().size() == pc.getBuiltInProject().getChildren().size());
		assert Node.getModelNode().getTLModel().getUserDefinedLibraries().isEmpty();
		assert mc.getModelNode().getUserLibraries().isEmpty();
		assert Node.getLibraryModelManager().getUserLibraries().isEmpty();

		assert defaultProject.getLibraries().isEmpty();
		assert defaultProject.getChildren().isEmpty();
		assert defaultProject.getTLProject().getProjectItems().isEmpty();

		LOGGER.debug("Project - After Each Test Complete");
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

	public static ProjectNode createProject(String name, RepositoryNode nodeForNamespace, String nameSpaceSuffix) {
		File projectDir = new File(tmpWorkspace, name);
		File projectFile = new File(projectDir, name + ".otp");
		ProjectNode project = pc.create(projectFile, nodeForNamespace.getNamespace() + "/" + nameSpaceSuffix, name, "");
		projectsToClean.add(project);
		return project;
	}

	@AfterClass
	public final static void afterTests() throws Exception {
		RepositoryTestUtils.deleteContents(tmpWorkspace);
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
		file.mkdir();
		return file;
	}

	// FIXME - add these in
	// public abstract TLModelElement createTL();
	// public abstract void check();
}
