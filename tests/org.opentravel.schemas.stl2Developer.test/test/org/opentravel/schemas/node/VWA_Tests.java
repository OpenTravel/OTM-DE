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

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLAttributeType;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLPropertyType;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemas.node.facets.AttributeFacetNode;
import org.opentravel.schemas.node.facets.SimpleFacetFacadeNode;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.properties.AttributeNode;
import org.opentravel.schemas.node.properties.IndicatorNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.properties.SimpleAttributeFacadeNode;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.testUtils.MockLibrary;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeUser;
import org.opentravel.schemas.utils.BaseProjectTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dave Hollander
 * 
 */
public class VWA_Tests extends BaseProjectTest {
	private final static Logger LOGGER = LoggerFactory.getLogger(VWA_Tests.class);

	TypeProvider emptyNode = null;
	TypeProvider sType = null;

	ProjectNode defaultProject;
	LoadFiles lf = new LoadFiles();
	MockLibrary ml = new MockLibrary();
	LibraryChainNode lcn = null;
	LibraryNode ln = null;

	@Before
	public void beforeEachTest() throws Exception {
		LOGGER.debug("***Before VWA Object Tests ----------------------");
		callBeforeEachTest();
		defaultProject = testProject;
		ln = ml.createNewLibrary("http://test.com", "CoreTest", defaultProject);
		ln.setEditable(true);

		emptyNode = (TypeProvider) ModelNode.getEmptyNode();
		sType = (TypeProvider) NodeFinders.findNodeByName("date", ModelNode.XSD_NAMESPACE);
		assertTrue(sType != null);
	}

	@Test
	public void VWA_ConstructorsTests() {
		// Given a library and a Simple Type
		TLSimple tlsType = (TLSimple) sType.getTLModelObject();
		assertTrue("Found tlsType.", tlsType != null);
		String vName = "TestVWA";

		//
		// Given - TLValueWithAttributes without parent type
		//
		TLAttribute tlAttr1 = new TLAttribute();
		tlAttr1.setType(tlsType);
		tlAttr1.setName("attr1");
		TLValueWithAttributes tlVWAnoParent = new TLValueWithAttributes();
		tlVWAnoParent.addAttribute(tlAttr1);
		tlVWAnoParent.setName(vName + "noParent");

		// When - construct node from TL object
		VWA_Node nVwaNoParent = new VWA_Node(tlVWAnoParent);
		// Then - value access is recurring problem, test each step
		assertTrue("VWA must have name set.", nVwaNoParent.getName().startsWith(vName));
		assertTrue("VWA must NOT be a Type User", !(nVwaNoParent instanceof TypeUser));
		assertTrue("VWA must not have library because TL did not.", nVwaNoParent.getLibrary() == null);
		assertTrue("VWA must NOT have assigned type.", nVwaNoParent.getType() == null);
		assertTrue("Listener must be to VWA.", nVwaNoParent == Node.GetNode(nVwaNoParent.getTLModelObject()));
		// assertTrue(nVwaNoParent.getChildrenHandler() != null);
		List<Node> vKids = nVwaNoParent.getChildren();

		// Then - test simple facet
		SimpleFacetFacadeNode fs = nVwaNoParent.getFacet_Simple();
		assertTrue("VWA must have simple facet.", fs != null);
		// List<Node> sKids = fs.getChildren();
		assertTrue("VWA simple facet must NOT be a Type User", !(fs instanceof TypeUser));
		// Deprecated usage - should be typeUser.getAssignedType()
		assertTrue("VWA simple facet must NOT have assigned type.", fs.getType() == null);

		// Then - test simple attribute
		SimpleAttributeFacadeNode sa = nVwaNoParent.getSimpleAttribute();
		assertTrue("VWA must have simple attribute.", sa != null);
		assertTrue("VWA simple attribute must be a Type User", sa instanceof TypeUser);
		Node et = sa.getType();
		assertTrue("VWA simple attribute must have Empty as assigned type.", sa.getAssignedType() == emptyNode);
		assertTrue("VWA simple attribute must have Empty as assigned type.", sa.getType() == emptyNode);

		// Then - test attribute facet
		AttributeFacetNode fa = (AttributeFacetNode) nVwaNoParent.getFacet_Attributes();
		List<Node> aKids = fa.getChildren();
		assertTrue(!fa.getChildren().isEmpty());
		assertTrue("VWA must have 1 child.", fa.getChildren().size() == 1);

		//
		// Given - TLValueWithAttributes with parent type
		//
		TLAttribute tlAttr2 = new TLAttribute();
		tlAttr2.setType(tlsType);
		tlAttr2.setName("attr2");
		TLValueWithAttributes tlVWAwithParent = new TLValueWithAttributes();
		tlVWAwithParent.addAttribute(tlAttr2);
		tlVWAwithParent.setName(vName + "withParent");
		tlVWAwithParent.setParentType(tlsType);
		assertTrue("tlVWA must have type set.", tlVWAwithParent.getParentType() == tlsType);

		// When - construct node from TL object
		VWA_Node nVwawithParent = new VWA_Node(tlVWAwithParent);
		// Then
		assertTrue("VWA has name set.", nVwawithParent.getName().startsWith(vName));
		assertTrue("VWA has 1 child.", nVwawithParent.getFacet_Attributes().getChildren().size() == 1);
		// Then - value access is recurring problem, test each step
		assertTrue("VWA is NOT a Type User", !(nVwawithParent instanceof TypeUser));
		assertTrue("VWA does NOT have assigned type.", nVwawithParent.getType() == null);
		assertTrue("VWA simple facet is NOT a Type User", !(nVwawithParent instanceof TypeUser));
		assertTrue("VWA simple facet does NOT have assigned type.", nVwawithParent.getFacet_Simple().getType() == null);
		sa = nVwawithParent.getSimpleAttribute();
		assertTrue("VWA simple attribute is Type User", sa instanceof TypeUser);
		assertTrue("VWA simple attribute has  assigned type.", sa.getAssignedType() == sType);

		ln.addMember(nVwawithParent);
		ln.addMember(nVwaNoParent);
		ml.check(nVwawithParent);
		ml.check(nVwaNoParent);
	}

	@Test
	public void changeToVWA() {
		LibraryNode ln = ml.createNewLibrary(defaultProject.getNSRoot(), "test", defaultProject);
		BusinessObjectNode bo = ml.addBusinessObjectToLibrary(ln, "bo");
		CoreObjectNode core = ml.addCoreObjectToLibrary(ln, "co");
		VWA_Node tVwa = null, vwa = ml.addVWA_ToLibrary(ln, "vwa");
		int typeCount = ln.getDescendants_LibraryMembers().size();

		tVwa = (VWA_Node) bo.changeObject(SubType.VALUE_WITH_ATTRS);
		checkVWA(tVwa);
		tVwa = (VWA_Node) core.changeObject(SubType.VALUE_WITH_ATTRS);
		checkVWA(tVwa);
		tVwa = (VWA_Node) vwa.changeObject(SubType.VALUE_WITH_ATTRS);
		checkVWA(tVwa);

		// tn.visit(ln);
		Assert.assertEquals(typeCount, ln.getDescendants_LibraryMembers().size());
	}

	@Test
	public void VWA_LoadLibraryTests() throws Exception {
		// test all libs
		lf.loadTestGroupA(mc);
		for (LibraryNode ln : mc.getModelNode().getUserLibraries()) {
			List<Node> types = ln.getDescendants_LibraryMembers();
			for (Node n : types)
				if (n instanceof VWA_Node)
					checkVWA((VWA_Node) n);
		}
	}

	@Test
	public void VWA_InvalidTypeSettingTests() {
		// Try setting the simple attribute node with a variety of nodes
		// Core should not work.
	}

	@Test
	public void VWA_TypeSettingTests() {
		// Given - an unmanaged and managed library and 3 simple types
		ln = ml.createNewLibrary("http://opentravel.org/test", "test", defaultProject);
		LibraryChainNode lcn = ml.createNewManagedLibrary("inChain", defaultProject);
		TypeProvider aType = (TypeProvider) NodeFinders.findNodeByName("date", ModelNode.XSD_NAMESPACE);
		TypeProvider bType = (TypeProvider) NodeFinders.findNodeByName("int", ModelNode.XSD_NAMESPACE);
		TypeProvider cType = (TypeProvider) NodeFinders.findNodeByName("string", ModelNode.XSD_NAMESPACE);
		assertTrue("Unmanaged library must be editable.", ln.isEditable());
		assertTrue("Managed library must be editable.", lcn.isEditable());
		assertTrue("Simple type A must not be null.", aType != null);

		// Given - a new VWA in the unmanaged library
		VWA_Node vwa = ml.addVWA_ToLibrary(ln, "VWA_Test");

		// When - simple type is set
		SimpleAttributeFacadeNode sa = vwa.getFacet_Simple().getSimpleAttribute();
		assertTrue("Simple type must be assigned.", sa.setAssignedType(cType));
		// Then
		assertTrue("Simple type must equal type assigned.", cType == sa.getAssignedType());
		// Then - alternate way to get simple type
		assertTrue("Simple type must be set.", vwa.getAssignedType() != null);

		// When - a new VWA in managed library is created and type set
		vwa = ml.addVWA_ToLibrary(lcn.getHead(), "InChainTest");
		assertTrue("Simple type must be assignable.", vwa.getFacet_Simple().getSimpleAttribute().setAssignedType(bType));
		// Then
		assertTrue("Simple type must equal type assigned.", bType == vwa.getFacet_Simple().getSimpleAttribute()
				.getAssignedType());

		// Given - the tlModelObject from simple type B
		TLModelElement target = bType.getTLModelObject();

		// NamedEntity v1 = vwa.getTLTypeObject();
		TLValueWithAttributes t1 = (TLValueWithAttributes) vwa.getTLModelObject();
		TLAttributeType a1 = t1.getParentType(); // null

		// Test accessing the simple facet via TL objects
		SimpleFacetFacadeNode sf = vwa.getFacet_Simple();
		assertTrue("Simple facet must not be null.", sf != null);

		// Then - access via simple facet
		TLModelElement v2 = sf.getAssignedType().getTLModelObject();
		assertTrue("Simple facet's simple type must be set to bType.", v2 == target);

		// Then - access via simple attribute
		sa = sf.getSimpleAttribute();
		TLModelElement tlo = sa.getTLModelObject();

		// Then - access via simple attribute's Model object and TL object
		NamedEntity v3 = sa.getAssignedTLNamedEntity();
		// 7/2016 - returns the xsd simple not the TLSimple
		// assertTrue("Assigned TL Named Entity getter must return target.", v3 == target);
		// assertTrue("Model object TLType getter must return target.", m3 == target);

		// Test parent type
		// Test via tl Model Object
		((TLValueWithAttributes) vwa.getTLModelObject()).setParentType((TLAttributeType) target);
		TLAttributeType p1 = ((TLValueWithAttributes) vwa.getTLModelObject()).getParentType();
		assertTrue(p1 == target);
	}

	@Test
	public void VWA_AttributeAssignedTypeTests() {
		ln = ml.createNewLibrary("http://opentravel.org/test", "test", defaultProject);
		ln.setEditable(true);
		VWA_Node vwa = ml.addVWA_ToLibrary(ln, "VWA1");
		new AttributeNode(vwa.getFacet_Attributes(), "A1");
		new IndicatorNode(vwa.getFacet_Attributes(), "I1");
		TypeProvider a = (TypeProvider) NodeFinders.findNodeByName("decimal", ModelNode.XSD_NAMESPACE);

		// Check simple type
		vwa.setAssignedType(a);
		Assert.assertEquals(a, vwa.getAssignedType());

		// Check all attributes/indicators
		for (Node n : vwa.getFacet_Attributes().getChildren()) {
			PropertyNode pn = (PropertyNode) n;
			pn.setAssignedType(a);
			if (pn instanceof AttributeNode)
				assertTrue(pn.getAssignedType() == a);
		}
	}

	@Test
	public void VWA_EqEx_Tests() {
		// Given a library and a Simple Type
		ln = ml.createNewLibrary("http://opentravel.org/test", "test", defaultProject);
		ln.setEditable(true);
		TLSimple tlsType = (TLSimple) sType.getTLModelObject();
		assertTrue("Must find tlsType.", tlsType != null);
		String vName = "TestVWA";
		String Ex1 = "Example Value 1";
		String Eq1 = "Equivalent Value 1";

		// Build VWA
		TLAttribute tlAttr1 = new TLAttribute();
		tlAttr1.setType(tlsType);
		tlAttr1.setName("attr1");
		TLValueWithAttributes thisTLVWA = new TLValueWithAttributes();
		thisTLVWA.addAttribute(tlAttr1);
		thisTLVWA.setName(vName + "noParent");
		VWA_Node thisVWA = new VWA_Node(thisTLVWA);
		ln.addMember(thisVWA); // needed to set context on eq/ex

		// When - eq and ex set on vwa
		thisVWA.getExampleHandler().set(Ex1, null);
		thisVWA.getEquivalentHandler().set(Eq1, null);
		// Then - can get value
		assertTrue("Must have same example.", thisVWA.getExample(null).equals(Ex1));
		assertTrue("Must have same equivalent.", thisVWA.getEquivalent(null).equals(Eq1));

		// Test each child
		for (Node n : thisVWA.getFacet_Attributes().getChildren()) {
			n.getExampleHandler().set(Ex1, null);
			n.getEquivalentHandler().set(Eq1, null);
			// Then - can get value
			assertTrue("Must have same example.", ((PropertyNode) n).getExample(null).equals(Ex1));
			assertTrue("Must have same equivalent.", ((PropertyNode) n).getEquivalent(null).equals(Eq1));
		}
	}

	// @Test
	// public void mockVWATest() {
	//
	//
	// ln = ml.createNewLibrary("http://opentravel.org/test", "test", defaultProject);
	// ln.setEditable(true);
	// VWA_Node vwa = ml.addVWA_ToLibrary(ln, "VWA_Test");
	// Assert.assertEquals("VWA_Test", vwa.getName());
	// Assert.assertTrue(vwa.getSimpleFacet() instanceof SimpleFacetNode);
	// SimpleFacetNode sfn = vwa.getSimpleFacet();
	// Assert.assertTrue(vwa.getAssignedType() != null);
	// Assert.assertTrue(sfn.getSimpleAttribute().getType() == vwa.getAssignedType());

	// TypeProvider aType = (TypeProvider) NodeFinders.findNodeByName("date", ModelNode.XSD_NAMESPACE);
	// Assert.assertTrue(vwa.setAssignedType(aType));
	// Assert.assertTrue(sfn.setAssignedType(aType));
	// Assert.assertTrue(vwa.setSimpleType(aType));
	// Assert.assertTrue(vwa.getAssignedType() == aType);

	// // 2/22/2015 dmh - vwa not allowed as type of simple type. Should it?
	// String OTA_NS = "http://opentravel.org/common/v02";
	// Node oType = NodeFinders.findNodeByName("CodeList", OTA_NS);
	// }

	/**
	 * Check the structure of the passed VWA
	 */
	public void checkVWA(VWA_Node vwa) {

		// Make sure named structures are present
		assertTrue(vwa.getFacet_Simple() != null);
		assertTrue(vwa.getSimpleAttribute() != null);
		assertTrue(vwa.getFacet_Attributes() != null);

		// Make sure there are libraries assigned to all
		assertTrue(vwa.getLibrary() != null);
		assertTrue(vwa.getFacet_Simple().getLibrary() != null);
		assertTrue(vwa.getSimpleAttribute().getLibrary() != null);
		assertTrue(vwa.getFacet_Attributes().getLibrary() != null);

		// SimpleType
		if (vwa.getAssignedType() == null)
			LOGGER.debug("Null Simple Type on " + vwa);
		assertTrue(vwa.getAssignedType() != null);
		// assertTrue(vwa.getSimpleAttribute().getType() == vwa.getAssignedType());

		// Attribute Facet
		for (Node ap : vwa.getFacet_Attributes().getChildren()) {
			assert ap instanceof PropertyNode;
			assert Node.GetNode(ap.getTLModelObject()) == ap;
			if (((PropertyNode) ap).getAssignedType() == null)
				LOGGER.debug("Null must not be attribute type.");
			assertTrue(((PropertyNode) ap).getAssignedType() != null);
			assert ap.getLibrary() == vwa.getLibrary();
		}

	}

	@Test
	public void VWA_FactoryTest() {
		// Given - an editable library
		ln = ml.createNewLibrary("http://opentravel.org/test", "test", defaultProject);
		ln.setEditable(true);
		String name = "Vwa1";
		assertTrue(sType.getTLModelObject() != null);

		// Given - a tlVWA has 100 properties
		TLValueWithAttributes tlVWA = new TLValueWithAttributes();
		tlVWA.setName(name);
		for (int attCnt = 1; attCnt < 100; attCnt++) {
			TLAttribute tlA = new TLAttribute();
			tlA.setName(name + "_a" + attCnt);
			tlVWA.addAttribute(tlA);
			tlA.setType((TLPropertyType) sType.getTLModelObject());
		}

		// When - the factory is used to create a node
		VWA_Node v = (VWA_Node) NodeFactory.newLibraryMember(tlVWA);
		ln.addMember(v);
		checkVWA(v);
	}
}
