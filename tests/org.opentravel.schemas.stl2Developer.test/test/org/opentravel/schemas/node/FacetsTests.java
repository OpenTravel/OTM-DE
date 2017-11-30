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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemacompiler.event.ModelElementListener;
import org.opentravel.schemacompiler.model.TLAbstractFacet;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLListFacet;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLSimpleFacet;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.util.OTM16Upgrade;
import org.opentravel.schemas.controllers.DefaultProjectController;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.node.facets.AttributeFacetNode;
import org.opentravel.schemas.node.facets.ChoiceFacetNode;
import org.opentravel.schemas.node.facets.ContextualFacetNode;
import org.opentravel.schemas.node.facets.ContributedFacetNode;
import org.opentravel.schemas.node.facets.CustomFacetNode;
import org.opentravel.schemas.node.facets.FacetNode;
import org.opentravel.schemas.node.facets.ListFacetNode;
import org.opentravel.schemas.node.facets.OperationFacetNode;
import org.opentravel.schemas.node.facets.OperationNode;
import org.opentravel.schemas.node.facets.PropertyOwnerNode;
import org.opentravel.schemas.node.facets.QueryFacetNode;
import org.opentravel.schemas.node.facets.RoleFacetNode;
import org.opentravel.schemas.node.facets.SimpleFacetFacadeNode;
import org.opentravel.schemas.node.facets.SimpleFacetNode;
import org.opentravel.schemas.node.facets.UpdateFacetNode;
import org.opentravel.schemas.node.interfaces.ContextualFacetOwnerInterface;
import org.opentravel.schemas.node.interfaces.ExtensionOwner;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.listeners.InheritanceDependencyListener;
import org.opentravel.schemas.node.properties.AttributeNode;
import org.opentravel.schemas.node.properties.ElementNode;
import org.opentravel.schemas.node.properties.IndicatorNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.properties.PropertyNodeType;
import org.opentravel.schemas.node.properties.RoleNode;
import org.opentravel.schemas.node.properties.SimpleAttributeFacadeNode;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.testUtils.MockLibrary;
import org.opentravel.schemas.testUtils.NodeTesters;
import org.opentravel.schemas.types.TestTypes;
import org.opentravel.schemas.utils.FacetNodeBuilder;
import org.opentravel.schemas.utils.LibraryNodeBuilder;
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Dave Hollander
 * 
 */
public class FacetsTests {
	static final Logger LOGGER = LoggerFactory.getLogger(FacetsTests.class);

	ModelNode model = null;
	TestTypes tt = new TestTypes();

	NodeTesters nt = new NodeTesters();
	LoadFiles lf = new LoadFiles();
	Library_FunctionTests lt = new Library_FunctionTests();
	MockLibrary ml = new MockLibrary();
	LibraryNode ln = null;
	MainController mc;
	DefaultProjectController pc;
	ProjectNode defaultProject;

	@Before
	public void beforeAllTests() {
		mc = new MainController();
		pc = (DefaultProjectController) mc.getProjectController();
		defaultProject = pc.getDefaultProject();
	}

	@After
	public void afterAllTests() {
		OTM16Upgrade.otm16Enabled = false;
	}

	@Test
	public void Facets_ConstructorTests() {

		// Given two libraries, one managed one not managed
		ln = ml.createNewLibrary("http://www.test.com/test1", "test1", defaultProject);
		LibraryNode ln_inChain = ml.createNewLibrary("http://www.test.com/test1c", "test1c", defaultProject);
		new LibraryChainNode(ln_inChain);
		ln_inChain.setEditable(true);
		assertTrue("Library must exist.", ln != null);
		assertTrue("Library must exist.", ln_inChain != null);

		// Given - one of each TLFacet type.
		TLFacet tlf = new TLFacet();
		assertTrue("Facet must not be null.", tlf != null);
		TLAbstractFacet tlAf = new TLFacet();
		assertTrue("Facet must not be null.", tlAf != null);
		TLListFacet tlLf = new TLListFacet(tlAf); // tlaf should be simple or detail.
		assertTrue("Facet must not be null.", tlLf != null);
		TLSimpleFacet tlSf = new TLSimpleFacet();
		assertTrue("Facet must not be null.", tlSf != null);
		TLValueWithAttributes tlVWA = new TLValueWithAttributes();
		assertTrue("Facet must not be null.", tlVWA != null);
		// TLnValueWithAttributesFacet tlVf = new TLnValueWithAttributesFacet(tlVWA);
		// assertTrue("Facet must not be null.", tlVf != null);
	}

	@Test
	public void buildersTests() {
		ln = ml.createNewLibrary("http://www.test.com/test1", "test1", defaultProject);

		FacetNode facetNode1 = FacetNodeBuilder.create(ln).addElements("E1", "E2", "E3").build();
		FacetNode facetNode2 = FacetNodeBuilder.create(ln).addAttributes("A1", "A2", "A3").build();
		FacetNode facetNode3 = FacetNodeBuilder.create(ln).addIndicators("I1", "I2").addAliases("Alias1").build();
		assertTrue("Built node 1 must not be null.", facetNode1 != null);
		assertTrue("Built node 2 must not be null.", facetNode2 != null);
		assertTrue("Built node 3 must not be null.", facetNode3 != null);
	}

	@Test
	public void Facets_InheritanceTests() {

		// Given - a BO in a library
		ln = ml.createNewLibrary("http://www.test.com/test1", "test1", defaultProject);
		BusinessObjectNode baseBO = ml.addBusinessObjectToLibrary(ln, "BaseBO");
		CustomFacetNode c1 = (CustomFacetNode) baseBO.addFacet("BaseC1", TLFacetType.CUSTOM);
		AttributeNode a1 = new AttributeNode(c1, "cAttr1");
		// Then - finding c1 facet must work because it is used in children handler
		assertTrue("Must be able to find c1 by name.", baseBO.findChildByName(c1.getName()) == c1);
		// Then - c1 must have children to be inherited.
		assertTrue("Summary must have children.", !baseBO.getFacet_Summary().getChildren().isEmpty());
		assertTrue("Summary must not have inherited children.", baseBO.getFacet_Summary().getInheritedChildren()
				.isEmpty());

		// Given - a second, empty BO to be extended
		BusinessObjectNode extendedBO = ml.addBusinessObjectToLibrary_Empty(ln, "ExBO");
		new ElementNode(extendedBO.getFacet_Summary(), "ExEle");

		//
		// When - objects are extended
		extendedBO.setExtension(baseBO);
		assertTrue("ExtendedBO extends BaseBO.", extendedBO.isExtendedBy(baseBO));

		// Then - there must be an inherited facet.
		//
		assertTrue("Must have inherited child.", !extendedBO.getInheritedChildren().isEmpty());
		CustomFacetNode inheritedCustom = null;
		List<CustomFacetNode> customFacets = extendedBO.getCustomFacets(true);
		for (CustomFacetNode cf : customFacets) {
			if (c1.getName().equals(cf.getName()))
				inheritedCustom = cf;
		}
		assertTrue("Must have inherited c1 custom facet.", inheritedCustom != null);
		assertTrue("InheritedFrom must be the c1 custom facet.", inheritedCustom.getInheritedFrom() == c1);
		// Then - verify listeners are correct
		for (ModelElementListener l : c1.getTLModelObject().getListeners())
			if (l instanceof InheritanceDependencyListener) {
				assertTrue(((InheritanceDependencyListener) l).getNode() == inheritedCustom);
				assertTrue(((InheritanceDependencyListener) l).getHandler() == inheritedCustom.getParent()
						.getChildrenHandler());
			}

		// Then - there must be inherited children in the facets.
		//
		List<Node> baseKids = baseBO.getFacet_Summary().getChildren();
		assertTrue("Base BO summary must have properties.", !baseKids.isEmpty());
		List<Node> exKids = extendedBO.getFacet_Summary().getChildren();
		assertTrue("Extended BO summary must have properties.", !exKids.isEmpty());
		List<Node> inheritedKids = extendedBO.getFacet_Summary().getInheritedChildren();
		assertTrue("Extended BO summary must have inherited properties.", !inheritedKids.isEmpty());
		// Then - verify listeners are correct
		for (Node i : inheritedKids) {
			ComponentNode ci = (ComponentNode) i;
			assertTrue(baseBO.getFacet_Summary().contains(ci.getInheritedFrom()));
			for (ModelElementListener l : ci.getInheritedFrom().getTLModelObject().getListeners())
				if (l instanceof InheritanceDependencyListener) {
					assertTrue(((InheritanceDependencyListener) l).getNode() == i);
					assertTrue(((InheritanceDependencyListener) l).getHandler() == i.getParent().getChildrenHandler());
				}
		}

		//
		// Tests to assure changes to objects are synchronized with the inherited "ghosts".
		//
		// When - custom facet name changes to include owner and newName
		String newName = "ChangedName";
		String startingName = inheritedCustom.getName();
		c1.setName(newName);
		assertTrue(c1.getName().contains(newName));

		// Then - listener wont be removed until inherited children retrieved from exBO
		for (ModelElementListener l : c1.getTLModelObject().getListeners())
			if (l instanceof InheritanceDependencyListener)
				assertTrue(((InheritanceDependencyListener) l).getNode() == inheritedCustom);

		// Then - the old inherited custom node is no longer valid.
		assertTrue(inheritedCustom.isDeleted());
		// Then - check the new inherited custom.
		for (CustomFacetNode cf : extendedBO.getCustomFacets(true))
			if (c1.getName().equals(cf.getName()))
				inheritedCustom = cf;
		assertTrue(!inheritedCustom.getName().equals(startingName));
		assertTrue(inheritedCustom.getName().contains(newName));
		// Then - listener will only be to new custom
		for (ModelElementListener l : c1.getTLModelObject().getListeners())
			if (l instanceof InheritanceDependencyListener)
				assertTrue(((InheritanceDependencyListener) l).getNode() == inheritedCustom);

		// When - add an attribute
		AttributeNode a2 = new AttributeNode(c1, "cAttr2");
		// Then - inherited custom must NOT have that attr in its children
		Node ia2 = inheritedCustom.findChildByName(a2.getName());
		assertTrue("Must not find attribute.", inheritedCustom.findChildByName(a2.getName()) == null);
		for (Node n : inheritedCustom.getInheritedChildren())
			if (n.getName().equals(a2.getName()))
				ia2 = n;
		assertTrue("Must find ghost node with a2's name.", ia2 != null);

		// When - delete the attribute in c1 (base custom)
		a1.delete();
		// Then - node with name of a1 must not be in inherited children
		assertTrue("Must not find a1 by name.", inheritedCustom.findChildByName(a1.getName()) == null);
	}

	@Test
	public void Facets_SortTests() {
		// Given - a BO in a library that has one of each type of property
		ln = ml.createNewLibrary("http://www.test.com/test1", "test1", defaultProject);
		BusinessObjectNode baseBO = ml.addBusinessObjectToLibrary(ln, "BaseBO");
		assertTrue(baseBO != null);
		assertTrue(baseBO.getFacet_Summary() != null);

		// Given - 3 of each property type in summary facet
		ml.addAllProperties(baseBO.getFacet_Summary());
		assertTrue(baseBO.getFacet_Summary().getTLModelObject() != null);
		IndicatorNode i1 = checkIndicatorOrder(baseBO.getFacet_Summary());
		int i1Index = baseBO.getFacet_Summary().getTLModelObject().getIndicators().indexOf(i1.getTLModelObject());
		// ElementNode e1 = checkElementOrder(baseBO.getFacet_Summary());
		// AttributeNode a1 = checkAttributeOrder(baseBO.getFacet_Summary());

		// When moved down
		i1.moveDown();
		i1.moveDown();
		assertTrue(baseBO.getFacet_Summary().getTLModelObject().getIndicators().indexOf(i1.getTLModelObject()) != i1Index);
		// e1.moveDown();
		// a1.moveDown();

		// When sorted
		baseBO.getFacet_Summary().sort();
		// Then order is synchronized (order checked by manual inspection)
		checkOrder(baseBO.getFacet_Summary());
	}

	/**
	 * Assure the order of element, attribute and indicator nodes matches TL order
	 * 
	 * @param facet
	 */
	public void checkOrder(FacetNode facet) {
		checkElementOrder(facet);
		checkAttributeOrder(facet);
		checkIndicatorOrder(facet);
	}

	/**
	 * Assure the order of indicator nodes matches TLIndicator order
	 * 
	 * @param facet
	 */
	public IndicatorNode checkIndicatorOrder(FacetNode facet) {
		ArrayList<IndicatorNode> properties = new ArrayList<IndicatorNode>();
		List<TLIndicator> tlProperties = facet.getTLModelObject().getIndicators();

		for (Node n : facet.getChildren())
			if (n instanceof IndicatorNode)
				properties.add((IndicatorNode) n);
		assertTrue(properties.size() == tlProperties.size());

		for (int i = 0; i < properties.size(); i++)
			assertTrue(properties.get(i).getName().equals(tlProperties.get(i).getName()));

		return properties.get(0);
	}

	// Test not needed since facet uses caching children handler.
	// Test became a property name test
	// /**
	// * Assure the order of element nodes matches TLProperty order
	// *
	// * @param facet
	// */
	public ElementNode checkElementOrder(FacetNode facet) {
		// ArrayList<ElementNode> properties = new ArrayList<ElementNode>();
		// List<TLProperty> tlProperties = facet.getTLModelObject().getElements();
		//
		// for (Node n : facet.getChildren())
		// if (n instanceof ElementNode)
		// properties.add((ElementNode) n);
		// assertTrue(properties.size() == tlProperties.size());
		//
		// for (int i = 0; i < properties.size(); i++)
		// assertTrue(properties.get(i).getName().equals(tlProperties.get(i).getName()));
		//
		// return properties.get(0);
		return null;
	}

	/**
	 * Assure the order of element nodes matches TLAttribute order
	 * 
	 * @param facet
	 */
	public AttributeNode checkAttributeOrder(FacetNode facet) {
		// ArrayList<AttributeNode> properties = new ArrayList<AttributeNode>();
		// List<TLAttribute> tlProperties = facet.getTLModelObject().getAttributes();
		//
		// for (Node n : facet.getChildren())
		// if (n instanceof AttributeNode)
		// properties.add((AttributeNode) n);
		// assertTrue(properties.size() == tlProperties.size());
		//
		// for (int i = 0; i < properties.size(); i++)
		// assertTrue(properties.get(i).getName().equals(tlProperties.get(i).getName()));
		//
		// return properties.get(0);
		return null;
	}

	@Test
	public void Facets_MergeTests() {

		// Given - a BO in a library
		ln = ml.createNewLibrary("http://www.test.com/test1", "test1", defaultProject);
		BusinessObjectNode baseBO = ml.addBusinessObjectToLibrary(ln, "BaseBO");
		// Given - a second BO
		BusinessObjectNode sourceBO = ml.addBusinessObjectToLibrary(ln, "SourceBO");
		int originalSize = baseBO.getCustomFacets().size();

		// When - merged
		baseBO.merge(sourceBO);
		// Then - there must be additional custom facets.
		assertTrue(baseBO.getCustomFacets().size() > originalSize);
	}

	@Test
	public void Facets_CopyFacetTests() {
		ln = ml.createNewLibrary("http://www.test.com/test1", "test1", defaultProject);

		// Given a facet with 8 mixed properties
		FacetNode facetNode1 = FacetNodeBuilder.create(ln).addElements("E1", "E2", "E3").addIndicators("I1", "I2")
				.addAttributes("A1", "A2", "A3").build();
		assertTrue("Starting facet must have 8 properties.", facetNode1.getChildren().size() == 8);

		// Given an standard facet and a VWA Attribute facet
		TLFacet tlf = new TLFacet();
		tlf.setFacetType(TLFacetType.SUMMARY);
		FacetNode fn = (FacetNode) NodeFactory.newChild(null, tlf);
		VWA_Node vwa = ml.addVWA_ToLibrary(ln, "myVWA"); // vwa with one attr
		AttributeFacetNode an = (AttributeFacetNode) vwa.getFacet_Attributes();

		// When copied
		fn.copyFacet(facetNode1); // FIXME - very slow
		an.copyFacet(facetNode1);

		// Then both should have 8 properties
		assertTrue("Facet must have 8 properties.", fn.getChildren().size() == 8);
		assertTrue("Attribute facet must have 9 properties.", an.getChildren().size() == 9);
	}

	@Test
	public void Facets_copyContextualFacet_Tests() {
		OTM16Upgrade.otm16Enabled = true;

		// Given libraries loaded from file that contain contextual facets
		// NOTE: Only version 1.6 and later.
		// Note: load order is important if the compiler is to resolve contributors
		// Lib1 has two of each contextual facet type, one contributing to base lib objects
		LibraryNode lib1 = lf.loadFile_Facets1(defaultProject);
		LibraryNode lib2 = lf.loadFile_Facets2(defaultProject); // one of each
		LibraryNode baseLib = lf.loadFile_FacetBase(defaultProject);
		lib1.setEditable(false);
		lib2.setEditable(false);
		baseLib.setEditable(true);

		// Given - a destination library
		LibraryNode destLib = ml.createNewLibrary("http://www.test.com/test1", "test1", defaultProject);
		// int destCF_Count = destLib.getDescendants_ContextualFacets().size();

		// When - facets copied from non-editable library
		// Then - destination library should contain a facet with that name
		ContextualFacetNode found = null;
		for (ContextualFacetNode cf : lib1.getDescendants_ContextualFacets()) {
			if (cf instanceof ContributedFacetNode)
				continue;
			destLib.copyMember(cf);
			for (ContextualFacetNode candidate : destLib.getDescendants_ContextualFacets())
				if (candidate.getName().equals(cf.getName()))
					found = candidate;
			assert found != null;
			// Then the copy must be Ok
			checkFacet(found);
			found = null;
		}

		OTM16Upgrade.otm16Enabled = false;
	}

	// TODO
	// createPropertyTests()
	// getComponentType

	// @Test
	// public void Facets_RenameableFacetTests() throws Exception {
	// lf.loadTestGroupA(mc);
	// for (LibraryNode lib : pc.getDefaultProject().getLibraries()) {
	// lib.setEditable(true);
	// assertTrue("Library must be editable.", lib.isEditable());
	// ml.addChoice(lib, "choice1");
	// classBasedTests(lib);
	// }
	// }

	public void checkAllFacetsInLibrary(LibraryNode lib) {
		// LOGGER.debug("Checking all facets in " + lib);
		for (Node n : lib.getDescendants()) {
			if (n instanceof FacetNode)
				check((FacetNode) n);
		}
	}

	@Test
	public void Facets_editableTests_v15() {
		OTM16Upgrade.otm16Enabled = false;
		LibraryNode ln = lf.loadFile2(defaultProject);
		assertTrue(ln != null);
		ln.setEditable(true);
		BusinessObjectNode bo = null;

		// Find a business object
		for (LibraryMemberInterface n : ln.get_LibraryMembers())
			if (n instanceof BusinessObjectNode)
				bo = (BusinessObjectNode) n;
		ml.check(bo);

		ContextualFacetNode cf = bo.addFacet("F1", TLFacetType.QUERY);
		assertTrue(cf.isEditable());
		assertTrue(!(cf instanceof ContributedFacetNode));
	}

	@Test
	public void Facets_editableTests_v16() {
		OTM16Upgrade.otm16Enabled = true;

		// Given libraries loaded from file that contain contextual facets
		// NOTE: Only version 1.6 and later.
		// Note: load order is important if the compiler is to resolve contributors
		// Lib1 has two of each contextual facet type, one contributing to base lib objects
		LibraryNode lib1 = lf.loadFile_Facets1(defaultProject);
		LibraryNode lib2 = lf.loadFile_Facets2(defaultProject); // one of each
		LibraryNode baseLib = lf.loadFile_FacetBase(defaultProject);
		lib1.setEditable(false);
		lib2.setEditable(false);
		baseLib.setEditable(true);

		// When - a contextual facet is in editable base library
		for (Node n : baseLib.getDescendants_ContextualFacets())
			// Then contextual not contributed facets must be editable
			if (n instanceof ContributedFacetNode)
				assertTrue(!n.isEditable()); // never edit contributed facets
			else
				assertTrue(n.isEditable());

		// When - a contextual facet is in non-editable library 1
		// Then - contextual not contributed facets must NOT be editable
		for (Node n : lib1.getDescendants_ContextualFacets())
			if (n instanceof ContributedFacetNode)
				assertTrue(!n.isEditable()); // never edit contextual facets
			else
				assertTrue("Must not be editable.", !n.isEditable());

		// When - a contextual facet is in editable library 1
		lib1.setEditable(true);
		baseLib.setEditable(false);
		// Then - contextual facets must be editable
		for (Node n : lib1.getDescendants_ContextualFacets())
			if (n instanceof ContributedFacetNode)
				assertTrue(!n.isEditable()); // never edit contextual facets
			else {
				assertTrue("Must be editable.", n.isEditable());
				assertTrue("Must be in Lib1", n.getLibrary() == lib1);
				// Then - children must also be editable
				for (Node child : n.getChildren()) {
					if (!(child instanceof ContributedFacetNode)) {
						assertTrue("Child must be in facet's library.", child.getLibrary() == n.getLibrary());
						assertTrue("Child must be editable.", child.isEditable());
					}
				}
			}
		OTM16Upgrade.otm16Enabled = false;
	}

	/**
	 * Construct a custom facet and its contributor and verify their relationships.
	 */
	@Test
	public void Facets_BuildContributed_v16() {
		OTM16Upgrade.otm16Enabled = true;

		// Given - two libraries
		ln = ml.createNewLibrary(pc, "bc");
		assertTrue(ln.isEditable());
		LibraryNode ln2 = ml.createNewLibrary_Empty(ln.getNamespace(), "bc2", pc.getDefaultProject());
		assertTrue(ln2.isEditable());

		// Given - a business object in the first library
		BusinessObjectNode bo = null;
		for (LibraryMemberInterface n : ln.get_LibraryMembers())
			if (n instanceof BusinessObjectNode)
				bo = (BusinessObjectNode) n;
		bo.getChildrenHandler().clear();

		// Given - a tlContextualFacet contributing to the bo in lib2
		TLContextualFacet tlc = new TLContextualFacet();
		tlc.setFacetType(TLFacetType.CUSTOM);
		tlc.setName("Custom1");

		// When - new custom facet node created without TLOwner or TLLibrary set
		CustomFacetNode cf = new CustomFacetNode(tlc);
		// Then -
		assertTrue(cf.getTLModelObject() == tlc);
		assertTrue("Listener is correct.", Node.GetNode(tlc) == cf);
		assertTrue("No where contributed location.", cf.getWhereContributed() == null);

		// When - Add member to library
		ln2.addMember(cf);
		// Then -
		assertTrue(ln2.getDescendants_ContextualFacets().contains(cf));
		assertTrue(cf.getParent() instanceof NavNode);
		assertTrue(cf.canBeLibraryMember());
		assertTrue(cf.getLibrary() == ln2);
		assertTrue("No where contributed location.", cf.getWhereContributed() == null);

		// When - owner set on CF
		cf.setOwner(bo);
		// Then -
		assertTrue(cf.getWhereContributed() != null);
		assertTrue(cf.getTLModelObject() == tlc);
		assertTrue(tlc.getOwningEntity() == bo.getTLModelObject());
		assertTrue("tlc identity listener must be the contextual facet.", Node.GetNode(tlc) == cf);
		// Then - contributed component must be correct
		ContributedFacetNode contributed = cf.getWhereContributed();
		LibraryNode ll = contributed.getLibrary(); // ln2
		assertTrue("Contributor must be set.", contributed.getContributor() == cf);
		assertTrue("Must return TlContextualFacet", contributed.getTLModelObject() == tlc);
		assertTrue("Business Object must find contributed facet.", bo.getContributedFacet(tlc) == contributed);
		assertTrue("Contributed must have an owning component", contributed.getOwningComponent() == bo);
		assertTrue("Owner must be parent.", contributed.getParent() == contributed.getOwningComponent());

		// When - a contextual facet is contributed to a contextual facet
		//
		TLContextualFacet tlc2 = new TLContextualFacet();
		tlc2.setFacetType(TLFacetType.CUSTOM);
		tlc2.setName("Custom2");
		CustomFacetNode cf2 = new CustomFacetNode(tlc2);
		ln2.addMember(cf2);
		cf2.setOwner(cf);
		// Then - base custom has new custom as child
		assertTrue("TLC2 must be child of tlc.", cf.getChildrenHandler().getChildren_TL().contains(tlc2));
		// Then - custom facet is correct
		assertTrue(ln2.getDescendants_ContextualFacets().contains(cf2));
		assertTrue(cf2.getParent() instanceof NavNode);
		assertTrue(cf2.canBeLibraryMember());
		assertTrue(cf2.getLibrary() == ln2);
		assertTrue("Must have contributed location.", cf.getWhereContributed() != null);
		// Then - contributed node is correct
		ContributedFacetNode contrib2 = cf2.getWhereContributed();
		assertTrue("Cf2 must have where contributed.", contrib2 != null);
		assertTrue("Contrib2 must be child of cf.", cf.getChildren().contains(contrib2));
		assertTrue("Custom Facet must find contributed facet.", cf.getContributedFacet(tlc2) == contrib2);
		assertTrue("Contributed must have an owning component", contrib2.getOwningComponent() == cf);
		assertTrue("Owner must be parent.", contrib2.getParent() == contrib2.getOwningComponent());

		//
		// Final check - make sure contributed is reported as a child of the bo
		//
		List<Node> kids = bo.getChildren();
		assertTrue("Business object must have contributed child.", kids.contains(contributed));
		LOGGER.debug("Done");

		OTM16Upgrade.otm16Enabled = false;
	}

	@Test
	public void Facets_BuildContextual_v16() {
		OTM16Upgrade.otm16Enabled = true;

		// Given - two libraries
		ln = ml.createNewLibrary(pc, "bc");
		assertTrue(ln.isEditable());
		LibraryNode ln2 = ml.createNewLibrary_Empty(ln.getNamespace(), "bc2", pc.getDefaultProject());
		assertTrue(ln2.isEditable());

		// Given - a business object in the first library
		BusinessObjectNode bo = null;
		for (LibraryMemberInterface n : ln.get_LibraryMembers())
			if (n instanceof BusinessObjectNode)
				bo = (BusinessObjectNode) n;
		bo.getChildrenHandler().clear();

		// Given - a tlContextualFacet contributing to the bo in lib2
		TLContextualFacet tlc = new TLContextualFacet();
		tlc.setFacetType(TLFacetType.CUSTOM);
		tlc.setName("Custom1");
		CustomFacetNode cf = new CustomFacetNode(tlc);
		ln2.addMember(cf);
		cf.setOwner(bo);
		bo.addFacet("Q1", TLFacetType.QUERY);
		bo.getTLModelObject().addCustomFacet(tlc);
		// ln2.getTLModelObject().addNamedMember(tlc);
		assertTrue(tlc.getName().contains("Custom1"));

		// When - modeled by getting the children of BO
		//
		List<Node> kids = bo.getChildren();
		// Then - contributed node is correct
		ContributedFacetNode contributed = null;
		for (Node n : bo.getChildren())
			if (n instanceof ContributedFacetNode)
				if (n.getName().contains("Custom1"))
					contributed = (ContributedFacetNode) n;
		assertTrue(contributed != null);
		assertTrue(contributed.getLibrary() == ln2);
		// Then ???
		TLModelElement tl1 = contributed.getTLModelObject();
		Node tln = Node.GetNode(contributed.getTLModelObject());

		// Then - contributor is in different library
		assertTrue(contributed.getContributor() != null);
		CustomFacetNode custom = (CustomFacetNode) contributed.getContributor();
		assertTrue(custom.getLibrary() == ln2);
		// Then ???
		TLModelElement tl2 = custom.getTLModelObject();
		Node tlcu = Node.GetNode(custom.getTLModelObject());

		LOGGER.debug("TODO");
		// When - contributor is contributed to
		//
	}

	@Test
	public void Facets_OTM16EnabledTests() {
		OTM16Upgrade.otm16Enabled = true;

		// Given libraries loaded from file that contain contextual facets
		// NOTE: load order is important if the compiler is to resolve contributors
		lf.loadFile_Facets1(defaultProject);
		lf.loadFile_Facets2(defaultProject);
		LibraryNode baseLib = lf.loadFile_FacetBase(defaultProject);

		// Then libraries must have contextual facets and those facets must have owners.
		for (LibraryNode ln : defaultProject.getLibraries()) {
			List<ContextualFacetNode> facets = getContextualFacets(ln);
			assertTrue("Must have some contextual facets.", !facets.isEmpty());
			for (ContextualFacetNode cf : facets) {
				assertTrue("Must have owner.", cf.getOwningComponent() != null);
			}
		}

		// Then - check contributions to objects in the base library
		//
		// I am seeing 15 in test and DE: 6 local, 9 contributed to the 6 local facets
		checkFacetContents(baseLib, "FacetTestBO", 6, 9);

		// // extended BO must have 4 contextual facets as children and no inherited
		// 3 local, 0
		checkFacetContents(baseLib, "ExtFacetTestBO", 3, 0);

		// // FacetTestChoice must have 2 choice facets. One from base library and one local and one from lib1
		// 2 local, 3 contributed
		checkFacetContents(baseLib, "FacetTestChoice", 2, 3);

		// // extended choice must have 4 contextual facets as children and no inherited
		// 1 local, 0
		checkFacetContents(baseLib, "ExtFacetTestChoice", 1, 0);

		// Then extensions must have inherited children
		for (Node n : baseLib.getDescendants_LibraryMembers()) {
			if (n instanceof ExtensionOwner)
				if (((ExtensionOwner) n).getExtensionBase() != null) {
					// Contextual facets should all have inherited properties
					for (Node cf : n.getChildren())
						if (cf instanceof ContextualFacetNode) {
							// List<?> moKids = ((FacetMO) cf.getModelObject()).getInheritedChildren();
							List<Node> iKids = cf.getInheritedChildren();
							// LOGGER.debug(cf + " has " + moKids.size() + " inherited model kids and " + iKids.size()
							// + " inherited kids.");
							assertTrue(!iKids.isEmpty());
							// assertTrue("Must have inherited children.", !cf.getInheritedChildren().isEmpty());
						}
				}
		}

		OTM16Upgrade.otm16Enabled = false;
	}

	private List<ContextualFacetNode> getContextualFacets(Node container) {
		ArrayList<ContextualFacetNode> facets = new ArrayList<ContextualFacetNode>();
		for (Node n : container.getDescendants())
			if (n instanceof ContextualFacetNode)
				facets.add((ContextualFacetNode) n);
		return facets;
	}

	/**
	 * Starting with the base node, find descendants with the target name. Those descendants must match the passed
	 * counts.
	 * 
	 * @param base
	 * @param targetName
	 * @param local
	 *            number of isLocal() contextual facets
	 * @param contributed
	 *            number of other facets (contributed)
	 */
	private void checkFacetContents(Node base, String targetName, int local, int contributed) {
		int localCnt = 0, contributedCnt = 0;
		int total = local + contributed;
		for (Node n : base.getDescendants_LibraryMembers())
			if (n.getName().equals(targetName)) {
				List<ContextualFacetNode> facets = getContextualFacets(n);
				assertTrue(targetName + " must have " + total + " contextual facets.", facets.size() == total);
				// assertTrue(targetName + " must not have inherited children.", n.getInheritedChildren().isEmpty());
				for (ContextualFacetNode cf : facets)
					// if (cf.isLocal())
					// localCnt++;
					// else
					contributedCnt++;
				assertTrue(targetName + " must have " + local + " local contextual facets.", localCnt == local);
				assertTrue(targetName + " must have " + contributed + " contributed facets.",
						contributedCnt == contributed);
			}
	}

	@Test
	public void Facets_Constructors_v16() {
		OTM16Upgrade.otm16Enabled = true;
		// Given a versioned library
		ln = ml.createNewLibrary("http://www.test.com/test1", "test1", defaultProject);
		// LibraryNode ln_inChain = ml.createNewLibrary("http://www.test.com/test1c", "test1c", defaultProject);
		// new LibraryChainNode(ln_inChain);
		// ln_inChain.setEditable(true);
		// assertTrue("Library must exist.", ln_inChain != null);

		BusinessObjectNode bo = null;
		assertTrue("Library must exist.", ln != null);
		for (LibraryMemberInterface n : ln.get_LibraryMembers())
			if (n instanceof BusinessObjectNode)
				bo = (BusinessObjectNode) n;
		assertTrue("Business Object must exist.", bo != null);

		// Given - a TLContextual facet used to create each of the contextual facets
		TLContextualFacet tlObj = new TLContextualFacet();
		ContextualFacetNode cf = null;
		ContextualFacetNode cfFactory = null;

		// When - created from constructor and factory
		tlObj.setFacetType(TLFacetType.CUSTOM);
		cf = new CustomFacetNode(tlObj);
		cfFactory = (ContextualFacetNode) NodeFactory.newLibraryMember(tlObj);
		// Then - objects exist.
		assertTrue("Must not be null.", cf != null);
		assertTrue(Node.GetNode(tlObj) == cf);
		assertTrue("Must not be null.", cfFactory != null);

		// Given - Set TLContextualFacet library and owning entity to fully exercise constructor

		// When - created from constructor and factory
		tlObj = buildTL(bo, TLFacetType.CUSTOM);
		cf = new CustomFacetNode(tlObj);
		cfFactory = (ContextualFacetNode) NodeFactory.newLibraryMember(tlObj);
		// Then - objects exist.
		assertTrue("Must not be null.", cf != null);
		assertTrue(Node.GetNode(tlObj) == cf);
		assertTrue("Must not be null.", cfFactory != null);
		assertTrue("Must know where contributed.", cf.getWhereContributed() != null);

		tlObj = buildTL(bo, TLFacetType.QUERY);
		cf = new QueryFacetNode(tlObj);
		cfFactory = (ContextualFacetNode) NodeFactory.newLibraryMember(tlObj);
		assertTrue(Node.GetNode(tlObj) == cf);
		assertTrue("Must not be null.", cf != null);
		assertTrue("Must not be null.", cfFactory != null);

		tlObj = new TLContextualFacet();
		tlObj.setFacetType(TLFacetType.CHOICE);
		cf = new ChoiceFacetNode(tlObj);
		cfFactory = (ContextualFacetNode) NodeFactory.newLibraryMember(tlObj);
		assertTrue("Must not be null.", cf != null);
		assertTrue("Must not be null.", cfFactory != null);

		tlObj = buildTL(bo, TLFacetType.UPDATE);
		cf = new UpdateFacetNode(tlObj);
		cfFactory = (ContextualFacetNode) NodeFactory.newLibraryMember(tlObj);
		assertTrue("Must not be null.", cf != null);
		assertTrue("Must not be null.", cfFactory != null);

		List<Node> kids = bo.getChildren();
		assertTrue(!kids.isEmpty());

		OTM16Upgrade.otm16Enabled = false;
	}

	private TLContextualFacet buildTL(BusinessObjectNode owner, TLFacetType type) {
		TLContextualFacet tlObj = new TLContextualFacet();
		tlObj.setFacetType(type);
		switch (type) {
		case CUSTOM:
			owner.getTLModelObject().addCustomFacet(tlObj);
			break;
		case QUERY:
			owner.getTLModelObject().addQueryFacet(tlObj);
			break;
		case UPDATE:
			owner.getTLModelObject().addUpdateFacet(tlObj);
			break;
		default:
			break;

		}
		// tlObj.setOwningEntity(owner.getTLModelObject());
		tlObj.setOwningLibrary(ln.getTLModelObject());
		assertTrue(tlObj.getOwningLibrary() != null);
		assertTrue(tlObj.getOwningEntity() != null);
		return tlObj;
	}

	@Test
	public void Facets_RoleTests() throws Exception {
		String myNS = "http://local/junits";
		// ln = ml.createNewLibrary("http://www.test.com/test1", "test1", defaultProject);
		ln = LibraryNodeBuilder.create("Example", myNS, "p", new Version(1, 1, 0)).build(defaultProject, pc);
		ln.setEditable(true);
		assertTrue("Library is minor version.", ln.isMinorVersion());

		CoreObjectNode core = ml.addCoreObjectToLibrary(ln, "Core1");
		checkFacet(core.getFacet_Role());
		List<Node> inheritedKids = core.getFacet_Role().getInheritedChildren();
		// TODO - make sure minor version has inherited children
	}

	// @Test
	// public void repositoryTestsNeedToBeMoved() throws RepositoryException {
	// String myNS = "http://local/junits";
	// DefaultRepositoryController rc = (DefaultRepositoryController) mc.getRepositoryController();
	// assertTrue("Repository controller must not be null.", rc != null);
	// assertTrue("Local repository must not be null.", rc.getLocalRepository() != null);
	// List<RepositoryNode> repos = rc.getAll();
	// RepositoryNode localRepoNode = rc.getLocalRepository();
	// LOGGER.debug("Repo namespace is ", rc.getLocalRepository().getNamespaceWithPrefix());
	// Repository localRepo = localRepoNode.getRepository();
	// List<String> repoRootNSs = localRepo.listRootNamespaces();
	// List<String> repoNSs = localRepo.listAllNamespaces();
	// List<String> repoBaseNSs = localRepo.listBaseNamespaces();
	// LOGGER.debug("Repo Root namespaces: ", repoRootNSs);
	// LOGGER.debug("Repo Base namespaces: ", repoBaseNSs);
	// LOGGER.debug("Repo All namespaces: ", repoNSs);
	// try {
	// localRepo.createRootNamespace(myNS);
	// } catch (Exception e) {
	// LOGGER.debug("Error setting Repo Root namespaces: ", e.getLocalizedMessage());
	// }
	// LOGGER.debug("Repo Root namespaces: ", localRepo.listRootNamespaces());
	//
	// // Given - a library in the local repo namespace
	// ln = ml.createNewLibrary(rc.getLocalRepository().getNamespace(), "test1r", defaultProject);
	// assertTrue("Library must not be null.", ln != null);
	// // When - managed
	// List<LibraryChainNode> lcns = rc.manage(rc.getLocalRepository(), Collections.singletonList(ln));
	// // Then
	// assertTrue("There must be library chains.", !lcns.isEmpty());
	// }

	public void checkFacet(ListFacetNode lf) {
		// LOGGER.debug("Checking List Facet Node: " + lf);

		if (lf.isSimpleListFacet()) {
			// LOGGER.debug("Simple List Facet Node");
			assertTrue(lf.getName().startsWith(lf.getOwningComponent().getName()));
			assertTrue(lf.getName().contains("Simple"));
			assertTrue(lf.getLabel().equals(ComponentNodeType.SIMPLE_LIST.getDescription()));
		}
		if (lf.isDetailListFacet()) {
			// LOGGER.debug("Detail List Facet Node");
			assertTrue(lf.getName().startsWith(lf.getOwningComponent().getName()));
			assertTrue(lf.getName().contains("Detail"));
			assertTrue(lf.getLabel().equals(ComponentNodeType.DETAIL_LIST.getDescription()));
		}
		assertFalse("Must NOT be delete-able.", lf.isDeleteable());
		assertFalse("Must NOT be valid parent.", lf.isValidParentOf(PropertyNodeType.ATTRIBUTE));
		assertFalse("Must NOT be valid parent.", lf.isValidParentOf(PropertyNodeType.ELEMENT));
		assertFalse("Must NOT be valid parent.", lf.isValidParentOf(PropertyNodeType.INDICATOR));
	}

	@Deprecated
	public void checkFacet(SimpleFacetNode sf) {
		// LOGGER.debug("Checking Simple Facet Node: " + sf);
		assertFalse("Must NOT be delete-able.", sf.isDeleteable());

		// assertTrue(sf.getModelObject() instanceof SimpleFacetMO);
		assertTrue(sf.getTLModelObject() instanceof TLSimpleFacet);
		assertTrue(sf.getLibrary() == sf.getParent().getLibrary());

		if (sf.getOwningComponent() instanceof VWA_Node) {
			assertTrue("Simple facet must have 1 child.", sf.getChildren().size() == 1);
			Node sp = sf.getChildren().get(0);
			assertTrue(sp instanceof PropertyNode);
			// assertTrue(sp.getModelObject() instanceof SimpleAttributeMO);
			assertTrue(sp.getType() != null);
			assertTrue(!sp.getType().getName().isEmpty());
			assertTrue(sp.getLibrary() == sf.getLibrary());
		}
	}

	public void checkFacet(SimpleFacetFacadeNode sf) {
		// LOGGER.debug("Checking Simple Facet Facade Node: " + sf);
		assertFalse("Must NOT be delete-able.", sf.isDeleteable());

		assertTrue(sf.getTLModelObject() != null);
		assertTrue(sf.getLibrary() == sf.getParent().getLibrary());

		assertTrue(sf.getSimpleAttribute() instanceof SimpleAttributeFacadeNode);
		if (sf.getSimpleAttribute().getAssignedType() == null)
			LOGGER.debug("ERROR " + sf.getSimpleAttribute().getAssignedType());
		assertTrue(sf.getSimpleAttribute().getAssignedType() != null); // could be empty
	}

	public void checkFacet(AttributeFacetNode vf) {
		// LOGGER.debug("Checking VWA Attribute Facet Node: " + vf);

		assertTrue("Must be valid parent of attribute.", vf.isValidParentOf(PropertyNodeType.ATTRIBUTE));
		assertTrue("Must be valid parent of indicator.", vf.isValidParentOf(PropertyNodeType.INDICATOR));
		assertTrue("Must be valid parent of id.", vf.isValidParentOf(PropertyNodeType.ID));

		assertFalse("Must NOT be delete-able.", vf.isDeleteable());
		assertFalse("Must NOT be type provider.", vf.isNamedEntity());
		assertFalse("Must NOT be assignable.", vf.isAssignable());
		assertFalse("Must NOT be valid parent of element.", vf.isValidParentOf(PropertyNodeType.ELEMENT));
		assertFalse("Must NOT be valid parent of ID Reference.", vf.isValidParentOf(PropertyNodeType.ID_REFERENCE));
		assertFalse("Must NOT be valid parent of indicator element.",
				vf.isValidParentOf(PropertyNodeType.INDICATOR_ELEMENT));

		// Behaviors
		if (vf.getOwningComponent().isEditable()) {
			// Given - a list with a new property
			List<Node> properties = new ArrayList<Node>();
			PropertyNode p = new AttributeNode(new TLAttribute(), null);
			p.setName("attr1");
			properties.add(p);
			// When - list is added to attribute facet
			vf.addProperties(properties, false);
			// Then
			assertTrue("Must have new property as child.", vf.getChildren().contains(p));
		}
	}

	/**
	 * WARNING - ADDS Properties!!!
	 * 
	 * @param rf
	 */
	public void check(PropertyOwnerNode rf) {
		if (rf instanceof ChoiceFacetNode)
			checkFacet((ChoiceFacetNode) rf);
		else if (rf instanceof CustomFacetNode)
			checkFacet((CustomFacetNode) rf);
		else if (rf instanceof ListFacetNode)
			checkFacet((ListFacetNode) rf);
		else if (rf instanceof OperationNode)
			checkFacet((OperationNode) rf);
		else if (rf instanceof OperationFacetNode)
			checkFacet((OperationFacetNode) rf);
		else if (rf instanceof QueryFacetNode)
			checkFacet((QueryFacetNode) rf);
		else if (rf instanceof QueryFacetNode)
			checkFacet((QueryFacetNode) rf);
		else if (rf instanceof RoleFacetNode)
			checkFacet((RoleFacetNode) rf);
		else if (rf instanceof SimpleFacetNode)
			checkFacet((SimpleFacetNode) rf);
		else if (rf instanceof SimpleFacetFacadeNode)
			checkFacet((SimpleFacetFacadeNode) rf);
		else if (rf instanceof UpdateFacetNode)
			checkFacet((UpdateFacetNode) rf);
		else if (rf instanceof AttributeFacetNode)
			checkFacet((AttributeFacetNode) rf);
		else
			checkBaseFacet(rf);
	}

	public void checkFacet(OperationNode sf) {
		// LOGGER.debug("Checking Operation Node: " + sf);
		// assertTrue("Must be delete-able.", sf.isDeleteable());
	}

	public void checkFacet(OperationFacetNode of) {
		// LOGGER.debug("Checking Operation Facet Node: " + of);

		assertTrue("Must be delete-able.", of.isDeleteable());
		assertFalse("Must NOT be type provider.", of.isNamedEntity());
		assertFalse("Must NOT be assignable.", of.isAssignable());
	}

	public void checkBaseFacet(PropertyOwnerNode fn) {
		assertTrue("Must have owning library.", fn.getLibrary() != null);
		if (fn instanceof ContextualFacetNode) {
			if (!OTM16Upgrade.otm16Enabled || fn instanceof ContributedFacetNode)
				assertTrue("Must have same owning library as TL object.",
						fn.getLibrary() == Node.GetNode(fn.getLibrary().getTLModelObject()));
		} else
			assertTrue("Must have same owning library as TL object.",
					fn.getLibrary() == Node.GetNode(fn.getLibrary().getTLModelObject()));

		if (!fn.isInherited())
			// Check children
			for (Node property : fn.getChildren()) {
				PropertyNodeTest pnTests = new PropertyNodeTest();
				if (fn instanceof ContributedFacetNode) {
					assertTrue(OTM16Upgrade.otm16Enabled);
					assertTrue(property.getParent() == ((ContributedFacetNode) fn).getContributor());
					continue;
				}
				if (property instanceof AliasNode)
					continue;
				if (property instanceof ContextualFacetNode) {
					assertTrue(OTM16Upgrade.otm16Enabled);
					if (property instanceof ContributedFacetNode)
						assertTrue(property.getParent() == fn);
					continue;
				}
				if (property instanceof PropertyNode) {
					assertTrue(property.getParent() == fn);
					pnTests.check((PropertyNode) property);
				} else {
					LOGGER.debug("ERROR - invalid property type: " + property + " "
							+ property.getClass().getSimpleName());
					assertTrue(false);
				}
			}

		assertTrue("Must be valid parent to attributes.", fn.isValidParentOf(PropertyNodeType.ATTRIBUTE));
		assertTrue("Must be valid parent to elements.", fn.isValidParentOf(PropertyNodeType.ELEMENT));

		if (fn.isIDFacet() || fn.isDetailFacet() || fn.isSummaryFacet())
			assertFalse("Must NOT be delete-able.", fn.isDeleteable());

		// TODO - property order between node children and TL
		//

		// Behaviors
		if (fn.getOwningComponent().isEditable()) {
			// Cached children are removed often. Use string at end of name to identify
			final String locatingSuffix = "ZZZXCXZ1234554321VCXZZXCV";
			final int StartingCount = fn.getChildren().size();

			new AttributeNode(fn, "att1x" + locatingSuffix);
			assertTrue("Must be plus 1 size.", fn.getChildren().size() == StartingCount + 1);
			new ElementNode(fn, "ele1x" + locatingSuffix);

			// Create array of attribute and element properties to add
			List<Node> properties = new ArrayList<Node>();
			AttributeNode attr2 = new AttributeNode(new TLAttribute(), null);
			ElementNode ele2 = new ElementNode(new TLProperty(), null);
			attr2.setName("att2x" + locatingSuffix);
			ele2.setName("ele2x" + locatingSuffix);
			properties.add(attr2);
			properties.add(ele2);
			// When - add using facet method with array param
			fn.addProperties(properties, false);

			// Then - get all children and make sure they exist
			// Then remove them - Note - delete will clear fn's children
			int found = 0;
			for (Node child : fn.getChildren_New()) {
				if (child.getName().endsWith(locatingSuffix)) {
					found++;
					child.delete();
					if (fn.contains(child))
						child.delete();
					assertTrue("Must be able to delete added child.", !fn.contains(child));
				}
			}
			assert found == 4;

			fn.getChildren();
			assertTrue("Must be same size as when starting.", fn.getChildren().size() == StartingCount);
		}
	}

	public void checkFacet(QueryFacetNode qn) {
		// LOGGER.debug("Checking Query Facet: " + qn);
		checkBaseFacet(qn);
		if (qn.getOwningComponent().isEditable())
			assertTrue("Must be delete-able.", qn.isDeleteable());
	}

	public void checkFacet(ContextualFacetNode cf) {
		// LOGGER.debug("Checking Contextual Facet: " + rf);
		checkBaseFacet(cf);

		// setName()
		//
		final String NEWNAME = "myName";
		final String oldName = cf.getName();
		assertTrue("Must be renamable.", cf.isRenameable());
		cf.setName(NEWNAME);
		String n = cf.getName();
		assertTrue("Facet must contain new name.",
				cf.getName().contains(NodeNameUtils.fixContextualFacetName(cf, NEWNAME)));
		cf.setName(oldName);

		// Inherited statements
		//
		assertTrue("Must be assignable.", cf.isAssignable());
		assertTrue("Must be assignable to complex.", cf.isComplexAssignable());
		assertTrue("Must be valid parent to attributes.", cf.isValidParentOf(PropertyNodeType.ATTRIBUTE));
		assertTrue("Must be valid parent to elements.", cf.isValidParentOf(PropertyNodeType.ELEMENT));
		if (OTM16Upgrade.otm16Enabled)
			assertTrue("Must be named entity.", cf.isNamedEntity());
		else
			assertFalse("Must NOT be named entity.", cf.isNamedEntity());

		assertFalse("Must NOT be assignable to element ref", cf.isAssignableToElementRef());
		assertFalse("Must NOT be assignable to simple.", cf.isAssignableToSimple());
		assertFalse("Must NOT be assignable to simple.", cf.isSimpleAssignable());
		assertFalse("Must NOT be assignable to VWA.", cf.isAssignableToVWA());
		assertFalse("Must NOT be default facet.", cf.isDefaultFacet());
		if (OTM16Upgrade.otm16Enabled == false)
			assertFalse("Must NOT be named type.", cf.isNamedEntity());

		// Behaviors
		//
		AttributeNode attr = new AttributeNode(cf, "att1");
		ElementNode ele = new ElementNode(cf, "ele1");
		assertTrue("Must be able to add attributes.", attr.getParent() == cf);
		assertTrue("Must be able to add elements.", ele.getParent() == cf);
		assertTrue(cf.getChildren().contains(ele));
		attr.delete();
		ele.delete();
		assertFalse(cf.getChildren().contains(attr));
		assertFalse(cf.getChildren().contains(ele));

		// relationships
		//
		if (OTM16Upgrade.otm16Enabled) {
			// Contributed/contextual relationship
			ContributedFacetNode contrib = cf.getWhereContributed();
			assertTrue(contrib.getContributor() == cf);
			assertTrue(contrib.getOwningComponent() instanceof ContextualFacetOwnerInterface);
			assertTrue(contrib.getOwningComponent().getChildren().contains(contrib));
		}

		if (cf instanceof QueryFacetNode)
			checkFacet((QueryFacetNode) cf);
		else if (cf instanceof CustomFacetNode)
			checkFacet((CustomFacetNode) cf);
		else if (cf instanceof UpdateFacetNode)
			checkFacet((UpdateFacetNode) cf);
		else if (cf instanceof ChoiceFacetNode)
			checkFacet((ChoiceFacetNode) cf);
	}

	public void checkFacet(UpdateFacetNode qn) {
		LOGGER.debug("Checking Update Contextual Facet: " + qn);
		if (qn.getOwningComponent().isEditable())
			assertTrue("Must be delete-able.", qn.isDeleteable());
	}

	public void checkFacet(CustomFacetNode qn) {
		// LOGGER.debug("Checking Custom Facet: " + qn);
		checkBaseFacet(qn);
		if (qn.getOwningComponent().isEditable())
			assertTrue("Must be delete-able.", qn.isDeleteable());
	}

	public void checkFacet(ChoiceFacetNode qn) {
		// LOGGER.debug("Checking Facet: " + qn);
		if (!qn.isInherited() && qn.getParent().isDeleteable())
			assertTrue("Must be delete-able.", qn.isDeleteable());
		checkBaseFacet(qn);
	}

	public void checkFacet(RoleFacetNode roleFacetNode) {
		// LOGGER.debug("Checking Facet: " + roleFacetNode);

		assertTrue("Must be role facet.", roleFacetNode instanceof RoleFacetNode);
		// assertTrue("Must be delete-able.", roleFacet.isDeleteable());
		assertTrue("Must be assignable.", roleFacetNode.isAssignable());
		assertTrue("Must be assignable to complex.", roleFacetNode.isComplexAssignable());
		assertTrue("Must be type provider.", roleFacetNode.isNamedEntity());
		assertTrue("Must be valid parent to roles.", roleFacetNode.isValidParentOf(PropertyNodeType.ROLE));
		// FIXME - assertTrue("Must be assignable to VWA.", roleFacet.isAssignableToVWA());

		assertFalse("Must NOT be assignable to simple.", roleFacetNode.isAssignableToSimple());
		assertFalse("Must NOT be assignable to simple.", roleFacetNode.isSimpleAssignable());
		assertFalse("Must NOT be valid parent to attributes.",
				roleFacetNode.isValidParentOf(PropertyNodeType.ATTRIBUTE));
		assertFalse("Must NOT be valid parent to elements.", roleFacetNode.isValidParentOf(PropertyNodeType.ELEMENT));
		assertFalse("Must NOT be renamable.", roleFacetNode.isRenameable());

		assertFalse("Must NOT be assignable to element ref", roleFacetNode.isAssignableToElementRef());
		assertFalse("Must NOT be default facet.", roleFacetNode.isDefaultFacet());
		assertFalse("Must NOT be named type.", !roleFacetNode.isNamedEntity());

		// Behaviors
		RoleNode role = new RoleNode(roleFacetNode, "newRole1");
		assertTrue("Must be able to add roles.", role.getParent() == roleFacetNode);
		assertTrue("Must be able to add child.", roleFacetNode.getChildren().contains(role));
		role.delete();
	}
}
