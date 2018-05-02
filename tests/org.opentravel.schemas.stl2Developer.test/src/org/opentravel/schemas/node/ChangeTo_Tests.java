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
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemas.controllers.DefaultProjectController;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.node.interfaces.FacetOwner;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.properties.ElementNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.typeProviders.VWA_Node;
import org.opentravel.schemas.node.typeProviders.facetOwners.BusinessObjectNode;
import org.opentravel.schemas.node.typeProviders.facetOwners.CoreObjectNode;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.testUtils.MockLibrary;
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
public class ChangeTo_Tests {
	private final static Logger LOGGER = LoggerFactory.getLogger(ChangeTo_Tests.class);

	ModelNode model = null;
	TestNode tn = new NodeTesters().new TestNode();
	MockLibrary ml = null;
	LibraryNode ln = null;
	LibraryChainNode lcn = null;
	MainController mc;
	DefaultProjectController pc;
	ProjectNode defaultProject;

	@Before
	public void beforeEachTest() {
		// mc = OtmRegistry.getMainController(); // creates one if needed
		mc = OtmRegistry.getMainController(); // New one for each test
		ml = new MockLibrary();
		pc = (DefaultProjectController) mc.getProjectController();
		defaultProject = pc.getDefaultProject();
		lcn = ml.createNewManagedLibrary("test", defaultProject);
		Assert.assertNotNull(lcn);
		ln = lcn.getHead();
		Assert.assertNotNull(ln);
		Assert.assertTrue(ln.isEditable());
	}

	@Test
	public void changeToVWA1() {
		LibraryNode ln = ml.createNewLibrary(defaultProject.getNSRoot(), "test", defaultProject);
		BusinessObjectNode bo = ml.addBusinessObjectToLibrary(ln, "bo");
		CoreObjectNode core = ml.addCoreObjectToLibrary(ln, "co");
		VWA_Node tVwa = null, vwa = ml.addVWA_ToLibrary(ln, "vwa");

		int typeCount = ln.getDescendants_LibraryMembers().size();
		// When converted to VWA, the facets are lost.
		typeCount -= bo.getDescendants_ContributedFacets().size();

		VWA_Tests tests = new VWA_Tests();
		tVwa = (VWA_Node) bo.changeObject(SubType.VALUE_WITH_ATTRS);
		tests.check(tVwa);
		tVwa = (VWA_Node) core.changeObject(SubType.VALUE_WITH_ATTRS);
		tests.check(tVwa);
		tVwa = (VWA_Node) vwa.changeObject(SubType.VALUE_WITH_ATTRS);
		tests.check(tVwa);

		// tn.visit(ln);
		Assert.assertEquals(typeCount, ln.getDescendants_LibraryMembers().size());
	}

	@Test
	public void changeToVWA2() {

		// Given: business and core objects in a managed library used as types.
		BusinessObjectNode bo = ml.addBusinessObjectToLibrary(ln, "A");
		CoreObjectNode core = ml.addCoreObjectToLibrary(ln, "B");
		CoreObjectNode user = ml.addCoreObjectToLibrary(ln, "User");
		PropertyNode p1 = new ElementNode(user.getFacet_Summary(), "P1", bo);
		PropertyNode p2 = new ElementNode(user.getFacet_Summary(), "P2", core);
		PropertyNode p3 = new ElementNode(user.getFacet_Summary(), "P3", core.getFacet_Detail());

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
		ml.check(vwa);
		assertEquals("Replace assignments on non-referene users.", boWhereAssignedCount - 1,
				vwa.getWhereUsedAndDescendantsCount());
		assertEquals(vwa, p1.getAssignedType());
		ml.check(vwa);

		//
		// When - a new VWA is created from Core
		vwa = new VWA_Node(core);
		assertEquals("B", vwa.getName());
		assertEquals(core.getAssignedType(), vwa.getAssignedType());
		assertEquals("Must have attribute for each Core property.", coreCount,
				vwa.getFacet_Attributes().getChildren().size());
		// assertEquals("TL properties must match property nodes.", vwa.getFacet_Attributes().getModelObject()
		// .getChildren().size(), vwa.getFacet_Attributes().getChildren().size());

		// When - VWA replaces core.
		core.replaceWith(vwa);
		tn.visit(vwa);
		// Then
		ml.check(vwa);
		assertEquals(coreWhereAssignedCount, vwa.getWhereUsedAndDescendantsCount());
		assertEquals(vwa, p2.getAssignedType());
		assertEquals(vwa, p3.getAssignedType());
		assertTrue("VWA must be in the library after swap.", ln.getDescendants_LibraryMembersAsNodes().contains(vwa));
		assertTrue("Core must NOT be in the library after swap.",
				!ln.getDescendants_LibraryMembersAsNodes().contains(core));
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
		assertTrue("Core must be in the library after swap.", ln.getDescendants_LibraryMembersAsNodes().contains(core));
		assertTrue("BO must NOT be in the library after swap.",
				!ln.getDescendants_LibraryMembersAsNodes().contains(bo));

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
		assertTrue("Core must be in the library after swap.", ln.getDescendants_LibraryMembersAsNodes().contains(core));
		assertTrue("VWA must NOT be in the library after swap.",
				!ln.getDescendants_LibraryMembersAsNodes().contains(vwa));
	}

	@Test
	public void changeCheckUsersCounts() {
		CoreObjectNode core = ml.addCoreObjectToLibrary(ln, "C");
		PropertyNode p1 = new ElementNode(core.getFacet_Summary(), "P1");
		VWA_Node vwa = ml.addVWA_ToLibrary(ln, "B");
		tn.visit(ln);
		tn.visit(lcn);

		// Make assignment and assure counts are correct.
		Collection<TypeUser> list = null;
		p1.setAssignedType(vwa);
		Assert.assertTrue("P1 must be assigned VWA as type.", p1.getAssignedType() == vwa);
		list = vwa.getWhereAssigned();
		Assert.assertEquals(1, vwa.getWhereAssigned().size());

		ComponentNode nc = vwa.changeObject(SubType.CORE_OBJECT);
		Assert.assertTrue("P1 must now be assigned the new core object.", p1.getAssignedType() == nc);
		list = ((TypeProvider) nc).getWhereAssigned();
		Assert.assertTrue("New core must have P1 in its where assigned list.", list.contains(p1));
		Assert.assertEquals(1, ((TypeProvider) nc).getWhereAssigned().size());
	}

	@Test
	public void changeAsInMainController() {
		VWA_Node nodeToReplace = ml.addVWA_ToLibrary(ln, "B");
		CoreObjectNode core = ml.addCoreObjectToLibrary(ln, "C");
		PropertyNode p1 = new ElementNode(core.getFacet_Summary(), "P1");
		p1.setAssignedType(nodeToReplace);

		// NodeToReplace is input param
		Assert.assertEquals(1, nodeToReplace.getWhereAssigned().size());
		LOGGER.debug("Changing selected component: " + nodeToReplace.getName() + " with "
				+ nodeToReplace.getWhereAssignedCount() + " users.");

		// WHAT THE HECK IS THIS? Why is there only one object?
		ComponentNode editedNode = nodeToReplace;
		// nodeToReplace.replaceWith(editedComponent);

		// code used in ChangeWizardPage
		editedNode = editedNode.changeObject(SubType.CORE_OBJECT);
		Assert.assertEquals(0, nodeToReplace.getWhereAssigned().size());
		// deleted in main controller
		if (editedNode != nodeToReplace)
			nodeToReplace.delete();

		Assert.assertEquals(1, ((TypeProvider) editedNode).getWhereAssigned().size());
		Assert.assertEquals(editedNode, p1.getAssignedType());
		// 1/22/15 - the counts are wrong!

	}

	@Test
	public void changeToBO() {
		CoreObjectNode core = ml.addCoreObjectToLibrary(ln, "A");
		VWA_Node vwa = ml.addVWA_ToLibrary(ln, "B");
		Assert.assertNotNull(core);
		Assert.assertNotNull(vwa);

		BusinessObjectNode bo = null;
		TLBusinessObject tlBO = null;

		bo = new BusinessObjectNode(core);
		tlBO = bo.getTLModelObject();
		Assert.assertEquals("A", bo.getName());
		Assert.assertEquals(1, bo.getFacet_Summary().getChildren().size());
		Assert.assertEquals(tlBO.getSummaryFacet().getElements().size(), bo.getFacet_Summary().getChildren().size());

		bo = new BusinessObjectNode(vwa);
		tlBO = bo.getTLModelObject();
		Assert.assertEquals("B", bo.getName());
		Assert.assertEquals(1, bo.getFacet_Summary().getChildren().size());
		Assert.assertEquals(tlBO.getSummaryFacet().getAttributes().size(), bo.getFacet_Summary().getChildren().size());
	}

	@Test
	public void changeTestGroupA_Tests() throws Exception {
		MainController mc = OtmRegistry.getMainController();
		LoadFiles lf = new LoadFiles();
		model = mc.getModelNode();
		LibraryChainNode lcn;

		lf.loadTestGroupA(mc);
		for (LibraryNode ln : model.getUserLibraries()) {
			if (!ln.isInChain())
				lcn = new LibraryChainNode(ln);
			else
				lcn = ln.getChain();

			changeMembers(ln);

			ln.visitAllNodes(tn);
		}
	}

	/**
	 * Change all library members in the passed library.
	 * 
	 * @param ln
	 */
	private void changeMembers(LibraryNode ln) {

		LibraryMemberInterface nn = null;
		int equCount = 0, newEquCount = 0;
		PropertyNode aProperty = null;
		PropertyNode newProperty = null;
		ln.setEditable(true);

		// Get all library members and change them.
		for (LibraryMemberInterface lm : ln.getDescendants_LibraryMembers()) {
			equCount = countEquivelents(lm);

			if (lm instanceof ComponentNode && !lm.isDeleted()) {
				ComponentNode cn = (ComponentNode) lm;
				if (lm instanceof BusinessObjectNode) {
					// LOGGER.debug("When - Changing " + lm + " from business object to core.");
					nn = new CoreObjectNode((BusinessObjectNode) lm);
					cn.replaceWith(nn);
					cn.delete();
					// Then
					assertEquals(equCount, countEquivelents(nn));

				} else if (lm instanceof CoreObjectNode) {
					// LOGGER.debug("When - Changing " + lm + " from core to business object.");

					// Pick last summary property for testing.
					aProperty = null;
					if (((FacetOwner) lm).getFacet_Summary().getChildren_TypeUsers().size() > 0)
						aProperty = (PropertyNode) ((FacetOwner) lm).getFacet_Summary().getChildren_TypeUsers()
								.get(((FacetOwner) lm).getFacet_Summary().getChildren_TypeUsers().size() - 1);

					// Do not test assignment if the core is used as a type
					if (cn == aProperty.getAssignedType())
						aProperty = null;
					// If the type of is in this core then do not test it.
					if (aProperty != null && cn.contains((Node) aProperty.getAssignedType()))
						aProperty = null;

					nn = new BusinessObjectNode((CoreObjectNode) lm);
					cn.replaceWith(nn);

					ml.check((Node) nn, false);

					// Find the property with the same name for testing.
					if (aProperty != null)
						newProperty = (PropertyNode) ((BusinessObjectNode) nn).getFacet_Summary()
								.findChildByName(aProperty.getName());
					if (aProperty != null && newProperty != null) {
						TypeProvider npAT = newProperty.getAssignedType();
						TypeProvider apAT = aProperty.getAssignedType();
						if (npAT != apAT)
							LOGGER.debug("Assigned types do not match.");
						assertTrue(newProperty.getAssignedType() == aProperty.getAssignedType());
					}
					cn.delete();
					tn.visit(nn);
				}

				else if (lm instanceof VWA_Node) {
					// LOGGER.debug("Changing " + lm + " from VWA to core.");
					nn = new CoreObjectNode((VWA_Node) lm);
					cn.replaceWith(nn);
					cn.delete();
				} else
					nn = lm;

			}
			ml.check((Node) nn, false);
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
