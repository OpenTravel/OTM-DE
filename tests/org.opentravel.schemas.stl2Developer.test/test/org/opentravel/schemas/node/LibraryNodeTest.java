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
package org.opentravel.schemas.node;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.saver.LibrarySaveException;
import org.opentravel.schemas.controllers.LibraryModelManager;
import org.opentravel.schemas.node.interfaces.InheritedInterface;
import org.opentravel.schemas.node.interfaces.LibraryInterface;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNavNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.testUtils.MockLibrary;
import org.opentravel.schemas.utils.BaseProjectTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dave Hollander
 * 
 */
public class LibraryNodeTest extends BaseProjectTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(LibraryNodeTest.class);

	MockLibrary ml = new MockLibrary();

	public void check(LibraryNode ln) {
		check(ln, true);
	}

	public void check(LibraryNode ln, boolean validate) {
		MockLibrary ml = new MockLibrary();

		assertTrue(ln.getParent() != null);

		// check all members
		for (LibraryMemberInterface n : ln.getDescendants_LibraryMembers()) {
			ml.check((Node) n, validate);
			assert (((NavNode) n.getParent()).contains((Node) n));
			// Inherited members may be in different libraries
			if (!(n instanceof InheritedInterface)) {
				assert n.getLibrary() == n.getParent().getLibrary();
				assert n.getLibrary() == ln;
			}
		}
	}

	@Test
	public void libraryConstructorsTests() {
		// Given - a project
		ProjectNode project1 = createProject("Project1", rc.getLocalRepository(), "IT1");
		String ns = "http://example.com/ns1";

		// When - constructed from tl library
		LibraryNode fromTL = new LibraryNode(createTL("FTL", ns), project1);
		// Then - node with listener is correct
		assertTrue(fromTL != null);
		assertTrue(Node.GetNode(fromTL.getTLModelObject()) == fromTL);

		// When - mock library used
		LibraryNode fromML = ml.createNewLibrary_Empty(ns, "FML", project1);
		assertTrue(fromML != null);
		assertTrue(Node.GetNode(fromML.getTLModelObject()) == fromML);
		boolean ans = fromML.isEditable();

		LibraryChainNode lcn = new LibraryChainNode(fromML);
		LN_isEditableTests(fromML);
		assertTrue(lcn != null);
		assertTrue(fromML.isInChain());
		assertTrue("Must not be editable, wrong namespace.", !fromML.isEditable());

		// When - new library in project's namespace is managed
		LibraryNode projNS = ml.createNewLibrary_Empty(project1.getNamespace(), "FMP", project1);
		new LibraryChainNode(projNS);
		assertTrue("Must be editable.", projNS.isEditable());

		// TODO
		// new LibraryNode(createTL("FVL", ns), VersionAggregate);
		// new LibraryNode(projectItem, ns), lcn);

	}

	@Test
	public void checkBuiltIns() {
		for (Node n : Node.getAllLibraries()) {
			Assert.assertTrue(n instanceof LibraryNode);
			ml.check(n);
		}
	}

	private void LN_isEditableTests(LibraryNode ln) {
		boolean ans = false;
		ans = ln.isAbsLibEditable();
		ans = ln.isEditable();
		ans = ln.isEditable_description();
		ans = ln.isEditable_equivalent();
		ans = ln.isEditable_example();
		ans = ln.isEditable_ifMinorCreated();
		ans = ln.isEditable_inMinor();
		ans = ln.isEditable_inService();
		ans = ln.isEditable_isNewOrAsMinor();
		ans = ln.isEditable_newToChain();
	}

	public TLLibrary createTL(String name, String ns) {
		TLLibrary tllib = new TLLibrary();
		tllib.setName(name);
		tllib.setStatus(TLLibraryStatus.DRAFT);
		tllib.setNamespaceAndVersion(ns, "1.0.0");
		tllib.setPrefix("nsPrefix");
		return tllib;
	}

	@Test
	public void LN_multipleLibraryIncludes() throws Exception {
		LoadFiles lf = new LoadFiles();
		ProjectNode defaultProject = mc.getProjectController().getDefaultProject();
		LibraryModelManager lm = Node.getModelNode().getLibraryManager();

		// Given - load test group
		ProjectNode project1 = createProject("Project1", rc.getLocalRepository(), "IT1");
		ProjectNode project2 = createProject("Project2", rc.getLocalRepository(), "IT2");
		lf.loadTestGroupAc(defaultProject); // Load into default project
		lf.loadFile2(project1); // Load file 2 again
		lf.loadTestGroupAc(project2);

		// Then - make sure default project libraries are in the library manager
		for (LibraryNode ln : defaultProject.getLibraries())
			assertTrue(lm.findKey(ln) != null);

		// When - loading files from OTA Repository
		ProjectNode vt1 = lf.loadVersionTestProject(mc.getProjectController());
		// ProjectNode vt2 = lf.loadVersionTestProject(mc.getProjectController());

		// Then - make sure all libraries create canonical names w/o error
		for (LibraryNode li : lm.getAllLibraries()) {
			LOGGER.debug(lm.getCanonicalName(li.getProjectItem()));
		}

		Object[] mapped = lm.getMapValues();
		Collection<LibraryInterface> list = lm.getListValues();
		for (Object li : mapped)
			assert list.contains(li);

		// Then - make sure all libraries are loaded only once
		HashMap<String, LibraryInterface> libMap = checkUnique(lm.getLibraries());

		// Then - make sure all project libraries are found and unique
		for (ProjectNode pn : mc.getProjectController().getAll()) {
			checkLibsUnique(pn.getLibraries());
			for (LibraryNode ln : pn.getLibraries()) {
				String cn = lm.getCanonicalName(ln.getProjectItem());
				LibraryInterface li = libMap.get(cn);
				if (li instanceof LibraryNode)
					assertTrue("Must be in map.", libMap.get(cn) == ln);
				else
					assertTrue("Must be in map.", libMap.get(cn) == ln.getChain());
			}
		}
	}

	// Make sure each library (ns+name) is in the list only once.
	private HashMap<String, LibraryInterface> checkUnique(List<LibraryInterface> list) {
		HashMap<String, LibraryInterface> libMap = new HashMap<>();
		LibraryModelManager lm = Node.getModelNode().getLibraryManager();
		for (LibraryInterface li : list)
			if (li instanceof LibraryNode) {
				assertTrue("Must not be in map.",
						libMap.get(lm.getCanonicalName(((LibraryNode) li).getProjectItem())) == null);
				libMap.put(lm.getCanonicalName(((LibraryNode) li).getProjectItem()), li);
			} else
				// May be duplicated since all minor verisons are in one entry
				for (LibraryNode ln : ((LibraryChainNode) li).getLibraries()) {
				// assertTrue("Must not be in map.", libMap.get(ln.getName_Canonical()) == null);
				libMap.put(lm.getCanonicalName(ln.getProjectItem()), li);
				}
		return libMap;
	}

	private void checkLibsUnique(List<LibraryNode> list) {
		HashMap<String, LibraryInterface> libMap = new HashMap<>();
		LibraryModelManager lm = Node.getModelNode().getLibraryManager();
		for (LibraryNode li : list) {
			assertTrue("Must not be in map.", libMap.get(li.getNamespace() + li.getName()) == null);
			libMap.put(li.getNamespace() + li.getName(), li);
		}
	}

	// See DefaultLibraryController_Tests.removeManagedInMultipleProjects_Test()
	@Test
	public void libraryInMultipleProjects() throws LibrarySaveException {
		LoadFiles lf = new LoadFiles();

		// Given - the same file opened in 2 projects
		ProjectNode project1 = createProject("Project1", rc.getLocalRepository(), "IT1");
		ProjectNode project2 = createProject("Project2", rc.getLocalRepository(), "IT2");
		LibraryNode lib1 = lf.loadFile2(project1);
		LibraryNode lib2 = lf.loadFile2(project2);
		assertTrue("Library1 must not be null.", lib1 != null);
		assertTrue("Library2 must not be null.", lib2 != null);
		assertTrue("Project1 must have 1 child library.", project1.getChildren().size() == 1);
		assertTrue("Project2 must have 1 child library.", project2.getChildren().size() == 1);
		// Library parent is not reliable way to find project
		LibraryNavNode lnn1 = (LibraryNavNode) project1.getChildren().get(0);
		LibraryNavNode lnn2 = (LibraryNavNode) project2.getChildren().get(0);
		assertTrue("LibraryNavNode 1 must not be null.", lnn1 != null);
		assertTrue("LibraryNavNode 2 must not be null.", lnn2 != null);
		assertTrue("LibraryNavNodes must be different.", lnn1 != lnn2);
		// hold onto for later use.
		// List<Node> complexNamedtypes = lib2.getDescendants_NamedTypes();
		int ln2NamedTypeCount = lib2.getDescendants_LibraryMembers().size();

		// When - a library is removed
		pc.remove(lnn1);

		// Then - check to make sure it was closed and removed.
		assertTrue("Project 1 must be empty.", project1.getLibraries().isEmpty());
		assertTrue("Project 1 must be empty.", project1.getChildren().isEmpty());
		// Then - lib1 is lib2 therefore it must be altered.
		assertTrue("Lib1 must NOT be empty.", !lib1.getDescendants_LibraryMembers().isEmpty());
		// Then - check the other library to make sure it was not effected.
		assertTrue("Lib2 must have same number of named types.",
				lib2.getDescendants_LibraryMembers().size() == ln2NamedTypeCount);
		for (LibraryMemberInterface n : lib2.getDescendants_LibraryMembers())
			assertTrue("Named type must not be deleted.", !n.isDeleted());

		// Same test with libraries in a chain
		//
		// Given - lib1 reloaded from file, 2 projects containing chains
		lib1 = lf.loadFile2(project1);
		LibraryChainNode lcn1 = new LibraryChainNode(lib1);
		lnn1 = (LibraryNavNode) project1.getChildren().get(0);
		// Project 2 already has LNN for library

		// Then there must be two
		assertTrue("Lib nav nodes must be different.", lnn1 != lnn2);

		// When - library chain 1 is removed
		pc.remove(lnn1);

		// Then
		assertTrue("Project 1 must have no libraries.", project1.getLibraries().isEmpty());
		assertTrue("Project 1 must be empty.", project1.getChildren().isEmpty());

		assertTrue("Lib2 must have same number of named types.",
				lib2.getDescendants_LibraryMembers().size() == ln2NamedTypeCount);
		// Each named type must not be deleted and must have a valid nav node and library
		for (LibraryMemberInterface n : lib2.getDescendants_LibraryMembers()) {
			assertTrue("Named type must not be deleted.", !n.isDeleted());
			assertTrue("Named type must be in lib2.", n.getLibrary() == lib2);
			assertTrue("Named type's parent must not be deleted.", !n.getParent().isDeleted());
			assertTrue("Named type's parent must be in lib2.", n.getParent().getLibrary() == lib2);
		}

		// delete second lib and insure deleted.
		pc.remove(lnn2);
		List<LibraryMemberInterface> l2libs = lib2.getDescendants_LibraryMembers();
		assertTrue("Project 2 must be empty.", project2.getChildren().isEmpty());
		assertTrue("Lib2 must be empty.", lib2.getDescendants_LibraryMembers().isEmpty());

		// TODO - close the projects containing the libraries
		// done in after tests
	}

	@Test
	public void Library_LoadTests() throws Exception {
		// Library Manager has other libraries in it!
		List<LibraryNode> startingLibs = ModelNode.getLibraryModelManager().getUserLibraries();
		if (!startingLibs.isEmpty())
			LOGGER.debug("Error - not starting with empty library manager.");

		LoadFiles lf = new LoadFiles();
		LibraryNode l1 = lf.loadFile1(mc);
		List<LibraryNode> managedLibs = ModelNode.getLibraryModelManager().getUserLibraries();
		// 4 libraries loaded // if (!managedLibs.isEmpty())
		// LOGGER.debug("Error - not starting with empty library manager.");
		ml.check(l1);

		lf.loadFile2(mc);
		lf.loadFile3(mc);
		lf.loadFile4(mc);
		lf.loadFile5Clean(mc);

		for (LibraryNode ln : Node.getAllLibraries())
			ml.check(ln, false);

		// If not editable,most of the other tests will fail.
		for (LibraryNode ln : Node.getAllUserLibraries()) {
			ln.setEditable(true);
			assertTrue(ln.isEditable());
			assertFalse(ln.getPath().isEmpty());
			assertTrue(ln.getNamespace().equals(ln.getTLModelObject().getNamespace()));
			assertTrue(ln.getPrefix().equals(ln.getTLModelObject().getPrefix()));
		}

		// // Make sure we can create new empty libraries as used by wizard
		// LibraryNode newLib = new LibraryNode(l1.getProject());
		// assertTrue(newLib != null);

		for (LibraryNode ln : Node.getAllUserLibraries()) {
			// removeAllMembers(ln);
			for (LibraryMemberInterface n : ln.getDescendants_LibraryMembers()) {
				if (n instanceof ServiceNode)
					LOGGER.debug("ready to remove service.");
				ln.removeMember(n); // May change type assignments!
			}
			List<LibraryMemberInterface> dlmn = ln.getDescendants_LibraryMembers();
			List<LibraryMemberInterface> dlm = ln.getDescendants_LibraryMembers();
			Assert.assertTrue(ln.getDescendants_LibraryMembers().size() < 1);
		}
	}

	@Test
	public void NT_governedLibEditable() throws Exception {
		LoadFiles lf = new LoadFiles();

		LibraryNode sourceLib = lf.loadFile5Clean(mc); // load into default project
		LibraryNode destLib = lf.loadFile1(mc);
		LibraryChainNode lcn = new LibraryChainNode(destLib);
		ml.check(sourceLib);

		// When - source lib added to the chain
		lcn.add(sourceLib.getProjectItem());

		// Make sure they loaded OK.
		ml.check(sourceLib);
		ml.check(destLib);

		ProjectNode defaultProject = mc.getProjectController().getDefaultProject();
		defaultProject.closeAll();

		// LOGGER.debug("\n");
		LOGGER.debug("Start Import ***************************");
		// TODO - what has this to do with IMPORT???
		// make sure that destLib is editable
		String projectFile = MockLibrary.createTempFile("TempProject", ".otp");
		ProjectNode project = pc.create(new File(projectFile), destLib.getNamespace(), "Name", "");
		assertTrue("Project must have same namespace as destLib.",
				project.getNamespace().equals(destLib.getNamespace()));

		// When - a library is added to a project that governs it's namespace
		destLib = pc.add(project, destLib.getTLModelObject()).getLibrary();

		// Then - the library must be editable.
		// Will only be true if library is Read-Write in file system.
		// assertTrue(destLib.isEditable());

		// Make sure still OK
		ml.check(sourceLib);
		ml.check(destLib);

	}

}
