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
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemas.controllers.DefaultProjectController;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.modelObject.TLnSimpleAttribute;
import org.opentravel.schemas.node.facets.SimpleFacetNode;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.properties.SimpleAttributeNode;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.testUtils.MockLibrary;
import org.opentravel.schemas.testUtils.NodeTesters;
import org.opentravel.schemas.testUtils.NodeTesters.TestNode;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeUser;

/**
 * @author Dave Hollander
 * 
 */
public class VWA_Tests {
	ModelNode model = null;
	MockLibrary mockLibrary = null;
	LibraryNode ln = null;
	MainController mc;
	DefaultProjectController pc;
	ProjectNode defaultProject;
	TestNode tn = new NodeTesters().new TestNode();

	@Before
	public void beforeEachTest() {
		mc = new MainController();
		mockLibrary = new MockLibrary();
		pc = (DefaultProjectController) mc.getProjectController();
		defaultProject = pc.getDefaultProject();
	}

	@Test
	public void changeToVWA() {
		MockLibrary ml = new MockLibrary();
		// MainController mc = new MainController();
		// DefaultProjectController pc = (DefaultProjectController) mc.getProjectController();
		// ProjectNode defaultProject = pc.getDefaultProject();

		LibraryNode ln = ml.createNewLibrary(defaultProject.getNSRoot(), "test", defaultProject);
		BusinessObjectNode bo = ml.addBusinessObjectToLibrary(ln, "bo");
		CoreObjectNode core = ml.addCoreObjectToLibrary(ln, "co");
		VWA_Node tVwa = null, vwa = ml.addVWA_ToLibrary(ln, "vwa");
		int typeCount = ln.getDescendants_LibraryMembers().size();

		tVwa = (VWA_Node) core.changeToVWA();
		checkVWA(tVwa);
		tVwa = (VWA_Node) vwa.changeToVWA();
		checkVWA(tVwa);

		tn.visit(ln);
		Assert.assertEquals(typeCount, ln.getDescendants_LibraryMembers().size());
	}

	@Test
	public void loadLibraryTests() throws Exception {
		MainController mc = new MainController();
		LoadFiles lf = new LoadFiles();
		model = mc.getModelNode();

		// test the lib with only vwa
		LibraryNode vwaLib = lf.loadFile3(mc);
		for (Node vwa : vwaLib.getDescendants_LibraryMembers()) {
			Assert.assertTrue(vwa instanceof VWA_Node);
			checkVWA((VWA_Node) vwa);
		}
		mc.getProjectController().remove(vwaLib.getLibraryNavNode());
		// vwaLib.close();

		// test all libs
		lf.loadTestGroupA(mc);
		for (LibraryNode ln : model.getUserLibraries()) {
			List<Node> types = ln.getDescendants_LibraryMembers();
			for (Node n : types) {
				if (n instanceof VWA_Node)
					checkVWA((VWA_Node) n);
			}
		}
		// make sure we can build one.
		makeVwa("TEST_V1", vwaLib);
	}

	// @Test
	// public void getTypeQName() {
	// ln = mockLibrary.createNewLibrary("http://sabre.com/test", "test", defaultProject);
	// ln.setEditable(true);
	// VWA_Node vwa = mockLibrary.addVWA_ToLibrary(ln, "VWA_Test");
	// // QName typeQname = vwa.getTLTypeQName();
	// SimpleAttributeNode sa = (SimpleAttributeNode) vwa.getSimpleFacet().getSimpleAttribute();
	// Assert.assertNotNull(sa);
	//
	// TypeProvider aType = (TypeProvider) NodeFinders.findNodeByName("date", Node.XSD_NAMESPACE);
	// vwa.setSimpleType(aType);
	// // typeQname = sa.getTLTypeQName();
	// // Assert.assertEquals("date", typeQname.getLocalPart());
	// // Assert.assertEquals(Node.XSD_NAMESPACE, typeQname.getNamespaceURI());
	// }

	@Test
	public void typeSetting() {
		// Given - an unmanaged and managed library and 3 simple types
		ln = mockLibrary.createNewLibrary("http://sabre.com/test", "test", defaultProject);
		LibraryChainNode lcn = mockLibrary.createNewManagedLibrary("inChain", defaultProject);
		TypeProvider aType = (TypeProvider) NodeFinders.findNodeByName("date", ModelNode.XSD_NAMESPACE);
		TypeProvider bType = (TypeProvider) NodeFinders.findNodeByName("int", ModelNode.XSD_NAMESPACE);
		TypeProvider cType = (TypeProvider) NodeFinders.findNodeByName("string", ModelNode.XSD_NAMESPACE);
		assertTrue("Unmanaged library is editable.", ln.isEditable());
		assertTrue("Managed library is editable.", lcn.isEditable());
		assertTrue("Simple type A must not be null.", aType != null);

		// Given - a new VWA in the unmanaged library
		VWA_Node vwa = mockLibrary.addVWA_ToLibrary(ln, "VWA_Test");

		// When - simple type is set
		assertTrue("Simple type must be assigned.", vwa.getSimpleFacet().getSimpleAttribute().setAssignedType(cType));
		// Then
		assertTrue("Simple type must equal type assigned.", cType == vwa.getSimpleFacet().getSimpleAttribute()
				.getAssignedType());

		// When - a new VWA in managed library is created and type set
		vwa = mockLibrary.addVWA_ToLibrary(lcn.getHead(), "InChainTest");
		assertTrue("Simple type must be assignable.", vwa.getSimpleFacet().getSimpleAttribute().setAssignedType(bType));
		// Then
		assertTrue("Simple type must equal type assigned.", bType == vwa.getSimpleFacet().getSimpleAttribute()
				.getAssignedType());

		// Given - the tlModelObject from simple type B
		TLModelElement target = bType.getTLModelObject();

		// NamedEntity v1 = vwa.getTLTypeObject();
		TLValueWithAttributes t1 = (TLValueWithAttributes) vwa.getTLModelObject();
		TLAttributeType a1 = t1.getParentType(); // null

		// Test accessing the simple facet via TL objects
		SimpleFacetNode sf = vwa.getSimpleFacet();
		assertTrue("Simple facet must not be null.", sf != null);

		// Then - access via simple facet
		TLModelElement v2 = sf.getSimpleType().getTLModelObject();
		assertTrue("Simple facet's simple type must be set to bType.", v2 == target);

		// Then - access via simple attribute
		SimpleAttributeNode sa = (SimpleAttributeNode) sf.getSimpleAttribute();
		TLModelElement tlo = sa.getTLModelObject();

		// Then - access via simple attribute's Model object and TL object
		NamedEntity v3 = sa.getAssignedTLNamedEntity();
		NamedEntity m3 = sa.getModelObject().getTLType();
		// 7/2016 - returns the xsd simple not the TLSimple
		// assertTrue("Assigned TL Named Entity getter must return target.", v3 == target);
		// assertTrue("Model object TLType getter must return target.", m3 == target);

		// Test parent type
		// Test via tl Model Object
		((TLValueWithAttributes) vwa.getTLModelObject()).setParentType((TLAttributeType) target);
		TLAttributeType p1 = ((TLValueWithAttributes) vwa.getTLModelObject()).getParentType();
		assertTrue(p1 == target);
		// Test via TLnSimpleAttribute
		NamedEntity target2 = (NamedEntity) aType.getTLModelObject();
		((TLnSimpleAttribute) tlo).setType(target2);
		NamedEntity p2 = ((TLnSimpleAttribute) tlo).getType();
		assertTrue(p2 == target2);
	}

	@Test
	public void VWA_EqEx_Tests() {
		// Given a library and a Simple Type
		ln = mockLibrary.createNewLibrary("http://opentravel.org/test", "test", defaultProject);
		ln.setEditable(true);
		TypeProvider emptyNode = (TypeProvider) ModelNode.getEmptyNode();
		TypeProvider sType = (TypeProvider) NodeFinders.findNodeByName("date", ModelNode.XSD_NAMESPACE);
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
		for (Node n : thisVWA.getAttributeFacet().getChildren()) {
			n.getExampleHandler().set(Ex1, null);
			n.getEquivalentHandler().set(Eq1, null);
			// Then - can get value
			assertTrue("Must have same example.", ((PropertyNode) n).getExample(null).equals(Ex1));
			assertTrue("Must have same equivalent.", ((PropertyNode) n).getEquivalent(null).equals(Eq1));
		}
	}

	@Test
	public void VWA_ConstructorsTests() {
		// Given a library and a Simple Type
		ln = mockLibrary.createNewLibrary("http://opentravel.org/test", "test", defaultProject);
		ln.setEditable(true);
		TypeProvider emptyNode = (TypeProvider) ModelNode.getEmptyNode();
		TypeProvider sType = (TypeProvider) NodeFinders.findNodeByName("date", ModelNode.XSD_NAMESPACE);
		TLSimple tlsType = (TLSimple) sType.getTLModelObject();
		assertTrue("Found tlsType.", tlsType != null);
		String vName = "TestVWA";

		// Build 2 TLValueWithAttributes
		TLAttribute tlAttr1 = new TLAttribute();
		tlAttr1.setType(tlsType);
		tlAttr1.setName("attr1");

		TLValueWithAttributes tlVWAnoParent = new TLValueWithAttributes();
		tlVWAnoParent.addAttribute(tlAttr1);
		tlVWAnoParent.setName(vName + "noParent");

		TLValueWithAttributes tlVWAwithParent = new TLValueWithAttributes();
		tlVWAwithParent.addAttribute(tlAttr1);
		tlVWAwithParent.setName(vName + "withParent");
		tlVWAwithParent.setParentType(tlsType);
		assertTrue("tlVWA must have type set.", tlVWAwithParent.getParentType() == tlsType);

		// When - construct node from TL object
		VWA_Node nVwaNoParent = new VWA_Node(tlVWAnoParent);
		// Then - value access is recurring problem, test each step
		assertTrue("VWA must have name set.", nVwaNoParent.getName().startsWith(vName));
		assertTrue("VWA must have 1 child.", nVwaNoParent.getAttributeFacet().getChildren().size() == 1);
		assertTrue("VWA must NOT be a Type User", !(nVwaNoParent instanceof TypeUser));
		assertTrue("VWA must NOT have assigned type.", nVwaNoParent.getType() == null);
		// Then - test simple facet
		assertTrue("VWA must have simple facet.", nVwaNoParent.getSimpleFacet() != null);
		assertTrue("VWA simple facet must NOT be a Type User", !(nVwaNoParent.getSimpleFacet() instanceof TypeUser));
		assertTrue("VWA simple facet must NOT have assigned type.", nVwaNoParent.getSimpleFacet().getType() == null);
		// Then - test simple attribute
		assertTrue("VWA must have simple attribute.", nVwaNoParent.getSimpleAttribute() != null);
		assertTrue("VWA simple attribute must be a Type User", nVwaNoParent.getSimpleAttribute() instanceof TypeUser);
		// Node et = nVwaNoParent.getSimpleAttribute().getType();
		assertTrue("VWA simple attribute must have Empty as assigned type.", nVwaNoParent.getSimpleAttribute()
				.getType() == emptyNode);

		// When - construct node from TL object
		VWA_Node nVwawithParent = new VWA_Node(tlVWAwithParent);
		// Then
		assertTrue("VWA has name set.", nVwawithParent.getName().startsWith(vName));
		assertTrue("VWA has 1 child.", nVwawithParent.getAttributeFacet().getChildren().size() == 1);
		// Then - value access is recurring problem, test each step
		assertTrue("VWA is NOT a Type User", !(nVwawithParent instanceof TypeUser));
		assertTrue("VWA does NOT have assigned type.", nVwawithParent.getType() == null);
		assertTrue("VWA simple facet is NOT a Type User", !(nVwawithParent instanceof TypeUser));
		assertTrue("VWA simple facet does NOT have assigned type.", nVwawithParent.getSimpleFacet().getType() == null);
		assertTrue("VWA simple attribute is Type User", nVwawithParent.getSimpleAttribute() instanceof TypeUser);
		assertTrue("VWA simple attribute has  assigned type.", nVwawithParent.getSimpleAttribute().getType() == sType);

		// TODO - add tests for cast constructors (see changeTo)
	}

	@Test
	public void mockVWATest() {

		//
		ln = mockLibrary.createNewLibrary("http://opentravel.org/test", "test", defaultProject);
		ln.setEditable(true);
		VWA_Node vwa = mockLibrary.addVWA_ToLibrary(ln, "VWA_Test");
		Assert.assertEquals("VWA_Test", vwa.getName());
		Assert.assertTrue(vwa.getSimpleFacet() instanceof SimpleFacetNode);
		SimpleFacetNode sfn = vwa.getSimpleFacet();
		Assert.assertTrue(vwa.getSimpleType() != null);
		Assert.assertTrue(sfn.getSimpleAttribute().getType() == vwa.getSimpleType());

		TypeProvider aType = (TypeProvider) NodeFinders.findNodeByName("date", ModelNode.XSD_NAMESPACE);
		// Assert.assertTrue(vwa.setAssignedType(aType));
		// Assert.assertTrue(sfn.setAssignedType(aType));
		// Assert.assertTrue(vwa.setSimpleType(aType));
		// Assert.assertTrue(vwa.getSimpleType() == aType);

		// 2/22/2015 dmh - vwa not allowed as type of simple type. Should it?
		String OTA_NS = "http://opentravel.org/common/v02";
		Node oType = NodeFinders.findNodeByName("CodeList", OTA_NS);
	}

	private void checkVWA(VWA_Node vwa) {

		Assert.assertNotNull(vwa.getLibrary());

		// SimpleType
		// Assert.assertNotNull(vwa.getAssignedType());
		Assert.assertNotNull(vwa.getSimpleType());
		// Node a = vwa.getAssignedType();
		TypeProvider b = vwa.getSimpleType();
		// Assert.assertEquals(a, b);
		if (vwa.isEditable()) {
			TypeProvider a = (TypeProvider) NodeFinders.findNodeByName("decimal", ModelNode.XSD_NAMESPACE);
			vwa.setSimpleType(a);
			Assert.assertEquals(a, vwa.getSimpleType());
		}

		// Must have only two children
		Assert.assertTrue(vwa.getChildren().size() == 2);

		// Simple Facet
		Assert.assertNotNull(vwa.getSimpleFacet());
		Assert.assertTrue(vwa.getSimpleFacet().getChildren().size() == 1);

		// Attribute Facet
		Assert.assertNotNull(vwa.getAttributeFacet());
		for (Node ap : vwa.getAttributeFacet().getChildren()) {
			assert ap instanceof PropertyNode;
			assert Node.GetNode(ap.getTLModelObject()) == ap;
			Assert.assertNotNull(((PropertyNode) ap).getAssignedType());
			assert ap.getLibrary() == vwa.getLibrary();
		}

	}

	protected void makeVwa(String name, LibraryNode ln) {
		TLValueWithAttributes tlVWA = new TLValueWithAttributes();
		tlVWA.setName(name);
		for (int attCnt = 1; attCnt < 100; attCnt++) {
			TLAttribute tlA = new TLAttribute();
			tlA.setName(name + "_a" + attCnt);
			tlVWA.addAttribute(tlA);
		}

		VWA_Node v = (VWA_Node) NodeFactory.newComponent(tlVWA);
		ln.addMember(v);
		checkVWA(v);
	}
}
