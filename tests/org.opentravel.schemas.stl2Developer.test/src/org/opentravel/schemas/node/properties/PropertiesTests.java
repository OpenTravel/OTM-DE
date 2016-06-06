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
package org.opentravel.schemas.node.properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLEnumValue;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLOpenEnumeration;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLRole;
import org.opentravel.schemas.controllers.DefaultProjectController;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.node.BusinessObjectNode;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.CoreObjectNode;
import org.opentravel.schemas.node.EnumerationClosedNode;
import org.opentravel.schemas.node.EnumerationOpenNode;
import org.opentravel.schemas.node.FacetNode;
import org.opentravel.schemas.node.LibraryChainNode;
import org.opentravel.schemas.node.LibraryNode;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFinders;
import org.opentravel.schemas.node.NodeNameUtils;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.node.PropertyNodeType;
import org.opentravel.schemas.node.RoleFacetNode;
import org.opentravel.schemas.node.VWA_Node;
import org.opentravel.schemas.testUtils.MockLibrary;
import org.opentravel.schemas.types.TypeProvider;

/**
 * @author Dave Hollander
 * 
 */
public class PropertiesTests {
	ModelNode model = null;
	MockLibrary mockLibrary = null;
	LibraryNode ln = null;
	MainController mc;
	DefaultProjectController pc;
	ProjectNode defaultProject;

	// Node_Tests nt = new Node_Tests();
	// LoadFiles lf = new LoadFiles();
	// LibraryTests lt = new LibraryTests();

	@Before
	public void beforeEachTest() {
		mc = new MainController();
		mockLibrary = new MockLibrary();
		pc = (DefaultProjectController) mc.getProjectController();
		defaultProject = pc.getDefaultProject();
		ln = mockLibrary.createNewLibrary("http://example.com/test", "test", defaultProject);
	}

	@Test
	public void equivalents() {
		LibraryChainNode lcn = mockLibrary.createNewManagedLibrary("EQ_Test", pc.getDefaultProject());
		ln = lcn.getHead();
		BusinessObjectNode bo = mockLibrary.addBusinessObjectToLibrary(ln, "EQBO");
		PropertyNode p = (PropertyNode) bo.getSummaryFacet().getChildren().get(0);
		createEquivalents(p);
	}

	/**
	 * Adds an equivalent to the property node and tests it.
	 */
	public void createEquivalents(PropertyNode p) {
		Assert.assertNotNull(p);
		p.setEquivalent("V1"); // creates handler
		IValueWithContextHandler eqh = p.getEquivalentHandler();
		Assert.assertNotNull(eqh);
		Assert.assertNotNull(p.getLibrary().getTLLibrary());
		testValueWithContextHandler(eqh);
	}

	@Test
	public void examples() {
		LibraryChainNode lcn = mockLibrary.createNewManagedLibrary("EQ_Test", pc.getDefaultProject());
		ln = lcn.getHead();
		BusinessObjectNode bo = mockLibrary.addBusinessObjectToLibrary(ln, "EQBO");
		PropertyNode p = (PropertyNode) bo.getSummaryFacet().getChildren().get(0);
		Assert.assertNotNull(p);
		p.setExample("V1"); // creates handler
		IValueWithContextHandler exh = p.getExampleHandler();
		Assert.assertNotNull(exh);
		Assert.assertNotNull(p.getLibrary().getTLLibrary());
		testValueWithContextHandler(exh);
	}

	private void testValueWithContextHandler(IValueWithContextHandler handler) {
		handler.set("V1", null); // Uses default context
		Assert.assertEquals(1, handler.getCount());
		String defaultAppContext = handler.getApplicationContext();
		String defaultContextId = handler.getContextID();
		Assert.assertFalse(defaultAppContext.isEmpty());
		Assert.assertFalse(defaultContextId.isEmpty());

		// Add context to library and context manager -
		// must use context controller because it is needed in the valid context tests.
		mc.getContextController().newContext(ln, "C1", "CA1");
		mc.getContextController().newContext(ln, "C2", "CA2");

		// Create 2 values
		handler.set("V2", "C1"); // removes other values
		handler.set("V3", "C2"); // removes other values
		Assert.assertEquals(1, handler.getCount());

		Assert.assertEquals("", handler.get("C1"));
		Assert.assertEquals("V3", handler.get("C2"));

		handler.set("V4", "C2"); // removes other values
		handler.fix("C2"); // should do nothing
		Assert.assertEquals(1, handler.getCount());
		Assert.assertEquals("V4", handler.get("C2"));
		Assert.assertTrue(handler.getApplicationContext().equals("CA2"));

		// Test fix to move value V4 to default context.
		handler.fix(null);
		Assert.assertEquals(1, handler.getCount());
		Assert.assertEquals("V4", handler.get(null));
		Assert.assertTrue(handler.getApplicationContext().equals(defaultAppContext));

		handler.set(null, "C1"); // remove value
		Assert.assertEquals(0, handler.getCount());

		// Test setting and getting with no context.
		handler.set("V5", null); // removes other values
		Assert.assertTrue(handler.get(null).equals("V5"));
	}

	@Test
	public void createElements() {
		BusinessObjectNode bo = mockLibrary.addBusinessObjectToLibrary(ln, "TestBO");
		FacetNode summary = bo.getSummaryFacet();
		Assert.assertNotNull(summary);
		Node aType = NodeFinders.findNodeByName("date", ModelNode.XSD_NAMESPACE);
		PropertyNode pn = null;

		pn = new ElementNode(summary, "A");
		Assert.assertNotNull(pn);
		Assert.assertNotNull(pn.getLibrary());
		Assert.assertEquals(pn.getName(), "A");

		pn = new ElementNode(new TLProperty(), summary);
		Assert.assertNotNull(pn);
		pn.setName("b");
		Assert.assertNotNull(pn.getLibrary());
		Assert.assertTrue(pn instanceof ElementNode);
		Assert.assertEquals(pn.getName(), "B");
		Assert.assertFalse(pn.getLabel().isEmpty());
		pn.setName("AAA");
		Assert.assertEquals(pn.getName(), "AAA");
		Assert.assertNotNull(pn.getLibrary());

		pn = (PropertyNode) pn.createProperty(aType);
		Assert.assertNotNull(pn);

		Assert.assertEquals(4, summary.getChildren().size()); // addBO creates one
	}

	@Test
	public void createElementRefs() {
		BusinessObjectNode bo = mockLibrary.addBusinessObjectToLibrary(ln, "TestBO");
		BusinessObjectNode A = mockLibrary.addBusinessObjectToLibrary(ln, "A");
		PropertyOwnerInterface summary = bo.getSummaryFacet();
		Assert.assertNotNull(summary);
		Node aType = NodeFinders.findNodeByName("date", ModelNode.XSD_NAMESPACE);
		PropertyNode pn = null;

		pn = new ElementReferenceNode(summary, "ThisIsIgnored");
		Assert.assertNotNull(pn);
		Assert.assertNotNull(pn.getLibrary());
		pn.setAssignedType(A);
		Assert.assertEquals("ARef", pn.getName());

		pn = new ElementReferenceNode(new TLProperty(), summary);
		Assert.assertNotNull(pn);
		pn.setName("b");
		Assert.assertNotNull(pn.getLibrary());
		Assert.assertEquals(NodeNameUtils.fixElementRefName("B"), pn.getName());
		Assert.assertFalse(pn.getLabel().isEmpty());
		Assert.assertNotNull(pn.getLibrary());

		pn = (PropertyNode) pn.createProperty(aType);
		Assert.assertNotNull(pn);

		Assert.assertEquals(4, summary.getChildren().size()); // addBO creates one
	}

	@Test
	public void createAttributes() {
		BusinessObjectNode bo = mockLibrary.addBusinessObjectToLibrary(ln, "TestBO");
		FacetNode summary = bo.getSummaryFacet();
		Assert.assertNotNull(summary);
		Node aType = NodeFinders.findNodeByName("date", ModelNode.XSD_NAMESPACE);
		PropertyNode pn, pn1 = null;

		pn1 = new AttributeNode(summary, "A");
		Assert.assertNotNull(pn1);
		Assert.assertNotNull(pn1.getLibrary());
		Assert.assertEquals("a", pn1.getName());

		pn = new AttributeNode(new TLAttribute(), summary);
		Assert.assertNotNull(pn);
		pn.setName("b");
		Assert.assertNotNull(pn.getLibrary());
		Assert.assertEquals(NodeNameUtils.fixAttributeName("B"), pn.getName());
		Assert.assertFalse(pn.getLabel().isEmpty());
		Assert.assertNotNull(pn.getLibrary());

		pn = (PropertyNode) pn1.createProperty(aType);
		Assert.assertNotNull(pn);

		// TODO - test descriptions ~!!!
		// TODO - test examples ~!!!
		// TODO - test equivalents ~!!!

		Assert.assertEquals(4, summary.getChildren().size()); // addBO creates one
	}

	@Test
	public void createIds() {
		VWA_Node vwa = mockLibrary.addVWA_ToLibrary(ln, "Vwa");
		IdNode id = new IdNode(vwa.getAttributeFacet(), "SomeIgnoredName");
		Node idType = NodeFinders.findNodeByName("ID", ModelNode.XSD_NAMESPACE);

		Assert.assertEquals("id", id.getName());
		Assert.assertEquals(idType, id.getAssignedType());
		Assert.assertEquals(id.getPropertyType(), PropertyNodeType.ID);
		Assert.assertEquals(id.alternateRoles.currentType, PropertyNodeType.ID);
		Assert.assertNotNull(id.alternateRoles.oldIdN);
	}

	@Test
	public void createIndicatorElements() {
		BusinessObjectNode bo = mockLibrary.addBusinessObjectToLibrary(ln, "TestBO");
		FacetNode summary = bo.getSummaryFacet();
		Assert.assertNotNull(summary);
		Node aType = NodeFinders.findNodeByName("date", ModelNode.XSD_NAMESPACE);
		PropertyNode pn, pn1 = null;

		pn1 = new IndicatorElementNode(summary, "A");
		Assert.assertNotNull(pn1);
		Assert.assertNotNull(pn1.getLibrary());
		Assert.assertEquals("AInd", pn1.getName());

		pn = new IndicatorElementNode(new TLIndicator(), summary);
		Assert.assertNotNull(pn);
		pn.setName("b");
		Assert.assertNotNull(pn.getLibrary());
		Assert.assertEquals(NodeNameUtils.fixIndicatorElementName("B"), pn.getName());
		Assert.assertNotNull(pn.getRequiredType());
		Assert.assertFalse(pn.getLabel().isEmpty());
		Assert.assertNotNull(pn.getLibrary());

		pn = (PropertyNode) pn1.createProperty(aType);
		Assert.assertNotNull(pn);

		// TODO - test descriptions ~!!!
		// TODO - test examples ~!!!
		// TODO - test equivalents ~!!!

		Assert.assertEquals(4, summary.getChildren().size()); // addBO creates one
	}

	@Test
	public void createIndicator() {
		BusinessObjectNode bo = mockLibrary.addBusinessObjectToLibrary(ln, "TestBO");
		FacetNode summary = bo.getSummaryFacet();
		Assert.assertNotNull(summary);
		Node aType = NodeFinders.findNodeByName("date", ModelNode.XSD_NAMESPACE);
		PropertyNode pn, pn1 = null;

		pn1 = new IndicatorNode(summary, "A");
		Assert.assertNotNull(pn1);
		Assert.assertNotNull(pn1.getLibrary());
		Assert.assertEquals("aInd", pn1.getName());

		pn = new IndicatorElementNode(new TLIndicator(), summary);
		Assert.assertNotNull(pn);
		pn.setName("b");
		Assert.assertNotNull(pn.getLibrary());
		Assert.assertEquals(NodeNameUtils.fixIndicatorElementName("b"), pn.getName());
		Assert.assertNotNull(pn.getRequiredType());
		Assert.assertFalse(pn.getLabel().isEmpty());
		Assert.assertNotNull(pn.getLibrary());

		pn = (PropertyNode) pn1.createProperty(aType);
		Assert.assertNotNull(pn);

		// TODO - test descriptions ~!!!
		// TODO - test examples ~!!!
		// TODO - test equivalents ~!!!

		Assert.assertEquals(4, summary.getChildren().size()); // addBO creates one
	}

	@Test
	public void createEnumLiterals() {
		assertTrue(ln.isEditable_newToChain());
		EnumerationOpenNode open = new EnumerationOpenNode(new TLOpenEnumeration());
		EnumerationClosedNode closed = new EnumerationClosedNode(new TLClosedEnumeration());
		open.setName("O");
		ln.addMember(open);
		closed.setName("C");
		ln.addMember(closed);
		EnumLiteralNode lit, litA;
		Node aType = NodeFinders.findNodeByName("date", ModelNode.XSD_NAMESPACE);

		// TODO - use open.addLiteral() instead
		litA = new EnumLiteralNode(open, "A");
		Assert.assertNotNull("litA");
		lit = new EnumLiteralNode(new TLEnumValue(), open);
		lit.setName("B");
		Assert.assertNotNull("litA");

		lit = (EnumLiteralNode) litA.createProperty(aType);
		Assert.assertEquals(NodeNameUtils.fixEnumerationValue(aType.getName()), lit.getName());
		Assert.assertEquals(3, open.getChildren().size());

	}

	@Test
	public void createRoles() {
		CoreObjectNode core = mockLibrary.addCoreObjectToLibrary(ln, "Core");
		Node aType = NodeFinders.findNodeByName("date", ModelNode.XSD_NAMESPACE);
		RoleFacetNode roles = core.getRoleFacet();
		RoleNode rn1, rn = null;

		rn1 = new RoleNode(roles, "A");
		Assert.assertNotNull(rn1);
		Assert.assertEquals("A", rn1.getName());

		rn = new RoleNode(new TLRole(), roles);
		rn.setName("B");
		Assert.assertNotNull(rn);
		Assert.assertEquals("B", rn.getName());

		rn = (RoleNode) rn1.createProperty(aType);
		Assert.assertNotNull(rn);
		Assert.assertEquals("date", rn.getName());

		Assert.assertEquals(3, roles.getChildren().size());
	}

	@Test
	public void changeRoles() {
		ln.setEditable(true);
		BusinessObjectNode bo = mockLibrary.addBusinessObjectToLibrary(ln, "ct");
		PropertyOwnerInterface summary = bo.getSummaryFacet();
		Node aType = NodeFinders.findNodeByName("date", ModelNode.XSD_NAMESPACE);
		PropertyNode pn, epn, apn, ipn, rpn, iepn = null;
		String eText = "Element";
		String aText = "attribute";
		String iText = "xInd";
		String rText = "ctRef";
		String ieText = "XInd";

		epn = new ElementNode(summary, eText);
		epn.setAssignedType((TypeProvider) aType);
		apn = new AttributeNode(summary, aText);
		apn.setAssignedType((TypeProvider) NodeFinders.findNodeByName("id", ModelNode.XSD_NAMESPACE));
		ipn = new IndicatorNode(summary, iText);
		iepn = new IndicatorElementNode(summary, ieText);
		rpn = new ElementReferenceNode(summary, rText);
		boolean x = rpn.setAssignedType(bo);
		Assert.assertEquals(NodeNameUtils.fixElementName(eText), epn.getName());
		Assert.assertEquals(NodeNameUtils.fixAttributeName(aText), apn.getName());
		Assert.assertEquals(NodeNameUtils.fixIndicatorName(iText), ipn.getName());
		Assert.assertEquals(NodeNameUtils.fixIndicatorElementName(ieText), iepn.getName());
		Assert.assertEquals(NodeNameUtils.fixElementRefName(rText), rpn.getName());

		epn.setDescription(eText);
		Assert.assertEquals(eText, epn.getDescription());

		apn.copyDetails(epn);
		Assert.assertEquals(eText, apn.getDescription());

		List<Node> kids = new ArrayList<Node>(summary.getChildren());
		for (Node n : kids) {
			if (n instanceof PropertyNode)
				changeToAll((PropertyNode) n);
		}
		// Do it again to assure the alternateRoles logic works
		kids = new ArrayList<Node>(summary.getChildren());
		for (Node n : kids) {
			if (n instanceof PropertyNode)
				changeToAll((PropertyNode) n);
		}
	}

	@Test
	public void addPropertyFromDND_Tests() {
		// This seems to be dependent on the type of "this" node. create property should only be implemented for facets.
		ln.setEditable(true);
		BusinessObjectNode bo = mockLibrary.addBusinessObjectToLibrary(ln, "ct");
		PropertyOwnerInterface summary = bo.getSummaryFacet();
		Node aType = NodeFinders.findNodeByName("date", ModelNode.XSD_NAMESPACE);
		ComponentNode cn = (ComponentNode) summary;

		Node result = cn.addPropertyFromDND(aType, false); // should create property added to summary facet
		assertNotNull(result);
		assertTrue(bo.getSummaryFacet().getChildren().contains(result));

		result = cn.addPropertyFromDND(aType, true); // should create new property with type of aType
		assertNotNull(result);
		assertEquals(aType, result.getType());
		assertTrue(bo.getSummaryFacet().getChildren().contains(result));

		result = ((ComponentNode) result).addPropertyFromDND(aType, false);
		assertNotNull(result); // should create new node and assign type
		assertTrue(bo.getSummaryFacet().getChildren().contains(result));

		((PropertyNode) result).setAssignedType();
		result = ((ComponentNode) result).addPropertyFromDND(aType, false);
		assertNull(result); // should assign type but not create new node
	}

	private void changeToAll(PropertyNode pn) {
		int children = pn.getParent().getChildren().size();
		assert pn.getParent() != null; // test the swap behavior too
		pn = pn.changePropertyRole(PropertyNodeType.ATTRIBUTE);
		pn = pn.changePropertyRole(PropertyNodeType.INDICATOR);
		pn = pn.changePropertyRole(PropertyNodeType.INDICATOR_ELEMENT);
		pn = pn.changePropertyRole(PropertyNodeType.ID_REFERENCE);
		pn = pn.changePropertyRole(PropertyNodeType.ELEMENT);
		Assert.assertEquals(children, pn.getParent().getChildren().size());
	}
}
