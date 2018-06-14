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
import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Test;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.interfaces.SimpleMemberInterface;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.typeProviders.SimpleTypeNode;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.testUtils.BaseTest;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.types.TypeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dave Hollander
 * 
 */
public class SimpleTypeNodeTests extends BaseTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(SimpleTypeNodeTests.class);

	@Test
	public void simpleTypeNode_LoadFileTests() throws Exception {
		MainController mc = OtmRegistry.getMainController();
		LoadFiles lf = new LoadFiles();
		lf.loadTestGroupA(mc);
		for (LibraryNode ln : Node.getAllLibraries()) {
			checkGetDescendants_SimpleComponents(ln);
			ml.check(ln, false);
		}
	}

	private void checkGetDescendants_SimpleComponents(LibraryNode lib) {
		int simpleCnt = 0;
		for (LibraryMemberInterface type : lib.getDescendants_LibraryMembers())
			if (type instanceof SimpleMemberInterface) {
				simpleCnt++;
				ml.check((Node) type);
			}
		assertEquals(simpleCnt, lib.getDescendants_SimpleMembers().size());
	}

	public void check(SimpleTypeNode node) {
		check(node, true);
	}

	public void check(SimpleTypeNode st, boolean validate) {
		assertTrue(st != null);
		assertTrue(st.getLibrary() != null);
		assertTrue(st.getParent() != null);
		if (st.getAssignedType() == null)
			LOGGER.debug("Null assigned type: " + st + " " + st.getAssignedType());
		assertTrue(st.getAssignedType() != null);
		assertTrue(st.getName() != null);
		assertTrue(!st.getName().isEmpty());
		assertTrue(st.getLabel() != null);
		assertTrue(st.getTLModelObject() != null);

		assertTrue(st.getChildren().isEmpty());

		// where assigned hander
		// type handler
		assertTrue(st.getChildrenHandler() != null);
		assertTrue(st.getDocHandler() != null);
		assertTrue(st.getConstraintHandler() != null);
		if (st.getXsdObjectHandler() != null)
			assertTrue(st.getXsdObjectHandler().getOwner() == st);
	}

	// Constructor Tests
	@Test
	public void ST_ConstructorTests() {
		// Given - a tl simple
		TLSimple tls = new TLSimple();

		// When - simple type node created
		SimpleTypeNode simple = new SimpleTypeNode(tls);

		// Then - get tl model object
		assertTrue(simple.getTLModelObject() == tls);
		assertTrue(simple == Node.GetNode(simple.getTLModelObject()));

		// Then - handlers: type, constraint, equivalent, example
		assertTrue(simple.getConstraintHandler() != null);
		assertTrue(simple.getTypeHandler() != null);

		// Then - handlers: whereExtended, documentation
		assertTrue(simple.getDocHandler() != null);
		assertTrue(simple.getWhereAssignedHandler() != null);

		// Make it valid then check it.
		simple.setName("MySimple");
		ln = ml.createNewLibrary(pc, "simpleTest");
		ln.addMember(simple);
		check(simple);
	}

	@Test
	public void simpleType_MethodTests() {
		// Given - a valid simpleTypeNode
		TLSimple tls = new TLSimple();
		SimpleTypeNode simple = new SimpleTypeNode(tls);
		simple.setName("MySimple");
		ln = ml.createNewLibrary(pc, "simpleTest");
		ln.addMember(simple);
		check(simple);

		// equivalent and example - see handler tests
		simple.setExample("Ex1");
		simple.setEquivalent("EQ1");
		assertTrue(simple.getExample(null).equals("Ex1"));
		assertTrue(simple.getEquivalent(null).equals("EQ1"));

		// List
		assertTrue(simple.isSimpleList() == false);
		simple.setList(true);
		assertTrue(simple.isSimpleList() == true);

		// Delete
		assertTrue(ln.get_LibraryMembers().contains(simple));
		simple.delete();
		assertTrue(!ln.get_LibraryMembers().contains(simple));
	}

	@Test
	public void simpleType_TypeTests() {
		// Given - a valid simpleTypeNode
		TLSimple tls = new TLSimple();
		SimpleTypeNode simple = new SimpleTypeNode(tls);
		simple.setName("MySimple");
		ln = ml.createNewLibrary(pc, "simpleTest");
		ln.addMember(simple);
		check(simple);

		// Given - A built in type
		Node aType = NodeFinders.findNodeByName("date", ModelNode.XSD_NAMESPACE);

		TypeProvider initialType = simple.getAssignedType();
		assertTrue(initialType != null);

	}

	@Test
	public void simpleType_checkDescendantCounts() {
		for (LibraryNode lib : Node.getAllLibraries()) {
			int simpleCnt = 0;
			for (Node type : lib.getDescendants_LibraryMembersAsNodes()) {
				if (type instanceof SimpleMemberInterface) {
					simpleCnt++;
				}
			}
			int libCnt = lib.getDescendants_SimpleMembers().size();
			Assert.assertEquals(simpleCnt, lib.getDescendants_SimpleMembers().size());
		}
	}

}
