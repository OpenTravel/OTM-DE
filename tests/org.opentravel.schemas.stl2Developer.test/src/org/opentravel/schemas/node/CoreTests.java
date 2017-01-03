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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemas.controllers.DefaultProjectController;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.modelObject.SimpleAttributeMO;
import org.opentravel.schemas.modelObject.SimpleFacetMO;
import org.opentravel.schemas.node.facets.SimpleFacetNode;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.properties.ElementNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.testUtils.MockLibrary;
import org.opentravel.schemas.testUtils.NodeTesters;
import org.opentravel.schemas.testUtils.NodeTesters.TestNode;
import org.opentravel.schemas.types.TypeProvider;

/**
 * @author Dave Hollander
 * 
 */
public class CoreTests {
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
	public void extendedCO() {
		MainController mc = new MainController();
		LoadFiles lf = new LoadFiles();
		MockLibrary ml = new MockLibrary();
		ProjectNode proj = mc.getProjectController().getDefaultProject();
		assertNotNull("Null project", proj);
		LibraryNode ln = lf.loadFile4(mc);
		LibraryChainNode lcn = new LibraryChainNode(ln); // Test in managed library
		ln.setEditable(true);

		LibraryNode ln2 = ml.createNewLibrary("http://test.com", "tl2", proj);
		LibraryChainNode lcn2 = new LibraryChainNode(ln2);
		ln2.setEditable(true);

		CoreObjectNode extendedCO = ml.addCoreObjectToLibrary(ln2, "ExtendedCO");
		assertNotNull("Null object created.", extendedCO);

		for (Node n : ln.getDescendants_LibraryMembers())
			if (n instanceof CoreObjectNode && n != extendedCO) {
				extendedCO.setExtension(n);
				checkCore((CoreObjectNode) n);
				checkCore(extendedCO);
			}

	}

	@Test
	public void changeToCore() {
		MockLibrary ml = new MockLibrary();
		MainController mc = new MainController();
		DefaultProjectController pc = (DefaultProjectController) mc.getProjectController();
		ProjectNode defaultProject = pc.getDefaultProject();

		LibraryNode ln = ml.createNewLibrary(defaultProject.getNSRoot(), "test", defaultProject);
		new LibraryChainNode(ln); // Test in a chain
		BusinessObjectNode bo = ml.addBusinessObjectToLibrary(ln, "bo");
		CoreObjectNode tco = null, core = ml.addCoreObjectToLibrary(ln, "co");
		VWA_Node vwa = ml.addVWA_ToLibrary(ln, "vwa");
		int typeCount = ln.getDescendants_LibraryMembers().size();

		tco = (CoreObjectNode) core.changeToCoreObject();
		checkCore(tco);
		tco = (CoreObjectNode) vwa.changeToCoreObject();
		checkCore(tco);

		tn.visit(ln);
		Assert.assertEquals(typeCount, ln.getDescendants_LibraryMembers().size());
	}

	@Test
	public void coreTest() throws Exception {
		MainController mc = new MainController();
		LoadFiles lf = new LoadFiles();
		// model = mc.getModelNode();

		LibraryNode coreLib = lf.loadFile4(mc);
		new LibraryChainNode(coreLib); // Test in a chain
		for (Node core : coreLib.getDescendants_LibraryMembers()) {
			if (core instanceof CoreObjectNode)
				checkCore((CoreObjectNode) core);
		}
	}

	@Test
	public void nameChange() {
		// On name change, all users of the BO and its aliases and facets also need to change.
		MockLibrary ml = new MockLibrary();
		MainController mc = new MainController();
		DefaultProjectController pc = (DefaultProjectController) mc.getProjectController();
		ProjectNode defaultProject = pc.getDefaultProject();
		LibraryNode ln = ml.createNewLibrary(defaultProject.getNSRoot(), "test", defaultProject);

		// Given - a Core Object with alias
		final String coreName = "initialcoreName";
		CoreObjectNode core = ml.addCoreObjectToLibrary_Empty(ln, coreName);
		AliasNode alias1 = core.addAlias("coreAlias");
		AliasNode aliasSummary = null;
		for (Node n : core.getSummaryFacet().getChildren())
			if (n instanceof AliasNode)
				aliasSummary = (AliasNode) n;
		// Then the alias must exist on the core and it's facet
		assertNotNull(alias1);
		assertNotNull(aliasSummary);

		// When - a core is created that has elements that use the core and aliases as properties
		CoreObjectNode elements = ml.addCoreObjectToLibrary(ln, "user");
		PropertyNode pcore = new ElementNode(elements.getSummaryFacet(), "p1", core);
		PropertyNode pAlias1 = new ElementNode(elements.getSummaryFacet(), "p2", alias1);
		PropertyNode pcoreSummary = new ElementNode(elements.getSummaryFacet(), "p3", core.getSummaryFacet());
		PropertyNode pcoreSumAlias = new ElementNode(elements.getSummaryFacet(), "p4", aliasSummary);

		// Then - the facet alias has where used
		assertTrue("Facet alias must be assigned as type.", !aliasSummary.getWhereAssigned().isEmpty());
		// Then - the elements are named after their type
		assertTrue("Element name must be the core name.", pcore.getName().equals(core.getName()));
		assertTrue("Element name must be alias name.", pAlias1.getName().contains(alias1.getName()));
		assertTrue("Element name must be facet name.", pcoreSummary.getName().equals(core.getSummaryFacet().getName()));
		assertTrue("Element name must start with core name.", pcoreSummary.getName().startsWith(core.getName()));
		assertTrue("Element name must start with alias name.", pcoreSumAlias.getName().startsWith(alias1.getName()));

		// When - Change the core name
		String changedName = "changedName";
		core.setName(changedName);
		changedName = NodeNameUtils.fixCoreObjectName(changedName); // get the "fixed" name

		// Then - the business object name and facets must change.
		assertTrue("Core Object name must be fixed name.", pcore.getName().equals(changedName));
		assertTrue("Alias name must be unchanged.", pAlias1.getName().equals(alias1.getName()));
		assertTrue("Facet name must start with core name.", pcoreSummary.getName().startsWith(changedName));
		// Then - the facet alias has where used
		assertTrue("Facet alias must be assigned as type.", !aliasSummary.getWhereAssigned().isEmpty());
		// Then - the elements are named after their type
		assertTrue("Element name must be the core name.", pcore.getName().equals(changedName));
		assertTrue("Element name must contain new core name.", pcoreSummary.getName().contains(changedName));
		assertTrue("Element name must start with core name.", pcoreSummary.getName().startsWith(changedName));
		assertTrue("Element name must start with alias name.", pcoreSumAlias.getName().startsWith(alias1.getName()));
		assertTrue("Element name must start with alias name.", pAlias1.getName().startsWith(alias1.getName()));

		// When - alias name changed
		String aliasName2 = "aliasName2";
		alias1.setName(aliasName2);
		aliasName2 = alias1.getName(); // get the "fixed" name

		// Then - all aliases on core must change name
		assertTrue("Alias Name must change.", pAlias1.getName().equals(aliasName2));
		assertTrue("Alias on summary facet must change.", aliasSummary.getName().startsWith(aliasName2));

		// Then - all type users of those aliases must change name
		assertTrue("Element name must start with changed alias name.", pcoreSumAlias.getName().startsWith(aliasName2));
		assertTrue("Element name must start with changed alias name.", pAlias1.getName().startsWith(aliasName2));
	}

	@Test
	public void mockCoreTest() {
		ln = mockLibrary.createNewLibrary("http://sabre.com/test", "test", defaultProject);
		new LibraryChainNode(ln); // Test in a chain
		CoreObjectNode core = mockLibrary.addCoreObjectToLibrary(ln, "CoreTest");
		Assert.assertEquals("CoreTest", core.getName());
		Assert.assertTrue(core.getSimpleFacet() instanceof SimpleFacetNode);
		SimpleFacetNode sfn = core.getSimpleFacet();
		Assert.assertTrue(core.getSimpleType() != null);
		Assert.assertTrue(sfn.getSimpleAttribute().getType() == core.getSimpleType());

		TypeProvider aType = (TypeProvider) NodeFinders.findNodeByName("date", ModelNode.XSD_NAMESPACE);
		Assert.assertTrue(core.getSimpleFacet().getSimpleAttribute().setAssignedType(aType));
		Assert.assertTrue(sfn.getSimpleAttribute().setAssignedType(aType));
		// works - Assert.assertTrue(sfn.getSimpleAttribute().setAssignedType(aType));
		Assert.assertTrue(core.setSimpleType(aType));
		Assert.assertTrue(core.getSimpleType() == aType);
	}

	private void checkCore(CoreObjectNode core) {
		Assert.assertNotNull(core.getLibrary());
		Assert.assertTrue(core instanceof CoreObjectNode);

		// must have 6 children
		Assert.assertEquals(6, core.getChildren().size());

		// Simple Facet (SimpleFacetMO)
		Assert.assertNotNull(core.getSimpleFacet());
		INode sf = core.getSimpleFacet();
		Assert.assertTrue(sf.getModelObject() instanceof SimpleFacetMO);

		// Owns one property of type SimpleAttributeMO
		Assert.assertTrue(core.getSimpleFacet().getChildren().size() == 1);
		Node sp = core.getSimpleFacet().getChildren().get(0);
		Assert.assertTrue(sp instanceof PropertyNode);
		Assert.assertTrue(sp.getModelObject() instanceof SimpleAttributeMO);
		Assert.assertTrue(sp.getType() != null);
		Assert.assertFalse(sp.getType().getName().isEmpty());
		// the simple facet and attribute share the type class ... either could be owner
		Assert.assertTrue(sp.getLibrary() == core.getLibrary());

		Assert.assertNotNull(core.getSimpleFacet());
		Assert.assertNotNull(core.getSummaryFacet());
		Assert.assertNotNull(core.getDetailFacet());

		for (Node property : core.getSummaryFacet().getChildren()) {
			Assert.assertTrue(property instanceof PropertyNode);
			Assert.assertTrue(property.getType() != null);
			// Assert.assertTrue(property.getTypeClass().getTypeOwner() == property);
			Assert.assertTrue(property.getLibrary() == core.getLibrary());
		}
		for (Node property : core.getDetailFacet().getChildren()) {
			Assert.assertTrue(property instanceof PropertyNode);
			Assert.assertTrue(property.getType() != null);
			Assert.assertFalse(property.getType().getName().isEmpty());
			Assert.assertTrue(property.getLibrary() == core.getLibrary());
		}

		Assert.assertNotNull(core.getSimpleListFacet());
		Assert.assertNotNull(core.getDetailListFacet());
	}
	// Problem = getType(IndicatorMO) is null
}
