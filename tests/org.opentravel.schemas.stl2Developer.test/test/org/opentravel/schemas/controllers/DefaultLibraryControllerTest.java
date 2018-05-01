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

import java.util.List;

import org.junit.Test;
import org.opentravel.schemacompiler.saver.LibrarySaveException;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.node.interfaces.LibraryInterface;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNavNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.testUtils.MockLibrary;
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
		DefaultLibraryController lc = (DefaultLibraryController) mc.getLibraryController();
		ProjectController pc = mc.getProjectController();
		ProjectNode builtins = pc.getBuiltInProject();
		LibraryModelManager mgr = Node.getModelNode().getLibraryManager();

		// When - library created
		LibraryNavNode lib = lc.createLibrary();

		// Then - no workbench that createLibrary must have so nothing happens.
		assertTrue("Default Project must be empty.", pc.getDefaultProject().getChildren().isEmpty());

		// Then - the built in libraries must be correct.
		final int builtInLibCount = 2;
		assertTrue("Built in project must have " + builtInLibCount + " libraries.",
				builtins.getChildren().size() == builtInLibCount);
		for (Node n : builtins.getChildren())
			assertTrue("Built in must be a library nav node.", n instanceof LibraryNavNode);
		assertTrue("Library Manager must have " + builtInLibCount + "  libraries.",
				mgr.libraries.size() == builtInLibCount);
	}

	// // FIXME - something here causes after each test to fail and changes test resources
	// // @Test
	// public void createNewLibraryFromPrototype_Test() {
	// // Given - library as created by NewLibraryWizard
	// DefaultLibraryController lc = (DefaultLibraryController) mc.getLibraryController();
	// LibraryModelManager libMrg = LibraryNavNode.getModelNode().getLibraryManager();
	// int startingLibCount = libMrg.libraries.size();
	// String libName = "NewlyCreated1";
	// String path = pc.getDefaultProject().getTLProject().getProjectFile().getParentFile() + "/" + libName + ".otm";
	// String prefix = "nc";
	// String ns = pc.getDefaultProject().getNamespace();
	// String extension = "nc";
	// String version = "0.0";
	//
	// LibraryNode protoLib = new LibraryNode(pc.getDefaultProject());
	// protoLib.setPath(path);
	// protoLib.setName(libName);
	// protoLib.getNsHandler().createValidNamespace(ns, extension, version);
	// protoLib.setNSPrefix(prefix);
	//
	// // When - creating library from prototype
	// ProjectNode pn = pc.getDefaultProject();
	// LibraryNavNode lnn = lc.createNewLibraryFromPrototype(protoLib, pn);
	//
	// // Then - the library is created correctly
	// assertTrue("New library contains namespace base.", lnn.getNamespace().contains(ns));
	// assertTrue("New library has correct name.", lnn.getName().startsWith(libName)); // ignore test decoration
	// // Then - library must be linked properly
	// assertTrue("This must be a library Nav Node.", lnn instanceof LibraryNavNode);
	// assertTrue("LNN child must be a library.", lnn.getLibrary() instanceof LibraryNode);
	// assertTrue("Parent must be default project.", lnn.getProject() == pn);
	// // Then - default project must be correct
	// assertTrue("Project must contain parent LibraryNavNode.", pn.getChildren().contains(lnn));
	// assertTrue("Project must only have one child.", pn.getChildren().size() == 1);
	// // Then - library must be in library model manager correctly
	// assertTrue("Library must be in library model manager.", libMrg.getUserLibraries().contains(lnn.getLibrary()));
	// assertTrue("Library manager must have one more library.", libMrg.libraries.size() == startingLibCount + 1);
	// }

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
	public void closeUnManaged_Test() {
		// Given - 3 unmanaged libraries
		LibraryController lc = mc.getLibraryController();
		LibraryModelManager libMgr = LibraryNavNode.getModelNode().getLibraryManager();
		MockLibrary ml = new MockLibrary();

		assertTrue("Startup - model library manager has 2 libraries.", libMgr.getLibraries().size() == 2);

		ml.createNewLibrary(testProject.getNamespace() + "/lib1", "lib1", testProject);
		ml.createNewLibrary(testProject.getNamespace() + "/lib2", "lib2", testProject);
		LibraryNode ulib3 = ml.createNewLibrary(testProject.getNamespace() + "/lib3", "lib3", testProject);
		int libCount = 3;
		assertTrue(libCount + " libraries must be in test project.", testProject.getChildren().size() == libCount);
		assertTrue("Project contains library parent.", testProject.getChildren().contains(ulib3.getParent()));
		assertTrue("Library is managed by library model manager.", LibraryNavNode.getModelNode().getUserLibraries()
				.contains(ulib3));

		// When - one library closed
		ulib3.closeLibraryInterface(); // parent driven close
		assertTrue("Project does not contain library parent.", !testProject.getChildren().contains(ulib3.getParent()));
		assertTrue("Library model manager does not manage library.", !libMgr.getUserLibraries().contains(ulib3));

		// When - closed
		testProject.close();
		// Then
		assertTrue("No libraries may be in default project.", testProject.getChildren().size() == 0);
		assertTrue("No user libraries managed in library model manager.", libMgr.getUserLibraries().size() == 0);
	}

	@SuppressWarnings("unused")
	@Test
	public void createAndOpen_Tests() {
		// Given
		LibraryController lc = mc.getLibraryController();
		MockLibrary ml = new MockLibrary();
		LoadFiles lf = new LoadFiles();

		// When - libraries are created using project controller
		LibraryNode lib1 = createLib("lib1", "/lib1", "l1", testProject);
		LibraryNode lib2 = createLib("lib2", "/lib2", "l2", testProject);
		LibraryNode lib3 = createLib("lib3", "/lib3", "l3", testProject);
		assertTrue("3 libraries must be in default project.", testProject.getChildren().size() == 3);

		// When Projects created using Project Controller
		ProjectNode projectA = createProject("A", rc.getLocalRepository(), "a");
		ProjectNode projectB = createProject("B", rc.getLocalRepository(), "b");
		lf.loadFile2(projectA);
		lf.loadFile2(projectB);
		assertTrue("Projects have unique children.", projectA.getChildren().get(0) != projectB.getChildren().get(0));
		assertTrue("Both projects have the same library.", projectA.getChildren().get(0).getLibrary() == projectB
				.getChildren().get(0).getLibrary());

		// When - Projects are opened using Project Controller
		ProjectNode projectC = lf.loadProject(pc);
		ProjectNode projectD = lf.loadProject2(pc);
		// Then
		assertTrue("Project must have children.", !projectC.getChildren().isEmpty());
		assertTrue("Project must have children.", !projectD.getChildren().isEmpty());

		// Then - common library is shared
		// OTA_SimpleTypes is no long loaded
		// LibraryNavNode libC = findLibrary(projectC, "OTA_SimpleTypes");
		// LibraryNavNode libD = findLibrary(projectD, "OTA_SimpleTypes");
		// assertTrue("Project must have OTA_SimpleTypes", libC != null);
		// assertTrue("Project must have OTA_SimpleTypes", libD != null);
		// assertTrue("Projects must have different library nav nodes.", libC != libD);
		// assertTrue("Projects must share the library.", libC.getLibrary() == libD.getLibrary());

		// Then - all libraries must be managed
		List<LibraryInterface> managedLibs = Node.getModelNode().getLibraryManager().getLibraries();
		for (Node l : projectC.getChildren())
			assertTrue("Must be managed library.", managedLibs.contains(((LibraryNavNode) l).getThisLib()));
		for (Node l : projectD.getChildren())
			assertTrue("Must be managed library.", managedLibs.contains(((LibraryNavNode) l).getThisLib()));

		// Make sure library constructor gets all object types
		LibraryModelManager libMgr = Node.getModelNode().getLibraryManager();
		mc.getProjectController().closeAll();
		assertTrue("Startup - model library manager has 2 libraries.", libMgr.libraries.size() == 2);
	}

	private LibraryNavNode findLibrary(ProjectNode pn, String prefix) {
		for (Node lnn : pn.getChildren())
			if (lnn.getName().startsWith(prefix))
				return (LibraryNavNode) lnn;
		return null;
	}

	@Test
	public void removeManagedInMultipleProjects_Test() {
		// Given - one library in each of 2 projects A and B
		LoadFiles lf = new LoadFiles();
		ProjectNode projectA = createProject("A", rc.getLocalRepository(), "a");
		ProjectNode projectB = createProject("B", rc.getLocalRepository(), "b");
		LibraryNode lib1 = lf.loadFile6(projectA);
		LibraryNavNode lnn1 = (LibraryNavNode) lib1.getParent();
		assertTrue("Project A has LNN1 as child.", projectA.getChildren().contains(lnn1));
		LibraryNode lib2 = lf.loadFile6(projectB); // projectNode.add(Files)
		LibraryNavNode lnn2 = (LibraryNavNode) lib2.getParent();
		assertTrue("Must have same project item.", lib1.getProjectItem() == lib2.getProjectItem());
		assertTrue("Library Model manager must have made them the same library.", lib1 == lib2);
		assertTrue("Library Nav Nodes must be different.", lnn1 != lnn2);
		assertTrue("Project A has LNN1 as child.", projectA.getChildren().contains(lnn1));

		// When - Make a chain out of the library
		LibraryChainNode lcn1 = new LibraryChainNode(lib1);
		int chainCount = lcn1.getDescendants_LibraryMembersAsNodes().size();
		// assertTrue("Project A has LNN1 as child.", projectA.getChildren().contains(lnn1));
		// Then
		assertTrue("Chain must have members.", chainCount > 0);
		assertTrue("Project A must have one library. ", projectA.getLibraries().size() == 1);
		assertTrue("Project A must have one child.", projectA.getChildren().size() == 1);
		assertTrue("Project A child must be Library Nav Node.", projectA.getChildren().get(0) instanceof LibraryNavNode);
		LibraryInterface li = ((LibraryNavNode) projectA.getChildren().get(0)).getThisLib();
		assertTrue("Project A library must be a chain", li instanceof LibraryChainNode);
		assertTrue("Library Nav Node must now link to chain.", lnn1.getThisLib() == lcn1);
		// assertTrue lnn2 = lcn1
		assertTrue("Project B must have one child.", projectB.getChildren().size() == 1);
		LibraryInterface liB = ((LibraryNavNode) projectB.getChildren().get(0)).getThisLib();
		assertTrue("Project B library must be a chain", liB instanceof LibraryChainNode);

		lib1.setEditable(true);
		lib2.setEditable(true);

		// When
		projectA.close(lcn1);
		// Then
		assertTrue("Project A must be empty.", projectA.getLibraries().isEmpty());
		assertTrue("Project A must be empty.", projectA.getChildren().isEmpty());
		assertTrue("lib2 must NOT be empty.", !lib2.getDescendants_LibraryMembers().isEmpty());

		// When
		projectB.close(lcn1);
		// Then
		List<LibraryMemberInterface> lms2 = lib2.getDescendants_LibraryMembers();
		assertTrue("Chain must be empty.", lcn1.isEmpty());
		assertTrue("Project B must be empty.", projectB.getLibraries().isEmpty());
		assertTrue("lib2 must be empty.", lib2.getDescendants_LibraryMembers().isEmpty());
		assertTrue("Lib2 must report empty.", lib2.isEmpty());
	}

	@Test
	public void removeUnManagedInMultipleProjects_Test() {
		LoadFiles lf = new LoadFiles();
		// Given 2 projects with the same library
		ProjectNode projectA = createProject("A", rc.getLocalRepository(), "a");
		ProjectNode projectB = createProject("B", rc.getLocalRepository(), "b");
		LibraryNode lib1 = lf.loadFile6(projectA);
		LibraryNode lib2 = lf.loadFile6(projectB); // projectNode.add(Files)
		// Fix - ProjectNode.getLibraries()
		assertTrue("Must have library. ", !projectA.getLibraries().isEmpty());

		lib1.setEditable(true);
		lib2.setEditable(true);
		assertTrue("Must have same project item.", lib1.getProjectItem() == lib2.getProjectItem());

		List<Node> Amembers = lib1.getDescendants_LibraryMembersAsNodes();
		List<Node> Bmembers = lib2.getDescendants_LibraryMembersAsNodes();
		assertTrue("Must have library members.", !lib1.isEmpty());

		final String simpleTypeName = "SampleEnum_Open";
		Node AsimpleType = null;
		for (Node n : Amembers)
			if (n.getName().equals(simpleTypeName))
				AsimpleType = n;
		assertTrue("Must find simple type.", AsimpleType != null);
		Node BsimpleType = null;
		for (Node n : Bmembers)
			if (n.getName().equals(simpleTypeName))
				BsimpleType = n;
		assertTrue("TestLib 6 Must have simple type " + simpleTypeName, BsimpleType != null);
		assertTrue("Must be same type.", AsimpleType == BsimpleType);

		// When
		AsimpleType.delete();
		// Then
		assertTrue("Must be deleted.", BsimpleType.isDeleted());

		// When
		projectA.close(lib1);
		// Then
		assertTrue("lib2 must NOT be empty.", !lib2.getDescendants_LibraryMembers().isEmpty());

		// When
		projectB.close(lib2);
		// Then
		assertTrue("lib2 must be empty.", lib2.getDescendants_LibraryMembers().isEmpty());
		assertTrue("Lib2 must report empty.", lib2.isEmpty());

		// // Open Project
		// projectA = lf.loadProject(mc.getProjectController());
		// assert projectA != null;
		// assertTrue("Project A is not empty", !projectA.getChildren().isEmpty());

		// OK - Make sure everything in library is closed - services, resources, etc
		// OK - try deleting libraries - must not delete children of other project
		// fix other open/load paths
		// fix other close paths
		// fix nav children
		// Do again with library chains
		// test, test and test some more.

		// Given - 1 library in two projects
		// NodeTesters nt = new NodeTesters();
		// LibraryController lc = mc.getLibraryController();
		// MockLibrary ml = new MockLibrary();
		// ProjectNode project2 = createProject("ToClose", rc.getLocalRepository(), "close");
		// LibraryNode lib1 = lf.loadFile7(defaultProject);
		// LibraryNode lib2 = lf.loadFile7(project2);
		// assertTrue("Default project must have one library.", defaultProject.getLibraries().size() == 1);
		// assertTrue("Project2 must have one library.", project2.getLibraries().size() == 1);
		// assertTrue("Libraries in the projects must be different libraries.", lib1 != lib2);
		// assertTrue("Library 1 must be valid.", lib1.isValid());
		// assertTrue("Library 2 must be valid.", lib2.isValid());
		// lib1.visitAllNodes(nt.new TestNode());
		//
		// // Given -
		// // When - library is removed from 2nd project but not the first
		// lc.remove(project2.getLibraries());
		// assertTrue("Default project must have one library.", defaultProject.getLibraries().size() == 1);
		// assertTrue("Project2 must have no library.", project2.getLibraries().size() == 0);
		// ValidationFindings findings = lib1.validate();
		// for (ValidationFinding finding : findings.getAllFindingsAsList())
		// System.out.println("Finding: " + finding.getFormattedMessage(FindingMessageFormat.MESSAGE_ONLY_FORMAT));
		//
		// assertTrue("Remaining Library must be valid.", lib1.isValid());
		// lib1.visitAllNodes(nt.new TestNode());
		//
		// // TODO - test closing one version in a chain
	}

	@SuppressWarnings("unused")
	@Test
	public void removeManaged_Test() {
		// Given - 3 managed libraries and 3 unmanaged
		LibraryController lc = mc.getLibraryController();
		MockLibrary ml = new MockLibrary();
		assertTrue("Startup - model library manager has 2 libraries.", LibraryNavNode.getModelNode()
				.getLibraryManager().libraries.size() == 2);

		ml.createNewLibrary(testProject.getNamespace() + "/lib1", "lib1", testProject);
		ml.createNewLibrary(testProject.getNamespace() + "/lib2", "lib2", testProject);
		LibraryNode ulib3 = ml.createNewLibrary(testProject.getNamespace() + "/lib3", "lib3", testProject);
		int libCount = 3;
		assertTrue(libCount + " libraries must be in default project.", testProject.getChildren().size() == libCount);
		assertTrue("Library is managed by node library manager.", LibraryNavNode.getModelNode().getUserLibraries()
				.contains(ulib3));

		LibraryChainNode lcn1 = ml.createNewManagedLibrary("Lib1", testProject);
		LibraryChainNode lcn2 = ml.createNewManagedLibrary("Lib2", testProject);
		LibraryChainNode lcn3 = ml.createNewManagedLibrary("Lib3", testProject);
		LibraryNode lcn3Lib = lcn3.getHead();
		libCount += 3;
		List<LibraryInterface> libs = LibraryNavNode.getModelNode().getLibraryManager().getLibraries();
		assertTrue("Library is managed.", libs.contains(lcn3));
		assertTrue(libCount + " libraries must be in default project.", testProject.getChildren().size() == libCount);

		// When - 1 is removed
		testProject.close(lcn3);
		libCount--;

		// Then - that library is deleted and removed
		assertTrue("Chain is deleted.", lcn3.isDeleted());
		assertTrue("Library is deleted.", lcn3Lib.isDeleted());
		assertTrue("Library is not managed.", !LibraryNavNode.getModelNode().getUserLibraries().contains(lcn3));
		assertTrue(libCount + " libraries must be in default project.", testProject.getChildren().size() == libCount);
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
			local1 = LibraryNodeBuilder.create(name, testProject.getNamespace() + nsExtension, prefix,
					new Version(1, 0, 0)).build(testProject, pc);
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
