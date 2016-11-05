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
package org.opentravel.schemas.commands;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemas.controllers.DefaultProjectController;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.node.BusinessObjectNode;
import org.opentravel.schemas.node.ChoiceObjectNode;
import org.opentravel.schemas.node.ChoiceObjectTests;
import org.opentravel.schemas.node.CoreObjectNode;
import org.opentravel.schemas.node.FamilyNode;
import org.opentravel.schemas.node.LibraryChainNode;
import org.opentravel.schemas.node.LibraryNode;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.Node.NodeVisitor;
import org.opentravel.schemas.node.NodeFinders;
import org.opentravel.schemas.node.NodeVisitors;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.node.VersionNode;
import org.opentravel.schemas.node.facets.FacetNode;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.properties.AttributeNode;
import org.opentravel.schemas.node.properties.ElementNode;
import org.opentravel.schemas.node.properties.PropertyOwnerInterface;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.testUtils.MockLibrary;
import org.opentravel.schemas.testUtils.NodeTesters;
import org.opentravel.schemas.testUtils.NodeTesters.PrintNode;
import org.opentravel.schemas.testUtils.NodeTesters.TestNode;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeUser;
import org.opentravel.schemas.utils.BaseProjectTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dave Hollander
 * 
 */
public class Delete_Tests extends BaseProjectTest {
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

	/**
	 * DeleteNodesHandler - uses node model controller
	 * 
	 * @throws Exception
	 */
	@Test
	public void deleteHandler_Tests() throws Exception {
		DeleteNodesHandler handler = new DeleteNodesHandler();

		// Given an null list
		List<Node> deleteList = null;
		// When deleted
		mc.getNodeModelController().deleteNodes(deleteList);
		// Then no failure

		// Given an empty list of selected nodes
		deleteList = new ArrayList<Node>();
		// When deleted
		mc.getNodeModelController().deleteNodes(deleteList);
		// Then no failure

		// Given libraries in the default project
		ProjectNode project = pc.getDefaultProject();
		lf.loadTestGroupA(mc);
		// Then all libraries must be deleted
		deleteLibrariesTest(project);

		// Given a user defined project with libraries in a chain
		project = createProject("Project1", rc.getLocalRepository(), "IT1");
		LibraryChainNode lcn1 = ml.createNewManagedLibrary("Lib1", project);
		LibraryChainNode lcn2 = ml.createNewManagedLibrary("Lib2", project);
		LibraryChainNode lcn3 = ml.createNewManagedLibrary("Lib3", project);
		// List<LibraryNode> managed = new ArrayList<LibraryNode>();
		// managed.add(lcn1.getHead());
		// rc.manage(rc.getLocalRepository(), managed);
		// rc.createMajorVersion(lcn1.getHead());
		// Then all libraries must be deleted
		deleteLibrariesTest(project);

	}

	private void deleteLibrariesTest(ProjectNode project) throws Exception {
		ModelNode model = Node.getModelNode();
		List<Node> deleteList = new ArrayList<Node>();

		// Given - Project with libraries loaded
		assertTrue("Project must have libraries.", !project.getUserLibraries().isEmpty());

		// When - one library is deleted
		LibraryNode lib = project.getUserLibraries().get(0);
		lib.setEditable(true);
		deleteList.add(lib);
		mc.getNodeModelController().deleteNodes(deleteList);

		// Then - library is deleted and not in the project and the library's objects are all deleted.
		assertTrue("Library is deleted.", deleteList.get(0).isDeleted());
		assertTrue("Project does not contain library.", !project.getUserLibraries().contains(lib));
		for (Node n : lib.getDescendants_LibraryMembers())
			assertTrue("Named Type " + n + " is deleted.", n.isDeleted());

		// When - rest are deleted
		deleteList.clear();
		for (LibraryNode ln : project.getUserLibraries())
			deleteList.add(ln);
		mc.getNodeModelController().deleteNodes(deleteList);

		// Then - they must not be in the list of children collected by navigator view
		// see LibraryTreeContentProvider
		int libCnt = project.getUserLibraries().size();
		assertTrue("Default Project must be empty.", libCnt == 0);
		ml.assertOnlyBuiltInLibraries(model);
	}

	@Test
	public void deleteProperties() {
		ln = ml.createNewLibrary("http://opentravel.org/test", "TestLib", defaultProject);
		BusinessObjectNode bo = new BusinessObjectNode(new TLBusinessObject());
		bo.setName("TestBO");
		ln.addMember(bo);
		FacetNode facet = bo.getSummaryFacet();
		Assert.assertNotNull(facet);
		TypeProvider aType = ml.getSimpleTypeProvider();

		// Given type user properties assigned types
		ElementNode ele = new ElementNode(facet, "e1");
		ele.setAssignedType(aType);
		AttributeNode attr = new AttributeNode(facet, "att1");
		attr.setAssignedType(aType);
		int whereAssignedCount = aType.getWhereAssignedCount();
		Assert.assertEquals(2, facet.getChildren().size());

		// Library must be editable to delete
		Assert.assertTrue(ln.isEditable());
		Assert.assertTrue(ele.isDeleteable());

		// delete them and assure where used is updated.
		ele.delete();
		attr.delete();
		Assert.assertEquals(0, facet.getChildren().size());
		Assert.assertEquals(whereAssignedCount - 2, aType.getWhereAssignedCount());
	}

	@Test
	public void deleteFacets_BusinessObject() {
		ln = ml.createNewLibrary("http://opentravel.org/test", "TestLib", defaultProject);

		// Given a business object with all facet types
		BusinessObjectNode bo = ml.addBusinessObjectToLibrary(ln, "TestBO");
		int facetCount = bo.getChildren().size();
		FacetNode q1 = bo.addFacet("Query1", TLFacetType.QUERY);
		FacetNode c1 = bo.addFacet("Custom1", TLFacetType.CUSTOM);
		assertTrue("Must have five children", bo.getChildren().size() == facetCount + 2);
		assertTrue("Query facet is NOT deleted.", !q1.isDeleted());
		assertTrue("Custom facet is NOT deleted.", !c1.isDeleted());

		// When the facets are deleted
		q1.delete();
		c1.delete();

		// Then object must not contain facets
		assertTrue("Object does NOT contain query facet.", !bo.getChildren().contains(q1));
		assertTrue("Object does NOT contain custom facet.", !bo.getChildren().contains(c1));
		assertTrue("Object only has 3 children.", bo.getChildren().size() == facetCount);
		assertTrue("Facet is deleted.", q1.isDeleted());
		assertTrue("Facet is deleted.", c1.isDeleted());
	}

	@Test
	public void deleteFacets_ChoiceObject() {
		ln = ml.createNewLibrary("http://opentravel.org/test", "TestLib", defaultProject);
		ChoiceObjectTests tests = new ChoiceObjectTests();

		// Given a business object with all facet types
		ChoiceObjectNode co = ml.addChoice(ln, "TestCO");
		int facetCount = co.getChildren().size();
		FacetNode c1 = co.addFacet("Added1");
		FacetNode c2 = co.addFacet("Added2");
		assertTrue("Must have five children", co.getChildren().size() == facetCount + 2);
		assertTrue("Choice 1 facet is NOT deleted.", !c1.isDeleted());
		assertTrue("Choice 2 facet is NOT deleted.", !c2.isDeleted());
		tests.checkChoice(co);

		// When the facets are deleted
		c1.delete();
		c2.delete();

		// Then object must not contain facets
		assertTrue("Object does NOT contain query facet.", !co.getChildren().contains(c1));
		assertTrue("Object does NOT contain custom facet.", !co.getChildren().contains(c2));
		assertTrue("Object only has original children count.", co.getChildren().size() == facetCount);
		assertTrue("Facet is deleted.", c1.isDeleted());
		assertTrue("Facet is deleted.", c2.isDeleted());

		// When the other choice facets are deleted
		for (PropertyOwnerInterface f : co.getChoiceFacets())
			((FacetNode) f).delete();

		// Then the object is still valid
		tests.checkChoice(co);
	}

	@Test
	public void deleteObjects() {
		ln = ml.createNewLibrary("http://opentravel.org/test", "TestLib", defaultProject);
	}

	@Test
	public void deleteTypeUsers_Test() {
		ln = ml.createNewLibrary("http://opentravel.org/test", "TestLib", defaultProject);
		BusinessObjectNode bo = new BusinessObjectNode(new TLBusinessObject());
		bo.setName("TestBO");
		ln.addMember(bo);
		FacetNode facet = bo.getSummaryFacet();
		Assert.assertNotNull(facet);
		TypeProvider aType = (TypeProvider) NodeFinders.findNodeByName("date", ModelNode.XSD_NAMESPACE);
		int whereAssignedCount = aType.getWhereAssignedCount();

		// Given a BO with two properties with assigned types
		ElementNode ele = new ElementNode(facet, "e1");
		ele.setAssignedType(aType);
		AttributeNode attr = new AttributeNode(facet, "att1");
		attr.setAssignedType(aType);
		Assert.assertEquals(2, facet.getChildren().size());

		// Delete the BO and assure the assigned types on properties are correct.
		bo.delete();
		Assert.assertTrue(ele.isDeleted());
		Assert.assertTrue(attr.isDeleted());
		Assert.assertEquals("Should be equal.", whereAssignedCount, aType.getWhereAssignedCount());
	}

	@Test
	public void deleteTypeProvider_Test() {
		ln = ml.createNewLibrary("http://opentravel.org/test", "TestLib", defaultProject);
		BusinessObjectNode bo = new BusinessObjectNode(new TLBusinessObject());
		bo.setName("TestBO");
		ln.addMember(bo);
		FacetNode facet = bo.getSummaryFacet();
		Assert.assertNotNull(facet);
		ElementNode ele = new ElementNode(facet, "e1");
		AttributeNode attr = new AttributeNode(facet, "att1");
		Assert.assertEquals(2, facet.getChildren().size());

		TypeProvider aType = ml.getSimpleTypeProvider();
		int aTypeCount = aType.getWhereAssignedCount();

		TypeProvider simpleType = ml.addSimpleTypeToLibrary(ln, "A_Simple");
		((TypeUser) simpleType).setAssignedType(aType);
		assertTrue("Must be 0.", simpleType.getWhereAssignedCount() == 0);

		// Given a BO with two properties with assigned to simpleType
		ele.setAssignedType(simpleType);
		attr.setAssignedType(simpleType);
		assertTrue("Must be 2.", simpleType.getWhereAssignedCount() == 2);

		// Delete the simple type and assure the assigned types on properties are correct.
		((Node) simpleType).delete();
		assertTrue("Must be unassigned.", ele.getAssignedType() == ModelNode.getUnassignedNode());
		assertTrue("Must be unassigned.", attr.getAssignedType() == ModelNode.getUnassignedNode());
		assertTrue("Must be 0.", simpleType.getWhereAssignedCount() == 0);
		assertTrue("Must be starting value.", aTypeCount == aType.getWhereAssignedCount());

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
		Assert.assertEquals(namedTypeCnt, ln.getDescendants_LibraryMembers().size());

		namedTypeCnt = setUpCase(2);
		ln.visitAllNodes(dv);
		ln.visitAllNodes(pv);
		assert ln.isEmpty();

		LOGGER.debug("***Setting Up Test Case 3");
		namedTypeCnt = setUpCase(3);
		LibraryChainNode lcn = ln.getChain();
		ln.visitAllNodes(dv);
		ln.visitAllNodes(pv);
		assert ln.isEmpty();
		assert lcn.isEmpty();

		LOGGER.debug("***Setting Up Test Case 4");
		namedTypeCnt = setUpCase(4);
		lcn = ln.getChain();
		ln.visitAllNodes(dv);
		ln.visitAllNodes(pv);
		assert ln.isEmpty();
		assert lcn.isEmpty();
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
			Assert.assertEquals(count, ln.getDescendants_LibraryMembers().size());
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
			Assert.assertEquals(count, ln.getDescendants_LibraryMembers().size());
			Assert.assertTrue(ln.isDeleteable());
			Assert.assertTrue(ln.isEditable());
			break;

		case 3:
			// Case 3 - managed library, editable, with constructed objects
			ln = ml.createNewLibrary("http://test.com/ns" + testCase, "testCase" + testCase, defaultProject);
			count = ml.addOneOfEach(ln, "case" + testCase);
			ln.visitAllNodes(tv);
			Assert.assertEquals(count, ln.getDescendants_LibraryMembers().size());
			new LibraryChainNode(ln);
			ln.setEditable(true); // must be done after LCN created
			Assert.assertTrue(ln.isEditable());
			Assert.assertTrue(ln.getChain().isEditable());
			Assert.assertTrue(ln.getChain().isMajor());
			Assert.assertEquals(count, ln.getDescendants_LibraryMembers().size());
			Assert.assertEquals(count, ln.getChain().getDescendants_LibraryMembers().size());
			Assert.assertFalse(ln.isManaged());
			Assert.assertFalse(ln.isInHead());
			break;

		case 4:
			ln = ml.createNewLibrary("http://test.com/ns" + testCase, "testCase" + testCase, defaultProject);
			count = ml.addOneOfEach(ln, "case" + testCase);
			ln.visitAllNodes(tv);
			Assert.assertEquals(count, ln.getDescendants_LibraryMembers().size());
			new LibraryChainNode(ln);
			ln.setEditable(true); // must be done after LCN created

			Node n = ml.addSimpleTypeToLibrary(ln, "case4S"); // creates family
			final String fixedName = n.getName();
			count++;
			// Node n = ln.findNodeByName(fixedName);
			Assert.assertNotNull(n);
			Assert.assertNotNull(ln.getChain().findNodeByName(fixedName));
			Assert.assertNotNull(ln.findNodeByName(fixedName));
			Assert.assertTrue(n.getParent() instanceof VersionNode);
			Assert.assertEquals(count, ln.getDescendants_LibraryMembers().size());
			Assert.assertEquals(count, ln.getChain().getDescendants_LibraryMembers().size());
			break;
		case 10:
			// TODO - managed library this is head with multiple libraries
			// TODO - managed library that is not head
		}
		return count;
	}

	@Test
	public void deleteResource() throws Exception {
		// FIXME
	}

	@Test
	public void deleteFamily() throws Exception {
		// TODO - if in chain, make sure both library and aggregate family is done
		ln = getEmptyLibrary(false);

		// Create a family
		Node n1 = ml.addBusinessObjectToLibrary(ln, "AAA_BO");
		assert ln.getComplexRoot().getChildren().contains(n1);
		Node n2 = ml.addCoreObjectToLibrary(ln, "AAA_Core");
		// July 2016 - family processing removed from app
		assert ln.getComplexRoot().getChildren().contains(n1);
		// assert !ln.getComplexRoot().getChildren().contains(n1);
		Node n3 = ml.addVWA_ToLibrary(ln, "AAA_VWA");
		FamilyNode family = null;
		for (Node n : ln.getComplexRoot().getChildren())
			if (n instanceof FamilyNode)
				family = (FamilyNode) n;
		assertTrue("Must not have a family node as they were removed from the application.", family == null);

		// assert family != null;
		// assert family.getChildren().size() == 3;
		// // delete them and make sure family deleted when only one left
		// n3.delete();
		// assert family.getChildren().size() == 2;
		// n2.delete();
		// assert family.isDeleted();
		// assert ln.getComplexRoot().getChildren().contains(n1);
		// n1.delete();
		// assert ln.isEmpty();
		ln.delete();

		//
		// Now repeat in managed library
		//
		ln = getEmptyLibrary(true);
		// Create a family. because they are managed, the components will be wrapped in version nodes
		Node nc1 = ml.addBusinessObjectToLibrary(ln, "AAA_BO");
		assert ln.getComplexRoot().getChildren().contains(nc1.getParent());
		assert ln.getChain().getComplexAggregate().getChildren().contains(nc1); // nc1 version node
		Node nc2 = ml.addCoreObjectToLibrary(ln, "AAA_Core");
		// assert !ln.getComplexRoot().getChildren().contains(nc1.getParent());
		assertTrue("Nodes that used to be in family must have version node with complex root as parent.", nc1
				.getParent().getParent() == ln.getComplexRoot());
		Node nc3 = ml.addVWA_ToLibrary(ln, "AAA_VWA");
		FamilyNode family2 = null;
		for (Node n : ln.getComplexRoot().getChildren())
			if (n instanceof FamilyNode)
				family2 = (FamilyNode) n;
		assertTrue("Must not have a family node as they were removed from the application.", family2 == null);

		// assert family2 != null;
		// assert family2.getChildren().size() == 3;
		// AggregateFamilyNode afn = null;
		// for (Node n : ln.getChain().getComplexAggregate().getChildren())
		// if (n instanceof AggregateFamilyNode)
		// afn = (AggregateFamilyNode) n;
		// assert afn != null;

		// // delete them and make sure family deleted when only one left
		nc3.delete();
		// assert afn.getChildren().size() == 2;
		nc2.delete();
		// assert family.isDeleted();
		// assert afn.isDeleted();
		// assert ln.getComplexRoot().getChildren().contains(nc1.getVersionNode());// nc1 version node
		// assert ln.getChain().getComplexAggregate().getChildren().contains(nc1);
		nc1.delete();
		assert ln.isEmpty();
		assert ln.getChain().isEmpty();
	}

	private LibraryNode getEmptyLibrary(boolean managed) {
		LibraryNode ln = ml.createNewLibrary("http://test.com/ns/df1", "testCase-deleteFamily", defaultProject);
		if (managed)
			new LibraryChainNode(ln);
		ln.setEditable(true); // must be done after LCN created
		List<Node> list = new ArrayList<Node>(ln.getDescendants_LibraryMembers());
		for (Node n : list)
			n.delete();
		assert ln.isEmpty();
		return ln;
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
				if (n.getName().startsWith("PaymentC"))
					LOGGER.debug("Core: " + n);

			if (!n.isNavigation()) {
				if (n.isDeleted())
					continue;
				INode user = null;

				// Make sure the users of this type are informed of deletion.
				if (n instanceof TypeProvider) {
					List<TypeUser> users = new ArrayList<TypeUser>(((TypeProvider) n).getWhereAssigned());
					if (users.size() > 0) {
						user = (INode) users.get(0);
					}
					n.delete();
					if (user != null && n.isDeleteable()) {
						Assert.assertNotSame(n, user.getType());
					}
				}
			}
		}
		ln.visitAllNodes(tv);
		MockLibrary.printDescendants(ln);
		MockLibrary.printDescendants(ln.getChain());
		// assert ln.isEmpty();
		// assert ln.getChain().isEmpty();
	}

	private void deleteNodeListTest(LibraryNode ln) {
		ArrayList<Node> members = new ArrayList<Node>(ln.getDescendants_LibraryMembers());
		ln.visitAllNodes(tv);
		Node.deleteNodeList(members);
		ln.visitAllNodes(tv);
		assert ln.isEmpty();
	}

}
