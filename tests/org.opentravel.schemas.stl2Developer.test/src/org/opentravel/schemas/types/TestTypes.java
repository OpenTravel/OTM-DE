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
package org.opentravel.schemas.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemacompiler.codegen.example.ExampleBuilder;
import org.opentravel.schemacompiler.codegen.example.ExampleDocumentBuilder;
import org.opentravel.schemacompiler.codegen.example.ExampleGeneratorOptions;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAttributeType;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemas.controllers.DefaultProjectController;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.node.AliasNode;
import org.opentravel.schemas.node.BusinessObjectNode;
import org.opentravel.schemas.node.ComponentNode;
import org.opentravel.schemas.node.CoreObjectNode;
import org.opentravel.schemas.node.ImpliedNode;
import org.opentravel.schemas.node.LibraryChainNode;
import org.opentravel.schemas.node.LibraryNode;
import org.opentravel.schemas.node.LibraryTests;
import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeFinders;
import org.opentravel.schemas.node.ProjectNode;
import org.opentravel.schemas.node.SimpleTypeNode;
import org.opentravel.schemas.node.VWA_Node;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.properties.AttributeNode;
import org.opentravel.schemas.node.properties.ElementReferenceNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.properties.SimpleAttributeNode;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.testUtils.MockLibrary;
import org.opentravel.schemas.testUtils.NodeTesters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * @author Dave Hollander
 * 
 */
public class TestTypes {
	private static final Logger LOGGER = LoggerFactory.getLogger(TestTypes.class);

	NodeTesters nt = new NodeTesters();
	LoadFiles lf = new LoadFiles();
	LibraryTests lt = new LibraryTests();
	MockLibrary ml = null;
	LibraryNode ln = null;
	MainController mc;
	DefaultProjectController pc;
	ProjectNode defaultProject;

	@Before
	public void beforeAllTests() {
		mc = new MainController();
		ml = new MockLibrary();
		pc = (DefaultProjectController) mc.getProjectController();
		defaultProject = pc.getDefaultProject();
	}

	@Test
	public void listenerTest() {
		ln = ml.createNewLibrary("http://www.test.com/test1", "test1", defaultProject);
		LibraryNode ln_inChain = ml.createNewLibrary("http://www.test.com/test1c", "test1c", defaultProject);
		LibraryChainNode lcn = new LibraryChainNode(ln_inChain);
		ln_inChain.setEditable(true);

		Node type1 = NodeFinders.findNodeByName("string", Node.XSD_NAMESPACE);
		TLModelElement tlType1 = type1.getTLModelObject();
		assertNotNull(type1);
		assertNotNull(tlType1);
		assertTrue(tlType1 instanceof TLAttributeType);
		Node type2 = NodeFinders.findNodeByName("date", Node.XSD_NAMESPACE);
		Node type3 = NodeFinders.findNodeByName("decimal", Node.XSD_NAMESPACE);
		assertNotNull(type2);
		assertNotNull(type3);

		// Assign type via tlModel to simple Not in a chain
		SimpleTypeNode s1 = ml.addSimpleTypeToLibrary(ln, "s3");
		TLModelElement tlS1 = s1.getTLModelObject();
		if (tlS1 instanceof TLSimple)
			((TLSimple) tlS1).setParentType((TLAttributeType) tlType1);
		assertEquals(type1, s1.getAssignedType());

		// Not in a chain
		SimpleTypeNode s2 = ml.addSimpleTypeToLibrary(ln, "s1");
		assertNotNull(s2);
		assertEquals(1, s2.getTLModelObject().getListeners().size());
		s2.setAssignedType(type1);
		assertEquals(type1, s2.getAssignedType());

		// In a chain (has version node)
		SimpleTypeNode v2 = ml.addSimpleTypeToLibrary(ln_inChain, "s2");
		assertNotNull(v2);
		v2.setAssignedType(type1);
		assertEquals(type1, v2.getAssignedType());
		assertEquals(1, v2.getTLModelObject().getListeners().size());

		// Simple facet of a VWA
		VWA_Node vwa = ml.addVWA_ToLibrary(ln, "Vwa1");
		vwa.setAssignedType(type1);
		Node at = vwa.getSimpleFacet().getAssignedType();
		assertEquals(type1, at);
		vwa.getSimpleFacet().setAssignedType(type2);
		assertEquals(type2, vwa.getAssignedType());
		assertEquals(1, vwa.getTLModelObject().getListeners().size());
		assertEquals(1, vwa.getSimpleFacet().getTLModelObject().getListeners().size());
		// attribute of a vwa
		AttributeNode attr = (AttributeNode) vwa.getAttributeFacet().getChildren().get(0);
		assertNotNull(attr);
		assertEquals(1, attr.getTLModelObject().getListeners().size());
		attr.setAssignedType(s2);
		assertEquals(s2, attr.getAssignedType());

		// Simple facet of a core object
		CoreObjectNode core = ml.addCoreObjectToLibrary(ln, "Core1");
		core.setAssignedType(s1);
		assertEquals(s1, core.getSimpleType());
		core.setSimpleType(s2);
		assertEquals(s2, core.getSimpleType());
		assertEquals(1, core.getSimpleFacet().getTLModelObject().getListeners().size());
		PropertyNode p1 = (PropertyNode) core.getSummaryFacet().getChildren().get(0);
		p1.setAssignedType(s1);
		assertEquals(s1, p1.getAssignedType());
		p1.setAssignedType(v2); // a versioned node
		assertEquals(v2, p1.getAssignedType());

		// Test with both property and type in versioned libraries.
		BusinessObjectNode bo = ml.addBusinessObjectToLibrary(ln_inChain, "Bo1");
		p1 = (PropertyNode) bo.getSummaryFacet().getChildren().get(0);
		p1.setAssignedType(s1);
		assertEquals(s1, p1.getAssignedType());
		p1.setAssignedType(v2); // a versioned node
		assertEquals(v2, p1.getAssignedType());

		// Test with aliases as types
		AliasNode a1 = new AliasNode(bo, "A4bo1");
		p1.setAssignedType(a1);
		assertEquals(a1, p1.getAssignedType());

		ElementReferenceNode newProp = new ElementReferenceNode(bo.getSummaryFacet(), "TestSum");
		newProp.setAssignedType(bo);

		// Test known bad assignments
		assertFalse(attr.setAssignedType(bo));
		// FIXME - assertFalse(vwa.setAssignedType(bo));
		// FIXME - assertFalse(core.setAssignedType(bo));
	}

	@Test
	public void checkTypes() throws Exception {
		lf.loadFile1(mc);
		ln = lf.loadFile5(mc);

		for (Node n : Node.getAllUserLibraries()) {
			visitAllNodes(n);
		}
		testSettingType();

		// 9 is if you do not get the family owned types, 17 if you do.
		Assert.assertEquals(9, testSimples(ln));

		lf.loadFile3(mc);
		lf.loadFile4(mc);
		lf.loadFile2(mc);

		for (Node n : Node.getAllLibraries()) {
			visitAllNodes(n);
		}

	}

	@Test
	public void testsetAssignedTypeForThisNode() {
		String ns = "http://example.com/test";
		boolean ret = false;
		Node prop;
		LOGGER.debug("TEST being run -- testsetAssignedTypeForThisNode");

		ln = ml.createNewLibrary(ns, "LIB", null);
		BusinessObjectNode bo = ml.addBusinessObjectToLibrary(ln, "BO");

		ret = bo.getTypeClass().setAssignedTypeForThisNode(bo);
		Assert.assertFalse(ret); // should fail since BO is not type user.

		// Test using the finders.
		prop = bo.getIDFacet().getChildren().get(0);
		// ret = prop.getTypeClass().setAssignedTypeForThisNode();
		// Assert.assertTrue(ret);

		// Test using TypeResolver map.
		TypeResolver tr = new TypeResolver();
		prop = bo.getSummaryFacet().getChildren().get(0);
		Assert.assertNotNull(prop);
		ret = prop.getTypeClass().setAssignedTypeForThisNode(prop, tr.getProviderMap());
		Assert.assertTrue(ret);

		String simpleName = "simple";
		prop = ml.addSimpleTypeToLibrary(ln, simpleName);
		ret = prop.getTypeClass().setAssignedTypeForThisNode(prop, tr.getProviderMap());
		Assert.assertTrue(ret);

		// Make sure GUI can access types
		Assert.assertFalse(prop.getTypeName().equals(simpleName)); // properties view
		Assert.assertTrue(prop.getTypeName().equals("int")); // properties view
		Assert.assertFalse(prop.getTypeNameWithPrefix().isEmpty()); // facet view
		Assert.assertFalse(prop.getAssignedType() instanceof ImpliedNode);

	}

	public int testSimples(LibraryNode ln) {
		int simpleCnt = 0;
		for (Node sn : ln.getSimpleRoot().getChildren()) {
			if (sn.isSimpleType()) {
				simpleCnt++;
				Assert.assertNotNull(sn.getAssignedType());
				if (sn.getAssignedType() instanceof ImpliedNode) {
					boolean x = sn.getAssignedType() instanceof ImpliedNode;
				}
				Assert.assertFalse(sn.getAssignedType() instanceof ImpliedNode);
			}
		}
		return simpleCnt;
	}

	private void testSettingType() {
		final String testNS = "http://www.sabre.com/ns/OTA2/Demo/Profile/v01";
		Node typeToAssign = NodeFinders.findNodeByName("String_Long", testNS);
		Assert.assertNotNull(typeToAssign);
		Node tn = NodeFinders.findNodeByName("EmploymentZZZ", testNS);
		tn.getLibrary().setEditable(true);
		Assert.assertNotNull(tn);
		int usrCnt = ((ComponentNode) typeToAssign).getTypeUsersCount();
		// 8 summary properties

		for (INode facet : tn.getChildren()) {
			for (Node property : facet.getChildren()) {
				if (property.isTypeUser()) {
					if (property instanceof SimpleAttributeNode)
						property.setAssignedType(typeToAssign);
					Assert.assertTrue(property.setAssignedType(typeToAssign));
					if (typeToAssign != property.getAssignedType())
						LOGGER.debug("Assignment Error on " + property);
					Assert.assertEquals(typeToAssign, property.getAssignedType());

					if (typeToAssign.getTLModelObject() != property.getTLTypeObject()) {
						NamedEntity x = property.getTLTypeObject();
						LOGGER.debug("Assigned TL type does not match typeNode assignment.");
					}
					Assert.assertEquals(typeToAssign.getTLModelObject(), property.getTLTypeObject());

					Assert.assertEquals(((ComponentNode) typeToAssign).getTypeUsersCount(), ++usrCnt);
				}
			}
		}
	}

	/**
	 * Test the type providers and assure where used and owner. Test type users and assure getType returns valid node.
	 * 
	 * @param n
	 */
	public void visitAllNodes(Node n) {
		visitTypeNode(n);
		for (Node c : n.getChildren())
			visitAllNodes(c);
	}

	public void GenExampleAndValidate(Node cn) {
		ValidationFindings findings = null;
		final ExampleBuilder<Document> exampleBuilder =
				new ExampleDocumentBuilder(new ExampleGeneratorOptions());
		String xml = "";
		int errorCount = 0;

		// Make sure we can create findings.
		findings = cn.validate();
		errorCount = findings.getFindingsAsList(FindingType.ERROR).size();
		// LOGGER.debug("Validation Error count for " + n + " = " +
		// errorCount);

		// Make sure we can create examples for types without errors.
		xml = cn.compileExampleXML(errorCount > 0);
		if (errorCount < 1)
			Assert.assertFalse(xml.endsWith("ERROR"));
		else
			Assert.assertTrue(xml.endsWith("ERROR"));

	}

	/**
	 * JUnit test to validate the contents of the Type assignments within the node. TypeProvider and TypeUser specific
	 * <b>assertions</b> are made. Generates Examples and runs compiler validation. <b>Very cpu intensive!</b>
	 * 
	 * {@link NodeTesters#visit(Node)} node based test.
	 * 
	 * @param n
	 */
	public void visitTypeNode(Node n) {
		ValidationFindings findings = null;
		final ExampleBuilder<Document> exampleBuilder =
				new ExampleDocumentBuilder(new ExampleGeneratorOptions());
		String xml = "";
		int errorCount = 0;
		if (n == null)
			return;

		if (n.getParent() == null)
			n.getParent();
		Assert.assertNotNull(n.getParent());
		if (!n.isLibraryContainer()) {
			if (n.getLibrary() == null)
				LOGGER.debug("Null library in " + n);
			Assert.assertNotNull(n.getLibrary());
		}
		Assert.assertNotNull(n.getComponentType());
		Assert.assertFalse(n.getComponentType().isEmpty());

		if (n.isTypeProvider()) {
			ComponentNode cn = (ComponentNode) n;
			Assert.assertNotNull(cn.getWhereUsed());
			Assert.assertNotNull(cn.getTypeOwner());
			Assert.assertFalse(cn.getTypeOwner().getName().isEmpty());
			Assert.assertFalse(cn.getTypeOwner().getNamespace().isEmpty());
		}

		if (n.isTypeUser()) {
			ComponentNode cn = (ComponentNode) n;
			if (cn.getType() == null)
				LOGGER.debug("FIXME - Null type: " + cn);
			else {
				// Why do you get null types? Maybe the library is not editable.
				// LOGGER.debug("Testing " + cn.getLibrary() + "-" + cn + " of type \t"
				// + cn.getType().getClass().getSimpleName() + ":" + cn.getType());
				Assert.assertNotNull(cn.getType());
				Assert.assertFalse(cn.getType().getName().isEmpty());
				if (cn.getType().getNamespace().isEmpty())
					LOGGER.warn("Namespace is empty for " + cn);
				Assert.assertFalse(cn.getType().getNamespace().isEmpty());
			}
		}
	}

}
