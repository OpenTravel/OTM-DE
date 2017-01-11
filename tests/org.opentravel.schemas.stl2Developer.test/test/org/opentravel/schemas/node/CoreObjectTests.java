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
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLPropertyType;
import org.opentravel.schemas.controllers.DefaultProjectController;
import org.opentravel.schemas.controllers.MainController;
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
public class CoreObjectTests {
	ModelNode model = null;
	MainController mc;
	DefaultProjectController pc;
	ProjectNode defaultProject;
	TestNode tn = new NodeTesters().new TestNode();
	LoadFiles lf = new LoadFiles();
	MockLibrary ml = new MockLibrary();
	LibraryChainNode lcn = null;
	LibraryNode ln = null;

	@Before
	public void beforeEachTest() {
		mc = new MainController();
		// ml = new MockLibrary();
		pc = (DefaultProjectController) mc.getProjectController();
		defaultProject = pc.getDefaultProject();
		ln = ml.createNewLibrary("http://test.com", "CoreTest", defaultProject);
		ln.setEditable(true);
	}

	/**
	 * constructor tests
	 */
	@Test
	public void Core_ConstructorTests() {
		// Given - tl core object
		TLCoreObject tlc = buildTLCoreObject("TestCore1");
		CoreObjectNode core1 = new CoreObjectNode(tlc);
		ln.addMember(core1);
		checkCore(core1);
		if (!core1.isValid())
			ml.printValidationFindings(core1);

		// Given - business object
		BusinessObjectNode bo = ml.addBusinessObjectToLibrary(ln, "TestBO2");
		CoreObjectNode core2 = new CoreObjectNode(bo);
		bo.removeFromLibrary();
		checkCore(core2);

		// Given - vwa
		VWA_Node vwa = ml.addVWA_ToLibrary(ln, "TestVWA");
		CoreObjectNode core3 = new CoreObjectNode(vwa);
		vwa.removeFromLibrary();
		checkCore(core3);

		// check mock library
		CoreObjectNode core4 = ml.addCoreObjectToLibrary(ln, "Tc");
		checkCore(core4);
	}

	/**
	 * factory tests
	 */
	@Test
	public void Core_FactoryTests() {
		CoreObjectNode newNode = (CoreObjectNode) NodeFactory.newComponent(buildTLCoreObject("test2"));
		ln.addMember(newNode);
		checkCore(newNode);
	}

	/**
	 * load from library tests
	 * 
	 * @throws Exception
	 */
	@Test
	public void Core_FileLoadTests() throws Exception {
		lf.loadTestGroupA(mc);
		mc.getModelNode();
		for (LibraryNode lib : Node.getAllUserLibraries())
			for (Node n : lib.getDescendants_LibraryMembers())
				if (n instanceof CoreObjectNode)
					checkCore((CoreObjectNode) n);
	}

	/**
	 * assigned type tests
	 */
	@Test
	public void Core_TypeAssignmentTests() {
		// Given - a core object
		CoreObjectNode core = ml.addCoreObjectToLibrary(ln, "CoreTest");
		TypeProvider aType = (TypeProvider) NodeFinders.findNodeByName("date", ModelNode.XSD_NAMESPACE);
		TypeProvider bType = core.getSimpleType(); // string from mock library

		// Then
		assertTrue("Type must be same from Core and simple attribute methods.",
				core.getSimpleAttribute().getType() == core.getSimpleType());

		// When - set with simple attribute
		assertTrue("Assigning type must return true. ", core.getSimpleAttribute().setAssignedType(aType));
		assertTrue("Type must be as assigned.", core.getSimpleType() == aType);
		// When - set with core method
		Assert.assertTrue(core.setSimpleType(bType));
		Assert.assertTrue("Type must be as assigned.", core.getSimpleType() == bType);
	}

	@Test
	public void Core_ExtensionTests() {
		ProjectNode proj = mc.getProjectController().getDefaultProject();
		assertNotNull("Null project", proj);
		ln = lf.loadFile4(mc);
		lcn = new LibraryChainNode(ln); // Test in managed library
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
	public void Core_changeToTests() {
		// The change to method adds swap() to the constructor generated core.
		//
		CoreObjectNode tco = null;
		// Given - a chain with BO and VWA
		lcn = new LibraryChainNode(ln); // Test in a chain
		ln.setEditable(true);
		BusinessObjectNode bo = ml.addBusinessObjectToLibrary(ln, "bo");
		VWA_Node vwa = ml.addVWA_ToLibrary(ln, "vwa");
		// Given - an element to assign types to
		BusinessObjectNode typeUser = ml.addBusinessObjectToLibrary(ln, "userBO");
		ElementNode ele = new ElementNode(typeUser.getSummaryFacet(), "EleUser");
		// Given - the number of library members in library (must not change)
		int typeCount = ln.getDescendants_LibraryMembers().size();

		// Given - an element assigned the bo as a type
		ele.setAssignedType(bo);
		// When - changed to core
		tco = (CoreObjectNode) bo.changeToCoreObject();
		// Then - the core is valid and element is assigned the core
		checkCore(tco);
		assertTrue(bo.getLibrary() != ln);
		assertTrue("Type assignment must be to the new core.", ele.getAssignedType() == tco);
		assertTrue("New core must have element in where used list.", tco.getWhereAssigned().contains(ele));

		// Repeat with VWA
		ele.setAssignedType(vwa);
		tco = (CoreObjectNode) vwa.changeToCoreObject();
		checkCore(tco);
		assertTrue(vwa.getLibrary() != ln);
		assertTrue("Type assignment must be to the new core.", ele.getAssignedType() == tco);

		tn.visit(ln);
		assertTrue("Number of library members must be same as before changes.", typeCount == ln
				.getDescendants_LibraryMembers().size());
	}

	@Test
	public void Core_NameChangeTests() {
		// On name change, all users of the BO and its aliases and facets also need to change.

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

	public TLCoreObject buildTLCoreObject(String name) {
		if (name == null || name.isEmpty())
			name = "TestCore";
		TypeProvider type = ((TypeProvider) NodeFinders.findNodeByName("string", ModelNode.XSD_NAMESPACE));
		NamedEntity tlType = (NamedEntity) type.getTLModelObject();

		// Create tl core, set name and type
		TLCoreObject tlc = new TLCoreObject();
		tlc.setName(name);
		tlc.getSimpleFacet().setSimpleType(tlType);

		// Add attribute and indicator properties
		TLAttribute tla = new TLAttribute();
		tla.setName(name + "Attr1");
		tla.setType((TLPropertyType) tlType);
		TLIndicator tli = new TLIndicator();
		tli.setName(name + "Ind1");
		tlc.getSummaryFacet().addAttribute(tla);
		tlc.getSummaryFacet().addIndicator(tli);
		return tlc;
	}

	/**
	 * checkCore - all tests to be used in these tests and by other junits
	 */
	public void checkCore(CoreObjectNode core) {
		assertTrue(core.getLibrary() != null);

		assertTrue("Core must have at least 6 children.", core.getChildren().size() >= 6);

		// Facets
		ml.checkObject(core.getSimpleFacet());
		ml.checkObject(core.getSummaryFacet());
		ml.checkObject(core.getDetailFacet());
		ml.checkObject(core.getSimpleListFacet());
		ml.checkObject(core.getDetailListFacet());
		ml.checkObject(core.getRoleFacet());
	}
}
