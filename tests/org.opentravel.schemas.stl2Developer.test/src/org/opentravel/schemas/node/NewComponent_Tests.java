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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemacompiler.event.ModelElementListener;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLOpenEnumeration;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItemState;
import org.opentravel.schemacompiler.saver.LibrarySaveException;
import org.opentravel.schemas.node.interfaces.ExtensionOwner;
import org.opentravel.schemas.node.interfaces.VersionedObjectInterface;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.listeners.NodeIdentityListener;
import org.opentravel.schemas.node.properties.ElementNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.typeProviders.ChoiceObjectNode;
import org.opentravel.schemas.node.typeProviders.EnumerationClosedNode;
import org.opentravel.schemas.node.typeProviders.EnumerationOpenNode;
import org.opentravel.schemas.node.typeProviders.SimpleTypeNode;
import org.opentravel.schemas.node.typeProviders.VWA_Node;
import org.opentravel.schemas.node.typeProviders.facetOwners.BusinessObjectNode;
import org.opentravel.schemas.node.typeProviders.facetOwners.CoreObjectNode;
import org.opentravel.schemas.testUtils.BaseRepositoryTest;
import org.opentravel.schemas.testUtils.NodeTesters;
import org.opentravel.schemas.testUtils.NodeTesters.TestNode;
import org.opentravel.schemas.trees.repository.RepositoryNode;
import org.opentravel.schemas.utils.LibraryNodeBuilder;
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests the creation of new components.
 * 
 * @author Dave Hollander
 * 
 */
public class NewComponent_Tests extends BaseRepositoryTest {
	private final static Logger LOGGER = LoggerFactory.getLogger(NewComponent_Tests.class);

	TestNode nt = new NodeTesters().new TestNode();
	ModelNode model = null;
	TestNode tn = new NodeTesters().new TestNode();

	// LoadFiles lf = new LoadFiles();
	Library_FunctionTests lt = new Library_FunctionTests();
	// MockLibrary ml = null;
	// LibraryNode ln = null;
	// MainController mc;
	// DefaultProjectController pc;
	// ProjectNode defaultProject;
	EditNode en;

	@Before
	public void beforeAllTests() {
		// mc = OtmRegistry.getMainController();
		// ml = new MockLibrary();
		// pc = (DefaultProjectController) mc.getProjectController();
		// defaultProject = pc.getDefaultProject();

		en = new EditNode(ln);
		en.setName("SOME_Component");
		en.setDescription("THIS IS A DESCRIPTION");

	}

	public NewComponent_Tests() {
		// mc = OtmRegistry.getMainController();
		// lf = new LoadFiles();
	}

	@Test
	public void newComponentTests() throws Exception {
		// MainController mc = OtmRegistry.getMainController();
		// LoadFiles lf = new LoadFiles();
		LibraryNode noService = lf.loadFile2(mc);
		LibraryNode hasService = lf.loadFile1(mc);

		for (Node n : Node.getAllUserLibraries()) {
			n.visitAllNodes(nt);
		}

		// createNewComponents(noService);

		for (Node n : Node.getAllUserLibraries()) {
			n.visitAllNodes(nt);
		}
	}

	private LibraryNode majorLibrary = null;
	private LibraryNode minorLibrary = null;
	private LibraryNode patchLibrary = null;
	private LibraryChainNode chain = null;

	@Test
	public void baseTypeTests() throws LibrarySaveException, RepositoryException {

		// Given - an editable major version library in the respository
		LOGGER.debug("Before test.");
		ProjectNode uploadProject = createProject("ToUploadLibrary", getRepositoryForTest(), "Test");
		majorLibrary = LibraryNodeBuilder.create("TestLibrary", getRepositoryForTest().getNamespace() + "/Test/T2",
				"prefix", new Version(1, 0, 0)).build(uploadProject, pc);
		chain = rc.manage(getRepositoryForTest(), Collections.singletonList(majorLibrary)).get(0);
		boolean locked = rc.lock(chain.getHead());
		assertTrue("Repository must successfully lock library.", locked);
		assertTrue("Library in repo must be editable.", majorLibrary.isEditable());
		assertTrue("Head library must be managed WIP.",
				RepositoryItemState.MANAGED_WIP == chain.getHead().getProjectItem().getState());
		LOGGER.debug("Managed major library in repository.");

		// Given - an unmanaged Library to contain objects created to be extended.
		LibraryNode eln = ml.createNewLibrary(defaultProject.getNSRoot() + "/extensions", "testE", defaultProject);
		ml.addBusinessObjectToLibrary(eln, "ExtensionBase");
		assertTrue("Unmanaged library must be editable.", eln.isEditable());

		// Given - another unmanaged library with content
		ln = ml.createNewLibrary(defaultProject.getNSRoot(), "test", defaultProject);
		assertTrue("Unmanaged library must be editable.", ln.isEditable());
		ml.addOneOfEach(ln, "Unmanaged");
		// When - adding an extension point to unmanaged
		ml.addEP(ln, eln); // add ep to ln referencing BO in eln
		// Then - must be valid extension
		testExtension(ln, eln);
		ml.check(chain);

		String ns1 = ln.getNamespace();
		String ns2 = majorLibrary.getNamespace();
		LOGGER.debug("Must be different namespaces." + ns1 + ns2);

		// Test in a managed major
		ml.addOneOfEach(majorLibrary, "Major");
		// When - adding an extension point to managed major library
		ml.addEP(majorLibrary, eln);
		// Then - must be valid extension
		testExtension(majorLibrary, eln);
		ml.check(chain);

		// FIXME
		// The library must be managed to be promoted or versioned.
		// TLLibraryStatus status = ln.getStatus();
		// rc.promote(ln, TLLibraryStatus.FINAL);
		// minorLibrary = rc.createMinorVersion(chain.getHead());
		// assertNotNull(minorLibrary);
		// ml.addOneOfEach(minorLibrary, "Minor");
		// ml.addEP(minorLibrary, eln);
		// testExtension(minorLibrary, eln);
		// checkValid(chain);
		// testNewVersion(minorLibrary, majorLibrary);
		// checkValid(chain);

		// patchLibrary = rc.createPatchVersion(chain.getHead());
		// these objects should and do throw validation errors
		// ml.addOneOfEach(patchLibrary, "Patch");
		// ml.addEP(patchLibrary, minorLibrary);
		// testExtension(patchLibrary, eln);
		// checkValid(chain);
	}

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

	@Override
	public RepositoryNode getRepositoryForTest() {
		for (RepositoryNode rn : rc.getAll()) {
			if (rn.isRemote()) {
				return rn;
			}
		}
		throw new IllegalStateException("Missing remote repository. Check your configuration.");
	}

	// eln is library to allow extension points must extend content in a different library
	/**
	 * 
	 * @param ln
	 *            library containing the extension point
	 * @param eln
	 *            library containing the referenced object
	 */
	private void testExtension(LibraryNode ln, LibraryNode eln) {
		List<Node> namedTypes = ln.getDescendants_LibraryMembersAsNodes();
		for (Node n : namedTypes) {
			// Then - the EP exists
			// Then - the EP has a referenced object
			// Then - the referenced object exists in eln
			// Then - the referenced object where-extended include EP

			// LOGGER.debug("Testing: " + n);
			// if (n instanceof ExtensionOwner) {
			// LOGGER.debug("Extension owner: " + n);
			// Node nc = NodeFactory.newComponentMember(n, eln, n.getName());
			// assertNotNull(nc.getLibrary());
			// if (nc instanceof ExtensionOwner)
			// ((ExtensionOwner) nc).setExtension(n);
			// assertNotNull(((ExtensionOwner) nc).getExtensionBase());
			// assertEquals(n, ((ExtensionOwner) nc).getExtensionBase());
			// }
		}
	}

	private void testNewVersion(LibraryNode ln, LibraryNode major) {
		List<Node> namedTypes = major.getDescendants_LibraryMembersAsNodes();
		for (Node n : namedTypes) {
			if (n instanceof VersionedObjectInterface) {
				LOGGER.debug("Version Extension owner: " + n);
				Node nc = ((VersionedObjectInterface) n).createMinorVersionComponent();
				assertNotNull(nc);
				assertNotNull(nc.getLibrary());
				assertNotNull(((ExtensionOwner) nc).getExtensionBase());
				assertEquals(n, ((ExtensionOwner) nc).getExtensionBase());
				// Verify there is only one of these in the library
				for (Node t : nc.getParent().getChildren())
					if (t.getName().equals(nc.getName()))
						LOGGER.debug("Found " + t);
			}
		}
	}

	// TODO - move this test to listeners tests
	@Test
	public void checkListeners() {
		ln = ml.createNewLibrary(defaultProject.getNSRoot(), "test", defaultProject);
		// 2/2016 - not assigned in constructors - assert ln.getNode(ln.getTLModelObject().getListeners()) == ln;

		// check listeners in built-ins
		Node string = NodeFinders.findNodeByName("string", ModelNode.XSD_NAMESPACE);
		assert hasOneNamedTypeListener(string);
		assert string.getNode(string.getTLModelObject().getListeners()) == string;

		// Check to assure one and only one listener is created with each node
		BusinessObjectNode bo = (BusinessObjectNode) NodeFactory.newLibraryMember(new TLBusinessObject());
		assert hasOneNamedTypeListener(bo);
		bo.setName("BO");
		ln.addMember(bo);
		assert hasOneNamedTypeListener(bo);
		assert bo.getNode(bo.getTLModelObject().getListeners()) == bo;
		assert bo.getNode(((Node) bo.getFacet_ID()).getTLModelObject().getListeners()) == bo.getFacet_ID();

		CoreObjectNode core = (CoreObjectNode) NodeFactory.newLibraryMember(new TLCoreObject());
		assert hasOneNamedTypeListener(core);
		core.setName("CO");
		ln.addMember(core);
		assert hasOneNamedTypeListener(core);
		assert core.getNode(core.getTLModelObject().getListeners()) == core;
		assert core.getNode(((Node) core.getFacet_Summary()).getTLModelObject().getListeners()) == core
				.getFacet_Summary();

		VWA_Node vwa = (VWA_Node) NodeFactory.newLibraryMember(new TLValueWithAttributes());
		assert hasOneNamedTypeListener(vwa);
		vwa.setName("VWA");
		ln.addMember(vwa);
		assert hasOneNamedTypeListener(vwa);
		assert vwa.getNode(vwa.getTLModelObject().getListeners()) == vwa;

		ChoiceObjectNode choice = (ChoiceObjectNode) NodeFactory.newLibraryMember(new TLChoiceObject());
		assert hasOneNamedTypeListener(choice);
		choice.setName("Choice");
		ln.addMember(choice);
		assert hasOneNamedTypeListener(choice);
		assert choice.getNode(choice.getTLModelObject().getListeners()) == choice;

		SimpleTypeNode simple = (SimpleTypeNode) NodeFactory.newLibraryMember(new TLSimple());
		assert hasOneNamedTypeListener(simple);
		simple.setName("Simple");
		ln.addMember(simple);
		assert hasOneNamedTypeListener(simple);
		assert simple.getNode(simple.getTLModelObject().getListeners()) == simple;

		EnumerationClosedNode ec = (EnumerationClosedNode) NodeFactory.newLibraryMember(new TLClosedEnumeration());
		assert hasOneNamedTypeListener(ec);
		ec.setName("ec");
		ln.addMember(ec);
		assert hasOneNamedTypeListener(ec);
		assert ec.getNode(ec.getTLModelObject().getListeners()) == ec;

		EnumerationOpenNode eo = (EnumerationOpenNode) NodeFactory.newLibraryMember(new TLOpenEnumeration());
		assert hasOneNamedTypeListener(eo);
		eo.setName("eo");
		ln.addMember(eo);
		assert hasOneNamedTypeListener(eo);
		assert eo.getNode(eo.getTLModelObject().getListeners()) == eo;

		// TODO - other property types
		PropertyNode p1 = new ElementNode(bo.getFacet_ID(), "i1");
		assert hasOneNamedTypeListener(p1);
		assert p1.getNode(p1.getTLModelObject().getListeners()) == p1;
	}

	public boolean hasOneNamedTypeListener(Node n) {
		int cnt = 0;
		for (ModelElementListener l : n.getTLModelObject().getListeners())
			if (l instanceof NodeIdentityListener) {
				Assert.assertEquals(n, ((NodeIdentityListener) l).getNode());
				cnt++;
			}
		return cnt == 1;
	}

	// @Test
	// public void createAllTypes() {
	// ln = ml.createNewLibrary(defaultProject.getNSRoot(), "test", defaultProject);
	// BusinessObjectNode bo = ml.addBusinessObjectToLibrary(ln, "testBO");
	// ServiceNode svc;
	// if (ln.getServiceRoot() == null)
	// svc = new ServiceNode(ln);
	// else
	// svc = (ServiceNode) ln.getServiceRoot();
	// OperationNode operation = new OperationNode(svc, "Op1");
	// EditNode editNode = new EditNode(ln);
	// String name = "testEditNode";
	// String description = "Testing creating new objects.";
	// editNode.setName(name);
	// editNode.setDescription(description);
	// Node n2;
	//
	// NodeFactory factory = new NodeFactory();
	// for (ComponentNodeType type : ComponentNodeType.values()) {
	// String tName = type.getDescription();
	// Assert.assertNotNull(ComponentNodeType.fromString(tName));
	//
	// if (type.equals(ComponentNodeType.ALIAS))
	// n2 = factory.newComponent(bo, type);
	// else {
	// // LOGGER.debug("Ready to create a " + type.getDescription());
	// n2 = factory.newComponent(editNode, type);
	// // null returned when type is not supported
	// if (n2 != null) {
	// if (type != ComponentNodeType.EXTENSION_POINT) {
	// assertTrue("Names must be equal ignoring case.", name.equalsIgnoreCase(n2.getName()));
	// }
	// Assert.assertEquals(description, n2.getDescription());
	// Assert.assertNotNull(n2.getParent());
	// Assert.assertEquals(ln, n2.getLibrary());
	// }
	// }
	// }
	// }

	// public void createNewComponents(LibraryNode ln) {
	// Node newOne, newBO, newCO, newChoice;
	// en = new EditNode(ln);
	// ln.setEditable(true);
	// en.setName("TestObject");
	//
	// NodeFactory factory = new NodeFactory();
	// // Create Business Object
	// newBO = factory.newComponent(en, ComponentNodeType.BUSINESS);
	// newBO.setName(newBO.getName() + "Business");
	// nt.visit(newBO);
	// addProperties((ComponentNode) newBO); // properties first so alias is not counted as child
	// newBO.visitAllNodes(nt);
	// newOne = factory.newComponent(newBO, ComponentNodeType.ALIAS);
	// newOne.setName(newOne.getName() + "Alias");
	// nt.visit(newBO);
	// Assert.assertTrue(newBO instanceof BusinessObjectNode);
	//
	// // Create new core object.
	// newCO = factory.newComponent(en, ComponentNodeType.CORE);
	// newCO.setName(newCO.getName() + "Core");
	// addProperties((ComponentNode) newCO);
	// addRoles((CoreObjectNode) newCO);
	// newCO.visitAllNodes(nt);
	// newOne = factory.newComponent(newCO, ComponentNodeType.ALIAS);
	// nt.visit(newCO);
	// Assert.assertTrue(newCO instanceof CoreObjectNode);
	//
	// newChoice = factory.newComponent(en, ComponentNodeType.CHOICE);
	// newCO.setName(newCO.getName() + "Choice");
	//
	// newOne = factory.newComponent(en, ComponentNodeType.VWA);
	// newOne.setName(newOne.getName() + "VWA");
	// nt.visit(newOne);
	// Assert.assertTrue(newOne instanceof VWA_Node);
	//
	// // FIXME - you can not extend an object from the same library as the EP
	// // newOne = en.newComponent(ComponentNodeType.EXTENSION_POINT);
	// // newOne.setExtendsType(((BusinessObjectNode) newBO).getSummaryFacet());
	// // newOne.setName(newOne.getName() + "EP");
	// // nt.visit(newOne);
	//
	// newOne = factory.newComponent(en, ComponentNodeType.CLOSED_ENUM);
	// newOne.setName(newOne.getName() + "CE");
	// addLiterals(newOne);
	// nt.visit(newOne);
	//
	// newOne = factory.newComponent(en, ComponentNodeType.OPEN_ENUM);
	// newOne.setName(newOne.getName() + "OE");
	// nt.visit(newOne);
	//
	// newOne = factory.newComponent(en, ComponentNodeType.SIMPLE);
	// newOne.setName(newOne.getName() + "Simple");
	// nt.visit(newOne);
	//
	// en.setTLType(newBO); // used as subject of CRUD operations
	// newOne = factory.newComponent(en, ComponentNodeType.SERVICE);
	// newOne.setName(newOne.getName() + "SVC");
	// nt.visit(newOne);
	// }

	// private void addProperties(ComponentNode n) {
	// Assert.assertNotNull(n.getFacet_Summary());
	// FacetInterface po = n.getFacet_Summary();
	//
	// PropertyNode pne = new ElementNode(po, "Property");
	// PropertyNode pna = new AttributeNode(po, "Attribute");
	// PropertyNode pni = new IndicatorNode(po, "Indicator");
	// PropertyNode pner = new ElementReferenceNode(po);
	// Assert.assertEquals(4, n.getFacet_Summary().getChildren().size());
	// }
	//
	// private void addRoles(CoreObjectNode n) {
	// RoleFacetNode rf = n.getFacet_Role();
	// Assert.assertNotNull(rf);
	// PropertyNode pnr1 = new RoleNode(rf, "Role1");
	// PropertyNode pnr2 = new RoleNode(rf, "Role2");
	// PropertyNode pnr3 = new RoleNode(rf, "Role3");
	// Assert.assertEquals(3, n.getFacet_Role().getChildren().size());
	// }
	//
	// private void addLiterals(Node n) {
	// Assert.assertNotNull(n);
	// if (n instanceof Enumeration) {
	// ((Enumeration) n).addLiteral("lit1");
	// ((Enumeration) n).addLiteral("lit2");
	// ((Enumeration) n).addLiteral("lit3");
	// }
	// Assert.assertEquals(3, n.getChildren().size());
	// }

}
