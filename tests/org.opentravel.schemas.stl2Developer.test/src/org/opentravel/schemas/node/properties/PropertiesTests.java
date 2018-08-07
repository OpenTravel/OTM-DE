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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLEnumValue;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLOpenEnumeration;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLRole;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFinders;
import org.opentravel.schemas.node.NodeNameUtils;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.typeProviders.EnumerationClosedNode;
import org.opentravel.schemas.node.typeProviders.EnumerationOpenNode;
import org.opentravel.schemas.node.typeProviders.FacetProviderNode;
import org.opentravel.schemas.node.typeProviders.RoleFacetNode;
import org.opentravel.schemas.node.typeProviders.VWA_Node;
import org.opentravel.schemas.node.typeProviders.facetOwners.BusinessObjectNode;
import org.opentravel.schemas.node.typeProviders.facetOwners.CoreObjectNode;
import org.opentravel.schemas.testUtils.BaseTest;
import org.opentravel.schemas.types.TypeProvider;

/**
 * @author Dave Hollander
 * 
 */
public class PropertiesTests extends BaseTest {

	@Before
	public void beforeEachOfTheseTests() {
		ln = ml.createNewLibrary("http://example.com/test", "test", defaultProject);
	}

	@Test
	public void equivalents() {
		LibraryChainNode lcn = ml.createNewManagedLibrary("EQ_Test", pc.getDefaultProject());
		ln = lcn.getHead();
		BusinessObjectNode bo = ml.addBusinessObjectToLibrary(ln, "EQBO");
		PropertyNode p = (PropertyNode) bo.getFacet_Summary().getChildren().get(0);
		createEquivalents(p);
	}

	/**
	 * Adds an equivalent to the property node and tests it.
	 */
	public void createEquivalents(PropertyNode p) {
		assertNotNull(p);
		p.setEquivalent("V1"); // creates handler
		IValueWithContextHandler eqh = p.getEquivalentHandler();
		assertNotNull(eqh);
		assertNotNull(p.getLibrary().getTLLibrary());
		testValueWithContextHandler(eqh);
	}

	@Test
	public void examples() {
		LibraryChainNode lcn = ml.createNewManagedLibrary("EQ_Test", pc.getDefaultProject());
		ln = lcn.getHead();
		BusinessObjectNode bo = ml.addBusinessObjectToLibrary(ln, "EQBO");
		PropertyNode p = (PropertyNode) bo.getFacet_Summary().getChildren().get(0);
		assertNotNull(p);
		p.setExample("V1"); // creates handler
		IValueWithContextHandler exh = p.getExampleHandler();
		assertNotNull(exh);
		assertNotNull(p.getLibrary().getTLLibrary());
		testValueWithContextHandler(exh);
	}

	private void testValueWithContextHandler(IValueWithContextHandler handler) {
		handler.set("V1", null); // Uses default context
		assertEquals(1, handler.getCount());
		String defaultAppContext = handler.getApplicationContext();
		String defaultContextId = handler.getContextID();
		assertFalse(defaultAppContext.isEmpty());
		assertFalse(defaultContextId.isEmpty());

		// Add context to library and context manager -
		// must use context controller because it is needed in the valid context tests.
		// 1/1/2017 dmh - commented out as this is the only caller of newContext()
		// mc.getContextController().newContext(ln, "C1", "CA1");
		// mc.getContextController().newContext(ln, "C2", "CA2");

		// Create 2 values
		handler.set("V2", "C1"); // removes other values
		handler.set("V3", "C2"); // removes other values
		assertEquals(1, handler.getCount());

		assertEquals("", handler.get("C1"));
		assertEquals("V3", handler.get("C2"));

		handler.set("V4", "C2"); // removes other values

		// C2 is not in context controller so fix changes context value to nsPrefix
		handler.fix("C2");
		assertEquals(1, handler.getCount());
		assertEquals("V4", handler.get(defaultContextId));
		assertTrue(handler.getApplicationContext().equals(defaultAppContext));

		// Test fix to move value V4 to default context.
		handler.fix(null);
		assertEquals(1, handler.getCount());
		assertEquals("V4", handler.get(null));
		assertTrue(handler.getApplicationContext().equals(defaultAppContext));

		handler.set(null, "C1"); // remove value
		assertEquals(0, handler.getCount());

		// Test setting and getting with no context.
		handler.set("V5", null); // removes other values
		assertTrue(handler.get(null).equals("V5"));
	}

	@Test
	public void createElements() {
		BusinessObjectNode bo = ml.addBusinessObjectToLibrary(ln, "TestBO");
		FacetProviderNode summary = bo.getFacet_Summary();
		assertNotNull(summary);
		Node aType = NodeFinders.findNodeByName("date", ModelNode.XSD_NAMESPACE);
		PropertyNode pn = null;
		int startCount = summary.getChildren().size();
		// When - add new element
		pn = new ElementNode(summary, "A");
		assertNotNull(pn);
		assertNotNull(pn.getLibrary());
		assertEquals(pn.getName(), "A");
		// When - add new element
		pn = new ElementNode(new TLProperty(), summary);
		assertNotNull(pn);
		pn.setName("b");
		assertNotNull(pn.getLibrary());
		assertTrue(pn instanceof ElementNode);
		assertEquals(pn.getName(), "B");
		assertFalse(pn.getLabel().isEmpty());
		pn.setName("AAA");
		assertEquals(pn.getName(), "AAA");
		assertNotNull(pn.getLibrary());

		// When - add new element
		pn = (PropertyNode) pn.createProperty(aType);
		assertNotNull(pn);

		assertEquals(startCount + 3, summary.getChildren().size()); // addBO creates one
	}

	@Test
	public void createElementRefs() {
		BusinessObjectNode bo = ml.addBusinessObjectToLibrary(ln, "TestBO");
		BusinessObjectNode A = ml.addBusinessObjectToLibrary(ln, "A");
		FacetProviderNode summary = bo.getFacet_Summary();
		assertNotNull(summary);
		Node aType = NodeFinders.findNodeByName("date", ModelNode.XSD_NAMESPACE);
		TypedPropertyNode pn = null;
		int startCount = summary.getChildren().size();

		pn = new ElementReferenceNode(summary); // Name is "Missing"
		assertNotNull(pn);
		assertNotNull(pn.getLibrary());
		pn.setAssignedType(A);
		assertEquals("ARef", pn.getName());

		TLProperty tlp = new TLProperty();
		tlp.setReference(true);
		pn = new ElementReferenceNode(tlp, summary);
		assertNotNull(pn);
		assertNotNull(pn.getLibrary());
		// No effect! - pn.setName("b");
		pn.setAssignedType(bo);
		assertFalse(pn.getName().isEmpty());
		assertFalse(pn.getLabel().isEmpty());
		assertNotNull(pn.getLibrary());

		assertEquals(startCount + 2, summary.getChildren().size());
	}

	@Test
	public void createAttributes() {
		BusinessObjectNode bo = ml.addBusinessObjectToLibrary(ln, "TestBO");
		FacetProviderNode summary = bo.getFacet_Summary();
		assertTrue(summary != null);
		Node aType = NodeFinders.findNodeByName("date", ModelNode.XSD_NAMESPACE);
		assertTrue(aType != null);
		PropertyNode pn, pn1 = null;
		int startingCount = summary.getChildren().size();

		// When - new attribute
		pn1 = new AttributeNode(summary, "A");
		assertTrue(pn1 != null);
		assertTrue(pn1.getLibrary() != null);
		assertEquals("a", pn1.getName());

		// When - new attribute - different constructor
		pn = new AttributeNode(new TLAttribute(), summary);
		assertTrue(pn != null);
		pn.setName("b");
		assertTrue(pn.getLibrary() != null);
		assertEquals(NodeNameUtils.fixAttributeName("B"), pn.getName());
		assertFalse(pn.getLabel().isEmpty());
		assertTrue(pn.getLibrary() != null);

		// When - new attribute
		pn = (PropertyNode) pn1.createProperty(aType);
		assertTrue(pn != null);

		// TODO - test descriptions ~!!!
		// TODO - test examples ~!!!
		// TODO - test equivalents ~!!!

		assertEquals("All children must be accounted for.", startingCount + 3, summary.getChildren().size()); // addBO
																												// creates
																												// one
	}

	@Test
	public void createIds() {
		VWA_Node vwa = ml.addVWA_ToLibrary(ln, "Vwa");
		final String idName = "SomeIgnoredName";
		IdNode id = new IdNode(vwa.getFacet_Attributes(), idName);
		Node idType = NodeFinders.findNodeByName("ID", ModelNode.XSD_NAMESPACE);

		// Assert.assertEquals("id", id.getName());
		assertEquals(NodeNameUtils.fixAttributeName(idName), id.getName());
		assertEquals(idType, id.getAssignedType());
		assertEquals(id.getPropertyType(), PropertyNodeType.ID);
		// assertEquals(id.alternateRoles.currentType, PropertyNodeType.ID);
		// assertNotNull(id.alternateRoles.oldIdN);
	}

	@Test
	public void createIndicatorElements() {
		BusinessObjectNode bo = ml.addBusinessObjectToLibrary(ln, "TestBO");
		FacetProviderNode summary = bo.getFacet_Summary();
		assertNotNull(summary);
		Node aType = NodeFinders.findNodeByName("date", ModelNode.XSD_NAMESPACE);
		PropertyNode pn, pn1 = null;
		int startCount = summary.getChildren().size();
		// When - add new element
		pn1 = new IndicatorElementNode(summary, "A");
		assertNotNull(pn1);
		assertNotNull(pn1.getLibrary());
		assertEquals("AInd", pn1.getName());
		// When - add new element
		pn = new IndicatorElementNode(new TLIndicator(), summary);
		assertNotNull(pn);
		pn.setName("b");
		assertNotNull(pn.getLibrary());
		assertEquals(NodeNameUtils.fixIndicatorElementName("B"), pn.getName());
		// assertNotNull(pn.getRequiredType());
		assertFalse(pn.getLabel().isEmpty());
		assertNotNull(pn.getLibrary());
		// When - add new element
		pn = (PropertyNode) pn1.createProperty(aType);
		assertNotNull(pn);

		// TODO - test descriptions ~!!!
		// TODO - test examples ~!!!
		// TODO - test equivalents ~!!!

		assertEquals(startCount + 3, summary.getChildren().size());
	}

	@Test
	public void createIndicator() {
		BusinessObjectNode bo = ml.addBusinessObjectToLibrary(ln, "TestBO");
		FacetProviderNode summary = bo.getFacet_Summary();
		assertNotNull(summary);
		Node aType = NodeFinders.findNodeByName("date", ModelNode.XSD_NAMESPACE);
		PropertyNode pn, pn1 = null;
		int startCount = summary.getChildren().size();

		pn1 = new IndicatorNode(summary, "A");
		assertNotNull(pn1);
		assertNotNull(pn1.getLibrary());
		assertEquals("aInd", pn1.getName());

		pn = new IndicatorElementNode(new TLIndicator(), summary);
		assertNotNull(pn);
		pn.setName("b");
		assertNotNull(pn.getLibrary());
		assertEquals(NodeNameUtils.fixIndicatorElementName("b"), pn.getName());
		// assertNotNull(pn.getRequiredType());
		assertFalse(pn.getLabel().isEmpty());
		assertNotNull(pn.getLibrary());

		pn = (PropertyNode) pn1.createProperty(aType);
		assertNotNull(pn);

		// TODO - test descriptions ~!!!
		// TODO - test examples ~!!!
		// TODO - test equivalents ~!!!

		assertEquals(startCount + 3, summary.getChildren().size()); // addBO creates one
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
		assertNotNull("litA");
		lit = new EnumLiteralNode(new TLEnumValue(), open);
		lit.setName("B");
		assertNotNull("litA");

		lit = (EnumLiteralNode) litA.createProperty(aType);
		assertEquals(NodeNameUtils.fixEnumerationValue(aType.getName()), lit.getName());
		assertEquals(3, open.getChildren().size());

	}

	@Test
	public void createRoles() {
		CoreObjectNode core = ml.addCoreObjectToLibrary(ln, "Core");
		Node aType = NodeFinders.findNodeByName("date", ModelNode.XSD_NAMESPACE);
		RoleFacetNode roles = core.getFacet_Role();
		RoleNode rn1, rn = null;
		int startCount = roles.getChildren().size();

		rn1 = new RoleNode(roles, "A");
		assertNotNull(rn1);
		assertEquals("A", rn1.getName());

		rn = new RoleNode(new TLRole(), roles);
		rn.setName("B");
		assertNotNull(rn);
		assertEquals("B", rn.getName());

		rn = (RoleNode) rn1.createProperty(aType);
		assertNotNull(rn);
		assertEquals("date", rn.getName());

		assertEquals(startCount + 3, roles.getChildren().size());
	}

	@Test
	public void assignedNames() {
		ln.setEditable(true);
		BusinessObjectNode bo = ml.addBusinessObjectToLibrary(ln, "Ct");
		FacetProviderNode summary = bo.getFacet_Summary();
		TypeProvider aType = (TypeProvider) NodeFinders.findNodeByName("date", ModelNode.XSD_NAMESPACE);
		TypedPropertyNode pn, epn, apn, rpn = null;
		PropertyNode ipn, iepn = null;
		String eText = "Element";
		String aText = "attribute";
		String iText = "xInd";
		String rText = "ctRef";
		String ieText = "XInd";

		epn = new ElementNode(summary, eText, aType);
		apn = new AttributeNode(summary, aText);
		apn.setAssignedType((TypeProvider) NodeFinders.findNodeByName("id", ModelNode.XSD_NAMESPACE));
		ipn = new IndicatorNode(summary, iText);
		iepn = new IndicatorElementNode(summary, ieText);
		rpn = new ElementReferenceNode(summary, bo);

		// Then - all named as expected.
		assertEquals(NodeNameUtils.fixElementName(eText), epn.getName());
		assertEquals(NodeNameUtils.fixAttributeName(aText), apn.getName());
		assertEquals(NodeNameUtils.fixIndicatorName(iText), ipn.getName());
		assertEquals(NodeNameUtils.fixIndicatorElementName(ieText), iepn.getName());
		// Node name utils do not do element ref.
		// assertEquals(NodeNameUtils.fixElementRefName(rText), rpn.getName());
		assertTrue(rpn.getName().startsWith(bo.getName()));

		epn.setDescription(eText);
		assertEquals(eText, epn.getDescription());

		apn.copyDetails(epn);
		assertEquals(eText, apn.getDescription());

		AttributeReferenceNode arn = new AttributeReferenceNode(summary, bo);
		assertTrue(arn != null);
		assertTrue(arn.getName().startsWith(bo.getName()));
	}

	@Test
	public void changeRoles() {
		ln.setEditable(true);
		BusinessObjectNode bo = ml.addBusinessObjectToLibrary(ln, "ct");
		FacetProviderNode summary = bo.getFacet_Summary();
		TypeProvider aType = (TypeProvider) NodeFinders.findNodeByName("date", ModelNode.XSD_NAMESPACE);
		TypedPropertyNode pn, epn, apn, rpn = null;
		PropertyNode ipn, iepn = null;
		String eText = "Element";
		String aText = "attribute";
		String iText = "xInd";
		String rText = "ctRef";
		String ieText = "XInd";

		epn = new ElementNode(summary, eText, aType);
		apn = new AttributeNode(summary, aText);
		apn.setAssignedType((TypeProvider) NodeFinders.findNodeByName("id", ModelNode.XSD_NAMESPACE));
		ipn = new IndicatorNode(summary, iText);
		iepn = new IndicatorElementNode(summary, ieText);
		rpn = new ElementReferenceNode(summary);

		// Change all property roles
		List<Node> kids = new ArrayList<>(summary.getChildren());
		for (Node n : kids) {
			if (n instanceof PropertyNode)
				changeToAll((PropertyNode) n);
		}
		// Do it again to assure the alternateRoles logic works
		kids = new ArrayList<>(summary.getChildren());
		for (Node n : kids) {
			if (n instanceof PropertyNode)
				changeToAll((PropertyNode) n);
		}
	}

	@Test
	public void addPropertyFromDND_Tests() {
		// This seems to be dependent on the type of "this" node. create property should only be implemented for facets.
		ln.setEditable(true);
		BusinessObjectNode bo = ml.addBusinessObjectToLibrary(ln, "ct");
		FacetProviderNode summary = bo.getFacet_Summary();
		Node aType = NodeFinders.findNodeByName("date", ModelNode.XSD_NAMESPACE);
		ComponentNode cn = summary;

		// Does not work. Requires workbench to resolve view.
		// OtmRegistry.getNavigatorView().setCurrentNode(aType);
		// Event event = new Event();
		// event.data = cn; // signals handler to add to this node
		// new AddNodeHandler2().execute(event);
		// TODO - add assertion
		// Node result = cn.addPropertyFromDND(aType, false); // should create property added to summary facet
		// assertNotNull(result);
		// assertTrue(bo.getSummaryFacet().getChildren().contains(result));
		//
		// result = cn.addPropertyFromDND(aType, true); // should create new property with type of aType
		// assertNotNull(result);
		// assertEquals(aType, result.getType());
		// assertTrue(bo.getSummaryFacet().getChildren().contains(result));
		//
		// result = ((ComponentNode) result).addPropertyFromDND(aType, false);
		// assertNotNull(result); // should create new node and assign type
		// assertTrue(bo.getSummaryFacet().getChildren().contains(result));
		//
		// ((PropertyNode) result).setAssignedType();
		// result = ((ComponentNode) result).addPropertyFromDND(aType, false);
		// assertNull(result); // should assign type but not create new node
	}

	private void changeToAll(PropertyNode pn) {
		int children = pn.getParent().getChildren().size();
		assert pn.getParent() != null; // test the swap behavior too
		pn = pn.changePropertyRole(PropertyNodeType.ATTRIBUTE);
		pn = pn.changePropertyRole(PropertyNodeType.INDICATOR);
		pn = pn.changePropertyRole(PropertyNodeType.INDICATOR_ELEMENT);
		pn = pn.changePropertyRole(PropertyNodeType.ID_REFERENCE);
		pn = pn.changePropertyRole(PropertyNodeType.ELEMENT);
		assertEquals(children, pn.getParent().getChildren().size());
	}
}
