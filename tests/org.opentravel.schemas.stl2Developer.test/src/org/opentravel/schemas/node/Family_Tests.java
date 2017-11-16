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

import org.junit.Before;
import org.opentravel.schemas.controllers.DefaultProjectController;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.testUtils.MockLibrary;
import org.opentravel.schemas.testUtils.NodeTesters;
import org.opentravel.schemas.types.TestTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * July 2016 - family behavior removed from application.
 * 
 * @author Dave Hollander
 * 
 */
public class Family_Tests {
	static final Logger LOGGER = LoggerFactory.getLogger(Family_Tests.class);

	ModelNode model = null;
	TestTypes tt = new TestTypes();

	NodeTesters nt = new NodeTesters();
	LoadFiles lf = new LoadFiles();
	Library_FunctionTests lt = new Library_FunctionTests();
	MockLibrary ml = null;
	LibraryNode ln = null;
	MainController mc;
	DefaultProjectController pc;
	ProjectNode defaultProject;

	@Before
	public void beforeAllTests() {
		// mc = new MainController();
		// ml = new MockLibrary();
		// pc = (DefaultProjectController) mc.getProjectController();
		// defaultProject = pc.getDefaultProject();
	}

	// @Test
	// public void familyConstructors() {

	// // Given two libraries, one managed one not managed
	// ln = ml.createNewLibrary("http://www.test.com/test1", "test1", defaultProject);
	// LibraryNode ln_inChain = ml.createNewLibrary("http://www.test.com/test1c", "test1c", defaultProject);
	// new LibraryChainNode(ln_inChain);
	// ln_inChain.setEditable(true);
	//
	// // Given two simple types with same family name, only one in library
	// Node n1 = (Node) ml.createSimple("s_1");
	// Node n2 = (Node) ml.createSimple("s_2");
	// ln.addMember(n1);
	// assertTrue("Library must be editable.", ln.isEditable());
	// assertTrue("Managed library must be editable", ln_inChain.isEditable());
	// assertTrue("Must have same family name", n1.getFamily().equals(n2.getFamily()));
	//
	// // When - new family node from the 2 nodes (unmanaged)
	// FamilyNode fn = new FamilyNode(n1, n2);
	// // NOTE - there will be a warning from Node.remove() because n2 was not in library.
	//
	// // Then - check all constructor managed values
	// checkNewFamily(ln, fn, n1, n2);
	//
	// //
	// // Given - node in managed library
	// Node nc1 = (Node) ml.createSimple("m_1");
	// ln_inChain.addMember(nc1);
	// assertTrue("Node is in managed library.", nc1.getParent() instanceof VersionNode);
	//
	// // Given - node in managed library but added to bypass family processing
	// Node nc2 = (Node) ml.createSimple("m_2");
	// ln_inChain.getTLLibrary().addNamedMember((LibraryMember) nc2.getTLModelObject());
	// ln_inChain.simpleRoot.linkChild(nc2, false); // no family processing
	// // nc1 is version wrapped, nc2 is not.
	//
	// // When - family node created
	// fn = new FamilyNode(nc1, nc2);
	//
	// // Then
	// checkNewFamily(ln_inChain, fn, nc1, nc2);
	//
	// //
	// // When - family constructor (AggregateFamilyNode usage)
	// final String TestName = "TestFamily";
	// fn = new FamilyNode(TestName, ln_inChain.getComplexRoot());
	// // Then
	// assertTrue("Family node created.", fn != null);
	// assertTrue("Library contains family.", ln_inChain.getComplexRoot().getChildren().contains(fn));
	//
	// LOGGER.debug("Family Test constructor test complete.");
	// }

	// public void checkNewFamily(LibraryNode ln, FamilyNode fn, Node n1, Node n2) {
	// assertTrue("All parameters are not null.", fn != null && ln != null && n1 != null && n2 != null);
	// assertTrue("Family name set the same as child.", fn.getName().equals(n1.getFamily()));
	// assertTrue("Family library set.", ln == fn.getLibrary());
	// assertTrue("Family identity set.", fn.getIdentity().startsWith("Family"));
	//
	// Node root = ln.getSimpleRoot();
	// if (fn.getParent() == ln.getComplexRoot())
	// root = ln.getComplexRoot();
	// assertTrue("Family parent is root.", fn.getParent() == root);
	// assertTrue("Root does not contain node.", !root.getChildren().contains(n1));
	// assertTrue("Root does not contain node.", !root.getChildren().contains(n2));
	// assertTrue("Root has only one child.", root.getChildren().size() == 1);
	// assertTrue("Root contains family.", root.getChildren().contains(fn));
	//
	// assertTrue("Family has two members.", fn.getChildren().size() == 2);
	// if (n1.getVersionNode() == null) {
	// assertTrue("Node parent is the family.", n1.getParent() == fn);
	// assertTrue("Family contains the node.", fn.getChildren().contains(n1));
	// } else {
	// // bad test - it assumes version node implementation not used in constructor
	// assertTrue("Node parent is still version node.", n1.getParent() == n1.getVersionNode());
	// assertTrue("Version Node parent is the family.", n1.getVersionNode().getParent() == fn);
	// assertTrue("Family contains the version node.", fn.getChildren().contains(n1.getVersionNode()));
	// }
	// if (n2.getVersionNode() == null) {
	// assertTrue("Node parent is the family.", n2.getParent() == fn);
	// assertTrue("Family contains the node.", fn.getChildren().contains(n2));
	// } else {
	// assertTrue("Version Node parent is the family.", n2.getVersionNode().getParent() == fn);
	// assertTrue("Family contains the version node.", fn.getChildren().contains(n2.getVersionNode()));
	// }
	// }

	// // Given - a managed library with two simple types in a family
	// public FamilyNode createFamilyAndManagedLibrary() {
	// LibraryChainNode lcn = ml.createNewManagedLibrary_Empty("http://www.test.com/test1", "test1", defaultProject);
	// ln = lcn.getHead();
	// Node lc1 = (Node) ml.createSimple("f1_nc1");
	// Node lc2 = (Node) ml.createSimple("f1_nc2");
	// ln.addMember(lc1); // uses Node.linkMember()
	// ln.addMember(lc2);
	//
	// FamilyNode fn = null;
	// for (Node child : ln.getSimpleRoot().getChildren())
	// if (child instanceof FamilyNode)
	// fn = (FamilyNode) child;
	// checkNewFamily(ln, fn, lc1, lc2);
	// return fn;
	// }

	// @Test
	// public void usageLinkChildInUnmangedLibrary() {
	// // Given - conditions found in Node.linkChild()
	// ln = ml.createNewLibrary("http://www.test.com/test1", "test1", defaultProject);
	// ln.setEditable(true);
	// Node lc1 = (Node) ml.createSimple("f1_nc1");
	// Node lc2 = (Node) ml.createSimple("f1_nc2");
	// ln.addMember(lc1);
	// assertTrue("First child is in library.", ln.getSimpleRoot().getChildren().contains(lc1));
	//
	// // When - linking 2nd child
	// ln.getSimpleRoot().linkChild(lc2, true);
	//
	// // Then - should have valid new family
	// FamilyNode fn = null;
	// for (Node child : ln.getSimpleRoot().getChildren())
	// if (child instanceof FamilyNode)
	// fn = (FamilyNode) child;
	// checkNewFamily(ln, fn, lc1, lc2);
	//
	// // When - linking different name
	// Node lc3 = (Node) ml.createSimple("fred");
	// ln.getSimpleRoot().linkChild(lc3, true);
	// // Then - no change to family
	// assertTrue("Family does not contain node.", !fn.getChildren().contains(lc3));
	// assertTrue("Simple root contains node.", ln.getSimpleRoot().getChildren().contains(lc3));
	//
	// // When - linking node with name root
	// Node lc4 = (Node) ml.createSimple("f1");
	// ln.getSimpleRoot().linkChild(lc4, true);
	// // Then - added to family
	// assertTrue("Family contains node.", fn.getChildren().contains(lc4));
	// assertTrue("Simple root does not contains node.", !ln.getSimpleRoot().getChildren().contains(lc4));
	//
	// }

	// @Test
	// public void usageLinkChildInMangedLibrary() {
	// // Given - conditions found in Node.linkChild()
	// LibraryChainNode lcn = ml.createNewManagedLibrary("http://www.test.com/test1", "test1", defaultProject);
	// ln = lcn.getHead();
	// ln.setEditable(true);
	// Node lc1 = (Node) ml.createSimple("f1_nc1");
	// Node lc2 = (Node) ml.createSimple("f1_nc2");
	// ln.addMember(lc1);
	// assertTrue("First child is in library.", ln.getSimpleRoot().getChildren().contains(lc1.getVersionNode()));
	//
	// // When - linking 2nd child
	// ln.getSimpleRoot().linkChild(lc2, true);
	//
	// // Then - should have valid new family
	// FamilyNode fn = null;
	// for (Node child : ln.getSimpleRoot().getChildren())
	// if (child instanceof FamilyNode)
	// fn = (FamilyNode) child;
	// checkNewFamily(ln, fn, lc1, lc2);
	//
	// // When - linking different name
	// Node lc3 = (Node) ml.createSimple("fred");
	// ln.getSimpleRoot().linkChild(lc3, true);
	// // Then - no change to family
	// assertTrue("Family does not contain node.", !fn.getChildren().contains(lc3));
	// assertTrue("Simple root contains node.", ln.getSimpleRoot().getChildren().contains(lc3));
	//
	// // When - linking node with name root
	// Node lc4 = (Node) ml.createSimple("f1");
	// ln.getSimpleRoot().linkChild(lc4, true);
	// // Then - added to family
	// assertTrue("Family contains node.", fn.getChildren().contains(lc4));
	// assertTrue("Simple root does not contains node.", !ln.getSimpleRoot().getChildren().contains(lc4));
	//
	// }

	// @Test
	// public void methodGetDescendants_NamedTypes() {
	// // on 6/2016 this method was overridden. Confirm it need not be.
	//
	// // Given - managed library
	// LibraryChainNode lcn = ml.createNewManagedLibrary_Empty("http://www.test.com/test1", "test1", defaultProject);
	// ln = lcn.getHead();
	// ln.setEditable(true);
	// int initialCount = ln.getDescendants_NamedTypes().size();
	//
	// // When - adding two members with same family name
	// Node lc1 = (Node) ml.createSimple("f1_nc1");
	// Node lc2 = (Node) ml.createSimple("f1_nc2");
	// ln.addMember(lc1); // uses Node.linkMember()
	// ln.addMember(lc2);
	//
	// FamilyNode simpleFamily = null;
	// for (Node child : ln.getSimpleRoot().getChildren())
	// if (child instanceof FamilyNode)
	// simpleFamily = (FamilyNode) child;
	// checkNewFamily(ln, simpleFamily, lc1, lc2);
	//
	// // Then - there should be 2 descendants
	// assertTrue("Library should have 2 more descendants.", ln.getDescendants_NamedTypes().size() == initialCount + 2);
	//
	// // When - adding two complex types
	// Node lc3 = (Node) ml.createComplex("f2_cc1");
	// Node lc4 = (Node) ml.createComplex("f2_cc2");
	// ln.addMember(lc3);
	// ln.addMember(lc4);
	//
	// FamilyNode complexFamily = null;
	// for (Node child : ln.getComplexRoot().getChildren())
	// if (child instanceof FamilyNode)
	// complexFamily = (FamilyNode) child;
	// checkNewFamily(ln, complexFamily, lc3, lc4);
	//
	// // Then - there should be 4 named descendants
	// assertTrue("Library should have 4 more descendants.", ln.getDescendants_NamedTypes().size() == initialCount + 4);
	// }

	// @Test
	// public void methods() {
	// // Given - a managed library with two simple types in a family
	// FamilyNode fn = createFamilyAndManagedLibrary();
	//
	// // Then - make sure the methods run
	// assertTrue("Method returned value.", fn.getImage() == null);// no registry
	// assertTrue("Method returned value.", !fn.getComponentType().isEmpty());
	// assertTrue("Method returned value.", !fn.getName().isEmpty());
	// assertTrue("Method returned value.", !fn.getLabel().isEmpty());
	//
	// assertTrue("Family has type providers.", fn.hasChildren_TypeProviders() == true);
	//
	// assertTrue("Family is not importable.", fn.isImportable() == false);
	// assertTrue("Family is navigation.", fn.isNavigation() == true);
	// }

	// @Test
	// public void methodSetName() {
	// // Given - a managed library with two simple types in a family
	// FamilyNode fn = createFamilyAndManagedLibrary();
	// final String newName = "NF1";
	//
	// // When - setting the family name
	// fn.setName(newName);
	//
	// // Then - the members should also have their name changed
	// for (Node n : fn.getChildren())
	// assertTrue("Name prefix changed.", n.getName().startsWith(newName));
	// }

	// @Test
	// public void methodDelete() {
	// // Given - a managed library with two simple types in a family
	// FamilyNode fn = createFamilyAndManagedLibrary();
	// List<Node> kids = fn.getChildren();
	//
	// // When - deleting the family
	// fn.delete();
	//
	// // Then - family and all kids are deleted
	// assertTrue("Simple root is empty.", ln.getSimpleRoot().getChildren().isEmpty());
	// assertTrue("Family node is deleted.", fn.deleted);
	// for (Node child : kids)
	// assertTrue("Child is deleted.", child.deleted);
	// }

	// @Test
	// public void familyMethods() {
	//
	// // makeFamilyName
	// // add child to family
	// // new FamilyNode(n, peer)
	//
	// // Setup
	// ln = ml.createNewLibrary("http://www.test.com/test1", "test1", defaultProject);
	// Node n1 = new SimpleTypeNode(new TLSimple());
	// final Node type = NodeFinders.findNodeByName("int", ModelNode.XSD_NAMESPACE);
	// n1.setName("s_1");
	// ((SimpleTypeNode) n1).setAssignedType((TypeProvider) type);
	// Node n2 = new SimpleTypeNode(new TLSimple());
	// n2.setName("s_2");
	// ((SimpleTypeNode) n2).setAssignedType((TypeProvider) type);
	//
	// // Add simples to an un-managed library
	// ln.addMember(n1);
	// ln.addMember(n2);
	// Assert.assertTrue(n1.getParent() instanceof FamilyNode);
	// new LibraryChainNode(ln);
	// // n2.addChildToFamily(n1); // can't test direct because adding to library does family
	// Assert.assertTrue(n1.getParent() instanceof VersionNode);
	// Assert.assertTrue(n1.getParent().getParent() instanceof FamilyNode);
	//
	// ln = ml.createNewLibrary("http://www.test.com/test", "test", defaultProject);
	// ml.addSimpleTypeToLibrary(ln, "C1_1");
	// ml.addSimpleTypeToLibrary(ln, "C1_2");
	// Node c1 = ln.findNodeByName("C1_1");
	// Assert.assertNotNull(ln.findNodeByName("C1_1"));
	// Assert.assertTrue(c1.getParent() instanceof FamilyNode);
	// Assert.assertTrue(c1.getParent().getParent() instanceof NavNode);
	// FamilyNode fn = (FamilyNode) c1.getParent();
	// Assert.assertEquals(2, fn.getChildren().size());
	//
	// new LibraryChainNode(ln);
	// c1 = ln.findNodeByName("C1_1");
	// // List<Node> kids = ln.getDescendants_NamedTypes();
	// Assert.assertNotNull(ln.findNodeByName("C1_1"));
	// Assert.assertTrue(c1.getParent() instanceof VersionNode);
	// Assert.assertTrue(c1.getParent().getParent() instanceof FamilyNode);
	// fn = (FamilyNode) c1.getParent().getParent();
	// Assert.assertEquals(2, fn.getChildren().size());
	// }

	// @Test
	// public void mockFamilyTest() {
	// ln = ml.createNewLibrary("http://www.test.com/test", "test", defaultProject);
	// mockFamilyTest(ln);
	// ln = ml.createNewLibrary("http://www.test.com/test2", "test2", defaultProject);
	// new LibraryChainNode(ln);
	// ln.setEditable(true);
	// mockFamilyTest(ln);
	// }

	public void mockFamilyTest(LibraryNode ln) {
		// ln = ml.createNewLibrary("http://www.sabre.com/test", "test", defaultProject);
		// Node simpleNav = null;
		// if (!ln.isEditable())
		// return;
		//
		// // Find the simple type node.
		// for (Node n : ln.getChildren()) {
		// if (n.getName().equals("Simple Objects"))
		// simpleNav = n;
		// }
		// Assert.assertNotNull(simpleNav);
		//
		// ml.addSimpleTypeToLibrary(ln, "A_a");
		// ml.addSimpleTypeToLibrary(ln, "B_b");
		// ml.addSimpleTypeToLibrary(ln, "C");
		// Assert.assertEquals(3, simpleNav.getChildren().size());
		//
		// // These three should create families
		// ml.addSimpleTypeToLibrary(ln, "A_a1"); // uses LibraryNode.addMember()
		// ml.addSimpleTypeToLibrary(ln, "B_b1");
		// ml.addSimpleTypeToLibrary(ln, "C");
		// Assert.assertEquals(3, simpleNav.getChildren().size());
		// // Fixed 1/12/15 - does not put C family into the aggregates
		// // Fixed 1/19/15 - 2 families and two "C" nodes in aggregates.
		// if (ln.isInChain())
		// Assert.assertEquals(4, ln.getChain().getSimpleAggregate().getChildren().size());
	}

	// @Test
	// public void FamilyTests() throws Exception {
	// MainController mc = new MainController();
	// LoadFiles lf = new LoadFiles();
	// model = mc.getModelNode();
	//
	// lf.loadTestGroupA(mc);
	// for (LibraryNode ln : model.getUserLibraries()) {
	// int beforeCnt = ln.getDescendants_NamedTypes().size();
	// tt.visitAllNodes(ln);
	//
	// siblingTest(ln);
	// tt.visitAllNodes(ln);
	// Assert.assertEquals(beforeCnt, ln.getDescendants_NamedTypes().size());
	//
	// deleteFamilies(ln);
	// // You can't visit nodes because the deleted families may be used as a type on one of
	// // the remaining types. tt.visitAllNodes(ln);
	// }
	// }

	// private void deleteFamilies(LibraryNode ln) {
	// ArrayList<FamilyNode> families = new ArrayList<FamilyNode>();
	// for (INode nav : ln.getChildren())
	// for (Node n : nav.getChildren()) {
	// if (n instanceof FamilyNode)
	// families.add((FamilyNode) n);
	// }
	// for (FamilyNode f : families) {
	// Node parent = f.getParent();
	// Assert.assertNotNull(parent);
	// Assert.assertTrue(f instanceof FamilyNode);
	// Assert.assertTrue(f.getChildren().size() > 0);
	// ArrayList<Node> kids = new ArrayList<Node>(f.getChildren());
	// for (INode k : kids) {
	// k.removeFromLibrary();
	// }
	// Assert.assertEquals(0, f.getChildren().size());
	// Assert.assertNull(f.getParent());
	//
	// }
	// }

	// private void siblingTest(LibraryNode ln) {
	// Node nn1 = null;
	// Node nn2 = null;
	// Node nn3 = null;
	// int siblingCount = 0;
	//
	// for (Node n : ln.getDescendants_LibraryMembers()) {
	// tt.visitTypeNode(n);
	// Assert.assertFalse(n instanceof FamilyNode);
	// siblingCount = 0;
	// if (n.getParent() instanceof FamilyNode)
	// siblingCount = n.getParent().getChildren().size();
	// nn1 = n.clone(); // makes duplication in library
	// nn2 = n.clone();
	// nn3 = n.clone();
	// INode parent = n.getParent();
	// if (nn1 != null) {
	// tt.visitTypeNode(nn1); // skip services
	// Assert.assertTrue(nn1.getParent() instanceof FamilyNode);
	// nn1.removeFromLibrary();
	// }
	// if (nn2 != null) {
	// tt.visitTypeNode(nn2); // skip services
	// Assert.assertTrue(nn2.getParent() instanceof FamilyNode);
	// nn2.removeFromLibrary();
	// }
	// if (nn3 != null) {
	// tt.visitTypeNode(nn3); // skip services
	// Assert.assertTrue(nn3.getParent() instanceof FamilyNode);
	// n.replaceWith(nn3);
	// n = nn3;
	// tt.visitTypeNode(n);
	// }
	// if (siblingCount > 0) {
	// Assert.assertEquals(siblingCount, n.getParent().getChildren().size());
	// Assert.assertTrue(n.getParent() instanceof FamilyNode);
	// }
	// }
	//
	// }
}
