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

import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.util.OTM16Upgrade;
import org.opentravel.schemas.controllers.DefaultProjectController;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.node.facets.ContextualFacetNode;
import org.opentravel.schemas.node.facets.ContributedFacetNode;
import org.opentravel.schemas.node.facets.CustomFacetNode;
import org.opentravel.schemas.node.facets.FacetNode;
import org.opentravel.schemas.node.interfaces.FacadeInterface;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.properties.AttributeNode;
import org.opentravel.schemas.node.properties.ElementNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.testUtils.MockLibrary;
import org.opentravel.schemas.testUtils.NodeTesters;
import org.opentravel.schemas.testUtils.NodeTesters.TestNode;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dave Hollander
 * 
 */
public class BusinessObjectTests {
	static final Logger LOGGER = LoggerFactory.getLogger(MockLibrary.class);

	ModelNode model = null;
	MockLibrary ml = null;
	LibraryNode ln = null;
	MainController mc;
	DefaultProjectController pc;
	ProjectNode defaultProject;
	LoadFiles lf = null;
	TestNode tn = new NodeTesters().new TestNode();
	TypeProvider emptyNode = null;
	TypeProvider sType = null;

	@Before
	public void beforeEachTest() {
		mc = new MainController();
		ml = new MockLibrary();
		pc = (DefaultProjectController) mc.getProjectController();
		defaultProject = pc.getDefaultProject();
		lf = new LoadFiles();
		emptyNode = (TypeProvider) ModelNode.getEmptyNode();
		sType = (TypeProvider) NodeFinders.findNodeByName("date", ModelNode.XSD_NAMESPACE);
	}

	@Test
	public void BO_ConstructorsTests() {

	}

	// factory tests
	@Test
	public void BO_FactoryTests() {

	}

	@Test
	public void BO_ContextualFacets_v15() {
		// Given a business object in a library to be editable
		OTM16Upgrade.otm16Enabled = false;
		LibraryNode ln = ml.createNewLibrary_Empty("http://example.com", "TestLib1", defaultProject);
		ln.setEditable(true);
		BusinessObjectNode bo = new BusinessObjectNode(new TLBusinessObject());
		bo.setName("TestBO");
		ln.addMember(bo);
		assertTrue(bo.isEditable_newToChain()); // required to add facets
		// Given - an id facet property to make the bo valid
		TypeProvider string = (TypeProvider) NodeFinders.findNodeByName("string", ModelNode.XSD_NAMESPACE);
		new ElementNode(bo.getIDFacet(), "TestEleInID" + bo.getName(), string);
		//
		int count = ln.getDescendants_LibraryMembers().size();
		checkBusinessObject(bo);

		// When - using addFacet to add a custom facet
		ContextualFacetNode cf = bo.addFacet("Custom1", TLFacetType.CUSTOM);
		// Then
		assertTrue(cf != null);
		assertTrue(cf.getParent() == bo);
		assertTrue(cf.getOwningComponent() == bo);
		assertTrue(cf instanceof CustomFacetNode);
		assertTrue("Identity listener must be set.", Node.GetNode(((CustomFacetNode) cf).getTLModelObject()) == cf);

		// When - using the factory to add a custom facet
		TLContextualFacet tlCf = new TLContextualFacet();
		tlCf.setFacetType(TLFacetType.CUSTOM);
		tlCf.setName("Custom2");
		bo.getTLModelObject().addCustomFacet(tlCf);
		ComponentNode cf2 = NodeFactory.newMember(bo, tlCf);
		// Then
		assertTrue(cf2 != null);
		assertTrue(cf2.getParent() == bo);
		assertTrue(cf2.getOwningComponent() == bo);
		assertTrue(cf2 instanceof CustomFacetNode);
		assertTrue("Identity listener must be set.", Node.GetNode(((CustomFacetNode) cf2).getTLModelObject()) == cf2);

		// Then - no new Library Members were added to library
		assertTrue(count == ln.getDescendants_LibraryMembers().size());
		assertTrue(ln.getDescendants_ContextualFacets().size() == 2);
		assertTrue(ln.getComplexRoot().getChildren().size() == 1);

		// Then
		checkBusinessObject(bo);

		// When - version the library
		LibraryChainNode lcn = new LibraryChainNode(ln);
		// Then - assure contextual facets are NOT wrapped in version nodes
		for (Node n : bo.getChildren())
			assertTrue(n instanceof FacetNode);
	}

	@Test
	public void BO_ContextualFacets_v16() {
		// Given a business object in a library to be editable
		OTM16Upgrade.otm16Enabled = true;
		LibraryNode ln = ml.createNewLibrary_Empty("http://example.com/t2", "TestLib2", defaultProject);
		LibraryChainNode lcn = new LibraryChainNode(ln);
		ln.setEditable(true);
		BusinessObjectNode bo = new BusinessObjectNode(new TLBusinessObject());
		bo.setName("TestBO");
		ln.addMember(bo);
		assertTrue(bo.isEditable_newToChain()); // required to add facets
		// Given - an id facet property to make the bo valid
		TypeProvider string = (TypeProvider) NodeFinders.findNodeByName("string", ModelNode.XSD_NAMESPACE);
		new ElementNode(bo.getIDFacet(), "TestEleInID" + bo.getName(), string);
		//
		int count = ln.getDescendants_LibraryMembers().size();
		checkBusinessObject(bo);

		// When - addFacet() used to add a custom facet
		ContextualFacetNode cf = bo.addFacet("Custom1", TLFacetType.CUSTOM);
		// Then
		assertTrue(cf != null);
		assertTrue(!(cf instanceof ContributedFacetNode));
		assertTrue(cf instanceof CustomFacetNode);
		assertTrue(ln.contains(cf));
		// Only true if non-versioned library - assertTrue("Contextual Facet parent must be nav node", cf.getParent() ==
		// bo.getParent());
		assertTrue(cf.getOwningComponent() == bo);
		assertTrue("Identity listener must be set.", Node.GetNode(((CustomFacetNode) cf).getTLModelObject()) == cf);

		// When - adding elements and attributes to contextual facet
		AttributeNode attr = new AttributeNode(cf, "att1");
		ElementNode ele = new ElementNode(cf, "ele1");
		// Then
		assertTrue("Must be able to add attributes.", attr.getParent() == cf);
		assertTrue("Must be able to add elements.", ele.getParent() == cf);

		// When - adding elements and attributes to contributed facet
		ContributedFacetNode contrib = cf.getWhereContributed();
		AttributeNode attr2 = new AttributeNode(contrib, "att2");
		ElementNode ele2 = new ElementNode(contrib, "ele2");
		// Then
		assertTrue("Must be able to add attributes.", attr2.getParent() == cf);
		assertTrue("Must be able to add elements.", ele2.getParent() == cf);

		// When - factory used to add a custom facet
		TLContextualFacet tlCf = ContextualFacetNode.createTL("Custom2", TLFacetType.CUSTOM);
		ContextualFacetNode cf2 = (ContextualFacetNode) NodeFactory.newObjectNode(tlCf, ln);
		assertTrue(cf2.getWhereContributed() == null);
		ContributedFacetNode conf2 = (ContributedFacetNode) NodeFactory.newMember(bo, tlCf);
		// // Then
		assertTrue(conf2.getLocalName().startsWith(bo.getName()));
		assertTrue(cf2 instanceof CustomFacetNode);
		assertTrue("Identity listener must be set.", Node.GetNode(((CustomFacetNode) cf2).getTLModelObject()) == cf2);

		// Then - new Library Members were added to library
		assertTrue(count != ln.getDescendants_LibraryMembers().size());
		List<ContextualFacetNode> cfs = ln.getDescendants_ContextualFacets();
		assertTrue(ln.getDescendants_ContextualFacets().size() == 4);

		// When - adding other facet types
		bo.addFacet("q1", TLFacetType.QUERY);
		bo.addFacet("u1", TLFacetType.UPDATE);

		// Then
		checkBusinessObject(bo);

		// Then - assure contextual facets are NOT wrapped in version nodes
		for (Node n : bo.getChildren())
			assertTrue(n instanceof FacetNode);

		OTM16Upgrade.otm16Enabled = false;
	}

	@Test
	public void BO_MockLibraryTest() {
		// Given a business object with one of each contextual facet
		LibraryNode ln = ml.createNewLibrary(defaultProject.getNSRoot(), "test", defaultProject);
		BusinessObjectNode bo = ml.addBusinessObjectToLibrary(ln, "bo");

		// Assure the mock library created a valid BO
		checkBusinessObject(bo);
	}

	// load from library tests
	@Test
	public void BO_LibraryLoadTests() throws Exception {
		lf.loadTestGroupA(mc);

		List<LibraryNode> libs = mc.getModelNode().getUserLibraries();
		for (LibraryNode lib : libs) {
			for (Node bo : lib.getDescendants_LibraryMembers()) {
				if (bo instanceof BusinessObjectNode)
					checkBusinessObject((BusinessObjectNode) bo);
			}
			if (lib.isInChain())
				continue;
			// Repeat test with library in a chain
			LibraryChainNode lcn = new LibraryChainNode(lib);
			for (Node bo : lcn.getDescendants_LibraryMembers()) {
				if (bo instanceof BusinessObjectNode)
					checkBusinessObject((BusinessObjectNode) bo);
			}
		}
	}

	// Simulate process in addMOChildren
	// load from library tests
	@Test
	public void BO_LibraryLoadTests_v16() throws Exception {
		OTM16Upgrade.otm16Enabled = true;
		lf.loadFile_FacetBase(defaultProject);

		List<LibraryNode> libs = mc.getModelNode().getUserLibraries();
		assertTrue(libs.size() > 0);
		LibraryNode lib = libs.get(0);
		lib.setEditable(true);

		for (ContextualFacetNode cf : lib.getDescendants_ContextualFacets()) {
			if (cf instanceof ContributedFacetNode)
				assertTrue(((ContributedFacetNode) cf).getContributor() != null);
			else
				assertTrue(cf.getWhereContributed() != null);
			assertTrue(cf.getParent() != null);
			ml.checkObject(cf);
		}

		for (Node bo : lib.getDescendants_LibraryMembers()) {
			if (bo instanceof BusinessObjectNode)
				checkBusinessObject((BusinessObjectNode) bo);
		}

		// Repeat test with library in a chain
		LibraryChainNode lcn = new LibraryChainNode(lib);
		for (Node bo : lcn.getDescendants_LibraryMembers()) {
			if (bo instanceof BusinessObjectNode)
				checkBusinessObject((BusinessObjectNode) bo);
		}

		OTM16Upgrade.otm16Enabled = false;
	}

	/**
	 * all tests to be used in these tests and by other junits
	 */
	public boolean checkBusinessObject(BusinessObjectNode bo) {

		// Check fixed structure
		assertTrue("Must have identity listener.", Node.GetNode(bo.getTLModelObject()) == bo);
		assertTrue("Must have id facet.", bo.getIDFacet() != null);
		assertTrue("ID Facet parent must be bo.", ((Node) bo.getIDFacet()).getParent() == bo);
		assertTrue("TL Facet must report this is ID facet.", bo.getIDFacet().isIDFacet());

		assertTrue("Must have summary facet.", bo.getSummaryFacet() != null);
		assertTrue("Summary Facet parent must be bo.", ((Node) bo.getSummaryFacet()).getParent() == bo);
		assertTrue("TL Facet must report this is Summary facet.", bo.getSummaryFacet().isSummaryFacet());

		assertTrue("Must have detail facet.", bo.getDetailFacet() != null);
		assertTrue("Facet parent must be bo.", ((Node) bo.getDetailFacet()).getParent() == bo);
		assertTrue("TL Facet must report this is Detail facet.", bo.getDetailFacet().isDetailFacet());
		assertTrue(bo.getAttributeFacet() == null); // It does not have one.
		assertTrue("Must have TL Busness Object.", bo.getTLModelObject() instanceof TLBusinessObject);

		// Is assertions
		assertTrue("If editable it must also be aliasable.", bo.isAliasable() == bo.isEditable_newToChain());
		assertTrue("", bo.isExtensibleObject());
		assertTrue("", bo.isNamedEntity());
		assertTrue("", bo.isNamedType());
		assertTrue("", bo instanceof TypeProvider);

		// check name and label
		assertTrue("BO must have a name.", !bo.getName().isEmpty());
		assertTrue("BO must have a label.", !bo.getLabel().isEmpty());

		// Check all descendants
		assertTrue(bo.getLibrary() != null);
		LibraryNode thisLib = bo.getLibrary();
		for (Node n : bo.getDescendants()) {
			assertTrue(n.getLibrary() == thisLib);
			// Nested contextual facets are owned by parent facet.
			if (!(n.getOwningComponent() instanceof ContextualFacetNode))
				assertTrue("Business object must be owning component.", n.getOwningComponent() == bo);
			assertTrue("Must not be deleted.", !n.isDeleted());
			if (n instanceof ContributedFacetNode) {
				assertTrue("Contributed facets only used for version 1.6 and higher.", OTM16Upgrade.otm16Enabled);
				assertTrue("Must have identity listener of contributor contextual facet.",
						Node.GetNode(n.getTLModelObject()) == ((ContributedFacetNode) n).get());
			} else if (n instanceof FacadeInterface)
				assertTrue("Must have identity listener.",
						Node.GetNode(n.getTLModelObject()) == ((FacadeInterface) n).get());
			else
				assertTrue("Must have identity listener.", Node.GetNode(n.getTLModelObject()) == n);
		}

		// Parent Links
		assertTrue("BO must be child of parent.", bo.getParent().getChildren().contains(bo));
		assertTrue("BO must be in list only once.", bo.getParent().getChildren().indexOf(bo) == bo.getParent()
				.getChildren().lastIndexOf(bo));

		// must have at least 3 children
		assertTrue(3 <= bo.getChildren().size());

		// Check all the children
		for (Node n : bo.getChildren()) {
			ml.checkObject(n);
			assertTrue(!(n instanceof VersionNode));
			if (!OTM16Upgrade.otm16Enabled)
				assertTrue("Contributed facets are only supported in version 1.6 and later.",
						!(n instanceof ContributedFacetNode));
		}

		tn.visit(bo);

		return true;
	}

	/**
	 * Business Object Specific Tests *******************************************************
	 * 
	 */
	@Test
	public void BO_ExensionTests() {
		MainController mc = new MainController();
		LoadFiles lf = new LoadFiles();
		MockLibrary ml = new MockLibrary();

		LibraryNode ln = lf.loadFile4(mc);
		LibraryChainNode lcn = new LibraryChainNode(ln); // Test in managed library
		ln.setEditable(true);

		BusinessObjectNode extendedBO = ml.addBusinessObjectToLibrary_Empty(ln, "ExtendedBO");
		assertNotNull("Null object created.", extendedBO);

		for (Node n : ln.getDescendants_LibraryMembers())
			if (n instanceof BusinessObjectNode && n != extendedBO) {
				extendedBO.setExtension(n);
				checkBusinessObject((BusinessObjectNode) n);
				checkBusinessObject(extendedBO);
			}
		// see also org.opentravel.schemas.node.InheritedChildren_Tests
	}

	@Test
	public void BO_ChangeToTests() {
		MockLibrary ml = new MockLibrary();
		MainController mc = new MainController();
		DefaultProjectController pc = (DefaultProjectController) mc.getProjectController();
		ProjectNode defaultProject = pc.getDefaultProject();
		TypeProvider string = (TypeProvider) NodeFinders.findNodeByName("string", ModelNode.XSD_NAMESPACE);

		LibraryNode ln = ml.createNewLibrary(defaultProject.getNSRoot(), "test", defaultProject);
		ml.addBusinessObjectToLibrary(ln, "bo");
		VWA_Node vwa = ml.addVWA_ToLibrary(ln, "vwa");
		CoreObjectNode core = ml.addCoreObjectToLibrary(ln, "co");
		new ElementNode(core.getSummaryFacet(), "TE2", vwa);

		// When VWA and Core are changed
		BusinessObjectNode tboCore = (BusinessObjectNode) core.changeToBusinessObject();
		BusinessObjectNode tboVwa = (BusinessObjectNode) vwa.changeToBusinessObject();
		// Given - an id facet property to make the bo valid
		new ElementNode(tboCore.getIDFacet(), "TestEleInID" + tboCore.getName(), string);
		new ElementNode(tboVwa.getIDFacet(), "TestEleInID" + tboVwa.getName(), string);

		// Then
		checkBusinessObject(tboCore);
		checkBusinessObject(tboVwa);

		// Same test, but as part of a chain
		LibraryChainNode lcn = new LibraryChainNode(ln); // make sure is version safe
		core = ml.addCoreObjectToLibrary(ln, "co2");
		vwa = ml.addVWA_ToLibrary(ln, "vwa2");
		new ElementNode(core.getSummaryFacet(), "TestElement").setAssignedType(vwa);

		tboCore = (BusinessObjectNode) core.changeToBusinessObject();
		tboVwa = (BusinessObjectNode) vwa.changeToBusinessObject();
		new ElementNode(tboCore.getIDFacet(), "TestEleInID" + tboCore.getName(), string);
		new ElementNode(tboVwa.getIDFacet(), "TestEleInID" + tboVwa.getName(), string);
		checkBusinessObject(tboCore);
		checkBusinessObject(tboVwa);

		// TODO - validate where assigned was changed
	}

	@Test
	public void BO_FacetAsTypeTests() {
		MainController mc = new MainController();
		LoadFiles lf = new LoadFiles();
		LibraryNode ln = lf.loadFile1(mc);
		ln.setEditable(true);

		// Find an element to use to make sure all facets can be assigned as a type
		TypeUser user = null;
		for (TypeUser n : ln.getDescendants_TypeUsers())
			if (!((Node) n).getOwningComponent().getName().equals("Profile") && n instanceof ElementNode) {
				user = n;
				break;
			}
		assert user != null;
		assert user.isEditable();

		// File 1 has a business object Profile with 5 facets and 1 alias
		BusinessObjectNode bo = null;
		List<Node> members = ln.getDescendants_LibraryMembers();
		for (Node n : members)
			if (n.getName().equals("Profile") && n instanceof BusinessObjectNode)
				bo = (BusinessObjectNode) n;
		assertTrue("Profile object must be in test 1.", bo != null);

		// Check facets
		final int expectedFacetCount = 5;
		int facetCnt = 0;
		for (Node n : bo.getChildren())
			if (n instanceof FacetNode) {
				facetCnt++;
				user.setAssignedType((FacetNode) n);
				assertTrue("User must be assigned facet as type.", user.getAssignedType() == n);
				assertTrue("Facet must have user in where assigned list.",
						((FacetNode) n).getWhereAssigned().contains(user));
			}
		assertTrue("Profile business object in test 1 must have " + expectedFacetCount + " facets.",
				facetCnt == expectedFacetCount);

		// check alias
		int aliasCnt = 0;
		for (Node n : bo.getChildren())
			if (n instanceof AliasNode) {
				aliasCnt++;
				user.setAssignedType((TypeProvider) n);
				assert user.getAssignedType() == n;
			}
		assert aliasCnt == 1;
	}

	@Test
	public void BO_NameChangeTests() {
		// On name change, all users of the BO and its aliases and facets also need to change.
		LibraryNode ln = ml.createNewLibrary(defaultProject.getNSRoot(), "test", defaultProject);

		// Given - a Business Object with alias
		final String boName = "initialBOName";
		BusinessObjectNode bo = ml.addBusinessObjectToLibrary(ln, boName);
		AliasNode alias1 = bo.addAlias("boAlias");
		AliasNode aliasSummary = null;
		for (Node n : bo.getSummaryFacet().getChildren())
			if (n instanceof AliasNode)
				aliasSummary = (AliasNode) n;
		// Then the alias must exist on the bo and it's facet
		assertNotNull(alias1);
		assertNotNull(aliasSummary);

		// When - a core is created that has elements that use the BO and aliases as properties
		CoreObjectNode co = ml.addCoreObjectToLibrary(ln, "user");
		PropertyNode pBO = new ElementNode(co.getSummaryFacet(), "p1", bo);
		PropertyNode pAlias1 = new ElementNode(co.getSummaryFacet(), "p2", alias1);
		PropertyNode pBOSummary = new ElementNode(co.getSummaryFacet(), "p3", bo.getSummaryFacet());
		PropertyNode pBOSumAlias = new ElementNode(co.getSummaryFacet(), "p4", aliasSummary);
		// Then - the facet alias has where used
		assertTrue("Facet alias must be assigned as type.", !aliasSummary.getWhereAssigned().isEmpty());
		// Then - the elements are named after their type
		assertTrue("Element name must be the BO name.", pBO.getName().equals(bo.getName()));
		assertTrue("Element name must be alias name.", pAlias1.getName().contains(alias1.getName()));
		assertTrue("Element name must NOT be facet name.", !pBOSummary.getName().equals(bo.getSummaryFacet().getName()));
		// Then - assigned facet name will be constructed by compiler using owning object and facet type.
		assertTrue("Element name must start with BO name.", pBOSummary.getName().startsWith(bo.getName()));
		assertTrue("Element name must contain facet name.",
				pBOSummary.getName().contains(bo.getSummaryFacet().getName()));
		assertTrue("Element name must start with alias name.", pBOSumAlias.getName().startsWith(alias1.getName()));

		// When - Change the BO name
		String changedName = "changedName";
		bo.setName(changedName);
		changedName = NodeNameUtils.fixBusinessObjectName(changedName); // get the "fixed" name

		// Then - the business object name and facets must change.
		assertTrue("Business Object name must be fixed name.", pBO.getName().equals(changedName));
		assertTrue("Alias name must be unchanged.", pAlias1.getName().equals(alias1.getName()));
		assertTrue("Facet name must start with BO name.", pBOSummary.getName().startsWith(changedName));
		// Then - the facet alias has where used
		assertTrue("Facet alias must be assigned as type.", !aliasSummary.getWhereAssigned().isEmpty());
		// Then - the elements are named after their type
		assertTrue("Element name must be the BO name.", pBO.getName().equals(changedName));
		assertTrue("Element name must contain facet name.",
				pBOSummary.getName().contains(bo.getSummaryFacet().getName()));
		assertTrue("Element name must start with BO name.", pBOSummary.getName().startsWith(changedName));
		assertTrue("Element name must start with alias name.", pBOSumAlias.getName().startsWith(alias1.getName()));
		assertTrue("Element name must start with alias name.", pAlias1.getName().startsWith(alias1.getName()));

		// When - alias name changed
		String aliasName2 = "aliasName2";
		alias1.setName(aliasName2);
		aliasName2 = alias1.getName(); // get the "fixed" name

		// Then - all aliases on BO must change name
		assertTrue("Alias Name must change.", pAlias1.getName().equals(aliasName2));
		assertTrue("Alias on summary facet must change.", aliasSummary.getName().startsWith(aliasName2));

		// Then - all type users of those aliases must change name
		assertTrue("Element name must start with changed alias name.", pBOSumAlias.getName().startsWith(aliasName2));
		assertTrue("Element name must start with changed alias name.", pAlias1.getName().startsWith(aliasName2));
	}

}
