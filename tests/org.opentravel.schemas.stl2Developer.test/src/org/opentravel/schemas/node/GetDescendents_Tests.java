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
/**
 * 
 */
package org.opentravel.schemas.node;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemacompiler.saver.LibrarySaveException;
import org.opentravel.schemas.controllers.DefaultProjectController;
import org.opentravel.schemas.controllers.DefaultRepositoryController;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.controllers.ProjectController;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.testUtils.MockLibrary;
import org.opentravel.schemas.testUtils.NodeTesters;
import org.opentravel.schemas.types.TypeUser;
import org.opentravel.schemas.utils.LibraryNodeBuilder;
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dave Hollander
 * 
 */
public class GetDescendents_Tests {
	static final Logger LOGGER = LoggerFactory.getLogger(MockLibrary.class);

	ModelNode model = null;
	NodeTesters nt = new NodeTesters();
	LoadFiles lf = new LoadFiles();
	LibraryTests lt = new LibraryTests();
	MockLibrary mockLibrary = null;
	LibraryNode ln = null;
	MainController mc;
	DefaultProjectController pc;
	ProjectNode defaultProject;
	String OTA = "OTA2_BuiltIns_v2.0.0"; // name
	String XSD = "XMLSchema";

	@Before
	public void beforeAllTests() {
		mc = new MainController();
		mockLibrary = new MockLibrary();
		pc = (DefaultProjectController) mc.getProjectController();
		defaultProject = pc.getDefaultProject();
	}

	@Test
	public void visitAllTypeUsers() {
		ln = mockLibrary.createNewLibrary("http://example.com/test", "test", defaultProject);
		CoreObjectNode co = mockLibrary.addCoreObjectToLibrary(ln, "");
		BusinessObjectNode bo = mockLibrary.addBusinessObjectToLibrary(ln, "");
		VWA_Node vwa = mockLibrary.addVWA_ToLibrary(ln, "");

		assertTrue("Core simple is assigned type.", co.getSimpleFacet().getSimpleAttribute().getAssignedType() != null);
		co.visitAllTypeUsers(nt.new TestNode());
		// MOVE Assert.assertEquals(2, co.getDescendants_TypeUsers().size());

		assertTrue("VWA simple is assigned type.", vwa.getSimpleFacet().getSimpleAttribute().getAssignedType() != null);
		vwa.visitAllTypeUsers(nt.new TestNode());
		// MOVE Assert.assertEquals(2, vwa.getDescendants_TypeUsers().size());

		// MOVE Assert.assertEquals(2, bo.getDescendants_TypeUsers().size());
	}

	@Test
	public void mockDescendents() {
		ln = mockLibrary.createNewLibrary("http://example.com/test", "test", defaultProject);
		ln.setEditable(true);
		CoreObjectNode co = mockLibrary.addCoreObjectToLibrary(ln, "");
		VWA_Node vwa = mockLibrary.addVWA_ToLibrary(ln, "");
		EnumerationOpenNode oe = mockLibrary.addOpenEnumToLibrary(ln, "");
		EnumerationClosedNode ce = mockLibrary.addClosedEnumToLibrary(ln, "");
		co.setSimpleType(ce);
		vwa.setSimpleType(oe);

		// Then - spot check descendant lists
		assertTrue("Library must contain core.", ln.getDescendants().contains(co));
		assertTrue("Library must contain core summary.", ln.getDescendants().contains(co.getSummaryFacet()));
		assertTrue("Library must contain core roles.", ln.getDescendants_TypeProviders().contains(co.getRoleFacet()));
		assertTrue("Library must contain core element.",
				ln.getDescendants_TypeUsers().contains(co.getSummaryFacet().getChildren().get(0)));
		assertTrue("Library must contain vwa.", ln.getDescendants().contains(vwa));
		assertTrue("Library must contain open enum.", ln.getDescendants_LibraryMembers().contains(oe));
		assertTrue("Library must contain closed enum.", ln.getDescendants_LibraryMembers().contains(ce));

		// TODO - move these type of count tests to mock library
		// List<Node> all = ln.getDescendants();
		// Assert.assertEquals(27, all.size());
		// List<Node> named = ln.getDescendants_NamedTypes();
		// Assert.assertEquals(5, named.size());
		// List<TypeUser> users = ln.getDescendants_TypeUsers();
		// Assert.assertEquals(9, users.size());
	}

	@Test
	public void mockDescendentsInManagedLibrary() {
		ln = mockLibrary.createNewLibrary("http://example.com/test", "test", defaultProject);
		new LibraryChainNode(ln);
		ln.setEditable(true);
		CoreObjectNode co = mockLibrary.addCoreObjectToLibrary(ln, "");
		VWA_Node vwa = mockLibrary.addVWA_ToLibrary(ln, "");
		EnumerationOpenNode oe = mockLibrary.addOpenEnumToLibrary(ln, "");
		EnumerationClosedNode ce = mockLibrary.addClosedEnumToLibrary(ln, "");
		co.setSimpleType(ce);
		vwa.setSimpleType(oe);

		// TODO - move these type of count tests to mock library
		// List<Node> named = ln.getDescendants_NamedTypes();
		// Assert.assertEquals(5, named.size());
		// List<TypeUser> users = ln.getDescendants_TypeUsers();
		// Assert.assertEquals(9, users.size());
		// MockLibrary.printDescendants(ln);
		// List<Node> all = ln.getDescendants();
		// Assert.assertEquals(32, all.size()); // 26 + 5 version nodes
	}

	@Test
	public void xsdBuiltinDescendents() throws Exception {

		for (LibraryNode n : Node.getAllLibraries()) {
			if (n.getName().equals(XSD))
				ln = n;
		}
		Assert.assertNotNull(ln);
		// 20 xsd simple types, 0 complex, 0 resources
		List<Node> all = ln.getDescendants();
		Assert.assertEquals(24, all.size()); // 4 nav nodes and 20 simple type nodes
		List<Node> named = ln.getDescendants_LibraryMembers();
		Assert.assertEquals(20, named.size());
		List<TypeUser> users = ln.getDescendants_TypeUsers();
		Assert.assertEquals(20, users.size());
	}

	@Test
	public void OTA_Descendents() throws Exception {
		// Built in library removed from version 4.0
		//
		// for (LibraryNode n : Node.getAllLibraries()) {
		// if (n.getName().equals(OTA))
		// ln = n;
		// }
		// Assert.assertNotNull(ln);
		//
		// List<Node> all = ln.getDescendants();
		// Assert.assertEquals(495, all.size());
		// List<Node> named = ln.getDescendants_NamedTypes();
		// Assert.assertEquals(85, named.size());
		// List<Node> users = ln.getDescendants_TypeUsers();
		// Assert.assertEquals(130, users.size());
	}

	@Test
	public void descendantTypeUsersTest() {
		MockLibrary ml = new MockLibrary();
		MainController mc = new MainController();
		DefaultProjectController pc = (DefaultProjectController) mc.getProjectController();
		ProjectNode defaultProject = pc.getDefaultProject();
		LibraryNode ln = ml.createNewLibrary(defaultProject.getNSRoot(), "test", defaultProject);
		Node bo = ml.addNestedTypes(ln);

		List<Node> types = bo.getDescendants_AssignedTypes(true);

		Assert.assertNotNull(types);
		Assert.assertEquals(2, types.size());
	}

	@Test
	public void getDescendents_Tests() throws LibrarySaveException {
		// Needed to isolate results from other tests
		mc = new MainController();
		DefaultRepositoryController lrc = (DefaultRepositoryController) mc.getRepositoryController();
		ProjectController lpc = mc.getProjectController();

		LibraryNode moveFrom = LibraryNodeBuilder.create("MoveFrom", defaultProject.getNamespace() + "/Test/One", "o1",
				new Version(1, 0, 0)).build(defaultProject, pc);

		List<Node> list1 = moveFrom.getDescendants_LibraryMembers();
		List<Node> list2 = moveFrom.getDescendentsNamedTypes();
		assert list1.size() == list2.size();

		LoadFiles lf = new LoadFiles();
		moveFrom = lf.loadFile5Clean(mc);
		list1 = moveFrom.getDescendants_LibraryMembers();
		list2 = moveFrom.getDescendentsNamedTypes();
		for (Node n : list1)
			if (!list2.contains(n))
				LOGGER.debug("list2 is missing: " + n);
		for (Node n : list2)
			if (!list1.contains(n))
				LOGGER.debug("list 1 is missing: " + n);

		// FIXME - these should be equal once the inclusion of services is resolved
		Assert.assertNotEquals(list1.size(), list2.size());

	}

	@Test
	public void getDescendents_LibraryMember_Tests() throws LibrarySaveException {
		// Needed to isolate results from other tests
		mc = new MainController();
		DefaultRepositoryController lrc = (DefaultRepositoryController) mc.getRepositoryController();
		ProjectController lpc = mc.getProjectController();
		MockLibrary ml = new MockLibrary();

		// Given an unmanaged library
		LibraryNode ln = ml.createNewLibrary_Empty("http://example.com", "LM_Tests", lpc.getDefaultProject());
		assertTrue("Initial library must be empty.", 0 == ln.getDescendants_LibraryMembers().size());
		assertTrue("Library Must be editable.", ln.isEditable());
		// When adding one of everything to it
		int count = ml.addOneOfEach(ln, "OE");
		// Then the counts must match
		assertTrue("Must have correct library member count.", count == ln.getDescendants_LibraryMembers().size());

		// Given an managed library
		LibraryChainNode lcn = ml.createNewManagedLibrary_Empty(lpc.getDefaultProject().getNamespace(), "OE2",
				lpc.getDefaultProject());
		ln = lcn.getHead();
		assertTrue("Library Must be editable.", ln.isEditable());
		assertTrue("Initial library must be empty.", 0 == ln.getDescendants_LibraryMembers().size());
		// When adding one of everything to it
		count = ml.addOneOfEach(ln, "OE");
		// Then the counts must match
		List<Node> dList = ln.getDescendants_LibraryMembers();
		LOGGER.debug("Descendant list has " + dList.size() + " members.");
		assertTrue("Must have correct library member count.", count == ln.getDescendants_LibraryMembers().size());
	}

}
