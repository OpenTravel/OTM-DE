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

import static org.junit.Assert.assertTrue;

import java.util.Collections;

import org.junit.Test;
import org.opentravel.schemacompiler.saver.LibrarySaveException;
import org.opentravel.schemacompiler.validate.FindingMessageFormat;
import org.opentravel.schemacompiler.validate.ValidationFinding;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemas.node.LibraryChainNode;
import org.opentravel.schemas.node.LibraryNode;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.testUtils.MockLibrary;
import org.opentravel.schemas.testUtils.NodeTesters;
import org.opentravel.schemas.utils.BaseProjectTest;
import org.opentravel.schemas.utils.LibraryNodeBuilder;
import org.osgi.framework.Version;

/**
 * Cloned from DefaultProjectControllerTest. All public methods are tested but many of the tests are empty.
 * 
 * @author Dave Hollander
 * 
 */
public class DefaultLibraryControllerTest extends BaseProjectTest {

	private static final int Collection = 0;

	// @Test
	// public void changeNamespace_Test() {
	// // Given - nothing
	// LibraryController lc = mc.getLibraryController();
	// }
	//
	// @Test
	// public void changeNamespaceExtension_Test() {
	// // Given - nothing
	// LibraryController lc = mc.getLibraryController();
	// }
	//
	// @Test
	// public void convertXSD2OTM_Test() {
	// // Given - nothing
	// LibraryController lc = mc.getLibraryController();
	// // TWO methods
	// }

	@Test
	public void createlibrary_Test() {
		// Given - nothing
		LibraryController lc = mc.getLibraryController();

		// When - library created
		LibraryNode lib = lc.createLibrary();

		// Then - nothing happens because there is no workbench.
		assertTrue("Default Project must be empty.", pc.getDefaultProject().getChildren().isEmpty());
	}

	@Test
	public void createNewLibraryFromPrototype_Test() {
		// Given - library as created by NewLibraryWizard
		String libName = "NewlyCreated1";
		String path = pc.getDefaultProject().getProject().getProjectFile().getParentFile() + "/" + libName + ".otm";
		String prefix = "nc";
		String ns = pc.getDefaultProject().getNamespace();
		String extension = "nc";
		String version = "0.0";

		LibraryNode lib = new LibraryNode(pc.getDefaultProject());
		lib.setPath(path);
		lib.setName(libName);
		lib.getNsHandler().createValidNamespace(ns, extension, version);
		lib.setNSPrefix(prefix);

		// When - creating library from prototype
		DefaultLibraryController lc = (DefaultLibraryController) mc.getLibraryController();
		LibraryNode lib2 = lc.createNewLibraryFromPrototype(lib);

		// Then - the library is created correctly
		assertTrue("New library is in default project.", pc.getDefaultProject().getChildren().contains(lib2));
		String ns2 = lib2.getNamespace();
		assertTrue("New library contains namespace base.", lib2.getNamespace().contains(ns));
		assertTrue("New library has correct name.", lib2.getName().equals(libName));

	}

	// @Test
	// public void getLibrariesWithNamespace_Test() {
	// // Given - nothing
	// LibraryController lc = mc.getLibraryController();
	// }
	//
	// @Test
	// public void getLibraryStatus_Test() {
	// // Given - nothing
	// LibraryController lc = mc.getLibraryController();
	// }
	//
	// @Test
	// public void openLibrary_Test() {
	// // Given - nothing
	// LibraryController lc = mc.getLibraryController();
	// // With any node
	// // With project node
	// }

	@Test
	public void remove_Test() {
		// Given - 3 libraries
		LibraryController lc = mc.getLibraryController();
		LibraryNode lib1 = createLib("lib1", "/lib1", "l1");
		LibraryNode lib2 = createLib("lib2", "/lib2", "l2");
		LibraryNode lib3 = createLib("lib3", "/lib3", "l3");
		assertTrue("3 libraries must be in default project.", defaultProject.getChildren().size() == 3);

		// When - 1 is removed
		lc.remove(Collections.singletonList(lib3));
		// Then - 2 are left
		assertTrue("Library is deleted.", lib3.isDeleted());
		assertTrue("2 libraries must be in default project.", defaultProject.getChildren().size() == 2);
	}

	@Test
	public void removeManagedInMultipleProjects_Test() {
		// Given - 1 library in two projects
		NodeTesters nt = new NodeTesters();
		LibraryController lc = mc.getLibraryController();
		MockLibrary ml = new MockLibrary();
		LoadFiles lf = new LoadFiles();

		ProjectNode project2 = createProject("ToClose", rc.getLocalRepository(), "close");
		LibraryNode lib1 = lf.loadFile7(defaultProject);
		LibraryNode lib2 = lf.loadFile7(project2);
		assertTrue("Default project must have one library.", defaultProject.getLibraries().size() == 1);
		assertTrue("Project2 must have one library.", project2.getLibraries().size() == 1);
		assertTrue("Libraries in the projects must be different libraries.", lib1 != lib2);
		assertTrue("Library 1 must be valid.", lib1.isValid());
		assertTrue("Library 2 must be valid.", lib2.isValid());
		lib1.visitAllNodes(nt.new TestNode());

		// Given -
		// When - library is removed from 2nd project but not the first
		lc.remove(project2.getLibraries());
		assertTrue("Default project must have one library.", defaultProject.getLibraries().size() == 1);
		assertTrue("Project2 must have no library.", project2.getLibraries().size() == 0);
		ValidationFindings findings = lib1.validate();
		for (ValidationFinding finding : findings.getAllFindingsAsList())
			System.out.println("Finding: " + finding.getFormattedMessage(FindingMessageFormat.MESSAGE_ONLY_FORMAT));

		assertTrue("Remaining Library must be valid.", lib1.isValid());
		lib1.visitAllNodes(nt.new TestNode());

		// TODO - test closing one version in a chain
	}

	@Test
	public void removeManaged_Test() {
		// Given - 3 managed libraries and 3 unmanaged
		LibraryController lc = mc.getLibraryController();
		MockLibrary ml = new MockLibrary();

		LibraryChainNode lcn1 = ml.createNewManagedLibrary("Lib1", defaultProject);
		LibraryChainNode lcn2 = ml.createNewManagedLibrary("Lib2", defaultProject);
		LibraryChainNode lcn3 = ml.createNewManagedLibrary("Lib3", defaultProject);
		LibraryNode lcn3Lib = lcn3.getHead();

		LibraryNode ulib1 = createLib("lib1", "/lib1", "l1");
		LibraryNode ulib2 = createLib("lib2", "/lib2", "l2");
		LibraryNode ulib3 = createLib("lib3", "/lib3", "l3");
		int libCount = 6;
		assertTrue(libCount + " libraries must be in default project.", defaultProject.getChildren().size() == libCount);

		// When - 1 is removed
		lc.remove(Collections.singletonList(lcn3));
		libCount--;

		// Then - 2 are left
		assertTrue("Library is deleted.", lcn3Lib.isDeleted());
		assertTrue(libCount + " libraries must be in default project.", defaultProject.getChildren().size() == libCount);
		assertTrue("Chain is deleted.", lcn3.isDeleted());
	}

	// @Test
	// public void saveAllLibraries_Test() {
	// // Given - nothing
	// LibraryController lc = mc.getLibraryController();
	// }
	//
	// @Test
	// public void saveLibraries_Test() {
	// // Given - nothing
	// LibraryController lc = mc.getLibraryController();
	// }
	//
	// @Test
	// public void saveLibrary_Test() {
	// // Given - nothing
	// LibraryController lc = mc.getLibraryController();
	// }
	//
	// @Test
	// public void updateLibraryStatus_Test() {
	// // Given - nothing
	// LibraryController lc = mc.getLibraryController();
	// }

	// @Test
	// public void validateLibrary_Test() {
	// // Given - nothing
	// LibraryController lc = mc.getLibraryController();
	// }

	private LibraryNode createLib(String name, String nsExtension, String prefix) {
		LibraryNode local1 = null;
		try {
			local1 = LibraryNodeBuilder.create(name, defaultProject.getNamespace() + nsExtension, prefix,
					new Version(1, 0, 0)).build(defaultProject, pc);
		} catch (LibrarySaveException e) {
			assertTrue(e.getLocalizedMessage(), 1 == 1);
		}
		return local1;
	}

	// @Test
	// public void closeShouldRemoveProject() throws LibrarySaveException {
	// ProjectNode toCloseProject = createProject("ToClose", rc.getLocalRepository(), "close");
	// pc.close(toCloseProject);
	// Assert.assertFalse(Node.getModelNode().getChildren().contains(toCloseProject));
	// }
	//
	// @Test
	// public void closeAllShouldRemoveProject() throws LibrarySaveException {
	// ProjectNode toCloseProject = createProject("ToClose", rc.getLocalRepository(), "close");
	// pc.closeAll();
	// Assert.assertFalse(Node.getModelNode().getChildren().contains(toCloseProject));
	// }
	//
	// @Test
	// public void closeAllShouldRemoveDefaultProject() throws LibrarySaveException {
	// pc.closeAll();
	// Assert.assertFalse(Node.getModelNode().getChildren().contains(defaultProject));
	// }
	//
	// @Test
	// public void closeShouldReloadDefaultProject() throws LibrarySaveException {
	// LibraryNode lib = LibraryNodeBuilder.create("TestLib", pc.getDefaultProject().getNamespace(), "a",
	// Version.emptyVersion).build(pc.getDefaultProject(), pc);
	// ProjectNode defaultProjectBeforeClose = pc.getDefaultProject();
	//
	// Assert.assertEquals(1, defaultProjectBeforeClose.getLibraries().size());
	// pc.close(pc.getDefaultProject());
	//
	// ProjectNode defaultProjectAfterClose = pc.getDefaultProject();
	// Assert.assertNotSame(defaultProjectBeforeClose, defaultProjectAfterClose);
	// Assert.assertEquals(1, defaultProjectAfterClose.getLibraries().size());
	// LibraryNode libAfterClose = defaultProjectAfterClose.getLibraries().get(0);
	// Assert.assertNotSame(lib, libAfterClose);
	// Assert.assertEquals(lib.getName(), libAfterClose.getName());
	// }
	//
	// @Test
	// public void crossLibraryLinks() throws LibrarySaveException {
	// // Given - library LocalOne with one member
	// LibraryNode local1 = LibraryNodeBuilder.create("LocalOne", defaultProject.getNamespace() + "/Test/One", "o1",
	// new Version(1, 0, 0)).build(defaultProject, pc);
	// SimpleTypeNode so = ComponentNodeBuilder.createSimpleObject("SO")
	// .assignType(NodeFinders.findNodeByName("string", ModelNode.XSD_NAMESPACE)).get();
	// local1.addMember(so);
	//
	// // Given - second library that uses type from first library // Given - expected imports are 1st lib and common
	// LibraryNode local2 = LibraryNodeBuilder.create("LocalTwo", defaultProject.getNamespace() + "/Test/Two", "o2",
	// new Version(1, 0, 0)).build(defaultProject, pc);
	// PropertyNode property = PropertyNodeBuilder.create(PropertyNodeType.ELEMENT).setName("Reference").assign(so)
	// .build();
	// CoreObjectNode co = ComponentNodeBuilder.createCoreObject("CO").addToSummaryFacet(property).get();
	// local2.addMember(co);
	//
	// // Given - set of namespaces from Local 2's tlLibrary
	// Set<String> expectedImports = new HashSet<String>();
	// for (TLNamespaceImport imported : local2.getTLLibrary().getNamespaceImports()) {
	// expectedImports.add(imported.getNamespace());
	// }
	//
	// // When - saved and closed
	// mc.getLibraryController().saveAllLibraries(false);
	// mc.getLibraryController().remove(defaultProject.getLibraries());
	// // FIXME
	// // assertTrue("Default project must be empty.", defaultProject.getLibraries().isEmpty());
	//
	// // When - opening Local 2 from file
	// File local2File = URLUtils.toFile(local2.getTLLibrary().getLibraryUrl());
	// defaultProject.add(Collections.singletonList(local2File));
	// LibraryNode reopenedLibrary = defaultProject.getLibraries().get(0);
	// TLLibrary tlLib = reopenedLibrary.getTLLibrary();
	//
	// Set<String> actaulsImports = new HashSet<String>();
	// for (TLNamespaceImport imported : tlLib.getNamespaceImports()) {
	// actaulsImports.add(imported.getNamespace());
	// }
	//
	// for (String e : expectedImports) {
	// if (!actaulsImports.contains(e)) {
	// fail("Missing imported namespace: " + e);
	// }
	// }
	// }

}
