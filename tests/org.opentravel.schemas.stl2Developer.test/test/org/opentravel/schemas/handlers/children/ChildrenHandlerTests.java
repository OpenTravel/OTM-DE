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
package org.opentravel.schemas.handlers.children;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemas.node.NavNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.node.VersionAggregateNode;
import org.opentravel.schemas.node.VersionNode;
import org.opentravel.schemas.node.handlers.children.FacetProviderChildrenHandler;
import org.opentravel.schemas.node.handlers.children.NavNodeChildrenHandler;
import org.opentravel.schemas.node.interfaces.Enumeration;
import org.opentravel.schemas.node.interfaces.FacetInterface;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.interfaces.SimpleMemberInterface;
import org.opentravel.schemas.node.interfaces.WhereUsedNodeInterface;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNavNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.objectMembers.ExtensionPointNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.properties.RoleNode;
import org.opentravel.schemas.node.typeProviders.AliasNode;
import org.opentravel.schemas.node.typeProviders.RoleFacetNode;
import org.opentravel.schemas.node.typeProviders.SimpleTypeNode;
import org.opentravel.schemas.node.typeProviders.VWA_Node;
import org.opentravel.schemas.node.typeProviders.facetOwners.BusinessObjectNode;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.testUtils.MockLibrary;
import org.opentravel.schemas.trees.repository.RepositoryNode;
import org.opentravel.schemas.utils.BaseProjectTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChildrenHandlerTests extends BaseProjectTest {
	private final static Logger LOGGER = LoggerFactory.getLogger(ChildrenHandlerTests.class);

	public static MockLibrary ml = new MockLibrary();
	public static LoadFiles lf = new LoadFiles();
	LibraryChainNode lcn;

	// @Override
	// @Before
	// public void beforeEachTest() {
	// mc = OtmRegistry.getMainController();
	// ml = new MockLibrary();
	// pc = (DefaultProjectController) mc.getProjectController();
	// defaultProject = pc.getDefaultProject();
	// model = mc.getModelNode();
	// }

	@Test
	public void ChildH_InheritanceTests() {
		// Given a facet that extends another facet
		LibraryNode ln = ml.createNewLibrary(pc, "t1");
		assert ln.isEditable();
		BusinessObjectNode boBase = null;
		for (LibraryMemberInterface lm : ln.get_LibraryMembers())
			if (lm instanceof BusinessObjectNode)
				boBase = (BusinessObjectNode) lm;
		assert boBase != null;
		BusinessObjectNode boExt = ml.addBusinessObjectToLibrary(ln, "BoExt");
		boExt.setExtension(boBase);
		assert boExt.getExtendsType() == boBase;

		// Given a facet child handler for the extended summary facet
		FacetProviderChildrenHandler handler = boExt.getFacet_Summary().getChildrenHandler();
		List<Node> boKids = boExt.getChildren();
		List<Node> boIKids = boExt.getInheritedChildren();
		List<?> kids = handler.get();
		List<?> iKids = handler.getInheritedChildren();
		List<TLModelElement> iTlKids = handler.getInheritedChildren_TL();
		// Then
		assertTrue(!kids.isEmpty());
		assertTrue(!iKids.isEmpty());
		assertTrue(!iTlKids.isEmpty());
	}

	@Test
	public void ChildH_NavNodeTests() {
		ProjectNode project1 = createProject("Project1", rc.getLocalRepository(), "IT1");
		LibraryNode ln = ml.createNewLibrary_Empty("x", "x", project1);
		assertTrue(ln != null);

		// Static children handler
		NavNode n = ln.getComplexRoot();
		NavNodeChildrenHandler nnch = new NavNodeChildrenHandler(n);
		// from nnch
		assertTrue(nnch.getChildren_TL().isEmpty());
		// From static
		assertTrue(nnch.getInheritedChildren().isEmpty());
		// From base
		assertTrue(nnch.get().isEmpty());
		assertTrue(nnch.getChildren_New().isEmpty());

		NavNode nn = new NavNode("MyNavNode", null);
		SimpleTypeNode s1 = new SimpleTypeNode(new TLSimple());
		nn.add(s1);
		nn.removeLM(s1);
		assertTrue(nn.getChildren().isEmpty());

	}

	/**
	 * Test all specialized behaviors in overrides. Also tests alignment of isNavChild() with getNavChildren()
	 * 
	 * One big test that uses lots of libraries...so to keep running time down, all tests are done at once.
	 * 
	 * @throws Exception
	 */
	@Test
	public void ChildH_testGroupA_Tests() throws Exception {
		//
		// Given = lots of libraries in chains
		lf.loadTestGroupA(mc);
		for (LibraryNode ln : defaultProject.getLibraries())
			if (!ln.isInChain())
				new LibraryChainNode(ln);

		//
		// When - each node is examined
		//
		List<Node> kids = mc.getModelNode().getDescendants();
		for (Node n : kids)
			check(n);
	}

	public void check(Node n) {
		if (n.getChildrenHandler() == null) {
			LOGGER.debug("No child handler: " + n.getClass().getSimpleName() + " " + n);
			return;
		}

		//
		// Then - Make sure getTreeChildren() does not error out.
		//
		boolean deep = false;
		@SuppressWarnings("unchecked")
		List<Node> tKids = (List<Node>) n.getChildrenHandler().getTreeChildren(deep);
		if (tKids.isEmpty() && (n.hasTreeChildren(deep) || n.hasNavChildren(deep)))
			LOGGER.debug("Error " + n.getChildrenHandler().getTreeChildren(deep).size());
		if (n.hasTreeChildren(deep))
			assertTrue("If there are tree children then there must be tree children.", !tKids.isEmpty());
		if (n.hasNavChildren(deep))
			assertTrue("If there are nav children then there must be tree children.", !tKids.isEmpty());

		//
		// Then - Make sure getNavChildren() and hasNavChildren() and isNavChild() are aligned
		//
		deep = false;
		List<Node> nKids = n.getNavChildren(deep);
		if (n.hasNavChildren(deep))
			assertTrue("hasNavChildren is true so getNavChildren() must have kids.", !nKids.isEmpty());
		else
			assertTrue("hasNavChildren must match getNavChildren().", nKids.isEmpty());
		for (Node kid : nKids)
			assertTrue("Get nav children must pass isNavChild()", kid.isNavChild(deep));

		deep = true;
		nKids = n.getNavChildren(deep);
		if (n.hasNavChildren(deep))
			assertTrue("hasNavChildren is true so getNavChildren() must have kids.", !nKids.isEmpty());
		else
			assertTrue("hasNavChildren must match getNavChildren().", nKids.isEmpty());
		for (Node kid : nKids)
			assertTrue("Get nav children must pass isNavChild()", kid.isNavChild(deep));

		//
		// Then - Test classes that always do not have navChildren
		//
		if ((n instanceof LibraryNode) && (n.getParent() instanceof VersionAggregateNode))
			assertTrue("Must be empty.", n.getNavChildren(true).isEmpty());
		else if (n instanceof LibraryNavNode)
			assertTrue("Must be size from library",
					n.getNavChildren(false).size() == ((LibraryNavNode) n).getThisLib().getNavChildren(false).size());
		else if (n instanceof VersionAggregateNode)
			assertTrue("Must be empty.", n.getNavChildren(true).isEmpty());
		else if (n instanceof RepositoryNode)
			assertTrue("Must be empty.", n.getNavChildren(true).isEmpty());
		else if (n instanceof AliasNode)
			assertTrue("Must be empty.", n.getNavChildren(true).isEmpty());

		//
		// Then - Test Overridden behavior
		//
		else if (n instanceof ExtensionPointNode)
			// Some properties will not be in the list. Could have children but no navChildren
			assertTrue("Must not be null.", n.getNavChildren(true) != null);
		else if (n instanceof FacetInterface)
			// Some properties will not be in the list. Could have children but no navChildren
			assertTrue("Must not be null.", n.getNavChildren(true) != null);
		else if (n instanceof PropertyNode)
			// getNavChildren may return assigned type and aliases
			assertTrue("Must not be null.", n.getNavChildren(true) != null);
		else if (n instanceof SimpleMemberInterface)
			assertTrue("Nav child must only be assigned type if any.", n.getNavChildren(true).size() < 2);
		else if (n instanceof VersionNode)
			assertTrue("Head node children must be version node's navChildren.",
					((VersionNode) n).getNewestVersion().getNavChildren(true).size() == n.getNavChildren(true).size());
		else if (n instanceof Enumeration)
			assertTrue("Enumeration getNavChildren must be empty.", n.getNavChildren(true).isEmpty());
		else if (n instanceof RoleNode || n instanceof RoleFacetNode)
			assertTrue("Role getNavChildren must be empty.", n.getNavChildren(true).isEmpty());
		else if (n instanceof VWA_Node)
			// only the simple is Nav child ??Why??
			assertTrue("Get Nav Children must not be null.", n.getNavChildren(true) != null);
		else if (n instanceof LibraryChainNode)
			LOGGER.debug("TODO - children of LCN.");
		else {
			assertTrue("Must not be TypeUser node.", !(n instanceof WhereUsedNodeInterface));
			//
			// Finally - if not special case all children are nav children
			//
			if (n.getNavChildren(true).size() != n.getChildren().size()) {
				List<Node> nc = n.getNavChildren(true);
				List<Node> ch = n.getChildren();
				LOGGER.debug("Invalid nav child count.");
			}
			assertTrue("Size error in default case where all children are navigation.",
					n.getNavChildren(true).size() == n.getChildren().size());
		}
	}

}
