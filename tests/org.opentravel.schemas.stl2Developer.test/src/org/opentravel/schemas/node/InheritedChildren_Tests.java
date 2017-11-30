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

import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemacompiler.codegen.util.FacetCodegenUtils;
import org.opentravel.schemacompiler.event.ModelElementListener;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.util.OTM16Upgrade;
import org.opentravel.schemas.controllers.DefaultProjectController;
import org.opentravel.schemas.controllers.MainController;
import org.opentravel.schemas.node.facets.ContextualFacetNode;
import org.opentravel.schemas.node.facets.ContributedFacetNode;
import org.opentravel.schemas.node.facets.FacetNode;
import org.opentravel.schemas.node.interfaces.FacadeInterface;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.properties.ElementNode;
import org.opentravel.schemas.node.properties.InheritedElementNode;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.testUtils.LoadFiles;
import org.opentravel.schemas.testUtils.MockLibrary;
import org.opentravel.schemas.testUtils.NodeTesters;
import org.opentravel.schemas.testUtils.NodeTesters.TestNode;
import org.opentravel.schemas.types.TypeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dave Hollander
 * 
 * @see ChoiceObjectTests#ChoiceFacetsTests()
 * 
 */
public class InheritedChildren_Tests {
	private final static Logger LOGGER = LoggerFactory.getLogger(ComponentNode.class);

	ModelNode model = null;
	TestNode tn = new NodeTesters().new TestNode();
	LoadFiles lf = new LoadFiles();
	Library_FunctionTests lt = new Library_FunctionTests();
	MockLibrary ml = null;
	LibraryNode ln = null;
	MainController mc;
	DefaultProjectController pc;
	ProjectNode defaultProject;
	BusinessObjectNode baseBO, extensionBO;

	@Before
	public void beforeAllTests() {
		mc = new MainController();
		ml = new MockLibrary();
		pc = (DefaultProjectController) mc.getProjectController();
		defaultProject = pc.getDefaultProject();
		ln = ml.createNewLibrary(defaultProject.getNSRoot(), "test", defaultProject);
	}

	public void check(InheritedElementNode ie) {
		PropertyNode baseElement = (PropertyNode) ie.get();
		assertTrue("Must be inherited", ie.isInherited());
		assertTrue("Must inherit from base", ie.getInheritedFrom() == baseElement);
		assertTrue("Must not have same parent.", ie.getParent() != baseElement.getParent());
		assertTrue("Must have same name.", ie.getName().equals(baseElement.getName()));
		assertTrue("Must have same type.", ie.getAssignedType() == baseElement.getAssignedType());
		assertTrue("Must have same TLModelObject.", ie.getTLModelObject() == baseElement.getTLModelObject());
	}

	public void setUpExtendedBO() {
		// Given -- two business objects. extensionBO extends baseBO
		//
		baseBO = ml.addBusinessObjectToLibrary(ln, "BaseBO");
		// If a fully populated BO is used it will be invalid because of too many id attributes
		extensionBO = ml.addBusinessObjectToLibrary_Empty(ln, "ExtensionBO");
		extensionBO.setExtension(baseBO);
		assertTrue("Must extension must extend base.", !extensionBO.getExtendsTypeName().isEmpty());
		assertTrue("Base where extended must contain extension.", baseBO.getWhereExtendedHandler().getWhereExtended()
				.contains(extensionBO));
		assertTrue("Extension base type must be baseBO.", extensionBO.getExtensionBase() == baseBO);
	}

	@Test
	public void IC_addCustomToBase_Test() {
		// Given extensionBO extends baseBO
		setUpExtendedBO();

		// Then - inherited children
		List<Node> inherited = extensionBO.getFacet_Summary().getInheritedChildren();
		Assert.assertFalse(inherited.isEmpty());
		assertTrue("All base properties must be inherited.",
				baseBO.getFacet_Summary().getChildren().size() == inherited.size());
		Assert.assertTrue(inherited.get(0).isInherited());

		baseBO.addFacet("C2", TLFacetType.CUSTOM);
		List<TLContextualFacet> inf = FacetCodegenUtils.findGhostFacets(extensionBO.getTLModelObject(),
				TLFacetType.CUSTOM);
		assertTrue("Ghosts must be found.", !inf.isEmpty());
		List<Node> iKids = extensionBO.getInheritedChildren();
		assertTrue("extended object has inherited children.", !extensionBO.getInheritedChildren().isEmpty());

		LOGGER.debug("Done");
	}

	@Test
	public void IC_settingBase_Test() {
		setUpExtendedBO();
		assertTrue("Library must be valid.", ln.isValid()); // validates TL library
		BusinessObjectNode bo2 = ml.addBusinessObjectToLibrary_Empty(ln, "Bo2");
		FacetNode sf = bo2.getFacet_Summary();
		List<?> children = sf.getChildren();
		bo2.setExtension(baseBO);
		Assert.assertEquals(sf, bo2.getFacet_Summary());
		List<?> inherited = sf.getInheritedChildren();
		LOGGER.debug("Done");
	}

	@Test
	public void IC_inheritedElementNodeTests() {
		// Given - two BO, baseBO and extensionBO
		setUpExtendedBO();
		ElementNode baseElement = new ElementNode(new TLProperty(), baseBO.getFacet_Summary());
		TypeProvider simpleType = ml.getSimpleTypeProvider();
		baseElement.setAssignedType(simpleType);
		baseElement.setName("Ele1");
		String elementName = baseElement.getName();

		// Constructor
		PropertyNode ie = new InheritedElementNode(baseElement, extensionBO.getFacet_Summary());
		assertTrue("Must inherit from base", ((FacadeInterface) ie).get() == baseElement);
		assertTrue("Must have correct parent.", ie.getParent() == extensionBO.getFacet_Summary());
		assertTrue("Must have same name.", ie.getName().equals(elementName));
		assertTrue("Must have same type.", ie.getAssignedType() == simpleType);
		ml.check(ie);

		// Factory
		ie = NodeFactory.newInheritedProperty(baseElement, extensionBO.getFacet_Summary());
		ml.check(ie);
	}

	@Test
	public void IC_inheritedPropertiesTests() {
		// Given - two BO, baseBO and extensionBO
		setUpExtendedBO();

		// Children handler
		List<Node> fromKids = baseBO.getFacet_Summary().getChildren();
		List<Node> kids = extensionBO.getFacet_Summary().getInheritedChildren();
		assertTrue("Must inherit all properties.", fromKids.size() == kids.size());
		Collection<ModelElementListener> listeners = baseBO.getFacet_Summary().getTLModelObject().getListeners();
		assertTrue(listeners.size() > 0);

		for (Node n : kids)
			if (n instanceof InheritedElementNode)
				check((InheritedElementNode) n);

		// When more properties are added
		ml.addAllProperties(baseBO.getFacet_Summary());
		// extensionBO.getFacet_Summary().getChildrenHandler().clear(); // should not be needed
		List<Node> nKids = extensionBO.getFacet_Summary().getInheritedChildren();
		assert !nKids.isEmpty();
		// Then kids all check OK
		for (Node n : nKids)
			if (n instanceof InheritedElementNode)
				check((InheritedElementNode) n);
		assertTrue("Must have more inherited properties.", nKids.size() > kids.size());
	}

	@Test
	public void IC_inheritedContextualFacet_v15_Tests() {
		OTM16Upgrade.otm16Enabled = false;
		// Given - two BO, baseBO and extensionBO
		setUpExtendedBO();
		List<ContextualFacetNode> baseCFs = baseBO.getContextualFacets();
		List<ContributedFacetNode> baseContribs = baseBO.getContributedFacets();
		assertTrue("Version 1.5 must not use contributed facets.", baseContribs.isEmpty());
		List<Node> bKids = baseBO.getChildren();
		List<ContextualFacetNode> eCFs = extensionBO.getContextualFacets();
		List<Node> eKids = extensionBO.getInheritedChildren();

		// Then
		assertTrue("Must have inherited children.", !eKids.isEmpty());
		for (Node cf : eKids) {
			Node baseNode = baseBO.findChildByName(((ContextualFacetNode) cf).getTLModelObject().getName());
			assertTrue("Must have different TL objects.", cf.getTLModelObject() != baseNode.getTLModelObject());
		}
	}

	@Test
	public void IC_inheritedContextualFacet_v16_Tests() {
		OTM16Upgrade.otm16Enabled = true;
		// Given - two BO, baseBO and extensionBO
		setUpExtendedBO();
		List<ContextualFacetNode> baseCFs = baseBO.getContextualFacets();
		List<ContributedFacetNode> baseContribs = baseBO.getContributedFacets();
		List<Node> bKids = baseBO.getChildren();
		assertTrue("Version 1.6 must use contributed facets.", !baseContribs.isEmpty());

		List<ContextualFacetNode> eCFs = extensionBO.getContextualFacets();
		List<ContributedFacetNode> eContribs = extensionBO.getContributedFacets();
		List<Node> eKids = extensionBO.getInheritedChildren();

		assertTrue("Library must now have these inherited facets.", true);

		assertTrue(!eKids.isEmpty());
		OTM16Upgrade.otm16Enabled = false;
	}

	@Test
	public void IC_facetsFiles_v16_Tests() {
		OTM16Upgrade.otm16Enabled = true;

		LibraryNode baseLN = lf.loadFile_FacetBase(defaultProject);
		LibraryNode f1LN = lf.loadFile_Facets1(defaultProject);
		LibraryNode f2LN = lf.loadFile_Facets2(defaultProject);
		ml.check(baseLN);
		ml.check(f1LN);
		ml.check(f2LN);
		baseLN.setEditable(true);
		f1LN.setEditable(true);
		f2LN.setEditable(true);
		assert baseLN.isEditable();
		assert f1LN.isEditable();

		final String EXCHOICE = "ExtFacetTestChoice";
		final String EXBO = "ExtFacetTestBO";
		ChoiceObjectNode ch = (ChoiceObjectNode) baseLN.findLibraryMemberByName(EXCHOICE);
		BusinessObjectNode bo = (BusinessObjectNode) baseLN.findLibraryMemberByName(EXBO);
		ml.check(ch);
		ml.check(bo);
		ChoiceObjectNode chBase = (ChoiceObjectNode) ch.getExtensionBase();
		BusinessObjectNode boBase = (BusinessObjectNode) bo.getExtensionBase();
		assertTrue("Choice must extend base choice.", chBase != null);
		assertTrue("Business object must extend base object.", boBase != null);

		List<ContextualFacetNode> cfs = baseLN.getDescendants_ContextualFacets();
		for (Node cf : ch.getInheritedChildren())
			assertTrue(cf.isInherited());
		for (Node cf : bo.getInheritedChildren())
			assertTrue(cf.isInherited());
		cfs = baseLN.getDescendants_ContextualFacets();

		OTM16Upgrade.otm16Enabled = false;

	}
}
