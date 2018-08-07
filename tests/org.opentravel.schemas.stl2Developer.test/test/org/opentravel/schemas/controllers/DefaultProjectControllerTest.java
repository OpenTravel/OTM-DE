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
package org.opentravel.schemas.controllers;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLNamespaceImport;
import org.opentravel.schemacompiler.saver.LibrarySaveException;
import org.opentravel.schemacompiler.util.URLUtils;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFinders;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.node.libraries.LibraryNavNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.properties.ElementNode;
import org.opentravel.schemas.node.properties.TypedPropertyNode;
import org.opentravel.schemas.node.typeProviders.SimpleTypeNode;
import org.opentravel.schemas.node.typeProviders.facetOwners.CoreObjectNode;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.utils.BaseProjectTest;
import org.opentravel.schemas.utils.ComponentNodeBuilder;
import org.opentravel.schemas.utils.LibraryNodeBuilder;
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Pawel Jedruch / Dave Hollander
 * 
 */
public class DefaultProjectControllerTest extends BaseProjectTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultProjectControllerTest.class);

	ProjectNode pn1;
	ProjectNode pn2;

	@Test
	public void closeShouldRemoveProject() throws LibrarySaveException {
		ProjectNode toCloseProject = createProject("ToClose", rc.getLocalRepository(), "close");
		pc.close(toCloseProject);
		Assert.assertFalse(Node.getModelNode().getChildren().contains(toCloseProject));
	}

	@Test
	public void closeAllShouldRemoveProject() throws LibrarySaveException {
		ProjectNode toCloseProject = createProject("ToClose", rc.getLocalRepository(), "close");
		pc.closeAll();
		assertFalse(Node.getModelNode().getChildren().contains(toCloseProject));
	}

	@Test
	public void closeAllShouldRemoveDefaultProject() throws LibrarySaveException {
		pc.closeAll();
		Assert.assertFalse(Node.getModelNode().getChildren().contains(testProject));
	}

	@Test
	public void closeShouldReloadDefaultProject() throws LibrarySaveException {
		// Given - one library in the default project
		LibraryNode lib = LibraryNodeBuilder
				.create("TestLib", pc.getDefaultProject().getNamespace(), "a", Version.emptyVersion)
				.build(pc.getDefaultProject(), pc);
		ProjectNode defaultProjectBeforeClose = pc.getDefaultProject();
		Assert.assertEquals(1, defaultProjectBeforeClose.getLibraries().size());

		// When - project is closed
		pc.close(pc.getDefaultProject());

		ProjectNode defaultProjectAfterClose = pc.getDefaultProject();
		assertTrue("Default project must be same project as before close.",
				defaultProjectBeforeClose == defaultProjectAfterClose);
		// 12/6/2016 - dmh - changed default project close behavior
		// Assert.assertNotSame(defaultProjectBeforeClose, defaultProjectAfterClose);
		// Assert.assertEquals(1, defaultProjectAfterClose.getLibraries().size());
		// LibraryNode libAfterClose = defaultProjectAfterClose.getLibraries().get(0);
		// Assert.assertNotSame(lib, libAfterClose);
		// Assert.assertEquals(lib.getName(), libAfterClose.getName());
	}

	@Test
	public void crossLibraryLinks() throws LibrarySaveException {
		// Given - library LocalOne with one member
		LibraryNode local1 = LibraryNodeBuilder
				.create("LocalOne", testProject.getNamespace() + "/Test/One", "o1", new Version(1, 0, 0))
				.build(testProject, pc);
		SimpleTypeNode so = ComponentNodeBuilder.createSimpleObject("SO")
				.assignType(NodeFinders.findNodeByName("string", ModelNode.XSD_NAMESPACE)).get();
		local1.addMember(so);

		// Given - second library that uses type from first library // Given - expected imports are 1st lib and common
		LibraryNode local2 = LibraryNodeBuilder
				.create("LocalTwo", testProject.getNamespace() + "/Test/Two", "o2", new Version(1, 0, 0))
				.build(testProject, pc);
		// PropertyNode property = PropertyNodeBuilder.create(PropertyNodeType.ELEMENT).setName("Reference").assign(so)
		// .build();
		CoreObjectNode co = new CoreObjectNode(new TLCoreObject());
		co.setName("CO");
		TypedPropertyNode property = new ElementNode(co.getFacet_Summary(), "e1", so);
		// CoreObjectNode co = ComponentNodeBuilder.createCoreObject("CO").addToSummaryFacet(property).get();
		local2.addMember(co);
		assertTrue("Property must be assigned so as type.", property.getAssignedType() == so);

		// Given - set of namespaces from Local 2's tlLibrary
		Set<String> expectedImports = new HashSet<>();
		for (TLNamespaceImport imported : local2.getTLLibrary().getNamespaceImports()) {
			expectedImports.add(imported.getNamespace());
		}

		// When - saved and closed
		pc.save(testProject); // the project and libraries
		List<LibraryNavNode> libsToRemove = new ArrayList<>();
		libsToRemove.add((LibraryNavNode) local1.getParent());
		libsToRemove.add((LibraryNavNode) local2.getParent());
		pc.remove(libsToRemove); // must remove as list to avoid re-saving
		pc.close(pc.getDefaultProject());
		assertTrue("Default project must be empty.", pc.getDefaultProject().getLibraries().isEmpty());
		assertTrue("Test project must be empty.", testProject.getLibraries().isEmpty());

		// When - opening Local 2 from file
		File local2File = URLUtils.toFile(local2.getTLLibrary().getLibraryUrl());
		testProject.add(Collections.singletonList(local2File));
		LibraryNode reopenedLibrary = testProject.getLibraries().get(0);
		TLLibrary tlLib = reopenedLibrary.getTLLibrary();

		Set<String> actaulsImports = new HashSet<>();
		for (TLNamespaceImport imported : tlLib.getNamespaceImports())
			actaulsImports.add(imported.getNamespace());

		for (String e : expectedImports)
			if (!actaulsImports.contains(e))
				fail("Missing imported namespace: " + e);
	}

	@Test
	public void addTests() {
		DefaultProjectController pc = (DefaultProjectController) mc.getProjectController();

		// Used by repository controller when creating new minor versions
		// add(ln, tlAbsLib)

		// Used by library controller to create new libraries
		// pc.add(pn, tlAbsLib)

		// Used to add libraries from repository to a project
		// ProjectNode project = null;
		// RepositoryItem ri = null;
		// pc.add(project, ri);

		// addLibrariesToTLProject(pn, libList)
	}

	@Test
	public void closeTests() {
		// closeAll()
		// close(pn)
	}

	@Test
	public void createTests() {
		// public ProjectNode create(File file, String ID, String name, String description) {

	}

	@Test
	public void getTests() {
		// getAll()
		// getBuildInProject()
		// getDefaultProject()
		// getDefaultUnmanagedNS()
		// getMemento()
		// getNamespace()
		// getOpenGovernedNamespaces()
		// getSuggestedNamespaces()

	}

	@Test
	public void loadTests() {
		// loadProject(tlProject)
		// loadProject_BuiltIn()
		// loadProjects(XMLMemento, monitor)

	}

	@Test
	public void newTests() {
		// newProject()
		// newProject(string, string, string)

	}

	@Test
	public void openTests() {
		// open() - runs dialog then open(fn, monitor)

		// pn = open(fn, monitor)
		// When - loadProject uses pc.open(ProjectFileName, monitor)
		ProjectNode pn1 = new LoadFiles().loadProject(pc);
		ProjectNode pn2 = new LoadFiles().loadProject2(pc);

		assertTrue("Project 1 must be opened.", pn1 != null);
		assertTrue("Project 2 must be open.", pn2 != null);

		LOGGER.debug("Project " + pn1 + " namespace is: " + pn1.getNamespace());
		for (Node n : pn1.getNavChildren(true)) {
			assertTrue("Must be library nav node", n instanceof LibraryNavNode);
			LibraryNavNode lnn = (LibraryNavNode) n;
			LOGGER.debug("LibraryNN " + lnn + " namespace is:  " + lnn.getNamespace());
			LOGGER.debug("LibraryNN " + lnn + " is editable? " + lnn.isEditable());
			LOGGER.debug("LibraryNN " + lnn + " edit status? " + lnn.getEditStatus());
			if (lnn.getThisLib() instanceof LibraryNode) {
				LibraryNode ln = (LibraryNode) lnn.getThisLib();
				ln.setNamespace(pn1.getNamespace());
				ln.updateLibraryStatus();
				LOGGER.debug("Library " + ln + " namespace is:  " + ln.getNamespace());
				LOGGER.debug("Library " + ln + " is editable? " + ln.isEditable());
			}
		}
		for (LibraryNode li : pn1.getLibraries()) {
			LOGGER.debug("Library " + li + " namespace is:  " + li.getNamespace());
			LOGGER.debug("Library " + li + " is editable? " + li.isEditable());
			LOGGER.debug("Library " + li + " edit status? " + li.getEditStatus());
			li.setNamespace(pn1.getNamespace());
			li.updateLibraryStatus();
			LOGGER.debug("Library " + li + " namespace is:  " + li.getNamespace());
			LOGGER.debug("Library " + li + " is editable? " + li.isEditable());
		}
		LOGGER.debug("Project " + pn1 + " namespace is: " + pn1.getNamespace());

		// openLibrary(Project, FileList, findings)
		// openProject(string)
		// openProject(string, findings)

	}

	@Test
	public void removeTests() {
		// remove(LibNavNodeList)
		// Simple remove - no files in multiple projects
		openProjectFiles();
		List<LibraryNavNode> lnn1s = new ArrayList<>();
		List<LibraryNavNode> lnn2s = new ArrayList<>();

		pc.remove(lnn1s);
		pc.remove(lnn2s);
	}

	@Test
	public void saveTests() {
		// save()
		// save(PN_List)
		// save(pn)
		// saveAll()
		// saveState()

	}

	private void openProjectFiles() {
		pn1 = new LoadFiles().loadProject(pc);
		pn2 = new LoadFiles().loadProject2(pc);

		assertTrue("Project 1 must be opened.", pn1 != null);
		assertTrue("Project 2 must be open.", pn2 != null);

	}
}
