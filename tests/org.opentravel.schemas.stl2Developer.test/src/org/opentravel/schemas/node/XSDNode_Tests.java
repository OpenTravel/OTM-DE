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

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.properties.AttributeNode;
import org.opentravel.schemas.node.properties.ElementNode;
import org.opentravel.schemas.node.typeProviders.SimpleTypeNode;
import org.opentravel.schemas.node.typeProviders.facetOwners.BusinessObjectNode;
import org.opentravel.schemas.testUtils.BaseTest;
import org.opentravel.schemas.types.TypeUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dave Hollander
 * 
 */
public class XSDNode_Tests extends BaseTest {
	private final static Logger LOGGER = LoggerFactory.getLogger(XSDNode_Tests.class);

	// LoadFiles lf = new LoadFiles();
	// MockLibrary ml = new MockLibrary();;
	// LibraryNode ln = null;
	// ProjectNode defaultProject;

	AttributeNode attr = null;
	ElementNode ele = null;
	SimpleTypeNode simple = null;

	// From baseProjecTest
	// rc, mc, pc, testProject
	// MainController mc;
	// DefaultProjectController pc;

	// @Before
	// public void beforeAllTests() {
	// LOGGER.debug("Initializing Test Setup.");
	// defaultProject = pc.getDefaultProject();
	// }

	@Test
	public void XSD_BuiltInTests() {
		// Given - the built-in libraries loaded into the model
		LibraryNode builtIn = ml.getBuiltInLibrary(false); // this is the ota2 builtin

		LOGGER.debug("Checking builtin library: " + builtIn);
		ml.check(builtIn);
		for (LibraryMember nm : builtIn.getTLModelObject().getNamedMembers()) {
			// Assure they have identity listener
			assertTrue(Node.GetNode(nm) != null);
		}

		for (LibraryMemberInterface n : builtIn.get_LibraryMembers())
			if (n instanceof SimpleTypeNode) {
				SimpleTypeNode st = (SimpleTypeNode) n;
				assertTrue(st.getXsdObjectHandler() != null);
				assertTrue(st.getXsdObjectHandler().getOwner() == st);

				assertTrue(st.getXsdObjectHandler().getTLLibraryMember() != null);
				assertTrue("Src TLSimle must have correct identity listener.",
						st == Node.GetNode(st.getXsdObjectHandler().getTLLibraryMember()));
				// Will be the builtTL not srcTL
				// assertTrue(st.getTLModelObject() == st.getXsdObjectHandler().getTLLibraryMember());

				assertTrue("Required type for simple types must be null.", st.getRequiredType() == null);
				// assertTrue(st.getRequiredType() == ModelNode.getUndefinedNode());
				assertTrue(st.getAssignedType() == ModelNode.getUndefinedNode());
				assertTrue(st.getAssignedTLObject() == null);
				assertTrue(st.getAssignedTLNamedEntity() == null);
				// assertTrue(st.getAssignable() == null);

				assertTrue(st.getNamespace().equals("http://www.opentravel.org/OTM/Common/v0"));
				assertTrue(st.getPrefix().equals("ota2"));
				// assertTrue("Must be assigned an XSD type.", st.getAssignedPrefix().equals("xsd"));

				assertTrue(st.getDecoration().contains("users"));
				assertTrue(!st.getName().isEmpty());

				Node at8 = st.getExtendsType(); // ?? null
				// LOGGER.debug("Built-In: " + st);
			} else {
				LOGGER.debug("TODO - test non-simple built-in: " + n);
				// expect a core object for extension point
			}
	}

	@Test
	public void XSD_AssignToTests() {
		createTypeUsers();

		// Given - the built-in libraries loaded into the model
		LibraryNode builtIn = ml.getBuiltInLibrary(false); // this is the ota2 built-in
		ml.check(builtIn);
		checkAssignments(builtIn, ele, attr, simple);

		builtIn = ml.getBuiltInLibrary(true); // this is the xsd built-in
		ml.check(builtIn);
		checkAssignments(builtIn, ele, attr, simple);
	}

	@Test
	public void XSD_LoadXSDFileTests() {
		// Given - 3 global type users in a library
		createTypeUsers();

		// Given - the 3 xsd files loaded into the model
		//
		// FIXME - all these fail to assign simple types on load
		//
		// lf.loadFileXsd1(pc.getDefaultProject());
		// lf.loadFileXsd2(pc.getDefaultProject());
		// lf.loadFileXsd3(pc.getDefaultProject());
		// new TypeResolver().resolveTypes();

		ml.check();

		// XSD Libraries are NOT considered user libraries.
		for (LibraryNode ln : Node.getModelNode().getLibraries()) {
			LOGGER.debug("Checking library: " + ln);
			ml.check(ln, false); // May not be valid
			checkAssignments(ln, ele, attr, simple);
		}
	}

	/**
	 * Assign all simple type nodes in the passed library to all type users passed as arguments.
	 */
	private void checkAssignments(LibraryNode ln, TypeUser... users) {
		if (ln.getName().equals("CommonTypes"))
			LOGGER.debug("Here is a test case that fails.");
		for (LibraryMemberInterface n : ln.get_LibraryMembers())
			if (n instanceof SimpleTypeNode) {
				SimpleTypeNode st = (SimpleTypeNode) n;
				for (TypeUser user : users) {
					// When - assign type is successful
					if (user.setAssignedType(st) != null) { // fails here
						// Then
						LOGGER.debug("Assigned " + st + " to " + user);
						assertTrue(st == user.getAssignedType());
						assertTrue(st.getWhereAssigned().contains(user));
					} else
						LOGGER.debug("Could not assign " + st + " to " + user);
				}
			}
	}

	/**
	 * Create a library and
	 */
	private void createTypeUsers() {
		// Given - a library for the types to assign to
		ln = ml.createNewLibrary(pc, "test");
		for (LibraryMemberInterface n : ln.get_LibraryMembers())
			if (n instanceof BusinessObjectNode)
				for (Node p : ((BusinessObjectNode) n).getFacet_Summary().getChildren())
					if (p instanceof AttributeNode)
						attr = (AttributeNode) p;
					else if (p instanceof ElementNode)
						ele = (ElementNode) p;

		// Given - a simple type to assign to
		simple = new SimpleTypeNode(new TLSimple());
		simple.setName("starget");
		simple.setAssignedType(ml.getSimpleTypeProvider());
		ln.addMember(simple);

		ml.check(attr);
		ml.check(simple);
		ml.check(ele);
	}

	// Not needed - check library would fail if not unique
	// @Test
	// public void XSD_UniqueTests() {
	//
	// }

}
