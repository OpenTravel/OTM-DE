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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.node.Node.NodeVisitor;
import org.opentravel.schemas.node.interfaces.FacetInterface;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.properties.AttributeNode;
import org.opentravel.schemas.node.properties.ElementNode;
import org.opentravel.schemas.node.properties.IndicatorNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.properties.PropertyNodeType;
import org.opentravel.schemas.node.properties.TypedPropertyNode;
import org.opentravel.schemas.node.typeProviders.FacetProviderNode;
import org.opentravel.schemas.node.typeProviders.VWA_Node;
import org.opentravel.schemas.node.typeProviders.facetOwners.BusinessObjectNode;
import org.opentravel.schemas.node.typeProviders.facetOwners.CoreObjectNode;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.testUtils.BaseTest;
import org.opentravel.schemas.testUtils.NodeTesters;
import org.opentravel.schemas.testUtils.NodeTesters.TestNode;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dave Hollander
 * 
 */
public class ChangePropertyType_Tests extends BaseTest {
	private final static Logger LOGGER = LoggerFactory.getLogger(ChangePropertyType_Tests.class);

	TestNode nt = new NodeTesters().new TestNode();
	private LibraryChainNode lcn;
	private TypeProvider aType;

	@Before
	public void beforeEachOfTheseTests() {
		lcn = ml.createNewManagedLibrary("test", defaultProject);
		ln = lcn.getHead();
		aType = (TypeProvider) NodeFinders.findNodeByName("date", ModelNode.XSD_NAMESPACE);

		Assert.assertNotNull(ln);
		Assert.assertNotNull(aType);
		Assert.assertTrue(ln.isEditable());
		Assert.assertNotNull(lcn);
	}

	@Test
	public void isValidParentOf_Tests() {
		// PropertyNode property = null;
		Node parent = null;

		// Indicator on VWA
		VWA_Node vwa = new VWA_Node(new TLValueWithAttributes());
		parent = vwa.getFacet_Attributes();
		// property = new IndicatorNode((PropertyOwnerInterface) parent, "ind");

		// // These all should fail
		// Assert.assertFalse(parent.canOwn(PropertyNodeType.INDICATOR_ELEMENT));
		// Assert.assertFalse(parent.canOwn(PropertyNodeType.ROLE));
		// Assert.assertFalse(parent.canOwn(PropertyNodeType.ALIAS));
		// Assert.assertFalse(parent.canOwn(PropertyNodeType.ENUM_LITERAL));
		// Assert.assertFalse(parent.canOwn(PropertyNodeType.ELEMENT));
		//
		// // These should pass
		// Assert.assertTrue(parent.canOwn(PropertyNodeType.ATTRIBUTE));
		// Assert.assertTrue(parent.canOwn(PropertyNodeType.INDICATOR));
		// Assert.assertTrue(parent.canOwn(PropertyNodeType.ID));

		// CHECK THIS - should this be false?
		// Assert.assertTrue(parent.isValidParentOf(PropertyNodeType.ID_REFERENCE));

		// TODO - add more test cases
	}

	/**
	 * Testing type assignments on changed properties.
	 */
	@Test
	public void changePropertyRoleTypeAssignment_Tests() {
		TypeProvider core = ml.addCoreObjectToLibrary(ln, "Core1");
		TypeProvider coreSummary = ((CoreObjectNode) core).getFacet_Summary();
		BusinessObjectNode bo = ml.addBusinessObjectToLibrary_Empty(ln, "BOTest");
		FacetProviderNode facet = bo.getFacet_Summary();
		TypedPropertyNode pn = new ElementNode(facet, "P1");
		pn.setAssignedType(core);
		assertEquals("Assigned to core.", core, pn.getAssignedType());
		TypedPropertyNode an = new AttributeNode(facet, "a1");
		an.setAssignedType(core);
		assertEquals("Assigned to core.", core, an.getAssignedType());

		// When changed to attribute the attribute gets a type and pn is removed from where used
		TypedPropertyNode changed = (TypedPropertyNode) pn.changePropertyRole(PropertyNodeType.ATTRIBUTE);
		assertTrue("Must be an attribute.", changed instanceof AttributeNode);
		assertEquals("Assigned to core.", core, changed.getAssignedType());
		assertTrue("Must be in where used.", core.getWhereAssigned().contains(changed));
		assertTrue("Must not be in where used.", !core.getWhereAssigned().contains(pn));
		assertTrue("Must be assigned to tl object.", core == pn.getAssignedType());

		// When changed back to element the old type assignment should be restored.
		changed.setAssignedType(aType);
		assertTrue("Must have parent.", changed.getParent() == facet);
		assertTrue("Must  be in where used.", aType.getWhereAssigned().contains(changed));

		TypedPropertyNode pn2 = (TypedPropertyNode) changed.changePropertyRole(PropertyNodeType.ELEMENT);
		assertEquals("Must be assigned to core.", core, pn2.getAssignedType());
		assertTrue("Must be in where used.", core.getWhereAssigned().contains(pn2));
		assertTrue("Must not be in where used.", !aType.getWhereAssigned().contains(pn2));
		// Make sure changed PN is not used
		assertTrue("Must not be in where used.", !aType.getWhereAssigned().contains(changed));
		assertTrue("Must not have parent.", changed.getParent() != facet);

		// New property whose type can not be assigned to attribute
		TypedPropertyNode pn3 = new ElementNode(facet, "P3");
		pn3.setAssignedType(coreSummary);
		TypedPropertyNode changed3 = (TypedPropertyNode) pn3.changePropertyRole(PropertyNodeType.ATTRIBUTE);
		assertEquals("Facet is not assignable.", changed3.getAssignedType(), ModelNode.getUnassignedNode());
		assertFalse("Must not be in where used.", core.getWhereAssigned().contains(pn3));

		// Implied types must not be restored.
		TypedPropertyNode pn4 = new ElementNode(facet, "P3");
		pn4.setAssignedType(core);
		PropertyNode changed4 = pn4.changePropertyRole(PropertyNodeType.INDICATOR);
		// assertEquals("Assigned to required type.", changed4.getRequiredType(), changed4.getAssignedType());
		TypedPropertyNode changed42 = (TypedPropertyNode) changed4.changePropertyRole(PropertyNodeType.ELEMENT);
		assertTrue("Must be in where used.", core.getWhereAssigned().contains(pn4));

		pn.setAssignedType(core);
		TypedPropertyNode changed5 = (TypedPropertyNode) pn.changePropertyRole(PropertyNodeType.ID_REFERENCE);
		assertEquals("Assigned to required type.", core, changed5.getAssignedType());
		assertFalse("Must not be in where used.", core.getWhereAssigned().contains(pn));
	}

	@Test
	public void changePropertyRole_Tests() {
		PropertyNodeType toType = null;
		PropertyNode property, changed = null;
		FacetInterface parent = null;

		// Indicator on VWA
		VWA_Node vwa = new VWA_Node(new TLValueWithAttributes());
		vwa.setName("Vwa");
		ln.addMember(vwa);
		parent = vwa.getFacet_Attributes();
		property = new IndicatorNode(parent, "ind");
		assert property.getPropertyType().equals(PropertyNodeType.INDICATOR);

		// Converting to attribute must create new property
		PropertyNode changedAttr = property.changePropertyRole(PropertyNodeType.ATTRIBUTE);
		Assert.assertNotEquals(property, changedAttr);
		ml.check(changedAttr, false); // not valid because no type is assigned

		// These all should fail and and change property to an attribute
		changed = changedAttr.changePropertyRole(PropertyNodeType.INDICATOR_ELEMENT);
		Assert.assertEquals(changedAttr, changed);
		changed = changed.changePropertyRole(PropertyNodeType.ELEMENT);
		Assert.assertEquals(changedAttr, changed);
		changed = changed.changePropertyRole(PropertyNodeType.ENUM_LITERAL);
		Assert.assertEquals(changedAttr, changed);
		changed = changed.changePropertyRole(PropertyNodeType.ROLE);
		Assert.assertEquals(changedAttr, changed);
		changed = changed.changePropertyRole(PropertyNodeType.ALIAS);
		Assert.assertEquals(changedAttr, changed);
		assert property.getPropertyType().equals(PropertyNodeType.INDICATOR);

		// converting back should reuse the previous indicator
		property = changed;
		changed = property.changePropertyRole(PropertyNodeType.INDICATOR);
		Assert.assertNotEquals(property, changed);
		ml.check(changed, false);

		// Attribute on VWA
		TypedPropertyNode tpn = new AttributeNode(parent, "Attr1");
		tpn.setAssignedType(aType);
		int whereUsedCount = aType.getWhereAssignedCount();
		changed = tpn.changePropertyRole(PropertyNodeType.INDICATOR);
		Assert.assertNotEquals(tpn, changed);
		ml.check(changed, false);
		Assert.assertTrue(whereUsedCount > aType.getWhereAssignedCount()); // attribute should

		// TODO - add more use cases
	}

	@Test
	public void changePropertyTypeTests() throws Exception {
		MainController mc = OtmRegistry.getMainController();

		lf.loadTestGroupA(mc);
		for (LibraryNode ln : Node.getAllLibraries()) {
			ln.visitAllNodes(nt);
			visitAllProperties(ln);
		}
		NodeModelTestUtils.testNodeModel();
	}

	public void visitAllProperties(LibraryNode ln) {
		VisitProperty visitor = new VisitProperty();
		for (TypeUser n : ln.getDescendants_TypeUsers()) {
			if (n instanceof PropertyNode)
				visitor.visit((INode) n);
		}
	}

	/**
	 * Change role of the property then test it.
	 */
	class VisitProperty implements NodeVisitor {

		@Override
		public void visit(INode n) {
			if (!(n instanceof PropertyNode))
				return;
			PropertyNode p = (PropertyNode) n;
			switch (p.getPropertyType()) {
			case ROLE:
			case ENUM_LITERAL:
			case SIMPLE:
			case ALIAS:
				return;
			}

			for (PropertyNodeType t : PropertyNodeType.values()) {
				assert p.getParent() instanceof FacetInterface;
				if (p.getName().equals("agentDuty"))
					LOGGER.debug("Agent duty caused errors.");
				switch (t) {
				case ELEMENT:
				case ID_REFERENCE:
				case ATTRIBUTE:
				case INDICATOR:
				case INDICATOR_ELEMENT:
					LOGGER.debug("Changing " + p.getPropertyType() + " " + n.getNameWithPrefix() + " to " + t);
					p = p.changePropertyRole(t);
					nt.visit(p);
					break;
				case ALIAS:
				case ENUM_LITERAL:
				case ID:
				case ID_ATTR_REF:
				case ROLE:
				case SIMPLE:
				case UNKNOWN:
				default:
					LOGGER.debug("UnTested changePropertyRole type " + t);
					break;
				}
				p.getOwningComponent().visitAllNodes(nt);
			}
			// LOGGER.debug("Tested " + n + "\n");
		}
	}
}
