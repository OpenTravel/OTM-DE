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
package org.opentravel.schemas.functional;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLExtensionPointFacet;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.saver.LibrarySaveException;
import org.opentravel.schemacompiler.validate.FindingMessageFormat;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemas.node.AggregateNode;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.NavNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeEditStatus;
import org.opentravel.schemas.node.NodeFinders;
import org.opentravel.schemas.node.ServiceNode;
import org.opentravel.schemas.node.VersionAggregateNode;
import org.opentravel.schemas.node.VersionNode;
import org.opentravel.schemas.node.handlers.ConstraintHandler;
import org.opentravel.schemas.node.interfaces.ExtensionOwner;
import org.opentravel.schemas.node.interfaces.FacetOwner;
import org.opentravel.schemas.node.interfaces.SimpleMemberInterface;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.objectMembers.ExtensionPointNode;
import org.opentravel.schemas.node.objectMembers.OperationNode;
import org.opentravel.schemas.node.properties.AttributeNode;
import org.opentravel.schemas.node.properties.ElementNode;
import org.opentravel.schemas.node.properties.IndicatorNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.properties.TypedPropertyNode;
import org.opentravel.schemas.node.typeProviders.AliasNode;
import org.opentravel.schemas.node.typeProviders.ChoiceObjectNode;
import org.opentravel.schemas.node.typeProviders.EnumerationClosedNode;
import org.opentravel.schemas.node.typeProviders.EnumerationOpenNode;
import org.opentravel.schemas.node.typeProviders.SimpleTypeNode;
import org.opentravel.schemas.node.typeProviders.VWA_Node;
import org.opentravel.schemas.node.typeProviders.facetOwners.BusinessObjectNode;
import org.opentravel.schemas.node.typeProviders.facetOwners.CoreObjectNode;
import org.opentravel.schemas.testUtils.BaseRepositoryTest;
import org.opentravel.schemas.testers.GlobalSelectionTester;
import org.opentravel.schemas.testers.NodeTester;
import org.opentravel.schemas.trees.repository.RepositoryNode;
import org.opentravel.schemas.types.TypeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//@Ignore("Tests currently failing and need attention.")
public class VersionsTest extends BaseRepositoryTest {
	static final Logger LOGGER = LoggerFactory.getLogger(VersionsTest.class);

	// MockLibrary ml = new MockLibrary();

	private BusinessObjectNode bo = null;
	private CoreObjectNode co = null;
	private ChoiceObjectNode ch = null;
	private VWA_Node vwa = null;
	private SimpleTypeNode simple = null;
	private EnumerationClosedNode cEnum = null;
	private EnumerationOpenNode oEnum = null;

	private ExtensionPointNode ep = null;
	private BusinessObjectNode sbo = null;
	private BusinessObjectNode nbo = null;
	private CoreObjectNode core2 = null;
	private LibraryNode majorLibrary = null;
	private LibraryNode minorLibrary = null;
	private LibraryNode patchLibrary = null;
	private LibraryNode secondLib = null;
	private LibraryChainNode chain = null;
	private CoreObjectNode mCo = null;
	private ExtensionPointNode ePatch = null;

	int TotalDescendents, AggregateSimple, AggregateComplex, TotalLibraries, MinorComplex;
	int MajorTotal, MinorTotal, PatchTotal;

	private Node xsdStringNode;

	@Override
	public RepositoryNode getRepositoryForTest() {
		for (RepositoryNode rn : rc.getAll()) {
			if (rn.isRemote()) {
				return rn;
			}
		}
		throw new IllegalStateException("Missing remote repository. Check your configuration.");
	}

	// FIXME
	// @Before
	public void runBeforeEachTest() throws LibrarySaveException, RepositoryException {
		// LOGGER.debug("Before test.");
		// xsdStringNode = NodeFinders.findNodeByName("string", ModelNode.XSD_NAMESPACE);
		// ProjectNode uploadProject = createProject("ToUploadLibrary", getRepositoryForTest(), "Test");
		//
		// majorLibrary = LibraryNodeBuilder.create("TestLibrary", getRepositoryForTest().getNamespace() + "/Test/T2",
		// "prefix", new Version(1, 0, 0)).build(uploadProject, pc);
		// LOGGER.debug("Created majorLibrary: " + majorLibrary);
		//
		// secondLib = LibraryNodeBuilder.create("TestLibrary2", getRepositoryForTest().getNamespace() + "/Test",
		// "prefix2", new Version(1, 0, 0)).build(uploadProject, pc);
		// LOGGER.debug("Created secondLib: " + secondLib);
		//
		// chain = rc.manage(getRepositoryForTest(), Collections.singletonList(majorLibrary)).get(0);
		// boolean locked = rc.lock(chain.getHead());
		//
		// assertTrue("Chain must be locked.", locked);
		// assertTrue("Major library must be editable.", majorLibrary.isEditable());
		// assertTrue("Head library must be in Managed WIP state.", RepositoryItemState.MANAGED_WIP == chain.getHead()
		// .getProjectItem().getState());
		// LOGGER.debug("Managed major library in repository.");
		//
		// // Create valid examples of each component type
		// addNamedObjects(majorLibrary);
		// MinorComplex = 0;
		// PatchTotal = 0;
		// assertTrue("Total Descendents must be set.", TotalDescendents > 0);
		//
		// // Create a valid extension point node in 2nd library
		// core2 = (CoreObjectNode) majorLibrary.findNodeByName("N2");
		// ExtensionPointNode ep = new ExtensionPointNode(new TLExtensionPointFacet());
		// secondLib.addMember(ep); // Extension point must be in different namespace than the type it extends.
		// ep.setExtension((Node) core2.getSummaryFacet());
		// LOGGER.debug("Created objects.");
		//
		// // Make sure chain validates without errors
		// checkValid(chain);
		//
		// // Create locked patch version
		// patchLibrary = rc.createPatchVersion(chain.getHead());
		// // TESTME - Uncommitted change set from previous task - rolling back. -- BUT not rolled back!
		// // TESTME - why is this 45 seconds?
		// TotalLibraries = 3;
		// specPatch(); // must do tests while editable
		// LOGGER.debug("Created patch library: " + patchLibrary);
		//
		// //
		// // Create locked minor version. Will contain bo with property from ePatch.
		// minorLibrary = rc.createMinorVersion(chain.getHead());
		// // AggregateComplex--; // new Core in minor will be head of both patch extension point and original core
		// MinorComplex++; // the new CO from patch EPF
		// TotalDescendents++; // new Co
		// // Make sure the patch library still has the extension point wrapped in a version node.
		// VersionNode vn = (VersionNode) patchLibrary.getComplexRoot().getChildren().get(0);
		// Assert.assertSame(vn.getChildren().get(0), ePatch);
		// LOGGER.debug("Created Minor version of: " + chain.getHead());
		// // Test core in minor library
		// mCo = null;
		// for (Node n : minorLibrary.getDescendants_LibraryMembers()) {
		// if (n.getName().equals(co.getName())) {
		// mCo = (CoreObjectNode) n;
		// break;
		// }
		// }
		// Assert.assertSame(co, mCo.getExtensionBase());
		// // Assert.assertSame(vn.getNewestVersion(), mCo);
		//
		// // checkCounts(chain);
		// checkValid(chain);
		//
		// LOGGER.debug("Before tests done.");

		// TESTME - why is this 58 seconds?

		// Before tests creates:
		// a chain with the one managed (majorLibrary) with content
		// a patch library in the chain
		// a minorLibrary created from the chain
		// second major with an extension point
		//
	}

	// @Test
	public void VT_versionTestInitialized() {
		assert false;
	}

	//
	// Test the library level testers.
	//
	// FIXME
	// @Test
	public void testLibraryTesters() {
		checkLibraryStatus();
		checkObjectStatus();

		// Run all tests from here to reduce set up time.
		specMinor(); // run before spec Major. adds objects and properties.
		checkNavChildren();
		assertTrue(TotalLibraries == chain.getLibraries().size());

		// Check specifications
		// patch tests done while patch library was editable - specPatch();
		specMajor();
		// Chain is now deleted and closed.

		// // adhoc tests
		// assertTrue(TotalLibraries == chain.getLibraries().size());
		// List<Node> dChain = chain.getDescendants_LibraryMembers();
		// List<Node> dMajor = majorLibrary.getDescendants_LibraryMembers();
		// assertTrue(chain.getDescendants_LibraryMembers().contains(bo));
		// assertTrue(majorLibrary.getDescendants_LibraryMembers().contains(bo));

		// // verify the core object, co, created in the minor library contains properties from the
		// // extension point in the patch.
		// Assert.assertNotNull(mCo);
		// Assert.assertEquals(1, mCo.getSummaryFacet().getChildren().size());

		// Assert.assertNotNull(mCo.getExtensionBase());

		// testAddNodeHandler();
		// testFacets();
		// testMove();
		// testCopying();
		// testAddingAndDeleting();
		// // testAddingProperties(nco);
		// testDelete();
	}

	private void checkLibraryStatus() {
		//
		// Chain assertions
		Assert.assertTrue(chain.isMinor());
		Assert.assertFalse(chain.isMajor());
		Assert.assertFalse(chain.isPatch());
		assertTrue("Minor must be head.", chain.getHead() == minorLibrary);
		assertTrue("Major must be anchor.", chain.getMajor() == majorLibrary);

		//
		// Major Library
		Assert.assertTrue(majorLibrary.isInChain());
		Assert.assertTrue(majorLibrary.isManaged());
		Assert.assertTrue(majorLibrary.isReadyToVersion());
		Assert.assertTrue(majorLibrary.isMajorVersion());
		Assert.assertTrue(majorLibrary.isMinorOrMajorVersion());
		//
		Assert.assertFalse(majorLibrary.isEditable());
		Assert.assertFalse(majorLibrary.isLocked());
		Assert.assertFalse(majorLibrary.isPatchVersion());
		Assert.assertFalse(majorLibrary.isMinorVersion());
		Assert.assertFalse(chain.isLaterVersion(majorLibrary, minorLibrary));
		Assert.assertFalse(chain.isLaterVersion(majorLibrary, patchLibrary));

		//
		// Minor Library
		Assert.assertTrue(minorLibrary.isInChain());
		Assert.assertTrue(minorLibrary.isManaged());
		Assert.assertTrue(minorLibrary.isReadyToVersion());
		Assert.assertTrue(minorLibrary.isEditable());
		Assert.assertTrue(minorLibrary.isLocked());
		Assert.assertTrue(minorLibrary.isMinorOrMajorVersion());
		Assert.assertTrue(minorLibrary.isMinorVersion());
		Assert.assertTrue(chain.isLaterVersion(minorLibrary, majorLibrary));
		Assert.assertTrue(chain.isLaterVersion(minorLibrary, patchLibrary));
		//
		Assert.assertFalse(minorLibrary.isMajorVersion());
		Assert.assertFalse(minorLibrary.isPatchVersion());

		//
		// patch Library
		Assert.assertTrue(patchLibrary.isInChain());
		Assert.assertTrue(patchLibrary.isManaged());
		Assert.assertTrue(patchLibrary.isReadyToVersion());
		Assert.assertTrue(patchLibrary.isPatchVersion());
		Assert.assertTrue(chain.isLaterVersion(patchLibrary, majorLibrary));
		//
		Assert.assertFalse(patchLibrary.isMajorVersion());
		Assert.assertFalse(patchLibrary.isEditable());
		Assert.assertFalse(patchLibrary.isLocked());
		Assert.assertFalse(patchLibrary.isMinorVersion());
		Assert.assertFalse(patchLibrary.isMinorOrMajorVersion());
		Assert.assertFalse(chain.isLaterVersion(patchLibrary, minorLibrary));

		// LibraryNode based status
		Assert.assertEquals(NodeEditStatus.MANAGED_READONLY, majorLibrary.getEditStatus());
		Assert.assertEquals(NodeEditStatus.MINOR, minorLibrary.getEditStatus());
		Assert.assertEquals(NodeEditStatus.MANAGED_READONLY, patchLibrary.getEditStatus());
		Assert.assertEquals(NodeEditStatus.FULL, secondLib.getEditStatus());

	}

	private void checkObjectStatus() {
		// Node based NodeEditStatus is based on chain head
		Assert.assertEquals(NodeEditStatus.MINOR, co.getEditStatus());
		Assert.assertEquals(NodeEditStatus.MINOR, bo.getEditStatus());

		// These objects are in the major version
		Assert.assertFalse(co.isInHead());
		Assert.assertFalse(bo.isInHead());
		Assert.assertFalse(vwa.isInHead());
		Assert.assertFalse(co.isEditable_newToChain());
		Assert.assertFalse(bo.isEditable_newToChain());
		Assert.assertFalse(vwa.isEditable_newToChain());

		//
		// Tests used to enable user actions.

		// Editable - should all be true to drive the navView display.
		Assert.assertTrue(co.isEditable());
		Assert.assertTrue(bo.isEditable());

		// Delete-able - NodeTester.canDelete() -> Node.isDeletable()
		Assert.assertFalse(co.isDeleteable());
		Assert.assertFalse(bo.isDeleteable());
		// Assert.assertTrue(sbo.isDeleteable());

		// CanAdd - Control for AddNodeHandler. GlobalSelectionTester.canAdd()
		GlobalSelectionTester gst = new GlobalSelectionTester();
		Assert.assertTrue(gst.test(co, GlobalSelectionTester.CANADD, null, null));
		Assert.assertTrue(gst.test(bo, GlobalSelectionTester.CANADD, null, null));
		Assert.assertTrue(gst.test(vwa, GlobalSelectionTester.CANADD, null, null));
		Assert.assertFalse(gst.test(ep, GlobalSelectionTester.CANADD, null, null));

		// New Component - NodeTester
		NodeTester tester = new NodeTester();
		Assert.assertTrue(tester.test(co, NodeTester.IS_IN_TLLIBRARY, null, null));
		Assert.assertTrue(tester.test(co, NodeTester.IS_OWNER_LIBRARY_EDITABLE, null, null));
		Assert.assertTrue(tester.test(bo, NodeTester.IS_IN_TLLIBRARY, null, null));
		Assert.assertTrue(tester.test(bo, NodeTester.IS_OWNER_LIBRARY_EDITABLE, null, null));

		// Move - NavigatorMenus.createMoveActionsForLibraries()
		Assert.assertFalse(co.getLibrary().isMoveable());
		Assert.assertFalse(bo.getLibrary().isMoveable());

	}

	private void testAddNodeHandler() {
		// Add node handler test
		CoreObjectNode nco = createCoreInMinor(); // extends co
		// Node object based status
		NodeTester tester = new NodeTester();
		GlobalSelectionTester gst = new GlobalSelectionTester();
		Assert.assertEquals(NodeEditStatus.MINOR, nco.getEditStatus());
		assertTrue(nco.isInHead());
		assertTrue(!nco.isEditable_newToChain());
		assertTrue(nco.isEditable());
		assertTrue(nco.isDeleteable());
		assertTrue(nco.getLibrary().isMoveable());
		assertTrue(gst.test(nco, GlobalSelectionTester.CANADD, null, null));
		assertTrue(tester.test(nco, NodeTester.IS_IN_TLLIBRARY, null, null));
		assertTrue(tester.test(nco, NodeTester.IS_OWNER_LIBRARY_EDITABLE, null, null));
		checkHeirarchy(nco);

	}

	// From OTM-DE Reference-Language Specification - Major Versions
	public void specMajor() {
		assertTrue(chain.isEditable());
		// List<Node> chainNodes = chain.getDescendants_NamedTypes();

		// Create major version which makes the minor final. Closes old chain.
		LibraryNode newMajor = rc.createMajorVersion(chain.getHead());

		// List<Node> newNodes = newMajor.getDescendants_NamedTypes();
		assertTrue(newMajor.isEditable());
		assertTrue("Old chain must not be editable because it is finalized.", !chain.isEditable());

		// // 1. If a prior version of the term existed, its content and structure can be modified in any way
		// // TEST - in a new major, verify named objects exist in both chains are both chains are valid.
		// // TEST - rename, delete and add properties to rolled up major objects
		// for (Node n : chain.getDescendants_LibraryMembers()) {
		// if (n instanceof ExtensionPointNode) {
		// LOGGER.debug("Found extension point " + n + " in chain (expected).");
		// continue; // would be rolled up
		// }
		// Node mn = newMajor.findNodeByName(n.getName());
		// if (mn == null) {
		// LOGGER.error("New major library did not contain " + n);
		// continue;
		// }
		// assertTrue(mn != null);
		// }
		ArrayList<Node> nodes = new ArrayList<>(newMajor.getDescendants_LibraryMembersAsNodes());
		for (Node n : nodes) {
			n.setName(n.getName() + "_TEST");
			// TODO - modify properties
			assertTrue(n.isDeleteable());
			// if (!n.getName().startsWith("TestBO"))
			n.delete(); // bo has contextual facets which would become invalid
		}

		// 2. Any new term can be defined
		// TEST - create all new object types
		TotalNamedObjects += ml.addOneOfEach(newMajor, "NewMajor_");
		// addNamedObjects(newMajor);
		// Assert.assertEquals(TotalNamedObjects, newMajor.getDescendants_LibraryMembers().size());

		// verify ability to add doc to all simple types
		for (Node n : newMajor.getSimpleRoot().getChildren())
			if (n instanceof SimpleTypeNode)
				addAndRemoveDoc((SimpleTypeNode) n);

		checkValid(newMajor);
		newMajor.delete();

		// Make sure nothing changed in the original chain
		checkValid(chain);
		// checkCounts(chain);
	}

	// From OTM-DE Reference-Language Specification
	// Minor Versions
	// 1. Any new term can be defined
	// 2. Existing versioned terms (see section 11.3) can only be modified by adding indicators, optional attributes, or
	// optional element declarations
	// 3. Non-versioned terms cannot be modified in a minor version library

	// TEST - limited modification of existing objects (VWA,Core Object,Business Object,Operation)
	// ---- 1. The terms must be of the same type (business object, core, etc.) and have the same name
	// ---- 2. The terms MUST be declared in different libraries, and both libraries must have the same name, version
	// scheme,
	// ---- and base namespace URI
	// ---- 3. The version of the extended term’s library MUST be lower than that of the extending term’s library
	// version,
	// ---- but both libraries MUST belong to the same major version chain
	// TEST - non-versioned objects: no editing of properties, names or assigned types
	// TEST - all new objects can be added (different names)

	// @Test
	public void specMinor() {
		// Minor library is head of chain and editable
		assertTrue(chain.isEditable());
		assertTrue(chain.getHead() == minorLibrary);
		assertTrue(minorLibrary.isEditable());
		String ctx = minorLibrary.getDefaultContextId();
		assertTrue(!minorLibrary.getDefaultContextId().isEmpty());

		//
		// Try all the things we are allowed to do in a minor
		//
		// Ensure it creates a BO in the minor and it extends the original BO
		assertTrue(!minorLibrary.getDescendants_LibraryMembersAsNodes().contains(bo));
		assertTrue(bo.isEditable()); // is the chain that contains it editable?

		BusinessObjectNode boInMinor = (BusinessObjectNode) bo.createMinorVersionComponent();
		TotalDescendents++;
		assertTrue(boInMinor.isEditable());
		assertTrue(!boInMinor.isEditable_newToChain());
		Assert.assertEquals(NodeEditStatus.MINOR, boInMinor.getEditStatus());
		assertTrue(minorLibrary.getDescendants_LibraryMembersAsNodes().contains(boInMinor));

		// change a pre-existing old property
		PropertyNode oldProperty = (PropertyNode) bo.getFacet_Summary().getChildren().get(0);
		assertTrue(oldProperty != null);
		addAndRemoveDoc(oldProperty);

		// Add a new property
		ElementNode newProperty = new ElementNode(boInMinor.getFacet_Summary(), "testProp");
		assertTrue(newProperty != null);
		assertTrue(newProperty.isEditable_newToChain());
		addAndRemoveDoc(newProperty);

		// Add a new business object to the minor
		String newBoName = "TestMinorBO";
		BusinessObjectNode newBO = new BusinessObjectNode(new TLBusinessObject());
		newBO.setName(newBoName);
		minorLibrary.addMember(newBO);
		ElementNode idProperty = new ElementNode(newBO.getFacet_ID(), "id");
		TotalDescendents++;
		AggregateComplex++;

		assertTrue(newBO.getName().equals(newBoName));
		assertTrue(newBO.isEditable_newToChain());
		assertTrue(idProperty.isEditable_newToChain());
		addAndRemoveDoc(idProperty);
		assertTrue(!idProperty.isMandatory());
		idProperty.setMandatory(true); // should work
		assertTrue("Must set mandatory on object new to the minor.", idProperty.isMandatory());
		Assert.assertEquals(NodeEditStatus.MINOR, newBO.getEditStatus());

		//
		// Make sure other actions are prohibited.
		//
		assertTrue(!newProperty.isMandatory());
		newProperty.setMandatory(true); // ignored because owning component is not new to chain.
		assertTrue("Must NOT be allowed.", !newProperty.isMandatory());
		assertTrue(!oldProperty.isMandatory());
		oldProperty.setMandatory(true); // should do nothing
		assertTrue("Must not be allowed.", !oldProperty.isMandatory());

		AliasNode alias = new AliasNode(boInMinor, "testAlias");
		assertTrue(alias != null);

		checkValid(chain);
		// checkCounts(chain);
	}

	//
	// Test the children/parent relationships
	//
	// @Test
	public void checkHeirarchy(CoreObjectNode nco) {
		if (nco == null)
			nco = createCoreInMinor();
		Assert.assertTrue(nco.getParent() instanceof VersionNode);
		Assert.assertTrue(nco.getLibrary() == minorLibrary);
		// TODO - why? if they are always the same, why have version node pointer? Just to save a
		// cast? If so, create a method w/ cast and remove data
		Assert.assertTrue(nco.getParent() == nco.getVersionNode());

		// check head and prev
		Assert.assertTrue(nco.getVersionNode().getNewestVersion() == nco);
		Assert.assertTrue(co.getVersionNode().getNewestVersion() == nco);
		Assert.assertTrue(nco.getVersionNode().getPreviousVersion() == co);
		Assert.assertTrue(co.getVersionNode().getPreviousVersion() == null);

		Node head = chain.getHead();
		Assert.assertTrue(head instanceof LibraryNode);
		Assert.assertTrue(head.getParent().getParent() == chain);
		Assert.assertTrue(head.getChain() == chain);

		// Make sure all versions are present.
		Node versionsAgg = chain.getVersions();
		Assert.assertTrue(versionsAgg instanceof VersionAggregateNode);
		Assert.assertEquals(TotalLibraries, versionsAgg.getChildren().size());
		Assert.assertEquals(TotalLibraries, versionsAgg.getNavChildren(false).size());
		Assert.assertTrue(versionsAgg.getParent() == chain);
		Assert.assertTrue(versionsAgg.getChain() == chain);

		for (Node lib : versionsAgg.getChildren()) {
			Assert.assertTrue(lib.getParent() == versionsAgg);
			Assert.assertTrue(lib instanceof LibraryNode);
			if (lib == chain.getHead())
				Assert.assertTrue(lib.isEditable());
			else
				Assert.assertFalse(lib.isEditable());
			// Either nav or service nodes.
			checkChildrenClassType(lib, NavNode.class, ServiceNode.class);

			// Check the children of the Nav Nodes and Service Node
			for (Node nn : lib.getChildren()) {
				if (nn instanceof NavNode) {
					// Nav node children must be version nodes.
					Assert.assertTrue(nn.getParent() == lib);
					checkChildrenClassType(nn, VersionNode.class, null);
					for (Node vn : nn.getChildren()) {
						// Version nodes wrap their one child
						Assert.assertTrue(vn.getParent() == nn);
						Assert.assertEquals(1, vn.getChildren().size());
						checkChildrenClassType(vn, ComponentNode.class, null);
						for (Node cc : vn.getChildren()) {
							// Check the actual component nodes.
							Assert.assertTrue(cc.getParent() == vn);
						}
					}
				} else {
					// FIXME - operations in the library should be wrapped.
					checkChildrenClassType(nn, OperationNode.class, null);
				}
			}
		}

		// Check the aggregates
		Node complexAgg = chain.getComplexAggregate();
		Assert.assertTrue(complexAgg.getParent() == chain);
		// checkChildrenClassType(complexAgg, ComplexComponentInterface.class, AggregateFamilyNode.class);
		for (Node n : complexAgg.getChildren()) {
			Assert.assertTrue(n.getParent() != complexAgg);
		}

		Node simpleAgg = chain.getSimpleAggregate();
		Assert.assertTrue(simpleAgg.getParent() == chain);
		checkChildrenClassType(simpleAgg, SimpleMemberInterface.class, null);
		for (Node n : simpleAgg.getChildren())
			Assert.assertTrue(n.getParent() != simpleAgg);

		Node svcAgg = chain.getServiceAggregate();
		Assert.assertTrue(svcAgg.getParent() == chain);

	}

	private void checkChildrenClassType(Node parent, Class<?> c, Class<?> c2) {
		for (Node n : parent.getChildren()) {
			// n instanceof c.class
			if (c2 != null)
				Assert.assertTrue(c.isAssignableFrom(n.getClass()) || c2.isAssignableFrom(n.getClass()));
			else
				Assert.assertTrue(c.isAssignableFrom(n.getClass()));
		}
	}

	//
	// test adding custom facets
	//
	// @Test
	public void testFacets() {
		int facetCount = bo.getChildren().size();
		bo.addFacet("custom1", TLFacetType.CUSTOM);
		// Adding to bo should fail...in the future it might create a new bo and add it to that.
		Assert.assertEquals(facetCount, bo.getChildren().size());

		// test adding to a new minor version component
		nbo = (BusinessObjectNode) bo.createMinorVersionComponent();
		MinorComplex += 1;
		nbo.isInHead();
		// We should get at least the default context.
		List<String> contextIDs = minorLibrary.getContextIds();
		Assert.assertFalse(contextIDs.isEmpty());

		// Add a custom facet
		// FIXME - not allowed!! Should it be?
		// nbo.addFacet("c2", contextIDs.get(0), TLFacetType.CUSTOM);
		// Assert.assertEquals(4, nbo.getChildren().size());
		// minorComple + 1 (the inherited simple created by roll-up
		// Assert.assertEquals(MinorComplex + 1, minorLibrary.getDescendants_NamedTypes().size());

		Assert.assertTrue(chain.isValid());
		nbo.delete();
		MinorComplex -= 1;
		checkCounts(chain);
	}

	// @Test
	public void checkNavChildren() {
		// Chain should have 5 (include resources)
		Assert.assertEquals(5, chain.getNavChildren(false).size());
		checkChildrenClassType(chain, AggregateNode.class, null);

		// VersionAggregate should have 3, one for each library
		Assert.assertEquals(3, chain.getVersions().getChildren().size());

		// Libraries that are not at the head will return empty list.
		Assert.assertEquals(0, patchLibrary.getNavChildren(false).size());
		Assert.assertEquals(0, minorLibrary.getNavChildren(false).size());
		Assert.assertEquals(0, majorLibrary.getNavChildren(false).size());

		// Nav Nodes should ONLY have version -- NEVER GOES INTO LOOP
		for (Node nn : patchLibrary.getNavChildren(false)) {
			Assert.assertTrue(nn instanceof NavNode);
			checkChildrenClassType(nn, VersionNode.class, null);
			// Version nodes should have NO nav children.
			for (Node vn : nn.getNavChildren(false))
				Assert.assertEquals(0, vn.getNavChildren(false).size());
		}

		// Aggregates should have Active Simple, Active Complex and Service.
		// This checks both children and then navChildren.
		// checkChildrenClassType(((Node) chain.getComplexAggregate()), ComplexComponentInterface.class,
		// AggregateFamilyNode.class);
		for (Node nc : ((Node) chain.getComplexAggregate()).getNavChildren(false)) {
			Assert.assertTrue(nc instanceof FacetOwner);
		}
		checkChildrenClassType((chain.getSimpleAggregate()), SimpleMemberInterface.class, null);
		for (Node nc : ((Node) chain.getSimpleAggregate()).getNavChildren(false)) {
			Assert.assertTrue(nc instanceof SimpleMemberInterface);
		}

	}

	//
	// Test handling of adding and deleting of new objects
	//
	// @Test
	public void testAddingAndDeleting() {
		// FIXME - minor is not editable
		// nbo = ml.addBusinessObjectToLibrary(minorLibrary, "nbo");
		//
		// // The new bo should be in the minor library, not the base library.
		// Assert.assertTrue(chain.getDescendants_NamedTypes().contains(nbo));
		// Assert.assertTrue(minorLibrary.getDescendants_NamedTypes().contains(nbo));
		// Assert.assertFalse(majorLibrary.getDescendants_NamedTypes().contains(nbo));
		//
		// // Add some other object types
		// EnumerationClosedNode nec = ml.addClosedEnumToLibrary(chain.getHead(), "ce2");
		// VWA_Node nvwa = ml.addVWA_ToLibrary(chain.getHead(), "vwa2");
		// Assert.assertTrue(chain.isValid());
		//
		// Assert.assertTrue(nec.isNewToChain());
		// Assert.assertTrue(nvwa.isNewToChain());
		// Assert.assertTrue(nbo.isNewToChain()); // this object is in previous version.
		//
		// // Remove and delete them
		// nbo.delete();
		// nec.delete();
		// nvwa.delete();
		// Assert.assertTrue(chain.isValid());
		// checkCounts(chain);
	}

	// TODO - test adding more minor chains and adding objects to all of them to verify the prev
	// link.

	// @Test
	public void testAddingProperties(CoreObjectNode nco) {
		// FIXME - it has 2 children
		// Assert.assertEquals(1, co.getSummaryFacet().getChildren().size());
		//
		// if (nco == null)
		// nco = createCoreInMinor();
		// testAddingPropertiesToFacet(nco.getSummaryFacet());
		//
		// BusinessObjectNode nbo = createBO_InMinor();
		// testAddingPropertiesToFacet(nbo.getDetailFacet());
		//
		// VWA_Node nVwa = createVWA_InMinor();
		// testAddingPropertiesToFacet(nVwa.getAttributeFacet());
		//
		// Assert.assertEquals(1, co.getSummaryFacet().getChildren().size());
		// Assert.assertTrue(chain.isValid());

		// nco.delete();
		// nbo.delete();
		// TotalDescendents -= 2;
		// MinorComplex -= 0; // keep counts accurate
		// // Active complex should remain unchanged.
		// checkCounts(chain);
	}

	// Adds the removed properties from the facet.
	// Emulate AddNodeHandler and newPropertiesWizard
	// private void testAddingPropertiesToFacet(ComponentNode propOwner) {
	// int cnt = propOwner.getChildren().size();
	// PropertyNode newProp = null;
	// if (!(propOwner instanceof AttributeFacetNode)) {
	// newProp = new ElementNode(new FacetNode(), "np" + cnt++);
	// propOwner.addProperty(newProp); // Needed???
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

	/**
	 * Create a new minor version of core: co. Add a property "te2".
	 * 
	 * Emulates behavior in AddNodeHandler. AddNodeHandler notifies the user then createMinorVersionComponent().
	 * Constructors can not do this because they are needed for initial rendering of the objects and can't do user
	 * dialogs/notifications.
	 */
	private CoreObjectNode createCoreInMinor() {
		CoreObjectNode nco = (CoreObjectNode) co.createMinorVersionComponent();
		TypeProvider type = ((TypeProvider) NodeFinders.findNodeByName("string", ModelNode.XSD_NAMESPACE));
		PropertyNode newProp = new ElementNode(nco.getFacet_Summary(), "te2", type);

		Assert.assertEquals(1, co.getFacet_Summary().getChildren().size());
		TotalDescendents += 1;
		MinorComplex += 1;

		// Make sure a new CO was created in the newMinor library.
		Assert.assertNotNull(nco);

		// TODO - why does creating a second version end up as not versioned?
		// if (nco.getVersionNode().getParent() instanceof FamilyNode)
		// assertTrue(nco.isEditable_newToChain()); // Why?
		// else {
		Assert.assertFalse(nco.isEditable_newToChain()); // not new because it's base type exists in previous chain
		Assert.assertNotNull(nco.getVersionNode().getPreviousVersion());
		Assert.assertEquals(nco.getVersionNode().getPreviousVersion(), co);
		// }
		Assert.assertFalse(co.isEditable_newToChain());
		Assert.assertEquals(1, nco.getFacet_Summary().getChildren().size());
		Assert.assertTrue(chain.getDescendants_LibraryMembersAsNodes().contains(nco));
		Assert.assertTrue(minorLibrary.getDescendants_LibraryMembersAsNodes().contains(nco));
		Assert.assertFalse(majorLibrary.getDescendants_LibraryMembersAsNodes().contains(nco));

		return nco;
	}

	private BusinessObjectNode createBO_InMinor() {
		BusinessObjectNode nbo = (BusinessObjectNode) bo.createMinorVersionComponent();
		TypeProvider type = ((TypeProvider) NodeFinders.findNodeByName("string", ModelNode.XSD_NAMESPACE));
		PropertyNode newProp = new ElementNode(nbo.getFacet_Summary(), "te2", type);
		Assert.assertEquals(1, bo.getFacet_Summary().getChildren().size());
		TotalDescendents += 1;
		MinorComplex += 1;

		// Make sure a new CO was created in the newMinor library.
		Assert.assertNotNull(nbo);
		Assert.assertNotNull(nbo.getVersionNode().getPreviousVersion());
		Assert.assertEquals(1, nbo.getFacet_Summary().getChildren().size());
		Assert.assertTrue(chain.getDescendants_LibraryMembers().contains(nbo));
		Assert.assertTrue(minorLibrary.getDescendants_LibraryMembers().contains(nbo));
		Assert.assertFalse(majorLibrary.getDescendants_LibraryMembers().contains(nbo));

		return nbo;
	}

	private VWA_Node createVWA_InMinor() {
		VWA_Node nVwa = (VWA_Node) vwa.createMinorVersionComponent();
		Assert.assertNotNull(nVwa);
		TypedPropertyNode newProp = new AttributeNode(nVwa.getFacet_Attributes(), "te2");
		newProp.setAssignedType((TypeProvider) NodeFinders.findNodeByName("string", ModelNode.XSD_NAMESPACE));
		Assert.assertEquals(1, bo.getFacet_Summary().getChildren().size());
		TotalDescendents += 1;
		MinorComplex += 1;

		// Make sure a new was created in the newMinor library.
		Assert.assertNotNull(nVwa);
		Assert.assertEquals(1, nVwa.getFacet_Attributes().getChildren().size());
		Assert.assertTrue(chain.getDescendants_LibraryMembers().contains(nVwa));
		Assert.assertTrue(minorLibrary.getDescendants_LibraryMembers().contains(nVwa));
		Assert.assertFalse(majorLibrary.getDescendants_LibraryMembers().contains(nVwa));

		return nVwa;
	}

	// @Test
	public void testCopying() {
		// FIXME - failed to delete the family member
		// if (bo.getLibrary().isEditable()) {
		// nbo = (BusinessObjectNode) bo.clone("_copy");
		// // copy should be in the new library
		// Assert.assertTrue(chain.getDescendants_NamedTypes().contains(nbo));
		// Assert.assertTrue(minorLibrary.getDescendants_NamedTypes().contains(nbo));
		// Assert.assertFalse(majorLibrary.getDescendants_NamedTypes().contains(nbo));
		// ValidationFindings findings = chain.validate();
		// Assert.assertTrue(chain.isValid());
		// nbo.delete();
		// checkCounts(chain);
		// } else
		// System.out.println("ERROR - testCopying using non-editable library.");
	}

	/**
	 * From MoveObjectToLibraryAction if (source.isInTLLibrary() && source.isTopLevelObject())
	 * source.getLibrary().moveMember(source, destination);
	 * 
	 * From MainController.ChangeNode() srcLib.moveMember(editedNode, destLib);
	 */
	// @Test
	public void testMove() {
		// This will work because moveMember is at the model level. It is used by the controller
		// which applies the business logic if it is valid to move.
		List<Node> namedTypes = chain.getDescendants_LibraryMembersAsNodes();
		int namedTypeCnt = chain.getDescendants_LibraryMembersAsNodes().size();
		LOGGER.debug("testMove " + TotalDescendents + " " + namedTypeCnt + " " + bo.getLibrary());

		// FIXME -
		// majorLibrary.moveMember(bo, minorLibrary);
		// // checkValid(chain);
		// List<Node> after = chain.getDescendants_NamedTypes();
		// int afterCnt = chain.getDescendants_NamedTypes().size();
		// LOGGER.debug("testMove " + TotalDescendents + " " + afterCnt);
		//
		// checkCounts(chain);
		// Assert.assertTrue(chain.getDescendants_NamedTypes().contains(bo));
		// Assert.assertFalse(majorLibrary.getDescendants_NamedTypes().contains(bo));
		// Assert.assertTrue(minorLibrary.getDescendants_NamedTypes().contains(bo));
		// minorLibrary.moveMember(bo, majorLibrary); // put it back
		// Assert.assertTrue(chain.isValid());
		// checkCounts(chain);
		//
		// // Test moving from another library
		// nbo = ml.addBusinessObjectToLibrary(secondLib, "secondLibBO");
		// secondLib.moveMember(nbo, minorLibrary);
		// Assert.assertTrue(chain.getDescendants_NamedTypes().contains(nbo));
		// Assert.assertFalse(secondLib.getDescendants_NamedTypes().contains(nbo));
		// nbo.delete();
		//
		// // Test moving to another library
		// nbo = ml.addBusinessObjectToLibrary(minorLibrary, "newBO");
		// minorLibrary.moveMember(nbo, secondLib);
		// Assert.assertTrue(secondLib.getDescendants_NamedTypes().contains(nbo));
		// Assert.assertFalse(minorLibrary.getDescendants_NamedTypes().contains(nbo));
		// Assert.assertFalse(majorLibrary.getDescendants_NamedTypes().contains(nbo));
		// Assert.assertFalse(chain.getDescendants_NamedTypes().contains(nbo));
		checkCounts(chain);

		Assert.assertTrue(chain.isValid());
	}

	// @Test
	public void testDelete() {
		// FIXME - bo was not in before delete
		// bo.delete(); // Should and does fail.
		// Assert.assertTrue(majorLibrary.getDescendants_NamedTypes().contains(bo));
		// List<?> kids = chain.getComplexAggregate().getChildren();
		// Assert.assertTrue(kids.contains(bo));
		// checkCounts(chain);
		// Assert.assertTrue(chain.isValid());
		//
		// //
		// // Test deleting properties
		// co.getSummaryFacet().getChildren().get(0).delete();
		// Assert.assertEquals(1, co.getSummaryFacet().getChildren().size());
		//
		// //
		// // Test handling of new object with same name as existing object.
		// nbo = ml.addBusinessObjectToLibrary(chain.getHead(), "testBO");
		// Assert.assertFalse(minorLibrary.isValid());
		//
		// kids = chain.getComplexAggregate().getChildren();
		// Assert.assertTrue(kids.contains(nbo));
		// Assert.assertFalse(kids.contains(bo)); // was replaced with nbo
		//
		// // The new bo should be in the minor library, not the base library.
		// Assert.assertTrue(chain.getDescendants_NamedTypes().contains(nbo));
		// Assert.assertTrue(minorLibrary.getDescendants_NamedTypes().contains(nbo));
		// Assert.assertFalse(majorLibrary.getDescendants_NamedTypes().contains(nbo));
		//
		// // Deleting via GUI should make it valid and replace the old one back into the aggregate
		// // model level delete should just do it.
		// nbo.delete();
		// Assert.assertTrue(minorLibrary.isValid());
		// Assert.assertTrue(chain.getDescendants_NamedTypes().contains(bo));
		// Assert.assertTrue(majorLibrary.getDescendants_NamedTypes().contains(bo));
		// checkCounts(chain);
		// // counts will be wrong.
		//
		// // Renaming it should make chain valid
		// nbo = ml.addBusinessObjectToLibrary(minorLibrary, "testBO");
		// nbo.setName("testBO2");
		// Assert.assertTrue(minorLibrary.isValid());
		// Assert.assertTrue(chain.getDescendants_NamedTypes().contains(bo));
		// Assert.assertTrue(majorLibrary.getDescendants_NamedTypes().contains(bo));
		// Assert.assertTrue(chain.getDescendants_NamedTypes().contains(nbo));
		// Assert.assertTrue(minorLibrary.getDescendants_NamedTypes().contains(nbo));
	}

	// From OTM-DE Reference-Language Specification
	// 1. Only new simple types, open/closed enumerations, and extension point facets can be defined
	// 2. Extension point facets are only allowed to reference (extend) standard or contextual facets declared in a
	// prior major or minor version of the patch library’s major version chain
	// Patch Versions
	public void specPatch() {
		// int beforeCount = chain.getDescendants_NamedTypes().size();
		if (patchLibrary == null)
			return;

		assertTrue(patchLibrary.isEditable());

		// TEST adding content types
		// FIXME - compiler validation claims illegal patch
		// https://github.com/OpenTravel/OTM-DE-Compiler/issues/28
		//
		// Add enumerations
		// EnumerationOpenNode oe = ml.addOpenEnumToLibrary(patchLibrary, "oEnumInPatch");
		// Add a closed enumeration
		// SimpleTypeNode ce = ml.addClosedEnumToLibrary(patchLibrary, "cEnumInPatch");
		// TotalDescendents += 2; // Number in whole chain
		// AggregateSimple += 1;
		// AggregateComplex++;

		// Add a simple type
		// SimpleTypeNode simple = ml.addSimpleTypeToLibrary(patchLibrary, "simpleInPatch");
		// TotalDescendents += 1; // Number in whole chain
		// AggregateSimple += 1;

		// TEST - adding documentation, examples and equivalents
		addAndRemoveDoc(simple);

		// TEST - Extension points, valid and invalid
		ePatch = new ExtensionPointNode(new TLExtensionPointFacet());
		patchLibrary.addMember(ePatch);
		((ExtensionOwner) ePatch).setExtension(co.getFacet_Summary());
		ePatch.addProperty(new IndicatorNode(ePatch, "patchInd"));

		addAndRemoveDoc((PropertyNode) ePatch.getChildren().get(0));

		TotalDescendents += 1; // Number in whole chain
		AggregateComplex += 1; // Number in the aggregates
		PatchTotal += 1;

		//
		// Test actions that should be prevented.
		//
		assertTrue(!bo.isEditable_newToChain());
		// adding facets can throw exceptions
		boolean exception = false;
		try {
			bo.addFacet("patchFacetC", TLFacetType.CUSTOM); // throws exception
			bo.addFacet("patchFacetQ", TLFacetType.QUERY); // throws exception
		} catch (Exception e) {
			exception = true;
		}
		assertTrue(exception);

		// ((FacetInterface) bo.getCustomFacets().get(0)).setContext(bo.getLibrary().getDefaultContextId());
		assertTrue(co.getFacet_Role().add("patchRole") == null);

		oEnum.addLiteral("patchLiteral");
		assertTrue(!oEnum.getLiterals().contains("patchLiteral"));
		assertTrue(!oEnum.getLiterals().contains(""));
		cEnum.addLiteral("patchLiteral");
		assertTrue(!cEnum.getLiterals().contains("patchLiteral"));

		bo.addAlias("patchAlias"); // should not work
		assertTrue(bo.getAliases().isEmpty());

		assertTrue(bo.isExtensible());
		bo.setExtensible(false); // should not work
		assertTrue(bo.isExtensible());

		// bo.setName("patchBO");
		// These should not error but have no effect on the tlObject.
		ConstraintHandler ch = simple.getConstraintHandler();
		int fd = ch.getFractionDigits();
		ch.setFractionDigits(3);
		assertTrue(fd == ch.getFractionDigits());
		ch.setMaxExclusive("3");
		ch.setMinExclusive("3");
		ch.setMaxLength(3);
		ch.setMinLength(3);
		ch.setMaxInclusive("3");
		ch.setMinInclusive("3");
		ch.setPattern("[A-Z]");

		checkValid(chain);
		checkCounts(chain);
	}

	private void addAndRemoveDoc(SimpleTypeNode ch) {
		String Text = "SampleText";
		Assert.assertNotNull(simple);
		simple.setDescription(Text);
		simple.getDocHandler().setMoreInfo(Text, 0);
		simple.setExample(Text);
		simple.setEquivalent(Text);

		assertTrue(simple.getDescription().equals(Text));
		assertTrue(simple.getDocHandler().getMoreInfo(0).equals(Text));
		assertTrue(simple.getExample(null).equals(Text));
		assertTrue(simple.getEquivalent(null).equals(Text));

		simple.setDescription("");
		simple.getDocHandler().setMoreInfo("", 0);
		simple.setExample("");
		simple.setEquivalent("");
	}

	private void addAndRemoveDoc(PropertyNode property) {
		String Text = "SampleText";
		Assert.assertNotNull(property);
		property.setDescription(Text);
		property.getDocHandler().setMoreInfo(Text, 0);
		property.setExample(Text);
		property.setEquivalent(Text);

		assertTrue(property.getDescription().equals(Text));
		assertTrue(property.getDocHandler().getMoreInfo(0).equals(Text));
		if (property.getExampleHandler() != null) {
			assertTrue(property.getExample(null).equals(Text));
			assertTrue(property.getEquivalent(null).equals(Text));
		}

		property.setDescription("");
		property.getDocHandler().setMoreInfo("", 0);
		property.setExample("");
		property.setEquivalent("");
	}

	// private void testPatch() {
	// Adding the simple to the patch causes it to be duplicated then create error finding.

	// Add a extension point
	// ePatch = new ExtensionPointNode(new TLExtensionPointFacet());
	// patchLibrary.addMember(ePatch);
	// ePatch.setExtendsType(core2.getSummaryFacet());
	// ePatch.addProperty(new IndicatorNode(ePatch, "patchInd"));
	// TotalDescendents += 1; // Number in whole chain
	// AggregateComplex += 1; // Number in the aggregates
	// PatchTotal += 1;
	// Assert.assertTrue(chain.isValid());
	// checkCounts(chain);

	// FIXME - compiler validation claims illegal patch
	// https://github.com/OpenTravel/OTM-DE-Compiler/issues/28
	//
	// Add an open enumeration
	// EnumerationOpenNode oe = ml.addOpenEnumToLibrary(patchLibrary, "oEnumInPatch");
	// Add a closed enumeration
	// SimpleTypeNode ce = ml.addClosedEnumToLibrary(patchLibrary, "cEnumInPatch");
	// TotalDescendents += 2; // Number in whole chain
	// AggregateSimple += 1;
	// AggregateComplex++;

	// Add a simple type
	// SimpleTypeNode simple = ml.addSimpleTypeToLibrary(patchLibrary, "simpleInPatch");
	// TotalDescendents += 1; // Number in whole chain
	// AggregateSimple += 1;
	//
	// assertNotNull(simple);
	// assertTrue(patchLibrary.isInChain());
	// assertTrue(patchLibrary.getChain() == chain);
	//
	// ValidationFindings findings = chain.validate();
	// assertTrue(chain.isValid());
	//
	// int afterCount = chain.getDescendants_NamedTypes().size();
	// checkCounts(chain);

	// TODO - validate the assertion that get descendants only does head
	// List<Node> after = chain.getComplexAggregate().getDescendants_NamedTypes();
	// }

	// Remember, getDescendents uses HashMap - only unique nodes.
	private void checkCounts(LibraryChainNode chain) {

		// Make sure all the base objects are accessible.
		List<Node> namedTypes = chain.getDescendants_LibraryMembersAsNodes();
		int namedTypeCnt = chain.getDescendants_LibraryMembersAsNodes().size();
		Assert.assertEquals(TotalDescendents, namedTypeCnt);

		// Make sure all the types are in the versions aggregate
		// Only gets "head" objects in a version chain by using the aggregate.
		AggregateNode aggregate = chain.getComplexAggregate();
		List<Node> nt = aggregate.getDescendants_LibraryMembersAsNodes();
		namedTypeCnt = aggregate.getDescendants_LibraryMembersAsNodes().size();
		Assert.assertEquals(AggregateComplex, namedTypeCnt);

		// FIXME - should be 8. The patch extension point should not be included because it is
		// wrapped up into the minor
		namedTypeCnt = chain.getSimpleAggregate().getDescendants_LibraryMembersAsNodes().size();
		Assert.assertEquals(AggregateSimple, namedTypeCnt);

		// Check the service
		namedTypeCnt = chain.getServiceAggregate().getDescendants_LibraryMembersAsNodes().size();
		Assert.assertEquals(1, namedTypeCnt);

		// Check counts against the underlying TL library
		// FIXME - needs to be fixed for version 1.6
		// for (LibraryNode lib : chain.getLibraries()) {
		// int libCnt = lib.getDescendants_LibraryMembers().size();
		// int tlCnt = lib.getTLaLib().getNamedMembers().size();
		// if (libCnt != tlCnt) {
		// // will not find contextual facets in v 1.5 because they are not in the library as children
		// List<Node> x = lib.getDescendants_LibraryMembers();
		// List<LibraryMember> y = lib.getTLaLib().getNamedMembers();
		// LOGGER.debug("HERE " + x.size() + y.size()); // Contextual Facets not in getDescendants
		// }
		// Assert.assertEquals(libCnt, tlCnt);
		// }
	}

	/**
	 * Create named objects in the passed library and set the counts.
	 */
	private static int TotalNamedObjects = 10; // The number of objects created by addNamedObjects

	/**
	 * Add named objects and set counts
	 */
	private void addNamedObjects(LibraryNode lib) {
		assertTrue(lib.isEditable());
		BusinessObjectNode nobo = ml.addBusinessObjectToLibrary(lib, "testBO");
		CoreObjectNode nco = ml.addCoreObjectToLibrary(lib, "testCO");
		VWA_Node nvwa = ml.addVWA_ToLibrary(lib, "testVWA");
		SimpleTypeNode nsimple = ml.addSimpleTypeToLibrary(lib, "testSimple");
		EnumerationClosedNode ncEnum = ml.addClosedEnumToLibrary(lib, "testCEnum");
		EnumerationOpenNode noEnum = ml.addOpenEnumToLibrary(lib, "testOEnum");
		ChoiceObjectNode nChoice = ml.addChoice(lib, "testChoice");

		ml.addNestedTypes(lib);
		ml.addExtendedBO(lib, secondLib, "test");
		// TODO - add choice
		// TODO - merge w/ Mock Library

		// Adds to the library
		nobo.addFacet("boCustomFacet", TLFacetType.CUSTOM);

		ServiceNode svc = new ServiceNode(nobo);
		svc.setName(nobo.getName() + "_Service");

		// Only update counts IFF working on the main chain
		if (lib.getChain() == chain) {
			bo = nobo;
			ch = nChoice;
			co = nco;
			vwa = nvwa;
			simple = nsimple;
			cEnum = ncEnum;
			oEnum = noEnum;

			TotalDescendents = lib.getChain().getDescendants_LibraryMembersAsNodes().size();
			AggregateComplex = lib.getChain().getComplexAggregate().getDescendants_LibraryMembersAsNodes().size();
			AggregateSimple = lib.getChain().getSimpleAggregate().getDescendants_LibraryMembersAsNodes().size();
		}
	}

	/**
	 * Run compiler validation. Print errors if found.
	 * 
	 * @param chain
	 */
	private void checkValid(Node chain) {
		LibraryChainNode cn = chain.getChain();
		if (cn.isValid())
			return;
		ValidationFindings findings = cn.validate();
		for (String f : findings.getAllValidationMessages(FindingMessageFormat.IDENTIFIED_FORMAT)) {
			LOGGER.debug("Finding: " + f);
		}
		assertTrue("Chain must be valid.", cn.isValid());
	}
}
