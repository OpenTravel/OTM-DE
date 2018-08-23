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

import java.util.Collection;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemacompiler.event.ModelElementListener;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemas.actions.ChangeNodeActionTests;
import org.opentravel.schemas.node.interfaces.FacetOwner;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.listeners.TypeUserAssignmentListener;
import org.opentravel.schemas.node.properties.ElementNode;
import org.opentravel.schemas.node.properties.TypedPropertyNode;
import org.opentravel.schemas.node.typeProviders.VWA_Node;
import org.opentravel.schemas.node.typeProviders.facetOwners.BusinessObjectNode;
import org.opentravel.schemas.node.typeProviders.facetOwners.CoreObjectNode;
import org.opentravel.schemas.testUtils.BaseTest;
import org.opentravel.schemas.testUtils.NodeTesters;
import org.opentravel.schemas.testUtils.NodeTesters.TestNode;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test changing types of library members (business objects, core object, vwa, etc.)
 * 
 * @author Dave Hollander
 * 
 */
public class ChangeTo_Tests extends BaseTest {
	private final static Logger LOGGER = LoggerFactory.getLogger(ChangeTo_Tests.class);

	TestNode tn = new NodeTesters().new TestNode();
	LibraryChainNode lcn = null;

	@Before
	public void beforeCT() {
		LOGGER.debug("BeforeCT running.");
		lcn = ml.createNewManagedLibrary("test", defaultProject);
		Assert.assertNotNull(lcn);
		ln = lcn.getHead();
		Assert.assertNotNull(ln);
		Assert.assertTrue(ln.isEditable());
	}

	@Test
	public void changeToVWA1() {
		BusinessObjectNode bo = ml.addBusinessObjectToLibrary(ln, "bo");
		CoreObjectNode core = ml.addCoreObjectToLibrary(ln, "co");
		VWA_Node tVwa = null, vwa = ml.addVWA_ToLibrary(ln, "vwa");

		int typeCount = ln.getDescendants_LibraryMembers().size();
		// When converted to VWA, the facets are lost.
		// typeCount -= bo.getDescendants_ContributedFacets().size();

		tVwa = new VWA_Node(bo);
		tVwa.setName(tVwa.getName() + "v");
		ml.check(tVwa);
		tVwa = new VWA_Node(core);
		tVwa.setName(tVwa.getName() + "vv");
		ml.check(tVwa);

		Assert.assertEquals(typeCount + 2, ln.getDescendants_LibraryMembers().size());
	}

	@Test
	public void changeToVWA2() {

		// Given: business and core objects in a managed library used as types.
		BusinessObjectNode bo = ml.addBusinessObjectToLibrary(ln, "A");
		CoreObjectNode core = ml.addCoreObjectToLibrary(ln, "B");
		CoreObjectNode user = ml.addCoreObjectToLibrary(ln, "User");
		TypedPropertyNode p1 = new ElementNode(user.getFacet_Summary(), "P1", bo);
		TypedPropertyNode p2 = new ElementNode(user.getFacet_Summary(), "P2", core);
		TypedPropertyNode p3 = new ElementNode(user.getFacet_Summary(), "P3", core.getFacet_Detail());

		int boCount = bo.getFacet_ID().getChildren().size() + bo.getFacet_Summary().getChildren().size()
				+ bo.getFacet_Detail().getChildren().size();
		int coreCount = core.getFacet_Summary().getChildren().size() + core.getFacet_Detail().getChildren().size();
		int boWhereAssignedCount = bo.getWhereUsedAndDescendantsCount();
		int coreWhereAssignedCount = core.getWhereUsedAndDescendantsCount();
		ml.check(bo);
		ml.check(core);

		VWA_Node vwa = null;

		// When - a new VWA is created from BO
		vwa = new VWA_Node(bo);
		// Then - name and property counts are correct.
		assertEquals(bo.getName(), vwa.getName());
		assertEquals("Must have attribute for each BO property.", boCount,
				vwa.getFacet_Attributes().getChildren().size());
		// Then - TL object may have attributes and indicators

		// When - VWA replaces BO. BO is assigned to an element and elementRef
		bo.replaceWith(vwa);
		ml.check(vwa, false);
		assertEquals("Replace assignments on non-referene users.", boWhereAssignedCount - 1,
				vwa.getWhereUsedAndDescendantsCount());
		assertEquals(vwa, p1.getAssignedType());
		ml.check(vwa, false);

		//
		// When - a new VWA is created from Core
		vwa = new VWA_Node(core);
		assertEquals("B", vwa.getName());
		assertEquals(core.getAssignedType(), vwa.getAssignedType());
		assertEquals("Must have attribute for each Core property.", coreCount,
				vwa.getFacet_Attributes().getChildren().size());

		// When - VWA replaces core.
		core.replaceWith(vwa);
		// Then
		ml.check(vwa, false);
		assertEquals(coreWhereAssignedCount, vwa.getWhereUsedAndDescendantsCount());
		assertEquals(vwa, p2.getAssignedType());
		assertEquals(vwa, p3.getAssignedType());
		assertTrue("VWA must be in the library after swap.", ln.getDescendants_LibraryMembers().contains(vwa));
		assertTrue("Core must NOT be in the library after swap.", !ln.getDescendants_LibraryMembers().contains(core));
	}

	@Test
	public void changeToCore() {
		CoreObjectNode core = null;
		// TODO - add where used tests

		// Given a Business Object in managed, editable library
		BusinessObjectNode bo = ml.addBusinessObjectToLibrary(ln, "A");
		int boCount = bo.getFacet_Summary().getChildren().size() + bo.getFacet_ID().getChildren().size();
		tn.visit(bo);
		// Given a VWA
		VWA_Node vwa = ml.addVWA_ToLibrary(ln, "B");
		// vwa value is added as attribute to core
		int vwaCount = vwa.getFacet_Attributes().getChildren().size() + 1;

		// When - core created from BO replaces the business object
		core = new CoreObjectNode(bo);
		bo.replaceWith(core);

		// Then - test name and summary facet properties
		tn.visit(core);
		assertEquals("A", core.getName());
		assertEquals(boCount, core.getFacet_Summary().getChildren().size());
		assertTrue("Core must be in the library after swap.", ln.getDescendants_LibraryMembers().contains(core));
		assertTrue("BO must NOT be in the library after swap.", !ln.getDescendants_LibraryMembers().contains(bo));

		// When - core created from VWA replaces VWA
		core = new CoreObjectNode(vwa);
		vwa.replaceWith(core);

		// Then - test name and summary facet properties
		assertEquals("B", core.getName());
		tn.visit(core);
		assertEquals(vwaCount, core.getFacet_Summary().getChildren().size());
		assertEquals(core.getAssignedType(), vwa.getAssignedType());
		assertEquals(core.getTLModelObject().getSummaryFacet().getAttributes().size(),
				core.getFacet_Summary().getChildren().size());
		assertTrue("Core must be in the library after swap.", ln.getDescendants_LibraryMembers().contains(core));
		assertTrue("VWA must NOT be in the library after swap.", !ln.getDescendants_LibraryMembers().contains(vwa));
	}

	@Test
	public void changeCheckUsersCounts() {
		CoreObjectNode core = ml.addCoreObjectToLibrary(ln, "C");
		TypedPropertyNode p1 = new ElementNode(core.getFacet_Summary(), "P1");
		VWA_Node vwa = ml.addVWA_ToLibrary(ln, "B");
		tn.visit(ln);
		tn.visit(lcn);

		// Make assignment and assure counts are correct.
		Collection<TypeUser> list = null;
		p1.setAssignedType(vwa);
		Assert.assertTrue("P1 must be assigned VWA as type.", p1.getAssignedType() == vwa);
		list = vwa.getWhereAssigned();
		Assert.assertEquals(1, vwa.getWhereAssigned().size());

		ComponentNode nc = new CoreObjectNode(vwa);
		vwa.replaceWith((LibraryMemberInterface) nc);
		Assert.assertTrue("P1 must now be assigned the new core object.", p1.getAssignedType() == nc);
		list = ((TypeProvider) nc).getWhereAssigned();
		Assert.assertTrue("New core must have P1 in its where assigned list.", list.contains(p1));
		Assert.assertEquals(1, ((TypeProvider) nc).getWhereAssigned().size());
	}

	/**
	 * @see ChangeNodeActionTests
	 */
	@Test
	public void changeAsInMainController() {
	}

	@Test
	public void changeToBO() {
		// Given - a core and VWA in a library
		CoreObjectNode core = ml.addCoreObjectToLibrary(ln, "A");
		VWA_Node vwa = ml.addVWA_ToLibrary(ln, "B");
		Assert.assertNotNull(core);
		Assert.assertNotNull(vwa);
		BusinessObjectNode bo = null;
		ml.check();

		// When - BO created from Core
		bo = new BusinessObjectNode(core);
		// Then - properties created and ID fixed
		assertTrue("BO must have core's name.", bo.getName().equals(core.getName()));
		assertTrue("Must have one ID property.", bo.getFacet_ID().getChildren().size() == 1);
		assertTrue("Must have one Summary property.", bo.getFacet_Summary().getChildren().size() == 1);

		// Then - structure must be OK, but not valid due to name collisions.
		ml.check(ln, false);

		// When - removed from library it will be valid
		ln.removeMember(core);
		// Then - valid
		ml.check();

		// When - bo created from vwa
		bo = new BusinessObjectNode(vwa);
		assertTrue("BO must have vwa's name.", bo.getName().equals(vwa.getName()));
		assertTrue("Must have one ID property.", bo.getFacet_ID().getChildren().size() == 1);
		assertTrue("Must have one Summary property.", bo.getFacet_Summary().getChildren().size() == 1);
		// Then - structure must be OK, but not valid due to name collisions.
		ml.check(ln, false);

		// When - core and vwa removed from library it will be valid
		ln.removeMember(vwa);
		// Then - valid
		ml.check();
	}

	@Test
	public void changeTestGroupA_Tests() throws Exception {

		lf.loadTestGroupA(mc);
		// Age is troublesome -- a VWA in Test5c assigned to two elements in Test4
		LibraryMemberInterface age = null;
		for (LibraryMemberInterface lm : mc.getModelNode().getDescendants_LibraryMembers())
			if (lm.getName().equals("Age"))
				age = lm;
		assert age != null;
		Collection<TypeUser> ageUsers = ((TypeProvider) age).getWhereAssigned();
		// Verify the users' listeners are correct
		Collection<ModelElementListener> listeners = null;
		Node p = null;
		for (TypeUser u : ageUsers) {
			listeners = u.getTLModelObject().getListeners();
			for (ModelElementListener li : listeners)
				if (li instanceof TypeUserAssignmentListener)
					assert ((TypeUserAssignmentListener) li).getNode() == age;
		}

		for (LibraryNode ln : mc.getModelNode().getUserLibraries()) {
			if (!ln.isInChain())
				lcn = new LibraryChainNode(ln);
			else
				lcn = ln.getChain();

			changeMembers(ln);

			// ln.visitAllNodes(tn);
		}
	}

	/**
	 * Change all library members in the passed library.
	 * 
	 * @param ln
	 */
	private void changeMembers(LibraryNode ln) {
		LOGGER.debug("Changing members in " + ln);
		LibraryMemberInterface nn = null;
		int equCount = 0, newEquCount = 0;
		TypedPropertyNode aProperty = null;
		TypedPropertyNode newProperty = null;
		ln.setEditable(true);

		// Do not validate - name changes and base object type changes may invalidate library
		boolean validate = false;
		ml.check(ln, validate);

		// Get all library members and change them.
		for (LibraryMemberInterface lm : ln.getDescendants_LibraryMembers()) {
			equCount = countEquivelents(lm);

			if (lm instanceof ComponentNode && !lm.isDeleted()) {
				ComponentNode cn = (ComponentNode) lm;
				assert cn.isDeleteable();
				ml.check(cn, validate);

				if (lm instanceof BusinessObjectNode) {
					// LOGGER.debug("When - Changing " + lm + " from business object to core.");
					nn = new CoreObjectNode((BusinessObjectNode) lm);
					cn.replaceWith(nn);
					// Then
					assertEquals(equCount, countEquivelents(nn));

				} else if (lm instanceof CoreObjectNode) {
					// LOGGER.debug("When - Changing " + lm + " from core to business object.");

					// Pick last summary property for testing.
					aProperty = null;
					if (((FacetOwner) lm).getFacet_Summary().getChildren_TypeUsers().size() > 0)
						aProperty = (TypedPropertyNode) ((FacetOwner) lm).getFacet_Summary().getChildren_TypeUsers()
								.get(((FacetOwner) lm).getFacet_Summary().getChildren_TypeUsers().size() - 1);

					// Do not test assignment if the core is used as a type
					if (cn == aProperty.getAssignedType())
						aProperty = null;
					// If the type of is in this core then do not test it.
					if (aProperty != null && cn.contains((Node) aProperty.getAssignedType()))
						aProperty = null;

					nn = new BusinessObjectNode((CoreObjectNode) lm);
					cn.replaceWith(nn);

					// Find the property with the same name for testing.
					if (aProperty != null)
						newProperty = (TypedPropertyNode) ((BusinessObjectNode) nn).getFacet_Summary()
								.findChildByName(aProperty.getName());
					if (aProperty != null && newProperty != null) {
						TypeProvider npAT = newProperty.getAssignedType();
						TypeProvider apAT = aProperty.getAssignedType();
						if (npAT != apAT)
							LOGGER.debug("Assigned types do not match.");
						assertTrue(newProperty.getAssignedType() == aProperty.getAssignedType());
					}
				}

				else if (lm instanceof VWA_Node) {
					// LOGGER.debug("Changing " + lm + " from VWA to core.");
					if (lm.getName().equals("Age"))
						LOGGER.debug("Check assignement listeners.");
					nn = new CoreObjectNode((VWA_Node) lm);
					cn.replaceWith(nn); // types not re-assigned if target lib is not editable
				} else
					nn = lm;

			}
			ml.check((Node) nn, validate);
			if (nn != null) {
				newEquCount = countEquivelents(nn);
				if (newEquCount != equCount) {
					if (!nn.getName().equals("Flight"))
						LOGGER.debug("Equ error on " + nn);
				}
			}
		}

	}

	/**
	 * @return total number of tl model equivalents in all descendants
	 */
	private int countEquivelents(LibraryMemberInterface nn) {
		int cnt = 0;
		Assert.assertNotNull(nn);
		for (Node p : ((Node) nn).getDescendants()) {
			if (p instanceof ElementNode) {
				cnt += ((TLProperty) p.getTLModelObject()).getEquivalents().size();
			}
		}
		return cnt;
	}

	protected void listTypeUsersCounts(LibraryNode ln) {
		// for (Node provider : ln.getDescendentsNamedTypeProviders())
		// LOGGER.debug(provider.getWhereAssignedCount() + "\t users of type provider: " + provider);
	}
}
