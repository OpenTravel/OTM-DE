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
package org.opentravel.schemas.functional;

import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.objectMembers.ExtensionPointNode;
import org.opentravel.schemas.node.properties.AttributeNode;
import org.opentravel.schemas.node.properties.IdNode;
import org.opentravel.schemas.node.typeProviders.AliasNode;
import org.opentravel.schemas.node.typeProviders.ChoiceObjectNode;
import org.opentravel.schemas.node.typeProviders.EnumerationClosedNode;
import org.opentravel.schemas.node.typeProviders.EnumerationOpenNode;
import org.opentravel.schemas.node.typeProviders.FacetProviderNode;
import org.opentravel.schemas.node.typeProviders.ImpliedNode;
import org.opentravel.schemas.node.typeProviders.SimpleTypeNode;
import org.opentravel.schemas.node.typeProviders.VWA_Node;
import org.opentravel.schemas.node.typeProviders.facetOwners.BusinessObjectNode;
import org.opentravel.schemas.node.typeProviders.facetOwners.CoreObjectNode;
import org.opentravel.schemas.testUtils.BaseTest;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeResolver;
import org.opentravel.schemas.types.TypeUser;
import org.opentravel.schemas.utils.FacetNodeBuilder;

public class TypeAssignmentTests extends BaseTest {

	// private LibraryNode ln = null;
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
	TypeProvider emptyNode = null;
	TypeProvider sType = null;
	TypeProvider intType = null;

	@Before
	public void beforeEachEachOfTheseTests() {
		ln = ml.createNewLibrary_Empty(defaultProject.getNamespace(), "Test1", defaultProject);
		assert ln.isEditable();

		emptyNode = (TypeProvider) ModelNode.getEmptyNode();
		sType = ml.getXsdDate();
		intType = ml.getXsdInt();

		bo = ml.addBusinessObjectToLibrary(ln, "boBase", true);
		co = ml.addCoreObjectToLibrary(ln, "coBase");
		ch = ml.addChoice(ln, "chBase");
		vo = ml.addVWA_ToLibrary(ln, "vwaBase");
		// ep = ml.addExtensionPoint(ln, facet)"epBase");
		eo = ml.addOpenEnumToLibrary(ln, "eoBase");

		ec = ml.addClosedEnumToLibrary(ln, "ecBase");
		so = ml.addSimpleTypeToLibrary(ln, "simpleBase");

		ao = new AliasNode(bo, "boAlias");
		un = ModelNode.getUnassignedNode();

	}

	// @See TestTypes
	@Test
	public void assignBuiltInTests() {
		// Given a built in not used by core or vwa
		SimpleTypeNode builtIn = (SimpleTypeNode) ml.getXsd("decimal");
		assert builtIn != null;
		Collection<TypeUser> whereAssigned = builtIn.getWhereAssigned();

		// When assigned to VWA
		vo.setAssignedType(builtIn);
		Collection<TypeUser> whereAssigned2 = builtIn.getWhereAssigned();
		// Then - must have additional where used
		assertTrue("Int msut be assigned.", vo.getAssignedType() == builtIn);
		assertTrue("Must be assigned to more places.", whereAssigned.size() < whereAssigned2.size());
		assertTrue(builtIn.getWhereAssigned().contains(vo.getSimpleAttribute()));

		// When assigned to Core
		assert co.getAssignedType() != builtIn;
		co.setAssignedType(builtIn);
		Collection<TypeUser> whereAssigned3 = builtIn.getWhereAssigned();
		// Then - must have additional where used
		assertTrue("Int msut be assigned.", co.getAssignedType() == builtIn);
		assertTrue("Must be assigned to more places.", whereAssigned2.size() < whereAssigned3.size());
		assertTrue(builtIn.getWhereAssigned().contains(co.getSimpleAttribute()));

		// When assigned to core properties
		AttributeNode a = new AttributeNode(co.getFacet_Summary(), "attr1", sType);
		List<TypeUser> users = co.getDescendants_TypeUsers();
		TypeProvider actual = null;
		for (TypeUser u : co.getDescendants_TypeUsers()) {
			ml.check(co);
			// Not all type users (such as ID) can be assigned
			if ((actual = u.setAssignedType(builtIn)) != null)
				assertTrue(actual.getWhereAssigned().contains(u));
			ml.check(co);
		}
	}

	@Test
	public void assignToID() {

		// Given - an ID node added to a business object
		BusinessObjectNode bo = ml.addBusinessObjectToLibrary(ln, "MyBO");
		TypeUser id = new IdNode(bo.getFacet_ID(), "ThisID");

		// Then
		TypeProvider type = id.getAssignedType();
		assertTrue("ID must have type.", type != null);
		assertTrue("ID must be in types where used.", type.getWhereAssigned().contains(id));
	}

	// @See TestTypes
	@Test
	public void assignToElements() {
	}

	@Test
	public void assignToAttributes() {
		FacetProviderNode facetNode = FacetNodeBuilder.create(ln).addAttributes("A1", "A2", "A3").build();
		new TypeResolver().resolveTypes(ln); // create where used and listeners
		int soCount = so.getWhereAssignedCount();
		int ecCount = ec.getWhereAssignedCount();
		// int unusedCount = un.getWhereAssignedCount();

		// Make the assignments
		for (Node child : facetNode.getChildren()) {
			assert child instanceof TypeUser;
			TypeUser user = (TypeUser) child;
			assertTrue("Must be unassigned.", un.getWhereAssigned().contains(user));

			// Assign to one type,
			user.setAssignedType(so);
			assertTrue("Is assigned.", user.getAssignedType() == so);
			assertTrue("Is not unassigned.", !un.getWhereAssigned().contains(user));
			assertTrue("Type's where used must contain user.", so.getWhereAssigned().contains(user));

			// Assign none
			user.setAssignedType();
			assertTrue("Must be unchanged.", user.getAssignedType() == so); // UNCHANGED!
			assertTrue("Previous type must not have this as were assigned.", !so.getWhereAssigned().contains(user));
			assertTrue("Is unassigned.", un.getWhereAssigned().contains(user));

			// then assign a different one.
			user.setAssignedType(ec);
			assertTrue("Is assigned.", user.getAssignedType() == ec);
			assertTrue("New type must have this as where assigned.", ec.getWhereAssigned().contains(user));
			assertTrue("Unassigned type must not have this as were assigned.", !un.getWhereAssigned().contains(user));
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
