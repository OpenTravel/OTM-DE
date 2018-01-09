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
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.testUtils.MockLibrary;
import org.opentravel.schemas.testUtils.NodeTesters;
import org.opentravel.schemas.testUtils.NodeTesters.TestNode;
import org.opentravel.schemas.types.TestTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test getNavChildren(), hasNavChildren() and isNavChild(). These are used by content providers for navigation menu and
 * node-tree based wizards.
 * 
 * @author Dave Hollander
 * 
 */
@Deprecated
public class Node_NavChildren_Tests {
	private final static Logger LOGGER = LoggerFactory.getLogger(Node_NavChildren_Tests.class);

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
	LibraryNode ln_inChain;
	LibraryChainNode lcn;

	@Before
	public void beforeAllTests() {
		mc = OtmRegistry.getMainController();
		ml = new MockLibrary();
		pc = (DefaultProjectController) mc.getProjectController();
		defaultProject = pc.getDefaultProject();
		model = mc.getModelNode();

		ln = ml.createNewLibrary("http://www.test.com/test1", "test1", defaultProject);
		ln_inChain = ml.createNewLibrary("http://www.test.com/test1c", "test1c", defaultProject);
		lcn = new LibraryChainNode(ln_inChain);
		ln_inChain.setEditable(true);
		ml.addOneOfEach(ln_inChain, "OE");

		TestNode tn = new NodeTesters().new TestNode();
		ln_inChain.visitAllNodes(tn);
	}

	// /**
	// * Test all specialized behaviors in overrides. Also tests alignment of isNavChild() with getNavChildren()
	// *
	// * One big test that uses lots of libraries...so to keep running time down, all tests are done at once.
	// *
	// * @throws Exception
	// */
	// @Test
	// public void getNavChildrenTests() throws Exception {
	// //
	// // Given = lots of libraries in chains
	// lf.loadTestGroupA(mc);
	// for (LibraryNode ln : defaultProject.getLibraries())
	// if (!ln.isInChain())
	// new LibraryChainNode(ln);
	// else
	// lcn = ln.getChain();
	// ChildrenHandlerTests chTests = new ChildrenHandlerTests();
	//
	// //
	// // When - each node is examined
	// //
	// List<Node> kids = model.getDescendants();
	// // assertTrue(kids.size() == 12);
	//
	// for (Node n : kids) {

	// Debugging trap
	// if (n instanceof FacetNode) {
	// List<Node> tk = n.getTreeChildren(false);
	// List<Node> nk = n.getNavChildren(false);
	// if (n.hasTreeChildren(false) && tk.isEmpty()) {
	// LOGGER.debug("Trouble - " + n);
	// tk = n.getTreeChildren(false);
	// }
	// boolean tc = n.hasTreeChildren(false);
	// boolean nc = n.hasNavChildren(false);
	// System.out.println("HELPME");
	// }

	// ?? Only getting projects and libraryNavNodes.
	//
	// un-comment to focus on a specific node type
	// if (!(n instanceof LibraryNode))
	// continue;
	// LOGGER.debug("Testing children of " + n);
	// chTests.check(n);

	// //
	// // Then - Make sure getTreeChildren() does not error out.
	// //
	//
	// List<Node> tKids = n.getTreeChildren(false);
	// if (n.hasTreeChildren(false))
	// assertTrue("If there are tree children the must be tree children.", !tKids.isEmpty());
	// if (n.hasNavChildren(false))
	// assertTrue("If there are nav children the must be tree children.", !tKids.isEmpty());
	//
	// //
	// // Then - Make sure getNavChildren() and hasNavChildren() and isNavChild() are aligned
	// //
	// boolean deep = false;
	// List<Node> nKids = n.getNavChildren(deep);
	// if (n.hasNavChildren(deep))
	// assertTrue("hasNavChildren is true so getNavChildren() must have kids.", !nKids.isEmpty());
	// else
	// assertTrue("hasNavChildren must match getNavChildren().", nKids.isEmpty());
	// for (Node kid : nKids)
	// assertTrue("Get nav children must pass isNavChild()", kid.isNavChild(deep));
	//
	// deep = true;
	// nKids = n.getNavChildren(deep);
	// if (n.hasNavChildren(deep))
	// assertTrue("hasNavChildren is true so getNavChildren() must have kids.", !nKids.isEmpty());
	// else
	// assertTrue("hasNavChildren must match getNavChildren().", nKids.isEmpty());
	// for (Node kid : nKids)
	// assertTrue("Get nav children must pass isNavChild()", kid.isNavChild(deep));
	//
	// //
	// // Then - Test classes that always do not have navChildren
	// //
	// if ((n instanceof LibraryNode) && (n.parent instanceof VersionAggregateNode))
	// assertTrue("Must be empty.", n.getNavChildren(true).isEmpty());
	// else if (n instanceof LibraryNavNode)
	// assertTrue("Must be size from library", n.getNavChildren(false).size() == ((LibraryNavNode) n)
	// .getThisLib().getNavChildren(false).size());
	// else if (n instanceof VersionAggregateNode)
	// assertTrue("Must be empty.", n.getNavChildren(true).isEmpty());
	// else if (n instanceof RepositoryNode)
	// assertTrue("Must be empty.", n.getNavChildren(true).isEmpty());
	// else if (n instanceof AliasNode)
	// assertTrue("Must be empty.", n.getNavChildren(true).isEmpty());
	//
	// //
	// // Then - Test Overridden behavior
	// //
	// else if (n instanceof ExtensionPointNode)
	// // Some properties will not be in the list. Could have children but no navChildren
	// assertTrue("Must not be null.", n.getNavChildren(true) != null);
	// else if (n instanceof PropertyOwnerNode)
	// // Some properties will not be in the list. Could have children but no navChildren
	// assertTrue("Must not be null.", n.getNavChildren(true) != null);
	// else if (n instanceof PropertyNode)
	// // getNavChildren may return assigned type and aliases
	// assertTrue("Must not be null.", n.getNavChildren(true) != null);
	// else if (n instanceof SimpleComponentNode)
	// assertTrue("Nav child must only be assigned type if any.", n.getNavChildren(true).size() < 2);
	// else if (n instanceof VersionNode)
	// assertTrue("Head node children must be version node's navChildren.", ((VersionNode) n)
	// .getNewestVersion().getNavChildren(true).size() == n.getNavChildren(true).size());
	// else if (n instanceof Enumeration)
	// assertTrue("Enumeration getNavChildren must be empty.", n.getNavChildren(true).isEmpty());
	// else if (n instanceof RoleNode || n instanceof RoleFacetNode)
	// assertTrue("Role getNavChildren must be empty.", n.getNavChildren(true).isEmpty());
	// else if (n instanceof VWA_Node)
	// // only the simple is Nav child ??Why??
	// assertTrue("Get Nav Children must not be null.", n.getNavChildren(true) != null);
	// else {
	// assertTrue("Must not be TypeUser node.", !(n instanceof WhereUsedNodeInterface));
	// //
	// // Finally - if not special case all children are nav children
	// //
	// // if (n.getNavChildren(true).size() != n.getChildren().size()) {
	// // List<Node> nc = n.getNavChildren(true);
	// // List<Node> ch = n.getChildren();
	// // LOGGER.debug("Invalid nav child count.");
	// // }
	// assertTrue("Size error in default case where all children are navigation.", n.getNavChildren(true)
	// .size() == n.getChildren().size());
	// }
	// }
	// }
}
