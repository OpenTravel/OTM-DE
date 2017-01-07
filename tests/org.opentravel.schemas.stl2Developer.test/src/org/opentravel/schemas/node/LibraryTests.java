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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLAdditionalDocumentationItem;
import org.opentravel.schemacompiler.model.TLContext;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemas.controllers.DefaultLibraryController;
import org.opentravel.schemas.controllers.DefaultProjectController;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNavNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.testUtils.MockLibrary;
import org.opentravel.schemas.testUtils.NodeTesters;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dave Hollander
 * 
 */
// TODO - validate assertion: Every new library is obliged to register itself
// within the
// NamespaceHandler.

public class LibraryTests {
	private static final Logger LOGGER = LoggerFactory.getLogger(LibraryTests.class);

	private NodeTesters testVisitor;
	private MainController mc;
	private LoadFiles lf;
	private MockLibrary ml;
	private DefaultProjectController pc;
	private ProjectNode defaultProject;

	@Before
	public void beforeEachTest() {
		mc = new MainController();
		lf = new LoadFiles();
		ml = new MockLibrary();
		testVisitor = new NodeTesters();
		pc = (DefaultProjectController) mc.getProjectController();
		defaultProject = pc.getDefaultProject();
	}

	// TODO - verify isEmpty() behavior

	@Test
	public void checkBuiltIns() {
		for (INode n : Node.getAllLibraries()) {
			Assert.assertTrue(n instanceof LibraryNode);
			visitLibrary((LibraryNode) n);
		}
	}

	@Test
	public void checkLibraries() throws Exception {
		LibraryNode l1 = lf.loadFile1(mc);
		visitLibrary(l1);

		// testNewWizard((ProjectNode) l1.getProject());

		lf.loadFile2(mc);
		lf.loadFile3(mc);
		lf.loadFile4(mc);
		lf.loadFile5(mc);

		for (LibraryNode ln : Node.getAllLibraries())
			visitLibrary(ln);

		// If not editable,most of the other tests will fail.
		for (LibraryNode ln : Node.getAllUserLibraries()) {
			ln.setEditable(true);
			assertTrue(ln.isEditable());
			assertFalse(ln.getPath().isEmpty());
			assertTrue(ln.getNamespace().equals(ln.getTLaLib().getNamespace()));
			assertTrue(ln.getPrefix().equals(ln.getTLaLib().getPrefix()));
		}

		// Make sure we can create new empty libraries as used by wizard
		LibraryNode newLib = new LibraryNode(l1.getProject());
		assertTrue(newLib != null);

		for (LibraryNode ln : Node.getAllLibraries())
			removeAllMembers(ln);
	}

	private void removeAllMembers(LibraryNode ln) {
		for (Node n : ln.getDescendants_LibraryMembers())
			ln.removeMember(n); // May change type assignments!

		Assert.assertTrue(ln.getDescendants_LibraryMembers().size() < 1);
	}

	/**
	 * Check the library. Checks library structures then all children. Asserts error if the library is empty!
	 * 
	 * @param ln
	 */
	protected void visitLibrary(LibraryNode ln) {
		if (ln.isXSDSchema()) {
			assertTrue(ln.getGeneratedLibrary() != null);
			assertTrue(ln.hasGeneratedChildren());
		}
		assertTrue(ln.getChildren().size() > 1);
		assertTrue(ln.getDescendants_LibraryMembers().size() > 1);

		if (ln.getName().equals("OTA2_BuiltIns_v2.0.0")) {
			Assert.assertEquals(85, ln.getDescendants_LibraryMembers().size());
		}

		if (ln.getName().equals("XMLSchema")) {
			Assert.assertEquals(20, ln.getDescendants_LibraryMembers().size());
		}

		if (!ln.isInChain()) {
			assertTrue(ln.getChildren().size() == ln.getNavChildren(false).size());
			assertTrue(ln.getParent() instanceof LibraryNavNode);
			assertTrue("Must have at least one related project.", ln.getProject() != null);
		} else {
			assertTrue(ln.getParent() instanceof VersionAggregateNode);
			assertTrue(ln.getChain().getParent() instanceof LibraryNavNode);
			// What about size? == 0
			assertTrue("Chain members do not have navChildren.", ln.getNavChildren(false).size() == 0);
		}

		Assert.assertNotNull(ln.getTLaLib());

		Assert.assertFalse(ln.getName().isEmpty());
		Assert.assertFalse(ln.getNamespace().isEmpty());
		Assert.assertFalse(ln.getPrefix().isEmpty());

		if (ln.isTLLibrary()) {
			Assert.assertFalse(ln.getContextIds().isEmpty());
		}

		Assert.assertFalse(ln.getPath().isEmpty());

		for (Node n : ln.getChildren()) {
			visitNode(n);
		}
	}

	public void visitNode(Node n) {
		// LOGGER.debug("Visit Node: " + n + " of type " + n.getClass().getSimpleName());
		Assert.assertNotNull(n);
		Assert.assertNotNull(n.getParent());
		Assert.assertNotNull(n.getLibrary());
		Assert.assertNotNull(n.modelObject);
		Assert.assertNotNull(n.getTLModelObject());
		// Assert.assertTrue(n.getTypeClass().verifyAssignment());

		// Assert.assertNotNull(n.getTypeClass());
		if (n instanceof TypeUser) {
			// LOGGER.debug("Visit Node: " + n + " of type " + n.getClass().getSimpleName());
			// boolean x = n.getTypeClass().verifyAssignment();
			// Resolver may not have run
			// Assert.assertNotNull(n.getType());
			Assert.assertEquals(n.getType(), ((TypeUser) n).getAssignedType());
		}

		if (n.getName().isEmpty())
			LOGGER.debug("no name");
		Assert.assertFalse(n.getName().isEmpty());
		for (Node nn : n.getChildren()) {
			visitNode(nn);
		}
	}

	// Emulates the logic within the wizard
	private void testNewWizard(ProjectNode parent) {
		final String InitialVersionNumber = "0_1";
		final String prefix = "T1T";
		final DefaultLibraryController lc = new DefaultLibraryController(mc);
		final LibraryNode ln = new LibraryNode(parent);
		final String baseNS = parent.getNamespace();
		final ProjectNode pn = mc.getProjectController().getDefaultProject();
		final int libCnt = pn.getLibraries().size();
		// Strip the project file
		String path = pn.getPath();
		path = new File(path).getParentFile().getPath();
		path = new File(path, "Test.otm").getPath();
		final String name = "Test";

		String ns = ln.getNsHandler().createValidNamespace(baseNS, InitialVersionNumber);
		ln.getTLaLib().setNamespace(ns);
		ln.getTLaLib().setPrefix(prefix);
		ln.setPath(path);
		ln.setName(name);
		Assert.assertEquals(name, ln.getName());
		LOGGER.debug("Done setting up for wizard complete.Path = " + path);

		// This code runs after the wizard completes
		LibraryNode resultingLib = lc.createNewLibraryFromPrototype(ln, pn).getLibrary();
		// See DefaultLibraryControllerTests

		// LOGGER.debug("new library created. Cnt = " + pn.getLibraries().size());
		// Assert.assertEquals(libCnt + 1, pn.getLibraries().size());
		//
		// // Leave something in it
		// NewComponent_Tests nct = new NewComponent_Tests();
		// nct.createNewComponents(resultingLib);
		//
		// // resultingLib.getRepositoryDisplayName();
		// visitLibrary(resultingLib);
	}

	@Test
	public void checkStatus() {
		LibraryNode ln = lf.loadFile5Clean(mc);

		Assert.assertEquals(ln.getEditStatus(), NodeEditStatus.NOT_EDITABLE);
		Assert.assertFalse(ln.getEditStatusMsg().isEmpty());
		Assert.assertFalse(ln.isManaged());
		Assert.assertFalse(ln.isLocked());
		Assert.assertFalse(ln.isInProjectNS());
		Assert.assertTrue(ln.isMajorVersion());
		Assert.assertTrue(ln.isMinorOrMajorVersion());
		Assert.assertFalse(ln.isPatchVersion());

		ln.setNamespace(ln.getProject().getNamespace() + "test/v1_2_3");
		Assert.assertNotNull(ln.getNsHandler());
		String n = ln.getNamespace();
		Assert.assertFalse(ln.getNamespace().isEmpty());
		n = ln.getNSExtension();
		Assert.assertTrue(ln.getNSExtension().equals("test"));
		n = ln.getNSVersion();
		Assert.assertTrue(ln.getNSVersion().equals("1.2.3"));
		Assert.assertTrue(ln.isPatchVersion());

		ln.setNamespace(ln.getProject().getNamespace() + "test/v1_2");
		n = ln.getNSVersion();
		Assert.assertTrue(ln.getNSVersion().equals("1.2.0"));
		Assert.assertTrue(ln.isMinorOrMajorVersion());
	}

	@Test
	public void checkNS() {

	}

	@Test
	public void collapseTests() {
		// LibraryChainNode fromChain = ml.createNewManagedLibrary("FromChain", defaultProject);
		// LibraryNode fromLib = fromChain.getHead();
		LibraryNode fromLib = ml.createNewLibrary("http://test.com/ns1", "FromLib", defaultProject);
		LibraryNode toLib = ml.createNewLibrary("http://test.com/ns2", "ToLib", defaultProject);
		Assert.assertTrue(fromLib.isEditable());

		// Check initial library contexts
		List<TLContext> fromLibContexts = fromLib.getTLLibrary().getContexts();
		assertTrue("Must only have 1 context.", fromLibContexts.size() == 1);
		List<TLContext> toLibContexts = toLib.getTLLibrary().getContexts();
		assertTrue("Must only have 1 context.", toLibContexts.size() == 1);

		// Add then check context users
		Node object = addContextUsers(fromLib);
		Assert.assertEquals(2, fromLibContexts.size()); // default and other doc
		PropertyNode pn = (PropertyNode) object.getDescendants_TypeUsers().get(0);
		Assert.assertNotNull(pn);
		String appContext1 = pn.getExampleHandler().getApplicationContext();
		Assert.assertTrue(appContext1.startsWith("http://test.com/ns1"));

		// Add another Context to TL Library
		TLContext tlc = new TLContext();
		tlc.setApplicationContext("AppContext1");
		tlc.setContextId("Cid1");
		fromLib.getTLLibrary().addContext(tlc);
		Assert.assertEquals(3, fromLibContexts.size());

		fromLib.collapseContexts();
		Assert.assertEquals(1, fromLibContexts.size());
		String appContext2 = pn.getExampleHandler().getApplicationContext();
		Assert.assertFalse(appContext2.isEmpty());
		Assert.assertTrue(appContext2.startsWith("http://test.com/ns1"));

		// moveNamedMember will create contexts if the object has a context not already in destination library
		try {
			object.getLibrary().getTLLibrary()
					.moveNamedMember((LibraryMember) object.getTLModelObject(), toLib.getLibrary().getTLLibrary());
		} catch (Exception e) {
			LOGGER.debug("moveNamedMember failed. " + e.getLocalizedMessage());
		}
		String appContext3 = pn.getExampleHandler().getApplicationContext();
		Assert.assertTrue(appContext3.startsWith("http://test.com/ns1")); // app context copied on moveNamedMember
		Assert.assertEquals(2, toLibContexts.size());

		//
		toLib.collapseContexts();
		Assert.assertEquals(1, fromLibContexts.size());
		Assert.assertEquals(1, toLibContexts.size());
	}

	/**
	 * Add example and equivalent to 1st property in the library. Add a custom facet. Add TLAdditionalDocumentationItem
	 * and set context
	 * 
	 * @param lib
	 * @return
	 */
	private Node addContextUsers(LibraryNode lib) {
		Node object = lib.getDescendants_LibraryMembers().get(0);
		Node tu = object.getChildren_TypeUsers().get(0);
		Assert.assertNotNull(tu);
		Assert.assertTrue(tu instanceof PropertyNode);
		PropertyNode pn = (PropertyNode) tu;
		pn.setExample("Ex1"); // use default context
		Assert.assertTrue(pn.getExample(null).equals("Ex1"));

		// add an equivalent
		pn.setEquivalent("Eq1");

		// add a facet with context
		if (object instanceof BusinessObjectNode)
			((BusinessObjectNode) object).addFacet("TestFacet1", TLFacetType.CUSTOM);

		// add an other doc with context - creates a second context
		TLAdditionalDocumentationItem otherDoc = new TLAdditionalDocumentationItem();
		otherDoc.setContext("OD1");
		otherDoc.setText("description in OD1 context");
		pn.getDocumentation().addOtherDoc(otherDoc);
		return object;
	}

	@SuppressWarnings("unused")
	@Test
	public void moveMemberTests() throws Exception {
		LibraryChainNode fromChain = ml.createNewManagedLibrary("FromChain", defaultProject);
		LibraryChainNode toChain = ml.createNewManagedLibrary("ToChain", defaultProject);
		LibraryNode fromLib = ml.createNewLibrary("http://test.com/ns1", "FromLib", defaultProject);
		LibraryNode toLib = ml.createNewLibrary("http://test.com/ns2", "ToLib", defaultProject);
		Assert.assertTrue(fromLib.isEditable());
		Assert.assertTrue(toLib.isEditable());
		List<TLContext> toLibContexts = toLib.getTLLibrary().getContexts();
		List<TLContext> fromLibContexts = fromLib.getTLLibrary().getContexts(); // live list

		// Assure context is used by a property (ex, eq, facet and other doc)
		Node object = addContextUsers(fromLib);
		Assert.assertEquals(2, fromLibContexts.size());

		// do the move and check to assure only one context in to-library
		object.getLibrary().moveMember(object, toLib);
		Assert.assertEquals(2, fromLibContexts.size());
		Assert.assertEquals(1, toLibContexts.size());
		Assert.assertEquals(2, toLib.getDescendants_LibraryMembers().size()); // BO and CustomFacet

		// Load up the old test libraries and move lots and lots of stuff then test to-library
		ModelNode model = mc.getModelNode();
		lf.loadTestGroupA(mc);

		int count = toLib.getDescendants_LibraryMembers().size();
		for (LibraryNode ln : model.getUserLibraries()) {
			if (ln != toLib && ln != fromLib) {
				LibraryChainNode lcn;
				if (ln.isInChain())
					lcn = ln.getChain();
				else
					lcn = new LibraryChainNode(ln);
				int libCount = ln.getDescendants_LibraryMembers().size();
				for (Node n : ln.getDescendants_LibraryMembers()) {
					if (n instanceof ServiceNode)
						continue;

					// Custom Facet test case
					// if (n instanceof BusinessObjectNode && !((BusinessObjectNode) n).getCustomFacets().isEmpty())
					// LOGGER.debug("Business Object with custom facets.");
					LOGGER.debug("Moving " + n + " from " + n.getLibrary() + " to " + toLib);

					if (n.getLibrary().moveMember(n, toLib))
						count++;

					// Make sure the node is removed.
					if (libCount - 1 != ln.getDescendants_LibraryMembers().size()) {
						List<Node> dl = ln.getDescendants_LibraryMembers();
						LOGGER.debug("Bad Counts: " + dl.size());
					}
					// Assert.assertEquals(--libCount, ln.getDescendants_LibraryMembers().size());

					// Track toLib count growth - use to breakpoint when debugging
					int toCount = toLib.getDescendants_LibraryMembers().size();
					if (count != toCount) {
						LOGGER.debug("Problem with " + n);
						// count = toCount; // fix the count so the loop can continue
					}
				}
			}
		}
		Assert.assertEquals(1, toLibContexts.size());
		Assert.assertEquals(count, toLib.getDescendants_LibraryMembers().size());

		// FIXME Assert.assertFalse(fromChain.getContextIds().isEmpty());
		// FIXME Assert.assertFalse(toChain.getContextIds().isEmpty());
	}

	@Test
	public void addMember() {
		LibraryNode ln = ml.createNewLibrary("http://www.test.com/test1", "testUnmanaged", defaultProject);
		LibraryNode ln_inChain = ml.createNewLibrary("http://www.test.com/test1c", "testManaged", defaultProject);
		LibraryChainNode lcn = new LibraryChainNode(ln_inChain);
		ln_inChain.setEditable(true);

		// makeSimple() does
		// SimpleTypeNode sn = new SimpleTypeNode(new TLSimple());
		// sn.setName(name);
		// sn.setAssignedType(NodeFinders.findNodeByName("int", ModelNode.XSD_NAMESPACE));
		ComponentNode s1 = (ComponentNode) makeSimple("s_1");
		ComponentNode s2 = (ComponentNode) makeSimple("s_2");
		ComponentNode sv1 = (ComponentNode) makeSimple("sv_1");
		ComponentNode sv2 = (ComponentNode) makeSimple("sv_2");

		// Test un-managed
		ln.addMember(s1);
		assertEquals(1, ln.getSimpleRoot().getChildren().size());
		assertEquals(2, ln.getDescendants_LibraryMembers().size());
		ln.addMember(s2);
		assertEquals(2, ln.getSimpleRoot().getChildren().size());
		assertEquals(3, ln.getDescendants_LibraryMembers().size());

		// Test managed
		ln_inChain.addMember(sv1);
		ln_inChain.addMember(sv2);
		assertEquals(2, ln_inChain.getSimpleRoot().getChildren().size());
		assertEquals(3, ln_inChain.getDescendants_LibraryMembers().size());
		assertEquals(2, lcn.getSimpleAggregate().getChildren().size());

		// When - test adding s1 - see fix me in addMember
		// ln_inChain.addMember(s1);
		// // Then - make sure s1 is removed from ln
		// assertTrue("S1 must now be in new library", lcn.getDescendants_LibraryMembers().contains(s1));
		// assertTrue("S1 must NOT be in old library", ln.getDescendants_LibraryMembers().contains(s1));
	}

	@Test
	public void linkMember() {
		LibraryNode ln = ml.createNewLibrary("http://www.test.com/test1", "test1", defaultProject);
		LibraryNode ln_inChain = ml.createNewLibrary("http://www.test.com/test1c", "test1c", defaultProject);
		LibraryChainNode lcn = new LibraryChainNode(ln_inChain);

		ComponentNode s1 = (ComponentNode) makeSimple("s_1");
		ComponentNode s2 = (ComponentNode) makeSimple("s_2");
		ComponentNode sv1 = (ComponentNode) makeSimple("sv_1");
		ComponentNode sv2 = (ComponentNode) makeSimple("sv_2");

		// Link first member
		// Library already has InitialBO
		ln_inChain.getTLLibrary().addNamedMember((LibraryMember) s1.getTLModelObject());
		ln_inChain.linkMember(s1);
		Assert.assertEquals(2, ln_inChain.getDescendants_LibraryMembers().size());

		//
		// Now test with a family member
		//
		ln_inChain.getTLLibrary().addNamedMember((LibraryMember) s2.getTLModelObject());
		ln_inChain.linkMember(s2);
		Assert.assertEquals(3, ln_inChain.getDescendants_LibraryMembers().size());
		Assert.assertEquals(2, ln_inChain.getSimpleRoot().getChildren().size());

		//
		// Test with Version wrapped objects
		//
		ln_inChain.getTLLibrary().addNamedMember((LibraryMember) sv1.getTLModelObject());
		ln_inChain.linkMember(sv1);
		new VersionNode(sv1);
		Assert.assertEquals(3, ln_inChain.getSimpleRoot().getChildren().size());

		ln_inChain.getTLLibrary().addNamedMember((LibraryMember) sv2.getTLModelObject());
		ln_inChain.linkMember(sv2);
		Assert.assertEquals(4, ln_inChain.getSimpleRoot().getChildren().size()); // two families
		// for (Node n : ln_inChain.getSimpleRoot().getChildren())
		// Assert.assertTrue(n instanceof FamilyNode);
	}

	private Node makeSimple(String name) {
		Node n = new SimpleTypeNode(new TLSimple());
		n.setName(name);
		((TypeUser) n).setAssignedType((TypeProvider) NodeFinders.findNodeByName("int", ModelNode.XSD_NAMESPACE));
		return n;
	}

}
