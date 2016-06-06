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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLAttributeType;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemas.controllers.DefaultProjectController;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.modelObject.TLnSimpleAttribute;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.properties.SimpleAttributeNode;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.testUtils.MockLibrary;
import org.opentravel.schemas.testUtils.NodeTesters;
import org.opentravel.schemas.testUtils.NodeTesters.TestNode;
import org.opentravel.schemas.types.TypeProvider;

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
		int typeCount = ln.getDescendants_NamedTypes().size();

		tVwa = (VWA_Node) core.changeToVWA();
		checkVWA(tVwa);
		tVwa = (VWA_Node) vwa.changeToVWA();
		checkVWA(tVwa);

		tn.visit(ln);
		Assert.assertEquals(typeCount, ln.getDescendants_NamedTypes().size());
	}

	@Test
	public void loadLibraryTests() throws Exception {
		MainController thisModel = new MainController();
		LoadFiles lf = new LoadFiles();
		model = thisModel.getModelNode();

		// test the lib with only vwa
		LibraryNode vwaLib = lf.loadFile3(thisModel);
		for (Node vwa : vwaLib.getDescendants_NamedTypes()) {
			Assert.assertTrue(vwa instanceof VWA_Node);
			checkVWA((VWA_Node) vwa);
		}
		vwaLib.close();

		// test all libs
		lf.loadTestGroupA(thisModel);
		for (LibraryNode ln : model.getUserLibraries()) {
			List<Node> types = ln.getDescendants_NamedTypes();
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
		ln = mockLibrary.createNewLibrary("http://sabre.com/test", "test", defaultProject);
		LibraryChainNode lcn = mockLibrary.createNewManagedLibrary("inChain", defaultProject);
		ln.setEditable(true);
		TypeProvider aType = (TypeProvider) NodeFinders.findNodeByName("date", ModelNode.XSD_NAMESPACE);
		TypeProvider bType = (TypeProvider) NodeFinders.findNodeByName("int", ModelNode.XSD_NAMESPACE);
		TypeProvider cType = (TypeProvider) NodeFinders.findNodeByName("string", ModelNode.XSD_NAMESPACE);

		// Check explicitly set by code.
		VWA_Node vwa = mockLibrary.addVWA_ToLibrary(ln, "VWA_Test");

		// assertTrue(vwa.setAssignedType(aType));
		// assertTrue(aType == vwa.getAssignedType());
		// assertTrue(vwa.setAssignedType(cType));
		// assertTrue(cType == vwa.getSimpleType());
		// assertTrue(vwa.setSimpleType(aType));
		// assertTrue(aType == vwa.getAssignedType());
		//
		// // Test getters and setters on all 3 levels of hierarchy.
		// assertTrue(vwa.getSimpleFacet().setAssignedType(bType));
		// assertTrue(bType == vwa.getAssignedType());
		assertTrue(vwa.getSimpleFacet().getSimpleAttribute().setAssignedType(cType));
		// assertTrue(cType == vwa.getAssignedType());
		// assertTrue(cType == vwa.getSimpleFacet().getAssignedType());
		assertTrue(cType == vwa.getSimpleFacet().getSimpleAttribute().getAssignedType());

		// Test to assure that read in VWAs in a library chain are typed.
		vwa = mockLibrary.addVWA_ToLibrary(lcn.getHead(), "InChainTest");
		assertTrue(vwa.getSimpleFacet().getSimpleAttribute().setAssignedType(bType));
		// assertTrue(bType == vwa.getAssignedType());
		// assertTrue(bType == vwa.getSimpleFacet().getAssignedType());
		assertTrue(bType == vwa.getSimpleFacet().getSimpleAttribute().getAssignedType());

		// Test TL based access
		TLModelElement target = bType.getTLModelObject();

		NamedEntity v1 = vwa.getTLTypeObject();
		TLValueWithAttributes t1 = (TLValueWithAttributes) vwa.getTLModelObject();
		TLAttributeType a1 = t1.getParentType(); // null

		// Test accessing the simple facet via TL objects
		SimpleFacetNode sf = vwa.getSimpleFacet();
		assertNotNull(sf);
		TLModelElement v2 = sf.getSimpleType().getTLModelObject();
		// NamedEntity m2 = sf.getModelObject().getTLType();
		// These return null. The "parent type" on the tlVWA is the simple type.
		// NamedEntity a2 = ((TLSimpleFacet) sf.getTLModelObject()).getSimpleType();
		assertTrue(v2 == target);
		// assertTrue(a2 == target);
		// assertTrue(m2 == target);

		SimpleAttributeNode sa = (SimpleAttributeNode) sf.getSimpleAttribute();
		TLModelElement tlo = sa.getTLModelObject();
		// assertTrue(sa.getTLModelObject() == vwa.getTLModelObject());
		// NamedEntity v3 = sa.getTLTypeObject();
		NamedEntity v3 = sa.getAssignedTLNamedEntity();
		// ERROR - NamedEntity a3 = ((TLnSimpleAttribute) sa.getTLModelObject()).getType();
		NamedEntity m3 = sa.getModelObject().getTLType();
		assertTrue(v3 == target);
		// assertTrue(a3 == target);
		assertTrue(m3 == target);

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
		// Test via simpleFacet
		// sf.setAssignedType(cType);
		// NamedEntity p3 = sf.getTLTypeObject();
		// assertTrue(p3 == cType.getTLModelObject());
	}

	@Test
	public void mockVWATest() {
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
