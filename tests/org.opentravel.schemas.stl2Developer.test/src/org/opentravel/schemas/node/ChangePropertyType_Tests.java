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
import org.opentravel.schemas.controllers.DefaultProjectController;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.node.Node.NodeVisitor;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.properties.AttributeNode;
import org.opentravel.schemas.node.properties.ElementNode;
import org.opentravel.schemas.node.properties.IndicatorNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.properties.PropertyNodeType;
import org.opentravel.schemas.node.properties.PropertyOwnerInterface;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.testUtils.MockLibrary;
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
public class ChangePropertyType_Tests {
	private final static Logger LOGGER = LoggerFactory.getLogger(ChangePropertyType_Tests.class);

	ModelNode model = null;
	TestNode nt = new NodeTesters().new TestNode();
	LoadFiles lf = new LoadFiles();
	LibraryTests lt = new LibraryTests();

	private MainController mc;
	private MockLibrary ml;
	private DefaultProjectController pc;
	private ProjectNode defaultProject;
	private LibraryChainNode lcn;
	private LibraryNode ln;
	private TypeProvider aType;

	@Before
	public void beforeEachTest() {
		mc = new MainController(); // New one for each test
		ml = new MockLibrary();
		pc = (DefaultProjectController) mc.getProjectController();
		defaultProject = pc.getDefaultProject();
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
		parent = (Node) vwa.getAttributeFacet();
		// property = new IndicatorNode((PropertyOwnerInterface) parent, "ind");

		// These all should fail
		Assert.assertFalse(parent.isValidParentOf(PropertyNodeType.INDICATOR_ELEMENT));
		Assert.assertFalse(parent.isValidParentOf(PropertyNodeType.ROLE));
		Assert.assertFalse(parent.isValidParentOf(PropertyNodeType.ALIAS));
		Assert.assertFalse(parent.isValidParentOf(PropertyNodeType.ENUM_LITERAL));
		Assert.assertFalse(parent.isValidParentOf(PropertyNodeType.ELEMENT));

		// These should pass
		Assert.assertTrue(parent.isValidParentOf(PropertyNodeType.ATTRIBUTE));
		Assert.assertTrue(parent.isValidParentOf(PropertyNodeType.INDICATOR));
		Assert.assertTrue(parent.isValidParentOf(PropertyNodeType.ID));

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
		TypeProvider coreSummary = (TypeProvider) ((CoreObjectNode) core).getSummaryFacet();
		BusinessObjectNode bo = ml.addBusinessObjectToLibrary_Empty(ln, "BOTest");
		PropertyOwnerInterface facet = bo.getSummaryFacet();
		PropertyNode pn = new ElementNode(facet, "P1");
		pn.setAssignedType(core);
		assertEquals("Assigned to core.", core, pn.getAssignedType());
		PropertyNode an = new AttributeNode(facet, "a1");
		an.setAssignedType(core);
		assertEquals("Assigned to core.", core, an.getAssignedType());

		// When changed to attribute the attribute gets a type and pn is removed from where used
		PropertyNode changed = pn.changePropertyRole(PropertyNodeType.ATTRIBUTE);
		assertEquals("Assigned to core.", core, changed.getAssignedType());
		assertFalse("Must not be in where used.", core.getWhereAssigned().contains(pn));
		assertTrue("Must be assigned to tl object.", core == pn.getAssignedType());

		// When changed back to element the old type assignment should be restored.
		changed.setAssignedType(aType);
		PropertyNode pn2 = changed.changePropertyRole(PropertyNodeType.ELEMENT);
		assertEquals("Must be assigned to core.", core, pn2.getAssignedType());
		assertTrue("Must be in where used.", core.getWhereAssigned().contains(pn2));
		assertFalse("Must not be in where used.", aType.getWhereAssigned().contains(changed));

		// New property whose type can not be assigned to attribute
		PropertyNode pn3 = new ElementNode(facet, "P3");
		pn3.setAssignedType(coreSummary);
		PropertyNode changed3 = pn3.changePropertyRole(PropertyNodeType.ATTRIBUTE);
		assertEquals("Facet is not assignable.", changed3.getAssignedType(), ModelNode.getUnassignedNode());
		assertFalse("Must not be in where used.", core.getWhereAssigned().contains(pn3));

		// Implied types must not be restored.
		PropertyNode pn4 = new ElementNode(facet, "P3");
		pn4.setAssignedType(core);
		PropertyNode changed4 = pn4.changePropertyRole(PropertyNodeType.INDICATOR);
		assertEquals("Assigned to required type.", changed4.getRequiredType(), changed4.getAssignedType());
		PropertyNode changed42 = changed4.changePropertyRole(PropertyNodeType.ELEMENT);
		assertTrue("Must be in where used.", core.getWhereAssigned().contains(pn4));

		pn.setAssignedType(core);
		PropertyNode changed5 = pn.changePropertyRole(PropertyNodeType.ID_REFERENCE);
		assertEquals("Assigned to required type.", core, changed5.getAssignedType());
		assertFalse("Must not be in where used.", core.getWhereAssigned().contains(pn));
	}

	@Test
	public void changePropertyRole_Tests() {
		PropertyNodeType toType = null;
		PropertyNode property, changed = null;
		PropertyOwnerInterface parent = null;

		// Indicator on VWA
		VWA_Node vwa = new VWA_Node(new TLValueWithAttributes());
		vwa.setName("Vwa");
		ln.addMember(vwa);
		parent = vwa.getAttributeFacet();
		property = new IndicatorNode(parent, "ind");

		// These all should fail and return the property
		changed = property.changePropertyRole(PropertyNodeType.INDICATOR_ELEMENT);
		changed = property.changePropertyRole(PropertyNodeType.ELEMENT);
		changed = property.changePropertyRole(PropertyNodeType.ENUM_LITERAL);
		changed = property.changePropertyRole(PropertyNodeType.ROLE);
		changed = property.changePropertyRole(PropertyNodeType.ALIAS);
		Assert.assertEquals(property, changed);

		// Converting to attribute should create new property
		changed = property.changePropertyRole(PropertyNodeType.ATTRIBUTE);
		Assert.assertNotEquals(property, changed);
		nt.visit(changed);

		// converting back should reuse the previous indicator
		property = changed;
		changed = property.changePropertyRole(PropertyNodeType.INDICATOR);
		Assert.assertNotEquals(property, changed);
		nt.visit(changed);

		// Attribute on VWA
		property = new AttributeNode(parent, "Attr1");
		property.setAssignedType(aType);
		int whereUsedCount = aType.getWhereAssignedCount();
		changed = property.changePropertyRole(PropertyNodeType.INDICATOR);
		Assert.assertNotEquals(property, changed);
		nt.visit(changed);
		Assert.assertTrue(whereUsedCount > aType.getWhereAssignedCount()); // attribute should

		// TODO - add more use cases
	}

	@Test
	public void changePropertyTypeTests() throws Exception {
		MainController mc = new MainController();

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
				switch (t) {
				case ELEMENT:
				case ID_REFERENCE:
				case ATTRIBUTE:
				case INDICATOR:
				case INDICATOR_ELEMENT:
					// LOGGER.debug("Changing " + p.getPropertyType() + " " + n.getNameWithPrefix() + " to " + t);
					p = p.changePropertyRole(t);
					nt.visit(p);
					break;
				default:
					LOGGER.debug("unknown property type " + p.getPropertyType() + " " + n.getNameWithPrefix() + " to "
							+ t);
					break;
				}
				p.getOwningComponent().visitAllNodes(nt);
			}
			// LOGGER.debug("Tested " + n + "\n");
		}
	}
}
