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
import org.opentravel.schemas.controllers.DefaultProjectController;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.node.Node.NodeVisitor;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.testUtils.MockLibrary;
import org.opentravel.schemas.testUtils.NodeTesters;
import org.opentravel.schemas.testUtils.NodeTesters.PrintNode;
import org.opentravel.schemas.testUtils.NodeTesters.TestNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dave Hollander
 * 
 */
public class Delete_Tests {
	static final Logger LOGGER = LoggerFactory.getLogger(MockLibrary.class);

	MockLibrary ml = null;
	LibraryNode ln = null;
	MainController mc;
	DefaultProjectController pc;
	ProjectNode defaultProject;
	ModelNode model = null;
	LoadFiles lf = null;

	NodeVisitor dv = new NodeVisitors().new deleteVisitor();
	PrintNode pv = new NodeTesters().new PrintNode();
	TestNode tv = new NodeTesters().new TestNode(); // preferred tester
	NodeTesters tt = new NodeTesters();

	@Before
	public void beforeAllTests() {
		mc = new MainController();
		ml = new MockLibrary();
		pc = (DefaultProjectController) mc.getProjectController();
		defaultProject = pc.getDefaultProject();
		lf = new LoadFiles();
	}

	@Test
	public void deleteVisitor() throws Exception {

		// Delete Visitor
		int namedTypeCnt = setUpCase(1);
		int descendants = ln.getDescendants().size();
		ln.getComplexRoot().visitAllNodes(dv); // should do nothing
		ln.getSimpleRoot().visitAllNodes(dv); // should do nothing
		ln.visitAllNodes(tv); // test visitor, should not change library
		Assert.assertEquals(descendants, ln.getDescendants().size());
		Assert.assertEquals(namedTypeCnt, ln.getDescendants_NamedTypes().size());

		namedTypeCnt = setUpCase(2);
		ln.visitAllNodes(dv);
		ln.visitAllNodes(pv);
		Assert.assertEquals(2, ln.getDescendants().size());

		LOGGER.debug("***Setting Up Test Case 3");
		namedTypeCnt = setUpCase(3);
		LibraryChainNode lcn = ln.getChain();
		ln.visitAllNodes(dv);
		ln.visitAllNodes(pv);
		Assert.assertEquals(0, ln.getDescendants_NamedTypes().size());
		Assert.assertEquals(2, ln.getDescendants().size());
		Assert.assertEquals(0, lcn.getDescendants_NamedTypes().size());
		Assert.assertEquals(4, lcn.getDescendants().size());

		LOGGER.debug("***Setting Up Test Case 4");
		namedTypeCnt = setUpCase(4);
		lcn = ln.getChain();
		ln.visitAllNodes(dv);
		ln.visitAllNodes(pv);
		Assert.assertEquals(0, ln.getDescendants_NamedTypes().size());
		Assert.assertEquals(2, ln.getDescendants().size());
		Assert.assertEquals(0, lcn.getDescendants_NamedTypes().size());
		Assert.assertEquals(4, lcn.getDescendants().size());

	}

	// TODO - create test that assures naming errors are caught
	// 2nd library same name
	// 2nd library different name but not valid because of duplicate types w/ same name

	// TODO - create test case that assures all children of all types are deleted.
	// match against list created by visitAllNodes used in delete();

	private int setUpCase(int testCase) {
		int count = 0;
		switch (testCase) {

		case 1:
			// Case 1 - simple library, not editable, with constructed objects
			ln = ml.createNewLibrary("http://test.com/ns" + testCase, "testCase" + testCase, defaultProject);
			ln.setEditable(false);
			count = ml.addOneOfEach(ln, "case1");
			ln.visitAllNodes(tv);
			Assert.assertEquals(count, ln.getDescendants_NamedTypes().size());
			Assert.assertFalse(ln.isManaged());
			Assert.assertFalse(ln.isInHead());
			Assert.assertFalse(ln.isEditable());
			Assert.assertTrue(ln.isDeleteable()); // NOTE - lib is delete-able
			break;

		case 2:
			// Case 2 - unmanaged library, editable, with constructed objects
			ln = ml.createNewLibrary("http://test.com/ns" + testCase, "testCase" + testCase, defaultProject);
			ln.setEditable(true);
			count = ml.addOneOfEach(ln, "case2");
			ln.visitAllNodes(tv);
			Assert.assertEquals(count, ln.getDescendants_NamedTypes().size());
			Assert.assertTrue(ln.isDeleteable());
			Assert.assertTrue(ln.isEditable());
			break;

		case 3:
			// Case 3 - managed library, editable, with constructed objects
			ln = ml.createNewLibrary("http://test.com/ns" + testCase, "testCase" + testCase, defaultProject);
			count = ml.addOneOfEach(ln, "case" + testCase);
			ln.visitAllNodes(tv);
			Assert.assertEquals(count, ln.getDescendants_NamedTypes().size());
			new LibraryChainNode(ln);
			ln.setEditable(true); // must be done after LCN created
			Assert.assertTrue(ln.isEditable());
			Assert.assertTrue(ln.getChain().isEditable());
			Assert.assertTrue(ln.getChain().isMajor());
			Assert.assertEquals(count, ln.getDescendants_NamedTypes().size());
			Assert.assertEquals(count, ln.getChain().getDescendants_NamedTypes().size());
			Assert.assertFalse(ln.isManaged());
			Assert.assertFalse(ln.isInHead());
			break;

		case 4:
			ln = ml.createNewLibrary("http://test.com/ns" + testCase, "testCase" + testCase, defaultProject);
			count = ml.addOneOfEach(ln, "case" + testCase);
			ln.visitAllNodes(tv);
			Assert.assertEquals(count, ln.getDescendants_NamedTypes().size());
			new LibraryChainNode(ln);
			ln.setEditable(true); // must be done after LCN created

			final String fixedName = NodeNameUtils.fixSimpleTypeName("case4S");
			ml.addSimpleTypeToLibrary(ln, "case4S"); // creates family
			count++;
			Node n = ln.findNodeByName(fixedName);
			Assert.assertNotNull(n);
			Assert.assertNotNull(ln.getChain().findNodeByName(fixedName));
			Assert.assertNotNull(ln.findNodeByName(fixedName));
			Assert.assertTrue(n.getParent() instanceof VersionNode);
			Assert.assertEquals(count, ln.getDescendants_NamedTypes().size());
			Assert.assertEquals(count, ln.getChain().getDescendants_NamedTypes().size());
			break;
		case 10:
			// TODO - managed library this is head with multiple libraries
			// TODO - managed library that is not head
		}
		return count;
	}

	/**
	 * Run the tests against test library files.
	 * 
	 * @throws Exception
	 */
	@Test
	public void deleteFiles() throws Exception {
		model = mc.getModelNode();

		lf.loadTestGroupA(mc);
		int i = 0;
		for (LibraryNode ln : model.getUserLibraries()) {
			new LibraryChainNode(ln); // Test in a chain
			ln.setEditable(true);
			if (i++ == 1)
				deleteNodeListTest(ln);
			else
				deleteEachMember(ln);
		}
		tt.visitAllNodes(model);
	}

	private void deleteEachMember(LibraryNode ln) {
		for (Node n : ln.getDescendants()) {
			if (n instanceof CoreObjectNode)
				LOGGER.debug("Core: " + n);

			if (!n.isNavigation()) {
				if (n.isDeleted())
					continue;
				INode user = null;

				// Make sure the users of this type are informed of deletion.
				if (n.getTypeUsers().size() > 0) {
					user = n.getTypeUsers().get(0);
				}
				n.delete();
				if (user != null && n.isDeleteable()) {
					Assert.assertNotSame(n, user.getType());
				}
			}
		}
		ln.visitAllNodes(tv);
		MockLibrary.printDescendants(ln);
		MockLibrary.printDescendants(ln.getChain());
		Assert.assertEquals(7, ln.getChain().getDescendants().size());
		Assert.assertEquals(2, ln.getDescendants().size());
	}

	private void deleteNodeListTest(LibraryNode ln) {
		ArrayList<Node> members = new ArrayList<Node>(ln.getDescendants_NamedTypes());
		ln.visitAllNodes(tv);
		Node.deleteNodeList(members);
		ln.visitAllNodes(tv);
		Assert.assertEquals(2, ln.getDescendants().size());
	}

}
