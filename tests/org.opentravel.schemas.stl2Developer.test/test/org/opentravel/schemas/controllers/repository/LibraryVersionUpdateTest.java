package org.opentravel.schemas.controllers.repository;

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

import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemacompiler.repository.Repository;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryItemState;
import org.opentravel.schemacompiler.saver.LibrarySaveException;
import org.opentravel.schemas.node.BusinessObjectNode;
import org.opentravel.schemas.node.CoreObjectNode;
import org.opentravel.schemas.node.EnumerationClosedNode;
import org.opentravel.schemas.node.EnumerationOpenNode;
import org.opentravel.schemas.node.ExtensionPointNode;
import org.opentravel.schemas.node.ImpliedNode;
import org.opentravel.schemas.node.LibraryChainNode;
import org.opentravel.schemas.node.LibraryNode;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFinders;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.node.SimpleTypeNode;
import org.opentravel.schemas.node.VWA_Node;
import org.opentravel.schemas.testUtils.MockLibrary;
import org.opentravel.schemas.trees.repository.RepositoryNode;
import org.opentravel.schemas.trees.repository.RepositoryNode.RepositoryItemNode;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeUser;
import org.opentravel.schemas.utils.LibraryNodeBuilder;
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//@Ignore("Tests currently failing and need attention.")
public class LibraryVersionUpdateTest extends RepositoryIntegrationTestBase {
	static final Logger LOGGER = LoggerFactory.getLogger(LibraryVersionUpdateTest.class);

	MockLibrary ml = new MockLibrary();
	private LibraryNode lib1 = null;
	private LibraryNode lib2 = null;
	private LibraryChainNode chain1 = null;
	private LibraryChainNode chain2 = null;
	private Node xsdString;
	private ProjectNode uploadProject = null;
	// from base class - protected static DefaultRepositoryController rc;

	private BusinessObjectNode bo = null;
	private CoreObjectNode co = null;
	private VWA_Node vwa = null;
	private SimpleTypeNode simple = null;
	private EnumerationClosedNode cEnum = null;
	private EnumerationOpenNode oEnum = null;
	private ExtensionPointNode ep = null;
	private BusinessObjectNode sbo = null;
	private BusinessObjectNode nbo = null;
	private CoreObjectNode core2 = null;
	private LibraryNode minorLibrary = null;
	private LibraryNode patchLibrary = null;
	private CoreObjectNode mCo = null;
	private ExtensionPointNode ePatch = null;

	private LibraryNode providerLib;

	@Override
	public RepositoryNode getRepositoryForTest() {
		for (RepositoryNode rn : rc.getAll()) {
			if (rn.isRemote()) {
				return rn;
			}
		}
		throw new IllegalStateException("Missing remote repository. Check your configuration.");
	}

	@Before
	public void runBeforeEachTest() throws LibrarySaveException, RepositoryException {
		LOGGER.debug("Before test.");
		xsdString = NodeFinders.findNodeByName("string", Node.XSD_NAMESPACE);
		uploadProject = createProject("ToUploadLibrary", getRepositoryForTest(), "Test");

		lib1 = LibraryNodeBuilder.create("TestLibrary1", getRepositoryForTest().getNamespace() + "/Test/NS1",
				"prefix1", new Version(1, 0, 0)).build(uploadProject, pc);

		lib2 = LibraryNodeBuilder.create("TestLibrary2", getRepositoryForTest().getNamespace() + "/Test/NS2",
				"prefix2", new Version(1, 0, 0)).build(uploadProject, pc);

		chain1 = rc.manage(getRepositoryForTest(), Collections.singletonList(lib1)).get(0);
		chain2 = rc.manage(getRepositoryForTest(), Collections.singletonList(lib2)).get(0);
		boolean locked = rc.lock(chain1.getHead());
		locked = rc.lock(chain2.getHead());

		assertTrue("Repository controller must not be null.", rc != null);
		assertTrue("Repository must be available.", getRepositoryForTest() != null);
		assertTrue("Project must not be null.", uploadProject != null);
		assertTrue("MockLibrary must not be null.", ml != null);
		assertTrue("xsdString must not be null.", xsdString != null);
		assertTrue("lib1 must not be null.", lib1 != null);
		assertTrue("lib1 must be editable.", lib1.isEditable());
		assertTrue("lib2 must not be null. ", lib2 != null);
		assertTrue("lib2 must be editable.", lib2.isEditable());
		assertTrue("chain must be locked.", locked);
		assertTrue("chain must be MANAGED_WIP.", RepositoryItemState.MANAGED_WIP == chain2.getHead().getProjectItem()
				.getState());

		// Use repository controller to version the libraries.
		// patchLibrary = rc.createPatchVersion(chain.getHead());
		// minorLibrary = rc.createMinorVersion(chain.getHead());
		// LibraryNode major2 = rc.createMajorVersion(chain.getHead());

		LOGGER.debug("Before tests done.");
	}

	@Test
	public void updateVersionTest() throws RepositoryException {
		// Create two libraries where one uses types from the other then version the type provider
		LibraryNode userLib = lib1;
		providerLib = lib2;

		ml.addOneOfEach(userLib, "User");
		SimpleTypeNode simpleType = ml.addSimpleTypeToLibrary(providerLib, "simpleType");
		for (TypeUser user : userLib.getDescendants_TypeUsers())
			user.setAssignedType(simpleType);

		providerLib = rc.createMajorVersion(providerLib);
		assertTrue("Must have major version of provider library.", providerLib != null);
		assertTrue("Must have type providers", !providerLib.getDescendants_TypeProviders().isEmpty());
		assertTrue("Must be new library.", providerLib != lib2);
		// As a major library, providerLib is now in its own chain.
		assertTrue("Major versions must be head of chain.", providerLib == providerLib.getChain().getHead());

		// // Walk selected library type users and collect all used libraries (type assignments and extensions)
		HashMap<String, LibraryNode> usedLibs = new HashMap<String, LibraryNode>();
		for (TypeUser user : userLib.getDescendants_TypeUsers()) {
			TypeProvider provider = user.getAssignedType();
			if (provider.getLibrary() != null)
				if (provider.getLibrary().getChain() != null)
					usedLibs.put(provider.getLibrary().getNamespace(), provider.getLibrary());
		}
		assertTrue("Must have at least one used library.", usedLibs.size() > 0);

		// Create replacement map
		HashMap<LibraryNode, LibraryNode> replacementMap = buildUpdateMap(getRepositoryForTest());

		// replace type users using the replacement map
		userLib.replaceTypeUsers(replacementMap);

		// Make sure it worked
		for (TypeUser user : userLib.getDescendants_TypeUsers()) {
			if (!(user.getAssignedType() instanceof ImpliedNode)) {
				if (user.getAssignedType().getLibrary() != providerLib)
					LOGGER.debug("Error - " + user + " assigned type is in wrong library: "
							+ ((Node) user.getAssignedType()).getNameWithPrefix());
				assertTrue("Must be in providerLib.", user.getAssignedType().getLibrary() == providerLib);
			}
		}

	}

	/**
	 * For each item in the map parameter find the latest version. If different, add item and latest version to returned
	 * map.
	 * 
	 * WARNING: this is very slow
	 * 
	 * @param usedLibs
	 * @return
	 * @throws RepositoryException
	 */
	private HashMap<LibraryNode, LibraryNode> buildUpdateMap(RepositoryNode repoNode) throws RepositoryException {
		HashMap<LibraryNode, LibraryNode> replacementMap = new HashMap<>();

		// get a map of all repository item nodes that have a later version and their later version repository item
		Repository tlRepo = repoNode.getRepository();
		HashMap<RepositoryItem, RepositoryItem> itemMap = new HashMap<>();

		// For each item from the GUI repository find its latest version.
		for (Node rn : repoNode.getDescendents_RepositoryItems()) {
			if (!(rn instanceof RepositoryItemNode))
				continue;
			RepositoryItemNode ri = ((RepositoryItemNode) rn);
			List<RepositoryItem> latestList = tlRepo.listItems(ri.getItem().getBaseNamespace(), true, true);
			if (latestList.size() > 0) {
				RepositoryItem latest = latestList.get(0);
				if (latest != null && !latest.getNamespace().equals(ri.getItem().getNamespace()))
					itemMap.put(((RepositoryItemNode) ri).getItem(), latestList.get(0));
			}
		}

		// Create library map of namespace to library node for all libraries open in the GUI
		HashMap<String, LibraryNode> libraryMap = new HashMap<>();
		for (LibraryNode lib : ModelNode.getAllUserLibraries())
			libraryMap.put(lib.getNamespace(), lib);

		// Now map the repository items to actual libraries.
		for (Entry<RepositoryItem, RepositoryItem> entry : itemMap.entrySet()) {
			String entryNS = entry.getKey().getNamespace();
			String latestNS = entry.getValue().getNamespace();
			if (libraryMap.containsKey(entryNS))
				replacementMap.put(libraryMap.get(entryNS), libraryMap.get(latestNS));
			else
				LOGGER.debug(entryNS + " has later version but is not open in GUI.");
		}

		// Print out replacement map
		for (Entry<LibraryNode, LibraryNode> entry : replacementMap.entrySet())
			LOGGER.debug("Replace " + entry.getKey() + " with " + entry.getValue());

		return replacementMap;

	}

	// private List<Node> getDescendents_Libraries(RepositoryNode rn) {
	// List<Node> rnKids = new ArrayList<Node>();
	// for (Node rnChild : rn.getChildren())
	// rnKids.addAll(getDescendents_Libraries(rnChild));
	// return rnKids;
	// }
	//
	// private List<Node> getDescendents_Libraries(Node n) {
	// List<Node> kids = new ArrayList<Node>();
	// for (Node m : n.getChildren())
	// if (m instanceof RepositoryItemNode)
	// kids.add(m);
	// else if (m.hasChildren())
	// kids.addAll(getDescendents_Libraries(m));
	// return kids;
	// }

	//
	// Test the library level testers.
	//
	// @Test
	// public void testLibraryTesters() {
	// checkLibraryStatus();
	// checkObjectStatus();
	//
	// // Run all tests from here to reduce set up time.
	// specMinor(); // run before spec Major
	// checkNavChildren();
	//
	// // Check specifications - leaves chain untouched.
	// // patch tests done while patch library was editable - specPatch();
	// specMajor();
	//
	// // adhoc tests
	// assertTrue(TotalLibraries == chain.getLibraries().size());
	// assertTrue(chain.getDescendants_NamedTypes().contains(bo));
	// assertTrue(majorLibrary.getDescendants_NamedTypes().contains(bo));
	//
	// // verify the core object, co, created in the minor library contains properties from the
	// // extension point in the patch.
	// Assert.assertNotNull(mCo);
	// Assert.assertEquals(1, mCo.getSummaryFacet().getChildren().size());
	//
	// // Assert.assertNotNull(mCo.getExtensionBase());
	//
	// // testAddNodeHandler();
	// // testFacets();
	// // testMove();
	// // testCopying();
	// // testAddingAndDeleting();
	// // // testAddingProperties(nco);
	// // testDelete();
	// }
	//
	// private void checkLibraryStatus() {
	// //
	// // Chain assertions
	// Assert.assertTrue(chain.isMinor());
	// Assert.assertFalse(chain.isMajor());
	// Assert.assertFalse(chain.isPatch());
	//
	// //
	// // Major Library
	// Assert.assertTrue(majorLibrary.isInChain());
	// Assert.assertTrue(majorLibrary.isManaged());
	// Assert.assertTrue(majorLibrary.isReadyToVersion());
	// Assert.assertTrue(majorLibrary.isMajorVersion());
	// Assert.assertTrue(majorLibrary.isMinorOrMajorVersion());
	// //
	// Assert.assertFalse(majorLibrary.isEditable());
	// Assert.assertFalse(majorLibrary.isLocked());
	// Assert.assertFalse(majorLibrary.isPatchVersion());
	// Assert.assertFalse(majorLibrary.isMinorVersion());
	// Assert.assertFalse(chain.isLaterVersion(majorLibrary, minorLibrary));
	// Assert.assertFalse(chain.isLaterVersion(majorLibrary, patchLibrary));
	//
	// //
	// // Minor Library
	// Assert.assertTrue(minorLibrary.isInChain());
	// Assert.assertTrue(minorLibrary.isManaged());
	// Assert.assertTrue(minorLibrary.isReadyToVersion());
	// Assert.assertTrue(minorLibrary.isEditable());
	// Assert.assertTrue(minorLibrary.isLocked());
	// Assert.assertTrue(minorLibrary.isMinorOrMajorVersion());
	// Assert.assertTrue(minorLibrary.isMinorVersion());
	// Assert.assertTrue(chain.isLaterVersion(minorLibrary, majorLibrary));
	// Assert.assertTrue(chain.isLaterVersion(minorLibrary, patchLibrary));
	// //
	// Assert.assertFalse(minorLibrary.isMajorVersion());
	// Assert.assertFalse(minorLibrary.isPatchVersion());
	//
	// //
	// // patch Library
	// Assert.assertTrue(patchLibrary.isInChain());
	// Assert.assertTrue(patchLibrary.isManaged());
	// Assert.assertTrue(patchLibrary.isReadyToVersion());
	// Assert.assertTrue(patchLibrary.isPatchVersion());
	// Assert.assertTrue(chain.isLaterVersion(patchLibrary, majorLibrary));
	// //
	// Assert.assertFalse(patchLibrary.isMajorVersion());
	// Assert.assertFalse(patchLibrary.isEditable());
	// Assert.assertFalse(patchLibrary.isLocked());
	// Assert.assertFalse(patchLibrary.isMinorVersion());
	// Assert.assertFalse(patchLibrary.isMinorOrMajorVersion());
	// Assert.assertFalse(chain.isLaterVersion(patchLibrary, minorLibrary));
	//
	// // LibraryNode based status
	// Assert.assertEquals(NodeEditStatus.MANAGED_READONLY, majorLibrary.getEditStatus());
	// Assert.assertEquals(NodeEditStatus.MINOR, minorLibrary.getEditStatus());
	// Assert.assertEquals(NodeEditStatus.MANAGED_READONLY, patchLibrary.getEditStatus());
	// Assert.assertEquals(NodeEditStatus.FULL, secondLib.getEditStatus());
	//
	// }
	//
	// private void checkObjectStatus() {
	// // Node based NodeEditStatus is based on chain head
	// Assert.assertEquals(NodeEditStatus.MINOR, co.getEditStatus());
	// Assert.assertEquals(NodeEditStatus.MINOR, bo.getEditStatus());
	//
	// //
	// Assert.assertFalse(co.isInHead());
	// Assert.assertFalse(bo.isInHead());
	// Assert.assertFalse(vwa.isInHead());
	// Assert.assertFalse(co.isEditable_newToChain());
	// Assert.assertFalse(bo.isEditable_newToChain());
	// Assert.assertFalse(vwa.isEditable_newToChain());
	//
	// //
	// // Tests used to enable user actions.
	//
	// // Editable - should all be true to drive the navView display.
	// Assert.assertTrue(co.isEditable());
	// Assert.assertTrue(bo.isEditable());
	//
	// // Delete-able - NodeTester.canDelete() -> Node.isDeletable()
	// Assert.assertFalse(co.isDeleteable());
	// Assert.assertFalse(bo.isDeleteable());
	// // Assert.assertTrue(sbo.isDeleteable());
	//
	// // CanAdd - Control for AddNodeHandler. GlobalSelectionTester.canAdd()
	// GlobalSelectionTester gst = new GlobalSelectionTester();
	// Assert.assertTrue(gst.test(co, GlobalSelectionTester.CANADD, null, null));
	// Assert.assertTrue(gst.test(bo, GlobalSelectionTester.CANADD, null, null));
	// Assert.assertTrue(gst.test(vwa, GlobalSelectionTester.CANADD, null, null));
	// Assert.assertFalse(gst.test(ep, GlobalSelectionTester.CANADD, null, null));
	//
	// // New Component - NodeTester
	// NodeTester tester = new NodeTester();
	// Assert.assertTrue(tester.test(co, NodeTester.IS_IN_TLLIBRARY, null, null));
	// Assert.assertTrue(tester.test(co, NodeTester.IS_OWNER_LIBRARY_EDITABLE, null, null));
	// Assert.assertTrue(tester.test(bo, NodeTester.IS_IN_TLLIBRARY, null, null));
	// Assert.assertTrue(tester.test(bo, NodeTester.IS_OWNER_LIBRARY_EDITABLE, null, null));
	//
	// // Move - NavigatorMenus.createMoveActionsForLibraries()
	// Assert.assertFalse(co.getLibrary().isMoveable());
	// Assert.assertFalse(bo.getLibrary().isMoveable());
	//
	// }
	//
	// private void testAddNodeHandler() {
	// // Add node handler test
	// CoreObjectNode nco = createCoreInMinor(); // extends co
	// // Node object based status
	// NodeTester tester = new NodeTester();
	// GlobalSelectionTester gst = new GlobalSelectionTester();
	// Assert.assertEquals(NodeEditStatus.MINOR, nco.getEditStatus());
	// assertTrue(nco.isInHead());
	// assertTrue(!nco.isEditable_newToChain());
	// assertTrue(nco.isEditable());
	// assertTrue(nco.isDeleteable());
	// assertTrue(nco.getLibrary().isMoveable());
	// assertTrue(gst.test(nco, GlobalSelectionTester.CANADD, null, null));
	// assertTrue(tester.test(nco, NodeTester.IS_IN_TLLIBRARY, null, null));
	// assertTrue(tester.test(nco, NodeTester.IS_OWNER_LIBRARY_EDITABLE, null, null));
	// checkHeirarchy(nco);
	//
	// }
	//
	// // From OTM-DE Reference-Language Specification - Major Versions
	// public void specMajor() {
	// Assert.assertTrue(chain.isEditable());
	// // List<Node> chainNodes = chain.getDescendants_NamedTypes();
	//
	// // Create major version which makes the minor final.
	// LibraryNode newMajor = rc.createMajorVersion(chain.getHead());
	//
	// // List<Node> newNodes = newMajor.getDescendants_NamedTypes();
	// assertTrue(newMajor.isEditable());
	// assertTrue(!chain.isEditable());
	//
	// // 1. If a prior version of the term existed, its content and structure can be modified in any way
	// // TEST - in a new major, verify named objects exist in both chains are both chains are valid.
	// // TEST - rename, delete and add properties to rolled up major objects
	// for (Node n : chain.getDescendants_NamedTypes()) {
	// if (n instanceof ExtensionPointNode)
	// continue; // would be rolled up
	// Node mn = newMajor.findNodeByName(n.getName());
	// if (mn == null)
	// continue;
	// assertTrue(mn != null);
	// }
	// ArrayList<Node> nodes = new ArrayList<Node>(newMajor.getDescendants_NamedTypes());
	// for (Node n : nodes) {
	// n.setName(n.getName() + "_TEST");
	// // TODO - modify properties
	// Assert.assertTrue(n.isDeleteable());
	// n.delete();
	// }
	//
	// // 2. Any new term can be defined
	// // TEST - create all new object types
	// addNamedObjects(newMajor);
	// Assert.assertEquals(TotalNamedObjects, newMajor.getDescendants_NamedTypes().size());
	//
	// // verify ability to add doc to all simple types
	// for (Node n : newMajor.getSimpleRoot().getChildren())
	// if (n instanceof SimpleTypeNode)
	// addAndRemoveDoc((SimpleTypeNode) n);
	//
	// checkValid(newMajor);
	// newMajor.delete();
	//
	// // Make sure nothing changed in the base chain
	// checkValid(chain);
	// checkCounts(chain);
	// }
	//
	// // From OTM-DE Reference-Language Specification
	// // Minor Versions
	// // 1. Any new term can be defined
	// // 2. Existing versioned terms (see section 11.3) can only be modified by adding indicators, optional attributes,
	// or
	// // optional element declarations
	// // 3. Non-versioned terms cannot be modified in a minor version library
	//
	// // TEST - limited modification of existing objects (VWA,Core Object,Business Object,Operation)
	// // ---- 1. The terms must be of the same type (business object, core, etc.) and have the same name
	// // ---- 2. The terms MUST be declared in different libraries, and both libraries must have the same name, version
	// // scheme,
	// // ---- and base namespace URI
	// // ---- 3. The version of the extended term’s library MUST be lower than that of the extending term’s library
	// // version,
	// // ---- but both libraries MUST belong to the same major version chain
	// // TEST - non-versioned objects: no editing of properties, names or assigned types
	// // TEST - all new objects can be added (different names)
	//
	// // @Test
	// public void specMinor() {
	// // Minor library is head of chain and editable
	// assertTrue(chain.isEditable());
	// assertTrue(chain.getHead() == minorLibrary);
	// assertTrue(minorLibrary.isEditable());
	// String ctx = minorLibrary.getDefaultContextId();
	// assertTrue(!minorLibrary.getDefaultContextId().isEmpty());
	//
	// //
	// // Try all the things we are allowed to do in a minor
	// //
	// // Ensure it creates a BO in the minor and it extends the original BO
	// assertTrue(!minorLibrary.getDescendants_NamedTypes().contains(bo));
	// assertTrue(bo.isEditable()); // is the chain that contains it editable?
	//
	// BusinessObjectNode boInMinor = (BusinessObjectNode) bo.createMinorVersionComponent();
	// TotalDescendents++;
	// assertTrue(boInMinor.isEditable());
	// assertTrue(!boInMinor.isEditable_newToChain());
	// Assert.assertEquals(NodeEditStatus.MINOR, boInMinor.getEditStatus());
	// assertTrue(minorLibrary.getDescendants_NamedTypes().contains(boInMinor));
	//
	// // change a pre-existing old property
	// PropertyNode oldProperty = (PropertyNode) bo.getSummaryFacet().getChildren().get(0);
	// assertTrue(oldProperty != null);
	// addAndRemoveDoc(oldProperty);
	//
	// // Add a new property
	// ElementNode newProperty = new ElementNode(boInMinor.getSummaryFacet(), "testProp");
	// assertTrue(newProperty != null);
	// assertTrue(newProperty.isEditable_newToChain());
	// addAndRemoveDoc(newProperty);
	//
	// // Add a new business object to the minor
	// String newBoName = "TestMinorBO";
	// BusinessObjectNode newBO = new BusinessObjectNode(new TLBusinessObject());
	// newBO.setName(newBoName);
	// minorLibrary.addMember(newBO);
	// ElementNode idProperty = new ElementNode(newBO.getIDFacet(), "id");
	// TotalDescendents++;
	// AggregateComplex++;
	//
	// assertTrue(newBO.getName().equals(newBoName));
	// assertTrue(newBO.isEditable_newToChain());
	// assertTrue(idProperty.isEditable_newToChain());
	// addAndRemoveDoc(idProperty);
	// assertTrue(!idProperty.isMandatory());
	// idProperty.setMandatory(true); // should work
	// assertTrue("Must set mandatory on object new to the minor.", idProperty.isMandatory());
	// Assert.assertEquals(NodeEditStatus.MINOR, newBO.getEditStatus());
	//
	// //
	// // Make sure other actions are prohibited.
	// //
	// assertTrue(!newProperty.isMandatory());
	// newProperty.setMandatory(true); // ignored because owning component is not new to chain.
	// assertTrue("Must NOT be allowed.", !newProperty.isMandatory());
	// assertTrue(!oldProperty.isMandatory());
	// oldProperty.setMandatory(true); // should do nothing
	// assertTrue("Must not be allowed.", !oldProperty.isMandatory());
	//
	// AliasNode alias = new AliasNode(boInMinor, "testAlias");
	// assertTrue(alias != null);
	//
	// checkValid(chain);
	// checkCounts(chain);
	//
	// }
	//
	// //
	// // Test the children/parent relationships
	// //
	// // @Test
	// public void checkHeirarchy(CoreObjectNode nco) {
	// if (nco == null)
	// nco = createCoreInMinor();
	// Assert.assertTrue(nco.getParent() instanceof VersionNode);
	// Assert.assertTrue(nco.getLibrary() == minorLibrary);
	// // TODO - why? if they are always the same, why have version node pointer? Just to save a
	// // cast? If so, create a method w/ cast and remove data
	// Assert.assertTrue(nco.getParent() == nco.getVersionNode());
	//
	// // check head and prev
	// Assert.assertTrue(nco.getVersionNode().getNewestVersion() == nco);
	// Assert.assertTrue(co.getVersionNode().getNewestVersion() == nco);
	// Assert.assertTrue(nco.getVersionNode().getPreviousVersion() == co);
	// Assert.assertTrue(co.getVersionNode().getPreviousVersion() == null);
	//
	// Node head = chain.getHead();
	// Assert.assertTrue(head instanceof LibraryNode);
	// Assert.assertTrue(head.getParent().getParent() == chain);
	// Assert.assertTrue(head.getChain() == chain);
	//
	// // Make sure all versions are present.
	// Node versionsAgg = chain.getVersions();
	// Assert.assertTrue(versionsAgg instanceof VersionAggregateNode);
	// Assert.assertEquals(TotalLibraries, versionsAgg.getChildren().size());
	// Assert.assertEquals(TotalLibraries, versionsAgg.getNavChildren().size());
	// Assert.assertTrue(versionsAgg.getParent() == chain);
	// Assert.assertTrue(versionsAgg.getChain() == chain);
	//
	// for (Node lib : versionsAgg.getChildren()) {
	// Assert.assertTrue(lib.getParent() == versionsAgg);
	// Assert.assertTrue(lib instanceof LibraryNode);
	// if (lib == chain.getHead())
	// Assert.assertTrue(lib.isEditable());
	// else
	// Assert.assertFalse(lib.isEditable());
	// // Either nav or service nodes.
	// checkChildrenClassType(lib, NavNode.class, ServiceNode.class);
	//
	// // Check the children of the Nav Nodes and Service Node
	// for (Node nn : lib.getChildren()) {
	// if (nn instanceof NavNode) {
	// // Nav node children must be version nodes.
	// Assert.assertTrue(nn.getParent() == lib);
	// checkChildrenClassType(nn, VersionNode.class, null);
	// for (Node vn : nn.getChildren()) {
	// // Version nodes wrap their one child
	// Assert.assertTrue(vn.getParent() == nn);
	// Assert.assertEquals(1, vn.getChildren().size());
	// checkChildrenClassType(vn, ComponentNode.class, null);
	// for (Node cc : vn.getChildren()) {
	// // Check the actual component nodes.
	// Assert.assertTrue(cc.getParent() == vn);
	// }
	// }
	// } else {
	// // FIXME - operations in the library should be wrapped.
	// checkChildrenClassType(nn, OperationNode.class, null);
	// }
	// }
	// }
	//
	// // Check the aggregates
	// Node complexAgg = (Node) chain.getComplexAggregate();
	// Assert.assertTrue(complexAgg.getParent() == chain);
	// checkChildrenClassType(complexAgg, ComplexComponentInterface.class, AggregateFamilyNode.class);
	// for (Node n : complexAgg.getChildren()) {
	// if (!(n instanceof AggregateFamilyNode)) {
	// Assert.assertTrue(n.getParent() != complexAgg);
	// }
	// }
	//
	// Node simpleAgg = (Node) chain.getSimpleAggregate();
	// Assert.assertTrue(simpleAgg.getParent() == chain);
	// checkChildrenClassType(simpleAgg, SimpleComponentInterface.class, null);
	// for (Node n : simpleAgg.getChildren())
	// Assert.assertTrue(n.getParent() != simpleAgg);
	//
	// Node svcAgg = (Node) chain.getServiceAggregate();
	// Assert.assertTrue(svcAgg.getParent() == chain);
	//
	// }
	//
	// private void checkChildrenClassType(Node parent, Class<?> c, Class<?> c2) {
	// for (Node n : parent.getChildren()) {
	// // n instanceof c.class
	// if (c2 != null)
	// Assert.assertTrue(c.isAssignableFrom(n.getClass()) || c2.isAssignableFrom(n.getClass()));
	// else
	// Assert.assertTrue(c.isAssignableFrom(n.getClass()));
	// }
	// }
	//
	// //
	// // test adding custom facets
	// //
	// // @Test
	// public void testFacets() {
	// int facetCount = bo.getChildren().size();
	// bo.addFacet("custom1", TLFacetType.CUSTOM);
	// // Adding to bo should fail...in the future it might create a new bo and add it to that.
	// Assert.assertEquals(facetCount, bo.getChildren().size());
	//
	// // test adding to a new minor version component
	// nbo = (BusinessObjectNode) bo.createMinorVersionComponent();
	// MinorComplex += 1;
	// nbo.isInHead();
	// // We should get at least the default context.
	// List<String> contextIDs = minorLibrary.getContextIds();
	// Assert.assertFalse(contextIDs.isEmpty());
	//
	// // Add a custom facet
	// // FIXME - not allowed!! Should it be?
	// // nbo.addFacet("c2", contextIDs.get(0), TLFacetType.CUSTOM);
	// // Assert.assertEquals(4, nbo.getChildren().size());
	// // minorComple + 1 (the inherited simple created by roll-up
	// // Assert.assertEquals(MinorComplex + 1, minorLibrary.getDescendants_NamedTypes().size());
	//
	// Assert.assertTrue(chain.isValid());
	// nbo.delete();
	// MinorComplex -= 1;
	// checkCounts(chain);
	// }
	//
	// // @Test
	// public void checkNavChildren() {
	// // Chain should have 5 (include resources)
	// Assert.assertEquals(5, chain.getNavChildren().size());
	// checkChildrenClassType(chain, AggregateNode.class, null);
	//
	// // VersionAggregate should have 3, one for each library
	// Assert.assertEquals(3, chain.getVersions().getNavChildren().size());
	//
	// // Libraries should have 2 or 3, simple, complex and service
	// // Libraries that are not at the head will return empty list.
	// Assert.assertEquals(0, patchLibrary.getNavChildren().size());
	// Assert.assertEquals(0, minorLibrary.getNavChildren().size());
	// Assert.assertEquals(0, majorLibrary.getNavChildren().size());
	//
	// // Nav Nodes should ONLY have version -- NEVER GOES INTO LOOP
	// for (Node nn : patchLibrary.getNavChildren()) {
	// Assert.assertTrue(nn instanceof NavNode);
	// checkChildrenClassType(nn, VersionNode.class, null);
	// // Version nodes should have NO nav children.
	// for (Node vn : nn.getNavChildren())
	// Assert.assertEquals(0, vn.getNavChildren().size());
	// }
	//
	// // Aggregates should have Active Simple, Active Complex and Service.
	// // This checks both children and then navChildren.
	// checkChildrenClassType(((Node) chain.getComplexAggregate()), ComplexComponentInterface.class,
	// AggregateFamilyNode.class);
	// for (Node nc : ((Node) chain.getComplexAggregate()).getNavChildren()) {
	// Assert.assertTrue(nc instanceof ComplexComponentInterface);
	// }
	// checkChildrenClassType(((Node) chain.getSimpleAggregate()), SimpleComponentInterface.class, null);
	// for (Node nc : ((Node) chain.getSimpleAggregate()).getNavChildren()) {
	// Assert.assertTrue(nc instanceof SimpleComponentInterface);
	// }
	//
	// }
	//
	// //
	// // Test handling of adding and deleting of new objects
	// //
	// // @Test
	// public void testAddingAndDeleting() {
	// // FIXME - minor is not editable
	// // nbo = ml.addBusinessObjectToLibrary(minorLibrary, "nbo");
	// //
	// // // The new bo should be in the minor library, not the base library.
	// // Assert.assertTrue(chain.getDescendants_NamedTypes().contains(nbo));
	// // Assert.assertTrue(minorLibrary.getDescendants_NamedTypes().contains(nbo));
	// // Assert.assertFalse(majorLibrary.getDescendants_NamedTypes().contains(nbo));
	// //
	// // // Add some other object types
	// // EnumerationClosedNode nec = ml.addClosedEnumToLibrary(chain.getHead(), "ce2");
	// // VWA_Node nvwa = ml.addVWA_ToLibrary(chain.getHead(), "vwa2");
	// // Assert.assertTrue(chain.isValid());
	// //
	// // Assert.assertTrue(nec.isNewToChain());
	// // Assert.assertTrue(nvwa.isNewToChain());
	// // Assert.assertTrue(nbo.isNewToChain()); // this object is in previous version.
	// //
	// // // Remove and delete them
	// // nbo.delete();
	// // nec.delete();
	// // nvwa.delete();
	// // Assert.assertTrue(chain.isValid());
	// // checkCounts(chain);
	// }
	//
	// // TODO - test adding more minor chains and adding objects to all of them to verify the prev
	// // link.
	//
	// // @Test
	// public void testAddingProperties(CoreObjectNode nco) {
	// // FIXME - it has 2 children
	// // Assert.assertEquals(1, co.getSummaryFacet().getChildren().size());
	// //
	// // if (nco == null)
	// // nco = createCoreInMinor();
	// // testAddingPropertiesToFacet(nco.getSummaryFacet());
	// //
	// // BusinessObjectNode nbo = createBO_InMinor();
	// // testAddingPropertiesToFacet(nbo.getDetailFacet());
	// //
	// // VWA_Node nVwa = createVWA_InMinor();
	// // testAddingPropertiesToFacet(nVwa.getAttributeFacet());
	// //
	// // Assert.assertEquals(1, co.getSummaryFacet().getChildren().size());
	// // Assert.assertTrue(chain.isValid());
	//
	// // nco.delete();
	// // nbo.delete();
	// // TotalDescendents -= 2;
	// // MinorComplex -= 0; // keep counts accurate
	// // // Active complex should remain unchanged.
	// // checkCounts(chain);
	// }
	//
	// // Adds the removed properties from the facet.
	// // Emulate AddNodeHandler and newPropertiesWizard
	// private void testAddingPropertiesToFacet(ComponentNode propOwner) {
	// int cnt = propOwner.getChildren().size();
	// PropertyNode newProp = null;
	// if (!propOwner.isVWA_AttributeFacet()) {
	// newProp = new ElementNode(new FacetNode(), "np" + cnt++);
	// propOwner.addProperty(newProp);
	// newProp.setAssignedType((TypeProvider) xsdStringNode);
	// Assert.assertTrue(newProp.getLibrary() != null);
	// Assert.assertTrue(newProp.isDeleteable());
	// newProp.delete();
	// Assert.assertEquals(--cnt, propOwner.getChildren().size());
	// }
	// PropertyNode newAttr = new AttributeNode(new FacetNode(), "np" + cnt++);
	// propOwner.addProperty(newAttr);
	// newAttr.setAssignedType((TypeProvider) xsdStringNode);
	// Assert.assertEquals(cnt, propOwner.getChildren().size());
	// }
	//
	// /**
	// * Create a new minor version of core: co. Add a property "te2".
	// *
	// * Emulates behavior in AddNodeHandler. AddNodeHandler notifies the user then createMinorVersionComponent().
	// * Constructors can not do this because they are needed for initial rendering of the objects and can't do user
	// * dialogs/notifications.
	// */
	// private CoreObjectNode createCoreInMinor() {
	// CoreObjectNode nco = (CoreObjectNode) co.createMinorVersionComponent();
	// PropertyNode newProp = new ElementNode(nco.getSummaryFacet(), "te2");
	// newProp.setAssignedType((TypeProvider) NodeFinders.findNodeByName("string", Node.XSD_NAMESPACE));
	//
	// Assert.assertEquals(1, co.getSummaryFacet().getChildren().size());
	// TotalDescendents += 1;
	// MinorComplex += 1;
	//
	// // Make sure a new CO was created in the newMinor library.
	// Assert.assertNotNull(nco);
	//
	// // TODO - why does creating a second version end up as not versioned?
	// if (nco.getVersionNode().getParent() instanceof FamilyNode)
	// assertTrue(nco.isEditable_newToChain()); // Why?
	// else {
	// Assert.assertFalse(nco.isEditable_newToChain()); // not new because it's base type exists in previous chain
	// Assert.assertNotNull(nco.getVersionNode().getPreviousVersion());
	// Assert.assertEquals(nco.getVersionNode().getPreviousVersion(), co);
	// }
	// Assert.assertFalse(co.isEditable_newToChain());
	// Assert.assertEquals(1, nco.getSummaryFacet().getChildren().size());
	// Assert.assertTrue(chain.getDescendants_NamedTypes().contains(nco));
	// Assert.assertTrue(minorLibrary.getDescendants_NamedTypes().contains(nco));
	// Assert.assertFalse(majorLibrary.getDescendants_NamedTypes().contains(nco));
	//
	// return nco;
	// }
	//
	// private BusinessObjectNode createBO_InMinor() {
	// BusinessObjectNode nbo = (BusinessObjectNode) bo.createMinorVersionComponent();
	// PropertyNode newProp = new ElementNode(nbo.getSummaryFacet(), "te2");
	// newProp.setAssignedType((TypeProvider) NodeFinders.findNodeByName("string", Node.XSD_NAMESPACE));
	// Assert.assertEquals(1, bo.getSummaryFacet().getChildren().size());
	// TotalDescendents += 1;
	// MinorComplex += 1;
	//
	// // Make sure a new CO was created in the newMinor library.
	// Assert.assertNotNull(nbo);
	// Assert.assertNotNull(nbo.getVersionNode().getPreviousVersion());
	// Assert.assertEquals(1, nbo.getSummaryFacet().getChildren().size());
	// Assert.assertTrue(chain.getDescendants_NamedTypes().contains(nbo));
	// Assert.assertTrue(minorLibrary.getDescendants_NamedTypes().contains(nbo));
	// Assert.assertFalse(majorLibrary.getDescendants_NamedTypes().contains(nbo));
	//
	// return nbo;
	// }
	//
	// private VWA_Node createVWA_InMinor() {
	// VWA_Node nVwa = (VWA_Node) vwa.createMinorVersionComponent();
	// Assert.assertNotNull(nVwa);
	// PropertyNode newProp = new AttributeNode(nVwa.getAttributeFacet(), "te2");
	// newProp.setAssignedType((TypeProvider) NodeFinders.findNodeByName("string", Node.XSD_NAMESPACE));
	// Assert.assertEquals(1, bo.getSummaryFacet().getChildren().size());
	// TotalDescendents += 1;
	// MinorComplex += 1;
	//
	// // Make sure a new was created in the newMinor library.
	// Assert.assertNotNull(nVwa);
	// Assert.assertEquals(1, nVwa.getAttributeFacet().getChildren().size());
	// Assert.assertTrue(chain.getDescendants_NamedTypes().contains(nVwa));
	// Assert.assertTrue(minorLibrary.getDescendants_NamedTypes().contains(nVwa));
	// Assert.assertFalse(majorLibrary.getDescendants_NamedTypes().contains(nVwa));
	//
	// return nVwa;
	// }
	//
	// // @Test
	// public void testCopying() {
	// // FIXME - failed to delete the family member
	// // if (bo.getLibrary().isEditable()) {
	// // nbo = (BusinessObjectNode) bo.clone("_copy");
	// // // copy should be in the new library
	// // Assert.assertTrue(chain.getDescendants_NamedTypes().contains(nbo));
	// // Assert.assertTrue(minorLibrary.getDescendants_NamedTypes().contains(nbo));
	// // Assert.assertFalse(majorLibrary.getDescendants_NamedTypes().contains(nbo));
	// // ValidationFindings findings = chain.validate();
	// // Assert.assertTrue(chain.isValid());
	// // nbo.delete();
	// // checkCounts(chain);
	// // } else
	// // System.out.println("ERROR - testCopying using non-editable library.");
	// }
	//
	// /**
	// * From MoveObjectToLibraryAction if (source.isInTLLibrary() && source.isTopLevelObject())
	// * source.getLibrary().moveMember(source, destination);
	// *
	// * From MainController.ChangeNode() srcLib.moveMember(editedNode, destLib);
	// */
	// // @Test
	// public void testMove() {
	// // This will work because moveMember is at the model level. It is used by the controller
	// // which applies the business logic if it is valid to move.
	// List<Node> namedTypes = chain.getDescendants_NamedTypes();
	// int namedTypeCnt = chain.getDescendants_NamedTypes().size();
	// LOGGER.debug("testMove " + TotalDescendents + " " + namedTypeCnt + " " + bo.getLibrary());
	//
	// // FIXME -
	// // majorLibrary.moveMember(bo, minorLibrary);
	// // // checkValid(chain);
	// // List<Node> after = chain.getDescendants_NamedTypes();
	// // int afterCnt = chain.getDescendants_NamedTypes().size();
	// // LOGGER.debug("testMove " + TotalDescendents + " " + afterCnt);
	// //
	// // checkCounts(chain);
	// // Assert.assertTrue(chain.getDescendants_NamedTypes().contains(bo));
	// // Assert.assertFalse(majorLibrary.getDescendants_NamedTypes().contains(bo));
	// // Assert.assertTrue(minorLibrary.getDescendants_NamedTypes().contains(bo));
	// // minorLibrary.moveMember(bo, majorLibrary); // put it back
	// // Assert.assertTrue(chain.isValid());
	// // checkCounts(chain);
	// //
	// // // Test moving from another library
	// // nbo = ml.addBusinessObjectToLibrary(secondLib, "secondLibBO");
	// // secondLib.moveMember(nbo, minorLibrary);
	// // Assert.assertTrue(chain.getDescendants_NamedTypes().contains(nbo));
	// // Assert.assertFalse(secondLib.getDescendants_NamedTypes().contains(nbo));
	// // nbo.delete();
	// //
	// // // Test moving to another library
	// // nbo = ml.addBusinessObjectToLibrary(minorLibrary, "newBO");
	// // minorLibrary.moveMember(nbo, secondLib);
	// // Assert.assertTrue(secondLib.getDescendants_NamedTypes().contains(nbo));
	// // Assert.assertFalse(minorLibrary.getDescendants_NamedTypes().contains(nbo));
	// // Assert.assertFalse(majorLibrary.getDescendants_NamedTypes().contains(nbo));
	// // Assert.assertFalse(chain.getDescendants_NamedTypes().contains(nbo));
	// checkCounts(chain);
	//
	// Assert.assertTrue(chain.isValid());
	// }
	//
	// // @Test
	// public void testDelete() {
	// // FIXME - bo was not in before delete
	// // bo.delete(); // Should and does fail.
	// // Assert.assertTrue(majorLibrary.getDescendants_NamedTypes().contains(bo));
	// // List<?> kids = chain.getComplexAggregate().getChildren();
	// // Assert.assertTrue(kids.contains(bo));
	// // checkCounts(chain);
	// // Assert.assertTrue(chain.isValid());
	// //
	// // //
	// // // Test deleting properties
	// // co.getSummaryFacet().getChildren().get(0).delete();
	// // Assert.assertEquals(1, co.getSummaryFacet().getChildren().size());
	// //
	// // //
	// // // Test handling of new object with same name as existing object.
	// // nbo = ml.addBusinessObjectToLibrary(chain.getHead(), "testBO");
	// // Assert.assertFalse(minorLibrary.isValid());
	// //
	// // kids = chain.getComplexAggregate().getChildren();
	// // Assert.assertTrue(kids.contains(nbo));
	// // Assert.assertFalse(kids.contains(bo)); // was replaced with nbo
	// //
	// // // The new bo should be in the minor library, not the base library.
	// // Assert.assertTrue(chain.getDescendants_NamedTypes().contains(nbo));
	// // Assert.assertTrue(minorLibrary.getDescendants_NamedTypes().contains(nbo));
	// // Assert.assertFalse(majorLibrary.getDescendants_NamedTypes().contains(nbo));
	// //
	// // // Deleting via GUI should make it valid and replace the old one back into the aggregate
	// // // model level delete should just do it.
	// // nbo.delete();
	// // Assert.assertTrue(minorLibrary.isValid());
	// // Assert.assertTrue(chain.getDescendants_NamedTypes().contains(bo));
	// // Assert.assertTrue(majorLibrary.getDescendants_NamedTypes().contains(bo));
	// // checkCounts(chain);
	// // // counts will be wrong.
	// //
	// // // Renaming it should make chain valid
	// // nbo = ml.addBusinessObjectToLibrary(minorLibrary, "testBO");
	// // nbo.setName("testBO2");
	// // Assert.assertTrue(minorLibrary.isValid());
	// // Assert.assertTrue(chain.getDescendants_NamedTypes().contains(bo));
	// // Assert.assertTrue(majorLibrary.getDescendants_NamedTypes().contains(bo));
	// // Assert.assertTrue(chain.getDescendants_NamedTypes().contains(nbo));
	// // Assert.assertTrue(minorLibrary.getDescendants_NamedTypes().contains(nbo));
	// }
	//
	// // From OTM-DE Reference-Language Specification
	// // 1. Only new simple types, open/closed enumerations, and extension point facets can be defined
	// // 2. Extension point facets are only allowed to reference (extend) standard or contextual facets declared in a
	// // prior major or minor version of the patch library’s major version chain
	// // Patch Versions
	// public void specPatch() {
	// // int beforeCount = chain.getDescendants_NamedTypes().size();
	// assertTrue(patchLibrary.isEditable());
	//
	// // TEST adding content types
	// // FIXME - compiler validation claims illegal patch
	// // https://github.com/OpenTravel/OTM-DE-Compiler/issues/28
	// //
	// // Add enumerations
	// // EnumerationOpenNode oe = ml.addOpenEnumToLibrary(patchLibrary, "oEnumInPatch");
	// // Add a closed enumeration
	// // SimpleTypeNode ce = ml.addClosedEnumToLibrary(patchLibrary, "cEnumInPatch");
	// // TotalDescendents += 2; // Number in whole chain
	// // AggregateSimple += 1;
	// // AggregateComplex++;
	//
	// // Add a simple type
	// // SimpleTypeNode simple = ml.addSimpleTypeToLibrary(patchLibrary, "simpleInPatch");
	// // TotalDescendents += 1; // Number in whole chain
	// // AggregateSimple += 1;
	//
	// // TEST - adding documentation, examples and equivalents
	// addAndRemoveDoc(simple);
	//
	// // TEST - Extension points, valid and invalid
	// ePatch = new ExtensionPointNode(new TLExtensionPointFacet());
	// patchLibrary.addMember(ePatch);
	// ((ExtensionOwner) ePatch).setExtension((Node) co.getSummaryFacet());
	// ePatch.addProperty(new IndicatorNode(ePatch, "patchInd"));
	//
	// addAndRemoveDoc((PropertyNode) ePatch.getChildren().get(0));
	//
	// TotalDescendents += 1; // Number in whole chain
	// AggregateComplex += 1; // Number in the aggregates
	// PatchTotal += 1;
	//
	// //
	// // Test actions that should be prevented.
	// //
	// assertTrue(!bo.isEditable_newToChain());
	// // adding facets can throw exceptions
	// boolean exception = false;
	// try {
	// bo.addFacet("patchFacetC", TLFacetType.CUSTOM); // throws exception
	// bo.addFacet("patchFacetQ", TLFacetType.QUERY); // throws exception
	// } catch (Exception e) {
	// exception = true;
	// }
	// assertTrue(exception);
	//
	// ((FacetNode) bo.getCustomFacets().get(0)).setContext(bo.getLibrary().getDefaultContextId());
	// assertTrue(co.getRoleFacet().addRole("patchRole") == null);
	//
	// oEnum.addLiteral("patchLiteral");
	// assertTrue(!oEnum.getLiterals().contains("patchLiteral"));
	// assertTrue(!oEnum.getLiterals().contains(""));
	// cEnum.addLiteral("patchLiteral");
	// assertTrue(!cEnum.getLiterals().contains("patchLiteral"));
	//
	// bo.addAlias("patchAlias"); // should not work
	// assertTrue(bo.getAliases().isEmpty());
	//
	// assertTrue(bo.isExtensible());
	// bo.setExtensible(false); // should not work
	// assertTrue(bo.isExtensible());
	//
	// // bo.setName("patchBO");
	// // These should not error but have no effect on the tlObject.
	// int fd = simple.getFractionDigits();
	// simple.setFractionDigits(3);
	// assertTrue(fd == simple.getFractionDigits());
	// simple.setMaxExclusive("3");
	// simple.setMinExclusive("3");
	// simple.setMaxLength(3);
	// simple.setMinLength(3);
	// simple.setMaxInclusive("3");
	// simple.setMinInclusive("3");
	// simple.setPattern("[A-Z]");
	//
	// checkValid(chain);
	// checkCounts(chain);
	// }
	//
	// private void addAndRemoveDoc(SimpleTypeNode simple) {
	// String Text = "SampleText";
	// Assert.assertNotNull(simple);
	// simple.setDescription(Text);
	// simple.setMoreInfo(Text, 0);
	// simple.setExample(Text);
	// simple.setEquivalent(Text);
	//
	// assertTrue(simple.getDescription().equals(Text));
	// assertTrue(simple.getDocumentation().getMoreInfos().get(0).getText().equals(Text));
	// assertTrue(simple.getExample(null).equals(Text));
	// assertTrue(simple.getEquivalent(null).equals(Text));
	//
	// simple.setDescription("");
	// simple.setMoreInfo("", 0);
	// simple.setExample("");
	// simple.setEquivalent("");
	// }
	//
	// private void addAndRemoveDoc(PropertyNode property) {
	// String Text = "SampleText";
	// Assert.assertNotNull(property);
	// property.setDescription(Text);
	// property.setMoreInfo(Text, 0);
	// property.setExample(Text);
	// property.setEquivalent(Text);
	//
	// assertTrue(property.getDescription().equals(Text));
	// assertTrue(property.getDocumentation().getMoreInfos().get(0).getText().equals(Text));
	// if (property.getExampleHandler() != null) {
	// assertTrue(property.getExample(null).equals(Text));
	// assertTrue(property.getEquivalent(null).equals(Text));
	// }
	//
	// property.setDescription("");
	// property.setMoreInfo("", 0);
	// property.setExample("");
	// property.setEquivalent("");
	// }
	//
	// // private void testPatch() {
	// // Adding the simple to the patch causes it to be duplicated then create error finding.
	//
	// // Add a extension point
	// // ePatch = new ExtensionPointNode(new TLExtensionPointFacet());
	// // patchLibrary.addMember(ePatch);
	// // ePatch.setExtendsType(core2.getSummaryFacet());
	// // ePatch.addProperty(new IndicatorNode(ePatch, "patchInd"));
	// // TotalDescendents += 1; // Number in whole chain
	// // AggregateComplex += 1; // Number in the aggregates
	// // PatchTotal += 1;
	// // Assert.assertTrue(chain.isValid());
	// // checkCounts(chain);
	//
	// // FIXME - compiler validation claims illegal patch
	// // https://github.com/OpenTravel/OTM-DE-Compiler/issues/28
	// //
	// // Add an open enumeration
	// // EnumerationOpenNode oe = ml.addOpenEnumToLibrary(patchLibrary, "oEnumInPatch");
	// // Add a closed enumeration
	// // SimpleTypeNode ce = ml.addClosedEnumToLibrary(patchLibrary, "cEnumInPatch");
	// // TotalDescendents += 2; // Number in whole chain
	// // AggregateSimple += 1;
	// // AggregateComplex++;
	//
	// // Add a simple type
	// // SimpleTypeNode simple = ml.addSimpleTypeToLibrary(patchLibrary, "simpleInPatch");
	// // TotalDescendents += 1; // Number in whole chain
	// // AggregateSimple += 1;
	// //
	// // assertNotNull(simple);
	// // assertTrue(patchLibrary.isInChain());
	// // assertTrue(patchLibrary.getChain() == chain);
	// //
	// // ValidationFindings findings = chain.validate();
	// // assertTrue(chain.isValid());
	// //
	// // int afterCount = chain.getDescendants_NamedTypes().size();
	// // checkCounts(chain);
	//
	// // TODO - validate the assertion that get descendants only does head
	// // List<Node> after = chain.getComplexAggregate().getDescendants_NamedTypes();
	// // }
	//
	// // Remember, getDescendents uses HashMap - only unique nodes.
	// private void checkCounts(LibraryChainNode chain) {
	//
	// // Make sure all the base objects are accessible.
	// List<Node> namedTypes = chain.getDescendants_NamedTypes();
	// int namedTypeCnt = chain.getDescendants_NamedTypes().size();
	// Assert.assertEquals(TotalDescendents, namedTypeCnt);
	//
	// // Make sure all the types are in the versions aggregate
	// // Only gets "head" objects in a version chain by using the aggregate.
	// AggregateNode aggregate = (AggregateNode) chain.getComplexAggregate();
	// List<Node> nt = aggregate.getDescendants_NamedTypes();
	// namedTypeCnt = aggregate.getDescendants_NamedTypes().size();
	// Assert.assertEquals(AggregateComplex, namedTypeCnt);
	//
	// // FIXME - should be 8. The patch extension point should not be included because it is
	// // wrapped up into the minor
	// namedTypeCnt = chain.getSimpleAggregate().getDescendants_NamedTypes().size();
	// Assert.assertEquals(AggregateSimple, namedTypeCnt);
	//
	// // Check the service
	// namedTypeCnt = chain.getServiceAggregate().getDescendants_NamedTypes().size();
	// Assert.assertEquals(1, namedTypeCnt);
	//
	// // Check counts against the underlying TL library
	// for (LibraryNode lib : chain.getLibraries()) {
	// int libCnt = lib.getDescendants_NamedTypes().size();
	// int tlCnt = lib.getTLaLib().getNamedMembers().size();
	// Assert.assertEquals(libCnt, tlCnt);
	// }
	// }
	//
	// /**
	// * Create named objects in the passed library and set the counts.
	// */
	// private static int TotalNamedObjects = 10; // The number of objects created by addNamedObjects
	//
	// private void addNamedObjects(LibraryNode lib) {
	// assertTrue(lib.isEditable());
	// BusinessObjectNode nobo = ml.addBusinessObjectToLibrary(lib, "testBO");
	// CoreObjectNode nco = ml.addCoreObjectToLibrary(lib, "testCO");
	// VWA_Node nvwa = ml.addVWA_ToLibrary(lib, "testVWA");
	// SimpleTypeNode nsimple = ml.addSimpleTypeToLibrary(lib, "testSimple");
	// EnumerationClosedNode ncEnum = ml.addClosedEnumToLibrary(lib, "testCEnum");
	// EnumerationOpenNode noEnum = ml.addOpenEnumToLibrary(lib, "testOEnum");
	// ml.addNestedTypes(lib);
	//
	// nobo.addFacet("boCustomFacet", TLFacetType.CUSTOM);
	//
	// ServiceNode svc = new ServiceNode(nobo);
	// svc.setName(nobo.getName() + "_Service");
	//
	// // Only update counts IFF working on the main chain
	// if (lib.getChain() == chain) {
	// bo = nobo;
	// co = nco;
	// vwa = nvwa;
	// simple = nsimple;
	// cEnum = ncEnum;
	// oEnum = noEnum;
	//
	// TotalDescendents = lib.getChain().getDescendants_NamedTypes().size();
	// AggregateComplex = lib.getChain().getComplexAggregate().getDescendants_NamedTypes().size();
	// AggregateSimple = lib.getChain().getSimpleAggregate().getDescendants_NamedTypes().size();
	// }
	// }
	//
	// private void checkValid(Node chain) {
	// LibraryChainNode cn = chain.getChain();
	// if (cn.isValid())
	// return;
	// ValidationFindings findings = cn.validate();
	// for (String f : findings.getAllValidationMessages(FindingMessageFormat.IDENTIFIED_FORMAT)) {
	// LOGGER.debug("Finding: " + f);
	// }
	// Assert.assertTrue(cn.isValid());
	// }
}
