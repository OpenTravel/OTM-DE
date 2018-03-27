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
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLPropertyType;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.properties.ElementNode;
import org.opentravel.schemas.node.typeProviders.AliasNode;
import org.opentravel.schemas.node.typeProviders.FacetProviderNode;
import org.opentravel.schemas.node.typeProviders.VWA_Node;
import org.opentravel.schemas.node.typeProviders.facetOwners.BusinessObjectNode;
import org.opentravel.schemas.node.typeProviders.facetOwners.CoreObjectNode;
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
public class CoreObjectTests extends BaseProjectTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(CoreObjectTests.class);

	ProjectNode defaultProject;
	LoadFiles lf = new LoadFiles();
	MockLibrary ml = new MockLibrary();
	LibraryChainNode lcn = null;
	LibraryNode ln = null;

	@Override
	@Before
	public void beforeEachTest() throws Exception {
		LOGGER.debug("***Before Core Object Tests ----------------------");
		// callBeforeEachTest();
		defaultProject = pc.getDefaultProject();
		ln = ml.createNewLibrary("http://test.com", "CoreTest", defaultProject);
		ln.setEditable(true);
	}

	/**
	 * constructor tests
	 * 
	 * @throws Exception
	 */
	@Test
	public void CO_ConstructorTests() throws Exception {
		if (ln == null)
			beforeEachTest();

		// Given - tl core object
		TLCoreObject tlc = buildTLCoreObject("TestCore1");
		// When - constructed
		CoreObjectNode core1 = new CoreObjectNode(tlc);
		// Then - pass check tests
		ln.addMember(core1);
		check(core1, false);
		if (!core1.isValid())
			ml.printValidationFindings(core1);
		assertTrue(core1.isValid()); // you cant build bo unless valid

		// Given - business object
		BusinessObjectNode bo = ml.addBusinessObjectToLibrary(ln, "TestBO2");
		CoreObjectNode core2 = new CoreObjectNode(bo);
		ln.removeMember(bo);
		check(core2);

		// Given - vwa
		VWA_Node vwa = ml.addVWA_ToLibrary(ln, "TestVWA");
		CoreObjectNode core3 = new CoreObjectNode(vwa);
		ln.removeMember(vwa);
		check(core3);

		// check mock library
		CoreObjectNode core4 = ml.addCoreObjectToLibrary(ln, "Tc");
		check(core4);
	}

	/**
	 * factory tests
	 */
	@Test
	public void CO_FactoryTests() {
		CoreObjectNode newNode = (CoreObjectNode) NodeFactory.newLibraryMember(buildTLCoreObject("test2"));
		ln.addMember(newNode);
		check(newNode);
	}

	/**
	 * load from library tests
	 * 
	 * @throws Exception
	 */
	@Test
	public void CO_FileLoadTests() throws Exception {
		lf.loadTestGroupA(mc);
		mc.getModelNode();
		for (LibraryNode lib : Node.getAllUserLibraries())
			for (LibraryMemberInterface n : lib.getDescendants_LibraryMembers())
				if (n instanceof CoreObjectNode)
					check((CoreObjectNode) n, false);
	}

	@Test
	public void CO_FileLoadTests2() throws Exception {
		lf.loadTestGroupAc(mc);
		mc.getModelNode();
		for (LibraryNode lib : Node.getAllUserLibraries())
			for (LibraryMemberInterface n : lib.getDescendants_LibraryMembers())
				if (n instanceof CoreObjectNode)
					check((CoreObjectNode) n, true);
		// FIXME - Passes when run alone.
		// When run alone, 3 libs before loading test group then after
		// there are 9 libraries, including testFile5.otm
		// Fails validation when run with other tests.
		// When run with other tests, there are 9 libraries, including testFile5.otm
	}

	/**
	 * assigned type tests
	 */
	@Test
	public void CO_TypeAssignmentTests() {
		// Given - a core object
		CoreObjectNode core = ml.addCoreObjectToLibrary_Empty(ln, "CoreTest");
		TypeProvider cType = core.getAssignedType();
		assertTrue(cType == ModelNode.getEmptyNode());
		TypeProvider dType = ml.getXsdDate();
		TypeProvider sType = ml.getXsdString();

		// When - initial assignment
		boolean result = core.setAssignedType(dType);
		assert result;
		cType = core.getAssignedType();
		assertTrue(cType == dType); // assignment worked

		// Then
		assertTrue("Type must be same from Core and simple attribute methods.", core.getSimpleAttribute()
				.getAssignedType() == core.getAssignedType());

		// When - set with simple attribute
		assertTrue("Assigning type must return true. ", core.getSimpleAttribute().setAssignedType(sType));
		assertTrue("Type must be as assigned.", core.getAssignedType() == sType);
		// When - set with core method
		Assert.assertTrue(core.setAssignedType(dType));
		Assert.assertTrue("Type must be as assigned.", core.getAssignedType() == dType);
	}

	@Test
	public void CO_ExtensionTests() {
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
		// Access before assigning base to insure updated when assigned a base type
		List<Node> iKids = ((Node) extendedCO.getFacet_Default()).getInheritedChildren();
		assertTrue(iKids.isEmpty());

		for (Node n : ln.getDescendants_LibraryMemberNodes())
			if (n instanceof CoreObjectNode && n != extendedCO) {
				extendedCO.setExtension(n);
				check((CoreObjectNode) n);
				check(extendedCO);
				iKids = ((Node) extendedCO.getFacet_Default()).getInheritedChildren();
				if (!n.getFacet_Default().getChildren().isEmpty())
					assertTrue(!iKids.isEmpty());
			}

	}

	@Test
	public void CO_ChangeToTests() {
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
		ElementNode ele = new ElementNode(typeUser.getFacet_Summary(), "EleUser");
		// Given - the number of library members in library (must not change)
		int typeCount = ln.getDescendants_LibraryMemberNodes().size();

		// Given - an element assigned the bo as a type
		ele.setAssignedType(bo);
		// When - changed to core
		tco = (CoreObjectNode) bo.changeObject(SubType.CORE_OBJECT);
		// Then - the core is valid and element is assigned the core
		check(tco);
		assertTrue(bo.getLibrary() != ln);
		assertTrue("Type assignment must be to the new core.", ele.getAssignedType() == tco);
		assertTrue("New core must have element in where used list.", tco.getWhereAssigned().contains(ele));

		// Repeat with VWA
		ele.setAssignedType(vwa);
		tco = (CoreObjectNode) vwa.changeObject(SubType.CORE_OBJECT);
		check(tco);
		assertTrue(vwa.getLibrary() != ln);
		assertTrue("Type assignment must be to the new core.", ele.getAssignedType() == tco);

		// tn.visit(ln);
		assertTrue("Number of library members must be same as before changes.", typeCount == ln
				.getDescendants_LibraryMemberNodes().size());
	}

	@Test
	public void CO_NameChangeTests() {
		// On name change, all users of the BO and its aliases and facets also need to change.

		// Given - a Core Object with alias
		final String coreName = "initialcoreName";
		CoreObjectNode core = ml.addCoreObjectToLibrary_Empty(ln, coreName);
		AliasNode alias1 = core.addAlias("coreAlias");
		AliasNode aliasSummary = null;
		for (Node n : core.getFacet_Summary().getChildren())
			if (n instanceof AliasNode)
				aliasSummary = (AliasNode) n;
		// Then - the alias must exist on the core and it's facet
		assertNotNull(alias1);
		assertNotNull(aliasSummary);

		// When - a core is created that has elements that use the core and aliases as properties
		CoreObjectNode elements = ml.addCoreObjectToLibrary(ln, "user");
		FacetProviderNode eleOwner = elements.getFacet_Summary();

		// When - assigned core as type
		ElementNode e1 = new ElementNode(eleOwner, "p1", core);
		assertTrue("Element name must be the core name.", e1.getName().equals(core.getName()));
		assertTrue("Core must be assigned as type.", core.getWhereAssigned().contains(e1));

		// When - assigned alias as type
		e1 = new ElementNode(eleOwner, "p2", alias1);
		assertTrue("Element name must be alias name.", e1.getName().equals(alias1.getName()));
		assertTrue("Facet alias must be assigned as type.", alias1.getWhereAssigned().contains(e1));

		// When - assigned summary facet as type
		e1 = new ElementNode(eleOwner, "p3", core.getFacet_Summary());
		assertTrue("Element name must be facet name.", e1.getName().equals(core.getFacet_Summary().getName()));
		assertTrue("Element name must start with core name.", e1.getName().startsWith(core.getName()));
		assertTrue("Summary Facet must be assigned as type.", core.getFacet_Summary().getWhereAssigned().contains(e1));

		// When - assigned alias from summary facet
		e1 = new ElementNode(eleOwner, "p4", aliasSummary);
		assertTrue("Element name must start with alias name.", e1.getName().startsWith(alias1.getName()));
		assertTrue("Summary Facet alias must be assigned as type.", aliasSummary.getWhereAssigned().contains(e1));

		// When - Change the core name
		String changedName = "changedName";
		core.setName(changedName);
		changedName = NodeNameUtils.fixCoreObjectName(changedName); // get the "fixed" name
		assertTrue(changedName.equals(core.getName()));

		// Then - the elements and facets name must change.
		for (Node n : eleOwner.getChildren()) {
			TypeUser tn = (TypeUser) n;
			if (tn.getAssignedType() == core)
				assertTrue(tn.getName().equals(changedName));
			else if (tn.getAssignedType() == alias1)
				assertTrue(tn.getName().equals(alias1.getName()));
			else if (tn.getAssignedType() == core.getFacet_Summary())
				assertTrue(tn.getName().equals(changedName));
			else if (tn.getAssignedType() == aliasSummary)
				assertTrue(tn.getName().startsWith(alias1.getName()));
			else
				assert true; // no-op - created by Mock Library to make core valid
		}
		// NOTE - not valid!

		// When - alias name changed
		String aliasName2 = "aliasName2";
		alias1.setName(aliasName2);
		aliasName2 = alias1.getName(); // get the "fixed" name
		// Then - all aliases on core must change name
		assertTrue("Alias Name must change.", alias1.getName().equals(aliasName2));
		assertTrue("Alias on summary facet must change.", aliasSummary.getName().startsWith(aliasName2));

		// Then - must find an element with the alias name
		int found = 0;
		for (Node n : eleOwner.getChildren()) {
			TypeUser tn = (TypeUser) n;
			if (tn.getAssignedType() == core)
				assertTrue(tn.getName().equals(changedName));
			else if (tn.getAssignedType() == alias1)
				assertTrue(tn.getName().equals(aliasName2));
			else if (tn.getAssignedType() == core.getFacet_Summary())
				assertTrue(tn.getName().equals(changedName));
			else if (tn.getAssignedType() == aliasSummary)
				assertTrue(tn.getName().startsWith(aliasName2));
			else
				assert true; // no-op - created by Mock Library to make core valid
		}
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
	public void check(CoreObjectNode core) {
		check(core, true);
	}

	public void check(CoreObjectNode core, boolean validate) {
		assertTrue(core.getLibrary() != null);

		// Core must only have 6 children + aliases
		int cSize = 6 + core.getAliases().size();
		assertTrue("Core children count must be " + cSize, core.getChildren().size() == cSize);

		// Facets
		for (Node child : core.getChildren()) {
			ml.check(child, validate);
		}
		assertTrue(core.getFacet_Simple() != null);
		assertTrue(core.getFacet_Default() != null);
		assertTrue(core.getFacet_Summary() != null);
		assertTrue(core.getFacet_Detail() != null);
		assertTrue(core.getFacet_Role() != null);
		assertTrue(core.getSimpleAttribute() != null);
		assertTrue(core.getType() == null);

		if (validate)
			assertTrue(core.isValid());

	}
}
