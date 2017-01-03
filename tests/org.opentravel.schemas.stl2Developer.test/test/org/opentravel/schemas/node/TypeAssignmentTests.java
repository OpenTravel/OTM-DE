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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemacompiler.saver.LibrarySaveException;
import org.opentravel.schemas.controllers.DefaultProjectController;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.node.facets.FacetNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.testUtils.MockLibrary;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeResolver;
import org.opentravel.schemas.types.TypeUser;
import org.opentravel.schemas.utils.ComponentNodeBuilder;
import org.opentravel.schemas.utils.FacetNodeBuilder;
import org.opentravel.schemas.utils.LibraryNodeBuilder;
import org.osgi.framework.Version;

public class TypeAssignmentTests {

	private LibraryNode ln = null;
	BusinessObjectNode bo = null;
	CoreObjectNode co = null;
	ChoiceObjectNode ch = null;
	VWA_Node vo = null;
	ExtensionPointNode ep = null;
	EnumerationOpenNode eo = null;
	EnumerationClosedNode ec = null;
	SimpleTypeNode so = null;
	AliasNode ao = null;
	ImpliedNode un = null;
	SimpleTypeNode builtin = null;

	@Before
	public void beforeEachTest() throws LibrarySaveException {
		MainController mc = new MainController();
		MockLibrary mockLibrary = new MockLibrary();
		DefaultProjectController pc = (DefaultProjectController) mc.getProjectController();
		ProjectNode defaultProject = pc.getDefaultProject();
		// ln = mockLibrary.createNewLibrary("http://example.com/test", "test", defaultProject);
		ln = LibraryNodeBuilder.create("Example", "http://example.org", "p", new Version(1, 1, 1)).build(
				defaultProject, pc);
		ln.setEditable(true);

		builtin = (SimpleTypeNode) NodeFinders.findNodeByName("date", ModelNode.XSD_NAMESPACE);
		TypeProvider tp;
		// Consistent set of all type providers
		bo = ComponentNodeBuilder.createBusinessObject("boBase").get(ln);
		co = ComponentNodeBuilder.createCoreObject("coBase").get(ln);
		ch = ComponentNodeBuilder.createChoiceObject("chBase").get(ln);
		vo = ComponentNodeBuilder.createVWA("vwaBase").get(ln);
		ep = ComponentNodeBuilder.createExtensionPoint("epBase").get(ln);
		eo = ComponentNodeBuilder.createEnumerationOpen("eoBase").get(ln);

		ec = ComponentNodeBuilder.createEnumerationClosed("ecBase").get(ln);
		so = ComponentNodeBuilder.createSimpleObject("simpleBase").assignType(builtin).get(ln);
		ao = new AliasNode(bo, "boAlias");
		un = ModelNode.getUnassignedNode();
	}

	// See TestTypes
	@Test
	public void assignToElements() {
		FacetNode facetNode = FacetNodeBuilder.create(ln).addElements("E1", "E2", "E3").build();
	}

	@Test
	public void assignToAttributes() {
		FacetNode facetNode = FacetNodeBuilder.create(ln).addAttributes("A1", "A2", "A3").build();
		new TypeResolver().resolveTypes(ln); // create where used and listeners
		int soCount = so.getWhereAssignedCount();
		int ecCount = ec.getWhereAssignedCount();
		int unusedCount = un.getWhereAssignedCount();

		// Make the assignments
		for (Node child : facetNode.getChildren()) {
			assert child instanceof TypeUser;
			TypeUser user = (TypeUser) child;
			// Assign to one type, none then a different one.
			assertTrue("Is unassigned.", un.getWhereAssigned().contains(user));
			assertTrue("Assure deprecated method works.", un.getWhereAssigned().contains(user));

			user.setAssignedType(so);
			assertFalse("Is unassigned.", un.getWhereAssigned().contains(user));
			assertTrue("Is assigned.", so.getWhereAssigned().contains(user));

			user.setAssignedType();
			assertFalse("Is unassigned.", so.getWhereAssigned().contains(user));
			assertTrue("Is unassigned.", un.getWhereAssigned().contains(user));

			user.setAssignedType(ec);
			assertFalse("Is assigned.", un.getWhereAssigned().contains(user));
			assertTrue("Is assigned.", ec.getWhereAssigned().contains(user));
		}

		// Check resulting assignment and where used information
		assert ((TypeUser) facetNode.getChildren().get(0)).getAssignedType() == ec;

		assert !so.getWhereAssigned().contains(facetNode.getChildren().get(0));
		assert !un.getWhereAssigned().contains(facetNode.getChildren().get(0));
		assert ec.getWhereAssigned().contains(facetNode.getChildren().get(0));

		assert soCount == so.getWhereAssignedCount();
		assert ecCount != ec.getWhereAssignedCount(); // ec was assigned so should be different
	}

}
