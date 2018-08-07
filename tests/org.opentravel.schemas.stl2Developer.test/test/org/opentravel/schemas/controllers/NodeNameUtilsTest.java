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
package org.opentravel.schemas.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.opentravel.schemacompiler.codegen.util.PropertyCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.XsdCodegenUtils;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemas.node.ModelNode;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFinders;
import org.opentravel.schemas.node.NodeNameUtils;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.properties.AttributeNode;
import org.opentravel.schemas.node.properties.ElementNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.properties.PropertyNodeType;
import org.opentravel.schemas.node.properties.TypedPropertyNode;
import org.opentravel.schemas.node.typeProviders.SimpleTypeNode;
import org.opentravel.schemas.node.typeProviders.facetOwners.CoreObjectNode;
import org.opentravel.schemas.stl2developer.OtmRegistry;
import org.opentravel.schemas.testUtils.MockLibrary;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.utils.PropertyNodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Pawel Jedruch
 * 
 */
public class NodeNameUtilsTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(NodeNameUtilsTest.class);

	public static final String[] INVALID_SUFFIX = new String[] { "_Type", "_type" };
	private static final String ID_REFERENCE_SUFFIX = "Ref";
	private static MainController mc;

	@BeforeClass
	public static void beforeTests() {
		// new ModelNode(new TLModel());
		mc = OtmRegistry.getMainController(); // isolate from previous test (I think)
		// When run in all tests i got 4 type assignment errors where listener did not match target

		assert OtmRegistry.getMainController() == mc;
		assert NodeFinders.findNodeByName("ID", ModelNode.XSD_NAMESPACE) != null;
		LOGGER.debug("Before class");
	}

	@After
	public void clearModel() {
		mc.getProjectController().closeAll();
	}

	@Rule
	public ErrorCollector collector = new ErrorCollector();

	@Test
	public void NodeTypeNameTests() {
		// Given - a library with one of each object type in it.
		MockLibrary ml = new MockLibrary();
		ModelNode root = mc.getModelNode();
		LibraryNode ln = ml.createNewLibrary_Empty("http://example.com/test", "TestLib",
				mc.getProjectController().getDefaultProject());
		ml.addOneOfEach(ln, "OneOf");
		// TODO - service and resource - role for core

		// Then - all nodes must report type as used in Facet View
		// mc.getFields().postField(typeField, node.getComponentType(), false);
		for (Node n : root.getDescendants()) {
			LOGGER.debug("Component Type = " + n.getComponentType() + "\tclass = " + n.getClass().getSimpleName());
			LOGGER.debug("   Label = " + n.getLabel());
			LOGGER.debug("   Name  = " + n.getName());
			LOGGER.debug("");
			assertTrue("Must have component type.", !n.getComponentType().isEmpty());
		}

		// Test Setting and then reading names
		// elementRef
	}

	/**
	 * <pre>
	 * Element = InitialCap
	 * ---Type---
	 * SimpleList -> InitialCap + 's'
	 * DetailList -> InitialCap + 'Detail'
	 * UNASSIGNED -> 'Undefined' (check Node.UNDEFINED_PROPERTY_TEXT)
	 * </pre>
	 */

	@Test
	public void elementAssignedSimpleType() {

		// Given a simple type and a built-in type
		TypeProvider string = (TypeProvider) NodeFinders.findNodeByName("string", ModelNode.XSD_NAMESPACE);
		assert string != null;
		SimpleTypeNode myString = new SimpleTypeNode(new TLSimple());
		myString.setAssignedType(string);

		// Then - codegen utils MUST return null for simple types allowing elements to be renamed.
		assertTrue("PropertyCodegenUtils must return null.",
				PropertyCodegenUtils.getDefaultXmlElementName((NamedEntity) string.getTLModelObject(), false) == null);
		assertTrue("PropertyCodegenUtils must return null.",
				PropertyCodegenUtils.getDefaultXmlElementName(myString.getTLModelObject(), false) == null);

		// Given a lower case name and upper case name
		String typeName = "lowerCase";
		String expected = "LowerCase";

		// Given an element on a core summary facet
		CoreObjectNode core = new CoreObjectNode(new TLCoreObject());
		// When NodeNameUtils fix the name in the constructor
		ElementNode element = new ElementNode(core.getFacet_Summary(), typeName);
		// Then the name is as expected.
		assertTrue("When unassigned, name is as set.", element.getName().equals(expected));

		// When assigned built in the name should not change
		element.setAssignedType(string);
		assertTrue("When built-in type assigned, name is as set.", element.getName().equals(expected));
		// When assigned simple type the name should not change
		element.setAssignedType(string);
		assertTrue("When simple type asssigned, name is as set.", element.getName().equals(expected));

		PropertyNode pn = PropertyNodeBuilder.create(PropertyNodeType.ELEMENT).assignVWA("VWA").setName(typeName)
				.build();
		String actual = NodeNameUtils.fixElementName(pn);
		assertEquals("NodeNameUtils must return expected name.", expected, actual);
		assertEquals("Node must have expected name.", expected, pn.getName()); // make sure user could reassign name
	}

	@Test
	public void elementAssignedComplexType() {
		String typeName = "lowerCase";
		PropertyNode pn = PropertyNodeBuilder.create(PropertyNodeType.ELEMENT).assignBuisnessObject("BO")
				.setName(typeName).build();
		String actual = NodeNameUtils.fixElementName(pn);
		String expected = "BO";
		assertEquals(expected, actual);
		assertEquals(expected, pn.getName()); // make sure name comes from assigned BO
	}

	@Test
	public void elementWithLowerCase() {
		String typeName = "lowerCase";
		PropertyNode pn = PropertyNodeBuilder.create(PropertyNodeType.ELEMENT).assignVWA("VWA").setName(typeName)
				.build();
		String actual = NodeNameUtils.fixElementName(pn);
		String expected = "LowerCase";
		assertEquals(expected, actual);
		assertEquals(expected, pn.getName());
	}

	@Test
	public void elementRef() {
		// Element Ref names defined by compiler
		//
		// // Create a BO to reference
		// BusinessObjectNode bo = new BusinessObjectNode(new TLBusinessObject());
		// bo.setName("BO");
		// bo.getFacet_ID().addProperty(PropertyNodeBuilder.create(PropertyNodeType.ELEMENT).build());
		//
		// // Make sure the assigned name uses the full name used by the compiler.
		// PropertyNode pn = PropertyNodeBuilder.create(PropertyNodeType.ID_REFERENCE).build();
		// pn.setAssignedType(bo);
		// QName name = PropertyCodegenUtils.getDefaultSchemaElementName((NamedEntity) bo.getTLModelObject(), true);
		// assert name.getLocalPart().equals(pn.getName());
		//
		// // Facets get named by the compiler to use their long name. Make sure GUI matches.
		// pn.setAssignedType((TypeProvider) bo.getFacet_ID());
		// name = PropertyCodegenUtils.getDefaultSchemaElementName(
		// (NamedEntity) ((Node) bo.getFacet_ID()).getTLModelObject(), true);
		// assert name.getLocalPart().equals(pn.getName());
	}

	@Test
	public void elementWithSimpleList() {
		// Given - a core simple list facet and an element
		String typeName = "CO";
		ElementNode ele = new ElementNode(new TLProperty(), null);
		CoreObjectNode core = new CoreObjectNode(new TLCoreObject());
		core.setName(typeName);
		// When assigned
		ele.setAssignedType((TypeProvider) core.getSimpleListFacet());
		String expected = typeName + "s";
		String actual = NodeNameUtils.fixElementName(ele);
		// Then
		assertTrue("Utils corrected name.", actual.equals(expected));
		assertTrue("Element has corrected name.", ele.getName().equals(expected));

		// PropertyNode pn = PropertyNodeBuilder.create(PropertyNodeType.ELEMENT).makeSimpleList(typeName).build();
		// String actual = NodeNameUtils.fixElementName(pn);
		// assertEquals(expected, actual);
	}

	@Test
	public void elementWithSimpleListWithInitialName() {
		String typeName = "CO";
		PropertyNode pn = PropertyNodeBuilder.create(PropertyNodeType.ELEMENT).setName("InitialName")
				.makeSimpleList(typeName).build();
		String actual = NodeNameUtils.fixElementName(pn);
		String expected = typeName + "s";
		assertEquals(expected, actual);
	}

	@Test
	public void elementWithDetailList() {
		String typeName = "CO";
		TypedPropertyNode pn = (TypedPropertyNode) PropertyNodeBuilder.create(PropertyNodeType.ELEMENT)
				.makeDetailList(typeName).build();
		String actual = NodeNameUtils.fixElementName(pn);
		// will append Detail suffix
		String expected = XsdCodegenUtils.getGlobalElementName(pn.getAssignedTLNamedEntity()).getLocalPart();
		assertEquals(expected, actual);
	}

	@Test
	public void elementWithUnassigedType() {
		// PropertyNode pn = PropertyNodeBuilder.create(PropertyNodeType.ELEMENT).build();
		PropertyNode pn = new ElementNode(new TLProperty(), null);
		String fixed = NodeNameUtils.fixElementName(pn);
		String expected = Node.UNDEFINED_PROPERTY_TXT;
		assertEquals(expected, fixed);
		// 3/20/2018 - name comes directly from tlModelObject and is empty
		// String actual = pn.getName();
		// assertEquals(expected, actual);
	}

	@Test
	public void attriubteWithUnassigedType() {
		// PropertyNode pn = PropertyNodeBuilder.create(PropertyNodeType.ELEMENT).build();
		PropertyNode pn = new AttributeNode(new TLAttribute(), null);
		String fixed = NodeNameUtils.fixElementName(pn);
		String expected = Node.UNDEFINED_PROPERTY_TXT;
		assertEquals(expected, fixed);
		// 3/20/2018 - name comes directly from tlModelObject and is empty
		// String actual = pn.getName();
		// assertEquals(expected, actual);
	}

	/**
	 * Attribute = lowerCase
	 */

	@Test
	public void attributeWithUpperCase() {
		String actual = "InvalidAttribute";
		String expected = "invalidAttribute";
		assertEquals(expected, NodeNameUtils.fixAttributeName(actual));
	}

	/**
	 * <pre>
	 * Indicator = lowerCase + 'Ind'
	 * Can not start with 'is'
	 * </pre>
	 */

	@Test
	public void indicatorWithUpperCase() {
		String actual = "InvalidIndicatorElement";
		String expected = "invalidIndicatorElement" + NodeNameUtils.IndicatorSuffix;
		assertEquals(expected, NodeNameUtils.fixIndicatorName(actual));
	}

	@Test
	public void indicatorWithBannedPrefixShouldRemove() {
		String expected = "invalidIndicatorElementInd";
		String actual = NodeNameUtils.IndicatorBannedPrefix + expected;
		assertEquals(expected, NodeNameUtils.fixIndicatorName(actual));
	}

	/**
	 * <pre>
	 * IndicatorElement = upperCase + 'Ind'
	 * </pre>
	 * 
	 * Can not start with 'is'
	 */

	@Test
	public void indicatorElementShouldInitialCapAndSuffix() {
		String actual = "invalidIndicatorElement";
		String expected = "InvalidIndicatorElement" + NodeNameUtils.IndicatorSuffix;
		assertEquals(expected, NodeNameUtils.fixIndicatorElementName(actual));
	}

	@Test
	public void indicatorElementWithInvalidSuffixShouldAdd() {
		String actual = "InvalidIndicatorElementind";
		String expected = actual + NodeNameUtils.IndicatorSuffix;
		assertEquals(expected, NodeNameUtils.fixIndicatorElementName(actual));
	}

	@Test
	public void indicatorElementWithBannedPrefixShouldRemove() {
		String expected = "InvalidIndicatorElementInd";
		String actual = NodeNameUtils.IndicatorBannedPrefix + expected;
		assertEquals(expected, NodeNameUtils.fixIndicatorElementName(actual));
	}

	@Test
	public void indicatorElementWithCorrectShouldDoNothing() {
		String actual = "InvalidIndicatorElementInd";
		assertEquals(actual, NodeNameUtils.fixIndicatorElementName(actual));
	}

	/**
	 * <pre>
	 * Simple Type = upperCase 
	 * Without suffixes: 'Type', 'type', '_'
	 * </pre>
	 */

	@Test
	public void simpleTypeWithLowerCase() {
		String expected = "LowerCase";
		String actual = "lowerCase";
		// assertEquals(expected, NodeNameUtils.fixSimpleTypeName(actual));
	}

	// @Test
	// public void simpleTypeWithInvalidSuffixes() {
	// String expected = "LowerCase";
	// checkNameAgainsInvalidSuffixes(expected, new NameFixer() {
	//
	// @Override
	// public String fixName(String name) {
	// return NodeNameUtils.fixSimpleTypeName(name);
	// }
	//
	// }, INVALID_SUFFIX);
	// }

	// @Test
	// public void simpleTypeWithValidSuffixAndInvalidSuffixes() {
	// String typeName = "LowerCase";
	// // String actual = typeName + "_typeType";
	// String expected = typeName;
	// assertEquals(expected, NodeNameUtils.fixSimpleTypeName(actual));
	// }

	// @Test
	// public void simpleTypeWithValidSuffixAndInvalidSuffixes2() {
	// String typeName = "LowerCase";
	// String expected = typeName;
	// // String actual = typeName + "type";
	// assertEquals(expected, NodeNameUtils.fixSimpleTypeName(actual));
	// }

	@Test
	public void simpleTypeWithInvalidSuffixInTheMiddle() {
		String expected = "LowerTypeCase";
		String actual = expected;
		// assertEquals(expected, NodeNameUtils.fixSimpleTypeName(actual));
	}

	private void checkNameAgainsInvalidSuffixes(final String initialName, NameFixer fix, String... suffixes) {
		for (String suffix : INVALID_SUFFIX) {
			final String beforeFix = initialName + suffix;
			final String actual = fix.fixName(beforeFix);
			addError(collector, new Runnable() {

				@Override
				public void run() {
					assertEquals("Before fix name: " + beforeFix, initialName, actual);

				}
			});
		}
	}

	private void addError(ErrorCollector collector, Runnable assertClause) {
		try {
			assertClause.run();
		} catch (Throwable e) {
			collector.addError(e);
		}
	}

	static interface NameFixer {

		String fixName(String name);
	}

	/**
	 * <pre>
	 * Enumeration = 'Enum_' + name
	 * </pre>
	 */
	@Test
	public void enumerationShouldStartWithPrefix() {
		// changed behavior 12/2015
		// String actual = "LowerCase";
		// String expected = NodeNameUtils.ENUM_PREFIX + actual;
		// assertEquals(expected, NodeNameUtils.fixEnumerationName(actual));
	}

	/**
	 * <pre>
	 * VWA = UpperCase
	 * Without suffixes: 'Type', 'type', '_'
	 * </pre>
	 */
	@Test
	public void vwaTypeWithLowerCase() {
		String expected = "LowerCase";
		String actual = "lowerCase";
		assertEquals(expected, NodeNameUtils.fixVWAName(actual));
	}

	// @Test
	// public void vwaTypeWithInvalidSuffixes() {
	// String expected = "LowerCase";
	// checkNameAgainsInvalidSuffixes(expected, new NameFixer() {
	//
	// @Override
	// public String fixName(String name) {
	// return NodeNameUtils.fixVWAName(name);
	// }
	//
	// }, INVALID_SUFFIX);
	// }

	/**
	 * <pre>
	 * CoreObject = UpperCase
	 * Without suffixes: 'Type', 'type', '_'
	 * </pre>
	 */
	@Test
	public void coreObjectWithLowerCase() {
		String expected = "LowerCase";
		String actual = "lowerCase";
		assertEquals(expected, NodeNameUtils.fixCoreObjectName(actual));
	}

	// @Test
	// public void coreObjectWithInvalidSuffixes() {
	// String expected = "LowerCase";
	// checkNameAgainsInvalidSuffixes(expected, new NameFixer() {
	//
	// @Override
	// public String fixName(String name) {
	// return NodeNameUtils.fixCoreObjectName(name);
	// }
	//
	// }, INVALID_SUFFIX);
	// }

	/**
	 * <pre>
	 * alias = UpperCase
	 * </pre>
	 */
	@Test
	public void aliasObjectWithLowerCase() {
		String actual = "lowerCase";
		String expected = "LowerCase";
		assertEquals(expected, NodeNameUtils.fixCoreObjectName(actual));
	}

	/**
	 * <pre>
	 * IDReference = UpperCase + 'Ref'
	 * </pre>
	 */
	// @Test
	// public void idReferenceTypeBO() {
	// String typeName = "Name";
	// String referencedType = "BOObject";
	// PropertyNode pn = PropertyNodeBuilder.create(PropertyNodeType.ID_REFERENCE)
	// .assignBuisnessObject(referencedType).setName(typeName).build();
	//
	// String actual = NodeNameUtils.fixIdReferenceName(pn);
	// String expected = referencedType + ID_REFERENCE_SUFFIX;
	// assertEquals(expected, actual);
	// }

	@Test
	public void idReferenceTypeCoreObject() {
		String typeName = "Name";
		String referencedType = "CoreObjectObject";
		PropertyNode pn = PropertyNodeBuilder.create(PropertyNodeType.ID_REFERENCE).assignCoreObject(referencedType)
				.setName(typeName).build();

		String actual = NodeNameUtils.fixIdReferenceName(pn);
		String expected = referencedType + ID_REFERENCE_SUFFIX;
		assertEquals(expected, actual);
	}

	@Test
	public void stripQueryFacetPrefix() {
		String boName = "BO";
		String facetName = "myQuery";
		// FIXME - addCustomFacet
		// BusinessObjectNode bo = ComponentNodeBuilder.createBusinessObject(boName).addQueryFacet(facetName).get();
		// ContextualFacetNode fn = (ContextualFacetNode) bo.getQueryFacets().get(0);
		// assertTrue("TL name must equal node name.", fn.getTLModelObject().getLocalName().equals(fn.getName()));

		// 8/2017 - no longer true for contextual facets. The tl localName is used.
		// String expectedName = NodeNameUtils.fixContextualFacetName(fn, facetName);
		// assertTrue("Actual name must be fixed by node name utils.", fn.getName().equals(expectedName));
	}

	@Test
	public void stripCustomFacetPrefix() {
		String boName = "BO";
		String facetName = "myCustom";
		// FIXME - addCustomFacet
		// BusinessObjectNode bo = ComponentNodeBuilder.createBusinessObject(boName).addCustomFacet(facetName).get();
		// ContextualFacetNode fn = bo.getCustomFacets().get(0);
		// String gtn = XsdCodegenUtils.getGlobalTypeName(fn.getTLModelObject());
		// String cfn = fn.getName();
		// String lfn = fn.getLocalName();

		// // Then - assure custom facet name is corrected
		// String expectedName = NodeNameUtils.fixContextualFacetName(fn, facetName);
		// // assertTrue("Contextual node name corrected.", fn.getName().equals(expectedName));
		// assertTrue("TL name must equal node name.", fn.getTLModelObject().getLocalName().equals(fn.getName()));
		//
		// // When - rename
		// String changedName = "SomeOtherName";
		// fn.setName(changedName);
		//
		// // Then - make sure original boName is not repeated
		// assertTrue("Custom facet has new name.", fn.getName().contains(changedName));
	}

	@Test
	public void stripCustomFacetPrefix2() {
		// Given - a BO with custom facet
		String boName = "BO";
		String facetName = "Custom_myCustom";
		// FIXME - addCustomFacet
		// BusinessObjectNode bo = ComponentNodeBuilder.createBusinessObject(boName).addCustomFacet(facetName).get();
		// ContextualFacetNode fn = bo.getCustomFacets().get(0);
		// String fullName = fn.getTLModelObject().getLocalName(); // used for contextual facets

		// Then
		// assertEquals("Node and TL object names must be equal.", fullName, fn.getName());

		// When - facet name is fixed (is OK as is) as done in ContextualFacet.setName()
		// String expectedName = NodeNameUtils.fixContextualFacetName(fn, facetName);
		// Then name must not be changed and as predicted.
		// assertEquals("Name must be " + expectedName, facetName, expectedName);
	}

	private String getFacetName(TLFacet tlFacet) {
		return XsdCodegenUtils.getGlobalTypeName(tlFacet);
	}
}
