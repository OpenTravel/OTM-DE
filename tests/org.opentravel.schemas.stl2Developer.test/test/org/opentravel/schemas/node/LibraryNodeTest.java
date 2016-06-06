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
package org.opentravel.schemas.node;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.opentravel.schemacompiler.model.TLContext;
import org.opentravel.schemacompiler.saver.LibrarySaveException;
import org.opentravel.schemas.node.interfaces.ExtensionOwner;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.utils.BaseProjectTest;
import org.opentravel.schemas.utils.ComponentNodeBuilder;
import org.opentravel.schemas.utils.LibraryNodeBuilder;
import org.opentravel.schemas.utils.PropertyNodeBuilder;
import org.osgi.framework.Version;

/**
 * @author Pawel Jedruch
 * 
 */
public class LibraryNodeTest extends BaseProjectTest {

	@Test
	public void libraryInMultipleProjects() throws LibrarySaveException {
		ProjectNode project1 = createProject("Project1", rc.getLocalRepository(), "IT1");
		ProjectNode project2 = createProject("Project2", rc.getLocalRepository(), "IT2");

		LoadFiles lf = new LoadFiles();
		// MockLibrary ml = new MockLibrary();
		LibraryNode ln1 = lf.loadFile2(project1);
		LibraryNode ln2 = lf.loadFile2(project2);
		int ln2NamedTypeCount = ln2.getDescendants_NamedTypes().size();
		List<Node> complexNamedtypes = ln2.getDescendants_NamedTypes(); // hold onto for later use.

		assertTrue("Project is not empty.", !project1.getChildren().isEmpty());
		assertTrue("Project is not empty.", !project2.getChildren().isEmpty());

		// Close a library and check the other one to make sure it was not effected.
		mc.getLibraryController().remove(Arrays.asList(ln1)); // used in close library command
		// ln1.close(); // library controller uses close()
		assertTrue("Project 1 must be empty.", project1.getChildren().isEmpty());
		assertTrue("Lib1 must be empty.", ln1.getDescendants_NamedTypes().isEmpty());
		assertTrue("Lib2 must have same number of named types.",
				ln2.getDescendants_NamedTypes().size() == ln2NamedTypeCount);
		for (Node n : ln2.getDescendants_NamedTypes())
			assertTrue("Named type must not be deleted.", !n.isDeleted());

		// Same test with libraries in a chain
		ln1 = lf.loadFile2(project1);
		LibraryChainNode lcn1 = new LibraryChainNode(ln1);
		LibraryChainNode lcn2 = new LibraryChainNode(ln2);
		mc.getLibraryController().remove(Arrays.asList(lcn1)); // used in close library controller
		assertTrue("Project 1 must be empty.", project1.getChildren().isEmpty());
		assertTrue("Lib1 must be empty.", ln1.getDescendants_NamedTypes().isEmpty());
		assertTrue("Lib2 must have same number of named types.",
				ln2.getDescendants_NamedTypes().size() == ln2NamedTypeCount);
		for (Node n : ln2.getDescendants_NamedTypes())
			assertTrue("Named type must not be deleted.", !n.isDeleted());

		// delete second lib and insure deleted.
		mc.getLibraryController().remove(Arrays.asList(lcn2)); // used in close library command
		assertTrue("Project 2 must be empty.", project2.getChildren().isEmpty());
		assertTrue("Lib2 must be empty.", ln2.getDescendants_NamedTypes().isEmpty());

		// TODO - close the projects containing the libraries
		// done in after tests
	}

	@Test
	public void shouldNotDuplicatedContextOnImport() throws LibrarySaveException {
		LibraryNode importFrom = LibraryNodeBuilder.create("ImportFrom", defaultProject.getNamespace() + "/Test/One",
				"o1", new Version(1, 0, 0)).build(defaultProject, pc);

		TLContext c = new TLContext();
		c.setContextId("ContextID");
		c.setApplicationContext("newContext");
		importFrom.getTLLibrary().addContext(c);
		importFrom.addContexts();
		BusinessObjectNode bo = ComponentNodeBuilder.createBusinessObject("BO")
				.addCustomFacet("name", c.getContextId()).addCustomFacet("name2", c.getContextId()).get();
		importFrom.addMember(bo);

		LibraryNode importTo = LibraryNodeBuilder.create("ImportTo", defaultProject.getNamespace() + "/Test/TO", "to",
				new Version(1, 0, 0)).build(defaultProject, pc);

		List<String> beforeImport = importTo.getContextIds();
		List<String> fromContexts = importFrom.getContextIds();
		Node newNode = importTo.importNode(bo);
		List<String> afterImport = importTo.getContextIds();

		// FIXME - Assert.assertEquals(2, importTo.getContextIds().size());
		Assert.assertNotNull(importTo.getTLLibrary().getContext(c.getContextId()));
	}

	@Test
	public void moveNodeFromOneToOther() throws LibrarySaveException {
		LibraryNode moveFrom = LibraryNodeBuilder.create("MoveFrom", defaultProject.getNamespace() + "/Test/One", "o1",
				new Version(1, 0, 0)).build(defaultProject, pc);

		SimpleTypeNode moved = ComponentNodeBuilder.createSimpleObject("MyString")
				.assignType(NodeFinders.findNodeByName("string", ModelNode.XSD_NAMESPACE)).get();
		moveFrom.addMember(moved);

		PropertyNode withAssignedType = PropertyNodeBuilder.create(PropertyNodeType.ELEMENT).assign(moved).build();
		BusinessObjectNode bo = ComponentNodeBuilder.createBusinessObject("BO").get();
		bo.getSummaryFacet().addProperty(withAssignedType);
		moveFrom.addMember(bo);

		LibraryNode moveTo = LibraryNodeBuilder.create("MoveTo", defaultProject.getNamespace() + "/Test/TO", "to",
				new Version(1, 0, 0)).build(defaultProject, pc);

		moveFrom.moveMember(moved, moveTo);

		Assert.assertSame(moveTo, moved.getLibrary());
		assertTypeAssigments(moved, withAssignedType);
	}

	private void assertTypeAssigments(Node moved, Node withAssignedType) {
		// make sure that after move assigned pointing to the same node
		Assert.assertSame(moved, withAssignedType.getTypeNode());
		// make sure that after move TLObjects are pointing to the same TLType
		Assert.assertSame(moved.getModelObject().getTLModelObj(), withAssignedType.getTLTypeObject());
	}

	@Test
	public void moveBOToOther() throws LibrarySaveException {
		LibraryNode moveFrom = LibraryNodeBuilder.create("MoveFrom", defaultProject.getNamespace() + "/Test/One", "o1",
				new Version(1, 0, 0)).build(defaultProject, pc);
		// Create CO
		CoreObjectNode co = ComponentNodeBuilder.createCoreObject("CO").get(moveFrom);
		// Create attribute assigned to CO detail
		PropertyNode withAssignedType = PropertyNodeBuilder.create(PropertyNodeType.ATTRIBUTE).build();
		// CreateVWA
		VWA_Node vwa = ComponentNodeBuilder.createVWA("VWA").addAttribute(withAssignedType).get();
		moveFrom.addMember(vwa);
		withAssignedType.setAssignedType(co);

		LibraryNode moveTo = LibraryNodeBuilder.create("MoveTo", defaultProject.getNamespace() + "/Test/TO", "to",
				new Version(1, 0, 0)).build(defaultProject, pc);

		moveFrom.moveMember(co, moveTo);

		assertTypeAssigments(co, withAssignedType);
	}

	/**
	 * @throws LibrarySaveException
	 */
	@Test
	public void importNodesLocallyShouldReplaceTypesInDestination() throws LibrarySaveException {
		// given
		LibraryNode moveFrom = LibraryNodeBuilder.create("MoveFrom", defaultProject.getNamespace() + "/Test/One", "o1",
				new Version(1, 0, 0)).build(defaultProject, pc);
		CoreObjectNode coBase = ComponentNodeBuilder.createCoreObject("COBase").get(moveFrom);
		PropertyNode element = PropertyNodeBuilder.create(PropertyNodeType.ELEMENT).assign(coBase)
				.setName(coBase.getName()).build();
		CoreObjectNode coExt = ComponentNodeBuilder.createCoreObject("COExt").addToSummaryFacet(element).get(moveFrom);

		LibraryNode moveTo = LibraryNodeBuilder.create("MoveTo", defaultProject.getNamespace() + "/Test/TO", "to",
				new Version(1, 0, 0)).build(defaultProject, pc);

		// when
		moveTo.importNodes(moveFrom.getDescendants_NamedTypes(), false);

		// then
		assertSame(coBase, element.getAssignedType());
		Node movedBase = moveTo.findNodeByName("COBase");
		Node movedExt = moveTo.findNodeByName("COExt");
		// FIXME
		// assertSame(movedBase, movedExt.findNode(movedBase.getName(), movedExt.getNamespace()).getAssignedType());
	}

	@Test
	public void importNodesGloballyShouldReplaceBaseTypes() throws LibrarySaveException {
		// given
		LibraryNode moveFrom = LibraryNodeBuilder.create("MoveFrom", defaultProject.getNamespace() + "/Test/One", "o1",
				new Version(1, 0, 0)).build(defaultProject, pc);
		CoreObjectNode coBase = ComponentNodeBuilder.createCoreObject("COBase").get(moveFrom);
		CoreObjectNode coExt = ComponentNodeBuilder.createCoreObject("COExt").extend(coBase).get(moveFrom);
		assertTrue(coExt.isInstanceOf(coBase));

		LibraryNode importTo = LibraryNodeBuilder.create("MoveTo", defaultProject.getNamespace() + "/Test/TO", "to",
				new Version(1, 0, 0)).build(defaultProject, pc);

		// when
		importTo.importNodes(moveFrom.getDescendants_NamedTypes(), true);

		// then
		assertEquals(2, importTo.getDescendants_NamedTypes().size());
		Node newBase = importTo.findNodeByName("COBase");
		Node newExt = importTo.findNodeByName("COExt");
		boolean x = newExt.isInstanceOf(newBase);
		boolean y = newBase.isInstanceOf(newExt);
		boolean z = coExt.isInstanceOf(newBase); // true for global

		assertFalse("New extension must NOT be to old base.", newExt.isInstanceOf(coBase));
		assertTrue("New extension must be to new base.", newExt.isInstanceOf(newBase));
		assertTrue("Global import must change base type.", coExt.isInstanceOf(newBase));
	}

	@Test
	public void importNodesLocallyShouldReplaceBaseTypes() throws LibrarySaveException {
		// given
		LibraryNode moveFrom = LibraryNodeBuilder.create("MoveFrom", defaultProject.getNamespace() + "/Test/One", "o1",
				new Version(1, 0, 0)).build(defaultProject, pc);
		CoreObjectNode coBase = ComponentNodeBuilder.createCoreObject("COBase").get(moveFrom);
		CoreObjectNode coExt = ComponentNodeBuilder.createCoreObject("COExt").extend(coBase).get(moveFrom);
		assertTrue(coExt.isInstanceOf(coBase));

		LibraryNode importTo = LibraryNodeBuilder.create("MoveTo", defaultProject.getNamespace() + "/Test/TO", "to",
				new Version(1, 0, 0)).build(defaultProject, pc);

		// when
		importTo.importNodes(moveFrom.getDescendants_NamedTypes(), false);

		// then
		assertEquals(2, importTo.getDescendants_NamedTypes().size());
		Node newBase = importTo.findNodeByName("COBase");
		Node newExt = importTo.findNodeByName("COExt");
		assertTrue("Original extension must extend original base.", coExt.isInstanceOf(coBase));
		assertTrue("Imported extension must extend imported base.", newExt.isInstanceOf(newBase));
		assertTrue("Imported must have new base.", ((ExtensionOwner) newExt).getExtensionBase() == newBase);
	}

	/**
	 * ImportNodes uses importNode(source). This test focuses just on the clone and library add function of
	 * importNode().
	 * 
	 * @throws LibrarySaveException
	 */
	@Test
	public void importNode_Tests() throws LibrarySaveException {
		// given
		LibraryNode moveFrom = LibraryNodeBuilder.create("MoveFrom", defaultProject.getNamespace() + "/Test/One", "o1",
				new Version(1, 0, 0)).build(defaultProject, pc);
		CoreObjectNode coBase = ComponentNodeBuilder.createCoreObject("COBase").get(moveFrom);
		CoreObjectNode coExt = ComponentNodeBuilder.createCoreObject("COExt").extend(coBase).get(moveFrom);
		assertTrue(coExt.isInstanceOf(coBase));

		LibraryNode moveTo = LibraryNodeBuilder.create("MoveTo", defaultProject.getNamespace() + "/Test/TO", "to",
				new Version(1, 0, 0)).build(defaultProject, pc);

		// when
		Node newNode = moveTo.importNode(coBase);

		// then - cloned node must be in target library
		assertTrue(newNode != null);
		assertTrue(moveTo.getDescendants_NamedTypes().contains(newNode));

		// when
		newNode = moveTo.importNode(coExt);

		// then - cloned node must be in target library
		assertTrue(newNode != null);
		assertTrue(moveTo.getDescendants_NamedTypes().contains(newNode));
		assertTrue(newNode.isInstanceOf(coBase)); // should have cloned extension
		// NOTE - no type resolution has happened yet so where extended will not be set.

		// TODO - check contexts
	}
}
