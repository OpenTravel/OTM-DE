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

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemas.controllers.DefaultProjectController;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.testUtils.MockLibrary;
import org.opentravel.schemas.testUtils.NodeTesters;
import org.opentravel.schemas.types.TestTypes;

/**
 * @author Dave Hollander
 * 
 */
public class Family_Tests {
	ModelNode model = null;
	TestTypes tt = new TestTypes();

	NodeTesters nt = new NodeTesters();
	LoadFiles lf = new LoadFiles();
	LibraryTests lt = new LibraryTests();
	MockLibrary ml = null;
	LibraryNode ln = null;
	MainController mc;
	DefaultProjectController pc;
	ProjectNode defaultProject;

	@Before
	public void beforeAllTests() {
		mc = new MainController();
		ml = new MockLibrary();
		pc = (DefaultProjectController) mc.getProjectController();
		defaultProject = pc.getDefaultProject();
	}

	@Test
	public void familyConstructors() {
		ln = ml.createNewLibrary("http://www.test.com/test1", "test1", defaultProject);
		LibraryNode ln_inChain = ml.createNewLibrary("http://www.test.com/test1c", "test1c", defaultProject);
		LibraryChainNode lcn = new LibraryChainNode(ln_inChain);
		ln_inChain.setEditable(true);

		Node n1 = makeSimple("s_1");
		Node n2 = makeSimple("s_2");

		// Make a family from 2 nodes in un-managed library
		ln.addMember(n1);
		FamilyNode fn = new FamilyNode(n1, n2);
		Assert.assertNotNull(fn);
		Assert.assertTrue(fn.getName().equals(n1.getFamily()));
		Assert.assertEquals(2, fn.getChildren().size());
		Assert.assertEquals(ln, fn.getLibrary());
		Assert.assertEquals(fn, n1.getParent());
		Assert.assertEquals(fn, n2.getParent());

		//
		// Make family in managed library
		//
		Node nc1 = makeSimple("s_1");
		ln_inChain.addMember(nc1);
		Assert.assertTrue(nc1.getParent() instanceof VersionNode);

		Node nc2 = makeSimple("s_2");
		ln_inChain.getTLLibrary().addNamedMember((LibraryMember) nc2.getTLModelObject());
		ln_inChain.simpleRoot.linkChild(nc2, false);

		// nc1 is version wrapped, nc2 is not.
		fn = new FamilyNode(nc1, nc2);
		Assert.assertNotNull(fn);
		Assert.assertEquals(ln_inChain, fn.getLibrary());
		Assert.assertEquals(ln_inChain.simpleRoot, fn.getParent());
		Assert.assertTrue(fn.getName().equals(nc1.getFamily()));
		Assert.assertEquals(2, fn.getChildren().size());
		Assert.assertEquals(fn, nc1.getParent().getParent()); // parent is version node
		Assert.assertEquals(fn, nc2.getParent());
		Assert.assertEquals(1, ln_inChain.simpleRoot.getChildren().size());

		lcn.add((ComponentNode) nc2);
		MockLibrary.printDescendants_NamedTypes(ln_inChain);
		MockLibrary.printDescendants_NamedTypes(lcn);

		// check chain
		Assert.assertEquals(3, lcn.getDescendants_NamedTypes().size());
		Assert.assertEquals(2, lcn.getSimpleAggregate().getDescendants_NamedTypes().size());
		Assert.assertTrue(lcn.getSimpleAggregate().getChildren().get(0) instanceof FamilyNode);

		// TODO - Make family from string and parent (AggregateFamilyNode usage)
	}

	private Node makeSimple(String name) {
		Node n2 = new SimpleTypeNode(new TLSimple());
		n2.setName(name);
		n2.setAssignedType(NodeFinders.findNodeByName("int", Node.XSD_NAMESPACE));
		return n2;
	}

	@Test
	public void familyMethods() {
		// node.linkChild
		// makeFamilyName
		// add child to family
		// new FamilyNode(n, peer)

		// Setup
		ln = ml.createNewLibrary("http://www.test.com/test1", "test1", defaultProject);
		Node n1 = new SimpleTypeNode(new TLSimple());
		final Node type = NodeFinders.findNodeByName("int", Node.XSD_NAMESPACE);
		n1.setName("s_1");
		n1.setAssignedType(type);
		Node n2 = new SimpleTypeNode(new TLSimple());
		n2.setName("s_2");
		n2.setAssignedType(type);

		// Add simples to an un-managed library
		ln.addMember(n1);
		ln.addMember(n2);
		Assert.assertTrue(n1.getParent() instanceof FamilyNode);
		new LibraryChainNode(ln);
		// n2.addChildToFamily(n1); // can't test direct because adding to library does family
		Assert.assertTrue(n1.getParent() instanceof VersionNode);
		Assert.assertTrue(n1.getParent().getParent() instanceof FamilyNode);

		ln = ml.createNewLibrary("http://www.test.com/test", "test", defaultProject);
		ml.addSimpleTypeToLibrary(ln, "c1_1");
		ml.addSimpleTypeToLibrary(ln, "c1_2");
		Node c1 = ln.findNodeByName("c1_1");
		Assert.assertNotNull(ln.findNodeByName("c1_1"));
		Assert.assertTrue(c1.getParent() instanceof FamilyNode);
		Assert.assertTrue(c1.getParent().getParent() instanceof NavNode);
		FamilyNode fn = (FamilyNode) c1.getParent();
		Assert.assertEquals(2, fn.getChildren().size());

		new LibraryChainNode(ln);
		c1 = ln.findNodeByName("c1_1");
		// List<Node> kids = ln.getDescendants_NamedTypes();
		Assert.assertNotNull(ln.findNodeByName("c1_1"));
		Assert.assertTrue(c1.getParent() instanceof VersionNode);
		Assert.assertTrue(c1.getParent().getParent() instanceof FamilyNode);
		fn = (FamilyNode) c1.getParent().getParent();
		Assert.assertEquals(2, fn.getChildren().size());
	}

	@Test
	public void mockFamilyTest() {
		ln = ml.createNewLibrary("http://www.test.com/test", "test", defaultProject);
		mockFamilyTest(ln);
		ln = ml.createNewLibrary("http://www.test.com/test2", "test2", defaultProject);
		new LibraryChainNode(ln);
		ln.setEditable(true);
		mockFamilyTest(ln);
	}

	public void mockFamilyTest(LibraryNode ln) {
		// ln = ml.createNewLibrary("http://www.sabre.com/test", "test", defaultProject);
		Node simpleNav = null;
		if (!ln.isEditable())
			return;

		// Find the simple type node.
		for (Node n : ln.getChildren()) {
			if (n.getName().equals("Simple Objects"))
				simpleNav = n;
		}
		Assert.assertNotNull(simpleNav);

		ml.addSimpleTypeToLibrary(ln, "A_a");
		ml.addSimpleTypeToLibrary(ln, "B_b");
		ml.addSimpleTypeToLibrary(ln, "C");
		Assert.assertEquals(3, simpleNav.getChildren().size());

		// These three should create families
		ml.addSimpleTypeToLibrary(ln, "A_a1"); // uses LibraryNode.addMember()
		ml.addSimpleTypeToLibrary(ln, "B_b1");
		ml.addSimpleTypeToLibrary(ln, "C");
		Assert.assertEquals(3, simpleNav.getChildren().size());
		// Fixed 1/12/15 - does not put C family into the aggregates
		// Fixed 1/19/15 - 2 families and two "C" nodes in aggregates.
		if (ln.isInChain())
			Assert.assertEquals(4, ln.getChain().getSimpleAggregate().getChildren().size());
	}

	@Test
	public void FamilyTests() throws Exception {
		MainController mc = new MainController();
		LoadFiles lf = new LoadFiles();
		model = mc.getModelNode();

		lf.loadTestGroupA(mc);
		for (LibraryNode ln : model.getUserLibraries()) {
			int beforeCnt = ln.getDescendants_NamedTypes().size();
			tt.visitAllNodes(ln);

			siblingTest(ln);
			tt.visitAllNodes(ln);
			Assert.assertEquals(beforeCnt, ln.getDescendants_NamedTypes().size());

			deleteFamilies(ln);
			// You can't visit nodes because the deleted families may be used as a type on one of
			// the remaining types. tt.visitAllNodes(ln);
		}
	}

	private void deleteFamilies(LibraryNode ln) {
		ArrayList<FamilyNode> families = new ArrayList<FamilyNode>();
		for (INode nav : ln.getChildren())
			for (Node n : nav.getChildren()) {
				if (n.isFamily())
					families.add((FamilyNode) n);
			}
		for (FamilyNode f : families) {
			Node parent = f.getParent();
			Assert.assertNotNull(parent);
			Assert.assertTrue(f instanceof FamilyNode);
			Assert.assertTrue(f.getChildren().size() > 0);
			ArrayList<Node> kids = new ArrayList<Node>(f.getChildren());
			for (INode k : kids) {
				k.removeFromLibrary();
			}
			Assert.assertEquals(0, f.getChildren().size());
			Assert.assertNull(f.getParent());

		}
	}

	private void siblingTest(LibraryNode ln) {
		Node nn1 = null;
		Node nn2 = null;
		Node nn3 = null;
		int siblingCount = 0;

		for (Node n : ln.getDescendants_NamedTypes()) {
			tt.visitTypeNode(n);
			Assert.assertFalse(n instanceof FamilyNode);
			siblingCount = 0;
			if (n.getParent().isFamily())
				siblingCount = n.getParent().getChildren().size();
			nn1 = n.clone(); // makes duplication in library
			nn2 = n.clone();
			nn3 = n.clone();
			INode parent = n.getParent();
			if (nn1 != null) {
				tt.visitTypeNode(nn1); // skip services
				Assert.assertTrue(nn1.getParent() instanceof FamilyNode);
				nn1.removeFromLibrary();
			}
			if (nn2 != null) {
				tt.visitTypeNode(nn2); // skip services
				Assert.assertTrue(nn2.getParent() instanceof FamilyNode);
				nn2.removeFromLibrary();
			}
			if (nn3 != null) {
				tt.visitTypeNode(nn3); // skip services
				Assert.assertTrue(nn3.getParent() instanceof FamilyNode);
				n.replaceWith(nn3);
				n = nn3;
				tt.visitTypeNode(n);
			}
			if (siblingCount > 0) {
				Assert.assertEquals(siblingCount, n.getParent().getChildren().size());
				Assert.assertTrue(n.getParent() instanceof FamilyNode);
			}
		}

	}
}
